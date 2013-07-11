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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2013 Bo Zimmerman

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
public class StdArea implements Area
{
	public String ID(){ return "StdArea";}
	protected String	name="the area";
	protected String	description 	="";
	protected String	miscText		="";
	protected String	archPath		="";
	protected String	imageName   	="";
	protected int   	techLevel   	=0;
	protected int   	climateID   	=Area.CLIMASK_NORMAL;
	protected long  	tickStatus  	=Tickable.STATUS_NOT;
	protected long  	expirationDate  =0;
	protected long  	lastPlayerTime  =System.currentTimeMillis();
	protected State   	flag			=State.ACTIVE;
	protected String[]  xtraValues  	=null;
	protected String	author  		="";
	protected String	currency		="";
	protected String	devalueRate 	="";
	protected String	budget  		="";
	protected String	ignoreMask  	="";
	protected String	prejudiceFactors="";
	protected boolean   amDestroyed 	=false;
	protected PhyStats  phyStats		=(PhyStats)CMClass.getCommon("DefaultPhyStats");
	protected PhyStats  basePhyStats	=(PhyStats)CMClass.getCommon("DefaultPhyStats");
	protected boolean   initializedArea = false;
	
	protected STreeMap<String,String> blurbFlags	 =new STreeMap<String,String>();
	protected STreeMap<String, Room>  properRooms    =new STreeMap<String, Room>(new RoomIDComparator());
	protected RoomnumberSet 		  properRoomIDSet=null;
	protected RoomnumberSet 		  metroRoomIDSet =null;
	protected SLinkedList<Area> 	  children  	 =null;
	protected SLinkedList<Area> 	  parents   	 =null;
	protected SLinkedList<String>     childrenToLoad =new SLinkedList<String>();
	protected SLinkedList<String>     parentsToLoad  =new SLinkedList<String>();
	protected SVector<Ability>  	  affects   	 =new SVector<Ability>(1);
	protected SVector<Behavior> 	  behaviors 	 =new SVector<Behavior>(1);
	protected SVector<String>   	  subOps		 =new SVector<String>(1);
	protected SVector<ScriptingEngine>scripts   	 =new SVector<ScriptingEngine>(1);
	protected final Area 			  me			 =this;

	protected final static int[]	  emptyStats	 =new int[Area.Stats.values().length];
	
	public void initializeClass(){}
	public long flags(){return 0;}
	public void setAuthorID(String authorID){author=authorID;}
	public String getAuthorID(){return author;}
	public void setCurrency(String newCurrency)
	{
		if(currency.length()>0)
		{
			CMLib.beanCounter().unloadCurrencySet(currency);
			currency=newCurrency;
			for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
				CMLib.beanCounter().getCurrencySet(((Area)e.nextElement()).getCurrency());
		}
		else
		{
			currency=newCurrency;
			CMLib.beanCounter().getCurrencySet(currency);
		}
	}
	public String getCurrency(){return currency;}

	protected Enumeration<String> allBlurbFlags()
	{
		MultiEnumeration<String> multiEnum = new MultiEnumeration<String>(areaBlurbFlags());
		for(Iterator<Area> i=getParentsIterator();i.hasNext();)
			multiEnum.addEnumeration(i.next().areaBlurbFlags());
		return multiEnum;
	}

	public String getBlurbFlag(String flag)
	{
		if((flag==null)||(flag.trim().length()==0))
			return null;
		return blurbFlags.get(flag.toUpperCase().trim());
	}
	public int numBlurbFlags(){return blurbFlags.size();}
	public int numAllBlurbFlags()
	{
		int num=numBlurbFlags();
		for(Iterator<Area> i=getParentsIterator();i.hasNext();)
			num += i.next().numAllBlurbFlags();
		return num;
	}
	public Enumeration<String> areaBlurbFlags()
	{
		return new IteratorEnumeration<String>(blurbFlags.keySet().iterator());
	}
	
	public void addBlurbFlag(String flagPlusDesc)
	{
		if(flagPlusDesc==null) return;
		flagPlusDesc=flagPlusDesc.trim();
		if(flagPlusDesc.length()==0) return;
		int x=flagPlusDesc.indexOf(' ');
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
	public void delBlurbFlag(String flagOnly)
	{
		if(flagOnly==null) return;
		flagOnly=flagOnly.toUpperCase().trim();
		if(flagOnly.length()==0) return;
		blurbFlags.remove(flagOnly);
	}

	public long expirationDate(){return expirationDate;}
	public void setExpirationDate(long time){expirationDate=time;}
	protected Climate climateObj=(Climate)CMClass.getCommon("DefaultClimate");
	public void setClimateObj(Climate obj){climateObj=obj;}
	public Climate getClimateObj()
	{
		return climateObj;
	}
	protected TimeClock myClock=null;
	public void setTimeObj(TimeClock obj){myClock=obj;}
	public TimeClock getTimeObj()
	{
		if(myClock==null) myClock=CMLib.time().globalClock();
		return myClock;
	}

	public StdArea()
	{
		super();
		//CMClass.bumpCounter(this,CMClass.CMObjectType.AREA);//removed for mem & perf
		xtraValues=CMProps.getExtraStatCodesHolder(this);
	}
	//protected void finalize(){CMClass.unbumpCounter(this,CMClass.CMObjectType.AREA);}//removed for mem & perf
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
		children=null;
		parents=null;
		initializedArea=true;
		childrenToLoad=new SLinkedList<String>();
		parentsToLoad=new SLinkedList<String>();
		blurbFlags=new STreeMap<String,String>();
		subOps=new SVector<String>(1);
		properRooms=new STreeMap();
		//metroRooms=null;
		myClock=null;
		climateObj=null;
		properRoomIDSet=null;
		metroRoomIDSet=null;
		author="";
		currency="";
		devalueRate="";
		budget="";
		ignoreMask="";
		prejudiceFactors="";
	}
	
