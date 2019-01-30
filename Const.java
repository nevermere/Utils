package com.ly.neuter.core.utils;

import android.util.Log;

import java.io.UnsupportedEncodingException;

/**
 * 常量定义 Created by Jijl on 2016-3-11.
 */
public class Const {
    /**
     * 林洋版本APP
     */
    public static int APP_LY = 1;

    /**
     * 中性版本APP
     */
    public static int APP_NEUTER = 2;
    /**
     * 设置app版本
     */
    private static int appType = APP_NEUTER;

    public static int getAppType() {
        return appType;
    }

    public static final String MENU_LIST_MAIN = "menu_list_main";

    public static final String MENU_LIST_MORE = "menu_list_more";
    /**
     * 当前登陆用户名
     */
    private static String loginUserName = "admin";

    private static String loginUserNameWeb;

    public static String getLoginUserNameWeb() {
        return loginUserNameWeb;
    }

    public static void setLoginUserNameWeb(String loginUserNameWeb) {
        Const.loginUserNameWeb = loginUserNameWeb;
    }

    /**
     * 当前登陆用户密码
     */
    private static String loginPassword = "5D4E48706188419B346DAA285E176ADE";

    public static String getLoginUserName() {
        return loginUserName;
    }

    public static void setLoginUserName(String loginUserName) {
        if (loginUserName == null)
            return;
        Const.loginUserName = loginUserName;
        try {
            Const.loginUserNameWeb = new String(loginUserName.getBytes(), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d("Const", "UnsupportedEncodingException:" + e);
        }
    }

    public static String getLoginPassword() {
        return loginPassword;
    }

    public static void setLoginPassword(String loginPassword) {
        if (loginPassword == null)
            return;
        Const.loginPassword = loginPassword;
    }


    /**
     * 推送常量定义
     *
     * @author Xuqn
     */
    public class PushConst {

        /**
         * 通知内容,用于获取通知内容并展示
         */
        public static final String NOTIFICATION_EXTRA = "notificationExtra";
        /**
         * 通知id
         */
        public static final String NOTIFICATION_ID = "notificationID";

        /**
         * 推送状态消息(接收,已读)
         */
        public static final String NOTIFICATION_STATUS_CHANGED = "com.ly.core.NOTIFICATION_STATUS_CHANGED";
        /**
         * 订阅变更
         */
        public static final String NOTIFICATION_BOOK_CHANGED = "com.ly.core.NOTIFICATION_BOOK_CHANGED";

        /**
         * 标签定义
         *
         * @author Xuqn
         */
        public class TAG {
            public static final String EVENT = "event";
            public static final String NEWS = "news";
            public static final String REPORT = "report";
            public static final String POLICY = "policy";
            public static final String DEFINE_MSG = "definedMsg";
            public static final String CHANGE_TAGS = "tagChanged";

        }
    }
}
