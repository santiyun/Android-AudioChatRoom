package com.ttt.chatroom.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
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
import com.ttt.chatroom.dialog.TestDialog;
import com.ttt.chatroom.utils.MyLog;
import com.ttt.chatroom.utils.MySpUtils;
import com.wushuangtech.bean.VideoCompositingLayout;
import com.wushuangtech.library.Constants;
import com.wushuangtech.wstechapi.TTTRtcEngine;
import com.wushuangtech.wstechapi.model.PublisherConfiguration;
import com.yanzhenjie.permission.AndPermission;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.wushuangtech.library.Constants.CLIENT_ROLE_BROADCASTER;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_BAD_VERSION;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_NOEXIST;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_TIMEOUT;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_UNKNOW;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_VERIFY_FAILED;

public class SplashActivity extends BaseActivity {

    private ProgressDialog mDialog;
    public boolean mIsLoging;
    private EditText mRoomIDET;
    private MyLocalBroadcastReceiver mLocalBroadcast;
    private String mRoomName;
    private long mUserId;
    private RadioButton mBroadcastBT, mAuthorBT;
    private int mRole = CLIENT_ROLE_BROADCASTER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // 权限申请
        AndPermission.with(this)
                .permission(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE)
                .start();

        init();
        initTestCode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyLog.d("SplashActivity onDestroy....");
        TTTRtcEngine.destroy();
        try {
            unregisterReceiver(mLocalBroadcast);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String mAuidoMixFilePath = Environment.getExternalStorageDirectory() + "/3T_MixFile.mp3";
        File temp = new File(mAuidoMixFilePath);
        if (temp.exists()) {
            temp.delete();
        }
    }

    private void initView() {
        mAuthorBT = findViewById(R.id.vice);
        mBroadcastBT = findViewById(R.id.broadcast);
        mRoomIDET = findViewById(R.id.room_id);
        TextView mVersion = findViewById(R.id.version);
        String string = getResources().getString(R.string.version_info);
        String result = String.format(string, TTTRtcEngine.getInstance().getVersion());
        mVersion.setText(result);
//        if (!isZh(this)) {
//            mImageFlagIV.setImageResource(R.drawable.logo_en);
//        } else {
//            mImageFlagIV.setImageResource(R.drawable.logo);
//        }
    }

