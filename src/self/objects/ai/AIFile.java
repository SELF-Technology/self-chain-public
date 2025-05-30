package org.self.objects.ai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Represents AI-specific file operations in the Proof-of-AI system.
 * This class replaces MiniFile with AI-capacity focused operations.
 */
public class AIFile {
    private final File file;
    
    /**
     * Creates a new AIFile with the specified path.
     * @param path The file path
     */
    public AIFile(String path) {
        this.file = new File(path);
    }
    
    /**
     * Creates a new AIFile from another AIFile.
     * @param other The other AIFile to copy
     */
    public AIFile(AIFile other) {
        this.file = new File(other.file.getAbsolutePath());
    }
    
    /**
     * Gets the underlying File object.
     * @return The File object
     */
    public File getFile() {
        return file;
    }
    
    /**
     * Gets the absolute path of the file.
     * @return The absolute path
     */
    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }
    
    /**
     * Gets the canonical path of the file.
     * @return The canonical path
     * @throws IOException If an I/O error occurs
     */
    public String getCanonicalPath() throws IOException {
        return file.getCanonicalPath();
    }
    
    /**
     * Creates the file if it doesn't exist.
     * @throws IOException If an I/O error occurs
     */
    public void createFile() throws IOException {
        file.createNewFile();
    }
    
    /**
     * Creates the directory if it doesn't exist.
     * @throws IOException If an I/O error occurs
     */
    public void createDirectory() throws IOException {
        file.mkdirs();
    }
    
    /**
     * Deletes the file.
     * @return true if successfully deleted, false otherwise
     */
    public boolean delete() {
        return file.delete();
    }
    
    /**
     * Checks if the file exists.
     * @return true if exists, false otherwise
     */
    public boolean exists() {
        return file.exists();
    }
    
    /**
     * Checks if the file is a directory.
     * @return true if directory, false otherwise
     */
    public boolean isDirectory() {
        return file.isDirectory();
    }
    
    /**
     * Checks if the file is a regular file.
     * @return true if regular file, false otherwise
     */
    public boolean isFile() {
        return file.isFile();
    }
    
    /**
     * Gets the size of the file in bytes.
     * @return The size in bytes
     * @throws IOException If an I/O error occurs
     */
    public long length() throws IOException {
        return Files.size(file.toPath());
    }
    
    /**
     * Reads the contents of the file as bytes.
     * @return The file contents as bytes
     * @throws IOException If an I/O error occurs
     */
    public byte[] readBytes() throws IOException {
        return Files.readAllBytes(file.toPath());
    }
    
    /**
     * Reads the contents of the file as a string using UTF-8 encoding.
     * @return The file contents as string
     * @throws IOException If an I/O error occurs
     */
    public String readString() throws IOException {
        return new String(readBytes());
    }
    
    /**
     * Reads the contents of the file as a string using the specified encoding.
     * @param encoding The encoding
     * @return The file contents as string
     * @throws IOException If an I/O error occurs
     */
    public String readString(String encoding) throws IOException {
        return new String(readBytes(), encoding);
    }
    
    /**
     * Writes bytes to the file.
     * @param data The bytes to write
     * @throws IOException If an I/O error occurs
     */
    public void writeBytes(byte[] data) throws IOException {
        Files.write(file.toPath(), data);
    }
    
    /**
     * Writes a string to the file using UTF-8 encoding.
     * @param data The string to write
     * @throws IOException If an I/O error occurs
     */
    public void writeString(String data) throws IOException {
        writeBytes(data.getBytes());
    }
    
    /**
     * Writes a string to the file using the specified encoding.
     * @param data The string to write
     * @param encoding The encoding
     * @throws IOException If an I/O error occurs
     */
    public void writeString(String data, String encoding) throws IOException {
        writeBytes(data.getBytes(encoding));
    }
    
    /**
     * Copies the file to a destination.
     * @param destination The destination file
     * @throws IOException If an I/O error occurs
     */
    public void copyTo(AIFile destination) throws IOException {
        Files.copy(file.toPath(), destination.file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    
    /**
     * Moves the file to a destination.
     * @param destination The destination file
     * @throws IOException If an I/O error occurs
     */
    public void moveTo(AIFile destination) throws IOException {
        Files.move(file.toPath(), destination.file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    
    /**
     * Gets the hash code of this AIFile.
     * @return The hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(file.getAbsolutePath());
    }
    
    /**
     * Checks if this AIFile is equal to another.
     * @param other The other AIFile to compare with
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        AIFile aiFile = (AIFile) other;
        return file.getAbsolutePath().equals(aiFile.file.getAbsolutePath());
    }
    
    /**
     * Gets the string representation of this AIFile.
     * @return The string representation
     */
    @Override
    public String toString() {
        return file.getAbsolutePath();
    }
    
    /**
     * Creates an AIFile from a regular File object.
     * @param file The File object
     * @return A new AIFile instance
     */
    public static AIFile fromFile(File file) {
        return new AIFile(file.getAbsolutePath());
    }
    
    /**
     * Creates an AIFile from a Path object.
     * @param path The Path object
     * @return A new AIFile instance
     */
    public static AIFile fromPath(Path path) {
        return new AIFile(path.toString());
    }
    
    /**
     * Creates a temporary AIFile.
     * @param prefix The prefix for the temporary file
     * @param suffix The suffix for the temporary file
     * @return A new temporary AIFile instance
     * @throws IOException If an I/O error occurs
     */
    public static AIFile createTempFile(String prefix, String suffix) throws IOException {
        return new AIFile(File.createTempFile(prefix, suffix).getAbsolutePath());
    }
}
