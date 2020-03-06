package com.hollysmart.gridslib.beans;

import com.hollysmart.formlib.beans.FormModelBean;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;


@DatabaseTable(tableName = "grid_table")
public class GridBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @DatabaseField(columnName = "id", id = true)
    private String id;
    @DatabaseField(columnName = "fdBlockNum")
    private String fdBlockNum;
    @DatabaseField(columnName = "fdBlockCode")
    private String fdBlockCode;
    @DatabaseField(columnName = "fdLbLng")
    private String fdLbLng;
    @DatabaseField(columnName = "fdLbLat")
    private String fdLbLat;
    @DatabaseField(columnName = "fdRtLng")
    private String fdRtLng;
    @DatabaseField(columnName = "fdRtLat")
    private String fdRtLat;


    private FormModelBean formModel;
    private int childTreeCount;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFdBlockNum() {
        return fdBlockNum;
    }

    public void setFdBlockNum(String fdBlockNum) {
        this.fdBlockNum = fdBlockNum;
    }

    public String getFdBlockCode() {
        return fdBlockCode;
    }

    public void setFdBlockCode(String fdBlockCode) {
        this.fdBlockCode = fdBlockCode;
    }

    public String getFdLbLng() {
        return fdLbLng;
    }

    public void setFdLbLng(String fdLbLng) {
        this.fdLbLng = fdLbLng;
    }

    public String getFdLbLat() {
        return fdLbLat;
    }

    public void setFdLbLat(String fdLbLat) {
        this.fdLbLat = fdLbLat;
    }

    public String getFdRtLng() {
        return fdRtLng;
    }

    public void setFdRtLng(String fdRtLng) {
        this.fdRtLng = fdRtLng;
    }

    public String getFdRtLat() {
        return fdRtLat;
    }

    public void setFdRtLat(String fdRtLat) {
        this.fdRtLat = fdRtLat;
    }

    public FormModelBean getFormModel() {
        return formModel;
    }

    public void setFormModel(FormModelBean formModel) {
        this.formModel = formModel;
    }

    public int getChildTreeCount() {
        return childTreeCount;
    }

    public void setChildTreeCount(int childTreeCount) {
        this.childTreeCount = childTreeCount;
    }
}


