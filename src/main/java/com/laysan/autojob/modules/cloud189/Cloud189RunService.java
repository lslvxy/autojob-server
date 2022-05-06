package com.laysan.autojob.modules.cloud189;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.TaskLog;
import com.laysan.autojob.core.helper.AutojobContextHolder;
import com.laysan.autojob.core.helper.ServiceCallback;
import com.laysan.autojob.core.helper.ServiceTemplete;
import com.laysan.autojob.core.repository.AccountRepository;
import com.laysan.autojob.core.repository.TaskLogRepository;
import com.laysan.autojob.core.service.AutoRun;
import com.laysan.autojob.core.service.MessageService;
import com.laysan.autojob.core.service.TaskLogService;
import com.laysan.autojob.core.utils.AESUtil;
import com.laysan.autojob.core.utils.LogUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * @author lise
 * @version CloudAutoCheckInService.java, v 0.1 2020年11月27日 13:43 lise
 */
@Service
@Slf4j
public class Cloud189RunService implements AutoRun {
    private static String loginPageUrl = "https://cloud.189.cn/api/portal/loginUrl.action?redirectURL=https://cloud.189.cn/web/redirect.html";
    private static Pattern returnUrlPattern = Pattern.compile("returnUrl = '(.*)'");
    private static Pattern paramIdPattern = Pattern.compile("paramId = \"(.*)\"");
    private static Pattern ltPattern = Pattern.compile("lt = \"(.*)\"");
    private static Pattern reqIdPattern = Pattern.compile("reqId = \"(.*)\"");
    private static Pattern guidPattern = Pattern.compile("guid = \"(.*)\"");
    private String unifyAccountLoginUrl = "";

    private static String loginUrl = "https://open.e.189.cn/api/logbox/oauth2/loginSubmit.do";

    String url2 = "https://m.cloud.189.cn/v2/drawPrizeMarketDetails.action?taskId=TASK_SIGNIN_PHOTOS&activityId=ACT_SIGNIN";
    String url = "https://m.cloud.189.cn/v2/drawPrizeMarketDetails.action?taskId=TASK_SIGNIN&activityId=ACT_SIGNIN";
    @Autowired
    private TaskLogRepository taskLogRepository;
    @Autowired
    private TaskLogService taskLogService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MessageService messageService;
    @Autowired
    private AESUtil aesUtil;

    Map<String, List<Cookie>> cookies = new ConcurrentHashMap<>();

    @Override
    @PostConstruct
    public void registry() {
        HANDLERS.put(AccountType.MODULE_CLOUD189.getCode(), this);
    }


    public boolean run(Account account, boolean forceRun) {

        ServiceTemplete.execute(AccountType.MODULE_CLOUD189, account, new ServiceCallback() {
            @Override
            public void checkTodayExecuted() {
                if (!forceRun) {
                    if (Boolean.TRUE.equals(taskLogService.todayExecuted(account))) {
                        throw new BizException("今日已执行!");
                    }
                }
            }

            @Override
            public OkHttpClient initOkHttpClient() {
                return new OkHttpClient.Builder().cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
                        JSONObject cookiesMap = account.buildExtendInfo();
                        list.forEach(v -> {
                            String cookieString = v.toString$okhttp(false);
                            String key = cookieString.substring(0, cookieString.indexOf("="));
                            cookiesMap.put(key, cookieString);
                        });
                        account.saveExtendInfo(cookiesMap);
                        log.info("cookies={}", JSON.toJSONString(cookiesMap));
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                        List<Cookie> list = new ArrayList<>();
                        JSONObject cookiesMap = account.buildExtendInfo();
                        cookiesMap.forEach((k, cookieString) -> {
                            Cookie c = parseCookie(cookieString.toString(), httpUrl);
                            list.add(c);
                        });
                        return list;
                    }
                }).build();

            }

            @Override
            public void prepare() {
                boolean loginSuccess = true;
                if (StrUtil.isBlank(account.getExtendInfo())) {
                    loginSuccess = login(account.getAccount(), account.getPassword());
                }
                try {
                    Cloud189CheckInResult cloud189CheckInResult = checkIn(account);
                    AutojobContextHolder.get().setCheckInSuccess(!cloud189CheckInResult.isError());
                } catch (Exception e) {
                    loginSuccess = login(account.getAccount(), account.getPassword());
                }
                if (!loginSuccess) {
                    throw new BizException("登录失败");
                }
            }

            @Override
            public void process() {
                if (AutojobContextHolder.get().getCheckInSuccess().equals(Boolean.FALSE)) {
                    checkIn(account);
                }
                lottery(url);
                lottery(url2);
            }

            @Override
            public void saveTaskLog(TaskLog taskLog) {
                taskLog.setDetail(AutojobContextHolder.get().getDetailMessage());
                taskLogRepository.save(taskLog);
            }

