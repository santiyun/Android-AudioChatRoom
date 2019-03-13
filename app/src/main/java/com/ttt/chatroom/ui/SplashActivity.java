package com.ttt.chatroom.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ttt.chatroom.LocalConfig;
import com.ttt.chatroom.LocalConstans;
import com.ttt.chatroom.R;
import com.ttt.chatroom.bean.JniObjs;
import com.ttt.chatroom.callback.MyTTTRtcEngineEventHandler;
import com.ttt.chatroom.utils.MySpUtils;
import com.wushuangtech.library.Constants;
import com.wushuangtech.wstechapi.TTTRtcEngine;
import com.wushuangtech.wstechapi.model.PublisherConfiguration;
import com.yanzhenjie.permission.AndPermission;

import java.io.File;
import java.util.Random;

public class SplashActivity extends BaseActivity {

    private ProgressDialog mDialog;
    public static boolean mIsLoging;
    private EditText mRoomIDET;
    private MyLocalBroadcastReceiver mLocalBroadcast;
    private RadioButton mBroadcastBT, mAuthorBT;
    private String mRoomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //1.首先需要申请 SDK 运行的必要权限。
        AndPermission.with(this)
                .permission(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE)
                .start();

        //2.创建 SDK 的回调接收类对象，接收 SDK 回调的所有信令。
        LocalConfig.mMyTTTRtcEngineEventHandler = new MyTTTRtcEngineEventHandler(getApplicationContext());
        //3.创建 SDK 的实例对象，用于执行 SDK 各项功能。此函数仅需要调用一次即可。
        mTTTEngine = TTTRtcEngine.create(getApplicationContext(), <这里填写APPID>, false,
                LocalConfig.mMyTTTRtcEngineEventHandler);
        if (mTTTEngine == null) {
            System.exit(0);
        }
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mLocalBroadcast);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //删除用户手机sd卡上的伴奏文件
        String mAuidoMixFilePath = Environment.getExternalStorageDirectory() + "/3T_MixFile.mp3";
        File temp = new File(mAuidoMixFilePath);
        if (temp.exists()) {
            temp.delete();
        }
    }


    private void init() {
        mAuthorBT = findViewById(R.id.vice);
        mBroadcastBT = findViewById(R.id.broadcast);
        mRoomIDET = findViewById(R.id.room_id);
        TextView mVersion = findViewById(R.id.version);
        String string = getResources().getString(R.string.version_info);
        String result = String.format(string, TTTRtcEngine.getInstance().getSdkVersion());
        mVersion.setText(result);

        //读取保存的数据
        Object roomIDObj = MySpUtils.getParam(this, "RoomID", "");
        if (roomIDObj != null) {
            mRoomIDET.setText((String) roomIDObj);
        }
        //注册广播，接收 SDK 回调的信令。
        mLocalBroadcast = new MyLocalBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyTTTRtcEngineEventHandler.TAG);
        registerReceiver(mLocalBroadcast, filter);
        //创建进度对话框
        mDialog = new ProgressDialog(this);
        mDialog.setTitle("");
        mDialog.setCancelable(false);
        mDialog.setMessage(getString(R.string.ttt_loading_channel));
    }

    public void onClickRoleButton(View v) {
        mBroadcastBT.setChecked(false);
        mAuthorBT.setChecked(false);
        ((RadioButton) v).setChecked(true);
    }

    /**
     * 配置 SDK 相关信息
     */
    private void initSDK() {
        //4.设置频道模式为通信模式。
        mTTTEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
        //5.设置麦上用户或麦下用户。
        if (mBroadcastBT.isChecked()) {
            LocalConfig.mRole = Constants.CLIENT_ROLE_BROADCASTER;
        } else if (mAuthorBT.isChecked()) {
            LocalConfig.mRole = Constants.CLIENT_ROLE_AUDIENCE;
        }
        mTTTEngine.setClientRole(LocalConfig.mRole);
        //6.设置本地视频预览分辨率为360P，此步骤为可选操作，默认就是360P。
        mTTTEngine.setVideoProfile(Constants.TTTRTC_VIDEOPROFILE_360P, false);
        //7.设置音频推流地址。
        PublisherConfiguration mPublisherConfiguration = new PublisherConfiguration();
        mPublisherConfiguration.setPushUrl("rtmp://127.0.0.1/live/" + mRoomName);
        mTTTEngine.configPublisher(mPublisherConfiguration);
    }

    public void onClickEnterButton(View v) {
        mRoomName = mRoomIDET.getText().toString().trim();
        if (TextUtils.isEmpty(mRoomName)) {
            Toast.makeText(this, R.string.hint_channel_name_limit, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.getTrimmedLength(mRoomName) > 18) {
            Toast.makeText(this, R.string.hint_channel_name_limit, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mIsLoging) {
            return;
        }
        mIsLoging = true;
        //随机生成用户ID
        Random mRandom = new Random();
        long mUserId = mRandom.nextInt(999999);
        LocalConfig.mUid = mUserId;
        //保存配置
        MySpUtils.setParam(this, "RoomID", mRoomName);
        //配置 SDK
        initSDK();
        // 开始加入频道
        mTTTEngine.joinChannel("", mRoomName, mUserId);
        mDialog.setMessage(getString(R.string.ttt_loading_channel));
        mDialog.show();
    }

    private class MyLocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MyTTTRtcEngineEventHandler.TAG.equals(action)) {
                JniObjs mJniObjs = intent.getParcelableExtra(MyTTTRtcEngineEventHandler.MSG_TAG);
                switch (mJniObjs.mJniType) {
                    case LocalConstans.CALL_BACK_ON_ENTER_ROOM: //接收进房间成功的回调信令
                        //进行界面跳转，进入主界面
                        Intent activityIntent = new Intent();
                        activityIntent.setClass(SplashActivity.this, MainActivity.class);
                        startActivity(activityIntent);
                        mDialog.dismiss();
                        mIsLoging = false;
                        break;
                    case LocalConstans.CALL_BACK_ON_ERROR: //接收进房间失败的回调信令，具体的错误信息，请看 Toast 的提示信息
                        mIsLoging = false;
                        mDialog.dismiss();
                        int errorType = mJniObjs.mErrorType;
                        if (errorType == Constants.ERROR_ENTER_ROOM_INVALIDCHANNELNAME) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.error_room_format), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_TIMEOUT) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.error_timeout), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_VERIFY_FAILED) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.error_token), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_BAD_VERSION) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.error_version), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_CONNECT_FAILED) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.error_unconnect), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_NOEXIST) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.error_room_no_exist), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_SERVER_VERIFY_FAILED) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.error_verification_code), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_UNKNOW) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.error_room_unknow), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }
    }
}
