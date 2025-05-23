package com.veeo.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.constant.AuditStatus;
import com.veeo.common.constant.RedisConstant;
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
import com.veeo.file.api.FileClient;
import com.veeo.interest.api.InterestClient;
import com.veeo.user.mapper.UserMapper;
import com.veeo.user.service.FollowService;
import com.veeo.user.service.UserService;
import com.veeo.user.service.UserSubscribeService;
import com.veeo.video.api.FavoritesClient;
import com.veeo.video.api.ImageAuditClient;
import com.veeo.video.api.TextAuditClient;
import com.veeo.video.api.TypeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;


import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.veeo.common.constant.RedisConstant.USER_SEARCH_HISTORY_TIME;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Autowired(required = false)
    private TypeClient typeClient;

    @Autowired(required = false)
    private UserSubscribeService userSubscribeService;

    @Autowired(required = false)
    private FollowService followService;

    @Autowired(required = false)
    private RedisCacheUtil redisCacheUtil;

    @Autowired(required = false)
    private FileClient fileClient;

    @Autowired(required = false)
    private InterestClient interestClient;

    @Autowired(required = false)
    private FavoritesClient favoritesClient;

    @Autowired(required = false)
    private TextAuditClient textAuditClient;

    @Autowired(required = false)
    private ImageAuditClient imageAuditClient;


    @Override
    public boolean register(RegisterVO registerVO) throws Exception {

        // 邮箱是否存在
        int count = (int) count(new LambdaQueryWrapper<User>().eq(User::getEmail, registerVO.getEmail()));
        if (count == 1){
            throw new BaseException("邮箱已被注册");
        }
        String code = registerVO.getCode();
        Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + registerVO.getEmail());
        if (o == null){
            throw new BaseException("验证码为空");
        }
        if (!code.equals(o)){
            return false;
        }

        User user = new User();
        user.setNickName(registerVO.getNickName());
        user.setEmail(registerVO.getEmail());
        user.setDescription("这个人很懒...");
        user.setPassword(registerVO.getPassword());
        save(user);

        // 创建默认收藏夹
        final Favorites favorites = new Favorites();
        favorites.setUserId(user.getId());
        favorites.setName("默认收藏夹");
        favoritesClient.save(favorites);

        // 这里如果单独抽出一个用户配置表就好了,但是没有必要再搞个表
        user.setDefaultFavoritesId(favorites.getId());
        updateById(user);
        return true;
    }

    @Override
    public UserVO getInfo(Long userId){

        User user = getById(userId);
        if (ObjectUtils.isEmpty(user)){
            return new UserVO();
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);

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
        interestClient.initUserModel(modelVO.getUserId(),  modelVO.getLabels());
    }

    @Override
    public Page<User> getFollows(Long userId, BasePage basePage) {
        Page<User> page = new Page<>();
        // 获取关注列表
        Collection<Long> followIds = followService.getFollow(userId, basePage);
        if (ObjectUtils.isEmpty(followIds)) return page;
        // 获取粉丝列表
        HashSet<Long> fans = new HashSet<>();
        // 这里需要将数据转换，因为存到redis中数值小是用int保存，取出来需要用long比较
        fans.addAll(followService.getFans(userId, null));
        Map<Long,Boolean> map = new HashMap<>();
        for (Long followId : followIds) {
            map.put(followId,fans.contains(followId));
        }

        // 获取头像

        List<User> users = new ArrayList<>();
        Map<Long, User> userMap = getBaseInfoUserToMap(map.keySet());
        List<Long> avatarIds = userMap.values().stream().map(User::getAvatar).collect(Collectors.toList());
        for (Long followId : followIds) {
            User user = userMap.get(followId);
            user.setEach(map.get(user.getId()));
            users.add(user);
        }
        page.setRecords(users);
        page.setTotal(users.size());

        return page;
    }

    @Override
    public Page<User> getFans(Long userId, BasePage basePage) {
        Page<User> page = new Page<>();
        // 获取粉丝列表
        Collection<Long> fansIds = followService.getFans(userId, basePage);
        if (ObjectUtils.isEmpty(fansIds)) return page;
        // 获取关注列表
        HashSet<Long> followIds = new HashSet<>();
        followIds.addAll(followService.getFollow(userId,null));
        Map<Long,Boolean> map = new HashMap<>();
        // 遍历粉丝，查看关注列表中是否有
        for (Long fansId : fansIds) {
            map.put(fansId,followIds.contains(fansId));
        }
        Map<Long, User> userMap = getBaseInfoUserToMap(map.keySet());
        List<User> users = new ArrayList<>();
        // 遍历粉丝列表,保证有序性
        for (Long fansId : fansIds) {
            final User user = userMap.get(fansId);
            user.setEach(map.get(user.getId()));
            users.add(user);
        }

        page.setRecords(users);
        page.setTotal(users.size());
        return page;
    }

    private Map<Long,User> getBaseInfoUserToMap(Collection<Long> userIds){
        List<User> users = new ArrayList<>();
        if (!ObjectUtils.isEmpty(userIds)){
            users = list(new LambdaQueryWrapper<User>().in(User::getId, userIds)
                    .select(User::getId, User::getNickName, User::getDescription
                    , User::getSex, User::getAvatar));
        }
        return users.stream().collect(Collectors.toMap(User::getId,Function.identity()));
    }

    @Override
    public List<User> list(Collection<Long> userIds) {
        return list(new LambdaQueryWrapper<User>().in(User::getId,userIds)
                .select(User::getId,User::getNickName,User::getSex,User::getAvatar,User::getDescription));
    }

    @Override
    @Transactional
    public void subscribe(Set<Long> typeIds) {
        if (ObjectUtils.isEmpty(typeIds)) return;
        // 校验分类
        Collection<Long> ids = typeIds;
        Collection<Type> types = typeClient.listByIds(ids);
        if (typeIds.size() != types.size()){
            throw new BaseException("不存在的分类");
        }
        Long userId = UserHolder.get();
        List<UserSubscribe> userSubscribes = new ArrayList<>();
        for (Long typeId : typeIds) {
            UserSubscribe userSubscribe = new UserSubscribe();
            userSubscribe.setUserId(userId);
            userSubscribe.setTypeId(typeId);
            userSubscribes.add(userSubscribe);
        }
        // 删除之前的
        // 构造查询条件（尽管是删除条件）
        userSubscribeService.remove(new LambdaQueryWrapper<UserSubscribe>().eq(UserSubscribe::getUserId, userId));
        userSubscribeService.saveBatch(userSubscribes);
        // 初始化模型
        ModelVO modelVO = new ModelVO();
        modelVO.setUserId(UserHolder.get());
        // 获取分类下的标签
        List<String> labels = new ArrayList();
        for (Type type : types) {
            labels.addAll(type.buildLabel());
        }
        modelVO.setLabels(labels);
        initModel(modelVO);

    }

    @Override
    public Collection<Type> listSubscribeType(Long userId) {
        if (userId == null){
            return Collections.EMPTY_SET;
        }
        List<Long> typeIds = userSubscribeService.list(new LambdaQueryWrapper<UserSubscribe>().eq(UserSubscribe::getUserId, userId))
                .stream().map(UserSubscribe::getTypeId).collect(Collectors.toList());

        if (ObjectUtils.isEmpty(typeIds)) return Collections.EMPTY_LIST;

        QueryCondition typeSelectCondition = QueryCondition.select("id", "name", "icon");
        QueryCondition typeQueryCondition = QueryCondition.and().in("id", typeIds);
        QueryDTO typeQueryDTO = new QueryDTO();
        typeQueryDTO.addConditions(typeSelectCondition, typeQueryCondition);
        List<Type> types = typeClient.list(typeQueryDTO);
        return types;
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
        final Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + findPWVO.getEmail());
        if (o==null){
            return false;
        }
        // 校验
        if (Integer.parseInt(o.toString()) != findPWVO.getCode()){
            return false;
        }
        // 修改
        final User user = new User();
        user.setEmail(findPWVO.getEmail());
        user.setPassword(findPWVO.getNewPassword());
        update(user,new UpdateWrapper<User>().lambda().set(User::getPassword,findPWVO.getNewPassword()).eq(User::getEmail,findPWVO.getEmail()));
        return true;
    }

    @Override
    public void updateUser(UpdateUserVO user) {

        final Long userId = UserHolder.get();

        final User oldUser = getById(userId);
        // 需要审核
        if (!oldUser.getNickName().equals(user.getNickName())){
            oldUser.setNickName(user.getNickName());
            final AuditResponse audit = textAuditClient.audit(user.getNickName());
            if (audit.getAuditStatus() != AuditStatus.SUCCESS) {
                throw new BaseException(audit.getMsg());
            }
        }
        if (!ObjectUtils.isEmpty(user.getDescription()) && !oldUser.getDescription().equals(user.getDescription())){
            oldUser.setDescription(user.getDescription());
            final AuditResponse audit = textAuditClient.audit(user.getNickName());
            if (audit.getAuditStatus() != AuditStatus.SUCCESS) {
                throw new BaseException(audit.getMsg());
            }
        }
        if (!Objects.equals(user.getAvatar(),oldUser.getAvatar())){
            final AuditResponse audit = imageAuditClient.audit(fileClient.getById(user.getAvatar()).getFileKey());
            if (audit.getAuditStatus() != AuditStatus.SUCCESS) {
                throw new BaseException(audit.getMsg());
            }
            oldUser.setAvatar(user.getAvatar());
        }

        if (!ObjectUtils.isEmpty(user.getDefaultFavoritesId())){
            // 校验收藏夹
            favoritesClient.exist(userId, user.getDefaultFavoritesId());
        }



        oldUser.setSex(user.getSex());

        oldUser.setDefaultFavoritesId(user.getDefaultFavoritesId());

        updateById(oldUser);
    }

    @Override
    public Collection<String> searchHistory(Long userId) {
        List<String> searchs = new ArrayList<>();
        if (userId!=null){
            searchs.addAll(redisCacheUtil.zGet(RedisConstant.USER_SEARCH_HISTORY+userId));
            searchs = searchs.subList(0,searchs.size() < 20 ? searchs.size() : 20);
        }
        return searchs;
    }

    @Override
    @Async
    public void addSearchHistory(Long userId, String search) {
        if (userId!=null){
          redisCacheUtil.zadd(RedisConstant.USER_SEARCH_HISTORY+userId,new Date().getTime(),search, USER_SEARCH_HISTORY_TIME);
        }
    }

    @Override
    public void deleteSearchHistory(Long userId) {
        if (userId!=null){
            redisCacheUtil.del(RedisConstant.USER_SEARCH_HISTORY+userId);
        }
    }

    @Override
    public Collection<Type> listNoSubscribeType(Long userId) {

        // 获取用户订阅的分类
        Set<Long> set = listSubscribeType(userId).stream().map(Type::getId).collect(Collectors.toSet());
        log.warn("set: {}", set);
        // 获取所有分类
        QueryDTO queryDTO = new QueryDTO();
        List<Type> allType = typeClient.list(queryDTO);
        log.warn("allType: {}", allType);
        List<Type> types = new ArrayList<>();
        for (Type type : allType) {
            if (!set.contains(type.getId())) {
                types.add(type);
            }
        }

        return types;
    }


    public List<User> getUsers(Collection<Long> ids){
        Map<Long, User> userMap = listByIds(ids).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        List<User> result = new ArrayList<>();
        for (Long followId : ids) {
            User user = new User();
            user.setId(followId);
            User u = userMap.get(followId);
            user.setNickName(u.getNickName());
            user.setSex(u.getSex());
            user.setDescription(u.getDescription());
            result.add(user);
        }
        return result;
    }
}
