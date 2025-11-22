package cn.lxinet.lfs.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 上传块 DTO
 * 分片上传参数
 *
 * @author zcx
 * @date 2023/11/09
 */
@Data
public class UploadChunkDto implements Serializable {
    /**
     * 文件夹id
     */
    private Long dirId = 0L;
    /**
     * 文件md5值
     */
    @NotNull(message = "md5不能为空")
    @Length(min = 32, max = 32, message = "md5格式错误")
    private String md5;
    /**
     * 当前第几片
     */
    @NotNull(message = "chunkNumber不能为空")
    @DecimalMin(value = "1", message = "chunkNumber必须为大于0的数字")
    private Integer chunkNumber;
    /**
     * 总分片数
     */
    @NotNull(message = "chunkTotal不能为空")
    @DecimalMin(value = "1", message = "chunkTotal必须为大于0的数字")
    private Integer chunkTotal;
    /**
     * 文件名
     */
    @NotBlank(message = "文件名不能为空")
    @Length(min = 1, max = 255, message = "文件名必须小于255个字符")
    private String fileName;
    /**
     * 上传id，第一片上传时为空，第二片开始，从第一片上传返回结果中读取该值并带上
     */
    @NotBlank(message = "uploadId不能为空")
    private String uploadId;
    /**
     * 分片文件
     */
    @NotNull(message = "file不能为空")
    private MultipartFile file;

}
