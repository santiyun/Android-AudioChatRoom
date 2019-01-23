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
    public boolean mIsSave = true;

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
        if (mIsSave) {
            mIsSaveCallBack = true;
        }
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
    public void onUserEnableVideo(long uid, boolean muted) {
        MyLog.i("wzg", "onUserEnableVideo.... uid : " + uid + " | mute : " + muted);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_USER_MUTE_VIDEO;
        mJniObjs.mUid = uid;
        mJniObjs.mIsEnableVideo = muted;
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
    public void onFirstRemoteVideoFrame(long uid, int width, int height) {
        MyLog.i("wzg", "onFirstRemoteVideoFrame.... uid ： " + uid + " | width : " + width + " | height : " + height);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_REMOVE_FIRST_FRAME_COME;
        mJniObjs.mUid = uid;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onSetSEI(String sei) {
        MyLog.i("wzg", "onSei.... sei : " + sei);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_SEI;
        mJniObjs.mSEI = sei;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onUserMuteAudio(long uid, boolean muted) {
        MyLog.i("wzg", "OnRemoteAudioMuted.... uid : " + uid + " | muted : " + muted + " | mIsSaveCallBack : " + mIsSaveCallBack);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_MUTE_AUDIO;
        mJniObjs.mUid = uid;
        mJniObjs.mIsDisableAudio = muted;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onSpeakingMuted(long uid, boolean muted) {
        MyLog.i("wzg", "onSpeakingMuted.... uid : " + uid + " | muted : " + muted + " | mIsSaveCallBack : " + mIsSaveCallBack);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_SPEAK_MUTE_AUDIO;
        mJniObjs.mUid = uid;
        mJniObjs.mIsDisableAudio = muted;
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

    @Override
    public void onAudioRouteChanged(int routing) {
        MyLog.i("wzg", "onAudioRouteChanged.... routing : " + routing);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_AUDIO_ROUTE;
        mJniObjs.mAudioRoute = routing;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onUserRoleChanged(long userID, int userRole) {
        MyLog.i("wzg", "onUserRoleChanged... userID : " + userID + " userRole : " + userRole);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_USER_ROLE_CHANGED;
        mJniObjs.mUid = userID;
        mJniObjs.mIdentity = userRole;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onScreenRecordTime(int s) {
        MyLog.i("wzg", "onScreenRecordTime: " + s);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_SCREEN_RECORD_TIME;
        mJniObjs.mScreenRecordTime = s;
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
