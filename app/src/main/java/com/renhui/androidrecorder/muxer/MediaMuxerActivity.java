package com.renhui.androidrecorder.muxer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.ActionBar;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.renhui.androidrecorder.MyApplication;
import com.renhui.androidrecorder.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * 音视频混合界面
 */
public class MediaMuxerActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    static MediaMuxerActivity mainActivity;
    SurfaceView surfaceView;
    VideoView mVideo;
    Button videoStartStopButton;
    Button audioStartStopButton;
    Button changeCameraButton;
    Button reUploadButton;
    Chronometer chronometer;
    Button noButton;
    ProgressBar uploadProgress;

    Camera camera;
    int cameraId;
    boolean occupiedByCanvas = false;
    boolean running = false;
    SurfaceHolder surfaceHolder;
    SurfaceTexture surfaceTexture;

    // 当前是否有情绪认知部分在播放
    boolean videoDisplay = true;
    // 用于显示的图片
    Bitmap audioBitmap;
    // 文件名
    String filePath, floatWindow;
    String[] filePathList;
    // 判断是否需要摄像头小窗
    boolean cameraWindow = true;
    // 任务类型的宏参数
    int ACTION_TYPE = 0;
    int RECOGNITION_TYPE = 1;
    int EMOTION_TYPE = 3;
    int DESCRIPTION_TYPE = 4;
    int WRONG_TYPE = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = this;

        // 隐藏标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
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

        // 刚进入页面给文件名置空，防止重新上传错误的文件
        MediaMuxerThread.filePath = null;
        AudioEncoderThread.filePath = null;

        // 拿到从上一个页面传过来的文件名
        Intent intent = getIntent();
        filePath = intent.getStringExtra("complete_info");
        filePathList = filePath.split("/");
        switchBroadcast(filePathList[1]);

        // 选择是否需要小窗口  yes or no
        floatWindow = intent.getStringExtra("cameraWindow") ;
        if (floatWindow != null &&  floatWindow.equals("no")) {
            cameraWindow = false;
        }// default  true

        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        videoStartStopButton = (Button) findViewById(R.id.videoStartStop);
        audioStartStopButton = (Button) findViewById(R.id.audioStartStop);
//        changeCameraButton = (Button) findViewById(R.id.changeCamera);
        reUploadButton = (Button) findViewById(R.id.reUpload);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        // 默认不可见
        chronometer.setVisibility(View.INVISIBLE);
        mVideo = (VideoView) findViewById(R.id.video);

        // 摄像头小窗位置标定
        noButton = (Button) findViewById(R.id.noButton);
        noButton.setVisibility(View.INVISIBLE);

        // 上传进度条
        uploadProgress = (ProgressBar) findViewById(R.id.upload_progress);
        uploadProgress.setVisibility(View.GONE);

        videoStartStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag().toString().equalsIgnoreCase("stop")) {
                    // 如果此时视频还在播放，跳出弹窗提醒是否要关闭
                    if (mVideo.isPlaying() && filePathList[1].startsWith("recognition")) {
                        AlertDialog closeDialog = new AlertDialog.Builder(MediaMuxerActivity.this)
                                .setTitle("视频正在播放：")
                                .setMessage("确定要停止录制吗")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        view.setTag("start");
                                        ((TextView) view).setText("录制视频");
                                        Toast.makeText(MediaMuxerActivity.this, "拍摄完成", Toast.LENGTH_SHORT).show();
                                        MediaMuxerThread.stopMuxer();
                                        // 视频录制完，上传文件
                                        uploadProgress.setVisibility(View.VISIBLE);
                                        FileUploadThread.startUpload(MediaMuxerActivity.this, MediaMuxerThread.filePath, MediaMuxerThread.tagName);
                                        // 如果结束时为横屏屏，不显示摄像头预览
                                        if (cameraWindow && camera != null && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                            // 小窗透明、最高层设定取消
                                            surfaceView.setAlpha(0);
                                            surfaceView.setZOrderOnTop(false);
                                        }
                                        VideoPlayerThread.stopPlay(mVideo, surfaceView);
                                        stopCamera();
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).create();
                        closeDialog.show();
                    } else {
                        view.setTag("start");
                        ((TextView) view).setText("录制视频");
                        Toast.makeText(MediaMuxerActivity.this, "拍摄完成", Toast.LENGTH_SHORT).show();
                        MediaMuxerThread.stopMuxer();
                        // 视频录制完，上传文件
                        uploadProgress.setVisibility(View.VISIBLE);
                        FileUploadThread.startUpload(MediaMuxerActivity.this, MediaMuxerThread.filePath, MediaMuxerThread.tagName);
                        // 如果结束时为横屏屏，不显示摄像头预览
                        if (cameraWindow && camera != null && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            // 小窗透明、最高层设定取消
                            surfaceView.setAlpha(0);
                            surfaceView.setZOrderOnTop(false);
                        }
                        VideoPlayerThread.stopPlay(mVideo, surfaceView);
                        stopCamera();
                    }

