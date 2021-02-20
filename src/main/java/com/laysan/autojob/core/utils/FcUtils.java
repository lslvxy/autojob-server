package com.laysan.autojob.core.utils;

import com.aliyuncs.fc.client.FunctionComputeClient;
import com.aliyuncs.fc.model.Code;
import com.aliyuncs.fc.request.CreateFunctionRequest;
import com.aliyuncs.fc.request.DeleteFunctionRequest;
import com.aliyuncs.fc.request.InvokeFunctionRequest;
import com.aliyuncs.fc.response.CreateFunctionResponse;
import com.aliyuncs.fc.response.DeleteFunctionResponse;
import com.aliyuncs.fc.response.InvokeFunctionResponse;
import com.laysan.autojob.core.entity.Account;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FcUtils {
    public static final String REGION = "cn-shanghai";
    public static final String SERVICE_NAME = "AutoJob";
    public static String accessKey = System.getenv("autojob_aliyun_key");
    public static String accessSecretKey = System.getenv("autojob_aliyun_key_secret");
    public static String accountId = "1190605675870101";
    public static FunctionComputeClient fcClient = new FunctionComputeClient(REGION, accountId, accessKey, accessSecretKey);

    public static void createFunction(Account account) {

        // 初始化客户端。

        String jobName = JobUtils.buildJobName(account);
        String functionName = jobName.replaceAll("\\.", "_");


        // 删除函数。
        try {
            DeleteFunctionRequest dfReq = new DeleteFunctionRequest(SERVICE_NAME, functionName);
            DeleteFunctionResponse dfResp = fcClient.deleteFunction(dfReq);
            System.out.println("Deleted function, request ID " + dfResp.getRequestId());
        } catch (Exception e) {
        }

        try {  // 创建函数。
            CreateFunctionRequest cfReq = new CreateFunctionRequest(SERVICE_NAME);
            cfReq.setFunctionName(functionName);
            cfReq.setDescription("Function for " + account.getType());
            cfReq.setMemorySize(128);
            cfReq.setRuntime("python3");
            cfReq.setHandler("index.main_handler");
            Map<String, String> env = new HashMap<>();
            env.put("username", account.getAccount());
            env.put("password", AESUtil.decrypt(account.getPassword()));
            cfReq.setEnvironmentVariables(env);


            // 用于初始化的情况。
            String path = ResourceUtils.getURL("classpath:").getPath();
//            Code code = new Code().setDir(path + account.getType());
            Code code = new Code().setDir("/opt/autojob/scripts/" + account.getType());
            cfReq.setCode(code);
            cfReq.setTimeout(10);

            CreateFunctionResponse crResp = fcClient.createFunction(cfReq);
            System.out.println("Create function, request ID " + crResp.getRequestId());

        } catch (Exception e) {
            e.printStackTrace();
        }


//        // 创建Trigger
//        CreateTriggerRequest ctReq = new CreateTriggerRequest(SERVICE_NAME, functionName);
//        ctReq.setTriggerName(functionName);
//        ctReq.setTriggerType("timer");
//        Map<String, Object> config = new HashMap<>();
//        config.put("CronExpression", corn);
//        config.put("Enabled", true);
//        ctReq.setTriggerConfig(config);
//        fcClient.createTrigger(ctReq);
    }


    public static String invokeFunction(Account account) {
        String jobName = JobUtils.buildJobName(account);
        String functionName = jobName.replaceAll("\\.", "_");

        InvokeFunctionRequest invkReq = new InvokeFunctionRequest(FcUtils.SERVICE_NAME, functionName);
        InvokeFunctionResponse invkResp = FcUtils.fcClient.invokeFunction(invkReq);
        return new String(invkResp.getContent());
    }

    public static void deleteFunction(Account account) {
        String jobName = JobUtils.buildJobName(account);
        String functionName = jobName.replaceAll("\\.", "_");
        DeleteFunctionRequest dfReq = new DeleteFunctionRequest(SERVICE_NAME, functionName);
        DeleteFunctionResponse dfResp = fcClient.deleteFunction(dfReq);
        System.out.println("Deleted function, request ID " + dfResp.getRequestId());
    }


    public static void main(String[] args) throws IOException {
        Account a = new Account();
        a.setId(236L);
        a.setAccount("11");
        a.setPassword("22");
        a.setType("cloud189");
        a.setUserId("openId_xxx");
        createFunction(a);
    }
}
