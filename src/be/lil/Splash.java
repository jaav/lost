package be.lil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Locale;

public class Splash extends Activity implements View.OnClickListener{
  private Locale locale;


  private Runnable timeTask = new Runnable() {
    public void run() {
      goHome();
    }
  };

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.splash);
    applyPreferences();
    ImageButton showMaps = (ImageButton)findViewById(R.id.showMapButton);
    showMaps.setOnClickListener(this);
    ImageButton showPhotos = (ImageButton)findViewById(R.id.showPhotoButton);
    showPhotos.setOnClickListener(this);
    ImageButton showSearch = (ImageButton)findViewById(R.id.showListButton);
    showSearch.setOnClickListener(this);

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
    inflater.inflate(R.menu.justprefsmenu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      // Handle item selection
      switch (item.getItemId()) {
      case R.id.preferences:
        startActivity(new Intent(this, PreferencesActivity.class));
        break;
      default:
      }
      return super.onOptionsItemSelected(item);

  }

  private void goHome(){
    startActivity(new Intent(this, MapOverviewActivity.class));
  }

  @Override
  public void onClick(View view) {
    if(view.getId() == R.id.showMapButton)
      startActivity(new Intent(this, MapOverviewActivity.class));
    if(view.getId() == R.id.showPhotoButton)
      startActivity(new Intent(this, ViewSpotActivity.class));
    if(view.getId() == R.id.showListButton)
      startActivity(new Intent(this, SpotListActivity.class));
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
