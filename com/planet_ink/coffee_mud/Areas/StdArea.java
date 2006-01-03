package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2006 Bo Zimmerman

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
	public String ID(){	return "StdArea";}
	public long flags(){return 0;}
	protected String name="the area";
	protected String description="";
	protected String miscText="";
	protected String archPath="";
	protected String imageName="";
	protected int techLevel=0;
	protected int climateID=Area.CLIMASK_NORMAL;
	protected Vector properRooms=new Vector();
	//protected Vector metroRooms=new Vector();
	protected boolean mobility=true;
	protected long tickStatus=Tickable.STATUS_NOT;
	protected boolean stopTicking=false;
	protected long expirationDate=0;
	protected RoomnumberSet properRoomIDSet=null;
	protected RoomnumberSet metroRoomIDSet=null;

    protected Vector children=null;
    protected Vector parents=null;
    protected Vector childrenToLoad=new Vector();
    protected Vector parentsToLoad=new Vector();

	protected EnvStats envStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
	protected EnvStats baseEnvStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
	protected String author="";
	public void setAuthorID(String authorID){author=authorID;}
	public String getAuthorID(){return author;}
	protected String currency="";
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

	public long expirationDate(){return expirationDate;}
	public void setExpirationDate(long time){expirationDate=time;}
	protected Vector affects=new Vector();
	protected Vector behaviors=new Vector();
	protected Vector subOps=new Vector();
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
        if(myClock==null) myClock=CMClass.globalClock();
        return myClock;
    }

	public StdArea()
	{
        super();
        CMClass.bumpCounter(CMClass.OBJECT_AREA);
	}
    protected void finalize(){CMClass.unbumpCounter(CMClass.OBJECT_AREA);}
    protected boolean amDestroyed=false;
    public void destroy()
    {
        envStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
        baseEnvStats=envStats;
        miscText=null;
        imageName=null;
        affects=null;
        behaviors=null;
        author=null;
        currency=null;
        children=null;
        parents=null;
        childrenToLoad=null;
        parentsToLoad=null;
        subOps=null;
        properRooms=null;
        //metroRooms=null;
        myClock=null;
        climateObj=null;
        properRoomIDSet=null;
        metroRoomIDSet=null;
        amDestroyed=true;
    }
    public boolean amDestroyed(){return amDestroyed;}
    public boolean savable(){return !amDestroyed;}
    
	public String name()
	{
		if(envStats().newName()!=null) return envStats().newName();
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
			Room R=null;
			for(int p=properRooms.size()-1;p>=0;p--)
			{
				R=(Room)properRooms.elementAt(p);
				if(R.roomID().length()>0)
					set.add(R.roomID());
			}
		}
		return set;
	}
	public void setName(String newName){name=newName;}
	public String Name(){return name;}
	public EnvStats envStats()
	{
		return envStats;
	}
	public EnvStats baseEnvStats()
	{
		return baseEnvStats;
	}
	public void recoverEnvStats()
	{
		envStats=(EnvStats)baseEnvStats.copyOf();
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if(A!=null)
				A.affectEnvStats(this,envStats);
		}
	}
	public void setBaseEnvStats(EnvStats newBaseEnvStats)
	{
		baseEnvStats=(EnvStats)newBaseEnvStats.copyOf();
	}
	public int getTechLevel(){return techLevel;}
	public void setTechLevel(int level){techLevel=level;}

	public String getArchivePath(){return archPath;}
	public void setArchivePath(String pathFile){archPath=pathFile;}

	public String image(){return imageName;}
    public String rawImage(){return imageName;}
	public void setImage(String newImage){imageName=newImage;}

	public boolean getMobility(){return mobility;}
	public void toggleMobility(boolean onoff){mobility=onoff;}
	public boolean amISubOp(String username)
	{
		for(int s=subOps.size()-1;s>=0;s--)
		{
			if(((String)subOps.elementAt(s)).equalsIgnoreCase(username))
				return true;
		}
		return false;
	}
	public String getSubOpList()
	{
		StringBuffer list=new StringBuffer("");
		for(int s=subOps.size()-1;s>=0;s--)
		{
			String str=((String)subOps.elementAt(s));
			list.append(str);
			list.append(";");
		}
		return list.toString();
	}
	public void setSubOpList(String list)
	{
		subOps=CMParms.parseSemicolons(list,true);
	}
	public void addSubOp(String username){subOps.addElement(username);}
	public void delSubOp(String username)
	{
		for(int s=subOps.size()-1;s>=0;s--)
		{
			if(((String)subOps.elementAt(s)).equalsIgnoreCase(username))
				subOps.removeElementAt(s);
		}
	}

	public String getNewRoomID(Room startRoom, int direction)
	{
		int highest=Integer.MIN_VALUE;
		int lowest=Integer.MAX_VALUE;
		Hashtable allNums=new Hashtable();
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((R.getArea().Name().equals(Name()))
				&&(R.roomID().startsWith(Name()+"#")))
				{
					int newnum=CMath.s_int(R.roomID().substring(Name().length()+1));
					if(newnum>=highest)	highest=newnum;
					if(newnum<=lowest) lowest=newnum;
					allNums.put(new Integer(newnum),R);
				}
			}
	    }catch(NoSuchElementException e){}
		if((highest<0)&&(CMLib.map().getRoom(Name()+"#0"))==null)
			return Name()+"#0";
		if(lowest>highest) lowest=highest+1;
		for(int i=lowest;i<=highest+1000;i++)
		{
			if((!allNums.containsKey(new Integer(i)))
			&&(CMLib.map().getRoom(Name()+"#"+i)==null))
				return Name()+"#"+i;
		}
		return Name()+"#"+Math.random();
	}


	public CMObject newInstance()
	{
		try
        {
			return (CMObject)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
        return new StdArea();
	}
	public boolean isGeneric(){return false;}
	protected void cloneFix(StdArea E)
	{
		baseEnvStats=(EnvStats)E.baseEnvStats().copyOf();
		envStats=(EnvStats)E.envStats().copyOf();

		parents=null;
		if(E.parents!=null)
			parents=(Vector)E.parents.clone();
		children=null;
		if(E.children!=null)
			children=(Vector)E.children.clone();
		affects=new Vector();
		behaviors=new Vector();
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null)
				behaviors.addElement(B);
		}
		for(int a=0;a<E.numEffects();a++)
		{
			Ability A=E.fetchEffect(a);
			if(A!=null)
				affects.addElement(A);
		}
		setSubOpList(E.getSubOpList());
	}
	public CMObject copyOf()
	{
		try
		{
			StdArea E=(StdArea)this.clone();
            CMClass.bumpCounter(CMClass.OBJECT_AREA);
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

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

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
	public void setClimateType(int newClimateType){	climateID=newClimateType;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(!B.okMessage(this,msg)))
				return false;
		}
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(!A.okMessage(this,msg)))
				return false;
		}
		if((!mobility)||(!CMLib.flags().allowsMovement(this)))
		{
			if((msg.sourceMinor()==CMMsg.TYP_ENTER)
			||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
			||(msg.sourceMinor()==CMMsg.TYP_FLEE))
				return false;
		}
		if(parents!=null)
		for(int i=0;i<parents.size();i++)
			if(!((Area)parents.elementAt(i)).okMessage(myHost,msg))
				return false;

		if((getTechLevel()>0)&&(!CMath.bset(getTechLevel(),Area.THEME_FANTASY)))
		{
			if((CMath.bset(msg.sourceCode(),CMMsg.MASK_MAGIC))
			||(CMath.bset(msg.targetCode(),CMMsg.MASK_MAGIC))
			||(CMath.bset(msg.othersCode(),CMMsg.MASK_MAGIC)))
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
		if((getTechLevel()>0)&&(!CMath.bset(getTechLevel(),Area.THEME_TECHNOLOGY)))
		{
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Electronics))
			{
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_BUY:
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
					break;
				default:
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
				}
			}
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if(B!=null)
				B.executeMsg(this,msg);
		}
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if(A!=null)
				A.executeMsg(this,msg);
		}
		if((msg.sourceMinor()==CMMsg.TYP_RETIRE)
		&&(amISubOp(msg.source().Name())))
			delSubOp(msg.source().Name());

		if(parents!=null)
		for(int i=0;i<parents.size();i++)
			((Area)parents.elementAt(i)).executeMsg(myHost,msg);
	}

	public void tickControl(boolean start)
	{
		if(start)
		{
			stopTicking=false;
			CMLib.threads().startTickDown(this,Tickable.TICKID_AREA,1);
		}
		else
			stopTicking=true;

	}

	public long getTickStatus(){ return tickStatus;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(stopTicking) return false;
		tickStatus=Tickable.STATUS_START;
		if(tickID==Tickable.TICKID_AREA)
		{
			getClimateObj().tick(this,tickID);
			getTimeObj().tick(this,tickID);
			for(int b=0;b<numBehaviors();b++)
			{
				tickStatus=Tickable.STATUS_BEHAVIOR+b;
				Behavior B=fetchBehavior(b);
				if(B!=null)
					B.tick(ticking,tickID);
			}

			int a=0;
			while(a<numEffects())
			{
				Ability A=fetchEffect(a);
				if(A!=null)
				{
					tickStatus=Tickable.STATUS_AFFECT+a;
					int s=affects.size();
					if(!A.tick(ticking,tickID))
						A.unInvoke();
					if(affects.size()==s)
						a++;
				}
				else
					a++;
			}
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(envStats().sensesMask()>0)
			affectableStats.setSensesMask(affectableStats.sensesMask()|envStats().sensesMask());
		int disposition=envStats().disposition()
			&((Integer.MAX_VALUE-(EnvStats.IS_SLEEPING|EnvStats.IS_HIDDEN)));
		if((affected instanceof Room)&&(CMLib.map().hasASky((Room)affected)))
		{
			if((getClimateObj().weatherType((Room)affected)==Climate.WEATHER_BLIZZARD)
			   ||(getClimateObj().weatherType((Room)affected)==Climate.WEATHER_DUSTSTORM)
			   ||(getTimeObj().getTODCode()==TimeClock.TIME_NIGHT))
				disposition=disposition|EnvStats.IS_DARK;
		}
		if(disposition>0)
			affectableStats.setDisposition(affectableStats.disposition()|disposition);
		affectableStats.setWeight(affectableStats.weight()+envStats().weight());
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}

	public void addNonUninvokableEffect(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A==to))
				return;
		}
		to.makeNonUninvokable();
		to.makeLongLasting();
		affects.addElement(to);
		to.setAffectedOne(this);
	}
	public void addEffect(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A==to))
				return;
		}
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
	public int numEffects()
	{
		return affects.size();
	}
	public Ability fetchEffect(int index)
	{
		try
		{
			return (Ability)affects.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchEffect(String ID)
	{
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
			if((A!=null)&&(A.ID().equals(ID)))
			   return A;
		}
		return null;
	}
	public boolean inMetroArea(Area A)
	{
		if(A==this) return true;
		if(getNumChildren()==0) return false;
		for(int i=0;i<getNumChildren();i++)
			if(getChild(i).inMetroArea(A))
				return true;
		return false;
	}

	public void fillInAreaRooms()
	{
		for(Enumeration r=getProperMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			R.clearSky();
			if(R.roomID().length()>0)
			{
				if(R instanceof GridLocale)
					((GridLocale)R).buildGrid();
			}
		}
		for(Enumeration r=getProperMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			R.clearSky();
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
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equals(to.ID())))
				return;
		}
        to.startBehavior(this);
		behaviors.addElement(to);
	}
	public void delBehavior(Behavior to)
	{
		behaviors.removeElement(to);
	}
	public int numBehaviors()
	{
		return behaviors.size();
	}
	public int maxRange(){return Integer.MAX_VALUE;}
	public int minRange(){return Integer.MIN_VALUE;}

	public int[] getAreaIStats()
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return new int[Area.AREASTAT_NUMBER];
		int[] s=(int[])Resources.getResource("STATS_"+Name().toUpperCase());
		if(s!=null) return s;
		Resources.removeResource("HELP_"+Name().toUpperCase());
		getAreaStats();
		s=(int[])Resources.getResource("STATS_"+Name().toUpperCase());
		if(s==null) return new int[Area.AREASTAT_NUMBER];
		return s;
	}
	public synchronized StringBuffer getAreaStats()
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return new StringBuffer("");
		StringBuffer s=(StringBuffer)Resources.getResource("HELP_"+Name().toUpperCase());
		if(s!=null) return s;
		s=new StringBuffer("");
		s.append(description()+"\n\r");
		if(author.length()>0)
			s.append("Author         : "+author+"\n\r");

		Vector levelRanges=new Vector();
		Vector alignRanges=new Vector();
		Faction theFaction=null;
		for(Enumeration e=CMLib.factions().factionSet().elements();e.hasMoreElements();)
		{
		    Faction F=(Faction)e.nextElement();
		    if(F.showinspecialreported())
		        theFaction=F;
		}
		int[] statData=new int[Area.AREASTAT_NUMBER];
		statData[Area.AREASTAT_POPULATION]=0;
		statData[Area.AREASTAT_MINLEVEL]=Integer.MAX_VALUE;
		statData[Area.AREASTAT_MAXLEVEL]=Integer.MIN_VALUE;
		statData[Area.AREASTAT_AVGLEVEL]=0;
		statData[Area.AREASTAT_MEDLEVEL]=0;
		statData[Area.AREASTAT_AVGALIGN]=0;
		statData[Area.AREASTAT_TOTLEVEL]=0;
		statData[Area.AREASTAT_INTLEVEL]=0;
        statData[Area.AREASTAT_VISITABLEROOMS]=getProperRoomnumbers().roomCountAllAreas();
        s.append("Number of rooms: "+statData[Area.AREASTAT_VISITABLEROOMS]+"\n\r");
		long totalAlignments=0;
        Room R=null;
        MOB mob=null;
		for(Enumeration r=getProperMap();r.hasMoreElements();)
		{
			R=(Room)r.nextElement();
			for(int i=0;i<R.numInhabitants();i++)
			{
				mob=R.fetchInhabitant(i);
				if((mob!=null)&&(mob.isMonster()))
				{
					int lvl=mob.baseEnvStats().level();
					levelRanges.addElement(new Integer(lvl));
					if((theFaction!=null)&&(mob.fetchFaction(theFaction.factionID())!=Integer.MAX_VALUE))
					{
					    alignRanges.addElement(new Integer(mob.fetchFaction(theFaction.factionID())));
					    totalAlignments+=mob.fetchFaction(theFaction.factionID());
					}
					statData[Area.AREASTAT_POPULATION]++;
					statData[Area.AREASTAT_TOTLEVEL]+=lvl;
					if(!CMLib.flags().isAnimalIntelligence(mob))
						statData[Area.AREASTAT_INTLEVEL]+=lvl;
					if(lvl<statData[Area.AREASTAT_MINLEVEL])
						statData[Area.AREASTAT_MINLEVEL]=lvl;
					if(lvl>statData[Area.AREASTAT_MAXLEVEL])
						statData[Area.AREASTAT_MAXLEVEL]=lvl;
				}
			}
		}
		if((statData[Area.AREASTAT_POPULATION]==0)||(levelRanges.size()==0))
		{
			statData[Area.AREASTAT_MINLEVEL]=0;
			statData[Area.AREASTAT_MAXLEVEL]=0;
			if(getProperRoomnumbers().roomCountAllAreas()/2<properRooms.size())
				s.append("Population     : 0\n\r");
		}
		else
		{
			Collections.sort(levelRanges);
			Collections.sort(alignRanges);
			statData[Area.AREASTAT_MEDLEVEL]=((Integer)levelRanges.elementAt((int)Math.round(Math.floor(CMath.div(levelRanges.size(),2.0))))).intValue();
			statData[Area.AREASTAT_MEDALIGN]=((Integer)alignRanges.elementAt((int)Math.round(Math.floor(CMath.div(alignRanges.size(),2.0))))).intValue();
			statData[Area.AREASTAT_AVGLEVEL]=(int)Math.round(CMath.div(statData[Area.AREASTAT_TOTLEVEL],statData[Area.AREASTAT_POPULATION]));
			statData[Area.AREASTAT_AVGALIGN]=(int)Math.round(new Long(totalAlignments).doubleValue()/new Integer(statData[Area.AREASTAT_POPULATION]).doubleValue());
			s.append("Population     : "+statData[Area.AREASTAT_POPULATION]+"\n\r");
            LegalBehavior B=CMLib.utensils().getLegalBehavior(this);
			if(B!=null)
			{
                String ruler=B.rulingClan();
                if(ruler.length()>0)
				{
					Clan C=CMLib.clans().getClan(ruler);
					if(C!=null)
						s.append("Controlled by  : "+C.typeName()+" "+C.name()+"\n\r");
				}
			}
			s.append("Level range    : "+statData[Area.AREASTAT_MINLEVEL]+" to "+statData[Area.AREASTAT_MAXLEVEL]+"\n\r");
			s.append("Average level  : "+statData[Area.AREASTAT_AVGLEVEL]+"\n\r");
			s.append("Median level   : "+statData[Area.AREASTAT_MEDLEVEL]+"\n\r");
			if(theFaction!=null) s.append("Avg. "+CMStrings.padRight(theFaction.name(),10)+": "+theFaction.fetchRangeName(statData[Area.AREASTAT_AVGALIGN])+"\n\r");
			if(theFaction!=null) s.append("Med. "+CMStrings.padRight(theFaction.name(),10)+": "+theFaction.fetchRangeName(statData[Area.AREASTAT_MEDALIGN])+"\n\r");
		}
		Resources.submitResource("STATS_"+Name().toUpperCase(),statData);
		Resources.submitResource("HELP_"+Name().toUpperCase(),s);
		return s;
	}

	public Behavior fetchBehavior(int index)
	{
		try
		{
			return (Behavior)behaviors.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Behavior fetchBehavior(String ID)
	{
		for(int b=0;b<numBehaviors();b++)
		{
			Behavior B=fetchBehavior(b);
			if((B!=null)&&(B.ID().equalsIgnoreCase(ID)))
				return B;
		}
		return null;
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
            if(!properRooms.contains(R))
            {
                addMetroRoom(R);
                addProperRoomnumber(R.roomID());
                Room R2=null;
                if(R.roomID().length()==0)
                {
                	properRooms.insertElementAt(R,0);
                	return;
                }
                for(int i=0;i<properRooms.size();i++)
                {
                    R2=(Room)properRooms.elementAt(i);
                    if(R2.roomID().compareToIgnoreCase(R.roomID())>=0)
                    {
                        if(R2.roomID().compareToIgnoreCase(R.roomID())==0)
                            properRooms.setElementAt(R,i);
                        else
                            properRooms.insertElementAt(R,i);
                        return;
                    }
                }
                properRooms.addElement(R);
            }
        }
    }
    
	public void addMetroRoom(Room R)
	{
		if(R!=null)
		{
			/*synchronized(metroRooms)
			{
				if(!metroRooms.contains(R)) 
					metroRooms.add(R);
			}/
			for(int p=getNumParents()-1;p>=0;p--)
				getParent(p).addMetroRoom(R);
			*/
			addMetroRoomnumber(R.roomID());
		}
	}
	public void delMetroRoom(Room R)
	{
		if(R!=null)
		{
			/*synchronized(metroRooms)
			{
				if(metroRooms.contains(R)) 
					metroRooms.remove(R);
			}
			for(int p=getNumParents()-1;p>=0;p--)
				getParent(p).delMetroRoom(R);
			*/
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
			for(int p=getNumParents()-1;p>=0;p--)
				getParent(p).addMetroRoomnumber(roomID);
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
			for(int p=getNumParents()-1;p>=0;p--)
				getParent(p).delMetroRoomnumber(roomID);
		}
    }
    public boolean isRoom(Room R)
    {
        if(R==null) return false;
        if(R.roomID().length()>0) 
        	return getProperRoomnumbers().contains(R.roomID());
        return properRooms.contains(R);
    }
    public void delProperRoom(Room R)
    {
        if(R==null) return;
        if(R instanceof GridLocale)
            ((GridLocale)R).clearGrid(null);
        synchronized(properRooms)
        {
            if(properRooms.contains(R))
            {
                properRooms.removeElement(R);
	            delMetroRoom(R);
	            delProperRoomnumber(R.roomID());
            }
        }
    }
    
    public Room getRoom(String roomID)
    {
        if(properRooms.size()==0) return null;
        synchronized(properRooms)
        {
	        int start=0;
	        int end=properRooms.size()-1;
	        while(start<=end)
	        {
	            int mid=(end+start)/2;
	            int comp=((Room)properRooms.elementAt(mid)).roomID().compareToIgnoreCase(roomID);
	            if(comp==0)
	                return (Room)properRooms.elementAt(mid);
	            else
	            if(comp>0)
	                end=mid-1;
	            else
	                start=mid+1;
	
	        }
        }
        return null;
    }
    
	public int metroSize()
	{
		int num=properSize();
		for(int c=getNumChildren()-1;c>=0;c--)
			num+=getChild(c).metroSize();
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
	public Room getRandomProperRoom()
	{
		synchronized(properRooms)
		{
			if(properSize()==0) return null;
			Room R=(Room)properRooms.elementAt(CMLib.dice().roll(1,properRooms.size(),-1));
			if(R instanceof GridLocale) return ((GridLocale)R).getRandomGridChild();
			return R;
		}
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
		Room R=getRoom(metroRoomIDSet.random()); 
		if(R instanceof GridLocale) return ((GridLocale)R).getRandomGridChild();
		return R;
	}

	public Enumeration getProperMap()
	{
		synchronized(properRooms)
		{
			return properRooms.elements();
		}
	}
	public Vector getMetroCollection()
	{
		Vector V=(Vector)properRooms.clone();
		V.ensureCapacity(metroSize());
		for(int c=getNumChildren()-1;c>=0;c--)
			V.addAll(getChild(c).getMetroCollection());
		return V;
		
	}
	public Enumeration getCompleteMap(){return getProperMap();}
	public Enumeration getMetroMap(){return getMetroCollection().elements();}
	public Vector getSubOpVectorList()
	{
		return subOps;
	}

    public void addChildToLoad(String str) { childrenToLoad.addElement(str);}
    public void addParentToLoad(String str) { parentsToLoad.addElement(str);}

	// Children
	public void initChildren()
	{
	    if(children==null)
		{
	        children=new Vector();
	        for(int i=0;i<childrenToLoad.size();i++)
			{
	          Area A=CMLib.map().getArea((String)childrenToLoad.elementAt(i));
	          if(A==null)
	            continue;
			children.addElement(A);
			}
		}
	}
	public Enumeration getChildren() { initChildren(); return children.elements(); }
	public String getChildrenList() {
	        initChildren();
	        StringBuffer str=new StringBuffer("");
	        for(Enumeration e=getChildren(); e.hasMoreElements();) {
	                Area A=(Area)e.nextElement();
	                if(str.length()>0) str.append(";");
	                str.append(A.name());
	        }
	        return str.toString();
	}

	public int getNumChildren() { initChildren(); return children.size(); }
	public Area getChild(int num) { initChildren(); return (Area)children.elementAt(num); }
	public Area getChild(String named) {
	        initChildren();
	        for(int i=0;i<children.size();i++){
	                Area A=(Area)children.elementAt(i);
	                if((A.name().equalsIgnoreCase(named))
	                   ||(A.Name().equalsIgnoreCase(named)))
	                       return A;
	        }
	        return null;
	}
	public boolean isChild(Area named) {
	        initChildren();
	        for(int i=0;i<children.size();i++){
	                Area A=(Area)children.elementAt(i);
	                if(A.equals(named))
	                       return true;
	        }
	        return false;
	}
	public boolean isChild(String named) {
	        initChildren();
	        for(int i=0;i<children.size();i++){
	                Area A=(Area)children.elementAt(i);
	                if((A.name().equalsIgnoreCase(named))
	                   ||(A.Name().equalsIgnoreCase(named)))
	                        return true;
	        }
	        return false;
	}
	public void addChild(Area Adopted) {
	        initChildren();
	        // So areas can load ok, the code needs to be able to replace 'dummy' children with 'real' ones
	        for(int i=0;i<children.size();i++){
	                Area A=(Area)children.elementAt(i);
	                if(A.Name().equalsIgnoreCase(Adopted.Name())){
	                        children.setElementAt(Adopted, i);
	                        return;
	                }
	        }
	        children.addElement(Adopted);
	}
	public void removeChild(Area Disowned) { initChildren(); children.removeElement(Disowned); }
	public void removeChild(int Disowned) { initChildren(); children.removeElementAt(Disowned); }
	// child based circular reference check
	public boolean canChild(Area newChild) {
	        initParents();
	        // Someone asked this area if newChild can be a child to them,
	        // which means this is a parent to someone.  If newChild is a
	        // parent, directly or indirectly, return false.
	        if(parents.contains(newChild))
	        {
	                return false; // It is directly a parent
	        }
	        for(int i=0;i<parents.size();i++) {
	                // check with all the parents about how they feel
	                Area rent=(Area)parents.elementAt(i);
	                // as soon as any parent says false, dump that false back to them
	                if(!(rent.canChild(newChild)))
	                {
	                        return false;
	                }
	        }
	        // no parent is the same as newChild, nor is it indirectly a parent.
	        // Go for it!
	        return true;
	}

	// Parent
	public void initParents() {
	        if (parents == null) {
	                parents = new Vector();
	                for (int i = 0; i < parentsToLoad.size(); i++) {
	                        Area A = CMLib.map().getArea( (String) parentsToLoad.elementAt(i));
	                        if (A == null)
	                                continue;
	                        parents.addElement(A);
	                }
	        }
	}
	public Enumeration getParents() { initParents(); return parents.elements(); }
    public Vector getParentsRecurse()
    {
        Vector V=new Vector();
        Area A=null;
        for(Enumeration e=getParents();e.hasMoreElements();)
        {
            A=(Area)e.nextElement();
            V.addElement(A);
            CMParms.addToVector(A.getParentsRecurse(),V);
        }
        return V;
    }
    
	public String getParentsList() {
	        initParents();
	        StringBuffer str=new StringBuffer("");
	        for(Enumeration e=getParents(); e.hasMoreElements();) {
	                Area A=(Area)e.nextElement();
	                if(str.length()>0) str.append(";");
	                str.append(A.name());
	        }
	        return str.toString();
	}

	public int getNumParents() { initParents(); return parents.size(); }
	public Area getParent(int num) { initParents(); return (Area)parents.elementAt(num); }
	public Area getParent(String named) {
	        initParents();
	        for(int i=0;i<parents.size();i++){
	                Area A=(Area)parents.elementAt(i);
	                if((A.name().equalsIgnoreCase(named))
	                   ||(A.Name().equalsIgnoreCase(named)))
	                       return A;
	        }
	        return null;
	}
	public boolean isParent(Area named) {
	        initParents();
	        for(int i=0;i<parents.size();i++){
	                Area A=(Area)parents.elementAt(i);
	                if(A.equals(named))
	                       return true;
	        }
	        return false;
	}
	public boolean isParent(String named) {
	        initParents();
	        for(int i=0;i<parents.size();i++){
	                Area A=(Area)parents.elementAt(i);
	                if((A.name().equalsIgnoreCase(named))
	                   ||(A.Name().equalsIgnoreCase(named)))
	                        return true;
	        }
	        return false;
	}
	public void addParent(Area Adopted) {
	        initParents();
	        for(int i=0;i<parents.size();i++){
	                Area A=(Area)parents.elementAt(i);
	                if(A.Name().equalsIgnoreCase(Adopted.Name())){
	                        parents.setElementAt(Adopted, i);
	                        return;
	                }
	        }
	        parents.addElement(Adopted);
	}
	public void removeParent(Area Disowned) { initParents();parents.removeElement(Disowned); }
	public void removeParent(int Disowned) { initParents();parents.removeElementAt(Disowned); }
	public boolean canParent(Area newParent) {
	        initChildren();
	        // Someone asked this area if newParent can be a parent to them,
	        // which means this is a child to someone.  If newParent is a
	        // child, directly or indirectly, return false.
	        if(children.contains(newParent))
	        {
	                return false; // It is directly a child, so it can't Parent
	        }
	        for(int i=0;i<children.size();i++) {
	                // check with all the children about how they feel
	                Area child=(Area)children.elementAt(i);
	                // as soon as any child says false, dump that false back to them
	                if(!(child.canParent(newParent)))
	                {
	                        return false;
	                }
	        }
	        // no child is the same as newParent, nor is it indirectly a child.
	        // Go for it!
	        return true;
	}




	private static final String[] CODES={"CLASS",
	    								 "CLIMATE",
	    								 "DESCRIPTION",
	    								 "TEXT",
	    								 "TECHLEVEL"};
	public String[] getStatCodes(){return CODES;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		case 1: return ""+climateType();
		case 2: return description();
		case 3: return text();
		case 4: return ""+getTechLevel();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		case 1: setClimateType(CMath.s_int(val)); break;
		case 2: setDescription(val); break;
		case 3: setMiscText(val); break;
		case 4: setTechLevel(CMath.s_int(val)); break;
		}
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof Area)) return false;
		for(int i=0;i<CODES.length;i++)
			if(!E.getStat(CODES[i]).equals(getStat(CODES[i])))
				return false;
		return true;
	}
}
