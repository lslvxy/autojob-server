package com.laysan.autojob.core.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrJoiner;
import cn.hutool.core.util.StrUtil;
import com.laysan.autojob.core.entity.TaskLog;
import com.laysan.autojob.core.entity.User;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author lise
 * @version MessageService.java, v 0.1 2020年11月26日 14:11 lise
 */

@Service
@Slf4j
public class MessageService {
    @Resource
    UserService userService;
    @Resource
    TaskLogService taskLogService;

    public void sendMessage(Long userId) {

        //public void sendMessage(String openId, String title, String detail) {
        User user = userService.findById(userId);
        if (Objects.isNull(user) || StrUtil.isBlank(user.getSctKey())) {
            return;
        }
        if (!user.todayCompleted()) {
            log.info("用户所有任务全部完成后再发送消息，今日已执行:{},任务总数:{}", user.getTodayRunCount(), user.getTotalAccountCount());
            return;
        }
        String title = StrUtil.format("[AutoJob]{}执行结果", DateUtil.today());
        List<TaskLog> taskLogs = taskLogService.finaAllToday(userId);
        StrJoiner joiner = new StrJoiner("\n\n");
        taskLogs.forEach(v -> {
            String detail = v.getDetail();
            String type = v.getTypeName();
            joiner.append(type + "：" + detail);
        });
        sendSct(user.getSctKey(), title, joiner.toString());
    }

    private void sendSct(String sendKey, String title, String detail) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        RequestBody body = new FormBody.Builder().add("title", title).add("desp", detail).build();
        Request request = new Request.Builder().url("https://sctapi.ftqq.com/" + sendKey + ".send").post(body).build();
        try {
            Response execute = client.newCall(request).execute();
            String string = execute.body().string();
            log.info(string);

        } catch (IOException e) {
            log.error("发送消息失败");
        }
    }


}