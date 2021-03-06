package com.sd.lib.im;

import android.text.TextUtils;
import android.util.Log;

import com.sd.lib.im.callback.FIMMsgCallback;
import com.sd.lib.im.callback.FIMResultCallback;
import com.sd.lib.im.conversation.FIMConversationType;
import com.sd.lib.im.msg.FIMMsg;
import com.sd.lib.im.msg.FIMMsgData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * IM管理基类
 */
public class FIMManager
{
    private static FIMManager sInstance;

    private FIMHandler mIMHandler;

    private final Map<String, CallbackInfo> mMapCallback = new HashMap<>();
    private final List<FIMMsgCallback> mListMsgCallback = new CopyOnWriteArrayList<>();

    private boolean mIsDebug;

    private FIMManager()
    {
    }

    public static FIMManager getInstance()
    {
        if (sInstance == null)
        {
            synchronized (FIMManager.class)
            {
                if (sInstance == null)
                    sInstance = new FIMManager();
            }
        }
        return sInstance;
    }

    /**
     * 设置是否调试模式
     *
     * @param debug
     */
    public void setDebug(boolean debug)
    {
        mIsDebug = debug;
    }

    private String getDebugTag()
    {
        return FIMManager.class.getSimpleName();
    }

    /**
     * 设置IM处理对象
     *
     * @param handler
     */
    public void setIMHandler(FIMHandler handler)
    {
        mIMHandler = handler;
    }

    /**
     * 返回IM处理类
     *
     * @return
     */
    private FIMHandler getIMHandler()
    {
        if (mIMHandler == null)
            throw new NullPointerException("you must set a FIMHandler to FIMManager before this");
        return mIMHandler;
    }

    /**
     * 添加消息回调
     *
     * @param callback
     */
    public synchronized void addMsgCallback(FIMMsgCallback callback)
    {
        if (callback == null || mListMsgCallback.contains(callback))
            return;

        mListMsgCallback.add(callback);
        if (mIsDebug)
            Log.i(getDebugTag(), "FIMMsgCallback add size " + mListMsgCallback.size() + " " + callback + " " + Thread.currentThread().getName());
    }

    /**
     * 移除消息回调
     *
     * @param callback
     */
    public synchronized void removeMsgCallback(FIMMsgCallback callback)
    {
        if (mListMsgCallback.remove(callback))
        {
            if (mIsDebug)
                Log.e(getDebugTag(), "FIMMsgCallback remove size " + mListMsgCallback.size() + " " + callback + " " + Thread.currentThread().getName());
        }
    }

    synchronized void notifyReceiveMsg(FIMMsg fimMsg)
    {
        for (FIMMsgCallback item : mListMsgCallback)
        {
            if (item.ignoreMsg(fimMsg))
            {
                // 忽略当前消息
            } else
            {
                item.onReceiveMsg(fimMsg);
            }
        }
    }

    /**
     * 返回新创建的第三方IM消息接收对象
     *
     * @return
     */
    public FIMMsgReceiver newMsgReceiver()
    {
        return getIMHandler().newMsgReceiver();
    }

    /**
     * 发送C2C消息
     *
     * @param peer 对方id
     * @param data 要发送的数据
     * @return
     */
    public FIMMsg sendMsgC2C(String peer, FIMMsgData data, FIMResultCallback<FIMMsg> callback)
    {
        return getIMHandler().sendMsg(peer, data, FIMConversationType.C2C, generateCallbackId(callback));
    }

    /**
     * 发送Group消息
     *
     * @param peer group id
     * @param data 要发送的数据
     * @return
     */
    public FIMMsg sendMsgGroup(String peer, FIMMsgData data, FIMResultCallback<FIMMsg> callback)
    {
        return getIMHandler().sendMsg(peer, data, FIMConversationType.Group, generateCallbackId(callback));
    }

    /**
     * 加入群组
     *
     * @param groupId  群组id
     * @param callback
     */
    public void joinGroup(String groupId, FIMResultCallback callback)
    {
        getIMHandler().joinGroup(groupId, generateCallbackId(callback));
    }

    /**
     * 退出群组
     *
     * @param groupId  群组id
     * @param callback
     */
    public void quitGroup(String groupId, FIMResultCallback callback)
    {
        getIMHandler().quitGroup(groupId, generateCallbackId(callback));
    }

    /**
     * 移除并返回结果回调
     *
     * @param callbackId 回调对应的id
     * @return
     */
    synchronized FIMResultCallback removeCallbackById(String callbackId)
    {
        if (TextUtils.isEmpty(callbackId))
            return null;

        final CallbackInfo info = mMapCallback.remove(callbackId);
        return info == null ? null : info.callback;
    }

    /**
     * 根据tag移除结果回调
     *
     * @param tag
     * @return 移除的数量
     */
    public synchronized int removeCallbackByTag(String tag)
    {
        if (TextUtils.isEmpty(tag) || mMapCallback.isEmpty())
            return 0;

        int count = 0;
        final Iterator<Map.Entry<String, CallbackInfo>> it = mMapCallback.entrySet().iterator();
        while (it.hasNext())
        {
            final CallbackInfo info = it.next().getValue();
            if (tag.equals(info.tag))
            {
                it.remove();
                count++;
            }
        }
        return count;
    }

    /**
     * 生成callback的标识
     *
     * @param callback
     * @return
     */
    private synchronized String generateCallbackId(FIMResultCallback callback)
    {
        if (callback == null)
            return null;

        final String callbackId = String.valueOf(UUID.randomUUID());
        final CallbackInfo info = new CallbackInfo(callback, callback.getTag());

        mMapCallback.put(callbackId, info);
        return callbackId;
    }

    /**
     * 保存callback信息
     */
    private static final class CallbackInfo
    {
        public FIMResultCallback callback;
        public String tag;

        public CallbackInfo(FIMResultCallback callback, String tag)
        {
            this.callback = callback;
            this.tag = tag;
        }
    }
}
