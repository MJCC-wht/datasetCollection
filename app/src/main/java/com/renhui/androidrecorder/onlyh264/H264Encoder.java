package com.renhui.androidrecorder.onlyh264;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * H264 编码类
 */
public class H264Encoder {

    private final static int TIMEOUT_USEC = 12000;

    private MediaCodec mediaCodec;
    private MediaMuxer mediaMuxer;

    public boolean isRuning = false;
    private int width, height, framerate;
    private static final int COMPRESS_RATIO = 256;
    public static final int IMAGE_HEIGHT = 1080;
    public static final int IMAGE_WIDTH = 1920;
    private static final int FRAME_RATE = 60; // 帧率
    private static final int BIT_RATE = IMAGE_HEIGHT * IMAGE_WIDTH * 3; // bit rate CameraWrapper.
    public byte[] configbyte;

    private BufferedOutputStream outputStream;

    public ArrayBlockingQueue<byte[]> yuv420Queue = new ArrayBlockingQueue<>(10);

    /***
     * 构造函数
     * @param width
     * @param height
     * @param framerate
     */
    public H264Encoder(int width, int height, int framerate) {
        this.width = width;
        this.height = height;
        this.framerate = framerate;

        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
            // 创建混合器生成mp4
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp4";
            mediaMuxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void putData(byte[] buffer) {
        if (yuv420Queue.size() >= 10) {
            yuv420Queue.poll();
        }
        yuv420Queue.add(buffer);
    }

    /***
     * 开始编码
     */
    public void startEncoder() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                isRuning = true;
                byte[] input = null;
                long pts = 0;
                long generateIndex = 0;
                int mTrackIndex = 0;

                while (isRuning) {
                    // 从相机预览取出一帧待处理的数据，需要从NV21->NV12
                    if (yuv420Queue.size() > 0) {
                        input = yuv420Queue.poll();
                        byte[] yuv420sp = new byte[width * height * 3 / 2];
                        // 必须要转格式，否则录制的内容播放出来为绿屏
                        NV21toI420SemiPlanar(input, yuv420sp, width, height);
//                        NV21ToNV12(input, yuv420sp, width, height);
                        input = yuv420sp;
                    }
                    if (input != null) {
                        try {
                            // 拿到有空闲的输入缓存区的下标
                            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                            if (inputBufferIndex >= 0) {
                                pts = computePresentationTime(generateIndex);
                                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
                                inputBuffer.put(input);
                                generateIndex++;
                                // 将数据放到编码队列
                                mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
                            }
                            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                            // 得到成功编码后输出的outputBufferId
                            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                            if (outputBufferIndex >= 0) {
                                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
                                // mediaCodec的直接编码输出是h264
                                byte[] outData = new byte[bufferInfo.size];
                                outputBuffer.position(bufferInfo.offset);
                                outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                                // 直接用MediaMuxer写入
                                mediaMuxer.writeSampleData(mTrackIndex, outputBuffer, bufferInfo);
                                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                MediaFormat mediaFormat = mediaCodec.getOutputFormat();
                                mTrackIndex = mediaMuxer.addTrack(mediaFormat);
                                mediaMuxer.start();
                            }

                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // 停止编解码器并释放资源
                try {
                    mediaCodec.stop();
                    mediaCodec.release();
                    mediaCodec = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 关闭Muxer
                try {
                    mediaMuxer.stop();
                    mediaMuxer.release();
                    mediaMuxer = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 停止编码数据
     */
    public void stopEncoder() {
        isRuning = false;
    }

    private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
    }

    private static void NV21toI420SemiPlanar(byte[] nv21bytes, byte[] i420bytes, int width, int height) {
        System.arraycopy(nv21bytes, 0, i420bytes, 0, width * height);
        for (int i = width * height; i < nv21bytes.length; i += 2) {
            i420bytes[i] = nv21bytes[i + 1];
            i420bytes[i + 1] = nv21bytes[i];
        }
    }

    /**
     * 根据帧数生成时间戳
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / framerate;
    }
}
