package com.ttt.chatroom.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.ttt.chatroom.LocalConfig;
import com.ttt.chatroom.R;
import com.ttt.chatroom.utils.MySpUtils;


/**
 * Created by Administrator on 2017-10-11.
 */
public class TestDialog extends Dialog implements View.OnClickListener {

    private Context mContext;
    private EditText mPort, mIP, mPushUrl, mPullUrl;

    public TestDialog(@NonNull Context context) {
        super(context, R.style.NoBackGroundDialog);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_dialog_layout);

        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.width = dm.widthPixels;
            getWindow().setAttributes(lp);
        }

        mIP = findViewById(R.id.ip);
        mPort = findViewById(R.id.port);
        mPushUrl = findViewById(R.id.text_pushurl);
        mPullUrl = findViewById(R.id.text_pullurl);
        if (LocalConfig.mIsTestMode) {
            mPushUrl.setVisibility(View.VISIBLE);
            mPullUrl.setVisibility(View.VISIBLE);
            findViewById(R.id.text_pushurl_tv).setVisibility(View.VISIBLE);
            findViewById(R.id.text_pullurl_tv).setVisibility(View.VISIBLE);
        } else {
            mPushUrl.setVisibility(View.GONE);
            mPullUrl.setVisibility(View.GONE);
            findViewById(R.id.text_pushurl_tv).setVisibility(View.GONE);
            findViewById(R.id.text_pullurl_tv).setVisibility(View.GONE);
        }
        findViewById(R.id.cancel).setOnClickListener(this);
        setServerParams();
    }

    public void setServerParams() {
        if (mIP != null) {
            if (!TextUtils.isEmpty(LocalConfig.mIP)) {
                mIP.setText(LocalConfig.mIP);
            } else {
                mIP.setText("");
            }
        }

        if (mPort != null) {
            if (LocalConfig.mPort != 0) {
                mPort.setText(String.valueOf(LocalConfig.mPort));
            } else {
                mPort.setText("");
            }
        }

        if (mPushUrl != null) {
            Object spObj = MySpUtils.getParam(getContext(), "PushUrl", "");
            if (spObj != null) {
                LocalConfig.mPushUrl = (String) spObj;
            }
            mPushUrl.setText(LocalConfig.mPushUrl);
        }

        if (mPullUrl != null) {
            Object spObj = MySpUtils.getParam(getContext(), "PullUrl", "");
            if (spObj != null) {
                LocalConfig.mPullUrl = (String) spObj;
            }
            mPullUrl.setText(LocalConfig.mPullUrl);
        }
    }

    private void onOKButtonClick() {
        Editable mIPText = mIP.getText();
        if (!TextUtils.isEmpty(mIPText)) {
            LocalConfig.mIP = mIPText.toString();
        } else {
            LocalConfig.mIP = "";
        }

        Editable mPortText = mPort.getText();
        if (!TextUtils.isEmpty(mPortText)) {
            LocalConfig.mPort = Integer.valueOf(mPortText.toString());
        } else {
            LocalConfig.mPort = 0;
        }

        Editable mPushUrlEdit = mPushUrl.getText();
        if (mPushUrlEdit != null) {
            String pushUrl = mPushUrlEdit.toString();
            if (!TextUtils.isEmpty(pushUrl)) {
                LocalConfig.mPushUrl = pushUrl;
            } else {
                LocalConfig.mPushUrl = "";
            }
        } else {
            LocalConfig.mPushUrl = "";
        }
        MySpUtils.setParam(getContext(), "PushUrl", LocalConfig.mPushUrl);

        Editable mPullUrlEdit = mPullUrl.getText();
        if (mPullUrlEdit != null) {
            String pullUrl = mPullUrlEdit.toString();
            if (!TextUtils.isEmpty(pullUrl)) {
                LocalConfig.mPullUrl = pullUrl;
            } else {
                LocalConfig.mPullUrl = "";
            }
        } else {
            LocalConfig.mPullUrl = "";
        }
        MySpUtils.setParam(getContext(), "PullUrl", LocalConfig.mPullUrl);
        this.dismiss();
    }

    @Override
    public void onClick(View v) {
        onOKButtonClick();
    }
}
