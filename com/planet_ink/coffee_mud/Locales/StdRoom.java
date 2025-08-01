package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Event;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.EachApplicable.ApplyAffectPhyStats;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
/*
   Copyright 2001-2025 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class StdRoom implements Room
{
	@Override
	public String ID()
	{
		return "StdRoom";
	}

	private static final String DEFAULT_DISPLAY_TEXT=CMLib.lang().L("Standard Room");

	protected String			_roomID				= "";
	protected String			name				= "the room";
	protected String			displayText			= DEFAULT_DISPLAY_TEXT;
	protected String			rawImageName		= null;
	protected String			cachedImageName		= null;
	protected Object			description			= null;
	protected Area				myArea				= null;
	protected PhyStats			phyStats			= (PhyStats) CMClass.getCommon("DefaultPhyStats");
	protected PhyStats			basePhyStats		= (PhyStats) CMClass.getCommon("DefaultPhyStats");
	protected Exit[]			exits				= new Exit[Directions.NUM_DIRECTIONS()];
	protected Room[]			doors				= new Room[Directions.NUM_DIRECTIONS()];
	protected String[]			xtraValues			= null;
	protected boolean			mobility			= true;
	protected GridLocale		gridParent			= null;
	protected int				tickStatus			= Tickable.STATUS_NOT;
	protected long				expirationDate		= 0;
	protected int				atmosphere			= ATMOSPHERE_INHERIT;
	protected int				climask				= CLIMASK_INHERIT;
	protected int				myResource			= -1;
	protected long				lastResourceTime	= 0;
	protected boolean			amDestroyed			= false;
	protected boolean			skyedYet			= false;
	protected volatile short	combatTurnMobIndex	= 0;

	protected final AtomicInteger		roomRecoverMarker	= new AtomicInteger();
	protected SVector<Ability>			affects				= null;
	protected SVector<Behavior>			behaviors			= null;
	protected SVector<ScriptingEngine>	scripts				= null;
	protected SVector<MOB>				inhabitants			= new SVector<MOB>(1);
	protected SVector<Item>				contents			= new SVector<Item>(1);
	protected Room						me					= this;

	@SuppressWarnings("rawtypes")
	protected ApplyAffectPhyStats affectPhyStats 	= new ApplyAffectPhyStats<Physical>(this);
	// base move points and thirst points per round

	public StdRoom()
	{
		super();
		//CMClass.bumpCounter(this,CMClass.CMObjectType.LOCALE);//removed for mem & perf
		xtraValues=CMProps.getExtraStatCodesHolder(this);
		setMovementCost(2); // movement consumption
		setRoomSize(((domainType()&Room.INDOORS)>0)?1:10);
		recoverPhyStats();
	}

	/*
	protected void finalize()
	{
		CMClass.unbumpCounter(this, CMClass.CMObjectType.LOCALE);
	}// removed for mem & perf
	*/

	@Override
	public void initializeClass()
	{
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().getDeclaredConstructor().newInstance();
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdRoom();
	}

	@Override
	public String roomID()
	{
		return _roomID	;
	}

	@Override
	public String Name()
	{
		return name;
	}

	@Override
	public void setName(final String newName)
	{
		name=newName;
	}

	@Override
	public String name()
	{
		if(phyStats().newName()!=null)
			return phyStats().newName();
		return name;
	}

	@Override
	public String name(final MOB viewerMob)
	{
		return name();
	}

	@Override
	public String genericName()
	{
		final String domainName;
		if((domainType()&Room.INDOORS)==0)
			domainName=CMLib.english().startWithAorAn(Room.DOMAIN_OUTDOOR_DESCS[domainType()].toLowerCase());
		else
			domainName=CMLib.english().startWithAorAn(Room.DOMAIN_INDOORS_DESCS[CMath.unsetb(domainType(),Room.INDOORS)].toLowerCase());
		return L("@x1 place",domainName);
	}

	protected void setMovementCost(final int newCost)
	{
		basePhyStats().setWeight(newCost); // movement consumption
		recoverPhyStats();
	}

	protected int getMovementCost()
	{
		return phyStats().weight(); // movement consumption
	}

	protected void setRoomSize(final int newSize)
	{
		basePhyStats().setHeight(newSize);
		recoverPhyStats();
	}

	protected int getRoomSize()
	{
		return phyStats().height();
	}

	@Override
	public int getAtmosphereCode()
	{
		return atmosphere;
	}

	@Override
	public void setAtmosphere(final int resourceCode)
	{
		atmosphere=resourceCode;
	}

	@Override
	public int getAtmosphere()
	{
		if(getGridParent()!=null)
			return getGridParent().getAtmosphere();
		else
		if(getAtmosphereCode()!=ATMOSPHERE_INHERIT)
			return getAtmosphereCode();
		else
			return (myArea==null)?RawMaterial.RESOURCE_AIR:myArea.getAtmosphere();
	}

	@Override
	public String image()
	{
		if(cachedImageName==null)
		{
			if((rawImageName!=null)&&(rawImageName.length()>0))
				cachedImageName=rawImageName;
			else
				cachedImageName=CMLib.protocol().getDefaultMXPImage(this);
		}
		return cachedImageName;
	}

	@Override
	public String rawImage()
	{
		if(rawImageName==null)
			return "";
		return rawImageName;
	}

	@Override
	public void setImage(final String newImage)
	{
		if((newImage==null)||(newImage.trim().length()==0))
			rawImageName=null;
		else
			rawImageName=newImage;
		if((cachedImageName!=null)&&(!cachedImageName.equals(newImage)))
			cachedImageName=null;
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}

	@SuppressWarnings("rawtypes")
	protected void cloneFix(final Room R)
	{
		me=this;
		basePhyStats=(PhyStats)R.basePhyStats().copyOf();
		phyStats=(PhyStats)R.phyStats().copyOf();

		affectPhyStats = new ApplyAffectPhyStats(this);

		contents=new SVector<Item>();
		inhabitants=new SVector<MOB>();
		affects=null;
		behaviors=null;
		scripts=null;
		exits=new Exit[exits.length];
		Arrays.fill(exits, null);
		doors=new Room[doors.length];
		Arrays.fill(doors, null);
		for(int d=R.rawDoors().length-1;d>=0;d--)
		{
			if(R.getRawExit(d)!=null)
				exits[d]=(Exit)R.getRawExit(d).copyOf();
			if(R.rawDoors()[d]!=null)
				doors[d]=R.rawDoors()[d];
		}
		for(int i=0;i<R.numItems();i++)
		{
			final Item I2=R.getItem(i);
			if(I2!=null)
			{
				final Item I=(Item)I2.copyOf();
				I.setOwner(this);
				contents.addElement(I);
			}
		}
		for(int i=0;i<numItems();i++)
		{
			final Item I2=getItem(i);
			if((I2!=null)
			&&(I2.container()!=null)
			&&(!isContent(I2.container())))
			{
				for(int ii=0;ii<R.numItems();ii++)
				{
					if((R.getItem(ii)==I2.container())&&(ii<numItems()))
					{
						I2.setContainer((Container)getItem(ii));
						break;
					}
				}
			}
		}
		for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M2=m.nextElement();
			if(M2.isSavable())
			{
				final MOB M=(MOB)M2.copyOf();
				if(M.getStartRoom()==R)
					M.setStartRoom(this);
				M.setLocation(this);
				inhabitants.addElement(M);
			}
		}
		for(final Enumeration<Ability> a=R.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(!A.canBeUninvoked())
				addEffect((Ability)A.copyOf());
		}
		for(final Enumeration<Behavior> e=R.behaviors();e.hasMoreElements();)
		{
			final Behavior B=e.nextElement();
			addBehavior((Behavior)B.copyOf());
		}
		for(final Enumeration<ScriptingEngine> e=R.scripts();e.hasMoreElements();)
		{
			final ScriptingEngine SE=e.nextElement();
			addScript((ScriptingEngine)SE.copyOf());
		}
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final StdRoom R=(StdRoom)this.clone();
			//CMClass.bumpCounter(R,CMClass.CMObjectType.LOCALE);//removed for mem & perf
			R.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			R.cloneFix(this);
			return R;

		}
		catch(final CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	@Override
	public int domainType()
	{
		return Room.DOMAIN_OUTDOORS_CITY;
	}

	@Override
	public int getClimateTypeCode()
	{
		return climask;
	}

	@Override
	public void setClimateType(final int climask)
	{
		this.climask=climask;
	}

	@Override
	public int getClimateType()
	{
		if(getGridParent()!=null)
			return getGridParent().getClimateType();
		else
		if(getClimateTypeCode()!=CLIMASK_INHERIT)
			return getClimateTypeCode();
		else
			return (myArea==null)?CLIMASK_NORMAL:myArea.getClimateType();
	}

	@Override
	public long expirationDate()
	{
		return expirationDate;
	}

	@Override
	public void setExpirationDate(final long time)
	{
		expirationDate=time;
	}

	@Override
	public void setRawExit(final int direction, final Exit to)
	{
		if((direction<0)||(direction>=exits.length))
			return;
		final Exit E=exits[direction];
		if(E==to)
			return;
		if(E!=null)
			E.exitUsage((short)-1);
		if(to !=null )
		{
			to.exitUsage((short)1);
			exits[direction]=to;
		}
		else
			exits[direction]=null;

		/**
		 * cant be done
		 *
		if((E!=null)&&(E.exitUsage((short)0)==0))
			E.destroy();
		 */
	}

	@Override
	public String displayText()
	{
		return displayText;
	}

	@Override
	public void setDisplayText(final String newDisplayText)
	{
		displayText=newDisplayText;
	}

	@Override
	public String description()
	{
		if(description == null)
		{
			if(CMProps.getBoolVar(CMProps.Bool.ROOMDNOCACHE)&&(roomID().trim().length()>0))
			{
				final String txt=CMLib.database().DBReadRoomDesc(roomID());
				if(txt==null)
				{
					Log.errOut("Unable to recover description for "+roomID()+".");
					return "";
				}
				return txt;
			}
			return "";
		}
		else
		if(description instanceof byte[])
		{
			final byte[] descriptionBytes=(byte[])description;
			if(CMProps.getBoolVar(CMProps.Bool.ROOMDCOMPRESS))
				return CMLib.encoder().decompressString(descriptionBytes);
			return CMStrings.bytesToStr(descriptionBytes);
		}
		else
			return ((String)description);
	}

	@Override
	public void setDescription(final String newDescription)
	{
		if(newDescription.length()==0)
			description=null;
		else
		if(CMProps.getBoolVar(CMProps.Bool.ROOMDCOMPRESS))
			description=CMLib.encoder().compressString(newDescription);
		else
			description=newDescription;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getEnvironmentalMiscTextXML(this,true);
	}

	@Override
	public String miscTextFormat()
	{
		return CMParms.FORMAT_UNDEFINED;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		if(newMiscText.trim().length()>0)
			CMLib.coffeeMaker().unpackEnvironmentalMiscTextXML(this,newMiscText,true);
	}

	@Override
	public void setRoomID(final String newID)
	{
		if((_roomID!=null)&&(!_roomID.equals(newID)))
		{
			_roomID=newID;
			if(myArea!=null)
			{
				// force the re-sort
				myArea.delProperRoom(this);
				myArea.addProperRoom(this);
			}
		}
		else
			_roomID=newID;
	}

	@Override
	public Area getArea()
	{
		if(myArea==null)
			return CMClass.randomArea();
		return myArea;
	}

	@Override
	public void setArea(final Area newArea)
	{
		if(newArea!=myArea)
		{
			if(myArea!=null)
				myArea.delProperRoom(this);
			myArea=newArea;
			if(myArea!=null)
				myArea.addProperRoom(this);
		}
	}

	@Override
	public void setGridParent(final GridLocale room)
	{
		gridParent=room;
	}

	@Override
	public GridLocale getGridParent()
	{
		return gridParent;
	}

	@Override
	public void giveASky(final int depth)
	{
		if(skyedYet)
			return;
		if(depth>1000)
			return;

		final Area A=getArea();
		if(A==null)
			return;
		skyedYet=true;
		if((roomID().length()==0)
		&&(getGridParent()!=null)
		&&(getGridParent().roomID().length()==0))
			return;

		if((doors[Directions.UP]==null)
		&&((domainType()&Room.INDOORS)==0)
		&&(domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(domainType()!=Room.DOMAIN_OUTDOORS_AIR) // prevents InTheAir from having a sky
		&&(CMProps.getIntVar(CMProps.Int.SKYSIZE)!=0))
		{
			Exit upE=null;
			final Exit dnE=CMClass.getExit("StdOpenDoorway");
			if(CMProps.getIntVar(CMProps.Int.SKYSIZE)>0)
				upE=CMClass.getExit("Skyway");
			else
				upE=CMClass.getExit("UnseenWalkway");

			final GridLocale sky=(GridLocale)CMClass.getLocale("EndlessThinSky");
			sky.setRoomID("");
			sky.setArea(getArea());
			doors[Directions.UP]=sky;
			setRawExit(Directions.UP,upE);
			sky.rawDoors()[Directions.DOWN]=this;
			sky.setRawExit(Directions.DOWN,dnE);

			if(!(getArea() instanceof Boardable))
			{
				for(int d=doors.length-1;d>=0;d--)
				{
					if((d!=Directions.UP)
					&&(d!=Directions.DOWN))
					{
						Room thatRoom=doors[d];
						if((thatRoom!=null)&&(getRawExit(d)!=null))
						{
							thatRoom=CMLib.map().getRoom(thatRoom);
							if(thatRoom != null)
							{
								thatRoom.giveASky(depth+1);
								final Room thatSky=thatRoom.rawDoors()[Directions.UP];
								if((thatSky!=null)
								&&(thatSky.roomID().length()==0)
								&&((thatSky instanceof EndlessThinSky)||(thatSky instanceof EndlessSky)))
								{
									Exit xo=getRawExit(d);
									if(xo!=null)
									{
										if(xo.hasADoor())
											xo=dnE;
										sky.rawDoors()[d]=thatSky;
										sky.setRawExit(d,xo);
									}
									final int opDir=Directions.getOpDirectionCode(d);
									xo=thatRoom.getRawExit(opDir);
									if(xo!=null)
									{
										if(xo.hasADoor())
											xo=dnE;
										thatSky.rawDoors()[opDir]=sky;
										thatSky.setRawExit(opDir,xo);
									}
									((GridLocale)thatSky).clearGrid(null);
								}
							}
						}
					}
				}
			}
			sky.clearGrid(null);
		}
	}

	@Override
	public List<Room> getSky()
	{
		final List<Room> skys = new Vector<Room>(1);
		if(!skyedYet)
			return skys;
		final Room skyGridRoom=doors[Directions.UP];
		if(skyGridRoom!=null)
		{
			if(((skyGridRoom.roomID()==null)||(skyGridRoom.roomID().length()==0))
			&&((skyGridRoom instanceof EndlessSky)||(skyGridRoom instanceof EndlessThinSky)))
				skys.add(skyGridRoom);
		}
		return skys;
	}

	@Override
	public void clearSky()
	{
		if(!skyedYet)
			return;
		final Room skyGridRoom=doors[Directions.UP];
		if(skyGridRoom!=null)
		{
			if(((skyGridRoom.roomID()==null)||(skyGridRoom.roomID().length()==0))
			&&((skyGridRoom instanceof EndlessSky)||(skyGridRoom instanceof EndlessThinSky)))
			{
				((GridLocale)skyGridRoom).clearGrid(null);
				doors[Directions.UP]=null;
				setRawExit(Directions.UP,null);
				for(int d=0;d<skyGridRoom.rawDoors().length;d++)
				{
					final Room thatSky=skyGridRoom.rawDoors()[d];
					final int opDir=Directions.getOpDirectionCode(d);
					if((thatSky!=null)&&(thatSky.rawDoors()[opDir]==skyGridRoom))
					{
						thatSky.rawDoors()[opDir]=null;
						thatSky.setRawExit(opDir, null);
					}
					skyGridRoom.rawDoors()[d]=null;
					skyGridRoom.setRawExit(d,null);
				}
				CMLib.map().emptyRoom(skyGridRoom,null,true);
				skyGridRoom.destroy();
				skyedYet=false;
			}
		}
		else
			skyedYet=false;
	}

	@Override
	public List<Integer> resourceChoices()
	{
		return null;
	}

	@Override
	public void setResource(final int resourceCode)
	{
		myResource=resourceCode;
		lastResourceTime= (resourceCode>=0) ? lastResourceTime=System.currentTimeMillis() : 0;
	}

	@Override
	public int myResource()
	{
		if(lastResourceTime!=0)
		{
			if(lastResourceTime<(System.currentTimeMillis()-(30*TimeManager.MILI_MINUTE)))
				setResource(-1);
		}
		if(myResource<0)
		{
			if(resourceChoices()==null)
				setResource(-1);
			else
			{
				final List<Integer> preferredChoices=new ArrayList<Integer>();
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					final Room R=doors[d];
					if((R!=null)
					&&(R.ID().equals(ID()))
					&&(R.resourceChoices()==resourceChoices())
					&&(R instanceof StdRoom)
					&&(((StdRoom)R).myResource>0))
						preferredChoices.add(Integer.valueOf(((StdRoom)R).myResource));
				}
				final IntegerRangeMap<Integer> map = new IntegerRangeMap<Integer>();
				int curTotal=0;
				for(int i=0;i<resourceChoices().size();i++)
				{
					final Integer resource=resourceChoices().get(i);
					final int min = curTotal;
					final int max = min + RawMaterial.CODES.FREQUENCY(resource.intValue());
					map.put(new int[] {min, max}, resource);
					curTotal = max + 1;
				}
				if(preferredChoices.size()>0)
				{
					final int prefAmt = map.getMax() / 4 / preferredChoices.size();
					for(final Integer I : preferredChoices)
					{
						map.put(new int[] {curTotal, curTotal + prefAmt},I);
						curTotal += prefAmt + 1;
					}
				}
				setResource(-1);
				final int theRoll=CMLib.dice().roll(1,map.getMax(),0);
				final Integer resource = map.get(new int[] {theRoll,theRoll});
				setResource(resource.intValue());
			}
		}
		return myResource;
	}

	@Override
	public void toggleMobility(final boolean onoff)
	{
		mobility=onoff;
	}

	@Override
	public boolean getMobility()
	{
		return mobility;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!getArea().okMessage(this,msg))
			return false;

		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_EXPIRE:
			{
				if((gridParent!=null)&&(!gridParent.okMessage(myHost,msg)))
					return false;
				if(!CMLib.map().isClearableRoom(this))
					return false;
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room R2=doors[d];
					if((R2!=null)&&(!CMLib.map().isClearableRoom(R2)))
						return false;
				}
				break;
			}
			case CMMsg.TYP_LEAVE:
				if((!CMLib.flags().allowsMovement(this))||(!getMobility()))
					return false;
				break;
			case CMMsg.TYP_FLEE:
			case CMMsg.TYP_ENTER:
				if((!CMLib.flags().allowsMovement(this))||(!getMobility()))
					return false;
				if(!mob.isMonster())
				{
					final Room R=mob.location();
					if((R!=null)&&(R.getArea()!=getArea()))
					{
						CMLib.factions().updatePlayerFactions(mob,this,false);
						CMLib.achievements().possiblyBumpAchievement(mob, Event.AREAVISIT, 1, new Object[] {getArea(), this});
					}
					giveASky(0);
				}
				break;
			case CMMsg.TYP_AREAAFFECT:
				// obsolete with the area objects
				break;
			case CMMsg.TYP_CAST_SPELL:
			case CMMsg.TYP_DELICATE_HANDS_ACT:
			case CMMsg.TYP_NOISYMOVEMENT:
			case CMMsg.TYP_OK_ACTION:
			case CMMsg.TYP_JUSTICE:
			case CMMsg.TYP_OK_VISUAL:
			case CMMsg.TYP_SNIFF:
				break;
			case CMMsg.TYP_LIST:
			case CMMsg.TYP_BUY:
			case CMMsg.TYP_BID:
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VIEW:
			case CMMsg.TYP_VALUE:
				if(CMLib.coffeeShops().getShopKeeper(this)==null)
				{
					mob.tell(L("You can't shop here."));
					return false;
				}
				break;
			case CMMsg.TYP_SPEAK:
				break;
			case CMMsg.TYP_DIG:
				if(CMLib.map().getExtendedRoomID(this).length()==0)
				{
					mob.tell(L("You can't really dig here."));
					return false;
				}
				switch(this.domainType())
				{
				case Room.DOMAIN_OUTDOORS_DESERT:
				case Room.DOMAIN_OUTDOORS_HILLS:
				case Room.DOMAIN_OUTDOORS_JUNGLE:
				case Room.DOMAIN_OUTDOORS_PLAINS:
				case Room.DOMAIN_OUTDOORS_SWAMP:
				case Room.DOMAIN_OUTDOORS_WOODS:
					break;
				case Room.DOMAIN_OUTDOORS_WATERSURFACE:
				case Room.DOMAIN_OUTDOORS_UNDERWATER:
				{
					if(getRoomInDir(Directions.DOWN)==null)
						break;
				}
				//$FALL-THROUGH$
				default:
					mob.tell(L("You can't really dig here."));
					return false;
				}
				break;
			default:
				if(((msg.targetMajor(CMMsg.MASK_HANDS))||(msg.targetMajor(CMMsg.MASK_MOUTH)))
				&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_MAGIC))
				&&(!(msg.tool() instanceof Ability))
				&&(msg.targetMinor()!=CMMsg.TYP_THROW)
				&&(isInhabitant(msg.source())))
				{
					mob.tell(L("You can't do that here."));
					return false;
				}
				break;
			}
		}

		if(isInhabitant(msg.source()))
		{
			if(!msg.source().okMessage(this,msg))
				return false;
		}
		for(final Enumeration<MOB> m=inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if((M!=msg.source())
			&&(!M.okMessage(this,msg)))
				return false;
		}
		for(final Enumeration<Item> i=items();i.hasMoreElements();)
		{
			final Item I=i.nextElement();
			if(!I.okMessage(this,msg))
				return false;
		}
		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(!A.okMessage(this,msg))
				return false;
		}
		for(final Enumeration<Behavior> b=behaviors();b.hasMoreElements();)
		{
			final Behavior B=b.nextElement();
			if(!B.okMessage(this,msg))
				return false;
		}
		for(final Enumeration<ScriptingEngine> s=scripts();s.hasMoreElements();)
		{
			final ScriptingEngine S=s.nextElement();
			if(!S.okMessage(this,msg))
				return false;
		}

		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Exit thisExit=getRawExit(d);
			if(thisExit!=null)
			{
				if(!thisExit.okMessage(this,msg))
					return false;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		getArea().executeMsg(this,msg);

		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LEAVE:
			{
				if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
					recoverRoomStats();
				break;
			}
			case CMMsg.TYP_FLEE:
			{
				if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
					recoverRoomStats();
				break;
			}
			case CMMsg.TYP_ENTER:
			case CMMsg.TYP_RECALL:
			{
				if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
					recoverRoomStats();
				if((msg.source().playerStats()!=null)
				&&(msg.source().soulMate()==null))
				{
					if(msg.source().playerStats().addRoomVisit(this))
						CMLib.players().bumpPrideStat(msg.source(),PrideStats.PrideStat.ROOMS_EXPLORED, 1);
				}
				break;
			}
			case CMMsg.TYP_LOOK_EXITS:
				if(msg.value()==CMMsg.MASK_OPTIMIZE)
					CMLib.commands().lookAtExitsShort(this, msg.source());
				else
					CMLib.commands().lookAtExits(this, msg.source());
				break;
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				CMLib.commands().handleBeingLookedAt(msg);
				break;
			case CMMsg.TYP_SNIFF:
				CMLib.commands().handleBeingSniffed(msg);
				break;
			case CMMsg.TYP_READ:
				if(CMLib.flags().canBeSeenBy(this,mob))
					mob.tell(L("There is nothing written here."));
				else
					mob.tell(L("You can't see that!"));
				break;
			case CMMsg.TYP_AREAAFFECT:
				// obsolete with the area objects
				break;
			case CMMsg.TYP_DIG:
				if(findItem("HoleInTheGround")==null)
					addItem(CMClass.getBasicItem("HoleInTheGround"));
				break;
			default:
				break;
			}
		}

		// should this really be here?  maybe in space ship area?
		if((msg.othersMinor() == CMMsg.TYP_GRAVITY)
		&&(msg.targetMinor() == CMMsg.NO_EFFECT)
		&&(numInhabitants()>0))
		{
			final CMMsg gmsg = (CMMsg)msg.copyOf();
			gmsg.setTargetCode(msg.othersCode());
			gmsg.setOthersCode(CMMsg.NO_EFFECT);
			final Room me=this;
			eachInhabitant(new EachApplicable<MOB>()
			{
				final Room R=me;
				@Override
				public void apply(final MOB M)
				{
					gmsg.setTarget(M);
					if(R.okMessage(M, gmsg))
						R.send(M, gmsg);
				}
			});
		}

		if(numItems()>0)
		{
			eachItem(new EachApplicable<Item>()
			{
				@Override
				public final void apply(final Item I)
				{
					I.executeMsg(me, msg);
				}
			});
		}

		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Exit E=getRawExit(d);
			if(E!=null)
				E.executeMsg(this,msg);
		}

		if(numBehaviors()>0)
		{
			eachBehavior(new EachApplicable<Behavior>()
			{
				@Override
				public final void apply(final Behavior B)
				{
					B.executeMsg(me, msg);
				}
			});
		}
		if(numScripts()>0)
		{
			eachScript(new EachApplicable<ScriptingEngine>()
			{
				@Override
				public final void apply(final ScriptingEngine S)
				{
					S.executeMsg(me, msg);
				}
			});
		}
		if(numEffects()>0)
		{
			eachEffect(new EachApplicable<Ability>()
			{
				@Override
				public final void apply(final Ability A)
				{
					A.executeMsg(me,msg);
				}
			});
		}

		if(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
		{
			try
			{
				if(CMSecurity.isSaveFlag(CMSecurity.SaveFlag.ROOMMOBS))
				{
					if(roomID().length()==0)
					{
						eachInhabitant(new EachApplicable<MOB>()
						{
							@Override
							public final void apply(final MOB M)
							{
								if((M.isSavable())
								&&(M.getStartRoom()!=me)
								&&(M.getStartRoom()!=null)
								&&(M.getStartRoom().roomID().length()>0))
									M.getStartRoom().bringMobHere(M,false);
							}
						});
					}
				}
				else
				if(CMSecurity.isSaveFlag(CMSecurity.SaveFlag.ROOMSHOPS))
				{
					eachInhabitant(new EachApplicable<MOB>()
					{
						@Override
						public final void apply(final MOB M)
						{
							if((M instanceof ShopKeeper)
							&&(M.isSavable())
							&&(M.getStartRoom()!=me)
							&&(M.getStartRoom()!=null))
								M.getStartRoom().bringMobHere(M,false);
						}
					});
				}
			}
			catch(final NoSuchElementException e)
			{
			}
		}

		if(msg.amITarget(this)
		&&(msg.targetMinor()==CMMsg.TYP_EXPIRE))
		{
			synchronized(CMClass.getSync(("SYNC"+roomID())))
			{
				final LinkedList<DeadBody> deadBodies=new LinkedList<DeadBody>();
				eachItem(new EachApplicable<Item>()
				{
					@Override
					public final void apply(final Item I)
					{
						if((I instanceof DeadBody)
						&&(((DeadBody)I).isPlayerCorpse()))
							deadBodies.add((DeadBody)I);
					}
				});
				for(final DeadBody D : deadBodies)
				{
					MOB M=CMLib.players().getLoadPlayer(D.getMobName());
					if(M==null)
						M=D.getSavedMOB();
					if((M!=null)&&(M.getStartRoom()!=null))
					{
						final Room startRoom=CMLib.map().getRoom(M.getStartRoom());
						M.tell(L("Your corpse has been moved to @x1",startRoom.displayText()));
						startRoom.moveItemTo(D);
					}
				}
				if(gridParent!=null)
					gridParent.executeMsg(myHost,msg);
				else
				if((roomID().length()>0)
				&&((getArea()==null)||(!CMath.bset(getArea().flags(), Area.FLAG_INSTANCE_CHILD)))
				&&(CMSecurity.isSaveFlag(CMSecurity.SaveFlag.ROOMMOBS)
					||CMSecurity.isSaveFlag(CMSecurity.SaveFlag.ROOMITEMS)
					||CMSecurity.isSaveFlag(CMSecurity.SaveFlag.ROOMSHOPS)))
				{
					final ArrayList<MOB> shopmobs=new ArrayList<MOB>(1);
					final ArrayList<Item> bodies=new ArrayList<Item>(1);
					if(CMSecurity.isSaveFlag(CMSecurity.SaveFlag.ROOMMOBS))
					{
						if(CMProps.isState(CMProps.HostState.SHUTTINGDOWN))
							CMLib.threads().rejuv(this,Tickable.TICKID_MOB);
						eachInhabitant(new EachApplicable<MOB>()
						{
							@Override
							public final void apply(final MOB M)
							{
								if(M.isSavable())
								{
									M.setStartRoom(me);
									M.text(); // this permanizes his current state
								}
							}
						});
						CMLib.database().DBUpdateMOBs(this);
					}
					else
					if(CMSecurity.isSaveFlag(CMSecurity.SaveFlag.ROOMSHOPS))
					{
						if(CMProps.isState(CMProps.HostState.SHUTTINGDOWN))
							CMLib.threads().rejuv(this,Tickable.TICKID_MOB);
						eachInhabitant(new EachApplicable<MOB>()
						{
							@Override
							public final void apply(final MOB M)
							{
								if((M.isSavable())
								&&(M instanceof ShopKeeper)
								&&(M.getStartRoom()==me))
									shopmobs.add(M);
							}
						});
						if(!shopmobs.isEmpty())
							CMLib.database().DBUpdateTheseMOBs(this,shopmobs);
					}
					// never else
					if(CMSecurity.isSaveFlag(CMSecurity.SaveFlag.ROOMITEMS))
					{
						if(CMProps.isState(CMProps.HostState.SHUTTINGDOWN))
							CMLib.threads().rejuv(this,Tickable.TICKID_ROOM_ITEM_REJUV);
						eachItem(new EachApplicable<Item>()
						{
							@Override public final void apply(final Item I)
							{
								if(I instanceof DeadBody)
									bodies.add(I);
							}
						});
						for(int i=0;i<bodies.size();i++)
							bodies.get(i).destroy();
						CMLib.database().DBUpdateItems(this);
					}
				}
				final Area A=getArea();
				final String roomID=roomID();
				setGridParent(null);
				if(!CMProps.isState(CMProps.HostState.SHUTTINGDOWN))
				{
					CMLib.map().emptyRoom(this,null,true);
					destroy();
					if(roomID.length()>0)
						A.addProperRoomnumber(roomID);
				}
			}
		}
	}

	@Override
	public void startItemRejuv()
	{
		eachItem(new EachApplicable<Item>()
		{
			@Override
			public final void apply(final Item item)
			{
				if(item.container()==null)
				{
					final ItemTicker I=(ItemTicker)CMClass.getAbility("ItemRejuv");
					I.unloadIfNecessary(item);
					if((item.phyStats().rejuv()!=PhyStats.NO_REJUV)
					&&(item.phyStats().rejuv()>0))
						I.loadMeUp(item,me);
				}
			}
		});
	}

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		tickStatus=Tickable.STATUS_START;
		if((myArea != null) && (myArea.amDestroyed()))
		{
			Log.errOut("Destroying "+CMLib.map().getExtendedRoomID(this)+" because of destroyed area.");
			destroy();
			return false;
		}
		if(tickID==Tickable.TICKID_ROOM_BEHAVIOR)
		{
			if((numBehaviors()<=0)&&(numScripts()<=0))
				return false;
			tickStatus=Tickable.STATUS_BEHAVIOR;
			eachBehavior(new EachApplicable<Behavior>()
			{
				@Override
				public final void apply(final Behavior B)
				{
					B.tick(ticking, tickID);
				}
			});
			tickStatus=Tickable.STATUS_SCRIPT;
			eachScript(new EachApplicable<ScriptingEngine>()
			{
				@Override
				public final void apply(final ScriptingEngine S)
				{
					S.tick(ticking, tickID);
				}
			});
		}
		else
		{
			tickStatus=Tickable.STATUS_AFFECT;
			if(numEffects()>0)
			{
				eachEffect(new EachApplicable<Ability>()
				{
					@Override
					public final void apply(final Ability A)
					{
						if(!A.tick(ticking,tickID))
							A.unInvoke();
					}
				});
			}
		}
		tickStatus=Tickable.STATUS_NOT;
		return !amDestroyed();
	}

	@Override
	public PhyStats phyStats()
	{
		return phyStats;
	}

	@Override
	public PhyStats basePhyStats()
	{
		return basePhyStats;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void recoverPhyStats()
	{
		basePhyStats.copyInto(phyStats);
		final Area myArea=getArea();
		if(myArea!=null)
			myArea.affectPhyStats(this,phyStats());

		eachEffect(affectPhyStats);
		eachItem(affectPhyStats);
		eachInhabitant(affectPhyStats);
	}

	private final static EachApplicable<Item> recoverRoomStatsItemApplicable=new EachApplicable.ApplyRecoverPhyStats<Item>();

	private final static EachApplicable<MOB> recoverRoomStatsInhabitantApplicable=new EachApplicable<MOB>()
	{
		@Override
		public final void apply(final MOB M)
		{
			M.recoverCharStats();
			M.recoverPhyStats();
			M.recoverMaxState();
		}
	};

	private void reallyRecoverRoomStats()
	{
		recoverPhyStats();
		eachInhabitant(recoverRoomStatsInhabitantApplicable);
		for(final Exit X : exits)
		{
			if(X!=null)
				X.recoverPhyStats();
		}
		eachItem(recoverRoomStatsItemApplicable);
	}

	@Override
	public void recoverRoomStats()
	{
		if(roomRecoverMarker.addAndGet(1)!=1)
			return;
		try
		{
			reallyRecoverRoomStats();
			if(roomRecoverMarker.addAndGet(-1)>0)
				reallyRecoverRoomStats();
		}
		finally
		{
			roomRecoverMarker.set(0);
		}
	}

	@Override
	public void setBasePhyStats(final PhyStats newStats)
	{
		basePhyStats=(PhyStats)newStats.copyOf();
	}

	private final static int phyStatsMaskOut = ~(PhyStats.IS_DARK|PhyStats.IS_LIGHTSOURCE|PhyStats.IS_SLEEPING|PhyStats.IS_HIDDEN|PhyStats.IS_SWIMMING|PhyStats.IS_NOT_SEEN);

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		getArea().affectPhyStats(affected,affectableStats);
		final int disposition=phyStats().disposition() & phyStatsMaskOut;
		if(disposition>0)
			affectableStats.setDisposition(affectableStats.disposition()|disposition);
		eachEffect(new EachApplicable<Ability>()
		{
			@Override
			public final void apply(final Ability A)
			{
				if(A.bubbleAffect())
					A.affectPhyStats(affected,affectableStats);
			}
		});
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
		getArea().affectCharStats(affectedMob,affectableStats);
		eachEffect(new EachApplicable<Ability>()
		{
			@Override
			public final void apply(final Ability A)
			{
				if(A.bubbleAffect())
					A.affectCharStats(affectedMob,affectableStats);
			}
		});
	}

	@Override
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
		getArea().affectCharState(affectedMob,affectableMaxState);
		eachEffect(new EachApplicable<Ability>()
		{
			@Override
			public final void apply(final Ability A)
			{
				if(A.bubbleAffect())
					A.affectCharState(affectedMob,affectableMaxState);
			}
		});
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public String displayText(final MOB mob)
	{
		return CMLib.commands().parseVaries(mob,getArea(),this,displayText());
	}

	@Override
	public String description(final MOB mob)
	{
		return CMLib.commands().parseVaries(mob,getArea(),this,description());
	}

	@Override
	public void bringMobHere(final MOB mob, final boolean andFollowers)
	{
		if(mob==null)
			return;
		final Room oldRoom=mob.location();
		if(oldRoom!=null)
			oldRoom.delInhabitant(mob);
		if(!isInhabitant(mob))
			addInhabitant(mob);
		mob.setLocation(this);

		if((andFollowers)&&(oldRoom!=null))
		{
			for(final Enumeration<Pair<MOB,Short>> f=mob.followers();f.hasMoreElements();)
			{
				final MOB folM=f.nextElement().first;
				if((folM.location()==oldRoom)
				&&(folM != mob)
				&&(oldRoom != this))
					bringMobHere(folM,true);
			}
		}
		final Rideable RI=mob.riding();
		if((RI!=null)&&(CMLib.map().roomLocation(RI)==oldRoom))
		{
			if((RI.isMobileRideBasis())
			&&((!(RI instanceof Item))||(!CMLib.flags().isMobile(RI))))
			{
				if(RI instanceof MOB)
					bringMobHere((MOB)RI,andFollowers);
				else
				if(RI instanceof Item)
				{
					if(andFollowers)
						moveItemTo((Item)RI,ItemPossessor.Expire.Player_Drop,Move.Followers);
					else
						moveItemTo((Item)RI,ItemPossessor.Expire.Player_Drop);
				}
				// refuse is good for above, since mostly moving player stuff around
			}
			else
				mob.setRiding(null);
		}
		if((oldRoom!=null)
		&&(mob instanceof Rideable)
		&&(oldRoom!=this))
		{
			for(final Enumeration<Rider> r=((Rideable)mob).riders();r.hasMoreElements();)
			{
				final Rider RR=r.nextElement();
				if(CMLib.map().roomLocation(RR)==oldRoom)
				{
					if(((Rideable)mob).isMobileRideBasis())
					{
						if((RR instanceof MOB)
						&&(andFollowers)
						&&(RR!=mob))
							bringMobHere((MOB)RR,andFollowers);
						else
						if(RR instanceof Item)
						{
							if(andFollowers)
								moveItemTo((Item)RR,ItemPossessor.Expire.Player_Drop,Move.Followers);
							else
								moveItemTo((Item)RR,ItemPossessor.Expire.Player_Drop);
							// refuse is good for above, since mostly moving player stuff around
						}
					}
					else
						RR.setRiding(null);
				}
			}
		}
		if(oldRoom!=null)
			oldRoom.recoverRoomStats();
		recoverRoomStats();
	}

	@Override
	public void moveItemTo(final Item container)
	{
		moveItemTo(container, Expire.Never);
	}

	@Override
	public void moveItemTo(final Item item, final Expire expire, final Move... moveFlags)
	{
		if(item==null)
			return;
		final ItemPossessor o;
		synchronized(item)
		{
			o=item.owner();
		}
		if(o==null)
			return;

		List<Item> V=new ArrayList<Item>();
		if(item instanceof Container)
			V=((Container)item).getDeepContents();
		o.delItem(item);
		if(o.isContent(item))
			o.delItem(item);

		addItem(item, expire);
		for(int v=0;v<V.size();v++)
		{
			final Item i2=V.get(v);
			o.delItem(i2);
			if(o.isContent(i2))
				o.delItem(i2);
			addItem(i2);
		}
		item.setContainer(null);

		final Rideable RI=item.riding();
		if((RI!=null)&&(o instanceof Room)&&(CMLib.map().roomLocation(RI)==o))
		{
			if((RI.isMobileRideBasis())
			&&((!(RI instanceof Item))||(!CMLib.flags().isMobile(RI))))
			{
				if(RI instanceof MOB)
					bringMobHere((MOB)RI,true);
				else
				if(RI instanceof Item)
					moveItemTo((Item)RI,null,ItemPossessor.Move.Followers);
			}
			else
				item.setRiding(null);
		}
		if(CMParms.contains(moveFlags, Move.Followers)
		&&(o instanceof Room)
		&&(item instanceof Rideable)
		&&(o!=this))
		{
			Rider RR=null;
			for(int r=0;r<((Rideable)item).numRiders();r++)
			{
				RR=((Rideable)item).fetchRider(r);
				if(CMLib.map().roomLocation(RR)==o)
				{
					if((((Rideable)item).isMobileRideBasis())
					&&(!CMLib.flags().isMobile(item)))
					{
						if(RR instanceof MOB)
							bringMobHere((MOB)RR,true);
						else
						if(RR instanceof Item)
							moveItemTo((Item)RR,ItemPossessor.Expire.Player_Drop,ItemPossessor.Move.Followers);
					}
					else
						RR.setRiding(null);
				}
			}
		}

		if(!CMParms.contains(moveFlags, Move.Optimize))
		{
			if(o instanceof Room)
				((Room)o).recoverRoomStats();
			else
			if(o instanceof MOB)
			{
				((MOB)o).recoverCharStats();
				((MOB)o).recoverPhyStats();
				((MOB)o).recoverMaxState();
			}
			recoverRoomStats();
		}
	}

	@Override
	public int getReverseDir(final int direction)
	{
		if((direction<0)||(direction>=Directions.NUM_DIRECTIONS()))
			return -1;
		final Room opRoom=getRoomInDir(direction);
		if(opRoom!=null)
		{
			if(direction == Directions.GATE)
				return direction;
			final int formalOpDir=Directions.getOpDirectionCode(direction);
			if(opRoom.rawDoors()[formalOpDir]==this)
				return formalOpDir;
			if(opRoom.getRoomInDir(formalOpDir)==this)
				return formalOpDir;
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				if(opRoom.rawDoors()[d]==this)
					return d;
			}
			return formalOpDir;
		}
		return -1;
	}

	@Override
	public Exit getReverseExit(final int direction)
	{
		final int opDir=getReverseDir(direction);
		if((opDir<0)||(opDir>=Directions.NUM_DIRECTIONS()))
			return null;
		final Room opRoom=getRoomInDir(direction);
		if(opRoom!=null)
			return opRoom.getExitInDir(opDir);
		return null;
	}

	@Override
	public Exit getPairedExit(final int direction)
	{
		final Exit opExit=getReverseExit(direction);
		final Exit myExit=getExitInDir(direction);
		if((myExit==null)||(opExit==null))
			return null;
		if(myExit.hasADoor()!=opExit.hasADoor())
			return null;
		return opExit;
	}

	@Override
	public Room prepareRoomInDir(final Room R, final int direction)
	{
		if(amDestroyed)
		{
			if(roomID().length()>0)
			{
				final Room thinMeR=CMClass.getLocale("ThinRoom");
				thinMeR.setRoomID(roomID());
				thinMeR.setArea(myArea);
				if(R.rawDoors()[direction]==this)
					R.rawDoors()[direction]=thinMeR;
				return thinMeR.prepareRoomInDir(R,direction);
			}
			return null;
		}
		if(expirationDate()!=0)
			setExpirationDate(System.currentTimeMillis()+WorldMap.ROOM_EXPIRATION_MILLIS);
		return this;
	}

	@Override
	public Room getRoomInDir(final int direction)
	{
		if((direction<0)||(direction>=doors.length)||(amDestroyed))
			return null;
		Room nextRoom=doors[direction];
		if(gridParent!=null)
			nextRoom=gridParent.prepareGridLocale(this,nextRoom,direction);
		if(nextRoom!=null)
		{
			nextRoom=nextRoom.prepareRoomInDir(this,direction);
			if((nextRoom!=null)&&(nextRoom.amDestroyed()))
				return null;
		}
		return nextRoom;
	}

	@Override
	public Exit getExitInDir(final int direction)
	{
		if((direction<0)||(direction>=exits.length))
			return null;
		if((gridParent!=null)&&(getRawExit(direction)==null))
			getRoomInDir(direction);
		return getRawExit(direction);
	}

	protected void sendAndExec(final MOB source, final CMMsg msg)
	{
		if((Log.debugChannelOn())&&(CMSecurity.isDebugging(CMSecurity.DbgFlag.MESSAGES)))
			Log.debugOut("StdRoom",msg.source().ID()+":"+msg.sourceCode()+":"+msg.sourceMessage()+"/"+((msg.target()!=null)?msg.target().ID():"null")+":"+msg.targetCode()+":"+msg.targetMessage()+"/"+((msg.tool()!=null)?msg.tool().ID():"null")+"/"+msg.othersCode()+":"+msg.othersMessage());
		for(final MOB otherMOB : inhabitants)
		{
			if(otherMOB!=source)
				otherMOB.executeMsg(otherMOB,msg);
		}
		executeMsg(source,msg);
		CMLib.commands().monitorGlobalMessage(this, msg);
	}

	protected void sendTrailermsgs(final MOB source, final CMMsg msg, final int depth, final boolean includeSource)
	{
		// now handle trailer msgs
		if(depth<3)
		{
			if(msg.trailerMsgs()!=null)
			{
				for(final CMMsg msg2 : msg.trailerMsgs())
				{
					if((msg!=msg2)
					&&((msg2.target()==null)
					   ||(!(msg2.target() instanceof MOB))
					   ||((!((MOB)msg2.target()).amDead())||(msg2.sourceMinor()==CMMsg.TYP_DEATH)))
					&&(okMessage(source,msg2)))
					{
						if(includeSource)
							source.executeMsg(source,msg2);
						sendAndExec(source,msg2);
						sendTrailermsgs(source, msg2, depth+1, includeSource);
					}
				}
			}
			final List<Runnable> trails=msg.trailerRunnables();
			if((trails!=null)
			&&(trails.size()>0))
			{
				for(final Runnable r : trails)
					CMLib.threads().executeRunnable(r);
			}
		}
	}

	@Override
	public void send(final MOB source, final CMMsg msg)
	{
		source.executeMsg(source,msg);
		sendAndExec(source,msg);
		sendTrailermsgs(source, msg, 0, true);
	}

	@Override
	public void sendOthers(final MOB source, final CMMsg msg)
	{
		sendAndExec(source,msg);
		sendTrailermsgs(source, msg, 0, false);
	}

	@Override
	public void showHappens(final int allCode, final String allMessage)
	{
		final MOB everywhereMOB=CMLib.map().getFactoryMOB(this);
		final CMMsg msg=CMClass.getMsg(everywhereMOB,null,null,allCode,allCode,allCode,allMessage);
		sendOthers(everywhereMOB,msg);
		everywhereMOB.destroy();
	}

	@Override
	public void showHappens(final int allCode, final Environmental like, final String allMessage)
	{
		final MOB everywhereMOB=CMClass.getFactoryMOB();
		everywhereMOB.setName(like.name());
		if(like instanceof Physical)
			everywhereMOB.setBasePhyStats(((Physical)like).phyStats());
		everywhereMOB.setLocation(this);
		everywhereMOB.recoverPhyStats();
		final CMMsg msg=CMClass.getMsg(everywhereMOB,null,null,allCode,allCode,allCode,allMessage);
		send(everywhereMOB,msg);
		everywhereMOB.destroy();
	}

	@Override
	public boolean show(final MOB source, final Environmental target, final int allCode, final String allMessage)
	{
		final CMMsg msg=CMClass.getMsg(source,target,null,allCode,allCode,allCode,allMessage);
		if((!CMath.bset(allCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
			return false;
		send(source,msg);
		return true;
	}

	@Override
	public boolean show(final MOB source, final Environmental target, final Environmental tool, final int allCode, final String allMessage)
	{
		final CMMsg msg=CMClass.getMsg(source,target,tool,allCode,allCode,allCode,allMessage);
		if((!CMath.bset(allCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
			return false;
		send(source,msg);
		return true;
	}

	@Override
	public boolean show(final MOB source,
						final Environmental target,
						final Environmental tool,
						final int srcCode,
						final int tarCode,
						final int othCode,
						final String allMessage)
	{
		final CMMsg msg=CMClass.getMsg(source,target,tool,srcCode,tarCode,othCode,allMessage);
		if((!CMath.bset(srcCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
			return false;
		send(source,msg);
		return true;
	}

	@Override
	public boolean show(final MOB source,
						final Environmental target,
						final Environmental tool,
						final int allCode,
						final String srcMessage,
						final String tarMessage,
						final String othMessage)
	{
		final CMMsg msg=CMClass.getMsg(source,target,tool,allCode,srcMessage,allCode,tarMessage,allCode,othMessage);
		if((!CMath.bset(allCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
			return false;
		send(source,msg);
		return true;
	}

	@Override
	public boolean show(final MOB source,
						final Environmental target,
						final Environmental tool,
						final int srcCode,
						final String srcMessage,
						final int tarCode,
						final String tarMessage,
						final int othCode,
						final String othMessage)
	{
		final CMMsg msg=CMClass.getMsg(source,target,tool,srcCode,srcMessage,tarCode,tarMessage,othCode,othMessage);
		if((!CMath.bset(srcCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
			return false;
		send(source,msg);
		return true;
	}

	@Override
	public boolean showOthers(final MOB source,
							  final Environmental target,
							  final int allCode,
							  final String allMessage)
	{
		final CMMsg msg=CMClass.getMsg(source,target,null,allCode,allCode,allCode,allMessage);
		if((!CMath.bset(allCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
			return false;
		sendAndExec(source,msg);
		sendTrailermsgs(source, msg, 0, false);
		return true;
	}

	@Override
	public boolean showOthers(final MOB source,
							  final Environmental target,
							  final Environmental tool,
							  final int allCode,
							  final String allMessage)
	{
		final CMMsg msg=CMClass.getMsg(source,target,tool,allCode,allCode,allCode,allMessage);
		if((!CMath.bset(allCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
			return false;
		sendAndExec(source,msg);
		sendTrailermsgs(source, msg, 0, false);
		return true;
	}

	@Override
	public boolean showSource(final MOB source,
							  final Environmental target,
							  final int allCode,
							  final String allMessage)
	{
		final CMMsg msg=CMClass.getMsg(source,target,null,allCode,allCode,allCode,allMessage);
		if((!CMath.bset(allCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
			return false;
		source.executeMsg(source,msg);
		return true;
	}

	@Override
	public boolean showSource(final MOB source,
							  final Environmental target,
							  final Environmental tool,
							  final int allCode,
							  final String allMessage)
	{
		final CMMsg msg=CMClass.getMsg(source,target,tool,allCode,allCode,allCode,allMessage);
		if((!CMath.bset(allCode,CMMsg.MASK_ALWAYS))&&(!okMessage(source,msg)))
			return false;
		source.executeMsg(source,msg);
		return true;
	}

	@Override
	public Exit getRawExit(final int dir)
	{
		if(dir<exits.length)
			return exits[dir];
		return null;
	}

	@Override
	public Room[] rawDoors()
	{
		return doors;
	}

	@Override
	public Room getRawDoor(final int direction)
	{
		if(direction<doors.length)
			return doors[direction];
		return null;
	}

	@Override
	public void setRawDoor(final int direction, final Room R)
	{
		if(direction<doors.length)
			doors[direction] = R;
	}

	@Override
	public boolean isSavable()
	{
		return ((roomID().length()>0)
				&&((getArea()==null)
					|| (!CMath.bset(getArea().flags(),Area.FLAG_INSTANCE_CHILD)))
				&&(CMLib.flags().isSavable(this)));
	}

	@Override
	public void setSavable(final boolean truefalse)
	{
		CMLib.flags().setSavable(this, truefalse);
	}

	@Override
	public void destroy()
	{
		if((phyStats().sensesMask()&PhyStats.SENSE_UNDESTROYABLE)>0)
			return;
		CMLib.map().registerWorldObjectDestroyed(getArea(),this,this);
		delAllEffects(true);
		delAllInhabitants(true);
		delAllBehaviors();
		delAllScripts();
		CMLib.threads().deleteTick(this,-1);
		delAllItems(true);
		if(this instanceof GridLocale)
			((GridLocale)this).clearGrid(null);
		clearSky();
		if((roomID().length()==0)&&(doors!=null))
		{
			Room roomDir=null;
			for(int d=doors.length-1;d>=0;d--)
			{
				roomDir=doors[d];
				if((roomDir!=null)&&(roomDir.rawDoors()!=null))
				{
					for(int d2=roomDir.rawDoors().length-1;d2>=0;d2--)
					{
						if(roomDir.rawDoors()[d2]==this)
						{
							roomDir.rawDoors()[d2]=null;
							roomDir.setRawExit(d2,null);
						}
					}
				}
			}
		}
		rawImageName=null;
		cachedImageName=null;
		setArea(null); // this actually deletes the room from the cache map
		basePhyStats=(PhyStats)CMClass.getCommon("DefaultPhyStats");
		phyStats=basePhyStats;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			setRawExit(d,null);
		Arrays.fill(exits, null);
		Arrays.fill(doors, null);
		affects=null;
		behaviors=null;
		scripts=null;
		contents.setSize(0);
		inhabitants.setSize(0);
		gridParent=null;
		amDestroyed=true;
	}

	@Override
	public final boolean amDestroyed()
	{
		return amDestroyed;
	}

	@Override
	public int getCombatTurnMobIndex()
	{
		return combatTurnMobIndex;
	}

	@Override
	public void setCombatTurnMobIndex(final int index)
	{
		combatTurnMobIndex = (short)index;
	}

	@Override
	public boolean isHere(final Environmental E)
	{
		if(E instanceof Item)
			return isContent((Item)E);
		else
		if(E instanceof MOB)
			return isInhabitant((MOB)E);
		else
		if(E instanceof Exit)
		{
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if(getRawExit(d)==E)
					return true;
			}
		}
		else
		if(E instanceof Room)
		{
			if (E == this)
				return true;
			if (E instanceof Room)
			{
				if (CMLib.map().getExtendedRoomID(this).equals(CMLib.map().getExtendedRoomID((Room) E)))
					return true;
			}
			return false;
		}
		else
		if(E instanceof Ability)
			return fetchEffect(E.ID())!=null;
		else
		if(E instanceof Behavior)
			return fetchBehavior(E.ID())!=null;
		return false;
	}

	@Override
	public MOB fetchRandomInhabitant()
	{
		if(inhabitants.isEmpty())
			return null;
		return fetchInhabitant(CMLib.dice().roll(1,numInhabitants(),-1));
	}

	@Override
	public MOB fetchInhabitant(final String inhabitantID)
	{
		if(inhabitants.isEmpty())
			return null;
		MOB mob=(MOB)CMLib.english().fetchEnvironmental(inhabitants,inhabitantID,true);
		if(mob==null)
			mob=(MOB)CMLib.english().fetchEnvironmental(inhabitants,inhabitantID, false);
		return mob;
	}

	@Override
	public MOB fetchInhabitantExact(final String inhabitantID)
	{
		if(inhabitants.isEmpty())
			return null;
		return (MOB)CMLib.english().fetchEnvironmental(inhabitants,inhabitantID,true);
	}

	private static final ReadOnlyVector<MOB> emptyMOBV=new ReadOnlyVector<MOB>(1);

	@SuppressWarnings("unchecked")

	@Override
	public List<MOB> fetchInhabitants(final String inhabitantID)
	{
		if(inhabitants.isEmpty())
			return emptyMOBV;
		@SuppressWarnings("rawtypes")
		List inhabs=CMLib.english().fetchEnvironmentals(inhabitants,inhabitantID,true);
		if(inhabs.isEmpty())
			inhabs=CMLib.english().fetchEnvironmentals(inhabitants,inhabitantID, false);
		return inhabs;
	}

	@Override
	public void addInhabitant(final MOB mob)
	{
		inhabitants.addElement(mob);
	}

	@Override
	public Enumeration<MOB> inhabitants()
	{
		return inhabitants.elements();
	}

	@Override
	public int numInhabitants()
	{
		return inhabitants.size();
	}

	@Override
	public int numPCInhabitants()
	{
		final Set<MOB> playerInhabitants=CMLib.players().getPlayersHere(this);
		if(playerInhabitants.isEmpty())
			return 0;
		int num=0;
		for(final MOB M : playerInhabitants)
		{
			if((M!=null)&&(M.session()!=null))
				num++;
		}
		return num;
	}

	@Override
	public boolean isInhabitant(final MOB mob)
	{
		return inhabitants.contains(mob);
	}

	@Override
	public MOB fetchInhabitant(final int i)
	{
		try
		{
			return inhabitants.elementAt(i);
		}
		catch(final IndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public void eachInhabitant(final EachApplicable<MOB> applier)
	{
		final List<MOB> inhabitants=this.inhabitants;
		if((inhabitants!=null)&&(!inhabitants.isEmpty()))
		{
			try
			{
				for(int a=0;a<inhabitants.size();a++)
				{
					final MOB M=inhabitants.get(a);
					if(M!=null)
						applier.apply(M);
				}
			}
			catch(final ArrayIndexOutOfBoundsException e)
			{
			}
		}
	}

	@Override
	public void delInhabitant(final MOB mob)
	{
		inhabitants.removeElement(mob);
	}

	@Override
	public void delAllInhabitants(final boolean destroy)
	{
		try
		{
			for(int i=numInhabitants()-1;i>=0;i--)
			{
				final MOB M=fetchInhabitant(i);
				if(M!=null)
				{
					if(destroy || (M.location()==this))
					{
						M.setLocation(null);
						if(destroy && (!M.isPlayer()))
							M.destroy();
					}
				}
			}
			inhabitants.clear();
		}
		catch(final Exception e)
		{
		}
	}

	@Override
	public Exit fetchExit(final String itemID)
	{
		final int dir=CMLib.directions().getGoodDirectionCode(itemID);
		Exit E=null;
		if(dir >= 0)
			E=getExitInDir(dir);
		final List<Exit> exitList=Arrays.asList(exits);
		if(E==null)
			E=CMLib.english().fetchExit(exitList, itemID, true);
		if(E==null)
			E=CMLib.english().fetchExit(exitList, itemID, false);
		if(contents.isEmpty())
			return E;
		if(E==null)
			E=CMLib.english().fetchExit(contents, itemID, true);
		if(E==null)
			E=CMLib.english().fetchExit(contents, itemID, false);
		return E;
	}

	@Override
	public Item findItem(final String itemID)
	{
		if(contents.isEmpty())
			return null;
		Item item=(Item)CMLib.english().fetchEnvironmental(contents,itemID,true);
		if(item==null)
			item=(Item)CMLib.english().fetchEnvironmental(contents,itemID,false);
		return item;
	}

	@Override
	public Enumeration<Item> items()
	{
		return contents.elements();
	}

	@Override
	public Enumeration<Item> itemsRecursive()
	{
		return new MultiEnumeration<Item>()
			.addEnumeration(items())
			.addEnumeration(new Enumeration<Item>()
			{
				private final Enumeration<MOB> curMobEnumeration = inhabitants();
				private volatile Enumeration<Item> curItemEnumeration;

				@Override
				public boolean hasMoreElements()
				{
					boolean hasMore = (curItemEnumeration!=null)&&(curItemEnumeration.hasMoreElements());
					while(!hasMore)
					{
						if((curMobEnumeration == null)||(!curMobEnumeration.hasMoreElements()))
							return false;
						curItemEnumeration=curMobEnumeration.nextElement().items();
						hasMore = (curItemEnumeration!=null)&&(curItemEnumeration.hasMoreElements());
					}
					return hasMore;
				}

				@Override
				public Item nextElement()
				{
					if(!hasMoreElements())
						throw new NoSuchElementException();
					return curItemEnumeration.nextElement();
				}
			});
	}


	@Override
	public Item findItem(final Item goodLocation, final String itemID)
	{
		if(contents.isEmpty())
			return null;
		Item item=CMLib.english().fetchAvailableItem(contents,itemID,goodLocation,Wearable.FILTER_ANY,true);
		if(item==null)
			item=CMLib.english().fetchAvailableItem(contents,itemID,goodLocation,Wearable.FILTER_ANY,false);
		return item;
	}

	@Override
	public List<Item> findItems(final Item goodLocation, final String itemID)
	{
		if(contents.isEmpty())
			return new Vector<Item>(1);
		List<Item> items=CMLib.english().fetchAvailableItems(contents,itemID,goodLocation,Wearable.FILTER_ANY,true);
		if(items.isEmpty())
			items=CMLib.english().fetchAvailableItems(contents,itemID,goodLocation,Wearable.FILTER_ANY,false);
		return items;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<Item> findItems(final String itemID)
	{
		if(contents.isEmpty())
			return new Vector<Item>(1);
		List items=CMLib.english().fetchEnvironmentals(contents,itemID,true);
		if(items.isEmpty())
			items=CMLib.english().fetchEnvironmentals(contents,itemID, false);
		return items;
	}

	@Override
	public void addItem(final Item item, Expire expire)
	{
		if(expire == null)
			expire=Expire.Never;
		switch(expire)
		{
		case Monster_Body:
		case Player_Body:
			insertItemUpTop(item);
			break;
		case Resource:
		case Monster_EQ:
		case Player_Drop:
		case Never:
			addItem(item);
			break;
		case Inheret:
			addItem(item);
			return;
		}
		final long expireMs = expire.getExpirationMilliseconds();
		if(expireMs<=0)
			item.setExpirationDate(0);
		else
			item.setExpirationDate(System.currentTimeMillis()+expireMs);
	}

	protected void insertItemUpTop(final Item item)
	{
		if((item!=null)&&(!item.amDestroyed()))
		{
			item.setOwner(this);
			if(!contents.isEmpty())
				contents.add(0,item);
			else
				contents.add(item);
			item.recoverPhyStats();
		}
	}

	@Override
	public void addItem(final Item item)
	{
		if((item!=null)&&(!item.amDestroyed()))
		{
			item.setOwner(this);
			contents.addElement(item);
			item.recoverPhyStats();
		}
	}

	@Override
	public void delItem(final Item item)
	{
		contents.removeElement(item);
		item.recoverPhyStats();
	}

	@Override
	public void delAllItems(final boolean destroy)
	{
		if((destroy)
		&&(numItems()>0))
		{
			final List<Item> delThese = new LinkedList<Item>();
			delThese.addAll(contents);
			contents.clear();
			for(final Item I : delThese)
			{
				if(I!=null)
				{
					// since were deleting you AND all your peers, no need for Item to do it.
					I.setOwner(null);
					I.destroy();
				}
			}
		}
		contents.clear();
	}

	@Override
	public int numItems()
	{
		return contents.size();
	}

	@Override
	public boolean isContent(final Item item)
	{
		return contents.contains(item);
	}

	@Override
	public Item getItem(final int i)
	{
		try
		{
			return contents.elementAt(i);
		}
		catch(final IndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public void eachItem(final EachApplicable<Item> applier)
	{
		final List<Item> contents=this.contents;
		if((contents!=null)&&(!contents.isEmpty()))
		{
			try
			{
				for(int a=0;a<contents.size();a++)
				{
					final Item I=contents.get(a);
					if(I!=null)
						applier.apply(I);
				}
			}
			catch(final ArrayIndexOutOfBoundsException e)
			{
			}
		}
	}

	@Override
	public Item getRandomItem()
	{
		if(numItems()==0)
			return null;
		return getItem(CMLib.dice().roll(1,numItems(),-1));
	}

	@Override
	public String getContextName(final Environmental E)
	{
		if(E instanceof Exit)
		{
			for(int e=0;e<exits.length;e++)
			{
				if(exits[e]==E)
					return CMLib.directions().getDirectionName(e, CMLib.flags().getDirType(this));
			}
			return E.Name();
		}
		else
		if(E instanceof MOB)
		{
			final String ctxName=CMLib.english().getContextName(inhabitants,E);
			if(ctxName!=null)
				return ctxName;
		}
		else
		if(E instanceof Item)
		{
			final String ctxName=CMLib.english().getContextName(contents,E);
			if(ctxName!=null)
				return ctxName;
		}
		else
		if(E!=null)
			return E.name();
		return "nothing";
	}

	@Override
	public PhysicalAgent fetchFromMOBRoomItemExit(MOB mob, final Item goodLocation, String thingName, final Filterer<Environmental> filter)
	{
		PhysicalAgent found=null;
		if(CMStrings.startsWithIgnoreCase(thingName,"room.")
		||CMStrings.startsWithIgnoreCase(thingName,CMLib.english().removeArticleLead(genericName())+"."))
		{
			thingName = thingName.substring(5);
			mob=null;
		}
		String newThingName=CMLib.lang().preItemParser(thingName);
		if(newThingName!=null)
			thingName=newThingName;
		final boolean mineOnly=(mob!=null)
				&&(CMStrings.startsWithIgnoreCase(thingName, "my"))
				&&(thingName.length()>3)
				&&((thingName.charAt(2)==' ')||(thingName.charAt(2)=='.'));
		if(mineOnly)
			thingName=thingName.trim().substring(3).trim();
		if((mob!=null)&&((filter!=Wearable.FILTER_WORNONLY)))
		{
			found=mob.fetchItem(goodLocation, new Filterer<Environmental>()
			{
				@Override
				public boolean passesFilter(final Environmental obj)
				{
					return filter.passesFilter(obj) && Wearable.FILTER_UNWORNONLY.passesFilter(obj);
				}
			}, thingName);
		}
		if((found==null)&&(!mineOnly))
		{
			found=(Exit)CMLib.english().fetchEnvironmental(Arrays.asList(exits),thingName,true);
			if(found==null)
				found=CMLib.english().fetchAvailableItem(contents,thingName,goodLocation,filter,true);
			if(found==null)
				found=(Exit)CMLib.english().fetchEnvironmental(Arrays.asList(exits),thingName,false);
			if(found==null)
				found=CMLib.english().fetchAvailableItem(contents,thingName,goodLocation,filter,false);

			if((found instanceof Item)  // the smurfy well/gate exception
			&&(goodLocation==null)
			&&(found.displayText().length()==0)
			&&(thingName.indexOf('.')<0))
			{
				PhysicalAgent visibleItem=null;
				visibleItem=(Exit)CMLib.english().fetchEnvironmental(Arrays.asList(exits),thingName,false);
				if(visibleItem==null)
					visibleItem=fetchFromMOBRoomItemExit(null,null,thingName+".2",filter);
				if(visibleItem!=null)
					found=visibleItem;
			}

			if((found!=null)&&(CMLib.flags().canBeSeenBy(found,mob)))
				return found;
			while((found!=null)&&(!CMLib.flags().canBeSeenBy(found,mob)))
			{
				newThingName=CMLib.english().bumpDotContextNumber(thingName,1);
				if(!newThingName.equals(thingName))
				{
					thingName=newThingName;
					found=fetchFromRoomFavorItems(goodLocation, thingName);
				}
				else
					found=null;
			}
		}
		if((mob!=null)&&(found==null)&&((filter!=Wearable.FILTER_UNWORNONLY)))
		{
			found=mob.fetchItem(null, new Filterer<Environmental>()
			{
				@Override
				public boolean passesFilter(final Environmental obj)
				{
					return filter.passesFilter(obj) && Wearable.FILTER_WORNONLY.passesFilter(obj);
				}
			}, thingName);
		}
		if(found==null)
		{
			newThingName=CMLib.lang().failedItemParser(thingName);
			if(newThingName!=null)
				return fetchFromMOBRoomItemExit(mob,goodLocation,newThingName,filter);
		}
		return found;
	}

	@Override
	public PhysicalAgent fetchFromRoomFavorItems(final Item goodLocation, String thingName)
	{
		// def was Wearable.FILTER_UNWORNONLY;
		String newThingName=CMLib.lang().preItemParser(thingName);
		if(newThingName!=null)
			thingName=newThingName;
		PhysicalAgent found=null;
		final int[] contextNumber=new int[]{0};
		if(!contents.isEmpty())
			found=(PhysicalAgent)CMLib.english().fetchAvailable(contents,thingName,goodLocation,Wearable.FILTER_ANY,true,contextNumber);
		if(found==null)
			found=(PhysicalAgent)CMLib.english().fetchAvailable(Arrays.asList(exits),thingName,goodLocation,Wearable.FILTER_ANY,true,contextNumber);
		if((found==null)&&(!inhabitants.isEmpty()))
			found=(PhysicalAgent)CMLib.english().fetchAvailable(inhabitants,thingName,goodLocation,Wearable.FILTER_ANY,true,contextNumber);
		contextNumber[0]=0;
		if((found==null)&&(!contents.isEmpty()))
			found=(PhysicalAgent)CMLib.english().fetchAvailable(contents,thingName,goodLocation,Wearable.FILTER_ANY,false,contextNumber);
		if(found==null)
			found=(PhysicalAgent)CMLib.english().fetchAvailable(Arrays.asList(exits),thingName,goodLocation,Wearable.FILTER_ANY,false,contextNumber);
		if((found==null)&&(!inhabitants.isEmpty()))
			found=(PhysicalAgent)CMLib.english().fetchAvailable(inhabitants,thingName,goodLocation,Wearable.FILTER_ANY,false,contextNumber);

		if((found!=null) // the smurfy well exception
		&&(found instanceof Item)
		&&(goodLocation==null)
		&&(found.displayText().length()==0)
		&&(thingName.indexOf('.')<0))
		{
			final PhysicalAgent visibleItem=fetchFromRoomFavorItems(null,thingName+".2");
			if(visibleItem!=null)
				found=visibleItem;
		}
		if(found==null)
		{
			newThingName=CMLib.lang().failedItemParser(thingName);
			if(newThingName!=null)
				return fetchFromRoomFavorItems(goodLocation,newThingName);
		}
		return found;
	}

	@Override
	public PhysicalAgent fetchFromRoomFavorExits(String thingName)
	{
		String newThingName=CMLib.lang().preItemParser(thingName);
		if(newThingName!=null)
			thingName=newThingName;
		final Item goodLocation=null;
		PhysicalAgent found=null;
		final int dirCode = CMLib.directions().getGoodDirectionCode(thingName);
		final int[] contextNumber=new int[]{0};
		if(dirCode>=0)
			found=getRoomInDir(dirCode);
		if(found==null)
			found=(PhysicalAgent)CMLib.english().fetchAvailable(Arrays.asList(exits),thingName,goodLocation,Wearable.FILTER_ANY,true,contextNumber);
		if((found==null)&&(!contents.isEmpty()))
			found=(PhysicalAgent)CMLib.english().fetchAvailable(contents,thingName,goodLocation,Wearable.FILTER_ANY,true,contextNumber);
		if((found==null)&&(!inhabitants.isEmpty()))
			found=(PhysicalAgent)CMLib.english().fetchAvailable(inhabitants,thingName,goodLocation,Wearable.FILTER_ANY,true,contextNumber);
		contextNumber[0]=0;
		if(found==null)
			found=(PhysicalAgent)CMLib.english().fetchAvailable(Arrays.asList(exits),thingName,goodLocation,Wearable.FILTER_ANY,false,contextNumber);
		if((found==null)&&(!contents.isEmpty()))
			found=(PhysicalAgent)CMLib.english().fetchAvailable(contents,thingName,goodLocation,Wearable.FILTER_ANY,false,contextNumber);
		if((found==null)&&(!inhabitants.isEmpty()))
			found=(PhysicalAgent)CMLib.english().fetchAvailable(inhabitants,thingName,goodLocation,Wearable.FILTER_ANY,false,contextNumber);
		if(found==null)
		{
			newThingName=CMLib.lang().failedItemParser(thingName);
			if(newThingName!=null)
				return fetchFromRoomFavorMOBs(goodLocation,newThingName);
		}
		return found;
	}

	@Override
	public PhysicalAgent fetchFromRoomFavorMOBs(final Item goodLocation, String thingName)
	{
		// def was Wearable.FILTER_UNWORNONLY;
		String newThingName=CMLib.lang().preItemParser(thingName);
		if(newThingName!=null)
			thingName=newThingName;
		final int[] contextNumber = new int[]{0};
		PhysicalAgent found=null;
		if(!inhabitants.isEmpty())
			found=(PhysicalAgent)CMLib.english().fetchAvailable(inhabitants,thingName,goodLocation,Wearable.FILTER_ANY,true,contextNumber);
		if((found==null)&&(!contents.isEmpty()))
			found=(PhysicalAgent)CMLib.english().fetchAvailable(contents,thingName,goodLocation,Wearable.FILTER_ANY,true,contextNumber);
		if(found==null)
			found=(PhysicalAgent)CMLib.english().fetchAvailable(Arrays.asList(exits),thingName,goodLocation,Wearable.FILTER_ANY,true,contextNumber);
		contextNumber[0]=0;
		if((found==null)&&(!inhabitants.isEmpty()))
			found=(PhysicalAgent)CMLib.english().fetchAvailable(inhabitants,thingName,goodLocation,Wearable.FILTER_ANY,false,contextNumber);
		if((found==null)&&(!contents.isEmpty()))
			found=(PhysicalAgent)CMLib.english().fetchAvailable(contents,thingName,goodLocation,Wearable.FILTER_ANY,false,contextNumber);
		if(found==null)
			found=(PhysicalAgent)CMLib.english().fetchAvailable(Arrays.asList(exits),thingName,goodLocation,Wearable.FILTER_ANY,false,contextNumber);
		if(found==null)
		{
			newThingName=CMLib.lang().failedItemParser(thingName);
			if(newThingName!=null)
				return fetchFromRoomFavorMOBs(goodLocation,newThingName);
		}
		return found;
	}

	@Override
	public PhysicalAgent fetchFromMOBRoomFavorsItems(final MOB mob, final Item goodLocation, final String thingName, final Filterer<Environmental> filter)
	{
		return fetchFromMOBRoom(mob,goodLocation,thingName,filter,true);
	}

	@Override
	public PhysicalAgent fetchFromMOBRoomFavorsMOBs(final MOB mob, final Item goodLocation, final String thingName, final Filterer<Environmental> filter)
	{
		return fetchFromMOBRoom(mob,goodLocation,thingName,filter,false);
	}

	protected PhysicalAgent fetchFromMOBRoom(MOB mob, final Item goodLocation, String thingName, final Filterer<Environmental> filter, final boolean favorItems)
	{
		PhysicalAgent found=null;
		if(CMStrings.startsWithIgnoreCase(thingName,"room.")
		||CMStrings.startsWithIgnoreCase(thingName,CMLib.english().removeArticleLead(genericName())+"."))
		{
			thingName = thingName.substring(5);
			mob=null;
		}
		String newThingName=CMLib.lang().preItemParser(thingName);
		if(newThingName!=null)
			thingName=newThingName;
		final boolean mineOnly=(mob!=null)
				&&(CMStrings.startsWithIgnoreCase(thingName, "my"))
				&&(thingName.length()>3)
				&&((thingName.charAt(2)==' ')||(thingName.charAt(2)=='.'));
		if(mineOnly)
			thingName=thingName.trim().substring(3).trim();
		if((mob!=null)&&(favorItems)&&(filter!=Wearable.FILTER_WORNONLY))
		{
			final Filterer<Environmental> mobCheckFilter = new Filterer<Environmental>()
			{
				@Override
				public boolean passesFilter(final Environmental obj)
				{
					return filter.passesFilter(obj) && Wearable.FILTER_UNWORNONLY.passesFilter(obj);
				}
			};
			found=mob.fetchItem(goodLocation, mobCheckFilter, thingName);
			if(found == null) // smurfy well exception -- see below -- this is under favorItems and !wornonly
			{
				// this ugliness allows you do use dot syntax on things on the ground when you have SOME stuff in inventory, but not much
				final int dotNumber=CMLib.english().getContextDotNumber(thingName);
				if(dotNumber > 1)
				{
					String testThingName =  CMLib.english().bumpDotContextNumber(thingName, -(dotNumber-1));
					int numMobHas = 0;
					for(int i=1;i<=dotNumber;i++)
					{
						if(mob.fetchItem(goodLocation, mobCheckFilter, testThingName)==null)
							break;
						numMobHas++;
						testThingName =  CMLib.english().bumpDotContextNumber(testThingName, 1);
					}
					if(dotNumber > numMobHas)
					{
						final int curDotNumber=numMobHas+1;
						final int delta = -(curDotNumber-1) + (dotNumber-numMobHas-1);
						testThingName =  CMLib.english().bumpDotContextNumber(testThingName, delta);
						found = fetchFromRoomFavorItems(goodLocation, testThingName);
						if((found != null)
						&&((filter==null)||(filter.passesFilter(found))))
							return found;
					}
				}
			}
		}
		if((found==null)&&(!mineOnly))
		{
			if(favorItems)
				found=fetchFromRoomFavorItems(goodLocation, thingName);
			else
				found=fetchFromRoomFavorMOBs(goodLocation, thingName);
			if((found!=null)
			&&(CMLib.flags().canBeSeenBy(found,mob)))
				return found;
			while((found!=null)&&(!CMLib.flags().canBeSeenBy(found,mob)))
			{
				newThingName=CMLib.english().bumpDotContextNumber(thingName,1);
				if(!newThingName.equals(thingName))
				{
					thingName=newThingName;
					if(favorItems)
						found=fetchFromRoomFavorItems(goodLocation, thingName);
					else
						found=fetchFromRoomFavorMOBs(goodLocation, thingName);
				}
				else
					found=null;
			}
		}
		if((found==null)
		&&(mob!=null)
		&&(!favorItems)
		&&(filter!=Wearable.FILTER_WORNONLY))
		{
			found=mob.fetchItem(goodLocation, new Filterer<Environmental>()
			{
				@Override
				public boolean passesFilter(final Environmental obj)
				{
					return filter.passesFilter(obj) && Wearable.FILTER_UNWORNONLY.passesFilter(obj);
				}
			}, thingName);
		}
		if((mob!=null)
		&&(found==null)
		&&(filter!=Wearable.FILTER_UNWORNONLY))
		{
			found=mob.fetchItem(null, new Filterer<Environmental>()
			{
				@Override
				public boolean passesFilter(final Environmental obj)
				{
					return filter.passesFilter(obj) && Wearable.FILTER_WORNONLY.passesFilter(obj);
				}
			}, thingName);
		}
		if((mob!=null)&&(found==null))
			found=mob.fetchItem(goodLocation,filter,thingName);
		if(found==null)
		{
			final Directions.DirType dirType=CMLib.flags().getDirType(this);
			for(int d=0;d<exits.length;d++)
			{
				if((exits[d]!=null)
				&&(thingName.equalsIgnoreCase(CMLib.directions().getDirectionName(d,dirType))))
					return getExitInDir(d);
			}
		}
		if(found==null)
		{
			newThingName=CMLib.lang().failedItemParser(thingName);
			if(newThingName!=null)
				return fetchFromMOBRoom(mob,goodLocation,newThingName,filter,favorItems);
		}
		return found;
	}

	@Override
	public int pointsPerMove()
	{
		return getArea().getClimateObj().adjustMovement(getMovementCost(),this);
	}

	protected int baseThirst()
	{
		return 1;
	}

	@Override
	public int thirstPerRound()
	{
		final int derivedClimate=getClimateType();
		int adjustment=0;
		if(CMath.bset(derivedClimate, Places.CLIMASK_HOT))
			adjustment+=1;
		if(CMath.bset(derivedClimate, Places.CLIMASK_WET))
			adjustment-=1;
		if(CMath.bset(derivedClimate, Places.CLIMASK_DRY))
			adjustment+=1;
		if(CMath.bset(derivedClimate, Places.CLIMASK_WINDY))
			adjustment+=1;
		if(getArea().getClimateObj()!=null)
			return getArea().getClimateObj().adjustWaterConsumption(baseThirst()+adjustment,this);
		return 0;
	}

	@Override
	public int minRange()
	{
		return Integer.MIN_VALUE;
	}

	@Override
	public int maxRange()
	{
		return phyStats().height();
	}

	@Override
	public void addEffect(final Ability to)
	{
		if(to==null)
			return;
		if(fetchEffect(to.ID())!=null)
			return;
		if(affects==null)
			affects=new SVector<Ability>(1);
		if(affects.contains(to))
			return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}

	@Override
	public void addNonUninvokableEffect(final Ability to)
	{
		if(to==null)
			return;
		if(fetchEffect(to.ID())!=null)
			return;
		if(affects==null)
			affects=new SVector<Ability>(1);
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}

	@Override
	public void delEffect(final Ability to)
	{
		if(affects==null)
			return;
		if(affects.remove(to))
		{
			to.setAffectedOne(null);
			if(affects.isEmpty())
				affects=new SVector<Ability>(1);
		}
	}

	@Override
	public void eachEffect(final EachApplicable<Ability> applier)
	{
		final List<Ability> affects=this.affects;
		if((affects!=null)&&(!affects.isEmpty()))
		try
		{
			for(int a=0;a<affects.size();a++)
			{
				final Ability A=affects.get(a);
				if(A!=null)
					applier.apply(A);
			}
		}
		catch(final ArrayIndexOutOfBoundsException e)
		{
		}
	}

	@Override
	public void delAllEffects(final boolean unInvoke)
	{
		if(affects==null)
			return;
		for(int a=numEffects()-1;a>=0;a--)
		{
			final Ability A=fetchEffect(a);
			if(A!=null)
			{
				if(unInvoke)
					A.unInvoke();
				A.setAffectedOne(null);
			}
		}
		affects=new SVector<Ability>(1);
	}

	@Override
	public int numEffects()
	{
		if(affects==null)
			return 0;
		return affects.size();
	}

	@SuppressWarnings("unchecked")

	@Override
	public Enumeration<Ability> effects()
	{
		return (affects==null)?EmptyEnumeration.INSTANCE:affects.elements();
	}

	@Override
	public Ability fetchEffect(final int index)
	{
		if(affects==null)
			return null;
		try
		{
			return affects.elementAt(index);
		}
		catch(final IndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public Ability fetchEffect(final String ID)
	{
		if(affects==null)
			return null;
		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A.ID().equals(ID))
				return A;
		}
		return null;
	}

	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	@Override
	public void addBehavior(final Behavior to)
	{
		if(to==null)
			return;
		if(behaviors==null)
			behaviors=new SVector<Behavior>(1);
		for(final Behavior B : behaviors)
		{
			if(B.ID().equals(to.ID()))
				return;
		}
		if(behaviors.isEmpty())
			CMLib.threads().startTickDown(this,Tickable.TICKID_ROOM_BEHAVIOR,1);
		to.startBehavior(this);
		behaviors.addElement(to);
	}

	@Override
	public void delBehavior(final Behavior to)
	{
		if(behaviors==null)
			return;
		if(behaviors.remove(to))
		{
			to.endBehavior(this);
			if(behaviors.isEmpty())
				behaviors=new SVector<Behavior>(1);
			if(((behaviors==null)||(behaviors.isEmpty()))&&((scripts==null)||(scripts.isEmpty())))
				CMLib.threads().deleteTick(this,Tickable.TICKID_ROOM_BEHAVIOR);
		}
	}

	@Override
	public void delAllBehaviors()
	{
		final boolean didSomething=(behaviors!=null)&&(!behaviors.isEmpty());
		if(didSomething)
			behaviors.clear();
		behaviors=null;
		if(didSomething && ((scripts==null)||(scripts.isEmpty())))
			CMLib.threads().deleteTick(this,Tickable.TICKID_ROOM_BEHAVIOR);
	}

	@Override
	public int numBehaviors()
	{
		if(behaviors==null)
			return 0;
		return behaviors.size();
	}

	@SuppressWarnings("unchecked")

	@Override
	public Enumeration<Behavior> behaviors()
	{
		return (behaviors==null)?EmptyEnumeration.INSTANCE:behaviors.elements();
	}

	@Override
	public Behavior fetchBehavior(final int index)
	{
		if(behaviors==null)
			return null;
		try
		{
			return behaviors.elementAt(index);
		}
		catch(final IndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public Behavior fetchBehavior(final String ID)
	{
		if(behaviors==null)
			return null;
		for(final Behavior B : behaviors)
		{
			if(B.ID().equalsIgnoreCase(ID))
				return B;
		}
		return null;
	}

	@Override
	public void eachBehavior(final EachApplicable<Behavior> applier)
	{
		final List<Behavior> behaviors=this.behaviors;
		if((behaviors!=null)&&(!behaviors.isEmpty()))
		try
		{
			for(int a=0;a<behaviors.size();a++)
			{
				final Behavior B=behaviors.get(a);
				if(B!=null)
					applier.apply(B);
			}
		}
		catch(final ArrayIndexOutOfBoundsException e)
		{
		}
	}

	/** Manipulation of the scripts list */
	@Override
	public void addScript(final ScriptingEngine S)
	{
		if(scripts==null)
			scripts=new SVector<ScriptingEngine>(1);
		if(S==null)
			return;
		if(!scripts.contains(S))
		{
			for(final Enumeration<ScriptingEngine> s2=scripts();s2.hasMoreElements();)
			{
				final ScriptingEngine S2=s2.nextElement();
				if(S2.getScript().equalsIgnoreCase(S.getScript()))
					return;
			}
			if(scripts.isEmpty())
				CMLib.threads().startTickDown(this,Tickable.TICKID_ROOM_BEHAVIOR,1);
			scripts.addElement(S);
		}
	}

	@Override
	public void delScript(final ScriptingEngine S)
	{
		if(scripts!=null)
		{
			if(scripts.remove(S))
			{
				if(scripts.isEmpty())
					scripts=new SVector<ScriptingEngine>(1);
				if(((behaviors==null)||(behaviors.isEmpty()))&&((scripts==null)||(scripts.isEmpty())))
					CMLib.threads().deleteTick(this,Tickable.TICKID_ROOM_BEHAVIOR);
			}
		}
	}

	@Override
	public void delAllScripts()
	{
		final boolean didSomething=(scripts!=null)&&(!scripts.isEmpty());
		if(didSomething)
			scripts.clear();
		scripts=null;
		if(didSomething && ((behaviors==null)||(behaviors.isEmpty())))
			CMLib.threads().deleteTick(this,Tickable.TICKID_ROOM_BEHAVIOR);
	}

	@Override
	public int numScripts()
	{
		return (scripts==null)?0:scripts.size();
	}

	@SuppressWarnings("unchecked")

	@Override
	public Enumeration<ScriptingEngine> scripts()
	{
		return (scripts==null)?EmptyEnumeration.INSTANCE:scripts.elements();
	}

	@Override
	public ScriptingEngine fetchScript(final int x)
	{
		try
		{
			return scripts.elementAt(x);
		}
		catch (final Exception e)
		{
		}
		return null;
	}

	@Override
	public void eachScript(final EachApplicable<ScriptingEngine> applier)
	{
		final List<ScriptingEngine> scripts=this.scripts;
		if((scripts!=null)&&(!scripts.isEmpty()))
		try
		{
			for(int a=0;a<scripts.size();a++)
			{
				final ScriptingEngine S=scripts.get(a);
				if(S!=null)
					applier.apply(S);
			}
		}
		catch(final ArrayIndexOutOfBoundsException e)
		{
		}
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(getClass(), str, xs);
	}

	@Override
	public int getSaveStatIndex()
	{
		return (xtraValues == null) ? getStatCodes().length : getStatCodes().length - xtraValues.length;
	}

	protected static final String[]	STDCODES	= { "CLASS", "DISPLAY", "DESCRIPTION", "TEXT", "AFFBEHAV", "IMAGE", "CLIMATE", "ATMOSPHERE", "ROOMID" };
	private static String[]			codes		= null;

	@Override
	public String[] getStatCodes()
	{
		if (codes == null)
			codes = CMProps.getStatCodesList(STDCODES, this);
		return codes;
	}

	@Override
	public boolean isStat(final String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	protected int getCodeNum(final String code)
	{
		return CMParms.indexOf(codes, code.toUpperCase());
	}

	@Override
	public String getStat(final String code)
	{
		switch (getCodeNum(code))
		{
		case 0:
			return CMClass.classID(this);
		case 1:
			return displayText();
		case 2:
			return description();
		case 3:
			return text();
		case 4:
			return CMLib.coffeeMaker().getExtraEnvironmentalXML(this);
		case 5:
			return rawImage();
		case 6:
			return "" + getClimateTypeCode();
		case 7:
			return "" + getAtmosphereCode();
		case 8:
			return ""+CMLib.map().getExtendedRoomID(this);
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		switch (getCodeNum(code))
		{
		case 0:
			return;
		case 1:
			setDisplayText(val);
			break;
		case 2:
			setDescription(val);
			break;
		case 3:
			setMiscText(val);
			break;
		case 4:
		{
			delAllEffects(true);
			delAllBehaviors();
			CMLib.coffeeMaker().unpackExtraEnvironmentalXML(this, CMLib.xml().parseAllXML(val));
			break;
		}
		case 5:
			setImage(val);
			break;
		case 6:
			setClimateType((CMath.s_int(val) < 0) ? -1 : CMath.s_parseBitIntExpression(Places.CLIMATE_DESCS, val));
			break;
		case 7:
		{
			if (CMath.isMathExpression(val))
				setAtmosphere(CMath.s_parseIntExpression(val));
			final int matCode = RawMaterial.CODES.FIND_IgnoreCase(val);
			if (matCode >= 0)
				setAtmosphere(matCode);
			break;
		}
		case 8:
			this.setRoomID(val);
			break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if (!(E instanceof StdRoom))
			return false;
		final String[] codes = getStatCodes();
		for (int i = 0; i < codes.length; i++)
		{
			if((!E.getStat(codes[i]).equals(getStat(codes[i])))
			&&(!codes[i].equals("ROOMID"))
			&&(!codes[i].equals("ATMOSPHERE")))
				return false;
		}
		return true;
	}
}
