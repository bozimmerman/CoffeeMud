package com.planet_ink.coffee_mud.WebMacros.grinder;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.net.*;
import java.util.*;
import java.net.URLEncoder;

/*
   Copyright 2002-2018 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

public class GrinderMap extends GrinderFlatMap
{
	private GrinderRoom[][][] grid=null;
	protected int minZ = 0;
	protected int maxZ = 0;
	protected int zFix = 0;

	public GrinderMap()
	{
		areaMap = new Vector<GrinderRoom>();
		hashRooms = new Hashtable<String,GrinderRoom>();
		for (final Enumeration<Area> q = CMLib.map().areas(); q.hasMoreElements(); )
		{
			final Area A = q.nextElement();
			// for now, skip hidden areas.  Areas are often hidden if they aren't linked
			// to the world (ie under construction or Archon only)
			if ((CMLib.flags().isHidden(A))
			||(CMath.bset(A.flags(),Area.FLAG_INSTANCE_CHILD)))
					continue;
			for (final Enumeration<String> r = A.getProperRoomnumbers().getRoomIDs(); r.hasMoreElements(); )
			{
				final String roomID=r.nextElement();
				if((roomID.length() > 0)&&(roomID.indexOf("#(")<0))
				{
					final GrinderRoom GR = new GrinderRoom(roomID);
					areaMap.add(GR);
					hashRooms.put(GR.roomID, GR);
				}
			}
		}
	}

	public GrinderMap(Area A, int[] xyxy)
	{
		super(A,xyxy);
	}

	@Override
	public void rePlaceRooms()
	{
		if(areaMap==null)
			return;
		grid=null;
		placeRooms();
		hashRooms=null;
		rebuildGrid();
	}

	@Override
	public void rebuildGrid()
	{
		if (areaMap == null) return;
		// build grid!
		int xoffset = 0;
		int yoffset = 0;

		for (int x = 0; x < areaMap.size(); x++)
		{
			final GrinderRoom GR =  areaMap.get(x);
			if (GR.xy[0] < xoffset)
			{
				xoffset = GR.xy[0];
				if (debug) Log.sysOut("GR-REGRID", "xoffset set0: " + xoffset);
				if((debug) && (GR.xy[0]>0) )
					Log.sysOut("GR-REGRID", "positive GRx: " + GR.xy[0]);
			}
			if (GR.xy[1] < yoffset)
				yoffset = GR.xy[1];
		}

		xoffset = xoffset * -1;
		yoffset = yoffset * -1;

		if (debug) 
			Log.sysOut("GR-REGRID", "xoffset set : " + xoffset);
		if (debug) 
			Log.sysOut("GR-REGRID", "Xbound  set : " + Xbound);

		Xbound = 0;
		Ybound = 0;
		for (int x = 0; x < areaMap.size(); x++)
		{
			final GrinderRoom room = areaMap.get(x);
			room.xy[0] = room.xy[0] + xoffset;
			if (room.xy[0] > Xbound) Xbound = room.xy[0];
			room.xy[1] = room.xy[1] + yoffset;
			if (room.xy[1] > Ybound) Ybound = room.xy[1];
		}
		if (debug) 
			Log.sysOut("GR-REGRID", "Xbound  set2: " + Xbound);
		grid = new GrinderRoom[Xbound + 1][Ybound + 1][maxZ + 1];
		if (debug) 
			Log.sysOut("GR-REGRID", "GrinderRoom Grid Created: (x,y,z) " +
					  (Xbound + 1) + "," + (Ybound + 1) + "," + (maxZ + 1));
		for (int y = 0; y < areaMap.size(); y++)
		{
			final GrinderRoom room = areaMap.get(y);
			// this was hardcoded to look for below zero, but my zFix math
			// often ended up with minZ >= 1
			if(room.z<minZ)
			{
				grid[room.xy[0]][room.xy[1]][(room.z + zFix)] = room;
				if (debug) 
					Log.sysOut("GR-REGRID", "Room outside z range: " + room.z);
			}
			else
			{
				if ((debug) && ((room.z > maxZ) || (room.z < minZ)))
				{
					Log.sysOut("GR-HTML", "Room.z error: " + room.z + " outside " + maxZ + "-" + minZ + "(" +
								room.roomID + ")");
				}
				grid[room.xy[0]][room.xy[1]][room.z] = room;
			}
		}
	}

	protected GrinderRoom getProcessedRoomAt(Hashtable<String,GrinderRoom> processed, int x, int y, int z)
	{
		for (final Enumeration<GrinderRoom> e = processed.elements(); e.hasMoreElements(); )
		{
			final GrinderRoom room = e.nextElement();
			if ( (room.xy[0] == x) && (room.xy[1] == y) && (room.z == z))
				return room;
		}
		return null;
	}

	@Override
	public GrinderRoom getRoom(String ID)
	{
		if ( (hashRooms != null) && (hashRooms.containsKey(ID)))
			return hashRooms.get(ID);

		if (areaMap != null)
		{
			for (int r = 0; r < areaMap.size(); r++)
			{
				final GrinderRoom room = areaMap.get(r);
				if (room.roomID.equalsIgnoreCase(ID))
					return room;
			}
		}
		return null;
	}

	protected boolean isEmptyCluster(Hashtable<String,GrinderRoom> processed, int x, int y, int z)
	{
		for (final Enumeration<GrinderRoom> e = processed.elements(); e.hasMoreElements(); )
		{
			final GrinderRoom room = e.nextElement();
			if ( ( ( (room.xy[0] > x - CLUSTERSIZE) && (room.xy[0] < x + CLUSTERSIZE))
				  && ( (room.xy[1] > y - CLUSTERSIZE) && (room.xy[1] < y + CLUSTERSIZE)))
				|| ( (room.xy[0] == x) && (room.xy[1] == y)) && (room.z == z))
				return false;
		}
		return true;
	}

	protected void findEmptyCluster(Hashtable<String,GrinderRoom> processed, Vector<Integer> XYZ)
	{
		final int x = XYZ.elementAt(0).intValue();
		final int y = XYZ.elementAt(1).intValue();
		final int z = XYZ.elementAt(2).intValue();
		int spacing = CLUSTERSIZE;
		while (true)
		{
			for (int i = 0; i < 8; i++)
			{
				int yadjust = 0;
				int xadjust = 0;
				switch (i)
				{
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
				if (isEmptyCluster(processed, x + (spacing * xadjust), y + (spacing * yadjust), z))
				{
					XYZ.setElementAt(Integer.valueOf(x + (spacing * xadjust)), 0);
					XYZ.setElementAt(Integer.valueOf(y + (spacing * yadjust)), 1);
					XYZ.setElementAt(Integer.valueOf(z), 2);
					return;
				}
			}
			spacing += 1;
		}
	}

	@Override
	public void placeRooms()
	{
		if (areaMap == null)
			return;
		if (areaMap.size() == 0)
			return;

		for (int i = 0; i < areaMap.size(); i++)
		{
			final GrinderRoom room = areaMap.get(i);
			room.xy=new int[2];
			for (int d = 0; d < Directions.NUM_DIRECTIONS(); d++)
			{
				final GrinderDir dir = room.doors[d];
				if (dir != null)
					dir.positionedAlready = false;
			}
		}

		final Hashtable<String,GrinderRoom> processed = new Hashtable<String,GrinderRoom>();
		boolean doneSomething = true;

		while ( (areaMap.size() > processed.size()) && (doneSomething))
		{
			doneSomething = false;
			for (int i = 0; i < areaMap.size(); i++)
			{
				final GrinderRoom room = areaMap.get(i);
				if (!processed.containsKey(room.roomID))
				{
					placeRoom(room, 0, 0, processed, true, true, 0, 0);
					doneSomething = true;
				}
			}
		}

		// For sanity, we rehash all the Z levels into positive numbers
		// some overhead, but worthwhile
		for (int x = 0; x < areaMap.size(); x++)
		{
			final GrinderRoom GR = areaMap.get(x);
			if(GR.xy==null)
				Log.errOut("GrinderMap",GR.roomID+" not assigned an XY!");
			if (GR.z < minZ)
			{
				if (debug) Log.sysOut("GR-PLACERS", "minZ changed: " + minZ + " to " + GR.z);
				minZ = GR.z;
			}
			if (GR.z > maxZ)
			{
				if (debug) Log.sysOut("GR-PLACERS", "maxZ changed: " + maxZ + " to " + GR.z);
				maxZ = GR.z;
			}
		}

		zFix = maxZ - minZ;
		if ((zFix + minZ) > 0)
			zFix -= (0 - (minZ + zFix)) * -1;

		if (debug) 
			Log.sysOut("GR-PLACERS", "zFix set    : " + zFix);
		if (debug) 
			Log.sysOut("GR-PLACERS", "areaMap size: " + areaMap.size());
		int updatedCount = 0;
		for (int x = 0; x < areaMap.size(); x++)
		{
			final GrinderRoom GR = areaMap.get(x);
			final int oldZ = GR.z;
			GR.z += zFix;
			areaMap.set(x,GR);
			if (GR.z!=oldZ) updatedCount++;
		}
		if (debug) 
			Log.sysOut("GR-PLACERS", "maybe update: " + updatedCount);
		if (debug) 
			Log.sysOut("GR-PLACERS", "maxZ changed: " + maxZ + " to " + (maxZ + zFix));
		maxZ += zFix;
		if (debug) 
			Log.sysOut("GR-PLACERS", "minZ changed: " + minZ + " to " + (minZ + zFix));
		minZ += zFix;

		if (areaMap.size() > processed.size())
		{
			Log.errOut("GrinderMap",
					   areaMap.size() - processed.size() +
					   " room(s) were not placed.");
		}
	}

	@Override
	public StringBuffer getHTMLTable(HTTPRequest httpReq)
	{
		final StringBuffer buf = new StringBuffer("");
		// For now, we will populate the SELECT element prior to the
		// map layers, but for our cool hover list, we make it a DIV
		buf.append("<DIV id=\"layersMenu\" style=\"position:absolute; width:110px; "
					 + "height:200px; z-index:1000000" +
					 "; left: 0px; top: 10px; visibility: show\">");
		buf.append("<select name=\"layerSelect\" size=\"18\" onChange=\"showSelected()\">");
		for (int z = minZ; z <= maxZ; z++)
			buf.append("<option value=\"MapLayer" + z + "\">Level " + z + "</option>");

		buf.append("</select></div>");
		for (int z = 0; z <= maxZ; z++)
		{
			// Z levels are representations of elevation
			// As per the new evalation = LAYER handling
			// So, here we create the (for now) hidden DIV's
			buf.append("<DIV id=\"MapLayer" + z + "\" style=\"position:absolute; width:" +
					   ( (Xbound + 1) * 130) + "px; " + "height:" +
					   ( (Ybound + 1) * 120) + "px; z-index:" + z +
					   "; left: 120px; top: 10px; visibility: hidden\">");
			buf.append("<TABLE WIDTH=" + ( (Xbound + 1) * 130) +
					   " BORDER=0 CELLSPACING=0 CELLPADDING=0>");
			for(int l=0;l<5;l++)
			{
				buf.append("<TR HEIGHT=1>");
				for(int x=Xstart;x<=Xbound;x++)
					buf.append("<TD WIDTH=20><BR></TD><TD WIDTH=30><BR></TD><TD WIDTH=30><BR></TD><TD WIDTH=30><BR></TD><TD WIDTH=20><BR></TD>");
				buf.append("</TR>");
			}
			for (int y = 0; y <= Ybound; y++)
			{
				for (int l = 0; l < 5; l++)
				{
					buf.append("<TR HEIGHT=20>");
					for (int x = 0; x <= Xbound; x++)
					{
						if ((debug) && ((z > maxZ) || (z < minZ)))
							Log.sysOut("GR-HTML", "z error     : " + z + " outside " + maxZ + "-" + minZ);
						final GrinderRoom GR = grid[x][y][z];
						if (GR == null)
							buf.append("<TD COLSPAN=5"+((boundsXYXY!=null)?" ID=X"+(x+boundsXYXY[0])+"_"+(y+boundsXYXY[1]):"")+"><BR></TD>");
						else
						{
							switch (l)
							{
							case 0:
							{ // north, up
								buf.append("<TD>"+getDoorLabelGif(Directions.NORTHWEST,GR,httpReq)+"</TD>");
								buf.append("<TD><BR></TD>");
								buf.append("<TD>" +
										   getDoorLabelGif(Directions.NORTH, GR, httpReq) +
										   "</TD>");
								buf.append("<TD>" +
										   getDoorLabelGif(Directions.UP, GR, httpReq) +
										   "</TD>");
								buf.append("<TD>"+getDoorLabelGif(Directions.NORTHEAST,GR,httpReq)+"</TD>");
								break;
							}
							case 1:
							{ // west, east
								buf.append("<TD>" +
										   getDoorLabelGif(Directions.WEST, GR, httpReq) +
										   "</TD>");
								buf.append("<TD COLSPAN=3 ROWSPAN=3 VALIGN=TOP ");
								buf.append(roomColorStyle(GR));
								buf.append((boundsXYXY!=null)?" ID=X"+(x+boundsXYXY[0])+"_"+(y+boundsXYXY[1]):"");
								buf.append(">");
								String roomID = GR.roomID;
								if (roomID.startsWith(area.Name() + "#"))
								{
									roomID = roomID.substring(roomID.indexOf('#'));
								}
								try
								{
									buf.append("<a name=\"" +
												URLEncoder.encode(GR.roomID, "UTF-8") +
												"\" href=\"javascript:RC('" +GR.roomID
												+ "');\"><FONT SIZE=-1><B>" + roomID +
												"</B></FONT></a><BR>");
								}
								catch (final java.io.UnsupportedEncodingException e)
								{
								  Log.errOut("GrinderMap", "Wrong Encoding");
								}
								buf.append("<FONT SIZE=-2>(" + CMClass.classID(GR.room()) +
										   ")<BR>");
								String displayText = GR.room().displayText();
								if (displayText.length() > 20)
								{
									displayText = displayText.substring(0, 20) + "...";
								}
								buf.append(displayText + "</FONT></TD>");
								buf.append("<TD>" +
										   getDoorLabelGif(Directions.EAST, GR, httpReq) +
										   "</TD>");
								break;
							}
							case 2: // nada
								buf.append("<TD><BR></TD>");
								buf.append("<TD><BR></TD>");
								break;
							case 3:
							{ // alt e,w
								buf.append("<TD><BR></TD>");
								buf.append("<TD><BR></TD>");
								break;
							}
							case 4:
							{ // south, down
								buf.append("<TD>"+getDoorLabelGif(Directions.SOUTHWEST,GR,httpReq)+"</TD>");
								buf.append("<TD><BR></TD>");
								buf.append("<TD>" +
										   getDoorLabelGif(Directions.SOUTH, GR, httpReq) +
										   "</TD>");
								buf.append("<TD>" +
										   getDoorLabelGif(Directions.DOWN, GR, httpReq) +
										   "</TD>");
								buf.append("<TD>"+getDoorLabelGif(Directions.SOUTHEAST,GR,httpReq)+"</TD>");
								break;
							}
							default:
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

	@Override
	public StringBuffer getHTMLMap(HTTPRequest httpReq)
	{
		return getHTMLMap(httpReq, 4);
	}

	// this is much like getHTMLTable, but tiny rooms for world map viewing. No exits or ID's for now.
	@Override
	public StringBuffer getHTMLMap(HTTPRequest httpReq, int roomSize)
	{
		final StringBuffer buf = new StringBuffer("");
		// For now, we will populate the SELECT element prior to the
		// map layers, but for our cool hover list, we make it a DIV
		buf.append("<DIV id=\"layersMenu\" style=\"position:absolute; width:110px; "
					 + "height:200px; z-index:1000000" +
					 "; left: 0px; top: 10px; visibility: show\">");
		buf.append("<select name=\"layerSelect\" size=\"18\" onChange=\"showSelected()\">");
		String MaPlayer=httpReq.getUrlParameter("MAPLAYER");
		if(MaPlayer==null)
			MaPlayer="";
		for (int z = minZ; z <= maxZ; z++)
		{
			buf.append("<option value=\"MapLayer" + z + "\"");
			if(MaPlayer.equalsIgnoreCase("MapLayer"+z))
				buf.append(" SELECTED");
			buf.append(">Level " + z + "</option>");

		}
		buf.append("</select></div>");
		for (int z = 0; z <= maxZ; z++)
		{
			// Z levels are representations of elevation
			// As per the new evalation = LAYER handling
			// So, here we create the (for now) hidden DIV's
			buf.append("<DIV id=\"MapLayer" + z + "\" style=\"position:absolute; width:" +
					   ( (Xbound + 1) * roomSize) + "px; " + "height:" +
					   ( (Ybound + 1) * roomSize) + "px; z-index:" + z +
					   "; left: 120px; top: 10px; visibility: hidden\">");
			buf.append("<TABLE WIDTH=" + ( (Xbound + 1) * roomSize) +
					   " BORDER=0 CELLSPACING=0 CELLPADDING=0>");
			buf.append("<TR HEIGHT=" + roomSize + ">");
			for (int x = 0; x <= Xbound; x++)
				buf.append("<TD WIDTH=" + roomSize + " HEIGHT=" + roomSize +"></TD>");
			buf.append("</TR>");

			for (int y = 0; y <= Ybound; y++)
			{
				buf.append("<TR HEIGHT=" + roomSize + ">");
				for (int x = 0; x <= Xbound; x++)
				{
					if ( (debug) && ( (z > maxZ) || (z < minZ)))
						Log.sysOut("GR-HTML",
								   "z error     : " + z + " outside " + maxZ + "-" + minZ);
					final GrinderRoom GR = grid[x][y][z];
					final String tdins=(boundsXYXY!=null)?" ID=X"+(x+boundsXYXY[0])+"_"+(y+boundsXYXY[1]):"";
					if (GR == null)
						buf.append("<TD"+tdins+"></TD>");
					else
					{
						buf.append("<TD"+tdins+" ");
						if(!GR.isRoomGood())
							buf.append("BGCOLOR=BLACK");
						else
							buf.append(roomColorStyle(GR));
						buf.append("></TD>");
					}
				}
				buf.append("</TR>");
			}
			buf.append("</TABLE>");
			buf.append("</DIV>");
		}
		return buf;
	}

	@Override
	protected GrinderRoom getRoomInDir(GrinderRoom room, int d)
	{
		switch (d)
		{
		case Directions.NORTH:
			if (room.xy[1] > 0)
				return grid[room.xy[0]][room.xy[1] - 1][room.z];
			break;
		case Directions.SOUTH:
			if (room.xy[1] < Ybound)
				return grid[room.xy[0]][room.xy[1] + 1][room.z];
			break;
		case Directions.NORTHWEST:
			if((room.xy[1]>0)&&(room.xy[0]>0))
				return grid[room.xy[0]-1][room.xy[1]-1][room.z];
			break;
		case Directions.SOUTHWEST:
			if((room.xy[1]<Ybound)&&(room.xy[0]>0))
				return grid[room.xy[0]-1][room.xy[1]+1][room.z];
			break;
		case Directions.NORTHEAST:
			if((room.xy[1]>0)&&(room.xy[0]<Xbound))
				return grid[room.xy[0]+1][room.xy[1]-1][room.z];
			break;
		case Directions.SOUTHEAST:
			if((room.xy[1]<Ybound)&&(room.xy[0]<Xbound))
				return grid[room.xy[0]+1][room.xy[1]+1][room.z];
			break;
		case Directions.EAST:
			if (room.xy[0] < Xbound)
				return grid[room.xy[0] + 1][room.xy[1]][room.z];
			break;
		case Directions.WEST:
			if (room.xy[0] > 0)
				return grid[room.xy[0] - 1][room.xy[1]][room.z];
			break;
		case Directions.UP:
			if (room.z > maxZ)
				return grid[room.xy[0]][room.xy[1]][room.z - 1];
			break;
		case Directions.DOWN:
			if (room.z < minZ)
				return grid[room.xy[0]][room.xy[1]][room.z + 1];
			break;
		}
		return null;
	}

	public void placeRoom(GrinderRoom room,
						  int favoredX,
						  int favoredY,
						  Hashtable<String,GrinderRoom> processed,
						  boolean doNotDefer,
						  boolean passTwo,
						  int depth,
						  int zLevel)
	{
		if (room == null)
			return;
		if (depth > 500)
			return;

		final GrinderRoom anythingAt = getProcessedRoomAt(processed, favoredX, favoredY, zLevel);
		if (anythingAt != null)
		{
			// maybe someone else will take care of it?
			if (!doNotDefer)
			{
				for (int r = 0; r < areaMap.size(); r++)
				{
					final GrinderRoom roomToBlame = areaMap.get(r);
					if (roomToBlame != room)
					{
						for (int rd = 0; rd < Directions.NUM_DIRECTIONS(); rd++)
						{
							final GrinderDir RD = roomToBlame.doors[rd];
							if ( (RD != null)
							&& (RD.room != null)
							&& (!RD.positionedAlready)
							&& (RD.room.equals(room.roomID)))
								return;
						}
					}
				}
			}
			// nope; nobody can.  It's up to this!
			final Vector<Integer> XYZ = new Vector<Integer>();
			XYZ.addElement(Integer.valueOf(0));
			XYZ.addElement(Integer.valueOf(0));
			XYZ.addElement(Integer.valueOf(0));
			findEmptyCluster(processed, XYZ);
			room.xy[0] = XYZ.elementAt(0).intValue();
			room.xy[1] = XYZ.elementAt(1).intValue();
			room.z = XYZ.elementAt(2).intValue();
		}
		else
		{
			room.xy[0] = favoredX;
			room.xy[1] = favoredY;
			room.z = zLevel;
		}

		// once done, is never undone.  A room is
		// considered processed only once!
		processed.put(room.roomID, room);

		for (int d = 0; d < Directions.NUM_DIRECTIONS(); d++)
		{
			String roomID = null;
			if (room.doors[d] != null)
				roomID = room.doors[d].room;

			if ( (roomID != null)
			&& (roomID.length() > 0)
			&& (processed.get(roomID) == null)
			&& (passTwo || ( (d != Directions.UP) && (d != Directions.DOWN))))
			{
				final GrinderRoom nextRoom = getRoom(roomID);
				if (nextRoom != null)
				{
					int newFavoredX = room.xy[0];
					int newFavoredY = room.xy[1];
					int newZLevel = room.z;
					switch (d)
					{
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
					case Directions.NORTHEAST:
						newFavoredY--;
						newFavoredX++;
						break;
					case Directions.NORTHWEST:
						newFavoredY--;
						newFavoredX--;
						break;
					case Directions.SOUTHEAST:
						newFavoredY++;
						newFavoredX++;
						break;
					case Directions.SOUTHWEST:
						newFavoredY++;
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
