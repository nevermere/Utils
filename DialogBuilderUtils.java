package com.ly.neuter.core.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

import com.ly.neuter.core.R;
import com.ly.neuter.core.commons.FusionCode;
import com.ly.neuter.core.commons.FusionWebCommand;
import com.ly.neuter.core.net.WebService;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 对话框工具类
 * Created by fzJiang on 2016-5-19.
 */
public class DialogBuilderUtils {

    //发现新版本,升级确认对话框
    public static void initConfirmDialog(Context mContext, final Handler mHandler, final int request) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.version_confirm_dialog_title));
        builder.setMessage(mContext.getResources().getString(R.string.version_confirm_dialog_message));
        builder.setPositiveButton(mContext.getResources().getString(R.string.version_confirm_dialog_positive_button), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //开始执行更新查询操作
                mHandler.sendEmptyMessage(request);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(mContext.getResources().getString(R.string.version_confirm_dialog_negative_button), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    //发现新版本,下载升级对话框
    public static void initUpdateDialog(final Context mContext, final String apk) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.version_Update_dialog_title));
        if (NetUtil.isWifi(mContext)) {
            builder.setMessage(mContext.getResources().getString(R.string.version_Update_dialog_message));
        } else {
            builder.setMessage(mContext.getResources().getString(R.string.version_Update_dialog_message02));
        }
        builder.setPositiveButton(mContext.getResources().getString(R.string.version_Update_dialog_positive_button), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //更新版本号大于当前版本号,执行更新确认
                try {
                    new ApkDownLoadTask(mContext, apk).execute(new URL(WebService.getUpdateDownloadUrl
                            (FusionWebCommand.HOST_ADDRESS, apk)));
                } catch (MalformedURLException e) {
                    ToastUtil.showShortToast(mContext, mContext.getResources().getString(R.string.version_exception));
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(mContext.getResources().getString(R.string.version_Update_dialog_negative_button), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    //用户进入需量分析界面，提示选择测量点对话框
    public static void initChooseDcpDialog(Context mContext, final Handler mHandler) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.dcp_dialog_title));
        builder.setMessage(mContext.getResources().getString(R.string.dcp_dialog_message));
        builder.setPositiveButton(mContext.getResources().getString(R.string.dcp_dialog_positive_button), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mHandler.sendEmptyMessage(FusionCode.Target.IS_TO_CHOOSE_DCP);
            }
        });
        builder.setNegativeButton(mContext.getResources().getString(R.string.dcp_dialog_negative_button), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mHandler.sendEmptyMessage(FusionCode.Target.NO_TO_CHOOSE_DCP);
            }
        });
        builder.create().show();
    }
}