    private void init() {
        initView();
        readSp();
        // 注册回调函数接收的广播
        mLocalBroadcast = new MyLocalBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyTTTRtcEngineEventHandler.TAG);
        registerReceiver(mLocalBroadcast, filter);
        MyLog.d("SplashActivity onCreate.... model : " + Build.MODEL);
        mDialog = new ProgressDialog(this);
        mDialog.setTitle("");
        mDialog.setCancelable(false);
        mDialog.setMessage(getString(R.string.ttt_loading_channel));
    }

    private void readSp() {
        // 读取保存的数据
        Object roomIDObj = MySpUtils.getParam(this, "RoomID", "");
        if (roomIDObj != null) {
            mRoomIDET.setText((String) roomIDObj);
        }

        Object spObj = MySpUtils.getParam(this, "PushUrl", "");
        if (spObj != null) {
            LocalConfig.mPushUrl = (String) spObj;
        }

        Object spObj2 = MySpUtils.getParam(this, "PullUrl", "");
        if (spObj2 != null) {
            LocalConfig.mPullUrl = (String) spObj;
        }
    }

    public void onClickRoleButton(View v) {
        mBroadcastBT.setChecked(false);
        mAuthorBT.setChecked(false);

        ((RadioButton) v).setChecked(true);
        switch (v.getId()) {
            case R.id.broadcast:
                mRole = Constants.CLIENT_ROLE_BROADCASTER;
                break;
            case R.id.vice:
                mRole = Constants.CLIENT_ROLE_AUDIENCE;
                break;
        }
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

        Random mRandom = new Random();
        mUserId = mRandom.nextInt(999999);
        // 保存配置
        MySpUtils.setParam(this, "RoomID", mRoomName);
        // 设置推流地址
        initSDK();
        LocalConfig.mUid = mUserId;
        // 开始加入频道
        mTTTEngine.joinChannel("", mRoomName, mUserId);
        mDialog.setMessage(getString(R.string.ttt_loading_channel));
        mDialog.show();
    }

    private void initSDK() {
        // 设置频道模式为通信模式
        mTTTEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
        // 设置麦上或麦下
        mTTTEngine.setClientRole(mRole, null);
        LocalConfig.mRole = mRole;
        mTTTEngine.setVideoProfile(Constants.TTTRTC_VIDEOPROFILE_360P, false);
        PublisherConfiguration mPublisherConfiguration = new PublisherConfiguration();
        if (LocalConfig.mIsTestMode) {
            mPublisherConfiguration.setPushUrl(LocalConfig.mPushUrl);
        } else {
            mPublisherConfiguration.setPushUrl("rtmp://127.0.0.1/live/" + mRoomName);
        }
        mTTTEngine.configPublisher(mPublisherConfiguration);
        MyLog.d("设置服务器地址 : " + LocalConfig.mIP);
        mTTTEngine.setServerIp(LocalConfig.mIP, LocalConfig.mPort);
    }

    public VideoCompositingLayout.Region[] buildRemoteLayoutLocation() {
        List<VideoCompositingLayout.Region> tempList = new ArrayList<>();

        VideoCompositingLayout.Region mRegion = new VideoCompositingLayout.Region();
        mRegion.mUserID = mUserId;
        mRegion.x = 0;
        mRegion.y = 0;
        mRegion.width = 1;
        mRegion.height = 1;
        mRegion.zOrder = 0;
        tempList.add(mRegion);

        VideoCompositingLayout.Region[] mRegions = new VideoCompositingLayout.Region[tempList.size()];
        for (int k = 0; k < tempList.size(); k++) {
            VideoCompositingLayout.Region region = tempList.get(k);
            mRegions[k] = region;
        }
        return mRegions;
    }

    private class MyLocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MyTTTRtcEngineEventHandler.TAG.equals(action)) {
                JniObjs mJniObjs = intent.getParcelableExtra(MyTTTRtcEngineEventHandler.MSG_TAG);
                switch (mJniObjs.mJniType) {
                    case LocalConstans.CALL_BACK_ON_ENTER_ROOM:
                        //界面跳转
                        Intent activityIntent = new Intent();
                        activityIntent.putExtra("ROOM_ID", Long.parseLong(mRoomName));
                        activityIntent.putExtra("USER_ID", mUserId);
                        activityIntent.putExtra("ROLE", mRole);
                        activityIntent.setClass(SplashActivity.this, MainActivity.class);
                        startActivity(activityIntent);
                        mDialog.dismiss();
                        mIsLoging = false;
                        break;
                    case LocalConstans.CALL_BACK_ON_ERROR:
                        mIsLoging = false;
                        mDialog.dismiss();
                        final int errorType = mJniObjs.mErrorType;
                        SplashActivity.this.runOnUiThread(() -> {
                            MyLog.d("onReceive CALL_BACK_ON_ERROR errorType : " + errorType);
                            if (errorType == ERROR_ENTER_ROOM_TIMEOUT) {
                                Toast.makeText(mContext, mContext.getResources().getString(R.string.error_timeout), Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_UNKNOW) {
                                Toast.makeText(mContext, mContext.getResources().getString(R.string.error_unconnect), Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_VERIFY_FAILED) {
                                Toast.makeText(mContext, mContext.getResources().getString(R.string.error_verification_code), Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_BAD_VERSION) {
                                Toast.makeText(mContext, mContext.getResources().getString(R.string.error_version), Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_NOEXIST) {
                                Toast.makeText(mContext, mContext.getResources().getString(R.string.error_noroom), Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                }
            }
        }
    }

//    public static boolean isZh(Context context) {
//        Locale locale = context.getResources().getConfiguration().locale;
//        String language = locale.getLanguage();
//        return language.endsWith("zh");
//    }

    TestDialog mTestDialog;

    // -----test code , ignore-----
    public void initTestCode() {
        mTestDialog = new TestDialog(mContext);
        mTestDialog.setCanceledOnTouchOutside(false);
    }

    public void onTestButtonClick(View v) {
        mTestDialog.setServerParams();
        mTestDialog.show();
    }
    // -----test code , ignore-----
}
