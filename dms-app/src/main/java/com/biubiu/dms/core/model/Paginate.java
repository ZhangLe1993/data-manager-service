package com.biubiu.dms.core.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Paginate<T> implements Serializable {

    private int pageNo = -1;
    private int pageSize = -1;
    private long totalCount = -1;
    private List<T> resultList = new ArrayList<T>();
}
