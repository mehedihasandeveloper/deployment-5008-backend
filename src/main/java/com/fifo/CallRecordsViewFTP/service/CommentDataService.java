package com.fifo.CallRecordsViewFTP.service;

import com.fifo.CallRecordsViewFTP.model.CommentData;
import com.fifo.CallRecordsViewFTP.repository.CommentDataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentDataService {
    @Autowired
    private CommentDataRepo repository;

    public void saveComment(CommentData commentData) {
        repository.save(commentData);
    }
}
