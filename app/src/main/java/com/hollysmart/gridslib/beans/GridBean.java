package com.hollysmart.gridslib.beans;

import com.hollysmart.formlib.beans.FormModelBean;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;


@DatabaseTable(tableName = "grid_table")
public class GridBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @DatabaseField(columnName = "Id", id = true)
    private String Id;
    @DatabaseField(columnName = "fdBlockNum")
    private String fdBlockNum;
    @DatabaseField(columnName = "fdBlockCode")
    private String fdBlockCode;
    @DatabaseField(columnName = "fdLbLng")
    private double fdLbLng;
    @DatabaseField(columnName = "fdLbLat")
    private double fdLbLat;
    @DatabaseField(columnName = "fdRtLng")
    private double fdRtLng;
    @DatabaseField(columnName = "fdRtLat")
    private double fdRtLat;
    @DatabaseField(columnName = "fdAreaId")
    private String fdAreaId;
    @DatabaseField(columnName = "fdAreaName")
    private String fdAreaName;


    private FormModelBean formModel;
    private int childTreeCount;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
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

    public double getFdLbLng() {
        return fdLbLng;
    }

    public void setFdLbLng(double fdLbLng) {
        this.fdLbLng = fdLbLng;
    }

    public double getFdLbLat() {
        return fdLbLat;
    }

    public void setFdLbLat(double fdLbLat) {
        this.fdLbLat = fdLbLat;
    }

    public double getFdRtLng() {
        return fdRtLng;
    }

    public void setFdRtLng(double fdRtLng) {
        this.fdRtLng = fdRtLng;
    }

    public double getFdRtLat() {
        return fdRtLat;
    }

    public void setFdRtLat(double fdRtLat) {
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

    public String getFdAreaId() {
        return fdAreaId;
    }

    public void setFdAreaId(String fdAreaId) {
        this.fdAreaId = fdAreaId;
    }

    public String getFdAreaName() {
        return fdAreaName;
    }

    public void setFdAreaName(String fdAreaName) {
        this.fdAreaName = fdAreaName;
    }
}


