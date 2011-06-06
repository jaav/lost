package be.lil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.*;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import static be.lil.Constants.*;

/**
 * Created by IntelliJ IDEA.
 * User: jefw
 * Date: 4/26/11
 * Time: 5:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class ViewSpotActivity extends Activity implements View.OnClickListener{

  private static String TAG = "LIL - ViewSpotActivity";
	private static final int EXIT = 0;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;
	private ViewFlipper viewFlipper;
  private SpotsData spotsData;
  private static List<Spot> allSpots;
  private Locale locale;
	private Spot currentSpot;
  private int startspot;
  private ProgressDialog dialog;
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
    setContentView(R.layout.viewspot);
    Intent message = getIntent();
    startspot = message.getIntExtra("spotOrder", -1);
    viewFlipper = (ViewFlipper)findViewById(R.id.flipper);
    applyPreferences();
    DataFetchingTask fetcher = new DataFetchingTask(this, locale);
    fetcher.execute();
    dialog = ProgressDialog.show(ViewSpotActivity.this, "",
      "Loading data.\nPlease wait...", true);
    debugLocation.setLatitude(50.646821);
    debugLocation.setLongitude(5.577106);

    slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
		slideLeftOut = AnimationUtils
				.loadAnimation(this, R.anim.slide_left_out);
		slideRightIn = AnimationUtils
				.loadAnimation(this, R.anim.slide_right_in);
		slideRightOut = AnimationUtils.loadAnimation(this,
				R.anim.slide_right_out);

		System.gc();
    //showStop("Till System.gc()");***3500

		gestureDetector = new GestureDetector(new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		};
    registerLocationListeners();

  }

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onResume() {
    //showStop("Till onResume()");***14
    super.onResume();
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

  private void useSpotsData(){
    if(startspot<0)
      currentSpot = allSpots.get(0);
    else currentSpot = allSpots.get(startspot);
    System.gc();
    setFlipperContent(currentSpot.getSpotorder(), true);
    viewFlipper.setDisplayedChild(currentSpot.getSpotorder());

    for (int i = 0; i < allSpots.size(); i++) {
      ImageButton showMaps = (ImageButton)viewFlipper.getChildAt(i).findViewById(R.id.showMapButton);
      showMaps.setOnClickListener(this);
      ImageButton goBack = (ImageButton)viewFlipper.getChildAt(i).findViewById(R.id.goBackButton);
      goBack.setOnClickListener(this);
      ImageButton go = (ImageButton)viewFlipper.getChildAt(i).findViewById(R.id.goButton);
      go.setOnClickListener(this);
    }
    dialog.dismiss();
  }

  private void showPictures(long spot_id){
    Intent picsIntent = new Intent(this, ViewPicsActivity.class);
    picsIntent.putExtra("spot_id", spot_id);
    startActivity(picsIntent);
    Log.d(TAG, "Clicked on showDetail button");
  }

  private View.OnClickListener detailClickListener = new View.OnClickListener() {
    public void onClick(View v) {
      long spot_id = Long.parseLong(((TextView) ((RelativeLayout) v.getParent()).findViewWithTag("data")).getText().toString());
      showPictures(spot_id);
    }
  };

  private void setFlipperContent(int currentSpotOrder, boolean root){
    //View spotContainer = viewFlipper.getChildAt(currentSpotOrder).findViewWithTag("spot");
    RelativeLayout spotContainer = (RelativeLayout)viewFlipper.getChildAt(currentSpotOrder).findViewWithTag("mainImage");
    spotContainer.setBackgroundDrawable(getImageFromAssets(allSpots.get(currentSpotOrder).getPictures().get(0)));
    Button detailButton = (Button)spotContainer.findViewWithTag("showDetail");
    detailButton.setText(allSpots.get(currentSpotOrder).getName()+"   >>");
    detailButton.setOnClickListener(detailClickListener);
    TextView dataView = (TextView)spotContainer.findViewWithTag("data");
    dataView.setText("" + allSpots.get(currentSpotOrder).getId());
    /*if(root){
      if(currentSpotOrder==0){
        setFlipperContent(allSpots.size()-1, false);
        setFlipperContent(1, false);
      }
      else if(currentSpotOrder==allSpots.size()-1){
        setFlipperContent(allSpots.size()-2, false);
        setFlipperContent(0, false);
      }
      else{
        setFlipperContent(currentSpotOrder-1, false);
        setFlipperContent(currentSpotOrder+1, false);
      }
    }*/
  }

  private Spot getSpot(int movement){
    int newSpotOrder;
    if(currentSpot.getSpotorder() + movement > allSpots.size()-1)
      newSpotOrder = currentSpot.getSpotorder() + movement - allSpots.size();
    else if (currentSpot.getSpotorder() + movement < 0)
      newSpotOrder = currentSpot.getSpotorder() + movement + allSpots.size();
    else
      newSpotOrder = currentSpot.getSpotorder() + movement;
    return allSpots.get(newSpotOrder);
  }

  private Drawable getImageFromAssets(String imageName){
  Bitmap bitmap = null;
   try{
     AssetManager am = getAssets();
     BufferedInputStream buf = new BufferedInputStream(am.open("full/"+imageName+".jpg"));
     bitmap = BitmapFactory.decodeStream(buf);
     buf.close();
   }
   catch (IOException e)  {
    e.printStackTrace();
   }
   return new BitmapDrawable(bitmap);
  }

  @Override
  public void onClick(View view) {
    if(view.getId() == R.id.showMapButton)
      startActivity(new Intent(this, MapOverviewActivity.class));
    if(view.getId() == R.id.goBackButton)
      finish();
      //startActivity(new Intent(this, Splash.class));
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


  private void applyPreferences(){
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    locale = new Locale(prefs.getString(PreferencesActivity.KEY_LOCALE, "en"));
    Configuration config = new Configuration();
    config.locale = locale;
    getBaseContext().getResources().updateConfiguration(config,
      getBaseContext().getResources().getDisplayMetrics());
  }

  class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					viewFlipper.setInAnimation(slideLeftIn);
					viewFlipper.setOutAnimation(slideLeftOut);
          setStart();
          currentSpot = getSpot(1);
          showStop("getSpot()");
          setFlipperContent(currentSpot.getSpotorder(), true);
					viewFlipper.showNext();
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					viewFlipper.setInAnimation(slideRightIn);
					viewFlipper.setOutAnimation(slideRightOut);
          currentSpot = getSpot(-1);
          setFlipperContent(currentSpot.getSpotorder(), true);
					viewFlipper.showPrevious();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event))
			return true;
		else
			return false;
	}

  private class DataFetchingTask extends AsyncTask<Void, Void, Void> {

    private Context localAppContext;
    private Locale localLocale;

    private String[] FIELDS = {_ID, NAME, X, Y};

    public DataFetchingTask(Context appContext, Locale locale){
      localAppContext = appContext;
      localLocale = locale;
    }

    @Override
    protected Void doInBackground(Void... params) {
      //setStart();
      spotsData = new SpotsData(localAppContext);
      //showStop("SpotsData()");
      try {
        getAllSpots();
        //showStop("getAllSpots()");*** 300ms
      } finally {
        spotsData.close();
      }
      return null;
    }
    @Override
    protected void onPreExecute(){
    }

    @Override
    protected void onPostExecute(Void result){
      useSpotsData();
    }


    private void getAllSpots() {
      Cursor cursor = spotsData.getAllSpotsJoinedPicsCursor();
      startManagingCursor(cursor);
      allSpots = new ArrayList<Spot>();

      int spotorder = -1;
      Spot spot = null;
      while (cursor.moveToNext()) {
        try {
          if(cursor.getInt(2)!=spotorder){
            spot = new Spot(cursor.getLong(0), cursor.getString(1), cursor.getFloat(4), cursor.getFloat(5), cursor.getInt(2));
            spot.setPictures(new ArrayList<String>());
            spot.getPictures().add(cursor.getString(3));
            allSpots.add(spot);
          }
          else{
            spot.getPictures().add(cursor.getString(3));
          }
          spotorder = cursor.getInt(2);
        } catch (ParseException e) {
          e.printStackTrace();
        }
      }

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
          if (getDistanceInM(
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
          if (getDistanceInM(
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

  private long start;

  private void setStart(){
    start = SystemClock.currentThreadTimeMillis();
  }
  private void showStop(String method){
    Log.d(TAG+"-"+method, "Duration = "+(SystemClock.currentThreadTimeMillis()-start)+" millis.");
  }

}
