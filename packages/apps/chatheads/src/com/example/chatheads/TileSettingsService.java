package com.example.chatheads;

import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.content.Intent;

public class TileSettingsService extends TileService {
    private final int STATE_OFF = 0;
    private final int STATE_ON = 1;
    private final String LOG_TAG = "MyTileService";
    private int toggleState = STATE_ON;

    @Override
    public void onTileAdded() {
        Log.v(LOG_TAG, "onTileAdded.1");
    }

    @Override
    public void onTileRemoved() {
        Log.v(LOG_TAG, "onTileRemoved");
    }	

    @Override
    public void onStartListening () {
        Log.v(LOG_TAG, "onStartListening");
    }

    @Override
    public void onStopListening () {
        Log.v(LOG_TAG, "onStopListening");
    }    

    @Override
    public void onClick() {
    	try {
	        Log.v(LOG_TAG, "onClick state = " + Integer.toString(getQsTile().getState()));
	        Icon icon;
	        if (toggleState == STATE_ON) {
	            toggleState = STATE_OFF;
	            icon =  Icon.createWithResource(getApplicationContext(), R.drawable.turn_on);
                /*stop the service*/
                stopService(new Intent(getApplication(), ChatHeadService.class));
	        } else {
                /*turn on the service*/
	            toggleState = STATE_ON;
	            icon = Icon.createWithResource(getApplicationContext(), R.drawable.turn_off);
                startService(new Intent(getApplication(), ChatHeadService.class));
	        }
	        getQsTile().setIcon(icon);
	        getQsTile().updateTile();
	    } catch(Exception ex) {
	    	Log.v(LOG_TAG,"Exception in TileSettingsService " + ex.getMessage());
	    }
    }    
}
