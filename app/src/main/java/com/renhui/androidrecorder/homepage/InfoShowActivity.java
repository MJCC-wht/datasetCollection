package com.renhui.androidrecorder.homepage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.renhui.androidrecorder.R;
import com.renhui.androidrecorder.muxer.MediaMuxerActivity;

public class InfoShowActivity extends AppCompatActivity {
    private Button btnOK, btnBack;
    private TextView complete_info;
    private String file_name,choice; // 合成文件名
    private String camera_window;

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_name() {
        return file_name;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_show);

        Intent intent = getIntent();
        file_name = intent.getStringExtra("fileName");
        camera_window = intent.getStringExtra("floatWindow");
        complete_info=(TextView) findViewById(R.id.textView);
        complete_info.setText(this.file_name);

        btnOK = (Button) findViewById(R.id.btnOK);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 点击按钮跳转到录制界面
                Intent intent = new Intent();
                intent.setClass(InfoShowActivity.this, HomepageActivity.class);
                // file name transit
                intent.putExtra("complete_info",file_name);
                intent.putExtra("cameraWindow",camera_window);
                startActivity(intent);
            }
        });
        btnBack = (Button) findViewById(R.id.btnBack);
        //return back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击按钮跳转到页面
                Intent intent = new Intent();
                intent.setClass(InfoShowActivity.this, InfoInputActivity.class);
                startActivity(intent);
            }
        });
    }
}