package com.renhui.androidrecorder.muxer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.renhui.androidrecorder.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

public class FileUploadThread extends Thread {

    //设置访问服务端IP
    private final String serverIp = "124.222.64.141:8080";
    private static FileUploadThread fileUploadThread;

    // 页面的上下文
    private Context mContext;
    // 本地文件地址
    private String androidFilePath;
    // 传输到云服务器的文件名
    private String tagName;

    public FileUploadThread(Context mContext, String androidFilePath, String tagName) {
        // 初始化相关对象和参数
        this.androidFilePath = androidFilePath;
        this.tagName = tagName;
        this.mContext = mContext;
    }

    public static void startUpload(Context mContext, String androidFilePath, String tagName) {
        if (fileUploadThread == null) {
            synchronized (FileUploadThread.class) {
                if (fileUploadThread == null) {
                    fileUploadThread = new FileUploadThread(mContext, androidFilePath, tagName);

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
            // TODO：添加一个开始上传的弹窗，最好显示进度
//            AlertDialog alertDialog = new AlertDialog.Builder(MediaMuxerActivity.mainActivity)
//                    .setTitle("上传文件提示：")
//                    .setMessage("文件正在上传，请点击确定后在下方查看上传进度条")
//                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Looper.prepare();
//                            Log.e("ProgressBar", "正在上传");
//                            Looper.loop();
//                        }
//                    }).create();

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
            // 创建自己的requestBody，用于显示进度条
            ProgressRequestBody progressRequestBody = new ProgressRequestBody(requestBody, new ProgressListener() {
                @Override
                public void onProgress(long bytesWritten, long contentLength) {
                    Log.e("ProgressBar", bytesWritten + "/" + contentLength);
                    int progress = (int) (((double) bytesWritten / contentLength) * 100);
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 如果是txt，不要显示进度条
                            if (!filePath.endsWith("txt")) {
                                ProgressBar uploadProgress = MediaMuxerActivity.mainActivity.findViewById(R.id.upload_progress);
                                uploadProgress.setProgress(progress);
                            }
                        }
                    });
                }
            });

            // 其中使用progressRequestBody代表显示进度条
            Request request = new Request.Builder()
                    .url(url)
                    .post(progressRequestBody)
                    .build();

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
                    Looper.prepare();
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog failDialog = new AlertDialog.Builder(mContext)
                                    .setTitle("文件上传失败：")
                                    .setMessage("由于网络错误，请确认网络连接正常后重新上传")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Log.e("ProgressBar", "上传失败");
                                        }
                                    }).create();
                            failDialog.show();
                        }
                    });
                    VoiceBroadcastThread.stopBroadcast();
                    VoiceBroadcastThread.startBroadcast(mContext, "网络错误，请先连接网络！");
                    Log.d("failureResult", "网络错误！");
                    Looper.loop();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.body() != null){
                        Looper.prepare();

                        String result = response.body().string();
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog successDialog = new AlertDialog.Builder(mContext)
                                        .setTitle("文件上传成功：")
                                        .setMessage("请点击确定以明确文件上传成功")
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Log.e("ProgressBar", "上传成功");
                                            }
                                        }).create();
                                successDialog.show();
                            }
                        });
                        Log.d("theResult", result);
                        VoiceBroadcastThread.stopBroadcast();
                        String action = tagName.endsWith("mp4") ? "拍摄" : "录制";
                        VoiceBroadcastThread.startBroadcast(mContext, action + "完成，" + result);
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

class ProgressRequestBody extends RequestBody {

    private RequestBody requestBody;
    private long currentLength;
    private long totalLength;
    private ProgressListener progressListener;

    public ProgressRequestBody(RequestBody requestBody, ProgressListener progressListener) {
        this.requestBody = requestBody;
        this.progressListener = progressListener;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {
        totalLength = contentLength();
        ForwardingSink forwardingSink = new ForwardingSink(bufferedSink) {
            @Override
            public void write(@NotNull Buffer source, long byteCount) throws IOException {
                currentLength += byteCount;
                if (progressListener != null) {
                    progressListener.onProgress(currentLength, totalLength);
                }
                super.write(source, byteCount);
            }
        };
        BufferedSink buffer = Okio.buffer(forwardingSink);
        requestBody.writeTo(buffer);
        buffer.flush();
    }
}

