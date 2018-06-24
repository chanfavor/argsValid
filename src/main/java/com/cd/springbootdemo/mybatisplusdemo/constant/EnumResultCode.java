package com.cd.springbootdemo.mybatisplusdemo.constant;

public enum EnumResultCode {

    FAIL("失败", 2001),
    SUCESS("成功", 2000);

    private String msg;

    private int code;


    EnumResultCode(String msg, int code) {
        this.msg = msg;
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
