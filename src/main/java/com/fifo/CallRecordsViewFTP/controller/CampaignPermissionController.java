package com.fifo.CallRecordsViewFTP.controller;


import com.fifo.CallRecordsViewFTP.model.CampaignPermission;
import com.fifo.CallRecordsViewFTP.service.CampaignPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://43.231.78.77:5010", allowCredentials = "true")
@RequestMapping("/api/campaignPermission")
public class CampaignPermissionController {
    @Autowired
    private CampaignPermissionService service;

    @PostMapping("/allowPermission")
    public CampaignPermission allowCampaign(@RequestBody CampaignPermission campaignPermission) {
        return service.allowCampaign(campaignPermission);
    }

    @GetMapping("/getCampaignNamesByClient")
    public List<CampaignPermission> getAllByUser(String username){
        return service.getAllCampaignNames(username);
    }

}
