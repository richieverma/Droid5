package com.example.chatheads;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.content.ComponentName;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.util.Log;

public class ChatHeadService extends Service {

	private WindowManager windowManager;
	private ImageView chatHead;
	WindowManager.LayoutParams params;

	@Override
	public void onCreate() {
		super.onCreate();

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		chatHead = new ImageView(this);
		chatHead.setImageResource(R.drawable.face1);

		params= new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.x = 0;
		params.y = 0;

		chatHead.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		        Log.v("chatHead","OnClickListener called");
		    }
		});
		//this code is for dragging the chat head
		chatHead.setOnTouchListener(new View.OnTouchListener() {
			private int initialX;
			private int initialY;
			private float initialTouchX;
			private float initialTouchY;

			private int finalTouchX, finalTouchY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					initialX = params.x;
					initialY = params.y;


					initialTouchX = event.getRawX();
					initialTouchY = event.getRawY();
					// Log.v("chatHeadTouch","DOWN_initialX " + initialX + "=" + initialY);
					// Log.v("chatHeadTouch","DOWN_initialTouch " + event.getRawX() + "=" + event.getRawY());
					return true;
				case MotionEvent.ACTION_UP:
					//Does not work
					//startService(new Intent(getApplication(), Calculator.class));
					/* Does not work
					Intent i = new Intent();
					i.setAction(Intent.ACTION_MAIN);
					i.addCategory(Intent.CATEGORY_APP_CALCULATOR);
					startActivity(i);*/


					//WORKS!
					Log.v("chatHeadTouch","X_DIFF " + Math.abs(finalTouchX-initialX) );
					Log.v("chatHeadTouch","Y_DIFF " + Math.abs(finalTouchY-initialY) );
					if( Math.abs(finalTouchX-initialX) >= 1
						|| Math.abs(finalTouchY-initialY) >= 1 ) {
						return true;
					}
					final String OVERLAY_PACKAGE_NAME = "com.example.chatheads";
    				final String OVERLAY_CLASS_NAME = "com.example.chatheads.OverlayActivity";
         			Intent intent = new Intent();
         			intent.setAction(Intent.ACTION_MAIN);
         			intent.addCategory(Intent.CATEGORY_LAUNCHER);
         			intent.setComponent(new ComponentName(OVERLAY_PACKAGE_NAME, OVERLAY_CLASS_NAME));
         			startActivity(intent);

         			//Does not work
         			//Intent i = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALCULATOR);
					//startActivity(i);
					return true;
				case MotionEvent.ACTION_MOVE:
					params.x = initialX
							+ (int) (event.getRawX() - initialTouchX);
					params.y = initialY
							+ (int) (event.getRawY() - initialTouchY);

					finalTouchX = params.x;
					finalTouchY = params.y;
					windowManager.updateViewLayout(chatHead, params);
					return true;
				}
				return false;
			}
		});
		chatHead.bringToFront();
		chatHead.setClickable(true); 
		windowManager.addView(chatHead, params);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (chatHead != null) 
			windowManager.removeView(chatHead);
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
