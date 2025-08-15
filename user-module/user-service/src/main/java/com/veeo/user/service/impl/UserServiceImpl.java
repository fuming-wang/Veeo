package com.veeo.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.entity.File;
import com.veeo.common.entity.response.AuditResponse;
import com.veeo.common.entity.user.Favorites;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.user.UserSubscribe;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.vo.*;
import com.veeo.common.exception.BaseException;
import com.veeo.common.holder.UserHolder;
import com.veeo.common.query.QueryCondition;
import com.veeo.common.query.QueryDTO;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.common.util.Result;
import com.veeo.file.client.AuditClient;
import com.veeo.file.client.FileClient;
import com.veeo.interest.client.InterestClient;
import com.veeo.user.constant.AuditStatus;
import com.veeo.user.constant.RedisConstant;
import com.veeo.user.mapper.UserMapper;
import com.veeo.user.service.FollowService;
import com.veeo.user.service.UserService;
import com.veeo.user.service.UserSubscribeService;
import com.veeo.user.utils.SafePassword;
import com.veeo.video.client.FavoritesClient;
import com.veeo.video.client.TypeClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private TypeClient typeClient;
    @Resource
    private UserSubscribeService userSubscribeService;
    @Resource
    private FollowService followService;
    @Resource
    private RedisCacheUtil redisCacheUtil;
    @Resource
    private FileClient fileClient;
    @Resource
    private InterestClient interestClient;
    @Resource
    private FavoritesClient favoritesClient;
    @Resource
    private AuditClient auditClient;



    @Override
    public boolean register(RegisterVO registerVO) {

        // 邮箱是否存在
        int count = (int) count(new LambdaQueryWrapper<User>().eq(User::getEmail, registerVO.getEmail()));
        if (count == 1) {
            throw new BaseException("邮箱已被注册");
        }
        String code = registerVO.getCode();
        Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + registerVO.getEmail());
        if (o == null) {
            throw new BaseException("验证码为空");
        }
        if (!code.equals(o)) {
            return false;
        }
        User user = new User();
        user.setNickName(registerVO.getNickName());
        user.setEmail(registerVO.getEmail());
        user.setDescription("这个人很懒...");
        user.setPassword(SafePassword.safeHash(registerVO.getPassword()));
        save(user);
        // 创建默认收藏夹
        Favorites favorites = new Favorites();
        favorites.setUserId(user.getId());
        favorites.setName("默认收藏夹");
        Result<Favorites> result = favoritesClient.save(favorites);
        // ok
        if (result.getCode() == 0) {
            favorites = result.getData();
            user.setDefaultFavoritesId(favorites.getId());
            // 这里不用删除，用户正在注册，理论上不会出现缓存
            updateById(user);
            return true;
        } else {
            // 在执行收藏表创建时出现错误
            throw new BaseException(result.getMessage());
        }
    }


    @Override
    public UserVO getInfo(Long userId) {

        User user;
        user = (User) redisCacheUtil.get(RedisConstant.USER_CACHE + userId);
        if (user == null) {
            user = getById(userId);
            // 如果通过userId查找为null, 也保存。
            redisCacheUtil.set(RedisConstant.USER_CACHE + userId, user, 600);
        }
        if (ObjectUtils.isEmpty(user)) {
            return new UserVO();
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        // 查出关注数量
        long followCount = followService.getFollowCount(userId);
        // 查出粉丝数量
        long fansCount = followService.getFansCount(userId);
        userVO.setFollow(followCount);
        userVO.setFans(fansCount);
        return userVO;
    }


    public void initModel(ModelVO modelVO) {
        // 初始化模型
        interestClient.initUserModel(modelVO.getUserId(), modelVO.getLabels());
    }

    @Override
    public Page<User> getFollows(Long userId, BasePage basePage) {
        Page<User> page = new Page<>();
        // 获取关注列表
        Collection<Long> followIds = followService.getFollow(userId, basePage);
        if (ObjectUtils.isEmpty(followIds)) {
            return page;
        }
        // 获取粉丝列表
        // 这里需要将数据转换，因为存到redis中数值小是用int保存，取出来需要用long比较
        Set<Long> fans = new HashSet<>(followService.getFans(userId, null));
        Map<Long, Boolean> map = new HashMap<>();
        for (Long followId : followIds) {
            map.put(followId, fans.contains(followId));
        }

        // 获取头像
        List<User> users = new ArrayList<>();
        Map<Long, User> userMap = getBaseInfoUserToMap(map.keySet());
        followIds.forEach(followId -> {
            User user = userMap.get(followId);
            user.setEach(map.get(user.getId()));
            users.add(user);
        });
        page.setRecords(users);
        page.setTotal(users.size());

        return page;
    }

    @Override
    public Page<User> getFans(Long userId, BasePage basePage) {
        Page<User> page = new Page<>();
        // 获取粉丝列表
        Collection<Long> fansIds = followService.getFans(userId, basePage);
        if (ObjectUtils.isEmpty(fansIds)) {
            return page;
        }
        // 获取关注列表
        Set<Long> followIds = new HashSet<>(followService.getFollow(userId, null));
        Map<Long, Boolean> map = new HashMap<>();
        // 遍历粉丝，查看关注列表中是否有
        for (Long fansId : fansIds) {
            map.put(fansId, followIds.contains(fansId));
        }
        Map<Long, User> userMap = getBaseInfoUserToMap(map.keySet());
        List<User> users = new ArrayList<>();
        // 遍历粉丝列表,保证有序性
        for (Long fansId : fansIds) {
            User user = userMap.get(fansId);
            user.setEach(map.get(user.getId()));
            users.add(user);
        }

        page.setRecords(users);
        page.setTotal(users.size());
        return page;
    }

    private Map<Long, User> getBaseInfoUserToMap(Collection<Long> userIds) {
        List<User> users = new ArrayList<>();
        if (!ObjectUtils.isEmpty(userIds)) {
            users = list(new LambdaQueryWrapper<User>()
                    .in(User::getId, userIds)
                    .select(User::getId, User::getNickName, User::getDescription, User::getSex, User::getAvatar));
        }
        return users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    @Override
    public List<User> list(Collection<Long> userIds) {
        return list(new LambdaQueryWrapper<User>().in(User::getId, userIds)
                .select(User::getId, User::getNickName, User::getSex, User::getAvatar, User::getDescription));
    }

    @Override
    @Transactional
    public void subscribe(Set<Long> typeIds) {
        if (ObjectUtils.isEmpty(typeIds)) {
            return;
        }
        // 校验分类
        Result<Collection<Type>> collectionResult = typeClient.listByIds(typeIds);
        if (!collectionResult.getState()) {
            throw new BaseException(collectionResult.getMessage());
        }
        Collection<Type> types = collectionResult.getData();
        if (typeIds.size() != types.size()) {
            throw new BaseException("不存在的分类");
        }
        Long userId = UserHolder.get();
        List<UserSubscribe> userSubscribes = new ArrayList<>(typeIds.size());
        typeIds.forEach(typeId -> {
            UserSubscribe userSubscribe = new UserSubscribe();
            userSubscribe.setUserId(userId);
            userSubscribe.setTypeId(typeId);
            userSubscribes.add(userSubscribe);
        });
        // 删除之前的
        // 构造查询条件（尽管是删除条件）
        userSubscribeService.remove(new LambdaQueryWrapper<UserSubscribe>().eq(UserSubscribe::getUserId, userId));
        userSubscribeService.saveBatch(userSubscribes);
        // 初始化模型
        ModelVO modelVO = new ModelVO();
        modelVO.setUserId(UserHolder.get());
        // 获取分类下的标签
        List<String> labels = new ArrayList<>();
        for (Type type : types) {
            labels.addAll(type.buildLabel());
        }
        modelVO.setLabels(labels);
        initModel(modelVO);
    }

    @Override
    public Collection<Type> listSubscribeType(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        List<Long> typeIds = userSubscribeService.list(new LambdaQueryWrapper<UserSubscribe>()
                        .eq(UserSubscribe::getUserId, userId))
                .stream().map(UserSubscribe::getTypeId).collect(Collectors.toList());

        if (ObjectUtils.isEmpty(typeIds)) {
            return Collections.emptySet();
        }

        QueryCondition typeSelectCondition = QueryCondition.select("id", "name", "icon");
        QueryCondition typeQueryCondition = QueryCondition.and().in("id", typeIds);
        QueryDTO typeQueryDTO = new QueryDTO();
        typeQueryDTO.addConditions(typeSelectCondition, typeQueryCondition);
        Result<List<Type>> listResult = typeClient.list(typeQueryDTO);
        if (listResult.getState()) {
            throw new BaseException(listResult.getMessage());
        }
        return listResult.getData();
    }

    @Override
    public boolean follows(Long followsUserId) {

        Long userId = UserHolder.get();

        return followService.follows(followsUserId, userId);
    }

    @Override
    public void updateUserModel(UserModel userModel) {
        interestClient.updateUserModel(userModel);
    }

    @Override
    public Boolean findPassword(FindPWVO findPWVO) {

        // 从redis中取出
        Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + findPWVO.getEmail());
        if (o == null) {
            return false;
        }
        // 校验
        if (Integer.parseInt(o.toString()) != findPWVO.getCode()) {
            return false;
        }
        // 修改
        User user = new User();
        user.setEmail(findPWVO.getEmail());
        String hashedPassword = SafePassword.safeHash(findPWVO.getNewPassword());
        user.setPassword(hashedPassword);
        update(user, new UpdateWrapper<User>().lambda().set(User::getPassword, hashedPassword)
                .eq(User::getEmail, findPWVO.getEmail()));
        return true;
    }

    @Override
    public void updateUser(UpdateUserVO user) {
        // 获取userId
        Long userId = UserHolder.get();
        // 数据库查询userId
        // TODO 这里可以尝试从redis中获取, 没有再从数据库中取
        User oldUser = getById(userId);
        // 审核用户名, 相同就不需要审核
        if (!oldUser.getNickName().equals(user.getNickName())) {
            // 文字审核
            Result<AuditResponse> audit1 = auditClient.textAudit(user.getNickName());
            if (!audit1.getState()) {
                throw new BaseException(audit1.getMessage());
            }
            AuditResponse audit = audit1.getData();
            if (!Objects.equals(audit.getAuditStatus(), AuditStatus.SUCCESS)) {
                // 审核失败
                throw new BaseException(audit.getMsg());
            }
            oldUser.setNickName(user.getNickName());
        }
        // 审核用户简介, 相同就不需要审核
        if (!ObjectUtils.isEmpty(user.getDescription()) &&
                !oldUser.getDescription().equals(user.getDescription())) {
            // 文字审核
            Result<AuditResponse> audit1 = auditClient.textAudit(user.getDescription());
            if (!audit1.getState()) {
                throw new BaseException(audit1.getMessage());
            }
            AuditResponse audit = audit1.getData();
            if (!Objects.equals(audit.getAuditStatus(), AuditStatus.SUCCESS)) {
                // 审核失败
                throw new BaseException(audit.getMsg());
            }
            // 保存
            oldUser.setDescription(user.getDescription());
        }
//        File file = fileClient.getById(user.getAvatar());
        if (!Objects.equals(user.getAvatar(), oldUser.getAvatar())) {
            // 图像审核
            Result<File> fileClientById = fileClient.getById(user.getAvatar());
            if (!fileClientById.getState()) {
                throw new BaseException(fileClientById.getMessage());
            }
            Result<AuditResponse> audit1 = auditClient.imageAudit(fileClientById.getData().getFileKey());
            if (!audit1.getState()) {
                throw new BaseException(audit1.getMessage());
            }
            AuditResponse audit = audit1.getData();

            if (!Objects.equals(audit.getAuditStatus(), AuditStatus.SUCCESS)) {
                // 审核失败
                throw new BaseException(audit.getMsg());
            }
            // 保存
            oldUser.setAvatar(user.getAvatar());
        }

        if (!ObjectUtils.isEmpty(user.getDefaultFavoritesId())) {
            // 校验收藏夹
            favoritesClient.exist(userId, user.getDefaultFavoritesId());
        }
        // 修改性别, 这个不用审核吧
        oldUser.setSex(user.getSex());
        // 设置默认收藏夹id, 也不用审核
        oldUser.setDefaultFavoritesId(user.getDefaultFavoritesId());

        updateById(oldUser);

    }

    @Override
    public Collection<String> searchHistory(Long userId) {
        List<String> searchs = new ArrayList<>();
        if (userId != null) {
            searchs.addAll(redisCacheUtil.zGet(RedisConstant.USER_SEARCH_HISTORY + userId));
            searchs = searchs.subList(0, searchs.size() < 20 ? searchs.size() : 20);
        }
        return searchs;
    }

    @Override
    @Async
    public void addSearchHistory(Long userId, String search) {
        if (userId != null) {
            redisCacheUtil.zadd(RedisConstant.USER_SEARCH_HISTORY + userId, new Date().getTime(), search, RedisConstant.USER_SEARCH_HISTORY_TIME);
        }
    }

    @Override
    public void deleteSearchHistory(Long userId) {
        if (userId != null) {
            redisCacheUtil.del(RedisConstant.USER_SEARCH_HISTORY + userId);
        }
    }

    @Override
    public Collection<Type> listNoSubscribeType(Long userId) {

        // 获取用户订阅的分类
        Set<Long> set = listSubscribeType(userId).stream().map(Type::getId).collect(Collectors.toSet());
        // 获取所有分类
        QueryDTO queryDTO = new QueryDTO();
        Result<List<Type>> listResult = typeClient.list(queryDTO);
        if (!listResult.getState()) {
            throw new BaseException(listResult.getMessage());
        }
        List<Type> allType = listResult.getData();
        List<Type> types = new ArrayList<>();
        for (Type type : allType) {
            if (!set.contains(type.getId())) {
                types.add(type);
            }
        }
        return types;
    }


    public List<User> getUsers(Collection<Long> ids) {
        Map<Long, User> userMap = listByIds(ids)
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));
        List<User> result = new ArrayList<>(ids.size());
        ids.forEach(followId -> {
            User user = new User();
            user.setId(followId);
            User u = userMap.get(followId);
            user.setNickName(u.getNickName());
            user.setSex(u.getSex());
            user.setDescription(u.getDescription());
            result.add(user);
        });
        return result;
    }
}
