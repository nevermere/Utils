package com.ly.neuter.core.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.ly.neuter.core.db.DbManager;
import com.ly.neuter.core.db.bean.EventData;
import com.ly.neuter.core.db.bean.TagsData;
import com.ly.neuter.core.db.dialog.EventDetailDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;


/**
 * 推送工具类
 *
 * @author Xuqn
 */
public class PushUtil {

    public static final String TAG = "PushUtil";

    public static final String KEY_APP_KEY = "JPUSH_APPKEY";

    /**
     * 获取系统标签
     *
     * @return
     */
    public static Set<String> getSystemPushTags() {
        Set<String> tags = new HashSet<String>();
        tags.add(Const.PushConst.TAG.CHANGE_TAGS);
        tags.add(Const.PushConst.TAG.EVENT);
//		tags.add(Const.PushConst.TAG.NEWS);
//		tags.add(Const.PushConst.TAG.POLICY);
//		tags.add(Const.PushConst.TAG.REPORT);
//		tags.add(Const.PushConst.TAG.DEFINE_MSG);
        return tags;
    }

    public static boolean isEmpty(String s) {
        if (null == s)
            return true;
        if (s.length() == 0)
            return true;
        return s.trim().length() == 0;
    }

    // 校验Tag Alias 只能是数字,英文字母和中文
    public static boolean isValidTagAndAlias(String s) {
        Pattern p = Pattern.compile("^[\u4E00-\u9FA50-9a-zA-Z_-]{0,}$");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    // 取得AppKey
    public static String getAppKey(Context context) {
        Bundle metaData = null;
        String appKey = null;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (null != ai)
                metaData = ai.metaData;
            if (null != metaData) {
                appKey = metaData.getString(KEY_APP_KEY);
                if ((null == appKey) || appKey.length() != 24) {
                    appKey = null;
                }
            }
        } catch (NameNotFoundException e) {

        }
        return appKey;
    }

    // 取得版本号
    public static String GetVersion(Context context) {
        try {
            PackageInfo manager = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return manager.versionName;
        } catch (NameNotFoundException e) {
            return "Unknown";
        }
    }

    public static void showToast(final String toast, final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conn.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    public static String getImei(Context context, String imei) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imei = telephonyManager.getDeviceId();
        } catch (Exception e) {
            Log.e(PushUtil.class.getSimpleName(), e.getMessage());
        }
        return imei;
    }

    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return line;
    }

    /**
     * 判断是否为小米系统
     *
     * @return
     */
    public static boolean isMIUIRom() {
        String property = getSystemProperty("ro.miui.ui.version.name");
        return !TextUtils.isEmpty(property);
    }

    /**
     * 是否同一天
     *
     * @param timeA
     * @param timeB
     * @return
     */
    public static boolean isSameDay(long timeA, long timeB) {
        Calendar a = Calendar.getInstance();
        Calendar b = Calendar.getInstance();
        a.setTimeInMillis(timeA);
        b.setTimeInMillis(timeB);
        if (a.get(Calendar.YEAR) != b.get(Calendar.YEAR)) {
            return false;
        } else if (a.get(Calendar.MONTH) != b.get(Calendar.MONTH)) {
            return false;
        } else if (a.get(Calendar.DAY_OF_MONTH) != b.get(Calendar.DAY_OF_MONTH)) {
            return false;
        }
        return true;
    }

    /**
     * 获取消息展示对话框
     *
     * @param context
     * @param notification
     * @return
     */
    public static Dialog getNotificationDialog(Context context, Object notification) {
        Dialog dialog = null;
        if (notification instanceof EventData) {
            dialog = new EventDetailDialog(context, (EventData) notification);
        }
        return dialog;
    }

    /**
     * 处理订阅绑定变更信息
     *
     * @param context
     * @param accountId
     * @param json
     */
    public static boolean handleBookChanged(Context context, String accountId, String json) {
        try {
            JSONObject obj = new JSONObject(json);
            if (obj.has("bookInfo")) {
                //数据为订阅消息,执行订阅消息更新
                Dao<TagsData, Integer> dao = DbManager.getInstance(context).getTagsDataDao();
                JSONArray books = obj.getJSONArray("bookInfo");
                JSONObject book;
                String bookStr;
                Set<String> tags = new HashSet<String>();
                StringBuilder tagsStr = new StringBuilder();
                for (int i = 0; i < books.length(); i++) {
                    book = books.getJSONObject(i);
                    bookStr = book.getString("BOOK_ID");
                    tags.add(bookStr);
                    tagsStr.append(bookStr).append(";");
                }
                try {
                    //移除旧绑定信息
                    dao.delete(dao.queryForAll());
                    TagsData data = new TagsData();
                    data.setAlia(accountId);
                    if (tagsStr.toString().endsWith(";")) {
                        data.setTags(tagsStr.substring(0, tagsStr.length() - 1));
                    } else {
                        data.setTags(tagsStr.toString());
                    }
                    //保存新绑定信息
                    dao.create(data);
                } catch (SQLException e) {
                    Log.e(TAG, "save new tags data failure", e);
                }
                //添加系统级绑定
                for (String tag : PushUtil.getSystemPushTags()) {
                    tags.add(tag);
                }
                //进行标签绑定
                JPushInterface.setTags(context, tags, new TagAliasCallback() {

                    @Override
                    public void gotResult(int responseCode, String alias,
                                          Set<String> tags) {
                        Log.d(TAG, "[NetServerApp] SetTags Result:" + String.valueOf(responseCode));
                    }
                });
                return true;
            }
        } catch (JSONException e) {
            Log.e(TAG, "parse web service response failure", e);
        }
        return false;
    }
}
