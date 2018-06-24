package com.cd.springbootdemo.mybatisplusdemo.exception;

public class ParamIsNullException extends RuntimeException {
    private String parameterName;
    private String parameterType;

    public ParamIsNullException(String parameterName, String parameterType) {
        this.parameterName = parameterName;
        this.parameterType = parameterType;
    }

    public ParamIsNullException(String message, String parameterName, String parameterType) {
        super(message);
        this.parameterName = parameterName;
        this.parameterType = parameterType;
    }

    public ParamIsNullException(String message, Throwable cause, String parameterName, String parameterType) {
        super(message, cause);
        this.parameterName = parameterName;
        this.parameterType = parameterType;
    }

    public ParamIsNullException(Throwable cause, String parameterName, String parameterType) {
        super(cause);
        this.parameterName = parameterName;
        this.parameterType = parameterType;
    }

    public ParamIsNullException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String parameterName, String parameterType) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.parameterName = parameterName;
        this.parameterType = parameterType;
    }

    @Override
    public String getMessage() {
        return "Required " + this.parameterType + " parameter \'" + this.parameterName + "\' must be not null !";
    }
}
