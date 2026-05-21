package com.yupi.yupicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import com.yupi.yupicturebackend.mapper.PictureApprovalMapper;
import com.yupi.yupicturebackend.model.dto.approval.PictureApprovalAddRequest;
import com.yupi.yupicturebackend.model.dto.approval.PictureApprovalQueryRequest;
import com.yupi.yupicturebackend.model.dto.approval.PictureApprovalReviewRequest;
import com.yupi.yupicturebackend.model.entity.Picture;
import com.yupi.yupicturebackend.model.entity.PictureApproval;
import com.yupi.yupicturebackend.model.entity.Space;
import com.yupi.yupicturebackend.model.entity.SpaceUser;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.enums.PictureApprovalStatusEnum;
import com.yupi.yupicturebackend.model.enums.SpaceTypeEnum;
import com.yupi.yupicturebackend.model.vo.PictureApprovalVO;
import com.yupi.yupicturebackend.model.vo.UserVO;
import com.yupi.yupicturebackend.service.PictureApprovalService;
import com.yupi.yupicturebackend.service.PictureService;
import com.yupi.yupicturebackend.service.SpaceService;
import com.yupi.yupicturebackend.service.SpaceUserService;
import com.yupi.yupicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PictureApprovalServiceImpl extends ServiceImpl<PictureApprovalMapper, PictureApproval>
        implements PictureApprovalService {

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public Long addApproval(PictureApprovalAddRequest addRequest, User loginUser) {
        ThrowUtils.throwIf(addRequest == null || addRequest.getPictureId() == null || addRequest.getSpaceId() == null,
                ErrorCode.PARAMS_ERROR);
        Long pictureId = addRequest.getPictureId();
        Long spaceId = addRequest.getSpaceId();
        // 检查图片是否存在且为公共图库图片
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        ThrowUtils.throwIf(picture.getSpaceId() != null, ErrorCode.PARAMS_ERROR, "该图片已在某个空间中");
        // 检查目标空间是否存在
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "目标空间不存在");
        // 检查用户与目标空间的关系
        Integer spaceType = space.getSpaceType();
        if (SpaceTypeEnum.PRIVATE.getValue() == spaceType) {
            // 私有空间：只有空间主人可以申请添加
            ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()),
                    ErrorCode.NO_AUTH_ERROR, "您不是该私有空间的主人");
        } else {
            // 团队空间：需要是空间成员
            QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("spaceId", spaceId);
            queryWrapper.eq("userId", loginUser.getId());
            SpaceUser spaceUser = spaceUserService.getOne(queryWrapper);
            ThrowUtils.throwIf(spaceUser == null, ErrorCode.NO_AUTH_ERROR, "您不是该团队空间的成员");
        }
        // 检查是否已有待审核的记录
        QueryWrapper<PictureApproval> approvalQuery = new QueryWrapper<>();
        approvalQuery.eq("pictureId", pictureId);
        approvalQuery.eq("spaceId", spaceId);
        approvalQuery.eq("userId", loginUser.getId());
        approvalQuery.eq("reviewStatus", PictureApprovalStatusEnum.PENDING.getValue());
        long count = this.count(approvalQuery);
        ThrowUtils.throwIf(count > 0, ErrorCode.OPERATION_ERROR, "已有待审核的申请，请勿重复提交");
        // 创建审批记录
        PictureApproval approval = new PictureApproval();
        approval.setPictureId(pictureId);
        approval.setPictureUrl(picture.getUrl());
        approval.setPictureName(picture.getName());
        approval.setSpaceId(spaceId);
        approval.setUserId(loginUser.getId());
        approval.setReviewStatus(PictureApprovalStatusEnum.PENDING.getValue());
        boolean result = this.save(approval);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "提交申请失败");
        return approval.getId();
    }

    @Override
    public void doApprovalReview(PictureApprovalReviewRequest reviewRequest, User loginUser) {
        ThrowUtils.throwIf(reviewRequest == null || reviewRequest.getId() == null || reviewRequest.getReviewStatus() == null,
                ErrorCode.PARAMS_ERROR);
        Integer reviewStatus = reviewRequest.getReviewStatus();
        ThrowUtils.throwIf(!reviewStatus.equals(PictureApprovalStatusEnum.APPROVED.getValue())
                        && !reviewStatus.equals(PictureApprovalStatusEnum.REJECTED.getValue()),
                ErrorCode.PARAMS_ERROR, "审核状态不合法");
        // 查询审批记录
        PictureApproval approval = this.getById(reviewRequest.getId());
        ThrowUtils.throwIf(approval == null, ErrorCode.NOT_FOUND_ERROR, "审批记录不存在");
        ThrowUtils.throwIf(!approval.getReviewStatus().equals(PictureApprovalStatusEnum.PENDING.getValue()),
                ErrorCode.OPERATION_ERROR, "该申请已处理过");
        // 获取原始图片
        Picture originalPicture = pictureService.getById(approval.getPictureId());
        ThrowUtils.throwIf(originalPicture == null, ErrorCode.NOT_FOUND_ERROR, "原始图片不存在");
        // 获取目标空间
        Space space = spaceService.getById(approval.getSpaceId());
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "目标空间不存在");
        if (reviewStatus.equals(PictureApprovalStatusEnum.APPROVED.getValue())) {
            // 检查空间容量
            if (space.getTotalCount() >= space.getMaxCount()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数不足");
            }
            if (space.getTotalSize() >= space.getMaxSize()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
            }
            // 在事务中复制图片到目标空间
            transactionTemplate.execute(status -> {
                Picture newPicture = new Picture();
                BeanUtil.copyProperties(originalPicture, newPicture);
                newPicture.setId(null);
                newPicture.setSpaceId(space.getId());
                newPicture.setUserId(approval.getUserId());
                newPicture.setCreateTime(new Date());
                newPicture.setEditTime(new Date());
                newPicture.setUpdateTime(new Date());
                boolean saved = pictureService.save(newPicture);
                ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "复制图片失败");
                // 更新空间使用额度
                boolean spaceUpdated = spaceService.lambdaUpdate()
                        .eq(Space::getId, space.getId())
                        .setSql("totalSize = totalSize + " + originalPicture.getPicSize())
                        .setSql("totalCount = totalCount + 1")
                        .update();
                ThrowUtils.throwIf(!spaceUpdated, ErrorCode.OPERATION_ERROR, "额度更新失败");
                return null;
            });
        }
        // 更新审批记录
        approval.setReviewStatus(reviewStatus);
        approval.setReviewMessage(reviewRequest.getReviewMessage());
        approval.setReviewerId(loginUser.getId());
        approval.setReviewTime(new Date());
        boolean result = this.updateById(approval);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public QueryWrapper<PictureApproval> getQueryWrapper(PictureApprovalQueryRequest queryRequest) {
        QueryWrapper<PictureApproval> queryWrapper = new QueryWrapper<>();
        if (queryRequest == null) {
            return queryWrapper;
        }
        Long userId = queryRequest.getUserId();
        Integer reviewStatus = queryRequest.getReviewStatus();
        Long spaceId = queryRequest.getSpaceId();
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotNull(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotNull(spaceId), "spaceId", spaceId);
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public PictureApprovalVO getApprovalVO(PictureApproval approval) {
        if (approval == null) {
            return null;
        }
        PictureApprovalVO vo = new PictureApprovalVO();
        BeanUtil.copyProperties(approval, vo);
        // 填充空间名称
        Space space = spaceService.getById(approval.getSpaceId());
        if (space != null) {
            vo.setSpaceName(space.getSpaceName());
        }
        // 填充用户信息
        User user = userService.getById(approval.getUserId());
        if (user != null) {
            UserVO userVO = new UserVO();
            BeanUtil.copyProperties(user, userVO);
            vo.setUser(userVO);
        }
        return vo;
    }

    @Override
    public Page<PictureApprovalVO> getApprovalVOPage(Page<PictureApproval> approvalPage) {
        List<PictureApproval> approvalList = approvalPage.getRecords();
        Page<PictureApprovalVO> voPage = new Page<>(approvalPage.getCurrent(), approvalPage.getSize(), approvalPage.getTotal());
        if (CollUtil.isEmpty(approvalList)) {
            return voPage;
        }
        // 批量获取空间名称
        Set<Long> spaceIdSet = approvalList.stream().map(PictureApproval::getSpaceId).collect(Collectors.toSet());
        Map<Long, String> spaceNameMap = spaceService.listByIds(spaceIdSet).stream()
                .collect(Collectors.toMap(Space::getId, Space::getSpaceName, (k1, k2) -> k1));
        // 批量获取用户信息
        Set<Long> userIdSet = approvalList.stream().map(PictureApproval::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 组装VO
        List<PictureApprovalVO> voList = approvalList.stream().map(approval -> {
            PictureApprovalVO vo = new PictureApprovalVO();
            BeanUtil.copyProperties(approval, vo);
            vo.setSpaceName(spaceNameMap.getOrDefault(approval.getSpaceId(), ""));
            User user = userIdUserMap.getOrDefault(approval.getUserId(), CollUtil.newArrayList()).stream().findFirst().orElse(null);
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtil.copyProperties(user, userVO);
                vo.setUser(userVO);
            }
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }
}
