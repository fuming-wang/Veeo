package com.veeo.interest.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeo.common.constant.RedisConstant;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.HotVideo;
import com.veeo.common.entity.vo.Model;
import com.veeo.common.entity.vo.UserModel;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.interest.service.InterestPushService;
import com.veeo.video.api.TypeClient;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;


import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @description:
 */
// 暂时为异步
@Service
public class InterestPushServiceImpl implements InterestPushService {

    private final RedisCacheUtil redisCacheUtil;

    private final TypeClient typeClient;

    private final RedisTemplate redisTemplate;


    final ObjectMapper objectMapper = new ObjectMapper();

    public InterestPushServiceImpl(RedisCacheUtil redisCacheUtil, TypeClient typeClient, RedisTemplate redisTemplate) {
        this.redisCacheUtil = redisCacheUtil;
        this.typeClient = typeClient;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Async
    public void pushSystemStockIn(Video video) {
        // 往系统库中添加
        List<String> labels = video.buildLabel();
        Long videoId = video.getId();
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String label : labels) {
                connection.sAdd((RedisConstant.SYSTEM_STOCK + label).getBytes(), String.valueOf(videoId).getBytes());
            }
            return null;
        });
    }

    @Override
    @Async
    public void pushSystemTypeStockIn(Video video) {
        Long typeId = video.getTypeId();
        redisCacheUtil.sSet(RedisConstant.SYSTEM_TYPE_STOCK + typeId, video.getId());
    }

    @Override
    public Collection<Long> listVideoIdByTypeId(Long typeId) {
        // 随机推送10个

        List<Object> list = redisTemplate.opsForSet().randomMembers(RedisConstant.SYSTEM_TYPE_STOCK + typeId, 12);
        // 可能会有null
        HashSet<Long> result = new HashSet<>();
        for (Object aLong : list) {
            if (aLong!=null){
                result.add(Long.parseLong(aLong.toString()));
            }
        }
        return result;
    }

    @Override
    @Async
    public void deleteSystemStockIn(Video video) {
        List<String> labels = video.buildLabel();
        Long videoId = video.getId();
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String label : labels) {
                connection.sRem((RedisConstant.SYSTEM_STOCK + label).getBytes(),String.valueOf(videoId).getBytes());
            }
            return null;
        });
    }

    @Override
    @Async
    public void initUserModel(Long userId, List<String> labels) {

        String key = RedisConstant.USER_MODEL + userId;
        Map<Object, Object> modelMap = new HashMap<>();
        if (!ObjectUtils.isEmpty(labels)) {
            int size = labels.size();
            // 将标签分为等分概率,不可能超过100个分类
            double probabilityValue = 100 / size;
            for (String labelName : labels) {
                modelMap.put(labelName, probabilityValue);
            }
        }
        redisCacheUtil.del(key);
        redisCacheUtil.hmset(key, modelMap);
        // 为用户模型设置ttl

    }


    @Override
    @Async
    public void updateUserModel(UserModel userModel) {

        Long userId = userModel.getUserId();
        // 游客不用管
        if (userId != null) {
            List<Model> models = userModel.getModels();
            // 获取用户模型
            String key = RedisConstant.USER_MODEL + userId;
            Map<Object, Object> modelMap = redisCacheUtil.hmget(key);

            if (modelMap == null) {
                modelMap = new HashMap<>();
            }
            for (Model model : models) {
                // 修改用户模型
                if (modelMap.containsKey(model.getLabel())) {
                    modelMap.put(model.getLabel(), Double.parseDouble(modelMap.get(model.getLabel()).toString()) + model.getScore());
                    final Object o = modelMap.get(model.getLabel());
                    if (o == null || Double.parseDouble(o.toString()) > 0.0){
                        modelMap.remove(o);
                    }
                } else {
                    modelMap.put(model.getLabel(), model.getScore());
                }
            }

            // 每个标签概率同等加上标签数，再同等除以标签数  防止数据膨胀
            int labelSize = modelMap.keySet().size();
            for (Object o : modelMap.keySet()) {
                modelMap.put(o,(Double.parseDouble(modelMap.get(o).toString()) + labelSize )/ labelSize);
            }
            // 更新用户模型
            redisCacheUtil.hmset(key, modelMap);
        }

    }

    @Override
    public Collection<Long> listVideoIdByUserModel(User user) {
        // 创建结果集
        Set<Long> videoIds = new HashSet<>(10);

        if (user != null) {
            Long userId = user.getId();
            // 从模型中拿概率
            Map<Object, Object> modelMap = redisCacheUtil.hmget(RedisConstant.USER_MODEL + userId);
            if (!ObjectUtils.isEmpty(modelMap)) {
                // 组成数组
                String[] probabilityArray = initProbabilityArray(modelMap);
                Boolean sex = user.getSex();
                // 获取视频
                Random randomObject = new Random();
                List<String> labelNames = new ArrayList<>();
                // 随机获取X个视频
                for (int i = 0; i < 8; i++) {
                    String labelName = probabilityArray[randomObject.nextInt(probabilityArray.length)];
                    labelNames.add(labelName);
                }
                // 提升性能
                String t = RedisConstant.SYSTEM_STOCK;
                // 随机获取
                List<Object> list = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    for (String labelName : labelNames) {
                        String key = t + labelName;
                        connection.sRandMember(key.getBytes());
                    }
                    return null;
                });
                // 获取到的videoIds
                Set<Long> ids = list.stream().filter(id-> id != null).map(id -> Long.parseLong(id.toString())).collect(Collectors.toSet());
                String key2 = RedisConstant.HISTORY_VIDEO;

                // 去重
                List simpIds = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    for (Long id : ids) {
                        connection.get((key2 + id + ":" + userId).getBytes());
                    }
                    return null;
                });
                simpIds = (List) simpIds.stream().filter(o->!ObjectUtils.isEmpty(o)).collect(Collectors.toList());;
                if (!ObjectUtils.isEmpty(simpIds)){
                    for (Object simpId : simpIds) {
                        final Long l = Long.valueOf(simpId.toString());
                        if (ids.contains(l)){
                            ids.remove(l);
                        }
                    }
                }


                videoIds.addAll(ids);

                // 随机挑选一个视频,根据性别: 男：美女 女：宠物
                Long aLong = randomVideoId(sex);
                if (aLong!=null){
                    videoIds.add(aLong);
                }

                return videoIds;
            }
        }
        // 游客
        // 随机获取10个标签
        List<String> labels = typeClient.random10Labels();
        List<String> labelNames = new ArrayList<>();
        int size = labels.size();
        Random random = new Random();
        // 获取随机的标签
        for (int i = 0; i < 10; i++) {
            int randomIndex = random.nextInt(size);
            labelNames.add(RedisConstant.SYSTEM_STOCK + labels.get(randomIndex));
        }
        // 获取videoId
        List<Object> list = redisCacheUtil.sRandom(labelNames);
        if (!ObjectUtils.isEmpty(list)){
            videoIds = list.stream().filter(id ->!ObjectUtils.isEmpty(id)).map(id -> Long.valueOf(id.toString())).collect(Collectors.toSet());
        }

        return videoIds;
    }

    @Override
    public Collection<Long> listVideoIdByLabels(List<String> labelNames) {
        List<String> labelKeys = new ArrayList<>();
        for (String labelName : labelNames) {
            labelKeys.add(RedisConstant.SYSTEM_STOCK + labelName);
        }
        Set<Long> videoIds = new HashSet<>();
        List<Object> list = redisCacheUtil.sRandom(labelKeys);
        if (!ObjectUtils.isEmpty(list)){
            videoIds = list.stream().filter(id -> !ObjectUtils.isEmpty(id)).map(id -> Long.valueOf(id.toString())).collect(Collectors.toSet());
        }
        return videoIds;
    }

    @Override
    @Async
    public void deleteSystemTypeStockIn(Video video) {
        Long typeId = video.getTypeId();
        redisCacheUtil.setRemove(RedisConstant.SYSTEM_TYPE_STOCK + typeId,video.getId());
    }


    public Long randomHotVideoId(){
        Object o = redisTemplate.opsForZSet().randomMember(RedisConstant.HOT_RANK);
        try {
            return objectMapper.readValue(o.toString(), HotVideo.class).getVideoId();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Long randomVideoId(Boolean sex) {
        String key = RedisConstant.SYSTEM_STOCK + (sex ? "美女" : "宠物");
        Object o = redisCacheUtil.sRandom(key);
        if (o != null){
            return Long.parseLong(o.toString());
        }
        return null;
    }

    // 随机获取视频id
    public Long getVideoId(Random random, String[] probabilityArray) {
        String labelName = probabilityArray[random.nextInt(probabilityArray.length)];
        // 获取对应所有视频
        String key = RedisConstant.SYSTEM_STOCK + labelName;
        final Object o = redisCacheUtil.sRandom(key);
        if (o!=null){
            return Long.parseLong(o.toString()) ;
        }
        return null;
    }

    // 初始化概率数组 -> 保存的元素是标签
    public String[] initProbabilityArray(Map<Object, Object> modelMap) {
        // key: 标签  value：概率
        Map<String, Integer> probabilityMap = new HashMap<>();
        int size = modelMap.size();
        AtomicInteger n = new AtomicInteger(0);
        modelMap.forEach((k, v) -> {
            // 防止结果为0,每个同等加上标签数
            int probability = (((Double) v).intValue() + size) / size;
            n.getAndAdd(probability);
            probabilityMap.put(k.toString(), probability);
        });
        String[] probabilityArray = new String[n.get()];

        AtomicInteger index = new AtomicInteger(0);
        // 初始化数组
        probabilityMap.forEach((labelsId, p) -> {
            int i = index.get();
            int limit = i + p;
            while (i < limit) {
                probabilityArray[i++] = labelsId;
            }
            index.set(limit);
        });
        return probabilityArray;
    }




}
