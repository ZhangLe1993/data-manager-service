package com.biubiu.dms.model;

import lombok.Data;

@Data
public class Remote {
    private String user = "root";
    private String host = "172.172.0.11";
    private Integer port = 22;
    private String authMethod = "password";
    private String password = "root";
    private String identity = "~/.ssh/id_rsa";
    private String passphrase = "";
}
