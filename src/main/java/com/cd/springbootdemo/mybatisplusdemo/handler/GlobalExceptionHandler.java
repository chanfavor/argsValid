package com.cd.springbootdemo.mybatisplusdemo.handler;

import com.cd.springbootdemo.mybatisplusdemo.common.Result;
import com.cd.springbootdemo.mybatisplusdemo.constant.EnumResultCode;
import com.cd.springbootdemo.mybatisplusdemo.exception.ParamIsNullException;
import com.cd.springbootdemo.mybatisplusdemo.util.ResponseMsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 参数为空异常处理
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({MissingServletRequestParameterException.class, ParamIsNullException.class})
    public Result<Object> requestMissingServletRequest(Exception ex) {

        log.error("request Exception:", ex);
        return ResponseMsgUtil.builderResponse(EnumResultCode.FAIL.getCode(), ex.getMessage(),"异常~~~");
    }

    /**
     * 特别说明： 可以配置指定的异常处理,这里处理所有
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public Result<Object> errorHandler(HttpServletRequest request, Exception e) {

        log.error("request Exception:", e);
        return ResponseMsgUtil.builderResponse(EnumResultCode.FAIL.getCode(), e.getMessage(),"Exception异常~~~");
    }
}
