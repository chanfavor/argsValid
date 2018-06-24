package com.cd.springbootdemo.mybatisplusdemo.util;

import com.cd.springbootdemo.mybatisplusdemo.common.Result;

public class ResponseMsgUtil {
    public static Result<Object> builderResponse(int code, String message, Object o) {

        Result<Object> result = new Result<>(message, code, false, o.toString());

        return result;
    }
}
