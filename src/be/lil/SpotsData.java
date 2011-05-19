package be.lil;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.EventLogTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static be.lil.Constants.*;

/**
 * Created by IntelliJ IDEA.
 * User: jefw
 * Date: 4/21/11
 * Time: 2:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpotsData extends SQLiteOpenHelper {
  private static final String DATABASE_NAME = "spots.db";
  private static final int DATABASE_VERSION = 18;
  private Context myCtx;

  /**
   * Create a helper object for the Events database
   */
  public SpotsData(Context ctx) {
    super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    myCtx = ctx;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    String spots_creation = "CREATE TABLE " + SPOTS_TABLE_NAME + " ("
      + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
      + X + " REAL NOT NULL,"
      + Y + " REAL NOT NULL, "
      + NAME + " TEXT NOT NULL, "
      + SPOTORDER + " INTEGER NOT NULL);";
    String routes_creation = "CREATE TABLE " + ROUTES_TABLE_NAME + " ("
      + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
      + SPOT_ID + " INTEGER NOT NULL, "
      + TO_X + " REAL NOT NULL,"
      + TO_Y + " REAL NOT NULL);";
    String pictures_creation = "CREATE TABLE " + PICTURES_TABLE_NAME + " ("
      + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
      + SPOT_ID + " INTEGER NOT NULL, "
      + URL + " TEXT NOT NULL);";
    String descriptions_creation = "CREATE TABLE " + DESCRIPTION_TABLE_NAME + " ("
      + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
      + SPOT_ID + " INTEGER NOT NULL, "
      + TITLE + " TEXT NOT NULL,"
      + DESCRIPTION + " TEXT NOT NULL,"
      + LANG + " TEXT NOT NULL);";

    db.execSQL(spots_creation);
    db.execSQL(routes_creation);
    db.execSQL(pictures_creation);
    db.execSQL(descriptions_creation);

    initDB(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion,
                        int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + SPOTS_TABLE_NAME + ";");
    db.execSQL("DROP TABLE IF EXISTS " + ROUTES_TABLE_NAME + ";");
    db.execSQL("DROP TABLE IF EXISTS " + PICTURES_TABLE_NAME + ";");
    db.execSQL("DROP TABLE IF EXISTS " + DESCRIPTION_TABLE_NAME + ";");
    onCreate(db);
  }

  protected void initDB(SQLiteDatabase db) {
    //SQLiteDatabase db = getWritableDatabase();
    List<Spot> spots = getInitSpots();
    NodeList routes = getRoutes();
    NodeList pictures = getPictures();
    NodeList descriptions = getDescriptions();
    for(int i = 0; i<spots.size(); i++){
      Spot spot = spots.get(i);
      ContentValues values = new ContentValues();
      values.put(NAME, spot.getName());
      values.put(X, spot.getX());
      values.put(Y, spot.getY());
      values.put(SPOTORDER, i);
      long spot_id = db.insertOrThrow(SPOTS_TABLE_NAME, null, values);

      Node route = routes.item(i);
      NodeList coordinates = route.getChildNodes();
      for(int j = 0; j<coordinates.getLength(); j++){
        String coords = coordinates.item(j).getTextContent();
        ContentValues routeValues = new ContentValues();
        routeValues.put(SPOT_ID, spot_id);
        routeValues.put(TO_X, coords.substring(0, coords.indexOf(',')));
        routeValues.put(TO_Y, coords.substring(coords.indexOf(',')+1));
        long route_id = db.insertOrThrow(ROUTES_TABLE_NAME, null, routeValues);
        long test = route_id;
      }

      Node spotpictures = pictures.item(i);
      NodeList pics = spotpictures.getChildNodes();
      for(int j = 0; j<pics.getLength(); j++){
        ContentValues pictureValues = new ContentValues();
        pictureValues.put(SPOT_ID, spot_id);
        pictureValues.put(URL, pics.item(j).getTextContent());
        long pic_id = db.insertOrThrow(PICTURES_TABLE_NAME, null, pictureValues);
        long test = pic_id;
      }

      Node spotdescriptions = descriptions.item(i);
      NodeList descs = spotdescriptions.getChildNodes();
      for(int j = 0; j<descs.getLength(); j++){
        if(descs.item(j).getNodeType()==Node.ELEMENT_NODE){
          ContentValues descriptionValues = new ContentValues();
          descriptionValues.put(SPOT_ID, spot_id);
          descriptionValues.put(TITLE, descs.item(j).getFirstChild().getTextContent());
          descriptionValues.put(DESCRIPTION, descs.item(j).getLastChild().getTextContent());
          descriptionValues.put(LANG, descs.item(j).getNodeName());
          long desc_id = db.insertOrThrow(DESCRIPTION_TABLE_NAME, null, descriptionValues);
          long test = desc_id;
        }
      }
    }
  }

  protected List<Spot> getInitSpots() {
    List<Spot> spots = new ArrayList<Spot>();
    int i = 1;
    spots.add(new Spot("Vue depuis le milieu de la passerelle", 50.641266F, 5.577917F));
    spots.add(new Spot("La place Cockerill", 50.641746F, 5.5754544F));
    spots.add(new Spot("La statue d’André Dumont", 50.640878F, 5.5751388F));
    spots.add(new Spot("La Société libre d’Emulation", 50.640790F, 5.5749399F));
    spots.add(new Spot("L’université de Liège", 50.640358F, 5.5756266F));
    spots.add(new Spot("La cathédrale Saint-Paul", 50.640351F, 5.5717266F));
    spots.add(new Spot("La rue du Pont-d’Avroy", 50.640963F, 5.570865F));
    spots.add(new Spot("Le Carré", 50.641834F, 5.569897F));
    spots.add(new Spot("L’ancienne collégiale Saint-Jean-l’Évangéliste", 50.642970F, 5.567386F));
    spots.add(new Spot("La rue des Dominicains", 50.642865F, 5.570264F));
    spots.add(new Spot("Le vinâve d’Île", 50.642081F, 5.570908F));
    spots.add(new Spot("Le passage Lemonnier", 50.641948F, 5.571031F));
    spots.add(new Spot("Le pont d’Île au XIXe siècle", 50.642654F, 5.571780F));
    spots.add(new Spot("L’ancienne collégiale Saint-Denis", 50.643324F, 5.574373F));
    spots.add(new Spot("La place de la République-Française", 50.643453F, 5.572144F));
    spots.add(new Spot("La Société littéraire", 50.643688F, 5.572203F));
    spots.add(new Spot("Le Publémont", 50.643930F, 5.571747F));
    spots.add(new Spot("L’opéra", 50.643467F, 5.570642F));
    spots.add(new Spot("La statue de Grétry", 50.643559F, 5.570846F));
    spots.add(new Spot("La Sauvenière", 50.643780F, 5.571082F));
    spots.add(new Spot("Les grands magasins", 50.644851F, 5.573931F));
    spots.add(new Spot("La rue Léopold", 50.644902F, 5.574467F));
    spots.add(new Spot("L’ancienne cathédrale Notre-Dame-et-Saint-Lambert", 50.645243F, 5.573974F));
    spots.add(new Spot("La cour du Palais des Princes-Évêques", 50.646236F, 5.573684F));
    spots.add(new Spot("L’aile occidentale du palais", 50.646181F, 5.572949F));
    spots.add(new Spot("La rue du Palais", 50.646607F, 5.572670F));
    spots.add(new Spot("Hors-Château", 50.646494F, 5.576288F));
    spots.add(new Spot("L’ancienne église Saint-Antoine", 50.646630F, 5.576173F));
    spots.add(new Spot("Le couvent des frères mineurs", 50.646828F, 5.576034F));
    spots.add(new Spot("La fontaine Saint-Jean Baptiste", 50.646926F, 5.578048F));
    spots.add(new Spot("L’ancien couvent des Ursulines", 50.647386F, 5.578340F));
    spots.add(new Spot("La Montagne de Bueren", 50.647619F, 5.577959F));
    spots.add(new Spot("L’ancienne église des Carmes déchaussés", 50.647346F, 5.579330F));
    spots.add(new Spot("Les impasses", 50.647508F, 5.579958F));
    spots.add(new Spot("La chapelle des Filles de la Croix", 50.647682F, 5.580422F));
    spots.add(new Spot("La cour Saint-Antoine", 50.647678F, 5.580757F));
    spots.add(new Spot("La collégiale Saint-Barthélemy", 50.647719F, 5.582825F));
    spots.add(new Spot("La statue « Les Principautaires »", 50.647295F, 5.582584F));
    spots.add(new Spot("Le Grand Curtius", 50.647280F, 5.583058F));
    spots.add(new Spot("Le départ des diligences", 50.646418F, 5.582098F));
    spots.add(new Spot("La Batte", 50.646341F, 5.582004F));
    spots.add(new Spot("L’ancienne halle aux viandes", 50.645217F, 5.578340F));
    spots.add(new Spot("Le Pont des Arches", 50.643737F, 5.578313F));
    spots.add(new Spot("Neuvice", 50.644457F, 5.577624F));
    spots.add(new Spot("La place du Marché", 50.645726F, 5.576211F));
    spots.add(new Spot("L'hotel de Ville", 50.645426F, 5.575631F));
    spots.add(new Spot("Le Perron", 50.645683F, 5.575701F));

    return spots;
  }

  private NodeList getRoutes(){
    try {
      AssetManager assetManager = myCtx.getAssets();
      InputStream is = assetManager.open("routing.xml");
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document dom = builder.parse(is);
      Element root = dom.getDocumentElement();
      return root.getElementsByTagName("route");
    } catch (ParserConfigurationException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (SAXException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    return null;
  }

  private NodeList getPictures(){
    try {
      AssetManager assetManager = myCtx.getAssets();
      InputStream is = assetManager.open("pictures.xml");
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document dom = builder.parse(is);
      Element root = dom.getDocumentElement();
      return root.getElementsByTagName("spot");
    } catch (ParserConfigurationException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (SAXException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    return null;
  }

  private NodeList getDescriptions(){
    try {
      AssetManager assetManager = myCtx.getAssets();
      InputStream is = assetManager.open("descriptions.xml");
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document dom = builder.parse(is);
      Element root = dom.getDocumentElement();
      return root.getElementsByTagName("spot");
    } catch (ParserConfigurationException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (SAXException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } catch (IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    return null;
  }

  /****************************************************************
   *
   * Data Access layer
   *
   ****************************************************************/


  public Cursor getAllSpotsCursor() {
    // Perform a managed query. The Activity will handle closing
    // and re-querying the cursor when needed.
    SQLiteDatabase db = getReadableDatabase();
    String[] FIELDS = {_ID, NAME, X, Y, SPOTORDER};
    return db.query(SPOTS_TABLE_NAME, FIELDS, null, null, null,
      null, null);
  }

  public Cursor getAllSpotsJoinedPicsCursor() {
    // Perform a managed query. The Activity will handle closing
    // and re-querying the cursor when needed.
    SQLiteDatabase db = getReadableDatabase();
    return db.rawQuery("SELECT " +
      SPOTS_TABLE_NAME+"."+_ID+"," +
      SPOTS_TABLE_NAME+"."+NAME+"," +
      SPOTS_TABLE_NAME+"."+SPOTORDER+"," +
      PICTURES_TABLE_NAME+"."+URL+"," +
      SPOTS_TABLE_NAME+"."+X+"," +
      SPOTS_TABLE_NAME+"."+Y +
      " FROM "+SPOTS_TABLE_NAME +
      " JOIN " +PICTURES_TABLE_NAME +
      " ON ("+PICTURES_TABLE_NAME+"."+SPOT_ID+"="+SPOTS_TABLE_NAME+"."+_ID+")",
      null);
  }

  public Cursor getAllSpotsJoinedDescsCursor(String lang) {
    // Perform a managed query. The Activity will handle closing
    // and re-querying the cursor when needed.
    SQLiteDatabase db = getReadableDatabase();
    return db.rawQuery("SELECT " +
      SPOTS_TABLE_NAME+"."+_ID+"," +
      SPOTS_TABLE_NAME+"."+NAME+"," +
      SPOTS_TABLE_NAME+"."+SPOTORDER+"," +
      DESCRIPTION_TABLE_NAME+"."+TITLE+"," +
      DESCRIPTION_TABLE_NAME+"."+DESCRIPTION+"," +
      SPOTS_TABLE_NAME+"."+X+"," +
      SPOTS_TABLE_NAME+"."+Y +
      " WHERE "+DESCRIPTION_TABLE_NAME+"."+LANG+"="+lang +
      " FROM "+SPOTS_TABLE_NAME +
      " JOIN " +DESCRIPTION_TABLE_NAME +
      " ON ("+DESCRIPTION_TABLE_NAME+"."+SPOT_ID+"="+SPOTS_TABLE_NAME+"."+_ID+")",
      null);
  }

  public Cursor getSpotJoinedDescsCursor(long spot_id, String lang) {
    // Perform a managed query. The Activity will handle closing
    // and re-querying the cursor when needed.
    SQLiteDatabase db = getReadableDatabase();
    return db.rawQuery("SELECT " +
      SPOTS_TABLE_NAME+"."+_ID+"," +
      SPOTS_TABLE_NAME+"."+NAME+"," +
      SPOTS_TABLE_NAME+"."+SPOTORDER+"," +
      DESCRIPTION_TABLE_NAME+"."+TITLE+"," +
      DESCRIPTION_TABLE_NAME+"."+DESCRIPTION+"," +
      SPOTS_TABLE_NAME+"."+X+"," +
      SPOTS_TABLE_NAME+"."+Y +
      " FROM "+SPOTS_TABLE_NAME +
      " JOIN " +DESCRIPTION_TABLE_NAME +
      " ON ("+DESCRIPTION_TABLE_NAME+"."+SPOT_ID+"="+SPOTS_TABLE_NAME+"."+_ID+")" +
      " WHERE "+SPOTS_TABLE_NAME+"."+_ID+"="+spot_id+" AND "+DESCRIPTION_TABLE_NAME+"."+LANG+"='"+lang+"'",
      null);
  }

  public Cursor getFirstSpotCursor(){
    SQLiteDatabase db = getReadableDatabase();
    String[] FIELDS = {_ID, NAME, X, Y, SPOTORDER};
    String WHERE = SPOTORDER+"=0";
    return db.query(SPOTS_TABLE_NAME, FIELDS, WHERE, null, null,
      null, null);
  }

  public Cursor getSpotCursor(long spot_id){
    SQLiteDatabase db = getReadableDatabase();
    String[] FIELDS = {_ID, NAME, X, Y, SPOTORDER};
    String WHERE = _ID+"="+spot_id;
    return db.query(SPOTS_TABLE_NAME, FIELDS, WHERE, null, null,
      null, null);
  }

  public Cursor getpicturesCursor(long spot_id){
    SQLiteDatabase db = getReadableDatabase();
    String[] FIELDS = {URL};
    String WHERE = SPOT_ID+"="+spot_id;
    return db.query(PICTURES_TABLE_NAME, FIELDS, WHERE, null, null,
      null, null);
  }

  public Cursor getpicturesCursor(){
    SQLiteDatabase db = getReadableDatabase();
    String[] FIELDS = {SPOT_ID, URL};
    return db.query(PICTURES_TABLE_NAME, FIELDS, null, null, null,
      null, null);
  }

  public Cursor getDescriptionCursor(long spot_id){
    SQLiteDatabase db = getReadableDatabase();
    String[] FIELDS = {LANG, DESCRIPTION};
    String WHERE = SPOT_ID+"="+spot_id;
    return db.query(DESCRIPTION_TABLE_NAME, FIELDS, WHERE, null, null,
      null, null);
  }
}
