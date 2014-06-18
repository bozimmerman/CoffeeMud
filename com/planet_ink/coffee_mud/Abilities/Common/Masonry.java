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
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


/*
   Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings("rawtypes")
public class Masonry extends CraftingSkill
{
	@Override public String ID() { return "Masonry"; }
	private final static String localizedName = CMLib.lang()._("Masonry");
	@Override public String name() { return localizedName; }
	private static final String[] triggerStrings =_i(new String[] {"MASONRY"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public String supportedResourceString(){return "ROCK|STONE";}

	protected static final int BUILD_WALL=0;
	protected static final int BUILD_ROOF=1;
	protected static final int BUILD_ARCH=2;
	protected static final int BUILD_DEMOLISH=3;
	protected static final int BUILD_TITLE=4;
	protected static final int BUILD_DESC=5;
	protected static final int BUILD_MONUMENT=6;
	protected static final int BUILD_WINDOW=7;
	protected static final int BUILD_CRAWLWAY=8;
	protected static final int BUILD_POOL=9;
	protected static final int BUILD_PORTCULIS=10;
	protected static final int BUILD_STAIRS=11;

	private final static int DAT_NAME=0;
	private final static int DAT_WOOD=1;
	private final static int DAT_ROOF=2;
	private final static int DAT_REQDIR=3;
	private final static int DAT_REQNONULL=4;

	// name, wood, ok=0/roof=1/out=2, req direction=1, ok=0, ok=0, nonull=1, nullonly=2
	private final static String[][] data={
		{"Wall","250","1","1","0"},
		{"Roof","500","2","0","0"},
		{"Archway","200","0","1","0"},
		{"Demolish","0","0","1","0"},
		{"Title","0","0","0","0"},
		{"Description","0","0","0","0"},
		{"Druidic Monument","1000","2","0","0"},
		{"Window","100","1","1","1"},
		{"Crawlway","500","1","1","1"},
		{"Pool","700","2","0","0"},
		{"Portcullis","100","0","1","0"},
		{"Stairs","2550","1","0","0"}
	};

	protected Room room=null;
	protected int dir=-1;
	protected int doingCode=-1;
	protected int workingOn=-1;
	protected String designTitle="";
	protected String designDescription="";

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
		if(title==null) return;
		Room returnToRoom=null;
		Room backupToRoom1=null;
		Room backupToRoom2=null;
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			final Room R=room.getRoomInDir(d);
			if(CMLib.law().doesOwnThisProperty(mob, R))
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
		if(returnToRoom==null) returnToRoom=backupToRoom1;
		if(returnToRoom==null) returnToRoom=backupToRoom2;
		if(returnToRoom==null) returnToRoom=mob.getStartRoom();
		if(returnToRoom==null) returnToRoom=room.getArea().getRandomProperRoom();
		if(returnToRoom==null) returnToRoom=room.getArea().getRandomMetroRoom();
		if(returnToRoom==null) returnToRoom=CMLib.map().getRandomRoom();
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

	protected Room convertToPlains(Room room)
	{
		final Room R=CMClass.getLocale("Plains");
		R.setRoomID(room.roomID());
		R.setDisplayText(room.displayText());
		R.setDescription(room.description());
		final Area area=room.getArea();
		if(area!=null) area.delProperRoom(room);
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
			R.setRawExit(d,room);
		R.startItemRejuv();
		try
		{
			for(final Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				final Room R2=(Room)r.nextElement();
				for(int d=0;d<R2.rawDoors().length;d++)
					if(R2.rawDoors()[d]==room)
					{
						R2.rawDoors()[d]=R;
						if(R2 instanceof GridLocale)
							((GridLocale)R2).buildGrid();
					}
			}
		}catch(final NoSuchElementException e){}
		R.getArea().fillInAreaRoom(R);
		CMLib.database().DBUpdateRoom(R);
		CMLib.database().DBUpdateExits(R);
		room.destroy();
		return R;
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
					switch(doingCode)
					{
					case BUILD_ROOF:
						commonTell(mob,_("You've ruined the frame and roof!"));
						break;
					case BUILD_WALL:
						commonTell(mob,_("You've ruined the wall!"));
						break;
					case BUILD_ARCH:
						commonTell(mob,_("You've ruined the archway!"));
						break;
					case BUILD_PORTCULIS:
						commonTell(mob,_("You've ruined the portcullis!"));
						break;
					case BUILD_TITLE:
						commonTell(mob,_("You've ruined the titling!"));
						break;
					case BUILD_DESC:
						commonTell(mob,_("You've ruined the describing!"));
						break;
					case BUILD_MONUMENT:
						commonTell(mob,_("You've ruined the druidic monument!"));
						break;
					case BUILD_WINDOW:
						commonTell(mob,_("You've ruined the window!"));
						break;
					case BUILD_POOL:
						commonTell(mob,_("You've ruined the pool!"));
						break;
					case BUILD_CRAWLWAY:
						commonTell(mob,_("You've ruined the crawlway!"));
						break;
					case BUILD_STAIRS:
						commonTell(mob,_("You've ruined the stairs!"));
						break;
					case BUILD_DEMOLISH:
					default:
						commonTell(mob,_("You've failed to demolish!"));
						break;
					}
					else
					switch(doingCode)
					{
					case BUILD_ROOF:
					case BUILD_POOL:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							Room R=null;
							if(doingCode==BUILD_POOL)
							{
								if((room.domainType()&Room.INDOORS)==Room.INDOORS)
									R=CMClass.getLocale("IndoorWaterSurface");
								else
									R=CMClass.getLocale("WaterSurface");
							}
							else
								R=CMClass.getLocale("StoneRoom");
							R.setRoomID(room.roomID());
							R.setDisplayText(room.displayText());
							R.setDescription(room.description());
							if(R.image().equalsIgnoreCase(CMLib.protocol().getDefaultMXPImage(room))) R.setImage(null);

							final Area area=room.getArea();
							if(area!=null) area.delProperRoom(room);
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
								for(final Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
								{
									final Room R2=(Room)r.nextElement();
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
							}catch(final NoSuchElementException e){}
							try
							{
								for(final Enumeration e=CMLib.players().players();e.hasMoreElements();)
								{
									final MOB M=(MOB)e.nextElement();
									if(M.getStartRoom()==room)
										M.setStartRoom(R);
									else
									if(M.location()==room)
										M.setLocation(R);
								}
							}catch(final NoSuchElementException e){}
							if(doingCode==BUILD_POOL)
							{
								final Room R2=CMClass.getLocale("UnderWater");
								R2.setRoomID(R.getArea().getNewRoomID(R,Directions.DOWN));
								R2.setDisplayText(_("Under the water"));
								R2.setDescription(_("You are swimming around under the water."));
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
						break;
					}
					case BUILD_STAIRS:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							int floor=0;
							Room upRoom=room;
							while((upRoom!=null)&&(upRoom.ID().length()>0)&&(CMLib.law().getLandTitle(upRoom)!=null))
							{
								upRoom=upRoom.getRoomInDir(Directions.UP);
								floor++;
							}
							upRoom=CMClass.getLocale(CMClass.classID(room));
							upRoom.setRoomID(room.getArea().getNewRoomID(room,Directions.UP));
							if(upRoom.roomID().length()==0)
							{
								commonTell(mob,_("You've failed to build the stairs!"));
								break;
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
							room.rawDoors()[Directions.UP]=upRoom;
							final Exit upExit=CMClass.getExit("OpenDescriptable");
							upExit.setMiscText("Upstairs to the "+(floor+1)+CMath.numAppendage(floor+1)+" floor.");
							room.setRawExit(Directions.UP,upExit);

							final Exit downExit=CMClass.getExit("OpenDescriptable");
							downExit.setMiscText("Downstairs to the "+(floor)+CMath.numAppendage(floor)+" floor.");
							upRoom.rawDoors()[Directions.DOWN]=room;
							upRoom.setRawExit(Directions.DOWN,downExit);
							if(CMSecurity.isDebugging(CMSecurity.DbgFlag.PROPERTY))
								Log.debugOut("Lots4Sale",upRoom.roomID()+" created and put up for sale.");
							CMLib.database().DBCreateRoom(upRoom);
							if(newTitle!=null)
								newTitle.updateLot(null);
							upRoom.getArea().fillInAreaRoom(upRoom);
							CMLib.database().DBUpdateExits(upRoom);
							CMLib.database().DBUpdateExits(room);
						}
						break;
					}
					case BUILD_PORTCULIS:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							final Exit x=CMClass.getExit("GenExit");
							final Exit x2=CMClass.getExit("GenExit");
							x.setName(_("a portcullis"));
							x.setDescription(_("A portcullis lies this way."));
							x.setExitParams("portcullis","lower","raise","A portcullis blocks your way.");
							x.setDoorsNLocks(true,false,true,false,false,false);
							x2.setName(_("a portcullis"));
							x2.setDescription(_("A portcullis lies this way."));
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
						break;
					}
					case BUILD_ARCH:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							final Exit x=CMClass.getExit("GenExit");
							final Exit x2=CMClass.getExit("GenExit");
							x.setName(_("an archway"));
							x.setDescription(_("A majestic archway towers above you."));
							x2.setName(_("an archway"));
							x2.setDescription(_("A majestic archway towers above you."));
							room.setRawExit(dir,x);
							if(room.rawDoors()[dir]!=null)
							{
								room.rawDoors()[dir].setRawExit(Directions.getOpDirectionCode(dir),x2);
								CMLib.database().DBUpdateExits(room.rawDoors()[dir]);
							}
							CMLib.database().DBUpdateExits(room);
						}
						break;
					}
					case BUILD_WALL:
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
							if(title != null) title.updateLot(null);
						}
						break;
					}
					case BUILD_TITLE:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							room.setDisplayText(designTitle);
							CMLib.database().DBUpdateRoom(room);
						}
						break;
					}
					case BUILD_DESC:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							if(workingOn>=0)
							{
								Exit E=room.getExitInDir(workingOn);
								if((!E.isGeneric())&&(room.getRawExit(workingOn)==E))
								{
									E=generify(E);
									room.setRawExit(workingOn,E);
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
						break;
					}
					case BUILD_MONUMENT:
					{
						final Item I=CMClass.getItem("DruidicMonument");
						room.addItem(I);
						I.setExpirationDate(0);
						break;
					}
					case BUILD_CRAWLWAY:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							if((workingOn>=0)&&(room.getExitInDir(workingOn)!=null))
							{
								Exit E=room.getExitInDir(workingOn);
								if((!E.isGeneric())&&(room.getRawExit(workingOn)==E))
								{
									E=generify(E);
									room.setRawExit(workingOn,E);
								}
								final Ability A=CMClass.getAbility("Prop_Crawlspace");
								if(A!=null) E.addNonUninvokableEffect(A);
								CMLib.database().DBUpdateExits(room);
							}
						}
						break;
					}
					case BUILD_WINDOW:
					{
						synchronized(("SYNC"+room.roomID()).intern())
						{
							room=CMLib.map().getRoom(room);
							if((workingOn>=0)&&(room.getExitInDir(workingOn)!=null))
							{
								Exit E=room.getExitInDir(workingOn);
								if((!E.isGeneric())&&(room.getRawExit(workingOn)==E))
								{
									E=generify(E);
									room.setRawExit(workingOn,E);
								}
								final Room R2=room.getRoomInDir(workingOn);
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
						break;
					}
					case BUILD_DEMOLISH:
					default:
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
						break;
					}
					}
				}
			}
		}
		super.unInvoke();
	}

	public boolean isHomePeerRoom(Room R)
	{
		return ifHomePeerLandTitle(R)!=null;
	}

	public boolean isHomePeerTitledRoom(Room R)
	{
		final LandTitle title = ifHomePeerLandTitle(R);
		if(title == null) return false;
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

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if(commands.size()==0)
		{
			commonTell(mob,_("What kind of masonry, where? Try Masonry list."));
			return false;
		}
		final String str=(String)commands.elementAt(0);
		if(("LIST").startsWith(str.toUpperCase()))
		{
			final String mask=CMParms.combine(commands,1);
			final int colWidth=ListingLibrary.ColFixer.fixColWidth(20,mob.session());
			final StringBuffer buf=new StringBuffer(_("@x1 Stone required\n\r",CMStrings.padRight(_("Item"),colWidth)));
			for(int r=0;r<data.length;r++)
			{
				if(((r!=BUILD_MONUMENT)
						||(mob.charStats().getCurrentClass().baseClass().equals("Druid"))
						||CMSecurity.isASysOp(mob))
				&&((mask==null)||(mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(data[r][DAT_NAME],mask)))
				{
					final int woodRequired=adjustWoodRequired(CMath.s_int(data[r][DAT_WOOD]),mob);
					buf.append(CMStrings.padRight(data[r][DAT_NAME],colWidth)+" "+woodRequired);
					if(doingCode==BUILD_PORTCULIS)
						buf.append(_(" metal"));
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
		doingCode=-1;
		dir=-1;

		room=null;
		messedUp=false;

		final String firstWord=(String)commands.firstElement();
		helpingAbility=null;

		if(firstWord.equalsIgnoreCase("help"))
		{
			messedUp=!proficiencyCheck(mob,0,auto);
			duration=25;
			commands.removeElementAt(0);
			final MOB targetMOB=getTarget(mob,commands,givenTarget,false,true);
			if(targetMOB==null) return false;
			if(targetMOB==mob)
			{
				commonTell(mob,_("You can not do that."));
				return false;
			}
			helpingAbility=targetMOB.fetchEffect(ID());
			if(helpingAbility==null)
			{
				commonTell(mob,_("@x1 is not building anything.",targetMOB.Name()));
				return false;
			}
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			{
				helpingAbility=null;
				return false;
			}
			helping=true;
			verb=_("helping @x1 with @x2",targetMOB.name(),helpingAbility.name());
			startStr=_("<S-NAME> start(s) @x1",verb);
			final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),startStr+".");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,duration);
			}
			return true;
		}

		boolean canBuild=CMLib.law().doesOwnThisProperty(mob,mob.location());
		for(int r=0;r<data.length;r++)
		{
			if((r!=BUILD_MONUMENT)
				||(mob.charStats().getCurrentClass().baseClass().equals("Druid"))
				||CMSecurity.isASysOp(mob))
					if(data[r][DAT_NAME].toUpperCase().startsWith(firstWord.toUpperCase()))
						doingCode=r;
		}
		if(doingCode<0)
		{
			commonTell(mob,_("'@x1' is not a valid masonry project.  Try LIST.",firstWord));
			return false;
		}
		final String dirName=(String)commands.lastElement();
		dir=Directions.getGoodDirectionCode(dirName);
		if((doingCode==BUILD_DEMOLISH)&&(dirName.equalsIgnoreCase("roof"))||(dirName.equalsIgnoreCase("ceiling")))
		{

			final Room upRoom=mob.location().getRoomInDir(Directions.UP);
			if(isHomePeerRoom(upRoom))
			{
				commonTell(mob,_("You need to demolish the upstairs rooms first."));
				return false;
			}
			if(mob.location().domainType() == Room.DOMAIN_INDOORS_CAVE)
			{
				commonTell(mob,_("A cave can not have its roof demolished."));
				return false;
			}
			if(!CMath.bset(mob.location().domainType(), Room.INDOORS))
			{
				commonTell(mob,_("There is no ceiling here!"));
				return false;
			}
			if(CMLib.law().isHomeRoomUpstairs(mob.location()))
			{
				commonTell(mob,_("You can't demolish upstairs ceilings.  Try demolishing the room."));
				return false;
			}
			dir=-1;
		}
		else
		if((doingCode==BUILD_DEMOLISH)&&(dirName.equalsIgnoreCase("room")))
		{
			final LandTitle title=CMLib.law().getLandTitle(mob.location());
			if((!CMLib.law().doesOwnThisProperty(mob, mob.location()))
			&&(title!=null)
			&&(title.getOwnerName().length()>0))
			{
				commonTell(mob,_("You can't demolish property you don't own."));
				return false;
			}
			if((title==null)||(!title.allowsExpansionConstruction()))
			{
				commonTell(mob,_("You aren't permitted to demolish this room."));
				return false;
			}
			if(!CMLib.law().isHomeRoomUpstairs(mob.location()))
			{
				commonTell(mob,_("You can only demolish upstairs rooms.  You might try just demolishing the ceiling/roof?"));
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
				mob.tell(_("You can not demolish a room if there is more than one room adjacent to it.  Demolish those first."));
				return false;
			}
			dir=-1;
			canBuild=true;
		}
		else
		if(((dir<0)||(dir==Directions.UP)||(dir==Directions.DOWN))
		&&(CMath.s_int(data[doingCode][DAT_REQDIR])==1))
		{
			commonTell(mob,_("A valid direction in which to build must also be specified."));
			return false;
		}

		if(data[doingCode][DAT_REQNONULL].equals("1")
		&&(dir>=0)
		&&(mob.location().getExitInDir(dir)==null))
		{
			commonTell(mob,_("There is a wall that way that needs to be demolished first."));
			return false;
		}


		int woodRequired=adjustWoodRequired(CMath.s_int(data[doingCode][DAT_WOOD]),mob);
		if(((mob.location().domainType()&Room.INDOORS)==0)
		&&(data[doingCode][DAT_ROOF].equals("1")))
		{
			commonTell(mob,_("That can only be built after a roof, which includes the frame."));
			return false;
		}
		else
		if(((mob.location().domainType()&Room.INDOORS)>0)
		   &&(data[doingCode][DAT_ROOF].equals("2")))
		{
			commonTell(mob,_("That can only be built outdoors!"));
			return false;
		}

		if(doingCode==BUILD_STAIRS)
		{
			final LandTitle title=CMLib.law().getLandTitle(mob.location());
			if((title==null)||(!title.allowsExpansionConstruction()))
			{
				commonTell(mob,_("The title here does not permit the building of stairs."));
				return false;
			}
			if(!CMath.bset(mob.location().domainType(), Room.INDOORS))
			{
				commonTell(mob,_("You need to build a ceiling (or roof) first!"));
				return false;
			}
			if((mob.location().getRoomInDir(Directions.UP)!=null)||(mob.location().rawDoors()[Directions.UP]!=null))
			{
				commonTell(mob,_("There are already stairs here."));
				return false;
			}
		}

		if(doingCode==BUILD_WALL)
		{
			final Room nextRoom=mob.location().getRoomInDir(dir);
			if((nextRoom!=null)&&(CMLib.law().getLandTitle(nextRoom)==null))
			{
				commonTell(mob,_("You can not build a wall blocking off the main entrance!"));
				return false;
			}
			if(mob.location().getExitInDir(dir)==null)
			{
				commonTell(mob,_("There is already a wall in that direction!"));
				return false;
			}
		}

		if(doingCode==BUILD_POOL)
		{
			final Room nextRoom=mob.location().getRoomInDir(Directions.DOWN);
			final Exit exitRoom=mob.location().getExitInDir(Directions.DOWN);
			if((nextRoom!=null)||(exitRoom!=null))
			{
				commonTell(mob,_("You may not build a pool here!"));
				return false;
			}
		}

		if(doingCode==BUILD_TITLE)
		{
			final String title=CMParms.combine(commands,1);
			if(title.length()==0)
			{
				commonTell(mob,_("A title must be specified."));
				return false;
			}
			final TrackingLibrary.TrackingFlags flags=new TrackingLibrary.TrackingFlags();
			final List<Room> checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,20);
			for (final Room room2 : checkSet)
			{
				final Room R=CMLib.map().getRoom(room2);
				if(R.displayText(mob).equalsIgnoreCase(title))
				{
					commonTell(mob,_("That title has already been taken.  Choose another."));
					return false;
				}
			}
			designTitle=title;
		}
		else
		if(doingCode==BUILD_DESC)
		{
			if(commands.size()<3)
			{
				commonTell(mob,_("You must specify an exit direction or the word room, followed by a description for it."));
				return false;
			}
			if(Directions.getGoodDirectionCode((String)commands.elementAt(1))>=0)
			{
				dir=Directions.getGoodDirectionCode((String)commands.elementAt(1));
				if(mob.location().getExitInDir(dir)==null)
				{
					commonTell(mob,_("There is no exit @x1 to describe.",Directions.getInDirectionName(dir)));
					return false;
				}
				workingOn=dir;
				commands.removeElementAt(1);
			}
			else
			if(!((String)commands.elementAt(1)).equalsIgnoreCase("room"))
			{
				commonTell(mob,_("'@x1' is neither the word room, nor an exit direction.",((String)commands.elementAt(1))));
				return false;
			}
			else
				commands.removeElementAt(1);

			final String title=CMParms.combine(commands,1);
			if(title.length()==0)
			{
				commonTell(mob,_("A description must be specified."));
				return false;
			}
			designDescription=title;
		}
		else
		if((doingCode==BUILD_WINDOW)||(doingCode==BUILD_CRAWLWAY))
			workingOn=dir;

		int[][] idata=null;
		if(doingCode==BUILD_PORTCULIS)
		{
			final int[] pm={RawMaterial.MATERIAL_METAL,RawMaterial.MATERIAL_MITHRIL};
			idata=fetchFoundResourceData(mob,
			 							woodRequired,"metal",pm,
			 							0,null,null,
			 							false,
			 							0,null);
			if(idata==null) return false;
			woodRequired=idata[0][FOUND_AMT];
		}
		else
		{
			final int[] pm={RawMaterial.MATERIAL_ROCK};
			idata=fetchFoundResourceData(mob,
										woodRequired,"stone",pm,
										0,null,null,
										false,
										0,null);
			if(idata==null) return false;
			woodRequired=idata[0][FOUND_AMT];
		}

		if(!canBuild)
		{
			if((dir>=0)
			&&((data[doingCode][DAT_REQDIR].equals("1")||(workingOn==dir))))
			{
				final Room R=mob.location().getRoomInDir(dir);
				if((R!=null)&&(CMLib.law().doesOwnThisProperty(mob,R)))
					canBuild=true;
			}
		}
		if(!canBuild)
		{
			commonTell(mob,_("You'll need the permission of the owner to do that."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		room=mob.location();
		if(woodRequired>0)
			CMLib.materials().destroyResourcesValue(mob.location(),woodRequired,idata[0][FOUND_CODE],0,null);

		switch(doingCode)
		{
		case BUILD_ROOF:
			verb=_("building a frame and roof");
			break;
		case BUILD_POOL:
			verb=_("building a pool");
			break;
		case BUILD_WALL:
			verb=_("building the @x1 wall",Directions.getDirectionName(dir));
			break;
		case BUILD_ARCH:
			verb=_("building the @x1 archway",Directions.getDirectionName(dir));
			break;
		case BUILD_PORTCULIS:
			verb=_("building the @x1 portcullis",Directions.getDirectionName(dir));
			break;
		case BUILD_TITLE:
			verb=_("giving this place a title");
			break;
		case BUILD_DESC:
			verb=_("giving this place a description");
			break;
		case BUILD_MONUMENT:
			verb=_("building a druidic monument");
			break;
		case BUILD_WINDOW:
			verb=_("building a window @x1",Directions.getDirectionName(dir));
			break;
		case BUILD_CRAWLWAY:
			verb=_("building a crawlway @x1",Directions.getDirectionName(dir));
			break;
		case BUILD_STAIRS:
			verb=_("building another floor");
			break;
		case BUILD_DEMOLISH:
		default:
			if(dir<0)
			{
				if((mob.location().domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
				   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
						verb=_("demolishing the pool");
				else
				if((mob.location().domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
				   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER))
				{
					commonTell(mob,null,null,_("You must demolish a pool from above."));
					return false;
				}
				else
				if(mob.location().domainType()!=Room.DOMAIN_INDOORS_STONE)
				{
					commonTell(mob,null,null,_("There are no stone constructs to demolish here!"));
					return false;
				}
				else
				if(CMLib.law().isHomeRoomUpstairs(mob.location()))
					verb=_("demolishing the room");
				else
					verb=_("demolishing the roof");
			}
			else
				verb=_("demolishing the @x1 wall",Directions.getDirectionName(dir));
			break;
		}
		messedUp=!proficiencyCheck(mob,0,auto);
		startStr=_("<S-NAME> start(s) @x1",verb);
		playSound="stone.wav";
		if(duration<15) duration=15;

		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),startStr+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
