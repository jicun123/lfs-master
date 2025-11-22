package cn.lxinet.lfs.vo;

import cn.lxinet.lfs.message.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable{
    private int code;
    private String msg;
    private T data;
    private Page page;

    public Result() {

    }

    public Result(T data) {
        if (data instanceof Boolean){
            Boolean flag = (Boolean) data;
            if (flag == true){
                this.code = ErrorCode.SUCCESS.getCode();
                this.msg = ErrorCode.SUCCESS.getMsg();
            }else {
                this.code = ErrorCode.FAIL.getCode();
                this.msg = ErrorCode.FAIL.getMsg();
            }
        }else {
            this.data = data;
        }
    }

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static Result success(){
        Result result = new Result();
        result.setCode(ErrorCode.SUCCESS.getCode());
        result.setMsg(ErrorCode.SUCCESS.getMsg());
        return result;
    }

    public static Result success(Object data){
        Result result = new Result(data);
        result.setCode(ErrorCode.SUCCESS.getCode());
        result.setMsg(ErrorCode.SUCCESS.getMsg());
        return result;
    }

    public static Result fail(ErrorCode errorCode){
        Result result = new Result();
        result.setCode(errorCode.getCode());
        result.setMsg(errorCode.getMsg());
        return result;
    }

    public static Result success(Object data, Page page){
        Result result = new Result(data);
        result.setPage(page);
        return result;
    }

    public static Result response(int code, String msg, Object data) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
