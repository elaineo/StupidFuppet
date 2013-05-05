package com.elaineou.stupidfuppet;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.elaineou.stupidfuppet.ElizaActivity.OnSpeechListener;

public class MainActivity extends FragmentActivity implements OnSpeechListener{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		ElizaActivity vfragment = new ElizaActivity();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.voice_fragment, vfragment).commit();
		FuppetActivity ffragment = new FuppetActivity();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fuppet_fragment, ffragment).commit();
        
	}
	@Override
    public void onSpeech(Boolean talkOn) {
        // TODO Auto-generated method stub
         FuppetActivity Obj=(FuppetActivity) getSupportFragmentManager().findFragmentById(R.id.fuppet_fragment);
         Obj.animateSpeech(talkOn);
    }

}

