package com.laysan.autojob.config;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * 异常处理器
 *
 * @Author
 * @Date
 */
@RestControllerAdvice
@Slf4j
public class ColabsExceptionHandler {
    /**
     * 处理自定义异常
     */
    @ExceptionHandler(BaseException.class)
    public Response handleBizException(BaseException e) {
        log.error(e.getMessage(), e);
        return Response.buildFailure(e.getErrCode(), e.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Response handlerNoFoundException(Exception e) {
        log.error(e.getMessage(), e);
        return Response.buildFailure("NotFound", "路径不存在，请检查路径是否正确");
    }


    /**
     * @param e
     * @return
     * @Author 政辉
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Response httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        StringBuffer sb = new StringBuffer();
        sb.append("不支持");
        sb.append(e.getMethod());
        sb.append("请求方法，");
        sb.append("支持以下");
        String[] methods = e.getSupportedMethods();
        if (methods != null) {
            StringJoiner stringJoiner = new StringJoiner(",");
            Arrays.stream(methods).forEach(stringJoiner::add);
            sb.append(stringJoiner.toString());
        }
        log.error(sb.toString(), e);
        return Response.buildFailure("MethodNotSupport", sb.toString());
    }


    @ExceptionHandler(Exception.class)
    public Response handleException(Exception e) {
        log.error(e.getMessage(), e);
        return Response.buildFailure("500", "操作失败，" + e.getMessage());
    }
}
