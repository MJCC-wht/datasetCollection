package com.renhui.androidrecorder.muxer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.renhui.androidrecorder.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
    Bitmap audioBitmap;
    // 文件名
    String filePath ;

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
                } else {
                    startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                    surfaceView.setAlpha(0);
                    surfaceView.setTranslationZ(0);
                    view.setTag("stop");
                    ((TextView) view).setText("停止录制");
                    MediaMuxerThread.startMuxer(filePath);
                    if (camera == null) {
                        Log.w("MainActivity", "camera gone");
                    }
                    FileUploadThread.stopUpload();
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
//        if (videoDisplay) {
//            Canvas canvas = surfaceHolder.lockCanvas();
//            // 绘制图片
//            if (canvas != null) {
//                canvas.drawBitmap();
//            }
//        }


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

    // ----------------------- 摄像头操作相关 --------------------------------------

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
//            if (videoDisplay) {
//                camera.setPreviewTexture(surfaceTexture);
//            } else {
//                camera.setPreviewDisplay(surfaceHolder);
//            }
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