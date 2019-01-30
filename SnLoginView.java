package com.linyang.ihelper.ui.login.view;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.linyang.ihelper.R;
import com.linyang.ihelper.util.LogUtil;
import com.linyang.ihelper.util.ToastUtil;
import com.linyang.ihelper.widget.card.BaseCardView;
import com.techshino.fp.client.TcFingerClient;
import com.techshino.fp.util.FpConfig;
import com.techshino.fp.util.OnConnectListener;
import com.techshino.fp.util.USBUtil;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 描述:指纹登录卡片界面
 * Created by fzJiang on 2017-12-20 16:26
 */
public class SnLoginView extends BaseCardView implements OnConnectListener {

    @BindView(R.id.sn_bt)
    TextView snBt;

    private USBUtil mUsbUtil;// usb工具类
    private TcFingerClient mClient;// 指纹识别工具

    public SnLoginView(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.login_view_sn;
    }

    @Override
    protected void initViews(Context context) {

    }

    @Override
    public void onViewResume() {
        // 切换到指纹登录界面,则开始指纹登录操作
        initSnDevice();
    }

    @Override
    public void onViewPause() {
        if (mUsbUtil != null) {
            mUsbUtil.disconnectDevice();
        }
    }

    /**
     * 初始化指纹设备
     */
    private void initSnDevice() {
        if (mUsbUtil == null) {
            mUsbUtil = USBUtil.getInstance(context.getApplicationContext());
        }
        // 设置设备使用协议
        mUsbUtil.setProtocol(USBUtil.Protocol.SIMPLE);
        // 设置连接监听
        mUsbUtil.setConnectListener(this);
        // 打开指纹设备
        mUsbUtil.connectDevice();
        // 设置特征类型
        FpConfig.setFeatureType(FpConfig.FEATURE_TYPE_BASE64);
        // 设置出图规格
        FpConfig.setImageType(FpConfig.IMAGE_BIG);
    }

    @Override
    public void onConnected(TcFingerClient tcFingerClient) {
        mClient = tcFingerClient;
    }

    @Override
    public void onConnectFailed(int i, String s) {
        ToastUtil.showToast(context.getApplicationContext(), "指纹设备连接失败:" + s);
    }
}
