package cn.lxinet.lfs.exception;

import cn.lxinet.lfs.message.ErrorCode;

/**
 * 自定义异常
 *
 * @author zcx
 * @date 2023/11/23
 */
public class LfsException extends RuntimeException {
    private Integer code;
    private String msg;

    public LfsException(){
        super();
    }

    public LfsException(String msg){
        super(msg);
        this.msg = msg;
    }

    public LfsException(Integer code, String msg){
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public LfsException(ErrorCode message){
        super(message.getMsg());
        this.code = message.getCode();
        this.msg = message.getMsg();
    }

    public LfsException(ErrorCode message, String msg){
        super(message.getMsg());
        this.code = message.getCode();
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
