package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.model.dto.approval.PictureApprovalAddRequest;
import com.yupi.yupicturebackend.model.dto.approval.PictureApprovalQueryRequest;
import com.yupi.yupicturebackend.model.dto.approval.PictureApprovalReviewRequest;
import com.yupi.yupicturebackend.model.entity.PictureApproval;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.PictureApprovalVO;

/**
 * @author 鱼皮
 * @description 针对表【picture_approval(图片审批)】的数据库操作Service
 */
public interface PictureApprovalService extends IService<PictureApproval> {

    /**
     * 提交审批请求
     */
    Long addApproval(PictureApprovalAddRequest addRequest, User loginUser);

    /**
     * 审核审批
     */
    void doApprovalReview(PictureApprovalReviewRequest reviewRequest, User loginUser);

    /**
     * 获取查询条件
     */
    QueryWrapper<PictureApproval> getQueryWrapper(PictureApprovalQueryRequest queryRequest);

    /**
     * 获取审批视图对象
     */
    PictureApprovalVO getApprovalVO(PictureApproval approval);

    /**
     * 分页获取审批视图
     */
    Page<PictureApprovalVO> getApprovalVOPage(Page<PictureApproval> approvalPage);
}
