package com.yupi.yupicturebackend.model.dto.approval;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片添加到空间的审批请求
 */
@Data
public class PictureApprovalAddRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 目标空间 id
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
