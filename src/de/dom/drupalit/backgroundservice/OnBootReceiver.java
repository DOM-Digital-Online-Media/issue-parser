package de.dom.drupalit.backgroundservice;
import de.dom.drupalit.tabs.TabProfile;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

public class OnBootReceiver extends BroadcastReceiver {
  
  @Override
  public void onReceive(Context context, Intent intent) {
    TabProfile.registerAlarm(context);
  }
}