package com.fusen.workcode.pojo;

/**
 * @Description 自定义响应数据结构
 * 				这个类是提供给门户，ios，安卓，微信商城用的
 * 				门户接受此类数据后需要使用本类的方法转换成对于的数据类型格式（类，或者list）
 * 				其他自行处理

 *@author chenb
 *@date  2018-04-17
 */
public class JsonResult {

    // 定义jackson对象
    // private static final ObjectMapper MAPPER = new ObjectMapper();


    // 响应消息
    private String result;

    // 响应中的数据
    private Object data;


    private JsonResult(String result, Object data) {
        this.result = result;
        this.data = data;
    }

    public static JsonResult build(String result, Object data){
        return new JsonResult(result, data);
    }

    public static JsonResult buildSuccess(Object data) {
        return new JsonResult("success", data);
    }

    public static JsonResult buildFail(Object data) {
        return new JsonResult("fail", data);
    }

    public static JsonResult success() {
        return new JsonResult("success", "");
    }

    public static JsonResult successMsg(String msg) {
        return new JsonResult("success", msg);
    }

    public static JsonResult fail() {
        return new JsonResult("fail", "");
    }

    public static JsonResult failMsg(String msg) {
        return new JsonResult("fail", msg);
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
