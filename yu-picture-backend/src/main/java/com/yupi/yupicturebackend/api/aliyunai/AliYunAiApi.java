package com.yupi.yupicturebackend.api.aliyunai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.yupi.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.yupi.yupicturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AliYunAiApi {

    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";


    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        if (createOutPaintingTaskRequest == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "扩图参数为空");
        }
        String requestBody = JSONUtil.toJsonStr(createOutPaintingTaskRequest);
        log.info("AI扩图请求体：{}", requestBody);
        try (HttpResponse httpResponse = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("X-DashScope-Async", "enable")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .execute()) {
            log.info("AI扩图响应码：{}，响应体：{}", httpResponse.getStatus(), httpResponse.body());
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败");
            }
            JSONObject json = JSONUtil.parseObj(httpResponse.body());
            if (json.containsKey("code") && json.getStr("code") != null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败，" + json.getStr("message"));
            }
            CreateOutPaintingTaskResponse response = new CreateOutPaintingTaskResponse();
            JSONObject outputJson = json.getJSONObject("output");
            CreateOutPaintingTaskResponse.Output output = new CreateOutPaintingTaskResponse.Output();
            output.setTaskId(outputJson.getStr("task_id"));
            output.setTaskStatus(outputJson.getStr("task_status"));
            response.setOutput(output);
            response.setRequestId(json.getStr("request_id"));
            return response;
        }
    }

    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务 ID 不能为空");
        }
        String url = String.format(GET_OUT_PAINTING_TASK_URL, taskId);
        try (HttpResponse httpResponse = HttpRequest.get(url)
                .header("Authorization", "Bearer " + apiKey)
                .execute()) {
            log.info("AI查询任务响应码：{}，响应体：{}", httpResponse.getStatus(), httpResponse.body());
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务结果失败");
            }
            JSONObject json = JSONUtil.parseObj(httpResponse.body());
            GetOutPaintingTaskResponse response = new GetOutPaintingTaskResponse();
            JSONObject outputJson = json.getJSONObject("output");
            GetOutPaintingTaskResponse.Output output = new GetOutPaintingTaskResponse.Output();
            output.setTaskId(outputJson.getStr("task_id"));
            output.setTaskStatus(outputJson.getStr("task_status"));
            output.setOutputImageUrl(outputJson.getStr("output_image_url"));
            output.setSubmitTime(outputJson.getStr("submit_time"));
            output.setScheduledTime(outputJson.getStr("scheduled_time"));
            output.setEndTime(outputJson.getStr("end_time"));
            output.setCode(outputJson.getStr("code"));
            output.setMessage(outputJson.getStr("message"));
            if (outputJson.containsKey("task_metrics")) {
                JSONObject metricsJson = outputJson.getJSONObject("task_metrics");
                GetOutPaintingTaskResponse.TaskMetrics metrics = new GetOutPaintingTaskResponse.TaskMetrics();
                metrics.setTotal(metricsJson.getInt("total"));
                metrics.setSucceeded(metricsJson.getInt("succeeded"));
                metrics.setFailed(metricsJson.getInt("failed"));
                output.setTaskMetrics(metrics);
            }
            response.setOutput(output);
            response.setRequestId(json.getStr("request_id"));
            return response;
        }
    }
}
