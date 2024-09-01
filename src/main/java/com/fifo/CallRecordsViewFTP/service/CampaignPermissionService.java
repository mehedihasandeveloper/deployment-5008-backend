package com.fifo.CallRecordsViewFTP.service;

import com.fifo.CallRecordsViewFTP.model.CampaignPermission;
import com.fifo.CallRecordsViewFTP.repository.CampaignPermissionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CampaignPermissionService {
    @Autowired
    private CampaignPermissionRepo repository;


    public CampaignPermission allowCampaign(CampaignPermission campaignPermission) {
        return repository.save(campaignPermission);
    }

    public List<CampaignPermission> getAllCampaignNames(String username) {
        return repository.findAllByUsername(username);
    }
}
