package be.lil;

/**
 * Created by IntelliJ IDEA.
 * User: jefw
 * Date: 4/21/11
 * Time: 1:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoordinatesTranslator {
  public static int OVERVIEW = 0;
  public static int DETAIL = 1;
  public static int SUPERZOOM = 2;
  /*private static int imgWidth = 0;
  private static float maxDifX = 0;
  private static float leftCoord = 0;
  private static int imgHeight = 0;
  private static float maxDifY = 0;
  private static float topCoord = 0;*/

  //Method translating a set of global coordinates into map coordinates
  //
  public static void addMapCoordinates(Spot spot, int mapType){
    spot.setMapX(getMapX(spot.getX(), mapType));
    spot.setMapY(getMapY(spot.getY(), mapType));
  }
  //Method translating a set of global coordinates into map coordinates
  //
  public static void addMapCoordinates(Coordinates coordinates, int mapType){
    coordinates.mapx = getMapX(coordinates.x, mapType);
    coordinates.mapy = getMapY(coordinates.y, mapType);
  }

  //Method translating a set of map coordinates into global coordinates
  //
  public static Coordinates translateToGlobalCoordinates(float x, float y){
    return null;

  }



  // OVERVIEW
  //From left to right on map = 0,028517
  //Left value = 5.563567
  //map width = 1328dp
  //left margin = 1328 * ((x - 5.563567)/0.028517)
  //left margin = 1328*(x/0.028517) - 1328*(5.563567:0.028517)

  // DETAIL
  //From left to right on map = 0,028517
  //Left value = 5.566335
  //Top value = 50.648304
  //Right value = 5.585207
  //Bottom value = 50.640126
  //map width = 1736dp
  //Bottom Left = 50.640126,5.585207
  //Top right = 50.648318,5.566292

  // SUPERZOOM
  //From left to right on map = 0,028517
  //Left value = 5.566378
  //Top value = 50.648569
  //Right value = 5.584976
  //Bottom value = 50.639905
  //map width = 3472dp
  //map height = 2544dp
  //Bottom Right = 50.639905,5.584976
  //Top Left = 50.648569,5.566378

  public static float getRealX(float mapX, int mapType){
    if(mapType == OVERVIEW){
      int imgWidth = 1328;
      float maxDifX = 0.028517F;
      float leftCoord = 5.563567F;
      return maxDifX*mapX/imgWidth +leftCoord;
    }
    else if(mapType == DETAIL){
      int imgWidth = 1736;
      float maxDifX = 0.018872F;
      float leftCoord = 5.566335F;
      return maxDifX*mapX/imgWidth +leftCoord;
    }
    else if(mapType == SUPERZOOM){
      int imgWidth = 3472;
      float maxDifX = 0.018598F;
      float leftCoord = 5.566378F;
      return maxDifX*mapX/imgWidth +leftCoord;
    }
    return 0F;
  }

  public static float getRealY(float mapY, int mapType){
    if(mapType == OVERVIEW){
      int imgHeight = 800;
      float maxDifY = -0.010796F;
      float topCoord = 50.648294F;
      return maxDifY*mapY/imgHeight +topCoord;
    }
    else if(mapType == DETAIL){
      int imgHeight = 1201;
      float maxDifY = -0.008178F;
      float topCoord = 50.648304F;
      return maxDifY*mapY/imgHeight +topCoord;
    }
    else if(mapType == SUPERZOOM){
      int imgHeight = 2544;
      float maxDifY = -0.008664F;
      float topCoord = 50.648569F;
      return maxDifY*mapY/imgHeight +topCoord;
    }
    return 0F;
  }

  public static float getMapX(float realX, int mapType){
    if(mapType == OVERVIEW){
      int imgWidth = 1328;
      float maxDifX = 0.028517F;
      float leftCoord = 5.563567F;
      //return imgWidth*(realX/maxDifX) - imgWidth*(leftCoord/maxDifX);
      return imgWidth/maxDifX*(realX - leftCoord);
    }
    else if(mapType == DETAIL){
      int imgWidth = 1736;
      float maxDifX = 0.0185F;
      float leftCoord = 5.566335F;
      //return imgWidth*(realX/maxDifX) - imgWidth*(leftCoord/maxDifX);
      return imgWidth/maxDifX*(realX - leftCoord);
    }
    else if(mapType == SUPERZOOM){
      int imgWidth = 3472;
      float maxDifX = 0.018598F;
      float leftCoord = 5.566378F;
      //return imgWidth*(realX/maxDifX) - imgWidth*(leftCoord/maxDifX);
      return imgWidth/maxDifX*(realX - leftCoord);
    }
    return 0F;
  }

  public static float getMapY(float realY, int mapType){
    if(mapType == OVERVIEW){
      /*int imgHeight = 728;
      float maxDifY = -0.009824F;
      float topCoord = 50.648780F;*/

      int imgHeight = 800;
      float maxDifY = -0.010796F;
      float topCoord = 50.649266F;

      //return imgWidth*(realX/maxDifX) - imgWidth*(leftCoord/maxDifX);
      return imgHeight/maxDifY*(realY - topCoord);
    }
    else if(mapType == DETAIL){
      int imgHeight = 1201;
      float maxDifY = -0.008178F;
      float topCoord = 50.648304F;
      //return imgWidth*(realX/maxDifX) - imgWidth*(leftCoord/maxDifX);
      return imgHeight/maxDifY*(realY - topCoord);
    }
    else if(mapType == SUPERZOOM){
      int imgHeight = 2544;
      float maxDifY = -0.008664F;
      float topCoord = 50.648569F;
      //return imgWidth*(realX/maxDifX) - imgWidth*(leftCoord/maxDifX);
      return imgHeight/maxDifY*(realY - topCoord);
    }
    return 0F;
  }


}
