package com.elaineou.stupidfuppet;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class MainActivity extends FragmentActivity {
	    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		//VoiceActivity vfragment = new VoiceActivity();
		ElizaActivity vfragment = new ElizaActivity();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.voice_fragment, vfragment).commit();
		FuppetActivity ffragment = new FuppetActivity();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fuppet_fragment, ffragment).commit();
        
	}
}

