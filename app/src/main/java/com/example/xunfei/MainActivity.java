package com.example.xunfei;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG ="MainActivity";
    private SpeechRecognizer mIat;
    private RecognizerDialog mIatDialog;
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private SharedPreferences mSharedPreferences;
    private  String mEngineType = SpeechConstant.TYPE_CLOUD;
    private String language ="zh_cn";
    private TextView tvResult;
    private Button btnStart;
    private String resultType ="json";
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG,"SpeechRecognizer init() code ="+code);
            if (code != ErrorCode.SUCCESS){
                showMsg("初始化失败，错误码:"+code+",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }

        }
    };

    private final RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
        }

        @Override
        public void onError(SpeechError error) {
            showMsg(error.getPlainDescription(true));

        }
    };

    private void showMsg(String msg){
        Toast.makeText(MainActivity.this,msg, Toast.LENGTH_SHORT).show();
    }

    private void printResult(RecognizerResult results){
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        }catch(JSONException e){
            e.printStackTrace();
        }
        mIatResults.put(sn,text);
        StringBuffer resultButter = new StringBuffer();
        for (String key : mIatResults.keySet()){
            resultButter.append(mIatResults.get(key));
        }
        tvResult.setText(resultButter.toString());
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvResult = findViewById(R.id.tv_result);
        btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(this);
        mIat = SpeechRecognizer.createRecognizer(MainActivity.this,mInitListener);
        mIatDialog = new RecognizerDialog(MainActivity.this,mInitListener);
        mSharedPreferences = getSharedPreferences("ASR",
                Activity.MODE_PRIVATE);
    }


    @Override
    public void onClick(View v) {
        if (null == mIat){
            showMsg("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            return;
        }
        mIatResults.clear();
        setParam();
        mIatDialog.setListener(mRecognizerDialogListener);
        mIatDialog.show();

    }
    public void setParam(){
        mIat.setParameter(SpeechConstant.PARAMS,null);
        mIat.setParameter(SpeechConstant.ENGINE_TYPE,mEngineType);
        mIat.setParameter(SpeechConstant.RESULT_TYPE,resultType);
        if (language.equals("zh_cn")) {
            String lag = mSharedPreferences.getString("iat_language_preference",
                    "mandarin");
            Log.e(TAG, "language:" + language);// 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        }else {
            mIat.setParameter(SpeechConstant.LANGUAGE, language);
        }
        Log.e(TAG,"last language:"+mIat.getParameter(SpeechConstant.LANGUAGE));
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+ "/msc/iat.wav");

    }
    protected void onDestroy(){
        super.onDestroy();
        if (null != mIat){
            mIat.cancel();
            mIat.destroy();
        }
    }
}
