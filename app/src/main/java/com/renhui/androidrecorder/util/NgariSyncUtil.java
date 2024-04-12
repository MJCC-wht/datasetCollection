package com.renhui.androidrecorder.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ngari.openapi.Client;
import ngari.openapi.JSONResponseBean;
import ngari.openapi.Request;
import ngari.openapi.Response;
import ngari.openapi.util.JSONUtils;

public class NgariSyncUtil {

    private String filePath;
    private String analyzeResult;

    public NgariSyncUtil(String filePath, String analyzeResult) {
        this.filePath = filePath;
        this.analyzeResult = analyzeResult;
    }

    private String[] getCognitionRes() {
        String cognitionLevel = "";
        String cognitionDesc = "";
        if (analyzeResult.startsWith("ERROR") || analyzeResult.contains("nan")) {
            // 当前结果不足
            cognitionLevel = "未知";
            cognitionDesc = "分析失败，视频时长不足";
        } else {
            // 定义匹配浮点数的正则表达式
            String regex = "\\d+\\.\\d+";
            // 编译正则表达式
            Pattern pattern = Pattern.compile(regex);
            // 创建Matcher对象
            Matcher matcher = pattern.matcher(analyzeResult);
            double score = 0.0;
            if (matcher.find()) {
                String floatString = matcher.group();
                score = Double.parseDouble(floatString);
            }
            score = Double.parseDouble(String.format(Locale.CHINA, "%.2f", score));
            if (score >= 0.7) {
                cognitionLevel = "正常";
                cognitionDesc = "您的记忆减退风险为" + (1 - score) + "，为健康人群。建议定期筛查，感谢您的配合";
            } else if (score >= 0.4) {
                cognitionLevel = "轻度异常";
                cognitionDesc = "您的记忆减退风险为" + (1 - score) + "，为低风险人群。建议定期筛查，感谢您的配合";
            } else {
                cognitionLevel = "中度异常";
                cognitionDesc = "您的记忆减退风险为" + (1 - score) + "，为中风险人群。建议定期筛查，感谢您的配合";
            }
        }
        return new String[]{cognitionLevel, cognitionDesc};
    }

    private String getAccessTime(String[] personInfoList) {
        StringBuilder accessTime = new StringBuilder();
        accessTime.append(personInfoList[5]).append("-");
        accessTime.append(personInfoList[6]).append("-");
        accessTime.append(personInfoList[7]).append(" ");
        accessTime.append(personInfoList[8]).append(":");
        accessTime.append(personInfoList[9]).append(":");
        accessTime.append(personInfoList[10].split("\\.")[0]);

        return accessTime.toString();
    }

    public void syncData2Ngari() {
        // 必填
        String apiUrl = "https://docker-uat.ngarihealth.com/ehealth-openapi/gateway";
        // 必填
        String appKey = "ngari6614a2981f78244a";
        // 必填
        String appSecret = "1f78244a6c335100";
        // 如果开启加密，则必填（不进行加密）
        String encodingAesKey = "";
        Client client = new Client(apiUrl, appKey, appSecret, encodingAesKey);
        // 入参赋值
        List<Object> bodyList = new ArrayList<>();
        //根据接口 传入相应入参,这里只是个例子
        Map<String, String> body = new HashMap<>();

        // filename例子："video/action1/id-name-gender-age-degree-year-month-day-hour-minute-second.mp4"
        String personInfo = filePath.split("/")[filePath.split("/").length - 1];
        String[] personInfoList = personInfo.split("-");
        body.put("name", personInfoList[1]);
        body.put("iCard", personInfoList[0]);
        body.put("sex", personInfoList[2]);
        body.put("originId", "003");
        body.put("originName", "zju");
        body.put("sportLevel", "正常");
        body.put("sportDesc", "一切正常，请继续保持");
        String[] cognitionRes = getCognitionRes();
        body.put("cognitionLevel", cognitionRes[0]);
        body.put("cognitionDesc", cognitionRes[1]);
        body.put("languageLevel", "正常");
        body.put("languageDesc", "一切正常，请继续保持");
        body.put("disabilityLevel", "正常");
        body.put("disabilityDesc", "一切正常，请继续保持");
        body.put("accessTime", getAccessTime(personInfoList));
        bodyList.add(body);

        //X-Service-Id对应的值
        String serviceId = "capassess.elderlyCapabilityAssessmentService";
        //X-Service-Method对应的值
        String method = "ecaDataSync";
        Request request = new ngari.openapi.Request(serviceId, method, bodyList);
        Response response = null;
        try {
            response = client.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(JSONUtils.toString(response));

        if (((ngari.openapi.Response) response).isSuccess()) {
            JSONResponseBean result = response.getJsonResponseBean();
            System.out.println(JSONUtils.toString(result));
        } else {
            System.out.println(response.getCaErrorMsg());
            System.out.println(response.getErrorMessage());
        }
    }
}
