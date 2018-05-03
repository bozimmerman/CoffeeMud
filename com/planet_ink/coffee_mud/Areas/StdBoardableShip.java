package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.Places;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.Area.State;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2014-2018 Bo Zimmerman

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
public class StdBoardableShip implements Area, BoardableShip, PrivateProperty
{
	@Override
	public String ID()
	{
		return "StdBoardableShip";
	}

	protected String[]  	xtraValues  	= null;
	protected String		imageName   	= "";
	protected RoomnumberSet properRoomIDSet = null;
	protected String		currency		= "";
	private long			expirationDate  = 0;
	protected boolean   	amDestroyed 	= false;
	protected String		name			= "a sailing ship";
	protected Room			savedDock   	= null;
	protected String		displayText 	= "";
	protected String		description 	= "";
	protected String		miscText		= "";
	protected SVector<Room> myRooms 		= new SVector();
	protected State			flag			= State.ACTIVE;
	protected int			tickStatus  	= Tickable.STATUS_NOT;
	protected String		author  		= ""; // will be used for owner, I guess.
	protected PhyStats  	phyStats		= (PhyStats)CMClass.getCommon("DefaultPhyStats");
	protected PhyStats  	basePhyStats	= (PhyStats)CMClass.getCommon("DefaultPhyStats");
	protected Area 			me			 	= this;
	protected BoardableShip	shipItem		= null;

	protected SVector<Ability>			affects			= new SVector<Ability>(1);
	protected SVector<Behavior> 		behaviors		= new SVector<Behavior>(1);
	protected SVector<ScriptingEngine>	scripts			= new SVector<ScriptingEngine>(1);
	protected SLinkedList<Area>			parents			= new SLinkedList<Area>();
	protected STreeMap<String,String>	blurbFlags		= new STreeMap<String,String>();
	protected List<Pair<Room,Integer>>	shipExitCache	= new SLinkedList<Pair<Room,Integer>>();

	@Override
	public void initializeClass()
	{
	}

	public StdBoardableShip()
	{
		super();
		//CMClass.bumpCounter(this,CMClass.CMObjectType.AREA);
		xtraValues=CMProps.getExtraStatCodesHolder(this);
	}
	
	//protected void finalize(){CMClass.unbumpCounter(this,CMClass.CMObjectType.AREA);}//removed for mem & perf
	
	@Override
	public Room getIsDocked()
	{
		return CMLib.map().getRoom(savedDock);
	}

	@Override
	public void setClimateObj(Climate obj)
	{
	}

	protected Area getShipItemArea()
	{
		return (shipItem != null) ? CMLib.map().areaLocation(shipItem) : null;
	}
	
	protected Room getShipItemRoom()
	{
		return (shipItem != null) ? CMLib.map().roomLocation(shipItem) : null;
	}
	
	@Override
	public Climate getClimateObj()
	{
		final Area shipItemArea = getShipItemArea();
		return ((shipItemArea != null) && (shipItemArea != this)) 
				? shipItemArea.getClimateObj() 
				: CMLib.map().areas().nextElement().getClimateObj();
	}

	@Override
	public void setAuthorID(String authorID)
	{
		author = authorID;
	}

	@Override
	public String getAuthorID()
	{
		return author;
	}

	@Override 
	public TimeClock getTimeObj()
	{
		final Area shipItemArea = getShipItemArea();
		return ((shipItemArea != null) && (shipItemArea != this)) 
				? shipItemArea.getTimeObj() 
				: CMLib.time().globalClock();
	}

	@Override
	public void setDockableItem(Item dockableItem)
	{
		if(dockableItem instanceof BoardableShip)
			shipItem=(BoardableShip)dockableItem;
	}

	@Override
	public void setTimeObj(TimeClock obj)
	{
	}

	@Override
	public void setCurrency(String newCurrency)
	{
		currency = newCurrency;
	}

	@Override
	public String getCurrency()
	{
		return currency;
	}

	@Override
	public long expirationDate()
	{
		return expirationDate;
	}

	@Override
	public void setExpirationDate(long time)
	{
		expirationDate = time;
	}

	@Override 
	public int getAtmosphereCode() 
	{
		final Room shipItemRoom = getShipItemRoom();
		return ((shipItemRoom != null)&&(shipItemRoom.getArea()!=this)) 
				? shipItemRoom.getAtmosphereCode() 
				: RawMaterial.RESOURCE_AIR;
	}
	
	@Override
	public void setAtmosphere(int resourceCode)
	{
	}

	@Override
	public Item getShipItem()
	{
		return (shipItem instanceof Item) ? (Item)shipItem : null;
	}
	
	@Override 
	public int getAtmosphere()
	{
		final Room shipItemRoom = getShipItemRoom();
		return ((shipItemRoom != null)&&(shipItemRoom.getArea()!=this))
				? shipItemRoom.getAtmosphere() 
				: RawMaterial.RESOURCE_AIR;
	}
	
	@Override
	public long flags()
	{
		return 0;
	}

	@Override
	public void destroy()
	{
		CMLib.map().registerWorldObjectDestroyed(this,null,this);
		phyStats=(PhyStats)CMClass.getCommon("DefaultPhyStats");
		basePhyStats=phyStats;
		miscText=null;
		imageName=null;
		affects=null;
		behaviors=null;
		scripts=null;
		author=null;
		currency=null;
		parents=new SLinkedList<Area>();
		amDestroyed=true;
	}
	
	@Override
	public boolean amDestroyed()
	{
		return amDestroyed;
	}

