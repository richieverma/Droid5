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
import android.util.Log;
import 	android.content.Context;

import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Parcelable;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import android.app.Activity;
import android.support.v4.content.LocalBroadcastManager;

public class ChatHeadService extends Service {

	private WindowManager windowManager;
	private ImageView chatHead;
	WindowManager.LayoutParams params;
	List<Intent> targetedShareIntents;

	private String startActivityIntent = "START_ACTIVITY_INTENT"; 
	private String stopActivityIntent  = "STOP_ACTIVITY_INTENT"; 

	private String INTENT_OPERATION_TEXT= "INTENT_OPERATION_TEXT"; 
	
	public void getListOfAllIntents() {
	    Log.v("ChatHeadService","Content Chooser");
		PackageManager pm = getPackageManager();
		
		//Create new Intent with specific action and category to get a list of applications
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		//Get a list of apps with the properties of intent created above
		List<ResolveInfo> launchables=pm.queryIntentActivities(intent, 0);
		Collections.sort(launchables,new ResolveInfo.DisplayNameComparator(pm));

		targetedShareIntents = new ArrayList<Intent>();
		
		for (ResolveInfo launch : launchables) {
			String packageName = launch.activityInfo.packageName;
			Log.v("ChatHeadService","LaunchableAppPackageName:"+packageName);
			Intent targetedShareIntent = new Intent();
			targetedShareIntent.setPackage(packageName);
			targetedShareIntent.putExtra("parentId",1);
			//Add package to Intent Chooser list
			targetedShareIntents.add(targetedShareIntent);
		}

	}
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
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.x = 0;
		params.y = 0;

		//getListOfAllIntents();
		Intent i = new Intent();
		i.setClass(this, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);

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


            		//Log.v("ChatHeadService", "TargetIntentSize"+ Integer.toString(targetedShareIntents.size()));
					// Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Select Overlay App");
					// chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[targetedShareIntents.size()]));
					Context context = MainActivity.getAppContext();
					// activity.startActivityForResult(chooserIntent,1001);
					Intent intent = new Intent(MainActivity.START_ACTIVITY);
					// You can also include some extra data.
					intent.putExtra(Intent.EXTRA_TEXT,startActivityIntent);
					LocalBroadcastManager.getInstance(context).sendBroadcast(intent);					
					//sendBroadcast(new Intent(MainActivity.START_ACTIVITY));
					// final String OVERLAY_PACKAGE_NAME = "com.example.chatheads";
    	// 			final String OVERLAY_CLASS_NAME = "com.example.chatheads.OverlayActivity";
     //     			Intent intent = new Intent();
     //     			intent.setAction(Intent.ACTION_MAIN);
     //     			intent.addCategory(Intent.CATEGORY_LAUNCHER);
     //     			intent.setComponent(new ComponentName(OVERLAY_PACKAGE_NAME, OVERLAY_CLASS_NAME));
     //     			startActivity(intent);

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
		//chatHead.bringToFront();
		//chatHead.setClickable(true);
		windowManager.addView(chatHead, params);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (chatHead != null)
		{
			Context context = MainActivity.getAppContext();
			Intent intent = new Intent(MainActivity.START_ACTIVITY);
			// You can also include some extra data.
			intent.putExtra(Intent.EXTRA_TEXT,stopActivityIntent);
			//intent.putExtra("ChatHeadService", stopActivityIntent);
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
			windowManager.removeView(chatHead);

		}
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
