package com.biubiu.dms.core.model;
import com.alibaba.druid.util.StringUtils;
import com.biubiu.dms.core.consts.Consts;
import lombok.Data;

@Data
public class CustomDataSource {
    private String name;
    private String desc;
    private String version;
    private String driver;
    private String keyword_prefix;
    private String keyword_suffix;
    private String alias_prefix;
    private String alias_suffix;


    public void setKeyword_prefix(String keyword_prefix) {
        this.keyword_prefix = getStringValue(keyword_prefix);
    }

    public void setKeyword_suffix(String keyword_suffix) {
        this.keyword_suffix = getStringValue(keyword_suffix);
    }

    public void setAlias_prefix(String alias_prefix) {
        this.alias_prefix = getStringValue(alias_prefix);
    }

    public void setAlias_suffix(String alias_suffix) {
        this.alias_suffix = getStringValue(alias_suffix);
    }

    public String getKeyword_prefix() {
        return getStringValue(keyword_prefix);
    }

    public String getKeyword_suffix() {
        return getStringValue(keyword_suffix);
    }

    public String getAlias_prefix() {
        return StringUtils.isEmpty(getStringValue(alias_prefix)) ? "'" : getStringValue(alias_prefix);
    }

    public String getAlias_suffix() {
        return StringUtils.isEmpty(getStringValue(alias_suffix)) ? "'" : getStringValue(alias_suffix);
    }

    private String getStringValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return Consts.EMPTY;
        }

        if (value.contains("\\")) {
            return value.replace("\\", Consts.EMPTY);
        }
        return value;
    }
}
