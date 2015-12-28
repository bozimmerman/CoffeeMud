package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


/*
   Copyright 2015-2015 Bo Zimmerman

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

public class BuildingSkill extends CraftingSkill
{
	@Override
	public String ID()
	{
		return "BuildingSkill";
	}

	private final static String	localizedName	= CMLib.lang().L("BuildingSkill");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "BUILD", "BUILDING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "";
	}

	public BuildingSkill(){super();}

	protected Room		room				= null;
	protected int		dir					= -1;
	protected int		doingCode			= -1;
	protected int		workingOn			= -1;
	protected String	designTitle			= "";
	protected String	designDescription	= "";	
	

	public Exit generify(Exit X)
	{
		final Exit E2=CMClass.getExit("GenExit");
		E2.setName(X.name());
		E2.setDisplayText(X.displayText());
		E2.setDescription(X.description());
		E2.setDoorsNLocks(X.hasADoor(),X.isOpen(),X.defaultsClosed(),X.hasALock(),X.isLocked(),X.defaultsLocked());
		E2.setBasePhyStats((PhyStats)X.basePhyStats().copyOf());
		E2.setExitParams(X.doorName(),X.closeWord(),X.openWord(),X.closedText());
		E2.setKeyName(X.keyName());
		E2.setOpenDelayTicks(X.openDelayTicks());
		E2.setReadable(X.isReadable());
		E2.setReadableText(X.readableText());
		E2.setTemporaryDoorLink(X.temporaryDoorLink());
		E2.recoverPhyStats();
		return E2;
	}

	protected void demolishRoom(MOB mob, Room room)
	{
		final LandTitle title=CMLib.law().getLandTitle(room);
		if(title==null)
			return;
		Room returnToRoom=null;
		Room backupToRoom1=null;
		Room backupToRoom2=null;
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			final Room R=room.getRoomInDir(d);
			if(CMLib.law().doesOwnThisLand(mob, R))
			{
				returnToRoom=R;
				break;
			}
			final LandTitle adjacentTitle=CMLib.law().getLandTitle(R);
			if((adjacentTitle==null)||(adjacentTitle.getOwnerName().length()>0))
				backupToRoom1=R;
			else
			if(R.roomID().length()>0)
				backupToRoom2=R;
		}
		if(returnToRoom==null)
			returnToRoom=backupToRoom1;
		if(returnToRoom==null)
			returnToRoom=backupToRoom2;
		if(returnToRoom==null)
			returnToRoom=mob.getStartRoom();
		if(returnToRoom==null)
			returnToRoom=room.getArea().getRandomProperRoom();
		if(returnToRoom==null)
			returnToRoom=room.getArea().getRandomMetroRoom();
		if(returnToRoom==null)
			returnToRoom=CMLib.map().getRandomRoom();
		final Room theRoomToReturnTo=returnToRoom;
		room.eachInhabitant(new EachApplicable<MOB>()
		{
			@Override
			public void apply(MOB a)
			{
				theRoomToReturnTo.bringMobHere(a, false);
			}
		});
		room.eachItem(new EachApplicable<Item>()
		{
			@Override
			public void apply(Item a)
			{
				theRoomToReturnTo.addItem(a,Expire.Player_Drop);
			}
		});
		title.setOwnerName("");
		title.updateLot(null); // this is neat -- this will obliterate leaf rooms around this one.
		if((theRoomToReturnTo!=null)
		&&(theRoomToReturnTo.rawDoors()[Directions.UP]==room)
		&&(theRoomToReturnTo.getRawExit(Directions.UP)!=null))
		{
			theRoomToReturnTo.getRawExit(Directions.UP).destroy();
			theRoomToReturnTo.setRawExit(Directions.UP, null);
		}
		CMLib.map().obliterateRoom(room);
	}

	protected Room convertRoomType(Room room, String newLocale)
	{
		Room R=null;
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=CMLib.map().getRoom(room);
			R=CMClass.getLocale(newLocale);
			R.setRoomID(room.roomID());
			R.setDisplayText(room.displayText());
			R.setDescription(room.description());
			if(R.image().equalsIgnoreCase(CMLib.protocol().getDefaultMXPImage(room)))
				R.setImage(null);

			final Area area=room.getArea();
			if(area!=null)
				area.delProperRoom(room);
			R.setArea(area);
			for(int a=room.numEffects()-1;a>=0;a--)
			{
				final Ability A=room.fetchEffect(a);
				if(A!=null)
				{
					room.delEffect(A);
					R.addEffect(A);
				}
			}
			for(int i=room.numItems()-1;i>=0;i--)
			{
				final Item I=room.getItem(i);
				if(I!=null)
				{
					room.delItem(I);
					R.addItem(I);
				}
			}
			for(int m=room.numInhabitants()-1;m>=0;m--)
			{
				final MOB M=room.fetchInhabitant(m);
				if(M!=null)
				{
					room.delInhabitant(M);
					R.addInhabitant(M);
					M.setLocation(R);
				}
			}
			CMLib.threads().deleteTick(room,-1);
			for(int d=0;d<R.rawDoors().length;d++)
			{
				if((R.rawDoors()[d]==null)
				||(R.rawDoors()[d].roomID().length()>0))
					R.rawDoors()[d]=room.rawDoors()[d];
			}
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if((R.rawDoors()[d]==null)
				||(R.rawDoors()[d].roomID().length()>0))
					if(room.getRawExit(d)!=null)
						R.setRawExit(d, (Exit)room.getRawExit(d).copyOf());
			}
			R.clearSky();
			R.startItemRejuv();
			try
			{
				boolean rebuild=false;
				for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
				{
					final Room R2=r.nextElement();
					rebuild=false;
					for(int d=0;d<R2.rawDoors().length;d++)
					{
						if(R2.rawDoors()[d]==room)
						{
							rebuild=true;
							R2.rawDoors()[d]=R;
						}
					}
					if((rebuild)&&(R2 instanceof GridLocale))
						((GridLocale)R2).buildGrid();
				}
			}
			catch (final NoSuchElementException e)
			{
			}
			try
			{
				for(final Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
				{
					final MOB M=e.nextElement();
					if(M.getStartRoom()==room)
						M.setStartRoom(R);
					else
					if(M.location()==room)
						M.setLocation(R);
				}
			}
			catch (final NoSuchElementException e)
			{
			}
			if((R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
			||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
			{
				final Room R2=CMClass.getLocale("UnderWater");
				R2.setRoomID(R.getArea().getNewRoomID(R,Directions.DOWN));
				R2.setDisplayText(L("Under the water"));
				R2.setDescription(L("You are swimming around under the water."));
				R2.setArea(R.getArea());
				R2.rawDoors()[Directions.UP]=R;
				R2.setRawExit(Directions.UP,CMClass.getExit("Open"));
				R.clearSky();
				R.rawDoors()[Directions.DOWN]=R2;
				R.setRawExit(Directions.DOWN,CMClass.getExit("Open"));
				final LandTitle title=CMLib.law().getLandTitle(R);
				if((title!=null)&&(CMLib.law().getLandTitle(R2)==null))
				{
					final LandTitle A2=(LandTitle)title.newInstance();
					A2.setPrice(title.getPrice());
					R2.addNonUninvokableEffect((Ability)A2);
				}
				if(CMSecurity.isDebugging(CMSecurity.DbgFlag.PROPERTY))
					Log.debugOut("Masonry",R2.roomID()+" created for water.");
				CMLib.database().DBCreateRoom(R2);
				CMLib.database().DBUpdateExits(R2);
			}

			R.getArea().fillInAreaRoom(R);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.PROPERTY))
				Log.debugOut("Masonry",R.roomID()+" updated.");
			CMLib.database().DBUpdateRoom(R);
			CMLib.database().DBUpdateExits(R);
			room.destroy();
		}
		return R;
	}

	protected Room buildStairs(final MOB mob, Room room, int dirUpOrDown)
	{
		Room upRoom;
		synchronized(("SYNC"+room.roomID()).intern())
		{
			int opDirUpOrDown = Directions.getOpDirectionCode(dirUpOrDown);
			room=CMLib.map().getRoom(room);
			int floor=0;
			upRoom=room;
			while((upRoom!=null)&&(upRoom.ID().length()>0)&&(CMLib.law().getLandTitle(upRoom)!=null))
			{
				upRoom=upRoom.getRoomInDir(dirUpOrDown);
				floor++;
			}
			upRoom=CMClass.getLocale(CMClass.classID(room));
			upRoom.setRoomID(room.getArea().getNewRoomID(room,dirUpOrDown));
			if(upRoom.roomID().length()==0)
			{
				commonTell(mob,L("You've failed to build the stairs!"));
				return null;
			}
			upRoom.setArea(room.getArea());
			LandTitle newTitle=CMLib.law().getLandTitle(room);
			if((newTitle!=null)&&(CMLib.law().getLandTitle(upRoom)==null))
			{
				newTitle=(LandTitle)((Ability)newTitle).copyOf();
				newTitle.setLandPropertyID(upRoom.roomID());
				newTitle.setOwnerName("");
				newTitle.setBackTaxes(0);
				upRoom.addNonUninvokableEffect((Ability)newTitle);
			}
			room.rawDoors()[dirUpOrDown]=upRoom;
			final Exit upExit=CMClass.getExit("OpenDescriptable");
			if(dirUpOrDown == Directions.UP)
				upExit.setMiscText("Upstairs to the "+(floor+1)+CMath.numAppendage(floor+1)+" floor.");
			else
				upExit.setMiscText("Downstairs to the "+(floor+1)+CMath.numAppendage(floor+1)+" floor.");
			room.setRawExit(dirUpOrDown,upExit);

			final Exit downExit=CMClass.getExit("OpenDescriptable");
			if(opDirUpOrDown == Directions.DOWN)
				downExit.setMiscText("Downstairs to the "+(floor)+CMath.numAppendage(floor)+" floor.");
			else
				downExit.setMiscText("Upstairs to the "+(floor)+CMath.numAppendage(floor)+" floor.");
			upRoom.rawDoors()[opDirUpOrDown]=room;
			upRoom.setRawExit(opDirUpOrDown,downExit);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.PROPERTY))
				Log.debugOut("Lots4Sale",upRoom.roomID()+" created and put up for sale.");
			CMLib.database().DBCreateRoom(upRoom);
			if(newTitle!=null)
				newTitle.updateLot(null);
			upRoom.getArea().fillInAreaRoom(upRoom);
			CMLib.database().DBUpdateExits(upRoom);
			CMLib.database().DBUpdateExits(room);
		}
		return upRoom;
	}

	protected void buildWall(Room room, int dir)
	{
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=CMLib.map().getRoom(room);
			room.setRawExit(dir,null);
			if(room.rawDoors()[dir]!=null)
			{
				room.rawDoors()[dir].setRawExit(Directions.getOpDirectionCode(dir),null);
				CMLib.database().DBUpdateExits(room.rawDoors()[dir]);
			}
			CMLib.database().DBUpdateExits(room);
			final LandTitle title=CMLib.law().getLandTitle(room);
			if(title != null)
				title.updateLot(null);
		}
	}

	protected void buildTitle(Room room, String designTitle)
	{
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=CMLib.map().getRoom(room);
			room.setDisplayText(designTitle);
			CMLib.database().DBUpdateRoom(room);
		}
	}

	protected void buildDesc(Room room, int dir, String designDescription)
	{
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=CMLib.map().getRoom(room);
			if(dir>=0)
			{
				Exit E=room.getExitInDir(dir);
				if((!E.isGeneric())&&(room.getRawExit(dir)==E))
				{
					E=generify(E);
					room.setRawExit(dir,E);
				}
				E.setDescription(designDescription);
				CMLib.database().DBUpdateExits(room);
			}
			else
			{
				room.setDescription(designDescription);
				CMLib.database().DBUpdateRoom(room);
			}
		}
	}

	protected void buildCrawlway(Room room, int dir)
	{
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=CMLib.map().getRoom(room);
			if((dir>=0)&&(room.getExitInDir(dir)!=null))
			{
				Exit E=room.getExitInDir(dir);
				if((!E.isGeneric())&&(room.getRawExit(dir)==E))
				{
					E=generify(E);
					room.setRawExit(dir,E);
				}
				final Ability A=CMClass.getAbility("Prop_Crawlspace");
				if(A!=null)
					E.addNonUninvokableEffect(A);
				CMLib.database().DBUpdateExits(room);
			}
		}
	}

	protected void buildWindow(Room room, int dir)
	{
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=CMLib.map().getRoom(room);
			if((dir>=0)&&(room.getExitInDir(dir)!=null))
			{
				Exit E=room.getExitInDir(dir);
				if((!E.isGeneric())&&(room.getRawExit(dir)==E))
				{
					E=generify(E);
					room.setRawExit(dir,E);
				}
				final Room R2=room.getRoomInDir(dir);
				if(R2!=null)
				{
					final Ability A=CMClass.getAbility("Prop_RoomView");
					if(A!=null)
					{
						A.setMiscText(CMLib.map().getExtendedRoomID(R2));
						E.addNonUninvokableEffect(A);
					}
				}
				CMLib.database().DBUpdateExits(room);
			}
		}	
	}

	protected void demolish(final MOB mob, Room room, int dir)
	{
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=CMLib.map().getRoom(room);
			if(dir<0)
			{

				if(CMLib.law().isHomeRoomUpstairs(room))
				{
					demolishRoom(mob,room);
				}
				else
					convertToPlains(room);
			}
			else
			{
				room.setRawExit(dir,CMClass.getExit("Open"));
				if(room.rawDoors()[dir]!=null)
				{
					room.rawDoors()[dir].setRawExit(Directions.getOpDirectionCode(dir),CMClass.getExit("Open"));
					CMLib.database().DBUpdateExits(room.rawDoors()[dir]);
				}
				CMLib.database().DBUpdateExits(room);
			}
		}
	}
	
	protected Room convertToPlains(Room room)
	{
		final Room R=CMClass.getLocale("Plains");
		R.setRoomID(room.roomID());
		R.setDisplayText(room.displayText());
		R.setDescription(room.description());
		final Area area=room.getArea();
		if(area!=null)
			area.delProperRoom(room);
		R.setArea(room.getArea());
		for(int a=room.numEffects()-1;a>=0;a--)
		{
			final Ability A=room.fetchEffect(a);
			if(A!=null)
			{
				room.delEffect(A);
				R.addEffect(A);
			}
		}
		for(int i=room.numItems()-1;i>=0;i--)
		{
			final Item I=room.getItem(i);
			if(I!=null)
			{
				room.delItem(I);
				R.addItem(I);
			}
		}
		for(int m=room.numInhabitants()-1;m>=0;m--)
		{
			final MOB M=room.fetchInhabitant(m);
			if(M!=null)
			{
				room.delInhabitant(M);
				R.addInhabitant(M);
				M.setLocation(R);
			}
		}
		CMLib.threads().deleteTick(room,-1);
		for(int d=0;d<R.rawDoors().length;d++)
			R.rawDoors()[d]=room.rawDoors()[d];
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			R.setRawExit(d,room.getRawExit(d));
		R.startItemRejuv();
		try
		{
			for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
			{
				final Room R2=r.nextElement();
				for(int d=0;d<R2.rawDoors().length;d++)
				{
					if(R2.rawDoors()[d]==room)
					{
						R2.rawDoors()[d]=R;
						if(R2 instanceof GridLocale)
							((GridLocale)R2).buildGrid();
					}
				}
			}
		}
		catch (final NoSuchElementException e)
		{
		}
		R.getArea().fillInAreaRoom(R);
		CMLib.database().DBUpdateRoom(R);
		CMLib.database().DBUpdateExits(R);
		room.destroy();
		return R;
	}

	public boolean isHomePeerRoom(Room R)
	{
		return ifHomePeerLandTitle(R)!=null;
	}

	public boolean isHomePeerTitledRoom(Room R)
	{
		final LandTitle title = ifHomePeerLandTitle(R);
		if(title == null)
			return false;
		return title.getOwnerName().length()>0;
	}

	public LandTitle ifHomePeerLandTitle(Room R)
	{
		if((R!=null)
		&&(R.ID().length()>0)
		&&(CMath.bset(R.domainType(),Room.INDOORS)))
			return CMLib.law().getLandTitle(R);
		return null;
	}
}
