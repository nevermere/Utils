package com.ly.neuter.core.utils;


import com.ly.neuter.core.db.bean.ChartData;
import com.ly.neuter.core.net.WebService;
import com.ly.neuter.core.net.response.CustomerDataInfo;
import com.ly.neuter.core.net.response.HistoryDataInfo;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * ECharts������
 */
public class EChartsUtil {

    /**
     * 创建JS方法
     *
     * @param function
     * @param params
     * @return
     */
    public static String createJSFunction(String function, Object... params) {
        StringBuilder command = new StringBuilder("javascript:");
        command.append(function);
        if (params.length != 0) {
            //添加参数
            command.append("(");
            Object obj;
            for (int i = 0; i < params.length; i++) {
                obj = params[i];
                if (obj == null) {
                    command.append("null");
                } else if (obj instanceof String) {
                    String str = obj.toString();
                    if (str.startsWith("[") && str.endsWith("]")) {
                        command.append(str);
                    } else {
                        command.append(toJSString(str));
                    }
                } else if (obj instanceof Number) {
                    command.append(obj.toString());
                } else {
                    continue;
                }
                command.append(',');
            }
            if (command.charAt(command.length() - 1) == ',') {
                command.deleteCharAt(command.length() - 1);
            }
            command.append(")");
        } else {
            //无参数
            return createJSFunction(function);
        }
        return command.toString();
    }

    /**
     * 创建无参数JS方法
     *
     * @param function
     * @return
     */
    public static String createJSFunction(String function) {
        StringBuilder command = new StringBuilder("javascript:");
        command.append(function);
        command.append("()");
        return command.toString();
    }

