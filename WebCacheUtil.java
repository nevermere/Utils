package com.ly.core.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * webview缓存工具
 * Created by Xuqn on 2016-8-25.
 */
public class WebCacheUtil {

    private static final String TAG = "WebCacheUtil";

    /**
     * 清除WebView缓存
     */
    public static void clearWebViewCache(Context mContext) {

        //清理Webview缓存数据库
        try {
            mContext.deleteDatabase("webview.db");
            mContext.deleteDatabase("webviewCache.db");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //WebView 缓存文件
        String appCache = mContext.getFilesDir().getAbsolutePath() + Const.APP_CACHE_DIRNAME;
        File appCacheDir = new File(appCache);

        String webviewCache = mContext.getCacheDir().getAbsolutePath() + Const.WEB_CACHE_DIRNAME;
        File webviewCacheDir = new File(webviewCache);

        //删除webview 缓存目录
        if (webviewCacheDir.exists()) {
            mContext.deleteFile(webviewCache);
        }
        //删除webview 缓存 缓存目录
        if (appCacheDir.exists()) {
            mContext.deleteFile(appCache);
        }
    }

    /**
     * 递归删除 文件/文件夹
     *
     * @param file
     */
    public void deleteFile(File file) {

        Log.i(TAG, "delete file path=" + file.getAbsolutePath());

        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
            file.delete();
        } else {
            Log.e(TAG, "delete file no exists " + file.getAbsolutePath());
        }
    }

}
