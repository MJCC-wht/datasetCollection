package com.renhui.androidrecorder.homepage;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.renhui.androidrecorder.R;
import com.renhui.androidrecorder.muxer.MediaMuxerActivity;
import com.renhui.androidrecorder.survey.GDSSurveyActivity;
import com.renhui.androidrecorder.survey.HealthSurveyActivity;

public class HomepageActivity extends AppCompatActivity {
    private Button btn11,btn12,btn13,
            btn21,btn22,
            btn31,btn32,
            btn41,btn42;

    private static final String KEY_INDEX="INDEX";
    private String choice, choice2; // 选择的是哪个项目？
    private int[] flag = new int[]{0,0,0,0,0,0,0,0,0};//初始状态
    private String filePath, camera_window = "";

    public void setFlag(int[] flag) {
        this.flag = flag;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }

    public String getChoice() {
        return this.choice;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        initView();

        Intent intent = getIntent();
        filePath = intent.getStringExtra("complete_info");
        camera_window = intent.getStringExtra("cameraWindow");

        btn11 = (Button) findViewById(R.id.btn11);
        btn11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action1/") ;
                choice2 = "motion";
                judge1(flag, btn11,0);
            }
        });
        btn12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action2/") ;
                choice2 = "motion";
                judge1(flag, btn12,1);
            }
        });
        btn13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action3/") ;
                choice2 = "motion";
                judge1(flag, btn13, 2);
            }
        });

        btn21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition1/") ;
                choice2 = "recognition";
                judge2(flag, btn21,3);
            }
        });
        btn22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition2/") ;
                choice2 = "recognition";
                judge2(flag, btn22,4);
            }
        });
        btn31.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("audio/description1/") ;
                choice2 = "audio";
                judge3(flag, btn31,5);
            }
        });
        btn32.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("audio/description2/") ;
                choice2 = "audio";
                judge3(flag, btn32,6);
            }
        });
        btn41 = (Button) findViewById(R.id.btn41);
        btn41.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setClass(HomepageActivity.this, GDSSurveyActivity.class);
                setChoice("text/gds/");
                intent.putExtra("complete_info",choice + filePath);
                startActivity(intent);
                btnChange(flag, btn41, 7);
            }
        });
        btn42 = (Button) findViewById(R.id.btn42);
        btn42.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setClass(HomepageActivity.this, HealthSurveyActivity.class);
                setChoice("text/health/");
                intent.putExtra("complete_info",choice + filePath);
                startActivity(intent);
                btnChange(flag, btn42, 8);//
            }
        });
    }
    private void jump_to_collect(int[] flag, Button btn, int i){
        Intent intent = new Intent();
        if (filePath == null || filePath.equals(" ")) {
            Toast.makeText(HomepageActivity.this, "请点击左图，输入受试者信息",
                    Toast.LENGTH_SHORT).show();
        } else {
            btnChange(flag, btn, i);
            intent.setClass(HomepageActivity.this, MediaMuxerActivity.class);
            intent.putExtra("complete_info",choice + filePath);
            intent.putExtra("cameraWindow",camera_window);
            startActivity(intent);
        }
    }
    private void judge1(int[] flag, Button btn, int i){
        if (choice2 == null || choice2.charAt(0) != 'm') {
            Toast.makeText(HomepageActivity.this, "请正确选择 采集动作",
                    Toast.LENGTH_SHORT).show();
        } else if (choice2.equals("motion")){
            jump_to_collect(flag, btn, i);
        }
    }
    private void judge2(int[] flag, Button btn, int i){
        if (choice2 == null || choice2.charAt(0) != 'r') {
            Toast.makeText(HomepageActivity.this, "请正确选择 认知/情绪视频",
                    Toast.LENGTH_SHORT).show();
        } else if (choice2.equals("recognition")) {
            jump_to_collect(flag, btn, i);
        }
    }
    private void judge3(int[] flag, Button btn, int i){
        if (choice2 == null || choice2.charAt(0) != 'a') {
            Toast.makeText(HomepageActivity.this, "请正确选择 语音任务",
                    Toast.LENGTH_SHORT).show();
        } else if (choice2.equals("audio")){
            jump_to_collect(flag, btn, i);
        }
    }
    private void btnChange(int[] flag, Button btn, int i){
        if(flag[i] == 0)
        {
            btn.setBackgroundColor(Color.parseColor("#C5C5C5"));
            flag[i] = 1;
        }//选中
//        else {
//            btn.setBackgroundColor(Color.parseColor("#E4EEE5"));
//            flag[i] = 0;
//        }//取消
    }
    private void initView(){

        btn11=findViewById(R.id.btn11);
        btn12=findViewById(R.id.btn12);
        btn13=findViewById(R.id.btn13);

        btn21=findViewById(R.id.btn21);
        btn22=findViewById(R.id.btn22);

        btn31=findViewById(R.id.btn31);
        btn32=findViewById(R.id.btn32);

        btn41=findViewById(R.id.btn41);
        btn42=findViewById(R.id.btn42);

    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if (this.getResources().getConfiguration().orientation==  Configuration.ORIENTATION_LANDSCAPE) {
//        } else if (this.getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT) {
//        }
//        if (newConfig.hardKeyboardHidden== Configuration.HARDKEYBOARDHIDDEN_NO) {
//        }else if (newConfig.hardKeyboardHidden== Configuration.HARDKEYBOARDHIDDEN_YES) {
//        }
//    }

//    @Override
//    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        outState.putInt("count",count);
//        super.onSaveInstanceState(outState);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        count = savedInstanceState.getInt("count");
//    }
}
