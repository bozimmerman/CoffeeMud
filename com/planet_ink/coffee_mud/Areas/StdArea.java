package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.Area.CompleteRoomEnumerator;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

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
public class StdArea implements Area
{
	@Override
	public String ID()
	{
		return "StdArea";
	}

	protected String	name			= "the area";
	protected String	description 	= "";
	protected String	miscText		= "";
	protected String	archPath		= "";
	protected String	imageName   	= "";
	protected int		playerLevel		= 0;
	protected int   	theme			= Area.THEME_INHERIT;
	protected int		atmosphere		= Room.ATMOSPHERE_INHERIT;
	protected int		derivedTheme	= Area.THEME_INHERIT;
	protected int		derivedAtmo		= Places.ATMOSPHERE_INHERIT;
	protected int   	climask			= Area.CLIMASK_NORMAL;
	protected int		derivedClimate	= Places.CLIMASK_INHERIT;
	protected int	  	tickStatus  	= Tickable.STATUS_NOT;
	protected long  	expirationDate  = 0;
	protected long  	lastPlayerTime  = System.currentTimeMillis();
	protected State   	flag			= State.ACTIVE;
	protected String[]  xtraValues  	= null;
	protected String	author  		= "";
	protected String	currency		= "";
	protected double[]	devalueRate 	= null;
	protected String	budget  		= "";
	protected String	ignoreMask  	= "";
	protected String	prejudiceFactors= "";
	protected int 		invResetRate	= 0;
	protected boolean   amDestroyed 	= false;
	protected PhyStats  phyStats		= (PhyStats)CMClass.getCommon("DefaultPhyStats");
	protected PhyStats  basePhyStats	= (PhyStats)CMClass.getCommon("DefaultPhyStats");

	protected STreeMap<String,String> blurbFlags	 = new STreeMap<String,String>();
	protected STreeMap<String, Room>  properRooms	 = new STreeMap<String, Room>(new RoomIDComparator());
	protected RoomnumberSet 		  properRoomIDSet= null;
	protected RoomnumberSet 		  metroRoomIDSet = null;
	protected SLinkedList<Area> 	  children  	 = new SLinkedList<Area>();
	protected SLinkedList<Area> 	  parents   	 = new SLinkedList<Area>();
	protected SVector<Ability>  	  affects   	 = new SVector<Ability>(1);
	protected SVector<Behavior> 	  behaviors 	 = new SVector<Behavior>(1);
	protected SVector<String>   	  subOps		 = new SVector<String>(1);
	protected SVector<ScriptingEngine>scripts   	 = new SVector<ScriptingEngine>(1);
	protected Area 					  me			 = this;
	protected TimeClock 			  myClock		 = null;
	protected Climate 				  climateObj	 = (Climate)CMClass.getCommon("DefaultClimate");
	
	protected String[] 				itemPricingAdjustments	= new String[0];
	protected final static int[]	emptyStats				= new int[Area.Stats.values().length];
	protected final static String[] empty					= new String[0];
	
	@Override
	public void initializeClass()
	{
	}

	@Override
	public long flags()
	{
		return 0;
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
	public void setCurrency(String newCurrency)
	{
		if(currency.length()>0)
		{
			CMLib.beanCounter().unloadCurrencySet(currency);
			currency=newCurrency;
			for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
				CMLib.beanCounter().getCurrencySet(e.nextElement().getCurrency());
		}
		else
		{
			currency=newCurrency;
			CMLib.beanCounter().getCurrencySet(currency);
		}
	}
	
	@Override
	public String getCurrency()
	{
		return currency;
	}

	@Override
	public int getAtmosphereCode()
	{
		return atmosphere;
	}

	@Override
	public void setAtmosphere(int resourceCode)
	{
		atmosphere=resourceCode;
		derivedAtmo=ATMOSPHERE_INHERIT;
	}
	
	@Override
	public int getAtmosphere()
	{
		if(derivedAtmo!=ATMOSPHERE_INHERIT)
			return derivedAtmo;
		final Stack<Area> areasToDo=new Stack<Area>();
		areasToDo.push(this);
		while(areasToDo.size()>0)
		{
			final Area A=areasToDo.pop();
			derivedAtmo=A.getAtmosphereCode();
			if(derivedAtmo!=ATMOSPHERE_INHERIT)
				return derivedAtmo;
			for(final Enumeration<Area> a=A.getParents();a.hasMoreElements();)
				areasToDo.push(a.nextElement());
		}
		derivedAtmo=RawMaterial.RESOURCE_AIR;
		return derivedAtmo;
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
	public void setClimateObj(Climate obj)
	{
		if(obj != null)
			climateObj = obj;
	}

	@Override
	public Climate getClimateObj()
	{
		if(climateObj == null)
			climateObj = (Climate)CMClass.getCommon("DefaultClimate");
		return climateObj;
	}

	@Override
	public void setTimeObj(TimeClock obj)
	{
		if(obj != null)
		{
			myClock = obj;
			for(final Iterator<Area> i=getChildrenIterator(); i.hasNext();)
			{
				final Area A=i.next();
				A.setTimeObj(obj);
			}
		}
	}
	
	@Override
	public TimeClock getTimeObj()
	{
		if(myClock==null)
		{
			if((this.parents != null)&&(this.parents.size()>0))
				myClock=this.parents.iterator().next().getTimeObj();
			else
				myClock=CMLib.time().globalClock();
		}
		return myClock;
	}

	public StdArea()
	{
		super();
		//CMClass.bumpCounter(this,CMClass.CMObjectType.AREA);//removed for mem & perf
		xtraValues=CMProps.getExtraStatCodesHolder(this);
	}

	//protected void finalize(){CMClass.unbumpCounter(this,CMClass.CMObjectType.AREA);}//removed for mem & perf
	@Override
	public void destroy()
	{
		CMLib.map().registerWorldObjectDestroyed(this,null,this);
		phyStats=(PhyStats)CMClass.getCommon("DefaultPhyStats");
		basePhyStats=phyStats;
		amDestroyed=true;
		miscText=null;
		imageName=null;
		affects=new SVector<Ability>(1);
		behaviors=new SVector<Behavior>(1);
		scripts=new SVector<ScriptingEngine>(1);
		author=null;
		currency=null;
		children=new SLinkedList<Area>();
		parents=new SLinkedList<Area>();
		blurbFlags=new STreeMap<String,String>();
		subOps=new SVector<String>(1);
		properRooms=new STreeMap<String, Room>();
		//metroRooms=null;
		myClock=null;
		climateObj=null;
		properRoomIDSet=null;
		metroRoomIDSet=null;
		author="";
		currency="";
		devalueRate=null;
		budget="";
		ignoreMask="";
		prejudiceFactors="";
		derivedClimate=CLIMASK_INHERIT;
		derivedAtmo=ATMOSPHERE_INHERIT;
		derivedTheme=THEME_INHERIT;
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
	public String name()
	{
		if(phyStats().newName()!=null)
			return phyStats().newName();
		return name;
	}

	@Override
	public synchronized RoomnumberSet getProperRoomnumbers()
	{
		if(properRoomIDSet==null)
			properRoomIDSet=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		return properRoomIDSet;
	}

	@Override
	public RoomnumberSet getCachedRoomnumbers()
	{
		final RoomnumberSet set=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		synchronized(properRooms)
		{
			for(final Room R : properRooms.values())
			{
				if(R.roomID().length()>0)
					set.add(R.roomID());
			}
		}
		return set;
	}

	@Override
	public void setName(String newName)
	{
		if(newName != null)
		{
			name=newName.replace('\'', '`');
			CMLib.map().renamedArea(this);
		}
	}
	
	@Override
	public String Name()
	{
		return name;
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
				A.affectPhyStats(me, phyStats);
			}
		});
	}

