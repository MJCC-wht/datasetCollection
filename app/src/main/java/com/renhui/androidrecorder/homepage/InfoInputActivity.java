package com.renhui.androidrecorder.homepage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.renhui.androidrecorder.R;

public class InfoInputActivity extends AppCompatActivity {
    private Button btnBack, btnSave;
    private EditText etNumber,etName,etGender,etAge,etCondition,etEducation;
    private String Info;
    private String number = "",name= "",gender = "",age="",condition="",education="";

    public void setInfo(String info) {
        Info = info;
    }

    public String getInfo() {
        return Info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_input);
        initView();
        Intent intent = getIntent();
        String choice = intent.getStringExtra("choice");
        etNumber=(EditText) findViewById(R.id.number);
        etName=(EditText) findViewById(R.id.name);
        etGender=(EditText) findViewById(R.id.gender);
        etAge=(EditText) findViewById(R.id.age);
        etCondition=(EditText) findViewById(R.id.condition);
        etEducation=(EditText) findViewById(R.id.education);
        btnBack = (Button) findViewById(R.id.btnback);
        btnSave = (Button) findViewById(R.id.btnsave);
        //返回选择界面
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击按钮跳转到注册页面
                Intent intent = new Intent();
                intent.setClass(InfoInputActivity.this, HomepageActivity.class);
                startActivity(intent);
            }
        });
        // 输入信息保存，跳转至信息确认界面
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = etNumber.getText().toString();
                name = etName.getText().toString();
                gender = etGender.getText().toString();
                age = etAge.getText().toString();
                condition = etCondition.getText().toString();
                education = etEducation.getText().toString();
                if (education != ""){
                    Info = number+"-"+name+"-"+gender+"-"+age+"-"+condition+"-"+education;}
                else
                    Info = number+"-"+name+"-"+gender+"-"+age+"-"+condition;
                String allInfo = choice+"-"+Info;
                //点击按钮跳转到注册页面
                Intent intent = new Intent();
                intent.setClass(InfoInputActivity.this, InfoShowActivity.class);
                intent.putExtra("fileName",allInfo);
                startActivity(intent);

            }
        });
    }
    private void initView(){
        etNumber=findViewById(R.id.number);
        etName=findViewById(R.id.name);
        etGender=findViewById(R.id.gender);
        etAge=findViewById(R.id.age);
        etCondition=findViewById(R.id.condition);
        etEducation=findViewById(R.id.education);
    }

}