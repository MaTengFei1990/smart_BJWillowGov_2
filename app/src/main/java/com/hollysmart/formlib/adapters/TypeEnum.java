package com.hollysmart.formlib.adapters;

public enum  TypeEnum {


    DANHANG(0,null),
    VIEWTYPE_DANHANG_LIST(1,"text"),
    VIEWTYPE_DANHANG_TIME_SELECT(2,"datetime"),
    VIEWTYPE_CONTENT_CHILD_LIST(3,"list"),
    VIEWTYPE_CONTENT_IMAGE_CONTENT(4,"image"),
    VIEWTYPE_CONTENT_MARKER_CONTENT(5,"marker"),
    VIEWTYPE_CONTENT_PLANE_CONTENT(6,"plane"),
    VIEWTYPE_CONTENT_LINE_CONTENT(8,"line"),
    VIEWTYPE_CONTENT_SWITCH_CONTENT(9,"switch"),
    VIEWTYPE_CONTENT_MULTISTAGELIST_CONTENT(10,"multistageList"),

    VIEWTYPE_CONTENT_CHECKBOX(11,"checkbox");


    private int showtype;
    private String  showtype_str;

    TypeEnum(int showtype,String showtype_str){
        this.showtype = showtype;
        this.showtype_str = showtype_str;
    }


    public static int getShowType(String showtype_str) {

        for (TypeEnum typeEnum : values()) {

            if (showtype_str.equals(typeEnum.showtype_str)) {
                return typeEnum.showtype;
            }
        }
        return 0;
    }

//
//    public String getvalue(){
//        return this.value;
//    }







}