    public static String array2JSArray(SimpleDateFormat fromat, Date... array) {
        if (array == null || fromat == null) return "";
        String[] strArray = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            strArray[i] = toJSString(fromat.format(array[i]));
        }
        return toJSArray(strArray);
    }

    public static String array2JSArray(String... array) {
        if (array == null) return "";
        for (int i = 0; i < array.length; i++) {
            if (!array[i].startsWith("[") || !array[i].endsWith("]")) {
                array[i] = toJSString(array[i]);
            }
        }
        return toJSArray(array);
    }

    public static String array2JSArray(Number... array) {
        if (array == null) return "";
        String[] strArray = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                strArray[i] = array[i].toString();
            } else {
                strArray[i] = "";
            }
        }
        return toJSArray(strArray);
    }

    public static String toJSArray(String... array) {
        StringBuilder js = new StringBuilder();
        js.append("[");
        for (int i = 0; i < array.length; i++) {
            js.append(array[i]);
            js.append(',');
        }
        if (js.charAt(js.length() - 1) == ',') {
            js.deleteCharAt(js.length() - 1);
        }
        js.append("]");
        return js.toString();
    }

    public static String toJSString(String str) {
        return "'" + str + "'";
    }

    public static <T> String list2JSArray(Class<T> clasz, List<T> list, SimpleDateFormat format) throws Exception {
        if (clasz == String.class) {
            String[] array = new String[list.size()];
            list.toArray(array);
            return array2JSArray(array);
        } else if (clasz.getSuperclass() == Number.class) {
            Number[] array = new Number[list.size()];
            list.toArray(array);
            return array2JSArray(array);
        } else if (clasz == Date.class) {
            if (format == null) throw new Exception("date format is null");
            Date[] array = new Date[list.size()];
            list.toArray(array);
            return array2JSArray(format, array);
        } else {
            throw new Exception(clasz.getName() + " not supported");
        }
    }

    /**
     * ��ʾ��ͼ��
     */
    public static String createBlankChart(String msg) {
        return EChartsUtil.createJSFunction("showBlankChart", msg);
    }

    /**
     * ��ʾ��ͼ��
     */
    public static String createBlankChart() {
        return EChartsUtil.createJSFunction("showBlankChart");
    }

    /**
     * ��ʾ���ͼ��
     *
     * @param chartType
     * @param chartTimeType
     * @param meterInfo
     * @param idType
     * @param idValue
     * @param info
     * @return
     * @throws Exception
     */
    public static String createDataChart(ChartData.ChartType chartType, ChartData.ChartTimeType chartTimeType
            , CustomerDataInfo meterInfo, WebService.DataObjType idType, String idValue, HistoryDataInfo info) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        String[] dataNameList = new String[]{"��"};
        if (idType == WebService.DataObjType.CUSTOMER) {
            dataNameList = new String[]{"��"};
        } else if (chartType != ChartData.ChartType.VOL && chartType != ChartData.ChartType.CUR) {
            //��ȡ���������
            for (int i = 0; i < meterInfo.getMeterList().size(); i++) {
                CustomerDataInfo.MeterInfo meter = meterInfo.getMeterList().get(i);
                if (meter.getMeterId().equals(idValue)) {
                    dataNameList = new String[]{meter.getMeterName()};
                    break;
                }
            }
        } else {
            //����&��ѹ
            if (!info.getDataList().isEmpty()) {
                int phase = info.getDataList().get(0).getDataValue().split(";").length;
                if (phase == 2) {
                    //��������
                    dataNameList = new String[]{"A��", "C��"};
                } else if (phase == 3) {
                    //��������
                    dataNameList = new String[]{"A��", "B��", "C��"};
                } else {
                    return EChartsUtil.createBlankChart("����쳣,��ˢ��");
                }
            }
        }
        //��ȡ���
        int count = info.getDataList().size();
        Double[][] values = new Double[dataNameList.length][count];
        Date[] dates = new Date[count];
        for (int i = 0; i < count; i++) {
            HistoryDataInfo.DataInfo data = info.getDataList().get(i);
            try {
                dates[i] = dateFormat.parse(data.getDataName());
            } catch (ParseException e) {
                return EChartsUtil.createBlankChart("����쳣,��ˢ��");
            }
            String[] value = data.getDataValue().split(";");
            for (int a = 0; a < values.length; a++) {
                if (!value[a].equals("null")) {
                    values[a][i] = Double.valueOf(value[a]);
                } else {
                    values[a][i] = null;
                }
            }

        }
        String url = null;
        String format;
        String jsFun = null;
        String unit;
        int divide;
        Double totle;
        switch (chartType) {
            case ELE:
                //����ͼ��
                //���������
                unit = "kWh";
                divide = 1;
                totle = 0D;
                for (Double d : values[0]) {
                    if (d >= 100000) {
                        divide = 1000;
                        unit = "MWh";
                        break;
                    }
                }
                //��������
                for (int i = 0; i < count; i++) {
                    values[0][i] /= divide;
                    totle += values[0][i];
                }
                totle = new BigDecimal(totle).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                if (chartTimeType == ChartData.ChartTimeType.DAY || chartTimeType == ChartData.ChartTimeType.THREE_DAY) {
                    //��ʾ����ͼ��
                    if (chartTimeType == ChartData.ChartTimeType.DAY) {
                        format = "HH:mm";
                    } else {
                        format = "MM/dd HH:mm";
                    }
                    jsFun = "showEleLineChart";
                } else {
                    //��ʾ��״ͼ��
                    if (chartTimeType == ChartData.ChartTimeType.YEAR) {
                        format = "yy/MM";
                    } else {
                        format = "MM/dd";
                    }
                    jsFun = "showEleBarChart";
                }
                url = EChartsUtil.createJSFunction(jsFun
                        , dataNameList[0]
                        , unit
                        , totle
                        , EChartsUtil.array2JSArray(new SimpleDateFormat(format, Locale.CHINA), dates)
                        , EChartsUtil.array2JSArray(values[0]));
                break;
            case PF:
                //��������ͼ��
                jsFun = "showPfLineChart";
                Double baseLine = info.getBaseLine();
                if (chartTimeType == ChartData.ChartTimeType.YEAR) {
                    format = "yy/MM";
                } else if (chartTimeType == ChartData.ChartTimeType.DAY) {
                    format = "HH:mm";
                } else if (chartTimeType == ChartData.ChartTimeType.THREE_DAY) {
                    format = "MM/dd HH:mm";
                } else {
                    format = "MM/dd";
                }
                url = EChartsUtil.createJSFunction(jsFun
                        , dataNameList[0]
                        , baseLine
                        , EChartsUtil.array2JSArray(new SimpleDateFormat(format, Locale.CHINA), dates)
                        , EChartsUtil.array2JSArray(values[0]));
                break;
            case AP:
                jsFun = "showAPLineChart";
            case RP:
                if (jsFun == null) jsFun = "showRPLineChart";
                unit = "kW";
                divide = 1;
                totle = 0D;
                for (Double d : values[0]) {
                    if (d >= 100000) {
                        divide = 1000;
                        unit = "MW";
                        break;
                    }
                }
                for (int i = 0; i < count; i++) {
                    values[0][i] /= divide;
                    totle += values[0][i];
                }
                totle = new BigDecimal(totle).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                if (chartTimeType == ChartData.ChartTimeType.DAY) {
                    format = "HH:mm";
                } else {
                    format = "MM/dd HH:mm";
                }
                url = EChartsUtil.createJSFunction(jsFun
                        , dataNameList[0]
                        , unit
                        , totle
                        , EChartsUtil.array2JSArray(new SimpleDateFormat(format, Locale.CHINA), dates)
                        , EChartsUtil.array2JSArray(values[0]));
                break;
            case CUR:
                jsFun = "showCurLineChart";
            case VOL:
                if (jsFun == null) jsFun = "showVolLineChart";
                if (chartTimeType == ChartData.ChartTimeType.DAY) {
                    format = "HH:mm";
                } else {
                    format = "MM/dd HH:mm";
                }
                String[] pValues = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    pValues[i] = EChartsUtil.array2JSArray(values[i]);
                }
                url = EChartsUtil.createJSFunction(jsFun
                        , EChartsUtil.array2JSArray(dataNameList)
                        , EChartsUtil.array2JSArray(new SimpleDateFormat(format, Locale.CHINA), dates)
                        , EChartsUtil.array2JSArray(pValues));
                break;
            default:
                //��������������ͼ��,��ʾ�ձ�
                break;
        }
        return url;
    }

    public static String createDemandAnalyze(ChartData.ChartType chartType, ChartData.ChartTimeType chartTimeType
            , CustomerDataInfo meterInfo, WebService.DataObjType idType, String idValue, HistoryDataInfo info
            , int demandType) throws Exception {
        String url;
        String[] dataNameList = new String[1];
        if (idType == WebService.DataObjType.CUSTOMER) {
            dataNameList[0] = "��";
        } else {
            //��ȡ���������
            for (int i = 0; i < meterInfo.getMeterList().size(); i++) {
                CustomerDataInfo.MeterInfo meter = meterInfo.getMeterList().get(i);
                if (meter.getMeterId().equals(idValue)) {
                    dataNameList[0] = meter.getMeterName();
                    break;
                }
            }
        }
        SimpleDateFormat dateFormat;
        String chartDateFormat;
        //��ȡ��������
        switch (demandType) {
            case 0:
                //������
                dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                chartDateFormat = "MM/dd";
                break;
            case 1:
                //������
                dateFormat = new SimpleDateFormat("yyyy-MM", Locale.CHINA);
                chartDateFormat = "yy/MM";
                break;
            default:
                return null;
        }
        //�������
        Double[] values = new Double[info.getDataList().size()];
        Date[] dates = new Date[values.length];
        for (int i = 0; i < values.length; i++) {
            HistoryDataInfo.DataInfo item = info.getDataList().get(i);
            dates[i] = dateFormat.parse(item.getDataName());
            values[i] = Double.valueOf(item.getDataValue().split(";")[0]);
        }
        url = EChartsUtil.createJSFunction("showDemandAnalyzeLineChart"
                , dataNameList[0]
                , "kW"
                , EChartsUtil.array2JSArray(new SimpleDateFormat(chartDateFormat, Locale.CHINA), dates)
                , EChartsUtil.array2JSArray(values)
                , EChartsUtil.array2JSArray(info.getDemandApalyzeData().getDeclareMDList()));
        return url;
    }

    /**
     * ��ɵ�ѷ���ͼ��
     *
     * @param chartType
     * @param chartTimeType
     * @param meterInfo
     * @param idType
     * @param idValue
     * @param info
     * @return
     * @throws Exception
     */
    public static String createFeeAnalyzeChart(ChartData.ChartType chartType, ChartData.ChartTimeType chartTimeType
            , CustomerDataInfo meterInfo, WebService.DataObjType idType, String idValue, HistoryDataInfo info) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.CHINA);
        String[] dataNameList = new String[1];
        if (idType == WebService.DataObjType.CUSTOMER) {
            dataNameList[0] = "��";
        } else {
            //��ȡ���������
            for (int i = 0; i < meterInfo.getMeterList().size(); i++) {
                CustomerDataInfo.MeterInfo meter = meterInfo.getMeterList().get(i);
                if (meter.getMeterId().equals(idValue)) {
                    dataNameList[0] = meter.getMeterName();
                    break;
                }
            }
        }
        //��ȡ���
        int count = info.getDataList().size();
        Double[][] values = new Double[dataNameList.length][count];
        Date[] dates = new Date[count];
        for (int i = 0; i < count; i++) {
            HistoryDataInfo.DataInfo data = info.getDataList().get(i);
            try {
                dates[i] = dateFormat.parse(data.getDataName());
            } catch (ParseException e) {
                return EChartsUtil.createBlankChart("����쳣,��ˢ��");
            }
            String[] value = data.getDataValue().split(";");
            for (int a = 0; a < values.length; a++) {
                values[a][i] = Double.valueOf(value[a]);
            }

        }
        String url;
        String format = "yy/MM";
        String jsFun = "showFeeAnalyzeBarChart";
        String unit = "Ԫ";
        Double totel = 0D;
        for (int i = 0; i < count; i++) {
            totel += values[0][i];
        }
        url = EChartsUtil.createJSFunction(jsFun
                , dataNameList[0]
                , unit
                , totel
                , EChartsUtil.array2JSArray(new SimpleDateFormat(format, Locale.CHINA), dates)
                , EChartsUtil.array2JSArray(values[0]));
        return url;
    }

    /**
     * ��ɹ����������ͼ��
     *
     * @param chartType
     * @param chartTimeType
     * @param meterInfo
     * @param idType
     * @param idValue
     * @param info
     * @return
     * @throws Exception
     */
    public static String createPfAnalyzeChart(ChartData.ChartType chartType, ChartData.ChartTimeType chartTimeType
            , CustomerDataInfo meterInfo, WebService.DataObjType idType, String idValue, HistoryDataInfo info) throws Exception {
        String[] dataNameList = new String[1];
        if (idType == WebService.DataObjType.CUSTOMER) {
            dataNameList[0] = "��";
        } else {
            //��ȡ���������
            for (int i = 0; i < meterInfo.getMeterList().size(); i++) {
                CustomerDataInfo.MeterInfo meter = meterInfo.getMeterList().get(i);
                if (meter.getMeterId().equals(idValue)) {
                    dataNameList[0] = meter.getMeterName();
                    break;
                }
            }
        }
        String dateParseStr;
        String dateFormatStr;
        switch (chartTimeType) {
            case DAY:
                dateParseStr = "yyyy-MM-dd HH:mm";
                dateFormatStr = "MM-dd HH:mm";
                break;
            case TEN_DAY:
                dateParseStr = "yyyy-MM-dd";
                dateFormatStr = "MM/dd";
                break;
            case THREE_DAY:
                dateParseStr = "yyyy-MM-dd HH:mm";
                dateFormatStr = "MM/dd";
                break;
            case THIRTY_DAY:
                dateParseStr = "yyyy-MM-dd";
                dateFormatStr = "MM/dd";
                break;
            case YEAR:
                dateParseStr = "yyyy-MM";
                dateFormatStr = "yy/MM";
                break;
            default:
                return EChartsUtil.createBlankChart("����쳣,��ˢ��");
        }
        //��ȡ���
        int count = info.getDataList().size();
        Double[][] values = new Double[dataNameList.length][count];
        Date[] dates = new Date[count];
        SimpleDateFormat dateParse = new SimpleDateFormat(dateParseStr, Locale.CHINA);
        for (int i = 0; i < count; i++) {
            HistoryDataInfo.DataInfo data = info.getDataList().get(i);
            try {
                dates[i] = dateParse.parse(data.getDataName());
            } catch (ParseException e) {
                return EChartsUtil.createBlankChart("����쳣,��ˢ��");
            }
            String[] value = data.getDataValue().split(";");
            for (int a = 0; a < values.length; a++) {
                values[a][i] = Double.valueOf(value[a]);
            }

        }
        String url;
        String jsFun = "showPfAnalyzeLineChart";
        url = EChartsUtil.createJSFunction(jsFun
                , dataNameList[0]
                , info.getBaseLine()
                , EChartsUtil.array2JSArray(new SimpleDateFormat(dateFormatStr, Locale.CHINA), dates)
                , EChartsUtil.array2JSArray(values[0]));
        return url;
    }

    public static String createLineloseChart(ChartData.ChartType chartType, ChartData.ChartTimeType chartTimeType
            , CustomerDataInfo meterInfo, WebService.DataObjType idType, String idValue, HistoryDataInfo info) throws Exception {
        String[] names = new String[info.getDataList().size()];
        Double[][] values = new Double[1][names.length];
        for (int i = 0; i < names.length; i++) {
            HistoryDataInfo.DataInfo item = info.getDataList().get(i);
            names[i] = item.getDataName();
            String[] datas = item.getDataValue().split(";");
            values[0][i] = Double.valueOf(datas[2]);
        }
        String url;
        String jsFun = "showLineloseBarChart";
        url = EChartsUtil.createJSFunction(jsFun
                , EChartsUtil.array2JSArray(names)
                , EChartsUtil.array2JSArray(values[0]));
        return url;
    }

    public static String createLinelosePieChart(String[] names, Double[] values) throws Exception {
        String url;
        String jsFun = "showLinelosePieChart";
        url = EChartsUtil.createJSFunction(jsFun
                , EChartsUtil.array2JSArray(names)
                , EChartsUtil.array2JSArray(values));
        return url;
    }
}
