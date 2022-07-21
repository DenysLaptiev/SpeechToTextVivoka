package com.vivoka.freespeech;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vivoka.csdk.asr.Engine;
import com.vivoka.csdk.asr.models.AsrResult;
import com.vivoka.csdk.asr.models.AsrResultHypothesis;
import com.vivoka.csdk.asr.recognizer.IRecognizerListener;
import com.vivoka.csdk.asr.recognizer.Recognizer;
import com.vivoka.csdk.asr.recognizer.RecognizerErrorCode;
import com.vivoka.csdk.asr.recognizer.RecognizerEventCode;
import com.vivoka.csdk.asr.recognizer.RecognizerResultType;
import com.vivoka.csdk.asr.utils.AsrResultParser;
import com.vivoka.freespeech.assets.AsrAssetsExtractor;
import com.vivoka.freespeech.audio.AudioRecorder;
import com.vivoka.vsdk.Constants;
import com.vivoka.vsdk.Exception;
import com.vivoka.vsdk.Vsdk;
import com.vivoka.vsdk.audio.Pipeline;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "AsrActivity";

    private final String ASR_APPLICATION = "FreeSpeech";
    private final int ASR_CONFIDENCE_THRESHOLD = 5000;

    private Recognizer recognizer;
    private AudioRecorder audioRecorder;

    private TextView tvTotal;

    private final String SECRET_WORD = "robot";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTotal = findViewById(R.id.tvTotal);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        init();
    }

    private void init() {
        final Context mContext = this;
        final String assetsPath = getFilesDir().getAbsolutePath() + Constants.vsdkPath;
        try {
            new AsrAssetsExtractor(this, assetsPath, () -> {
                // VSDK Engine
                try {
                    Vsdk.init(mContext, "config/vsdk.json", success -> {
                        if (success) {

                            // ASR
                            try {
                                Engine.getInstance().init(mContext, success1 -> {
                                    if (success1) {
                                        make();
                                    } else {
                                        Log.e(TAG, "Cannot initialize the ASR engine");
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            Log.e(TAG, "Cannot initialize the VSDK engine");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void make() {
        IRecognizerListener recognizerListener = new IRecognizerListener() {
            @Override
            public void onEvent(RecognizerEventCode eventCode, int timeMarker, String message) {
                //Log.e("onEvent", eventCode.name() + " " + timeMarker + "ms");
            }

            @Override
            public void onResult(String result, RecognizerResultType resultType, boolean isFinal) {
                Log.e("onResult", resultType.name() + " " + result);
                processRecognitionResult(result, resultType);
            }

            @Override
            public void onError(RecognizerErrorCode error, String message) {
                Log.e("onError", error.name() + " " + message);
            }

            @Override
            public void onWarning(RecognizerErrorCode error, String message) {
                Log.e("onWarning", error.name() + " " + message);
            }
        };
        try {
            recognizer = Engine.getInstance().getRecognizer("rec", recognizerListener);
            recognizer.setModel(ASR_APPLICATION);

            audioRecorder = new AudioRecorder();

            Pipeline pipeline = new Pipeline();
            pipeline.pushBackConsumer(recognizer);

            pipeline.setProducer(audioRecorder);
            pipeline.start();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }


    private void processRecognitionResult(String result, RecognizerResultType resultType) {
        if (result == null || result.isEmpty()) {
            Log.e("AsrActivity", "result is null or empty");
            return;
        }

        if (resultType == RecognizerResultType.ASR) {
            AsrResult asrResult = null;
            try {
                asrResult = AsrResultParser.parseResult(result);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (asrResult != null) {
                /*
                List<AsrResultHypothesis> hypothesisList = asrResult.hypotheses;
                Collections.sort(hypothesisList, new Comparator<AsrResultHypothesis>() {
                    @Override
                    public int compare(AsrResultHypothesis h1, AsrResultHypothesis h2) {
                        return h2.confidence-h1.confidence;
                    }
                });
                for (AsrResultHypothesis hypothesis : hypothesisList) {
                    System.out.println(hypothesis.confidence);
                }
                for (AsrResultHypothesis asrResultHypothesis : asrResult.hypotheses) {
                    if (asrResultHypothesis.confidence >= ASR_CONFIDENCE_THRESHOLD) {
                        Log.e(TAG, "Found result !");
                        Log.e(TAG, asrResultHypothesis.toString());
                        String tmpResult = asrResultHypothesis.text;
                        showResult(tmpResult);
                    }
                }
                */
                AsrResultHypothesis asrResultHypothesis = findBestCandidate(asrResult);
                if (asrResultHypothesis.confidence >= ASR_CONFIDENCE_THRESHOLD) {
                    Log.e(TAG, "Found result !");
                    Log.e(TAG, asrResultHypothesis.toString());
                    String tmpResult = asrResultHypothesis.text;
                    showResult(tmpResult);
                }
            }
        }

        new Thread(() -> {
            try {
                recognizer.setModel(ASR_APPLICATION);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showResult(String tmpResult) {
        runOnUiThread(new Runnable() {
            public void run() {
                tvTotal.append(tmpResult + "\n");
                tvTotal.append("    * * *    " + "\n");
            }
        });

        if (tmpResult.toLowerCase().contains(SECRET_WORD)) {

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "Well done! " + tmpResult, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private AsrResultHypothesis findBestCandidate(AsrResult asrResult) {
        //get List
        List<AsrResultHypothesis> hypothesisList = asrResult.hypotheses;

        //sort List by confidence
        Collections.sort(hypothesisList, new Comparator<AsrResultHypothesis>() {
            @Override
            public int compare(AsrResultHypothesis h1, AsrResultHypothesis h2) {
                return h2.confidence - h1.confidence;
            }
        });

//        for (AsrResultHypothesis hypothesis : hypothesisList) {
//            System.out.println(hypothesis.confidence);
//        }
        AsrResultHypothesis bestCandidate = hypothesisList.get(0);
        for (AsrResultHypothesis hypothesis : hypothesisList) {
            if (hypothesis.text.toLowerCase().contains(SECRET_WORD)) {
                bestCandidate = hypothesis;
                break;
            }
        }

        //return best Candidate
        return bestCandidate;
    }

}