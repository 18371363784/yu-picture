package com.yupi.yupicturebackend.model.vo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 审批记录视图
 */
@Data
public class PictureApprovalVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 图片 url
     */
    private String pictureUrl;

    /**
     * 图片名称
     */
    private String pictureName;

    /**
     * 目标空间 id
     */
    private Long spaceId;

    /**
     * 目标空间名称
     */
    private String spaceName;

    /**
     * 申请人用户 id
     */
    private Long userId;

    /**
     * 申请人信息
     */
    private UserVO user;

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
