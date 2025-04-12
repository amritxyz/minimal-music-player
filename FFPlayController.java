import javax.swing.*;
import java.io.*;
import java.util.concurrent.*;

public class FFPlayController {
    private File file; // The file to be played
    private Process ffplayProcess;
    private boolean isPlaying = false;
    private double fileLength = 0; // Duration of the file
    private final int UPDATE_INTERVAL = 1000; // Update progress every second
    private JProgressBar progressBar;
    private ExecutorService executorService;

    public FFPlayController(JProgressBar progressBar) {
        this.progressBar = progressBar;
        // Add shutdown hook to kill ffplay on exit
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        executorService = Executors.newSingleThreadExecutor();
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean hasFile() {
        return file != null;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void play() {
        if (file == null) return;

        try {
            // Start ffplay process
            ffplayProcess = new ProcessBuilder("ffplay", "-nodisp", "-autoexit", file.getAbsolutePath())
                    .redirectErrorStream(true)
                    .start();

            // Get file length (for progress bar)
            fileLength = getFileLength(); // Get length based on the current file

            // Start a new thread to update the progress bar every second
            executorService.submit(this::updateProgress);

            isPlaying = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void stop() {
        if (ffplayProcess != null && ffplayProcess.isAlive()) {
            ffplayProcess.destroy();
            try {
                ffplayProcess.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        isPlaying = false;
    }

    private void updateProgress() {
        while (isPlaying && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(UPDATE_INTERVAL); // Wait for the next second
                if (ffplayProcess != null) {
                    // Get the current position from ffplay output
                    String progressOutput = getProgressOutput();
                    if (progressOutput != null) {
                        double currentTime = parseCurrentTime(progressOutput);
                        SwingUtilities.invokeLater(() -> {
                            int progress = (int) ((currentTime / fileLength) * 100);
                            progressBar.setValue(progress);
                        });
                    }
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String getProgressOutput() {
        // Read the ffplay error stream to get progress data
        if (ffplayProcess == null) return null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ffplayProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("time=")) {
                    return line; // This line contains the current time of playback
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private double parseCurrentTime(String output) {
        // Example ffplay output: "time=00:00:30.00"
        String timeStr = output.split("time=")[1].split(" ")[0];
        String[] timeParts = timeStr.split(":");
        double minutes = Double.parseDouble(timeParts[0]);
        double seconds = Double.parseDouble(timeParts[1]);
        return (minutes * 60) + seconds;
    }

    private void updateProgressBar(double currentTime) {
        SwingUtilities.invokeLater(() -> {
            int progress = (int) ((currentTime / fileLength) * 100);
            progressBar.setValue(progress);
        });
    }

    public void seek(double positionInSeconds) {
        if (ffplayProcess != null && ffplayProcess.isAlive()) {
            // Send a seek command to ffplay using the process input
            try {
                String seekCommand = String.format("seek %.2f\n", positionInSeconds);
                OutputStream os = ffplayProcess.getOutputStream();
                os.write(seekCommand.getBytes());
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setVolume(int volume) {
        if (ffplayProcess != null && ffplayProcess.isAlive()) {
            try {
                String volumeCommand = String.format("volume %d\n", volume);
                OutputStream os = ffplayProcess.getOutputStream();
                os.write(volumeCommand.getBytes());
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public double getFileLength() {
        if (file == null) return 0;

        // Use ffprobe to get the duration of the file
        try {
            Process ffprobe = new ProcessBuilder("ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", file.getAbsolutePath())
                .start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(ffprobe.getInputStream()));
            String line = reader.readLine();
            return line != null ? Double.parseDouble(line) : 0;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
