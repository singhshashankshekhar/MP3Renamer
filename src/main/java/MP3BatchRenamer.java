import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * A command-line utility to rename MP3 files based on their ID3v1 title tag.
 * This version supports batch processing of files within a specified directory.
 *
 * How It Works:
 * The ID3v1 tag standard stores metadata in the last 128 bytes of an MP3 file.
 * This program reads that specific block of data to find the song's title.
 *
 * - It checks for a "TAG" identifier to ensure an ID3v1 tag exists.
 * - It extracts the 30 bytes allocated for the title.
 * - It cleans the extracted title to create a valid filename.
 * - It renames the original MP3 file to "<New Title>.mp3".
 *
 * Limitations:
 * - This program only supports the older ID3v1 standard. It will not work for
 * modern ID3v2 tags, which are more complex and stored at the beginning of the file.
 * - If a file does not have an ID3v1 tag, the program will report that no title was found.
 *
 * How to Run:
 * 1. Compile the code: javac MP3Renamer.java
 * 2. Run from the command line: java MP3Renamer "<path_to_directory_containing_mp3s>"
 * Example: java MP3Renamer "C:\Users\YourUser\Music"
 */
public class MP3BatchRenamer {

    // Constants defining the structure of an ID3v1 tag (last 128 bytes of a file).
    private static final int ID3_TAG_SIZE = 128;
    private static final int TITLE_OFFSET = 3;
    private static final int TITLE_LENGTH = 30;

    public static void main(String[] args) {
        // Ensure the user has provided a path (file or directory) as a command-line argument.
        if (args.length < 1) {
            System.err.println("Usage: java MP3Renamer \"<path_to_mp3_file_or_directory>\"");
            return;
        }

        String inputPath = args[0];
        File inputFile = new File(inputPath);

        // Check if the provided path actually exists.
        if (!inputFile.exists()) {
            System.err.println("Error: Path not found at '" + inputPath + "'");
            return;
        }

        if (inputFile.isDirectory()) {
            System.out.println("Processing directory: '" + inputFile.getAbsolutePath() + "'");
            processDirectory(inputFile);
        } else if (inputFile.isFile() && inputFile.getName().toLowerCase().endsWith(".mp3")) {
            System.out.println("Processing single file: '" + inputFile.getName() + "'");
            processMp3File(inputFile);
        } else {
            System.err.println("Error: Provided path is neither a directory nor an MP3 file: '" + inputPath + "'");
        }
    }

    /**
     * Processes all MP3 files within a given directory.
     *
     * @param directory The directory to scan for MP3 files.
     */
    private static void processDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            System.err.println("Error: Could not list files in directory '" + directory.getAbsolutePath() + "'. Check permissions.");
            return;
        }

        if (files.length == 0) {
            System.out.println("No files found in directory: '" + directory.getAbsolutePath() + "'");
            return;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".mp3")) {
                processMp3File(file);
            }
        }
        System.out.println("\nBatch processing complete.");
    }

    /**
     * Processes a single MP3 file, attempting to rename it based on its ID3v1 tag.
     *
     * @param mp3File The MP3 file to process.
     */
    private static void processMp3File(File mp3File) {
        System.out.println("\n--- Processing file: " + mp3File.getName() + " ---");
        try {
            String title = readId3Title(mp3File);

            if (title != null && !title.isEmpty()) {
                System.out.println("  Found Title: \"" + title + "\"");

                // Sanitize the title to make it a valid filename.
                // This removes characters that are not allowed in filenames on most operating systems.
                String sanitizedTitle = title.replaceAll("[\\\\/:*?\"<>|]", "").trim();

                if (sanitizedTitle.isEmpty()) {
                    System.err.println("  Error: The extracted title contains only invalid filename characters for '" + mp3File.getName() + "'. Skipping.");
                    return;
                }

                // Construct the new file path and name.
                File parentDir = mp3File.getParentFile();
                File newFile = new File(parentDir, sanitizedTitle + ".mp3");

                // Check if the new name is identical to the old name (ignoring case for safety, though renameTo is case-sensitive on some OS).
                // This prevents unnecessary rename operations if the file is already correctly named.
                if (mp3File.getAbsolutePath().equalsIgnoreCase(newFile.getAbsolutePath())) {
                    System.out.println("  File already named correctly: '" + mp3File.getName() + "'. Skipping.");
                    return;
                }

                // Check if a file with the new name already exists.
                if (newFile.exists()) {
                    System.err.println("  Error: A file named \"" + newFile.getName() + "\" already exists in this folder. Skipping '" + mp3File.getName() + "'.");
                    return;
                }

                // Perform the rename operation.
                if (mp3File.renameTo(newFile)) {
                    System.out.println("  Successfully renamed to '" + newFile.getName() + "'");
                } else {
                    System.err.println("  Error: Failed to rename '" + mp3File.getName() + "'. Check permissions or if the file is open.");
                }

            } else {
                System.out.println("  No valid ID3v1 title tag found in '" + mp3File.getName() + "'. Skipping.");
            }
        } catch (IOException e) {
            System.err.println("  An error occurred while reading '" + mp3File.getName() + "': " + e.getMessage());
            // e.printStackTrace(); // Uncomment for full stack trace if needed for debugging
        }
    }

    /**
     * Reads the last 128 bytes of a file to parse an ID3v1 tag and extract the song title.
     *
     * @param file The MP3 file to read.
     * @return The song title as a String, or null if no valid tag is found.
     * @throws IOException If an error occurs during file access.
     */
    private static String readId3Title(File file) throws IOException {
        // RandomAccessFile allows us to seek to any position in the file.
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            // Check if the file is large enough to even contain an ID3 tag.
            if (raf.length() < ID3_TAG_SIZE) {
                return null;
            }

            // Move the file pointer to the beginning of the ID3 tag (128 bytes from the end).
            raf.seek(raf.length() - ID3_TAG_SIZE);

            // Read all 128 bytes of the tag into a byte array.
            byte[] tagBytes = new byte[ID3_TAG_SIZE];
            raf.readFully(tagBytes);

            // The first 3 bytes should be the ASCII characters "TAG".
            String tagIdentifier = new String(tagBytes, 0, 3, StandardCharsets.ISO_8859_1);
            if (!"TAG".equals(tagIdentifier)) {
                return null; // This file does not have a standard ID3v1 tag.
            }

            // The title is stored in the 30 bytes following the "TAG" identifier.
            String title = new String(tagBytes, TITLE_OFFSET, TITLE_LENGTH, StandardCharsets.ISO_8859_1).trim();

            // The trim() method might not remove null characters, so we do it manually.
            int nullCharIndex = title.indexOf(0);
            if (nullCharIndex != -1) {
                return title.substring(0, nullCharIndex);
            }

            return title;
        }
    }
}