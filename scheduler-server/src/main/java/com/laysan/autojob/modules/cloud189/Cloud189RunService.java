package com.laysan.autojob.modules.cloud189;

import static org.apache.hc.core5.http.protocol.HttpCoreContext.HTTP_REQUEST;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson.JSON;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.helper.AutojobContext;
import com.laysan.autojob.core.helper.ServiceCallback;
import com.laysan.autojob.core.helper.ServiceTemplate;
import com.laysan.autojob.core.utils.LogUtils;
import com.laysan.autojob.service.AbstractJobRuner;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.codec.binary.Base64;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.entity.GzipDecompressingEntity;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.stereotype.Service;

/**
 * @author lise
 * @version CloudAutoCheckInService.java, v 0.1 2020年11月27日 13:43 lise
 */
@Service
@Slf4j
public class Cloud189RunService extends AbstractJobRuner {

    private static String publicKey
                                         =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCZLyV4gHNDUGJMZoOcYauxmNEsKrc0TlLeBEVVIIQNzG4WqjimceOj5R9ETwDeeSN3yejAKLGHgx83lyy2wBjvnbfm/nLObyWwQD/09CmpZdxoFYCH6rdDjRpwZOZ2nXSZpgkZXoOBkfNXNxnN74aXtho2dqBynTw3NFTWyQl8BQIDAQAB";
    private static String appConfURL     = "https://open.e.189.cn/api/logbox/oauth2/appConf.do";
    private static String redirectURL    = "https://cloud.189.cn/api/portal/loginUrl.action?redirectURL=https://cloud.189.cn/web/redirect"
            + ".html?returnURL=/main.action";
    private static String preparePageUrl = "https://cloud.189.cn/unifyLoginForPC.action?appId=8025431004&clientType=10020&returnURL=https"
            + "://m.cloud.189.cn/zhuanti/2020/loginErrorPc/index.html&timeStamp=";

    private static Pattern returnUrlPattern = Pattern.compile("returnUrl = '(.*)'");
    private static Pattern paramIdPattern   = Pattern.compile("paramId = \"(.*)\"");
    private static Pattern ltPattern        = Pattern.compile("lt = \"(.*)\"");
    private static Pattern reqIdPattern     = Pattern.compile("reqId = \"(.*)\"");
    private static Pattern guidPattern      = Pattern.compile("guid = \"(.*)\"");
    private static String  loginUrl         = "https://open.e.189.cn/api/logbox/oauth2/loginSubmit.do";
    String url  = "https://m.cloud.189.cn/v2/drawPrizeMarketDetails.action?taskId=TASK_SIGNIN&activityId=ACT_SIGNIN";
    String url2 = "https://m.cloud.189.cn/v2/drawPrizeMarketDetails.action?taskId=TASK_SIGNIN_PHOTOS&activityId=ACT_SIGNIN";
    String url3 = "https://m.cloud.189.cn/v2/drawPrizeMarketDetails.action?taskId=TASK_2022_FLDFS_KJ&activityId=ACT_SIGNIN";
    private String unifyAccountLoginUrl = "";

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

    public static String uncompress(InputStream inputStream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPInputStream gis = new GZIPInputStream(inputStream)) {
            int b;
            while ((b = gis.read()) != -1) {
                baos.write((byte) b);
            }
        } catch (Exception e) {
            return null;
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    public static String encrypt(String str, String publicKey) {
        try {
            //base64编码的公钥
            byte[] decoded = Base64.decodeBase64(publicKey);
            RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
            //RSA加密
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            return base642hex(Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8"))));
        } catch (Exception e) {
            return null;
        }
    }

    static String base642hex(String str) {
        String b64map = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

        String d = "";
        int e = 0, c = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != '=') {
                int v = b64map.indexOf(str.charAt(i));
                if (e == 0) {
                    e = 1;
                    d += int2char(v >> 2);
                    c = 3 & v;
                } else if (e == 1) {
                    e = 2;
                    d += int2char(c << 2 | v >> 4);
                    c = 15 & v;
                } else if (e == 2) {
                    e = 3;
                    d += int2char(c);
                    d += int2char(v >> 2);
                    c = 3 & v;
                } else {
                    e = 0;
                    d += int2char(c << 2 | v >> 4);
                    d += int2char(15 & v);
                }
            }
        }
        if (e == 1) {
            d += int2char(c << 2);
        }
        return d;
    }

    private static String int2char(int i) {
        return String.valueOf("0123456789abcdefghijklmnopqrstuvwxyz".charAt(i));
    }

    @Override
    @PostConstruct
    public void registry() {
        HANDLERS.put(AccountType.MODULE_CLOUD189.getCode(), this);
    }

