package com.fifo.CallRecordsViewFTP.controller;

import com.fifo.CallRecordsViewFTP.model.CommentData;
import com.fifo.CallRecordsViewFTP.service.CommentDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://43.231.78.77:5010", allowCredentials = "true")
@RequestMapping("/api/CommentData")
public class CommentDataController {
    @Autowired
    private CommentDataService service;

    @PostMapping("/save-comment")
    public ResponseEntity<?> saveComment(@RequestBody CommentData commentData) {
        try {
            service.saveComment(commentData);
            return ResponseEntity.ok("Comment saved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving comment");
        }
    }
}
