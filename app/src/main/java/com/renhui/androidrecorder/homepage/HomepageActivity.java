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

public class HomepageActivity extends AppCompatActivity {
    private ImageButton imgbtn1,imgbtn2,imgbtn3,imgbtn4;
    private Button btn11,btn12,btn13,btn14,
            btn21,btn22,btn23,btn24,btn25,
            btn31,btn32,btn33,btn34,btn35,
            btn41,btn42,btn43;
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

        if (intent != null) {
            filePath = intent.getStringExtra("complete_info");
            camera_window = intent.getStringExtra("cameraWindow");
            choice2 = intent.getStringExtra("choice2");

        }

        imgbtn1 = (ImageButton) findViewById(R.id.yundong);
        imgbtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choice = "motion";
                jump_to_input();
            }
        });
        imgbtn2 = (ImageButton) findViewById(R.id.renzhi);
        imgbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choice = "recognition";
                jump_to_input();
            }
        });
        imgbtn3 = (ImageButton) findViewById(R.id.biaoqing);
        imgbtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choice = "emotion";
                jump_to_input();
            }
        });
        imgbtn4 = (ImageButton) findViewById(R.id.yuyin);
        imgbtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choice = "audio";
                jump_to_input();
            }
        });

        btn11 = (Button) findViewById(R.id.btn11);
        btn11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action1/") ;
                judge1();
            }
        });
        btn12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action2/") ;
                judge1();
            }
        });
        btn13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action3/") ;
                judge1();
            }
        });
        btn14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action4/") ;
                judge1();
            }
        });
        btn21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition1/") ;
                judge2();
            }
        });
        btn22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition2/") ;
                judge2();
            }
        });
        btn23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition3/") ;
                judge2();
            }
        });
        btn24.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition4/") ;
                judge2();
            }
        });
        btn25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition5/") ;
                judge2();
            }
        });
        btn31.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/emotion1/") ;
                judge3();
            }
        });
        btn32.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/emotion2/") ;
                judge3();
            }
        });
        btn33.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/emotion3/") ;
                judge3();
            }
        });
        btn34.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/emotion4/") ;
                judge3();
            }
        });
        btn35.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/emotion5/") ;
                judge3();
            }
        });
        btn41.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("audio/description1/") ;
                judge4();
            }
        });
        btn42.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("audio/description2/") ;
                judge4();
            }
        });
        btn43.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("audio/description3/") ;
                judge4();
            }
        });
    }
    //点击按钮跳转到信息输入页面
    private void jump_to_input(){
        filePath = " ";
        Intent intent = new Intent();
        intent.setClass(HomepageActivity.this, InfoInputActivity.class);
        intent.putExtra("choice",choice);
        startActivity(intent);
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
            Toast.makeText(HomepageActivity.this, "请正确选择 认知视频",
                    Toast.LENGTH_SHORT).show();
        } else if (choice2.equals("recognition")) {
            jump_to_collect();
        }
    }
    private void judge3(){
        if (choice2 == null || choice2.charAt(0) != 'e') {
            Toast.makeText(HomepageActivity.this, "请正确选择 情绪视频",
                    Toast.LENGTH_SHORT).show();
        } else if (choice2.equals("emotion")){
            jump_to_collect();
        }
    }
    private void judge4(){
        if (choice2 == null || choice2.charAt(0) != 'd') {
            Toast.makeText(HomepageActivity.this, "请正确选择 语音任务",
                    Toast.LENGTH_SHORT).show();
        } else if (choice2.equals("audio")){
            jump_to_collect();
        }
    }

    private void initView(){
        imgbtn1 = findViewById(R.id.yundong);
        imgbtn2 = findViewById(R.id.renzhi);
        imgbtn3 = findViewById(R.id.biaoqing);
        imgbtn4 = findViewById(R.id.yuyin);

        btn11=findViewById(R.id.btn11);
        btn12=findViewById(R.id.btn12);
        btn13=findViewById(R.id.btn13);
        btn14=findViewById(R.id.btn14);

        btn21=findViewById(R.id.btn21);
        btn22=findViewById(R.id.btn22);
        btn23=findViewById(R.id.btn23);
        btn24=findViewById(R.id.btn24);
        btn25=findViewById(R.id.btn25);

        btn31=findViewById(R.id.btn31);
        btn32=findViewById(R.id.btn32);
        btn33=findViewById(R.id.btn33);
        btn34=findViewById(R.id.btn34);
        btn35=findViewById(R.id.btn35);

        btn41=findViewById(R.id.btn41);
        btn42=findViewById(R.id.btn42);
        btn43=findViewById(R.id.btn43);
    }
}
