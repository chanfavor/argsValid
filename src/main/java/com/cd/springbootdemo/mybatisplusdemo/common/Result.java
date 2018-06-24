package com.cd.springbootdemo.mybatisplusdemo.common;

public class Result<T> {

    private String msg;

    private Integer code;

    private Boolean  success = false;

    private T data;

    public Result(String msg, Integer code, Boolean success, T data) {
        this.msg = msg;
        this.code = code;
        this.success = success;
        this.data = data;
    }

    public Result() {

    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
