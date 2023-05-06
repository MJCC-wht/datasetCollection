package com.renhui.androidrecorder.survey;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class CSVUtil {

    public static String fullPath = null;

    public static void writeToCSV(List<Object[]> list, String fileParentPath, String fileName) {
        boolean flag = createCsvFile(list, fileParentPath, fileName);
        if (flag) {
            Log.e("CSVUtil", "创建CSV文件成功");
        } else {
            Log.e("CSVUtil", "创建CSV文件失败");
        }
    }

    public static boolean createCsvFile(List<Object[]> rows, String filePath, String fileName) {
        //标记文件生成是否成功；
        boolean flag = true;

        //文件输出流
        BufferedWriter fileOutputStream = null;

        try {
            // 含文件名的全路径
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
            fullPath = filePath + File.separator + fileName + "-" +
                                simpleDateFormat.format(System.currentTimeMillis()) +  ".csv";
            File file = new File(fullPath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();

            //格式化浮点数据
            NumberFormat formatter = NumberFormat.getNumberInstance();
            formatter.setMaximumFractionDigits(10);     //设置最大小数位为10；


            //实例化文件输出流
            fileOutputStream = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8), 1024);

            //遍历输出每行
            Iterator<Object[]> ite = rows.iterator();

            while (ite.hasNext()) {
                Object[] rowData = (Object[]) ite.next();
                for (int i = 0; i < rowData.length; i++) {
                    Object obj = rowData[i];   //当前字段
                    //格式化数据
                    String field = "";
                    if (null != obj) {
                        if (obj.getClass() == String.class)     //如果是字符串
                        {
                            field = (String) obj;
                        } else if (obj.getClass() == Double.class || obj.getClass() == Float.class)   //如果是浮点型
                        {
                            field = formatter.format(obj);   //格式化浮点数，使浮点数不以科学计数法输出
                        } else if (obj.getClass() == Integer.class || obj.getClass() == Long.class | obj.getClass() == Short.class || obj.getClass() == Byte.class) {
                            //如果是整型
                            field += obj;
                        } else if (obj.getClass() == Date.class)   //如果是日期类型
                        {
                            field = simpleDateFormat.format(obj);
                        } else {
                            field = " ";   //null时给一个空格占位
                        }

                        // 拼接所有字段为一行数据
                        if (i < rowData.length - 1)     //不是最后一个元素
                        {
                            fileOutputStream.write("\"" + field + "\"" + ",");
                        } else {
                            //最后一个元素
                            fileOutputStream.write("\"" + field + "\"");
                        }
                    }
                    //创建一个新行
                    ite.hasNext();
                }
                fileOutputStream.newLine();     //换行，创建一个新行；
            }
            fileOutputStream.flush();
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        } finally {
            try {
                assert fileOutputStream != null;
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }
}
