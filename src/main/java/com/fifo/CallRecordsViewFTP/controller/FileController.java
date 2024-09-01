package com.fifo.CallRecordsViewFTP.controller;

import com.fifo.CallRecordsViewFTP.model.MP3FileInfo;
import com.fifo.CallRecordsViewFTP.service.FTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;


@RestController
@CrossOrigin(origins = "http://43.231.78.77:5010", allowCredentials = "true")
public class FileController {
    @Autowired
    private FTPService ftpService;

    @GetMapping("/download-folder")
    public String downloadFolder(@RequestParam String remoteDirPath, @RequestParam String localDirPath) {
        try {
            ftpService.downloadFilesFromDirectory(remoteDirPath, localDirPath);
            return "Files downloaded successfully!";
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }


    @GetMapping("/list-mp3-files")
    public Page<MP3FileInfo> listMP3Files(@RequestParam String folderPath,
                                          @RequestParam int page,
                                          @RequestParam int size) throws IOException {
        return ftpService.listMP3Files(folderPath, page, size);
    }


    @GetMapping("/list-mp3-files-by-filter")
    public ResponseEntity<Page<MP3FileInfo>> listMP3FilesByFilter(
            @RequestParam String folderPath,
            @RequestParam String campaignName,
            @RequestParam int page,
            @RequestParam int size) throws IOException {

        List<MP3FileInfo> mp3Files = ftpService.listMP3FilesByCampaignName(folderPath, campaignName, page, size);
        // Calculate the total number of pages based on the size of the list
        // Assuming you can determine the total count from your service or another method

        int totalElements = ftpService.countMP3FilesByCampaignName(folderPath, campaignName); // Add this method
        int totalPages = (int) Math.ceil((double) totalElements / size);

        Page<MP3FileInfo> result = new PageImpl<>(mp3Files, PageRequest.of(page, size), totalElements);

        return ResponseEntity.ok(result);
    }


    @GetMapping("/download-mp3")
    public ResponseEntity<byte[]> downloadMP3File(@RequestParam String fileName) {
        try {
            byte[] fileData = ftpService.downloadMP3File(fileName);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            headers.add(HttpHeaders.CONTENT_TYPE, "audio/mpeg");
            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/search")
    public ResponseEntity<Page<MP3FileInfo>> searchMP3Files(
            @RequestParam String directory,
            @RequestParam String msisdn,
            @RequestParam int page,
            @RequestParam int size) throws IOException {

        // Get the filtered list of MP3 files and the total count
        List<MP3FileInfo> mp3Files = ftpService.searchMP3Files(directory, msisdn, page, size);
        int totalElements = ftpService.countFilteredFiles(directory, msisdn); // Updated method

        // Create a PageImpl object for the paginated response
        Page<MP3FileInfo> result = new PageImpl<>(mp3Files, PageRequest.of(page, size), totalElements);

        return ResponseEntity.ok(result);
    }
}
