import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;

public class App extends JFrame {
    private JButton compressButton, decompressButton;
    private JProgressBar progressBar;

    public App() {
        super("File Compressor/Decompressor");
        initComponents();
    }

    private void initComponents() {
        // Create buttons and progress bar
        compressButton = createStyledButton("Compress");
        decompressButton = createStyledButton("Decompress");
        progressBar = new JProgressBar();

        // Set up the layout
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(compressButton);
        buttonPanel.add(decompressButton);

        add(buttonPanel, BorderLayout.NORTH);
        add(progressBar, BorderLayout.CENTER);

        // Attach event listeners to buttons
        compressButton.addActionListener(e -> compressFiles());
        decompressButton.addActionListener(e -> decompressFiles());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null);
        setResizable(true);
        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        // Helper method to create styled buttons
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(52, 152, 219)); // Button color
        button.setFocusPainted(false);
        return button;
    }

    private void compressFiles() {
        // Choose files to compress
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] filesToProcess = fileChooser.getSelectedFiles();
            
            // Choose destination directory for compressed files
            File destinationDirectory = chooseDestinationDirectory();

            if (destinationDirectory != null) {
                // Perform compression in the background
                performFileOperation(filesToProcess, destinationDirectory, true);
            }
        }
    }

    private void decompressFiles() {
        // Choose file to decompress
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File fileToDecompress = fileChooser.getSelectedFile();
            if (isZipFile(fileToDecompress)) {
                // Perform decompression in the background
                performFileOperation(new File[]{fileToDecompress}, fileToDecompress.getParentFile(), false);
            } else {
                JOptionPane.showMessageDialog(this, "Selected file is not a valid compressed file.");
            }
        }
    }

    private File chooseDestinationDirectory() {
        // Choose destination directory for compressed files
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = directoryChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return directoryChooser.getSelectedFile();
        }
        return null;
    }

    private void performFileOperation(File[] files, File destination, boolean isCompression) {
        // SwingWorker to perform file operations in the background
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    int totalFiles = files.length;
                    int currentFile = 0;

                    for (File file : files) {
                        // Simulate file processing delay
                        Thread.sleep(500);

                        if (isCompression) {
                            // Compress file
                            compressFile(file, destination);
                        } else {
                            // Decompress file
                            decompressFile(file, destination);
                        }

                        currentFile++;
                        int progress = (int) ((double) currentFile / totalFiles * 100);
                        publish(progress); // Update progress bar
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                // Update progress bar
                for (Integer progress : chunks) {
                    progressBar.setValue(progress);
                }
            }

            @Override
            protected void done() {
                String operationType = isCompression ? "Compression" : "Decompression";
                JOptionPane.showMessageDialog(App.this, operationType + " done!");
                progressBar.setValue(0);
            }
        };

        worker.execute();
    }

    private void compressFile(File inputFile, File destination) {
        try (FileOutputStream fos = new FileOutputStream(new File(destination, inputFile.getName() + ".zip"));
             ZipOutputStream zipOut = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(inputFile)) {

            // Compress file and save with ".zip" extension
            ZipEntry zipEntry = new ZipEntry(inputFile.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void decompressFile(File inputFile, File destination) {
        try (FileInputStream fis = new FileInputStream(inputFile);
             ZipInputStream zipIn = new ZipInputStream(fis)) {

            // Decompress file
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = new File(destination, entry.getName()).getPath();
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = zipIn.read(bytes)) >= 0) {
                        fos.write(bytes, 0, length);
                    }
                }
                entry = zipIn.getNextEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isZipFile(File file) {
        // Check if the file is a valid zip file
        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file))) {
            return zipInputStream.getNextEntry() != null;
        } catch (IOException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App());
    }
}
