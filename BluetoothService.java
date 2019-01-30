package com.android.sht.core.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.android.sht.Const;
import com.android.sht.core.MainSetInfo;
import com.android.sht.core.listener.BluetoothListener;
import com.android.sht.fragment.MainSetFragment;
import com.android.util.SystemUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.UUID;

import static android.os.Build.VERSION_CODES.KITKAT;

/**
 * 蓝牙服务
 *
 * @author Xuqn
 */
public class BluetoothService extends Service {

    public static final String TAG = "BluetoothService";

    public static final UUID SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final int BLUETOOTH_RECEIVE = 1100;//蓝牙接收
    public static final int BLUETOOTH_STATUS = 1101;//蓝牙状态
    public static final int BLUETOOTH_SEARCH = 1102;//蓝牙搜索
    public static final int BLUETOOTH_CONNECT = 1103;//蓝牙连接

    public static final int BLUETOOTH_SEARCH_INTERVAL = 20 * 1000;

    private Context mContext;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothHandler mHandler;

    private BluetoothStatus preStatus;//暂停前状态
    private BluetoothStatus mStatus;
    private BluetoothSocket mSocket;

    private IBinder mBinder;
    private BluetoothListener mListener;

    private IntentFilter mFilter;
    private BluetoothBroadcast mBluetoothBroadcast;

    private SharedPreferences mSharedPreferences;
    private String defaultName;
    private String defaultAddr;

    public BluetoothService() {
        mStatus = BluetoothStatus.STOP;
        preStatus = BluetoothStatus.STOP;
        mHandler = new BluetoothHandler(this);
        mFilter = new IntentFilter();
        mBluetoothBroadcast = new BluetoothBroadcast();
        mFilter.addAction(BluetoothDevice.ACTION_FOUND);//设备搜索事件
        mFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//设备状态变更事件
        mFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//搜索结束事件
    }

    /**
     * 获取服务状态
     *
     * @return
     */
    public BluetoothStatus getStatus() {
        return mStatus;
    }

    /**
     * 获取暂停前工作状态
     *
     * @return
     */
    public BluetoothStatus getPreStatus() {
        return preStatus;
    }

