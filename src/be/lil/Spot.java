package be.lil;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: jefw
 * Date: 4/21/11
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class Spot {
  private long id;
  private int spotorder;
  private float x;
  private float y;
  private float mapX;
  private float mapY;
  private String name;
  private List<String> pictures;
  private Map<String, String> descriptions;
  private String description;
  private List<Coordinates> routes;

  public Spot(String name, float y, float x) {
    this.x = x;
    this.y = y;
    this.name = name;
  }

  public Spot(long id, String name, String description, int spotorder, float x, float y) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.spotorder = spotorder;
    this.x = x;
    this.y = y;
  }

  public Spot(long id, String name, float x, float y, int spotorder) {
    this.id = id;
    this.x = x;
    this.y = y;
    this.name = name;
    this.spotorder = spotorder;
  }

  public Spot(float y, float x, String name, List<String> pictures, String description) {
    this.x = x;
    this.y = y;
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public int getSpotorder() {
    return spotorder;
  }

  public void setSpotorder(int spotorder) {
    this.spotorder = spotorder;
  }

  public float getX() {
    return x;
  }

  public void setX(float x) {
    this.x = x;
  }

  public float getY() {
    return y;
  }

  public void setY(float y) {
    this.y = y;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getPictures() {
    return pictures;
  }

  public void setPictures(List<String> pictures) {
    this.pictures = pictures;
  }

  public float getMapX() {
    return mapX;
  }

  public void setMapX(float mapX) {
    this.mapX = mapX;
  }

  public float getMapY() {
    return mapY;
  }

  public void setMapY(float mapY) {
    this.mapY = mapY;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, String> getDescriptions() {
    return descriptions;
  }

  public void setDescriptions(Map<String, String> descriptions) {
    this.descriptions = descriptions;
  }

  public List<Coordinates> getRoutes() {
    return routes;
  }

  public void setRoutes(List<Coordinates> routes) {
    this.routes = routes;
  }
}
