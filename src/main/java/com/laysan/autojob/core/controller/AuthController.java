package com.laysan.autojob.core.controller;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.laysan.autojob.core.entity.AuthKeyConfig;
import com.laysan.autojob.core.entity.User;
import com.laysan.autojob.core.handler.AuthRequestBuilderRegister;
import com.laysan.autojob.core.service.AuthKeyConfigService;
import com.laysan.autojob.core.service.UserService;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("auth")
@Slf4j
public class AuthController {

    @Resource
    private AuthKeyConfigService authKeyConfigService;
    @Resource
    private UserService userService;

    @GetMapping("/{source}")
    public void auth(@PathVariable("source") String source, HttpServletResponse response) throws IOException {
        Assert.notNull(source, "source cannot be null");
        AuthKeyConfig authKeyConfig = authKeyConfigService.findBySource(source);
        Assert.notNull(authKeyConfig, "authKeyConfig not found");
        AuthRequest authRequest = AuthRequestBuilderRegister.buildAuthRequest(authKeyConfig);
        String authorizeUrl = authRequest.authorize(AuthStateUtils.createState());
        response.sendRedirect(authorizeUrl);
    }

    @GetMapping("/callback/{source}")
    public void login(@PathVariable("source") String source, AuthCallback callback, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Assert.notNull(source, "source cannot be null");
        AuthKeyConfig authKeyConfig = authKeyConfigService.findBySource(source);
        AuthRequest authRequest = AuthRequestBuilderRegister.buildAuthRequest(authKeyConfig);
        AuthResponse<AuthUser> authResponse = authRequest.login(callback);
        log.info(JSON.toJSONString(authResponse));

        if (authResponse.ok()) {
            User save = userService.save(authResponse.getData());
            response.sendRedirect("http://localhost:8000/?t=" + save.getAccessToken());
        }

    }

   /* @RequestMapping("/refresh/{source}/{uuid}")
    @ResponseBody
    public Object refreshAuth(@PathVariable("source") String source, @PathVariable("uuid") String uuid) {
        AuthRequest authRequest = getAuthRequest(source.toLowerCase());

        AuthUser user = userService.getByUuid(uuid);
        if (null == user) {
            return Response.error("用户不存在");
        }
        AuthResponse<AuthToken> response = null;
        try {
            response = authRequest.refresh(user.getToken());
            if (response.ok()) {
                user.setToken(response.getData());
                userService.save(user);
                return Response.success("用户 [" + user.getUsername() + "] 的 access token 已刷新！新的 accessToken: " + response.getData().getAccessToken());
            }
            return Response.error("用户 [" + user.getUsername() + "] 的 access token 刷新失败！" + response.getMsg());
        } catch (AuthException e) {
            return Response.error(e.getErrorMsg());
        }
    }

    @RequestMapping("/revoke/{source}/{uuid}")
    @ResponseBody
    public Response revokeAuth(@PathVariable("source") String source, @PathVariable("uuid") String uuid) throws IOException {
        AuthRequest authRequest = getAuthRequest(source.toLowerCase());

        AuthUser user = userService.getByUuid(uuid);
        if (null == user) {
            return Response.error("用户不存在");
        }
        AuthResponse<AuthToken> response = null;
        try {
            response = authRequest.revoke(user.getToken());
            if (response.ok()) {
                userService.remove(user.getUuid());
                return Response.success("用户 [" + user.getUsername() + "] 的 授权状态 已收回！");
            }
            return Response.error("用户 [" + user.getUsername() + "] 的 授权状态 收回失败！" + response.getMsg());
        } catch (AuthException e) {
            return Response.error(e.getErrorMsg());
        }
    }
*/
}
