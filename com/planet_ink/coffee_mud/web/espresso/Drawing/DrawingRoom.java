package com.planet_ink.coffee_mud.web.espresso.Drawing;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.awt.*;
import java.util.*;
import java.io.*;

public class DrawingRoom implements Serializable {
  public int x = 0;
  public int y = 0;
  public int z = 0;
  public String roomID = "";
  public Color bgColor = null;
  public DrawingDir[] doors = new DrawingDir[Directions.NUM_DIRECTIONS];

  public DrawingRoom() { }

  // Data for speedy status bar updates in the client side
  public String LocaleTypeName = "";
  public String RoomName = "";
  public Integer MOBCount = new Integer(0);
  public Integer ItemCount = new Integer(0);
}
