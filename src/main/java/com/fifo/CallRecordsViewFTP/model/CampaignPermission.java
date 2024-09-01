package com.fifo.CallRecordsViewFTP.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class CampaignPermission {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private String campaignName;
}
