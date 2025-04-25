package com.veeo.file.controller;

import com.veeo.common.config.LocalCache;
import com.veeo.common.config.QiNiuConfig;
import com.veeo.common.entity.Setting;
import com.veeo.common.util.R;
import com.veeo.common.entity.File;
import com.veeo.common.holder.UserHolder;
import com.veeo.file.service.FileService;
import com.veeo.sys.api.SettingClient;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/veeo/file")
public class FileController implements InitializingBean {


    private final FileService fileService;

    private final QiNiuConfig qiNiuConfig;

    private final SettingClient settingClient;

    public FileController(FileService fileService, QiNiuConfig qiNiuConfig, SettingClient settingClient) {
        this.fileService = fileService;
        this.qiNiuConfig = qiNiuConfig;
        this.settingClient = settingClient;
    }


    /**
     * 保存到文件表
     */
    @PostMapping
    public R save(String fileKey){

        return R.ok().data(fileService.save(fileKey, UserHolder.get()));
    }

    @GetMapping("/getToken")
    public R token(String type){

        return R.ok().data(qiNiuConfig.uploadToken(type));
    }

    @GetMapping("/{fileId}")
    public void getUUid(HttpServletRequest request, HttpServletResponse response, @PathVariable Long fileId) throws IOException {

     /*   String ip = request.getHeader("referer");
        if (!LocalCache.containsKey(ip)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }*/
        // 如果不是指定ip调用的该接口，则不返回
        File url = fileService.getFileTrustUrl(fileId);
        response.setContentType(url.getType());
        response.sendRedirect(url.getFileKey());
    }

    @PostMapping("/auth")
    public void auth(@RequestParam(required = false) String uuid, HttpServletResponse response) throws IOException {
        if (uuid == null || LocalCache.containsKey(uuid) == null){
            response.sendError(401);
        }else {
            LocalCache.rem(uuid);
            response.sendError(200);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Setting setting = settingClient.list().get(0);
        for (String s : setting.getAllowIp().split(",")) {
            LocalCache.put(s,true);
        }
    }

    @RequestMapping("/test")
    public String test() {
        return "test";
    }

}
