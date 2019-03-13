package com.ttt.chatroom;

import com.ttt.chatroom.callback.MyTTTRtcEngineEventHandler;

public class LocalConfig {

    /**
     * 用户选择的角色，麦上用户或麦下用户
     */
    public static int mRole;
    /**
     * 用户的ID
     */
    public static long mUid;
    /**
     * SDK 的回调接收类
     */
    public static MyTTTRtcEngineEventHandler mMyTTTRtcEngineEventHandler;
}
