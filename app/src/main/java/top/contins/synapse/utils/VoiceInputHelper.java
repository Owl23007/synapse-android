package top.contins.synapse.utils;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.os.Bundle;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 语音输入助手
 * 处理语音识别功能
 */
@Singleton
public class VoiceInputHelper {

    private final Context context;
    private SpeechRecognizer speechRecognizer;
    private final MutableLiveData<String> voiceResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isListening = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public VoiceInputHelper(Context context) {
        this.context = context;
        initializeSpeechRecognizer();
    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(new VoiceRecognitionListener());
        }
    }

    /**
     * 开始语音识别
     */
    public void startListening() {
        if (speechRecognizer != null) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话...");

            isListening.setValue(true);
            speechRecognizer.startListening(intent);
        }
    }

    /**
     * 停止语音识别
     */
    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            isListening.setValue(false);
        }
    }

    /**
     * 销毁语音识别器
     */
    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }

    // Getters for LiveData
    public MutableLiveData<String> getVoiceResult() {
        return voiceResult;
    }

    public MutableLiveData<Boolean> getIsListening() {
        return isListening;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    private class VoiceRecognitionListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {
            // 准备开始语音识别
        }

        @Override
        public void onBeginningOfSpeech() {
            // 开始说话
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // 音量变化
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // 接收到音频数据
        }

        @Override
        public void onEndOfSpeech() {
            // 结束说话
            isListening.setValue(false);
        }

        @Override
        public void onError(int error) {
            isListening.setValue(false);
            String errorMsg = getErrorMessage(error);
            errorMessage.setValue(errorMsg);
        }

        @Override
        public void onResults(Bundle results) {
            isListening.setValue(false);
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                voiceResult.setValue(matches.get(0));
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            // 部分识别结果
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            // 其他事件
        }

        private String getErrorMessage(int error) {
            return switch (error) {
                case SpeechRecognizer.ERROR_AUDIO -> "音频录制错误";
                case SpeechRecognizer.ERROR_CLIENT -> "客户端错误";
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足";
                case SpeechRecognizer.ERROR_NETWORK -> "网络错误";
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时";
                case SpeechRecognizer.ERROR_NO_MATCH -> "无法识别语音";
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器忙碌";
                case SpeechRecognizer.ERROR_SERVER -> "服务器错误";
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "语音超时";
                default -> "语音识别错误";
            };
        }
    }
}
