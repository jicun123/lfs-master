package cn.lxinet.lfs.message;

public enum ErrorCode{
    SUCCESS(0, "操作成功"),
    FAIL(1001, "操作失败"),
    SYSTEM_EXCEPTION(1002, "系统异常"),
    PARAM_ERROR(1003, "参数错误"),
    UPLOADID_VOID(1004, "uploadId无效"),
    CHUNK_NUMBER_VERI_FAIL(1005, "chunkNumber校验失败"),
    FILE_NOT_EXIST(1006, "文件不存在"),
    VIDEO_TRANS_TEMPLATE_NOT_EXIST(1007, "视频转码模板不存在"),
    FILE_THUM_NOT_EXIST(1008, "缩略图不存在"),
    TRANS_SUCCESS_CANNOT_TRANS_AGAIN(1009, "已转码成功，无法再转码"),
    TRANS_IN_PROGRESS_CANNOT_TRANS_AGAIN(1010, "文件上传后2小时内，处于转码中，无法重新转码"),
    FILE_NOT_NEED_TRANS(1011, "该文件不需要转码"),
    FILE_NOT_SUPPORT_TRANS(1012, "该文件不支持转码"),
    TOKEN_INVALID(1013, "token无效"),
    FILE_DIR_NOT_EXIST(1014, "文件夹不存在"),
    FILE_DIR_MOVE_NOT_SELF_OR_SUBDIR(1015, "不能将文件移动到自身或其子目录下"),
    FILE_NAME_CANNOT_EMPTY(1016, "文件名不能为空"),
    FILE_NAME_CANNOT_EXCEED_200_CHARACTERS(1017, "文件名不能超过200个字符"),
    ;

    private Integer code;
    private String msg;

    ErrorCode(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
