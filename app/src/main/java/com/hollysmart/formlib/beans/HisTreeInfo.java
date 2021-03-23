package com.hollysmart.formlib.beans;

public class HisTreeInfo {


    private String id;                         //":32,
    private String fdAreaName;                         //":"海淀区",
    private String fdTreeCode;                         //":"0003-001-004",
    private String fdLng;                         //":116.21705556774472,
    private String fdLat;                         //":40.0295033895346,
    private String fdTreeType;                         //":"柳树",
    private String fdTreeState;                         //":"差",
    private String fdTreeDiam;                         //":"20cm以下",
    private String fdTreeCount;                         //":30,
    private String fdTreeInject;                         //":"否"

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFdTreeCode() {
        return fdTreeCode;
    }

    public void setFdTreeCode(String fdTreeCode) {
        this.fdTreeCode = fdTreeCode;
    }

    public String getFdLng() {
        return fdLng;
    }

    public void setFdLng(String fdLng) {
        this.fdLng = fdLng;
    }

    public String getFdLat() {
        return fdLat;
    }

    public void setFdLat(String fdLat) {
        this.fdLat = fdLat;
    }

    public String getFdTreeType() {
        return fdTreeType;
    }

    public void setFdTreeType(String fdTreeType) {
        this.fdTreeType = fdTreeType;
    }

    public String getFdTreeState() {
        return fdTreeState;
    }

    public void setFdTreeState(String fdTreeState) {
        this.fdTreeState = fdTreeState;
    }

    public String getFdTreeCount() {
        return fdTreeCount;
    }

    public void setFdTreeCount(String fdTreeCount) {
        this.fdTreeCount = fdTreeCount;
    }

    public String getFdAreaName() {
        return fdAreaName;
    }

    public void setFdAreaName(String fdAreaName) {
        this.fdAreaName = fdAreaName;
    }

    public String getFdTreeDiam() {
        return fdTreeDiam;
    }

    public void setFdTreeDiam(String fdTreeDiam) {
        this.fdTreeDiam = fdTreeDiam;
    }

    public String getFdTreeInject() {
        return fdTreeInject;
    }

    public void setFdTreeInject(String fdTreeInject) {
        this.fdTreeInject = fdTreeInject;
    }
}
