package com.ttt.chatroom.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ttt.chatroom.LocalConfig;
import com.ttt.chatroom.LocalConstans;
import com.ttt.chatroom.R;
import com.ttt.chatroom.bean.EnterUserInfo;
import com.ttt.chatroom.bean.JniObjs;
import com.ttt.chatroom.callback.MyTTTRtcEngineEventHandler;
import com.ttt.chatroom.utils.MyLog;
import com.wushuangtech.library.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private MyAdapter mMyAdapter;
    private Button mMainToolSpeak;
    private Button mMainToolMuteLocal;
    private Button mMainToolAudioMix;
    private Button mMainToolAudio;

    private boolean mIsLocalMute;
    private boolean mIsRemoteMute;
    private boolean mIsAudioSpeaker = true;
    private boolean mIsPlayAudioMix;

    private AlertDialog.Builder mErrorExitDialog;
    private MyLocalBroadcastReceiver mLocalBroadcast;
    private List<EnterUserInfo> mDatas;
    private String mAuidoMixFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        //启用本地音量上报功能，可以查看自己和远端用户的音量波动大小
        mTTTEngine.enableAudioVolumeIndication(300, 3);
        copyFileFromAsset();
        SplashActivity.mIsLoging = false;
    }

    @Override
    public void onBackPressed() {
        exitRoom();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mLocalBroadcast);
        } catch (Exception e) {

        }
    }

    private void init() {
        RecyclerView mMainList = findViewById(R.id.main_list);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mMainList.setLayoutManager(manager);
        mMyAdapter = new MyAdapter();
        mMainList.setAdapter(mMyAdapter);
        mMainToolSpeak = findViewById(R.id.main_tool_speak);
        mMainToolSpeak.setOnClickListener(this);
        mMainToolAudio = findViewById(R.id.main_tool_audio);
        mMainToolAudio.setOnClickListener(this);
        mMainToolMuteLocal = findViewById(R.id.main_tool_mute_local);
        mMainToolMuteLocal.setOnClickListener(this);
        mMainToolAudioMix = findViewById(R.id.main_tool_audio_mix);
        mMainToolAudioMix.setOnClickListener(this);
        Button mMainToolMuteOther = findViewById(R.id.main_tool_mute_other);
        mMainToolMuteOther.setOnClickListener(this);
        //退房间的按钮
        findViewById(R.id.main_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitRoom();
            }
        });

        ViewGroup mTitleViewGroup = findViewById(R.id.main_title);
        TypedArray actionbarSizeTypedArray = this.obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
        float actionbarHeight = actionbarSizeTypedArray.getDimension(0, 0);
        actionbarSizeTypedArray.recycle();
        ViewGroup.LayoutParams layoutParams = mTitleViewGroup.getLayoutParams();
        layoutParams.height = (int) (actionbarHeight + getStatusBarHeight(this));
        mTitleViewGroup.setLayoutParams(layoutParams);

        //注册回调函数，接收 SDK 发的广播
        mLocalBroadcast = new MyLocalBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyTTTRtcEngineEventHandler.TAG);
        registerReceiver(mLocalBroadcast, filter);
        LocalConfig.mMyTTTRtcEngineEventHandler.setIsSaveCallBack(false);
        //将自己添加到用户列表中
        mDatas = new ArrayList<>();
        EnterUserInfo localUser = new EnterUserInfo(LocalConfig.mUid);
        if (LocalConfig.mRole == Constants.CLIENT_ROLE_BROADCASTER) {
            adjustUser(true, localUser);
        }
        mMyAdapter.notifyDataSetChanged();

        //创建频道接收 SDK 的异常错误信息信令，弹出提示对话框并退出
        if (mErrorExitDialog == null) {
            mErrorExitDialog = new AlertDialog.Builder(mContext)
                    .setTitle("退出房间提示")
                    .setCancelable(false)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            exitRoom();
                        }
                    });
        }

        if (LocalConfig.mRole == Constants.CLIENT_ROLE_BROADCASTER) {
            mMainToolSpeak.setText("下麦");
            mMainToolMuteLocal.setEnabled(true);
        } else {
            mMainToolSpeak.setText("上麦");
            mMainToolMuteLocal.setEnabled(false);
        }
    }

    private int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.main_tool_speak:
                // 上麦和下麦切换的操作
                if (LocalConfig.mRole == Constants.CLIENT_ROLE_BROADCASTER) {
                    mTTTEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
                    LocalConfig.mRole = Constants.CLIENT_ROLE_AUDIENCE;
                    EnterUserInfo userInfo = new EnterUserInfo(LocalConfig.mUid);
                    adjustUser(false, userInfo);
                    mMainToolSpeak.setText("上麦");
                    if (mIsPlayAudioMix) {
                        mTTTEngine.stopAudioMixing();
                        mMainToolAudioMix.setText("伴奏");
                        mIsPlayAudioMix = !mIsPlayAudioMix;
                    }
                } else {
                    mTTTEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
                    LocalConfig.mRole = Constants.CLIENT_ROLE_BROADCASTER;
                    adjustUser(true, LocalConfig.mUid);
                    mMainToolSpeak.setText("下麦");
                }
                break;
            case R.id.main_tool_audio:
                // 扬声器和听筒、耳机切换的操作
                mIsAudioSpeaker = !mIsAudioSpeaker;
                mTTTEngine.setEnableSpeakerphone(mIsAudioSpeaker);
                if (mIsAudioSpeaker) {
                    mMainToolAudio.setText("外放");
                } else {
                    mMainToolAudio.setText("听筒");
                }
                break;
            case R.id.main_tool_mute_local:
                // 本地静音的操作，该操作实现不发送自己的音频流。麦下用户默认是静音。
                if (LocalConfig.mRole == Constants.CLIENT_ROLE_BROADCASTER) {
                    mIsLocalMute = !mIsLocalMute;
                    mTTTEngine.muteLocalAudioStream(mIsLocalMute);
                    for (int i = 0; i < mDatas.size(); i++) {
                        EnterUserInfo enterUserInfo = mDatas.get(i);
                        if (LocalConfig.mUid == enterUserInfo.uid) {
                            enterUserInfo.audioMute = mIsLocalMute;
                            mMyAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
                break;
            case R.id.main_tool_mute_other:
                // 静音所有远端用户的声音，该操作实现不接收某个远端用户的音频流
                if (LocalConfig.mRole == Constants.CLIENT_ROLE_BROADCASTER) {
                    mIsRemoteMute = !mIsRemoteMute;
                    for (EnterUserInfo mData : mDatas) {
                        if (mData.uid != LocalConfig.mUid) {
                            mData.audioMute = mIsRemoteMute;
                            mTTTEngine.muteRemoteAudioStream(mData.uid, mIsRemoteMute);
                        }
                    }
                    mMyAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.main_tool_audio_mix:
                // 播放或停止伴奏，Demo 中只有麦上用户可以操作伴奏功能。
                if (LocalConfig.mRole == Constants.CLIENT_ROLE_BROADCASTER) {
                    if (TextUtils.isEmpty(mAuidoMixFilePath)) {
                        return;
                    }

                    File temp = new File(mAuidoMixFilePath);
                    if (!temp.exists()) {
                        return;
                    }

                    mIsPlayAudioMix = !mIsPlayAudioMix;
                    if (mIsPlayAudioMix) {
                        mTTTEngine.startAudioMixing(mAuidoMixFilePath, false, false, 1);
                        mMainToolAudioMix.setText("停止伴奏");
                    } else {
                        mTTTEngine.stopAudioMixing();
                        mMainToolAudioMix.setText("伴奏");
                    }
                }
                break;
        }
    }

    /**
     * 执行退房间相关操作
     */
    public void exitRoom() {
        // 若正在播放伴奏，则停止播放。
        mTTTEngine.stopAudioMixing();
        // 退出房间
        mTTTEngine.leaveChannel();
        finish();
    }

    public synchronized void adjustUser(boolean mIsAdd, long uid) {
        if (mIsAdd) {
            EnterUserInfo userInfo = new EnterUserInfo(uid);
            mDatas.add(userInfo);
        } else {
            removeUser(uid);
        }
        mMyAdapter.notifyDataSetChanged();
    }

    public synchronized void adjustUser(boolean mIsAdd, EnterUserInfo info) {
        if (mIsAdd) {
            mDatas.add(info);
        } else {
            removeUser(info.uid);
        }
        mMyAdapter.notifyDataSetChanged();
    }

    private void removeUser(long uid) {
        EnterUserInfo remoed = null;
        for (EnterUserInfo mData : mDatas) {
            if (uid == mData.uid) {
                remoed = mData;
                break;
            }
        }

        if (remoed != null) {
            mDatas.remove(remoed);
        }
    }

    private void copyFileFromAsset() {
        File externalFilesDir = getFilesDir();
        if (externalFilesDir == null) {
            return;
        }

        mAuidoMixFilePath = externalFilesDir.getAbsolutePath() + "/3T_MixFile.mp3";
        File temp = new File(mAuidoMixFilePath);
        if (temp.exists()) {
            return;
        }

        AssetManager mAssetManager = getResources().getAssets();
        FileOutputStream mFileOutputStream = null;
        InputStream mInputStream = null;
        try {
            mInputStream = mAssetManager.open("Life.mp3");
            mFileOutputStream = new FileOutputStream(mAuidoMixFilePath);
            byte[] buf = new byte[1024];
            while (mInputStream.read(buf) != -1) {
                mFileOutputStream.write(buf, 0, buf.length);
                mFileOutputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (mInputStream != null) {
                try {
                    mInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (mFileOutputStream != null) {
                try {
                    mFileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MyLocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MyTTTRtcEngineEventHandler.TAG.equals(action)) {
                JniObjs mJniObjs = intent.getParcelableExtra(MyTTTRtcEngineEventHandler.MSG_TAG);
                switch (mJniObjs.mJniType) {
                    case LocalConstans.CALL_BACK_ON_USER_KICK: //接收 SDK 运行时异常退房间的信令
                        int errorType = mJniObjs.mErrorType;
                        String message = "";
                        if (errorType == Constants.ERROR_KICK_BY_HOST) {
                            message = getResources().getString(R.string.ttt_error_exit_kicked);
                        } else if (errorType == Constants.ERROR_KICK_BY_PUSHRTMPFAILED) {
                            message = getResources().getString(R.string.ttt_error_exit_push_rtmp_failed);
                        } else if (errorType == Constants.ERROR_KICK_BY_SERVEROVERLOAD) {
                            message = getResources().getString(R.string.ttt_error_exit_server_overload);
                        } else if (errorType == Constants.ERROR_KICK_BY_MASTER_EXIT) {
                            message = getResources().getString(R.string.ttt_error_exit_anchor_exited);
                        } else if (errorType == Constants.ERROR_KICK_BY_RELOGIN) {
                            message = getResources().getString(R.string.ttt_error_exit_relogin);
                        } else if (errorType == Constants.ERROR_KICK_BY_NEWCHAIRENTER) {
                            message = getResources().getString(R.string.ttt_error_exit_other_anchor_enter);
                        } else if (errorType == Constants.ERROR_KICK_BY_NOAUDIODATA) {
                            message = getResources().getString(R.string.ttt_error_exit_noaudio_upload);
                        } else if (errorType == Constants.ERROR_KICK_BY_NOVIDEODATA) {
                            message = getResources().getString(R.string.ttt_error_exit_novideo_upload);
                        } else if (errorType == Constants.ERROR_TOKEN_EXPIRED) {
                            message = getResources().getString(R.string.ttt_error_exit_token_expired);
                        }
                        if (!TextUtils.isEmpty(message)) {
                            mErrorExitDialog.setMessage("退出原因: " + message);
                            mErrorExitDialog.show();
                        }
                        break;
                    case LocalConstans.CALL_BACK_ON_CONNECTLOST: //接收 SDK 断开网络的信令
                        String connectLostMsg = getResources().getString(R.string.ttt_error_network_disconnected);
                        mErrorExitDialog.setMessage("退出原因: " + connectLostMsg);
                        mErrorExitDialog.show();
                        break;
                    case LocalConstans.CALL_BACK_ON_USER_JOIN: //接收其他用户加入当前频道的信令
                        long uid = mJniObjs.mUid;
                        int identity = mJniObjs.mIdentity;
                        MyLog.d("UI onReceive CALL_BACK_ON_USER_JOIN... uid : " + uid + " identity : " + identity);
                        EnterUserInfo userInfo = new EnterUserInfo(uid);
                        if (identity == Constants.CLIENT_ROLE_BROADCASTER) {
                            adjustUser(true, userInfo);
                        }
                        break;
                    case LocalConstans.CALL_BACK_ON_USER_OFFLINE: //接收其他用户离开当前频道的信令
                        long offLineUserID = mJniObjs.mUid;
                        adjustUser(false, offLineUserID);
                        break;
                    case LocalConstans.CALL_BACK_ON_AUDIO_VOLUME_INDICATION:  //接收自己和其他用户的音量大小值
                        long mUid = mJniObjs.mUid;
                        int mAudioLevel = mJniObjs.mAudioLevel;
                        for (int i = 0; i < mDatas.size(); i++) {
                            EnterUserInfo enterUserInfo = mDatas.get(i);
                            if (mUid == enterUserInfo.uid) {
                                enterUserInfo.volumeNum = mAudioLevel;
                                mMyAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                        break;
                }
            }
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View rootView = View.inflate(mContext, R.layout.adapter_main_item, null);
            return new MyViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
            EnterUserInfo enterUserInfo = mDatas.get(i);
            String name;
            if (enterUserInfo.uid == LocalConfig.mUid) {
                myViewHolder.mItemIcon.setImageResource(R.drawable.local_icon);
                name = String.valueOf(enterUserInfo.uid) + "(我)";
            } else {
                myViewHolder.mItemIcon.setImageResource(R.drawable.remote_icon);
                name = String.valueOf(enterUserInfo.uid);
            }

            myViewHolder.mItemName.setText(name);
            if (enterUserInfo.audioMute) {
                myViewHolder.mItemMute.setImageResource(R.drawable.audio_volume_mute);
            } else {
                myViewHolder.mItemMute.setImageResource(R.drawable.audio_volume);
            }

            String volumeNum = String.format(getResources().
                    getString(R.string.main_item_volume), String.valueOf(enterUserInfo.volumeNum));
            myViewHolder.mItemVolume.setText(volumeNum);
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView mItemIcon;
        TextView mItemName;
        ImageView mItemMute;
        TextView mItemVolume;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mItemIcon = itemView.findViewById(R.id.item_icon);
            this.mItemName = itemView.findViewById(R.id.item_name);
            this.mItemMute = itemView.findViewById(R.id.item_mute);
            this.mItemVolume = itemView.findViewById(R.id.item_volume);
        }
    }
}
