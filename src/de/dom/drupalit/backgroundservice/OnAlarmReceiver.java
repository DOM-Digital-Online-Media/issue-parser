package de.dom.drupalit.backgroundservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class OnAlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = context.getSharedPreferences("drupal", Context.MODE_PRIVATE);
		boolean doAutoUpdate = prefs.getBoolean("notify", false);

		if (doAutoUpdate) {
			WakefulIntentService.acquireStaticLock(context);
			context.startService(new Intent(context, AppService.class));
		}
	}
}