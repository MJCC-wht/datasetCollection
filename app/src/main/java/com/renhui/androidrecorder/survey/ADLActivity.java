package com.renhui.androidrecorder.survey;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.renhui.androidrecorder.MyApplication;
import com.renhui.androidrecorder.R;
import com.renhui.androidrecorder.muxer.FileUploadThread;
import com.renhui.androidrecorder.muxer.FileUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ADLActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adl);

        Intent intent = getIntent();
        String filePath = intent.getStringExtra("complete_info");

        List<RadioGroup> groupList = new ArrayList<>();
        groupList.add(findViewById(R.id.button1));
        groupList.add(findViewById(R.id.button2));
        groupList.add(findViewById(R.id.button3));
        groupList.add(findViewById(R.id.button4));
        groupList.add(findViewById(R.id.button5));
        groupList.add(findViewById(R.id.button6));
        groupList.add(findViewById(R.id.button7));
        groupList.add(findViewById(R.id.button8));
        groupList.add(findViewById(R.id.button9));
        groupList.add(findViewById(R.id.button10));
        groupList.add(findViewById(R.id.button11));
        groupList.add(findViewById(R.id.button12));
        groupList.add(findViewById(R.id.button13));
        groupList.add(findViewById(R.id.button14));

        Button isFinished = findViewById(R.id.isfinished);
        Button reUpload = findViewById(R.id.reUpload);
        FileUtil fileSwapHelper = new FileUtil(filePath);

        isFinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                List<Integer> unfinished = new ArrayList<>();
                List<Integer> scoreList = new ArrayList<>();
                int sumScore = 0;
                for (int i = 0; i < groupList.size(); i++) {
                    // 生成两个groupButton
                    String firstName = "button" + (i + 1) + '_' + 0;
                    String secondName = "button" + (i + 1) + '_' + 1;
                    String thirdName = "button" + (i + 1) + '_' + 2;
                    String firthName = "button" + (i + 1) + '_' + 3;
                    int firstId = getResources().getIdentifier(firstName, "id", getPackageName());
                    int secondId = getResources().getIdentifier(secondName, "id", getPackageName());
                    int thirdId = getResources().getIdentifier(thirdName, "id", getPackageName());
                    int firthId = getResources().getIdentifier(firthName, "id", getPackageName());
                    // 判断是否已作答与对应分值
                    if (groupList.get(i).getCheckedRadioButtonId() == -1) {
                        unfinished.add(i);
                    } else if (groupList.get(i).getCheckedRadioButtonId() == firstId) {
                        sumScore += 1;
                        scoreList.add(1);
                    } else if (groupList.get(i).getCheckedRadioButtonId() == secondId) {
                        sumScore += 2;
                        scoreList.add(2);
                    } else if (groupList.get(i).getCheckedRadioButtonId() == thirdId) {
                        sumScore += 3;
                        scoreList.add(3);
                    } else if (groupList.get(i).getCheckedRadioButtonId() == firthId) {
                        sumScore += 4;
                        scoreList.add(4);
                    } else {
                        Log.i("tag", String.valueOf(i) + "  ButtonId error");
                    }
                }

                if (unfinished.size() != 0) {
                    StringBuilder putMessage = new StringBuilder();
                    putMessage.append("第");
                    for (Integer integer : unfinished) {
                        putMessage.append(integer+1);
                        putMessage.append("、");
                    }
                    putMessage.deleteCharAt(putMessage.length() - 1);
                    putMessage.append("题未作答！");

                    AlertDialog failDialog = new AlertDialog.Builder(ADLActivity.this)
                            .setTitle("问卷未全部作答！")
                            .setMessage(putMessage)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).create();
                    failDialog.show();
                }
                else {
                    Log.i("tag", String.valueOf(sumScore));
                    StringBuilder scoreMessage = new StringBuilder("得分情况为：");
                    for (Integer score : scoreList) {
                        scoreMessage.append(score);
                        scoreMessage.append(",");
                    }
                    scoreMessage.deleteCharAt(scoreMessage.length() - 1);
                    String message = "您在ADL问卷中的得分是：" + sumScore + "分";
                    AlertDialog scoreDialog = new AlertDialog.Builder(ADLActivity.this)
                            .setTitle("问卷已完成！")
                            .setMessage(message)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 点击确定后保存到txt文件中并上传
                                    fileSwapHelper.getSaveFilePath();
                                    File file = new File(fileSwapHelper.getFullPath());
                                    try {
                                        FileOutputStream fos = new FileOutputStream(file);
                                        OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                                        BufferedWriter bw = new BufferedWriter(osw);
                                        bw.write(scoreMessage.toString());
                                        bw.write("\n");
                                        bw.write(message);
                                        bw.close();
                                        osw.close();
                                        fos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    // 上传文件
                                    FileUploadThread.stopUpload();
                                    FileUploadThread.startUpload(ADLActivity.this, fileSwapHelper.getFullPath(), fileSwapHelper.getFilePath());
                                }
                            }).create();
                    scoreDialog.show();
                }
            }
        });

        reUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileSwapHelper.getFullPath() == null) {
                    Toast.makeText(ADLActivity.this, "还没有填写完问卷并提交", Toast.LENGTH_SHORT).show();
                }
                // 重新上传最近的文件
                FileUploadThread.stopUpload();
                FileUploadThread.startUpload(ADLActivity.this, fileSwapHelper.getFullPath(), fileSwapHelper.getFilePath());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApplication.getInstance().setCurrentActivity(null);
    }
}
