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
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
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
	protected Vector<Room> properRooms=new Vector<Room>();
    protected Vector blurbFlags=new Vector(1);
	//protected Vector metroRooms=new Vector();
	protected long tickStatus=Tickable.STATUS_NOT;
	protected long expirationDate=0;
    protected long lastPlayerTime=System.currentTimeMillis();
    protected int flag=Area.STATE_ACTIVE;
	protected RoomnumberSet properRoomIDSet=null;
	protected RoomnumberSet metroRoomIDSet=null;

    protected Vector children=null;
    protected Vector parents=null;
    protected Vector childrenToLoad=new Vector(1);
    protected Vector parentsToLoad=new Vector(1);

	protected EnvStats envStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
	protected EnvStats baseEnvStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
    protected String[] xtraValues=null;
	
	protected String author="";
	public void setAuthorID(String authorID){author=authorID;}
	public String getAuthorID(){return author;}
	
	protected String currency="";
	
    public void initializeClass(){}
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

    protected Vector allBlurbFlags()
    {
        Vector V=(Vector)blurbFlags.clone();
        String flag=null;
        Area A=null;
        int num=0;
        for(Enumeration e=getParents();e.hasMoreElements();)
        {
            A=(Area)e.nextElement();
            num=A.numBlurbFlags();
            for(int x=0;x<num;x++)
            {
                flag=A.getBlurbFlag(x);
                V.addElement(flag+" "+A.getBlurbFlag(flag));
            }
        }
        return V;
    }

    public String getBlurbFlag(String flag)
    {
        if((flag==null)||(flag.trim().length()==0))
            return null;
        flag=flag.toUpperCase().trim()+" ";
        Vector V=allBlurbFlags();
        for(int i=0;i<V.size();i++)
            if(((String)V.elementAt(i)).startsWith(flag))
                return ((String)V.elementAt(i)).substring(flag.length());
        return null;
    }
    public int numBlurbFlags(){return blurbFlags.size();}
    public int numAllBlurbFlags(){return allBlurbFlags().size();}
    public String getBlurbFlag(int which)
    {
        if(which<0) return null;
        Vector V=allBlurbFlags();
        if(which>=V.size()) return null;
        try{
            String s=(String)V.elementAt(which);
            int x=s.indexOf(' ');
            return s.substring(0,x).trim();
        }catch(Exception e){}
        return null;
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
            blurbFlags.addElement((flag+" "+flagPlusDesc).trim());
    }
    public void delBlurbFlag(String flagOnly)
    {
        if(flagOnly==null) return;
        flagOnly=flagOnly.toUpperCase().trim();
        if(flagOnly.length()==0) return;
        flagOnly+=" ";
        try{
            for(int v=0;v<blurbFlags.size();v++)
                if(((String)blurbFlags.elementAt(v)).startsWith(flagOnly))
                {
                    blurbFlags.removeElementAt(v);
                    return;
                }
        }catch(Exception e){}
    }

	public long expirationDate(){return expirationDate;}
	public void setExpirationDate(long time){expirationDate=time;}
	protected Vector affects=new Vector(1);
	protected Vector behaviors=new Vector(1);
    protected Vector scripts=new Vector(1);
	protected Vector subOps=new Vector(1);
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
        CMClass.bumpCounter(this,CMClass.OBJECT_AREA);
        xtraValues=CMProps.getExtraStatCodesHolder(this);
	}
    protected void finalize(){CMClass.unbumpCounter(this,CMClass.OBJECT_AREA);}
    protected boolean amDestroyed=false;
    public void destroy()
    {
        envStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
        baseEnvStats=envStats;
        amDestroyed=true;
        miscText=null;
        imageName=null;
        affects=null;
        behaviors=null;
        scripts=null;
        author=null;
        currency=null;
        children=null;
        parents=null;
        childrenToLoad=null;
        parentsToLoad=null;
        blurbFlags=null;
        subOps=null;
        properRooms=null;
        //metroRooms=null;
        myClock=null;
        climateObj=null;
        properRoomIDSet=null;
        metroRoomIDSet=null;
    }
    public boolean amDestroyed(){return amDestroyed;}
    public boolean savable(){return ((!amDestroyed) && (!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD)));}

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
				R=properRooms.elementAt(p);
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
		baseEnvStats.copyInto(envStats);
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

    public void setAreaState(int newState)
    {
        if((newState==0)&&(!CMLib.threads().isTicking(this,Tickable.TICKID_AREA)))
            CMLib.threads().startTickDown(this,Tickable.TICKID_AREA,1);
        flag=newState;
    }
    public int getAreaState(){return flag;}

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
			String str=(String)subOps.elementAt(s);
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
							if(newnum>=highest)	highest=newnum;
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
		if(CMSecurity.isDisabled("FATAREAS")
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
		if(E.blurbFlags!=null)
			blurbFlags=(Vector)E.blurbFlags.clone();
		affects=new Vector(1);
		behaviors=new Vector(1);
        scripts=new Vector(1);
		for(int b=0;b<E.numBehaviors();b++)
		{
			Behavior B=E.fetchBehavior(b);
			if(B!=null)
				behaviors.addElement((Behavior)B.copyOf());
		}
		for(int a=0;a<E.numEffects();a++)
		{
			Ability A=E.fetchEffect(a);
			if(A!=null)
				affects.addElement((Ability)A.copyOf());
		}
        ScriptingEngine S=null;
        for(int i=0;i<E.numScripts();i++)
        {
            S=E.fetchScript(i);
            if(S!=null)
                addScript((ScriptingEngine)S.copyOf());
        }
		setSubOpList(E.getSubOpList());
	}
	public CMObject copyOf()
	{
		try
		{
			StdArea E=(StdArea)this.clone();
            CMClass.bumpCounter(this,CMClass.OBJECT_AREA);
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
    protected String prejudiceFactors="";
    public String finalPrejudiceFactors()
    {
        String s=finalPrejudiceFactors(this);
        if(s.length()>0) return s;
        return CMProps.getVar(CMProps.SYSTEM_IGNOREMASK);
    }
    protected String finalPrejudiceFactors(Area A){
        if(A.prejudiceFactors().length()>0) return A.prejudiceFactors();
        for(Enumeration e=A.getParents();e.hasMoreElements();)
        { String  s=finalPrejudiceFactors((Area)e.nextElement()); if(s.length()!=0) return s;}
        return "";
    }
    public String prejudiceFactors(){return prejudiceFactors;}
    public void setPrejudiceFactors(String factors){prejudiceFactors=factors;}
    protected String[] itemPricingAdjustments=new String[0];
    protected final static String[] empty=new String[0];
    public String[] finalItemPricingAdjustments()
    {
        String[] s=finalItemPricingAdjustments(this);
        if(s.length>0) return s;
        return CMParms.toStringArray(CMParms.parseSemicolons(CMProps.getVar(CMProps.SYSTEM_PRICEFACTORS).trim(),true));
    }
    protected String[] finalItemPricingAdjustments(Area A){
        if(A.itemPricingAdjustments().length>0) return A.itemPricingAdjustments();
        for(Enumeration e=A.getParents();e.hasMoreElements();)
        { String[]  s=finalItemPricingAdjustments((Area)e.nextElement()); if(s.length!=0) return s;}
        return empty;
    }
    public String[] itemPricingAdjustments(){return itemPricingAdjustments;}
    public void setItemPricingAdjustments(String[] factors){itemPricingAdjustments=factors;}
    protected String ignoreMask="";
    public String finalIgnoreMask()
    {
        String s=finalIgnoreMask(this);
        if(s.length()>0) return s;
        return CMProps.getVar(CMProps.SYSTEM_IGNOREMASK);
    }
    protected String finalIgnoreMask(Area A){
        if(A.ignoreMask().length()>0) return A.ignoreMask();
        for(Enumeration e=A.getParents();e.hasMoreElements();)
        { String  s=finalIgnoreMask((Area)e.nextElement()); if(s.length()!=0) return s;}
        return "";
    }
    public String ignoreMask(){return ignoreMask;}
    public void setIgnoreMask(String factors){ignoreMask=factors;}
    protected String budget="";
    public String finalBudget()
    {
        String s=finalBudget(this);
        if(s.length()>0) return s;
        return CMProps.getVar(CMProps.SYSTEM_BUDGET);
    }
    protected String finalBudget(Area A){
        if(A.budget().length()>0) return A.budget();
        for(Enumeration e=A.getParents();e.hasMoreElements();)
        { String  s=finalBudget((Area)e.nextElement()); if(s.length()!=0) return s;}
        return "";
    }
    public String budget(){return budget;}
    public void setBudget(String factors){budget=factors;}
    protected String devalueRate="";
    public String finalDevalueRate()
    {
        String s=finalDevalueRate(this);
        if(s.length()>0) return s;
        return CMProps.getVar(CMProps.SYSTEM_DEVALUERATE);
    }
    protected String finalDevalueRate(Area A){
        if(A.devalueRate().length()>0) return A.devalueRate();
        for(Enumeration e=A.getParents();e.hasMoreElements();)
        { String  s=finalDevalueRate((Area)e.nextElement()); if(s.length()!=0) return s;}
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
        return CMath.s_int(CMProps.getVar(CMProps.SYSTEM_INVRESETRATE));

    }
    protected int finalInvResetRate(Area A){
        if(A.invResetRate()!=0) return A.invResetRate();
        for(Enumeration e=A.getParents();e.hasMoreElements();)
        { int x=finalInvResetRate((Area)e.nextElement()); if(x!=0) return x;}
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
	public void setClimateType(int newClimateType){	climateID=newClimateType;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
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
        for(int i=0;i<numEffects();i++)
        {
            N=fetchEffect(i);
            if((N!=null)&&(!N.okMessage(this,msg)))
                return false;
        }
        if(!msg.source().isMonster())
        {
            lastPlayerTime=System.currentTimeMillis();
            if((flag==Area.STATE_PASSIVE)
            &&((msg.sourceMinor()==CMMsg.TYP_ENTER)
            ||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
            ||(msg.sourceMinor()==CMMsg.TYP_FLEE)))
                flag=Area.STATE_ACTIVE;
        }
		if((flag>=Area.STATE_FROZEN)||(!CMLib.flags().allowsMovement(this)))
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
        MsgListener N=null;
        for(int b=0;b<numBehaviors();b++)
        {
            N=fetchBehavior(b);
            if(N!=null)
                N.executeMsg(this,msg);
        }

        for(int s=0;s<numScripts();s++)
        {
            N=fetchScript(s);
            if(N!=null)
                N.executeMsg(this,msg);
        }

        for(int a=0;a<numEffects();a++)
        {
            N=fetchEffect(a);
            if(N!=null)
                N.executeMsg(this,msg);
        }

		if((msg.sourceMinor()==CMMsg.TYP_RETIRE)
		&&(amISubOp(msg.source().Name())))
			delSubOp(msg.source().Name());

		if(parents!=null)
		for(int i=0;i<parents.size();i++)
			((Area)parents.elementAt(i)).executeMsg(myHost,msg);
	}

	public long getTickStatus(){ return tickStatus;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(flag>=Area.STATE_STOPPED)
            return false;
		tickStatus=Tickable.STATUS_START;
		if(tickID==Tickable.TICKID_AREA)
		{
            if((flag<=Area.STATE_ACTIVE)
            &&((System.currentTimeMillis()-lastPlayerTime)>Area.TIME_PASSIVE_LAPSE))
            {
                if(CMSecurity.isDisabled("PASSIVEAREAS"))
                    lastPlayerTime=System.currentTimeMillis();
                else
                    flag=Area.STATE_PASSIVE;
            }
			tickStatus=Tickable.STATUS_ALIVE;
			getClimateObj().tick(this,tickID);
			tickStatus=Tickable.STATUS_REBIRTH;
			getTimeObj().tick(this,tickID);
			for(int b=0;b<numBehaviors();b++)
			{
				tickStatus=Tickable.STATUS_BEHAVIOR+b;
				Behavior B=fetchBehavior(b);
				if(B!=null)
					B.tick(ticking,tickID);
			}
            for(int s=0;s<numScripts();s++)
            {
                ScriptingEngine S=fetchScript(s);
                tickStatus=Tickable.STATUS_SCRIPT+s;
                if(S!=null)
                    S.tick(ticking,tickID);
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
	    int senses=envStats.sensesMask()&(Integer.MAX_VALUE-(EnvStats.SENSE_UNLOCATABLE|EnvStats.CAN_NOT_SEE));
		if(senses>0) affectableStats.setSensesMask(affectableStats.sensesMask()|senses);
		int disposition=envStats().disposition()
			&((Integer.MAX_VALUE-(EnvStats.IS_SLEEPING|EnvStats.IS_HIDDEN)));
		if((affected instanceof Room)&&(CMLib.map().hasASky((Room)affected)))
		{
		    Climate C=getClimateObj();
			if((C==null)
		    ||(C.weatherType((Room)affected)==Climate.WEATHER_BLIZZARD)
		    ||(C.weatherType((Room)affected)==Climate.WEATHER_DUSTSTORM)
		    ||(getTimeObj().getTODCode()==TimeClock.TIME_NIGHT))
				disposition=disposition|EnvStats.IS_DARK;
		}
		if(disposition>0)
			affectableStats.setDisposition(affectableStats.disposition()|disposition);
		affectableStats.setWeight(affectableStats.weight()+envStats().weight());
        for(int a=0;a<numEffects();a++)
        {
            Ability A=fetchEffect(a);
            if((A!=null)&&(A.bubbleAffect()))
               A.affectEnvStats(affected,affectableStats);
        }
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
        for(int a=0;a<numEffects();a++)
        {
            Ability A=fetchEffect(a);
            if((A!=null)&&(A.bubbleAffect()))
               A.affectCharStats(affectedMob,affectableStats);
        }
    }
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{
        for(int a=0;a<numEffects();a++)
        {
            Ability A=fetchEffect(a);
            if((A!=null)&&(A.bubbleAffect()))
               A.affectCharState(affectedMob,affectableMaxState);
        }
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
	public int numEffects()
	{
		return (affects==null)?0:affects.size();
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
	public boolean inMyMetroArea(Area A)
	{
		if(A==this) return true;
		if(getNumChildren()==0) return false;
		for(int i=0;i<getNumChildren();i++)
			if(getChild(i).inMyMetroArea(A))
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

    /** Manipulation of the scripts list */
    public void addScript(ScriptingEngine S)
    {
        if(scripts==null) scripts=new Vector(1);
        if(S==null) return;
        if(!scripts.contains(S)) {
            ScriptingEngine S2=null;
            for(int s=0;s<scripts.size();s++)
            {
                S2=(ScriptingEngine)scripts.elementAt(s);
                if((S2!=null)&&(S2.getScript().equalsIgnoreCase(S.getScript())))
                    return;
            }
            scripts.addElement(S);
        }
    }
    public void delScript(ScriptingEngine S)
    {
        if(scripts!=null)
        {
            scripts.removeElement(S);
            if(scripts.size()==0)
                scripts=new Vector(1);
        }
    }
    public int numScripts(){return (scripts==null)?0:scripts.size();}
    public ScriptingEngine fetchScript(int x){try{return (ScriptingEngine)scripts.elementAt(x);}catch(Exception e){} return null;}

	public int maxRange(){return Integer.MAX_VALUE;}
	public int minRange(){return Integer.MIN_VALUE;}

	public int[] getAreaIStats()
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return new int[Area.AREASTAT_NUMBER];
		int[] statData=(int[])Resources.getResource("STATS_"+Name().toUpperCase());
		if(statData!=null) return statData;
		synchronized(("STATS_"+Name()).intern())
		{
			Resources.removeResource("HELP_"+Name().toUpperCase());
			Vector levelRanges=new Vector();
			Vector alignRanges=new Vector();
			Faction theFaction=null;
			for(Enumeration e=CMLib.factions().factions();e.hasMoreElements();)
			{
			    Faction F=(Faction)e.nextElement();
			    if(F.showInSpecialReported())
			        theFaction=F;
			}
			statData=new int[Area.AREASTAT_NUMBER];
			statData[Area.AREASTAT_POPULATION]=0;
			statData[Area.AREASTAT_MINLEVEL]=Integer.MAX_VALUE;
			statData[Area.AREASTAT_MAXLEVEL]=Integer.MIN_VALUE;
			statData[Area.AREASTAT_AVGLEVEL]=0;
			statData[Area.AREASTAT_MEDLEVEL]=0;
			statData[Area.AREASTAT_AVGALIGN]=0;
			statData[Area.AREASTAT_TOTLEVEL]=0;
			statData[Area.AREASTAT_INTLEVEL]=0;
	        statData[Area.AREASTAT_VISITABLEROOMS]=getProperRoomnumbers().roomCountAllAreas();
			long totalAlignments=0;
	        Room R=null;
	        MOB mob=null;
			for(Enumeration r=getProperMap();r.hasMoreElements();)
			{
				R=(Room)r.nextElement();
				if(R instanceof GridLocale)
					statData[Area.AREASTAT_VISITABLEROOMS]--;
				for(int i=0;i<R.numInhabitants();i++)
				{
					mob=R.fetchInhabitant(i);
					if((mob!=null)&&(mob.isMonster()))
					{
						int lvl=mob.baseEnvStats().level();
						levelRanges.addElement(Integer.valueOf(lvl));
						if((theFaction!=null)&&(mob.fetchFaction(theFaction.factionID())!=Integer.MAX_VALUE))
						{
						    alignRanges.addElement(Integer.valueOf(mob.fetchFaction(theFaction.factionID())));
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
			}
			else
			{
				Collections.sort(levelRanges);
				Collections.sort(alignRanges);
				statData[Area.AREASTAT_MEDLEVEL]=((Integer)levelRanges.elementAt((int)Math.round(Math.floor(CMath.div(levelRanges.size(),2.0))))).intValue();
				statData[Area.AREASTAT_MEDALIGN]=((Integer)alignRanges.elementAt((int)Math.round(Math.floor(CMath.div(alignRanges.size(),2.0))))).intValue();
				statData[Area.AREASTAT_AVGLEVEL]=(int)Math.round(CMath.div(statData[Area.AREASTAT_TOTLEVEL],statData[Area.AREASTAT_POPULATION]));
				statData[Area.AREASTAT_AVGALIGN]=(int)Math.round(((double)totalAlignments)/((double)statData[Area.AREASTAT_POPULATION]));
			}

			Resources.submitResource("STATS_"+Name().toUpperCase(),statData);
		}
		return statData;
	}
	public synchronized StringBuffer getAreaStats()
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return new StringBuffer("");
		StringBuffer s=(StringBuffer)Resources.getResource("HELP_"+Name().toUpperCase());
		if(s!=null) return s;
		s=new StringBuffer("");
		int[] statData=getAreaIStats();
		s.append(description()+"\n\r");
		if(author.length()>0)
			s.append("Author         : "+author+"\n\r");
        s.append("Number of rooms: "+statData[Area.AREASTAT_VISITABLEROOMS]+"\n\r");
		Faction theFaction=null;
		for(Enumeration e=CMLib.factions().factions();e.hasMoreElements();)
		{
		    Faction F=(Faction)e.nextElement();
		    if(F.showInSpecialReported())
		        theFaction=F;
		}
		if(statData[Area.AREASTAT_POPULATION]==0)
		{
			if(getProperRoomnumbers().roomCountAllAreas()/2<properRooms.size())
				s.append("Population     : 0\n\r");
		}
		else
		{
			s.append("Population     : "+statData[Area.AREASTAT_POPULATION]+"\n\r");
			String currName=CMLib.beanCounter().getCurrency(this);
			if(currName.length()>0)
				s.append("Currency       : "+CMStrings.capitalizeAndLower(currName)+"\n\r");
			else
				s.append("Currency       : Gold coins (default)\n\r");
            LegalBehavior B=CMLib.law().getLegalBehavior(this);
			if(B!=null)
			{
                String ruler=B.rulingOrganization();
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
            try{
                String flag=null;
                int num=numAllBlurbFlags();
                boolean blurbed=false;
                for(int i=0;i<num;i++)
                {
                    flag=this.getBlurbFlag(i);
                    if(flag!=null) flag=getBlurbFlag(flag);
                    if(flag!=null)
                    {
                        if(!blurbed){blurbed=true; s.append("\n\r");}
                        s.append(flag+"\n\r");
                    }
                }
                if(blurbed) s.append("\n\r");
            }catch(Exception e){}
		}
		//Resources.submitResource("HELP_"+Name().toUpperCase(),s);
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
	protected int getProperIndex(Room R)
	{
		if(properRooms.size()==0) return -1;
		if(R.roomID().length()==0) return 0;
		String roomID=R.roomID();
		synchronized(properRooms)
		{
			int start=0;
			int end=properRooms.size()-1;
			int mid=0;
			while(start<=end)
			{
	            mid=(end+start)/2;
	            int comp=properRooms.elementAt(mid).roomID().compareToIgnoreCase(roomID);
	            if(comp==0) return mid;
	            else
	            if(comp>0)
	                end=mid-1;
	            else
	                start=mid+1;
			}
			if(end<0) return 0;
			if(start>=properRooms.size()) return properRooms.size()-1;
			return mid;
		}
	}
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
        	int insertAt=0;
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
        	if(properRooms.size()>0)
        	{
        		insertAt=getProperIndex(R);
	            int comp=properRooms.elementAt(insertAt).roomID().compareToIgnoreCase(roomID);
	            if(comp==0) return;
                addMetroRoom(R);
				if(comp>0)
					properRooms.insertElementAt(R,insertAt);
				else
				if(insertAt==properRooms.size()-1)
					properRooms.addElement(R);
				else
					properRooms.insertElementAt(R,insertAt+1);
                addProperRoomnumber(roomID);
        	}
        	else
        	{
        		properRooms.addElement(R);
                addProperRoomnumber(roomID);
                addMetroRoom(R);
        	}
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
			if(!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
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
        if(properRooms!=null)
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
            if(properRooms.removeElement(R))
            {
	            delMetroRoom(R);
	            delProperRoomnumber(R.roomID());
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
	        int start=0;
	        int end=properRooms.size()-1;
	        while(start<=end)
	        {
	            int mid=(end+start)/2;
	            int comp=properRooms.elementAt(mid).roomID().compareToIgnoreCase(roomID);
	            if(comp==0)
	                return properRooms.elementAt(mid);
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
		if(R==null) Log.errOut("StdArea","Unable to random-metro-find: "+roomID);
		return R;
	}

	public Enumeration getProperMap()
	{
		Vector<Room> V=(Vector<Room>)properRooms.clone();
		Room R=null;
		for(int v=V.size()-1;v>=0;v--)
		{
			R=V.elementAt(v);
			if(R instanceof GridLocale)
				V.addAll(((GridLocale)R).getAllRooms());
		}
		return V.elements();
	}

    public Enumeration getFilledProperMap()
    {
        Enumeration r=getProperMap();
        Vector V=new Vector();
        Room R=null;
        Room R2=null;
        for(;r.hasMoreElements();)
        {
            R=(Room)r.nextElement();
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
	public Vector getMetroCollection()
	{
		Vector<Room> V=(Vector<Room>)properRooms.clone();
		Room R=null;
		for(int v=V.size()-1;v>=0;v--)
		{
			R=V.elementAt(v);
			if(R instanceof GridLocale)
				V.addAll(((GridLocale)R).getAllRooms());
		}
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
	        children=new Vector(1);
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
	                        Area A = CMLib.map().getArea((String)parentsToLoad.elementAt(i));
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
	        for(Enumeration e=getParents(); e.hasMoreElements();) 
	        {
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

    public int getSaveStatIndex(){return (xtraValues==null)?getStatCodes().length:getStatCodes().length-xtraValues.length;}
	private static final String[] CODES={"CLASS",
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
            codes=CMProps.getStatCodesList(CODES,this);
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
		case 4: return ""+getTechLevel();
        case 5: return ""+CMLib.xml().getXMLList(blurbFlags);
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
		case 4: setTechLevel(CMath.s_parseBitIntExpression(Area.THEME_DESCS,val)); break;
        case 5:
        {
            if(val.startsWith("+"))
                addBlurbFlag(val.substring(1));
            else
            if(val.startsWith("-"))
                delBlurbFlag(val.substring(1));
            else
                blurbFlags=CMLib.xml().parseXMLList(val);
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
