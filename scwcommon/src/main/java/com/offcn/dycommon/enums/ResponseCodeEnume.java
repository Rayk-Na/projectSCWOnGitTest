package com.offcn.dycommon.enums;

public enum ResponseCodeEnume {
    SUCCESS(200,"操作成功"),
    FAIL(1,"服务器异常"),
    NOT_FOUND(404,"资源未找到"),
    NOT_AUTHED(403,"无权限，访问拒绝"),
    PARAM_INVAILD(400,"提交参数非法");

    ResponseCodeEnume() {
    }

    private String msg;
    private Integer code;

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

    ResponseCodeEnume(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }



}