    /**
     * 获取连接端口
     *
     * @return
     */
    public BluetoothSocket getSocket() {
        return mSocket;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder == null) mBinder = new BluetoothBinder();
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(mContext, "本设备不支持蓝牙设备", Toast.LENGTH_SHORT).show();
        }
        //加载默认设备
        mSharedPreferences = mContext.getSharedPreferences(MainSetInfo.SET_NAME, Context.MODE_PRIVATE);
        defaultName = mSharedPreferences.getString(MainSetFragment.DEFAULT_BLUETOOTH_NAME, "");
        defaultAddr = mSharedPreferences.getString(MainSetFragment.DEFAULT_BLUETOOTH_ADDR, "");
        //注册
        registerReceiver(mBluetoothBroadcast, mFilter);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothBroadcast);
        stop();
    }

    /**
     * 蓝牙暂停
     */
    public void pause() {
        if (mStatus == BluetoothStatus.PAUSE
                || mStatus == BluetoothStatus.STARTING
                || mStatus == BluetoothStatus.STOPING) {
            return;
        }
        //暂停蓝牙操作
        preStatus = mStatus;
        mStatus = BluetoothStatus.PAUSE;
        //关闭蓝牙搜索
        mHandler.removeMessages(BLUETOOTH_SEARCH);
        if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    /**
     * 蓝牙恢复
     */
    public void resume() {
        //重新加载
        defaultName = mSharedPreferences.getString(MainSetFragment.DEFAULT_BLUETOOTH_NAME, "");
        defaultAddr = mSharedPreferences.getString(MainSetFragment.DEFAULT_BLUETOOTH_ADDR, "");
        if (mSharedPreferences.getInt(MainSetInfo.QUERY_DEVICE, -1) != Const.QUERY_BLUETOOTH) {
            mStatus = preStatus;
            return;
        }
        if (mSocket == null) {
            //未建立蓝牙连接,则发起设备搜索
            mStatus = BluetoothStatus.STOP;
            mHandler.removeMessages(BLUETOOTH_SEARCH);
            mHandler.sendEmptyMessage(BLUETOOTH_SEARCH);
        } else if (mStatus == BluetoothStatus.PAUSE) {
            //恢复
            switch (preStatus) {
                case START:
                    mStatus = preStatus;
                    break;
                case STOP:
                    mStatus = preStatus;
                    start(mSocket);
                    break;
                default:
                    break;
            }
        } else if (mStatus == BluetoothStatus.STOP) {
            //若蓝牙异常关闭则重新打开
            start(mSocket);
        }
    }

    /**
     * 蓝牙停止
     */
    public void stop() {
        //关闭蓝牙
        if (mStatus == BluetoothStatus.PAUSE) {
            //若在暂停状态下关闭连接则更新暂停前状态
            if (preStatus != BluetoothStatus.START) return;
            preStatus = BluetoothStatus.STOPING;
        } else if (mStatus == BluetoothStatus.START) {
            mStatus = BluetoothStatus.STOPING;
        } else {
            return;
        }
        try {
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 蓝牙启动
     *
     * @param socket
     * @return
     */
    public boolean start(BluetoothSocket socket) {
        if (socket == null) {
            //搜索默认蓝牙设备用于建立连接
            switch (mStatus) {
                case STOP:
                    //只有在停止状态可以进行此操作
                    mHandler.removeMessages(BLUETOOTH_SEARCH);
                    mHandler.sendEmptyMessage(BLUETOOTH_SEARCH);
                    return true;
                default:
                    return false;
            }
        } else {
            //使用指定端口建立蓝牙连接
            //判断当前蓝牙连接状态
            switch (mStatus) {
                case PAUSE:
                    //暂停状态下不建立连接,但是保留预连接端口
                    if (preStatus == BluetoothStatus.STOP || preStatus == BluetoothStatus.STOPING) {
                        mSocket = socket;
                        return true;
                    } else {
                        Log.w(TAG, "当前连接未关闭,不可重复建立连接");
                        return false;
                    }
                case START:
                    if (mSocket == null) break;
                    if (socket.getRemoteDevice().getName().equals(mSocket.getRemoteDevice().getName())
                            && socket.getRemoteDevice().getAddress().equals(mSocket.getRemoteDevice().getAddress())) {
                        //若为相同蓝牙设备且已建立连接则不重复建立
                        return true;
                    } else {
                        //不同设备则建立连接失败
                        Log.w(TAG, "当前连接未关闭,不可重复建立连接");
                        return false;
                    }
                case STARTING:
                    //若为打开中则执行失败
                    return false;
                default:
                    break;
            }
            mSocket = socket;
            //若蓝牙为断开状态则建立蓝牙连接
            if (mStatus == BluetoothStatus.STOP) {
                //异步建立连接
                new Thread(new ConnectThread()).start();
                return true;
            } else
                return false;
        }
    }

    /**
     * 连接建立线程
     *
     * @author Xuqn
     */
    private class ConnectThread implements Runnable {

        @Override
        public void run() {
            try {
                //建立连接
                if (mStatus == BluetoothStatus.PAUSE) {
                    preStatus = BluetoothStatus.STARTING;
                } else {
                    mStatus = BluetoothStatus.STARTING;
                }
                mSocket.connect();
                //启动接收
                Thread bluetooth = new Thread(new BluetoothThread(mSocket.getInputStream()));
                bluetooth.setName("BluetootReceiveThread");
                bluetooth.start();
                if (mStatus == BluetoothStatus.PAUSE) {
                    preStatus = BluetoothStatus.START;
                } else {
                    mStatus = BluetoothStatus.START;
                }
                Log.i(TAG, "蓝牙连接成功");
                mHandler.sendMessage(mHandler.obtainMessage(BLUETOOTH_CONNECT, true));
            } catch (IOException e) {
                Log.e(TAG, "蓝牙连接失败", e);
                if (mStatus == BluetoothStatus.PAUSE) {
                    preStatus = BluetoothStatus.STOP;
                } else {
                    mStatus = BluetoothStatus.STOP;
                }
                mHandler.sendMessage(mHandler.obtainMessage(BLUETOOTH_CONNECT, false));
            }
        }
    }

    /**
     * 蓝牙发送
     *
     * @param data
     * @return
     */
    public boolean write(byte[] data) {
        if (mSocket != null && mStatus == BluetoothStatus.START) {
            try {
                mSocket.getOutputStream().write(data);
                mSocket.getOutputStream().flush();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * 设置蓝牙接收
     *
     * @param listener
     */
    public void setDeviceListener(BluetoothListener listener) {
        mListener = listener;
    }

    /**
     * 蓝牙服务Binder
     *
     * @author Xuqn
     */
    public class BluetoothBinder extends Binder {

        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    /**
     * 蓝牙接收线程
     *
     * @author Xuqn
     */
    private class BluetoothThread implements Runnable {

        private InputStream is;

        BluetoothThread(InputStream stream) {
            is = stream;
        }

        @Override
        public void run() {
            Message msg;
            while (true) {
                if (mStatus == BluetoothStatus.STOP || mStatus == BluetoothStatus.STOPING) {
                    //普通状态下关闭蓝牙
                    break;
                }
                if (mStatus == BluetoothStatus.PAUSE &&
                        (preStatus == BluetoothStatus.STOP || preStatus == BluetoothStatus.STOPING)) {
                    //暂停状态下关闭蓝牙
                    break;
                }
                int len;
                try {
                    byte[] buffer = new byte[1024];
                    len = is.read(buffer);
                    if (len > 0) {
                        byte[] rev = new byte[len];
                        System.arraycopy(buffer, 0, rev, 0, len);
                        if (mStatus != BluetoothStatus.PAUSE) {
                            //蓝牙服务未暂停,则发送数据
                            msg = mHandler.obtainMessage(BLUETOOTH_RECEIVE);
                            msg.obj = rev;
                            mHandler.sendMessage(msg);
                        }
                    }
                } catch (IOException e) {
                    //蓝牙关闭
                    msg = mHandler.obtainMessage(BLUETOOTH_STATUS);
                    if ((mStatus != BluetoothStatus.PAUSE && mStatus != BluetoothStatus.STOPING)
                            || (mStatus == BluetoothStatus.PAUSE && preStatus != BluetoothStatus.STOPING)) {
                        //异常关闭
                        msg.obj = e;
                    }
                    if (mStatus == BluetoothStatus.PAUSE) {
                        //若在暂停状态下关闭连接则更新暂停前状态
                        preStatus = BluetoothStatus.STOP;
                    } else {
                        mStatus = BluetoothStatus.STOP;
                    }
                    mHandler.sendMessage(msg);
                }
            }

        }

    }

    private static class BluetoothHandler extends Handler {
        WeakReference<BluetoothService> mService;

        BluetoothHandler(BluetoothService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            BluetoothService service = mService.get();
            if (service == null) return;
            switch (msg.what) {
                case BLUETOOTH_CONNECT:
                    //蓝牙连接状态
                    boolean status = (msg.obj != null) ? (Boolean) msg.obj : false;
                    if (status) {
                        Toast.makeText(service.mContext, "蓝牙连接成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(service.mContext, "蓝牙连接失败", Toast.LENGTH_SHORT).show();
                        //发起下次连接尝试(重新搜索)
                        service.mSocket = null;
                        service.mStatus = BluetoothStatus.STOP;
                        service.mHandler.sendEmptyMessageDelayed(BLUETOOTH_SEARCH, BLUETOOTH_SEARCH_INTERVAL / 5);
                    }
                    break;

                case BLUETOOTH_SEARCH:
                    //蓝牙设备搜索
                    if (service.mBluetoothAdapter != null) {
                        if (service.mBluetoothAdapter.isEnabled()) {
                            if (!service.mBluetoothAdapter.isDiscovering()) {
                                //蓝牙可用且蓝牙开启
                                if (!service.mBluetoothAdapter.startDiscovery()) {
                                    Log.w(TAG, "蓝牙设备搜索启动失败");
                                } else {
                                    //发起下次搜索
                                    service.mHandler.sendEmptyMessageDelayed(BLUETOOTH_SEARCH, BLUETOOTH_SEARCH_INTERVAL);
                                }
                            } else {
                                //搜索状态忙,发起下次搜索
                                service.mHandler.sendEmptyMessageDelayed(BLUETOOTH_SEARCH, BLUETOOTH_SEARCH_INTERVAL / 5);
                            }
                        }
                    }
                    break;

                case BLUETOOTH_RECEIVE:
                    //蓝牙设备数据接收
                    Message rev = service.mHandler.obtainMessage();
                    rev.copyFrom(msg);
                    if (service.mListener != null) service.mListener.onBluetoothReceived(rev);
                    break;
                case BLUETOOTH_STATUS:
                    //蓝牙设备状态
                    BluetoothStatus tmpStatus;
                    if (service.mStatus == BluetoothStatus.PAUSE) {
                        tmpStatus = service.preStatus;
                    } else {
                        tmpStatus = service.mStatus;
                    }
                    switch (tmpStatus) {
                        case START:
                            Log.i(TAG, "蓝牙启动(恢复)");
                            break;
                        case PAUSE:
                            Log.i(TAG, "蓝牙暂停");
                            break;
                        case STOP:
                            //蓝牙关闭
                            if (msg.obj != null) {
                                //包含异常信息,异常关闭
                                Log.e(TAG, "蓝牙异常关闭", (Throwable) msg.obj);
                            } else {
                                Log.i(TAG, "蓝牙关闭");
                            }
                            break;
                        case STARTING:
                            Log.i(TAG, "蓝牙启动中");
                            break;
                        case STOPING:
                            Log.i(TAG, "蓝牙停止中");
                            break;
                        default:
                            break;
                    }
                    break;
            }
        }
    }

    public class BluetoothBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mStatus != BluetoothStatus.PAUSE) {
                String action = intent.getAction();
                switch (action) {
                    case BluetoothDevice.ACTION_FOUND:
                        switch (mStatus) {
                            case START:
                            case STARTING:
                            case STOPING:
                                //此状态下不进行蓝牙设备操作
                                return;
                            default:
                                break;
                        }
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        Log.i(TAG, MessageFormat.format("Found: {0}:{1}", device.getName(), device.getAddress()));

                        if (device.getName() == null) return;
                        if (device.getName().equals(defaultName) && device.getAddress().equals(defaultAddr)) {
                            try {
                                //获取蓝牙连接
                                BluetoothSocket socket;
                                if (SystemUtil.getSystemVersion() < KITKAT) {
                                    //通过反射得到bltSocket对象
                                    Method m = device.getClass().getMethod("createRfcommSocket", int.class);
                                    socket = (BluetoothSocket) m.invoke(device, 1);
                                } else
                                    //通过和uuid来进行连接(解决4.4.4蓝牙无法连接问题)
                                    socket = device.createRfcommSocketToServiceRecord(SERVICE_UUID);

                                //关闭搜索
                                if (mBluetoothAdapter.isDiscovering()) {
                                    //确保在调用connect()时设备没有执行搜索设备的操作,如果搜索设备也在同时进行，
                                    //那么将会显著地降低连接速率，并很大程度上会连接失败。
                                    mBluetoothAdapter.cancelDiscovery();
                                }
                                //主动开启连接
                                start(socket);
                                mHandler.removeMessages(BLUETOOTH_SEARCH);
                            } catch (Exception e) {
                                Log.e(TAG, "蓝牙端口创建失败", e);
                                mHandler.sendEmptyMessageDelayed(BLUETOOTH_SEARCH, BLUETOOTH_SEARCH_INTERVAL / 5);
                            }
                        }
                        break;

                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        mHandler.removeMessages(BLUETOOTH_SEARCH);
                        switch (mStatus) {
                            case STOP:
                                Toast.makeText(mContext, "找不到默认蓝牙外设或默认蓝牙外设未设置", Toast.LENGTH_SHORT).show();
                                mHandler.sendEmptyMessageDelayed(BLUETOOTH_SEARCH, BLUETOOTH_SEARCH_INTERVAL);
                                break;
                            default:
                                break;
                        }
                        break;

                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        int status = intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE, -1);
                        Log.i(action, MessageFormat.format("蓝牙状态字:{0}", status));
                        break;
                }
            }
        }
    }

    /**
     * 蓝牙状态
     *
     * @author Xuqn
     */
    public enum BluetoothStatus {
        /**
         * 开启
         */
        START,
        /**
         * 开启中
         */
        STARTING,
        /**
         * 暂停
         */
        PAUSE,
        /**
         * 停止
         */
        STOP,
        /**
         * 关闭中
         */
        STOPING
    }
}
