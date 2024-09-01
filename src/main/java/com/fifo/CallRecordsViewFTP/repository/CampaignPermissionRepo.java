package com.fifo.CallRecordsViewFTP.repository;

import com.fifo.CallRecordsViewFTP.model.CampaignPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignPermissionRepo extends JpaRepository<CampaignPermission, Long> {
    List<CampaignPermission> findAllByUsername(String username);
}
