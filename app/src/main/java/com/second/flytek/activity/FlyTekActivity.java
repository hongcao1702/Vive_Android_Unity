package com.second.flytek.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.htc.vr.unity.WVRUnityVRActivity;
import com.second.flytek.setting.Qisr;
import com.second.flytek.setting.Qtts;
import com.tencent.gcloud.voice.GCloudVoiceEngine;
import com.tencent.gcloud.voice.IGCloudVoiceNotify;

import java.util.Timer;
import java.util.TimerTask;

public class FlyTekActivity extends WVRUnityVRActivity {
    private Qtts mQtts = null;
    private Qisr mQisr = null;

    private static GCloudVoiceEngine mCloudEngine = null;
    private static String appID = "1318065628";
    private static String appKey = "8d5d31cf4b1d425c9d17133c7563cdab";
    private static String openID = Long.toString(System.currentTimeMillis());
    private static String roomName = "cz-test";

    public final String tag = "GCloudVoiceNotify";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(mQtts == null) {
            mQtts = new Qtts(this);
        }
        if(mQisr == null) {
            mQisr = new Qisr(this);
        }

        // Important
        GCloudVoiceEngine.getInstance().init(getApplicationContext(), this);
        mCloudEngine = GCloudVoiceEngine.getInstance();
        mCloudEngine.SetAppInfo(appID, appKey, openID);
        mCloudEngine.Init();
        mCloudEngine.SetMode(0);
        Notify notify = new Notify();
        mCloudEngine.SetNotify(notify);

        //timer to poll
        TimerTask task = new TimerTask() {
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };

        Timer timer = new Timer(true);
        timer.schedule(task, 500, 500);
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

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mCloudEngine.Poll();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    class Notify implements IGCloudVoiceNotify {
        /*
        String TAG = "[cz]";
        @Override
        public void onJoinRoomComplete(int code) {
            // TODO Auto-generated method stub
            Log.i(TAG, "onJoinRoomComplete:" + code);
            _logTV.setText( "onJoinRoomComplete with "+ code);
        }*/


        @Override
        public void OnJoinRoom(int code, String roomName, int memberID) {
            Log.i(tag, "OnJoinRoom CallBack code=" + code + " roomname:" + roomName + " memberID:" + memberID);
            setText("OnJoinRoom CallBack code=" + code + " roomname:" + roomName + " memberID:" + memberID);
        }

        @Override
        public void OnStatusUpdate(int status, String roomName, int memberID) {
            Log.i(tag, "OnStatusUpdate CallBack code=" + status + " roomname:" + roomName + " memberID:" + memberID);
            setText("OnStatusUpdate CallBack code=" + status + " roomname:" + roomName + " memberID:" + memberID);
        }

        @Override
        public void OnQuitRoom(int code, String roomName) {
            Log.i(tag, "OnQuitRoom CallBack code=" + code + " roomname:" + roomName);
            setText("OnQuitRoom CallBack code=" + code + " roomname:" + roomName);
        }

        @Override
        public void OnMemberVoice(int[] members, int count) {
            Log.i(tag, "OnMemberVoice CallBack " + "count:" + count);
            String str = "OnMemberVoice Callback ";
            for (int i = 0; i < count; ++i) {
                str += " memberid:" + members[2 * i];
                str += " state:" + members[2 * i + 1];
            }
            setText("OnMemberVoice CallBack " + "count:" + count);
        }

        @Override
        public void OnUploadFile(int code, String filePath, String fileID) {
            Log.i(tag, "OnUploadFile CallBack code=" + code + " filePath:" + filePath + " fileID:" + fileID);
            setText("OnUploadFile CallBack code=" + code + " filePath:" + filePath + " fileID:" + fileID);
        }

        @Override
        public void OnDownloadFile(int code, String filePath, String fileID) {
            Log.i(tag, "OnDownloadFile CallBack code=" + code + " filePath:" + filePath + " fileID:" + fileID);
            setText("OnDownloadFile CallBack code=" + code + " filePath:" + filePath + " fileID:" + fileID);
        }

        @Override
        public void OnPlayRecordedFile(int code, String filePath) {
            Log.i(tag, "OnPlayRecordedFile CallBack code=" + code + " filePath:" + filePath);
            setText("OnPlayRecordedFile CallBack code=" + code + " filePath:" + filePath);
        }

        @Override
        public void OnApplyMessageKey(int code) {
            Log.i(tag, "OnApplyMessageKey CallBack code=" + code);
           setText("OnApplyMessageKey CallBack code=" + code);
        }

        @Override
        public void OnSpeechToText(int code, String fileID, String result) {
            Log.i(tag, "OnSpeechToText CallBack code=" + code + " fileID:" + fileID + " result:" + result);
            setText("OnSpeechToText CallBack code=" + code + " fileID:" + fileID + " result:" + result);
        }

        @Override
        public void OnRecording(char[] pAudioData, int nDataLength) {
            Log.i(tag, "OnRecording CallBack  nDataLength:" + nDataLength);
        }

        @Override
        public void OnStreamSpeechToText(int i, int i1, String s, String s1) {

        }

        @Override
        public void OnMemberVoice(String s, int i, int i1) {

        }

        @Override
        public void OnRoleChanged(int i, String s, int i1, int i2) {

        }
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

    public void MSG_JoinRoom(){
        if(mCloudEngine != null) {
            Log.d(tag,"+++MSG_JoinRoom");
            mCloudEngine.JoinTeamRoom(roomName,10000);
        }
    }

    public void MSG_OpenMic(){
        if(mCloudEngine != null) {
            Log.d(tag,"+++MSG_OpenMic");
            mCloudEngine.OpenMic();
        }
    }

    public void MSG_OpenSpeaker(){
        if(mCloudEngine != null) {
            Log.d(tag,"+++MSG_OpenSpeaker");
            mCloudEngine.OpenSpeaker();
        }
    }

    public void MSG_QuitRoom(){
        if(mCloudEngine != null) {
            Log.d(tag,"+++MSG_QuitRoom");
            mCloudEngine.QuitRoom(roomName,10000);
        }
    }

}
