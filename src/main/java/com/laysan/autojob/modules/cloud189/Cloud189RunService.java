package com.laysan.autojob.modules.cloud189;

import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.TaskLog;
import com.laysan.autojob.core.repository.AccountRepository;
import com.laysan.autojob.core.repository.TaskLogRepository;
import com.laysan.autojob.core.service.AutoRun;
import com.laysan.autojob.core.service.MessageService;
import com.laysan.autojob.core.utils.AESUtil;
import com.laysan.autojob.core.utils.LogUtils;
import io.vavr.control.Try;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.concurrent.atomic.AtomicReference;
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
    private String phone = "abc";

    private static String loginUrl = "https://open.e.189.cn/api/logbox/oauth2/loginSubmit.do";

    String url2 = "https://m.cloud.189.cn/v2/drawPrizeMarketDetails.action?taskId=TASK_SIGNIN_PHOTOS&activityId=ACT_SIGNIN";
    String url = "https://m.cloud.189.cn/v2/drawPrizeMarketDetails.action?taskId=TASK_SIGNIN&activityId=ACT_SIGNIN";
    private OkHttpClient client;
    @Autowired
    private TaskLogRepository taskLogRepository;
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


    public boolean run(Account account) {

        TaskLog taskLog = new TaskLog();
        taskLog.setUserId(account.getUserId());
        taskLog.setType(AccountType.MODULE_CLOUD189.getCode());

        String detail = "xxx";
        client = new OkHttpClient.Builder().cookieJar(new CookieJar() {
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

        phone = account.getAccount();

        boolean loginSuccess = true;

        try {
            lottery(url);
        } catch (Exception e) {
            Map<String, String> prepareMap = prepare();
            boolean needCaptcha = needCaptcha(account.getAccount(), prepareMap);
            if (needCaptcha) {
                throw new BizException("登录需要验证");
            }
            loginSuccess = login(account.getAccount(), account.getPassword(), prepareMap);
        }


        if (loginSuccess) {
            String checkInResult = checkIn();
            LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, checkInResult);

            String lottery = lottery(url);
            LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, lottery);

            String lottery1 = lottery(url2);
            LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, lottery1);
            detail = checkInResult + ";" + lottery + ";" + lottery1;
        }

        taskLog.setDetail(detail);
        taskLogRepository.save(taskLog);
        LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, detail);
        accountRepository.save(account);
//            messageService.sendMessage(account.getUserId(), "天翼网盘签到", detail);
        return true;
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

    @SneakyThrows
    private boolean needCaptcha(String username, Map<String, String> prepareMap) {
        username = encrypt(username, prepareMap.get("rsaKey"));
        String needCaptchaUrl = "https://open.e.189.cn/api/logbox/oauth2/needcaptcha.do";
        RequestBody body = new FormBody.Builder().add("appKey", "cloud").add("accountType", "01").add("userName", "{RSA}" + username + "").build();
        Request request = new Request.Builder().url(needCaptchaUrl).post(body).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:74.0) Gecko/20100101 Firefox/76.0").header("Referer", "https://open.e.189.cn/").build();
        Response needCaptchaResponse = client.newCall(request).execute();
        String responseText = Objects.requireNonNull(needCaptchaResponse.body()).string();
        LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, "needCaptcha:" + responseText);
        return !"0".equals(responseText);
    }

    @SneakyThrows
    private Map<String, String> prepare() {
        String returnUrl = "";
        String paramId = "";
        String lt = "";
        String reqId = "";
        String guid = "";
        Map<String, String> result = new HashMap<>();
        Request firstRequest = new Request.Builder().url(loginPageUrl).build();
        Response firstResponse = client.newCall(firstRequest).execute();
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
    private boolean login(String username, String password, Map<String, String> prepareMap) {
        String encryptUsername = encrypt(username, prepareMap.get("rsaKey"));
        String encryptPassword = encrypt(password, prepareMap.get("rsaKey"));
        RequestBody body = new FormBody.Builder().add("appKey", "cloud").add("accountType", "01").add("userName", "{RSA}" + encryptUsername + "").add("password", "{RSA}" + encryptPassword + "").add("validateCode", "").add("captchaToken", prepareMap.get("captchaToken")).add("returnUrl", prepareMap.get("returnUrl")).add("mailSuffix", "@189.cn").add("paramId", prepareMap.get("paramId")).build();
        Request request = new Request.Builder().url(loginUrl).post(body).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:74.0) Gecko/20100101 Firefox/76.0").header("Referer", "https://open.e.189.cn/").header("lt", prepareMap.get("lt")).build();
        Response response = client.newCall(request).execute();

        String s = Objects.requireNonNull(response.body()).string();
        LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, "login Result:" + s);
        JSONObject jsonObject = JSON.parseObject(s);
        if (jsonObject.containsKey("result") && jsonObject.getInteger("result").equals(0)) {
            String toUrl = jsonObject.getString("toUrl");
            Request request2 = new Request.Builder().url(toUrl).build();
            client.newCall(request2).execute().close();
            return true;
        } else {
            throw new BizException(s);
        }


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
            String surl = "https://api.cloud.189.cn/mkt/userSign.action?rand=" + System.currentTimeMillis() + "&clientType=TELEANDROID&version=8.6.3&model=SM-G930K";

            Request request = new Request.Builder().url(surl).header("User-Agent", "Mozilla/5.0 (Linux; Android 5.1.1; SM-G930K Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) " + "Version/4.0" + " Chrome/74.0.3729.136 Mobile Safari/537.36 Ecloud/8.6.3 Android/22 clientId/355325117317828 " + "clientModel/SM-G930K imsi/460071114317824 clientChannelId/qq proVersion/1.0.6").header("Referer", "https://m.cloud.189.cn/zhuanti/2016/sign/index.jsp?albumBackupOpened=1").header("Host", "m.cloud.189.cn").header("Accept-Encoding", "gzip, deflate").build();
            response.set(client.newCall(request).execute());
            String signInResult = uncompress(response.get().body().bytes());
