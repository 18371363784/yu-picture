package com.yupi.yupicturebackend.api.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetOutPaintingTaskResponse {

    @Alias("request_id")
    private String requestId;

    @Alias("output")
    private Output output;

    @Data
    public static class Output {
        @Alias("task_id")
        private String taskId;

        @Alias("task_status")
        private String taskStatus;

        @Alias("submit_time")
        private String submitTime;

        @Alias("scheduled_time")
        private String scheduledTime;

        @Alias("end_time")
        private String endTime;

        @Alias("output_image_url")
        private String outputImageUrl;

        @Alias("code")
        private String code;

        @Alias("message")
        private String message;

        @Alias("task_metrics")
        private TaskMetrics taskMetrics;
    }

    @Data
    public static class TaskMetrics {
        @Alias("total")
        private Integer total;

        @Alias("succeeded")
        private Integer succeeded;

        @Alias("failed")
        private Integer failed;
    }

}