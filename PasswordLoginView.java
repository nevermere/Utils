package com.linyang.ihelper.ui.login.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.linyang.ihelper.R;
import com.linyang.ihelper.data.db.entity.UserInfo;
import com.linyang.ihelper.util.RxBusUtil;
import com.linyang.ihelper.util.SPUtil;
import com.linyang.ihelper.util.LogUtil;
import com.linyang.ihelper.widget.card.BaseCardView;
import com.linyang.ihelper.common.Const;
import com.linyang.ihelper.util.StringUtil;
import com.linyang.ihelper.util.SystemUtil;
import com.linyang.ihelper.util.ToastUtil;
import com.linyang.ihelper.widget.SwitchView;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 描述:密码登录界面
 * Created by fzJiang on 2017-12-20 10:33
 */
public class PasswordLoginView extends BaseCardView {

    @BindView(R.id.login_name)
    TextView loginName;
    @BindView(R.id.login_password)
    TextView loginPassword;

    @BindView(R.id.login_name_value)
    EditText loginNameValue;// 用户名
    @BindView(R.id.login_password_value)
    EditText loginPasswordValue;// 密码
    @BindView(R.id.save_status)
    SwitchView saveStatus;// 记住登录信息
    @BindView(R.id.login_bt)
    TextView loginBt;// 登录按钮

    public PasswordLoginView(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.login_view_password;
    }

    @Override
    protected void initViews(Context context) {
        // 字体资源库
        Typeface iconfont = Typeface.createFromAsset(context.getAssets(),
                "iconfont/iconfont.ttf");
        loginName.setTypeface(iconfont);
        loginPassword.setTypeface(iconfont);
        //设置输入监听
        loginNameValue.addTextChangedListener(new PasswordTextWatcher());
        // 默认选中
        boolean isSave = SPUtil.getInstance(context).get(Const.SP_SAVE_STATUS, true);
        // 默认选中
        saveStatus.setOpened(isSave);
        // 输入用户信息
        if (isSave) {
            loginNameValue.setText(SPUtil.getInstance(context).get(Const.SP_USER_NAME, ""));
            loginPasswordValue.setText(SPUtil.getInstance(context).get(Const.SP_PASSWORD, ""));
        }
        // 已输入了登录信息,隐藏软键盘
        if (StringUtil.checkInputNotNull(loginNameValue, loginPasswordValue)) {
            SystemUtil.hideFragmentSoft((Activity) context);
        }
    }

    @OnClick(R.id.login_bt)
    public void onViewClicked() {
        // 登陆信息校验
//        if (!StringUtil.checkInputNotNull(loginNameValue, loginPasswordValue)) {
//            ToastUtil.showToast(context, context.getString(R.string.login_value_null_msg));
//        } else {
        // 发送登录请求
        RxBusUtil.get().post(new UserInfo());
//        }
    }

    @Override
    public void onViewResume() {

    }

    @Override
    public void onViewPause() {

    }

    /**
     * 输入监听
     */
    private class PasswordTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            //清空密码
            loginPasswordValue.setText("");
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }
}
