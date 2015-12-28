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
	
	protected String getMainResourceName()
	{
		return "Wood";
	}

	protected String getClosedLocaleType()
	{
		return "WoodRoom";
	}

	protected String getSoundName()
	{
		return "hammer.wav";
	}

	public BuildingSkill()
	{
		super();
	}

	protected enum Building
	{
		WALL,
		DOOR,
		GATE,
		FENCE,
		SECRETDOOR,
		ROOF,
		ARCH,
		DEMOLISH,
		TITLE,
		DESC,
		MONUMENT,
		WINDOW,
		CRAWLWAY,
		POOL,
		PORTCULIS,
		STAIRS
	}

	
	protected Room		room				= null;
	protected int		dir					= -1;
	protected Building	doingCode			= null;
	protected int		recipeIndex			= -1;
	protected int		workingOn			= -1;
	protected String	designTitle			= "";
	protected String	designDescription	= "";	
	
	//protected static final int	RCP_FINALNAME		= 0;
	//protected static final int	RCP_LEVEL			= 1;
	//protected static final int	RCP_TICKS			= 2;
	protected final static int	DAT_WOOD			= 3;
	protected final static int	DAT_ROOF			= 4;
	protected final static int	DAT_REQDIR			= 5;
	protected final static int	DAT_REQNONULL		= 6;
	protected final static int	DAT_BUILDCODE		= 7;
	protected final static int	DAT_BUILDERMASK		= 8;

	@Override
	public String parametersFile()
	{
		return "";
	}

	@Override
	protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost()
	{
		return CMProps.getNormalSkillGainCost(ID());
	}
	
	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!helping))
			{
				final MOB mob=(MOB)affected;
				if(!aborted)
				{
					if((messedUp)&&(room!=null))
					{
						notifyMessUp(mob);
					}
					else
					{
						this.buildComplete(mob, room, dir, workingOn, designTitle, designDescription);
					}
				}
			}
		}
		super.unInvoke();
	}

	protected int[][] getBasicMaterials(final MOB mob, int woodRequired)
	{
		final int[] pm={RawMaterial.MATERIAL_ROCK};
		final int[][] idata=fetchFoundResourceData(mob,
													woodRequired,"stone",pm,
													0,null,null,
													false,
													0,null);
		return idata;
	}
	
	public String[][] getRecipeData(final MOB mob)
	{
		List<List<String>> recipeData = addRecipes(mob,super.loadRecipes(super.parametersFile()));
		String[][] finalDat = new String[recipeData.size()][];
		for(int i=0;i<recipeData.size();i++)
			finalDat[i] = recipeData.get(i).toArray(new String[recipeData.get(i).size()]);
		return finalDat;
	}

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

	protected void notifyMessUp(final MOB mob)
	{
		switch(doingCode)
		{
		case ROOF:
			commonTell(mob,L("You've ruined the frame and roof!"));
			break;
		case WALL:
			commonTell(mob,L("You've ruined the wall!"));
			break;
		case DOOR:
			commonTell(mob,L("You've ruined the door!"));
			break;
		case ARCH:
			commonTell(mob,L("You've ruined the archway!"));
			break;
		case PORTCULIS:
			commonTell(mob,L("You've ruined the portcullis!"));
			break;
		case MONUMENT:
			commonTell(mob,L("You've ruined the druidic monument!"));
			break;
		case POOL:
			commonTell(mob,L("You've ruined the pool!"));
			break;		
		case SECRETDOOR:
			commonTell(mob,L("You've ruined the secret door!"));
			break;
		case FENCE:
			commonTell(mob,L("You've ruined the fence!"));
			break;
		case GATE:
			commonTell(mob,L("You've ruined the gate!"));
			break;
		case TITLE:
			commonTell(mob,L("You've ruined the titling!"));
			break;
		case DESC:
			commonTell(mob,L("You've ruined the describing!"));
			break;
		case WINDOW:
			commonTell(mob,L("You've ruined the window!"));
			break;
		case CRAWLWAY:
			commonTell(mob,L("You've ruined the crawlway!"));
			break;
		case STAIRS:
			commonTell(mob,L("You've ruined the stairs!"));
			break;
		case DEMOLISH:
			commonTell(mob,L("You've failed to demolish!"));
			break;
		}
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

	protected Room buildNewRoomType(Room room, String newLocale)
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
					Log.debugOut(ID(),R2.roomID()+" created for water.");
				CMLib.database().DBCreateRoom(R2);
				CMLib.database().DBUpdateExits(R2);
			}

			R.getArea().fillInAreaRoom(R);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.PROPERTY))
				Log.debugOut(ID(),R.roomID()+" updated.");
			CMLib.database().DBUpdateRoom(R);
			CMLib.database().DBUpdateExits(R);
			room.destroy();
		}
		return R;
	}
	
	protected void buildArchway(Room room, int dir)
	{
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=CMLib.map().getRoom(room);
			final Exit x=CMClass.getExit("GenExit");
			final Exit x2=CMClass.getExit("GenExit");
			x.setName(L("an archway"));
			x.setDescription(L("A majestic archway towers above you."));
			x2.setName(L("an archway"));
			x2.setDescription(L("A majestic archway towers above you."));
			room.setRawExit(dir,x);
			if(room.rawDoors()[dir]!=null)
			{
				room.rawDoors()[dir].setRawExit(Directions.getOpDirectionCode(dir),x2);
				CMLib.database().DBUpdateExits(room.rawDoors()[dir]);
			}
			CMLib.database().DBUpdateExits(room);
		}
	}
	
	protected void buildPortculis(Room room, int dir)
	{
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=CMLib.map().getRoom(room);
			final Exit x=CMClass.getExit("GenExit");
			final Exit x2=CMClass.getExit("GenExit");
			x.setName(L("a portcullis"));
			x.setDescription(L("A portcullis lies this way."));
			x.setExitParams("portcullis","lower","raise","A portcullis blocks your way.");
			x.setDoorsNLocks(true,false,true,false,false,false);
			x2.setName(L("a portcullis"));
			x2.setDescription(L("A portcullis lies this way."));
			x2.setExitParams("portcullis","lower","raise","A portcullis blocks your way.");
			x2.setDoorsNLocks(true,false,true,false,false,false);
			room.setRawExit(dir,x);
			if(room.rawDoors()[dir]!=null)
			{
				room.rawDoors()[dir].setRawExit(Directions.getOpDirectionCode(dir),x2);
				CMLib.database().DBUpdateExits(room.rawDoors()[dir]);
			}
			CMLib.database().DBUpdateExits(room);
		}
	}

	protected void buildDoor(String doorName, Room room, int dir, boolean secret)
	{
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=CMLib.map().getRoom(room);
			final Exit X=CMClass.getExit("GenExit");
			if(secret)
				X.basePhyStats().setDisposition(PhyStats.IS_HIDDEN);
			X.setName(L("a "+doorName));
			X.setDescription("");
			X.setDisplayText("");
			X.setOpenDelayTicks(9999);
			X.setExitParams(doorName,"close","open","a closed "+doorName);
			X.setDoorsNLocks(true,false,true,false,false,false);
			X.recoverPhyStats();
			X.text();
			room.setRawExit(dir,X);
			if(room.rawDoors()[dir]!=null)
			{
				final Exit X2=(Exit)X.copyOf();
				if(secret)
					X2.basePhyStats().setDisposition(PhyStats.IS_HIDDEN);
				X2.recoverPhyStats();
				X2.text();
				room.rawDoors()[dir].setRawExit(Directions.getOpDirectionCode(dir),X2);
				CMLib.database().DBUpdateExits(room.rawDoors()[dir]);
			}
			CMLib.database().DBUpdateExits(room);
		}
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
				Log.debugOut(ID(),upRoom.roomID()+" created and put up for sale.");
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
	
	protected void buildComplete(final MOB mob, Room room, int dir, int workingOn, String designTitle, String designDescription)
	{
		switch(doingCode)
		{
		case ARCH:
		{
			this.buildArchway(room, dir);
			break;
		}
		case CRAWLWAY:
		{
			this.buildCrawlway(room, workingOn);
			break;
		}
		case DEMOLISH:
		{
			this.demolish(mob, room, dir);
			break;
		}
		case DESC:
		{
			this.buildDesc(room, workingOn, designDescription);
			break;
		}
		case DOOR:
		{
			this.buildDoor("door", room, dir, false);
			break;
		}
		case FENCE:
		{
			this.buildWall(room, dir);
			break;
		}
		case GATE:
		{
			this.buildDoor("gate", room, dir, false);
			break;
		}
		case MONUMENT:
		{
			final Item I=CMClass.getItem("DruidicMonument");
			room.addItem(I);
			I.setExpirationDate(0);
			break;
		}
		case POOL:
		{
			String newLocale;
			if((room.domainType()&Room.INDOORS)==Room.INDOORS)
				newLocale = "IndoorWaterSurface";
			else
				newLocale = "WaterSurface";
			this.buildNewRoomType(room, newLocale);
			break;
		}
		case PORTCULIS:
		{
			this.buildPortculis(room, dir);
			break;
		}
		case ROOF:
		{
			this.buildNewRoomType(room, this.getClosedLocaleType());
			break;
		}
		case SECRETDOOR:
		{
			this.buildDoor("door", room, dir, true);
			break;
		}
		case STAIRS:
		{
			this.buildStairs(mob, room, Directions.UP);
			break;
		}
		case TITLE:
		{
			this.buildTitle(room, designTitle);
			break;
		}
		case WALL:
		{
			this.buildWall(room, dir);
			break;
		}
		case WINDOW:
		{
			this.buildWindow(room, workingOn);
			break;
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
	
	public String establishVerb(final MOB mob, final Building doingCode)
	{
		String verb="";
		switch(doingCode)
		{
		case ROOF:
			verb=L("building a frame and roof");
			break;
		case POOL:
			verb=L("building a pool");
			break;
		case WALL:
			verb=L("building the @x1 wall",Directions.getDirectionName(dir));
			break;
		case ARCH:
			verb=L("building the @x1 archway",Directions.getDirectionName(dir));
			break;
		case FENCE:
			verb=L("building the @x1 fence",Directions.getDirectionName(dir));
			break;
		case PORTCULIS:
			verb=L("building the @x1 portcullis",Directions.getDirectionName(dir));
			break;
		case TITLE:
			verb=L("giving this place a title");
			break;
		case DESC:
			verb=L("giving this place a description");
			break;
		case MONUMENT:
			verb=L("building a druidic monument");
			break;
		case GATE:
			verb=L("building the @x1 gate",Directions.getDirectionName(dir));
			break;
		case DOOR:
			verb=L("building the @x1 door",Directions.getDirectionName(dir));
			break;
		case SECRETDOOR:
			verb=L("building a hidden @x1 door",Directions.getDirectionName(dir));
			break;
		case WINDOW:
			verb=L("building a window @x1",Directions.getDirectionName(dir));
			break;
		case CRAWLWAY:
			verb=L("building a crawlway @x1",Directions.getDirectionName(dir));
			break;
		case STAIRS:
			verb=L("building another floor");
			break;
		case DEMOLISH:
			if(dir<0)
			{
				if((mob.location().domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
				   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
						verb=L("demolishing the pool");
				else
				if((mob.location().domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
				   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER))
				{
					commonTell(mob,null,null,L("You must demolish a pool from above."));
					return "";
				}
				else
				if(!mob.location().ID().equalsIgnoreCase(this.getClosedLocaleType()))
				{
					commonTell(mob,null,null,L("This building was not made with @x1, you can`t demolish it.",name()));
					return "";
				}
				else
				if(CMLib.law().isHomeRoomUpstairs(mob.location()))
					verb=L("demolishing the room");
				else
					verb=L("demolishing the roof");
			}
			else
				verb=L("demolishing the @x1 wall",Directions.getDirectionName(dir));
			break;
		}
		return verb;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,0);
		
		if(commands.size()==0)
		{
			commonTell(mob,L("What kind of @x1, where? Try @x2 list.",name(),CMStrings.capitalizeAndLower(this.triggerStrings()[0])));
			return false;
		}
		final String str=commands.get(0);
		final String[][] data=getRecipeData(mob);
		if(("LIST").startsWith(str.toUpperCase()))
		{
			final String mask=CMParms.combine(commands,1);
			final int colWidth=CMLib.lister().fixColWidth(20,mob.session());
			final StringBuffer buf=new StringBuffer(CMStrings.padRight(L("Item"),colWidth) + L(" @x2 required\n\r",this.getMainResourceName().toLowerCase()));
			for(int r=0;r<data.length;r++)
			{
				if(((data[r][DAT_BUILDERMASK].length()==0)
					||(CMLib.masking().maskCheck(data[r][DAT_BUILDERMASK], mob, false))
					||CMSecurity.isASysOp(mob))
				&&((mask==null)
					||(mask.length()==0)
					||mask.equalsIgnoreCase("all")
					||CMLib.english().containsString(CMStrings.padRight(data[r][RCP_FINALNAME],colWidth),mask)))
				{
					final int woodRequired=adjustWoodRequired(CMath.s_int(data[r][DAT_WOOD]),mob);
					buf.append(CMStrings.padRight(data[r][RCP_FINALNAME],colWidth)+" "+woodRequired);
					if(doingCode == Building.PORTCULIS)
						buf.append(L(" metal"));
					buf.append("\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}

		designTitle="";
		designDescription="";
		String startStr=null;
		int duration=15;
		workingOn=-1;
		doingCode=null;
		recipeIndex=-1;
		dir=-1;

		room=null;
		messedUp=false;

		final String firstWord=commands.get(0);

		helpingAbility=null;

		if(firstWord.equalsIgnoreCase("help"))
		{
			messedUp=!proficiencyCheck(mob,0,auto);
			duration=25;
			commands.remove(0);
			final MOB targetMOB=getTarget(mob,commands,givenTarget,false,true);
			if(targetMOB==null)
				return false;
			if(targetMOB==mob)
			{
				commonTell(mob,L("You can not do that."));
				return false;
			}
			helpingAbility=targetMOB.fetchEffect(ID());
			if(helpingAbility==null)
			{
				commonTell(mob,L("@x1 is not building anything.",targetMOB.Name()));
				return false;
			}
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			{
				helpingAbility=null;
				return false;
			}
			helping=true;
			verb=L("helping @x1 with @x2",targetMOB.name(),helpingAbility.name());
			startStr=L("<S-NAME> start(s) @x1",verb);
			final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),startStr+".");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,duration);
			}
			return true;
		}

		boolean canBuild=CMLib.law().doesOwnThisLand(mob,mob.location());
		for(int r=0;r<data.length;r++)
		{
			final Building buildCode = Building.valueOf(data[r][DAT_BUILDCODE]);
			if((data[r][0].toUpperCase().startsWith(firstWord.toUpperCase()))
			&&((data[r][DAT_BUILDERMASK].length()==0)
				||(CMLib.masking().maskCheck(data[r][DAT_BUILDERMASK], mob, false))
				||CMSecurity.isASysOp(mob)))
			{
				doingCode=buildCode;
				recipeIndex=r;
			}
		}
		if(doingCode == null)
		{
			commonTell(mob,L("'@x1' is not a valid @x2 project.  Try LIST.",firstWord,name()));
			return false;
		}
		if((mob.location()!=null)
		&&((mob.location() instanceof BoardableShip) || (mob.location().getArea() instanceof BoardableShip)))
		{
			commonTell(mob,L("You may not do @x1 projects here.",name()));
			return false;
		}
		final String dirName=commands.get(commands.size()-1);
		dir=Directions.getGoodDirectionCode(dirName);
		if((doingCode == Building.DEMOLISH)&&(dirName.equalsIgnoreCase("roof"))||(dirName.equalsIgnoreCase("ceiling")))
		{
			final Room upRoom=mob.location().getRoomInDir(Directions.UP);
			if(isHomePeerRoom(upRoom))
			{
				commonTell(mob,L("You need to demolish the upstairs rooms first."));
				return false;
			}
			if(mob.location().domainType() == Room.DOMAIN_INDOORS_CAVE)
			{
				commonTell(mob,L("A cave can not have its roof demolished."));
				return false;
			}
			if(!CMath.bset(mob.location().domainType(), Room.INDOORS))
			{
				commonTell(mob,L("There is no ceiling here!"));
				return false;
			}
			if(CMLib.law().isHomeRoomUpstairs(mob.location()))
			{
				commonTell(mob,L("You can't demolish upstairs ceilings.  Try demolishing the room."));
				return false;
			}
			dir=-1;
		}
		else
		if((doingCode == Building.DEMOLISH)&&(dirName.equalsIgnoreCase("room")))
		{
			final LandTitle title=CMLib.law().getLandTitle(mob.location());
			if((!CMLib.law().doesOwnThisLand(mob, mob.location()))
			&&(title!=null)
			&&(title.getOwnerName().length()>0))
			{
				commonTell(mob,L("You can't demolish property you don't own."));
				return false;
			}
			if((title==null)||(!title.allowsExpansionConstruction()))
			{
				commonTell(mob,L("You aren't permitted to demolish this room."));
				return false;
			}
			if(!CMLib.law().isHomeRoomUpstairs(mob.location()))
			{
				commonTell(mob,L("You can only demolish upstairs rooms.  You might try just demolishing the ceiling/roof?"));
				return false;
			}
			int numAdjacentProperties=0;
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				final Room adjacentRoom=mob.location().getRoomInDir(d);
				if(isHomePeerTitledRoom(adjacentRoom))
				{
					numAdjacentProperties++;
				}
			}
			if(numAdjacentProperties>1)
			{
				mob.tell(L("You can not demolish a room if there is more than one room adjacent to it.  Demolish those first."));
				return false;
			}
			dir=-1;
			canBuild=true;
		}
		else
		if(((dir<0)||(dir==Directions.UP)||(dir==Directions.DOWN))
		&&(CMath.s_int(data[recipeIndex][DAT_REQDIR])==1))
		{
			commonTell(mob,L("A valid direction in which to build must also be specified."));
			return false;
		}

		if((CMath.s_int(data[recipeIndex][DAT_REQNONULL])==1)
		&&(dir>=0)
		&&(mob.location().getExitInDir(dir)==null))
		{
			commonTell(mob,L("There is a wall that way that needs to be demolished first."));
			return false;
		}

		int woodRequired=adjustWoodRequired(CMath.s_int(data[recipeIndex][DAT_WOOD]),mob);
		if(((mob.location().domainType()&Room.INDOORS)==0)
		&&(CMath.s_int(data[recipeIndex][DAT_ROOF])==1))
		{
			commonTell(mob,L("That can only be built after a roof, which includes the frame."));
			return false;
		}
		else
		if(((mob.location().domainType()&Room.INDOORS)>0)
		&&(CMath.s_int(data[recipeIndex][DAT_ROOF])==2))
		{
			commonTell(mob,L("That can only be built outdoors!"));
			return false;
		}

		if(doingCode == Building.STAIRS)
		{
			final LandTitle title=CMLib.law().getLandTitle(mob.location());
			if((title==null)||(!title.allowsExpansionConstruction()))
			{
				commonTell(mob,L("The title here does not permit the building of stairs."));
				return false;
			}
			if(!CMath.bset(mob.location().domainType(), Room.INDOORS))
			{
				commonTell(mob,L("You need to build a ceiling (or roof) first!"));
				return false;
			}
			if((mob.location().getRoomInDir(Directions.UP)!=null)||(mob.location().rawDoors()[Directions.UP]!=null))
			{
				commonTell(mob,L("There are already stairs here."));
				return false;
			}
		}

		if(doingCode == Building.WALL)
		{
			final Room nextRoom=mob.location().getRoomInDir(dir);
			if((nextRoom!=null)&&(CMLib.law().getLandTitle(nextRoom)==null))
			{
				commonTell(mob,L("You can not build a wall blocking off the main entrance!"));
				return false;
			}
			if(mob.location().getExitInDir(dir)==null)
			{
				commonTell(mob,L("There is already a wall in that direction!"));
				return false;
			}
		}

		if(doingCode == Building.POOL)
		{
			final Room nextRoom=mob.location().getRoomInDir(Directions.DOWN);
			final Exit exitRoom=mob.location().getExitInDir(Directions.DOWN);
			if((nextRoom!=null)||(exitRoom!=null))
			{
				commonTell(mob,L("You may not build a pool here!"));
				return false;
			}
		}

		if(doingCode == Building.TITLE)
		{
			final String title=CMParms.combine(commands,1);
			if(title.length()==0)
			{
				commonTell(mob,L("A title must be specified."));
				return false;
			}
			final TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags();
			final List<Room> checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,20);
			for (final Room room2 : checkSet)
			{
				final Room R=CMLib.map().getRoom(room2);
				if(R.displayText(mob).equalsIgnoreCase(title))
				{
					commonTell(mob,L("That title has already been taken.  Choose another."));
					return false;
				}
			}
			designTitle=title;
		}
		else
		if(doingCode == Building.DESC)
		{
			if(commands.size()<3)
			{
				commonTell(mob,L("You must specify an exit direction or the word room, followed by a description for it."));
				return false;
			}
			if(Directions.getGoodDirectionCode(commands.get(1))>=0)
			{
				dir=Directions.getGoodDirectionCode(commands.get(1));
				if(mob.location().getExitInDir(dir)==null)
				{
					commonTell(mob,L("There is no exit @x1 to describe.",Directions.getInDirectionName(dir)));
					return false;
				}
				workingOn=dir;
				commands.remove(1);
			}
			else
			if(!commands.get(1).equalsIgnoreCase("room"))
			{
				commonTell(mob,L("'@x1' is neither the word room, nor an exit direction.",(commands.get(1))));
				return false;
			}
			else
				commands.remove(1);

			final String title=CMParms.combine(commands,1);
			if(title.length()==0)
			{
				commonTell(mob,L("A description must be specified."));
				return false;
			}
			designDescription=title;
		}
		else
		if((doingCode == Building.WINDOW)||(doingCode == Building.CRAWLWAY))
			workingOn=dir;

		int[][] idata=null;
		if(doingCode == Building.PORTCULIS)
		{
			final int[] pm={RawMaterial.MATERIAL_METAL,RawMaterial.MATERIAL_MITHRIL};
			idata=fetchFoundResourceData(mob,
			 							woodRequired,"metal",pm,
			 							0,null,null,
			 							false,
			 							0,null);
		}
		else
		{
			idata = this.getBasicMaterials(mob, woodRequired);
		}

		if(idata==null)
			return false;
		woodRequired=idata[0][FOUND_AMT];

		if(!canBuild)
		{
			if((dir>=0)
			&&((CMath.s_int(data[recipeIndex][DAT_REQDIR])==1)||(workingOn==dir)))
			{
				final Room R=mob.location().getRoomInDir(dir);
				if((R!=null)&&(CMLib.law().doesOwnThisLand(mob,R)))
					canBuild=true;
			}
		}
		if(!canBuild)
		{
			commonTell(mob,L("You'll need the permission of the owner to do that."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		room=mob.location();
		if(woodRequired>0)
			CMLib.materials().destroyResourcesValue(mob.location(),woodRequired,idata[0][FOUND_CODE],0,null);

		verb = establishVerb(mob, doingCode);
		
		messedUp=!proficiencyCheck(mob,0,auto);
		startStr=L("<S-NAME> start(s) @x1",verb);
		playSound=this.getSoundName();
		duration=getDuration(CMath.s_int(data[recipeIndex][RCP_TICKS]),mob,CMath.s_int(data[recipeIndex][RCP_LEVEL]),10);

		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),startStr+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
