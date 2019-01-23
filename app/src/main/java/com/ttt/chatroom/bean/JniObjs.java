package com.ttt.chatroom.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wangzhiguo on 17/10/13.
 */

public class JniObjs implements Parcelable {

    public int mJniType;
    public long mUid;
    public int mIdentity;
    public int mReason;
    public boolean mIsEnableVideo;
    public boolean mIsDisableAudio;
    public int mAudioLevel;
    public String mChannelName;
    public String mSEI;
    public int mErrorType;
    public int mScreenRecordTime;
    public int mAudioRoute;

    public JniObjs() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mJniType);
        dest.writeLong(this.mUid);
        dest.writeInt(this.mIdentity);
        dest.writeInt(this.mReason);
        dest.writeByte(this.mIsEnableVideo ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mIsDisableAudio ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mAudioLevel);
        dest.writeString(this.mChannelName);
        dest.writeString(this.mSEI);
        dest.writeInt(this.mErrorType);
        dest.writeInt(this.mScreenRecordTime);
        dest.writeInt(this.mAudioRoute);
    }

    protected JniObjs(Parcel in) {
        this.mJniType = in.readInt();
        this.mUid = in.readLong();
        this.mIdentity = in.readInt();
        this.mReason = in.readInt();
        this.mIsEnableVideo = in.readByte() != 0;
        this.mIsDisableAudio = in.readByte() != 0;
        this.mAudioLevel = in.readInt();
        this.mChannelName = in.readString();
        this.mSEI = in.readString();
        this.mErrorType = in.readInt();
        this.mScreenRecordTime = in.readInt();
        this.mAudioRoute = in.readInt();
    }

    public static final Creator<JniObjs> CREATOR = new Creator<JniObjs>() {
        @Override
        public JniObjs createFromParcel(Parcel source) {
            return new JniObjs(source);
        }

        @Override
        public JniObjs[] newArray(int size) {
            return new JniObjs[size];
        }
    };
}
