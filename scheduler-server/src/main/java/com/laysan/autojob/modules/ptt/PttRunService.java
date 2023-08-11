package com.laysan.autojob.modules.ptt;

import com.alibaba.cola.exception.BizException;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.helper.AutojobContext;
import com.laysan.autojob.core.helper.ServiceCallback;
import com.laysan.autojob.core.helper.ServiceTemplate;
import com.laysan.autojob.core.utils.LogUtils;
import com.laysan.autojob.service.AbstractJobRuner;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.DefaultClientConnectionReuseStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

/**
 * @author lise
 * @version CloudAutoCheckInService.java, v 0.1 2020年11月27日 13:43 lise
 */
@Service
@Slf4j
public class PttRunService extends AbstractJobRuner {

    private static String loginUrl = "https://www.pttime.org/takelogin.php";

    private static String indexUrl = "https://www.pttime.org/index.php";

    private static String attendanceUrl = "https://www.pttime.org/attendance.php";
    CookieStore cookieStore = new BasicCookieStore();

    @Override
    @PostConstruct
    public void registry() {
        HANDLERS.put(AccountType.MODULE_PTT.getCode(), this);
    }

    public void doRun(AutojobContext context) {
        Account account = context.getAccount();

        ServiceTemplate.execute(context, new ServiceCallback() {
            @Override
            public OkHttpClient initOkHttpClient() {
                return null;

            }

            @Override
            public CloseableHttpClient initHttpClient() {
                //String extendInfo = account.getExtendInfo();
                //if (StrUtil.isNotBlank(extendInfo)) {
                //    List<BasicClientCookie> cookies = JSON.parseObject(extendInfo, new TypeReference<List<BasicClientCookie>>() {});
                //    cookies.forEach(cookie -> {
                //        cookieStore.addCookie(cookie);
                //    });
                //}
                return HttpClients.custom().setConnectionReuseStrategy(DefaultClientConnectionReuseStrategy.INSTANCE).setDefaultCookieStore(
                        cookieStore).build();
            }

            @Override
            public void doLogin() {
                login(context, account.getAccount(), context.getDecryptPassword());

            }

            @Override
            public void doCheckIn() {
                checkIn(context);
                attendance(context);
            }

            @Override
            public String decryptPassword(String password) {
                return aesUtil.decrypt(password);
            }
        });
    }

    @SneakyThrows
    private void attendance(AutojobContext context) {
        HttpGet httpGet = new HttpGet(attendanceUrl);
        CloseableHttpResponse response = context.getHttpClient().execute(httpGet);
        String responseText = EntityUtils.toString(response.getEntity());

        LogUtils.info(log, AccountType.MODULE_PTT, context.getAccount(), "attendance  Result:" + responseText);
        if (responseText.contains("今天已签到")) {
            context.appendMessage("今日已签到");
        } else {
            context.appendMessage("签到成功");
        }
        saveCooKies(context.getAccount());

    }

    private void saveCooKies(Account account) {
        //List<org.apache.hc.client5.http.cookie.Cookie> cookies = cookieStore.getCookies();
        //String jsonString = JSON.toJSONString(cookies);
        account.setExtendInfo(null);
    }

    @SneakyThrows
    private void login(AutojobContext context, String username, String password) {

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("username", username));
        nvps.add(new BasicNameValuePair("password", password));
        HttpPost request = new HttpPost(loginUrl);
        request.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));
        request.addHeader("user-agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 "
                        + "Safari/537.36 Edg/110.0.1587.41");
        request.addHeader("referer", "https://www.pttime.org/login.php");
        request.addHeader("dnt", "1");
        CloseableHttpResponse response = context.getHttpClient().execute(request);
        int code = response.getCode();
        LogUtils.info(log, AccountType.MODULE_PTT, context.getAccount(), "response code:" + code);
        if (code != 200) {
            throw new BizException("登录失败");
        }
        saveCooKies(context.getAccount());
    }

    @SneakyThrows
    private void checkIn(AutojobContext context) {

        HttpGet httpGet = new HttpGet(indexUrl);
        CloseableHttpResponse response = context.getHttpClient().execute(httpGet);
        String responseText = EntityUtils.toString(response.getEntity());
        LogUtils.info(log, AccountType.MODULE_PTT, context.getAccount(), "login  Result:" + responseText);
        saveCooKies(context.getAccount());
        if (!responseText.contains("个人中心")) {
            throw new BizException("登录失败");
        }
        saveCooKies(context.getAccount());
    }

}