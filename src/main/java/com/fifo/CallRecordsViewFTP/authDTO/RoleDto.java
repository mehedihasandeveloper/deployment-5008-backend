package com.fifo.CallRecordsViewFTP.authDTO;

import lombok.Data;

@Data
public class RoleDto {
    private String roleName;
    private String roleDescription;
    private Boolean active;
}
