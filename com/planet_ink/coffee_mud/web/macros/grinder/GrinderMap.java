package com.planet_ink.coffee_mud.web.macros.grinder;

import java.net.*;
import java.util.*;
import java.net.URLEncoder;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class GrinderMap {
  private Vector areaMap = null;
  private Hashtable hashRooms = null;
  public GrinderRoom[][][] grid = null;
  public int Xbound = 0;
  public int Ybound = 0;
  public int minZ = 0;
  public int maxZ = 0;
  public int zFix = 0;
  public Area area = null;
  boolean debug = false;

  public GrinderMap(Area A) {
    area = A;
    areaMap = new Vector();
    hashRooms = new Hashtable();
    for (Enumeration r = A.getMap(); r.hasMoreElements(); ) {
      Room R = (Room) r.nextElement();
      if (R.roomID().length() > 0) {
        GrinderRoom GR = new GrinderRoom(R);
        areaMap.addElement(GR);
        hashRooms.put(GR.roomID, GR);
      }
    }
  }

  public GrinderMap() {
    areaMap = new Vector();
    hashRooms = new Hashtable();
    for (Enumeration q = CMMap.areas(); q.hasMoreElements(); ) {
      Area A = (Area) q.nextElement();
      // for now, skip hidden areas.  Areas are often hidden if they aren't linked
      // to the world (ie under construction or Archon only)
      if (A.fetchAffect("Prop_Hidden") != null)
        continue;
      for (Enumeration r = A.getMap(); r.hasMoreElements(); ) {
        Room R = (Room) r.nextElement();
        if (R.roomID().length() > 0) {
          GrinderRoom GR = new GrinderRoom(R);
          areaMap.addElement(GR);
          hashRooms.put(GR.roomID, GR);
        }
      }
    }
  }

  public void rebuildGrid() {
    if (areaMap == null) {
      return;
    }
    // build grid!
    int xoffset = 0;
    int yoffset = 0;

    for (int x = 0; x < areaMap.size(); x++) {
      GrinderRoom GR = (GrinderRoom) areaMap.elementAt(x);
      if (GR.x < xoffset) {
        xoffset = GR.x;
        if (debug) Log.sysOut("GR-REGRID", "xoffset set0: " + xoffset);
        if((debug) && (GR.x>0) ) Log.sysOut("GR-REGRID", "positive GRx: " + GR.x);
      }
      if (GR.y < yoffset) {
        yoffset = GR.y;
      }
    }

    xoffset = xoffset * -1;
    yoffset = yoffset * -1;

    if (debug) Log.sysOut("GR-REGRID", "xoffset set : " + xoffset);
    if (debug) Log.sysOut("GR-REGRID", "Xbound  set : " + Xbound);

    Xbound = 0;
    Ybound = 0;
    for (int x = 0; x < areaMap.size(); x++) {
      GrinderRoom room = (GrinderRoom) areaMap.elementAt(x);
      room.x = room.x + xoffset;
      if (room.x > Xbound) {
        Xbound = room.x;
      }
      room.y = room.y + yoffset;
      if (room.y > Ybound) {
        Ybound = room.y;
      }
    }
    if (debug) Log.sysOut("GR-REGRID", "Xbound  set2: " + Xbound);
    grid = new GrinderRoom[Xbound + 1][Ybound + 1][maxZ + 1];
    if (debug) Log.sysOut("GR-REGRID", "GrinderRoom Grid Created: (x,y,z) " +
                          (Xbound + 1) + "," + (Ybound + 1) + "," + (maxZ + 1));
    for (int y = 0; y < areaMap.size(); y++) {
      GrinderRoom room = (GrinderRoom) areaMap.elementAt(y);
      // this was hardcoded to look for below zero, but my zFix math
      // often ended up with minZ >= 1
      if(room.z<minZ)
      {
        grid[room.x][room.y][(room.z + zFix)] = room;
        if (debug) Log.sysOut("GR-REGRID", "Room outside z range: " + room.z);
      }
      else
      {
        if ((debug) && ((room.z > maxZ) || (room.z < minZ)))
              Log.sysOut("GR-HTML", "Room.z error: " + room.z + " outside " + maxZ + "-" + minZ + "(" +
                         room.roomID + ")");
        grid[room.x][room.y][room.z] = room;
      }
    }
  }

  public void rePlaceRooms() {
    if (areaMap == null) {
      return;
    }
    grid = null;
    hashRooms = null;
    placeRooms();
    rebuildGrid();
  }

  private GrinderRoom getProcessedRoomAt(Hashtable processed, int x, int y, int z) {
    for (Enumeration e = processed.elements(); e.hasMoreElements(); ) {
      GrinderRoom room = (GrinderRoom) e.nextElement();
      if ( (room.x == x) && (room.y == y) && (room.z == z)) {
        return room;
      }
    }
    return null;
  }

  public GrinderRoom getRoom(String ID) {
    if ( (hashRooms != null) && (hashRooms.containsKey(ID))) {
      return (GrinderRoom) hashRooms.get(ID);
    }

    if (areaMap != null) {
      for (int r = 0; r < areaMap.size(); r++) {
        GrinderRoom room = (GrinderRoom) areaMap.elementAt(r);
        if (room.roomID.equalsIgnoreCase(ID)) {
          return room;
        }
      }
    }
    return null;
  }

  private final static int CLUSTERSIZE = 3;

  private boolean isEmptyCluster(Hashtable processed, int x, int y, int z) {
    for (Enumeration e = processed.elements(); e.hasMoreElements(); ) {
      GrinderRoom room = (GrinderRoom) e.nextElement();
      if ( ( ( (room.x > x - CLUSTERSIZE) && (room.x < x + CLUSTERSIZE))
            && ( (room.y > y - CLUSTERSIZE) && (room.y < y + CLUSTERSIZE)))
          || ( (room.x == x) && (room.y == y)) && (room.z == z)) {
        return false;
      }
    }
    return true;
  }

  private void findEmptyCluster(Hashtable processed, Vector XYZ) {
    int x = ( (Integer) XYZ.elementAt(0)).intValue();
    int y = ( (Integer) XYZ.elementAt(1)).intValue();
    int z = ( (Integer) XYZ.elementAt(2)).intValue();
    int spacing = CLUSTERSIZE;
    while (true) {
      for (int i = 0; i < 8; i++) {
        int yadjust = 0;
        int xadjust = 0;
        switch (i) {
          case 0:
            xadjust = 1;
            break;
          case 1:
            xadjust = 1;
            yadjust = 1;
            break;
          case 2:
            yadjust = 1;
            break;
          case 3:
            xadjust = 1;
            xadjust = -1;
            break;
          case 4:
            xadjust = -1;
            break;
          case 5:
            xadjust = -1;
            yadjust = -1;
            break;
          case 6:
            yadjust = -1;
            break;
          case 7:
            yadjust = -1;
            xadjust = 1;
            break;
        }
        // I'm letting EmptyCluster always search the current Z level
        if (isEmptyCluster(processed, x + (spacing * xadjust),
                           y + (spacing * yadjust), z)) {
          XYZ.setElementAt(new Integer(x + (spacing * xadjust)), 0);
          XYZ.setElementAt(new Integer(y + (spacing * yadjust)), 1);
          XYZ.setElementAt(new Integer(z), 2);
          return;
        }
      }
      spacing += 1;
    }
  }

  public boolean anythingThatDirection(GrinderRoom room, int direction) {
    GrinderDir D = room.doors[direction];
    if ( (D == null) || ( (D != null) && (D.room.length() == 0))) {
      return false;
    }
    return true;
  }

  public void getRadiantRooms(GrinderRoom room,
                              Vector rooms,
                              int maxDepth) {
    int depth = 0;
    if (room == null) {
      return;
    }
    if (rooms.contains(room)) {
      return;
    }
    rooms.addElement(room);
    int min = 0;
    int size = rooms.size();
    while (depth < maxDepth) {
      for (int r = min; r < size; r++) {
        GrinderRoom R1 = (GrinderRoom) rooms.elementAt(r);
        if (R1 != null) {
          for (int d = 0; d < 6; d++) {
            GrinderDir R = R1.doors[d];
            GrinderRoom GR = ( (R != null) ? getRoom(R.room) : null);
            if ( (GR != null) && (!rooms.contains(GR))) {
              rooms.addElement(GR);
            }
          }
        }
      }
      min = size;
      size = rooms.size();
      depth++;
    }
  }

  public void placeRooms() {
    if (areaMap == null) {
      return;
    }
    if (areaMap.size() == 0) {
      return;
    }

    for (int i = 0; i < areaMap.size(); i++) {
      GrinderRoom room = (GrinderRoom) areaMap.elementAt(i);
      room.x = 0;
      room.y = 0;
      for (int d = 0; d < Directions.NUM_DIRECTIONS; d++) {
        GrinderDir dir = room.doors[d];
        if (dir != null) {
          dir.positionedAlready = false;
        }
      }
    }

    Hashtable processed = new Hashtable();
    boolean doneSomething = true;

    while ( (areaMap.size() > processed.size()) && (doneSomething)) {
      doneSomething = false;
      for (int i = 0; i < areaMap.size(); i++) {
        GrinderRoom room = (GrinderRoom) areaMap.elementAt(i);
        if (!processed.containsKey(room.roomID)) {
          placeRoom(room, 0, 0, processed, true, true, 0, 0);
          doneSomething = true;
        }
      }
    }

    // For sanity, we rehash all the Z levels into positive numbers
    // some overhead, but worthwhile
    for (int x = 0; x < areaMap.size(); x++) {
      GrinderRoom GR = (GrinderRoom) areaMap.elementAt(x);
      if (GR.z < minZ) {
        if (debug) Log.sysOut("GR-PLACERS", "minZ changed: " + minZ + " to " + GR.z);
        minZ = GR.z;
      }
      if (GR.z > maxZ) {
        if (debug) Log.sysOut("GR-PLACERS", "maxZ changed: " + maxZ + " to " + GR.z);
        maxZ = GR.z;
      }
    }

    zFix = maxZ - minZ;
    if ((zFix + minZ) > 0){
      zFix -= (0 - (minZ + zFix)) * -1;
    }
    if (debug) Log.sysOut("GR-PLACERS", "zFix set    : " + zFix);
    if (debug) Log.sysOut("GR-PLACERS", "areaMap size: " + areaMap.size());
    int updatedCount = 0;
    for (int x = 0; x < areaMap.size(); x++) {
      GrinderRoom GR = (GrinderRoom) areaMap.elementAt(x);
      int oldZ = GR.z;
      GR.z += zFix;
      areaMap.setElementAt(GR,x);
      if (GR.z!=oldZ) updatedCount++;
    }
    if (debug) Log.sysOut("GR-PLACERS", "maybe update: " + updatedCount);
    if (debug) Log.sysOut("GR-PLACERS", "maxZ changed: " + maxZ + " to " + (maxZ + zFix));
    maxZ += zFix;
    if (debug) Log.sysOut("GR-PLACERS", "minZ changed: " + minZ + " to " + (minZ + zFix));
    minZ += zFix;

    if (areaMap.size() > processed.size()) {
      Log.errOut("GrinderMap",
                 areaMap.size() - processed.size() +
                 " room(s) were not placed.");
    }
  }

  public StringBuffer getHTMLTable(ExternalHTTPRequests httpReq) {
    StringBuffer buf = new StringBuffer("");
    // For now, we will populate the SELECT element prior to the
    // map layers, but for our cool hover list, we make it a DIV
    buf.append("<DIV id=\"layersMenu\" style=\"position:absolute; width:110px; "
                 + "height:200px; z-index:1000000" +
                 "; left: 0px; top: 10px; visibility: show\">");
    buf.append("<select name=\"layerSelect\" size=\"18\" onChange=\"showSelected()\">");
    for (int z = minZ; z <= maxZ; z++) {
      buf.append("<option value=\"MapLayer" + z + "\">Level " + z + "</option>");
    }
    buf.append("</select></div>");
    for (int z = 0; z <= maxZ; z++) {
      // Z levels are representations of elevation
      // As per the new evalation = LAYER handling
      // So, here we create the (for now) hidden DIV's
      buf.append("<DIV id=\"MapLayer" + z + "\" style=\"position:absolute; width:" +
                 ( (Xbound + 1) * 138) + "px; " + "height:" +
                 ( (Ybound + 1) * 120) + "px; z-index:" + z +
                 "; left: 120px; top: 10px; visibility: hidden\">");
      buf.append("<TABLE WIDTH=" + ( (Xbound + 1) * 138) +
                 " BORDER=0 CELLSPACING=0 CELLPADDING=0>");
      for (int y = 0; y <= Ybound; y++) {
        for (int l = 0; l < 5; l++) {
          buf.append("<TR HEIGHT=24>");
          for (int x = 0; x <= Xbound; x++) {
            if ((debug) && ((z > maxZ) || (z < minZ)))
              Log.sysOut("GR-HTML", "z error     : " + z + " outside " + maxZ + "-" + minZ);
            GrinderRoom GR = grid[x][y][z];
            if (GR == null) {
              buf.append("<TD COLSPAN=5 WIDTH=138><BR></TD>");
            }
            else {
              switch (l) {
                case 0: { // north, up
                  buf.append("<TD WIDTH=24><BR></TD>");
                  buf.append("<TD WIDTH=30>" +
                             getDoorLabelGif(Directions.NORTH, GR, httpReq) +
                             "</TD>");
                  buf.append("<TD WIDTH=30><BR></TD>");
                  buf.append("<TD WIDTH=30>" +
                             getDoorLabelGif(Directions.UP, GR, httpReq) +
                             "</TD>");
                  buf.append("<TD WIDTH=24><BR></TD>");
                }
                break;
                case 1: { // west, east
                  buf.append("<TD WIDTH=24>" +
                             getDoorLabelGif(Directions.WEST, GR, httpReq) +
                             "</TD>");
                  buf.append("<TD WIDTH=90 COLSPAN=3 ROWSPAN=3 VALIGN=TOP ");
                  switch (GR.room.domainType()) {
                    case Room.DOMAIN_INDOORS_AIR:
                      buf.append("BGCOLOR=\"#FFFFFF\"");
                      break;
                    case Room.DOMAIN_INDOORS_MAGIC:
                      buf.append("BGCOLOR=\"#996600\"");
                      break;
                    case Room.DOMAIN_INDOORS_CAVE:
                      buf.append("BGCOLOR=\"#CC99FF\"");
                      break;
                    case Room.DOMAIN_INDOORS_STONE:
                      buf.append("BGCOLOR=\"#CC00FF\"");
                      break;
                    case Room.DOMAIN_INDOORS_UNDERWATER:
                      buf.append("BGCOLOR=\"#6666CC\"");
                      break;
                    case Room.DOMAIN_INDOORS_WATERSURFACE:
                      buf.append("BGCOLOR=\"#3399CC\"");
                      break;
                    case Room.DOMAIN_INDOORS_WOOD:
                      buf.append("BGCOLOR=\"#999900\"");
                      break;
                    case Room.DOMAIN_OUTDOORS_AIR:
                      buf.append("BGCOLOR=\"#FFFFFF\"");
                      break;
                    case Room.DOMAIN_OUTDOORS_CITY:
                      buf.append("BGCOLOR=\"#CCCCCC\"");
                      break;
                    case Room.DOMAIN_OUTDOORS_DESERT:
                      buf.append("BGCOLOR=\"#FFFF66\"");
                      break;
                    case Room.DOMAIN_OUTDOORS_HILLS:
                      buf.append("BGCOLOR=\"#99CC33\"");
                      break;
                    case Room.DOMAIN_OUTDOORS_JUNGLE:
                      buf.append("BGCOLOR=\"#669966\"");
                      break;
                    case Room.DOMAIN_OUTDOORS_MOUNTAINS:
                      buf.append("BGCOLOR=\"#996600\"");
                      break;
                    case Room.DOMAIN_OUTDOORS_PLAINS:
                      buf.append("BGCOLOR=\"#00FF00\"");
                      break;
                    case Room.DOMAIN_OUTDOORS_ROCKS:
                      buf.append("BGCOLOR=\"#996600\"");
                      break;
                    case Room.DOMAIN_OUTDOORS_SWAMP:
                      buf.append("BGCOLOR=\"#006600\"");
                      break;
                    case Room.DOMAIN_OUTDOORS_UNDERWATER:
                      buf.append("BGCOLOR=\"#6666CC\"");
                      break;
                    case Room.DOMAIN_OUTDOORS_WATERSURFACE:
                      buf.append("BGCOLOR=\"#3399CC\"");
                      break;
                    case Room.DOMAIN_OUTDOORS_WOODS:
                      buf.append("BGCOLOR=\"#009900\"");
                      break;
                    default:
                      buf.append("BGCOLOR=\"#CCCCFF\"");
                      break;
                  }
                  buf.append(">");
                  String roomID = GR.roomID;
                  if (roomID.startsWith(area.Name() + "#")) {
                    roomID = roomID.substring(roomID.indexOf("#"));
                  }
                  try {
                    buf.append("<a name=\"" +
                               URLEncoder.encode(GR.roomID, "UTF-8") +
                        "\" href=\"javascript:Clicked('rmmenu.cmvp','','" +
                        GR.roomID + "','');\"><FONT SIZE=-1><B>" + roomID +
                        "</B></FONT></a><BR>");
                  }
                  catch (java.io.UnsupportedEncodingException e) {
                    Log.errOut("GrinderMap", "Wrong Encoding");
                  }
                  buf.append("<FONT SIZE=-2>(" + CMClass.className(GR.room) +
                             ")<BR>");
                  String displayText = GR.room.displayText();
                  if (displayText.length() > 20) {
                    displayText = displayText.substring(0, 20) + "...";
                  }
                  buf.append(displayText + "</FONT></TD>");
                  buf.append("<TD WIDTH=24>" +
                             getDoorLabelGif(Directions.EAST, GR, httpReq) +
                             "</TD>");
                }
                break;
                case 2: // nada
                  buf.append("<TD WIDTH=24><BR></TD>");
                  buf.append("<TD WIDTH=24><BR></TD>");
                  break;
                case 3: { // alt e,w
                  buf.append("<TD WIDTH=24><BR></TD>");
                  buf.append("<TD WIDTH=24><BR></TD>");
                  break;
                }
                case 4: { // south, down
                  buf.append("<TD WIDTH=24><BR></TD>");
                  buf.append("<TD WIDTH=30>" +
                             getDoorLabelGif(Directions.SOUTH, GR, httpReq) +
                             "</TD>");
                  buf.append("<TD WIDTH=30><BR></TD>");
                  buf.append("<TD WIDTH=30>" +
                             getDoorLabelGif(Directions.DOWN, GR, httpReq) +
                             "</TD>");
                  buf.append("<TD WIDTH=24><BR></TD>");
                }
                break;
              }
            }
          }
          buf.append("</TR>");
        }
      }
      buf.append("</TABLE>");
      buf.append("</DIV>");
    }
    return buf;
  }

  public StringBuffer getHTMLMap(ExternalHTTPRequests httpReq) {
    return getHTMLMap(httpReq, 4);
  }

  // this is much like getHTMLTable, but tiny rooms for world map viewing. No exits or ID's for now.
  public StringBuffer getHTMLMap(ExternalHTTPRequests httpReq, int roomSize) {
    StringBuffer buf = new StringBuffer("");
    // For now, we will populate the SELECT element prior to the
    // map layers, but for our cool hover list, we make it a DIV
    buf.append("<DIV id=\"layersMenu\" style=\"position:absolute; width:110px; "
                 + "height:200px; z-index:1000000" +
                 "; left: 0px; top: 10px; visibility: show\">");
    buf.append("<select name=\"layerSelect\" size=\"18\" onChange=\"showSelected()\">");
    for (int z = minZ; z <= maxZ; z++) {
      buf.append("<option value=\"MapLayer" + z + "\">Level " + z + "</option>");
    }
    buf.append("</select></div>");
    for (int z = 0; z <= maxZ; z++) {
      // Z levels are representations of elevation
      // As per the new evalation = LAYER handling
      // So, here we create the (for now) hidden DIV's
      buf.append("<DIV id=\"MapLayer" + z + "\" style=\"position:absolute; width:" +
                 ( (Xbound + 1) * roomSize) + "px; " + "height:" +
                 ( (Ybound + 1) * roomSize) + "px; z-index:" + z +
                 "; left: 120px; top: 10px; visibility: hidden\">");
      buf.append("<TABLE WIDTH=" + ( (Xbound + 1) * roomSize) +
                 " BORDER=0 CELLSPACING=0 CELLPADDING=0>");
      for (int y = 0; y <= Ybound; y++) {
        buf.append("<TR HEIGHT=" + roomSize + ">");
        for (int x = 0; x <= Xbound; x++) {
          if ( (debug) && ( (z > maxZ) || (z < minZ)))
            Log.sysOut("GR-HTML",
                       "z error     : " + z + " outside " + maxZ + "-" + minZ);
          GrinderRoom GR = grid[x][y][z];
          if (GR == null) {
            buf.append("<TD WIDTH=" + roomSize + " HEIGHT=" + roomSize +
                       "><font size=1>&nbsp;</font></TD>");
          }
          else {
            buf.append("<TD WIDTH=" + roomSize + " HEIGHT=" + roomSize + " ");
            switch (GR.room.domainType()) {
              case Room.DOMAIN_INDOORS_AIR:
                buf.append("BGCOLOR=\"#FFFFFF\"");
                break;
              case Room.DOMAIN_INDOORS_MAGIC:
                buf.append("BGCOLOR=\"#996600\"");
                break;
              case Room.DOMAIN_INDOORS_CAVE:
                buf.append("BGCOLOR=\"#CC99FF\"");
                break;
              case Room.DOMAIN_INDOORS_STONE:
                buf.append("BGCOLOR=\"#CC00FF\"");
                break;
              case Room.DOMAIN_INDOORS_UNDERWATER:
                buf.append("BGCOLOR=\"#6666CC\"");
                break;
              case Room.DOMAIN_INDOORS_WATERSURFACE:
                buf.append("BGCOLOR=\"#3399CC\"");
                break;
              case Room.DOMAIN_INDOORS_WOOD:
                buf.append("BGCOLOR=\"#999900\"");
                break;
              case Room.DOMAIN_OUTDOORS_AIR:
                buf.append("BGCOLOR=\"#FFFFFF\"");
                break;
              case Room.DOMAIN_OUTDOORS_CITY:
                buf.append("BGCOLOR=\"#CCCCCC\"");
                break;
              case Room.DOMAIN_OUTDOORS_DESERT:
                buf.append("BGCOLOR=\"#FFFF66\"");
                break;
              case Room.DOMAIN_OUTDOORS_HILLS:
                buf.append("BGCOLOR=\"#99CC33\"");
                break;
              case Room.DOMAIN_OUTDOORS_JUNGLE:
                buf.append("BGCOLOR=\"#669966\"");
                break;
              case Room.DOMAIN_OUTDOORS_MOUNTAINS:
                buf.append("BGCOLOR=\"#996600\"");
                break;
              case Room.DOMAIN_OUTDOORS_PLAINS:
                buf.append("BGCOLOR=\"#00FF00\"");
                break;
              case Room.DOMAIN_OUTDOORS_ROCKS:
                buf.append("BGCOLOR=\"#996600\"");
                break;
              case Room.DOMAIN_OUTDOORS_SWAMP:
                buf.append("BGCOLOR=\"#006600\"");
                break;
              case Room.DOMAIN_OUTDOORS_UNDERWATER:
                buf.append("BGCOLOR=\"#6666CC\"");
                break;
              case Room.DOMAIN_OUTDOORS_WATERSURFACE:
                buf.append("BGCOLOR=\"#3399CC\"");
                break;
              case Room.DOMAIN_OUTDOORS_WOODS:
                buf.append("BGCOLOR=\"#009900\"");
                break;
              default:
                buf.append("BGCOLOR=\"#CCCCFF\"");
                break;
            }
            buf.append("><font size=1>&nbsp;</font></TD>");
          }
        }
        buf.append("</TR>");
      }
      buf.append("</TABLE>");
      buf.append("</DIV>");
    }
    return buf;
  }


  private GrinderRoom getRoomInDir(GrinderRoom room, int d) {
    switch (d) {
      case Directions.NORTH:
        if (room.y > 0) {
          return grid[room.x][room.y - 1][room.z];
        }
        break;
      case Directions.SOUTH:
        if (room.y < Ybound) {
          return grid[room.x][room.y + 1][room.z];
        }
        break;
      case Directions.EAST:
        if (room.x < Xbound) {
          return grid[room.x + 1][room.y][room.z];
        }
        break;
      case Directions.WEST:
        if (room.x > 0) {
          return grid[room.x - 1][room.y][room.z];
        }
        break;
      case Directions.UP:
        if (room.z > maxZ) {
          return grid[room.x][room.y][room.z - 1];
        }
      case Directions.DOWN:
        if (room.z < minZ) {
          return grid[room.x][room.y][room.z + 1];
        }
    }
    return null;
  }

  private int findRelGridDir(GrinderRoom room, String roomID) {
    for (int d = 0; d < Directions.NUM_DIRECTIONS; d++) {
      GrinderRoom possRoom = getRoomInDir(room, d);
      if ( (possRoom != null) && (possRoom.roomID.equals(roomID))) {
        return d;
      }
    }
    return -1;
  }

  private String getDoorLabelGif(int d, GrinderRoom room,
                                 ExternalHTTPRequests httpReq) {
    GrinderDir dir = (GrinderDir) room.doors[d];
    String dirLetter = "" +
        Directions.getDirectionName(d).toUpperCase().charAt(0);
    GrinderRoom roomPointer = null;
    if ( (dir == null) || ( (dir != null) && (dir.room.length() == 0))) {
      return "<a href=\"javascript:Clicked('lnkxmenu.cmvp','" +
          Directions.getDirectionName(d) + "','" + room.roomID +
          "','');\"><IMG BORDER=0 SRC=\"images/E" + dirLetter + ".gif\"></a>";
    }
    else
    roomPointer = getRoomInDir(room, d);

    if ( (dir.room.length() > 0) &&
        ( (roomPointer == null) ||
         ( (roomPointer != null) && (!roomPointer.roomID.equals(dir.room))))) {
      dirLetter += "R";
    }
    String theRest = ".gif\" BORDER=0 ALT=\"" + Directions.getDirectionName(d) +
        " to " + dir.room + "\"></a>";
    Exit exit = dir.exit;
    if (exit == null) {
      return "<a href=\"javascript:Clicked('edxmenu.cmvp','" +
          Directions.getDirectionName(d) + "','" + room.roomID + "','" +
          dir.room + "');\"><IMG SRC=\"images/U" + dirLetter + theRest;
    }
    else
    if (exit.hasADoor()) {
      return "<a href=\"javascript:Clicked('edxmenu.cmvp','" +
          Directions.getDirectionName(d) + "','" + room.roomID + "','" +
          dir.room + "');\"><IMG SRC=\"images/D" + dirLetter + theRest;
    }
    else {
      return "<a href=\"javascript:Clicked('edxmenu.cmvp','" +
          Directions.getDirectionName(d) + "','" + room.roomID + "','" +
          dir.room + "');\"><IMG SRC=\"images/O" + dirLetter + theRest;
    }
  }

  public void placeRoom(GrinderRoom room,
                        int favoredX,
                        int favoredY,
                        Hashtable processed,
                        boolean doNotDefer,
                        boolean passTwo,
                        int depth,
                        int zLevel) {
    if (room == null) {
      return;
    }
    if (depth > 500) {
      return;
    }
    GrinderRoom anythingAt = getProcessedRoomAt(processed, favoredX, favoredY, zLevel);
    if (anythingAt != null) {
      // maybe someone else will take care of it?
      if (!doNotDefer) {
        for (int r = 0; r < areaMap.size(); r++) {
          GrinderRoom roomToBlame = (GrinderRoom) areaMap.elementAt(r);
          if (roomToBlame != room) {
            for (int rd = 0; rd < Directions.NUM_DIRECTIONS; rd++) {
              GrinderDir RD = roomToBlame.doors[rd];
              if ( (RD != null)
                  && (RD.room != null)
                  && (!RD.positionedAlready)
                  && (RD.room.equals(room.roomID))) {
                return;
              }
            }
          }
        }
      }
      // nope; nobody can.  It's up to this!
      Vector XYZ = new Vector();
      XYZ.addElement(new Integer(0));
      XYZ.addElement(new Integer(0));
      XYZ.addElement(new Integer(0));
      findEmptyCluster(processed, XYZ);
      room.x = ( (Integer) XYZ.elementAt(0)).intValue();
      room.y = ( (Integer) XYZ.elementAt(1)).intValue();
      room.z = ( (Integer) XYZ.elementAt(2)).intValue();
    }
    else {
      room.x = favoredX;
      room.y = favoredY;
      room.z = zLevel;
    }

    // once done, is never undone.  A room is
    // considered processed only once!
    processed.put(room.roomID, room);

    for (int d = 0; d < Directions.NUM_DIRECTIONS; d++) {
      String roomID = null;
      if (room.doors[d] != null) {
        roomID = ( (GrinderDir) room.doors[d]).room;

      }
      if ( (roomID != null)
          && (roomID.length() > 0)
          && (processed.get(roomID) == null)
          && (passTwo || ( (d != Directions.UP) && (d != Directions.DOWN)))) {
        GrinderRoom nextRoom = getRoom(roomID);
        if (nextRoom != null) {
          int newFavoredX = room.x;
          int newFavoredY = room.y;
          int newZLevel = room.z;
          switch (d) {
            case Directions.NORTH:
              newFavoredY--;
              break;
            case Directions.SOUTH:
              newFavoredY++;
              break;
            case Directions.EAST:
              newFavoredX++;
              break;
            case Directions.WEST:
              newFavoredX--;
              break;
            case Directions.UP:
              newZLevel++;
              break;
            case Directions.DOWN:
              newZLevel--;
              break;
          }
          room.doors[d].positionedAlready = true;
          placeRoom(nextRoom, newFavoredX, newFavoredY, processed, false,
                    passTwo, depth + 1, newZLevel);
        }
      }
    }
  }
}
