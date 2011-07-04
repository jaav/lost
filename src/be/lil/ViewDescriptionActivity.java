package be.lil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.*;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: jefw
 * Date: 4/26/11
 * Time: 5:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class ViewDescriptionActivity extends Activity implements View.OnClickListener {

  private static String TAG = "LIL - ViewPicsActivity";
  private SpotsData spotsData;
  private Locale locale;
  private Spot currentSpot;
  private long spot_id;
  private Location currentLocation;
  private boolean locationAvailable = true;
  private LocationManager locationManager;
  private LocationListener listenerCoarse;
  private LocationListener listenerFine;
  private Location debugLocation = new Location("");

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent message = getIntent();
    applyPreferences();
    spot_id = message.getLongExtra("spot_id", -1);
    //50.646821,5.577106
    debugLocation.setLatitude(50.646821);
    debugLocation.setLongitude(5.577106);
  }

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onResume() {
    super.onResume();
    setContentView(R.layout.descriptionview);
    DataFetchingTask fetcher = new DataFetchingTask(this, locale);
    fetcher.execute();
    ImageButton showMaps = (ImageButton) findViewById(R.id.showMapButton);
    showMaps.setOnClickListener(this);
    ImageButton goBack = (ImageButton) findViewById(R.id.goBackButton);
    goBack.setOnClickListener(this);
    ImageButton go = (ImageButton) findViewById(R.id.goButton);
    go.setOnClickListener(this);
    registerLocationListeners();
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
      case R.id.about:
        startActivity(new Intent(this, AboutActivity.class));
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
    if (view.getId() == R.id.showMapButton)
      startActivity(new Intent(this, MapOverviewActivity.class));
    /*if(view.getId() == R.id.showPhotoButton)
      startActivity(new Intent(this, ViewSpotActivity.class));*/
    if (view.getId() == R.id.goBackButton)
      finish();
    /*{
      Intent spotIntent = new Intent(this, ViewSpotActivity.class);
      spotIntent.putExtra("spotOrder", currentSpot.getSpotorder());
      startActivity(spotIntent);
    }*/
    if (view.getId() == R.id.goButton) {
      Intent mapIntent;
      double dist = getDistanceInM(currentLocation.getLatitude(), currentLocation.getLongitude(), currentSpot.getY(), currentSpot.getX());
      if(dist > 400){
        mapIntent = new Intent(this, MapOverviewActivity.class);
        mapIntent.putExtra("centerX", currentSpot.getX());
        mapIntent.putExtra("centerY", currentSpot.getY());
      }
      else if(dist > 200){
        mapIntent = new Intent(this, MapOverviewActivity.class);
        mapIntent.putExtra("centerX", (float)((currentSpot.getX()+currentLocation.getLongitude())/2));
        mapIntent.putExtra("centerY", (float)((currentSpot.getY()+currentLocation.getLatitude())/2));
        mapIntent.putExtra("myX", (float)(currentLocation.getLongitude()));
        mapIntent.putExtra("myY", (float)(currentLocation.getLatitude()));
      }
      else if(dist > 100){
        mapIntent = new Intent(this, MapDetailActivity.class);
        mapIntent.putExtra("centerX", (float)((currentSpot.getX()+currentLocation.getLongitude())/2));
        mapIntent.putExtra("centerY", (float)((currentSpot.getY()+currentLocation.getLatitude())/2));
        mapIntent.putExtra("myX", (float)(currentLocation.getLongitude()));
        mapIntent.putExtra("myY", (float)(currentLocation.getLatitude()));
      }
      else{
        mapIntent = new Intent(this, MapSuperZoomActivity.class);
        mapIntent.putExtra("centerX", (float)((currentSpot.getX()+currentLocation.getLongitude())/2));
        mapIntent.putExtra("centerY", (float)((currentSpot.getY()+currentLocation.getLatitude())/2));
        mapIntent.putExtra("myX", (float)(currentLocation.getLongitude()));
        mapIntent.putExtra("myY", (float)(currentLocation.getLatitude()));
      }
      mapIntent.putExtra("spot_id", currentSpot.getId());
      startActivity(mapIntent);
    }
  }

  private void useData() {
    TextView title = (TextView) findViewById(R.id.title);
    title.setText(currentSpot.getName());
    TextView description = (TextView) findViewById(R.id.desc);
    description.setText(currentSpot.getDescription());
  }


  private void applyPreferences() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    locale = new Locale(prefs.getString(PreferencesActivity.KEY_LOCALE, "en"));
    Configuration config = new Configuration();
    config.locale = locale;
    getBaseContext().getResources().updateConfiguration(config,
      getBaseContext().getResources().getDisplayMetrics());
  }

  private class DataFetchingTask extends AsyncTask<Void, Void, Void> {

    private Context localAppContext;
    private Locale localLocale;

    public DataFetchingTask(Context appContext, Locale locale) {
      localAppContext = appContext;
      localLocale = locale;
    }

    @Override
    protected Void doInBackground(Void... params) {
      spotsData = new SpotsData(localAppContext);

      Cursor cursor = spotsData.getSpotJoinedDescsCursor(spot_id, locale.getLanguage());
      cursor.moveToFirst();
      try {
        currentSpot = new Spot(cursor.getLong(0), cursor.getString(3), cursor.getString(4), cursor.getInt(2), cursor.getFloat(5), cursor.getFloat(6));
      } catch (ParseException e) {
        e.printStackTrace();
      }
      cursor.close();
      return null;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onPostExecute(Void result) {
      useData();
    }
  }

  private void registerLocationListeners() {
    locationManager = (LocationManager)
      getSystemService(LOCATION_SERVICE);

    // Initialize criteria for location providers
    Criteria fine = new Criteria();
    fine.setAccuracy(Criteria.ACCURACY_FINE);
    Criteria coarse = new Criteria();
    coarse.setAccuracy(Criteria.ACCURACY_COARSE);

    // Get at least something from the device,
    // could be very inaccurate though
    currentLocation = locationManager.getLastKnownLocation(
      locationManager.getBestProvider(fine, true));
    //currentLocation = debugLocation;

    if (listenerFine == null || listenerCoarse == null)
      createLocationListeners();

    // Will keep updating about every 500 ms until accuracy is about 1000 meters to get quick fix.
    locationManager.requestLocationUpdates(
      locationManager.getBestProvider(coarse, true), 500, 1000, listenerCoarse);
    // Will keep updating about every 500 ms until accuracy is about 50 meters to get accurate fix.
    locationManager.requestLocationUpdates(
      locationManager.getBestProvider(fine, true), 500, 50, listenerFine);
  }

  /**
   * Creates LocationListeners
   */
  private void createLocationListeners() {

    listenerCoarse = new LocationListener() {
      public void onStatusChanged(String provider,
                                  int status, Bundle extras) {
        switch (status) {
          case LocationProvider.OUT_OF_SERVICE:
          case LocationProvider.TEMPORARILY_UNAVAILABLE:
            locationAvailable = false;
            break;
          case LocationProvider.AVAILABLE:
            locationAvailable = true;
        }
      }

      public void onProviderEnabled(String provider) {
      }

      public void onProviderDisabled(String provider) {
      }

      public void onLocationChanged(Location location) {
        if (location.getAccuracy() > 1000 && location.hasAccuracy())
          locationManager.removeUpdates(this);
        else {
          if(currentLocation == null) currentLocation = location;
          else if (getDistanceInM(
            currentLocation.getLatitude(),
            currentLocation.getLongitude(),
            location.getLatitude(),
            location.getLongitude()) > 100) {
            currentLocation = location;
            //currentLocation = debugLocation;
          }
        }
      }
    };

    listenerFine = new LocationListener() {
      public void onStatusChanged(String provider,
                                  int status, Bundle extras) {
        switch (status) {
          case LocationProvider.OUT_OF_SERVICE:
          case LocationProvider.TEMPORARILY_UNAVAILABLE:
            locationAvailable = false;
            break;
          case LocationProvider.AVAILABLE:
            locationAvailable = true;
        }
      }

      public void onProviderEnabled(String provider) {
      }

      public void onProviderDisabled(String provider) {
      }

      public void onLocationChanged(Location location) {
        if (location.getAccuracy() > 1000 && location.hasAccuracy())
          locationManager.removeUpdates(this);
        else {
          if(currentLocation == null) currentLocation = location;
          else if (getDistanceInM(
            currentLocation.getLatitude(),
            currentLocation.getLongitude(),
            location.getLatitude(),
            location.getLongitude()) > 100) {
            currentLocation = location;
            //currentLocation = debugLocation;
          }
        }
      }
    };
  }

  private static double getDistanceInM(double startLat, double startLng, double endLat, double endLng) {
    double d2r = Math.PI / 180;
    try {
      double dlong = (endLng - startLng) * d2r;
      double dlat = (endLat - startLat) * d2r;
      double a =
        Math.pow(Math.sin(dlat / 2.0), 2)
          + Math.cos(startLat * d2r)
          * Math.cos(endLat * d2r)
          * Math.pow(Math.sin(dlong / 2.0), 2);
      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      return 6367000 * c;
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }

  }
}
