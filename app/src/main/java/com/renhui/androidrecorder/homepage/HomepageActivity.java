package com.renhui.androidrecorder.homepage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.renhui.androidrecorder.R;

public class HomepageActivity extends AppCompatActivity {
    private Button btn11,btn12,btn13,btn14,
            btn21,btn22,btn23,btn24,btn25,
            btn31,btn32,btn33,btn34,btn35,
            btn41,btn42,btn43;
    private String choice; // 选择的是哪个项目？

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

        btn11 = (Button) findViewById(R.id.btn11);
        btn11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action1/") ;
                jump_to();
            }
        });
        btn12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action2/") ;
                jump_to();
            }
        });
        btn13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action3/") ;
                jump_to();
            }
        });
        btn14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action4/") ;
                jump_to();
            }
        });
        btn21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition1/") ;
                jump_to();
            }
        });
        btn22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition2/") ;
                jump_to();
            }
        });
        btn23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition3/") ;
                jump_to();
            }
        });
        btn24.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition4/") ;
                jump_to();
            }
        });
        btn25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition5/") ;
                jump_to();
            }
        });
        btn31.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/emotion1/") ;
                jump_to();
            }
        });
        btn32.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/emotion2/") ;
                jump_to();
            }
        });
        btn33.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/emotion3/") ;
                jump_to();
            }
        });
        btn34.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/emotion4/") ;
                jump_to();
            }
        });
        btn35.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/emotion5/") ;
                jump_to();
            }
        });
        btn41.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("audio/description1/") ;
                jump_to();
            }
        });
        btn42.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("audio/description2/") ;
                jump_to();
            }
        });
        btn43.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("audio/description3/") ;
                jump_to();
            }
        });
    }
    //点击按钮跳转到信息输入页面
    private void jump_to(){
        Intent intent = new Intent();
        intent.setClass(HomepageActivity.this, InfoInputActivity.class);
        intent.putExtra("choice",choice);
        startActivity(intent);
    }
    private void initView(){
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
