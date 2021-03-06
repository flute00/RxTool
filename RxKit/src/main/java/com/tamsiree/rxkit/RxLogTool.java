package com.tamsiree.rxkit;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author tamsiree
 * @date 2016/1/24
 */
public class RxLogTool {

    private final static SimpleDateFormat LOG_FORMAT = new SimpleDateFormat("yyyy年MM月dd日_HH点mm分ss秒");// 日志的输出格式
    private final static SimpleDateFormat FILE_SUFFIX = new SimpleDateFormat("HH点mm分ss秒");// 日志文件格式
    private final static SimpleDateFormat FILE_DIR = new SimpleDateFormat("yyyy年MM月dd日");// 日志文件格式
    private static Boolean LOG_SWITCH = true; // 日志文件总开关
    private static Boolean LOG_TO_FILE = true; // 日志写入文件开关
    private static String LOG_TAG = "RxLogTool"; // 默认的tag
    private static char LOG_TYPE = 'v';// 输入日志类型，v代表输出所有信息,w则只输出警告...
    private static int LOG_SAVE_DAYS = 7;// sd卡中日志文件的最多保存天数
    private static String LOG_FILE_PATH; // 日志文件保存路径
    private static String LOG_FILE_NAME;// 日志文件保存名称

    public static void init(Context context) { // 在Application中初始化
        LOG_FILE_PATH = RxFileTool.getRootPath().getPath() + File.separator + context.getPackageName() + File.separator + "Log";
        LOG_FILE_NAME = "RxLogTool_";
    }

    /****************************
     * Warn
     *********************************/
    public static File w(Object msg) {
        return w(LOG_TAG, msg);
    }

    public static File w(String tag, Object msg) {
        return w(tag, msg, null);
    }

    public static File w(String tag, Object msg, Throwable tr) {
        return log(tag, msg.toString(), tr, 'w');
    }

    /***************************
     * Error
     ********************************/
    public static File e(Object msg) {
        return e(LOG_TAG, msg);
    }

    public static File e(String tag, Object msg) {
        return e(tag, msg, null);
    }

    public static File e(String tag, Object msg, Throwable tr) {
        return log(tag, msg.toString(), tr, 'e');
    }

    /***************************
     * Debug
     ********************************/
    public static File d(Object msg) {
        return d(LOG_TAG, msg);
    }

    public static File d(String tag, Object msg) {// 调试信息
        return d(tag, msg, null);
    }

    public static File d(String tag, Object msg, Throwable tr) {
        return log(tag, msg.toString(), tr, 'd');
    }

    /****************************
     * Info
     *********************************/
    public static File i(Object msg) {
        return i(LOG_TAG, msg);
    }

    public static File i(String tag, Object msg) {
        return i(tag, msg, null);
    }

    public static File i(String tag, Object msg, Throwable tr) {
        return log(tag, msg.toString(), tr, 'i');
    }

    /**************************
     * Verbose
     ********************************/
    public static File v(Object msg) {
        return v(LOG_TAG, msg);
    }

    public static File v(String tag, Object msg) {
        return v(tag, msg, null);
    }

    public static File v(String tag, Object msg, Throwable tr) {
        return log(tag, msg.toString(), tr, 'v');
    }

    /**
     * 根据tag, msg和等级，输出日志
     *
     * @param tag
     * @param msg
     * @param level
     */
    private static File log(String tag, String msg, Throwable tr, char level) {
        File logFile = new File("");
        if (LOG_SWITCH) {
            if ('e' == level && ('e' == LOG_TYPE || 'v' == LOG_TYPE)) { // 输出错误信息
                Log.e(tag, msg, tr);
            } else if ('w' == level && ('w' == LOG_TYPE || 'v' == LOG_TYPE)) {
                Log.w(tag, msg, tr);
            } else if ('d' == level && ('d' == LOG_TYPE || 'v' == LOG_TYPE)) {
                Log.d(tag, msg, tr);
            } else if ('i' == level && ('d' == LOG_TYPE || 'v' == LOG_TYPE)) {
                Log.i(tag, msg, tr);
            } else {
                Log.v(tag, msg, tr);
            }
            if (LOG_TO_FILE) {
                String content = "";
                if (!RxDataTool.isNullString(msg)) {
                    content += msg;
                }
                if (tr != null) {
                    content += "\n";
                    content += Log.getStackTraceString(tr);
                }

                logFile = log2File(String.valueOf(level), tag, content);
            }
        }

        return logFile;
    }

    /**
     * 打开日志文件并写入日志
     *
     * @return
     **/
    private synchronized static File log2File(String mylogtype, String tag, String text) {
        Date nowtime = new Date();
        String date = FILE_SUFFIX.format(nowtime);
        String dateLogContent = "Date:" + LOG_FORMAT.format(nowtime) + "\nLogType:" + mylogtype + "\nTag:" + tag + "\nContent:\n" + text; // 日志输出格式
        String path = LOG_FILE_PATH + File.separator + FILE_DIR.format(nowtime);
        File destDir = new File(path);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        File file = new File(path, LOG_FILE_NAME + date + ".txt");
        try {
            FileWriter filerWriter = new FileWriter(file, true);
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(dateLogContent);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 删除指定的日志文件
     */
    public static void delAllLogFile() {// 删除日志文件
//        String needDelFiel = FILE_SUFFIX.format(getDateBefore());
        RxFileTool.deleteDir(LOG_FILE_PATH);
    }

    /**
     * 得到LOG_SAVE_DAYS天前的日期
     *
     * @return
     */
    private static Date getDateBefore() {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - LOG_SAVE_DAYS);
        return now.getTime();
    }

    public static void saveLogFile(String message) {
        File fileDir = new File(RxFileTool.getRootPath() + File.separator + RxTool.getContext().getPackageName());
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        File file = new File(fileDir, RxTimeTool.getCurrentDateTime("yyyyMMdd") + ".txt");
        try {
            if (file.exists()) {
                PrintStream ps = new PrintStream(new FileOutputStream(file, true));
                ps.append(RxTimeTool.getCurrentDateTime("\n\n\nyyyy-MM-dd HH:mm:ss") + "\n" + message);// 往文件里写入字符串
            } else {
                PrintStream ps = new PrintStream(new FileOutputStream(file));
                file.createNewFile();
                ps.println(RxTimeTool.getCurrentDateTime("yyyy-MM-dd HH:mm:ss") + "\n" + message);// 往文件里写入字符串
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
