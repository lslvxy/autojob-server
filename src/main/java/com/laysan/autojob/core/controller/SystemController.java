package com.laysan.autojob.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.dto.TypeDTO;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
            log.info("responseText={}", responseText);
            ObjectMapper mapper = new ObjectMapper();
            Map map = mapper.readValue(responseText, Map.class);

            return map.get("openid").toString();
        }).getOrElse("");
    }

    @GetMapping("/sys/typeList")
    public Object typeList() {
        List<TypeDTO> result = new ArrayList<>();
        TypeDTO everPhoto = new TypeDTO();
        everPhoto.setType(AccountType.MODULE_EVERPHOTO.getCode());
        everPhoto.setName("时光相册");
        everPhoto.setIcon("https://web.everphoto.cn/images/favicon.ico");
        result.add(everPhoto);
        TypeDTO cloud189 = new TypeDTO();
        cloud189.setType(AccountType.MODULE_CLOUD189.getCode());
        cloud189.setName("天翼云盘");
        cloud189.setIcon("https://cloud.189.cn/logo.ico");
        result.add(cloud189);
        TypeDTO yun139 = new TypeDTO();
        //yun139.setType(AccountType.MODULE_YOUDAO.getCode());
        yun139.setName("有道云");
        yun139.setIcon("https://th.bing.com/th/id/R42ec07afd649805d38ffb2ee84e826c8?rik=FjxbOtyizIGk5Q&riu=http%3a%2f%2fis5.mzstatic.com%2fimage%2fthumb%2fPurple118%2fv4%2f97%2f83%2f7c%2f97837c6f-284f-7435-241c-6dafb8132157%2fsource%2f512x512bb.png&ehk=3OE2Kpd1dBGgqjk1BitkrOMni2HqdlE6kglsDGoEw78%3d&risl=&pid=ImgRaw");
//        yun139.setStatus("开发中");
        result.add(yun139);

        TypeDTO wps = new TypeDTO();
        //wps.setType(AccountType.MODULE_WPS.getCode());
        wps.setName("WPS");
        wps.setIcon("https://tse3-mm.cn.bing.net/th/id/OIP.f8WmQ2SL6FirjzcebPVL5AHaHa?pid=ImgDet&rs=1");
        //        wps.setStatus("开发中");
        result.add(wps);
        return result;
    }

}
