package com.biubiu.dms.core.model;

import com.biubiu.dms.pojo.Connection;

import java.util.List;

public abstract class BaseSource extends RecordInfo<Connection> {

    public abstract String getJdbcUrl();

    public abstract String getUsername();

    public abstract String getPassword();

    public abstract String getDatabase();

    public abstract String getDbVersion();

    public abstract List<Dict> getProperties();

    public abstract boolean isExt();
}
