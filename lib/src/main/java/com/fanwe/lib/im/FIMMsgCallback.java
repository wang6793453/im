package com.fanwe.lib.im;

/**
 * Created by zhengjun on 2017/11/22.
 */
public interface FIMMsgCallback
{
    String filterMsgByPeer();

    void onReceiveMsg(FIMMsg msg);
}
