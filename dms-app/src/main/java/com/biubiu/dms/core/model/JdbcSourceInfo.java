package com.biubiu.dms.core.model;

import lombok.Getter;

import java.util.List;

@Getter
public class JdbcSourceInfo {
    private String jdbcUrl;

    private String username;

    private String password;

    private String database;

    private String dbVersion;

    private List<Dict> properties;

    private boolean ext;

    private JdbcSourceInfo(String jdbcUrl, String username, String password, String database, String dbVersion, List<Dict> properties, boolean ext) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.database = database;
        this.dbVersion = dbVersion;
        this.properties = properties;
        this.ext = ext;
    }


    public static final class JdbcSourceInfoBuilder {
        private String jdbcUrl;
        private String username;
        private String password;
        private String database;
        private String dbVersion;
        private List<Dict> properties;
        private boolean ext;

        private JdbcSourceInfoBuilder() {
        }

        public static JdbcSourceInfoBuilder aJdbcSourceInfo() {
            return new JdbcSourceInfoBuilder();
        }

        public JdbcSourceInfoBuilder withJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
            return this;
        }

        public JdbcSourceInfoBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public JdbcSourceInfoBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public JdbcSourceInfoBuilder withDatabase(String database) {
            this.database = database;
            return this;
        }

        public JdbcSourceInfoBuilder withDbVersion(String dbVersion) {
            this.dbVersion = dbVersion;
            return this;
        }

        public JdbcSourceInfoBuilder withProperties(List<Dict> dicts) {
            this.properties = dicts;
            return this;
        }

        public JdbcSourceInfoBuilder withExt(boolean ext) {
            this.ext = ext;
            return this;
        }

        public JdbcSourceInfo build() {
            return new JdbcSourceInfo(jdbcUrl, username, password, database, dbVersion, properties, ext);
        }
    }
}
