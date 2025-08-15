package com.veeo.file.service.audit;

import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.StringMap;
import com.veeo.file.config.QiNiuConfig;
import com.veeo.common.entity.Setting;
import com.veeo.common.entity.json.*;
import com.veeo.common.entity.response.AuditResponse;
import com.veeo.file.constant.AuditStatus;
import com.veeo.file.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @description: 图片审核
 */
@Slf4j
@Service("imageAuditService")
public class ImageAuditService extends AbstractAuditService<String, AuditResponse> {

    static String imageUlr = "http://ai.qiniuapi.com/v3/image/censor";
    static String imageBody = """
            {
                "data": {
                    "uri": "${url}"
                },
                "params": {
                    "scenes": [
                        "pulp",
                        "terror",
                        "politician"
                    ]
                }
            }
            """;

    @Override
    public AuditResponse audit(String url) {
        log.info("图像自动审核开始 {}", url);
        AuditResponse auditResponse = new AuditResponse();
        auditResponse.setAuditStatus(AuditStatus.SUCCESS);
        if (!isNeedAudit()) {
            return auditResponse;
        }
        try {
            if (!url.contains(QiNiuConfig.CNAME)) {
                String encodedFileName = URLEncoder.encode(url, StandardCharsets.UTF_8).replace("+", "%20");
                url = String.format("%s/%s", QiNiuConfig.CNAME, encodedFileName);
            }
            url = appendUUID(url);

            String body = imageBody.replace("${url}", url);
            // 获取token
            StringMap header = getHeader(imageUlr, body);
            Client client = getClient();
            Response response = client.post(imageUlr, body.getBytes(), header, contentType);
            Map map = objectMapper.readValue(response.getInfo().split(" \n")[2], Map.class);
            ResultChildJson result = objectMapper.convertValue(map.get("result"), ResultChildJson.class);
            BodyJson bodyJson = new BodyJson();
            ResultJson resultJson = new ResultJson();
            resultJson.setResult(result);
            bodyJson.setResult(resultJson);

            Setting setting = (Setting) redisCacheUtil.get(RedisConstant.SETTING_CACHE);
            SettingScoreJson settingScoreRule = objectMapper.readValue(setting.getAuditPolicy(), SettingScoreJson.class);

            List<ScoreJson> auditRule = Arrays.asList(settingScoreRule.getManualScore(),
                    settingScoreRule.getPassScore(), settingScoreRule.getSuccessScore());
            // 审核
            auditResponse = audit(auditRule, bodyJson);
            log.info("图像自动审核完成 {} {}", auditResponse.getAuditStatus(), url);
            return auditResponse;
        } catch (Exception e) {
            auditResponse.setAuditStatus(AuditStatus.PROCESS);
            log.error("图像自动审核错误 {}", e.getMessage(), e);
        }
        return auditResponse;
    }
}