    public void doRun(AutojobContext context) {
        Account account = context.getAccount();
        CookieStore cookieStore = new BasicCookieStore();

        ServiceTemplate.execute(context, new ServiceCallback() {
            @Override
            public OkHttpClient initOkHttpClient() {
                return null;

            }

            @Override
            public void doLogin() {
                login(context);
            }

            @Override
            public void doCheckIn() {
                checkIn(context, account);

                lottery(context, url);
                lottery(context, url2);
                lottery(context, url3);
            }

            @Override
            public String decryptPassword(String password) {
                return aesUtil.decrypt(password);
            }

            @Override
            public CloseableHttpClient initHttpClient() {
                return HttpClients.custom().setDefaultCookieStore(cookieStore).build();
            }
        });

    }

    private Map<String, String> getLoginFormData(AutojobContext context) {
        HttpContext httpContext = new BasicHttpContext();

        HttpGet httpGet = new HttpGet(redirectURL);

        try (CloseableHttpResponse httpResponse = context.getHttpClient().execute(httpGet, httpContext)) {
            if (httpResponse.getCode() != 200) {
                throw new BizException("跳转失败");
            }
        } catch (IOException e) {
            throw new BizException("跳转失败");
        }
        HttpRequest attribute = (HttpRequest) httpContext.getAttribute(HTTP_REQUEST);
        String path = attribute.getPath();
        ///api/logbox/separate/web/index
        // .html?appId=cloud&lt
        // =4BF329CAF6F8411E365D44A392242C8CC01216BD183D0E5B6FC2F53E9FFCA6747E1DF4C111C5B5973BD9F2E8D2A8095B303EA92C8C431BC36233E6333F29E2E3679725D474CE82B1138A76E3528B6B4F3278F159&reqId=71877c5f5ed84cafae128862b2c9a1b9
        List<NameValuePair> nameValuePairs;
        try {
            nameValuePairs = new URIBuilder(path).getQueryParams();
        } catch (URISyntaxException e) {
            throw new BizException("解析失败");
        }
        Map<String, String> params = new HashMap<>();
        params.put("rsaKey", publicKey);

        nameValuePairs.forEach(k -> params.put(k.getName(), k.getValue()));

        HttpPost httpPost = new HttpPost(appConfURL);
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("version", "2.0"));
        nvps.add(new BasicNameValuePair("appKey", "cloud"));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:74.0) Gecko/20100101 Firefox/76.0");
        httpPost.addHeader("Referer", "https://open.e.189.cn/");
        httpPost.addHeader("lt", params.get("lt"));
        httpPost.addHeader("REQID", params.get("reqId"));

        try (CloseableHttpResponse httpResponse = context.getHttpClient().execute(httpPost, httpContext)) {
            if (httpResponse.getCode() != 200) {
                throw new BizException("获取参数失败");
            }
            HttpEntity entity = httpResponse.getEntity();
            String resJson = EntityUtils.toString(entity);
            AppConfigResponse appConfigResponse = JSON.parseObject(resJson, AppConfigResponse.class);
            if (!appConfigResponse.getResult().equals("0")) {
                throw new BizException("登录失败");
            }
            params.put("returnUrl", appConfigResponse.getData().getReturnUrl());
            params.put("paramId", appConfigResponse.getData().getParamId());

        } catch (Exception e) {
            throw new BizException("获取参数失败");
        }

        context.setExtraMap(params);

        return params;

    }

    private Boolean login(AutojobContext context) {
        String username = context.getAccount().getAccount();
        String password = context.getDecryptPassword();
        Map<String, String> prepareMap = getLoginFormData(context);
        String encryptUsername = encrypt(username, prepareMap.get("rsaKey"));
        String encryptPassword = encrypt(password, prepareMap.get("rsaKey"));

        HttpPost httpPost = new HttpPost(loginUrl);
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("version", "2.0"));
        nvps.add(new BasicNameValuePair("appKey", "cloud"));
        nvps.add(new BasicNameValuePair("appKey", "cloud"));
        nvps.add(new BasicNameValuePair("version", "2.0"));
        nvps.add(new BasicNameValuePair("accountType", "01"));
        nvps.add(new BasicNameValuePair("mailSuffix", "@189.cn"));
        nvps.add(new BasicNameValuePair("validateCode", ""));
        nvps.add(new BasicNameValuePair("returnUrl", prepareMap.get("returnUrl")));
        nvps.add(new BasicNameValuePair("paramId", prepareMap.get("paramId")));
        nvps.add(new BasicNameValuePair("captchaToken", ""));
        nvps.add(new BasicNameValuePair("dynamicCheck", "FALSE"));
        nvps.add(new BasicNameValuePair("clientType", "1"));
        nvps.add(new BasicNameValuePair("cb_SaveName", "0"));
        nvps.add(new BasicNameValuePair("isOauth2", "false"));
        nvps.add(new BasicNameValuePair("userName", "{NRP}" + encryptUsername + ""));
        nvps.add(new BasicNameValuePair("password", "{NRP}" + encryptPassword + ""));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));

        httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:74.0) Gecko/20100101 Firefox/76.0");
        httpPost.addHeader("Referer", "https://open.e.189.cn/");
        httpPost.addHeader("lt", prepareMap.get("lt"));
        httpPost.addHeader("REQID", prepareMap.get("reqId"));
        try (CloseableHttpResponse httpResponse = context.getHttpClient().execute(httpPost)) {
            HttpEntity entity = httpResponse.getEntity();
            String responseText = EntityUtils.toString(entity);
            LogUtils.info(log, AccountType.MODULE_CLOUD189, context.getAccount(), "login Result:" + responseText);
            Cloud189LoginResult loginResult = JSON.parseObject(responseText, Cloud189LoginResult.class);
            if (loginResult.getResult().equals(0)) {
                String toUrl = loginResult.getToUrl();
                HttpGet httpGet = new HttpGet(toUrl);
                context.getHttpClient().execute(httpGet);
            } else {
                throw new BizException(loginResult.getMsg());
            }
        } catch (Exception e) {
            throw new BizException("登录失败");
        }
        return true;
    }

    private Cloud189CheckInResult checkIn(AutojobContext context, Account account) {
        String checkInUrl = "https://cloud.189.cn/mkt/userSign.action?rand=" + new Date().getTime()
                + "&clientType=TELEANDROID&version=9.0.6&model=KB2000";
        HttpGet httpGet = new HttpGet(checkInUrl);
        httpGet.addHeader("User-Agent",
                "Mozilla/5.0 (Linux; U; Android 11; KB2000 Build/RP1A.201005.001) AppleWebKit/537.36 (KHTML, like Gecko) "
                        + "Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36 Ecloud/9.0.6 Android/30 clientId/538135150693412"
                        + " clientModel/KB2000 clientChannelId/qq proVersion/1.0.6");
        httpGet.addHeader("Referer", "https://m.cloud.189.cn/zhuanti/2016/sign/index.jsp?albumBackupOpened=1");
        httpGet.addHeader("Accept-Encoding", "gzip, deflate");
        httpGet.addHeader("Host", "cloud.189.cn");
        Cloud189CheckInResult result = null;
        try (CloseableHttpResponse httpResponse = context.getHttpClient().execute(httpGet)) {
            HttpEntity entity = httpResponse.getEntity();
            byte[] byteArray = EntityUtils.toByteArray(entity);
            String signInResult = uncompress(byteArray);
            LogUtils.info(log, AccountType.MODULE_CLOUD189, context.getAccount(), "signIn  Result:" + signInResult);
            result = JSON.parseObject(signInResult, Cloud189CheckInResult.class);
            if (result.isError()) {
                throw new BizException(result.getErrorMsg());
            }
            context.appendMessage("签到得" + result.getNetdiskBonus() + "M");
            LogUtils.info(log, AccountType.MODULE_CLOUD189, account.getAccount(), "签到得{}M", result.getNetdiskBonus());
        } catch (Exception e) {
            throw new BizException("签到失败");
        }

        return result;

    }

    @SneakyThrows
    private Cloud189LotteryResult lottery(AutojobContext context, String url) {
        HttpGet httpGet = new HttpGet(url);

        httpGet.addHeader("User-Agent",
                "Mozilla/5.0 (Linux; U; Android 11; KB2000 Build/RP1A.201005.001) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 "
                        + "Chrome/74.0.3729.136 Mobile Safari/537.36 Ecloud/9.0.6 Android/30 clientId/538135150693412 clientModel/KB2000 "
                        + "clientChannelId/qq proVersion/1.0.6");
        httpGet.addHeader("Referer", "https://m.cloud.189.cn/zhuanti/2016/sign/index.jsp?albumBackupOpened=1");
        httpGet.addHeader("Host", "m.cloud.189.cn");
        httpGet.addHeader("Accept-Encoding", "gzip, deflate");

        Cloud189LotteryResult result = null;
        try (CloseableHttpResponse httpResponse = context.getHttpClient().execute(httpGet)) {
            HttpEntity entity = httpResponse.getEntity();
            //GzipDecompressingEntity gzip = new GzipDecompressingEntity(entity);
            String responseText = EntityUtils.toString(entity);
            LogUtils.info(log, AccountType.MODULE_CLOUD189, context.getAccount(), "lotteryResult:" + responseText);
            result = JSON.parseObject(responseText, Cloud189LotteryResult.class);
            if (result.userNotChance()) {
                context.appendMessage("今日已抽奖");
            }
            if (result.timeout()) {
                throw new BizException("请先登录");
            }
            if (result.isError()) {
                throw new BizException("抽奖失败");
            }
            if (StrUtil.isNotBlank(result.getPrizeName())) {
                context.appendMessage("抽奖得" + result.getPrizeName());
                LogUtils.info(log, AccountType.MODULE_CLOUD189, context.getAccount(), result.getPrizeName());
            }

        } catch (Exception e) {
            throw new BizException("抽奖失败");
        }

        return result;

    }

}