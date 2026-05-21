package com.yupi.yupicturebackend.model.dto.approval;

import com.yupi.yupicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 审批查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PictureApprovalQueryRequest extends PageRequest implements Serializable {

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 申请人用户 id
     */
    private Long userId;

    /**
     * 目标空间 id
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
