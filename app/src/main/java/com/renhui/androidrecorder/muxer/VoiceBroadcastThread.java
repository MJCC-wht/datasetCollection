package com.renhui.androidrecorder.muxer;

import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import java.sql.Connection;
import java.util.Locale;

public class VoiceBroadcastThread extends Thread{

    private static VoiceBroadcastThread voiceBroadcastThread;

    //创建自带语音对象
    private TextToSpeech textToSpeech = null;
    private Context mContext;
    // 记录需要播报的内容
    private String text;

    public VoiceBroadcastThread(Context mContext, String text) {
        // 初始化相关对象和参数
        this.mContext = mContext;
        this.text = text;
    }

    public static void startBroadcast(Context mContext, String text) {
        if (voiceBroadcastThread == null) {
            synchronized (VoiceBroadcastThread.class) {
                if (voiceBroadcastThread == null) {
                    voiceBroadcastThread = new VoiceBroadcastThread(mContext, text);
                    voiceBroadcastThread.initTTS(mContext);
                    voiceBroadcastThread.start();
                }
            }
        }
    }

    public static void stopBroadcast() {
        // 如果退出页面，不管是否正在朗读TTS都被打断
        if (voiceBroadcastThread != null && voiceBroadcastThread.textToSpeech != null) {
            voiceBroadcastThread.textToSpeech.stop();
        }
        voiceBroadcastThread = null;
    }

    private void initTTS(Context mContext) {
        //实例化自带语音对象
        textToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == textToSpeech.SUCCESS) {

                    //判断是否支持下面两种语言
                    int result1 = textToSpeech.setLanguage(Locale.US);
                    int result2 = textToSpeech.setLanguage(Locale.SIMPLIFIED_CHINESE);

                    boolean a = !(result1 == TextToSpeech.LANG_MISSING_DATA || result1 == TextToSpeech.LANG_NOT_SUPPORTED);
                    boolean b = !(result2 == TextToSpeech.LANG_MISSING_DATA || result2 == TextToSpeech.LANG_NOT_SUPPORTED);

                    Log.i("zhh_tts", "US支持否？--》" + a +
                            "\nzh-CN支持否》--》" + b);

                    startAuto(text);

                } else {
                    Log.i("broadcasterror", "语音播报不支持");
                }
            }
        });

        // 设置OnUtteranceProgressListener监听器
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                // TTS引擎开始读取文本
            }

            public void onDone(String utteranceId) {
                // TTS引擎已经读取完文本
                if (utteranceId.equals("utteranceId1")) {
                    Log.d("broatcastdone", "TextToSpeech has finished speaking");
                    // 关闭，释放资源
                    voiceBroadcastThread.textToSpeech.shutdown();
                }
            }

            @Override
            public void onError(String utteranceId) {
                // TTS引擎发生错误
            }
        });
    }

    private void startAuto(String data) {
        // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
        textToSpeech.setPitch(0.5f);
        // 设置语速
        textToSpeech.setSpeechRate(1.0f);
        textToSpeech.speak(data,//输入中文，若不支持的设备则不会读出来
                TextToSpeech.QUEUE_FLUSH, null);

    }

    @Override
    public void run() {
        initTTS(mContext);
    }
}