//                    view.setTag("start");
//                    ((TextView) view).setText("录制视频");
//                    Toast.makeText(MediaMuxerActivity.this, "拍摄完成", Toast.LENGTH_SHORT).show();
//                    MediaMuxerThread.stopMuxer();
//                    // 视频录制完，上传文件
//                    uploadProgress.setVisibility(View.VISIBLE);
//                    FileUploadThread.startUpload(MediaMuxerActivity.this, MediaMuxerThread.filePath, MediaMuxerThread.tagName);
//                    stopCamera();
                } else {
                    uploadProgress.setVisibility(View.GONE);
                    if (confirmType(filePathList[1]) == RECOGNITION_TYPE || confirmType(filePathList[1]) == EMOTION_TYPE) {
                        startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                        // 一开始为竖屏的时候，摄像头小窗透明
                        surfaceView.setAlpha(0);
                    } else if (confirmType(filePathList[1]) == ACTION_TYPE) {
                        startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                    } else if (confirmType(filePathList[1]) == DESCRIPTION_TYPE) {
                        Toast.makeText(MediaMuxerActivity.this, "禁止录制视频", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    view.setTag("stop");
                    ((TextView) view).setText("停止录制");
                    MediaMuxerThread.startMuxer(filePath);
                    FileUploadThread.stopUpload();
                    if (confirmType(filePathList[1]) == RECOGNITION_TYPE || confirmType(filePathList[1]) == EMOTION_TYPE) {
                        VideoPlayerThread.startPlay(MediaMuxerActivity.this, mVideo, filePathList[1]);
//                        if (cameraWindow) {
//                            // 设置摄像头小窗置于最高层、不透明、尺寸与nobuttun贴合
//                            surfaceView.setAlpha(0);
//                            surfaceView.setZOrderOnTop(true);
//                            if (camera != null) {
//                                RelativeLayout.LayoutParams cameraparams = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
//                                cameraparams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.noButton);
//                                cameraparams.addRule(RelativeLayout.ALIGN_LEFT, R.id.noButton);
//                                cameraparams.addRule(RelativeLayout.ALIGN_PARENT_TOP, R.id.topButton);
//                                cameraparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, R.id.video);
//                            }
//                        }
                    }
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
                    Toast.makeText(MediaMuxerActivity.this, "录音完成", Toast.LENGTH_SHORT).show();
                    AudioEncoderThread.stopAudio();
                    // 音频录制完，上传文件
                    uploadProgress.setVisibility(View.VISIBLE);
                    FileUploadThread.startUpload(MediaMuxerActivity.this, AudioEncoderThread.filePath, AudioEncoderThread.tagName);
                    if (filePathList[1] != null && filePathList[1].equals("description2")) {
                        updateImage(null);
                    }
                    // 停止计时器
                    if (running) {
                        chronometer.stop();
                        running = false;
                        chronometer.setVisibility(View.INVISIBLE);
                        videoStartStopButton.setVisibility(View.VISIBLE);
                        reUploadButton.setVisibility(View.VISIBLE);
                    }
                } else {
                    uploadProgress.setVisibility(View.GONE);
                    if (confirmType(filePathList[1]) != DESCRIPTION_TYPE) {
                        Toast.makeText(MediaMuxerActivity.this, "禁止录制视频", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 开始录制
                    view.setTag("stop");
                    ((TextView) view).setText("停止录制");
                    AudioEncoderThread.startAudio(filePath);
                    FileUploadThread.stopUpload();
                    updateImage(getImageFromAssetsFile(switchImage(filePathList[1])));
                    // 开始计时器
                    if (!running) {
                        chronometer.setVisibility(View.VISIBLE);
                        videoStartStopButton.setVisibility(View.INVISIBLE);
                        reUploadButton.setVisibility(View.INVISIBLE);
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        chronometer.start();
                        running = true;
                    }
                }
            }
        });

        reUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((MediaMuxerThread.filePath == null && confirmType(filePathList[1]) == ACTION_TYPE) ||
                        (AudioEncoderThread.filePath == null && confirmType(filePathList[1]) == DESCRIPTION_TYPE)) {
                    Toast.makeText(MediaMuxerActivity.this, "还没有录制过，请进行录制", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 重新上传最近的文件
                uploadProgress.setVisibility(View.VISIBLE);
                FileUploadThread.stopUpload();
                if (confirmType(filePathList[1]) != DESCRIPTION_TYPE) {
                    FileUploadThread.startUpload(MediaMuxerActivity.this, MediaMuxerThread.filePath, MediaMuxerThread.tagName);
                } else {
                    FileUploadThread.startUpload(MediaMuxerActivity.this, AudioEncoderThread.filePath, AudioEncoderThread.tagName);
                }
            }
        });

