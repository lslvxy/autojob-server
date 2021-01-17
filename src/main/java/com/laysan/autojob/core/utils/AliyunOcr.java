package com.laysan.autojob.core.utils;

import com.alibaba.fastjson.JSON;
import com.aliyun.ocr20191230.Client;
import com.aliyun.ocr20191230.models.RecognizeVerificationcodeAdvanceRequest;
import com.aliyun.ocr20191230.models.RecognizeVerificationcodeResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.tearpc.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;

@Slf4j
public class AliyunOcr {
    private static Client client;
    private static String accessKey= System.getenv("autojob_aliyun_key");
    private static String accessKeySecret = System.getenv("autojob_aliyun_key_secret");

    static {
        Config config = new Config();
        config.accessKeyId = accessKey;
        //你的accessKeyId
        config.accessKeySecret = accessKeySecret;
        //你的accessKeySecret
        config.type = "access_key";
        config.regionId = "cn-shanghai";
        // config.endpointType="internal";  //默认通过公网访问OSS，如需通过内网请打开这一行
        //url方式
        config.endpoint = "ocr.cn-shanghai.aliyuncs.com";
        try {
            client = new Client(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String ocr(String fileName) {
        try {
            RecognizeVerificationcodeAdvanceRequest req = new RecognizeVerificationcodeAdvanceRequest();
            req.imageURLObject = new FileInputStream(fileName);
            RecognizeVerificationcodeResponse rep = client.recognizeVerificationcodeAdvance(req, new RuntimeOptions());
            log.info("银行卡识别=" + JSON.toJSONString(rep));
            return rep.getData().getContent();
        } catch (TeaException e) {
            log.info("银行卡识别异常了");
            log.info(JSON.toJSONString(e.getData()));
        } catch (Exception e) {
            log.info("银行卡识别异常了");
            log.info(JSON.toJSONString(e.getMessage()));
        }
        return "";
    }

}
