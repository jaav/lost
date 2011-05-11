package be.lil;

/**
 * Created by IntelliJ IDEA.
 * User: jefw
 * Date: 4/21/11
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */
import android.net.Uri;
// ...

import android.provider.BaseColumns;

import java.util.List;

public interface Constants extends BaseColumns {
   public static final String SPOTS_TABLE_NAME = "spots";
   public static final String ROUTES_TABLE_NAME = "routes";
   public static final String PICTURES_TABLE_NAME = "pictures";
   public static final String DESCRIPTION_TABLE_NAME = "descriptions";


   // Columns in the Spots table
   public static final String NAME = "name";
   public static final String X = "x";
   public static final String Y = "y";
   public static final String SPOTORDER = "spotorder";

   // Columns in the Routes table
   public static final String SPOT_ID = "spot_id";
   public static final String TO_X = "to_x";
   public static final String TO_Y = "to_y";

   // Columns in the Pictures table
   public static final String URL = "url";

   // Columns in the Descriptions table
   public static final String LANG = "lang";
   public static final String TITLE = "title";
   public static final String DESCRIPTION = "description";
}
