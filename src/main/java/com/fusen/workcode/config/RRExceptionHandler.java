package com.fusen.workcode.config;


import com.fusen.workcode.pojo.JsonResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class RRExceptionHandler {

    private static Log log = LogFactory.getLog(RRExceptionHandler.class);


    /**
     * @description 处理参数校验异常   @Validated+@NotNull搭配使用
     * @date 14:52 2019-02-15
     **/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public JsonResult handleException1(MethodArgumentNotValidException e){
        log.error(e.getMessage(), e);
        return JsonResult.failMsg((e.getBindingResult().getAllErrors()).get(0).getDefaultMessage());
    }

    /**
     * 校验枚举类型不正确(如果没有传该枚举字段的话,不会触发异常)
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public JsonResult handleException(Exception e){
        log.error(e.getMessage());
        String s = "not one of the values accepted for Enum class";
        if (e.getMessage().contains(s)){
            String field = "";
            String regex = "(?<=enums.)[A-Za-z]+";
            Matcher matcher = Pattern.compile(regex).matcher(e.getMessage());
            if (matcher.find()){
                field = matcher.group();
            }
            return JsonResult.failMsg(field+"请求参数不符合枚举类内容");
        }
        return JsonResult.failMsg("");
    }

    /**
     * 方法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public JsonResult handleException2(IllegalArgumentException e){
        log.error(e.getMessage(), e);
        return JsonResult.failMsg(e.getMessage());
    }



}
