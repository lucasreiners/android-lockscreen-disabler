package com.lr.keyguarddisabler;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.util.Log;
import android.os.Bundle;

/**
 * The broadcast receiver will receive the intent action and lockscreen disabler preference
 * data and sets the preference directly in the SharedPreferences file.
 * Once set the preference takes effect immediately. This can be used to automate changing
 * preferences (for e.g. from Tasker or other tools capable of sending intents
 * <p> e.g. of passing intents
 * <p> adb shell am broadcast -e lockscreentype device -n com.lr.keyguarddisabler/.SettingsReceiver
 * <p> adb shell am broadcast -a com.lr.keyguarddisabler.action.SET_PREFERENCE -e lockscreentype device
 */
public class SettingsReceiver extends BroadcastReceiver {
  private static final String TAG = "SettingsReceiver";
  public SettingsReceiver() {
    Log.i(TAG, "Inside SettingsReceiver");
  }

  /**
   * {@inheritDoc}
   */
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    Log.i(TAG, "SettingsReceiver:" + action);
    final Bundle pluginBundle = intent.getBundleExtra("com.twofortyfouram.locale.intent.extra.BUNDLE");

    // Get the shared preferences to store the new lockscreentype value
    SharedPreferences sp = context.getSharedPreferences("com.lr.keyguarddisabler_preferences", context.MODE_WORLD_READABLE);
    SharedPreferences.Editor editor = sp.edit();

    // Get the value from the intent extra and store it in preference
    String lockScreenValue = pluginBundle.getString("lockscreentype");
    Log.i(TAG, "Lock screen value to set: " + lockScreenValue);
    editor.putString("lockscreentype", lockScreenValue).commit();
  }
}