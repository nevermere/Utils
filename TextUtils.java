package com.ly.neuter.core.utils;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fzJiang on 2016-5-9.
 */
public class TextUtils {

    //标红搜索关键字
    public static void setSpecifiedTextsColor(TextView view, String text, String specifiedTexts, int color) {
        List sTextsStartList = new ArrayList();

        int sTextLength = specifiedTexts.length();
        String temp = text;
        int lengthFront = 0;//记录被找出后前面的字段的长度
        int start;
        do {
            start = temp.indexOf(specifiedTexts);

            if (start != -1) {
                start = start + lengthFront;
                sTextsStartList.add(start);
                lengthFront = start + sTextLength;
                temp = text.substring(lengthFront);
            }

        } while (start != -1);

        SpannableStringBuilder styledText = new SpannableStringBuilder(text);


        for (int i = 0; i < sTextsStartList.size(); i++) {
            styledText.setSpan(
                    new ForegroundColorSpan(color),
                    i,
                    i + sTextLength,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        view.setText(styledText);
    }
}
