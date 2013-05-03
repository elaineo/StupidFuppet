package com.elaineou.stupidfuppet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class VoiceActivity extends Fragment implements OnClickListener {
	 public final static String VOICE_RESULT = "com.elaineou.stupidfuppet.RESULT"; 
	 public final static String CLIENT_ID="fuppet";
	 public final static String CLIENT_SECRET="JmYW9cIgtCIyKDixDtnIoUM4cFkTIIdvqfUzeEOBXLI=";
	 public final static String DatamarketAccessUri = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13";
	 public final static String TranslateUri = "http://api.microsofttranslator.com/v2/Http.svc/Translate?text=";
	 private static final String TAG = "VoiceActivity";

	 private TextView mHeard;
	 private TextView mDebug;
	 private Button mbtSpeak;
	 private SpeechRecognizer sr;
	 private String access_token;
	 
	 //eliza stuff
	 // readscript, runprogram


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.activity_voice, container, false);
  	  mDebug = (TextView) rootView.findViewById(R.id.fupDebug);
  	  mHeard = (TextView) rootView.findViewById(R.id.fupHeard);
  	  mbtSpeak = (Button) rootView.findViewById(R.id.btSpeak); 
      mbtSpeak.setOnClickListener(this);
 	 checkVoiceRecognition();
      sr = SpeechRecognizer.createSpeechRecognizer(this.getActivity());       
      sr.setRecognitionListener(new listener());     
//      GetAccessToken accesstok = new GetAccessToken();
//      accesstok.execute();
        return rootView;
    }
	 
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);


           
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
	            public void onRmsChanged(float rmsdB) {}
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
	                     Log.d(TAG,  "error " +  error);
	                     mDebug.setText("error " + error); 
	            }
	            public void onResults(Bundle results)                   
	            {
	            	Log.d(TAG, "onResults");
	                     ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
	                     mHeard.setText("What you said: "+data.get(0));
	                     String output=null;
	                     // create ElizaActivity
	                     output = data.get(0).toString();
		                 callSpeechActivity( output);
	            }
	            public void onPartialResults(Bundle partialResults)
	            {
	            }
	            public void onEvent(int eventType, Bundle params)
	            {
	                     Log.d(TAG, "onEvent " + eventType);
	            }
	   }
	 public void checkVoiceRecognition() {
	  // Check if voice recognition is present
	  PackageManager pm = this.getActivity().getPackageManager();
	  List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
	    RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
	  if (activities.size() == 0) {
	   mbtSpeak.setEnabled(false);
	   mbtSpeak.setText("Voice recognizer not present");
	   Toast.makeText(this.getActivity(), "Voice recognizer not present",
	     Toast.LENGTH_SHORT).show();
	  }
	 }

	 public void onClick(View view) {
      if (view.getId() == R.id.btSpeak) 
      { 	   
   	   
		  Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	
	       // Specify the calling package to identify your application
		  intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
		    .getPackage().getName());
	
		  // Given an hint to the recognizer about what the user is going to say
		  //There are two form of language model available
		  //1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
		  //2.LANGUAGE_MODEL_FREE_FORM  : If not sure about the words or phrases and its domain.
		  intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	
		  // Specify how many results you want to receive. The results will be
		  // sorted where the first result is the one with higher confidence.
		  intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
		  
	      sr.startListening(intent);
      }
	 }
	 public void callSpeechActivity(String results) {
		 
        //Start the thing to talk back
		 
        Intent intent = new Intent(this.getActivity(), SpeechActivity.class);
        intent.putExtra(VOICE_RESULT, results);
		startActivity(intent);
		 
	 }
	 
	 /**
	 * Helper method to show the toast message
	 **/
	 void showToastMessage(String message){
	  Toast.makeText(this.getActivity(), message, Toast.LENGTH_SHORT).show();
	 }
	 class TranslateText extends AsyncTask<String,Void,String> {
		 @Override
		 protected String doInBackground(String... text) {		      
		      // Creating HTTP client
		      HttpClient httpClient = new DefaultHttpClient();
		      String safetext = URLEncoder.encode(text[0]);
		      HttpGet httpGet = new HttpGet(TranslateUri+safetext+"&from=en&to=es");
		      httpGet.addHeader("Authorization", "Bearer "+access_token);
		      
			 //translate stuff
		      HttpResponse response;
			try {
		      response = httpClient.execute(httpGet);
	          BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
	          StringBuilder builder = new StringBuilder();
	          for (String line = null; (line = reader.readLine()) != null;) {
	              builder.append(line).append("\n");
	          }
	          String translatedTxt = android.text.Html.fromHtml(builder.toString()).toString();
	          Log.d("Http Response:", builder.toString());
	          return translatedTxt;
			} catch (ClientProtocolException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
		 }
	    protected void onPostExecute(String result) {
	        mDebug.setText("I say: "+result);
	        returnTT(result);
	    }
	    private String returnTT(String result){
	    	return result;
	    }
	}

	 class GetAccessToken extends AsyncTask<Void,Void,String> {
		 @Override
		 protected String doInBackground(Void... params) {		      
		      // Creating HTTP client
		      HttpClient httpClient = new DefaultHttpClient();
		      HttpPost httpPost = new HttpPost(DatamarketAccessUri);
		      
		      List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
		      nameValuePair.add(new BasicNameValuePair("client_id", CLIENT_ID));
		      nameValuePair.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));
		      nameValuePair.add(new BasicNameValuePair("scope", "http://api.microsofttranslator.com"));
		      nameValuePair.add(new BasicNameValuePair("grant_type", "client_credentials"));
		      // Url Encoding the POST parameters
		      try {
		          httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
		      } catch (UnsupportedEncodingException e) {
		          // writing error to Log
		          e.printStackTrace();
		      }
		      // Making HTTP Request
		      try {
		          HttpResponse response = httpClient.execute(httpPost);
		          BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		          StringBuilder builder = new StringBuilder();
		          for (String line = null; (line = reader.readLine()) != null;) {
		              builder.append(line).append("\n");
		          }
		          Log.d("Http Response:", builder.toString());
		          try {
			          JSONObject tokener = new JSONObject(builder.toString());
			          String key = tokener.getString("access_token");
				      return key;
		          } catch (JSONException e) {
		              // TODO Auto-generated catch block
		              e.printStackTrace();
		          }    
		          // writing response to log
		      } catch (ClientProtocolException e) {
		          // writing exception to log
		          e.printStackTrace();
		      } catch (IOException e) {
		          // writing exception to log
		          e.printStackTrace();
		      }
		      return null;
	   }
	    protected void onPostExecute(String result) {
	        access_token=result;
	        Log.d("accesstoken",result);
	    }
	 }
}

