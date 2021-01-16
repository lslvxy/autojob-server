
package com.laysan.autojob.modules.cloud189;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.laysan.autojob.core.constants.Constants;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.EventLog;
import com.laysan.autojob.core.repository.EventLogRepository;
import com.laysan.autojob.core.service.AutoRun;
import com.laysan.autojob.core.service.AutoRunService;
import com.laysan.autojob.core.service.MessageService;
import com.laysan.autojob.core.utils.AESUtil;
import com.laysan.autojob.core.utils.LogUtils;
import io.vavr.control.Try;
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
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lise
 * @version CloudAutoCheckInService.java, v 0.1 2020年11月27日 13:43 lise
 */
@Service
@Slf4j
public class Cloud189RunService implements AutoRun {
    private static String  loginPageUrl     = "https://cloud.189.cn/udb/udb_login.jsp?pageId=1&redirectURL=/main.action";
    private static Pattern returnUrlPattern = Pattern.compile("returnUrl = '(.*)'");
    private static Pattern paramIdPattern   = Pattern.compile("paramId = \"(.*)\"");
    private static Pattern ltPattern        = Pattern.compile("lt = \"(.*)\"");
    private static Pattern reqIdPattern     = Pattern.compile("reqId = \"(.*)\"");
    private static Pattern guidPattern      = Pattern.compile("guid = \"(.*)\"");
    private        String  returnUrl        = "";
    private        String  paramId          = "";
    private        String  lt               = "";
    private        String  reqId            = "";
    private        String  guid             = "";
    private        String  captchaTokenStr  = "";
    private static String  loginUrl         = "https://open.e.189.cn/api/logbox/oauth2/loginSubmit.do";

    String url  = "https://m.cloud.189.cn/v2/drawPrizeMarketDetails.action?taskId=TASK_SIGNIN&activityId=ACT_SIGNIN";
    String url2 = "https://m.cloud.189.cn/v2/drawPrizeMarketDetails.action?taskId=TASK_SIGNIN_PHOTOS&activityId=ACT_SIGNIN";
    private OkHttpClient       client;
    @Autowired
    private EventLogRepository eventLogRepository;
    @Autowired
    private MessageService     messageService;

    @Override
    @PostConstruct
    public void registry() {
        AutoRunService.handlers.put(Constants.LOG_TYPE_CLOUD189, this);
    }

