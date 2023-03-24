package com.renhui.androidrecorder.muxer;

import android.os.Looper;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.renhui.androidrecorder.R;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FileUploadThread extends Thread {

    //设置访问服务端IP
    private final String serverIp = "124.222.64.141:8080";
    private static FileUploadThread fileUploadThread;

    // 本地文件地址
    private String androidFilePath;
    // 传输到云服务器的文件名
    private String tagName;

    public FileUploadThread(String androidFilePath, String tagName) {
        // 初始化相关对象和参数
        this.androidFilePath = androidFilePath;
        this.tagName = tagName;
    }

    public static void startUpload(String androidFilePath, String tagName) {
        if (fileUploadThread == null) {
            synchronized (FileUploadThread.class) {
                if (fileUploadThread == null) {
                    fileUploadThread = new FileUploadThread(androidFilePath, tagName);

                    fileUploadThread.start();
                }
            }
        }
    }

    public static void stopUpload() {
        fileUploadThread = null;
    }

    @Override
    public void run() {
        try {
            Log.w("OkHttpButton", "OkHttpButton Push！");
            String url = "http://" + serverIp + "/upload";
            String filePath = androidFilePath;
            File file = new File(filePath);
            // 所有文件类型
            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
//            //所有图片类型
//            MediaType mediaType=MediaType.Companion.parse("image/*; charset=utf-8");
//            //第一层，说明数据为文件，以及文件类型
//            RequestBody fileBody = RequestBody.Companion.create(file,mediaType);

            String fileName = "/" + tagName;

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileName, fileBody)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
//            OkHttpClient okHttpClient = new OkHttpClient();
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(25, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            // 异步请求
            // 发送请求
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure( Call call, IOException e) {
                    Toast.makeText(MediaMuxerActivity.mainActivity, "网络错误，请先连接网络！", Toast.LENGTH_SHORT).show();
                    Log.d("failureResult", "网络错误！");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.body() != null){
                        Looper.prepare();
                        String result = response.body().string();
                        Toast.makeText(MediaMuxerActivity.mainActivity, result, Toast.LENGTH_SHORT).show();
                        Log.d("theResult", result);
                        VoiceBroadcastThread.stopBroadcast();
                        VoiceBroadcastThread.startBroadcast(MediaMuxerActivity.mainActivity, "拍摄完成，" + result);
                        Looper.loop();
                    }
                }
            });

////             同步相应
//                Response response = null;
//                try {
//                    response = okHttpClient.newCall(request).execute();
//                    // 处理响应
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                if (response.isSuccessful()) {
//                    String responseStr = response.body().string();
//                    Log.w("OkHttpStr", "response.isSuccessful() True" + responseStr);
//                } else {
//                    Log.w("OkHttpStr", "response.isSuccessful() False");
//                            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
