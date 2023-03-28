package com.renhui.androidrecorder.muxer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Camera;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.renhui.androidrecorder.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class VideoPlayerThread extends Thread{

    private static VideoPlayerThread videoPlayerThread;

    private VideoView mVideo;
    private Context mContext;
    private String videotext;

    public VideoPlayerThread(Context mContext, VideoView mVideo, String videotext) {
        // 初始化相关对象和参数
        this.mContext = mContext;
        this.mVideo = mVideo;
        this.videotext = videotext;
    }

    public static void startPlay(Context mContext, VideoView mVideo, String videotext) {
        if (videoPlayerThread == null) {
            synchronized (VideoPlayerThread.class) {
                if (videoPlayerThread == null) {
                    videoPlayerThread = new VideoPlayerThread(mContext, mVideo, videotext);
                    videoPlayerThread.start();
                }
            }
        }
    }

    public static void stopPlay(VideoView mVideo, SurfaceView surfaceView) {
        videoPlayerThread = null;
        Log.e("tag","播放手动停止");
        // 在任何状态下释放媒体播放器
        mVideo.suspend();
        // 移除对Surfaceview的布局位置调整和设置
        RelativeLayout.LayoutParams cameraparams = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
        cameraparams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        cameraparams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
        cameraparams.removeRule(RelativeLayout.ALIGN_BOTTOM);
        cameraparams.removeRule(RelativeLayout.ALIGN_LEFT);
    }

    // 选择需要播放的视频标签
    public String switchVideo(String videotext){
        switch(videotext) {
            case "recognition1": case "emotion1":
                videotext = "01maiguai.mp4";
                break;
            case "recognition2": case "emotion2":
                videotext = "02maiche.mp4";
                break;
            case "recognition3": case "emotion3":
                videotext = "03lixueqin.mp4";
                break;
            case "recognition4": case "emotion4":
                videotext = "04zhangerda.mp4";
                break;
            case "recognition5": case "emotion5":
                videotext = "05twins.mp4";
                break;
            default:
                break;
        }
        return videotext;
    }

    //将assets内文件存储到本地下载目录
    public Boolean Deposit(String path,String fileName){
        InputStream inputStream;
        try {

            //判断文件是否存在
            File file1 = new File(path + "/" + fileName);

            if (!file1.exists()) {

                inputStream = mContext.getAssets().open(fileName);
                File file = new File(path);

                //当目录不存在时创建目录
                if (!file.exists()) {
                    file.mkdirs();
                }

                FileOutputStream fileOutputStream = new FileOutputStream(path + "/" + fileName);// 保存到本地的文件夹下的文件
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, count);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                inputStream.close();

            } else {
                Log.e("fileexist","目标视频文件已存在");
            }

            return true;


        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public void run() {
        try {
            //通知主线程更新控件
            myHandler.sendEmptyMessage(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //handler为线程之间通信的桥梁
    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler(){
        @SuppressLint("HandlerLeak")
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 1:
                    // 根据上面的提示，当Message为1，表示数据处理完了，可以通知主线程了
                    // 下载目标视频
                    videotext = switchVideo(videotext);
                    Deposit(Environment.getExternalStorageDirectory() + "/android_records/targetvideo", videotext);
                    // 加载本地视频
                    String videoPath = Environment.getExternalStorageDirectory() + "/android_records/targetvideo/" + videotext;
                    File file = new File(videoPath);
                    if (file.exists()) {
                        // 设置视频地址
                        mVideo.setVideoPath(file.getAbsolutePath());
                    } else {
                        Log.w("videoopenError", "视频不存在！");
                        Toast.makeText(mContext, "视频不存在", Toast.LENGTH_SHORT).show();
                    }

                    //视频控制器
                    MediaController mediaController = new MediaController(mContext);
                    //VideoView绑定控制器
                    mVideo.setMediaController(mediaController);
                    //设置视频地址
                    mVideo.setVideoPath(videoPath);
                    //获取焦点
                    mVideo.requestFocus();
                    //播放视频
                    mVideo.start();

                    // 检测视频是否播放完成
                    mVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Log.e("tag","播放完成");
                            Toast.makeText(MediaMuxerActivity.mainActivity, "播放完成，请恢复竖屏并停止录制", Toast.LENGTH_SHORT).show();
                            VoiceBroadcastThread.stopBroadcast();
                            VoiceBroadcastThread.startBroadcast(MediaMuxerActivity.mainActivity, "播放完成，请恢复竖屏并停止录制");
//                            //停止播放视频,并且释放
//                            mVideoView.stopPlayback();
                            //在任何状态下释放媒体播放器
                            mVideo.suspend();
                        }
                    });
                    break;
                default :
                    break;
            }
        }
    };
}