	@Override
	public void setBasePhyStats(PhyStats newStats)
	{
		basePhyStats=(PhyStats)newStats.copyOf();
	}

	@Override
	public int getThemeCode()
	{
		return theme;
	}

	@Override
	public int getTheme()
	{
		if(derivedTheme!=THEME_INHERIT)
			return derivedTheme;
		final Stack<Area> areasToDo=new Stack<Area>();
		areasToDo.push(this);
		while(areasToDo.size()>0)
		{
			final Area A=areasToDo.pop();
			derivedTheme=A.getThemeCode();
			if(derivedTheme!=THEME_INHERIT)
				return derivedTheme;
			for(final Enumeration<Area> a=A.getParents();a.hasMoreElements();)
				areasToDo.push(a.nextElement());
		}
		derivedTheme=CMProps.getIntVar(CMProps.Int.MUDTHEME);
		return derivedTheme;
	}

	@Override
	public void setTheme(int level)
	{
		theme=level;
		derivedTheme=THEME_INHERIT;
	}

	@Override
	public String getArchivePath()
	{
		return archPath;
	}

	@Override
	public void setArchivePath(String pathFile)
	{
		archPath = pathFile;
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
	public void setAreaState(State newState)
	{
		if((newState==State.ACTIVE)
		&&(!CMLib.threads().isTicking(this,Tickable.TICKID_AREA)))
		{
			CMLib.threads().startTickDown(this,Tickable.TICKID_AREA,1);
			if(!CMLib.threads().isTicking(this, Tickable.TICKID_AREA))
				Log.errOut("StdArea","Area "+name()+" failed to start ticking.");
		}
		flag=newState;
		derivedClimate=CLIMASK_INHERIT;
		derivedAtmo=ATMOSPHERE_INHERIT;
		derivedTheme=THEME_INHERIT;
	}

	@Override
	public State getAreaState()
	{
		return flag;
	}

	@Override
	public boolean amISubOp(String username)
	{
		for(int s=subOps.size()-1;s>=0;s--)
		{
			if(subOps.elementAt(s).equalsIgnoreCase(username))
				return true;
		}
		return false;
	}

	@Override
	public String getSubOpList()
	{
		final StringBuffer list=new StringBuffer("");
		for(int s=subOps.size()-1;s>=0;s--)
		{
			final String str=subOps.elementAt(s);
			list.append(str);
			list.append(";");
		}
		return list.toString();
	}

	@Override
	public void setSubOpList(String list)
	{
		subOps.clear();
		subOps.addAll(CMParms.parseSemicolons(list,true));
	}

	@Override
	public void addSubOp(String username)
	{
		subOps.addElement(username);
	}

	@Override
	public void delSubOp(String username)
	{
		for(int s=subOps.size()-1;s>=0;s--)
		{
			if(subOps.elementAt(s).equalsIgnoreCase(username))
				subOps.removeElementAt(s);
		}
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
			for(final Enumeration<String> i=CMLib.map().roomIDs();i.hasMoreElements();)
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
		catch (final NoSuchElementException e)
		{
		}
		if(highest<0)
		{
			for(int i=0;i<Integer.MAX_VALUE;i++)
			{
				if(((CMLib.map().getRoom(Name()+"#"+i))==null)
				&&(getRoom(Name()+"#"+i)==null))
					return Name()+"#"+i;
			}
		}
		if(lowest>highest)
		{
			lowest=highest+1;
		}
		for(int i=lowest;i<=highest+1000;i++)
		{
			if((!set.contains(i))
			&&(CMLib.map().getRoom(Name()+"#"+i)==null)
			&&(getRoom(Name()+"#"+i)==null))
				return Name()+"#"+i;
		}
		return Name()+"#"+(int)Math.round(Math.random()*Integer.MAX_VALUE);
	}

	@Override
	public CMObject newInstance()
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.FATAREAS)
		&&(ID().equals("StdArea")))
		{
			final Area A=CMClass.getAreaType("StdThinArea");
			if(A!=null)
				return A;
		}
		try
		{
			return this.getClass().newInstance();
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdArea();
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}

	protected void cloneFix(StdArea areaA)
	{
		me=this;
		basePhyStats=(PhyStats)areaA.basePhyStats().copyOf();
		phyStats=(PhyStats)areaA.phyStats().copyOf();
		properRooms	=new STreeMap<String, Room>(new RoomIDComparator());
		properRoomIDSet=null;
		metroRoomIDSet =null;

		parents=areaA.parents.copyOf();
		children=areaA.children.copyOf();
		if(areaA.blurbFlags!=null)
			blurbFlags=areaA.blurbFlags.copyOf();
		affects=new SVector<Ability>(1);
		behaviors=new SVector<Behavior>(1);
		scripts=new SVector<ScriptingEngine>(1);
		derivedClimate=CLIMASK_INHERIT;
		derivedTheme=THEME_INHERIT;
		derivedAtmo=ATMOSPHERE_INHERIT;
		for(final Enumeration<Behavior> e=areaA.behaviors();e.hasMoreElements();)
		{
			final Behavior B=e.nextElement();
			if(B!=null)
				behaviors.addElement((Behavior)B.copyOf());
		}
		for(final Enumeration<Ability> a=areaA.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null)
				affects.addElement((Ability)A.copyOf());
		}
		ScriptingEngine SE=null;
		for(final Enumeration<ScriptingEngine> e=areaA.scripts();e.hasMoreElements();)
		{
			SE=e.nextElement();
			if(SE!=null)
				addScript((ScriptingEngine)SE.copyOf());
		}
		setSubOpList(areaA.getSubOpList());
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final StdArea E=(StdArea)this.clone();
			//CMClass.bumpCounter(this,CMClass.CMObjectType.AREA);//removed for mem & perf
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
		return "";
	}

	@Override
	public void setDisplayText(String newDisplayText)
	{
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
	public String finalPrejudiceFactors()
	{
		final String s=finalPrejudiceFactors(this);
		if(s.length()>0)
			return s;
		return CMProps.getVar(CMProps.Str.IGNOREMASK);
	}

	protected String finalPrejudiceFactors(Area A)
	{
		if(A.prejudiceFactors().length()>0)
			return A.prejudiceFactors();
		for(final Enumeration<Area> i=A.getParents();i.hasMoreElements();)
		{
			final String  s=finalPrejudiceFactors(i.nextElement());
			if(s.length()!=0)
				return s;
		}
		return "";
	}

	@Override
	public String prejudiceFactors()
	{
		return prejudiceFactors;
	}

	@Override
	public void setPrejudiceFactors(String factors)
	{
		prejudiceFactors = factors;
	}

	@Override
	public String[] finalItemPricingAdjustments()
	{
		final String[] s=finalItemPricingAdjustments(this);
		if(s.length>0)
			return s;
		return CMLib.coffeeShops().parseItemPricingAdjustments(CMProps.getVar(CMProps.Str.PRICEFACTORS).trim());
	}

	protected String[] finalItemPricingAdjustments(Area A)
	{
		if(A.itemPricingAdjustments().length>0)
			return A.itemPricingAdjustments();
		for(final Enumeration<Area> i=A.getParents();i.hasMoreElements();)
		{
			final String[] s=finalItemPricingAdjustments(i.nextElement());
			if(s.length!=0)
				return s;
		}
		return empty;
	}

	@Override
	public String[] itemPricingAdjustments()
	{
		return itemPricingAdjustments;
	}

	@Override
	public void setItemPricingAdjustments(String[] factors)
	{
		itemPricingAdjustments = factors;
	}

	@Override
	public String finalIgnoreMask()
	{
		final String s=finalIgnoreMask(this);
		if(s.length()>0)
			return s;
		return CMProps.getVar(CMProps.Str.IGNOREMASK);
	}

	protected String finalIgnoreMask(Area A)
	{
		if(A.ignoreMask().length()>0)
			return A.ignoreMask();
		for(final Enumeration<Area> i=A.getParents();i.hasMoreElements();)
		{
			final String s=finalIgnoreMask(i.nextElement());
			if(s.length()!=0)
				return s;
		}
		return "";
	}

	@Override
	public String ignoreMask()
	{
		return ignoreMask;
	}

	@Override
	public void setIgnoreMask(String factors)
	{
		ignoreMask = factors;
	}

	@Override
	public Pair<Long, TimePeriod> finalBudget()
	{
		final Pair<Long, TimePeriod> budget=finalAreaBudget(this);
		if(budget != null)
			return budget;
		return CMLib.coffeeShops().parseBudget(CMProps.getVar(CMProps.Str.BUDGET));
	}

	protected Pair<Long, TimePeriod> finalAreaBudget(Area A)
	{
		if(A.budget().length()>0)
			return CMLib.coffeeShops().parseBudget(A.budget());
		for(final Enumeration<Area> i=A.getParents();i.hasMoreElements();)
		{
			final Pair<Long, TimePeriod> budget=finalAreaBudget(i.nextElement());
			if(budget != null)
				return budget;
		}
		return null;
	}

	@Override
	public String budget()
	{
		return budget;
	}

	@Override
	public void setBudget(String factors)
	{
		budget = factors;
	}

	@Override
	public double[] finalDevalueRate()
	{
		final double[] rate=finalAreaDevalueRate(this);
		if(rate != null)
			return rate;
		
		return CMLib.coffeeShops().parseDevalueRate(CMProps.getVar(CMProps.Str.DEVALUERATE));
	}

	protected double[] finalAreaDevalueRate(Area A)
	{
		if(A.devalueRate().length()>0)
			return CMLib.coffeeShops().parseDevalueRate(A.devalueRate());
		for(final Enumeration<Area> i=A.getParents();i.hasMoreElements();)
		{
			final double[] rate=finalAreaDevalueRate(i.nextElement());
			if(rate != null)
				return rate;
		}
		return null;
	}

	@Override
	public String devalueRate()
	{
		return (devalueRate == null) ? "" : (devalueRate[0] + " " + devalueRate[1]);
	}

	@Override
	public void setDevalueRate(String factors)
	{
		devalueRate = CMLib.coffeeShops().parseDevalueRate(factors);
	}

	@Override
	public int invResetRate()
	{
		return invResetRate;
	}

	@Override
	public void setInvResetRate(int ticks)
	{
		invResetRate = ticks;
	}

	@Override
	public int finalInvResetRate()
	{
		final int x=finalInvResetRate(this);
		if(x!=0)
			return x;
		return CMath.s_int(CMProps.getVar(CMProps.Str.INVRESETRATE));
	}

	protected int finalInvResetRate(Area A)
	{
		if(A.invResetRate()!=0)
			return A.invResetRate();
		for(final Enumeration<Area> i=A.getParents();i.hasMoreElements();)
		{
			final int x=finalInvResetRate(i.nextElement());
			if(x!=0)
				return x;
		}
		return 0;
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
		derivedClimate=CLIMASK_INHERIT;
		derivedAtmo=ATMOSPHERE_INHERIT;
		derivedTheme=THEME_INHERIT;
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
	public int getClimateTypeCode()
	{
		return climask;
	}

	@Override
	public void setClimateType(int newClimateType)
	{
		climask=newClimateType;
		derivedClimate=CLIMASK_INHERIT;
	}

	@Override
	public int getClimateType()
	{
		if(derivedClimate!=CLIMASK_INHERIT)
			return derivedClimate;
		final Stack<Area> areasToDo=new Stack<Area>();
		areasToDo.push(this);
		while(areasToDo.size()>0)
		{
			final Area A=areasToDo.pop();
			derivedClimate=A.getClimateTypeCode();
			if(derivedClimate!=CLIMASK_INHERIT)
				return derivedClimate;
			for(final Enumeration<Area> a=A.getParents();a.hasMoreElements();)
				areasToDo.push(a.nextElement());
		}
		derivedClimate=Places.CLIMASK_NORMAL;
		return derivedClimate;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
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
		if(!msg.source().isMonster())
		{
			lastPlayerTime=System.currentTimeMillis();
			if((flag==State.PASSIVE)
			&&((msg.sourceMinor()==CMMsg.TYP_ENTER)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_FLEE)))
				flag=State.ACTIVE;
		}
		
		if((flag==State.FROZEN)||(flag==State.STOPPED)||(!CMLib.flags().allowsMovement(this)))
		{
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_FLEE))
				return false;
		}
		if(parents!=null)
		{
			for (final Area area : parents)
			{
				if(!area.okMessage(myHost,msg))
					return false;
			}
		}

		if((getThemeCode()>0)&&(!CMath.bset(getThemeCode(),Area.THEME_FANTASY)))
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
		else
		if((getTheme()>0)&&(!CMath.bset(getTheme(),Area.THEME_TECHNOLOGY)))
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_BUY:
			case CMMsg.TYP_BID:
			case CMMsg.TYP_CLOSE:
			case CMMsg.TYP_DEPOSIT:
			case CMMsg.TYP_DROP:
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
			case CMMsg.TYP_GET:
			case CMMsg.TYP_PUSH:
			case CMMsg.TYP_PULL:
			case CMMsg.TYP_GIVE:
			case CMMsg.TYP_OPEN:
			case CMMsg.TYP_PUT:
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_REMOVE:
			case CMMsg.TYP_VIEW:
			case CMMsg.TYP_WITHDRAW:
			case CMMsg.TYP_BORROW:
				break;
			case CMMsg.TYP_POWERCURRENT:
				return false;
			default:
				if(msg.tool() instanceof Electronics)
				{
					Room room=null;
					if((msg.target() instanceof MOB)
					&&(((MOB)msg.target()).location()!=null))
						room=((MOB)msg.target()).location();
					else
					if(msg.source().location()!=null)
						room=msg.source().location();
					if(room!=null)
						room.showHappens(CMMsg.MSG_OK_VISUAL,L("Technology doesn't seem to work here."));
					return false;
				}
				break;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		eachBehavior(new EachApplicable<Behavior>()
		{
			@Override
			public final void apply(final Behavior B)
			{
				B.executeMsg(me, msg);
			}
		});
		eachScript(new EachApplicable<ScriptingEngine>()
		{
			@Override
			public final void apply(final ScriptingEngine S)
			{
				S.executeMsg(me, msg);
			}
		});
		eachEffect(new EachApplicable<Ability>()
		{
			@Override
			public final void apply(final Ability A)
			{
				A.executeMsg(me, msg);
			}
		});

		if((msg.sourceMinor()==CMMsg.TYP_RETIRE)
		&&(amISubOp(msg.source().Name())))
			delSubOp(msg.source().Name());

		if(parents!=null)
		{
			for (final Area area : parents)
				area.executeMsg(myHost,msg);
		}
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
			if((flag==State.ACTIVE)
			&&((System.currentTimeMillis()-lastPlayerTime)>Area.TIME_PASSIVE_LAPSE))
			{
				if(CMSecurity.isDisabled(CMSecurity.DisFlag.PASSIVEAREAS)
				&&(!CMath.bset(flags(), Area.FLAG_INSTANCE_CHILD)))
					lastPlayerTime=System.currentTimeMillis();
				else
					flag=State.PASSIVE;
			}
			tickStatus=Tickable.STATUS_ALIVE;
			getClimateObj().tick(this,tickID);
			tickStatus=Tickable.STATUS_REBIRTH;
			getTimeObj().tick(this,tickID);
			tickStatus=Tickable.STATUS_BEHAVIOR;
			eachBehavior(new EachApplicable<Behavior>()
			{ 
				@Override
				public final void apply(final Behavior B)
				{
					B.tick(ticking,tickID);
				} 
			});
			tickStatus=Tickable.STATUS_SCRIPT;
			eachScript(new EachApplicable<ScriptingEngine>()
			{ 
				@Override
				public final void apply(final ScriptingEngine S)
				{
					S.tick(ticking,tickID);
				} 
			});
			tickStatus=Tickable.STATUS_AFFECT;
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
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		final int senses=phyStats.sensesMask()&(~(PhyStats.SENSE_UNLOCATABLE|PhyStats.CAN_NOT_SEE));
		if(senses>0)
			affectableStats.setSensesMask(affectableStats.sensesMask()|senses);
		int disposition=phyStats().disposition()
			&((~(PhyStats.IS_SLEEPING|PhyStats.IS_HIDDEN)));
		if((affected instanceof Room)
		&&(CMLib.map().hasASky((Room)affected)))
		{
			final Climate C=getClimateObj();
			if(((C==null)
				||(((C.weatherType((Room)affected)==Climate.WEATHER_BLIZZARD)
						||(C.weatherType((Room)affected)==Climate.WEATHER_DUSTSTORM))
						&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.DARKWEATHER)))
				||((getTimeObj().getTODCode()==TimeClock.TimeOfDay.NIGHT)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.DARKNIGHTS))))
			&&((disposition&PhyStats.IS_LIGHTSOURCE)==0))
				disposition=disposition|PhyStats.IS_DARK;
		}
		if(disposition>0)
			affectableStats.setDisposition(affectableStats.disposition()|disposition);
		affectableStats.setWeight(affectableStats.weight()+phyStats().weight());
		eachEffect(new EachApplicable<Ability>()
		{
			@Override
			public final void apply(final Ability A)
			{
				if (A.bubbleAffect())
					A.affectPhyStats(affected, affectableStats);
			}
		});
	}
	
	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
		eachEffect(new EachApplicable<Ability>()
		{
			@Override
			public final void apply(final Ability A)
			{
				if (A.bubbleAffect())
					A.affectCharStats(affectedMob, affectableStats);
			}
		});
	}
	
	@Override
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
		eachEffect(new EachApplicable<Ability>()
		{ 
			@Override
			public final void apply(final Ability A)
			{
				if (A.bubbleAffect())
					A.affectCharState(affectedMob, affectableMaxState);
			}
		});
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

	@SuppressWarnings("unchecked")

	@Override
	public Enumeration<Ability> effects()
	{
		return (affects == null) ? EmptyEnumeration.INSTANCE : affects.elements();
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
	public boolean inMyMetroArea(Area A)
	{
		if(A==this)
			return true;
		for(final Iterator<Area> i=getChildrenIterator();i.hasNext();)
		{
			if(i.next().inMyMetroArea(A))
				return true;
		}
		return false;
	}

	@Override
	public void fillInAreaRooms()
	{
		for(final Enumeration<Room> r=getProperMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			R.clearSky();
			if(R.roomID().length()>0)
			{
				if(R instanceof GridLocale)
					((GridLocale)R).buildGrid();
			}
		}
		for(final Enumeration<Room> r=getProperMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			R.giveASky(0);
		}
	}

	@Override
	public void fillInAreaRoom(Room R)
	{
		if(R==null)
			return;
		R.clearSky();
		if(R.roomID().length()>0)
		{
			if(R instanceof GridLocale)
				((GridLocale)R).buildGrid();
		}
		R.giveASky(0);
	}

	/** Manipulation of Behavior objects, which includes
	 * movement, speech, spellcasting, etc, etc.*/
	@Override
	public void addBehavior(Behavior to)
	{
		if(to==null)
			return;
		for(final Behavior B : behaviors)
		{
			if((B!=null)&&(B.ID().equals(to.ID())))
				return;
		}
		to.startBehavior(this);
		behaviors.addElement(to);
	}
	
	@Override
	public void delBehavior(Behavior to)
	{
		behaviors.removeElement(to);
	}
	
	@Override
	public void delAllBehaviors()
	{
		behaviors.clear();
	}
	
	@Override
	public int numBehaviors()
	{
		return behaviors.size();
	}
	
	@Override
	public Enumeration<Behavior> behaviors()
	{
		return behaviors.elements();
	}

	/** Manipulation of the scripts list */
	@Override
	public void addScript(ScriptingEngine S)
	{
		if(S==null)
			return;
		if(!scripts.contains(S))
		{
			ScriptingEngine S2=null;
			for(int s=0;s<scripts.size();s++)
			{
				S2=scripts.elementAt(s);
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
	public int numScripts()
	{
		return scripts.size();
	}

	@Override
	public Enumeration<ScriptingEngine> scripts()
	{
		return scripts.elements();
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
	public void delAllScripts()
	{
		scripts.clear();
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
	public int maxRange()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public int minRange()
	{
		return Integer.MIN_VALUE;
	}

	protected void buildAreaIMobStats(final int[] statData, final long[] totalAlignments, final Faction theFaction, final List<Integer> alignRanges, final List<Integer> levelRanges, final MOB mob)
	{
		if((mob!=null)&&(mob.isMonster())&&(!CMLib.flags().isUnattackable(mob)))
		{
			final int lvl=mob.basePhyStats().level();
			levelRanges.add(Integer.valueOf(lvl));
			if((theFaction!=null)&&(mob.fetchFaction(theFaction.factionID())!=Integer.MAX_VALUE))
			{
				alignRanges.add(Integer.valueOf(mob.fetchFaction(theFaction.factionID())));
				totalAlignments[0]+=mob.fetchFaction(theFaction.factionID());
			}
			statData[Area.Stats.POPULATION.ordinal()]++;
			statData[Area.Stats.TOTAL_LEVELS.ordinal()]+=lvl;
			if(!CMLib.flags().isAnimalIntelligence(mob))
				statData[Area.Stats.TOTAL_INTELLIGENT_LEVELS.ordinal()]+=lvl;
			if(lvl<statData[Area.Stats.MIN_LEVEL.ordinal()])
				statData[Area.Stats.MIN_LEVEL.ordinal()]=lvl;
			if(lvl>statData[Area.Stats.MAX_LEVEL.ordinal()])
				statData[Area.Stats.MAX_LEVEL.ordinal()]=lvl;
		}
	}
	
	protected int[] buildAreaIStats()
	{
		final List<Integer> levelRanges=new Vector<Integer>();
		final List<Integer> alignRanges=new Vector<Integer>();
		Faction theFaction=null;
		for(final Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
		{
			final Faction F=e.nextElement();
			if(F.showInSpecialReported())
				theFaction=F;
		}
		final int[] statData=new int[Area.Stats.values().length];
		statData[Area.Stats.POPULATION.ordinal()]=0;
		statData[Area.Stats.MIN_LEVEL.ordinal()]=Integer.MAX_VALUE;
		statData[Area.Stats.MAX_LEVEL.ordinal()]=Integer.MIN_VALUE;
		statData[Area.Stats.AVG_LEVEL.ordinal()]=0;
		statData[Area.Stats.MED_LEVEL.ordinal()]=0;
		statData[Area.Stats.AVG_ALIGNMENT.ordinal()]=0;
		statData[Area.Stats.TOTAL_LEVELS.ordinal()]=0;
		statData[Area.Stats.TOTAL_INTELLIGENT_LEVELS.ordinal()]=0;
		statData[Area.Stats.VISITABLE_ROOMS.ordinal()]=getProperRoomnumbers().roomCountAllAreas();
		final long[] totalAlignments=new long[]{0};
		for(final Enumeration<Room> r=getProperMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			if(R instanceof GridLocale)
				statData[Area.Stats.VISITABLE_ROOMS.ordinal()]--;
			if((R.domainType()&Room.INDOORS)>0)
				statData[Area.Stats.INDOOR_ROOMS.ordinal()]++;
			for(int i=0;i<R.numInhabitants();i++)
				buildAreaIMobStats(statData,totalAlignments,theFaction,alignRanges,levelRanges,R.fetchInhabitant(i));
			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
				if(I instanceof BoardableShip)
				{
					final Area A=((BoardableShip)I).getShipArea();
					if(A==null)
						continue;
					for(final Enumeration<Room> r2=A.getProperMap();r2.hasMoreElements();)
					{
						final Room R2=r2.nextElement();
						for(int i2=0;i2<R2.numInhabitants();i2++)
							buildAreaIMobStats(statData,totalAlignments,theFaction,alignRanges,levelRanges,R2.fetchInhabitant(i2));
					}
				}
			}
		}
		if((statData[Area.Stats.POPULATION.ordinal()]==0)||(levelRanges.size()==0))
		{
			statData[Area.Stats.MIN_LEVEL.ordinal()]=0;
			statData[Area.Stats.MAX_LEVEL.ordinal()]=0;
		}
		else
		{
			Collections.sort(levelRanges);
			Collections.sort(alignRanges);
			statData[Area.Stats.MED_LEVEL.ordinal()]=levelRanges.get((int)Math.round(Math.floor(CMath.div(levelRanges.size(),2.0)))).intValue();
			if(alignRanges.size()>0)
			{
				statData[Area.Stats.MED_ALIGNMENT.ordinal()]=alignRanges.get((int)Math.round(Math.floor(CMath.div(alignRanges.size(),2.0)))).intValue();
				statData[Area.Stats.MIN_ALIGNMENT.ordinal()]=alignRanges.get(0).intValue();
				statData[Area.Stats.MAX_ALIGNMENT.ordinal()]=alignRanges.get(alignRanges.size()-1).intValue();
			}
			statData[Area.Stats.AVG_LEVEL.ordinal()]=(int)Math.round(CMath.div(statData[Area.Stats.TOTAL_LEVELS.ordinal()],statData[Area.Stats.POPULATION.ordinal()]));
			statData[Area.Stats.AVG_ALIGNMENT.ordinal()]=(int)Math.round(((double)totalAlignments[0])/((double)statData[Area.Stats.POPULATION.ordinal()]));
		}
		return statData;
	}

	@Override
	public int getPlayerLevel()
	{
		return playerLevel;
	}

	@Override
	public void setPlayerLevel(int level)
	{
		playerLevel=level;
	}

	@Override
	public int[] getAreaIStats()
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return emptyStats;
		int[] statData=(int[])Resources.getResource("STATS_"+Name().toUpperCase());
		if(statData!=null)
			return statData;
		synchronized(("STATS_"+Name()).intern())
		{
			Resources.removeResource("HELP_"+Name().toUpperCase());
			statData=buildAreaIStats();
			Resources.submitResource("STATS_"+Name().toUpperCase(),statData);
		}
		return statData;
	}

	public int getPercentRoomsCached()
	{
		return 100;
	}

	protected StringBuffer buildAreaStats(int[] statData)
	{
		final StringBuffer s=new StringBuffer("^N");
		s.append("Area           : ^H"+Name()+"^N\n\r");
		s.append(description()+"\n\r");
		if(author.length()>0)
			s.append("Author         : ^H"+author+"^N\n\r");
		if(statData == emptyStats)
		{
			s.append("\n\r^HFurther information about this area is not available at this time.^N\n\r");
			return s;
		}
		s.append("Number of rooms: ^H"+statData[Area.Stats.VISITABLE_ROOMS.ordinal()]+"^N\n\r");
		Faction theFaction=CMLib.factions().getFaction(CMLib.factions().AlignID());
		if(theFaction == null)
		{
			for(final Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
			{
				final Faction F=e.nextElement();
				if(F.showInSpecialReported())
					theFaction=F;
			}
		}
		if(statData[Area.Stats.POPULATION.ordinal()]==0)
		{
			if(getProperRoomnumbers().roomCountAllAreas()/2<properRooms.size())
				s.append("Population     : ^H0^N\n\r");
		}
		else
		{
			s.append("Population     : ^H"+statData[Area.Stats.POPULATION.ordinal()]+"^N\n\r");
			final String currName=CMLib.beanCounter().getCurrency(this);
			if(currName.length()>0)
				s.append("Currency       : ^H"+CMStrings.capitalizeAndLower(currName)+"^N\n\r");
			else
				s.append("Currency       : ^HGold coins (default)^N\n\r");
			final LegalBehavior B=CMLib.law().getLegalBehavior(this);
			if(B!=null)
			{
				final String ruler=B.rulingOrganization();
				if(ruler.length()>0)
				{
					final Clan C=CMLib.clans().getClan(ruler);
					if(C!=null)
						s.append("Controlled by  : ^H"+C.getGovernmentName()+" "+C.name()+"^N\n\r");
				}
			}
			s.append("Level range    : ^H"+statData[Area.Stats.MIN_LEVEL.ordinal()]+"^N to ^H"+statData[Area.Stats.MAX_LEVEL.ordinal()]+"^N\n\r");
			//s.append("Average level  : ^H"+statData[Area.Stats.AVG_LEVEL.ordinal()]+"^N\n\r");
			if(getPlayerLevel()>0)
				s.append("Player level   : ^H"+getPlayerLevel()+"^N\n\r");
			else
				s.append("Median level   : ^H"+statData[Area.Stats.MED_LEVEL.ordinal()]+"^N\n\r");
			if(theFaction!=null)
				s.append("Avg. "+CMStrings.padRight(theFaction.name(),10)+": ^H"+theFaction.fetchRangeName(statData[Area.Stats.AVG_ALIGNMENT.ordinal()])+"^N\n\r");
			if(theFaction!=null)
				s.append("Med. "+CMStrings.padRight(theFaction.name(),10)+": ^H"+theFaction.fetchRangeName(statData[Area.Stats.MED_ALIGNMENT.ordinal()])+"^N\n\r");
		}
		try
		{
			boolean blurbed=false;
			String flag=null;
			for(final Enumeration<String> f= allBlurbFlags();f.hasMoreElements();)
			{
				flag=getBlurbFlag(f.nextElement());
				if((flag!=null)
				&&((!flag.startsWith("{"))||(!flag.endsWith("}"))))
				{
					if (!blurbed)
					{
						blurbed = true;
						s.append("\n\r");
					}
					s.append(flag+"\n\r");
				}
			}
			if(blurbed)
				s.append("\n\r");
		}
		catch(final Exception e)
		{
			Log.errOut("StdArea",e);
		}
		return s;
	}

	@Override
	public synchronized StringBuffer getAreaStats()
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return new StringBuffer("");
		StringBuffer s=(StringBuffer)Resources.getResource("HELP_"+Name().toUpperCase());
		if(s!=null)
			return s;
		s=buildAreaStats(getAreaIStats());
		//Resources.submitResource("HELP_"+Name().toUpperCase(),s); // the STAT_ data is cached instead.
		return s;
	}

	@Override
	public Behavior fetchBehavior(int index)
	{
		try
		{
			return behaviors.elementAt(index);
		}
		catch (final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
		return null;
	}
	
	@Override
	public Behavior fetchBehavior(String ID)
	{
		for(final Behavior B : behaviors)
		{
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

	@Override
	public int properSize()
	{
		synchronized(properRooms)
		{
			return properRooms.size();
		}
	}
	
	@Override
	public void setProperRoomnumbers(RoomnumberSet set)
	{
		properRoomIDSet = set;
	}

	@Override
	public void addProperRoom(Room R)
	{
		if(R==null)
			return;
		if(R.getArea()!=this)
		{
			R.setArea(this);
			return;
		}
		synchronized(properRooms)
		{
			final String roomID=R.roomID();
			if(roomID.length()==0)
			{
				if((R.getGridParent()!=null)
				&&(R.getGridParent().roomID().length()>0))
				{
					// for some reason, grid children always get the back of the bus.
					addProperRoomnumber(R.getGridParent().getGridChildCode(R));
					addMetroRoom(R);
				}
				return;
			}
			if(!properRooms.containsKey(R.roomID()))
				properRooms.put(R.roomID(),R);
			addProperRoomnumber(roomID);
			addMetroRoom(R);
		}
	}

	@Override
	public void addMetroRoom(Room R)
	{
		if(R!=null)
		{
			if(R.roomID().length()==0)
			{
				if((R.getGridParent()!=null)
				&&(R.getGridParent().roomID().length()>0))
					addMetroRoomnumber(R.getGridParent().getGridChildCode(R));
			}
			else
				addMetroRoomnumber(R.roomID());
		}
	}
	
	@Override
	public void delMetroRoom(Room R)
	{
		if(R!=null)
		{
			if(R.roomID().length()==0)
			{
				if((R.getGridParent()!=null)
				&&(R.getGridParent().roomID().length()>0))
					delMetroRoomnumber(R.getGridParent().getGridChildCode(R));
			}
			else
				delMetroRoomnumber(R.roomID());
		}
	}
	
	@Override
	public void addProperRoomnumber(String roomID)
	{
		if((roomID!=null)&&(roomID.length()>0))
		{
			getProperRoomnumbers().add(roomID);
			addMetroRoomnumber(roomID);
		}
	}
	
	@Override
	public void delProperRoomnumber(String roomID)
	{
		if((roomID!=null)&&(roomID.length()>0))
		{
			getProperRoomnumbers().remove(roomID);
			delMetroRoomnumber(roomID);
		}
	}
	
	@Override
	public void addMetroRoomnumber(String roomID)
	{
		if(metroRoomIDSet==null)
			metroRoomIDSet=(RoomnumberSet)getProperRoomnumbers().copyOf();
		if((roomID!=null)&&(roomID.length()>0)&&(!metroRoomIDSet.contains(roomID)))
		{
			metroRoomIDSet.add(roomID);
			if(!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
			{
				for(final Iterator<Area> a=getParentsReverseIterator();a.hasNext();)
					a.next().addMetroRoomnumber(roomID);
			}
		}
	}
	
	@Override
	public void delMetroRoomnumber(String roomID)
	{
		if((metroRoomIDSet!=null)
		&&(roomID!=null)
		&&(roomID.length()>0)
		&&(metroRoomIDSet.contains(roomID)))
		{
			metroRoomIDSet.remove(roomID);
			if(!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
			{
				for(final Iterator<Area> a=getParentsReverseIterator();a.hasNext();)
					a.next().delMetroRoomnumber(roomID);
			}
		}
	}
	
	@Override
	public boolean isRoom(Room R)
	{
		if(R==null)
			return false;
		if(R.roomID().length()>0)
			return getProperRoomnumbers().contains(R.roomID());
		return properRooms.containsValue(R);
	}

	@Override
	public void delProperRoom(Room R)
	{
		if(R==null)
			return;
		if(R instanceof GridLocale)
			((GridLocale)R).clearGrid(null);
		synchronized(properRooms)
		{
			if(R.roomID().length()==0)
			{
				if((R.getGridParent()!=null)&&(R.getGridParent().roomID().length()>0))
				{
					final String id=R.getGridParent().getGridChildCode(R);
					delProperRoomnumber(id);
					delMetroRoom(R);
				}
			}
			else
			if(properRooms.get(R.roomID())==R)
			{
				properRooms.remove(R.roomID());
				delMetroRoom(R);
				delProperRoomnumber(R.roomID());
			}
			else
			if(properRooms.containsValue(R))
			{
				for(final Map.Entry<String,Room> entry : properRooms.entrySet())
				{
					if(entry.getValue()==R)
					{
						properRooms.remove(entry.getKey());
						delProperRoomnumber(entry.getKey());
					}
				}
				delProperRoomnumber(R.roomID());
				delMetroRoom(R);
			}
		}
	}

	@Override
	public Room getRoom(String roomID)
	{
		if(properRooms.size()==0)
			return null;
		if(roomID.length()==0)
			return null;
		synchronized(properRooms)
		{
			if((!roomID.startsWith(Name()))
			&&(roomID.toUpperCase().startsWith(Name().toUpperCase()+"#")))
				roomID=Name()+roomID.substring(Name().length()); // for case sensitive situations
			return properRooms.get(roomID);
		}
	}

	@Override
	public int metroSize()
	{
		int num=properSize();
		for(final Iterator<Area> a=getChildrenReverseIterator();a.hasNext();)
			num+=a.next().metroSize();
		return num;
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
	public boolean isProperlyEmpty()
	{
		return getProperRoomnumbers().isEmpty();
	}

	@Override
	public Room getRandomProperRoom()
	{
		final String roomID=getProperRoomnumbers().random();
		final Room R=CMLib.map().getRoom(roomID);
		if(R instanceof GridLocale)
			return ((GridLocale)R).getRandomGridChild();
		if(R==null)
			Log.errOut("StdArea","Unable to random-find: "+roomID);
		return R;
	}

	@Override
	public Room getRandomMetroRoom()
	{
		/*synchronized(metroRooms)
		{
			if(metroSize()==0)
				return null;
			Room R=(Room)metroRooms.elementAt(CMLib.dice().roll(1,metroRooms.size(),-1));
			if(R instanceof GridLocale)
				return ((GridLocale)R).getRandomGridChild();
			return R;
		}*/
		final String roomID=metroRoomIDSet.random();
		final Room R=CMLib.map().getRoom(roomID);
		if(R instanceof GridLocale)
			return ((GridLocale)R).getRandomGridChild();
		if(R==null)
			Log.errOut("StdArea","Unable to random-metro-find: "+roomID);
		return R;
	}

	@Override
	public Enumeration<Room> getProperMap()
	{
		return new CompleteRoomEnumerator(new IteratorEnumeration<Room>(properRooms.values().iterator()));
	}

	@Override
	public Enumeration<Room> getFilledProperMap()
	{
		final Enumeration<Room> r=getProperMap();
		final List<Room> V=new LinkedList<Room>();
		for(;r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			if(R!=null)
			{
				V.add(R);
				for(final Room R2 : R.getSky())
				{
					if(R2 instanceof GridLocale)
						V.addAll(((GridLocale)R2).getAllRoomsFilled());
					else
					if(!V.contains(R2))
						V.add(R2);
				}
			}
		}
		return new IteratorEnumeration<Room>(V.iterator());
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

	@Override
	public Enumeration<Room> getMetroMap()
	{
		final MultiEnumeration<Room> multiEnumerator = new MultiEnumeration<Room>(new IteratorEnumeration<Room>(properRooms.values().iterator()));
		for(final Iterator<Area> a=getChildrenReverseIterator();a.hasNext();)
			multiEnumerator.addEnumeration(a.next().getMetroMap());
		return new CompleteRoomEnumerator(multiEnumerator);
	}

	@Override
	public Enumeration<String> subOps()
	{
		return subOps.elements();
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

	protected final Iterator<Area> getChildrenIterator()
	{
		return children.iterator();
	}

	protected final Iterator<Area> getChildrenReverseIterator()
	{
		return children.descendingIterator();
	}

	@Override
	public Enumeration<Area> getChildren()
	{
		return new IteratorEnumeration<Area>(getChildrenIterator());
	}

	@Override
	public Area getChild(String named)
	{
		for(final Iterator<Area> i=getChildrenIterator(); i.hasNext();)
		{
			final Area A=i.next();
			if((A.name().equalsIgnoreCase(named))
			||(A.Name().equalsIgnoreCase(named)))
				return A;
		}
		return null;
	}

	@Override
	public boolean isChild(Area area)
	{
		for(final Iterator<Area> i=getChildrenIterator(); i.hasNext();)
		{
			if(i.next().equals(area))
				return true;
		}
		return false;
	}

	@Override
	public boolean isChild(String named)
	{
		for(final Iterator<Area> i=getChildrenIterator(); i.hasNext();)
		{
			final Area A=i.next();
			if((A.name().equalsIgnoreCase(named))
			||(A.Name().equalsIgnoreCase(named)))
				return true;
		}
		return false;
	}

	@Override
	public void addChild(Area area)
	{
		if(!canChild(area))
			return;
		if(area.Name().equalsIgnoreCase(Name()))
			return;
		for(final Iterator<Area> i=getChildrenIterator(); i.hasNext();)
		{
			final Area A=i.next();
			if(A.Name().equalsIgnoreCase(area.Name()))
			{
				children.remove(A);
				break;
			}
		}
		children.add(area);
		if(getTimeObj()!=CMLib.time().globalClock())
			area.setTimeObj(getTimeObj());
	}

	@Override
	public void removeChild(Area area)
	{
		if(isChild(area))
			children.remove(area);
	}

	// child based circular reference check
	@Override
	public boolean canChild(Area area)
	{
		if(area instanceof BoardableShip)
			return false;
		if(parents != null)
		{
			for (Area A : parents)
			{
				if(A==area)
					return false;
				if(!A.canChild(area))
					return false;
			}
		}
		return true;
	}

	// Parent
	@Override
	public Enumeration<Area> getParents()
	{
		return new IteratorEnumeration<Area>(getParentsIterator());
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
		derivedClimate=CLIMASK_INHERIT;
		derivedAtmo=ATMOSPHERE_INHERIT;
		derivedTheme=THEME_INHERIT;
		if(!canParent(area))
			return;
		if(area.Name().equalsIgnoreCase(Name()))
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
		derivedClimate=CLIMASK_INHERIT;
		derivedAtmo=ATMOSPHERE_INHERIT;
		derivedTheme=THEME_INHERIT;
		if(isParent(area))
			parents.remove(area);
	}

	@Override
	public boolean canParent(Area area)
	{
		if(this instanceof BoardableShip)
			return false;
		if(children != null)
		{
			for (Area A : children)
			{
				if(A==area)
					return false;
				if(!A.canParent(area))
					return false;
			}
		}
		return true;
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	@Override
	public int getSaveStatIndex()
	{
		return (xtraValues == null) ? getStatCodes().length : getStatCodes().length - xtraValues.length;
	}

	protected static final String[] STDAREACODES={"CLASS",
												  "CLIMATE",
												  "DESCRIPTION",
												  "TEXT",
												  "THEME",
												  "BLURBS",
												  "PREJUDICE",
												  "BUDGET",
												  "DEVALRATE",
												  "INVRESETRATE",
												  "IGNOREMASK",
												  "PRICEMASKS",
												  "ATMOSPHERE",
												  "AUTHOR",
												  "NAME",
												  "PLAYERLEVEL"
												  };
	private static String[] codes=null;
	
	@Override
	public String[] getStatCodes()
	{
		if(codes==null)
			codes=CMProps.getStatCodesList(STDAREACODES,this);
		return codes;
	}
	
	@Override
	public boolean isStat(String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	protected int getCodeNum(String code)
	{
		return CMParms.indexOf(codes, code.toUpperCase());
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
			return prejudiceFactors();
		case 7:
			return budget();
		case 8:
			return devalueRate();
		case 9:
			return "" + invResetRate();
		case 10:
			return ignoreMask();
		case 11:
			return CMParms.toListString(itemPricingAdjustments());
		case 12:
			return "" + getAtmosphereCode();
		case 13:
			return getAuthorID();
		case 14:
			return name();
		case 15:
			return ""+playerLevel;
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1:
			setClimateType((CMath.s_int(val) < 0) ? -1 : CMath.s_parseBitIntExpression(Places.CLIMATE_DESCS, val));
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
		case 6:
			setPrejudiceFactors(val);
			break;
		case 7:
			setBudget(val);
			break;
		case 8:
			setDevalueRate(val);
			break;
		case 9:
			setInvResetRate(CMath.s_parseIntExpression(val));
			break;
		case 10:
			setIgnoreMask(val);
			break;
		case 11:
			setItemPricingAdjustments((val.trim().length() == 0) ? new String[0] : CMParms.toStringArray(CMParms.parseCommas(val, true)));
			break;
		case 12:
		{
			if (CMath.isMathExpression(val))
				setAtmosphere(CMath.s_parseIntExpression(val));
			final int matCode = RawMaterial.CODES.FIND_IgnoreCase(val);
			if (matCode >= 0)
				setAtmosphere(matCode);
			break;
		}
		case 13:
			setAuthorID(val);
			break;
		case 14:
			setName(val);
			break;
		case 15:
			setPlayerLevel((int)Math.round(CMath.parseMathExpression(val)));
			break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}
	
	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdArea))
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
