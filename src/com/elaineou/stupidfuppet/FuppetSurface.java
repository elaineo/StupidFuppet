package com.elaineou.stupidfuppet;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class FuppetSurface extends SurfaceView implements SurfaceHolder.Callback{
	private static final String TAG = "FuppetSVActivity";
	final static int INTERVAL=100;
	final static int MouthCycle=1000;
	
    private FuppetThread _thread;
    Bitmap _fface= BitmapFactory.decodeResource(getResources(),
            R.drawable.fuppet_head);
    Bitmap _feye= BitmapFactory.decodeResource(getResources(),
            R.drawable.fuppet_eye);
    Bitmap _fmouth= BitmapFactory.decodeResource(getResources(),
            R.drawable.fuppet_mouth);
    Rect bgRect = new Rect(100,400,1500,1300);
    Rect leRect = new Rect(500,650,600,750);
    Rect reRect = new Rect(900,650,1000,750);
    Rect moRect = new Rect(400,900,1200,1100);
    
    Boolean enableFuppetMouth=false;
    int elapsed;
    TimerTask talkMouth=new TimerTask(){
        @Override
        public void run() {
        	if (enableFuppetMouth) {
        		elapsed+=INTERVAL;
        	} else {elapsed=0;}
            moRect.bottom = 930+170*(elapsed/MouthCycle);
            if (elapsed>MouthCycle) {
            	elapsed=0;
            }
        }
    };
    Timer timerMouth = new Timer();
    
    public FuppetSurface(Context context) {
        super(context);
        getHolder().addCallback(this);
        timerMouth.scheduleAtFixedRate(talkMouth,INTERVAL,INTERVAL);
        _thread = new FuppetThread(getHolder(), this);
    }
    
    void animateFuppet(Boolean talk) {
    	if (talk) {
    		elapsed=0;
    		enableFuppetMouth=true;
    	} else {Log.d(TAG,"Stop Talking");
    		enableFuppetMouth=false;}
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(_fface, null, bgRect, null);
        canvas.drawBitmap(_feye, null, leRect, null);
        canvas.drawBitmap(_feye, null, reRect, null);
        canvas.drawBitmap(_fmouth, null, moRect, null);
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }

    public void surfaceCreated(SurfaceHolder arg0) {
        _thread.setRunning(true);
        _thread.start();
    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
        boolean retry = true;
        _thread.setRunning(false);
        while (retry) {
            try {
                _thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    class FuppetThread extends Thread {
        private SurfaceHolder _surfaceHolder;
        private FuppetSurface _panel;
        private boolean _run = false;

        public FuppetThread(SurfaceHolder surfaceHolder, FuppetSurface panel) {
            _surfaceHolder = surfaceHolder;
            _panel = panel;
        }

        public void setRunning(boolean run) {
            _run = run;
        }

        @SuppressLint("WrongCall")
		@Override
        public void run() {
            Canvas c;
            while (_run) {
                c = null;
                try {
                    c = _surfaceHolder.lockCanvas(null);
                    synchronized (_surfaceHolder) {
                        _panel.onDraw(c);
                    }
                } finally {
                    if (c != null) {
                        _surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }    

     
   }