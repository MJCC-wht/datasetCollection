package com.renhui.androidrecorder.muxer;

import android.Manifest;
import android.content.Intent;
import android.app.ActionBar;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.renhui.androidrecorder.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 音视频混合界面
 */
public class MediaMuxerActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    SurfaceView surfaceView;
    VideoView mVideo;
    Button videoStartStopButton;
    Button audioStartStopButton;
    Button changeCameraButton;
    Button noButton;

    Camera camera;
    int cameraId;
    SurfaceHolder surfaceHolder;
    SurfaceTexture surfaceTexture;

    // 当前是否有情绪认知部分在播放
    boolean videoDisplay = true;
    // 用于显示的图片
    Bitmap audioBitmap;
    // 文件名
    String filePath ;
    // 判断是否需要摄像头小窗
    boolean cameraWindow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media_muxer);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "申请权限", Toast.LENGTH_SHORT).show();
            // 申请相机、麦克风和存储权限
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

        // 拿到从上一个页面传过来的文件名
        Intent intent = getIntent();
        filePath = intent.getStringExtra("complete_info");

        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        videoStartStopButton = (Button) findViewById(R.id.videoStartStop);
        audioStartStopButton = (Button) findViewById(R.id.audioStartStop);
        changeCameraButton = (Button) findViewById(R.id.changeCamera);
        mVideo = (VideoView) findViewById(R.id.video);

        // 摄像头小窗位置标定
        noButton = (Button) findViewById(R.id.noButton);
        noButton.setVisibility(View.INVISIBLE);


        videoStartStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag().toString().equalsIgnoreCase("stop")) {
                    view.setTag("start");
                    ((TextView) view).setText("录制视频");
                    MediaMuxerThread.stopMuxer();
                    // 视频录制完，上传文件
                    FileUploadThread.startUpload(MediaMuxerThread.filePath, MediaMuxerThread.tagName);
                    VideoPlayerThread.stopPlay(mVideo, surfaceView);
                    // 恢复摄像头角度设置
                    camera.setDisplayOrientation(90);
                    stopCamera();
                } else {
                    startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    // 一开始为竖屏的时候，摄像头小窗透明
                    surfaceView.setAlpha(0);
                    view.setTag("stop");
                    ((TextView) view).setText("停止录制");
                    MediaMuxerThread.startMuxer(filePath);
                    if (camera == null) {
                        Log.w("MainActivity", "camera gone");
                    }
                    FileUploadThread.stopUpload();
                    VideoPlayerThread.startPlay(MediaMuxerActivity.this, mVideo);
                }
            }
        });

        audioStartStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 停止录制
                if (view.getTag().toString().equalsIgnoreCase("stop")) {
                    view.setTag("start");
                    ((TextView) view).setText("录制音频");
                    AudioEncoderThread.stopAudio();
                    // 音频录制完，上传文件
                    FileUploadThread.startUpload(AudioEncoderThread.filePath, AudioEncoderThread.tagName);
                    updateImage(null);
                } else {
                    // 开始录制
                    view.setTag("stop");
                    ((TextView) view).setText("停止录制");
                    AudioEncoderThread.startAudio(filePath);
                    FileUploadThread.stopUpload();
                    try {
                        updateImage(getLocalImage("file://" + Environment.getExternalStorageDirectory().getPath() + "/android_records/audioImage/1.png"));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        changeCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag().toString().equalsIgnoreCase("front")) {
                    view.setTag("back");
                    changeCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                } else {
                    view.setTag("front");
                    changeCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                }
            }
        });

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.w("MainActivity", "enter surfaceCreated method");
        this.surfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.w("MainActivity", "enter surfaceChanged method");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.w("MainActivity", "enter surfaceDestroyed method");
        MediaMuxerThread.stopMuxer();
        AudioEncoderThread.stopAudio();
        stopCamera();
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        MediaMuxerThread.addVideoFrameData(bytes);
    }

    // 重写onConfigurationChanged监听屏幕方向的改变
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏时给出横屏弹窗、全屏显示
            Toast.makeText(getApplicationContext(), "横屏", Toast.LENGTH_SHORT).show();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mVideo.getLayoutParams();
            // 移除必须在按钮之下的位置设定，让横屏能全屏播放
            params.removeRule(RelativeLayout.BELOW);
            mVideo.setLayoutParams(params);
            Log.e("view change", "横屏");

            // 横屏时显示摄像头预览窗口
            if (cameraWindow) {
                // 设置摄像头小窗置于最高层、不透明、尺寸与nobuttun贴合
                surfaceView.setAlpha(1);
                surfaceView.setZOrderOnTop(true);
                if (camera != null) {
                    RelativeLayout.LayoutParams cameraparams = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
                    cameraparams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.noButton);
                    cameraparams.addRule(RelativeLayout.ALIGN_LEFT, R.id.noButton);
                    cameraparams.addRule(RelativeLayout.ALIGN_PARENT_TOP, R.id.topButton);
                    cameraparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, R.id.video);

                    // 使摄像头方向为正
                    camera.setDisplayOrientation(0);
                }
            }
        } else {
            // 竖屏时给出竖屏弹窗、显示按钮
            Toast.makeText(getApplicationContext(), "竖屏", Toast.LENGTH_SHORT).show();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mVideo.getLayoutParams();
            // 竖屏时恢复在按钮之下的布局设定
            params.addRule(RelativeLayout.BELOW, R.id.topButton);
            mVideo.setLayoutParams(params);
            Log.e("view change", "竖屏");

            // 竖屏时不显示摄像头
            if (cameraWindow) {
                if (camera != null) {
                    // 小窗透明、最高层设定取消
                    surfaceView.setAlpha(0);
                    surfaceView.setZOrderOnTop(false);
                }
            }
        }
    }
    //----------------------- 摄像头操作相关 --------------------------------------

    /**
     * 打开摄像头
     */
    private void startCamera(int cameraId) {
        this.cameraId = cameraId;
        camera = Camera.open(cameraId);
        camera.setDisplayOrientation(90);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
//        camera.cancelAutoFocus();
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        // 这个宽高的设置必须和后面编解码的设置一样，否则不能正常处理
        parameters.setPreviewSize(1920, 1080);

        try {
            camera.setParameters(parameters);
            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(MediaMuxerActivity.this);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭摄像头
     */
    private void stopCamera() {
        // 停止预览并释放资源
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    /**
     * 切换前后摄像头
     */
    private void changeCamera(int cameraId) {
        // 先确认关闭
        stopCamera();
        // 然后开启对应Id的camera
        startCamera(cameraId);
    }

    // ----------------------- 在SurfaceView上显示图片 --------------------------------------

    private Bitmap getLocalImage(String imagePath) throws FileNotFoundException {
        Uri imageUri = Uri.parse(imagePath);
        InputStream imageStream = getContentResolver().openInputStream(imageUri);
        return BitmapFactory.decodeStream(imageStream);
    }

    /**
     * 画图
     */
    private void drawImage() {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas != null && audioBitmap != null) {
            canvas.drawBitmap(audioBitmap, 0, 0, null);
            surfaceHolder.unlockCanvasAndPost(canvas);
        } else if (canvas != null) {
            canvas.drawColor(Color.BLACK);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    /**
     * 更新图片
     */
    private void updateImage(Bitmap bitmap) {
        // 释放原有的Bitmap
        if (audioBitmap != null) {
            audioBitmap.recycle();
        }
        if (bitmap != null) {
            int surfaceWidth = surfaceView.getWidth();
            int surfaceHeight = surfaceView.getHeight();

            float widthRatio = (float) surfaceWidth / bitmap.getWidth();
            float heightRatio = (float) surfaceHeight / bitmap.getHeight();
            float ratio = Math.min(widthRatio, heightRatio);

            audioBitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * ratio), (int) (bitmap.getHeight() * ratio), false);
        } else {
            audioBitmap = null;
        }
        drawImage();
    }
}