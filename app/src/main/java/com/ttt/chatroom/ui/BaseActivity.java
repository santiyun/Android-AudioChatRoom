package com.ttt.chatroom.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.jaeger.library.StatusBarUtil;
import com.wushuangtech.wstechapi.TTTRtcEngine;

/**
 * Created by wangzhiguo on 17/10/12.
 */

public class BaseActivity extends AppCompatActivity {

    protected TTTRtcEngine mTTTEngine;
    protected Context mContext;
    protected ActionBar mActionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.hide();
        }
        //状态栏透明
        StatusBarUtil.setTranslucent(this);
        //获取上下文
        mContext = this;
        //获取SDK实例对象
        mTTTEngine = TTTRtcEngine.getInstance();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
