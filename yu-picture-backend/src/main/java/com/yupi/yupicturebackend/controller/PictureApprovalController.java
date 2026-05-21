package com.yupi.yupicturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupicturebackend.annotation.AuthCheck;
import com.yupi.yupicturebackend.common.BaseResponse;
import com.yupi.yupicturebackend.common.ResultUtils;
import com.yupi.yupicturebackend.constant.UserConstant;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import com.yupi.yupicturebackend.model.dto.approval.PictureApprovalAddRequest;
import com.yupi.yupicturebackend.model.dto.approval.PictureApprovalQueryRequest;
import com.yupi.yupicturebackend.model.dto.approval.PictureApprovalReviewRequest;
import com.yupi.yupicturebackend.model.entity.PictureApproval;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.PictureApprovalVO;
import com.yupi.yupicturebackend.service.PictureApprovalService;
import com.yupi.yupicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/approval")
public class PictureApprovalController {

    @Resource
    private PictureApprovalService approvalService;

    @Resource
    private UserService userService;

    /**
     * 提交添加到空间的审批
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApproval(@RequestBody PictureApprovalAddRequest addRequest,
                                          HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long approvalId = approvalService.addApproval(addRequest, loginUser);
        return ResultUtils.success(approvalId);
    }

    /**
     * 审核审批（管理员）
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doApprovalReview(@RequestBody PictureApprovalReviewRequest reviewRequest,
                                                  HttpServletRequest request) {
        ThrowUtils.throwIf(reviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        approvalService.doApprovalReview(reviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取我的审批记录
     */
    @PostMapping("/list/my")
    public BaseResponse<Page<PictureApprovalVO>> listMyApprovals(@RequestBody PictureApprovalQueryRequest queryRequest,
                                                                  HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        queryRequest.setUserId(loginUser.getId());
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();
        Page<PictureApproval> approvalPage = approvalService.page(new Page<>(current, size),
                approvalService.getQueryWrapper(queryRequest));
        return ResultUtils.success(approvalService.getApprovalVOPage(approvalPage));
    }

    /**
     * 分页获取所有审批记录（管理员）
     */
    @PostMapping("/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<PictureApprovalVO>> listApprovals(@RequestBody PictureApprovalQueryRequest queryRequest) {
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();
        Page<PictureApproval> approvalPage = approvalService.page(new Page<>(current, size),
                approvalService.getQueryWrapper(queryRequest));
        return ResultUtils.success(approvalService.getApprovalVOPage(approvalPage));
    }
}
