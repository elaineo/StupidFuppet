package com.elaineou.stupidfuppet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class FuppetSurface extends SurfaceView implements SurfaceHolder.Callback{
    private FuppetThread _thread;
    Bitmap _fface= BitmapFactory.decodeResource(getResources(),
            R.drawable.fuppet_head);
    
    public FuppetSurface(Context context) {
        super(context);
        getHolder().addCallback(this);
        _thread = new FuppetThread(getHolder(), this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(_fface, 10, 10, null);

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