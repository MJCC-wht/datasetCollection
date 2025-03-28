package com.renhui.androidrecorder.homepage;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.icu.text.IDNA;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.renhui.androidrecorder.MyApplication;
import com.renhui.androidrecorder.R;
import com.renhui.androidrecorder.muxer.MediaMuxerActivity;
import com.renhui.androidrecorder.muxer.ResultAnalyzeThread;
import com.renhui.androidrecorder.survey.ADLActivity;
import com.renhui.androidrecorder.survey.GDSSurveyActivity;
import com.renhui.androidrecorder.survey.HealthSurveyActivity;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomepageActivity extends AppCompatActivity {
    private Button btn11,btn12,btn13,
            btn21,btn22,
            btn31,btn32,
            btn41,btn42,btn43,btn5,btn_return;
    private TextView infoText;

    private static final String KEY_INDEX="INDEX";
    private String choice, choice2; // 选择的是哪个项目？
    private int[] flag = new int[]{0,0,0,0,0,0,0,0,0,0};//初始状态
    private String filePath, camera_window = "";

    public void setFlag(int[] flag) {
        this.flag = flag;
    }

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
        filePath = intent.getStringExtra("complete_info");
        camera_window = intent.getStringExtra("cameraWindow");

        // 直接显示info
        infoText = (TextView) findViewById(R.id.info);
        String[] infoList = filePath.split("-");
        String info = "编号：" + infoList[0] + "\n" + "姓名：" + infoList[1];
        infoText.setText(info);
        infoText.setTextColor(Color.RED);

        btn11 = (Button) findViewById(R.id.btn11);
        btn11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action1/") ;
                choice2 = "motion";
                judge1(flag, btn11,0);
            }
        });
        btn12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action2/") ;
                choice2 = "motion";
                judge1(flag, btn12,1);
            }
        });
        btn13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/action3/") ;
                choice2 = "motion";
                judge1(flag, btn13, 2);
            }
        });

        btn21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition1/") ;
                choice2 = "recognition";
                judge2(flag, btn21,3);
            }
        });
        btn22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("video/recognition2/") ;
                choice2 = "recognition";
                judge2(flag, btn22,4);
            }
        });
        btn31.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("audio/description1/") ;
                choice2 = "audio";
                judge3(flag, btn31,5);
            }
        });
        btn32.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChoice("audio/description2/") ;
                choice2 = "audio";
                judge3(flag, btn32,6);
            }
        });
        btn41 = (Button) findViewById(R.id.btn41);
        btn41.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setClass(HomepageActivity.this, GDSSurveyActivity.class);
                intent.putExtra("complete_info", "text/gds/" + filePath);
                startActivity(intent);
                btnChange(flag, btn41, 7);
            }
        });
        btn42 = (Button) findViewById(R.id.btn42);
        btn42.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setClass(HomepageActivity.this, HealthSurveyActivity.class);
                intent.putExtra("complete_info", "text/health/" + filePath);
                startActivity(intent);
                btnChange(flag, btn42, 8);//
            }
        });
        btn43 = (Button) findViewById(R.id.btn43);
        btn43.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setClass(HomepageActivity.this, ADLActivity.class);
                intent.putExtra("complete_info", "text/ADL/" + filePath);
                startActivity(intent);
                btnChange(flag, btn43, 9);//
            }
        });

        // 显示健康状态结果
        btn5 = (Button) findViewById(R.id.btn5);
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 检查有没有结果
                String analyzeResult = ResultAnalyzeThread.analyzeResult;
                if (analyzeResult == null) {
                    // 如果结果还没有来，显示预计还要多久
                    if (ResultAnalyzeThread.startTime == - 1) {
                        // 说明还没开始
                        AlertDialog noStartDialog = new AlertDialog.Builder(HomepageActivity.this)
                                .setTitle("分析未开始：")
                                .setMessage("请先完成测试再查看结果")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.e("HomePageActivity", "分析未开始");
                                    }
                                }).create();
                        noStartDialog.show();
                    } else {
                        long elapsedTime = System.currentTimeMillis() - ResultAnalyzeThread.startTime;
                        int elapsedMinute = (int) (elapsedTime / 1000 / 60);
                        AlertDialog timeDialog = new AlertDialog.Builder(HomepageActivity.this)
                                .setTitle("分析未完成：")
                                .setMessage("预计还剩" + (10 - elapsedMinute) + "分钟左右")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.e("HomePageActivity", "分析未完成");
                                    }
                                }).create();
                        timeDialog.show();
                    }
                } else {
                    // 有结果，但是要分析返回情况
                    if (analyzeResult.startsWith("ERROR") || analyzeResult.contains("nan") || analyzeResult.contains("失败")) {
                        AlertDialog failDialog = new AlertDialog.Builder(HomepageActivity.this)
                                .setTitle("分析失败：")
                                // TODO:需要改正
                                .setMessage("分析失败，视频时长不足")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.e("HomePageActivity", "分析失败");
                                    }
                                }).create();
                        failDialog.show();
                    } else {
                        String message = "";
                        // 定义匹配浮点数的正则表达式
                        String regex = "\\d+\\.\\d+";
                        // 编译正则表达式
                        Pattern pattern = Pattern.compile(regex);
                        // 创建Matcher对象
                        Matcher matcher = pattern.matcher(analyzeResult);
                        double score = 0.0;
                        if (matcher.find()) {
                            String floatString = matcher.group();
                            score = Double.parseDouble(floatString);
                        }
                        score = Double.parseDouble(String.format(Locale.CHINA, "%.2f", score));
                        if (score >= 0.7) {
                            message = "您的记忆减退风险为" + (1 - score) + "，为健康人群。建议定期筛查，感谢您的配合";
                        } else if (score >= 0.4) {
                            message = "您的记忆减退风险为" + (1 - score) + "，为低风险人群。建议定期筛查，感谢您的配合";
                        } else {
                            message = "您的记忆减退风险为" + (1 - score) + "，为中风险人群。建议定期筛查，感谢您的配合";
                        }

                        AlertDialog successDialog = new AlertDialog.Builder(HomepageActivity.this)
                                .setTitle("分析成功：")
                                .setMessage(message)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.e("HomePageActivity", "分析成功");
                                    }
                                }).create();
                        successDialog.show();
                    }
                }
            }
        });
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setClass(HomepageActivity.this, InfoInputActivity.class);
                startActivity(intent);
            }
        });
    }

    private void jump_to_collect(int[] flag, Button btn, int i){
        Intent intent = new Intent();
        if (filePath == null || filePath.equals(" ")) {
            Toast.makeText(HomepageActivity.this, "请点击左图，输入受试者信息",
                    Toast.LENGTH_SHORT).show();
        } else {
            btnChange(flag, btn, i);
            intent.setClass(HomepageActivity.this, MediaMuxerActivity.class);
            intent.putExtra("complete_info",choice + filePath);
            intent.putExtra("cameraWindow",camera_window);
            startActivity(intent);
        }
    }
    private void judge1(int[] flag, Button btn, int i){
        if (choice2 == null || choice2.charAt(0) != 'm') {
            Toast.makeText(HomepageActivity.this, "请正确选择 采集动作",
                    Toast.LENGTH_SHORT).show();
        } else if (choice2.equals("motion")){
            jump_to_collect(flag, btn, i);
        }
    }
    private void judge2(int[] flag, Button btn, int i){
        if (choice2 == null || choice2.charAt(0) != 'r') {
            Toast.makeText(HomepageActivity.this, "请正确选择 认知/情绪视频",
                    Toast.LENGTH_SHORT).show();
        } else if (choice2.equals("recognition")) {
            jump_to_collect(flag, btn, i);
        }
    }
    private void judge3(int[] flag, Button btn, int i){
        if (choice2 == null || choice2.charAt(0) != 'a') {
            Toast.makeText(HomepageActivity.this, "请正确选择 语音任务",
                    Toast.LENGTH_SHORT).show();
        } else if (choice2.equals("audio")){
            jump_to_collect(flag, btn, i);
        }
    }
    private void btnChange(int[] flag, Button btn, int i){
        if(flag[i] == 0)
        {
            btn.setBackgroundColor(Color.parseColor("#C5C5C5"));
            flag[i] = 1;
        }//选中
//        else {
//            btn.setBackgroundColor(Color.parseColor("#E4EEE5"));
//            flag[i] = 0;
//        }//取消
    }
    private void initView(){

        btn11=findViewById(R.id.btn11);
        btn12=findViewById(R.id.btn12);
        btn13=findViewById(R.id.btn13);

        btn21=findViewById(R.id.btn21);
        btn22=findViewById(R.id.btn22);

        btn31=findViewById(R.id.btn31);
        btn32=findViewById(R.id.btn32);

        btn41=findViewById(R.id.btn41);
        btn42=findViewById(R.id.btn42);

        btn5=findViewById(R.id.btn5);
        btn_return=findViewById(R.id.btn_return);

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

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if (this.getResources().getConfiguration().orientation==  Configuration.ORIENTATION_LANDSCAPE) {
//        } else if (this.getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT) {
//        }
//        if (newConfig.hardKeyboardHidden== Configuration.HARDKEYBOARDHIDDEN_NO) {
//        }else if (newConfig.hardKeyboardHidden== Configuration.HARDKEYBOARDHIDDEN_YES) {
//        }
//    }

//    @Override
//    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        outState.putInt("count",count);
//        super.onSaveInstanceState(outState);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        count = savedInstanceState.getInt("count");
//    }
}
