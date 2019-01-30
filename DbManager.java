package com.linyang.ihelper.data.db;

import android.content.Context;

import com.j256.ormlite.android.AndroidDatabaseConnection;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.linyang.ihelper.data.db.entity.UserFaceInfo;
import com.linyang.ihelper.data.db.entity.UserFingerInfo;
import com.linyang.ihelper.data.db.entity.UserInfo;


/**
 * 数据库管理类
 */
public class DbManager {

    private DbHelper dbHelper;
    private static DbManager manager = null;

    private static AndroidDatabaseConnection connection = null;

    private Dao<UserInfo, Long> userDaoOpe = null;
    private Dao<UserFaceInfo, Long> userFaceDaoOpe = null;
    private Dao<UserFingerInfo, Long> userFingerDaoOpe = null;

    private DbManager(Context context) {
        dbHelper = OpenHelperManager.getHelper(context, DbHelper.class);
        userDaoOpe = dbHelper.getUserDao();
        userFaceDaoOpe = dbHelper.getUserFaceDao();
        userFingerDaoOpe = dbHelper.getUserFingerDao();
    }

    /**
     * 获取管理器实例
     *
     * @param context
     * @return
     */
    public static DbManager getInstance(Context context) {
        if (manager == null) {
            manager = new DbManager(context);
        }
        return manager;
    }

    /**
     * 获取连接
     *
     * @return
     */
    public static AndroidDatabaseConnection getConnection() {
        if (manager != null) {
            if (connection != null) {
                return connection;
            } else {
                if (manager.dbHelper != null) {
                    connection = new AndroidDatabaseConnection(manager.dbHelper.getWritableDatabase(), true);
                    return connection;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 关闭管理器
     */
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
            OpenHelperManager.releaseHelper();
            dbHelper = null;
        }
    }

    /**
     * 获取用户信息数据库操作类
     *
     * @return
     */
    public Dao<UserInfo, Long> getUserDao() {
        return userDaoOpe;
    }

    /**
     * 获取用户人脸信息数据库操作类
     *
     * @return
     */
    public Dao<UserFaceInfo, Long> getUserFaceDao() {
        return userFaceDaoOpe;
    }

    /**
     * 获取用户指纹信息数据库操作类
     *
     * @return
     */
    public Dao<UserFingerInfo, Long> getUserFingerDao() {
        return userFingerDaoOpe;
    }
}
