package com.linyang.energy.utils;

import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 读取web.properties文件中的数据
  @description:
  @version:0.1
  @author:Cherry
  @date:Jul 15, 2013
 */
public class WebConstant {
	
	//服务器启动时间
	public final static Long SYSTEM_STARUP_TIME = System.currentTimeMillis();
	
	private static final String BUNDLE_NAME = "web"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	
	public static final int density = Integer.parseInt(getString("density","15"));
	
	public static final int pfperiod = Integer.parseInt(getString("pfperiod","30"));
	
	/**
	 * webService用户名
	 */
	public static final String wsUsername = getString("ws.username","admin");
	/**
	 * webService密码
	 */
	public static final String wsPassword = getString("ws.password","admin");


    /**
     * 事件计算结束时间为当前时间往前推一个小时（去分钟）
     */
    public static final int eventCalHour = 1;

    /**
     * 事件计算执行间隔小时数
     */
    public static final int eventIntervalHour = 1;

    /**
     * 右下角弹框事件告警条数，最大值为5
     */
    public static int warningNum = Integer.parseInt(getString("warningNum","5"));

    /**
     * 天气ws用户Id
     */
	public static String weatherUserId = getString("weatherUserId","");

    public static final int maxWarningNum = 5;


	/**
	 * 是否是演示版本
	 */
	public static final int demo = Integer.parseInt(getString("demo", "0"));
	
	/**
	 * 上传文件路径
	 */
	public static final String uploadFile = getString("uploadFile","upload/");
	
	/**
	 * 高温线
	 */
//	public static final int tempHighLine = Integer.parseInt(getString("tempHighLine", "32"));
	
	/**
	 * 低温线
	 */
//	public static final int tempLowLine = Integer.parseInt(getString("tempLowLine", "0"));
	
	/**
	 * 计算多少天历史数据
	 */
//	public static final int historyDay = Integer.parseInt(getString("historyDay", "300"));
	
	/**
	 * 概率占比
	 */
//	public static final int proportion = Integer.parseInt(getString("proportion", "50"));
	
	/**
	 * 连续多少个点
	 */
//	public static final int pointNum = Integer.parseInt(getString("pointNum", "8"));
	
	/**
	 * 演示的日期	
	 */
	public static final Date demoDate = DateUtil.convertStrToDate(getString("demodate", "2015-05-15"),
			DateUtil.SHORT_PATTERN);
	
	public static final Date publishDate = DateUtil.convertStrToDate(getString("publishDate", "2016-01-28 00:00:00"),
			DateUtil.DEFAULT_PATTERN);
	
	/**
	 * 极光推送appKey
	 */
	public static final String appKey = getString("appKey", "0f68d25ab29a6424f5fc4d77");

	/**
	 * 极光推送masterSecret
	 */
	public static final String masterSecret = getString("masterSecret", "a5d2090595d017759d191292");
	
	/**
	 * 极光推送环境(true：真实环境；false：测试环境）
	 */
	public static final boolean apnsProduction = Boolean.parseBoolean(getString("apnsProduction", "false"));

    /**
     * 手机端接口是否需要认证
     */
    public static final String appVerify = getString("appVerify", "0");

    /**
     * 等级配置数组
     */
    public static final int[] cyles = {0, 60, 180, 300, 600, 900, 1260, 1680, 2160, 2700,
            3360, 4140, 5100, 6240, 7620, 9240, 11160, 13380, 15960, 18900,
            22320, 26220, 30820, 35820, 41640, 48180, 55680, 64140, 73800,84660};

    /**
     * 未登录提醒 和 报告提醒 的每天的时间
     */
    public static final int startHour = Integer.parseInt(getString("startHour", "9"));
    public static final int startMinute = Integer.parseInt(getString("startMinute", "30"));
    
    public static final int spec1 = Integer.parseInt(getString("spec1", "5000"));
    public static final int spec2 = Integer.parseInt(getString("spec2", "5000"));

    public static final String[] eleNotLoadModules = getString("eleNotLoadModules","").split(",");
    public static final String[] emoNotLoadModules = getString("emoNotLoadModules","").split(",");
    
    /**
	 * 手机APP头部数据缓存时间
	 */
	public static final int appDataCacheTime = Integer.parseInt(getString("appDataCacheTime","5"));
	
	/**
	 * 极光推送中性版本
	 */
	public static final String appKeyNeuter = getString("appKeyNeuter", "58b00679457173670111089e");
	public static final String masterSecretNeuter = getString("masterSecretNeuter", "b618e8cb645a026ef76ee4db");


	static
	{
		if(warningNum > maxWarningNum ){
			warningNum = maxWarningNum;
		}
	}
	
	private WebConstant() {}

	public static String getString(String key) {
		return getString(key,"");
	}


	public static String getString(String key, String defaultValue) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return defaultValue;
		}
	}
	
	public static Date getChartBaseDate() {
		if (demo == 1)
			return demoDate;
		else
			return new Date();
	}
}
