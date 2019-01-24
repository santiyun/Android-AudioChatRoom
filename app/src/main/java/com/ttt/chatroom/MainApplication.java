package com.ttt.chatroom;

import android.app.Application;

import com.ttt.chatroom.callback.MyTTTRtcEngineEventHandler;
import com.wushuangtech.wstechapi.TTTRtcEngine;

public class MainApplication extends Application {

    public MyTTTRtcEngineEventHandler mMyTTTRtcEngineEventHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        //1.设置SDK的回调接收类
        mMyTTTRtcEngineEventHandler = new MyTTTRtcEngineEventHandler(getApplicationContext());
        //2.创建SDK的实例对象
//        TTTRtcEngine mTTTEngine = TTTRtcEngine.create(getApplicationContext(), <这里填申请到的appid>,
        TTTRtcEngine mTTTEngine = TTTRtcEngine.create(getApplicationContext(), "test900572e02867fab8131651339518",
                mMyTTTRtcEngineEventHandler);
        if (mTTTEngine == null) {
            System.exit(0);
        }
    }
}
