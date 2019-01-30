package com.ly.idisplay.net;

import android.net.ParseException;

import com.ly.idisplay.commons.FusionWeb;
import com.ly.idisplay.listener.HttpServiceListener;
import com.ly.idisplay.util.LogUtil;
import com.ly.idisplay.util.MD5Util;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * HttpService服务程序
 */
public class HttpService {

    private static final int MAX_POOL_SIZE = 3;
    private static HttpService httpService;
    private ExecutorService mPool;

    /**
     * 获取HttpService服务实例
     *
     * @return
     */
    public static HttpService getInstance() {
        if (httpService == null) {
            httpService = new HttpService();
        }
        return httpService;
    }

    /**
     * 停止HttpService服务,清空/中断执行任务
     *
     * @return
     */
    public boolean stop() {
        mPool.shutdown();
        return true;
    }

    /**
     * 启动HttpService服务
     *
     * @return
     */
    public boolean start() {
        if (mPool.isShutdown()) {
            mPool = Executors.newFixedThreadPool(MAX_POOL_SIZE);
            return true;
        }
        return false;
    }

    /**
     * 判断是否运行
     *
     * @return
     */
    private boolean isStart() {
        return !mPool.isShutdown() || !mPool.isTerminated();
    }

    private HttpService() {
        mPool = Executors.newFixedThreadPool(MAX_POOL_SIZE);
    }


    /**
     * 执行HttpService请求
     *
     * @param url
     * @param param
     * @param listener
     * @param timeout
     */
    public final void getReceive(String url, List<NameValuePair> param, HttpServiceListener listener, int timeout) {
        if (!isStart()) return;
        mPool.execute(new HttpServicePoster(url, param, listener, timeout));
    }

    /**
     * HttpService请求方法
     */
    private class HttpServicePoster implements Runnable {

        private HttpServiceListener mListener;
        private int timeOut;
        private String requestUrl;
        private List<NameValuePair> mParam;

        HttpServicePoster(String url, List<NameValuePair> param, HttpServiceListener listener, int timeout) {
            requestUrl = url;
            mParam = param;
            mListener = listener;
            timeOut = timeout;
        }

        @Override
        public void run() {
            HttpPost mRequest = new HttpPost(requestUrl);
            if (mParam != null) {
                try {
                    mRequest.setEntity(new UrlEncodedFormEntity(mParam, HTTP.UTF_8));
                } catch (UnsupportedEncodingException e) {
                    mListener.onException(e, 0);
                    return;
                }
            }
            try {
                HttpClient client = new DefaultHttpClient();
                if (timeOut != 0) {
                    client.getParams().setParameter(
                            CoreConnectionPNames.CONNECTION_TIMEOUT, 5 * 1000);
                    client.getParams().setParameter(
                            CoreConnectionPNames.SO_TIMEOUT, timeOut);
                }
                LogUtil.d("WebRequest:" + mRequest.getRequestLine().toString());

                HttpResponse mResponse = client.execute(mRequest);
                // 返回解析
                int retCode = mResponse.getStatusLine().getStatusCode();
                LogUtil.d("retCode:" + retCode);
                if (retCode == 200) {
                    if (mListener != null) {
                        mListener.onReceived(EntityUtils.toString(mResponse.getEntity()));
                    }
                } else if (retCode == 408) {
                    if (mListener != null) {
                        mListener.onTimeOut();
                    }
                } else {
                    if (mListener != null) {
                        mListener.onException(null, retCode);
                    }
                }
            } catch (ClientProtocolException e) {
                if (mListener != null)
                    mListener.onException(e, 0);
                LogUtil.d("ClientProtocolException");
            } catch (IOException e) {
                e.printStackTrace();

                if (e instanceof org.apache.http.conn.ConnectTimeoutException) {
                    if (mListener != null) {
                        mListener.onTimeOut();
                    }
                } else {
                    if (mListener != null)
                        mListener.onException(e, 0);
                    LogUtil.d("IOException");
                }
            } catch (ParseException e) {
                e.printStackTrace();
                if (mListener != null) {
                    mListener.onException(e, 0);
                }
                LogUtil.d("ParseException");
            } catch (Exception e) {
                e.printStackTrace();
                if (mListener != null) {
                    mListener.onException(e, 0);
                }
                LogUtil.d("Exception" + e);
            }
        }
    }

    /**
     * 获取命令请求URL
     *
     * @param command
     * @return
     */
    public static String getCommandUrl(String command) {
        return FusionWeb.HOST_ADDRESS + command;
    }

    /**
     * 判断当前页是否为404页面
     *
     * @param host
     * @param nowUrl
     * @return
     */
    public static boolean is404Page(String host, String nowUrl) {
        if (nowUrl == null) return false;
        String url = host + "/commonController/" + "show404Page.htm";
        return url.equals(nowUrl);
    }

    /**
     * 生成登录参数
     *
     * @param userName 用户名
     * @param password 密码
     * @return
     */
    public static List<NameValuePair> createLoginParam(String userName, String password) {
        List<NameValuePair> param = new ArrayList<>();
        //用户名
        param.add(new BasicNameValuePair("userName", userName));
        //密码,进行MD5加密
        param.add(new BasicNameValuePair("passWord", MD5Util.getMD5String(password).toUpperCase(Locale.CHINA)));
        return param;
    }
}
