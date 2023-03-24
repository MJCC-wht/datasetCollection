package com.renhui.androidrecorder.muxer;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 文件处理工具类
 * Created by renhui on 2017/9/25.
 */
public class FileUtils {

    // 文件路径
    private static final String MAIN_DIR_NAME = "/android_records";
    private static final String BASE_VIDEO = "/video/";
    private static final String BASE_AUDIO = "/audio/";
    private static final String BASE_VIDEO_EXT = ".mp4";
    private static final String BASE_AUDIO_EXT = ".wav";

    private String currentFilePath;
    private String currentFullPath;

    public FileUtils(String filePath) {
        currentFilePath = filePath;
    }

    public String getFilePath() {
        return this.currentFilePath;
    }

    public String getFullPath() {
        return this.currentFullPath;
    }


    public void getSaveFilePath() {
        // 先检查SD卡剩余空间
        checkSpace();
        // 添加时间戳
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);

        StringBuilder fullPath = new StringBuilder();
        fullPath.append(getExternalStorageDirectory());
        String[] filePathList = currentFilePath.split("/");
        if (filePathList.length < 3) {
            return;
        }
        fullPath.append(MAIN_DIR_NAME);
        fullPath.append(filePathList[0].equals("video") ? BASE_VIDEO : BASE_AUDIO);
        fullPath.append(filePathList[filePathList.length - 1]);
        fullPath.append("-" + simpleDateFormat.format(System.currentTimeMillis()));
        fullPath.append(filePathList[0].equals("video") ? BASE_VIDEO_EXT : BASE_AUDIO_EXT);

        currentFullPath = fullPath.toString();
        File file = new File(currentFullPath);
        File parentFile = file.getParentFile();
        assert parentFile != null;
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        currentFilePath = currentFilePath + "-" + simpleDateFormat.format(System.currentTimeMillis()) +
                (filePathList[0].equals("video") ? BASE_VIDEO_EXT : BASE_AUDIO_EXT);
    }

    /**
     * 检查剩余空间
     */
    private void checkSpace() {
        StringBuilder fullPath = new StringBuilder();
        String checkPath = getExternalStorageDirectory();
        // 赋予文件名
        String[] filePathList = currentFilePath.split("/");
        if (filePathList.length < 3) {
            return;
        }
        fullPath.append(checkPath);
        fullPath.append(MAIN_DIR_NAME);
        fullPath.append(filePathList[0].equals("video") ? BASE_VIDEO : BASE_AUDIO);

        // 检查SD卡是否还有剩余空间，如果剩余空间过小，进行清理
        if (checkCardSpace(checkPath)) {
            File file = new File(fullPath.toString());

            if (!file.exists()) {
                file.mkdirs();
            }

            String[] fileNames = file.list();
            if (fileNames.length < 1) {
                return;
            }

            List<String> fileNameLists = Arrays.asList(fileNames);
            Collections.sort(fileNameLists);

            for (int i = 0; i < fileNameLists.size() && checkCardSpace(checkPath); i++) {
                //清理视频
                String removeFileName = fileNameLists.get(i);
                File removeFile = new File(file, removeFileName);
                try {
                    removeFile.delete();
                    Log.e("angcyo-->", "删除文件 " + removeFile.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("angcyo-->", "删除文件失败 " + removeFile.getAbsolutePath());
                }
            }
        }
    }

    private boolean checkCardSpace(String filePath) {
        File dir = new File(filePath);
        double totalSpace = dir.getTotalSpace();//总大小
        double freeSpace = dir.getFreeSpace();//剩余大小
        if (freeSpace < totalSpace * 0.2) {
            return true;
        }
        return false;
    }

    /**
     * 获取sdcard路径
     */
    public static String getExternalStorageDirectory() {
        return Environment.getExternalStorageDirectory().getPath();
    }


}
