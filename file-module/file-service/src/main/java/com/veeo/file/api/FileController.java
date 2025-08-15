package com.veeo.file.api;

import com.veeo.common.config.LocalCache;
import com.veeo.file.config.QiNiuConfig;
import com.veeo.common.constant.VeeoHttpConstant;
import com.veeo.common.entity.File;
import com.veeo.common.exception.BaseException;
import com.veeo.common.util.JwtUtils;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.file.handler.FileHandler;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/veeo/file")
public class FileController {

    @Resource
    private LocalCache localCache;

    @Resource
    private QiNiuConfig qiNiuConfig;

    @Resource
    private FileHandler fileHandler;

    /**
     * 保存到文件表
     */
    @PostMapping
    public Result<Long> save(String fileKey, HttpServletRequest request) {
        /*
         * 上传文件必然是需要userId, 但是拦截器将该路径下的所有路径都放行
         * getUuid方法的设置导致了不能单独配置
         * 不得已在上传方法内部进行登录验证
         */
        String token = request.getHeader(VeeoHttpConstant.USER_LOGIN_TOKEN);
        if (!JwtUtils.checkToken(token)) {
            return ResultUtil.getFailRet("请登录后尝试");
        }
        Long userId = JwtUtils.getUserId(token);
        if (ObjectUtils.isEmpty(userId)) {
            return ResultUtil.getFailRet("请登录后尝试");
        }
        return fileHandler.save(fileKey, userId);
    }

    @GetMapping("/getToken")
    public Result<String> token(String type) {
        return ResultUtil.getSucRet(qiNiuConfig.uploadToken(type));
    }

    @GetMapping("/{fileId}")
    public void getUUid(HttpServletResponse response, @PathVariable Long fileId) throws IOException {

        // 如果不是指定ip调用的该接口，则不返回
        Result<File> urlResult = fileHandler.getFileTrustUrl(fileId);
        if (urlResult.isFailed()) {
            throw new BaseException(urlResult.getMessage());
        }
        File url = urlResult.getData();
        response.setContentType(url.getFormat());
        response.sendRedirect(url.getFileKey());
    }

    @PostMapping("/auth")
    public void auth(@RequestParam(required = false) String uuid,
                     HttpServletResponse response) throws IOException {
        if (uuid == null || localCache.containsKey(uuid) == null) {
            response.sendError(401);
        } else {
            localCache.rem(uuid);
            response.sendError(200);
        }
    }
}
