package com.fifo.CallRecordsViewFTP.repository;

import com.fifo.CallRecordsViewFTP.model.CommentData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentDataRepo extends JpaRepository<CommentData, Long> {
}
