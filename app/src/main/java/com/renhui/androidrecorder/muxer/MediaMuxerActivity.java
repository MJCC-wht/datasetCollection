package com.renhui.androidrecorder.muxer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.renhui.androidrecorder.R;

import java.io.IOException;

/**
 * 音视频混合界面
 */
public class MediaMuxerActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    SurfaceView surfaceView;
    Button videoStartStopButton;
    Button audioStartStopButton;
    Button changeCameraButton;

    Camera camera;
    SurfaceHolder surfaceHolder;
    SurfaceTexture surfaceTexture;

    // 当前是否有情绪认知部分在播放
    boolean videoDisplay = false;
    // 文件名
    String fileName ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media_muxer);

        // 接收文件名
        Intent intent = getIntent();
        fileName = intent.getStringExtra("complete_info");

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

        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        videoStartStopButton = (Button) findViewById(R.id.videoStartStop);
        audioStartStopButton = (Button) findViewById(R.id.audioStartStop);
        changeCameraButton = (Button) findViewById(R.id.changeCamera);

        videoStartStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag().toString().equalsIgnoreCase("stop")) {
                    view.setTag("start");
                    ((TextView) view).setText("录制视频");
                    MediaMuxerThread.stopMuxer();
                    stopCamera();
                    // 视频录制完，上传文件
                    FileUploadThread.startUpload(MediaMuxerThread.filePath, MediaMuxerThread.tagName);
//                    finish();
                } else {
                    startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                    view.setTag("stop");
                    ((TextView) view).setText("停止录制");
                    MediaMuxerThread.startMuxer();
                    FileUploadThread.stopUpload();
                }
            }
        });

        audioStartStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag().toString().equalsIgnoreCase("stop")) {
                    view.setTag("start");
                    ((TextView) view).setText("录制音频");
                    AudioEncoderThread.stopAudio();
                    // 音频录制完，上传文件
                    FileUploadThread.startUpload(AudioEncoderThread.filePath, AudioEncoderThread.tagName);
                } else {
                    view.setTag("stop");
                    ((TextView) view).setText("停止录制");
                    AudioEncoderThread.startAudio();
                    FileUploadThread.stopUpload();
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
        surfaceTexture = new SurfaceTexture(10);

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

    //----------------------- 摄像头操作相关 --------------------------------------

    /**
     * 打开摄像头
     */
    private void startCamera(int cameraId) {
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
            // 根据情况播放视频
            if (videoDisplay) {
                camera.setPreviewTexture(surfaceTexture);
            } else {
                camera.setPreviewDisplay(surfaceHolder);
            }
            camera.setPreviewCallback(MediaMuxerActivity.this);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 进行自动对焦
//        camera.autoFocus((b, camera) -> {
//            if (b) {
//                Log.w("MainActivity", "autofocus success");
//            }
//        });
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

}