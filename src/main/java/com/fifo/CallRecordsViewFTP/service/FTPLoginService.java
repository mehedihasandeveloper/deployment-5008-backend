package com.fifo.CallRecordsViewFTP.service;

import com.fifo.CallRecordsViewFTP.model.FTPLogin;
import com.fifo.CallRecordsViewFTP.repository.FTPLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class FTPLoginService {
    @Autowired
    private FTPLoginRepository repository;

    public FTPLogin addFTPLoginCredential(@RequestBody FTPLogin ftpLogin) {
        return repository.save(ftpLogin);
    }


    public FTPLogin editFTPLogin(FTPLogin ftpLogin) {
        FTPLogin existingFTPLogin = repository.findById(1L).get();
        existingFTPLogin.setServer(ftpLogin.getServer());
        existingFTPLogin.setUsername(ftpLogin.getUsername());
        existingFTPLogin.setPassword(ftpLogin.getPassword());

        return repository.save(existingFTPLogin);
    }
}
