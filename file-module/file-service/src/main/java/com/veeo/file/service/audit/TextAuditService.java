package com.veeo.file.service.audit;

import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.StringMap;
import com.veeo.common.entity.json.DetailsJson;
import com.veeo.common.entity.json.ResultChildJson;
import com.veeo.common.entity.response.AuditResponse;
import com.veeo.file.constant.AuditMsgMap;
import com.veeo.file.constant.AuditStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;

/**
 * @description: 内容审核
 */
@Slf4j
@Service("textAuditService")
public class TextAuditService extends AbstractAuditService<String, AuditResponse> {

    static String TEXT_URL = "http://ai.qiniuapi.com/v3/text/censor";
    static String textBody = """
            {
                "data": {
                    "text": "${text}"
                },
                "params": {
                    "scenes": [
                        "antispam"
                    ]
                }
            }
            """;

    @Override
    public AuditResponse audit(String text) {
        AuditResponse auditResponse = new AuditResponse();
        auditResponse.setAuditStatus(AuditStatus.SUCCESS);

        if (!isNeedAudit()) {
            return auditResponse;
        }

        String body = textBody.replace("${text}", text);
        StringMap header = getHeader(TEXT_URL, body);
        Client client = getClient();
        try {
            Response response = client.post(TEXT_URL, body.getBytes(), header, contentType);

            Map map = objectMapper.readValue(response.getInfo().split(" \n")[2], Map.class);
            ResultChildJson result = objectMapper.convertValue(map.get("result"), ResultChildJson.class);
            auditResponse.setAuditStatus(AuditStatus.SUCCESS);
            // 文本审核直接审核suggestion
            if (!"pass".equals(result.getSuggestion())) {
                auditResponse.setAuditStatus(AuditStatus.PASS);
                List<DetailsJson> details = result.getScenes().getAntispam().getDetails();
                if (!ObjectUtils.isEmpty(details)) {
                    // 遍历找到有问题的
                    for (DetailsJson detail : details) {
                        if (!"normal".equals(detail.getLabel())) {
                            auditResponse.setMsg(AuditMsgMap.getInfo(detail.getLabel()) + "\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("文字自动审核失败 {}", e.getMessage(), e);
        }
        log.error("文字自动审核完成 {}, {}", auditResponse.getAuditStatus(), text);
        return auditResponse;
    }
}
