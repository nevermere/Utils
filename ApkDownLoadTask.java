package com.ly.neuter.core.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.ly.neuter.core.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 更新文件下载服务
 * Created by fzJiang on 2016-5-19.
 */
public class ApkDownLoadTask extends AsyncTask<URL, Long, Integer> {

    public static final String TAG = "ApkDownLoadTask";

    public static final int DOWN_LOAD_SUCCESS = 0;
    public static final int DOWN_LOAD_FAILURE = -1;
    public static final int SD_CARD_NOT_FOUND = 101;
    public static final int DOWN_LOAD_CANCEL = 102;

    private HttpURLConnection downloadConn;

    private final String fileName;//http://192.168.128.225:8080/Energy/phoneInterface/downloadAPK.htm?

    private String downloadPath;//http://192.168.128.225:8080/Energy/phoneInterface/downloadAPK.htm?

    private float fileSize = 0;

    private ProgressDialog progressDialog;

    private boolean cancelFlag = false;

    private Context mContext;

    public ApkDownLoadTask(Context context, String path) {
        //fileName = name;
        mContext = context;
        downloadPath = path;
        fileName = mContext.getResources().getString(R.string.app_name);//EnergyApp
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("软件升级");
        progressDialog.setMessage("升级文件下载中,请稍候...");
        progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //取消升级
                cancelFlag = true;
                if (!fileName.equals("")) {
                    File apkFile = new File(Environment.getExternalStorageDirectory(), fileName);
                    if (apkFile.exists()) apkFile.delete();
                }
            }
        });
    }

    @Override
    protected Integer doInBackground(URL... params) {
        OutputStream output = null;
        InputStream downloadStream = null;
        URL url;
        try {
            url = new URL(downloadPath);
            //创建连接
            downloadConn = (HttpURLConnection) url.openConnection();
            fileSize = downloadConn.getContentLength();
            progressDialog.setMax((int) fileSize);
            //准备工作
            //判断文件名是否正确
            if (fileName == null || fileName.equals("")) return DOWN_LOAD_FAILURE;
            //检查SD卡是否存在
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return SD_CARD_NOT_FOUND;
            }
            //创建存储路径(根目录)
            File apkFile = new File(Environment.getExternalStorageDirectory(), fileName);
            if (apkFile.exists()) apkFile.delete();
            if (!apkFile.createNewFile()) return DOWN_LOAD_FAILURE;
            downloadStream = downloadConn.getInputStream();
            output = new FileOutputStream(apkFile);
            //创建缓存
            byte[] buffer = new byte[4 * 1024];
            long loadSize = 0;
            long readSize;
            while ((readSize = downloadStream.read(buffer)) != -1) {
                if (cancelFlag) {
                    if (downloadConn != null) downloadConn.disconnect();
                    return DOWN_LOAD_CANCEL;
                }
                output.write(buffer, 0, (int) readSize);
                loadSize += readSize;
                //更新下载进度
                publishProgress(loadSize);
            }
            output.flush();
            output.close();
            downloadStream.close();
            output = null;
            downloadStream = null;
            return DOWN_LOAD_SUCCESS;

        } catch (IOException e) {
            if (cancelFlag) {
                return DOWN_LOAD_CANCEL;
            } else {
                Log.e(TAG, "升级文件下载失败", e);
            }
        } finally {
            try {
                if (output != null) output.close();
            } catch (IOException e) {
            }
            try {
                if (downloadStream != null) downloadStream.close();
            } catch (IOException e) {
            }
        }
        return DOWN_LOAD_FAILURE;
    }

    @Override
    protected void onPostExecute(Integer result) {
        switch (result) {
            case DOWN_LOAD_SUCCESS:
                Toast.makeText(mContext, "下载成功", Toast.LENGTH_SHORT).show();
                //执行安装操作
                progressDialog.dismiss();
                //安装
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), fileName));
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                mContext.startActivity(intent);
                break;
            case DOWN_LOAD_FAILURE:
                Toast.makeText(mContext, "下载失败", Toast.LENGTH_SHORT).show();
                break;
            case DOWN_LOAD_CANCEL:
                Toast.makeText(mContext, "升级取消", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                break;
        }
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        if (values[0] == 0) {
            progressDialog.show();
        } else {
            if (cancelFlag) return;//已取消不在更新进度
            progressDialog.setProgress(values[0].intValue());
            if (!progressDialog.isShowing()) progressDialog.show();
        }
    }
}
