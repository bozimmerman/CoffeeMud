package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.GridZones.XYVector;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.GenCage;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

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

public class StdGrid extends StdRoom implements GridLocale
{
	@Override
	public String ID()
	{
		return "StdGrid";
	}

	protected Room[][] 		  subMap=null;
	protected String[] 		  descriptions=new String[0];
	protected String[] 		  displayTexts=new String[0];
	protected List<CrossExit> gridexits=new SVector<CrossExit>();
	protected int xsize=5;
	protected int ysize=5;

	public StdGrid()
	{
		super();
		myID=getClass().getName().substring(getClass().getName().lastIndexOf('.')+1);
	}

	@Override
	protected void cloneFix(Room E)
	{
		super.cloneFix(E);
		if(E instanceof StdGrid)
		{
			descriptions=((StdGrid)E).descriptions.clone();
			displayTexts=((StdGrid)E).displayTexts.clone();
			gridexits=new SVector<CrossExit>(((StdGrid)E).gridexits);
		}
	}

	@Override
	public String getGridChildLocaleID()
	{
		return "StdRoom";
	}

	@Override
	public Room getGridChild(XYVector xy)
	{
		if(xy==null)
			return null;
		return this.getGridChild(xy.x,xy.y);
	}

	@Override
	public CMObject newInstance()
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.FATGRIDS))
		{
			String name=ID();
			if(!name.endsWith("Grid"))
				return super.newInstance();
			name=name.substring(0,name.length()-4)+"ThinGrid";
			final Room R=CMClass.getLocale(name);
			if(R==null)
				return super.newInstance();
			if(R instanceof StdGrid)
				return super.newInstance();
			return R.newInstance();
		}
		return super.newInstance();
	}

	@Override
	public void destroy()
	{
		super.destroy();
		if(subMap!=null)
		{
			for (final Room[] element : subMap)
			{
				for(int y=0;y<element.length;y++)
				{
					if(element[y]!=null)
						element[y].destroy();
				}
			}
		}
		subMap=null;
		descriptions=null;
		displayTexts=null;
		gridexits=null;
	}

	@Override
	public int xGridSize()
	{
		return xsize;
	}

	@Override
	public int yGridSize()
	{
		return ysize;
	}

	@Override
	public void setXGridSize(int x)
	{
		if (x > 0)
			xsize = x;
	}

	@Override
	public void setYGridSize(int y)
	{
		if (y > 0)
			ysize = y;
	}

	@Override
	public XYVector getRoomXY(String roomID)
	{
		final Room room=CMLib.map().getRoom(roomID);
		if(room==null)
			return null;
		return getRoomXY(room);
	}

	@Override
	public XYVector getRoomXY(Room room)
	{
		final Room[][] subMap=getBuiltGrid();
		if(subMap!=null)
		{
			for(int x=0;x<subMap.length;x++)
			{
				for(int y=0;y<subMap[x].length;y++)
				{
					if(subMap[x][y]==room)
						return new XYVector(x,y);
				}
			}
		}
		return null;
	}

	@Override
	public Room prepareRoomInDir(Room fromRoom, int direction)
	{
		if(amDestroyed)
		{
			final Room R=CMLib.map().getRoom(roomID());
			if(R!=null)
				return R.prepareRoomInDir(fromRoom,direction);
			return super.prepareRoomInDir(fromRoom,direction);
		}
		return getAltRoomFrom(fromRoom,direction);
	}

	@Override
	public Room prepareGridLocale(Room fromRoom, Room toRoom, int direction)
	{
		return toRoom;
	}

	@Override
	public void setDescription(String newDescription)
	{
		super.setDescription(newDescription);
		final Vector<String> descriptions=new Vector<String>();
		int x=newDescription.toUpperCase().indexOf("<P>");
		while(x>=0)
		{
			final String s=newDescription.substring(0,x).trim();
			if(s.length()>0)
				descriptions.addElement(s);
			newDescription=newDescription.substring(x+3).trim();
			x=newDescription.toUpperCase().indexOf("<P>");
		}
		if(newDescription.length()>0)
			descriptions.addElement(newDescription);
		this.descriptions=descriptions.toArray(new String[0]);
	}

	@Override
	public void setDisplayText(String newDisplayText)
	{
		super.setDisplayText(newDisplayText);
		final Vector<String> displayTexts=new Vector<String>();
		int x=newDisplayText.toUpperCase().indexOf("<P>");
		while(x>=0)
		{
			final String s=newDisplayText.substring(0,x).trim();
			if(s.length()>0)
				displayTexts.addElement(s);
			newDisplayText=newDisplayText.substring(x+3).trim();
			x=newDisplayText.toUpperCase().indexOf("<P>");
		}
		if(newDisplayText.length()>0)
			displayTexts.addElement(newDisplayText);
		this.displayTexts=displayTexts.toArray(new String[0]);
	}

	@Override 
	public Iterator<CrossExit> outerExits()
	{
		return gridexits.iterator();
	}
	
	@Override 
	public void addOuterExit(CrossExit x)
	{
		if(x!=null)
			gridexits.add(x);
	}
	
	@Override 
	public void delOuterExit(CrossExit x)
	{
		gridexits.remove(x);
	}

	public Room getAltRoomFrom(Room loc, int direction)
	{
		if((loc==null)||(direction<0))
			return null;
		final int opDirection=Directions.getOpDirectionCode(direction);

		getBuiltGrid();
		Room[][] grid=null;
		final List<CrossExit> gridexits=this.gridexits;
		if((gridexits!=null)&&(gridexits.size()>0))
		{
			grid=getBuiltGrid();
			final String roomID=CMLib.map().getExtendedRoomID(loc);
			if(grid!=null)
			{
				for(int d=0;d<gridexits.size();d++)
				{
					final CrossExit EX=gridexits.get(d);
					if((!EX.out)
					&&(EX.destRoomID.equalsIgnoreCase(roomID))
					&&(EX.dir==direction)
					&&(EX.x>=0)&&(EX.y>=0)&&(EX.x<xGridSize())&&(EX.y<yGridSize())
					&&(grid[EX.x][EX.y]!=null))
						return grid[EX.x][EX.y];
				}
			}
		}

		final Room oldLoc=loc;
		if(loc.getGridParent()!=null)
			loc=loc.getGridParent();
		if((oldLoc!=loc)&&(loc instanceof GridLocale))
		{
			if(grid==null)
				grid=getBuiltGrid();
			if(grid!=null)
			{
				final XYVector xy=((GridLocale)loc).getRoomXY(oldLoc);
				if((xy!=null)&&(xy.x>=0)&&(xy.y>=0))
				{
					switch(opDirection)
					{
					case Directions.EAST:
						if((((GridLocale)loc).yGridSize()==yGridSize()))
							return grid[grid.length-1][xy.y];
						break;
					case Directions.WEST:
						if((((GridLocale)loc).yGridSize()==yGridSize()))
							return grid[0][xy.y];
						break;
					case Directions.NORTHWEST:
						return grid[0][0];
					case Directions.NORTHEAST:
						return grid[grid.length-1][0];
					case Directions.NORTH:
						if((((GridLocale)loc).xGridSize()==xGridSize()))
							return grid[xy.x][0];
						break;
					case Directions.SOUTH:
						if((((GridLocale)loc).xGridSize()==xGridSize()))
							return grid[xy.x][grid[0].length-1];
						break;
					case Directions.SOUTHEAST:
						return grid[grid.length-1][grid[0].length-1];
					case Directions.SOUTHWEST:
						return grid[0][grid[0].length-1];
					}
				}
			}
		}
		return findCenterRoom(opDirection);
	}

	protected Room[][] getBuiltGrid()
	{
		if(subMap==null)
			buildGrid();
		if(subMap!=null)
			return subMap.clone();
		return null;
	}

	@Override
	public Room getRandomGridChild()
	{
		final List<Room> V=getAllRooms();
		if(V.size()==0)
			return null;
		return V.get(CMLib.dice().roll(1,V.size(),-1));
	}

	@Override
	public List<Room> getAllRooms()
	{
		final Vector<Room> V=new Vector<Room>();
		final Room[][] subMap=getBuiltGrid();
		if(subMap!=null)
		{
			for (final Room[] element : subMap)
			{
				for(int y=0;y<element.length;y++)
					V.addElement(element[y]);
			}
		}
		return V;
	}
	
	@Override
	public List<Room> getAllRoomsFilled()
	{
		final Vector<Room> V=new Vector<Room>();
		final Room[][] subMap=getBuiltGrid();
		if(subMap!=null)
		{
			for (final Room[] element : subMap)
			{
				for(int y=0;y<element.length;y++)
				{
					Room R=element[y];
					if(R==null)
					{
						V.clear();
						if(!this.amDestroyed())
							return getAllRoomsFilled();
						return V;
					}
					if(!V.contains(R))
						V.addElement(R);
					final List<Room> sky = R.getSky();
					if(sky == null)
						Log.errOut("No Sky for "+R.roomID());
					else
					{
						for(Room R2 : sky)
						{
							if(R2 instanceof GridLocale)
								V.addAll(((GridLocale)R2).getAllRoomsFilled());
							else
							if(!V.contains(R2))
								V.add(R2);
						}
					}
				}
			}
		}
		return V;
	}
	
	private static final Iterator<Room> emptyIterator = new Iterator<Room>()
	{
		@Override
		public boolean hasNext()
		{
			return false;
		}

		@Override
		public Room next()
		{
			throw new NoSuchElementException();
		}

		@Override
		public void remove()
		{
			throw new NoSuchElementException();
		}
	};
	
	@Override
	public final Iterator<Room> getExistingRooms()
	{
		final Room[][] map=this.subMap;
		if(subMap!=null)
		{
			return new Iterator<Room>()
			{
				int x=0;
				int y=0;
				Room R=null;
				private void setRoom()
				{
					while(R==null)
					{
						while((x<map.length)&&(y>=map[x].length))
						{
							y=0;
							x++;
						}
						if(x>=map.length)
							return;
						if((x<map.length)&&(y<map[x].length))
							R=map[x][y];
						if(R==null)
							y++;
					}
				}

				@Override
				public boolean hasNext()
				{
					setRoom();
					return R != null;
				}

				@Override
				public Room next()
				{
					setRoom();
					final Room r = R;
					y++;
					R=null;
					setRoom();
					return r;
				}

				@Override
				public void remove()
				{
					throw new java.lang.UnsupportedOperationException();
				}
			};
		}
		return emptyIterator;
	}

	protected void halfLink(Room room, Room loc, int dirCode, Exit o)
	{
		if(room==null)
			return;
		if(loc==null)
			return;
		if(room.rawDoors()[dirCode]!=null)
		{
			if(room.rawDoors()[dirCode].getGridParent()==null)
				return;
			if(room.rawDoors()[dirCode].getGridParent().isMyGridChild(room.rawDoors()[dirCode]))
				return;
			room.rawDoors()[dirCode]=null;
		}
		if(o==null)
			o=CMClass.getExit("Open");
		room.rawDoors()[dirCode]=alternativeLink(room,loc,dirCode);
		room.setRawExit(dirCode,o);
	}

	protected Room alternativeLink(Room room, Room defaultRoom, int dir)
	{
		if((subMap!=null)&&(room.getGridParent()==this)&&(gridexits!=null))
		for(int d=0;d<gridexits.size();d++)
		{
			final CrossExit EX=gridexits.get(d);
			try
			{
				if((EX.out)&&(EX.dir==dir)
				&&(EX.x>=0)&&(EX.y>=0)&&(EX.x<xGridSize())&&(EX.y<yGridSize())
				&&(subMap[EX.x][EX.y]==room))
				{
					final Room R=CMLib.map().getRoom(EX.destRoomID);
					if(R!=null)
					{
						if(R.getGridParent()!=null)
							return R.getGridParent();
						return R;
					}
				}
			}
			catch(final Exception e)
			{
			}
		}
		return defaultRoom;
	}

	protected void linkRoom(Room room, Room loc, int dirCode, Exit o, Exit ao)
	{
		if(loc==null)
			return;
		if(room==null)
			return;
		final int opCode=Directions.getOpDirectionCode(dirCode);
		if(room.rawDoors()[dirCode]!=null)
		{
			if(room.rawDoors()[dirCode].getGridParent()==null)
				return;
			if(room.rawDoors()[dirCode].getGridParent().isMyGridChild(room.rawDoors()[dirCode]))
				return;
			room.rawDoors()[dirCode]=null;
		}
		if(o==null)
			o=CMClass.getExit("Open");
		room.rawDoors()[dirCode]=alternativeLink(room,loc,dirCode);
		room.setRawExit(dirCode,o);
		if(loc.rawDoors()[opCode]!=null)
		{
			if(loc.rawDoors()[opCode].getGridParent()==null)
				return;
			if(loc.rawDoors()[opCode].getGridParent().isMyGridChild(loc.rawDoors()[opCode]))
				return;
			loc.rawDoors()[opCode]=null;
		}
		if(ao==null)
			ao=CMClass.getExit("Open");
		loc.rawDoors()[opCode]=alternativeLink(loc,room,opCode);
		loc.setRawExit(opCode,ao);
	}

	protected int[] initCenterRoomXY(int dirCode)
	{
		final int[] xy=new int[2];
		switch(dirCode)
		{
		case Directions.NORTHEAST:
			xy[0]=xGridSize()-1;
			break;
		case Directions.NORTHWEST:
			break;
		case Directions.NORTH:
			xy[0]=xGridSize()/2;
			break;
		case Directions.SOUTHEAST:
			xy[1]=yGridSize()-1;
			xy[0]=xGridSize()-1;
			break;
		case Directions.SOUTHWEST:
			xy[1]=yGridSize()-1;
			break;
		case Directions.SOUTH:
			xy[0]=xGridSize()/2;
			xy[1]=yGridSize()-1;
			break;
		case Directions.EAST:
			xy[0]=xGridSize()-1;
			xy[1]=yGridSize()/2;
			break;
		case Directions.WEST:
			xy[1]=yGridSize()/2;
			break;
		default:
			xy[0]=xGridSize()/2;
			xy[1]=yGridSize()/2;
			break;
		}
		return xy;
	}

	private static int[][] XY_ADJUSTMENT_CHART=null;
	private static int[] XY_ADJUSTMENT_DEFAULT={1,1};
	protected int[] initCenterRoomAdjustsXY(int dirCode)
	{
		if((dirCode<0)||(dirCode>=Directions.NUM_DIRECTIONS()))
			return XY_ADJUSTMENT_DEFAULT;
		if((XY_ADJUSTMENT_CHART!=null)&&(dirCode<XY_ADJUSTMENT_CHART.length))
			return XY_ADJUSTMENT_CHART[dirCode];

		final int[][] xy=new int[Directions.NUM_DIRECTIONS()][2];
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			switch(d)
			{
			case Directions.NORTHEAST:
			case Directions.NORTH:
			case Directions.SOUTHEAST:
			case Directions.SOUTH:
				xy[d][0]=1;
				xy[d][1]=0;
				break;
			case Directions.NORTHWEST:
			case Directions.EAST:
			case Directions.SOUTHWEST:
			case Directions.WEST:
				xy[d][0]=0;
				xy[d][1]=1;
				break;
			default:
				xy[d][0]=1;
				xy[d][1]=1;
				break;
			}
		}
		XY_ADJUSTMENT_CHART=xy;
		return xy[dirCode];
	}

	protected Room findCenterRoom(int dirCode)
	{
		final int[] xy=initCenterRoomXY(dirCode);
		final int[] adjXY=initCenterRoomAdjustsXY(dirCode);

		final Room returnRoom=null;
		int xadjust=0;
		int yadjust=0;
		boolean moveAndCheckAgain=true;
		while((subMap!=null)&&(moveAndCheckAgain))
		{
			moveAndCheckAgain=false;

			if(((xy[0]-xadjust)>=0)&&((xy[1]-yadjust)>=0))
			{
				moveAndCheckAgain=true;
				try
				{
					if(subMap[xy[0]-xadjust][xy[1]-yadjust]!=null)
						return subMap[xy[0]-xadjust][xy[1]-yadjust];
				}
				catch(final Exception e)
				{
				}
			}

			if(((xy[0]+xadjust)<xGridSize())&&((xy[1]+yadjust)<yGridSize()))
			{
				moveAndCheckAgain=true;
				try
				{
					if(subMap[xy[0]+xadjust][xy[1]+yadjust]!=null)
						return subMap[xy[0]+xadjust][xy[1]+yadjust];
				}
				catch(final Exception e)
				{
				}
			}
			if(moveAndCheckAgain)
			{
				xadjust+=adjXY[0];
				yadjust+=adjXY[1];
			}
		}
		return returnRoom;
	}

	protected void buildFinalLinks()
	{
		final Exit ox=CMClass.getExit("Open");
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if(d==Directions.GATE)
				continue;
			final Room dirRoom=rawDoors()[d];
			Exit dirExit=getRawExit(d);
			if((dirExit==null)||(dirExit.hasADoor()))
				dirExit=ox;
			if(dirRoom!=null)
			{
				Exit altExit=dirRoom.getRawExit(Directions.getOpDirectionCode(d));
				if(altExit==null)
					altExit=ox;
				switch(d)
				{
				case Directions.NORTH:
					for (final Room[] element : subMap)
						linkRoom(element[0],dirRoom,d,dirExit,altExit);
					break;
				case Directions.SOUTH:
					for (final Room[] element : subMap)
						linkRoom(element[element.length-1],dirRoom,d,dirExit,altExit);
					break;
				case Directions.NORTHEAST:
					linkRoom(subMap[subMap.length-1][0],dirRoom,d,dirExit,altExit);
					break;
				case Directions.NORTHWEST:
					linkRoom(subMap[0][0],dirRoom,d,dirExit,altExit);
					break;
				case Directions.SOUTHEAST:
					linkRoom(subMap[subMap.length-1][subMap[0].length-1],dirRoom,d,dirExit,altExit);
					break;
				case Directions.SOUTHWEST:
					linkRoom(subMap[0][subMap[0].length-1],dirRoom,d,dirExit,altExit);
					break;
				case Directions.EAST:
					for(int y=0;y<subMap[0].length;y++)
						linkRoom(subMap[subMap.length-1][y],dirRoom,d,dirExit,altExit);
					break;
				case Directions.WEST:
					for(int y=0;y<subMap[0].length;y++)
						linkRoom(subMap[0][y],dirRoom,d,dirExit,altExit);
					break;
				case Directions.UP:
					for (final Room[] element : subMap)
					{
						for(int y=0;y<element.length;y++)
							linkRoom(element[y],dirRoom,d,dirExit,altExit);
					}
					break;
				case Directions.DOWN:
					for (final Room[] element : subMap)
					{
						for(int y=0;y<element.length;y++)
							linkRoom(element[y],dirRoom,d,dirExit,altExit);
					}
					break;
				}
			}
		}
	}

	public void tryFillInExtraneousExternal(CrossExit EX, Exit ox)
	{
		if(EX==null)
			return;
		Room linkFrom=null;
		if(subMap!=null)
			linkFrom=subMap[EX.x][EX.y];
		if(linkFrom!=null)
		{
			Room linkTo=CMLib.map().getRoom(EX.destRoomID);
			if((linkTo!=null)&&(linkTo.getGridParent()!=null))
				linkTo=linkTo.getGridParent();
			if((linkTo!=null)&&(linkFrom.rawDoors()[EX.dir]!=linkTo))
			{
				if(ox==null)
					ox=CMClass.getExit("Open");
				linkFrom.rawDoors()[EX.dir]=linkTo;
				linkFrom.setRawExit(EX.dir,ox);
			}
		}
	}

	public void fillInTheExtraneousExternals(Room[][] subMap, Exit ox)
	{
		if(subMap!=null)
		{
			for(int d=0;d<gridexits.size();d++)
			{
				final CrossExit EX=gridexits.get(d);
				try
				{
					if(!EX.out)
						continue;
					switch(EX.dir)
					{
					case Directions.NORTH:
						if(EX.y==0)
							tryFillInExtraneousExternal(EX,ox);
						break;
					case Directions.SOUTH:
						if(EX.y==yGridSize()-1)
							tryFillInExtraneousExternal(EX,ox);
						break;
					case Directions.EAST:
						if(EX.x==xGridSize()-1)
							tryFillInExtraneousExternal(EX,ox);
						break;
					case Directions.WEST:
						if(EX.x==0)
							tryFillInExtraneousExternal(EX,ox);
						break;
					case Directions.NORTHEAST:
						if((EX.y==0)&&(EX.x==xGridSize()-1))
							tryFillInExtraneousExternal(EX,ox);
						break;
					case Directions.SOUTHWEST:
						if((EX.y==yGridSize()-1)&&(EX.x==0))
							tryFillInExtraneousExternal(EX,ox);
						break;
					case Directions.NORTHWEST:
						if((EX.y==0)&&(EX.x==0))
							tryFillInExtraneousExternal(EX,ox);
						break;
					case Directions.SOUTHEAST:
						if((EX.y==yGridSize()-1)&&(EX.x==xGridSize()-1))
							tryFillInExtraneousExternal(EX,ox);
						break;
					}
				}
				catch (final Exception e)
				{
				}
			}
		}
	}

	@Override
	public void buildGrid()
	{
		clearGrid(null);
		try
		{
			subMap=new Room[xsize][ysize];
			final Exit ox=CMClass.getExit("Open");
			for(int x=0;x<subMap.length;x++)
			{
				for(int y=0;y<subMap[x].length;y++)
				{
					final Room newRoom=getGridRoom(x,y);
					if(newRoom!=null)
					{
						if((y>0)&&(subMap[x][y-1]!=null))
							linkRoom(newRoom,subMap[x][y-1],Directions.NORTH,ox,ox);
						if((x>0)&&(subMap[x-1][y]!=null))
							linkRoom(newRoom,subMap[x-1][y],Directions.WEST,ox,ox);
						if(Directions.NORTHEAST<Directions.NUM_DIRECTIONS())
						{
							if((y>0)&&(x>0)&&(subMap[x-1][y-1]!=null))
								linkRoom(newRoom,subMap[x-1][y-1],Directions.NORTHWEST,ox,ox);
							if(((y+1)<subMap[x].length)&&(x>0)&&(subMap[x-1][y+1]!=null))
								linkRoom(newRoom,subMap[x-1][y+1],Directions.SOUTHWEST,ox,ox);
						}
					}
				}
			}
			buildFinalLinks();
			fillInTheExtraneousExternals(subMap,ox);
		}
		catch(final Exception e)
		{
			Log.errOut("StdGrid",e);
			clearGrid(null);
		}
	}

	@Override
	public boolean isMyGridChild(Room loc)
	{
		final Room[][] subMap=getBuiltGrid();
		if(subMap!=null)
		{
			for (final Room[] element : subMap)
			{
				for(int y=0;y<element.length;y++)
				{
					final Room room=element[y];
					if(room==loc)
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public void clearGrid(Room backHere)
	{
		try
		{
			if(subMap!=null)
			{
				for (final Room[] element : subMap)
				{
					for(int y=0;y<element.length;y++)
					{
						final Room room=element[y];
						if(room!=null)
						{
							while(room.numInhabitants()>0)
							{
								final MOB M=room.fetchInhabitant(0);
								if(M!=null)
								{
									if(backHere!=null)
										backHere.bringMobHere(M,false);
									else
									if((M.getStartRoom()==null)
									||(M.getStartRoom()==room)
									||(M.getStartRoom().ID().length()==0))
									{
										if(!M.isMonster())
											M.destroy();
									}
									else
										M.getStartRoom().bringMobHere(M,false);
									if(room.isInhabitant(M))
									{
										M.destroy();
										M.setLocation(null);
										if(room.isInhabitant(M))
											room.delInhabitant(M);
									}
								}
							}
							while(room.numItems()>0)
							{
								final Item I=room.getItem(0);
								if(I!=null)
								{
									if(backHere!=null)
										backHere.moveItemTo(I,ItemPossessor.Expire.Player_Drop,ItemPossessor.Move.Followers);
									else
									if((I instanceof PrivateProperty)&&(((PrivateProperty)I).getOwnerName().length()>0))
									{
										final Room R=CMLib.map().getSafeRoomToMovePropertyTo(room, (PrivateProperty)I);
										if((R!=null)&&(R!=room))
											R.moveItemTo(I);
									}
									else
										I.destroy();
									if(room.isContent(I))
									{
										if((I instanceof PrivateProperty)&&(((PrivateProperty)I).getOwnerName().length()>0))
											room.delItem(I);
										else
											I.destroy();
										I.destroy();
										if(room.isContent(I))
										{
											I.setOwner(null);
											room.delItem(I);
										}
									}
								}
							}
							room.clearSky();
							room.destroy();
							room.setGridParent(null);
						}
					}
				}
			}
			subMap=null;
		}
		catch (final Exception e)
		{
		}
	}

	@Override
	public String getGridChildCode(Room loc)
	{
		if(roomID().length()==0)
			return "";
		final Room[][] subMap=getBuiltGrid();
		if(subMap!=null)
		{
			for(int x=0;x<subMap.length;x++)
			{
				for(int y=0;y<subMap[x].length;y++)
				{
					if(subMap[x][y]==loc)
						return roomID()+"#("+x+","+y+")";
				}
			}
		}
		return "";

	}

	@Override
	public int getGridChildX(Room loc)
	{
		if(roomID().length()==0)
			return -1;
		final Room[][] subMap=getBuiltGrid();
		if(subMap!=null)
		{
			for(int x=0;x<subMap.length;x++)
			{
				for(int y=0;y<subMap[x].length;y++)
				{
					if(subMap[x][y]==loc)
						return x;
				}
			}
		}
		return -1;
	}

	@Override
	public int getGridChildY(Room loc)
	{
		if(roomID().length()==0)
			return -1;
		final Room[][] subMap=getBuiltGrid();
		if(subMap!=null)
		{
			for (final Room[] element : subMap)
			{
				for(int y=0;y<element.length;y++)
				{
					if(element[y]==loc)
						return y;
				}
			}
		}
		return -1;
	}

	@Override
	public Room getGridChild(String childCode)
	{
		if(childCode.equalsIgnoreCase(roomID()))
			return this;
		if(!childCode.toUpperCase().startsWith(roomID().toUpperCase()+"#("))
			return null;
		final int len=roomID().length()+2;
		final int comma=childCode.indexOf(',',len);
		if(comma<0)
			return null;
		final Room[][] subMap=getBuiltGrid();
		final int x=CMath.s_int(childCode.substring(len,comma));
		final int y=CMath.s_int(childCode.substring(comma+1,childCode.length()-1));
		if(subMap!=null)
		if((x<subMap.length)&&(y<subMap[x].length))
			return subMap[x][y];
		return null;
	}

	@Override
	public Room getGridChild(int x, int y)
	{
		if((subMap==null)
		||(x<0)
		||(y<0)
		||(x>=subMap.length)
		||(y>=subMap[x].length))
			return null;
		return subMap[x][y];
	}

	protected Room getGridRoom(int x, int y)
	{
		if(subMap==null)
			subMap=new Room[xsize][ysize];
		if((x<0)||(y<0)||(y>=subMap[0].length)||(x>=subMap.length))
			return null;
		final Room gc=CMClass.getLocale(getGridChildLocaleID());
		gc.setRoomID("");
		gc.setGridParent(this);
		subMap[x][y]=gc;
		gc.setArea(getArea());
		gc.setDisplayText(displayText());
		gc.setDescription(description());
		int c=-1;
		if(displayTexts!=null)
		if(displayTexts.length>0)
		{
			c=CMLib.dice().roll(1,displayTexts.length,-1);
			gc.setDisplayText(displayTexts[c]);
		}
		if(descriptions!=null)
		if(descriptions.length>0)
		{
			if((c<0)||(c>descriptions.length)||(descriptions.length!=displayTexts.length))
				c=CMLib.dice().roll(1,descriptions.length,-1);
			gc.setDescription(descriptions[c]);
		}

		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
			gc.addEffect((Ability)a.nextElement().copyOf());
		if(behaviors != null)
		{
			for(final Behavior B : behaviors)
				gc.addBehavior((Behavior)B.copyOf());
		}
		return gc;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target()==this))
		{
			final MOB mob=msg.source();
			if((mob.location()!=null)&&(mob.location().roomID().length()>0))
			{
				int direction=-1;
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					if(mob.location().getRoomInDir(d)==this)
						direction=d;
				}
				if(direction<0)
				{
					mob.tell(L("Some great evil is preventing your movement that way."));
					return false;
				}
				msg.modify(msg.source(),
						   getAltRoomFrom(mob.location(),direction),
						   msg.tool(),
						   msg.sourceCode(),
						   msg.sourceMessage(),
						   msg.targetCode(),
						   msg.targetMessage(),
						   msg.othersCode(),
						   msg.othersMessage());
			}
		}
		return true;
	}

	private final static String[] MYCODES={"XSIZE","YSIZE"};
	
	@Override
	public String getStat(String code)
	{
		switch(getStdGridCodeNum(code))
		{
		case 0:
			return Integer.toString(xGridSize());
		case 1:
			return Integer.toString(yGridSize());
		default:
			return super.getStat(code);
		}
	}

	@Override
	public void setStat(String code, String val)
	{
		switch(getStdGridCodeNum(code))
		{
		case 0:
			setXGridSize(CMath.s_parseIntExpression(val));
			break;
		case 1:
			setYGridSize(CMath.s_parseIntExpression(val));
			break;
		default:
			super.setStat(code, val);
			break;
		}
	}

	protected int getStdGridCodeNum(String code)
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
		return (codes != null) ? codes : (codes =  CMProps.getStatCodesList(CMParms.appendToArray(STDCODES, MYCODES),this));
	}
}