	public boolean amDestroyed(){return amDestroyed;}
	public boolean isSavable()
	{    
		return ((!amDestroyed) 
				&& (!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
				&& (CMLib.flags().isSavable(this)));
	}
	public void setSavable(boolean truefalse){CMLib.flags().setSavable(this, truefalse);}

	public String name()
	{
		if(phyStats().newName()!=null) return phyStats().newName();
		return name;
	}
	public synchronized RoomnumberSet getProperRoomnumbers()
	{
		if(properRoomIDSet==null)
			properRoomIDSet=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		return properRoomIDSet;
	}
	public RoomnumberSet getCachedRoomnumbers()
	{
		RoomnumberSet set=(RoomnumberSet)CMClass.getCommon("DefaultRoomnumberSet");
		synchronized(properRooms)
		{
			for(Room R : properRooms.values())
				if(R.roomID().length()>0)
					set.add(R.roomID());
		}
		return set;
	}
	public void setName(String newName){name=newName;}
	public String Name(){return name;}
	public PhyStats phyStats()
	{
		return phyStats;
	}
	public PhyStats basePhyStats()
	{
		return basePhyStats;
	}
	public void recoverPhyStats()
	{
		basePhyStats.copyInto(phyStats);
		eachEffect(new EachApplicable<Ability>(){ public final void apply(final Ability A) {
			A.affectPhyStats(me,phyStats);
        } });
	}
	public void setBasePhyStats(PhyStats newStats)
	{
		basePhyStats=(PhyStats)newStats.copyOf();
	}
	public int getTheme(){return techLevel;}
	public void setTheme(int level){techLevel=level;}

	public String getArchivePath(){return archPath;}
	public void setArchivePath(String pathFile){archPath=pathFile;}

	public String image(){return imageName;}
	public String rawImage(){return imageName;}
	public void setImage(String newImage){imageName=newImage;}

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
	}
	public State getAreaState(){return flag;}

	public boolean amISubOp(String username)
	{
		for(int s=subOps.size()-1;s>=0;s--)
		{
			if(subOps.elementAt(s).equalsIgnoreCase(username))
				return true;
		}
		return false;
	}
	public String getSubOpList()
	{
		StringBuffer list=new StringBuffer("");
		for(int s=subOps.size()-1;s>=0;s--)
		{
			String str=subOps.elementAt(s);
			list.append(str);
			list.append(";");
		}
		return list.toString();
	}
	public void setSubOpList(String list)
	{
		subOps.clear();
		subOps.addAll(CMParms.parseSemicolons(list,true));
	}
	public void addSubOp(String username){subOps.addElement(username);}
	public void delSubOp(String username)
	{
		for(int s=subOps.size()-1;s>=0;s--)
		{
			if(subOps.elementAt(s).equalsIgnoreCase(username))
				subOps.removeElementAt(s);
		}
	}

