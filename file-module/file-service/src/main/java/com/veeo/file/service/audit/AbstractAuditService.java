package com.veeo.file.service.audit;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniu.http.Client;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.StringMap;
import com.veeo.common.config.LocalCache;
import com.veeo.file.config.QiNiuConfig;
import com.veeo.common.entity.Setting;
import com.veeo.common.entity.json.*;
import com.veeo.common.entity.response.AuditResponse;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.file.constant.AuditMsgMap;
import com.veeo.file.constant.AuditStatus;
import com.veeo.file.constant.RedisConstant;
import com.veeo.file.service.AuditService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.UUID;

/**
 * @description: 统一封装审核逻辑，并留给子类进行编排或者调用普通逻辑
 */
@Service
public abstract class AbstractAuditService<T, R> implements AuditService<T, R> {

    static final String contentType = "application/json";
    static final String CONTENT_TYPE = "Content-Type";
    static final String HOST_VALUE = "ai.qiniuapi.com";
    static final String HOST_KEY = "Host";
    static final String AUTH = "Authorization";
    static final String POST = "post";
    static final String GET = "get";

    private final Configuration cfg = new Configuration(Region.region2());
    private final Client client = new Client(cfg);

    @Resource
    protected QiNiuConfig qiNiuConfig;

    @Resource
    protected LocalCache localCache;

    @Resource
    protected RedisCacheUtil redisCacheUtil;

    protected ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * 审核
     *
     * @param scoreJsonList x
     * @param bodyJson      x
     * @return x
     */
    protected AuditResponse audit(List<ScoreJson> scoreJsonList, BodyJson bodyJson) {
        AuditResponse audit = new AuditResponse();
        // 遍历的是通过,人工,失败的审核规则,我当前没有办法知道是什么状态
        for (ScoreJson scoreJson : scoreJsonList) {
            audit = audit(scoreJson, bodyJson);
            // 如果为true,说明命中得分，提前返回
            if (audit.getFlag()) {
                audit.setAuditStatus(scoreJson.getAuditStatus());
                return audit;
            }
        }
        // 如果出来了说明审核的内容没分数 / 审核比例没调好(人员问题)
        // 比较suggest
        ScenesJson scenes = bodyJson.getResult().getResult().getScenes();
        if (endCheck(scenes)) {
            audit.setAuditStatus(AuditStatus.SUCCESS);
        } else {
            audit.setAuditStatus(AuditStatus.PASS);
            audit.setMsg("内容不合法");
        }
        return audit;
    }

    protected StringMap getHeader(String url, String body) {
        String token = qiNiuConfig.getToken(url, POST, body, contentType);
        StringMap header = new StringMap();
        header.put(HOST_KEY, HOST_VALUE);
        header.put(AUTH, token);
        header.put(CONTENT_TYPE, contentType);
        return header;
    }

    protected Client getClient() {
        return client;
    }


    /**
     * 返回对应规则的信息
     *
     * @param types         x
     * @param minPolitician x
     * @return x
     */
    private AuditResponse getInfo(List<CutsJson> types, Double minPolitician, String key) {
        AuditResponse auditResponse = new AuditResponse();
        auditResponse.setFlag(false);
        String info;
        // 获取信息
        for (CutsJson type : types) {
            for (DetailsJson detail : type.getDetails()) {
                // 人工/PASS ? 交给七牛云状态，我只获取信息和offset
                if (detail.getScore() > minPolitician) {
                    // 如果违规,则填充额外信息
                    if (!detail.getLabel().equals(key)) {
                        info = AuditMsgMap.getInfo(detail.getLabel());
                        auditResponse.setMsg(info);
                        auditResponse.setOffset(type.getOffset());
                    }
                    auditResponse.setFlag(true);
                }

            }
        }
        if (auditResponse.getFlag() && ObjectUtils.isEmpty(auditResponse.getMsg())) {
            auditResponse.setMsg("该视频违法Veeo平台规则");
        }

        return auditResponse;
    }


    /**
     * 当前审核规则如果能匹配上也就是进入了if判断中,则需要获取违规信息
     * 如果走到末尾则说明没有匹配上
     *
     * @param scoreJson x
     * @param bodyJson  x
     * @return x
     */
    private AuditResponse audit(ScoreJson scoreJson, BodyJson bodyJson) {

        AuditResponse auditResponse = new AuditResponse();
        auditResponse.setFlag(true);
        auditResponse.setAuditStatus(scoreJson.getAuditStatus());

        Double minPolitician = scoreJson.getMinPolitician();
        Double maxPolitician = scoreJson.getMaxPolitician();
        Double minPulp = scoreJson.getMinPulp();
        Double maxPulp = scoreJson.getMaxPulp();
        Double minTerror = scoreJson.getMinTerror();
        Double maxTerror = scoreJson.getMaxTerror();

        // 所有都要比较,如果返回的有问题则直接返回
        if (!ObjectUtils.isEmpty(bodyJson.getPolitician())) {
            if (bodyJson.checkViolation(bodyJson.getPolitician(), minPolitician, maxPolitician)) {
                AuditResponse response = getInfo(bodyJson.getPolitician(), minPolitician, "group");
                auditResponse.setMsg(response.getMsg());
                if (response.getFlag()) {
                    auditResponse.setOffset(response.getOffset());
                    return auditResponse;
                }
            }
        }
        if (!ObjectUtils.isEmpty(bodyJson.getPulp())) {
            if (bodyJson.checkViolation(bodyJson.getPulp(), minPulp, maxPulp)) {
                AuditResponse response = getInfo(bodyJson.getPulp(), minPulp, "normal");
                auditResponse.setMsg(response.getMsg());
                // 如果违规则提前返回
                if (response.getFlag()) {
                    auditResponse.setOffset(response.getOffset());
                    return auditResponse;
                }
            }
        }
        if (!ObjectUtils.isEmpty(bodyJson.getTerror())) {
            if (bodyJson.checkViolation(bodyJson.getTerror(), minTerror, maxTerror)) {
                AuditResponse response = getInfo(bodyJson.getTerror(), minTerror, "normal");
                auditResponse.setMsg(response.getMsg());
                if (response.getFlag()) {
                    auditResponse.setOffset(response.getOffset());
                    return auditResponse;
                }
            }
        }
        auditResponse.setMsg("正常");
        auditResponse.setFlag(false);
        return auditResponse;
    }

    /**
     * 最后检查,可能没得分,检查suggestion
     *
     * @param scenes x
     * @return x
     */
    private boolean endCheck(ScenesJson scenes) {
        TypeJson terror = scenes.getTerror();
        TypeJson politician = scenes.getPolitician();
        TypeJson pulp = scenes.getPulp();
        return !"block".equals(terror.getSuggestion()) &&
                !"block".equals(politician.getSuggestion()) &&
                !"block".equals(pulp.getSuggestion());
    }

    /**
     * 根据系统配置表查询是否需要审核
     *
     * @return x
     */
    protected Boolean isNeedAudit() {
        Setting setting = (Setting) redisCacheUtil.get(RedisConstant.SETTING_CACHE);
        return setting.getAuditOpen();
    }


    protected String appendUUID(String url) {
        Setting setting = (Setting) redisCacheUtil.get(RedisConstant.SETTING_CACHE);
        if (setting.getAuth()) {
            String uuid = UUID.randomUUID().toString();
            localCache.put(uuid, true);
            if (url.contains("?")) {
                url = url + "&uuid=" + uuid;
            } else {
                url = url + "?uuid=" + uuid;
            }
            return url;
        }
        return url;
    }

}
