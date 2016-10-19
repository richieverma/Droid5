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


public class OverlayActivity extends Activity {

	View Custmv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.overlay_main);

		final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
		Log.v("OverlayLog", " View group details : " + viewGroup.toString());

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(this.LAYOUT_INFLATER_SERVICE);
    Custmv = inflater.inflate(R.layout.overlay_main, viewGroup, true);

		Log.v("OverlayLog", "Before calling intent");
		Log.v("OverlayLog", " View details : " + Custmv.toString());
		Log.v("OverlayLog", " View group parent details : " + viewGroup.getParent().toString());


    final String CALCULATOR_PACKAGE_NAME = "com.android.calculator2";
    final String CALCULATOR_CLASS_NAME = "com.android.calculator2.Calculator";
    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    intent.setComponent(new ComponentName(CALCULATOR_PACKAGE_NAME, CALCULATOR_CLASS_NAME));
    startActivity(intent);


				Log.v("OverlayLog", "After calling intent");
    }

}
