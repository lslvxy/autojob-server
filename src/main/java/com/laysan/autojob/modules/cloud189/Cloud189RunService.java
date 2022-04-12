
package com.laysan.autojob.modules.cloud189;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.TaskLog;
import com.laysan.autojob.core.repository.TaskLogRepository;
import com.laysan.autojob.core.service.AutoRun;
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
import java.io.IOException;
import java.io.InputStream;
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
    private static String loginPageUrl = "https://cloud.189.cn/api/portal/loginUrl.action?redirectURL=https://cloud.189.cn/web/redirect.html";
    private static Pattern returnUrlPattern = Pattern.compile("returnUrl = '(.*)'");
    private static Pattern paramIdPattern = Pattern.compile("paramId = \"(.*)\"");
    private static Pattern ltPattern = Pattern.compile("lt = \"(.*)\"");
    private static Pattern reqIdPattern = Pattern.compile("reqId = \"(.*)\"");
    private static Pattern guidPattern = Pattern.compile("guid = \"(.*)\"");
    private String returnUrl = "";
    private String paramId = "";
    private String lt = "";
    private String reqId = "";
    private String guid = "";
    private String captchaTokenStr = "";
    private String unifyAccountLoginUrl = "";
    private String phone = "abc";

    private static String loginUrl = "https://open.e.189.cn/api/logbox/oauth2/loginSubmit.do";

    String url = "https://m.cloud.189.cn/v2/drawPrizeMarketDetails.action?taskId=TASK_SIGNIN&activityId=ACT_SIGNIN";
    String url2 = "https://m.cloud.189.cn/v2/drawPrizeMarketDetails.action?taskId=TASK_SIGNIN_PHOTOS&activityId=ACT_SIGNIN";
    private OkHttpClient client;
    @Autowired
    private TaskLogRepository taskLogRepository;
    @Autowired
    private MessageService messageService;
    @Autowired
    private AESUtil aesUtil;

    @Override
    @PostConstruct
    public void registry() {
        HANDLERS.put(AccountType.MODULE_CLOUD189.getCode(), this);
    }

    public boolean run(Account account) {
        TaskLog taskLog = new TaskLog();
        taskLog.setUserId(account.getUserId());
        taskLog.setType(AccountType.MODULE_CLOUD189.getCode());
        phone = account.getAccount();
        String detail;
        try {
//            FcUtils.createFunction(account);
            ThreadUtil.sleep(500);
//            detail = FcUtils.invokeFunction(account);
//            log.info(detail);
//            if (detail.startsWith("{")) {
//                detail = "签到失败,请确认验证码";
//            }
//            ThreadUtil.sleep(200);
//            FcUtils.deleteFunction(account);
        } catch (Exception e) {
            detail = "签到失败,请确认验证码";
        }

//        eventLog.setDetail(detail);
        taskLogRepository.save(taskLog);
//        LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, detail);
//        messageService.sendMessage(account.getUserId(), "天翼网盘签到", detail);

        return true;
    }

    public void run2(Account account) {
        if (Objects.isNull(account)) {
            throw new RuntimeException("用户未配置");
        }
        if (!Objects.equals(account.getType(), AccountType.MODULE_CLOUD189.getCode())) {
            log.error("账户type不正确");
            return;
        }
        TaskLog taskLog = new TaskLog();
        taskLog.setUserId(account.getUserId());
        taskLog.setType(AccountType.MODULE_CLOUD189.getCode());

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

            phone = account.getAccount();

            String loginResult = login(account.getAccount(), aesUtil.decrypt(account.getPassword()));
            LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, loginResult);

            if (loginResult.equals("登录成功")) {
                String checkInResult = checkIn();
                LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, checkInResult);

                String lottery = lottery(url);
                LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, lottery);

                String lottery1 = lottery(url2);
                LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, lottery1);
                detail = checkInResult + ";" + lottery + ";" + lottery1;
            } else {
                detail = loginResult;
            }

            taskLog.setDetail(detail);
            taskLogRepository.save(taskLog);
            LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, detail);

//            messageService.sendMessage(account.getUserId(), "天翼网盘签到", detail);
            return null;
        }).onFailure(ex -> {
            ex.printStackTrace();
            taskLog.setDetail(Objects.isNull(ex.getMessage()) ? "签到失败" : ex.getMessage());
            taskLogRepository.save(taskLog);
        });
    }

    private String needCaptcha(String username) {
        AtomicReference<Response> needCaptchaResponse = new AtomicReference<>();
        AtomicReference<Response> captchaImgResponse = new AtomicReference<>();
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
                    needCaptchaResponse.set(client.newCall(request).execute());
                    String responseText = Objects.requireNonNull(needCaptchaResponse.get().body()).string();
                    LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, "needCaptcha:" + responseText);
//            if (responseText.equals("1")) {
//                String cpUrl
//                        = "https://open.e.189.cn/api/logbox/oauth2/picCaptcha"
//                        + ".do?token=" + captchaTokenStr + "&REQID=" + reqId + "&rnd=" + System.currentTimeMillis() / 1000;
//
//                Request captchaRequest = new Request.Builder()
//                        .url(cpUrl)
//                        .header("Referer", unifyAccountLoginUrl)
//                        .header("DNT", "1")
//                        .build();
//                captchaImgResponse.set(client.newCall(captchaRequest).execute());
//                InputStream bytes = captchaImgResponse.get().body().byteStream();
//                String file = "/opt/autojob/captcha/" + phone + "_" + System.currentTimeMillis() + ".png";
//                LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, "验证码文件:" + file);
//                writeToLocal(file, bytes);
//                return RandomUtil.randomBoolean() ? AliyunOcr.ocr(file) : BaiduOcr.ocr(file);
//            }
                    return responseText;
                }).onFailure(Throwable::printStackTrace).
                andFinally(() -> {
                    if (!Objects.isNull(needCaptchaResponse.get())) {
                        needCaptchaResponse.get().close();
                    }
                    if (!Objects.isNull(captchaImgResponse.get())) {
                        captchaImgResponse.get().close();
                    }
                }).get();
    }

    private String login(String username, String password) throws Exception {
        AtomicReference<Response> response = new AtomicReference<>();

        return Try.of(() -> {
            Request firstRequest = new Request.Builder()
                    .url(loginPageUrl)
                    .build();
            Response firstResponse = client.newCall(firstRequest).execute();
            unifyAccountLoginUrl = firstResponse.request().url().toString();
            firstResponse.close();

            Document doc = Jsoup.connect(unifyAccountLoginUrl).get();
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
            LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, "login Result:" + s);

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
            LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, "lotteryResult:" + responseText);

            JSONObject jsonObject = JSON.parseObject(responseText);
            if (jsonObject.containsKey("errorCode")) {
                if (jsonObject.getString("errorCode").equals("User_Not_Chance")) {
                    result = "抽奖次数不足";
                } else {
                    throw new RuntimeException("抽奖出错");
                }
            } else if (jsonObject.containsKey("prizeName")) {
                result = "抽奖" + jsonObject.getString("prizeName").replace("天翼云盘", "").replace("空间", "");
                LogUtils.info(log, AccountType.MODULE_CLOUD189, phone, result);
            }
            return result;
        }).andFinally(() -> {
            if (!Objects.isNull(response.get())) {
                response.get().close();
            }
        }).get();

    }

    private static void writeToLocal(String destination, InputStream input)
            throws IOException {
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