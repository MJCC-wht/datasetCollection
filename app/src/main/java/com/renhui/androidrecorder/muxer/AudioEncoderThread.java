package com.renhui.androidrecorder.muxer;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 音频编码线程
 * Created by renhui on 2017/9/25.
 */
public class AudioEncoderThread extends Thread {

    public static final String TAG = "AudioEncoderThread";

    private static volatile AudioEncoderThread audioThread;

    public static final int SAMPLES_PER_FRAME = 1024;
    public static final int FRAMES_PER_BUFFER = 25;
    private static final int SAMPLE_RATE = 44100;
    private static final int[] AUDIO_SOURCES = new int[]{MediaRecorder.AudioSource.MIC};
    private volatile boolean isExit = false;
    private AudioRecord audioRecord;
    private FileOutputStream fos;

    // 用于上传的文件路径和名字
    public static String filePath;
    public static String tagName;

    public AudioEncoderThread() {
        // 构造函数
    }

    public static void startAudio(String filePath) {
        // 判断是否错误
        if (filePath.startsWith("video")) {
            Log.e("AudioEncoderThread", "record the audio in the video action!");
            return;
        }
        if (audioThread == null) {
            synchronized (AudioEncoderThread.class) {
                if (audioThread == null) {
                    audioThread = new AudioEncoderThread();

                    // 初始化AudioRecord和file
                    audioThread.prepareAudioRecord();
                    FileUtil fileSwapHelper = new FileUtil(filePath);
                    fileSwapHelper.getSaveFilePath();
                    AudioEncoderThread.filePath = fileSwapHelper.getFullPath();
                    tagName = fileSwapHelper.getFilePath();

                    try {
                        audioThread.fos = new FileOutputStream(AudioEncoderThread.filePath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    audioThread.isExit = false;

                    audioThread.start();
                }
            }
        }
    }

    public static void stopAudio() {
        if (audioThread != null) {
            audioThread.stopAudioRecord();
            try {
                audioThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            audioThread = null;
        }
    }


    @SuppressLint("MissingPermission")
    private void prepareAudioRecord() {
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        try {
            final int min_buffer_size = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            int buffer_size = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER;
            if (buffer_size < min_buffer_size)
                buffer_size = ((min_buffer_size / SAMPLES_PER_FRAME) + 1) * SAMPLES_PER_FRAME * 2;

            audioRecord = null;
            for (final int source : AUDIO_SOURCES) {
                try {
                    audioRecord = new AudioRecord(source, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer_size);
                    if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
                        audioRecord = null;
                } catch (Exception e) {
                    audioRecord = null;
                }
                if (audioRecord != null) break;
            }
        } catch (final Exception e) {
            Log.e(TAG, "AudioThread#run", e);
        }

        if (audioRecord != null) {
            audioRecord.startRecording();
        }
    }

    private void stopAudioRecord() {
        isExit = true;
        audioRecord.stop();
        audioRecord.release();
        try {
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        encode();
    }

    private void encode() {
        byte[] buffer = new byte[1024];
        try {
            // 写入wav文件头
            writeWavHeader(fos, audioRecord.getChannelCount(), audioRecord.getSampleRate(), AudioFormat.ENCODING_PCM_16BIT);
            while (!isExit) {
                int readSize = audioRecord.read(buffer, 0, buffer.length);
                if (readSize > 0) {
                    fos.write(buffer, 0, readSize);
                }
                // 通过chronometer获取时间，一分钟准时结束
                long elapsedTime = SystemClock.elapsedRealtime() - MediaMuxerActivity.mainActivity.chronometer.getBase();
                if (tagName.startsWith("audio/description2") && elapsedTime > 60 * 1000) {
                    isExit = true;
                    MediaMuxerActivity.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MediaMuxerActivity.mainActivity.audioStartStopButton.performClick();
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeWavHeader(FileOutputStream out, int channelCount, int sampleRate, int audioFormat) throws IOException {
        // 音频数据大小
        long totalAudioLen = 0;
        // 音频头部大小
        long totalDataLen = totalAudioLen + 36;
        // 采样率
        long longSampleRate = sampleRate;
        // 声道数
        int channels = channelCount;
        // 一个样本的大小
        int byteRate = audioFormat * channels * (int)longSampleRate;
        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1 for PCM
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * 2);  // block align
        header[33] = 0;
        header[34] = 16;  // bits per sample
        header[35] = 0;
        header[36] = 'd';  // 'data' chunk
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }
}
