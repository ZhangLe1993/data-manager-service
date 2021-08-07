package com.biubiu.dms.dto;

import com.biubiu.dms.core.model.Dict;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class SourceConfig {

    private String username;

    private String password;

    @NotBlank(message = "connection url cannot be EMPTY")
    private String url;

    private String parameters;

    private String version;

    private List<Dict> properties;

    private boolean isExt;

    public SourceConfig() {

    }
}
