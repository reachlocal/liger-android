package com.reachlocal.mobile.liger.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.reachlocal.mobile.liger.LIGER;

public class LigerGcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that GcmIntentService will handle the intent.
        Log.e(LIGER.TAG, "LigerGcmBroadcastReceiver  onReceive");
        ComponentName comp = new ComponentName(context.getPackageName(),
                LigerGcmIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }

}