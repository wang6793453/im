package com.sd.lib.im;

import android.os.Handler;
import android.os.Looper;

import com.sd.lib.im.callback.FIMResultCallback;
import com.sd.lib.im.msg.FIMMsg;
import com.sd.lib.im.msg.FIMMsgData;

import org.json.JSONObject;

/**
 * 第三方IM消息接收处理类
 *
 * @param <M>第三方IM消息类型
 */
public abstract class FIMMsgReceiver<M> implements FIMMsg
{
    private M mSDKMsg;
    private FIMMsgData mData;

    /**
     * 返回第三发IM消息对象
     *
     * @return
     */
    public final M getSDKMsg()
    {
        return mSDKMsg;
    }

    @Override
    public final int getDataType()
    {
        return mData == null ? 0 : mData.getType();
    }

    @Override
    public final FIMMsgData getData()
    {
        return mData;
    }

    /**
     * 返回用于猜测数据类型的json字段名称
     *
     * @return
     */
    protected String getJsonFieldNameForDataType()
    {
        return "type";
    }

    /**
     * 从json串中猜测对应的数据类型
     *
     * @param json
     * @return 返回猜测的数据类型，-1表示猜测失败
     */
    protected int guessDataTypeFromJson(String json)
    {
        try
        {
            return new JSONObject(json).getInt(getJsonFieldNameForDataType());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 解析第三方的SDK消息
     *
     * @return
     */
    public final boolean parse(M sdkMsg)
    {
        if (sdkMsg == null)
            throw new NullPointerException("sdkMsg must not be null");

        mSDKMsg = sdkMsg;
        try
        {
            mData = onParseSDKMsg(sdkMsg);
        } catch (Exception e)
        {
            onError(e);
        }

        if (mData != null)
        {
            onFillData(mData);
            return true;
        } else
        {
            return false;
        }
    }

    @Override
    public final void notifyReceiveMsg()
    {
        if (Looper.myLooper() == Looper.getMainLooper())
        {
            FIMManager.getInstance().notifyReceiveMsg(this);
        } else
        {
            new Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    notifyReceiveMsg();
                }
            });
        }
    }

    /**
     * 将第三方的SDK消息解析为数据
     *
     * @param sdkMsg
     * @return
     * @throws Exception
     */
    protected abstract FIMMsgData<M> onParseSDKMsg(M sdkMsg) throws Exception;

    /**
     * 填充解析好的数据
     *
     * @param data
     */
    protected abstract void onFillData(FIMMsgData<M> data);

    /**
     * 是否有需要下载的数据，true-需要<br>
     * 如果需要下载数据，并且callback不为null，则开始下载数据
     *
     * @param callback
     * @return
     */
    public abstract boolean isNeedDownloadData(FIMResultCallback callback);

    /**
     * 解析异常回调
     *
     * @param e
     */
    protected void onError(Exception e)
    {

    }
}
