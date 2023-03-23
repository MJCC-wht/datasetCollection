package com.renhui.androidrecorder.homepage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.renhui.androidrecorder.R;

public class InfoInputActivity extends AppCompatActivity {
    private Button btnBack, btnSave;
    private EditText etNumber,etName,etGender,etAge,etCondition,etEducation;
    private String Info;
    private String number = "",name= "",gender = "",age="",condition="",education="";
    private String floatWindow = "";
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
        String func = intent.getStringExtra("choice");
        etNumber=(EditText) findViewById(R.id.number);
        etName=(EditText) findViewById(R.id.name);
        etGender=(EditText) findViewById(R.id.gender);
        etAge=(EditText) findViewById(R.id.age);
        etCondition=(EditText) findViewById(R.id.condition);
        etEducation=(EditText) findViewById(R.id.education);
        btnBack = (Button) findViewById(R.id.btnback);
        btnSave = (Button) findViewById(R.id.btnsave);

        //return back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击按钮跳转到页面
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
                if (func.charAt(0)== 'a'){
                    Info = number+"-"+name+"-"+gender+"-"+age+"-"+condition+"-"+education;}
                else
                    Info = number+"-"+name+"-"+gender+"-"+age+"-"+condition;
                String allInfo = func+Info;
                //点击按钮跳转到页面
                Intent intent = new Intent();
                intent.setClass(InfoInputActivity.this, InfoShowActivity.class);
                intent.putExtra("fileName",allInfo);
                intent.putExtra("floatWindow",floatWindow);
                startActivity(intent);

            }
        });
    }
    // 初始化下拉模式的列表框
    private void initSpinnerForDropdown() {
        // 声明一个下拉列表的数组适配器
        ArrayAdapter<String> starAdapter = new ArrayAdapter<String>(this,
                R.layout.floatingwindow_select, starArray);
        // 从布局文件中获取名叫sp_dropdown的下拉框
        Spinner sp_dropdown = findViewById(R.id.spinner1);
        // 设置下拉框的标题。对话框模式才显示标题，下拉模式不显示标题
        sp_dropdown.setPrompt("请选择是否需要浮窗");
        sp_dropdown.setAdapter(starAdapter); // 设置下拉框的数组适配器
        sp_dropdown.setSelection(0); // 设置下拉框默认显示第一项
        // 给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的onItemSelected方法
        sp_dropdown.setOnItemSelectedListener(new MySelectedListener());
    }

    // 定义下拉列表需要显示的文本数组
    private String[] starArray = {"是", "否"};
    // 定义一个选择监听器，它实现了接口OnItemSelectedListener
    class MySelectedListener implements OnItemSelectedListener {
        // 选择事件的处理方法，其中arg2代表选择项的序号
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//            Toast.makeText(InfoInputActivity.this, "您选择的是" + starArray[arg2],
//                    Toast.LENGTH_LONG).show();
            if(starArray[arg2] == "否"){
                floatWindow = "no";
            } else {
                floatWindow = "yes";
            }
        }
        // 未选择时的处理方法，通常无需关注
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
    private void initView(){
        etNumber=findViewById(R.id.number);
        etName=findViewById(R.id.name);
        etGender=findViewById(R.id.gender);
        etAge=findViewById(R.id.age);
        etCondition=findViewById(R.id.condition);
        etEducation=findViewById(R.id.education);
        initSpinnerForDropdown(); // 初始化下拉模式的列表框
    }
}