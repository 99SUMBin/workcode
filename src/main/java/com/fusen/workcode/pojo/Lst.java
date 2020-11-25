package com.fusen.workcode.pojo;

import java.util.List;

/**
 * 包含分页的数据
 * @param <T>
 */
public class Lst<T> {
    private Integer total;

    private List<T> list;


    public Lst(){}

    public Lst(List<T> list, Integer total){
        this.list=list;
        this.total = total;
    }
    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