//            String signInResult = response.get().body().string();
            LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, "signIn  Result:" + signInResult);

            JSONObject jsonObject = JSON.parseObject(signInResult);
            if (jsonObject.containsKey("netdiskBonus")) {
                result = "签到" + jsonObject.getString("netdiskBonus") + "M";
                LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, result);
            }
            return result;
        }).andFinally(() -> {
            if (!Objects.isNull(response.get())) {
                response.get().close();
            }
        }).get();
    }

    @SneakyThrows
    private String lottery(String url) {
        AtomicReference<Response> response = new AtomicReference<>();

        String result = "";
        Request request = new Request.Builder().url(url).header("User-Agent", "Mozilla/5.0 (Linux; Android 5.1.1; SM-G930K Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) " + "Version/4.0" + " Chrome/74.0.3729.136 Mobile Safari/537.36 Ecloud/8.6.3 Android/22 clientId/355325117317828 " + "clientModel/SM-G930K imsi/460071114317824 clientChannelId/qq proVersion/1.0.6").header("Referer", "https://m.cloud.189.cn/zhuanti/2016/sign/index.jsp?albumBackupOpened=1").header("Host", "m.cloud.189.cn").header("Accept-Encoding", "gzip, deflate").build();

        response.set(client.newCall(request).execute());
        String responseText = uncompress(response.get().body().bytes());
        LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, "lotteryResult:" + responseText);

        JSONObject jsonObject = JSON.parseObject(responseText);
        if (jsonObject.containsKey("errorCode")) {
            if (jsonObject.getString("errorCode").equals("User_Not_Chance")) {
                result = "今日已抽奖";
            } else if (jsonObject.getString("errorCode").equals("TimeOut")) {
                result = "请先登录";
                throw new BizException("请先登录");
            } else {
                throw new BizException("抽奖出错");
            }
        } else if (jsonObject.containsKey("prizeName")) {
            result = "抽奖" + jsonObject.getString("prizeName").replace("天翼云盘", "").replace("空间", "");
            LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, result);
        }
        return result;


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

    private static void writeToLocal(String destination, InputStream input) throws IOException {
        int index;
        byte[] bytes = new byte[1024];
        FileOutputStream downloadFile = new FileOutputStream(destination);
        while ((index = input.read(bytes)) != -1) {
            downloadFile.write(bytes, 0, index);
            downloadFile.flush();
        }
        downloadFile.close();
        input.close();
    }
}