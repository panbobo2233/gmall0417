package com.atguigu.gmall0417.bean;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

public class BaseAttrInfo implements Serializable {
    //获取主键自增
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String attrName;
    @Column
    private String catalog3Id;

    //保存按钮属性名和属性值要组成一个对象
    //这个注解是说数据库没有的字段
    @Transient
    private List<BaseAttrValue> attrValueList;


    public List<BaseAttrValue> getAttrValueList() {
        return attrValueList;
    }

    public void setAttrValueList(List<BaseAttrValue> attrValueList) {
        this.attrValueList = attrValueList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getCatalog3Id() {
        return catalog3Id;
    }

    public void setCatalog3Id(String catalog3Id) {
        this.catalog3Id = catalog3Id;
    }

}