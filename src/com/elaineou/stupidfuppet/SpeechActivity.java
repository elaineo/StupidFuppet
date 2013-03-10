package com.elaineou.stupidfuppet;


import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.widget.Toast;

public class SpeechActivity extends Activity  implements OnInitListener, OnUtteranceCompletedListener{
    
	private static final String TAG = "SpeechActivity";    
    private TextToSpeech tts;
    private HashMap<String, String> hashAudio;
    private String message;
    
 @Override
 public void onCreate(Bundle savedInstanceState) {
	 Intent intent = getIntent();
	 message = intent.getStringExtra(VoiceActivity.VOICE_RESULT);
	 if(message.contains("*")) {
		 message = "I do not speak French";
	 } 
		 
	 super.onCreate(savedInstanceState);
	 setContentView(R.layout.activity_speak);
	   
	 tts = new TextToSpeech(this, this);
   	 hashAudio = new HashMap<String, String>();
    }

    @Override
    public void onInit(int status) {
    	Locale locale = new Locale("spa", "MEX");
        if (status == TextToSpeech.SUCCESS) {
            setTtsListener();
       	 	tts.setPitch(0.5f);
            switch (tts.isLanguageAvailable(locale))
            {
                case TextToSpeech.LANG_AVAILABLE:
                case TextToSpeech.LANG_COUNTRY_AVAILABLE:
                case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
                    Log.d(TAG, "SUPPORTED");
                    tts.setLanguage(locale);
                    break;
                case TextToSpeech.LANG_MISSING_DATA:
                    Log.d(TAG, "MISSING_DATA");
                    // missing data, install it
                    Intent installIntent = new Intent();
                    installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installIntent);
                    break;
                case TextToSpeech.LANG_NOT_SUPPORTED:
                    Log.d(TAG, "NOT SUPPORTED");
                    break;
            }
 
            hashAudio.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
            hashAudio.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "fuppet");
            tts.speak(message, TextToSpeech.QUEUE_ADD, hashAudio);

        }
        else if (status == TextToSpeech.ERROR) {
            Toast.makeText(SpeechActivity.this, 
                    "Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
        }
        
    }

    private void setTtsListener()
    {
        int listenerResult = tts.setOnUtteranceCompletedListener(this);
        if (listenerResult != TextToSpeech.SUCCESS)
        {
            Log.e(TAG, "failed to add utterance completed listener");
        }
      }
    
    
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

	@Override
	public void onUtteranceCompleted(String utteranceId) {
		Log.e(TAG, "completed");
		SpeechActivity.this.finish();
		
	}    
}
