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
public class FTPLogin {
    @Id
    @GeneratedValue
    private Long id;
    private String server;
    private String username;
    private String port;
    private String password;
}
