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
    private String choice; // 选择的是哪个项目？

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
                jump_to_collect();
            }
        });
        btn12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action2/") ;
                jump_to_collect();
            }
        });
        btn13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action3/") ;
                jump_to_collect();
            }
        });
        btn14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action4/") ;
                jump_to_collect();
            }
        });
        btn21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition1/") ;
                jump_to_collect();
            }
        });
        btn22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition2/") ;
                jump_to_collect();
            }
        });
        btn23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition3/") ;
                jump_to_collect();
            }
        });
        btn24.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition4/") ;
                jump_to_collect();
            }
        });
        btn25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition5/") ;
                jump_to_collect();
            }
        });
        btn31.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/emotion1/") ;
                jump_to_collect();
            }
        });
        btn32.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/emotion2/") ;
                jump_to_collect();
            }
        });
        btn33.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/emotion3/") ;
                jump_to_collect();
            }
        });
        btn34.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/emotion4/") ;
                jump_to_collect();
            }
        });
        btn35.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/emotion5/") ;
                jump_to_collect();
            }
        });
        btn41.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("audio/description1/") ;
                jump_to_collect();
            }
        });
        btn42.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("audio/description2/") ;
                jump_to_collect();
            }
        });
        btn43.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("audio/description3/") ;
                jump_to_collect();
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
