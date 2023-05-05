package com.renhui.androidrecorder.homepage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.renhui.androidrecorder.R;
import com.renhui.androidrecorder.muxer.MediaMuxerActivity;
import com.renhui.androidrecorder.survey.GDSSurveyActivity;

public class HomepageActivity extends AppCompatActivity {
    private Button btn11,btn12,btn13,
            btn21,btn22,
            btn31,btn32;
    private String choice, choice2; // 选择的是哪个项目？

    private String filePath, camera_window = "";

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
                // TODO:为了测试临时修改的，记得改回去，改为 "video/action1/"
                setChoice("video/action1/") ;
                choice2 = "motion";
                judge1();
            }
        });
        btn12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action2/") ;
                choice2 = "motion";
                judge1();
            }
        });
        btn13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action3/") ;
                choice2 = "motion";
                judge1();
            }
        });

        btn21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition1/") ;
                choice2 = "recognition";
                judge2();
            }
        });
        btn22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition2/") ;
                choice2 = "recognition";
                judge2();
            }
        });
        btn31.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("audio/description1/") ;
                choice2 = "audio";
                judge3();
            }
        });
        btn32.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("audio/description2/") ;
                choice2 = "audio";
                judge3();
            }
        });
    }
    private void jump_to_collect(){
        Intent intent = new Intent();
        if (filePath == null || filePath.equals(" ")) {
            Toast.makeText(HomepageActivity.this, "请点击左图，输入受试者信息",
                    Toast.LENGTH_SHORT).show();
        } else {
            intent.setClass(HomepageActivity.this, MediaMuxerActivity.class);
            intent.putExtra("complete_info",choice + filePath);
            intent.putExtra("cameraWindow",camera_window);
            startActivity(intent);
        }
    }
    private void judge1(){
        if (choice2 == null || choice2.charAt(0) != 'm') {
            Toast.makeText(HomepageActivity.this, "请正确选择 采集动作",
                    Toast.LENGTH_SHORT).show();
        } else if (choice2.equals("motion")){
            jump_to_collect();
        }
    }
    private void judge2(){
        if (choice2 == null || choice2.charAt(0) != 'r') {
            Toast.makeText(HomepageActivity.this, "请正确选择 认知/情绪视频",
                    Toast.LENGTH_SHORT).show();
        } else if (choice2.equals("recognition")) {
            jump_to_collect();
        }
    }
    private void judge3(){
        if (choice2 == null || choice2.charAt(0) != 'a') {
            Toast.makeText(HomepageActivity.this, "请正确选择 语音任务",
                    Toast.LENGTH_SHORT).show();
        } else if (choice2.equals("audio")){
            jump_to_collect();
        }
    }
    private void initView(){
        btn11=findViewById(R.id.btn11);
        btn12=findViewById(R.id.btn12);
        btn13=findViewById(R.id.btn13);

        btn21=findViewById(R.id.btn21);
        btn22=findViewById(R.id.btn22);

        btn31=findViewById(R.id.btn31);
        btn32=findViewById(R.id.btn32);
    }
}
