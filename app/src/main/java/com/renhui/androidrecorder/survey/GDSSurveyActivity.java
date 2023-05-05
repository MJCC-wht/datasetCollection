package com.renhui.androidrecorder.survey;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.renhui.androidrecorder.R;
import com.renhui.androidrecorder.homepage.HomepageActivity;
import com.renhui.androidrecorder.muxer.AudioEncoderThread;
import com.renhui.androidrecorder.muxer.FileUploadThread;
import com.renhui.androidrecorder.muxer.FileUtil;
import com.renhui.androidrecorder.muxer.MediaMuxerActivity;
import com.renhui.androidrecorder.muxer.MediaMuxerThread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GDSSurveyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gdssurvey);

        Intent intent = getIntent();
        String filePath = intent.getStringExtra("complete_info");

        int[] questionScore = new int[]{1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0,
                0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 1};

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
        groupList.add(findViewById(R.id.button15));
        groupList.add(findViewById(R.id.button16));
        groupList.add(findViewById(R.id.button17));
        groupList.add(findViewById(R.id.button18));
        groupList.add(findViewById(R.id.button19));
        groupList.add(findViewById(R.id.button20));
        groupList.add(findViewById(R.id.button21));
        groupList.add(findViewById(R.id.button22));
        groupList.add(findViewById(R.id.button23));
        groupList.add(findViewById(R.id.button24));
        groupList.add(findViewById(R.id.button25));
        groupList.add(findViewById(R.id.button26));
        groupList.add(findViewById(R.id.button27));
        groupList.add(findViewById(R.id.button28));
        groupList.add(findViewById(R.id.button29));
        groupList.add(findViewById(R.id.button30));

        Button isFinished = findViewById(R.id.isfinished);
        Button reUpload = findViewById(R.id.reUpload);
        FileUtil fileSwapHelper = new FileUtil(filePath);

        isFinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                List<Integer> unfinished = new ArrayList<>();
                int sumScore = 0;
                for (int i = 0; i < 30; i++) {
                    // 生成两个groupButton
                    String firstName = "button" + (i + 1) + '_' + 0;
                    String secondName = "button" + (i + 1) + '_' + 1;
                    int firstId = getResources().getIdentifier(firstName, "id", getPackageName());
                    int secondId = getResources().getIdentifier(secondName, "id", getPackageName());
                    // 判断是否已作答与对应分值
                    if (groupList.get(i).getCheckedRadioButtonId() == -1) {
                        unfinished.add(i);
                    } else if (groupList.get(i).getCheckedRadioButtonId() == firstId) {
                        if (questionScore[i] == 0) {
                            sumScore++;
                        }
                    } else if (groupList.get(i).getCheckedRadioButtonId() == secondId) {
                        if (questionScore[i] == 1) {
                            sumScore++;
                        }
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
                    putMessage.append("未作答！");

                    AlertDialog failDialog = new AlertDialog.Builder(GDSSurveyActivity.this)
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
                    String message = "您在GDS问卷中的得分是：" + sumScore + "分";
                    AlertDialog scoreDialog = new AlertDialog.Builder(GDSSurveyActivity.this)
                            .setTitle("问卷未全部作答！")
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
                                        bw.write(message);
                                        bw.close();
                                        osw.close();
                                        fos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    // 上传文件
                                    FileUploadThread.stopUpload();
                                    FileUploadThread.startUpload(GDSSurveyActivity.this, fileSwapHelper.getFullPath(), fileSwapHelper.getFilePath());
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
                    Toast.makeText(GDSSurveyActivity.this, "还没有填写完问卷并提交", Toast.LENGTH_SHORT).show();
                }
                // 重新上传最近的文件
                FileUploadThread.stopUpload();
                FileUploadThread.startUpload(GDSSurveyActivity.this, fileSwapHelper.getFullPath(), fileSwapHelper.getFilePath());
            }
        });
    }
}