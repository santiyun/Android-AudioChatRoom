package com.ttt.chatroom;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.Environment;

import com.ttt.chatroom.callback.MyTTTRtcEngineEventHandler;
import com.ttt.chatroom.utils.MyLog;
import com.wushuangtech.wstechapi.TTTRtcEngine;

import java.io.File;

public class MainApplication extends Application {

    public MyTTTRtcEngineEventHandler mMyTTTRtcEngineEventHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        //1.设置SDK的回调接收类
        mMyTTTRtcEngineEventHandler = new MyTTTRtcEngineEventHandler(getApplicationContext());
        //2.创建SDK的实例对象
        // 音视频模式用a967ac491e3acf92eed5e1b5ba641ab7 纯音频模式用496e737d22ecccb8cfa780406b9964d0
        TTTRtcEngine mTTTEngine = TTTRtcEngine.create(getApplicationContext(), "496e737d22ecccb8cfa780406b9964d0",
                mMyTTTRtcEngineEventHandler);
        if (mTTTEngine == null) {
            System.exit(0);
        }

        if (!isApkDebugable()) {
            //开启日志
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File externalStorageDirectory = Environment.getExternalStorageDirectory();
                String abs = externalStorageDirectory.toString() + "/3T_ChatRoom_Log";
                mTTTEngine.setLogFile(abs);
            } else {
                MyLog.i("Collection log failed! , No permission!");
            }
        }
    }

    public boolean isApkDebugable() {
        try {
            ApplicationInfo info = this.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception ignored) {
        }
        return false;
    }
}
