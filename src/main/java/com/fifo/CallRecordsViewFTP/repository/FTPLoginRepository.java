package com.fifo.CallRecordsViewFTP.repository;

import com.fifo.CallRecordsViewFTP.model.FTPLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FTPLoginRepository extends JpaRepository<FTPLogin, Long> {

}
