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
    private EditText etNumber,etName,etAge;
    private String allInfo,func;
    private String number = "",name= "",gender = "",age="",condition="",education="";
    private String floatWindow = "";

    // 定义下拉列表需要显示的文本数组
    private String[] starArray = {"是", "否"};
    private String[] conditionArray = {};
    private String[] genderArray = {"男", "女"};
    private String[] eduArray = {"小学", "初中", "高中", "大学","其他"};
    private String[] level_motion_Array = {"平衡功能差", "有一定平衡能力","平衡功能较好"};
    private String[] level_recognition_Array = {"正常","轻度","中度","重度"};
    private String[] level_audio_Array = {"正常","轻度认知障碍","痴呆"};
    public void setAllInfo(String allInfo) {
        this.allInfo = allInfo;
    }
    public String getAllInfo() {
        return allInfo;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_input);

        Intent intent = getIntent();
        func = intent.getStringExtra("choice");
        if (func.equals("motion")){
            conditionArray = level_motion_Array;
        } else if (func.equals("recognition") || func.equals("emotion")){
            conditionArray = level_recognition_Array;
        } else if (func.equals("audio")){
            conditionArray = level_audio_Array;
        }
        initView();
        etNumber=(EditText) findViewById(R.id.number);
        etName=(EditText) findViewById(R.id.name);
        etAge=(EditText) findViewById(R.id.age);
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
                age = etAge.getText().toString();
                allInfo = number + "-" + name + "-" + gender + "-" + age + "-" + education + "-" + condition;
                if (!pathCompleted(allInfo)) {
                    Toast.makeText(InfoInputActivity.this, "请完整填写信息！", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 点击按钮跳转到页面
                Intent intent = new Intent();
                intent.setClass(InfoInputActivity.this, InfoShowActivity.class);
                intent.putExtra("fileName",allInfo);
                intent.putExtra("floatWindow",floatWindow);
                intent.putExtra("choice",func);
                startActivity(intent);

            }
        });
    }

    // 用于判断文件路径是否合法
    private boolean pathCompleted(String filePath) {
        if (filePath.endsWith("-")) {
            return false;
        }
        String[] list = filePath.split("-");
        for (String s : list) {
            if (s.equals("")) {
                return false;
            }
        }
        return true;
    }

    // 初始化下拉模式的列表框
    private void initSpinnerForDropdown() {
        // 声明一个下拉列表的数组适配器
        ArrayAdapter<String> starAdapter = new ArrayAdapter<String>(this,
                R.layout.floatingwindow_select, starArray);
        ArrayAdapter<String> starAdapter2 = new ArrayAdapter<String>(this,
                R.layout.floatingwindow_select, genderArray);
        ArrayAdapter<String> starAdapter3 = new ArrayAdapter<String>(this,
                R.layout.floatingwindow_select, eduArray);
        ArrayAdapter<String> starAdapter4 = new ArrayAdapter<String>(this,
                R.layout.floatingwindow_select, conditionArray);
        // 从布局文件中获取名叫sp_dropdown的下拉框
        Spinner sp_dropdown = findViewById(R.id.spinner1);
        Spinner sp_gender = findViewById(R.id.gender);
        Spinner sp_edu = findViewById(R.id.education);
        Spinner sp_cond = findViewById(R.id.condition);
        // 设置下拉框的标题。对话框模式才显示标题，下拉模式不显示标题
        //sp_dropdown.setPrompt("请选择是否需要浮窗");
        sp_dropdown.setAdapter(starAdapter); // 设置下拉框的数组适配器
        sp_dropdown.setSelection(0); // 设置下拉框默认显示第一项
        sp_dropdown.setOnItemSelectedListener(new MySelectedListener());

        sp_gender.setAdapter(starAdapter2); // 设置下拉框的数组适配器
        sp_gender.setSelection(0); // 设置下拉框默认显示第一项
        sp_gender.setOnItemSelectedListener(new MySelectedListener2());

        sp_edu.setAdapter(starAdapter3); // 设置下拉框的数组适配器
        sp_edu.setSelection(0); // 设置下拉框默认显示第一项
        sp_edu.setOnItemSelectedListener(new MySelectedListener3());

        sp_cond.setAdapter(starAdapter4); // 设置下拉框的数组适配器
        sp_cond.setSelection(0); // 设置下拉框默认显示第一项
        sp_cond.setOnItemSelectedListener(new MySelectedListener4());
    }

    // 定义一个选择监听器，它实现了接口OnItemSelectedListener
    class MySelectedListener implements OnItemSelectedListener {
        // 选择事件的处理方法，其中arg2代表选择项的序号
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//            Toast.makeText(InfoInputActivity.this, "您选择的是" + starArray[arg2],
//                    Toast.LENGTH_SHORT).show();
            if(starArray[arg2] == "否"){
                floatWindow = "no";
            } else {
                floatWindow = "yes";
            }
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
    class MySelectedListener2 implements OnItemSelectedListener {
        // 选择事件的处理方法，其中arg2代表选择项的序号
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            gender = genderArray[arg2];
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
    class MySelectedListener3 implements OnItemSelectedListener {
        // 选择事件的处理方法，其中arg2代表选择项的序号
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            education = eduArray[arg2];
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
    class MySelectedListener4 implements OnItemSelectedListener {
        // 选择事件的处理方法，其中arg2代表选择项的序号
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            condition = conditionArray[arg2];
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
    private void initView(){
        etNumber=findViewById(R.id.number);
        etName=findViewById(R.id.name);
        etAge=findViewById(R.id.age);
        initSpinnerForDropdown(); // 初始化下拉模式的列表框
    }
}