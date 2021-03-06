package be.lil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import java.util.Locale;

public class AboutActivity extends Activity implements View.OnClickListener{
  private Locale locale;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    applyPreferences();
    setContentView(R.layout.aboutview);
    ImageButton goBack = (ImageButton)findViewById(R.id.goBackButton);
    goBack.setOnClickListener(this);
  }

  @Override
  public void onResume(){
    super.onResume();
    /*mHandler = new Handler();
    mHandler.postDelayed(timeTask, 5000);*/
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.prefsandintromenu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      // Handle item selection
      switch (item.getItemId()) {
      case R.id.preferences:
        startActivity(new Intent(this, PreferencesActivity.class));
        break;
      case R.id.intro:
        startActivity(new Intent(this, IntroActivity.class));
        break;
      default:
      }
      return super.onOptionsItemSelected(item);

  }

  @Override
  public void onClick(View view) {
    if(view.getId() == R.id.goBackButton)
      finish();
  }


  private void applyPreferences(){
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    locale = new Locale(prefs.getString(PreferencesActivity.KEY_LOCALE, "en"));
    Configuration config = new Configuration();
    config.locale = locale;
    getBaseContext().getResources().updateConfiguration(config,
      getBaseContext().getResources().getDisplayMetrics());
  }
}