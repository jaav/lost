package be.lil;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import static android.provider.BaseColumns._ID;
import static be.lil.Constants.NAME;
import static be.lil.Constants.X;
import static be.lil.Constants.Y;

/**
 * Created by IntelliJ IDEA.
 * User: jefw
 * Date: 4/29/11
 * Time: 6:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class SpotListActivity extends ListActivity {
  private LayoutInflater mInflater;
  private Vector<RowData> data;
  RowData rd;
  private SpotsData spotsData;
  private Locale locale;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.listview);
    applyPreferences();
    mInflater = (LayoutInflater) getSystemService(
      Activity.LAYOUT_INFLATER_SERVICE);
  }

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onResume() {
    super.onResume();
    DataFetchingTask fetcher = new DataFetchingTask(this, locale);
    fetcher.execute();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.prefsandhomemenu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      // Handle item selection
      switch (item.getItemId()) {
      case R.id.preferences:
        startActivity(new Intent(this, PreferencesActivity.class));
        break;

      case R.id.home:
        startActivity(new Intent(this, Splash.class));
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

  public void onListItemClick(ListView parent, View v, int position,
                              long id) {
      showSpot(position);
  }

  private void showSpot(int spotOrder){
    Intent viewIntent = new Intent(this, ViewSpotActivity.class);
    viewIntent.putExtra("spotOrder", spotOrder);
    startActivity(viewIntent);
  }

  private void useSpotsData(){
    CustomAdapter adapter = new CustomAdapter(this, R.layout.list,
      R.id.title, data);
    setListAdapter(adapter);
    getListView().setTextFilterEnabled(true);
  }

  private class RowData {
    protected int mId;
    protected String mTitle;
    protected String mData;
    protected String mUrl;

    RowData(int id, String title, String data, String url) {
      mId = id;
      mTitle = title;
      mData = data;
      mUrl = url;
    }

    @Override
    public String toString() {
      return mId + " " + mTitle + " " + mUrl;
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

  private class CustomAdapter extends ArrayAdapter<RowData> {
    public CustomAdapter(Context context, int resource,
                         int textViewResourceId, List<RowData> objects) {
      super(context, resource, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder = null;
      TextView title = null;
      TextView data = null;
      ImageView i11 = null;
      RowData rowData = getItem(position);
      if (null == convertView) {
        convertView = mInflater.inflate(R.layout.list, null);
        holder = new ViewHolder(convertView);
        convertView.setTag(holder);
      }
      holder = (ViewHolder) convertView.getTag();
      title = holder.gettitle();
      title.setText(rowData.mTitle);
      data = holder.getdata();
      data.setText(rowData.mData);
      i11 = holder.getImage();
      i11.setImageBitmap(getImageFromAssets(rowData.mUrl));
      return convertView;
    }



    private Bitmap getImageFromAssets(String imageName) {
      Bitmap bitmap = null;
      try {
        AssetManager am = getAssets();
        BufferedInputStream buf = new BufferedInputStream(am.open("thumbs/"+imageName + ".jpg"));
        bitmap = BitmapFactory.decodeStream(buf);
        buf.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return bitmap;
    }

    private class ViewHolder {
      private View mRow;
      private TextView title = null;
      private TextView data = null;
      private ImageView i11 = null;

      public ViewHolder(View row) {
        mRow = row;
      }

      public TextView gettitle() {
        if (null == title) {
          title = (TextView) mRow.findViewById(R.id.title);
        }
        return title;
      }

      public TextView getdata() {
        if (null == data) {
          data = (TextView) mRow.findViewById(R.id.data);
        }
        return data;
      }

      public ImageView getImage() {
        if (null == i11) {
          i11 = (ImageView) mRow.findViewById(R.id.img);
        }
        return i11;
      }
    }
  }


  private class DataFetchingTask extends AsyncTask<Void, Void, Void> {

    private Context localAppContext;
    private Locale localLocale;


    public DataFetchingTask(Context appContext, Locale locale){
      localAppContext = appContext;
      localLocale = locale;
    }

    @Override
    protected Void doInBackground(Void... params) {
      spotsData = new SpotsData(localAppContext);
      try {
        getAllSpots();
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

      data = new Vector<RowData>();
      int counter = -1;
      while (cursor.moveToNext()) {
        try {
          if(cursor.getInt(2)!=counter){
            rd = new RowData(cursor.getInt(2), cursor.getString(1), cursor.getString(2), cursor.getString(3));
            data.add(rd);
            counter = cursor.getInt(2);
          }
        } catch (ParseException e) {
          e.printStackTrace();
        }
      }

    }
  }
}
