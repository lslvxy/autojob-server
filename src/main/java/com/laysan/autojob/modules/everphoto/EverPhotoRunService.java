package com.laysan.autojob.modules.everphoto;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cola.exception.Assert;
import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.repository.AccountRepository;
import com.laysan.autojob.core.service.AutoRun;
import com.laysan.autojob.core.service.MessageService;
import com.laysan.autojob.core.service.TaskLogService;
import com.laysan.autojob.core.utils.AESUtil;
import com.laysan.autojob.core.utils.LogUtils;
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
    TaskLogService taskLogService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private AESUtil aesUtil;
    @Autowired
    private AccountRepository accountRepository;


    @Override
    @PostConstruct
    public void registry() {
        HANDLERS.put(AccountType.MODULE_EVERPHOTO.getCode(), this);
    }

    @Override
    public boolean run(Account account, boolean forceRun) throws Exception {
        JSONObject jsonObject = account.buildExtendInfo();
        String token = jsonObject.getString("token");
        if (StrUtil.isBlank(token)) {
            LoginResult loginResult = login(account);
            token = loginResult.getToken();
        }
        CheckInResult checkInResult = checkIn(account, token);
        //未登录
        if (ObjectUtil.isNull(checkInResult)) {
            LoginResult loginResult = login(account);
            token = loginResult.getToken();
            checkInResult = checkIn(account, token);
        }
        taskLogService.saveSuccessLog(account, checkInResult.toString());
        messageService.sendMessage(account.getUserId(), AccountType.MODULE_EVERPHOTO.getDesc() + "时光相册签到", checkInResult.toString());
        return true;
    }

    private CheckInResult checkIn(Account account, String token) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder().build();
        Request request = new Request.Builder().url(EverPhotoConstant.CHECKIN_URL).post(body).header("User-Agent", "EverPhoto/2.7.0 (Android;2702;ONEPLUS A6000;28;oppo").header("x-device-mac", "02:00:00:00:00:00").header("application", "tc.everphoto").header("x-locked", "1").header("content-length", "0").header("authorization", "Bearer " + token).build();
        try (Response response = client.newCall(request).execute()) {
            Assert.notNull(response.body(), "签到失败,请求错误");
            String responseStr = response.body().string();
            if (log.isDebugEnabled()) {
                LogUtils.debug(log, AccountType.MODULE_EVERPHOTO, account, "checkIn response is {}", responseStr);
            }
            HttpResult<CheckInResult> httpResult = JSON.parseObject(responseStr, new TypeReference<HttpResult<CheckInResult>>() {
            });
            if (ObjectUtil.isNull(httpResult)) {
                LogUtils.error(log, AccountType.MODULE_EVERPHOTO, account, "checkIn error,httpResult is {}", httpResult);
                throw new BizException("签到失败,未获取到登录信息");
            }
            if (ObjectUtil.equals(httpResult.getCode(), 20104)) {
                return null;
            }
            if (ObjectUtil.notEqual(httpResult.getCode(), 0)) {
                LogUtils.error(log, AccountType.MODULE_EVERPHOTO, account, "checkIn error,httpResult is {}", httpResult);
                throw new BizException("签到失败," + httpResult.getMessage());
            }
            CheckInResult checkInResult = httpResult.getData();
            LogUtils.info(log, AccountType.MODULE_EVERPHOTO, account, "签到成功:{}", checkInResult.toString());
            return checkInResult;
        }
    }

    private LoginResult login(Account account) throws IOException {
        String originalPassword = aesUtil.decrypt(account.getPassword());
        String password = DigestUtils.md5DigestAsHex(("tc.everphoto." + originalPassword).getBytes());
        String phone = account.getAccount();

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder().add("mobile", phone).add("password", password).build();
        Request request = new Request.Builder().url(EverPhotoConstant.LOGIN_URL).post(body).header("User-Agent", "EverPhoto/2.7.0 (Android;2702;ONEPLUS A6000;28;oppo").header("x-device-mac", "02:00:00:00:00:00").header("application", "tc.everphoto").header("authorization", "Bearer 94P6RfZFfqvVQ2hH4jULaYGI").header("x-locked", "1").header("content-length", "0").header("accept-encoding", "gzip").build();
        try (Response response = client.newCall(request).execute()) {
            String responseStr = uncompress(response.body().bytes());
            if (log.isDebugEnabled()) {
                LogUtils.debug(log, AccountType.MODULE_EVERPHOTO, account, "login response is {}", responseStr);
            }
            HttpResult<LoginResult> httpResult = JSON.parseObject(responseStr, new TypeReference<HttpResult<LoginResult>>() {
            });
            if (ObjectUtil.isNull(httpResult)) {
                LogUtils.error(log, AccountType.MODULE_EVERPHOTO, account, "login error,httpResult is {}", httpResult);
                throw new BizException("登录失败,未获取到登录信息");
            }
            if (ObjectUtil.notEqual(httpResult.getCode(), 0)) {
                LogUtils.error(log, AccountType.MODULE_EVERPHOTO, account, "login error,httpResult is {}", httpResult);
                throw new BizException("登录失败," + httpResult.getMessage());
            }
            LoginResult loginResult = httpResult.getData();
            if (CharSequenceUtil.isBlank(loginResult.getToken())) {
                LogUtils.error(log, AccountType.MODULE_EVERPHOTO, account, "token is null");
                throw new BizException("登录失败," + "获取Token失败");
            }
            account.setExtendInfo(JSON.toJSONString(loginResult));
            accountRepository.save(account);
            return loginResult;
        }
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
