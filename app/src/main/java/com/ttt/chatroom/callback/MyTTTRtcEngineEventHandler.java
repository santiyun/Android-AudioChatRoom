package com.ttt.chatroom.callback;

import android.content.Context;
import android.content.Intent;

import com.ttt.chatroom.LocalConstans;
import com.ttt.chatroom.bean.JniObjs;
import com.ttt.chatroom.utils.MyLog;
import com.wushuangtech.wstechapi.TTTRtcEngineEventHandler;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by wangzhiguo on 17/10/24.
 */

public class MyTTTRtcEngineEventHandler extends TTTRtcEngineEventHandler {

    public static final String TAG = "MyTTTRtcEngineEventHandlerCR";
    public static final String MSG_TAG = "MyTTTRtcEngineEventHandlerMSGCR";
    private boolean mIsSaveCallBack;
    private List<JniObjs> mSaveCallBack;
    private Context mContext;

    public MyTTTRtcEngineEventHandler(Context mContext) {
        this.mContext = mContext;
        mSaveCallBack = new ArrayList<>();
    }

    @Override
    public void onJoinChannelSuccess(String channel, long uid) {
        MyLog.i("wzg", "onJoinChannelSuccess.... channel ： " + channel + " | uid : " + uid);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_ENTER_ROOM;
        mJniObjs.mChannelName = channel;
        mJniObjs.mUid = uid;
        sendMessage(mJniObjs);
        mIsSaveCallBack = true;
    }

    @Override
    public void onError(final int errorType) {
        MyLog.i("wzg", "onError.... errorType ： " + errorType + "mIsSaveCallBack : " + mIsSaveCallBack);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_ERROR;
        mJniObjs.mErrorType = errorType;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onUserKicked(long uid, int reason) {
        super.onUserKicked(uid, reason);
        MyLog.i("wzg", "onUserKicked.... uid ： " + uid + "reason : " + reason);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_USER_KICK;
        mJniObjs.mErrorType = reason;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onUserJoined(long nUserId, int identity) {
        MyLog.i("wzg", "onUserJoined.... nUserId ： " + nUserId + " | identity : " + identity
                + " | mIsSaveCallBack : " + mIsSaveCallBack);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_USER_JOIN;
        mJniObjs.mUid = nUserId;
        mJniObjs.mIdentity = identity;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onUserOffline(long nUserId, int reason) {
        MyLog.i("wzg", "onUserOffline.... nUserId ： " + nUserId + " | reason : " + reason);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_USER_OFFLINE;
        mJniObjs.mUid = nUserId;
        mJniObjs.mReason = reason;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onAudioVolumeIndication(long nUserID, int audioLevel, int audioLevelFullRange) {
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_AUDIO_VOLUME_INDICATION;
        mJniObjs.mUid = nUserID;
        mJniObjs.mAudioLevel = audioLevel;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onReconnectServerFailed() {
        MyLog.i("wzg", "onReconnectServerFailed.... ");
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_CONNECTLOST;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    private void sendMessage(JniObjs mJniObjs) {
        Intent i = new Intent();
        i.setAction(TAG);
        i.putExtra(MSG_TAG, mJniObjs);
        i.setExtrasClassLoader(JniObjs.class.getClassLoader());
        mContext.sendBroadcast(i);
    }

    public void setIsSaveCallBack(boolean mIsSaveCallBack) {
        this.mIsSaveCallBack = mIsSaveCallBack;
        if (!mIsSaveCallBack) {
            for (int i = 0; i < mSaveCallBack.size(); i++) {
                sendMessage(mSaveCallBack.get(i));
            }
            mSaveCallBack.clear();
        }
    }

    private void saveCallBack(JniObjs mJniObjs) {
        if (mIsSaveCallBack) {
            mSaveCallBack.add(mJniObjs);
        }
    }
}
