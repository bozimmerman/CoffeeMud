package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.HostState;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.StdAutoGenInstance;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.Area.Stats;
import com.planet_ink.coffee_mud.Areas.interfaces.GridZones.XYVector;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine.MPContext;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.GridLocale.CrossExit;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2025-2025 Bo Zimmerman

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
public class AutoGrid extends StdGrid implements GridLocale, AutoGenArea
{
	protected volatile long				expires			= 0;
	protected String					filePath		= "randareas/example.xml";
	protected Map<String, String>		varMap			= new Hashtable<String, String>(1);
	protected Map<Integer, Room>		roomLinks		= new HashMap<Integer,Room>();

	@Override
	public String ID()
	{
		return "AutoGrid";
	}

	public AutoGrid()
	{
		super();
	}

	@Override
	public Room prepareRoomInDir(final Room fromRoom, final int direction)
	{
		if((expires < Long.MAX_VALUE)
		&&(CMath.bset(getArea().flags(),Area.FLAG_INSTANCE_CHILD)
			||CMath.bset(getArea().flags(),Area.FLAG_INSTANCE_PARENT)
			||(roomID().length()==0)))
		{
			if(playerNearby())
				expires = 0;
		}
		return super.prepareRoomInDir(fromRoom, direction);
	}

	@Override
	protected Room[][] getBuiltGrid()
	{
		if(System.currentTimeMillis() > expires)
			clearGrid(null);
		return super.getBuiltGrid();
	}

	protected boolean playerNearby()
	{
		for(int d1=0;d1<Directions.NUM_DIRECTIONS();d1++)
		{
			final Room R1 = doors[d1];
			if(R1 != null)
			{
				if(R1.numPCInhabitants()>0)
					return true;
				for(int d2=0;d2<Directions.NUM_DIRECTIONS();d2++)
				{
					final Room R2 = R1.getRawDoor(d2);
					if(R2 != null)
					{
						if(R2.numPCInhabitants()>0)
							return true;
					}
				}
			}
		}
		return false;
	}

	protected boolean canAutoGenerateNow()
	{
		if(!CMProps.isState(HostState.RUNNING))
			return false;
		if(expires == Long.MAX_VALUE)
			return true;

		if(CMath.bset(getArea().flags(),Area.FLAG_INSTANCE_CHILD)
		||CMath.bset(getArea().flags(),Area.FLAG_INSTANCE_PARENT)
		||(roomID().length()==0))
		{
			if(playerNearby())
				return true;
			return false;
		}
		return true;
	}

