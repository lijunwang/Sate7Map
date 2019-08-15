package com.sate7.geo.map.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;

import com.sate7.geo.map.R;

import razerdp.basepopup.BasePopupWindow;

public class SlideFromBottomPopup extends BasePopupWindow implements View.OnClickListener {

    private TextView mTitle;
    private OnPopupClickListener mListener;
    public interface OnPopupClickListener{
        void onSureClick();
    }
    public SlideFromBottomPopup(Context context,OnPopupClickListener listener) {
        super(context);
        setPopupGravity(Gravity.BOTTOM);
        bindEvent();
        setOutSideTouchable(false);
        mListener = listener;
    }

    @Override
    protected Animation onCreateShowAnimation() {
        return getTranslateVerticalAnimation(1f, 0, 500);
    }

    @Override
    protected Animation onCreateDismissAnimation() {
        return getTranslateVerticalAnimation(0, 1f, 500);
    }

    @Override
    public View onCreateContentView() {
        return createPopupById(R.layout.popup_slide_from_bottom);
    }

    private void bindEvent() {
        findViewById(R.id.popSure).setOnClickListener(this);
        findViewById(R.id.popCancel).setOnClickListener(this);
        mTitle = findViewById(R.id.popTitle);
    }

    public void setTitle(String title){
        mTitle.setText(title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.popSure:
                mListener.onSureClick();
                dismiss();
                break;
            case R.id.popCancel:
                dismiss();
                break;
            default:
                break;
        }

    }
}
