import javax.swing.*;
import java.io.File;

public class MinimalMusicPlayer {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            MusicPlayerUI playerUI = new MusicPlayerUI();
            FileSelector fileSelector = new FileSelector();
            File selectedFile = fileSelector.selectFile(playerUI); // Let user select a file
            FFPlayController controller = playerUI.getFFPlayController(); // Access the FFPlayController

            if (selectedFile != null) {
                controller.setFile(selectedFile);  // Set the file for playback
                controller.play();  // Start playing the file
            }
        });
    }
}
