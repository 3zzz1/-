package com.audit.rectification.service.impl;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.audit.rectification.config.WeComProperties;
import com.audit.rectification.service.IWeComMessageService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WeComMessageServiceImpl implements IWeComMessageService {

    private static final Logger log = LoggerFactory.getLogger(WeComMessageServiceImpl.class);
    private static final String API_BASE = "https://qyapi.weixin.qq.com/cgi-bin";

    private final WeComProperties properties;
    private String accessToken;
    private long expiresAt;

    public WeComMessageServiceImpl(WeComProperties properties) {
        this.properties = properties;
    }

    @Override
    public void sendTaskNotice(SysUser user, Long taskId, String title, String content) {
        if (!properties.isEnabled() || user == null || !hasConfig()) {
            return;
        }
        String toUser = resolveWeComUserId(user);
        if (StringUtils.isBlank(toUser)) {
            return;
        }
        try {
            JSONObject textcard = new JSONObject();
            textcard.put("title", value(title, "整改消息提醒"));
            textcard.put("description", buildDescription(content));
            textcard.put("url", buildTaskUrl(taskId));
            textcard.put("btntxt", "查看详情");

            JSONObject body = new JSONObject();
            body.put("touser", toUser);
            body.put("msgtype", "textcard");
            body.put("agentid", Integer.valueOf(properties.getAgentId()));
            body.put("textcard", textcard);
            body.put("safe", 0);

            String response = postJson(API_BASE + "/message/send?access_token=" + getAccessToken(), body.toJSONString());
            JSONObject result = JSON.parseObject(response);
            if (result == null || result.getIntValue("errcode") != 0) {
                log.warn("企业微信消息推送失败，userId={}, response={}", user.getUserId(), response);
            }
        } catch (Exception e) {
            log.warn("企业微信消息推送异常，userId={}", user.getUserId(), e);
        }
    }

    private synchronized String getAccessToken() throws Exception {
        long now = new Date().getTime();
        if (StringUtils.isNotBlank(accessToken) && now < expiresAt) {
            return accessToken;
        }
        String url = API_BASE + "/gettoken?corpid=" + encode(properties.getCorpId())
                + "&corpsecret=" + encode(properties.getSecret());
        String response = get(url);
        JSONObject result = JSON.parseObject(response);
        if (result == null || result.getIntValue("errcode") != 0) {
            throw new IllegalStateException("获取企业微信 access_token 失败");
        }
        accessToken = result.getString("access_token");
        expiresAt = now + Math.max(300, result.getIntValue("expires_in") - 300) * 1000L;
        return accessToken;
    }

    private String resolveWeComUserId(SysUser user) {
        String field = StringUtils.defaultString(properties.getUserIdField(), "username");
        if ("phone".equalsIgnoreCase(field)) {
            return user.getPhonenumber();
        }
        if ("email".equalsIgnoreCase(field)) {
            return user.getEmail();
        }
        return user.getUserName();
    }

    private String buildDescription(String content) {
        String text = value(content, "你有一条新的整改业务消息，请及时处理。");
        return "<div class=\"gray\">智慧审计平台系统</div><div class=\"normal\">" + escapeHtml(text) + "</div>";
    }

    private String buildTaskUrl(Long taskId) {
        String base = StringUtils.defaultIfBlank(properties.getAppUrl(), "http://localhost");
        if (taskId == null) {
            return trimRightSlash(base) + "/index";
        }
        return trimRightSlash(base) + "/rectification/task-page/detail/" + taskId;
    }

    private boolean hasConfig() {
        return StringUtils.isNotBlank(properties.getCorpId())
                && StringUtils.isNotBlank(properties.getAgentId())
                && StringUtils.isNotBlank(properties.getSecret());
    }

    private String get(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        return readResponse(connection);
    }

    private String postJson(String url, String json) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.setDoOutput(true);
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
        }
        return readResponse(connection);
    }

    private String readResponse(HttpURLConnection connection) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream(),
                StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } finally {
            connection.disconnect();
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String trimRightSlash(String value) {
        return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String value(String value, String fallback) {
        return StringUtils.isNotBlank(value) ? value : fallback;
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
