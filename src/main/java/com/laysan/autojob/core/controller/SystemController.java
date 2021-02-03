package com.laysan.autojob.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laysan.autojob.core.constants.Constants;
import com.laysan.autojob.core.dto.TypeDTO;
import io.vavr.control.Try;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class SystemController {

    @GetMapping("/user/getopenid")
    public String getOpenId(String code) {
        String secret = System.getenv("wx127525214d4abbe0_secret");
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=wx127525214d4abbe0&secret=" + secret + "&js_code=" + code
                + "&grant_type=authorization_code";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        return Try.of(() -> {
            Response response = client.newCall(request).execute();
            String responseText = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            Map map = mapper.readValue(responseText, Map.class);

            return map.get("openid").toString();
        }).getOrElse("");
    }

    @GetMapping("/sys/typeList")
    public Object typeList() {
        List<TypeDTO> result = new ArrayList<>();
        TypeDTO everPhoto = new TypeDTO();
        everPhoto.setType(Constants.MODULE_EVERPHOTO);
        everPhoto.setName("时光相册");
        everPhoto.setIcon("https://web.everphoto.cn/images/favicon.ico");
        result.add(everPhoto);
        TypeDTO cloud189 = new TypeDTO();
        cloud189.setType(Constants.MODULE_CLOUD189);
        cloud189.setName("天翼云盘");
        cloud189.setIcon("https://cloud.189.cn/logo.ico");
        result.add(cloud189);
        TypeDTO yun139 = new TypeDTO();
        yun139.setType(Constants.MODULE_YUN139);
        yun139.setName("和彩云");
        yun139.setIcon("https://yun.139.com/w/static/img/LOGO.png");
        yun139.setStatus("开发中");
        result.add(yun139);
        return result;
    }

}
