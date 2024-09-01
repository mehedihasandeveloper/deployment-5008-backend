package com.fifo.CallRecordsViewFTP.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MP3FileInfo {
    private String dateTime;
    private String phoneNumber;
    private String campaignName;
    private String agentId;
    private String fileName;
    private long duration; // Duration in seconds

    // Constructors, Getters, and Setters

    public MP3FileInfo(String dateTime, String phoneNumber, String campaignName, String agentId, String fileName) {
        this.dateTime = dateTime;
        this.phoneNumber = phoneNumber;
        this.campaignName = campaignName;
        this.agentId = agentId;
        this.fileName = fileName;
    }

    public MP3FileInfo() {

    }
}
