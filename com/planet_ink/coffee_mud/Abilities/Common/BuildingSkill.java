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
   Copyright 2015-2025 Bo Zimmerman

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
public class BuildingSkill extends CraftingSkill implements CraftorAbility
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
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_BUILDINGSKILL;
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

	protected String getDemolishRoom()
	{
		return "Plains";
	}

	protected String getSoundName()
	{
		return "hammer.wav";
	}

	public BuildingSkill()
	{
		super();
	}

	@Override
	public List<List<String>> fetchMyRecipes(final MOB mob)
	{
		return this.addRecipes(mob, loadRecipes());
	}

	protected boolean canBeDoneSittingDown = false;

	@Override
	public boolean canBeDoneSittingDown()
	{
		return canBeDoneSittingDown;
	}

	protected enum Building
	{
		WALL,
		DOOR,
		ROOM,
		ITEM,
		DEMOLISH,
		TITLE,
		DESC,
		STAIRS,
		EXCAVATE,
		ROOMEFFECT,
		EXITEFFECT,
		DELEFFECT
	}

	protected enum Flag
	{
		DIR,
		DIRUPDOWN,
		NOWALL,
		INDOOR,
		OUTDOOR,
		CAVEONLY,
		NODOWN,
		DOWNONLY,
		WATERONLY,
		WATERSURFACEONLY,
		UNDERWATERONLY,
		SALTWATER,
		FRESHWATER,
		UPONLY
	}

	protected Room		room				= null;
	protected int		dir					= -1;
	protected String[]	recipe				= null;
	protected int		poundsOfMatsUsed	= 0;
	protected String	designTitle			= "";
	protected String	designDescription	= "";

	//protected static final int	RCP_FINALNAME		= 0;
	//protected static final int	RCP_LEVEL			= 1;
	protected static final int	RCP_TICKS		= 2;
	protected final static int	DAT_WOOD		= 3;
	protected final static int	DAT_WOODTYPE	= 4;
	protected final static int	DAT_FLAG		= 5;
	protected final static int	DAT_BUILDCODE	= 6;
	protected final static int	DAT_CLASS		= 7;
	protected final static int	DAT_MISC		= 8;
	protected final static int	DAT_PROPERTIES	= 9;
	protected final static int	DAT_DESC		= 10;
	protected final static int	DAT_BUILDERMASK	= 11;
	protected final static int	DAT_DESCRIPTION	= 12;

	@Override
	public String getRecipeFormat()
	{
		if(Resources.getResource("BUILDING_SKILL_CODES_FLAGS")==null)
		{
			final String[] codes = CMParms.toStringArray(Building.values());
			final String[] flags = CMParms.toStringArray(Flag.values());
			final Pair<String[],String[]> codesFlags = new Pair<String[],String[]>(codes, flags);
			Resources.submitResource("BUILDING_SKILL_CODES_FLAGS", codesFlags);
		}
		return"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tOPTIONAL_BUILDING_RESOURCE_OR_MATERIAL\t"
			+ "BUILDING_FLAGS\tBUILDING_CODE\tROOM_CLASS_ID||EXIT_CLASS_ID||ALLITEM_CLASS_ID||ROOM_CLASS_ID_OR_NONE\t"
			+ "BUILDING_GRID_SIZE||EXIT_NAMES||STAIRS_DESC\tPCODED_SPELL_LIST\tBUILDING_NOUN\tBUILDER_MASK\tBUILDER_DESC";
	}

	@Override
	public String getRecipeFilename()
	{
		return "";
	}

	@Override
	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return null;
	}

	@Override
	protected CostDef getRawTrainingCost()
	{
		return CMProps.getNormalSkillGainCost(ID());
	}

	protected boolean canDescTitleHere(final Room R)
	{
		return true;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked() && (!super.unInvoked))
		{
			if((affected instanceof MOB)&&(!helping))
			{
				final MOB mob=(MOB)affected;
				if(!aborted)
				{
					if((messedUp)&&(room!=null))
					{
						notifyMessUp(mob, recipe);
					}
					else
					{
						this.buildComplete(mob, recipe, room, dir, designTitle, designDescription);
						if(poundsOfMatsUsed>0)
						{
							final CMMsg msg=CMClass.getMsg(mob,room,this,CMMsg.TYP_ITEMGENERATED|CMMsg.MASK_ALWAYS,null);
							msg.setValue(poundsOfMatsUsed/2);
							if(mob.location().okMessage(mob,msg))
								mob.location().send(mob,msg);
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	protected int[][] getBasicMaterials(final MOB mob, final int woodRequired, String miscType)
	{
		if(miscType.length()==0)
			miscType="rock";
		final int[][] idata=fetchFoundResourceData(mob,
													woodRequired,miscType,null,
													0,null,null,
													false,
													0,null);
		return idata;
	}

	public String[][] getRecipeData(final MOB mob)
	{
		final List<List<String>> recipeData = addRecipes(mob,loadRecipes(getRecipeFilename()));
		final String[][] finalDat = new String[recipeData.size()][];
		for(int i=0;i<recipeData.size();i++)
			finalDat[i] = recipeData.get(i).toArray(new String[recipeData.get(i).size()]);
		return finalDat;
	}

	public Exit generify(final Exit X)
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

	protected void notifyMessUp(final MOB mob, final String[] recipe)
	{
		commonTelL(mob,"You've ruined the "+recipe[DAT_DESC]+"!",CMLib.directions().getDirectionName(dir));
	}

	protected void demolishRoom(final MOB mob, final Room room)
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
			if(CMLib.law().doesOwnThisLand(mob, R)||(CMSecurity.isAllowed(mob, R, CMSecurity.SecFlag.CMDROOMS)))
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
			public void apply(final MOB a)
			{
				theRoomToReturnTo.bringMobHere(a, false);
			}
		});
		room.eachItem(new EachApplicable<Item>()
		{
			@Override
			public void apply(final Item a)
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
		CMLib.map().obliterateMapRoom(room);
	}

	private void removeEffects(final PhysicalAgent E, String extraProp)
	{
		extraProp=extraProp.trim();
		if(extraProp.length()>0)
		{
			final List<String> spells = CMParms.parseAny(extraProp, ")", true);
			for(String spellName : spells)
			{
				final int x=spellName.indexOf('(');
				if(x>0)
					spellName=spellName.substring(0,x);
				final Ability A=E.fetchEffect(spellName);
				if(A!=null)
				{
					A.unInvoke();
					E.delEffect(A);
				}
				else
				{
					final Behavior B=E.fetchBehavior(spellName);
					if(B!=null)
						E.delBehavior(B);
				}
			}
		}
	}

	private void addEffects(final PhysicalAgent E, final Room R2, String extraProp)
	{
		extraProp=extraProp.trim();
		if(extraProp.length()>0)
		{
			final List<String> spells = CMParms.parseAny(extraProp, ")", true);
			for(String spellName : spells)
			{
				String parms="";
				final int x=spellName.indexOf('(');
				if(x>0)
				{
					parms=spellName.substring(x+1);
					spellName=spellName.substring(0,x);
				}
				if(parms.trim().equalsIgnoreCase("@remove"))
				{
					final Ability A=E.fetchEffect(spellName);
					if(A!=null)
					{
						A.unInvoke();
						E.delEffect(A);
					}
					else
					{
						final Behavior B=E.fetchBehavior(spellName);
						if(B!=null)
							E.delBehavior(B);
					}
					continue;
				}
				final Ability A=CMClass.getAbility(spellName);
				if(A!=null)
				{

					if(parms.length()>0)
						A.setMiscText(parms);
					else
					if(A.ID().equals("Prop_RoomView"))
						A.setMiscText(CMLib.map().getExtendedRoomID(R2));
					E.addNonUninvokableEffect(A);
				}
				else
				{
					final Behavior B=CMClass.getBehavior(spellName);
					if(parms.length()>0)
						B.setParms(parms);
					E.addBehavior(B);
				}
			}
		}
	}

	protected Room buildRoomAbility(Room R, final int dir, String extraProp)
	{
		synchronized(CMClass.getSync("SYNC"+room.roomID()))
		{
			R=CMLib.map().getRoom(R);
			extraProp=CMStrings.replaceAll(extraProp, "@dir", CMLib.directions().getDirectionName(dir));
			addEffects(R,R,extraProp);
		}
		CMLib.database().DBUpdateRoom(R);
		return R;
	}

	protected Exit buildExitAbility(Room R, final int dir, final String extraProp)
	{
		Exit E=null;
		synchronized(CMClass.getSync("SYNC"+R.roomID()))
		{
			R=CMLib.map().getRoom(R);
			E=R.getExitInDir(dir);
			addEffects(E,R,extraProp);
		}
		CMLib.database().DBUpdateExits(R);
		return E;
	}

	protected Room removeRoomAbility(Room R, final int dir, String extraProp)
	{
		synchronized(CMClass.getSync("SYNC"+R.roomID()))
		{
			R=CMLib.map().getRoom(R);
			extraProp=CMStrings.replaceAll(extraProp, "@dir", CMLib.directions().getDirectionName(dir));
			removeEffects(R,extraProp);
		}
		CMLib.database().DBUpdateRoom(R);
		return R;
	}

	protected Exit removeExitAbility(Room R, final int dir, final String extraProp)
	{
		Exit E=null;
		synchronized(CMClass.getSync("SYNC"+R.roomID()))
		{
			R=CMLib.map().getRoom(R);
			E=R.getExitInDir(dir);
			removeEffects(E,extraProp);
		}
		CMLib.database().DBUpdateExits(R);
		return E;
	}

	protected Room buildNewRoomType(Room room, String newLocale, final String extraProp, int dimension)
	{
		Room R=null;
		synchronized(CMClass.getSync("SYNC"+room.roomID()))
		{
			if((dimension == 0)&&(room instanceof GridLocale))
				dimension = ((GridLocale)room).xGridSize();
			room=CMLib.map().getRoom(room);
			if(newLocale.equalsIgnoreCase("IndoorWaterSurface")
			&&((room.domainType()&Room.INDOORS)==0))
				newLocale="WaterSurface";
			R=CMClass.getLocale(newLocale);
			R.setRoomID(room.roomID());
			R.setDisplayText(room.displayText());
			R.setDescription(room.description());
			if(R.image().equalsIgnoreCase(CMLib.protocol().getDefaultMXPImage(room)))
				R.setImage(null);
			if(R instanceof GridLocale)
			{
				((GridLocale)R).setXGridSize(dimension);
				((GridLocale)R).setYGridSize(dimension);
			}

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
			room.clearSky();
			if(((room.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)||(room.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
			&&(R.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
			&&(R.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE))
			{
				final Room waterR=room.getRoomInDir(Directions.DOWN);
				if((waterR!=null)
				&&((waterR.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)||(waterR.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER))
				&&(waterR.roomID().length()>0))
				{
					final LandTitle title=CMLib.law().getLandTitle(waterR);
					if(title!=null)
					{
						title.setOwnerName("");
						title.updateLot(null);
					}
					room.setRawExit(Directions.DOWN, null);
					room.rawDoors()[Directions.DOWN]=null;
					waterR.setRawExit(Directions.UP, null);
					waterR.rawDoors()[Directions.UP]=null;
					CMLib.map().obliterateMapRoom(waterR);
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
				{
					if(room.getRawExit(d)!=null)
						R.setRawExit(d, (Exit)room.getRawExit(d).copyOf());
				}
			}
			final LandTitle title = CMLib.law().getLandTitle(room);
			if((title!=null)&&(title.gridLayout()))
			{
				final PairVector<Room,int[]> rooms=CMLib.tracking().buildGridList(R, null, 100);
				for(int dir=0;dir<Directions.NUM_DIRECTIONS();dir++)
				{
					if(dir==Directions.GATE)
						continue;
					Room R3=R.getRoomInDir(dir);
					if(R3 == null)
					{
						R3=CMLib.tracking().getCalculatedAdjacentGridRoom(rooms, R3, dir);
						if(R3!=null)
						{
							R.rawDoors()[dir]=R3;
							R3.rawDoors()[Directions.getOpDirectionCode(dir)]=R;
							CMLib.database().DBUpdateExits(R3);
						}
					}
				}
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
				if(dimension > 0)
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
					final LandTitle titleA=CMLib.law().getLandTitle(R);
					if((titleA!=null)&&(CMLib.law().getLandTitle(R2)==null))
					{
						final LandTitle A2=(LandTitle)titleA.copyOf();
						R2.addNonUninvokableEffect((Ability)A2);
					}
					final Ability capacityA = R.fetchEffect("Prop_ReqCapacity");
					if(capacityA != null)
					{
						final Ability A2=(Ability)capacityA.copyOf();
						R2.addNonUninvokableEffect(A2);
					}
					if(CMSecurity.isDebugging(CMSecurity.DbgFlag.PROPERTY))
						Log.debugOut(ID(),R2.roomID()+" created for water.");
					CMLib.database().DBCreateRoom(R2);
					CMLib.database().DBUpdateExits(R2);
				}
			}

			R.getArea().fillInAreaRoom(R);
			addEffects(R,R,extraProp);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.PROPERTY))
				Log.debugOut(ID(),R.roomID()+" updated.");
			CMLib.database().DBUpdateRoom(R);
			CMLib.database().DBUpdateExits(R);
			final MOB mob=CMClass.getFactoryMOB("the wind",1,R);
			try
			{
				R.executeMsg(mob, CMClass.getMsg(mob, R, CMMsg.MSG_NEWROOM, null));
			}
			finally
			{
				mob.destroy();
			}
			room.destroy();
			R.clearSky();
			R.giveASky(0);
		}
		return R;
	}

	protected void buildDoor(final String[] recipe, Room room, final int dir, final int recipeLevel)
	{
		synchronized(CMClass.getSync("SYNC"+room.roomID()))
		{
			final String localeName = recipe[DAT_CLASS];
			String doorName = recipe[DAT_MISC];
			final String spells = recipe[DAT_PROPERTIES];
			//int size = CMath.s_int(recipe[DAT_MISC]);
			String closeWord=null;
			String openWord=null;
			String closedWord=null;
			String displayText="";
			String description="";
			if(doorName.indexOf("|")>0)
			{
				final List<String> split=CMParms.parseAny(doorName, '|',false);
				if(split.get(0).length()>0)
					doorName=split.get(0);
				if((split.size()>1)&&(split.get(1).length()>0))
					openWord=split.get(1);
				if((split.size()>2)&&(split.get(2).length()>0))
					closeWord=split.get(2);
				if((split.size()>3)&&(split.get(3).length()>0))
					closedWord=split.get(3);
				if((split.size()>4)&&(split.get(4).length()>0))
					displayText=split.get(4);
				if((split.size()>5)&&(split.get(5).length()>0))
					description=split.get(5);
			}
			if(closeWord == null)
				closeWord="close";
			if(openWord == null)
				openWord="open";
			if(closedWord == null)
				closedWord=CMLib.english().startWithAorAn("closed "+doorName);
			room=CMLib.map().getRoom(room);
			final Exit X=CMClass.getExit(localeName);
			X.setName(CMLib.english().startWithAorAn(doorName));
			X.setDescription(description);
			X.setDisplayText(displayText);
			X.setOpenDelayTicks(9999);
			X.setExitParams(doorName,closeWord,openWord,closedWord);
			if(X.defaultsClosed() && X.hasADoor())
				X.setDoorsNLocks(X.hasADoor(), !X.defaultsClosed(), X.defaultsClosed(), X.hasALock(), X.hasALock(), X.defaultsLocked());
			addEffects(X, room.getRoomInDir(dir),spells);
			X.basePhyStats().setLevel(recipeLevel);
			X.recoverPhyStats();
			X.text();
			room.setRawExit(dir,X);
			if(room.rawDoors()[dir]!=null)
			{
				final Room oroom = room.rawDoors()[dir].prepareRoomInDir(room, dir);
				final Exit X2=(Exit)X.copyOf();
				X2.recoverPhyStats();
				X2.text();
				oroom.setRawExit(Directions.getOpDirectionCode(dir),X2);
				CMLib.database().DBUpdateExits(oroom);
			}
			CMLib.database().DBUpdateExits(room);
		}
	}

	protected int findFloorNumber(final Room room, final Set<Room> done, final int floor)
	{
		final LandTitle title = CMLib.law().getLandTitle(room);
		if(title == null)
			return floor;
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
		{
			final Room R=room.getRoomInDir(d);
			if((R!=null)&&(!done.contains(R)))
			{
				done.add(R);
				if(d==Directions.UP)
				{
					final int f=findFloorNumber(R,done,floor-1);
					if(f != Integer.MIN_VALUE)
						return f;
				}
				else
				if(d==Directions.DOWN)
				{
					final int f=findFloorNumber(R,done,floor+1);
					if(f != Integer.MIN_VALUE)
						return f;
				}
				else
				{
					final int f=findFloorNumber(R,done,floor);
					if(f != Integer.MIN_VALUE)
						return f;
				}
			}
		}
		return Integer.MIN_VALUE;
	}

	protected Room buildStairs(final MOB mob, Room room, final int dir, final String[] recipe)
	{
		Room newRoom;
		synchronized(CMClass.getSync("SYNC"+room.roomID()))
		{
			String varifiableDesc = recipe[DAT_MISC];
			final String addParms = recipe[DAT_PROPERTIES];
			final String localeClass = recipe[DAT_CLASS];
			if(varifiableDesc.equals("0"))
				varifiableDesc="";

			final int opDir = Directions.getOpDirectionCode(dir);
			room=CMLib.map().getRoom(room);
			final int floor=findFloorNumber(room, new HashSet<Room>(), 1);
			if(localeClass.length()==0)
				newRoom=CMClass.getLocale(CMClass.classID(room));
			else
				newRoom=CMClass.getLocale(localeClass);
			newRoom.setRoomID(room.getArea().getNewRoomID(room,dir));
			if(newRoom.roomID().length()==0)
			{
				final String verbDesc = recipe[DAT_DESC];
				commonTelL(mob,"You've failed to build the "+verbDesc+"!",CMLib.directions().getDirectionName(dir));
				return null;
			}
			newRoom.setArea(room.getArea());
			LandTitle newTitle=CMLib.law().getLandTitle(room);
			if((newTitle!=null)&&(CMLib.law().getLandTitle(newRoom)==null))
			{
				final Room testRoom = newTitle.getAConnectedPropertyRoom();
				if(testRoom != null)
				{
					final Ability cap = testRoom.fetchEffect("Prop_ReqCapacity");
					if(cap != null)
						newRoom.addNonUninvokableEffect((Ability)cap.copyOf());
				}
				newTitle = newTitle.generateNextRoomTitle();
				newTitle.setLandPropertyID(newRoom.roomID());
				newRoom.addNonUninvokableEffect((Ability)newTitle);
			}

			int newFloorNum;
			int curFloorNum;
			if(dir == Directions.DOWN)
			{
				newFloorNum = floor;
				curFloorNum = (floor - 1);
			}
			else
			{
				newFloorNum = (floor+1);
				curFloorNum = floor;
			}

			final Exit newExit;
			final Exit returnExit;

			if(((dir == Directions.UP)||(dir == Directions.DOWN))&&(newFloorNum > 0))
			{
				newExit=CMClass.getExit("GenExit");
				newExit.setName(L("a passageway"));
				newExit.setDescription(L(varifiableDesc,CMLib.directions().getDirectionName(dir),""+newFloorNum+CMath.numAppendage(newFloorNum)));
				newExit.setDisplayText(L(varifiableDesc,CMLib.directions().getDirectionName(dir),""+newFloorNum+CMath.numAppendage(newFloorNum)));
				addEffects(newExit, room.getRoomInDir(dir),addParms);
				newExit.recoverPhyStats();
				newExit.text();

				returnExit=CMClass.getExit("GenExit");
				returnExit.setName(L("a passageway"));
				returnExit.setDescription(L(varifiableDesc,CMLib.directions().getDirectionName(opDir),""+curFloorNum+CMath.numAppendage(curFloorNum)));
				returnExit.setDisplayText(L(varifiableDesc,CMLib.directions().getDirectionName(opDir),""+curFloorNum+CMath.numAppendage(curFloorNum)));
				addEffects(returnExit, room.getRoomInDir(dir),addParms);
				returnExit.recoverPhyStats();
				returnExit.text();
			}
			else
			{
				newExit=CMClass.getExit("GenExit");
				newExit.setName(L("a passageway"));
				addEffects(newExit, room.getRoomInDir(dir),addParms);
				newExit.recoverPhyStats();
				newExit.text();

				returnExit=CMClass.getExit("GenExit");
				returnExit.setName(L("a passageway"));
				addEffects(returnExit, room.getRoomInDir(dir),addParms);
				returnExit.recoverPhyStats();
				returnExit.text();
			}
			room.rawDoors()[dir]=newRoom;
			room.setRawExit(dir,newExit);

			newRoom.rawDoors()[opDir]=room;
			newRoom.setRawExit(opDir,returnExit);

			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.PROPERTY))
				Log.debugOut(ID(),newRoom.roomID()+" created and put up for sale.");
			CMLib.database().DBCreateRoom(newRoom);
			if(newTitle!=null)
			{
				if(newTitle.gridLayout())
				{
					final PairVector<Room,int[]> rooms=CMLib.tracking().buildGridList(newRoom, null, 100);
					for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
					{
						if(d==Directions.GATE)
							continue;
						Room R3=newRoom.getRoomInDir(d);
						if(R3 == null)
						{
							R3=CMLib.tracking().getCalculatedAdjacentGridRoom(rooms, R3, d);
							if(R3!=null)
							{
								newRoom.rawDoors()[d]=R3;
								if(R3.rawDoors()[Directions.getOpDirectionCode(d)]==null)
									R3.rawDoors()[Directions.getOpDirectionCode(d)]=newRoom;
							}
						}
					}
				}
				CMLib.law().colorRoomForSale(newRoom, newTitle, true);
				newTitle.updateLot(null);
			}
			newRoom.getArea().fillInAreaRoom(newRoom);
			CMLib.database().DBUpdateExits(newRoom);
			CMLib.database().DBUpdateExits(room);
			newRoom.executeMsg(mob, CMClass.getMsg(mob, newRoom, CMMsg.MSG_NEWROOM, null));
		}
		return newRoom;
	}

	protected void buildWall(Room room, final int dir)
	{
		synchronized(CMClass.getSync("SYNC"+room.roomID()))
		{
			room=CMLib.map().getRoom(room);
			room.setRawExit(dir,null);
			if(room.rawDoors()[dir]!=null)
			{
				final Room oroom = room.rawDoors()[dir].prepareRoomInDir(room, dir);
				oroom.setRawExit(Directions.getOpDirectionCode(dir),null);
				CMLib.database().DBUpdateExits(oroom);
			}
			CMLib.database().DBUpdateExits(room);
			final LandTitle title=CMLib.law().getLandTitle(room);
			if(title != null)
				title.updateLot(null);
		}
	}

	protected void buildTitle(Room room, final String designTitle)
	{
		synchronized(CMClass.getSync("SYNC"+room.roomID()))
		{
			room=CMLib.map().getRoom(room);
			Room returnRoom = null;
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				if(room.getRoomInDir(d)!=null)
					returnRoom=room.getRoomInDir(d);
			}
			room.setDisplayText(designTitle);
			CMLib.database().DBUpdateRoom(room);
			if(room instanceof GridLocale)
			{
				((GridLocale)room).clearGrid(returnRoom);
				((GridLocale)room).buildGrid();
			}
			final LandTitle T=CMLib.law().getLandTitle(room);
			if(T != null)
				T.updateLot(null);
		}
	}

	protected void buildDesc(Room room, final int dir, final String designDescription)
	{
		synchronized(CMClass.getSync("SYNC"+room.roomID()))
		{
			room=CMLib.map().getRoom(room);
			if(room == null)
				return;
			if(dir>=0)
			{
				Exit E=room.getExitInDir(dir);
				if(E!=null)
				{
					if((!E.isGeneric())&&(room.getRawExit(dir)==E))
					{
						E=generify(E);
						room.setRawExit(dir,E);
					}
					E.setDescription(designDescription);
					CMLib.database().DBUpdateExits(room);
				}
			}
			else
			{
				Room returnRoom = null;
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					if(room.getRoomInDir(d)!=null)
						returnRoom=room.getRoomInDir(d);
				}
				room.setDescription(designDescription);
				if(room instanceof GridLocale)
				{
					((GridLocale)room).clearGrid(returnRoom);
					((GridLocale)room).buildGrid();
				}
				CMLib.database().DBUpdateRoom(room);
				final LandTitle T=CMLib.law().getLandTitle(room);
				if(T != null)
					T.updateLot(null);
			}
		}
	}

	protected void demolish(final MOB mob, Room room, final int dir, final String[] recipe)
	{
		synchronized(CMClass.getSync("SYNC"+room.roomID()))
		{
			room=CMLib.map().getRoom(room);
			if(dir<0)
			{

				if((CMLib.law().isHomeRoomUpstairs(room))
				||(recipe[DAT_CLASS].trim().length()==0))
				{
					demolishRoom(mob,room);
				}
				else
					convertToPlains(room,recipe[DAT_CLASS]);
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

	protected void buildComplete(final MOB mob, final String[] recipe, final Room room, final int dir, final String designTitle, final String designDescription)
	{
		final Building doingCode = Building.valueOf(recipe[DAT_BUILDCODE]);
		switch(doingCode)
		{
		case DEMOLISH:
		{
			this.demolish(mob, room, dir, recipe);
			break;
		}
		case DESC:
		{
			this.buildDesc(room, dir, designDescription);
			break;
		}
		case DOOR:
		{
			this.buildDoor(recipe, room, dir, CMath.s_int(recipe[RCP_LEVEL]));
			break;
		}
		case ITEM:
		{
			final Item I=CMClass.getItem(recipe[DAT_CLASS]);
			room.addItem(I);
			I.setExpirationDate(0);
			break;
		}
		case ROOM:
		{
			final String localeName = recipe[DAT_CLASS];
			final String spells = recipe[DAT_PROPERTIES];
			final int size = CMath.s_int(recipe[DAT_MISC]);
			this.buildNewRoomType(room, localeName, spells, size);
			break;
		}
		case DELEFFECT:
		{
			final String spells = recipe[DAT_PROPERTIES];
			if(dir >=0)
				this.removeExitAbility(room, dir, spells);
			else
				this.removeRoomAbility(room, dir, spells);
			break;
		}
		case ROOMEFFECT:
		{
			final String spells = recipe[DAT_PROPERTIES];
			this.buildRoomAbility(room, dir, spells);
			break;
		}
		case EXITEFFECT:
		{
			final String spells = recipe[DAT_PROPERTIES];
			this.buildExitAbility(room, dir, spells);
			break;
		}
		case EXCAVATE:
		case STAIRS:
		{
			this.buildStairs(mob, room, dir, recipe);
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
		}
	}

	protected Room convertToPlains(final Room room, final String localeID)
	{
		final Room R=CMClass.getLocale(localeID);
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
			if((A!=null)
			&&(!A.ID().equalsIgnoreCase("Prop_Crawlspace")))
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
		CMLib.threads().rejuv(R, Tickable.TICKID_ROOM_ITEM_REJUV);
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

	public boolean isHomePeerRoom(final Room R, final boolean anyType)
	{
		return ifHomePeerLandTitle(R,anyType)!=null;
	}

	public boolean isHomePeerTitledRoom(final Room R, final boolean anyType)
	{
		final LandTitle title = ifHomePeerLandTitle(R,anyType);
		if(title == null)
			return false;
		return title.getOwnerName().length()>0;
	}

	public boolean countsAsACave(final Room R)
	{
		if(R==null)
			return false;
		if((R.domainType()==Room.DOMAIN_INDOORS_CAVE)
		||((R.getAtmosphere()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_ROCK))
			return true;
		if(R.domainType()==Room.DOMAIN_INDOORS_CAVE_SEAPORT)
			return true;
		if(R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
		{
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				if(countsAsACave(R.getRoomInDir(d)))
					return true;
		}
		return false;
	}

	public LandTitle ifHomePeerLandTitle(final Room R, final boolean anyType)
	{
		if((R!=null)
		&&(R.ID().length()>0)
		&&(anyType||CMath.bset(R.domainType(),Room.INDOORS)))
			return CMLib.law().getLandTitle(R);
		return null;
	}

	public String establishVerb(final MOB mob, final String[] recipe)
	{
		String verb="";
		final Building doingCode = Building.valueOf(recipe[DAT_BUILDCODE]);
		if(doingCode == Building.DEMOLISH)
		{
			if(dir<0)
			{
				final Room R=mob.location();
				boolean roomClassFound = false;
				for(final List<String> recipeChk : loadRecipes(getRecipeFilename()))
				{
					if(R.ID().equalsIgnoreCase(recipeChk.get(DAT_CLASS)))
						roomClassFound=true;
				}
				if((R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
				||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
					verb=L("demolishing the pool");
				else
				if((R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
				||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER))
				{
					commonTelL(mob,(Environmental)null,null,"You must demolish a pool from above.");
					return "";
				}
				else
				if(!roomClassFound)
				{
					commonTelL(mob,(Environmental)null,null,"This building was not made with @x1, you can`t demolish it.",name());
					return "";
				}
				else
				if(CMLib.law().isHomeRoomUpstairs(R))
					verb=L("demolishing the room");
				else
					verb=L("demolishing the roof");
			}
			else
				verb=L("demolishing the wall @x1",CMLib.directions().getInDirectionName(dir).toLowerCase());
		}
		else
			verb = L("building the @x1",
					CMStrings.replaceVariables(recipe[DAT_DESC],CMLib.directions().getInDirectionName(dir).toLowerCase()));
		return verb;
	}

	private Set<Flag> makeFlags(final String[] recipe)
	{
		final Set<Flag> flags = new HashSet<Flag>();
		final String[] flagStrs = CMParms.parse(recipe[DAT_FLAG].toUpperCase()).toArray(new String[0]);
		for(final String flag : flagStrs)
		{
			final Flag F=(Flag)CMath.s_valueOf(Flag.class, flag);
			if(F!=null)
				flags.add(F);
		}
		return flags;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(checkStop(mob, commands))
			return true;

		randomRecipeFix(mob,addRecipes(mob,loadRecipes(getRecipeFilename())),commands,0);

		if(commands.size()==0)
		{
			commonTelL(mob,"What kind of @x1, where? Try @x2 list.",name(),CMStrings.capitalizeAndLower(this.triggerStrings()[0]));
			return false;
		}
		poundsOfMatsUsed	= 0;
		canBeDoneSittingDown = false;
		final String str=commands.get(0);
		final String[][] data=getRecipeData(mob);
		final LandTitle title = CMLib.law().getLandTitle(mob.location());
		final double landValue = ((title == null) ? 0 : title.getPrice()) / 100.0;
		final String landCurrency = CMLib.beanCounter().getCurrency(mob.location());
		final boolean getInfo = ("INFO").startsWith(str.toUpperCase());
		if(("LIST").startsWith(str.toUpperCase())||getInfo)
		{
			boolean hasValueTag = false;
			String mask=CMParms.combine(commands,1);
			if((getInfo && mask.length()==0))
				mask="ALL";
			int colWidth=CMLib.lister().fixColWidth(20,mob.session());
			final StringBuffer buf;
			if(getInfo)
			{
				colWidth=CMLib.lister().fixColWidth(13,mob.session());
				buf=new StringBuffer("^H"+CMStrings.padRight(L("Item"),colWidth) + L(" Description^N\n\r",this.getMainResourceName()));
			}
			else
				buf=new StringBuffer("^H"+CMStrings.padRight(L("Item"),colWidth) + L(" @x1 required^N\n\r",this.getMainResourceName()));
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
					buf.append("^w"+CMStrings.padRight(data[r][RCP_FINALNAME],colWidth)+"^N ");
					if(getInfo)
					{
						if(DAT_DESCRIPTION < data[r].length)
							buf.append(data[r][DAT_DESCRIPTION]).append("\n\r");
					}
					else
					{
						String material=data[r][DAT_WOODTYPE];
						if(material.equalsIgnoreCase("VALUE"))
						{
							hasValueTag=true;
							final String woodStr = data[r][DAT_WOOD];
							if(CMath.isInteger(woodStr))
							{
								int wood=CMath.s_int(woodStr);
								wood=adjustWoodRequired(wood,mob);
								if(title == null)
									buf.append(wood+"% ??\n\r");
								else
									buf.append(CMLib.beanCounter().nameCurrencyLong(landCurrency, landValue * wood)).append("\n\r");
							}
							else
								buf.append("??\n\r");
						}
						else
						if(material.equalsIgnoreCase("MONEY"))
						{
							final String woodStr = data[r][DAT_WOOD];
							if(CMath.isInteger(woodStr))
							{
								int wood=CMath.s_int(woodStr);
								wood=adjustWoodRequired(wood,mob);
								buf.append(CMLib.beanCounter().nameCurrencyLong(landCurrency, wood)).append("\n\r");
							}
							else
								buf.append("??\n\r");
						}
						else
						{
							final String wood=getComponentDescription(mob,Arrays.asList(data[r]),DAT_WOOD);
							if(wood.length()>5)
								material="";
							if(material.equalsIgnoreCase("wooden"))
								material="wood";
							buf.append(wood+" "+material.toLowerCase()+"\n\r");
						}
					}
				}
			}
			if((title == null) && hasValueTag)
				buf.append(L("\n\rYou can't tell anything about some costs from this location.\n\r"));
			commonTell(mob,buf.toString());
			return true;
		}
		else
		if(("SURVEY").startsWith(str.toUpperCase()))
		{
			//TODO: finish this, but what's this do again?
		}

		designTitle="";
		designDescription="";
		String startStr=null;
		int duration=15;
		recipe = null;
		Building doingCode = null;
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
				commonTelL(mob,"You can not do that.");
				return false;
			}
			helpingAbility=targetMOB.fetchEffect(ID());
			if(helpingAbility==null)
			{
				commonTelL(mob,"@x1 is not building anything.",targetMOB.Name());
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

		boolean canBuild=CMLib.law().doesOwnThisLand(mob,mob.location()) || CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDROOMS);
		final String allWords=CMParms.combine(commands,0).toUpperCase();
		for(int r=0;r<data.length;r++)
		{
			final Building buildCode = Building.valueOf(data[r][DAT_BUILDCODE]);
			if((data[r][0].toUpperCase().equals(allWords))
			&&((data[r][DAT_BUILDERMASK].length()==0)
				||(CMLib.masking().maskCheck(data[r][DAT_BUILDERMASK], mob, false))
				||CMSecurity.isASysOp(mob)))
			{
				doingCode=buildCode;
				recipe = data[r];
			}
		}
		if((doingCode==null) || (recipe == null))
		{
			for(int r=0;r<data.length;r++)
			{
				final Building buildCode = Building.valueOf(data[r][DAT_BUILDCODE]);
				if((data[r][0].toUpperCase().startsWith(allWords))
				&&((data[r][DAT_BUILDERMASK].length()==0)
					||(CMLib.masking().maskCheck(data[r][DAT_BUILDERMASK], mob, false))
					||CMSecurity.isASysOp(mob)))
				{
					doingCode=buildCode;
					recipe = data[r];
				}
			}
		}
		if((doingCode==null) || (recipe == null))
		{
			for(int r=0;r<data.length;r++)
			{
				final Building buildCode = Building.valueOf(data[r][DAT_BUILDCODE]);
				if((data[r][0].toUpperCase().startsWith(firstWord.toUpperCase()))
				&&((data[r][DAT_BUILDERMASK].length()==0)
					||(CMLib.masking().maskCheck(data[r][DAT_BUILDERMASK], mob, false))
					||CMSecurity.isASysOp(mob)))
				{
					doingCode=buildCode;
					recipe = data[r];
				}
			}
		}
		if((doingCode == null)||(recipe == null))
		{
			commonTelL(mob,"'@x1' is not a valid @x2 project.  Try LIST.",firstWord,name());
			return false;
		}

		final Set<Flag> flags = makeFlags(recipe);

		if((mob.location()!=null) // this is correct!
		&&((mob.location() instanceof Boardable) || (mob.location().getArea() instanceof Boardable)))
		{
			commonTelL(mob,"You may not do @x1 projects here.",name());
			return false;
		}
		final String dirName=commands.get(commands.size()-1);
		dir=CMLib.directions().getGoodDirectionCode(dirName);

		if((doingCode == Building.DEMOLISH)&&(dirName.equalsIgnoreCase("roof")||(dirName.equalsIgnoreCase("ceiling"))))
		{
			this.canBeDoneSittingDown = true;
			final Room upRoom=mob.location().getRoomInDir(Directions.UP);
			if(isHomePeerRoom(upRoom,flags.contains(Flag.CAVEONLY)))
			{
				commonTelL(mob,"You need to demolish the upstairs rooms first.");
				return false;
			}
			if(flags.contains(Flag.CAVEONLY))
			{
				if((!countsAsACave(mob.location()))&&(!countsAsACave(upRoom)))
				{
					commonTelL(mob,"This can only be done underground.");
					return false;
				}
			}
			if(mob.location().domainType() == Room.DOMAIN_INDOORS_CAVE)
			{
				commonTelL(mob,"A cave can not have its roof demolished.");
				return false;
			}
			if(!CMath.bset(mob.location().domainType(), Room.INDOORS))
			{
				commonTelL(mob,"There is no ceiling here!");
				return false;
			}
			if(CMLib.law().isHomeRoomUpstairs(mob.location()))
			{
				commonTelL(mob,"You can't demolish a ceiling in an upstairs room.  Try demolishing the room.");
				return false;
			}
			dir=-1;
		}
		else
		if((doingCode == Building.DEMOLISH)&&(dirName.equalsIgnoreCase("room")))
		{
			if(flags.contains(Flag.CAVEONLY))
			{
				if(!countsAsACave(mob.location()))
				{
					commonTelL(mob,"This can only be done underground.");
					return false;
				}
			}
			this.canBeDoneSittingDown = true;
			if((!CMLib.law().doesOwnThisLand(mob, mob.location()))
			&&(!CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDROOMS))
			&&(title!=null)
			&&(title.getOwnerName().length()>0))
			{
				commonTelL(mob,"You can't demolish property you don't own.");
				return false;
			}
			if((title==null)||(!title.allowsExpansionConstruction()))
			{
				commonTelL(mob,"You aren't permitted to demolish this room.");
				return false;
			}
			if(!flags.contains(Flag.CAVEONLY))
			{
				if(!CMLib.law().isHomeRoomUpstairs(mob.location()))
				{
					commonTelL(mob,"You can only demolish upstairs/downstairs rooms.  You might try just demolishing the ceiling/roof?");
					return false;
				}
			}
			int numAdjacentProperties=0;
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				final Room adjacentRoom=mob.location().getRoomInDir(d);
				if(isHomePeerTitledRoom(adjacentRoom,flags.contains(Flag.CAVEONLY)))
				{
					numAdjacentProperties++;
				}
			}
			if(numAdjacentProperties>1)
			{
				commonTelL(mob,"You can not demolish a room if there is more than one room adjacent to it.  Demolish those first.");
				return false;
			}
			dir=-1;
			canBuild=true;
		}
		else
		if(((dir<0)||(dir==Directions.UP)||(dir==Directions.DOWN))
		&&(flags.contains(Flag.DIR)))
		{
			commonTelL(mob,"A valid direction in which to build must also be specified.");
			return false;
		}
		else
		if((dir<0)
		&&(flags.contains(Flag.DIRUPDOWN)))
		{
			commonTelL(mob,"A valid direction in which to build must also be specified.");
			return false;
		}

		if((flags.contains(Flag.NOWALL))
		&&(dir>=0)
		&&(mob.location().getExitInDir(dir)==null))
		{
			commonTelL(mob,"There is a wall that way that needs to be demolished first.");
			return false;
		}

		int woodRequired=adjustWoodRequired(CMath.s_int(recipe[DAT_WOOD]),mob);
		if(((mob.location().domainType()&Room.INDOORS)==0)
		&&(flags.contains(Flag.INDOOR)))
		{
			commonTelL(mob,"That can only be built after a roof, which includes the frame.");
			return false;
		}
		else
		if(((mob.location().domainType()&Room.INDOORS)>0)
		&&(flags.contains(Flag.OUTDOOR)))
		{
			commonTelL(mob,"That can only be built outdoors!");
			return false;
		}

		if(doingCode == Building.STAIRS)
		{
			if((dir!=Directions.UP)&&(dir!=Directions.DOWN))
			{
				commonTelL(mob,"A valid direction in which to build must also be specified.  Try UP or DOWN.");
				return false;
			}
		}

		if(doingCode == Building.EXCAVATE)
		{
			if(dir==Directions.DOWN)
			{
				switch(mob.location().domainType())
				{
				case Room.DOMAIN_INDOORS_METAL:
				case Room.DOMAIN_INDOORS_STONE:
				case Room.DOMAIN_INDOORS_WOOD:
				{
					final int floorNumber = this.findFloorNumber(mob.location(), new HashSet<Room>(), 1);
					if(floorNumber > 1)
					{
						commonTelL(mob,"You cannot excavate from above the ground.");
						return false;
					}
					break;
				}
				case Room.DOMAIN_OUTDOORS_AIR:
				case Room.DOMAIN_OUTDOORS_UNDERWATER:
				case Room.DOMAIN_OUTDOORS_WATERSURFACE:
				case Room.DOMAIN_INDOORS_AIR:
				case Room.DOMAIN_INDOORS_UNDERWATER:
				case Room.DOMAIN_INDOORS_WATERSURFACE:
					commonTelL(mob,"You can only excavate down into the ground.");
					return false;
				}
				flags.remove(Flag.CAVEONLY);  // caveonly only matters if complex DOWN rules don't apply.
			}
		}

		if((doingCode == Building.STAIRS)||(doingCode == Building.EXCAVATE))
		{
			if((title==null)||(!title.allowsExpansionConstruction()))
			{
				commonTelL(mob,"The title here does not permit the building of new places.");
				return false;
			}
			if((!CMath.bset(mob.location().domainType(), Room.INDOORS))&&(dir==Directions.UP))
			{
				commonTelL(mob,"You need to build a ceiling (or roof) first!");
				return false;
			}
			final Room inR=mob.location().getRoomInDir(dir);
			if(inR!=null)
			{
				if(dir == Directions.UP)
					commonTelL(mob,"There is already something up here.");
				else
				if(dir == Directions.DOWN)
					commonTelL(mob,"There is already something down here.");
				else
					commonTelL(mob,"There is already something over there.");
				return false;
			}

			final Room R = title.getAConnectedPropertyRoom();
			if(R != null)
			{
				final Ability cap = R.fetchEffect("Prop_ReqCapacity");
				if(cap != null)
				{
					final int numRooms = title.getNumConnectedPropertyRooms();
					final int roomLimit = CMParms.getParmInt(cap.text(),"rooms",Integer.MAX_VALUE);
					if(numRooms >= roomLimit)
					{
						commonTelL(mob,"You are not allowed to add more rooms.");
						return false;
					}
				}
			}
		}

		if(doingCode == Building.WALL)
		{
			final Room nextRoom=mob.location().getRoomInDir(dir);
			if((nextRoom!=null)&&(CMLib.law().getLandTitle(nextRoom)==null))
			{
				commonTelL(mob,"You can not build a wall blocking off the main entrance!");
				return false;
			}
			if(mob.location().getExitInDir(dir)==null)
			{
				commonTelL(mob,"There is already a wall in that direction!");
				return false;
			}
		}

		if(flags.contains(Flag.NODOWN))
		{
			final Room nextRoom=mob.location().getRoomInDir(Directions.DOWN);
			final Exit exitRoom=mob.location().getExitInDir(Directions.DOWN);
			if((nextRoom!=null)||(exitRoom!=null))
			{
				commonTelL(mob,"You may not build that here!");
				return false;
			}
		}

		if(flags.contains(Flag.DOWNONLY))
		{
			final Room nextRoom=mob.location().getRoomInDir(Directions.DOWN);
			final Exit exitRoom=mob.location().getExitInDir(Directions.DOWN);
			if((nextRoom!=null)&&(exitRoom!=null)&&(nextRoom.roomID().length()>0))
			{
				commonTelL(mob,"You may not build that here!");
				return false;
			}
			dir=Directions.DOWN;
		}

		if(flags.contains(Flag.UPONLY))
		{
			final Room nextRoom=mob.location().getRoomInDir(Directions.UP);
			final Exit exitRoom=mob.location().getExitInDir(Directions.UP);
			if((nextRoom!=null)&&(exitRoom!=null)&&(nextRoom.roomID().length()>0))
			{
				commonTelL(mob,"You may not build that here!");
				return false;
			}
			dir=Directions.UP;
		}

		if(flags.contains(Flag.CAVEONLY))
		{
			if(!countsAsACave(mob.location()))
			{
				commonTelL(mob,"This can only be done underground.");
				return false;
			}
		}

		if(flags.contains(Flag.WATERONLY))
		{
			if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
			&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
			&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
			&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_UNDERWATER))
			{
				commonTelL(mob,"This can only be done in water.");
				return false;
			}
		}

		if(flags.contains(Flag.WATERSURFACEONLY))
		{
			if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
			&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE))
			{
				commonTelL(mob,"This can only be done on the water.");
				return false;
			}
		}

		if(flags.contains(Flag.UNDERWATERONLY))
		{
			if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
			&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_UNDERWATER))
			{
				commonTelL(mob,"This can only be done under the water.");
				return false;
			}
		}

		if(flags.contains(Flag.SALTWATER))
		{
			if((mob.location().getAtmosphere()!=RawMaterial.RESOURCE_SALTWATER)
			&&((!(mob.location() instanceof Drink))||(((Drink)mob.location()).liquidType()!=RawMaterial.RESOURCE_SALTWATER)))
			{
				commonTelL(mob,"This can only be done in salt water.");
				return false;
			}
		}

		if(flags.contains(Flag.FRESHWATER))
		{
			if((mob.location().getAtmosphere()!=RawMaterial.RESOURCE_FRESHWATER)
			&&((!(mob.location() instanceof Drink))||(((Drink)mob.location()).liquidType()!=RawMaterial.RESOURCE_SALTWATER)))
			{
				commonTelL(mob,"This can only be done in fresh water.");
				return false;
			}
		}

		if(doingCode == Building.TITLE)
		{
			if(!canDescTitleHere(mob.location()))
			{
				commonTelL(mob,"You can't do that here.");
				return false;
			}
			String titleStr=CMParms.combine(commands,1);
			if(titleStr.length()==0)
			{
				commonTelL(mob,"A title must be specified.");
				return false;
			}
			titleStr=CMLib.coffeeFilter().secondaryUserInputFilter(titleStr);
			if(titleStr.length()>253)
			{
				commonTelL(mob,"That title is too long.");
				return false;
			}
			final TrackingLibrary.TrackingFlags trackingFlags=CMLib.tracking().newFlags();
			final List<Room> checkSet=CMLib.tracking().getRadiantRooms(mob.location(),trackingFlags,20);
			for (final Room room2 : checkSet)
			{
				final Room R=CMLib.map().getRoom(room2);
				if(R!=null)
				{
					if(R.displayText(mob).equalsIgnoreCase(titleStr))
					{
						commonTelL(mob,"That title has already been taken.  Choose another.");
						return false;
					}
				}
			}
			designTitle=titleStr;
		}
		else
		if(doingCode == Building.DESC)
		{
			if(commands.size()<3)
			{
				commonTelL(mob,"You must specify an exit direction or the word room, followed by a description for it.");
				return false;
			}

			if(!canDescTitleHere(mob.location()))
			{
				commonTelL(mob,"You can't do that here.");
				return false;
			}

			if(CMLib.directions().getGoodDirectionCode(commands.get(1))>=0)
			{
				dir=CMLib.directions().getGoodDirectionCode(commands.get(1));
				if(mob.location().getExitInDir(dir)==null)
				{
					commonTelL(mob,"There is no exit @x1 to describe.",CMLib.directions().getInDirectionName(dir));
					return false;
				}
				commands.remove(1);
			}
			else
			if(!commands.get(1).equalsIgnoreCase("room"))
			{
				commonTelL(mob,"'@x1' is neither the word room, nor an exit direction.",(commands.get(1)));
				return false;
			}
			else
				commands.remove(1);

			String descStr=CMParms.combine(commands,1);
			descStr=CMLib.coffeeFilter().secondaryUserInputFilter(descStr);
			if(descStr.length()==0)
			{
				commonTelL(mob,"A description must be specified.");
				return false;
			}
			designDescription=descStr;
		}

		int[][] idata;
		if(recipe[DAT_WOODTYPE].equalsIgnoreCase("MONEY"))
		{
			idata=null;
			int wood=CMath.s_int(recipe[DAT_WOOD]);
			wood=adjustWoodRequired(wood,mob);
			if(CMLib.beanCounter().getTotalAbsoluteValue(mob, landCurrency) < wood)
			{
				commonTelL(mob,"You'll need @x1 to do that.",CMLib.beanCounter().nameCurrencyLong(landCurrency, wood));
				return false;
			}
			woodRequired=0;
		}
		else
		if(recipe[DAT_WOODTYPE].equalsIgnoreCase("VALUE"))
		{
			idata=null;
			int wood=CMath.s_int(recipe[DAT_WOOD]);
			wood=adjustWoodRequired(wood,mob);
			final double roomValue = landValue * wood;
			if(CMLib.beanCounter().getTotalAbsoluteValue(mob, landCurrency) < roomValue)
			{
				commonTelL(mob,"You'll need @x1 to do that.",CMLib.beanCounter().nameCurrencyLong(landCurrency, roomValue));
				return false;
			}
			woodRequired=0;
		}
		else
		{
			idata=this.getBasicMaterials(mob, woodRequired, recipe[DAT_WOODTYPE]);

			if(idata==null)
				return false;
			woodRequired=idata[0][FOUND_AMT];
		}

		if(!canBuild)
		{
			if((dir>=0)&&(flags.contains(Flag.DIR)))
			{
				final Room R=mob.location().getRoomInDir(dir);
				if((R!=null)
				&&(CMLib.law().doesOwnThisLand(mob,R)))
					canBuild=true;
			}
		}
		if(!canBuild)
		{
			commonTelL(mob,"You'll need the permission of the owner to do that.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		room=mob.location();
		if(room.getGridParent() != null)
			room = room.getGridParent();

		if((woodRequired>0)&&(idata!=null))
			CMLib.materials().destroyResourcesValue(mob.location(),woodRequired,idata[0][FOUND_CODE],idata[0][FOUND_SUB],0,0);
		else
		if(recipe[DAT_WOODTYPE].equalsIgnoreCase("VALUE"))
		{
			int wood=CMath.s_int(recipe[DAT_WOOD]);
			wood=adjustWoodRequired(wood,mob);
			final double roomValue = landValue * wood;
			CMLib.beanCounter().subtractMoney(mob, landCurrency, roomValue);
		}
		else
		if(recipe[DAT_WOODTYPE].equalsIgnoreCase("MONEY"))
		{
			int wood=CMath.s_int(recipe[DAT_WOOD]);
			wood=adjustWoodRequired(wood,mob);
			CMLib.beanCounter().subtractMoney(mob, landCurrency, wood);
		}

		verb = establishVerb(mob, recipe);
		if(verb.length()==0)
			return false;
		messedUp=!proficiencyCheck(mob,0,auto);
		startStr=L("<S-NAME> start(s) @x1",verb);
		playSound=this.getSoundName();
		duration=getDuration(CMath.s_int(recipe[RCP_TICKS]),mob,CMath.s_int(recipe[RCP_LEVEL]),10);
		poundsOfMatsUsed += woodRequired;

		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),startStr+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
