package com.fusen.workcode.utils.enums;

/**
 * 枚举可以实现接口在每个实例中去实现方法
 */
public enum ReturnCode{
    //请求成功响应
    SUCCESS("0","请求成功") ,
    //登录失败响应
    LOGIN_ERROR("1001","登录错误");
//    INVALID_TOKEN("1002","token失效,请重新获取"){
//
//    },
//    INCORRECT_PARAMETER("1003","请求参数错误"),
//    SYSTEM_ERROR("1004","系统繁忙,请稍后重试");

    private String code;
    private String messege;

    //声明抽象方法,让每个枚举实例完成不同的动作
//    public abstract String info();

    ReturnCode(String code, String reason){
        this.code =code;
        this.messege = reason;
    }

    public String getCode() {
        return code;
    }

    public String getMessege() {
        return messege;
    }


}
