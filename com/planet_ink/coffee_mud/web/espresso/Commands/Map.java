package com.planet_ink.coffee_mud.web.espresso.Commands;
import com.planet_ink.coffee_mud.web.espresso.Drawing.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.web.espresso.*;
import java.util.*;
import java.awt.*;

/**
 * <p>Title: False Realities Flavored CoffeeMUD</p>
 * <p>Description: The False Realities Version of CoffeeMUD</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: http://falserealities.game-host.org</p>
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */

public class Map extends StdEspressoCommand {
  public String Name = "Map";
  public String ID() { return name(); }
  public String name() { return Name; }

  protected final static int CLUSTERSIZE=3;

  protected Vector areaMap=null;
  protected Hashtable hashRooms=null;
  protected boolean debug = false;

  public Map() {
  }

  public Object run(Vector param, EspressoServer server) {
    if (super.isAuthenticated(param, server)) {
      areaMap = new Vector();
      hashRooms = new Hashtable();
      String last=safelyGetStr(param,1);
      if (last.length() == 0)
        return null;
      Area A=CMMap.getArea(last);
      if(A==null) return null;
      // for now, skip hidden areas.  Areas are often hidden if they aren't linked
      // to the world (ie under construction or Archon only)
      for (Enumeration r = A.getMap(); r.hasMoreElements(); ) {
        Room R = (Room) r.nextElement();
        if (R.roomID().length() > 0) {
          DrawingRoom GR = makeRoom(R);
          areaMap.addElement(GR);
          hashRooms.put(GR.roomID, GR);
        }
      }
      return areaMap;
    }
    return null;
  }

  public DrawingRoom makeRoom(Room R) {
    DrawingRoom DR = new DrawingRoom();
    DR.roomID = R.roomID();
    for (int d = 0; d < Directions.NUM_DIRECTIONS; d++) {
      DrawingDir D = new DrawingDir();
      Room R2 = R.rawDoors()[d];
      if (R2 != null) {
        D.room = R2.roomID();
        Exit E2 = R.rawExits()[d];
        if (E2 != null) {
          D.hasADoor = E2.hasADoor();
        }
        else
        {
          D.exitNull = true;
        }
      }
      DR.doors[d] = D;
    }
    DR.bgColor=roomColorStyle(R);
    DR.RoomName=R.displayText();
    DR.LocaleTypeName=R.ID();
    DR.MOBCount=new Integer(R.numInhabitants());
    DR.ItemCount=new Integer(R.numItems());
    return DR;
  }

  protected Color roomColorStyle(Room GR)
  {
          switch (GR.domainType())
          {
          case Room.DOMAIN_INDOORS_AIR:
                  return new Color(255,255,255);
          case Room.DOMAIN_INDOORS_MAGIC:
                  return new Color(153,102,00);
          case Room.DOMAIN_INDOORS_METAL:
                  return new Color(153,102,00);
          case Room.DOMAIN_INDOORS_CAVE:
                  return new Color(204,153,255);
          case Room.DOMAIN_INDOORS_STONE:
                  return new Color(204,00,255);
          case Room.DOMAIN_INDOORS_UNDERWATER:
                  return new Color(102,102,204);
          case Room.DOMAIN_INDOORS_WATERSURFACE:
                  return new Color(51,153,204);
          case Room.DOMAIN_INDOORS_WOOD:
                  return new Color(153,153,00);
          case Room.DOMAIN_OUTDOORS_AIR:
                  return new Color(255,255,255);
          case Room.DOMAIN_OUTDOORS_CITY:
                  return new Color(204,204,204);
          case Room.DOMAIN_OUTDOORS_DESERT:
                  return new Color(255,255,102);
          case Room.DOMAIN_OUTDOORS_HILLS:
                  return new Color(153,204,51);
          case Room.DOMAIN_OUTDOORS_JUNGLE:
                  return new Color(102,153,102);
          case Room.DOMAIN_OUTDOORS_MOUNTAINS:
                  return new Color(153,102,00);
          case Room.DOMAIN_OUTDOORS_PLAINS:
                  return new Color(00,255,00);
          case Room.DOMAIN_OUTDOORS_ROCKS:
                  return new Color(153,102,00);
          case Room.DOMAIN_OUTDOORS_SPACEPORT:
                  return new Color(204,204,204);
          case Room.DOMAIN_OUTDOORS_SWAMP:
                  return new Color(00,102,00);
          case Room.DOMAIN_OUTDOORS_UNDERWATER:
                  return new Color(102,102,204);
          case Room.DOMAIN_OUTDOORS_WATERSURFACE:
                  return new Color(51,153,204);
          case Room.DOMAIN_OUTDOORS_WOODS:
                  return new Color(00,153,00);
          default:
                  return null;
          }
  }

}
