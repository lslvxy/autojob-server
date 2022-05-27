package com.laysan.autojob.modules.everphoto;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cola.exception.Assert;
import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.helper.AutojobContextHolder;
import com.laysan.autojob.core.helper.ServiceCallback;
import com.laysan.autojob.core.helper.ServiceTemplate;
import com.laysan.autojob.core.helper.ServiceTemplateService;
import com.laysan.autojob.core.service.AutoRun;
import com.laysan.autojob.core.utils.LogUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

@Service
@Slf4j
public class EverPhotoRunService implements AutoRun {
    @Autowired
    private ServiceTemplateService serviceTemplateService;


    @Override
    @PostConstruct
    public void registry() {
        HANDLERS.put(AccountType.MODULE_EVERPHOTO.getCode(), this);
    }

    @Override
    public boolean run(Account account, boolean forceRun) throws Exception {
        ServiceTemplate.execute(AccountType.MODULE_EVERPHOTO, account, serviceTemplateService, forceRun, new ServiceCallback() {

            @Override
            public OkHttpClient initOkHttpClient() {
                return new OkHttpClient();
            }

            @Override
            public void prepare() {
                String token;
                if (StrUtil.isBlank(account.getExtendInfo())) {
                    EverPhotoLoginResult loginResult = login(account);
                    token = loginResult.getToken();
                } else {
                    token = account.getExtendInfo();
                }

                account.setExtendInfo(token);
            }

            @Override
            public void process() {
                EverPhotoCheckInResult checkInResult = checkIn(account, account.getExtendInfo());
                //未登录
                if (ObjectUtil.isNull(checkInResult)) {
                    EverPhotoLoginResult loginResult = login(account);
                    account.setExtendInfo(loginResult.getToken());
                    checkIn(account, loginResult.getToken());
                }
            }
        });
        return true;
    }

    private EverPhotoCheckInResult checkIn(Account account, String token) {
        RequestBody body = new FormBody.Builder().build();
        Request request = new Request.Builder().url(EverPhotoConstant.CHECKIN_URL).post(body).header("User-Agent", "EverPhoto/2.7.0 (Android;2702;ONEPLUS A6000;28;oppo").header("x-device-mac", "02:00:00:00:00:00").header("application", "tc.everphoto").header("x-locked", "1").header("content-length", "0").header("authorization", "Bearer " + token).build();
        try (Response response = AutojobContextHolder.get().getClient().newCall(request).execute()) {
            Assert.notNull(response.body(), "签到失败,请求错误");
            String responseStr = response.body().string();
            if (log.isDebugEnabled()) {
                LogUtils.debug(log, AccountType.MODULE_EVERPHOTO, account, "checkIn response is {}", responseStr);
            }
            EverPhotoHttpResult<EverPhotoCheckInResult> httpResult = JSON.parseObject(responseStr, new TypeReference<EverPhotoHttpResult<EverPhotoCheckInResult>>() {
            });
            if (ObjectUtil.isNull(httpResult)) {
                LogUtils.error(log, AccountType.MODULE_EVERPHOTO, account, "checkIn error,httpResult is {}", httpResult);
                AutojobContextHolder.get().appendMessage("签到失败,未获取到登录信息");
                return null;
            }
            if (ObjectUtil.equals(httpResult.getCode(), 20104)) {
                AutojobContextHolder.get().appendMessage("签到失败:" + httpResult.getMessage());
                return null;
            }
            if (ObjectUtil.notEqual(httpResult.getCode(), 0)) {
                LogUtils.error(log, AccountType.MODULE_EVERPHOTO, account, "checkIn error,httpResult is {}", httpResult);
                AutojobContextHolder.get().appendMessage("签到失败:" + httpResult.getMessage());
                return null;
            }
            EverPhotoCheckInResult checkInResult = httpResult.getData();
            AutojobContextHolder.get().appendMessage(checkInResult.toString());
            LogUtils.info(log, AccountType.MODULE_EVERPHOTO, account, "签到成功:{}", checkInResult.toString());
            return checkInResult;
        } catch (IOException e) {
            return null;
        }
    }

    @SneakyThrows
    private EverPhotoLoginResult login(Account account) {
        String password = DigestUtils.md5DigestAsHex(("tc.everphoto." + AutojobContextHolder.get().getDecryptPassword()).getBytes());
        String phone = account.getAccount();

        RequestBody body = new FormBody.Builder().add("mobile", phone).add("password", password).build();
        Request request = new Request.Builder().url(EverPhotoConstant.LOGIN_URL).post(body).header("User-Agent", "EverPhoto/2.7.0 (Android;2702;ONEPLUS A6000;28;oppo").header("x-device-mac", "02:00:00:00:00:00").header("application", "tc.everphoto").header("authorization", "Bearer 94P6RfZFfqvVQ2hH4jULaYGI").header("x-locked", "1").header("content-length", "0").header("accept-encoding", "gzip").build();
        Response response = AutojobContextHolder.get().getClient().newCall(request).execute();
        String responseStr = uncompress(response.body().bytes());
        if (log.isDebugEnabled()) {
            LogUtils.debug(log, AccountType.MODULE_EVERPHOTO, account, "login response is {}", responseStr);
        }
        EverPhotoHttpResult<EverPhotoLoginResult> httpResult = JSON.parseObject(responseStr, new TypeReference<EverPhotoHttpResult<EverPhotoLoginResult>>() {
        });
        if (ObjectUtil.isNull(httpResult)) {
            LogUtils.error(log, AccountType.MODULE_EVERPHOTO, account, "login error,httpResult is {}", httpResult);
            throw new BizException("登录失败,未获取到登录信息");
        }
        if (ObjectUtil.notEqual(httpResult.getCode(), 0)) {
            LogUtils.error(log, AccountType.MODULE_EVERPHOTO, account, "login error,httpResult is {}", httpResult);
            throw new BizException("登录失败," + httpResult.getMessage());
        }
        EverPhotoLoginResult loginResult = httpResult.getData();
        if (CharSequenceUtil.isBlank(loginResult.getToken())) {
            LogUtils.error(log, AccountType.MODULE_EVERPHOTO, account, "token is null");
            throw new BizException("登录失败," + "获取Token失败");
        }
        return loginResult;

    }

    public static String uncompress(byte[] str) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str))) {
            int b;
            while ((b = gis.read()) != -1) {
                baos.write((byte) b);
            }
        } catch (Exception e) {
            return new String(str);
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }
}
