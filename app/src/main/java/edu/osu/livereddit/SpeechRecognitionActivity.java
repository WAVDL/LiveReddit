package edu.osu.livereddit;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class SpeechRecognitionActivity extends AppCompatActivity {

    private SpeechRecognizer sr;
    private static final String TAG = "speechRecognitionAct";
    private TextView mTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_recognition);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTextView = (TextView)findViewById(R.id.speech_recognizer_result);
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());


        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
        sr.startListening(intent);
        Log.i("111111", "11111111");



    }

    class listener implements RecognitionListener
    {
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "onReadyForSpeech");
        }
        public void onBeginningOfSpeech()
        {
            Log.d(TAG, "onBeginningOfSpeech");
        }
        public void onRmsChanged(float rmsdB)
        {
            Log.d(TAG, "onRmsChanged");
        }
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG, "onBufferReceived");
        }
        public void onEndOfSpeech()
        {
            Log.d(TAG, "onEndofSpeech");
        }
        public void onError(int error)
        {
            Log.d(TAG, "error " + error);

        }
        public void onResults(Bundle results)
        {
            String str = new String();
            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++)
            {
                Log.d(TAG, "result " + data.get(i));
                str += data.get(i);
            }
            String subredditName = String.valueOf(0);
            mTextView.setText("results: "+subredditName);

            sr.stopListening();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("SUBREDDITNAME",subredditName);
            setResult(SubredditsList.RESULT_OK, resultIntent);
            finish();
        }
        public void onPartialResults(Bundle partialResults)
        {
            Log.d(TAG, "onPartialResults");
        }
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG, "onEvent " + eventType);
        }
    }


}
