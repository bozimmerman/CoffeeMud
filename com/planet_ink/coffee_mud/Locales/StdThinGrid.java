package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.smtp.SMTPserver;
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
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class StdThinGrid extends StdRoom implements GridLocale
{
	@Override
	public String ID()
	{
		return "StdThinGrid";
	}

	protected String[] 			descriptions=new String[0];
	protected String[] 			displayTexts=new String[0];
	protected SVector<CrossExit>
								gridexits=new SVector<CrossExit>(1);
	protected int 				xsize=5;
	protected int 				ysize=5;
	protected int 				yLength=1;
	protected Exit 				ox=null;
	protected final SVector<ThinGridEntry>
								rooms=new SVector<ThinGridEntry>();
	protected static boolean 	tickStarted=false;

	public StdThinGrid()
	{
		super();
		myID=getClass().getName().substring(getClass().getName().lastIndexOf('.')+1);
	}

	@Override
	protected void cloneFix(Room E)
	{
		super.cloneFix(E);
		if(E instanceof StdThinGrid)
		{
			descriptions=((StdThinGrid)E).descriptions.clone();
			displayTexts=((StdThinGrid)E).displayTexts.clone();
			gridexits=((StdThinGrid)E).gridexits.copyOf();
		}
	}
	
	protected static class ThinGridEntryConverter implements Converter<ThinGridEntry, Room> 
	{
		public static ThinGridEntryConverter INSTANCE = new ThinGridEntryConverter();

		@Override
		public Room convert(ThinGridEntry obj) 
		{
			return obj.room;
		}
	}
	
	protected static class ThinGridEntry 
	{
		public Room room;
		public XYVector xy;

		public ThinGridEntry(Room R, int x, int y) 
		{
			room = R;
			xy = new XYVector(x, y);
		}
	}

	@Override 
	public String getGridChildLocaleID()
	{
		return "StdRoom";
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
	public void setXGridSize(final int x)
	{ 
		if(x>0)
			xsize=x; 
	}
	
	@Override 
	public void setYGridSize(final int y)
	{ 
		if(y>0)
			ysize=y; 
		yLength=Integer.toString(ysize).length();
	}

	@Override
	public void destroy()
	{
		super.destroy();
		Room R=null;
		for(int i=rooms.size()-1;i>=0;i--)
		{
			R=rooms.elementAt(i).room;
			if(R!=null)
				R.destroy();
		}
		rooms.clear();
		descriptions=new String[0];
		displayTexts=new String[0];
		gridexits=new SVector(1);
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
		this.descriptions = descriptions.toArray(new String[0]);
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
		this.displayTexts = displayTexts.toArray(new String[0]);
	}

	@Override
	public Room prepareRoomInDir(final Room fromRoom, final int direction)
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

	private int properRoomIndex(final int x, final int y)
	{
		if(rooms.size()==0)
			return 0;
		synchronized(rooms)
		{
			int start=0;
			int end=rooms.size()-1;
			int comp=0;
			final long total=((long)x<<31)+y;
			long comptotal=0;
			int mid=0;
			while(start<=end)
			{
				mid=(end+start)/2;
				comptotal=((long)rooms.elementAt(mid).xy.x<<31)+rooms.elementAt(mid).xy.y;
				comp=comptotal>total?1:(comptotal==total)?0:-1;
				if(comp==0)
					return mid;
				else
				if(comp>0)
					end=mid-1;
				else
					start=mid+1;
			}
			if(end<0)
				return 0;
			if(start>=rooms.size())
				return rooms.size()-1;
			return mid;
		}
	}

	private void addSortedRoom(final Room R, final int x, final int y)
	{
		if((x<0)||(y<0)||(y>=yGridSize())||(x>=xGridSize())||(R==null))
			return ;
		synchronized(rooms)
		{
			final long total=((long)x<<31)+y;
			final int pos=properRoomIndex(x,y);
			final ThinGridEntry entry = new ThinGridEntry(R,x,y);
			if(pos>=rooms.size())
			{
				rooms.addElement(entry);
				return;
			}
			final long comptotal=((long)rooms.elementAt(pos).xy.x<<31)+rooms.elementAt(pos).xy.y;
			final int comp=comptotal>total?1:(comptotal==total)?0:-1;
			if(comp==0)
				return;
			if(comp>0)
				rooms.add(pos,entry);
			else
			if(pos==rooms.size()-1)
				rooms.add(entry);
			else
				rooms.add(pos+1,entry);
		}
	}

	private Room getSortedRoom(final int x, final int y)
	{
		if(rooms.size()==0)
			return null;
		synchronized(rooms)
		{
			final int pos=properRoomIndex(x,y);
			if((rooms.elementAt(pos).xy.x==x)&&(rooms.elementAt(pos).xy.y==y))
				return rooms.elementAt(pos).room;
		}
		return null;
	}

	protected Room getGridRoomIfExists(final int x, final int y)
	{
		final Room R=getSortedRoom(x,y);
		if(R!=null)
			R.setExpirationDate(System.currentTimeMillis()+WorldMap.ROOM_EXPIRATION_MILLIS);
		return R;
	}

	@Override
	public Room prepareGridLocale(final Room fromRoom, final Room toRoom, final int direction)
	{
		final XYVector xy=getRoomXY(fromRoom);
		if((xy!=null)&&(xy.x>=0)&&(xy.x<xGridSize())&&(xy.y>=0)&&(xy.y<yGridSize()))
			fillExitsOfGridRoom(fromRoom,xy.x,xy.y);
		return fromRoom.rawDoors()[direction];
	}

	@Override 
	public Room getGridChild(final int x, final int y)
	{ 
		return getMakeGridRoom(x,y);
	}
	
	@Override 
	public Room getGridChild(final XYVector xy) 
	{ 
		return getGridChild(xy.x,xy.y); 
	}

	protected Room getMakeSingleGridRoom(final int x, final int y)
	{
		if((x<0)||(y<0)||(y>=yGridSize())||(x>=xGridSize()))
			return null;

		Room R=getGridRoomIfExists(x,y);
		if(R==null)
		{
			synchronized(rooms)
			{
				R=getGridRoomIfExists(x,y);
				if(R!=null)
					return R;
				R=CMClass.getLocale(getGridChildLocaleID());
				if(R==null)
					return null;
				R.setGridParent(this);
				R.setRoomID("");
				R.setDisplayText(displayText());
				R.setDescription(description());
				int c=-1;
				if(displayTexts!=null)
				if(displayTexts.length>0)
				{
					c=CMLib.dice().roll(1,displayTexts.length,-1);
					R.setDisplayText(displayTexts[c]);
				}
				if(descriptions!=null)
				if(descriptions.length>0)
				{
					if((c<0)||(c>descriptions.length)||(descriptions.length!=displayTexts.length))
						c=CMLib.dice().roll(1,descriptions.length,-1);
					R.setDescription(descriptions[c]);
				}

				for(int a=0;a<numEffects();a++)
					R.addEffect((Ability)fetchEffect(a).copyOf());
				if(behaviors != null)
				{
					for(final Behavior B : behaviors)
						R.addBehavior((Behavior)B.copyOf());
				}
				R.setExpirationDate(System.currentTimeMillis()+WorldMap.ROOM_EXPIRATION_MILLIS);
				addSortedRoom(R,x,y);
				R.setArea(getArea());
				//R.giveASky(0);
			}
		}
		return R;
	}

	@Override
	public XYVector getRoomXY(final Room room)
	{
		if((room.getGridParent()!=this)
		&&(room.getGridParent()!=null))
			return null;
		try
		{
			for(final ThinGridEntry entry : rooms)
			{
				if(entry.room==room)
					return entry.xy;
			}
		}
		catch(final Exception x)
		{
		}
		return null;
	}

	@Override
	public XYVector getRoomXY(final String roomID)
	{
		final Room room=CMLib.map().getRoom(roomID);
		if(room==null)
			return null;
		return getRoomXY(room);
	}

	protected void fillExitsOfGridRoom(final Room R, final int x, final int y)
	{
		if((x<0)||(y<0)||(y>=yGridSize())||(x>=xGridSize()))
			return;

		synchronized(R.basePhyStats())
		{
			final int mask=R.basePhyStats().sensesMask();
			if(CMath.bset(mask,PhyStats.SENSE_ROOMGRIDSYNC))
				return;
			R.basePhyStats().setSensesMask(mask|PhyStats.SENSE_ROOMGRIDSYNC);
		}

		// the adjacent rooms created by this method should also take
		// into account the possibility that they are on the edge.
		// it does NOT
		if(ox==null)
			ox=CMClass.getExit("Open");
		Room R2=null;
		if(y>0)
		{
			R2=getMakeSingleGridRoom(x,y-1);
			if(R2!=null)
				linkRoom(R,R2,Directions.NORTH,ox,ox);
		}
		else
		if((rawDoors()[Directions.NORTH]!=null)&&(exits[Directions.NORTH]!=null))
			linkRoom(R,rawDoors()[Directions.NORTH],Directions.NORTH,exits[Directions.NORTH],exits[Directions.NORTH]);

		if(x>0)
		{
			R2=getMakeSingleGridRoom(x-1,y);
			if(R2!=null)
				linkRoom(R,R2,Directions.WEST,ox,ox);
		}
		else
		if((rawDoors()[Directions.WEST]!=null)&&(exits[Directions.WEST]!=null))
			linkRoom(R,rawDoors()[Directions.WEST],Directions.WEST,exits[Directions.WEST],exits[Directions.WEST]);
		if(y<(yGridSize()-1))
		{
			R2=getMakeSingleGridRoom(x,y+1);
			if(R2!=null)
				linkRoom(R,R2,Directions.SOUTH,ox,ox);
		}
		else
		if((rawDoors()[Directions.SOUTH]!=null)&&(exits[Directions.SOUTH]!=null))
			linkRoom(R,rawDoors()[Directions.SOUTH],Directions.SOUTH,exits[Directions.SOUTH],exits[Directions.SOUTH]);

		if(x<(xGridSize()-1))
		{
			R2=getMakeSingleGridRoom(x+1,y);
			if(R2!=null)
				linkRoom(R,R2,Directions.EAST,ox,ox);
		}
		else
		if((rawDoors()[Directions.EAST]!=null)&&(exits[Directions.EAST]!=null))
			linkRoom(R,rawDoors()[Directions.EAST],Directions.EAST,exits[Directions.EAST],exits[Directions.EAST]);

		if(Directions.NORTHEAST<Directions.NUM_DIRECTIONS())
		{
			if((y>0)&&(x>0))
			{
				R2=getMakeSingleGridRoom(x-1,y-1);
				if(R2!=null)
					linkRoom(R,R2,Directions.NORTHWEST,ox,ox);
			}
			else
			if((rawDoors()[Directions.NORTHWEST]!=null)&&(exits[Directions.NORTHWEST]!=null))
				linkRoom(R,rawDoors()[Directions.NORTHWEST],Directions.NORTHWEST,exits[Directions.NORTHWEST],exits[Directions.NORTHWEST]);

			if((x>0)&&(y<(yGridSize()-1)))
			{
				R2=getMakeSingleGridRoom(x-1,y+1);
				if(R2!=null)
					linkRoom(R,R2,Directions.SOUTHWEST,ox,ox);
			}
			else
			if((rawDoors()[Directions.SOUTHWEST]!=null)&&(exits[Directions.SOUTHWEST]!=null))
				linkRoom(R,rawDoors()[Directions.SOUTHWEST],Directions.SOUTHWEST,exits[Directions.SOUTHWEST],exits[Directions.SOUTHWEST]);

			if((x<(xGridSize()-1))&&(y>0))
			{
				R2=getMakeSingleGridRoom(x+1,y-1);
				if(R2!=null)
					linkRoom(R,R2,Directions.NORTHEAST,ox,ox);
			}
			else
			if((rawDoors()[Directions.NORTHEAST]!=null)&&(exits[Directions.NORTHEAST]!=null))
				linkRoom(R,rawDoors()[Directions.NORTHEAST],Directions.NORTHEAST,exits[Directions.NORTHEAST],exits[Directions.NORTHEAST]);
			if((x<(xGridSize()-1))&&(y<(yGridSize()-1)))
			{
				R2=getMakeSingleGridRoom(x+1,y+1);
				if(R2!=null)
					linkRoom(R,R2,Directions.SOUTHEAST,ox,ox);
			}
			else
			if((rawDoors()[Directions.SOUTHEAST]!=null)&&(exits[Directions.SOUTHEAST]!=null))
				linkRoom(R,rawDoors()[Directions.SOUTHEAST],Directions.SOUTHEAST,exits[Directions.SOUTHEAST],exits[Directions.SOUTHEAST]);
		}

		for(int d=0;d<gridexits.size();d++)
		{
			final CrossExit EX=gridexits.elementAt(d);
			try
			{
				if((EX.out)&&(EX.x==x)&&(EX.y==y))
				{
					switch(EX.dir)
					{
					case Directions.NORTH:
						if(EX.y==0)
							tryFillInExtraneousExternal(EX,ox,R);
						break;
					case Directions.SOUTH:
						if(EX.y==yGridSize()-1)
							tryFillInExtraneousExternal(EX,ox,R);
						break;
					case Directions.EAST:
						if(EX.x==xGridSize()-1)
							tryFillInExtraneousExternal(EX,ox,R);
						break;
					case Directions.WEST:
						if(EX.x==0)
							tryFillInExtraneousExternal(EX,ox,R);
						break;
					case Directions.NORTHEAST:
						if((EX.y==0)&&(EX.x==xGridSize()-1))
							tryFillInExtraneousExternal(EX,ox,R);
						break;
					case Directions.SOUTHWEST:
						if((EX.y==yGridSize()-1)&&(EX.x==0))
							tryFillInExtraneousExternal(EX,ox,R);
						break;
					case Directions.NORTHWEST:
						if((EX.y==0)&&(EX.x==0))
							tryFillInExtraneousExternal(EX,ox,R);
						break;
					case Directions.SOUTHEAST:
						if((EX.y==yGridSize()-1)&&(EX.x==xGridSize()-1))
							tryFillInExtraneousExternal(EX,ox,R);
						break;
					}
				}
			}
			catch(final Exception e)
			{
			}
		}
		synchronized(R.basePhyStats())
		{
			R.basePhyStats().setSensesMask(CMath.unsetb(R.basePhyStats().sensesMask(),PhyStats.SENSE_ROOMGRIDSYNC));
		}
	}

	public void tryFillInExtraneousExternal(final CrossExit EX, Exit ox, final Room linkFrom)
	{
		if(EX==null)
			return;
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

	protected Room getMakeGridRoom(final int x, final int y)
	{
		if((x<0)||(y<0)||(y>=yGridSize())||(x>=xGridSize()))
			return null;

		final Room R=getMakeSingleGridRoom(x,y);
		if(R==null)
			return null;
		fillExitsOfGridRoom(R,x,y);
		return R;
	}

	@Override 
	public Iterator<CrossExit> outerExits()
	{
		return gridexits.iterator();
	}
	
	@Override 
	public void delOuterExit(final CrossExit x)
	{
		gridexits.remove(x);
	}
	
	@Override 
	public void addOuterExit(final CrossExit x)
	{
		gridexits.addElement(x);
	}

	public Room getAltRoomFrom(Room loc, final int direction)
	{
		if((loc==null)||(direction<0))
			return null;
		final int opDirection=Directions.getOpDirectionCode(direction);

		final String roomID=CMLib.map().getExtendedRoomID(loc);
		for(int d=0;d<gridexits.size();d++)
		{
			final CrossExit EX=gridexits.elementAt(d);
			if((!EX.out)
			&&(EX.destRoomID.equalsIgnoreCase(roomID))
			&&(EX.dir==direction)
			&&(EX.x>=0)&&(EX.y>=0)&&(EX.x<xGridSize())&&(EX.y<yGridSize()))
				return getMakeGridRoom(EX.x,EX.y);
		}

		final Room oldLoc=loc;
		if(loc.getGridParent()!=null)
			loc=loc.getGridParent();
		if((oldLoc!=loc)
		&&(loc instanceof GridLocale))
		{
			final XYVector xy=((GridLocale)loc).getRoomXY(oldLoc);
			if(xy != null)
			{
				if((xy.x>=0)&&(xy.y>=0))
				{
					switch(opDirection)
					{
					case Directions.EAST:
						if((((GridLocale)loc).yGridSize()==yGridSize()))
							return getMakeGridRoom(xGridSize()-1,xy.y);
						break;
					case Directions.WEST:
						if((((GridLocale)loc).yGridSize()==yGridSize()))
							return getMakeGridRoom(0,xy.y);
						break;
					case Directions.NORTH:
						if((((GridLocale)loc).xGridSize()==xGridSize()))
							return getMakeGridRoom(xy.x,0);
						break;
					case Directions.NORTHWEST:
						return getMakeGridRoom(0,0);
					case Directions.SOUTHEAST:
						return getMakeGridRoom(xGridSize()-1,yGridSize()-1);
					case Directions.NORTHEAST:
						return getMakeGridRoom(xGridSize()-1,0);
					case Directions.SOUTHWEST:
						return getMakeGridRoom(0,yGridSize()-1);
					case Directions.SOUTH:
						if((((GridLocale)loc).xGridSize()==xGridSize()))
							return getMakeGridRoom(xy.x,yGridSize()-1);
						break;
					}
				}
			}
		}
		int x=0;
		int y=0;
		switch(opDirection)
		{
		case Directions.NORTH:
			x=xGridSize()/2;
			break;
		case Directions.SOUTH:
			x=xGridSize()/2;
			y=yGridSize()-1;
			break;
		case Directions.EAST:
			x=xGridSize()-1;
			y=yGridSize()/2;
			break;
		case Directions.WEST:
			y=yGridSize()/2;
			break;
		case Directions.NORTHWEST:
			x=0;
			y=0;
			break;
		case Directions.NORTHEAST:
			x=xGridSize()-1;
			y=0;
			break;
		case Directions.SOUTHWEST:
			x=0;
			y=yGridSize()-1;
			break;
		case Directions.SOUTHEAST:
			x=xGridSize()-1;
			y=yGridSize()-1;
			break;
		case Directions.UP:
		case Directions.DOWN:
			x=xGridSize()/2;
			y=yGridSize()/2;
			break;
		}
		return getMakeGridRoom(x,y);
	}

	@Override
	public List<Room> getAllRooms()
	{
		getRandomGridChild();
		return new ConvertingList<ThinGridEntry,Room>(rooms,ThinGridEntryConverter.INSTANCE);
	}

	@Override
	public List<Room> getAllRoomsFilled()
	{
		getRandomGridChild();
		final Iterator<Room> r=new ConvertingList<ThinGridEntry,Room>(rooms,ThinGridEntryConverter.INSTANCE).iterator();
		final Vector<Room> V=new Vector<Room>();
		Room R=null;
		for(;r.hasNext();)
		{
			R=r.next();
			try
			{
				if(!V.contains(R))
					V.addElement(R);
				for(Room R2 : R.getSky())
				{
					if(R2 instanceof GridLocale)
						V.addAll(((GridLocale)R2).getAllRoomsFilled());
					else
					if(!V.contains(R2))
						V.add(R2);
				}
			}
			catch(Exception e)
			{
			}
		}
		return V;
	}

	@Override
	public Iterator<Room> getExistingRooms()
	{
		return new ConvertingIterator<ThinGridEntry,Room>(rooms.iterator(),ThinGridEntryConverter.INSTANCE);
	}
	
	protected Room alternativeLink(final Room room, final Room defaultRoom, final int dir)
	{
		if(room.getGridParent()==this)
		for(int d=0;d<gridexits.size();d++)
		{
			final CrossExit EX=gridexits.elementAt(d);
			try
			{
				if((EX.out)&&(EX.dir==dir)
				&&(getGridRoomIfExists(EX.x,EX.y)==room))
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

	protected void halfLink(final Room room, final Room loc, final int dirCode, Exit o)
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

	protected void linkRoom(final Room room, final Room loc, final int dirCode, Exit o, Exit ao)
	{
		if(loc==null)
			return;
		if(room==null)
			return;
		final int opCode=Directions.getOpDirectionCode(dirCode);
		final Room dirR=room.rawDoors()[dirCode];
		if(dirR!=null)
		{
			if(dirR.getGridParent()==null)
				return;
			if(dirR.getGridParent().isMyGridChild(dirR))
				return;
			room.rawDoors()[dirCode]=null;
		}
		if(o==null)
			o=CMClass.getExit("Open");
		room.rawDoors()[dirCode]=alternativeLink(room,loc,dirCode);
		room.setRawExit(dirCode,o);
		final Room opR=loc.rawDoors()[opCode];
		if(opR!=null)
		{
			if(opR.getGridParent()==null)
				return;
			if(opR.getGridParent().isMyGridChild(opR))
				return;
			loc.rawDoors()[opCode]=null;
		}
		loc.rawDoors()[opCode]=alternativeLink(loc,room,opCode);
		if(ao==null)
		{
			if((room.getGridParent()==loc.getGridParent())
			&&(loc.rawDoors()[opCode]==room))
				ao=o;
			else
				ao=CMClass.getExit("Open");
		}
		loc.setRawExit(opCode,ao);
	}

	@Override
	public void buildGrid()
	{
		clearGrid(null);
	}

	@Override
	public boolean isMyGridChild(final Room loc)
	{
		if(loc==null)
			return false;
		if(loc.getGridParent()==this)
			return true;
		if(loc.getGridParent()!=null)
			return false;
		try
		{
			for(final ThinGridEntry entry : rooms)
			{
				if(loc == entry.room)
					return true;
			}
		}
		catch(final Exception e){} // optimization
		return false;
	}

	@Override
	public void clearGrid(final Room bringBackHere)
	{
		try
		{
			final WorldMap mapper=CMLib.map();
			for(final ThinGridEntry entry : rooms)
				mapper.emptyRoom(entry.room,bringBackHere,true);
			for(final ThinGridEntry entry : rooms)
				entry.room.destroy();
			rooms.clear();
		}
		catch(final Exception e){Log.debugOut("StdThinGrid",e);}
	}

	@Override
	public String getGridChildCode(final Room loc)
	{
		if(roomID().length()==0)
			return "";
		try
		{
			for(final ThinGridEntry entry : rooms)
			{
				if(entry.room==loc)
					return roomID()+"#("+entry.xy.x+","+entry.xy.y+")";
			}
		}
		catch(final Exception x)
		{
		}
		return "";
	}

	@Override
	public int getGridChildX(final Room loc)
	{
		try
		{
			for(final ThinGridEntry entry : rooms)
				if(entry.room==loc)
					return entry.xy.x;
		}
		catch(final Exception x)
		{
		}
		return -1;
	}

	@Override
	public Room getRandomGridChild()
	{
		final int x=CMLib.dice().roll(1,xGridSize(),-1);
		final int y=CMLib.dice().roll(1,yGridSize(),-1);
		final Room R=getMakeGridRoom(x,y);
		if(R==null)
			Log.errOut("StdThinGrid",roomID()+" failed to get a random child!");
		return R;
	}

	@Override
	public int getGridChildY(final Room loc)
	{
		try
		{
			for(final ThinGridEntry entry : rooms)
			{
				if(entry.room==loc)
					return entry.xy.y;
			}
		}
		catch(final Exception x)
		{
		}
		return -1;
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
		return getMakeGridRoom(x,y);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_EXPIRE)
		&&(msg.target() instanceof Room))
		{
			final Room R=(Room)msg.target();
			if(R.getGridParent()==this)
			{
				if((roomID().length()>0)&&(getArea()!=null))
					getArea().delProperRoomnumber(getGridChildCode(R));
				try
				{
					for(final ThinGridEntry entry : rooms)
					{
						if(entry.room==R)
							rooms.remove(entry);
					}
					for(final ThinGridEntry entry : rooms)
					{
						for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
						{
							if(entry.room.rawDoors()[d]==R)
							{
								entry.room.rawDoors()[d]=null;
								entry.room.setRawExit(d,null);
							}
						}
					}
				}
				catch(final Exception x)
				{
				}
			}
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(msg.targetMinor()==CMMsg.TYP_ENTER)
		{
			if(msg.target()==this)
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
