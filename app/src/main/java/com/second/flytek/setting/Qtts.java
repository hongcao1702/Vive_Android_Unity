package com.second.flytek.setting;

import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.second.flytek.activity.FlyTekActivity;

/**
 * Created by hong on 2018/5/2.
 */

public class Qtts {
    private static String TAG = "FlyTek";
    // 语音合成对象
    private SpeechSynthesizer mTts;

    private FlyTekActivity mContext;

    private boolean mNeedStartAudio = true;

    private String mTtsPath = null;

    // 默认发音人
    private String mVoicer = "john";
    //用于离线合成使用
    private String mLocalText = null;

    // 函数调用返回值
    private int mReturn = 0;

    //如果需要本地，则mIsCloud改为false就行了
    private boolean mIsCloud = true;

    public Qtts(FlyTekActivity context) {
        mContext = context;
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(mContext, mTtsInitListener);
        mTtsPath = Environment.getExternalStorageDirectory()+"/msc/tts.wav";
    }

    private void setParams(){
        if(mTts == null) {
            return;
        }
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        if(mIsCloud) {
            // 根据合成引擎设置相应参数
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, mVoicer);

        } else {
            mVoicer = "xiaoyan";
            mLocalText = "亲爱的用户，您好，这是一个语音合成示例，感谢您对科大讯飞语音技术的支持！科大讯飞是亚太地区最大的语音上市公司，股票代码：002230";
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            mTts.setParameter(SpeechConstant.VOICE_NAME, mVoicer);
            mTts.setParameter(ResourceUtil.TTS_RES_PATH,getResourcePath());
        }
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "50");
        //设置播放器音频流类型
        //mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, mTtsPath);
    }

    //获取发音人资源路径
    private String getResourcePath(){
        StringBuffer tempBuffer = new StringBuffer();
        //合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "res/tts/common.jet"));
        tempBuffer.append(";");
        //发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "res/tts/" + mVoicer +".jet"));
        return tempBuffer.toString();
    }

    public void startSpeak(String text) {
        if(mTts != null) {
            mLocalText = text;
            // 设置参数
            setParams();
            if(mNeedStartAudio) {///合成完成后会播放音频
                mReturn = mTts.startSpeaking(mLocalText, mTtsListener);
            } else {///合成完毕后不会播放音频
                mReturn = mTts.synthesizeToUri(mLocalText, mTtsPath, mTtsListener);
            }
            if (mReturn != ErrorCode.SUCCESS) {
                mContext.MSG_TTSErrorCallback("语音合成失败,错误码: " + mReturn);
            }
        }
    }

    public void stopSpeak() {
        if(mTts != null) {
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
        }
    }

    public void setVoicer(String voicer) {
        if(!TextUtils.isEmpty(voicer)) {
            mVoicer = voicer;
        }
    }

    public String getTtsPath() {
        return mTtsPath;
    }

    public void setNeedStartAudio(boolean flag) {
        mNeedStartAudio = flag;
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            mContext.setText("开始播放");
        }

        @Override
        public void onSpeakPaused() {
            mContext.setText("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            mContext.setText("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            mContext.setText("onBufferProgress:" + percent);
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            mContext.setText("onSpeakProgress:" + percent);
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                mContext.MSG_TTSSuccessCallback("播放完成");
            } else if (error != null) {
                mContext.MSG_TTSErrorCallback(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                mContext.MSG_TTSErrorCallback("初始化失败,错误码："+code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };
}
