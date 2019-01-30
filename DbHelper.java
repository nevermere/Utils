package com.linyang.ihelper.data.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.stetho.inspector.protocol.module.Database;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.linyang.ihelper.BuildConfig;
import com.linyang.ihelper.data.db.entity.UserFaceInfo;
import com.linyang.ihelper.data.db.entity.UserFingerInfo;
import com.linyang.ihelper.data.db.entity.UserInfo;
import com.linyang.ihelper.util.LogUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库操作类
 */
public class DbHelper extends OrmLiteSqliteOpenHelper {

    private Dao<UserInfo, Long> userDaoOpe;
    private Dao<UserFaceInfo, Long> userFaceDaoOpe;
    private Dao<UserFingerInfo, Long> userFingerDaoOpe;

    public DbHelper(Context context) {
        super(context, BuildConfig.DATABASE_NAME, null, BuildConfig.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            // 创建数据库
            TableUtils.createTable(connectionSource, UserInfo.class);
            TableUtils.createTable(connectionSource, UserFingerInfo.class);
            TableUtils.createTable(connectionSource, UserFaceInfo.class);
        } catch (SQLException e) {
            LogUtil.e("数据库创建失败:" + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        // 数据库升级,创建新表,并迁移数据到新数据库
        Log.d("GreenDaoDbHelper", "数据库升级,创建新表,并迁移数据到新数据库:" + oldVersion + "---->" + newVersion);
        try {
            //删除原有表
            TableUtils.dropTable(connectionSource, UserInfo.class, true);
            TableUtils.dropTable(connectionSource, UserFingerInfo.class, true);
            TableUtils.dropTable(connectionSource, UserFaceInfo.class, true);
            //创建新表
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
            LogUtil.e("数据库升级失败!", e);
        }
    }

    /**
     * 获取用户信息数据库操作类
     *
     * @return
     */
    Dao<UserInfo, Long> getUserDao() {
        if (userDaoOpe == null) {
            try {
                userDaoOpe = getDao(UserInfo.class);
            } catch (SQLException e) {
                e.printStackTrace();
                LogUtil.e("用户信息数据库获取失败!", e);
            }
        }
        return userDaoOpe;
    }

    /**
     * 获取用户人脸信息数据库操作类
     *
     * @return
     */
    Dao<UserFaceInfo, Long> getUserFaceDao() {
        if (userFaceDaoOpe == null) {
            try {
                userFaceDaoOpe = getDao(UserFaceInfo.class);
            } catch (SQLException e) {
                e.printStackTrace();
                LogUtil.e("用户人脸信息数据库获取失败!", e);
            }
        }
        return userFaceDaoOpe;
    }

    /**
     * 获取用户指纹信息数据库操作类
     *
     * @return
     */
    Dao<UserFingerInfo, Long> getUserFingerDao() {
        if (userFingerDaoOpe == null) {
            try {
                userFingerDaoOpe = getDao(UserFingerInfo.class);
            } catch (SQLException e) {
                e.printStackTrace();
                LogUtil.e("用户指纹信息数据库获取失败!", e);
            }
        }
        return userFingerDaoOpe;
    }
}