    @Override
    public void run(Account account) {
        if (!Objects.equals(account.getType(), Constants.LOG_TYPE_CLOUD189)) {
            log.error("账户type不正确");
            return;
        }
        EventLog eventLog = new EventLog();
        eventLog.setUserId(account.getUserId());
        eventLog.setAccountId(account.getId());
        eventLog.setType(Constants.LOG_TYPE_CLOUD189);

        Try.of(() -> {
            String detail;
            List<Cookie> cookieStore = new ArrayList<>();
            client = new OkHttpClient.Builder().cookieJar(new CookieJar() {
                @Override
                public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
                    cookieStore.addAll(list);
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                    return cookieStore;
                }
            }).build();

            if (Objects.isNull(account)) {
                throw new RuntimeException("用户未配置");
            }
            String loginResult = login(account.getAccount(), AESUtil.decrypt(account.getPassword()));
            LogUtils.info(log, account.getAccount(), Constants.LOG_MODULES_CLOUD189, Constants.LOG_OPERATE_LOGIN, loginResult);

            if (loginResult.equals("登录成功")) {
                String checkInResult = checkIn();
                LogUtils.info(log, account.getAccount(), Constants.LOG_MODULES_CLOUD189, Constants.LOG_OPERATE_CHECKIN, checkInResult);

                String lottery = lottery(url);
                LogUtils.info(log, account.getAccount(), Constants.LOG_MODULES_CLOUD189, Constants.LOG_OPERATE_LOTTERY, lottery);

                String lottery1 = lottery(url2);
                LogUtils.info(log, account.getAccount(), Constants.LOG_MODULES_CLOUD189, Constants.LOG_OPERATE_LOTTERY, lottery1);
                detail = checkInResult + ";" + lottery + ";" + lottery1;
            } else {
                detail = loginResult;
            }

            eventLog.setDetail(detail);
            eventLogRepository.save(eventLog);
            LogUtils.info(log, account.getAccount(), Constants.LOG_MODULES_CLOUD189, Constants.LOG_OPERATE_LOTTERY, detail);

            messageService.sendMessage(account.getUserId(), "天翼网盘签到", detail);
            return null;
        }).onFailure(ex -> {
            eventLog.setDetail(ex.getMessage());
            eventLogRepository.save(eventLog);
        });
    }

    private String needCaptcha(String username) {
        AtomicReference<Response> response = new AtomicReference<>();
        return Try.of(() -> {
            String needCaptchaUrl = "https://open.e.189.cn/api/logbox/oauth2/needcaptcha.do";

            RequestBody body = new FormBody.Builder()
                    .add("appKey", "cloud")
                    .add("accountType", "01")
                    .add("userName", "{RSA}" + username + "")
                    .build();
            Request request = new Request.Builder()
                    .url(needCaptchaUrl)
                    .post(body)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:74.0) Gecko/20100101 Firefox/76.0")
                    .header("Referer", "https://open.e.189.cn/")
                    .build();
            response.set(client.newCall(request).execute());
            String responseText = Objects.requireNonNull(response.get().body()).string();
            log.info("needCaptcha:{}", responseText);
            if (responseText.equals("1")) {
                String cpUrl
                        = "https://open.e.189.cn/api/logbox/oauth2/picCaptcha"
                        + ".do?token=" + captchaTokenStr + "&REQID=" + reqId + "&rnd=" + System.currentTimeMillis() / 1000;

                Request captchaRequest = new Request.Builder()
                        .url(cpUrl)
                        .build();
                Response captchaResponse = client.newCall(request).execute();
                byte[] bytes = captchaResponse.body().bytes();
                FileOutputStream downloadFile = new FileOutputStream("/Users/lise/abc.png");
                downloadFile.write(bytes);
                downloadFile.flush();
                downloadFile.close();

            }
            return responseText;
        }).andFinally(() -> {
            if (!Objects.isNull(response.get())) {
                response.get().close();
            }
        }).getOrElse("1");
    }

    private String login(String username, String password) throws Exception {
        AtomicReference<Response> response = new AtomicReference<>();

        return Try.of(() -> {
            Document doc = Jsoup.connect(loginPageUrl).get();
            Element captchaToken = doc.body().selectFirst("input[name='captchaToken']");
            Element rsaKey = doc.getElementById("j_rsaKey");
            captchaTokenStr = captchaToken.val();
            log.info("captchaToken is:{}", captchaToken.val());
            log.info("rsaKey is:{}", rsaKey.val());
            Elements scriptList = doc.getElementsByTag("script");
            scriptList.forEach(e -> {
                String html = e.html();
                Matcher matcher = returnUrlPattern.matcher(html);
                Matcher matcher2 = paramIdPattern.matcher(html);
                Matcher matcher3 = ltPattern.matcher(html);
                Matcher matcher4 = reqIdPattern.matcher(html);
                Matcher matcher5 = guidPattern.matcher(html);
                if (matcher.find()) {
                    returnUrl = matcher.group(1);
                    log.info("returnUrl is:{}", returnUrl);
                }
                if (matcher2.find()) {
                    paramId = matcher2.group(1);
                    log.info("paramId is:{}", paramId);
                }
                if (matcher3.find()) {
                    lt = matcher3.group(1);
                    log.info("lt is:{}", lt);
                }
                if (matcher4.find()) {
                    reqId = matcher4.group(1);
                    log.info("reqId is:{}", lt);
                }
                if (matcher5.find()) {
                    guid = matcher5.group(1);
                    log.info("guid is:{}", lt);
                }
            });
            String encryptUsername = encrypt(username, rsaKey.val());
            String encryptPassword = encrypt(password, rsaKey.val());

            String needCaptcha = needCaptcha(encryptUsername);
            if (needCaptcha.equals("1")) {
                throw new RuntimeException("登录需要验证");
            }

            RequestBody body = new FormBody.Builder()
                    .add("appKey", "cloud")
                    .add("accountType", "01")
                    .add("userName", "{RSA}" + encryptUsername + "")
                    .add("password", "{RSA}" + encryptPassword + "")
                    .add("validateCode", "")
                    .add("captchaToken", captchaToken.val())
                    .add("returnUrl", returnUrl)
                    .add("mailSuffix", "@189.cn")
                    .add("paramId", paramId)
                    .build();
            Request request = new Request.Builder()
                    .url(loginUrl)
                    .post(body)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:74.0) Gecko/20100101 Firefox/76.0")
                    .header("Referer", "https://open.e.189.cn/")
                    .header("lt", lt)
                    .build();
            response.set(client.newCall(request).execute());

            String s = Objects.requireNonNull(response.get().body()).string();
            log.info("login Result:{}", s);
            JSONObject jsonObject = JSON.parseObject(s);
            if (jsonObject.containsKey("result") && jsonObject.getInteger("result").equals(0)) {
                String toUrl = jsonObject.getString("toUrl");
                Request request2 = new Request.Builder()
                        .url(toUrl).build();
                client.newCall(request2).execute().close();
                return jsonObject.getString("msg");
            } else {
                throw new RuntimeException(s);
            }

        }).andFinally(() -> {
            if (!Objects.isNull(response.get())) {
                response.get().close();
            }
        }).get();

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

    private String checkIn() {
        AtomicReference<Response> response = new AtomicReference<>();
        return Try.of(() -> {
            String result = "";
            String surl = "https://api.cloud.189.cn/mkt/userSign.action?rand=" + System.currentTimeMillis()
                    + "&clientType=TELEANDROID&version=8.6.3&model=SM-G930K";

            Request request = new Request.Builder()
                    .url(surl)
                    .header("User-Agent",
                            "Mozilla/5.0 (Linux; Android 5.1.1; SM-G930K Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) "
                                    + "Version/4.0"
                                    + " Chrome/74.0.3729.136 Mobile Safari/537.36 Ecloud/8.6.3 Android/22 clientId/355325117317828 "
                                    + "clientModel/SM-G930K imsi/460071114317824 clientChannelId/qq proVersion/1.0.6")
                    .header("Referer", "https://m.cloud.189.cn/zhuanti/2016/sign/index.jsp?albumBackupOpened=1")
                    .header("Host", "m.cloud.189.cn")
                    .header("Accept-Encoding", "gzip, deflate")
                    .build();
            response.set(client.newCall(request).execute());
            String signInResult = response.get().body().string();
            log.info("signInResult:{}", signInResult);
            JSONObject jsonObject = JSON.parseObject(signInResult);
            if (jsonObject.getString("isSign").equals("false")) {
                result = "签到获得" + jsonObject.getString("netdiskBonus") + "MB";
                log.info(result);
            } else {
                result = "已签到，获得" + jsonObject.getString("netdiskBonus") + "MB";
                log.info(result);
            }
            return result;
        }).andFinally(() -> {
            if (!Objects.isNull(response.get())) {
                response.get().close();
            }
        }).get();
    }

    private String lottery(String url) {
        AtomicReference<Response> response = new AtomicReference<>();

        return Try.of(() -> {
            String result = "";
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent",
                            "Mozilla/5.0 (Linux; Android 5.1.1; SM-G930K Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) "
                                    + "Version/4.0"
                                    + " Chrome/74.0.3729.136 Mobile Safari/537.36 Ecloud/8.6.3 Android/22 clientId/355325117317828 "
                                    + "clientModel/SM-G930K imsi/460071114317824 clientChannelId/qq proVersion/1.0.6")
                    .header("Referer", "https://m.cloud.189.cn/zhuanti/2016/sign/index.jsp?albumBackupOpened=1")
                    .header("Host", "m.cloud.189.cn")
                    .header("Accept-Encoding", "gzip, deflate")
                    .build();

            response.set(client.newCall(request).execute());
            String responseText = response.get().body().string();
            log.info("lotteryResult:{}", responseText);
            JSONObject jsonObject = JSON.parseObject(responseText);
            if (jsonObject.containsKey("errorCode")) {
                if (jsonObject.getString("errorCode").equals("User_Not_Chance")) {
                    result = "抽奖次数不足";
                } else {
                    throw new RuntimeException("抽奖出错");
                }
            } else if (jsonObject.containsKey("description")) {
                result = "获得" + jsonObject.getString("description") + "MB";
            }
            return result;
        }).andFinally(() -> {
            if (!Objects.isNull(response.get())) {
                response.get().close();
            }
        }).get();

    }
}