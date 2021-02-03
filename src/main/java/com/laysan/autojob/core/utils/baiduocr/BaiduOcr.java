package com.laysan.autojob.core.utils.baiduocr;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.laysan.autojob.core.utils.AliyunOcr;
import lombok.Getter;
import lombok.Setter;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

public class BaiduOcr {
    private static OkHttpClient client;
    public static  String       access_token = "";
    public static  long         tokenExpired = System.currentTimeMillis();
    public static  String       clientId     = System.getenv("baidu_ocr_client_id");
    public static  String       clientSecret = System.getenv("baidu_ocr_client_secret");

    public static String ocr(String fileName) {
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
        try {
            // 本地文件路径
            String filePath = fileName;
            byte[] imgData = FileUtil.readFileByBytes(filePath);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");

            String param = "image=" + imgParam + "&language_type=ENG&paragraph=false";

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = getAuth();

            String result = HttpUtil.post(url, accessToken, param);
            Result result1 = new Gson().fromJson(result, Result.class);
            if (result1.getWords_result_num() >= 1) {
                return result1.getWords_result().get(0).getWords();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Getter
    @Setter
    class Result {
        List<words_result> words_result;
        int                words_result_num;
    }

    @Getter
    @Setter
    class words_result {
        String words;
    }

    /**
     * 获取权限token
     *
     * @return 返回示例：
     * {
     * "access_token": "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567",
     * "expires_in": 2592000
     * }
     */
    public static String getAuth() {
        if (StrUtil.isBlank(access_token) || (tokenExpired - System.currentTimeMillis() < 3 * 24 * 60 * 60 * 1000)) {
            client = new OkHttpClient.Builder().build();
            getAuth(clientId, clientSecret);
        }
        return access_token;
    }

    /**
     * 获取API访问token
     * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
     *
     * @param ak - 百度云官网获取的 API Key
     * @param sk - 百度云官网获取的 Securet Key
     * @return assess_token 示例：
     * "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567"
     */
    public static void getAuth(String ak, String sk) {
        // 获取token地址
        String authHost = "https://aip.baidubce.com/oauth/2.0/token";

        RequestBody body = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", ak)
                .add("client_secret", sk)
                .build();
        Request request = new Request.Builder()
                .url(authHost)
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String string = response.body().string();
            JSONObject jsonObject = JSON.parseObject(string);
            access_token = jsonObject.getString("access_token");

            long expires_in = jsonObject.getLong("expires_in");
            tokenExpired = System.currentTimeMillis() + expires_in * 1000;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        System.out.println(ocr("/Users/lise/abc.png"));
        ThreadUtil.sleep(2000);
        System.out.println(AliyunOcr.ocr("/Users/lise/abc.png"));

    }
}
