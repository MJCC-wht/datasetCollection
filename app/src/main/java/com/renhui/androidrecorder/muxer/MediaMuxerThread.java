package com.renhui.androidrecorder.muxer;


import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * 音视频混合线程
 */
public class MediaMuxerThread extends Thread {

    private static final String TAG = "MediaMuxerThread";
    public static final int TRACK_VIDEO = 0;
    private final Object lock = new Object();
    private static MediaMuxerThread mediaMuxerThread;

    private static final String MAIN_DIR_NAME = "/android_records";
    private static final String BASE_VIDEO = "/video/";
    private static final String BASE_AUDIO = "/audio/";
    private static final String BASE_VIDEO_EXT = ".mp4";
    private static final String BASE_AUDIO_EXT = ".wav";

    private VideoEncoderThread videoThread;
    private MediaMuxer mediaMuxer;
    private Vector<MuxerData> muxerDatas;
    private int videoTrackIndex = -1;

    // 用于上传的文件路径和名字
    public static String filePath;
    public static String tagName;

    // 视频轨添加状态
    private volatile boolean isVideoTrackAdd;
    private volatile boolean isExit = false;

    private MediaMuxerThread() {
        // 构造函数
    }

    // 开始音视频混合任务
    public static void startMuxer(String filePath) {
        if (mediaMuxerThread == null) {
            synchronized (MediaMuxerThread.class) {
                if (mediaMuxerThread == null) {
                    mediaMuxerThread = new MediaMuxerThread();
                    MediaMuxerThread.filePath = filePath;
                    Log.e("111", "mediaMuxerThread.start();");
                    mediaMuxerThread.start();
                }
            }
        }
    }

    // 停止音视频混合任务
    public static void stopMuxer() {
        if (mediaMuxerThread != null) {
            mediaMuxerThread.exit();
            try {
                mediaMuxerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mediaMuxerThread = null;
        }
    }


    private void readyStart() throws IOException {
        isExit = false;
        isVideoTrackAdd = false;
        muxerDatas.clear();

        mediaMuxer = new MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        if (videoThread != null) {
            videoThread.setMuxerReady(true);
        }
        Log.e(TAG, "readyStart(String filePath, boolean restart) 保存至:" + filePath);
    }

    // 从预览图中添加视频帧数据
    public static void addVideoFrameData(byte[] data) {
        if (mediaMuxerThread != null) {
            mediaMuxerThread.addVideoData(data);
        }
    }

    // 向Muxer中添加数据
    public void addMuxerData(MuxerData data) {
        if (!isMuxerStart()) {
            return;
        }

        muxerDatas.add(data);
        synchronized (lock) {
            lock.notify();
        }
    }

    /**
     * 添加视频／音频轨
     *
     * @param index
     * @param mediaFormat
     */
    public synchronized void addTrackIndex(int index, MediaFormat mediaFormat) {
        if (isMuxerStart()) {
            return;
        }

        /* 如果已经添加了，就不做处理了 */
        if (index == TRACK_VIDEO && isVideoTrackAdd()) {
            return;
        }

        if (mediaMuxer != null) {
            int track = 0;
            try {
                track = mediaMuxer.addTrack(mediaFormat);
            } catch (Exception e) {
                Log.e(TAG, "addTrack 异常:" + e);
                return;
            }

            if (index == TRACK_VIDEO) {
                videoTrackIndex = track;
                isVideoTrackAdd = true;
                Log.e(TAG, "添加视频轨完成");
            }

            requestStart();
        }
    }

    /**
     * 请求Muxer开始启动
     */
    private void requestStart() {
        synchronized (lock) {
            if (isMuxerStart()) {
                mediaMuxer.start();
                Log.e(TAG, "requestStart启动混合器..开始等待数据输入...");
                lock.notify();
            }
        }
    }

    /**
     * 当前是否添加了视频轨
     *
     * @return
     */
    public boolean isVideoTrackAdd() {
        return isVideoTrackAdd;
    }

    /**
     * 当前音视频合成器是否运行了
     *
     * @return
     */
    public boolean isMuxerStart() {
        return isVideoTrackAdd;
    }


    // 添加视频数据
    private void addVideoData(byte[] data) {
        if (videoThread != null) {
            videoThread.add(data);
        }
    }

    private void initMuxer() {
        muxerDatas = new Vector<>();
        FileUtil fileSwapHelper = new FileUtil(filePath);
        fileSwapHelper.getSaveFilePath();
        filePath = fileSwapHelper.getFullPath();
        tagName = fileSwapHelper.getFilePath();
        videoThread = new VideoEncoderThread(1920, 1080, new WeakReference<MediaMuxerThread>(this));
        videoThread.start();
        try {
            // 对文件进行操作
            readyStart();
        } catch (IOException e) {
            Log.e(TAG, "initMuxer 异常:" + e);
        }
    }

    @Override
    public void run() {
        super.run();
        // 初始化Muxer
        initMuxer();
        // 不断接收数据
        while (!isExit) {
            if (isMuxerStart()) {
                if (muxerDatas.isEmpty()) {
                    synchronized (lock) {
                        try {
                            Log.e(TAG, "等待混合数据...");
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    // VideoThread传来的数据不为空的情况下，取出队首
                    MuxerData data = muxerDatas.remove(0);
                    int track = -1;
                    if (data.trackIndex == TRACK_VIDEO) {
                        track = videoTrackIndex;
                    } else {
                        Log.e(TAG, "视频轨道数据出现错误");
                    }
                    Log.e(TAG, "写入混合数据 " + data.bufferInfo.size);
                    try {
                        // 写入到本地文件
                        mediaMuxer.writeSampleData(track, data.byteBuf, data.bufferInfo);
                    } catch (Exception e) {
                        Log.e(TAG, "写入混合数据失败!" + e.toString());
                    }
                }
            } else {
                synchronized (lock) {
                    try {
                        Log.e(TAG, "等待音视轨添加...");
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e(TAG, "addTrack 异常:" + e.toString());
                    }
                }
            }
        }
        readyStop();
        Log.e(TAG, "混合器退出...");
    }

    private void readyStop() {
        if (mediaMuxer != null) {
            try {
                mediaMuxer.stop();
            } catch (Exception e) {
                Log.e(TAG, "mediaMuxer.stop() 异常:" + e.toString());
            }
            try {
                mediaMuxer.release();
            } catch (Exception e) {
                Log.e(TAG, "mediaMuxer.release() 异常:" + e.toString());

            }
            mediaMuxer = null;
        }
    }

    private void exit() {
        if (videoThread != null) {
            videoThread.exit();
            try {
                videoThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        isExit = true;
        synchronized (lock) {
            lock.notify();
        }
    }

    /**
     * 封装需要传输的数据类型
     */
    public static class MuxerData {

        int trackIndex;
        ByteBuffer byteBuf;
        MediaCodec.BufferInfo bufferInfo;

        public MuxerData(int trackIndex, ByteBuffer byteBuf, MediaCodec.BufferInfo bufferInfo) {
            this.trackIndex = trackIndex;
            this.byteBuf = byteBuf;
            this.bufferInfo = bufferInfo;
        }
    }


}
