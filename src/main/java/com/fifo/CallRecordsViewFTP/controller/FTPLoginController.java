package com.fifo.CallRecordsViewFTP.controller;

import com.fifo.CallRecordsViewFTP.model.FTPLogin;
import com.fifo.CallRecordsViewFTP.service.FTPLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://43.231.78.77:5010", allowCredentials = "true")
@RequestMapping("/api/FTPLogin")
public class FTPLoginController {

    @Autowired
    private FTPLoginService service;

    @PostMapping("/addFTPLogin")
    public FTPLogin addFTPLogin(@RequestBody FTPLogin ftpLogin) {
        return service.addFTPLoginCredential(ftpLogin);
    }

    @PutMapping("/editFTPLogin")
    public FTPLogin updateFTPLogin(@RequestBody FTPLogin ftpLogin) {
        return service.editFTPLogin(ftpLogin);
    }
}
