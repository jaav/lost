package be.lil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.provider.BaseColumns._ID;
import static be.lil.Constants.*;

public class MapOverviewActivity extends Activity {

  // Physical display width and height.
  private static int displayWidth = 0;
  private static int displayHeight = 0;
  private static String[] FROM = {_ID, NAME, X, Y};
  private SpotsData spotsData;
  private static List<Spot> currentSpots;
  private static String TAG = "LIL - MapOverviewActivity";
  private boolean touched = false;
  private boolean isMenuEvent = false;
  private Locale locale;
  private SampleView sampleView;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // displayWidth and displayHeight will change depending on screen
    // orientation. To get these dynamically, we should hook onSizeChanged().
    // This simple example uses only landscape mode, so it's ok to get them
    // once on startup and use those values throughout.
    Display display = ((WindowManager)
      getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    displayWidth = display.getWidth();
    displayHeight = display.getHeight();

    // SampleView constructor must be constructed last as it needs the
    // displayWidth and displayHeight we just got.
    Intent message = getIntent();
    float centerX = message.getFloatExtra("centerX", -1);
    float centerY = message.getFloatExtra("centerY", -1);
    long targetSpotId = message.getLongExtra("spot_id", -1);
    float myX = message.getFloatExtra("myX", 0);
    float myY = message.getFloatExtra("myY", 0);
    sampleView = new SampleView(this, centerX, centerY, targetSpotId, myX, myY);
    setContentView(sampleView);
    applyPreferences();
    spotsData = new SpotsData(this);
    try {
      Cursor cursor = spotsData.getAllSpotsCursor();
      startManagingCursor(cursor);
      createSpotsList(cursor);
    } finally {
      spotsData.close();
    }
    for (Spot spot : currentSpots) {
      CoordinatesTranslator.addMapCoordinates(spot, CoordinatesTranslator.OVERVIEW);
      spot.setRoutes(getRoutes(spot.getId()));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.overviewmapmenu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      // Handle item selection
      switch (item.getItemId()) {
      case R.id.preferences:
        startActivity(new Intent(this, PreferencesActivity.class));
        break;

      case R.id.goBack:{
        startActivity(new Intent(this, Splash.class));
        break;
      }
      case R.id.spots:{
        startActivity(new Intent(this, ViewSpotActivity.class));
        break;
      }
      case R.id.zoom:{
        toggleZoom(
          CoordinatesTranslator.getRealX(sampleView.scrollRectX + displayWidth / 2, CoordinatesTranslator.OVERVIEW),
          CoordinatesTranslator.getRealY(sampleView.scrollRectY + displayHeight / 2, CoordinatesTranslator.OVERVIEW));
        break;
      }
      default:
      }
      return super.onOptionsItemSelected(item);

  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if ( keyCode == KeyEvent.KEYCODE_MENU ) {
        isMenuEvent = true;
    }
    return super.onKeyDown(keyCode, event);
  }

  private Cursor getAllSpots() {
    // Perform a managed query. The Activity will handle closing
    // and re-querying the cursor when needed.
    SQLiteDatabase db = spotsData.getReadableDatabase();
    Cursor cursor = db.query(SPOTS_TABLE_NAME, FROM, null, null, null,
      null, null);
    startManagingCursor(cursor);
    return cursor;
  }

  private void createSpotsList(Cursor cursor) {
    currentSpots = new ArrayList<Spot>();
    while (cursor.moveToNext()) {
      currentSpots.add(new Spot(cursor.getLong(0), cursor.getString(1), cursor.getFloat(2), cursor.getFloat(3), cursor.getInt(4)));
    }
  }

  private List<Coordinates> getRoutes(long spot_id){
    List<Coordinates> coordinates = new ArrayList<Coordinates>();
    SQLiteDatabase db = spotsData.getReadableDatabase();
    try{
      String[] FIELDS = {_ID, TO_X, TO_Y};
      String WHERE = SPOT_ID+"="+spot_id;
      Cursor cursor = db.query(ROUTES_TABLE_NAME, FIELDS, WHERE, null, null,
        null, null);
      startManagingCursor(cursor);
      while (cursor.moveToNext()) {
        Coordinates includesMapData = new Coordinates(cursor.getFloat(1), cursor.getFloat(2));
        CoordinatesTranslator.addMapCoordinates(includesMapData, CoordinatesTranslator.OVERVIEW);
        coordinates.add(includesMapData);
      }
    } finally {
      db.close();
    }
    return coordinates;
  }

  private void toggleZoom(float theX, float theY){
    Intent detailIntent = new Intent(this, MapDetailActivity.class);
    detailIntent.putExtra("centerX", theX);
    detailIntent.putExtra("centerY", theY);
    startActivity(detailIntent);
  }


  private void applyPreferences(){
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    locale = new Locale(prefs.getString(PreferencesActivity.KEY_LOCALE, "en"));
    Configuration config = new Configuration();
    config.locale = locale;
    getBaseContext().getResources().updateConfiguration(config,
      getBaseContext().getResources().getDisplayMetrics());
  }

  private class SampleView extends View {
    private Bitmap bmLargeImage; //bitmap large enough to be scrolled
    private Bitmap bmHeart; //heart bitmap
    private Bitmap bmRedHeart; //red heart bitmap
    private Bitmap bmMyLocation; //my location
    private Rect displayRect = null; //rect we display to
    private Rect scrollRect = null; //rect we scroll over our bitmap with
    private int scrollRectX = 0; //current left location of scroll rect
    private int scrollRectY = 0; //current top location of scroll rect
    private float scrollByX = 0; //x amount to scroll by
    private float scrollByY = 0; //y amount to scroll by
    private float startX = 0; //track x from one ACTION_MOVE to the next
    private float startY = 0; //track y from one ACTION_MOVE to the next
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float startCenterX;
    private float startCenterY;
    private float myX;
    private float myY;
    private float mapMeX;
    private float mapMeY;
    long myTargetSpotId;
    private boolean drawRoutes = true;

    public SampleView(Context context, float centerX, float centerY, long targetSpotId, float meX, float meY) {
      super(context);
      startCenterX = centerX;
      startCenterY = centerY;
      myX = meX;
      myY = meY;
      myTargetSpotId = targetSpotId;

      // Destination rect for our main canvas draw. It never changes.
      displayRect = new Rect(0, 0, displayWidth, displayHeight);
      // Scroll rect: this will be used to 'scroll around' over the
      // bitmap in memory. Initialize as above.
      scrollRect = new Rect(0, 0, displayWidth, displayHeight);

      // Load a large bitmap into an offscreen area of memory.
      bmLargeImage = BitmapFactory.decodeResource(getResources(),
        R.drawable.lost_map_no_icons);

      // Load a heart icon into an offscreen area of memory.
      bmHeart = BitmapFactory.decodeResource(getResources(),
        R.drawable.iconpointeronmap);

      // Load a red heart icon into an offscreen area of memory.
      bmRedHeart = BitmapFactory.decodeResource(getResources(),
        R.drawable.iconpointeronmapred);

      // Load the "my location" icon into an offscreen area of memory.
      bmMyLocation = BitmapFactory.decodeResource(getResources(),
        R.drawable.mylocation);
      if(meX!=0 && meY!=0){
        mapMeX = CoordinatesTranslator.getMapX(meX, CoordinatesTranslator.OVERVIEW);
        mapMeY = CoordinatesTranslator.getMapY(meY, CoordinatesTranslator.OVERVIEW);
      }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          // Remember our initial down event location.
          /*if(isOnZoom(event.getRawX(), event.getRawY())){
            toggleZoom(
              CoordinatesTranslator.getRealX(scrollRectX+displayWidth/2, CoordinatesTranslator.OVERVIEW),
              CoordinatesTranslator.getRealY(scrollRectY + displayHeight / 2, CoordinatesTranslator.OVERVIEW));
            break;
          }*/
          startX = event.getRawX();
          startY = event.getRawY();
          touched = true;
          break;

        case MotionEvent.ACTION_MOVE:
          drawRoutes = false;
          float x = event.getRawX();
          float y = event.getRawY();
          // Calculate move update. This will happen many times
          // during the course of a single movement gesture.
          scrollByX = x - startX; //move update x increment
          scrollByY = y - startY; //move update y increment
          startX = x; //reset initial values to latest
          startY = y;
          invalidate(); //force a redraw
          break;

        case MotionEvent.ACTION_UP:
          drawRoutes = true;
          invalidate();
      }
      return true; //done with this event so consume it
    }

    @Override
    protected void onDraw(Canvas canvas) {

      Log.e(TAG, "REDRAWING");
      if(isMenuEvent){
        scrollByX = 0;
        scrollByY = 0;
        isMenuEvent = false;
        touched = true;
      }
      if (!touched)
        moveToCenter();
      // Our move updates are calculated in ACTION_MOVE in the opposite direction
      // from how we want to move the scroll rect. Think of this as dragging to
      // the left being the same as sliding the scroll rect to the right.
      int newScrollRectX = scrollRectX - (int) scrollByX;
      int newScrollRectY = scrollRectY - (int) scrollByY;

      // Don't scroll off the left or right edges of the bitmap.
      if (newScrollRectX < 0)
        newScrollRectX = 0;
      else if (newScrollRectX > (bmLargeImage.getWidth() - displayWidth))
        newScrollRectX = (bmLargeImage.getWidth() - displayWidth);

      // Don't scroll off the top or bottom edges of the bitmap.
      if (newScrollRectY < 0)
        newScrollRectY = 0;
      else if (newScrollRectY > (bmLargeImage.getHeight() - displayHeight))
        newScrollRectY = (bmLargeImage.getHeight() - displayHeight);

      // We have our updated scroll rect coordinates, set them and draw.
      scrollRect.set(newScrollRectX, newScrollRectY,
        newScrollRectX + displayWidth, newScrollRectY + displayHeight);
      Paint paint = new Paint();
      canvas.drawBitmap(bmLargeImage, scrollRect, displayRect, paint);

      if(myX!=(0) && myY!=(0)){
        canvas.drawBitmap(bmMyLocation, null, new Rect(
          (int) (mapMeX - newScrollRectX - 10),
          (int) (mapMeY - newScrollRectY - 10),
          (int) (mapMeX - newScrollRectX + 10),
          (int) (mapMeY - newScrollRectY + 10)), null);
      }

      Paint routePaint = new Paint();
      routePaint.setColor(0x8006749d);
      routePaint.setStrokeWidth(4F);
      paint.setColor(0xff06749d);
      //paint.setColor(0xff9c1c1f);
      for (Spot currentSpot : currentSpots) {
        Bitmap bm;
        if(currentSpot.getId()==myTargetSpotId) bm = bmRedHeart;
        else bm = bmHeart;
        //if(currentSpot.getMapX()>newScrollRectX && currentSpot.getMapY()>newScrollRectY){
          //canvas.drawCircle(currentSpot.getMapX()-newScrollRectX, currentSpot.getMapY()-newScrollRectY, 6F, paint);
        canvas.drawBitmap(bm, null, new Rect(
          (int)(currentSpot.getMapX()-newScrollRectX-6),
          (int)(currentSpot.getMapY()-newScrollRectY-24),
          (int)(currentSpot.getMapX()-newScrollRectX+6),
          (int)(currentSpot.getMapY()-newScrollRectY)), null);
        if(drawRoutes){
          for (int i = 0; i<currentSpot.getRoutes().size(); i++) {
            if(i<(currentSpot.getRoutes().size() - 1))
              canvas.drawLine(currentSpot.getRoutes().get(i).mapx-newScrollRectX, currentSpot.getRoutes().get(i).mapy-newScrollRectY, currentSpot.getRoutes().get(i+1).mapx-newScrollRectX, currentSpot.getRoutes().get(i+1).mapy-newScrollRectY, routePaint);
          }
        }
        //}
      }
      /*paint.setColor(0x40ffffff);
      canvas.drawRect(new Rect(displayWidth-74, displayHeight-74, displayWidth-7, displayHeight-7), paint);
      paint.setColor(0xff000000);
      Bitmap zoom_out = BitmapFactory.decodeResource(getResources(), R.drawable.zoom_60);
      canvas.drawBitmap(zoom_out, displayWidth-67, displayHeight-67, paint);*/

      // Reset current scroll coordinates to reflect the latest updates,
      // so we can repeat this update process.
      scrollRectX = newScrollRectX;
      scrollRectY = newScrollRectY;
    }

    protected boolean isStartup() {
      if (startX == 0 && startY == 0 && scrollByX == 0 && scrollByY == 0)
        return true;
      else
        return false;
    }

    protected void moveToCenter() {
      if(startCenterX<0 && startCenterY<0){
        int difX = (-displayWidth + bmLargeImage.getWidth()) / 2;
        int difY = (-displayHeight + bmLargeImage.getHeight()) / 2;
        scrollByX = -difX; //move update x increment
        scrollByY = -difY; //move update y increment
        startX = difX; //reset initial values to latest
        startY = difY;
      }
      else{
        startX = CoordinatesTranslator.getMapX(startCenterX, CoordinatesTranslator.OVERVIEW) - displayWidth/2;
        startY = CoordinatesTranslator.getMapY(startCenterY, CoordinatesTranslator.OVERVIEW) - displayHeight/2;
        if(startX<0) startX = 0;
        if(startY<0) startY = 0;
        scrollByX = -startX;
        scrollByY = -startY;
      }
    }

    /*private boolean isOnZoom(float x, float y){
      if(7<(displayWidth-x) && (displayWidth-x)<67 && 7<(displayHeight-y) && (displayHeight-y)<67) return true;
      return false;
    }*/
  }
}