	@Override
	public synchronized void buildGrid()
	{
		if(!canAutoGenerateNow())
		{
			expires = System.currentTimeMillis() + (CMProps.getTickMillis() * 2);
			super.buildGrid();
			return;
		}
		expires = System.currentTimeMillis() + (CMProps.getTickMillis() * 8);
		clearGrid(null);
		try
		{
			final Exit ox=CMClass.getExit("Open");
			final StringBuffer xml = Resources.getFileResource(getGeneratorXmlPath(), true);
			if((xml==null)||(xml.length()==0))
			{
				Log.errOut("Unable to load '"+xml+"' for "+roomID());
				super.buildGrid();
				return;
			}
			final List<XMLLibrary.XMLTag> xmlRoot = CMLib.xml().parseAllXML(xml);
			final Hashtable<String,Object> definedIDs = new Hashtable<String,Object>();
			CMLib.percolator().buildDefinedIDSet(xmlRoot,definedIDs, new TreeSet<String>());
			String idName = "";
			final List<String> idChoices = new Vector<String>();
			for(final String key : getAutoGenVariables().keySet())
			{
				if(key.equalsIgnoreCase("AREA_ID")
				||key.equalsIgnoreCase("AREA_IDS")
				||key.equalsIgnoreCase("AREAID")
				||key.equalsIgnoreCase("AREAIDS"))
					idChoices.addAll(CMParms.parseCommas(getAutoGenVariables().get(key),true));
			}
			if(idChoices.size()==0)
			{
				for(final Object key : definedIDs.keySet())
				{
					final Object val=definedIDs.get(key);
					if((key instanceof String)
					&&(val instanceof XMLTag)
					&&(((XMLTag)val).tag().equalsIgnoreCase("area")))
					{
						final XMLTag piece=(XMLTag)val;
						final String inserter = piece.getParmValue("INSERT");
						if(inserter!=null)
						{
							final List<String> V=CMParms.parseCommas(inserter,true);
							for(int v=0;v<V.size();v++)
							{
								String s = V.get(v);
								if(s.startsWith("$"))
									s=s.substring(1).trim();
								final XMLTag insertPiece =(XMLTag)definedIDs.get(s.toUpperCase().trim());
								if(insertPiece == null)
									continue;
								if(insertPiece.tag().equalsIgnoreCase("area"))
								{
									if(!idChoices.contains(s.toUpperCase().trim()))
										idChoices.add(s.toUpperCase().trim());
								}
							}
						}
						else
							idChoices.add((String)key);
					}
				}
			}

			if(idChoices.size()>0)
				idName=idChoices.get(CMLib.dice().roll(1, idChoices.size(), -1)).toUpperCase().trim();

			if((!(definedIDs.get(idName) instanceof XMLTag))
			||(!((XMLTag)definedIDs.get(idName)).tag().equalsIgnoreCase("area")))
			{
				Log.errOut(L("The area id '@x1' has not been defined in the data file for @x2.",idName,roomID()));
				super.buildGrid();
				return;
			}
			definedIDs.put("AREANAME", displayText());
			if(!definedIDs.containsKey("AREASIZE"))
				definedIDs.put("AREASIZE", ""+this.getGridSize());
			for(final String key : getAutoGenVariables().keySet())
			{
				if(!(key.equalsIgnoreCase("AREA_ID")
					||key.equalsIgnoreCase("AREA_IDS")
					||key.equalsIgnoreCase("AREAID")
					||key.equalsIgnoreCase("AREAIDS")))
				{
					final String val = getAutoGenVariables().get(key);
					definedIDs.put(key.toUpperCase(),val);
				}
			}
			if(!definedIDs.containsKey("LEVEL_RANGE"))
			{
				Log.warnOut(L("LEVEL_RANGE is missing argument for @x1",roomID()));
				definedIDs.put("LEVEL_RANGE", ""+1);
			}
			if(!definedIDs.containsKey("AGGROCHANCE"))
			{
				Log.warnOut(L("AGGROCHANCE is missing argument for @x1",roomID()));
				definedIDs.put("AGGROCHANCE", "50");
			}
			try
			{
				XMLTag piece=(XMLTag)definedIDs.get(idName);
				final List<XMLTag> pieces = CMLib.percolator().getAllChoices(piece.tag(), piece, definedIDs);
				if(pieces.size()>0)
					piece=pieces.get(CMLib.dice().roll(1, pieces.size(), -1));
				if(!definedIDs.containsKey("THEME"))
				{
					final Map<String,String> unfilled = CMLib.percolator().getUnfilledRequirements(definedIDs,piece);
					final List<String> themes = CMParms.parseCommas(unfilled.get("THEME"), true);
					if(themes.size()>0)
						definedIDs.put("THEME", themes.get(CMLib.dice().roll(1, themes.size(), -1)).toUpperCase().trim());
				}
				try
				{
					CMLib.percolator().checkRequirements(piece, definedIDs);
				}
				catch(final CMException cme)
				{
					Log.errOut(L("Required ids for @x1 were missing: @x2 for @x3",idName,cme.getMessage(),roomID()));
					super.buildGrid();
					return;
				}
				int direction = -1;
				Room fromR = null;
				Exit fromE = null;
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					final Room R2=this.getRawDoor(d);
					final Exit E2=this.getRawExit(d);
					if((R2 != null)
					&&(E2!=null)
					&&(R2.getRawDoor(Directions.getOpDirectionCode(d))==this))
					{
						fromR=R2;
						fromE = E2;
						direction=d;
						break;
					}
				}
				final int magicDir = Directions.getOpDirectionCode(direction);
				if(direction >= 0)
				{
					definedIDs.put("ROOMTAG_NODEGATEEXIT", CMLib.directions().getDirectionName(direction));
					definedIDs.put("ROOMTAG_GATEEXITROOM", fromR);
					definedIDs.put("ROOMTAG_GATEEXITCLASS", fromE);
					definedIDs.put("ROOMTAG_NODEGATEEXIT"+magicDir, CMLib.directions().getDirectionName(direction));
					definedIDs.put("ROOMTAG_GATEEXITROOM"+magicDir, fromR);
					definedIDs.put("ROOMTAG_GATEEXITCLASS"+magicDir, fromE);
					for(int dir = 0; dir<Directions.NUM_DIRECTIONS(); dir++)
					{
						final Room linkR = getRawDoor(dir);
						if((linkR != null)
						&&((linkR.roomID().length()>0)
							||((linkR.getGridParent()!=null)&&(linkR.getGridParent().roomID().length()>0)))
						&& (linkR.getRawDoor(Directions.getOpDirectionCode(dir))==this))
						{
							final Exit E = linkR.getRawExit(Directions.getOpDirectionCode(dir));
							if(E != null)
							{
								definedIDs.put("ROOMTAG_NODEGATEEXIT"+dir, CMLib.directions().getDirectionName(dir));
								definedIDs.put("ROOMTAG_GATEEXITROOM"+dir, linkR);
								definedIDs.put("ROOMTAG_GATEEXITCLASS"+dir, E);
							}
							else
								Log.errOut("Room "+linkR.roomID()+" needs an Exit object "+Directions.getOpDirectionCode(dir));
						}
					}
				}
				// finally, fill out the new random map!
				final Area fakeArea = CMClass.getAreaType("StdThinInstance");
				fakeArea.setName("TEMPAREA"+new Random(System.nanoTime()).nextDouble());
				if(!CMLib.percolator().fillInArea(piece, definedIDs, fakeArea, magicDir))
				{
					Log.errOut(L("Failed to enter the new area for @x1.  Try again later.",roomID()));
					super.buildGrid();
					return;
				}
				final Set<Room> unnodifiedRooms = new HashSet<Room>();
				for(final Enumeration<Room> r=fakeArea.getProperMap();r.hasMoreElements();)
					unnodifiedRooms.add(r.nextElement());
				CMLib.percolator().postProcess(definedIDs);
				@SuppressWarnings("unchecked")
				final List<LayoutNode> nodes = (List<LayoutNode>)definedIDs.get("LAYOUT_NODES");
				if((nodes==null)||(nodes.size()<1))
				{
					Log.errOut(L("Failed to create the new area for @x1.  Try again later.",roomID()));
					super.buildGrid();
					return;
				}
				int lowestX = 0;
				int lowestY = 0;
				for(final LayoutNode node : nodes)
				{
					if(node.coord().length>1)
					{
						if(node.coord()[0] < lowestX)
							lowestX = (int)node.coord()[0];
						if(node.coord()[1] < lowestY)
							lowestY = (int)node.coord()[1];
						if(node.room()!=null)
							unnodifiedRooms.remove(node.room());
					}
				}
				for(final LayoutNode node : nodes)
				{
					node.coord()[0] -= lowestX;
					node.coord()[1] -= lowestY;
				}
				int highestX = 0;
				int highestY = 0;
				for(final LayoutNode node : nodes)
				{
					if(node.coord()[0] > highestX)
						highestX = (int)node.coord()[0];
					if(node.coord()[1] > highestY)
						highestY = (int)node.coord()[1];
				}
				while((highestX * highestY) < (nodes.size()+unnodifiedRooms.size()))
				{
					highestX++;
					highestY++;
				}
				super.subMap = new Room[highestX+1][highestY+1];
				for(final LayoutNode node : nodes)
				{
					if(node.room() != null)
					{
						fakeArea.delProperRoom(node.room());
						node.room().setRoomID("");
						node.room().setGridParent(this);
						node.room().setSavable(false);
						final int rx = (int)node.coord()[0];
						final int ry = (int)node.coord()[1];
						subMap[rx][ry] = node.room();
						node.room().setArea(getArea());
					}
				}
				if(unnodifiedRooms.size()>0)
				{
					final List<int[]> freexys = new ArrayList<int[]>();
					for(int x=0;x<subMap.length;x++)
					{
						for(int y=0;y<subMap[x].length;y++)
							if(subMap[x][y]==null)
								freexys.add(new int[] {x,y});
					}
					final int midx = highestX/2;
					final int midy = highestY/2;
					freexys.sort(new Comparator<int[]>() {
						@Override
						public int compare(final int[] o1, final int[] o2)
						{
							final int d1 = Math.abs(o1[0] - midx) + Math.abs(o1[1] - midy);
							final int d2 = Math.abs(o2[0] - midx) + Math.abs(o2[1] - midy);
							return Integer.valueOf(d1).compareTo(Integer.valueOf(d2));
						}
					});

					for(final Room unR : unnodifiedRooms)
					{
						fakeArea.delProperRoom(unR);
						unR.setRoomID("");
						unR.setGridParent(this);
						unR.setSavable(false);
						if(freexys.size()>0)
						{
							final int[] mine = freexys.remove(0);
							subMap[mine[0]][mine[1]] = unR;
						}
						unR.setArea(getArea());
					}
				}
				buildFinalLinks(direction, nodes.get(0).room()); // links to outside rooms
				fillInTheExtraneousExternals(subMap,ox);
				expires = Long.MAX_VALUE;
				Resources.removeResource("HELP_" + getArea().Name().toUpperCase());
				Resources.removeResource("STATS_" + getArea().Name().toUpperCase());
			}
			catch(final CMException cme)
			{
				Log.errOut("AutoGrid",cme);
				super.buildGrid();
				return;
			}
		}
		catch(final Exception e)
		{
			Log.errOut("StdGrid",e);
			clearGrid(null);
		}
	}

	protected Room getSafeSubMapRoom(final int x, final int y, final int d)
	{
		if((x>=0) && (x<subMap.length)
		&& (y<subMap[x].length) && (subMap[x][y]!=null)
		&& (subMap[x][y].getRawDoor(d)==null))
			return subMap[x][y];
		return null;
	}

	@Override
	protected Room getGridRoomFrom(final Room loc, final int direction)
	{
		final Integer dirI = Integer.valueOf(Directions.getOpDirectionCode(direction));
		if(roomLinks.containsKey(dirI))
			return roomLinks.get(dirI);
		return super.getGridRoomFrom(loc, direction);
	}

	protected void buildFinalLinks(final int magicDir, final Room magicRoom)
	{
		roomLinks.clear();
		final Exit ox=CMClass.getExit("Open");
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if(d==Directions.GATE)
				continue;
			final Room dirRoom=doors[d];
			Exit dirExit=getRawExit(d);
			if((dirExit==null)||(dirExit.hasADoor()))
				dirExit=ox;
			if(dirRoom!=null)
			{
				Exit altExit=dirRoom.getRawExit(Directions.getOpDirectionCode(d));
				if(altExit==null)
					altExit=ox;
				if(d == magicDir)
				{
					roomLinks.put(Integer.valueOf(d),magicRoom);
					linkRoom(magicRoom,dirRoom,d,dirExit,altExit);
				}
				else
				{
					Room linkRoom = null;
					final int mid = (xsize>ysize)?(xsize/2):(ysize/2);
					switch(d)
					{
					case Directions.NORTH:
					{
						for(int y=0;y<ysize && (linkRoom == null);y++)
						{
							final int halfX = subMap.length/2;
							for(int xd=0;xd<=halfX && (linkRoom == null);xd++)
							{
								for(final int m : new int[] {xd,-xd})
									linkRoom = getSafeSubMapRoom(halfX+m,y,d);
							}
						}
						break;
					}
					case Directions.SOUTH:
					{
						for(int y=ysize-1;y>=0 && (linkRoom == null);y--)
						{
							final int halfX = subMap.length/2;
							for(int xd=0;xd<=halfX && (linkRoom == null);xd++)
							{
								for(final int m : new int[] {xd,-xd})
									linkRoom = getSafeSubMapRoom(halfX+m,y,d);
							}
						}
						break;
					}
					case Directions.EAST:
					{
						for(int x=xsize-1;x>=0 && (linkRoom == null) && x<subMap.length;x--)
						{
							final int halfY = subMap[x].length/2;
							for(int yd=0;yd<=halfY && (linkRoom == null);yd++)
							{
								for(final int m : new int[] {yd,-yd})
									linkRoom = getSafeSubMapRoom(x,halfY+m,d);
							}
						}
						break;
					}
					case Directions.WEST:
					{
						for(int x=0;x<xsize && (linkRoom == null) && x<subMap.length;x++)
						{
							final int halfY = subMap[x].length/2;
							for(int yd=0;yd<=halfY && (linkRoom == null);yd++)
							{
								for(final int m : new int[] {yd,-yd})
									linkRoom = getSafeSubMapRoom(x,halfY+m,d);
							}
						}
						break;
					}
					case Directions.NORTHEAST:
					{
						for(int m=0;m<mid;m++)
						{
							for(int x=xsize-1-m;x>=0 && (linkRoom == null) && x<subMap.length;x--)
							{
								for(int y=0;y<m && (linkRoom == null) && y<subMap[x].length;y++)
									linkRoom = getSafeSubMapRoom(x,y,d);
							}
						}
						linkRoom(subMap[subMap.length-1][0],dirRoom,d,dirExit,altExit);
						break;
					}
					case Directions.NORTHWEST:
					{
						for(int m=0;m<mid;m++)
						{
							for(int x=0;x<=m && (linkRoom == null) && x<subMap.length;x++)
							{
								for(int y=0;y<=m && (linkRoom == null) && y<subMap[x].length;y++)
									linkRoom = getSafeSubMapRoom(x,y,d);
							}
						}
						linkRoom(subMap[subMap.length-1][0],dirRoom,d,dirExit,altExit);
						break;
					}
					case Directions.SOUTHEAST:
					{
						for(int m=0;m<mid;m++)
						{
							for(int x=xsize-1-m;x>=0 && (linkRoom == null) && x<subMap.length;x--)
							{
								for(int y=ysize-1-m;y>=0 && (linkRoom == null) && y<subMap[x].length;y--)
									linkRoom = getSafeSubMapRoom(x,y,d);
							}
						}
						linkRoom(subMap[subMap.length-1][0],dirRoom,d,dirExit,altExit);
						break;
					}
					case Directions.SOUTHWEST:
					{
						for(int m=0;m<mid;m++)
						{
							for(int x=0;x<=m && (linkRoom == null) && x<subMap.length;x++)
							{
								for(int y=ysize-1-m;y>=0 && (linkRoom == null) && y<subMap[x].length;y--)
									linkRoom = getSafeSubMapRoom(x,y,d);
							}
						}
						linkRoom(subMap[subMap.length-1][0],dirRoom,d,dirExit,altExit);
						break;
					}
					case Directions.UP:
					case Directions.DOWN:
					{
						for(int m=0;m<mid;m++)
						{
							for(int x=mid-m;x<=mid+m && (linkRoom == null) && x<subMap.length;x++)
							{
								for(int y=mid-m;y<=mid+m && (linkRoom == null) && y<subMap.length;y++)
									linkRoom = getSafeSubMapRoom(x,y,d);
							}
						}
						linkRoom(subMap[subMap.length-1][0],dirRoom,d,dirExit,altExit);
						break;
					}
					default:
						break;
					}
					if(linkRoom != null)
					{
						roomLinks.put(Integer.valueOf(d),linkRoom);
						linkRoom(linkRoom,dirRoom,d,dirExit,altExit);
					}
				}
			}
		}
	}

	@Override
	public Room getGridChild(final String childCode)
	{
		if(childCode.equalsIgnoreCase(roomID()))
			return this;
		if(!childCode.toUpperCase().startsWith(roomID().toUpperCase()+"#("))
			return null;
		final int len=roomID().length()+2;
		final int comma=childCode.indexOf(',',len);
		if(comma<0)
			return null;
		final int x=CMath.s_int(childCode.substring(len,comma));
		final int y=CMath.s_int(childCode.substring(comma+1,childCode.length()-1));
		return getGridChild(x,y);
	}

	@Override
	public Room getGridChild(final int x, final int y)
	{
		if((subMap==null)
		||(x<0)
		||(y<0)
		||(x>=subMap.length)
		||(y>=subMap[x].length))
			return null;
		return subMap[x][y];
	}

	@Override
	public String getGeneratorXmlPath()
	{
		return filePath;
	}

	@Override
	public Map<String, String> getAutoGenVariables()
	{
		return varMap;
	}

	@Override
	public void setGeneratorXmlPath(final String path)
	{
		filePath = path;
	}

	@Override
	public void setAutoGenVariables(final Map<String, String> vars)
	{
		varMap = vars;
	}

	@Override
	public void setAutoGenVariables(final String vars)
	{
		setAutoGenVariables(CMParms.parseEQParms(vars));
	}

	@Override
	public boolean resetInstance(final Room returnToRoom)
	{
		this.clearGrid(returnToRoom);
		return true;
	}

	private final static String[] MYCODES={"GENERATIONFILEPATH","OTHERVARS"};

	@Override
	public String getStat(final String code)
	{
		if(CMParms.indexOfIgnoreCase(super.getStatCodes(), code)>=0)
			return super.getStat(code);
		else
		switch(getLocalCodeNum(code))
		{
			case 0:
				return this.getGeneratorXmlPath();
			case 1:
				return CMParms.toEqListString(this.getAutoGenVariables());
			default:
				break;
		}
		return "";
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(CMParms.indexOfIgnoreCase(super.getStatCodes(), code)>=0)
			super.setStat(code, val);
		else
		switch(getLocalCodeNum(code))
		{
			case 0:
				setGeneratorXmlPath(val);
				break;
			case 1:
				setAutoGenVariables(val);
				break;
			default:
				break;
		}
	}

	protected int getLocalCodeNum(final String code)
	{
		for(int i=0;i<MYCODES.length;i++)
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}

	private static String[] codes=null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(AutoGrid.MYCODES,this);
		final String[] superCodes=super.getStatCodes();
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof AutoGrid))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}

}
