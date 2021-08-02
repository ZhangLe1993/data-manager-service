package com.biubiu.dms.core.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

@Data
public class RecordInfo<T> {

    @JSONField(serialize = false)
    String createUk;

    @JSONField(serialize = false)
    Date createTime;

    @JSONField(serialize = false)
    String updateUk;

    @JSONField(serialize = false)
    Date updateTime;

    public T createdUk(String uk) {
        this.createUk = uk;
        this.createTime = new Date();
        return (T) this;
    }

    public void updatedUk(String uk) {
        this.updateUk = uk;
        this.updateTime = new Date();
    }
}
