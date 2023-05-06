package com.renhui.androidrecorder.survey;

import static com.renhui.androidrecorder.muxer.FileUtil.getExternalStorageDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.renhui.androidrecorder.R;
import com.renhui.androidrecorder.muxer.FileUploadThread;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HealthSurveyActivity extends AppCompatActivity {

    // 用于存放Health量表所有的条目
    Object[] items = new Object[]{"姓名", "年龄", "性别", "民族", "联系方式", "文化程度", "受教育时长", "籍贯",
            "居住时间", "是否独居", "近3个月的营养状况", "是否吸烟", "吸烟时长", "每天吸烟数量/支", "是否戒烟", "是否饮酒",
            "饮酒时长", "每天饮酒数量/ml", "饮酒种类", "是否戒酒", "身高/cm", "体重/kg", "是否失眠", "失眠时长", "是否需要助眠药",
            "糖尿病", "高血压", "心梗或心绞痛", "房颤", "脑梗塞", "脑出血", "帕金森", "轻度认知功能减退", "阿尔茨海默病",
            "主观听力下降", "抑郁症或焦虑症", "牙周病", "残留牙齿数", "佩戴假牙", "牙龈红肿", "牙龈出血", "喂养方式",
            "听力是否下降", "体重是否下降", "视力是否下降", "活动能力", "语言表达能力", "有无抑郁情况", "有无焦虑情况",
            "有无认知下降", "吞咽功能", "小便是否正常", "大便是否正常"};
    Object[] answers = new Object[items.length];
    String androidFileParentPath, androidFileName;
    String fullPath, tagName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healthsurvey);

        // 提交按钮和重新上传按钮
        List<Object> units = new ArrayList<>();
        EditText name = findViewById(R.id.edit_name);
        units.add(name);
        EditText age = findViewById(R.id.edit_age);
        units.add(age);
        RadioGroup gender = findViewById(R.id.group_gender);
        units.add(gender);
        EditText ethnic = findViewById(R.id.edit_ethnic);
        units.add(ethnic);
        EditText phone = findViewById(R.id.edit_phone);
        units.add(phone);
        RadioGroup education = findViewById(R.id.group_education);
        units.add(education);
        EditText educationTime = findViewById(R.id.edit_educationTime);
        units.add(educationTime);
        RadioGroup homeland = findViewById(R.id.group_homeland);
        units.add(homeland);
        RadioGroup liveTime = findViewById(R.id.group_livetime);
        units.add(liveTime);
        RadioGroup liveAlone = findViewById(R.id.group_livealone);
        units.add(liveAlone);
        RadioGroup nutrition = findViewById(R.id.group_month3);
        units.add(nutrition);
        RadioGroup smoke = findViewById(R.id.group_smoke);
        units.add(smoke);
        EditText smokeTime = findViewById(R.id.edit_smokeYear);
        units.add(smokeTime);
        EditText smokeNum = findViewById(R.id.edit_smokeNum);
        units.add(smokeNum);
        EditText smokeNow = findViewById(R.id.edit_smokeNow);
        units.add(smokeNow);
        RadioGroup drink = findViewById(R.id.group_drinking);
        units.add(drink);
        EditText drinkYear = findViewById(R.id.edit_drinkingYear);
        units.add(drinkYear);
        EditText drinkNum = findViewById(R.id.edit_drinkingNum);
        units.add(drinkNum);
        EditText drinkWhat = findViewById(R.id.edit_drinkingWhat);
        units.add(drinkWhat);
        EditText drinkNow = findViewById(R.id.edit_drinkingNow);
        units.add(drinkNow);
        EditText height = findViewById(R.id.edit_height);
        units.add(height);
        EditText weight = findViewById(R.id.edit_weight);
        units.add(weight);
        RadioGroup sleepless = findViewById(R.id.group_noSleep);
        units.add(sleepless);
        EditText sleepTime = findViewById(R.id.edit_noSleepTime);
        units.add(sleepTime);
        RadioGroup sleepMedicine = findViewById(R.id.group_noSleepMedicine);
        units.add(sleepMedicine);
        RadioGroup tangNiao = findViewById(R.id.group_tangNiao);
        units.add(tangNiao);
        RadioGroup gaoXueYa = findViewById(R.id.group_gaoXueYa);
        units.add(gaoXueYa);
        RadioGroup xinGeng = findViewById(R.id.group_xinGeng);
        units.add(xinGeng);
        RadioGroup fangChan = findViewById(R.id.group_fangChan);
        units.add(fangChan);
        RadioGroup naoGeng = findViewById(R.id.group_naoGeng);
        units.add(naoGeng);
        RadioGroup naoChuXue = findViewById(R.id.group_naoChuXie);
        units.add(naoChuXue);
        RadioGroup parkinson = findViewById(R.id.group_paJinSen);
        units.add(parkinson);
        RadioGroup recognition = findViewById(R.id.group_renZhi);
        units.add(recognition);
        RadioGroup AD = findViewById(R.id.group_AD);
        units.add(AD);
        RadioGroup listen = findViewById(R.id.group_listen);
        units.add(listen);
        RadioGroup depression = findViewById(R.id.group_depression);
        units.add(depression);
        RadioGroup yaZhou = findViewById(R.id.group_yaZhou);
        units.add(yaZhou);
        EditText toothNum = findViewById(R.id.edit_toothNum);
        units.add(toothNum);
        RadioGroup jiaYa = findViewById(R.id.group_jiaYa);
        units.add(jiaYa);
        RadioGroup yaYinRed = findViewById(R.id.group_hongZhong);
        units.add(yaYinRed);
        RadioGroup yaYinBlood = findViewById(R.id.group_yaChuXie);
        units.add(yaYinBlood);
        RadioGroup feed = findViewById(R.id.group_weiYang);
        units.add(feed);
        RadioGroup hear = findViewById(R.id.group_tingLi);
        units.add(hear);
        RadioGroup weightDynamic = findViewById(R.id.group_tiZhong);
        units.add(weightDynamic);
        RadioGroup sight = findViewById(R.id.group_shiLi);
        units.add(sight);
        RadioGroup action = findViewById(R.id.group_huoDong);
        units.add(action);
        RadioGroup expression = findViewById(R.id.group_yuYan);
        units.add(expression);
        RadioGroup depression1 = findViewById(R.id.group_yiYu);
        units.add(depression1);
        RadioGroup nervous = findViewById(R.id.group_jiaoLv);
        units.add(nervous);
        RadioGroup recognitionDynamic = findViewById(R.id.group_renZhiDown);
        units.add(recognitionDynamic);
        RadioGroup swallow = findViewById(R.id.group_tunYan);
        units.add(swallow);
        RadioGroup xiaoBian = findViewById(R.id.group_xiaoBian);
        units.add(xiaoBian);
        RadioGroup daBian = findViewById(R.id.group_daBian);
        units.add(daBian);

        Button isFinished = findViewById(R.id.isfinished);
        Button reUpload = findViewById(R.id.reUpload);

        Intent intent = getIntent();
        String filePath = intent.getStringExtra("complete_info");
        String[] filePathList = filePath.split("/");
        isFinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 遍历每个问题，拿到答案，放到answers中
                List<Integer> unfinished = new ArrayList<>();
                for (int i = 0; i < units.size(); i++) {
                    Object tmp = units.get(i);
                    if (tmp.getClass().getName().endsWith("EditText")) {
                        EditText text = (EditText) tmp;
                        answers[i] = text.getText().toString();
                    } else if (tmp.getClass().getName().endsWith("RadioGroup")) {
                        RadioGroup group = (RadioGroup) tmp;
                        if (group.getCheckedRadioButtonId() != -1) {
                            Button isSelected = findViewById(group.getCheckedRadioButtonId());
                            answers[i] = isSelected.getText().toString();
                        }
                    }
                    Log.i("HealthSurvey", answers[i] == null ? "" : answers[i].toString());
                }
                List<Object[]> res = new ArrayList<>();
                res.add(items);
                res.add(answers);
                androidFileParentPath = getExternalStorageDirectory() + "/android_records/text/";
                androidFileName = filePathList[1] + "-" + filePathList[2];

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
                tagName = filePathList[0] + File.separator + filePathList[1] + File.separator + filePathList[2] + "-" +
                        simpleDateFormat.format(System.currentTimeMillis()) +  ".csv";
                // 通过
                CSVUtil.writeToCSV(res, androidFileParentPath, androidFileName);
                fullPath = CSVUtil.fullPath;
                // 上传文件
                FileUploadThread.stopUpload();
                FileUploadThread.startUpload(HealthSurveyActivity.this, fullPath, tagName);
            }
        });

        reUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fullPath == null) {
                    Toast.makeText(HealthSurveyActivity.this, "还没有填写完问卷并提交", Toast.LENGTH_SHORT).show();
                }
                // 重新上传最近的文件
                FileUploadThread.stopUpload();
                FileUploadThread.startUpload(HealthSurveyActivity.this, fullPath, tagName);
            }
        });
    }
}
