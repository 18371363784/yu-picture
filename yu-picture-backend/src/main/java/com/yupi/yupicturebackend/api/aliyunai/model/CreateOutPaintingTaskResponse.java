package com.yupi.yupicturebackend.api.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOutPaintingTaskResponse {

    @Alias("output")
    private Output output;

    @Data
    public static class Output {
        @Alias("task_id")
        private String taskId;

        @Alias("task_status")
        private String taskStatus;
    }

    @Alias("code")
    private String code;

    @Alias("message")
    private String message;

    @Alias("request_id")
    private String requestId;

}