package com.renhui.androidrecorder.muxer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;

import com.renhui.androidrecorder.MyApplication;
import com.renhui.androidrecorder.R;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResultAnalyzeThread extends Thread {
    //设置访问服务端IP
    private final String serverIp = "124.222.64.141:8080";
    private static ResultAnalyzeThread resultAnalyzeThread;

    // 本地文件地址
    private String filePath = null;
    // 用于存储分析结果
    public static String analyzeResult = null;
    public static long startTime = - 1;

    public ResultAnalyzeThread(String filePath) {
        // 初始化相关对象和参数
        this.filePath = filePath;
    }

    public static void startUpload(String filePath) {
        if (resultAnalyzeThread == null) {
            synchronized (ResultAnalyzeThread.class) {
                if (resultAnalyzeThread == null) {
                    resultAnalyzeThread = new ResultAnalyzeThread(filePath);
                    resultAnalyzeThread.start();
                }
            }
        }
    }

    public static void stopUpload() {
        resultAnalyzeThread = null;
        startTime = -1;
        analyzeResult = null;
    }


    @Override
    public void run() {
        try {
            Log.w("OkHttpButton", "OkHttpButton Push！");
            String url = "http://" + serverIp + "/analyze";
            String fileName = "/" + filePath;
            File file = new File(filePath);
            // 传递fileName
            RequestBody fileBody = RequestBody.create(fileName, MediaType.parse("text/plain"));

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("fileName", fileName)
                    .build();

            // 其中使用progressRequestBody代表显示进度条
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.MINUTES)
                    .build();

            // 异步请求
            // 发送请求
            startTime = System.currentTimeMillis();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Looper.prepare();

                    Log.d("failureResult", "结果分析失败，网络错误！" + e.toString());

                    Looper.loop();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("analyze Result", "接收到反馈");
                    if (response.body() != null){
                        Looper.prepare();

                        analyzeResult = response.body().string();
                        Log.d("analyze Result", analyzeResult);

                        Looper.loop();
                    }
                }
            });
//                            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
