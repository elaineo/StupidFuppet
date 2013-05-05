package com.elaineou.stupidfuppet;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ElizaActivity extends Fragment implements OnClickListener, OnInitListener, OnUtteranceCompletedListener{
	 private static final String TAG = "ElizaActivity";
	 private static final String TAGsp = "SpeechActivity";
	 private static final String TAGvr = "VoiceRecActivity";
	 private SpeechRecognizer sr;
	 private OnSpeechListener spListener;

	 private FuppetStuff stupidf;
    private TextToSpeech tts;
    private HashMap<String, String> hashAudio;

	 private TextView elField;
	 private TextView elDebug;
	 private Button ebtSpeak;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.activity_eliza, container, false);
  	  elDebug = (TextView) rootView.findViewById(R.id.elizaDebug);
  	  elField = (TextView) rootView.findViewById(R.id.elizaField);
  	  ebtSpeak = (Button) rootView.findViewById(R.id.btEliza); 
  	  
  	  stupidf = new FuppetStuff();
  	  stupidf.readScript("script", getActivity());
      
  	  ebtSpeak.setOnClickListener(this);
      
  	 checkVoiceRecognition();
     sr = SpeechRecognizer.createSpeechRecognizer(this.getActivity());       
     sr.setRecognitionListener(new listener());     
	 tts = new TextToSpeech(getActivity(), this);
   	 hashAudio = new HashMap<String, String>();
   	 
     return rootView;
    }
	 
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	 }

	 public void onClick(View view) {
      if (view.getId() == R.id.btEliza) 
      { 	   
		  Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			
	       // Specify the calling package to identify your application
		  intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
		    .getPackage().getName());
	
		  // Given a hint to the recognizer about what the user is going to say
		  //1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
		  //2.LANGUAGE_MODEL_FREE_FORM  : If not sure about the words or phrases and its domain.
		  intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	
		  // Specify how many results you want to receive. The results will be
		  // sorted where the first result is the one with higher confidence.
		  intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
		  
	      sr.startListening(intent);
   	   
	      /*
            String input = elInput.getText().toString();
            String reply = processInput(input);
            elDebug.setText("");
            response(">> " + input);
            response(reply);    	 
		  */
      }
	 }
	 
	    @Override
	    public void onInit(int status) {
	    	Locale locale = new Locale("en", "EN");
	        if (status == TextToSpeech.SUCCESS) {
	            setTtsListener();
	       	 	tts.setPitch(0.5f);
	            switch (tts.isLanguageAvailable(locale))
	            {
	                case TextToSpeech.LANG_AVAILABLE:
	                case TextToSpeech.LANG_COUNTRY_AVAILABLE:
	                case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
	                    Log.d(TAGsp, "SUPPORTED");
	                    tts.setLanguage(locale);
	                    break;
	                case TextToSpeech.LANG_MISSING_DATA:
	                    Log.d(TAGsp, "MISSING_DATA");
	                    // missing data, install it
	                    Intent installIntent = new Intent();
	                    installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	                    startActivity(installIntent);
	                    break;
	                case TextToSpeech.LANG_NOT_SUPPORTED:
	                    Log.d(TAGsp, "NOT SUPPORTED");
	                    break;
	            }
	        }
	        else if (status == TextToSpeech.ERROR) {
	            Log.e(TAGsp, "Error occurred while initializing Text-To-Speech engine");
	        }
	        
	    }
	    private void setTtsListener()
	    {
	        int listenerResult = tts.setOnUtteranceCompletedListener(this);
	        if (listenerResult != TextToSpeech.SUCCESS)
	        {
	            Log.e(TAGsp, "failed to add utterance completed listener");
	        }
	      }
	 

	    
	    /** Voice Recognition stuff **/
		  class listener implements RecognitionListener          
		   {
	            public void onReadyForSpeech(Bundle params)
	            {
                     Log.d(TAGvr, "onReadyForSpeech");
	            }
	            public void onBeginningOfSpeech()
	            {
                     Log.d(TAGvr, "onBeginningOfSpeech");
	            }
	            public void onRmsChanged(float rmsdB) {}
	            public void onBufferReceived(byte[] buffer)
	            {
                     Log.d(TAGvr, "onBufferReceived");
	            }
	            public void onEndOfSpeech()
	            {
                     Log.d(TAGvr, "onEndofSpeech");
	            }
	            public void onError(int error)
	            {
                     Log.e(TAGvr,  "error " +  error);
                     elDebug.setText("error " + error); 
	            }
	            public void onResults(Bundle results)                   
	            {
	            	Log.d(TAGvr, "onResults");
                     ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                     elDebug.setText("What you said: "+data.get(0));
                     String input=null;
                     input = data.get(0).toString();
                     String reply = stupidf.processInput(input);
                     elField.setText(reply);
	                 callSpeechActivity( reply);
	            }
	            public void onPartialResults(Bundle partialResults)
	            {
	            }
	            public void onEvent(int eventType, Bundle params)
	            {
	                     Log.d(TAGvr, "onEvent " + eventType);
	            }
		   }
		 public void checkVoiceRecognition() {
		  // Check if voice recognition is present
		  PackageManager pm = this.getActivity().getPackageManager();
		  List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				  RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		  if (activities.size() == 0) {
			   ebtSpeak.setEnabled(false);
			   ebtSpeak.setText("Voice recognizer not present");
		  	}
		 }
		 
	 public void callSpeechActivity(String results) {
		 String message;
		 if(results.contains("*")) {
			 message = "I do not speak French";
		 } else { message = results; }
	        //Start the thing to talk back				 
            hashAudio.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
            hashAudio.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "fuppet");
            tts.speak(message, TextToSpeech.QUEUE_ADD, hashAudio);
            spListener.onSpeech(true);
		 }
		@Override
		public void onUtteranceCompleted(String utteranceId) {
			Log.e(TAGsp, "completed");
			spListener.onSpeech(false);
		}   
		
	/** external communications **/
    public interface OnSpeechListener {
        public void onSpeech(Boolean talkOn);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            spListener = (OnSpeechListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnSpeechListener");
        }
    }
}

