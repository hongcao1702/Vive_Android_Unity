package com.second.flytek.activity;

import android.os.Bundle;
import android.text.TextUtils;
import com.htc.vr.unity.WVRUnityVRActivity;
import com.second.flytek.setting.Qisr;
import com.second.flytek.setting.Qtts;


public class FlyTekActivity extends WVRUnityVRActivity {
    private Qtts mQtts = null;
    private Qisr mQisr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(mQtts == null) {
            mQtts = new Qtts(this);
        }
        if(mQisr == null) {
            mQisr = new Qisr(this);
        }
    }

    public void setText(String data) {
        MSG_SetResult(data);
    }

    @Override
    protected void onDestroy() {
        if( null != mQtts ){
            mQtts.stopSpeak();
            // 退出时释放连接
        }
        if( null != mQisr ){
            // 退出时释放连接
            mQisr.stopRecognize();
        }
        super.onDestroy();
    }

    /****************************************************************************************
    **                           以下为Unity和Android之间互相调用的接口                    **
    **                                                                                     **
    *****************************************************************************************/

    /**
     * 开始进行语音合成
     * @param text ： 需要合成的文本
     * @param isNeedPlay ： 合成完毕是否需要播放音频
     */
    public void MSG_StartTTS(String text,boolean isNeedPlay) {
        if(mQtts != null) {
            MSG_SetTTSNeedSpeak(isNeedPlay);
            mQtts.startSpeak(text);
        }
    }

    /**
     * 开始进行语音识别
     */
    public void MSG_StartISR() {
        if(mQisr != null) {
            mQisr.startRecognize();
        }
    }

    /**
     * 设置tts完成后是否需要播放合成的语音
     * @param value
     */
    public void MSG_SetTTSNeedSpeak(boolean value) {
        if(mQtts != null) {
            mQtts.setNeedStartAudio(value);
        }
    }

    /**
     * 获取tts的路径
     * @return
     */
    public String MSG_GetTtsPath() {
        if(mQtts != null) {
            return mQtts.getTtsPath();
        } else {
            return "";
        }
    }

    /**
     * 设置ISR的最相近的识别值
     * @param data
     */
    public void MSG_SetResult(String data) {
        if(mUnityPlayer != null) {
            //FlyTek为Unity对应的GameObject名称
            mUnityPlayer.UnitySendMessage("FlyTek", "SetResult", data);
        }
    }

    /**
     * 设置ISR语法文件名，默认为grammar_sample.abnf
     * @param fileName
     */
    public void MSG_SetISRGramarName(String fileName) {
        if(!TextUtils.isEmpty(fileName) && (mQisr != null)){
            mQisr.setGrammarName(fileName);
        }
    }

    /**
     * 设置tts的发音人(默认为john)
     * @param voicer
     */
    public void MSG_SetTTSVoicer(String voicer) {
        if(mQtts != null) {
            mQtts.setVoicer(voicer);
        }
    }

}
