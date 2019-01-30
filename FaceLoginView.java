package com.linyang.ihelper.ui.login.view;

import android.content.Context;
import android.widget.TextView;

import com.linyang.ihelper.R;
import com.linyang.ihelper.widget.card.BaseCardView;

import butterknife.BindView;
import butterknife.OnClick;
import cn.face.camera.AutoFocusCameraPreview;

/**
 * 描述:人脸登录界面
 * Created by fzJiang on 2017-12-20 16:28
 */
public class FaceLoginView extends BaseCardView {

    @BindView(R.id.start_face_bt)
    TextView startFaceBt;
    @BindView(R.id.shot_view)
    AutoFocusCameraPreview shotView;

    public FaceLoginView(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.login_view_face;
    }

    @Override
    protected void initViews(Context context) {

    }

    @OnClick(R.id.start_face_bt)
    public void onViewClicked() {

    }

    @Override
    public void onViewResume() {

    }

    @Override
    public void onViewPause() {

    }
}
