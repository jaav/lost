package be.lil;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: jefw
 * Date: 1/28/11
 * Time: 3:08 PM
 * To change this template use File | Settings | File Templates.
 */

public class PreferencesActivity extends PreferenceActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  static final String KEY_LOCALE = "locale";
  private ListPreference localePref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
    PreferenceScreen preferences = getPreferenceScreen();
    preferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    localePref = (ListPreference) preferences.findPreference(KEY_LOCALE);
    String lang = getBaseContext().getResources().getConfiguration().locale.getLanguage();
    localePref.setSummary(lang);
	}

	@Override
	public void onContentChanged() {
    super.onContentChanged();
	}

  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (key.equals(KEY_LOCALE)) {
      localePref.setSummary(localePref.getValue());
    }

    Configuration config = new Configuration();
    config.locale = new Locale(localePref.getValue());
    getBaseContext().getResources().updateConfiguration(config,
      getBaseContext().getResources().getDisplayMetrics());

    //startActivity(new Intent(this, Forecast.class));
  }
}