	public String getNewRoomID(Room startRoom, int direction)
	{
		int highest=Integer.MIN_VALUE;
		int lowest=Integer.MAX_VALUE;
		CMIntegerGrouper set=(CMIntegerGrouper)CMClass.getCommon("DefaultCMIntegerGrouper");
		try
		{
			String roomID=null;
			int newnum=0;
			String name=Name().toUpperCase();
			for(Enumeration i=CMLib.map().roomIDs();i.hasMoreElements();)
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
							if(newnum>=highest)    highest=newnum;
							if(newnum<=lowest) lowest=newnum;
							set.addx(newnum);
						}
					}
				}
			}
		}catch(NoSuchElementException e){}
		if(highest<0)
			for(int i=0;i<Integer.MAX_VALUE;i++)
			{
				if((CMLib.map().getRoom(Name()+"#"+i))==null)
					return Name()+"#"+i;
			}
		if(lowest>highest) lowest=highest+1;
		for(int i=lowest;i<=highest+1000;i++)
		{
			if((!set.contains(i))
			&&(CMLib.map().getRoom(Name()+"#"+i)==null))
				return Name()+"#"+i;
		}
		return Name()+"#"+(int)Math.round(Math.random()*Integer.MAX_VALUE);
	}


	public CMObject newInstance()
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.FATAREAS)
		&&(ID().equals("StdArea")))
		{
			Area A=CMClass.getAreaType("StdThinArea");
			if(A!=null) return A;
		}
		try
		{
			return this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdArea();
	}
	public boolean isGeneric(){return false;}
	protected void cloneFix(StdArea areaA)
	{
		basePhyStats=(PhyStats)areaA.basePhyStats().copyOf();
		phyStats=(PhyStats)areaA.phyStats().copyOf();
		properRooms    =new STreeMap<String, Room>(new RoomIDComparator());
		properRoomIDSet=null;
		metroRoomIDSet =null;

		if(areaA.parents==null)
			parents=null;
		else
			parents=areaA.parents.copyOf();
		if(areaA.children==null)
			children=null;
		else
			children=areaA.children.copyOf();
		initializedArea=areaA.initializedArea;
		if(areaA.blurbFlags!=null)
			blurbFlags=areaA.blurbFlags.copyOf();
		affects=new SVector<Ability>(1);
		behaviors=new SVector<Behavior>(1);
		scripts=new SVector<ScriptingEngine>(1);
		for(Enumeration<Behavior> e=areaA.behaviors();e.hasMoreElements();)
		{
			Behavior B=e.nextElement();
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
		for(Enumeration<ScriptingEngine> e=areaA.scripts();e.hasMoreElements();)
		{
			SE=e.nextElement();
			if(SE!=null)
				addScript((ScriptingEngine)SE.copyOf());
		}
		setSubOpList(areaA.getSubOpList());
	}
	public CMObject copyOf()
	{
		try
		{
			StdArea E=(StdArea)this.clone();
			//CMClass.bumpCounter(this,CMClass.CMObjectType.AREA);//removed for mem & perf
			E.xtraValues=(xtraValues==null)?null:(String[])xtraValues.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}
	public String displayText(){return "";}
	public void setDisplayText(String newDisplayText){}
	public String finalPrejudiceFactors()
	{
		String s=finalPrejudiceFactors(this);
		if(s.length()>0) return s;
		return CMProps.getVar(CMProps.Str.IGNOREMASK);
	}
	protected String finalPrejudiceFactors(Area A)
	{
		if(A.prejudiceFactors().length()>0) return A.prejudiceFactors();
		for(Enumeration<Area> i=A.getParents();i.hasMoreElements();)
		{ 
			final String  s=finalPrejudiceFactors(i.nextElement()); 
			if(s.length()!=0) 
				return s;
		}
		return "";
	}
	public String prejudiceFactors(){return prejudiceFactors;}
	public void setPrejudiceFactors(String factors){prejudiceFactors=factors;}
	protected String[] itemPricingAdjustments=new String[0];
	protected final static String[] empty=new String[0];
	public String[] finalItemPricingAdjustments()
	{
		final String[] s=finalItemPricingAdjustments(this);
		if(s.length>0) return s;
		return CMParms.toStringArray(CMParms.parseSemicolons(CMProps.getVar(CMProps.Str.PRICEFACTORS).trim(),true));
	}
	
	protected String[] finalItemPricingAdjustments(Area A)
	{
		if(A.itemPricingAdjustments().length>0) 
			return A.itemPricingAdjustments();
		for(Enumeration<Area> i=A.getParents();i.hasMoreElements();)
		{ 
			final String[] s=finalItemPricingAdjustments(i.nextElement()); 
			if(s.length!=0) 
				return s;
		}
		return empty;
	}
	
	public String[] itemPricingAdjustments(){return itemPricingAdjustments;}
	public void setItemPricingAdjustments(String[] factors){itemPricingAdjustments=factors;}
	public String finalIgnoreMask()
	{
		String s=finalIgnoreMask(this);
		if(s.length()>0) return s;
		return CMProps.getVar(CMProps.Str.IGNOREMASK);
	}
	protected String finalIgnoreMask(Area A)
	{
		if(A.ignoreMask().length()>0) 
			return A.ignoreMask();
		for(Enumeration<Area> i=A.getParents();i.hasMoreElements();)
		{ 
			 final String s=finalIgnoreMask(i.nextElement()); 
			if(s.length()!=0) 
				return s;
		}
		return "";
	}
	public String ignoreMask(){return ignoreMask;}
	public void setIgnoreMask(String factors){ignoreMask=factors;}
	public String finalBudget()
	{
		final String s=finalBudget(this);
		if(s.length()>0) return s;
		return CMProps.getVar(CMProps.Str.BUDGET);
	}
	protected String finalBudget(Area A)
	{
		if(A.budget().length()>0) 
			return A.budget();
		for(Enumeration<Area> i=A.getParents();i.hasMoreElements();)
		{ 
			final String s=finalBudget(i.nextElement()); 
			if(s.length()!=0) 
				return s;
		}
		return "";
	}
	public String budget(){return budget;}
	public void setBudget(String factors){budget=factors;}
	public String finalDevalueRate()
	{
		final String s=finalDevalueRate(this);
		if(s.length()>0) return s;
		return CMProps.getVar(CMProps.Str.DEVALUERATE);
	}
	protected String finalDevalueRate(Area A)
	{
		if(A.devalueRate().length()>0) 
			return A.devalueRate();
		for(Enumeration<Area> i=A.getParents();i.hasMoreElements();)
		{ 
			final String s=finalDevalueRate(i.nextElement()); 
			if(s.length()!=0) 
				return s;
		}
		return "";
	}
	public String devalueRate(){return devalueRate;}
	public void setDevalueRate(String factors){devalueRate=factors;}
	protected int invResetRate=0;
	public int invResetRate(){return invResetRate;}
	public void setInvResetRate(int ticks){invResetRate=ticks;}
	public int finalInvResetRate()
	{
		int x=finalInvResetRate(this);
		if(x!=0) return x;
		return CMath.s_int(CMProps.getVar(CMProps.Str.INVRESETRATE));
	}
	
	protected int finalInvResetRate(Area A)
	{
		if(A.invResetRate()!=0) 
			return A.invResetRate();
		for(Enumeration<Area> i=A.getParents();i.hasMoreElements();)
		{ 
			final int x=finalInvResetRate(i.nextElement()); 
			if(x!=0) 
				return x;
		}
		return 0;
	}

	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public String miscTextFormat(){return CMParms.FORMAT_UNDEFINED;}
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,true);
	}
	public void setMiscText(String newMiscText)
	{
		miscText="";
		if(newMiscText.trim().length()>0)
			CMLib.coffeeMaker().setPropertiesStr(this,newMiscText,true);
	}

	public String description()
	{ return description;}
	public void setDescription(String newDescription)
	{ description=newDescription;}
	public int climateType(){return climateID;}
	public void setClimateType(int newClimateType){    climateID=newClimateType;}

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
			for(final Iterator<Area> a=parents.iterator();a.hasNext();)
				if(!a.next().okMessage(myHost,msg))
					return false;

		if((getTheme()>0)&&(!CMath.bset(getTheme(),Area.THEME_FANTASY)))
		{
			if((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MAGIC))
			||(CMath.bset(msg.targetMajor(),CMMsg.MASK_MAGIC))
			||(CMath.bset(msg.othersMajor(),CMMsg.MASK_MAGIC)))
			{
				Room room=null;
				if((msg.target()!=null)
				&&(msg.target() instanceof MOB)
				&&(((MOB)msg.target()).location()!=null))
					room=((MOB)msg.target()).location();
				else
				if((msg.source()!=null)
				&&(msg.source().location()!=null))
					room=msg.source().location();
				if(room!=null)
				{
					if(room.getArea()==this)
						room.showHappens(CMMsg.MSG_OK_VISUAL,"Magic doesn't seem to work here.");
					else
						room.showHappens(CMMsg.MSG_OK_VISUAL,"Magic doesn't seem to work there.");
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
					if((msg.target()!=null)
					&&(msg.target() instanceof MOB)
					&&(((MOB)msg.target()).location()!=null))
						room=((MOB)msg.target()).location();
					else
					if((msg.source()!=null)
					&&(msg.source().location()!=null))
						room=msg.source().location();
					if(room!=null)
						room.showHappens(CMMsg.MSG_OK_VISUAL,"Technology doesn't seem to work here.");
					return false;
				}
				break;
			}
		}
		return true;
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		eachBehavior(new EachApplicable<Behavior>(){ public final void apply(final Behavior B){
			B.executeMsg(me,msg);
		} });
		eachScript(new EachApplicable<ScriptingEngine>(){ public final void apply(final ScriptingEngine S){
			S.executeMsg(me,msg);
		} });
		eachEffect(new EachApplicable<Ability>(){ public final void apply(final Ability A) {
			A.executeMsg(me,msg);
        } });

		if((msg.sourceMinor()==CMMsg.TYP_RETIRE)
		&&(amISubOp(msg.source().Name())))
			delSubOp(msg.source().Name());

		if(parents!=null)
			for(final Iterator<Area> a=parents.iterator();a.hasNext();)
				a.next().executeMsg(myHost,msg);
	}

	public long getTickStatus(){ return tickStatus;}

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
			eachBehavior(new EachApplicable<Behavior>(){ public final void apply(final Behavior B){
				B.tick(ticking,tickID);
			} });
			tickStatus=Tickable.STATUS_SCRIPT;
			eachScript(new EachApplicable<ScriptingEngine>(){ public final void apply(final ScriptingEngine S){
				S.tick(ticking,tickID);
			} });
			tickStatus=Tickable.STATUS_AFFECT;
			eachEffect(new EachApplicable<Ability>(){ public final void apply(final Ability A) {
				if(!A.tick(ticking,tickID))
					A.unInvoke();
	        } });
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}

	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		final int senses=phyStats.sensesMask()&(~(PhyStats.SENSE_UNLOCATABLE|PhyStats.CAN_NOT_SEE));
		if(senses>0) affectableStats.setSensesMask(affectableStats.sensesMask()|senses);
		int disposition=phyStats().disposition()
			&((~(PhyStats.IS_SLEEPING|PhyStats.IS_HIDDEN)));
		if((affected instanceof Room)
		&&(CMLib.map().hasASky((Room)affected)))
		{
			Climate C=getClimateObj();
			if(((C==null)
				||(((C.weatherType((Room)affected)==Climate.WEATHER_BLIZZARD)
						||(C.weatherType((Room)affected)==Climate.WEATHER_DUSTSTORM))
						&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.DARKWEATHER)))
				||((getTimeObj().getTODCode()==TimeClock.TIME_NIGHT)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.DARKNIGHTS))))
			&&((disposition&PhyStats.IS_LIGHTSOURCE)==0))
				disposition=disposition|PhyStats.IS_DARK;
		}
		if(disposition>0)
			affectableStats.setDisposition(affectableStats.disposition()|disposition);
		affectableStats.setWeight(affectableStats.weight()+phyStats().weight());
		eachEffect(new EachApplicable<Ability>(){ public final void apply(final Ability A) {
			if(A.bubbleAffect()) A.affectPhyStats(affected,affectableStats);
        } });
	}
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
		eachEffect(new EachApplicable<Ability>(){ public final void apply(final Ability A) {
			if(A.bubbleAffect()) A.affectCharStats(affectedMob,affectableStats);
        }});
	}
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
		eachEffect(new EachApplicable<Ability>(){ public final void apply(final Ability A) {
			if(A.bubbleAffect()) A.affectCharState(affectedMob,affectableMaxState);
        } });
	}

	public void addNonUninvokableEffect(Ability to)
	{
		if(to==null) return;
		if(fetchEffect(to.ID())!=null) return;
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addEffect(Ability to)
	{
		if(to==null) return;
		if(fetchEffect(to.ID())!=null) return;
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void delEffect(Ability to)
	{
		int size=affects.size();
		affects.removeElement(to);
		if(affects.size()<size)
			to.setAffectedOne(null);
	}
	public void eachEffect(final EachApplicable<Ability> applier)
	{
		final List<Ability> affects=this.affects;
		if(affects==null) return;
		try{
			for(int a=0;a<affects.size();a++)
			{
				final Ability A=affects.get(a);
				if(A!=null) applier.apply(A);
			}
		} catch(ArrayIndexOutOfBoundsException e){}
	}
	public void delAllEffects(boolean unInvoke)
	{
		for(int a=numEffects()-1;a>=0;a--)
		{
			Ability A=fetchEffect(a);
			if(A!=null)
			{
				if(unInvoke) A.unInvoke();
				A.setAffectedOne(null);
			}
		}
		affects.clear();
	}
	public int numEffects()
	{
		return (affects==null)?0:affects.size();
	}
	
	public Enumeration<Ability> effects(){return (affects==null)?EmptyEnumeration.INSTANCE:affects.elements();}
	
	public Ability fetchEffect(int index)
	{
		try
		{
			return affects.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
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
	public boolean inMyMetroArea(Area A)
	{
		if(A==this) 
			return true;
		for(final Iterator<Area> i=getChildrenIterator();i.hasNext();)
			if(i.next().inMyMetroArea(A))
				return true;
		return false;
	}

	public void fillInAreaRooms()
	{
		for(Enumeration<Room> r=getProperMap();r.hasMoreElements();)
		{
			Room R=r.nextElement();
			R.clearSky();
			if(R.roomID().length()>0)
			{
				if(R instanceof GridLocale)
					((GridLocale)R).buildGrid();
			}
		}
		for(Enumeration<Room> r=getProperMap();r.hasMoreElements();)
		{
			Room R=r.nextElement();
			R.giveASky(0);
		}
	}

	public void fillInAreaRoom(Room R)
	{
		if(R==null) return;
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
	public void addBehavior(Behavior to)
	{
		if(to==null) return;
		for(Behavior B : behaviors)
			if((B!=null)&&(B.ID().equals(to.ID())))
				return;
		to.startBehavior(this);
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		behaviors.removeElement(to);
	}
	public void delAllBehaviors()
	{
		behaviors.clear();
	}
	public int numBehaviors()
	{
		return behaviors.size();
	}
	public Enumeration<Behavior> behaviors() { return behaviors.elements();}

	/** Manipulation of the scripts list */
	public void addScript(ScriptingEngine S)
	{
		if(S==null) return;
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
	public void delScript(ScriptingEngine S)
	{
		scripts.removeElement(S);
	}
	public int numScripts(){return scripts.size();}
	public Enumeration<ScriptingEngine> scripts() { return scripts.elements();}
	public ScriptingEngine fetchScript(int x){try{return scripts.elementAt(x);}catch(Exception e){} return null;}
	public void delAllScripts() { scripts.clear(); }
	public void eachScript(final EachApplicable<ScriptingEngine> applier)
	{
		final List<ScriptingEngine> scripts=this.scripts;
		if(scripts!=null)
		try{
			for(int a=0;a<scripts.size();a++)
			{
				final ScriptingEngine S=scripts.get(a);
				if(S!=null) applier.apply(S);
			}
		} catch(ArrayIndexOutOfBoundsException e){}
	}

	public int maxRange(){return Integer.MAX_VALUE;}
	public int minRange(){return Integer.MIN_VALUE;}

	protected int[] buildAreaIStats()
	{
		List<Integer> levelRanges=new Vector<Integer>();
		List<Integer> alignRanges=new Vector<Integer>();
		Faction theFaction=null;
		for(Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
		{
			Faction F=e.nextElement();
			if(F.showInSpecialReported())
				theFaction=F;
		}
		int[] statData=new int[Area.Stats.values().length];
		statData[Area.Stats.POPULATION.ordinal()]=0;
		statData[Area.Stats.MIN_LEVEL.ordinal()]=Integer.MAX_VALUE;
		statData[Area.Stats.MAX_LEVEL.ordinal()]=Integer.MIN_VALUE;
		statData[Area.Stats.AVG_LEVEL.ordinal()]=0;
		statData[Area.Stats.MED_LEVEL.ordinal()]=0;
		statData[Area.Stats.AVG_ALIGNMENT.ordinal()]=0;
		statData[Area.Stats.TOTAL_LEVELS.ordinal()]=0;
		statData[Area.Stats.TOTAL_INTELLIGENT_LEVELS.ordinal()]=0;
		statData[Area.Stats.VISITABLE_ROOMS.ordinal()]=getProperRoomnumbers().roomCountAllAreas();
		long totalAlignments=0;
		Room R=null;
		MOB mob=null;
		for(Enumeration<Room> r=getProperMap();r.hasMoreElements();)
		{
			R=r.nextElement();
			if(R instanceof GridLocale)
				statData[Area.Stats.VISITABLE_ROOMS.ordinal()]--;
			if((R.domainType()&Room.INDOORS)>0)
				statData[Area.Stats.INDOOR_ROOMS.ordinal()]++;
			for(int i=0;i<R.numInhabitants();i++)
			{
				mob=R.fetchInhabitant(i);
				if((mob!=null)&&(mob.isMonster()))
				{
					int lvl=mob.basePhyStats().level();
					levelRanges.add(Integer.valueOf(lvl));
					if((theFaction!=null)&&(mob.fetchFaction(theFaction.factionID())!=Integer.MAX_VALUE))
					{
						alignRanges.add(Integer.valueOf(mob.fetchFaction(theFaction.factionID())));
						totalAlignments+=mob.fetchFaction(theFaction.factionID());
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
			statData[Area.Stats.MED_ALIGNMENT.ordinal()]=alignRanges.get((int)Math.round(Math.floor(CMath.div(alignRanges.size(),2.0)))).intValue();
			statData[Area.Stats.AVG_LEVEL.ordinal()]=(int)Math.round(CMath.div(statData[Area.Stats.TOTAL_LEVELS.ordinal()],statData[Area.Stats.POPULATION.ordinal()]));
			statData[Area.Stats.AVG_ALIGNMENT.ordinal()]=(int)Math.round(((double)totalAlignments)/((double)statData[Area.Stats.POPULATION.ordinal()]));
		}
		return statData;
	}
	
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
		StringBuffer s=new StringBuffer("^N");
		s.append(description()+"\n\r");
		if(author.length()>0)
			s.append("Author         : ^H"+author+"^N\n\r");
		if(statData == emptyStats)
		{
			s.append("\n\r^HFurther information about this area is not available at this time.^N\n\r");
			return s;
		}
		s.append("Number of rooms: ^H"+statData[Area.Stats.VISITABLE_ROOMS.ordinal()]+"^N\n\r");
		Faction theFaction=null;
		for(Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
		{
			Faction F=e.nextElement();
			if(F.showInSpecialReported())
				theFaction=F;
		}
		if(statData[Area.Stats.POPULATION.ordinal()]==0)
		{
			if(getProperRoomnumbers().roomCountAllAreas()/2<properRooms.size())
				s.append("Population     : ^H0^N\n\r");
		}
		else
		{
			s.append("Population     : ^H"+statData[Area.Stats.POPULATION.ordinal()]+"^N\n\r");
			String currName=CMLib.beanCounter().getCurrency(this);
			if(currName.length()>0)
				s.append("Currency       : ^H"+CMStrings.capitalizeAndLower(currName)+"^N\n\r");
			else
				s.append("Currency       : ^HGold coins (default)^N\n\r");
			LegalBehavior B=CMLib.law().getLegalBehavior(this);
			if(B!=null)
			{
				String ruler=B.rulingOrganization();
				if(ruler.length()>0)
				{
					Clan C=CMLib.clans().getClan(ruler);
					if(C!=null)
						s.append("Controlled by  : ^H"+C.getGovernmentName()+" "+C.name()+"^N\n\r");
				}
			}
			s.append("Level range    : ^H"+statData[Area.Stats.MIN_LEVEL.ordinal()]+"^N to ^H"+statData[Area.Stats.MAX_LEVEL.ordinal()]+"^N\n\r");
			s.append("Average level  : ^H"+statData[Area.Stats.AVG_LEVEL.ordinal()]+"^N\n\r");
			s.append("Median level   : ^H"+statData[Area.Stats.MED_LEVEL.ordinal()]+"^N\n\r");
			if(theFaction!=null) s.append("Avg. "+CMStrings.padRight(theFaction.name(),10)+": ^H"+theFaction.fetchRangeName(statData[Area.Stats.AVG_ALIGNMENT.ordinal()])+"^N\n\r");
			if(theFaction!=null) s.append("Med. "+CMStrings.padRight(theFaction.name(),10)+": ^H"+theFaction.fetchRangeName(statData[Area.Stats.MED_ALIGNMENT.ordinal()])+"^N\n\r");
			try{
				boolean blurbed=false;
				String flag=null;
				for(Enumeration<String> f= allBlurbFlags();f.hasMoreElements();)
				{
					flag=getBlurbFlag(f.nextElement());
					if(flag!=null)
					{
						if(!blurbed){blurbed=true; s.append("\n\r");}
						s.append(flag+"\n\r");
					}
				}
				if(blurbed) s.append("\n\r");
			}catch(Exception e){}
		}
		return s;
	}
	
	public synchronized StringBuffer getAreaStats()
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return new StringBuffer("");
		StringBuffer s=(StringBuffer)Resources.getResource("HELP_"+Name().toUpperCase());
		if(s!=null) return s;
		s=buildAreaStats(getAreaIStats());
		//Resources.submitResource("HELP_"+Name().toUpperCase(),s); // the STAT_ data is cached instead.
		return s;
	}

	public Behavior fetchBehavior(int index)
	{
		try
		{
			return behaviors.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Behavior fetchBehavior(String ID)
	{
		for(Behavior B : behaviors)
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				return B;
		return null;
	}
	public void eachBehavior(final EachApplicable<Behavior> applier)
	{
		final List<Behavior> behaviors=this.behaviors;
		if(behaviors!=null)
		try{
			for(int a=0;a<behaviors.size();a++)
			{
				final Behavior B=behaviors.get(a);
				if(B!=null) applier.apply(B);
			}
		} catch(ArrayIndexOutOfBoundsException e){}
	}

	public int properSize()
	{
		synchronized(properRooms)
		{
			return properRooms.size();
		}
	}
	public void setProperRoomnumbers(RoomnumberSet set){ properRoomIDSet=set;}
	
	public void addProperRoom(Room R)
	{
		if(R==null) return;
		if(R.getArea()!=this)
		{
			R.setArea(this);
			return;
		}
		synchronized(properRooms)
		{
			String roomID=R.roomID();
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
	public void addProperRoomnumber(String roomID)
	{
		if((roomID!=null)&&(roomID.length()>0))
		{
			getProperRoomnumbers().add(roomID);
			addMetroRoomnumber(roomID);
		}
	}
	public void delProperRoomnumber(String roomID)
	{
		if((roomID!=null)&&(roomID.length()>0))
		{
			getProperRoomnumbers().remove(roomID);
			delMetroRoomnumber(roomID);
		}
	}
	public void addMetroRoomnumber(String roomID)
	{
		if(metroRoomIDSet==null)
			metroRoomIDSet=(RoomnumberSet)getProperRoomnumbers().copyOf();
		if((roomID!=null)&&(roomID.length()>0)&&(!metroRoomIDSet.contains(roomID)))
		{
			metroRoomIDSet.add(roomID);
			if(!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
				for(final Iterator<Area> a=getParentsReverseIterator();a.hasNext();)
					a.next().addMetroRoomnumber(roomID);
		}
	}
	public void delMetroRoomnumber(String roomID)
	{
		if((metroRoomIDSet!=null)
		&&(roomID!=null)
		&&(roomID.length()>0)
		&&(metroRoomIDSet.contains(roomID)))
		{
			metroRoomIDSet.remove(roomID);
			if(!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
				for(final Iterator<Area> a=getParentsReverseIterator();a.hasNext();)
					a.next().delMetroRoomnumber(roomID);
		}
	}
	public boolean isRoom(Room R)
	{
		if(R==null) return false;
		if(R.roomID().length()>0)
			return getProperRoomnumbers().contains(R.roomID());
		return properRooms.containsValue(R);
	}
	
	public void delProperRoom(Room R)
	{
		if(R==null) return;
		if(R instanceof GridLocale)
			((GridLocale)R).clearGrid(null);
		synchronized(properRooms)
		{
			if(R.roomID().length()==0)
			{
				if((R.getGridParent()!=null)&&(R.getGridParent().roomID().length()>0))
				{
					String id=R.getGridParent().getGridChildCode(R);
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
				for(Map.Entry<String,Room> entry : properRooms.entrySet())
					if(entry.getValue()==R)
					{
						properRooms.remove(entry.getKey());
						delProperRoomnumber(entry.getKey());
					}
				delProperRoomnumber(R.roomID());
				delMetroRoom(R);
			}
		}
	}

	public Room getRoom(String roomID)
	{
		if(properRooms.size()==0) return null;
		if(roomID.length()==0) return null;
		synchronized(properRooms)
		{
			if(roomID.toUpperCase().startsWith(Name().toUpperCase()+"#"))
				roomID=Name()+roomID.substring(Name().length()); // for case sensitive situations
			return properRooms.get(roomID);
		}
	}

	public int metroSize()
	{
		int num=properSize();
		for(final Iterator<Area> a=getChildrenReverseIterator();a.hasNext();)
			num+=a.next().metroSize();
		return num;
	}
	
	public int numberOfProperIDedRooms()
	{
		int num=0;
		for(Enumeration e=getProperMap();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			if(R.roomID().length()>0)
				if(R instanceof GridLocale)
					num+=((GridLocale)R).xGridSize()*((GridLocale)R).yGridSize();
				else
					num++;
		}
		return num;
	}
	
	public boolean isProperlyEmpty()
	{
		return getProperRoomnumbers().isEmpty();
	}
	
	public Room getRandomProperRoom()
	{
		String roomID=getProperRoomnumbers().random();
		Room R=CMLib.map().getRoom(roomID);
		if(R instanceof GridLocale) return ((GridLocale)R).getRandomGridChild();
		if(R==null) 
			Log.errOut("StdArea","Unable to random-find: "+roomID);
		return R;
	}
	
	public Room getRandomMetroRoom()
	{
		/*synchronized(metroRooms)
		{
			if(metroSize()==0) return null;
			Room R=(Room)metroRooms.elementAt(CMLib.dice().roll(1,metroRooms.size(),-1));
			if(R instanceof GridLocale) return ((GridLocale)R).getRandomGridChild();
			return R;
		}*/
		String roomID=metroRoomIDSet.random();
		Room R=CMLib.map().getRoom(roomID);
		if(R instanceof GridLocale) return ((GridLocale)R).getRandomGridChild();
		if(R==null) 
			Log.errOut("StdArea","Unable to random-metro-find: "+roomID);
		return R;
	}

	public Enumeration<Room> getProperMap()
	{
		return new CompleteRoomEnumerator(new IteratorEnumeration<Room>(properRooms.values().iterator()));
	}

	public Enumeration<Room> getFilledProperMap()
	{
		Enumeration<Room> r=getProperMap();
		Vector V=new Vector();
		Room R=null;
		Room R2=null;
		for(;r.hasMoreElements();)
		{
			R=r.nextElement();
			if(!V.contains(R))
				V.addElement(R);
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				R2=R.rawDoors()[d];
				if((R2 != null)&&((R2.roomID().length()==0)))
				{
					if(R2 instanceof GridLocale)
						V.addAll(((GridLocale)R2).getAllRooms());
					else
					if(!V.contains(R2))
						V.add(R2);
				}
			}
		}
		return V.elements();
	}
	
	public Enumeration<Room> getCompleteMap(){return getProperMap();}
	
	public Enumeration<Room> getMetroMap()
	{
		MultiEnumeration<Room> multiEnumerator = new MultiEnumeration<Room>(new IteratorEnumeration<Room>(properRooms.values().iterator()));
		for(Iterator<Area> a=getChildrenReverseIterator();a.hasNext();)
			multiEnumerator.addEnumeration(a.next().getMetroMap());
		return new CompleteRoomEnumerator(multiEnumerator);
	}
	
	public Enumeration<String> subOps()
	{
		return subOps.elements();
	}

	public void addChildToLoad(String str) { childrenToLoad.add(str);}
	public void addParentToLoad(String str) { parentsToLoad.add(str); }
	
	public SLinkedList<Area> loadAreas(Collection<String> loadableSet) 
	{
		final SLinkedList<Area> finalSet = new SLinkedList<Area>();
		for (final String areaName : loadableSet) 
		{
			Area A = CMLib.map().getArea(areaName);
			if (A == null)
				continue;
			finalSet.add(A);
		}
		return finalSet;
	}
	
	public synchronized void initializeAreaLink() 
	{
		if(initializedArea)
			return;
		SLinkedList<Area> futureParents=loadAreas(parentsToLoad);
		parents=new SLinkedList<Area>();
		children=new SLinkedList<Area>();
		for(Area parentA : futureParents)
			if(canParent(parentA))
				parents.add(parentA);
			else
				Log.errOut("StdArea","Can not make '"+parentA.name()+"' parent of '"+name+"'");
		SLinkedList<Area> futureChildren=loadAreas(childrenToLoad);
		for(Area childA : futureChildren)
			if(canChild(childA))
				children.add(childA);
			else
				Log.errOut("StdArea","Can not make '"+childA.name()+"' child of '"+name+"'");
		initializedArea=true;
	}
	
	protected final Iterator<Area> getParentsIterator()
	{
		if(!initializedArea) initializeAreaLink();
		return parents.iterator();
	}
	
	protected final Iterator<Area> getParentsReverseIterator()
	{
		if(!initializedArea) initializeAreaLink();
		return parents.descendingIterator();
	}
	
	protected final Iterator<Area> getChildrenIterator()
	{
		if(!initializedArea) initializeAreaLink();
		return children.iterator();
	}
	
	protected final Iterator<Area> getChildrenReverseIterator()
	{
		if(!initializedArea) initializeAreaLink();
		return children.descendingIterator();
	}
	
	public Enumeration<Area> getChildren() 
	{ 
		return new IteratorEnumeration<Area>(getChildrenIterator()); 
	}
	
	public String getChildrenList() 
	{
		final StringBuffer str=new StringBuffer("");
		for(final Iterator<Area> i=getChildrenIterator(); i.hasNext();) 
		{
			if(str.length()>0) str.append(";");
			str.append(i.next().name());
		}
		return str.toString();
	}

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
	
	public boolean isChild(Area area) 
	{
		for(final Iterator<Area> i=getChildrenIterator(); i.hasNext();) 
			if(i.next().equals(area))
			   return true;
		return false;
	}
	
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
	
	public void addChild(Area area) 
	{
		if(!canChild(area))
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
	}
	
	public void removeChild(Area area) 
	{ 
		if(isChild(area))
			children.remove(area);
	}
	
	// child based circular reference check
	public boolean canChild(Area area) 
	{
		if(parents != null)
			for(final Iterator<Area> a=parents.iterator(); a.hasNext(); )
			{
				final Area A=a.next();
				if(A==area) 
					return false;
				if(!A.canChild(area))
					return false;
			}
		return true;
	}

	// Parent
	public Enumeration<Area> getParents() 
	{ 
		return new IteratorEnumeration<Area>(getParentsIterator());
	}
	
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

	public String getParentsList() 
	{
		StringBuffer str=new StringBuffer("");
		for(final Iterator<Area> a=getParentsIterator();a.hasNext();)
		{
			final Area A=a.next();
			if(str.length()>0) str.append(";");
			str.append(A.name());
		}
		return str.toString();
	}

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
	
	public void removeParent(Area area) 
	{ 
		if(isParent(area))
			parents.remove(area);
	}
	
	public boolean canParent(Area area) 
	{
		if(children != null)
			for(final Iterator<Area> a=children.iterator(); a.hasNext(); )
			{
				final Area A=a.next();
				if(A==area) 
					return false;
				if(!A.canParent(area))
					return false;
			}
		return true;
	}

	public int getSaveStatIndex(){return (xtraValues==null)?getStatCodes().length:getStatCodes().length-xtraValues.length;}
	protected static final String[] STDAREACODES={"CLASS",
												  "CLIMATE",
												  "DESCRIPTION",
												  "TEXT",
												  "TECHLEVEL",
												  "BLURBS",
												  "PREJUDICE",
												  "BUDGET",
												  "DEVALRATE",
												  "INVRESETRATE",
												  "IGNOREMASK",
												  "PRICEMASKS"};
	private static String[] codes=null;
	public String[] getStatCodes()
	{
		if(codes==null)
			codes=CMProps.getStatCodesList(STDAREACODES,this);
		return codes; 
	}
	public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code){ return CMParms.indexOf(codes, code.toUpperCase());}
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+climateType();
		case 2: return description();
		case 3: return text();
		case 4: return ""+getTheme();
		case 5: return ""+CMLib.xml().getXMLList(blurbFlags.toStringVector(" "));
		case 6: return prejudiceFactors();
		case 7: return budget();
		case 8: return devalueRate();
		case 9: return ""+invResetRate();
		case 10: return ignoreMask();
		case 11: return CMParms.toStringList(itemPricingAdjustments());
		default: return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setClimateType(CMath.s_parseBitIntExpression(Area.CLIMATE_DESCS,val)); break;
		case 2: setDescription(val); break;
		case 3: setMiscText(val); break;
		case 4: setTheme(CMath.s_parseBitIntExpression(Area.THEME_DESCS,val)); break;
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
				List<String> V=CMLib.xml().parseXMLList(val);
				for(String s : V)
				{
					int x=s.indexOf(' ');
					if(x<0)
						blurbFlags.put(s,"");
					else
						blurbFlags.put(s.substring(0,x),s.substring(x+1));
				}
			}
			break;
		}
		case 6: setPrejudiceFactors(val); break;
		case 7: setBudget(val); break;
		case 8: setDevalueRate(val); break;
		case 9: setInvResetRate(CMath.s_parseIntExpression(val)); break;
		case 10: setIgnoreMask(val); break;
		case 11: setItemPricingAdjustments((val.trim().length()==0)?new String[0]:CMParms.toStringArray(CMParms.parseCommas(val,true))); break;
		default: CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val); break;
		}
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdArea)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
