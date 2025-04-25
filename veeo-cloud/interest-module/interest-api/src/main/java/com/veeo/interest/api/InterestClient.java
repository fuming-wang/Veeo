package com.veeo.interest.api;

import com.veeo.common.entity.user.User;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.UserModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;


@FeignClient(value = "interest-service", contextId = "interest-client", path = "/api/interest")
public interface InterestClient {

    @RequestMapping("/pushSystemStockIn")
    void pushSystemStockIn(@RequestBody Video video);

    @RequestMapping("/pushSystemTypeStockIn")
    void pushSystemTypeStockIn(@RequestBody Video video);

    @RequestMapping("/listVideoIdByTypeId")
    Collection<Long> listVideoIdByTypeId(@RequestParam Long typeId);

    @RequestMapping("/deleteSystemStockIn")
    void deleteSystemStockIn(@RequestBody Video video);

    @RequestMapping("/initUserModel")
    void initUserModel(@RequestParam Long userId, @RequestParam List<String> labels);

    @RequestMapping("/updateUserModel")
    void updateUserModel(@RequestBody UserModel userModel);

    @RequestMapping("/listVideoIdByUserModel")
    Collection<Long> listVideoIdByUserModel(@RequestBody User user);

    @RequestMapping("/listVideoIdByUserModel2")
    Collection<Long> listVideoIdByUserModel();

    @RequestMapping("/listVideoIdByLabels")
    Collection<Long> listVideoIdByLabels(@RequestParam List<String> labelNames);

    @RequestMapping("/deleteSystemTypeStockIn")
    void deleteSystemTypeStockIn(@RequestBody Video video);
}