	@Override
	public boolean isSavable()
	{
		return ((!amDestroyed)
				&& (!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
				&& (CMLib.flags().isSavable(this)));
	}
	
	@Override
	public void setSavable(boolean truefalse)
	{
		CMLib.flags().setSavable(this, truefalse);
	}

	@Override 
	public int getClimateTypeCode()
	{
		final Room shipItemRoom = getShipItemRoom();
		return ((shipItemRoom != null)&&(shipItemRoom.getArea()!=this))
				? shipItemRoom.getClimateTypeCode() 
				: CLIMASK_NORMAL;
	}
	
	@Override 
	public int getClimateType() 
	{
		final Room shipItemRoom = getShipItemRoom();
		return ((shipItemRoom != null)&&(shipItemRoom.getArea()!=this))
				? shipItemRoom.getClimateType() 
				: CLIMASK_NORMAL;
	}
	
	@Override
	public void setClimateType(int newClimateType)
	{
	}

	@Override
	public String name()
	{
		if(phyStats().newName()!=null)
			return phyStats().newName();
		return name;
	}

	@Override
	public void setName(String newName)
	{
		name=newName;
		CMLib.map().renamedArea(this);
	}

	@Override
	public String Name()
	{
		return name;
	}

	@Override
	public void renameShip(String newName)
	{
		final String oldName=Name();
		setName(newName);
		if(myRooms.size()>0)
			CMLib.map().renameRooms(this, oldName, this.myRooms);
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

	@Override
	public void recoverPhyStats()
	{
		basePhyStats.copyInto(phyStats);
		eachEffect(new EachApplicable<Ability>()
		{ 
			@Override
			public final void apply(final Ability A)
			{
				if(A!=null)
					A.affectPhyStats(me,phyStats);
			}
		});
	}

	@Override
	public Area getShipArea()
	{
		return this;
	}

	@Override
	public void setShipArea(String xml)
	{
	}

	@Override
	public void setBasePhyStats(PhyStats newStats)
	{
		basePhyStats=(PhyStats)newStats.copyOf();
	}
	
	@Override 
	public int getThemeCode() 
	{
		final Area shipItemArea = getShipItemArea();
		return ((shipItemArea != null) && (shipItemArea != this)) 
				? shipItemArea.getThemeCode() 
				: Area.THEME_ALLTHEMES;
	}
	
	@Override
	public int getTheme()
	{
		final Area shipItemArea = getShipItemArea();
		return ((shipItemArea != null) && (shipItemArea != this))
				? shipItemArea.getTheme() 
				: Area.THEME_ALLTHEMES;
	}
	
	@Override
	public void setTheme(int level)
	{
	}

	@Override
	public String image()
	{
		return imageName;
	}

	@Override
	public String rawImage()
	{
		return imageName;
	}

	@Override
	public void setImage(String newImage)
	{
		imageName = newImage;
	}

	@Override 
	public String getHomePortID() 
	{ 
		return this.shipItem != null ? shipItem.getHomePortID() : ""; 
	}

	@Override 
	public void setHomePortID(String portID) 
	{ 
		if(this.shipItem != null)
			this.shipItem.setHomePortID(portID);
	}
	
	@Override
	public String getArchivePath()
	{
		return "";
	}

	@Override
	public void setArchivePath(String pathFile)
	{
	}

	@Override
	public void setAreaState(State newState)
	{
		if((newState==State.ACTIVE)&&(!CMLib.threads().isTicking(this,Tickable.TICKID_AREA)))
			CMLib.threads().startTickDown(this,Tickable.TICKID_AREA,1);
		else
		if((newState==State.STOPPED)&&(CMLib.threads().isTicking(this,Tickable.TICKID_AREA)))
			CMLib.threads().deleteTick(this, Tickable.TICKID_AREA);
		flag=newState;
	}

	@Override
	public State getAreaState()
	{
		return flag;
	}

	@Override
	public boolean amISubOp(String username)
	{
		return false;
	}

	@Override
	public String getSubOpList()
	{
		return "";
	}

	@Override
	public void setSubOpList(String list)
	{
	}

	@Override
	public void addSubOp(String username)
	{
	}

	@Override
	public void delSubOp(String username)
	{
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdBoardableShip();
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}
	
	protected void cloneFix(StdBoardableShip ship)
	{
		me=this;
		basePhyStats=(PhyStats)ship.basePhyStats().copyOf();
		phyStats=(PhyStats)ship.phyStats().copyOf();

		affects=new SVector<Ability>(1);
		behaviors=new SVector<Behavior>(1);
		scripts=new SVector<ScriptingEngine>(1);
		parents=new SLinkedList<Area>();
		parents.addAll(ship.parents);
		for(final Enumeration<Behavior> e=ship.behaviors();e.hasMoreElements();)
		{
			final Behavior B=e.nextElement();
			if(B!=null)
				behaviors.addElement(B);
		}
		for(final Enumeration<Ability> a=ship.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null)
				affects.addElement((Ability)A.copyOf());
		}
		ScriptingEngine SE=null;
		for(final Enumeration<ScriptingEngine> e=ship.scripts();e.hasMoreElements();)
		{
			SE=e.nextElement();
			if(SE!=null)
				addScript((ScriptingEngine)SE.copyOf());
		}
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final StdBoardableShip E=(StdBoardableShip)this.clone();
			//CMClass.bumpCounter(E,CMClass.CMObjectType.AREA);//removed for mem & perf
			E.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			E.cloneFix(this);
			return E;

		}
		catch(final CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	@Override
	public String displayText()
	{
		return displayText;
	}

	@Override
	public void setDisplayText(String newDisplayText)
	{
		displayText = newDisplayText;
	}

	@Override
	public String displayText(MOB viewerMob)
	{
		return displayText();
	}

	@Override
	public String name(MOB viewerMob)
	{
		return name();
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public String miscTextFormat()
	{
		return CMParms.FORMAT_UNDEFINED;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,true);
	}

	@Override
	public void setMiscText(String newMiscText)
	{
		miscText="";
		if(newMiscText.trim().length()>0)
			CMLib.coffeeMaker().setPropertiesStr(this,newMiscText,true);
	}

	@Override
	public String description()
	{
		return description;
	}

	@Override
	public void setDescription(String newDescription)
	{
		description = newDescription;
	}

	@Override
	public String description(MOB viewerMob)
	{
		return description();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_EXPIRE)
		&&(msg.target() != null)
		&&(this.getOwnerName().length()>0)
		&&(CMLib.map().areaLocation(msg.target())==this))
			return false;

		MsgListener N=null;
		for(int b=0;b<numBehaviors();b++)
		{
			N=fetchBehavior(b);
			if((N!=null)&&(!N.okMessage(this,msg)))
				return false;
		}
		for(int s=0;s<numScripts();s++)
		{
			N=fetchScript(s);
			if((N!=null)&&(!N.okMessage(this,msg)))
				return false;
		}
		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
		{
			N=a.nextElement();
			if((N!=null)&&(!N.okMessage(this,msg)))
				return false;
		}

		if((flag==State.FROZEN)
		||(flag==State.STOPPED)
		||(!CMLib.flags().allowsMovement(this)))
		{
			if(((msg.sourceMinor()==CMMsg.TYP_ENTER)
				||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
				||(msg.sourceMinor()==CMMsg.TYP_FLEE))
			&&(msg.target() instanceof Room))
			{
				if(this.isRoom((Room)msg.target())
				&&(msg.source().isMonster()))
					return false;
			}
		}

		if(!msg.source().isMonster())
		{
			final Item areaItem=getShipItem();
			if(areaItem != null)
			{
				final ItemPossessor possessor = areaItem.owner();
				final Area areaLoc = (possessor instanceof Room) ? ((Room)possessor).getArea() : null;
				if(areaLoc instanceof StdArea)
				{
					final StdArea otherArea=(StdArea)areaLoc;
					otherArea.lastPlayerTime=System.currentTimeMillis();
					if((otherArea.flag==State.PASSIVE)
					&&((msg.sourceMinor()==CMMsg.TYP_ENTER)
					  ||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
					  ||(msg.sourceMinor()==CMMsg.TYP_FLEE)
					  ||(msg.sourceMinor()==CMMsg.TYP_ADVANCE)
					  ||(msg.sourceMinor()==CMMsg.MSG_NOISYMOVEMENT)))
						otherArea.flag=State.ACTIVE;
				}
			}
		}

		if((getThemeCode()>0)
		&&(!CMath.bset(getThemeCode(),Area.THEME_FANTASY)))
		{
			if((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MAGIC))
			||(CMath.bset(msg.targetMajor(),CMMsg.MASK_MAGIC))
			||(CMath.bset(msg.othersMajor(),CMMsg.MASK_MAGIC)))
			{
				Room room=null;
				if((msg.target() instanceof MOB)
				&&(((MOB)msg.target()).location()!=null))
					room=((MOB)msg.target()).location();
				else
				if(msg.source().location()!=null)
					room=msg.source().location();
				if(room!=null)
				{
					if(room.getArea()==this)
						room.showHappens(CMMsg.MSG_OK_VISUAL,L("Magic doesn't seem to work here."));
					else
						room.showHappens(CMMsg.MSG_OK_VISUAL,L("Magic doesn't seem to work there."));
				}
				return false;
			}
		}
		if(this.shipItem != null)
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_HUH:
			case CMMsg.TYP_COMMANDFAIL:
			case CMMsg.TYP_COMMAND:
				if(!this.shipItem.okMessage(myHost, msg))
					return false;
				break;
			}
		}
		CMLib.law().robberyCheck(this, msg);
		return true;
	}

	protected Enumeration<String> allBlurbFlags()
	{
		final MultiEnumeration<String> multiEnum = new MultiEnumeration<String>(areaBlurbFlags());
		for(final Iterator<Area> i=getParentsIterator();i.hasNext();)
			multiEnum.addEnumeration(i.next().areaBlurbFlags());
		return multiEnum;
	}

	@Override
	public String getBlurbFlag(String flag)
	{
		if((flag==null)||(flag.trim().length()==0))
			return null;
		return blurbFlags.get(flag.toUpperCase().trim());
	}

	@Override
	public int numBlurbFlags()
	{
		return blurbFlags.size();
	}

	@Override
	public int numAllBlurbFlags()
	{
		int num=numBlurbFlags();
		for(final Iterator<Area> i=getParentsIterator();i.hasNext();)
			num += i.next().numAllBlurbFlags();
		return num;
	}

	@Override
	public Enumeration<String> areaBlurbFlags()
	{
		return new IteratorEnumeration<String>(blurbFlags.keySet().iterator());
	}

	@Override
	public void addBlurbFlag(String flagPlusDesc)
	{
		if(flagPlusDesc==null)
			return;
		flagPlusDesc=flagPlusDesc.trim();
		if(flagPlusDesc.length()==0)
			return;
		final int x=flagPlusDesc.indexOf(' ');
		String flag=null;
		if(x>=0)
		{
			flag=flagPlusDesc.substring(0,x).toUpperCase();
			flagPlusDesc=flagPlusDesc.substring(x).trim();
		}
		else
		{
			flag=flagPlusDesc.toUpperCase().trim();
			flagPlusDesc="";
		}
		if(getBlurbFlag(flag)==null)
			blurbFlags.put(flag,flagPlusDesc);
	}

	@Override
	public void delBlurbFlag(String flagOnly)
	{
		if(flagOnly==null)
			return;
		flagOnly=flagOnly.toUpperCase().trim();
		if(flagOnly.length()==0)
			return;
		blurbFlags.remove(flagOnly);
	}

	protected void lookOverBow(final Room R, final CMMsg msg)
	{
		msg.addTrailerRunnable(new Runnable()
		{
			@Override
			public void run()
			{
				if(CMLib.flags().canBeSeenBy(R, msg.source()) && (msg.source().session()!=null))
					msg.source().session().print(L("^HOff the bow you see: ^N"));
				final CMMsg msg2=CMClass.getMsg(msg.source(), R, msg.tool(), msg.sourceCode(), null, msg.targetCode(), null, msg.othersCode(), null);
				if((msg.source().isAttributeSet(MOB.Attrib.AUTOEXITS))
				&&(CMProps.getIntVar(CMProps.Int.EXVIEW)!=CMProps.Int.EXVIEW_PARAGRAPH))
					msg2.addTrailerMsg(CMClass.getMsg(msg.source(),R,null,CMMsg.MSG_LOOK_EXITS,null));
				if(R.okMessage(msg.source(), msg))
					R.send(msg.source(),msg2);
			}
		});
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if (numBehaviors() > 0)
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
		if (numScripts() > 0)
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
		if (numEffects() > 0)
		{
			eachEffect(new EachApplicable<Ability>()
			{
				@Override
				public final void apply(final Ability A)
				{
					A.executeMsg(me, msg);
				}
			});
		}
		if(this.shipItem != null)
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DROP:
				if(msg.target() instanceof Item)
				{
					final Item I=(Item)msg.target();
					msg.addTrailerRunnable(new Runnable()
					{
						@Override
						public void run()
						{
							I.setExpirationDate(0);
						}
					});
				}
				break;
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				if((msg.target() instanceof Exit)&&(((Exit)msg.target()).isOpen()))
				{
					final Room hereR=msg.source().location();
					if((hereR!=null)
					&&((hereR.domainType()&Room.INDOORS)==0)
					&&(hereR.getArea()==this.getShipArea()))
					{
						final Room lookingR=hereR.getRoomInDir(CMLib.map().getExitDir(hereR, (Exit)msg.target()));
						final Room R=CMLib.map().roomLocation(this.shipItem);
						if(lookingR==R)
							lookOverBow(R,msg);
					}
				}
				else
				if((msg.target() instanceof Room)
				&&((((Room)msg.target()).domainType()&Room.INDOORS)==0)
				&&(((Room)msg.target()).getArea()==this))
				{
					if(msg.targetMinor()==CMMsg.TYP_EXAMINE)
					{
						final Room R=CMLib.map().roomLocation(this.shipItem);
						if((R!=null)
						&&(R.getArea()!=this.getShipArea()))
							lookOverBow(R,msg);
					}
				}
				break;
			}
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_HUH:
			case CMMsg.TYP_COMMANDFAIL:
			case CMMsg.TYP_COMMAND:
				this.shipItem.executeMsg(myHost, msg);
				break;
			}
		}
		
	}

	@Override
	public Enumeration<Room> getCompleteMap()
	{
		return getProperMap();
	}
	
	@Override
	public Enumeration<Room> getFilledCompleteMap()
	{
		return getFilledProperMap();
	}

	public List<Room> getMetroCollection()
	{
		return new ReadOnlyList(myRooms);
	}

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((flag==State.STOPPED)||(amDestroyed()))
			return false;
		tickStatus=Tickable.STATUS_START;
		if(tickID==Tickable.TICKID_AREA)
		{
			tickStatus=Tickable.STATUS_BEHAVIOR;
			if (numBehaviors() > 0)
			{
				eachBehavior(new EachApplicable<Behavior>()
				{
					@Override
					public final void apply(final Behavior B)
					{
						B.tick(ticking, tickID);
					}
				});
			}
			tickStatus = Tickable.STATUS_SCRIPT;
			if (numScripts() > 0)
			{
				eachScript(new EachApplicable<ScriptingEngine>()
				{
					@Override
					public final void apply(final ScriptingEngine S)
					{
						S.tick(ticking, tickID);
					}
				});
			}
			tickStatus = Tickable.STATUS_AFFECT;
			if (numEffects() > 0)
			{
				eachEffect(new EachApplicable<Ability>()
				{
					@Override
					public final void apply(final Ability A)
					{
						if (!A.tick(ticking, tickID))
							A.unInvoke();
					}
				});
			}
			if(shipItem != null)
				shipItem.tick(ticking, tickID);
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		if(phyStats().sensesMask()>0)
			affectableStats.setSensesMask(affectableStats.sensesMask()|phyStats().sensesMask());
		final int disposition=phyStats().disposition()
			&((~(PhyStats.IS_SLEEPING|PhyStats.IS_HIDDEN)));
		if(disposition>0)
			affectableStats.setDisposition(affectableStats.disposition()|disposition);
		affectableStats.setWeight(affectableStats.weight()+phyStats().weight());
	}

	@Override
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
	}

	@Override
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
	}

	@Override
	public void addNonUninvokableEffect(Ability to)
	{
		if(to==null)
			return;
		if(fetchEffect(to.ID())!=null)
			return;
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}

	@Override
	public void addEffect(Ability to)
	{
		if(to==null)
			return;
		if(fetchEffect(to.ID())!=null)
			return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}

	@Override
	public void delEffect(Ability to)
	{
		final int size=affects.size();
		affects.removeElement(to);
		if(affects.size()<size)
			to.setAffectedOne(null);
	}

	@Override
	public void eachEffect(final EachApplicable<Ability> applier)
	{
		final List<Ability> affects=this.affects;
		if(affects==null)
			return;
		try
		{
			for(int a=0;a<affects.size();a++)
			{
				final Ability A=affects.get(a);
				if(A!=null)
					applier.apply(A);
			}
		}
		catch (final ArrayIndexOutOfBoundsException e)
		{
		}
	}

	@Override
	public void delAllEffects(boolean unInvoke)
	{
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
		affects.clear();
	}

	@Override
	public int numEffects()
	{
		return (affects==null)?0:affects.size();
	}

	@Override 
	public Enumeration<Ability> effects()
	{
		return (affects==null)?EmptyEnumeration.INSTANCE:affects.elements();
	}

	@Override
	public Ability fetchEffect(int index)
	{
		try
		{
			return affects.elementAt(index);
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public Ability fetchEffect(String ID)
	{
		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A.ID().equals(ID)))
				return A;
		}
		return null;
	}

	@Override
	public void fillInAreaRooms()
	{
	}

	@Override
	public boolean inMyMetroArea(Area A)
	{
		if(A==this)
			return true;
		return false;
	}
	
	@Override
	public void fillInAreaRoom(Room R)
	{
	}

	@Override
	public void dockHere(Room roomR)
	{
		if(roomR==null)
			return;
		savedDock=roomR;
		shipExitCache.clear();
		for(final Enumeration<Room> r=getProperMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				final Room nextR=R.rawDoors()[d];
				final Exit nextE=R.getRawExit(d);
				if((nextE!=null)
				&&((nextR==null)||(nextR.getArea()!=this)))
				{
					R.rawDoors()[d]=roomR;
					shipExitCache.add(new Pair<Room,Integer>(R,Integer.valueOf(d)));
				}
			}
		}
	}

	@Override
	public Room unDock(boolean moveToOutside)
	{
		final Room dock=getIsDocked();
		Room exitRoom = null;
		if(dock==null)
			return null;
		for(final Enumeration<Room> e=getProperMap();e.hasMoreElements();)
		{
			final Room R=e.nextElement();
			if(R!=null)
			{
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room nextR=R.rawDoors()[d];
					if((nextR!=null)
					&&((nextR==dock)||(nextR.getArea()!=this)))
					{
						exitRoom=R;
						R.rawDoors()[d]=null;
					}
				}
			}
		}
		shipExitCache.clear();
		savedDock=null;
		return exitRoom;
	}

	@Override
	public RoomnumberSet getCachedRoomnumbers()
	{
		final RoomnumberSet set=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		synchronized(myRooms)
		{
			Room R=null;
			for(int p=myRooms.size()-1;p>=0;p--)
			{
				R=myRooms.elementAt(p);
				if(R.roomID().length()>0)
					set.add(R.roomID());
			}
		}
		return set;
	}

	@Override
	public RoomnumberSet getProperRoomnumbers()
	{
		if(properRoomIDSet==null)
			properRoomIDSet=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		return properRoomIDSet;
	}

	@Override
	public String getNewRoomID(Room startRoom, int direction)
	{
		int highest=Integer.MIN_VALUE;
		int lowest=Integer.MAX_VALUE;
		final LongSet set=new LongSet();
		try
		{
			String roomID=null;
			int newnum=0;
			final String name=Name().toUpperCase();
			if(!CMLib.flags().isSavable(this))
			{
				for(final Enumeration<String> i=getProperRoomnumbers().getRoomIDs();i.hasMoreElements();)
				{
					roomID=i.nextElement();
					if((roomID.length()>0)&&(roomID.startsWith(name+"#")))
					{
						roomID=roomID.substring(name.length()+1);
						if(CMath.isInteger(roomID))
						{
							newnum=CMath.s_int(roomID);
							if(newnum>=0)
							{
								if(newnum>=highest)
									highest=newnum;
								if(newnum<=lowest)
									lowest=newnum;
								set.add(Long.valueOf(newnum));
							}
						}
					}
				}
			}
			for(final Enumeration i=CMLib.map().roomIDs();i.hasMoreElements();)
			{
				roomID=(String)i.nextElement();
				if((roomID.length()>0)&&(roomID.startsWith(name+"#")))
				{
					roomID=roomID.substring(name.length()+1);
					if(CMath.isInteger(roomID))
					{
						newnum=CMath.s_int(roomID);
						if(newnum>=0)
						{
							if(newnum>=highest)
								highest=newnum;
							if(newnum<=lowest)
								lowest=newnum;
							set.add(Long.valueOf(newnum));
						}
					}
				}
			}
		}
		catch (final NoSuchElementException e)
		{
		}
		if(highest<0)
		{
			if(!CMLib.flags().isSavable(this))
			{
				for(int i=0;i<Integer.MAX_VALUE;i++)
				{
					if((getRoom(Name()+"#"+i))==null)
						return Name()+"#"+i;
				}
			}
			for(int i=0;i<Integer.MAX_VALUE;i++)
			{
				if((CMLib.map().getRoom(Name()+"#"+i))==null)
					return Name()+"#"+i;
			}
		}
		if(lowest>highest)
		{
			lowest=highest+1;
		}
		if(!CMLib.flags().isSavable(this))
		{
			for(int i=lowest;i<=highest+1000;i++)
			{
				if((!set.contains(i))
				&&(getRoom(Name()+"#"+i)==null))
					return Name()+"#"+i;
			}
		}
		for(int i=lowest;i<=highest+1000;i++)
		{
			if((!set.contains(i))
			&&(CMLib.map().getRoom(Name()+"#"+i)==null))
				return Name()+"#"+i;
		}
		return Name()+"#"+(int)Math.round(Math.random()*Integer.MAX_VALUE);
	}

	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	@Override
	public void addBehavior(Behavior to)
	{
		if(to==null)
			return;
		for(int b=0;b<numBehaviors();b++)
		{
			final Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equals(to.ID())))
				return;
		}
		if(behaviors==null)
			behaviors=new SVector<Behavior>();
		behaviors.addElement(to);
	}

	@Override
	public void delBehavior(Behavior to)
	{
		if(behaviors!=null)
			behaviors.removeElement(to);
	}

	@Override
	public void delAllBehaviors()
	{
		final boolean didSomething=(behaviors!=null)&&(behaviors.size()>0);
		if(didSomething) 
			behaviors.clear();
		behaviors=null;
		if(didSomething && ((scripts==null)||(scripts.size()==0)))
			CMLib.threads().deleteTick(this,Tickable.TICKID_ROOM_BEHAVIOR);
	}

	@Override
	public int numBehaviors()
	{
		return (behaviors==null)?0:behaviors.size();
	}

	@Override 
	public Enumeration<Behavior> behaviors() 
	{ 
		return (behaviors!=null)?behaviors.elements():EmptyEnumeration.INSTANCE;
	}

	@Override
	public int maxRange()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public int minRange()
	{
		return Integer.MIN_VALUE;
	}

	@Override
	public int[] getAreaIStats()
	{
		return new int[Area.Stats.values().length];
	}

	@Override
	public StringBuffer getAreaStats()
	{
		return new StringBuffer(description());
	}

	@Override
	public int getPlayerLevel()
	{
		return 0;
	}

	@Override
	public void setPlayerLevel(int level)
	{
	}

	@Override
	public Behavior fetchBehavior(int index)
	{
		try
		{
			return behaviors.elementAt(index);
		}
		catch(final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public Behavior fetchBehavior(String ID)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			final Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				return B;
		}
		return null;
	}

	@Override
	public void eachBehavior(final EachApplicable<Behavior> applier)
	{
		final List<Behavior> behaviors=this.behaviors;
		if(behaviors!=null)
		{
			try
			{
				for(int a=0;a<behaviors.size();a++)
				{
					final Behavior B=behaviors.get(a);
					if(B!=null)
						applier.apply(B);
				}
			}
			catch (final ArrayIndexOutOfBoundsException e)
			{
			}
		}
	}

	/** Manipulation of the scripts list */
	@Override
	public void addScript(ScriptingEngine S)
	{
		if(S==null)
			return;
		if(!scripts.contains(S))
		{
			for(final ScriptingEngine S2 : scripts)
			{
				if((S2!=null)&&(S2.getScript().equalsIgnoreCase(S.getScript())))
					return;
			}
			scripts.addElement(S);
		}
	}

	@Override
	public void delScript(ScriptingEngine S)
	{
		scripts.removeElement(S);
	}

	@Override
	public void delAllScripts()
	{
		final boolean didSomething=(scripts!=null)&&(scripts.size()>0);
		if(didSomething)
			scripts.clear();
		scripts=null;
		if(didSomething && ((behaviors==null)||(behaviors.size()==0)))
			CMLib.threads().deleteTick(this,Tickable.TICKID_ITEM_BEHAVIOR);
	}

	@Override
	public int numScripts()
	{
		return (scripts == null) ? 0 : scripts.size();
	}

	@Override
	public Enumeration<ScriptingEngine> scripts()
	{
		return (scripts == null) ? EmptyEnumeration.INSTANCE : scripts.elements();
	}

	@Override
	public ScriptingEngine fetchScript(int x)
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
		if(scripts!=null)
		{
			try
			{
				for(int a=0;a<scripts.size();a++)
				{
					final ScriptingEngine S=scripts.get(a);
					if(S!=null)
						applier.apply(S);
				}
			}
			catch (final ArrayIndexOutOfBoundsException e)
			{
			}
		}
	}

	@Override
	public void addProperRoom(Room R)
	{
		if(R==null)
			return;
		if(!CMLib.flags().isSavable(this))
			CMLib.flags().setSavable(R,false);
		if(R.getArea()!=this)
		{
			R.setArea(this);
			return;
		}
		synchronized(myRooms)
		{
			if(!myRooms.contains(R))
			{
				addProperRoomnumber(R.roomID());
				Room R2=null;
				for(int i=0;i<myRooms.size();i++)
				{
					R2=myRooms.elementAt(i);
					if(R2.roomID().compareToIgnoreCase(R.roomID())>=0)
					{
						if(R2.ID().compareToIgnoreCase(R.roomID())==0)
							myRooms.setElementAt(R,i);
						else
							myRooms.insertElementAt(R,i);
						return;
					}
				}
				myRooms.addElement(R);
			}
		}
	}

	@Override
	public void delProperRoom(Room R)
	{
		if(R==null)
			return;
		if(R instanceof GridLocale)
			((GridLocale)R).clearGrid(null);
		synchronized(myRooms)
		{
			if(myRooms.removeElement(R))
				delProperRoomnumber(R.roomID());
		}
	}

	@Override
	public void addProperRoomnumber(String roomID)
	{
		if((roomID!=null)&&(roomID.length()>0))
			getProperRoomnumbers().add(roomID);
	}

	@Override
	public void delProperRoomnumber(String roomID)
	{
		if((roomID!=null)&&(roomID.length()>0))
			getProperRoomnumbers().remove(roomID);
	}

	@Override
	public boolean isRoom(Room R)
	{
		if(R==null)
			return false;
		return myRooms.contains(R);
	}

	@Override
	public Room getRoom(String roomID)
	{
		if(myRooms.size()==0)
			return null;
		synchronized(myRooms)
		{
			int start=0;
			int end=myRooms.size()-1;
			while(start<=end)
			{
				final int mid=(end+start)/2;
				final int comp=myRooms.elementAt(mid).roomID().compareToIgnoreCase(roomID);
				if(comp==0)
					return myRooms.elementAt(mid);
				else
				if(comp>0)
					end=mid-1;
				else
					start=mid+1;
			}
		}
		return null;
	}

	@Override
	public int metroSize()
	{
		return properSize();
	}

	@Override
	public int properSize()
	{
		synchronized(myRooms)
		{
			return myRooms.size();
		}
	}

	@Override
	public int numberOfProperIDedRooms()
	{
		int num=0;
		for(final Enumeration<Room> e=getProperMap();e.hasMoreElements();)
		{
			final Room R=e.nextElement();
			if(R.roomID().length()>0)
			{
				if(R instanceof GridLocale)
					num+=((GridLocale)R).xGridSize()*((GridLocale)R).yGridSize();
				else
					num++;
			}
		}
		return num;
	}

	@Override
	public Room getRandomMetroRoom()
	{
		return getRandomProperRoom();
	}

	@Override
	public Room getRandomProperRoom()
	{
		synchronized(myRooms)
		{
			if(properSize()==0)
				return null;
			final Room R=myRooms.elementAt(CMLib.dice().roll(1,properSize(),-1));
			if(R instanceof GridLocale)
				return ((GridLocale)R).getRandomGridChild();
			return R;
		}
	}

	@Override
	public boolean isProperlyEmpty()
	{
		return getProperRoomnumbers().isEmpty();
	}

	@Override
	public void setProperRoomnumbers(RoomnumberSet set)
	{
		properRoomIDSet = set;
	}

	public RoomnumberSet getMetroRoomnumbers()
	{
		return getProperRoomnumbers();
	}

	@Override
	public Enumeration<Room> getMetroMap()
	{
		return getProperMap();
	}

	@Override
	public void addMetroRoomnumber(String roomID)
	{
	}

	@Override
	public void delMetroRoomnumber(String roomID)
	{
	}

	@Override
	public void addMetroRoom(Room R)
	{
	}

	@Override
	public void delMetroRoom(Room R)
	{
	}

	@Override
	public Enumeration<Room> getProperMap()
	{
		synchronized(myRooms)
		{
			return myRooms.elements();
		}
	}

	@Override
	public Enumeration<Room> getFilledProperMap()
	{
		return getProperMap();
	}

	@Override
	public Enumeration<String> subOps()
	{
		return EmptyEnumeration.INSTANCE;
	}

	// Children
	@Override
	public Enumeration<Area> getChildren()
	{
		return EmptyEnumeration.INSTANCE;
	}

	@Override
	public Area getChild(String named)
	{
		return null;
	}

	@Override
	public boolean isChild(Area named)
	{
		return false;
	}

	@Override
	public boolean isChild(String named)
	{
		return false;
	}

	@Override
	public void addChild(Area area)
	{
	}

	@Override
	public void removeChild(Area area)
	{
	}

	@Override
	public boolean canChild(Area area)
	{
		return false;
	}

	public SLinkedList<Area> loadAreas(Collection<String> loadableSet)
	{
		final SLinkedList<Area> finalSet = new SLinkedList<Area>();
		for (final String areaName : loadableSet)
		{
			final Area A = CMLib.map().getArea(areaName);
			if (A == null)
				continue;
			finalSet.add(A);
		}
		return finalSet;
	}

	protected final Iterator<Area> getParentsIterator()
	{
		return parents.iterator();
	}

	protected final Iterator<Area> getParentsReverseIterator()
	{
		return parents.descendingIterator();
	}

	@Override
	public Enumeration<Area> getParents()
	{
		return new IteratorEnumeration<Area>(parents.iterator());
	}

	@Override
	public List<Area> getParentsRecurse()
	{
		final LinkedList<Area> V=new LinkedList<Area>();
		for(final Iterator<Area> a=getParentsIterator();a.hasNext();)
		{
			final Area A=a.next();
			V.add(A);
			V.addAll(A.getParentsRecurse());
		}
		return V;
	}

	@Override
	public Area getParent(String named)
	{
		for(final Iterator<Area> a=getParentsIterator();a.hasNext();)
		{
			final Area A=a.next();
			if((A.name().equalsIgnoreCase(named))
			||(A.Name().equalsIgnoreCase(named)))
				return A;
		}
		return null;
	}

	@Override
	public boolean isParent(Area area)
	{
		for(final Iterator<Area> a=getParentsIterator();a.hasNext();)
		{
			final Area A=a.next();
			if(A == area)
				return true;
		}
		return false;
	}

	@Override
	public boolean isParent(String named)
	{
		for(final Iterator<Area> a=getParentsIterator();a.hasNext();)
		{
			final Area A=a.next();
			if((A.name().equalsIgnoreCase(named))
			||(A.Name().equalsIgnoreCase(named)))
				return true;
		}
		return false;
	}

	@Override
	public void addParent(Area area)
	{
		if(!canParent(area))
			return;
		for(final Iterator<Area> i=getParentsIterator(); i.hasNext();)
		{
			final Area A=i.next();
			if(A.Name().equalsIgnoreCase(area.Name()))
			{
				parents.remove(A);
				break;
			}
		}
		parents.add(area);
	}

	@Override
	public void removeParent(Area area)
	{
		if(isParent(area))
			parents.remove(area);
	}

	@Override
	public boolean canParent(Area area)
	{
		return true;
	}

	@Override
	public String prejudiceFactors()
	{
		return "";
	}

	@Override
	public void setPrejudiceFactors(String factors)
	{
	}

	public final static String[]	empty	= new String[0];

	@Override
	public String[] itemPricingAdjustments()
	{
		return empty;
	}

	@Override
	public void setItemPricingAdjustments(String[] factors)
	{
	}

	@Override
	public String ignoreMask()
	{
		return "";
	}

	@Override
	public void setIgnoreMask(String factors)
	{
	}

	@Override
	public String budget()
	{
		return "";
	}

	@Override
	public void setBudget(String factors)
	{
	}

	@Override
	public String devalueRate()
	{
		return "";
	}

	@Override
	public void setDevalueRate(String factors)
	{
	}

	@Override
	public int invResetRate()
	{
		return 0;
	}

	@Override
	public void setInvResetRate(int ticks)
	{
	}

	@Override
	public int finalInvResetRate()
	{
		return 0;
	}

	@Override
	public String finalPrejudiceFactors()
	{
		return "";
	}

	@Override
	public String finalIgnoreMask()
	{
		return "";
	}

	@Override
	public String[] finalItemPricingAdjustments()
	{
		return empty;
	}

	@Override
	public Pair<Long, TimePeriod> finalBudget()
	{
		return null;
	}

	@Override
	public double[] finalDevalueRate()
	{
		return null;
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	private static final String[]	CODES	= { "CLASS", "CLIMATE", "DESCRIPTION", "TEXT", "THEME", "BLURBS", "AUTHOR", "NAME", "ATMOSPHERE" };

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	@Override
	public boolean isStat(String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	protected int getCodeNum(String code)
	{
		final String[] CODES=getStatCodes();
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(String code)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		case 1:
			return "" + getClimateTypeCode();
		case 2:
			return description();
		case 3:
			return text();
		case 4:
			return "" + getThemeCode();
		case 5:
			return "" + CMLib.xml().getXMLList(blurbFlags.toStringVector(" "));
		case 6:
			return getAuthorID();
		case 7:
			return Name();
		case 8:
			return "" + getAtmosphereCode();
		}
		return "";
	}

	@Override
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return;
		case 1:
			setClimateType(CMath.s_parseBitIntExpression(Places.CLIMATE_DESCS, val));
			break;
		case 2:
			setDescription(val);
			break;
		case 3:
			setMiscText(val);
			break;
		case 4:
			setTheme(CMath.s_parseBitIntExpression(Area.THEME_BIT_NAMES, val));
			break;
		case 5:
		{
			if(val.startsWith("+"))
				addBlurbFlag(val.substring(1));
			else
			if(val.startsWith("-"))
				delBlurbFlag(val.substring(1));
			else
			{
				blurbFlags=new STreeMap<String,String>();
				final List<String> V=CMLib.xml().parseXMLList(val);
				for(final String s : V)
				{
					final int x=s.indexOf(' ');
					if(x<0)
						blurbFlags.put(s,"");
					else
						blurbFlags.put(s.substring(0,x),s.substring(x+1));
				}
			}
			break;
		}
		case 7:
			setAuthorID(val);
			break;
		case 8:
			setName(val);
			break;
		case 9: {
			if(CMath.isMathExpression(val))
				setAtmosphere(CMath.s_parseIntExpression(val));
			final int matCode=RawMaterial.CODES.FIND_IgnoreCase(val);
			if(matCode>=0)
				setAtmosphere(matCode);
			break;
		}
		}
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdBoardableShip))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}

	@Override
	public int getPrice() 
	{
		if( getShipItem() instanceof PrivateProperty)
			return ((PrivateProperty)getShipItem()).getPrice();
		return 0;
	}

	@Override
	public void setPrice(int price) 
	{
		if( getShipItem() instanceof PrivateProperty)
			((PrivateProperty)getShipItem()).setPrice(price);
	}

	@Override
	public String getOwnerName() 
	{
		if( getShipItem() instanceof PrivateProperty)
			return ((PrivateProperty)getShipItem()).getOwnerName();
		return "";
	}

	@Override
	public void setOwnerName(String owner) 
	{
		if( getShipItem() instanceof PrivateProperty)
			((PrivateProperty)getShipItem()).setOwnerName(owner);
	}

	@Override
	public CMObject getOwnerObject() 
	{
		if( getShipItem() instanceof PrivateProperty)
			return ((PrivateProperty)getShipItem()).getOwnerObject();
		return null;
	}

	@Override
	public String getTitleID() 
	{
		if( getShipItem() instanceof PrivateProperty)
			return ((PrivateProperty)getShipItem()).getTitleID();
		return null;
	}
}
