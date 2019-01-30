package com.linyang.ihelper.widget.card;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;

import com.linyang.ihelper.util.FastBlur;

import butterknife.ButterKnife;

/**
 * 描述:卡片式布局基础类
 * Created by fzJiang on 2017-12-20 15:52
 */
public abstract class CustomCardView extends CardView {

    protected Context context;
    private int width;
    private int height;

    public CustomCardView(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * 构造器,获取界面
     *
     * @return 当前界面句柄
     */
    public CustomCardView builder() {
        // 初始化界面元素及参数
        init(context);
        // 界面其他元素初始化
        initViews(context);
        return this;
    }

    /**
     * 初始化界面元素及参数
     */
    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(getLayoutId(), this, true);
        ButterKnife.bind(this, view);
        // 参数设置
        this.setRadius(10f);
        this.setCardElevation(10f);
    }

    public CustomCardView setViewBackground(int color) {
        this.setBackgroundColor(color);
        return this;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 设置高斯模糊背景图片
     *
     * @param bitmap 背景图片
     * @return
     */
    public CustomCardView setViewBlur(Bitmap bitmap) {
        // 将设置的背景图片进行高斯模糊处理
        Bitmap blueBitmap = FastBlur.blurBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight());
        FastBlur.blur(context, blueBitmap, this);
        return this;
    }

    /**
     * layout资源
     *
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * 界面其他元素初始化
     *
     * @param context
     */
    protected abstract void initViews(Context context);
}
