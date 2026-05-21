package com.yupi.yupicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 图片审批状态枚举类
 */
@Getter
public enum PictureApprovalStatusEnum {

    PENDING("待审核", 0),
    APPROVED("通过", 1),
    REJECTED("拒绝", 2);

    private final String text;

    private final int value;

    PictureApprovalStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static PictureApprovalStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PictureApprovalStatusEnum anEnum : PictureApprovalStatusEnum.values()) {
            if (anEnum.value == value) {
                return anEnum;
            }
        }
        return null;
    }
}
