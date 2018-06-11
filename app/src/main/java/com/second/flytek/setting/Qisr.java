package com.second.flytek.setting;

import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.second.flytek.activity.FlyTekActivity;
import com.second.flytek.util.FucUtil;
import com.second.flytek.util.JsonParser;

/**
 * Created by hong on 2018/5/2.
 */

public class Qisr {
    private static String TAG = "FlyTek";

    private static final String GRAMMAR_TYPE_ABNF = "abnf";
    private static final String KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id";
    // 云端语法文件
    private String mCloudGrammar = null;
    private String mGrammarID = null;
    // 语音识别对象
    private SpeechRecognizer mAsr;

    private FlyTekActivity mContext;

    private String mGrammarName = "grammar_sample.abnf";
    private String mAsrPath = null;

    // 函数调用返回值
    private int mReturn = 0;

    public Qisr(FlyTekActivity context) {
        mContext = context;
        // 初始化合成对象
        mAsr = SpeechRecognizer.createRecognizer(mContext, mInitListener);
        mCloudGrammar = FucUtil.readFile(mContext,mGrammarName,"utf-8");
        mAsrPath = Environment.getExternalStorageDirectory()+"/msc/asr.wav";
    }

    private void buildGrammar(){
        if(mAsr == null) {
            return;
        }
        //指定引擎类型
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mAsr.setParameter(SpeechConstant.TEXT_ENCODING,"utf-8");
        mReturn = mAsr.buildGrammar(GRAMMAR_TYPE_ABNF, mCloudGrammar, mCloudGrammarListener);
        if(mReturn != ErrorCode.SUCCESS) {
            mContext.MSG_ISRErrorCallback("语法构建失败,错误码：" + mReturn);
        }

    }

    public void startRecognize() {
        if(mAsr != null) {
            mContext.setText("");
            if(TextUtils.isEmpty(mGrammarID)){
                buildGrammar();
            } else {
                mAsr.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
                //设置返回结果为json格式
                mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");
                mAsr.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
                mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH, mAsrPath);
                mAsr.setParameter(SpeechConstant.CLOUD_GRAMMAR, mGrammarID);
                mReturn = mAsr.startListening(mRecognizerListener);
                if (mReturn != ErrorCode.SUCCESS) {
                    mContext.MSG_ISRErrorCallback("识别失败,错误码: " + mReturn);
                }
            }

            /*mReturn = mAsr.startListening(mRecognizerListener);
            if (mReturn != ErrorCode.SUCCESS) {
                mContext.setText("识别失败,错误码: " + mReturn);
            }*/
        }
    }

    public void stopRecognize() {
        if( mAsr != null ){
            // 退出时释放连接
            mAsr.cancel();
            mAsr.destroy();
        }
        mAsr = null;
    }

    /**
     * 如果新的语法名称和原来的不一样，则重新获取
     * @param fileName
     */
    public void setGrammarName(String fileName) {
        if(!TextUtils.isEmpty(fileName) && !mGrammarName.equals(fileName)) {
            mGrammarName = fileName;
            mCloudGrammar = FucUtil.readFile(mContext,mGrammarName,"utf-8");
            mGrammarID = null;
        }
    }

    private String getNearestResult(String data) {
        if(!TextUtils.isEmpty(data)) {
            String[] temp = data.split("\n");
            if(temp != null && temp.length > 0) {
                return temp[0];
            } else {
                return data;
            }
        } else {
            return "";
        }
    }

    /**
     * 云端构建语法监听器。
     */
    private GrammarListener mCloudGrammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if(error == null){
                mGrammarID = grammarId;
                mAsr.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
                //设置返回结果为json格式
                mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");
                mAsr.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
                mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH, mAsrPath);
                mAsr.setParameter(SpeechConstant.CLOUD_GRAMMAR, grammarId);
                mReturn = mAsr.startListening(mRecognizerListener);
                if (mReturn != ErrorCode.SUCCESS) {
                    mContext.MSG_ISRErrorCallback("识别失败,错误码: " + mReturn);
                }
            }else{
                mGrammarID = null;
                mContext.MSG_ISRErrorCallback("语法构建失败,错误码：" + error.getErrorCode());
            }
        }
    };

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                mContext.MSG_ISRErrorCallback("初始化失败,错误码："+code);
            }
        }
    };

    /**
     * 识别监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            mContext.setText("当前正在说话，音量大小：" + volume);
        }

        @Override
        public void onResult(final RecognizerResult result, boolean isLast) {
            if (null != result) {
                mContext.MSG_ISRSuccessCallback(getNearestResult(JsonParser.parseGrammarResult(result.getResultString())));
               /* if("cloud".equalsIgnoreCase(mEngineType)){
                    text =
                }else {
                    text = JsonParser.parseLocalGrammarResult(result.getResultString());
                }*/
            } else {
                mContext.MSG_ISRSuccessCallback("");
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            mContext.setText("结束说话");
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            mContext.setText("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            mContext.MSG_ISRErrorCallback("onError Code："	+ error.getErrorCode());
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }

    };
}
