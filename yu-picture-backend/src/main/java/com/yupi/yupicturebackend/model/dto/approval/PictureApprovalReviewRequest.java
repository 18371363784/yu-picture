package com.yupi.yupicturebackend.model.dto.approval;

import lombok.Data;

import java.io.Serializable;

/**
 * 审批审核请求
 */
@Data
public class PictureApprovalReviewRequest implements Serializable {

    /**
     * 审批记录 id
     */
    private Long id;

    /**
     * 审核状态：1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}
