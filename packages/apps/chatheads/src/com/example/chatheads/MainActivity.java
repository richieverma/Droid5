package com.example.chatheads;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.content.IntentFilter;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Parcelable;

// Our handler for received Intents. This will be called whenever an Intent
// with an action named "custom-event-name" is broadcasted.
public class MainActivity extends Activity {
	Button startService,stopService;
    private static Context context;
    private String appName;
    public static String START_ACTIVITY = "START_ACTIVITY";
    private String startActivityIntent  = "START_ACTIVITY_INTENT"; 
    private String stopActivityIntent   = "STOP_ACTIVITY_INTENT"; 
    private String INTENT_OPERATION_TEXT= "INTENT_OPERATION_TEXT"; 
    
    List<Intent> targetedShareIntents;
    //private DataUpdateReceiver dataUpdateReceiver;
    private BroadcastReceiver mMessageReceiver;
    public static Context getAppContext() {
	        return MainActivity.context;
	}	
	private BroadcastReceiver onNotice;
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
		targetedShareIntents.add(new Intent());
		
		for (ResolveInfo launch : launchables) {
			String packageName = launch.activityInfo.packageName;
			//Log.v("ChatHeadService","LaunchableAppPackageName:"+packageName);
			Intent targetedShareIntent = new Intent();
			targetedShareIntent.setPackage(packageName);
			targetedShareIntent.putExtra("parentId",1);
			targetedShareIntent.setAction(Intent.ACTION_MAIN);
			targetedShareIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			//Add package to Intent Chooser list
			targetedShareIntents.add(targetedShareIntent);
		}

	}
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
		super.onDestroy();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		getListOfAllIntents();
		mMessageReceiver = new BroadcastReceiver() {
		  @Override
		  public void onReceive(Context context, Intent intent) {
		    // Get extra data included in the Intent
		    String message =intent.getExtras().getString(Intent.EXTRA_TEXT);
		    Log.v("ChatHeadService", "Got message: " + message);
			
		    if (message == null) {
		    	Log.v("ChatHeadService","MESSAGE NULL !!!");
		    	return;
		    }
		    if(message.equals(startActivityIntent)) {
				Intent chooserIntent = Intent.createChooser(targetedShareIntents.get(0), "Select Overlay App");
				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[targetedShareIntents.size()]));
			    startActivityForResult(chooserIntent,1001);

		    } else if (message.equals(stopActivityIntent)){
		    	finishActivity(1001);
		    	finish();
		    }	
		  }
		};

		appName = "com.example.chatheads.Main";
		context = getApplicationContext();
	  // Register to receive messages.
	  // We are registering an observer (mMessageReceiver) to receive Intents
	  // with actions named "custom-event-name".
	  LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
	      new IntentFilter(MainActivity.START_ACTIVITY));		
	}
}
