package com.fifo.CallRecordsViewFTP.service;

import com.fifo.CallRecordsViewFTP.model.FTPLogin;
import com.fifo.CallRecordsViewFTP.model.MP3FileInfo;
import com.fifo.CallRecordsViewFTP.repository.FTPLoginRepository;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import jakarta.annotation.PostConstruct;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class FTPService {
    @Autowired
    private FTPLoginRepository FTPRepo;

    private static final Logger LOGGER = Logger.getLogger(FTPService.class.getName());

//    private static String FTP_SERVER;
    private static final int FTP_PORT = 21;
//    private static String USERNAME;
//    private static String PASSWORD;

    private static final String FTP_SERVER = "192.168.100.100";
    private static final String USERNAME = "fifoftp";
    private static final String PASSWORD = "F1f0t3ch";

//    @PostConstruct
//    private void init() {
//        // Use @PostConstruct to initialize values after the dependency injection is complete.
//        FTPLogin loginCre = FTPRepo.findById(1L).orElseThrow(() -> new RuntimeException("FTP login credentials not found"));
//        FTP_SERVER = loginCre.getServer();
//        USERNAME = loginCre.getUsername();
//        PASSWORD = loginCre.getPassword();
//    }

    // Download whole folder
    public void downloadFilesFromDirectory(String remoteDirPath, String localDirPath) throws IOException {
        FTPClient ftpClient = new FTPClient();

        try {
            LOGGER.info("Connecting to FTP server...");
            ftpClient.connect(FTP_SERVER, FTP_PORT);

            LOGGER.info("Logging in...");
            boolean loginSuccess = ftpClient.login(USERNAME, PASSWORD);
            if (!loginSuccess) {
                throw new IOException("Failed to login to FTP server.");
            }

            ftpClient.enterLocalPassiveMode();
            LOGGER.info("Connected and logged in successfully.");

            FTPFile[] files = ftpClient.listFiles(remoteDirPath);
            if (files == null || files.length == 0) {
                throw new IOException("No files found in the directory: " + remoteDirPath);
            }

            LOGGER.info("Found " + files.length + " files in directory: " + remoteDirPath);

            for (FTPFile file : files) {
                if (file.isFile()) {
                    String remoteFilePath = remoteDirPath + "/" + file.getName();
                    String localFilePath = localDirPath + "/" + file.getName();

                    LOGGER.info("Downloading file: " + remoteFilePath);
                    try (OutputStream outputStream = new FileOutputStream(localFilePath)) {
                        boolean success = ftpClient.retrieveFile(remoteFilePath, outputStream);
                        if (!success) {
                            throw new IOException("Failed to download file: " + remoteFilePath);
                        }
                    }
                    LOGGER.info("File downloaded: " + remoteFilePath);
                }
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error occurred while connecting to FTP server or retrieving files.", ex);
            throw new IOException("Error occurred while connecting to FTP server or retrieving files.", ex);
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Error occurred while disconnecting from FTP server.", ex);
                }
            }
        }
    }

    // Download and Play the mp3
    public byte[] downloadMP3File(String remoteFilePath) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(FTP_SERVER, FTP_PORT);
            ftpClient.login(USERNAME, PASSWORD);
            ftpClient.enterLocalPassiveMode();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            boolean success = ftpClient.retrieveFile(remoteFilePath, outputStream);
            if (!success) {
                throw new IOException("Failed to download file: " + remoteFilePath);
            }

            return outputStream.toByteArray();
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
    }

    // Data list of mp3 files

    public Page<MP3FileInfo> listMP3Files(String remoteDirPath, int page, int size) throws IOException {
        FTPClient ftpClient = new FTPClient();
        List<MP3FileInfo> mp3Files = new ArrayList<>();

        FTPFile[] files;
        try {
            ftpClient.connect(FTP_SERVER, FTP_PORT);
            ftpClient.login(USERNAME, PASSWORD);
            ftpClient.enterLocalPassiveMode();

            files = ftpClient.listFiles(remoteDirPath);
            if (files == null || files.length == 0) {
                throw new IOException("No files found in the directory: " + remoteDirPath);
            }

            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, files.length);

            for (int i = startIndex; i < endIndex; i++) {
                FTPFile file = files[i];
                if (file.isFile() && file.getName().endsWith(".mp3")) {
                    try {
                        MP3FileInfo fileInfo = parseFileName(file.getName());

                        // Download file to a local temporary location
                        File tempFile = File.createTempFile("temp", ".mp3");
                        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                            ftpClient.retrieveFile(remoteDirPath + "/" + file.getName(), outputStream);
                        }

                        // Get the duration of the MP3 file
                        long duration = getMP3Duration(tempFile);
                        fileInfo.setDuration(duration);

                        mp3Files.add(fileInfo);

                        // Delete the temporary file
                        tempFile.delete();

                    } catch (IllegalArgumentException | InvalidDataException | UnsupportedTagException |
                             NotSupportedException ex) {
                        System.err.println("Error processing file: " + file.getName());
                        // Handle or log the error as needed
                    }
                }
            }

        } catch (IOException ex) {
            throw new IOException("Error occurred while connecting to FTP server or retrieving files.", ex);
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException ex) {
                    // Log error while disconnecting
                    System.err.println("Error occurred while disconnecting from FTP server.");
                }
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(mp3Files, pageable, files.length);
    }

    private long getMP3Duration(File mp3File) throws InvalidDataException, UnsupportedTagException, NotSupportedException, IOException {
        Mp3File mp3file = new Mp3File(mp3File);
        return mp3file.getLengthInSeconds();
    }

    private MP3FileInfo parseFileName(String fileName) {
        // Expected format: YYYYMMDD-HHMMSS_PHONE_NUMBER_CAMPAIGN_NAME_AGENT_ID-all.mp3
        String[] parts = fileName.split("_");

        // Check if the file name parts are valid
        if (parts.length < 3 || !parts[parts.length - 1].endsWith("-all.mp3")) {
            throw new IllegalArgumentException("Invalid file name format.");
        }

        // Extract dateTime and phoneNumber from the first two parts
        String dateTime = parts[0]; // YYYYMMDD-HHMMSS
        String phoneNumber = parts[1]; // PHONE_NUMBER

        // Extract campaignName and agentId
        String campaignName = String.join("_", Arrays.copyOfRange(parts, 2, parts.length - 1));
        String agentId = parts[parts.length - 1].split("-")[0]; // AGENT_ID (part before "-all.mp3")

        // Create and populate MP3FileInfo object
        MP3FileInfo mp3FileInfo = new MP3FileInfo();
        mp3FileInfo.setDateTime(dateTime);
        mp3FileInfo.setPhoneNumber(phoneNumber);
        mp3FileInfo.setCampaignName(campaignName);
        mp3FileInfo.setAgentId(agentId);
        mp3FileInfo.setFileName(fileName);

        return mp3FileInfo;
    }


    public List<MP3FileInfo> listMP3FilesByCampaignName(String remoteDirPath, String campaignName, int page, int size) throws IOException {
        FTPClient ftpClient = new FTPClient();
        List<MP3FileInfo> mp3Files = new ArrayList<>();

        try {
            ftpClient.connect(FTP_SERVER, FTP_PORT);
            ftpClient.login(USERNAME, PASSWORD);
            ftpClient.enterLocalPassiveMode();

            FTPFile[] files = ftpClient.listFiles(remoteDirPath);
            if (files == null || files.length == 0) {
                throw new IOException("No files found in the directory: " + remoteDirPath);
            }

            List<FTPFile> filteredFiles = new ArrayList<>();

            // Filter files by campaignName
            for (FTPFile file : files) {
                if (file.isFile() && file.getName().endsWith(".mp3")) {
                    try {
                        MP3FileInfo fileInfo = parseFileName(file.getName());

                        // Check if the campaignName matches
                        if (fileInfo.getCampaignName().equalsIgnoreCase(campaignName)) {
                            filteredFiles.add(file);
                        }
                    } catch (IllegalArgumentException ex) {
                        System.err.println("Error processing file: " + file.getName());
                    }
                }
            }

            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, filteredFiles.size());

            for (int i = startIndex; i < endIndex; i++) {
                FTPFile file = filteredFiles.get(i);
                try {
                    MP3FileInfo fileInfo = parseFileName(file.getName());

                    File tempFile = File.createTempFile("temp", ".mp3");
                    try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                        ftpClient.retrieveFile(remoteDirPath + "/" + file.getName(), outputStream);
                    }

                    long duration = getMP3Duration(tempFile);
                    fileInfo.setDuration(duration);

                    mp3Files.add(fileInfo);

                    tempFile.delete();

                } catch (IllegalArgumentException | InvalidDataException | UnsupportedTagException |
                         NotSupportedException ex) {
                    System.err.println("Error processing file: " + file.getName());
                }
            }

        } catch (IOException ex) {
            throw new IOException("Error occurred while connecting to FTP server or retrieving files.", ex);
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException ex) {
                    System.err.println("Error occurred while disconnecting from FTP server.");
                }
            }
        }

        return mp3Files;
    }

    public int countMP3FilesByCampaignName(String remoteDirPath, String campaignName) throws IOException {
        FTPClient ftpClient = new FTPClient();
        int count = 0;

        try {
            ftpClient.connect(FTP_SERVER, FTP_PORT);
            ftpClient.login(USERNAME, PASSWORD);
            ftpClient.enterLocalPassiveMode();

            FTPFile[] files = ftpClient.listFiles(remoteDirPath);
            if (files != null && files.length > 0) {
                for (FTPFile file : files) {
                    if (file.isFile() && file.getName().endsWith(".mp3")) {
                        try {
                            MP3FileInfo fileInfo = parseFileName(file.getName());
                            if (fileInfo.getCampaignName().equalsIgnoreCase(campaignName)) {
                                count++;
                            }
                        } catch (IllegalArgumentException ex) {
                            System.err.println("Error processing file: " + file.getName());
                        }
                    }
                }
            }

        } catch (IOException ex) {
            throw new IOException("Error occurred while connecting to FTP server or retrieving files.", ex);
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException ex) {
                    System.err.println("Error occurred while disconnecting from FTP server.");
                }
            }
        }

        return count;
    }


    public List<MP3FileInfo> searchMP3Files(String directory, String msisdn, int page, int size) throws IOException {
        FTPClient ftpClient = new FTPClient();
        List<MP3FileInfo> mp3Files = new ArrayList<>();

        try {
            ftpClient.connect(FTP_SERVER, FTP_PORT);
            ftpClient.login(USERNAME, PASSWORD);
            ftpClient.enterLocalPassiveMode();

            FTPFile[] files = ftpClient.listFiles(directory);
            if (files == null || files.length == 0) {
                throw new IOException("No files found in the directory: " + directory);
            }

            List<FTPFile> filteredFiles = new ArrayList<>();

            // Filter files based on msisdn
            for (FTPFile file : files) {
                if (file.isFile() && file.getName().endsWith(".mp3")) {
                    try {
                        MP3FileInfo fileInfo = parseFileName(file.getName());
                        if (msisdn == null || fileInfo.getPhoneNumber().equals(msisdn)) {
                            filteredFiles.add(file);
                        }
                    } catch (IllegalArgumentException ex) {
                        System.err.println("Error processing file: " + file.getName());
                    }
                }
            }
            // Apply pagination
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, filteredFiles.size());
            for (int i = startIndex; i < endIndex; i++) {
                FTPFile file = filteredFiles.get(i);
                try {
                    MP3FileInfo fileInfo = parseFileName(file.getName());
                    File tempFile = File.createTempFile("temp", ".mp3");
                    try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                        ftpClient.retrieveFile(directory + "/" + file.getName(), outputStream);
                    }
                    long duration = getMP3Duration(tempFile);
                    fileInfo.setDuration(duration);
                    mp3Files.add(fileInfo);
                    tempFile.delete();
                } catch (IllegalArgumentException | InvalidDataException | UnsupportedTagException |
                         NotSupportedException ex) {
                    System.err.println("Error processing file: " + file.getName());
                }
            }
        } catch (IOException ex) {
            throw new IOException("Error occurred while connecting to FTP server or retrieving files.", ex);
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException ex) {
                    System.err.println("Error occurred while disconnecting from FTP server.");
                }
            }
        }
        return mp3Files;
    }

    public int countFilteredFiles(String directory, String msisdn) throws IOException {
        FTPClient ftpClient = new FTPClient();
        int count = 0;
        try {
            ftpClient.connect(FTP_SERVER, FTP_PORT);
            ftpClient.login(USERNAME, PASSWORD);
            ftpClient.enterLocalPassiveMode();

            FTPFile[] files = ftpClient.listFiles(directory);
            if (files != null && files.length > 0) {
                for (FTPFile file : files) {
                    if (file.isFile() && file.getName().endsWith(".mp3")) {
                        try {
                            MP3FileInfo fileInfo = parseFileName(file.getName());

                            if (msisdn == null || fileInfo.getPhoneNumber().equals(msisdn)) {
                                count++;
                            }
                        } catch (IllegalArgumentException ex) {
                            System.err.println("Error processing file: " + file.getName());
                        }
                    }
                }
            }

        } catch (IOException ex) {
            throw new IOException("Error occurred while connecting to FTP server or retrieving files.", ex);
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException ex) {
                    System.err.println("Error occurred while disconnecting from FTP server.");
                }
            }
        }
        return count;
    }


}


