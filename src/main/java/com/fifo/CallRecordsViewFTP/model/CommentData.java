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
public class CommentData {
    @Id
    @GeneratedValue
    private Long id;
    private String fileName;
    private String dateTime;
    private String phoneNumber;
    private String campaignName;
    private String agentId;
    private String duration;
    private String comment;

    // Getters and setters

}
