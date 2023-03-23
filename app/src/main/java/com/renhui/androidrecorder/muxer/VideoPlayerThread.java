package com.renhui.androidrecorder.muxer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Camera;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
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
import java.io.IOException;


public class VideoPlayerThread extends Thread{

    private static VideoPlayerThread videoPlayerThread;

    private VideoView mVideo;
    private Context mContext;

    public VideoPlayerThread(Context mContext, VideoView mVideo) {
        // 初始化相关对象和参数
        this.mContext = mContext;
        this.mVideo = mVideo;
    }

    public static void startPlay(Context mContext, VideoView mVideo) {
        if (videoPlayerThread == null) {
            synchronized (VideoPlayerThread.class) {
                if (videoPlayerThread == null) {
                    videoPlayerThread = new VideoPlayerThread(mContext, mVideo);
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
        // 移除对Surfaceview的布局位置调整
        RelativeLayout.LayoutParams cameraparams = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
        cameraparams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        cameraparams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
        cameraparams.removeRule(RelativeLayout.ALIGN_BOTTOM);
        cameraparams.removeRule(RelativeLayout.ALIGN_LEFT);
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
                    // 加载本地视频
                    String videoPath = Environment.getExternalStorageDirectory() + "/android_records/video/smile1.mp4";  //2023_03_17_17_31.mp4
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
