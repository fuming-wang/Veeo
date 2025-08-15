package com.veeo.file.service.audit;

import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.StringMap;
import com.veeo.common.entity.Setting;
import com.veeo.common.entity.json.BodyJson;
import com.veeo.common.entity.json.ScoreJson;
import com.veeo.common.entity.json.SettingScoreJson;
import com.veeo.common.entity.response.AuditResponse;
import com.veeo.file.constant.AuditStatus;
import com.veeo.file.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @description:
 */
@Slf4j
@Service("videoAuditService")
public class VideoAuditService extends AbstractAuditService<String, AuditResponse> {

    static String VIDEO_URL = "http://ai.qiniuapi.com/v3/video/censor";
    static String VIDEO_BODY = """
            {
                "data": {
                    "uri": "${url}",
                    "id": "video_censor_test"
                },
                "params": {
                    "scenes": [
                        "pulp",
                        "terror",
                        "politician"
                    ],
                    "cut_param": {
                        "interval_msecs": 5000
                    }
                }
            }
            """;


    @Override
    public AuditResponse audit(String url) {
        log.error("视频自动审核开始  {}", url);
        AuditResponse auditResponse = new AuditResponse();
        auditResponse.setAuditStatus(AuditStatus.SUCCESS);

        if (!isNeedAudit()) {
            return auditResponse;
        }
        url = appendUUID(url);

        String body = VIDEO_BODY.replace("${url}", url);
        StringMap header = getHeader(VIDEO_URL, body);
        Client client = getClient();
        try {
            Response response = client.post(VIDEO_URL, body.getBytes(), header, contentType);
            Map map = objectMapper.readValue(response.getInfo().split(" \n")[2], Map.class);
            Object job = map.get("job");
            url = "http://ai.qiniuapi.com/v3/jobs/video/" + job.toString();
            header = new StringMap();
            header.put(HOST_KEY, HOST_VALUE);
            header.put(AUTH, qiNiuConfig.getToken(url, GET, null, null));
            while (true) {
                Response response1 = client.get(url, header);
                BodyJson bodyJson = objectMapper.readValue(response1.getInfo().split(" \n")[2], BodyJson.class);
                if ("FINISHED".equals(bodyJson.getStatus())) {
                    // 1.从系统配置表获取 pulp politician terror比例
                    Setting setting = (Setting) redisCacheUtil.get(RedisConstant.SETTING_CACHE);
                    SettingScoreJson settingScoreRule = objectMapper.readValue(setting.getAuditPolicy(),
                            SettingScoreJson.class);
                    List<ScoreJson> auditRule = Arrays.asList(settingScoreRule.getManualScore(),
                            settingScoreRule.getPassScore(), settingScoreRule.getSuccessScore());
                    auditResponse = audit(auditRule, bodyJson);
                    log.error("视频自动审核完成  {}", auditResponse);
                    return auditResponse;
                }
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            log.error("视频自动审核失败  {}", e.getMessage(), e);
        }
        return auditResponse;
    }
}
