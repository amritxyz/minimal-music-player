import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class FileSelector {
    public File selectFile(JFrame parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a music file");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(true);

        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Audio Files", "mp3", "wav", "flac", "ogg"
        );
        chooser.setFileFilter(filter);

        int result = chooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }
}
