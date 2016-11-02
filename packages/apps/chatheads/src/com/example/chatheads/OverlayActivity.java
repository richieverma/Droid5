package com.example.chatheads;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Window;

public class OverlayActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.overlay_main);
		//setOverlayParent(R.layout.overlay_main);

		final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
		Log.v("OverlayLog", "Userlog - View group details : " + viewGroup.toString());

		Log.v("OverlayLog", "Userlog - Root ViewGroup (Level 0) : " + viewGroup.getRootView());
		//getAllChildren(viewGroup.getRootView(),0);

		Log.v("OverlayLog", "Userlog - Before calling intent");

		//LayoutInflater inflater = (LayoutInflater) this.getSystemService(this.LAYOUT_INFLATER_SERVICE);
    //Custmv = inflater.inflate(R.layout.overlay_main, viewGroup, true);
		//Log.v("OverlayLog", "Userlog - View details : " + Custmv.toString());
		//Log.v("OverlayLog", "Userlog - View group parent details : " + viewGroup.getParent().toString());

		String CLOCK_PACKAGE_NAME = "com.android.messaging";
		String CLOCK_CLASS_NAME = "com.android.messaging.ui.conversationlist.ConversationListActivity";

    final String CALCULATOR_PACKAGE_NAME = "com.android.calculator2";
    final String CALCULATOR_CLASS_NAME = "com.android.calculator2.Calculator";
    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    intent.setComponent(new ComponentName(CLOCK_PACKAGE_NAME, CLOCK_CLASS_NAME));
		intent.putExtra("parentId",R.layout.overlay_main);
		//intent.setFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
    startActivity(intent);

/*
		Intent intentClock = new Intent();
		intentClock.setAction(Intent.ACTION_MAIN);
		intentClock.addCategory(Intent.CATEGORY_LAUNCHER);
		intentClock.setComponent(new ComponentName(CLOCK_PACKAGE_NAME, CLOCK_CLASS_NAME));
		intentClock.putExtra("overlayLayout", R.layout.overlay_main);
		//intentClock.setFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		//startActivity(intentClock);

		*/

				Log.v("OverlayLog", "Userlog - After calling intent");
    }

		private void getAllChildren(View v, int level) {

    if (!(v instanceof ViewGroup)) {
        Log.v("OverlayLog", "Userlog - Level " + level + ", view : "+v+" (not a ViewGroup)");
        return;
    }
    ViewGroup viewGroup = (ViewGroup) v;
    for (int i = 0; i < viewGroup.getChildCount(); i++) {

        View child = viewGroup.getChildAt(i);
        Log.v("OverlayLog", "Userlog - Level " + level + ", child "+ i + " : " + child + " of ViewGroup : "+v);
        getAllChildren(child,level+1);
    }
    return;
  }

}
