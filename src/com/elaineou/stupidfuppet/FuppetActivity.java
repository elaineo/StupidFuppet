package com.elaineou.stupidfuppet;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class FuppetActivity extends Fragment {
	FuppetSurface fuppetSV;
		   
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {

        // Inflate the layout for this fragment
    	/*
        View rootView = inflater.inflate(R.layout.activity_fuppet, container, false);
        ImageView image = (ImageView) rootView.findViewById(R.id.frog_image);
        return rootView;
        */
    	
        fuppetSV = new FuppetSurface(getActivity());
        return fuppetSV;
        
    }
}

