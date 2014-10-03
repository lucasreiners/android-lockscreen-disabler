package com.lr.keyguarddisabler;

import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.SharedPreferences;

import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * This class creates a simplified (lockscreentype only) preference for the tasker/locale
 * plugin. Once the desired preference is set using this plugin, then whenever this task is
 * run the preference will be set through the broadcast receiver.
 */
public class PluginActivity extends PreferenceActivity {

  final String SETTINGS_LOCKSCREENTYPE = "lockscreentype";
  final String PLUGIN_LOCKSCREENTYPE = "plugin_lockscreentype";

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
       
    setupSimplePreferencesScreen();
  }

  /** 
   * Replace the general preference of the application with a simpler preference for the
   * plugin
   */
  private void setupSimplePreferencesScreen() {
    getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
    addPreferencesFromResource(R.xml.pref_plugin);

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
      ListPreference typePref = (ListPreference) findPreference(PLUGIN_LOCKSCREENTYPE);
      typePref.setEntries(R.array.lockscreenTypePreJellyBean);
      typePref.setEntryValues(R.array.lockscreenTypeValuesPreJellyBean);
      typePref.setDefaultValue("none");
    }
  }

  /** 
   * Sends the preference back to the Tasker/Locale for reporting and to indicate
   * the activity finished successfully
   */
  @Override
  public void finish() {

    PreferenceManager.setDefaultValues(this, R.xml.pref_plugin, true);
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

    // Get the temporary PLUGIN_LOCKSCREENTYPE value
    final String lockScreenType = sharedPref.getString(PLUGIN_LOCKSCREENTYPE, "notset");
    final String blurb = "Lockscreentype value set: " + lockScreenType;

    final Intent resultIntent = new Intent();
    final Bundle resultBundle = new Bundle();
    // SETTINGS_LOCKSCREENTYPE key/value pair to send to the broadcast receiver
    resultBundle.putString(SETTINGS_LOCKSCREENTYPE, lockScreenType);
    resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BUNDLE", resultBundle);
    resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BLURB", blurb);

    this.setResult(RESULT_OK, resultIntent);
    super.finish();
  }
}