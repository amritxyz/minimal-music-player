import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class MusicPlayerUI extends JFrame {
    private final JButton selectButton;
    private final JButton playPauseButton;
    private final FFPlayController player;
    private final FileSelector fileSelector;
    private final JProgressBar progressBar;
    private final JSlider volumeSlider;

    public MusicPlayerUI() {
        super("Minimal Music Player");
        setSize(300, 200);  // Increase window size for volume slider
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.BLACK);
        setLayout(new FlowLayout());

        selectButton = new JButton("🎵 Select Music");
        playPauseButton = new JButton("▶️ Play");
        progressBar = new JProgressBar(0, 100);
        volumeSlider = new JSlider(0, 100, 100);  // Volume from 0 to 100

        player = new FFPlayController(progressBar);
        fileSelector = new FileSelector();

        styleButton(selectButton);
        styleButton(playPauseButton);

        progressBar.setPreferredSize(new Dimension(250, 20)); // Set progress bar size
        progressBar.setValue(0);
        volumeSlider.setPreferredSize(new Dimension(250, 20));

        add(selectButton);
        add(playPauseButton);
        add(progressBar);
        add(new JLabel("Volume:"));
        add(volumeSlider);

        selectButton.addActionListener(e -> {
            var selected = fileSelector.selectFile(this);
            if (selected != null) {
                player.stop();
                player.setFile(selected);
                player.play();
                playPauseButton.setText("⏸ Pause");
            }
        });

        playPauseButton.addActionListener(e -> {
            if (!player.hasFile()) return;

            if (player.isPlaying()) {
                player.stop();
                playPauseButton.setText("▶️ Play");
            } else {
                player.play();
                playPauseButton.setText("⏸ Pause");
            }
        });

        // Add mouse listener for seeking
        progressBar.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int barWidth = progressBar.getWidth();
                int clickPosition = e.getX();
                double newPosition = ((double) clickPosition / barWidth) * player.getFileLength();
                player.seek(newPosition);
            }
        });

        // Add volume control listener
        volumeSlider.addChangeListener(e -> {
            int volume = volumeSlider.getValue();
            setVolumeUsingWpctl(volume);  // Use wpctl to control the volume
        });

        setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setForeground(Color.WHITE);
        button.setBackground(Color.DARK_GRAY);
    }

    // Method to set the volume using wpctl
    private void setVolumeUsingWpctl(int volume) {
        try {
            // Run wpctl command to set the volume (you might need to adjust the device name)
            String command = String.format("wpctl set-volume @DEFAULT_AUDIO_DEVICE@ %d%%", volume);
            Process process = new ProcessBuilder("bash", "-c", command).start();
            process.waitFor();
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public FFPlayController getFFPlayController() {
        return player;  // 'player' is the instance of FFPlayController in MusicPlayerUI
    }

}
