package com.ly.idisplay.net;

import com.ly.idisplay.listener.WebServiceListener;
import com.ly.idisplay.util.LogUtil;
import com.ly.idisplay.util.MD5Util;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ly.idisplay.commons.FusionWeb.REQUEST_TIMEOUT_VALUE;

/**
 * WebService服务程序
 * Created by fzJiang on 2017-09-11 18:45.
 */
public class WebService {

    private static final String WEB_SERVER_URL = "http://192.168.128.3:8018/ccs/service?wsdl"; // WebService服务器地址
    private static final String NAMESPACE = "http://ws.dcm.linyang.com/";  // 命名空间

    private static final int MAX_POOL_SIZE = 3;
    private static WebService webService;
    private ExecutorService mPool;

    /**
     * 获取WebService服务实例
     *
     * @return
     */
    public static WebService getInstance() {
        if (webService == null) {
            webService = new WebService();
        }
        return webService;
    }

    private WebService() {
        mPool = Executors.newFixedThreadPool(MAX_POOL_SIZE);
    }

    /**
     * 判断是否运行
     *
     * @return
     */
    private boolean isStart() {
        return !mPool.isShutdown() || !mPool.isTerminated();
    }


    /**
     * 发送WebService请求
     *
     * @param methodName
     * @param properties
     * @param listener
     */
    public final void getWebReceive(String methodName, HashMap<String, String> properties, WebServiceListener listener) {
        if (!isStart()) return;
        mPool.execute(new WebServicePoster(methodName, properties, listener, REQUEST_TIMEOUT_VALUE));
    }

    /**
     * WebService请求方法
     */
    private class WebServicePoster implements Runnable {

        private String methodName;
        private HashMap<String, String> properties;
        private WebServiceListener listener;
        private int timeout;

        WebServicePoster(String methodName, HashMap<String, String> properties, WebServiceListener listener, int timeout) {
            this.methodName = methodName;
            this.properties = properties;
            this.listener = listener;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            // 创建HttpTransportSE
            HttpTransportSE httpTransportSE = new HttpTransportSE(WEB_SERVER_URL, timeout);
            // 创建SoapObject对象
            SoapObject soapObject = new SoapObject(NAMESPACE, methodName);
            // 添加参数
            if (properties != null) {
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    soapObject.addProperty(entry.getKey(), entry.getValue());
                }
            }
            // 设置SOAP协议版本号
            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
            // 添加请求头部信息
            Element[] header = new Element[1];
            header[0] = new Element().createElement(NAMESPACE, "authInfo");
            // 调用者用户名
            Element userName = new Element().createElement(NAMESPACE, "username");
            userName.addChild(Node.TEXT, "101102");
            header[0].addChild(Node.ELEMENT, userName);
            // 调用者密码
            Element pass = new Element().createElement(NAMESPACE, "password");
            pass.addChild(Node.TEXT, MD5Util.getMD5String("111111").toUpperCase(Locale.CHINA));
            header[0].addChild(Node.ELEMENT, pass);
            // 设置请求头部信息
            soapEnvelope.headerOut = header;
            // 设置请求体参数
            soapEnvelope.bodyOut = soapObject;
            // 设置是否是.Net
            soapEnvelope.dotNet = false;
            // 设置调试模式
            httpTransportSE.debug = true;
            try {
                // 发送请求
                httpTransportSE.call(NAMESPACE + methodName, soapEnvelope);
                LogUtil.d("发送:" + httpTransportSE.requestDump);
                // 获取服务器响应返回的信息实体
                SoapObject result = null;
                if (soapEnvelope.getResponse() != null) {
                    result = (SoapObject) soapEnvelope.bodyIn;
                }
                // 结果回调
                if (listener != null) {
                    listener.onWebReceived(result);
                }
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
                if (listener != null) {
                    listener.onWebException(e);
                }
            }
        }
    }
}