            @Override
            public void updateAccount() {
                accountRepository.save(account);
            }
        });

        return true;
    }


    @SneakyThrows
    private boolean needCaptcha(String username) {
        String needCaptchaUrl = "https://open.e.189.cn/api/logbox/oauth2/needcaptcha.do";
        RequestBody body = new FormBody.Builder().add("appKey", "cloud").add("accountType", "01").add("userName", "{RSA}" + username + "").build();
        Request request = new Request.Builder().url(needCaptchaUrl).post(body).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:74.0) Gecko/20100101 Firefox/76.0").header("Referer", "https://open.e.189.cn/").build();
        Response needCaptchaResponse = AutojobContextHolder.get().getClient().newCall(request).execute();
        String responseText = Objects.requireNonNull(needCaptchaResponse.body()).string();
        LogUtils.info(log, AccountType.MODULE_CLOUD189, AutojobContextHolder.get().getAccount(), "needCaptcha:" + responseText);
        return !"0".equals(responseText);
    }

    @SneakyThrows
    private Map<String, String> beforeLogin() {
        String returnUrl = "";
        String paramId = "";
        String lt = "";
        String reqId = "";
        String guid = "";
        Map<String, String> result = new HashMap<>();
        Request firstRequest = new Request.Builder().url(loginPageUrl).build();
        Response firstResponse = AutojobContextHolder.get().getClient().newCall(firstRequest).execute();
        unifyAccountLoginUrl = firstResponse.request().url().toString();
        firstResponse.close();

        Document doc = Jsoup.connect(unifyAccountLoginUrl).get();
        Element captchaToken = doc.body().selectFirst("input[name='captchaToken']");
        Element rsaKey = doc.getElementById("j_rsaKey");
        log.info("captchaToken is:{}", captchaToken.val());
        log.info("rsaKey is:{}", rsaKey.val());
        Elements scriptList = doc.getElementsByTag("script");
        for (Element e : scriptList) {
            String html = e.html();
            Matcher matcher = returnUrlPattern.matcher(html);
            Matcher matcher2 = paramIdPattern.matcher(html);
            Matcher matcher3 = ltPattern.matcher(html);
            Matcher matcher4 = reqIdPattern.matcher(html);
            Matcher matcher5 = guidPattern.matcher(html);
            if (matcher.find()) {
                returnUrl = matcher.group(1);
            }
            if (matcher2.find()) {
                paramId = matcher2.group(1);
            }
            if (matcher3.find()) {
                lt = matcher3.group(1);
            }
            if (matcher4.find()) {
                reqId = matcher4.group(1);
            }
            if (matcher5.find()) {
                guid = matcher5.group(1);
            }
        }
        result.put("captchaToken", captchaToken.val());
        result.put("returnUrl", returnUrl);
        result.put("mailSuffix", "@189.cn");
        result.put("paramId", paramId);
        result.put("lt", lt);
        result.put("reqId", reqId);
        result.put("guid", guid);
        result.put("rsaKey", rsaKey.val());

        return result;
    }

    @SneakyThrows
    private Boolean login(String username, String password) {
        password = aesUtil.decrypt(password);
        Map<String, String> prepareMap = beforeLogin();
        String encryptUsername = encrypt(username, prepareMap.get("rsaKey"));
        String encryptPassword = encrypt(password, prepareMap.get("rsaKey"));
        boolean needCaptcha = needCaptcha(encryptUsername);
        if (needCaptcha) {
            throw new BizException("登录需要验证");
        }
        RequestBody body = new FormBody.Builder().add("appKey", "cloud").add("accountType", "01").add("userName", "{RSA}" + encryptUsername + "").add("password", "{RSA}" + encryptPassword + "").add("validateCode", "").add("captchaToken", prepareMap.get("captchaToken")).add("returnUrl", prepareMap.get("returnUrl")).add("mailSuffix", "@189.cn").add("paramId", prepareMap.get("paramId")).build();
        Request request = new Request.Builder().url(loginUrl).post(body).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:74.0) Gecko/20100101 Firefox/76.0").header("Referer", "https://open.e.189.cn/").header("lt", prepareMap.get("lt")).build();
        Response response = AutojobContextHolder.get().getClient().newCall(request).execute();

        String responseText = Objects.requireNonNull(response.body()).string();
        if (log.isDebugEnabled()) {
            LogUtils.debug(log, AccountType.MODULE_CLOUD189, AutojobContextHolder.get().getAccount(), "login Result:" + responseText);
        }
        Cloud189LoginResult loginResult = JSON.parseObject(responseText, Cloud189LoginResult.class);
        if (loginResult.getResult().equals(0)) {
            String toUrl = loginResult.getToUrl();
            Request request2 = new Request.Builder().url(toUrl).build();
            AutojobContextHolder.get().getClient().newCall(request2).execute().close();
            IoUtil.close(response);
            return true;
        } else {
            throw new BizException(loginResult.getMsg());
        }
    }

    @SneakyThrows
    private Cloud189CheckInResult checkIn(Account account) {
        String checkInUrl = "https://api.cloud.189.cn/mkt/userSign.action?rand=" + System.currentTimeMillis() + "&clientType=TELEANDROID&version=8.6.3&model=SM-G930K";

        Request request = new Request.Builder().url(checkInUrl).header("User-Agent", "Mozilla/5.0 (Linux; Android 5.1.1; SM-G930K Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) " + "Version/4.0" + " Chrome/74.0.3729.136 Mobile Safari/537.36 Ecloud/8.6.3 Android/22 clientId/355325117317828 " + "clientModel/SM-G930K imsi/460071114317824 clientChannelId/qq proVersion/1.0.6").header("Referer", "https://m.cloud.189.cn/zhuanti/2016/sign/index.jsp?albumBackupOpened=1").header("Host", "m.cloud.189.cn").header("Accept-Encoding", "gzip, deflate").build();
        Response response = AutojobContextHolder.get().getClient().newCall(request).execute();
        String signInResult = uncompress(response.body().bytes());
        if (log.isDebugEnabled()) {
            LogUtils.debug(log, AccountType.MODULE_CLOUD189, AutojobContextHolder.get().getAccount(), "signIn  Result:" + signInResult);
        }
        Cloud189CheckInResult result = JSON.parseObject(signInResult, Cloud189CheckInResult.class);
        if (result.isError()) {
            throw new BizException(result.getErrorMsg());
        }
        AutojobContextHolder.get().appendMessage("签到获得" + result.getNetdiskBonus() + "M空间");
        LogUtils.info(log, AccountType.MODULE_CLOUD189, account.getAccount(), "签到获得{}M空间", result.getNetdiskBonus());
        IoUtil.close(response);
        return result;

    }

    @SneakyThrows
    private Cloud189LotteryResult lottery(String url) {

        Request request = new Request.Builder().url(url).header("User-Agent", "Mozilla/5.0 (Linux; Android 5.1.1; SM-G930K Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) " + "Version/4.0" + " Chrome/74.0.3729.136 Mobile Safari/537.36 Ecloud/8.6.3 Android/22 clientId/355325117317828 " + "clientModel/SM-G930K imsi/460071114317824 clientChannelId/qq proVersion/1.0.6").header("Referer", "https://m.cloud.189.cn/zhuanti/2016/sign/index.jsp?albumBackupOpened=1").header("Host", "m.cloud.189.cn").header("Accept-Encoding", "gzip, deflate").build();

        Response response = AutojobContextHolder.get().getClient().newCall(request).execute();
        String responseText = uncompress(response.body().bytes());
        LogUtils.info(log, AccountType.MODULE_CLOUD189, AutojobContextHolder.get().getAccount(), "lotteryResult:" + responseText);
        Cloud189LotteryResult jsonObject = JSON.parseObject(responseText, Cloud189LotteryResult.class);
        if (jsonObject.userNotChance()) {
            AutojobContextHolder.get().appendMessage("今日已抽奖");
        }
        if (jsonObject.timeout()) {
            throw new BizException("请先登录");
        }
        if (jsonObject.isError()) {
            throw new BizException("抽奖失败");
        }
        if (StrUtil.isNotBlank(jsonObject.getPrizeName())) {
            AutojobContextHolder.get().appendMessage("抽奖获得" + jsonObject.getPrizeName());
            LogUtils.info(log, AccountType.MODULE_CLOUD189, AutojobContextHolder.get().getAccount(), jsonObject.getPrizeName());
        }
        IoUtil.close(response);
        return jsonObject;

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

    private Cookie parseCookie(String cookieString, HttpUrl httpUrl) {
//        pageOp=633aa85b525636d07b2dd4ab6ca82088; domain=e.189.cn; path=/
        Cookie.Builder builder = new Cookie.Builder();
        String[] kvs = cookieString.split(";");
        for (String kv : kvs) {
            if (kv.trim().equals("httponly")) {
                builder.httpOnly();
            }
            builder.domain(httpUrl.host());
            if (!kv.contains("=")) {
                continue;
            }
            String[] split = kv.split("=");
            String key = split[0].trim();
            String value = split.length == 2 ? split[1] : "";
            switch (key) {
                case "domain":
                    builder.domain(value);
                    break;
                case "path":
                    builder.path(value);
                    break;
                case "expires":
                    builder.expiresAt(new Date(value).getTime());
                    break;
                default:
                    builder.name(key).value(value);
                    break;
            }
        }
        return builder.build();
    }

    public static String encrypt(String str, String publicKey) throws Exception {
        //base64编码的公钥
        byte[] decoded = Base64.decodeBase64(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return base642hex(Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8"))));
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

}