//        changeCameraButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (camera != null) {
//                    if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                        changeCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
//                    } else {
//                        changeCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
//                    }
//                } else {
//                    Toast.makeText(MediaMuxerActivity.this, "相机未开启", Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.w("MainActivity", "enter surfaceCreated method");
        if (filePathList[1] != null && (filePathList[1].startsWith("description"))) {
            updateImage(getImageFromAssetsFile(switchImage(filePathList[1])));
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.w("MainActivity", "enter surfaceChanged method");
        // 横竖屏切换显示图片
        if (occupiedByCanvas) {
            adaptImage();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.w("MainActivity", "enter surfaceDestroyed method");
        MediaMuxerThread.stopMuxer();
        AudioEncoderThread.stopAudio();
        VoiceBroadcastThread.stopBroadcast();
        VideoPlayerThread.stopPlay(mVideo, surfaceView);
        FileUploadThread.stopUpload();
        stopCamera();
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            int width = camera.getParameters().getPreviewSize().width;
            int height = camera.getParameters().getPreviewSize().height;
            if (!isTablet(MediaMuxerActivity.this)) {
                bytes = rotateYUV420Degree180(bytes, width, height);
            }
        }
        MediaMuxerThread.addVideoFrameData(bytes);
    }

    // 重写onConfigurationChanged监听屏幕方向的改变
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 先设置旋转角度
        if (camera != null) {
            camera.setDisplayOrientation(getCameraRotation());
        }
        if (filePathList[1].startsWith("recognition")) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                // 横屏时给出横屏弹窗、全屏显示
                Toast.makeText(getApplicationContext(), "横屏", Toast.LENGTH_SHORT).show();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mVideo.getLayoutParams();
                // 移除必须在按钮之下的位置设定，让横屏能全屏播放
                params.removeRule(RelativeLayout.BELOW);
                mVideo.setLayoutParams(params);
                Log.e("view change", "横屏");

                // 横屏时显示摄像头预览窗口
                if (cameraWindow && camera != null) {
                    // 设置摄像头小窗置于最高层、不透明、尺寸与nobuttun贴合
                    surfaceView.setAlpha(1);
                    surfaceView.setZOrderOnTop(true);
                    // 隐藏状态栏
                    mVideo.setSystemUiVisibility(View.INVISIBLE);

                    RelativeLayout.LayoutParams cameraparams = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
                    cameraparams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.noButton);
                    cameraparams.addRule(RelativeLayout.ALIGN_LEFT, R.id.noButton);
                    cameraparams.addRule(RelativeLayout.ALIGN_PARENT_TOP, R.id.topButton);
                    cameraparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, R.id.video);
                }
            } else {
                // 竖屏时给出竖屏弹窗、显示按钮
//                uploadProgress.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "竖屏", Toast.LENGTH_SHORT).show();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mVideo.getLayoutParams();
                // 竖屏时恢复在按钮之下的布局设定
                params.addRule(RelativeLayout.BELOW, R.id.topButton);
                mVideo.setLayoutParams(params);
                // 显示状态栏
                mVideo.setSystemUiVisibility(View.VISIBLE);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApplication.getInstance().setCurrentActivity(null);
    }

    //----------------------- 摄像头操作相关 --------------------------------------

    /**
     * 打开摄像头
     */
    private void startCamera(int cameraId) {
//        resumePreview();
        this.cameraId = cameraId;
        camera = Camera.open(cameraId);
        camera.setDisplayOrientation(getCameraRotation());
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        // 这个宽高的设置必须和后面编解码的设置一样，否则不能正常处理
        parameters.setPreviewSize(1920, 1080);

        try {
            surfaceView.setBackgroundColor(Color.parseColor("#00000000"));
            mVideo.setBackgroundColor(Color.parseColor("#00000000"));
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
            surfaceView.setBackgroundColor(Color.BLACK);
            mVideo.setBackgroundColor(Color.BLACK);
            camera.stopPreview();
            camera.release();
            camera = null;
            // 处理surfaceHolder
            surfaceHolder.removeCallback(this);
        }
//        updateImage(null);
    }

    private void stopCameraWithoutStopPreview() {
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
        stopCameraWithoutStopPreview();
        // 然后开启对应Id的camera
        startCamera(cameraId);
    }

    private void resumePreview() {
        if (occupiedByCanvas) {
            // 被占用
            surfaceView.setVisibility(View.GONE);
            surfaceView.setVisibility(View.VISIBLE);
            occupiedByCanvas = false;
        }
    }

    private int getCameraRotation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
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
        if (canvas != null) {
            if (audioBitmap != null) {
                int bitmapWidth = audioBitmap.getWidth();
                int bitmapHeight = audioBitmap.getHeight();
                int canvasWidth = canvas.getWidth();
                int canvasHeight = canvas.getHeight();
                float left = (canvasWidth - bitmapWidth) / 2f;
                float top = (canvasHeight - bitmapHeight) / 2f;
                Rect mSrcRect = new Rect(0, 0, bitmapWidth, bitmapHeight);
                RectF mDstRect = new RectF(left, top, left + bitmapWidth, top + bitmapHeight);
                canvas.drawBitmap(audioBitmap, mSrcRect, mDstRect, null);
            } else {
                canvas.drawColor(Color.BLACK);
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
            occupiedByCanvas = true;
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

    private void adaptImage() {
        if (audioBitmap != null) {
            int surfaceWidth = surfaceView.getWidth();
            int surfaceHeight = surfaceView.getHeight();

            float widthRatio = (float) surfaceWidth / audioBitmap.getWidth();
            float heightRatio = (float) surfaceHeight / audioBitmap.getHeight();
            float ratio = Math.min(widthRatio, heightRatio);

            audioBitmap = Bitmap.createScaledBitmap(audioBitmap, (int) (audioBitmap.getWidth() * ratio),
                    (int) (audioBitmap.getHeight() * ratio), false);
            drawImage();
        }
    }

    private int confirmType(String name) {
        if (name.startsWith("action")) {
            return ACTION_TYPE;
        } else if (name.startsWith("recognition")) {
            return RECOGNITION_TYPE;
        } else if (name.startsWith("emotion")) {
            return EMOTION_TYPE;
        } else if (name.startsWith("description")) {
            return DESCRIPTION_TYPE;
        } else {
            return WRONG_TYPE;
        }
    }

    // 检测需要播报的内容
    private void switchBroadcast(String text) {
        switch (text) {
            case "action1":
                VoiceBroadcastThread.stopBroadcast();
                VoiceBroadcastThread.startBroadcast(MediaMuxerActivity.this, "请正常行走，持续十秒左右");
                break;
            case "action2":
                VoiceBroadcastThread.stopBroadcast();
                VoiceBroadcastThread.startBroadcast(MediaMuxerActivity.this, "请从椅子上站起，再坐下");
                break;
            case "action3":
                VoiceBroadcastThread.stopBroadcast();
                VoiceBroadcastThread.startBroadcast(MediaMuxerActivity.this, "请顺时针三百六十度转身，再逆时针三百六十度转身");
                break;
            case "recognition1": case "recognition2":
                VoiceBroadcastThread.stopBroadcast();
                VoiceBroadcastThread.startBroadcast(MediaMuxerActivity.this, "请完整观看目标视频。");
                break;
            case "description1":
                VoiceBroadcastThread.stopBroadcast();
                VoiceBroadcastThread.startBroadcast(MediaMuxerActivity.this, "请朗读图中的文字。");
                break;
            case "description2":
                VoiceBroadcastThread.stopBroadcast();
                VoiceBroadcastThread.startBroadcast(MediaMuxerActivity.this, "请在一分钟内，描述目标图片的场景。");
                break;
            default:
                break;
        }
    }

    // 根据点击的按钮呈现不同的图片
    public String switchImage(String imageText){
        switch (imageText) {
            case "description1":
                imageText = "text.jpg";
                break;
            case "description2":
                imageText = "story.jpg";
                break;
            default:
                break;
        }
        return imageText;
    }

    // 获取assets中的图片
    private Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            Log.e("error", "getImageFromAssetsFile: IOException!");
        }
        return image;
    }

    // 对OnPreviewFrame中的byte数组进行上下翻转（前置摄像头）
    private  byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int i = 0;
        int count = 0;
        for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
            yuv[count] = data[i];
            count++;
        }
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth
                * imageHeight; i -= 2) {
            yuv[count++] = data[i - 1];
            yuv[count++] = data[i];
        }
        return yuv;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

}