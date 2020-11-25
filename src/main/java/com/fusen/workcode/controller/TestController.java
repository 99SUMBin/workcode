package com.fusen.workcode.controller;


import com.fusen.workcode.utils.enums.ReturnCode;

/**
 * @author: WBin
 * @create: 2020-11-23 10:11
 * @description:
 */
public class TestController {


    public static void main(String[] args) {
        System.out.println(ReturnCode.SUCCESS.demo());
        System.out.println(ReturnCode.LOGIN_ERROR.demo());

    }
}
