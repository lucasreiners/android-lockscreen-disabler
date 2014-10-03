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
 *
 * <b>Note:</b> This has an unintended consequence where, when the preference is set in the 
 * plugin for automated task it will actually write the current preference to the file.
 * <p> e.g. Lets say the current preference is "device".
 * <p> Now create a task using the plugin to set the preference to "none"
 * <p> When you go back to the SharedPreferences, the current value is actually "none" which
 * is not expected since creating tasks is only for automation and should not affect the current
 * setting. One way to overcome this will be to extend from Activity and create the preference 
 * layout.
 */
public class PluginActivity extends PreferenceActivity {

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
      ListPreference typePref = (ListPreference) findPreference("lockscreentype");
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
    final String SETTINGS_LOCKSCREENTYPE = "lockscreentype";

    PreferenceManager.setDefaultValues(this, R.xml.pref_plugin, true);
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

    final String lockScreenType = sharedPref.getString(SETTINGS_LOCKSCREENTYPE, "notset");
    final String blurb = "Lockscreentype value set: " + lockScreenType;

    final Intent resultIntent = new Intent();
    final Bundle resultBundle = new Bundle();
    resultBundle.putString(SETTINGS_LOCKSCREENTYPE, lockScreenType);
    resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BUNDLE", resultBundle);
    resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BLURB", blurb);

    this.setResult(RESULT_OK, resultIntent);
    super.finish();
  }
}