package com.planet_ink.coffee_mud.MOBS;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
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
public class StdDeity extends StdMOB implements Deity
{
	public String ID(){return "StdDeity";}
	protected int xpwrath=100;
	protected String clericReqs="";
	protected String worshipReqs="";
    protected String serviceRitual="";
	protected String clericRitual="";
	protected String clericSin="";
	protected String clericPowerup="";
	protected String worshipRitual="";
	protected String worshipSin="";
	protected Vector worshipTriggers=new Vector();
	protected Vector worshipCurseTriggers=new Vector();
	protected Vector clericTriggers=new Vector();
    protected Vector serviceTriggers=new Vector();
	protected Vector clericPowerTriggers=new Vector();
	protected Vector clericCurseTriggers=new Vector();
	protected DVector blessings=new DVector(2);
	protected DVector curses=new DVector(2);
	protected Vector powers=new Vector();
	protected Hashtable trigBlessingParts=new Hashtable();
	protected Hashtable trigBlessingTimes=new Hashtable();
	protected Hashtable trigPowerParts=new Hashtable();
	protected Hashtable trigPowerTimes=new Hashtable();
	protected Hashtable trigCurseParts=new Hashtable();
	protected Hashtable trigCurseTimes=new Hashtable();
    protected Hashtable trigServiceParts=new Hashtable();
    protected Hashtable trigServiceTimes=new Hashtable();
    protected Vector<WorshipService> services=new Vector<WorshipService>();
	protected int rebukeCheckDown=0;
	protected boolean norecurse=false;
    protected MOB blacklist=null;
    protected int blackmarks=0;
    protected long lastBlackmark=0;
    protected Vector waitingFor=new Vector();

	public StdDeity()
	{
		super();
		Username="a Mighty Deity";
		setDescription("He is Mighty.");
		setDisplayText("A Mighty Deity stands here!");
		baseEnvStats().setWeight(700);
		baseEnvStats().setAbility(200);
		baseEnvStats().setArmor(0);
		baseEnvStats().setAttackAdjustment(1000);
		baseEnvStats().setDamage(1000);
		baseCharStats().setMyRace(CMClass.getRace("Spirit"));
		recoverEnvStats();
	}
	
	private class WorshipService
	{
		MOB cleric = null;
		Room room = null;
		boolean serviceCompleted = false;
		long startTime = System.currentTimeMillis();
		Vector<MOB> parishaners = new Vector<MOB>();
	}

    protected void cloneFix(MOB E)
    {
        super.cloneFix(E);
        if(E instanceof StdDeity)
        {
            worshipTriggers=(Vector)((StdDeity)E).worshipTriggers.clone();
            worshipCurseTriggers=(Vector)((StdDeity)E).worshipCurseTriggers.clone();
            clericTriggers=(Vector)((StdDeity)E).clericTriggers.clone();
            clericPowerTriggers=(Vector)((StdDeity)E).clericPowerTriggers.clone();
            clericCurseTriggers=(Vector)((StdDeity)E).clericCurseTriggers.clone();
            blessings=(DVector)((StdDeity)E).blessings.copyOf();
            curses=(DVector)((StdDeity)E).curses.copyOf();
            powers=(Vector)((StdDeity)E).powers.clone();
            trigBlessingParts=(Hashtable)((StdDeity)E).trigBlessingParts.clone();
            trigBlessingTimes=(Hashtable)((StdDeity)E).trigBlessingTimes.clone();
            trigPowerParts=(Hashtable)((StdDeity)E).trigPowerParts.clone();
            trigPowerTimes=(Hashtable)((StdDeity)E).trigPowerTimes.clone();
            trigCurseParts=(Hashtable)((StdDeity)E).trigCurseParts.clone();
            trigCurseTimes=(Hashtable)((StdDeity)E).trigCurseTimes.clone();
            trigServiceParts=(Hashtable)((StdDeity)E).trigServiceParts.clone();
            trigServiceTimes=(Hashtable)((StdDeity)E).trigServiceTimes.clone();
        }
    }

	public String getClericRequirements(){return clericReqs;}
	public void setClericRequirements(String reqs){clericReqs=reqs;}
	public String getWorshipRequirements(){return worshipReqs;}
	public void setWorshipRequirements(String reqs){worshipReqs=reqs;}
	public String getClericRitual(){
		if(clericRitual.trim().length()==0) return "SAY Bless me "+name();
		return clericRitual;}
	public void setClericRitual(String ritual){
		clericRitual=ritual;
		parseTriggers(clericTriggers,ritual);
	}
	public String getWorshipRitual(){
		if(worshipRitual.trim().length()==0) return "SAY Bless me "+name();
		return worshipRitual;}
	public void setWorshipRitual(String ritual){
		worshipRitual=ritual;
		parseTriggers(worshipTriggers,ritual);
	}
    public String getServiceRitual(){
        return serviceRitual;}
    public void setServiceRitual(String ritual){
        if((ritual==null)||(ritual.length()==0))
            ritual="SAY Bless us "+name()+"&wait 10&wait 10&SAY May "+name()+" bless you all&ALLSAY Amen.&SAY Go in peace";
        serviceRitual=ritual;
        parseTriggers(serviceTriggers,ritual);
    }

	public String getTriggerDesc(Vector V)
	{
		if((V==null)||(V.size()==0)) return "Never";
		StringBuffer buf=new StringBuffer("");
		for(int v=0;v<V.size();v++)
		{
			DeityTrigger DT=(DeityTrigger)V.elementAt(v);
			if(v>0) buf.append(", "+((DT.previousConnect==CONNECT_AND)?"and ":"or "));
			switch(DT.triggerCode)
			{
			case TRIGGER_SAY:
				buf.append("the player should say '"+DT.parm1.toLowerCase()+"'");
				break;
			case TRIGGER_READING:
				if(DT.parm1.equals("0"))
					buf.append("the player should read something");
				else
					buf.append("the player should read '"+DT.parm1.toLowerCase()+"'");
				break;
			case TRIGGER_TIME:
				buf.append("the hour of the day is "+DT.parm1.toLowerCase()+"");
				break;
			case TRIGGER_PUTTHING:
				buf.append("the player should put "+DT.parm1.toLowerCase()+" in "+DT.parm2.toLowerCase());
				break;
			case TRIGGER_BURNTHING:
				buf.append("the player should burn "+DT.parm1.toLowerCase());
				break;
			case TRIGGER_DRINK:
				buf.append("the player should drink "+DT.parm1.toLowerCase());
				break;
			case TRIGGER_EAT:
				buf.append("the player should eat "+DT.parm1.toLowerCase());
				break;
			case TRIGGER_INROOM:
				{
                if(DT.parm1.equalsIgnoreCase("holy")
                ||DT.parm1.equalsIgnoreCase("unholy")
                ||DT.parm1.equalsIgnoreCase("balance"))
                    buf.append("the player should be in the deities room of infused "+DT.parm1.toLowerCase()+"-ness.");
                else
                {
    				Room R=CMLib.map().getRoom(DT.parm1);
    				if(R==null)
    					buf.append("the player should be in some unknown place");
    				else
    					buf.append("the player should be in '"+R.roomTitle(null)+"'");
                }
				}
				break;
			case TRIGGER_RIDING:
				buf.append("the player should be on "+DT.parm1.toLowerCase());
				break;
			case TRIGGER_CAST:
				{
				Ability A=CMClass.findAbility(DT.parm1);
				if(A==null)
					buf.append("the player should cast '"+DT.parm1+"'");
				else
					buf.append("the player should cast '"+A.name()+"'");
				}
				break;
			case TRIGGER_EMOTE:
				buf.append("the player should emote '"+DT.parm1.toLowerCase()+"'");
				break;
			case TRIGGER_RANDOM:
				buf.append(DT.parm1+"% of the time");
				break;
            case TRIGGER_WAIT:
                buf.append("wait "+((CMath.s_int(DT.parm1)*Tickable.TIME_TICK)/1000)+" seconds");
                break;
            case TRIGGER_YOUSAY:
                buf.append("then you will automatically say '"+DT.parm1.toLowerCase()+"'");
                break;
            case TRIGGER_OTHERSAY:
                buf.append("then all others will say '"+DT.parm1.toLowerCase()+"'");
                break;
            case TRIGGER_ALLSAY:
                buf.append("then all will say '"+DT.parm1.toLowerCase()+"'");
                break;
			case TRIGGER_CHECK:
				buf.append(CMLib.masking().maskDesc(DT.parm1));
				break;
			case TRIGGER_PUTVALUE:
				buf.append("the player should put an item worth at least "+DT.parm1.toLowerCase()+" in "+DT.parm2.toLowerCase());
				break;
			case TRIGGER_PUTMATERIAL:
				{
					String material="something";
					int t=CMath.s_int(DT.parm1);
					if(((t&RawMaterial.RESOURCE_MASK)==0)
					&&((t>>8)<RawMaterial.MATERIAL_MASK))
						material=RawMaterial.MATERIAL_DESCS[t>>8].toLowerCase();
					else
					if(RawMaterial.CODES.IS_VALID(t))
						material=RawMaterial.CODES.NAME(t).toLowerCase();
					buf.append("the player puts an item made of "+material+" in "+DT.parm2.toLowerCase());
				}
				break;
			case TRIGGER_BURNMATERIAL:
				{
					String material="something";
					int t=CMath.s_int(DT.parm1);
					if(((t&RawMaterial.RESOURCE_MASK)==0)
					&&((t>>8)<RawMaterial.MATERIAL_MASK))
						material=RawMaterial.MATERIAL_DESCS[t>>8].toLowerCase();
					else
					if(RawMaterial.CODES.IS_VALID(t))
						material=RawMaterial.CODES.NAME(t).toLowerCase();
					buf.append("the player should burn an item made of "+material);
				}
				break;
			case TRIGGER_BURNVALUE:
				buf.append("the player should burn an item worth at least "+DT.parm1.toLowerCase());
				break;
			case TRIGGER_SITTING:
				buf.append("the player should sit down");
				break;
			case TRIGGER_STANDING:
				buf.append("the player should stand up");
				break;
			case TRIGGER_SLEEPING:
				buf.append("the player should go to sleep");
				break;
			}
		}
		return buf.toString();
	}

	public String getClericRequirementsDesc()
	{
		return "The following may be clerics of "+name()+": "+CMLib.masking().maskDesc(getClericRequirements());
	}
	public String getClericTriggerDesc()
	{
		if(numBlessings()>0)
			return "The blessings of "+name()+" are bestowed to "+charStats().hisher()+" clerics whenever the cleric does the following: "+getTriggerDesc(clericTriggers)+".";
		return "";
	}
	public String getWorshipRequirementsDesc()
	{
		return "The following are acceptable worshipers of "+name()+": "+CMLib.masking().maskDesc(getWorshipRequirements());
	}
	public String getWorshipTriggerDesc()
	{
		if(numBlessings()>0)
			return "The blessings of "+name()+" are bestowed to "+charStats().hisher()+" worshippers whenever they do the following: "+getTriggerDesc(worshipTriggers)+".";
		return "";
	}

    public String getServiceTriggerDesc()
    {
        return "The services of "+name()+" are the following: "+getTriggerDesc(serviceTriggers)+".";
    }

	public void destroy()
	{
		super.destroy();
		CMLib.map().delDeity(this);
	}
	public void bringToLife(Room newLocation, boolean resetStats)
	{
		super.bringToLife(newLocation,resetStats);
		CMLib.map().addDeity(this);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
        if((msg.targetMinor()==CMMsg.TYP_EXPIRE)
        &&(msg.target()==location())
        &&(CMLib.flags().isInTheGame(this,true)))
        	return false;
        else
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_SERVE:
			if(msg.source().getMyDeity()==this)
			{
				msg.source().tell("You already worship "+name()+".");
                if(msg.source().isMonster())
                    CMLib.commands().postSay(msg.source(),null,"I already worship "+msg.source().getMyDeity().name()+".");
				return false;
			}
			if(msg.source().getMyDeity()!=null)
			{
				msg.source().tell("You already worship "+msg.source().getMyDeity().name()+".");
                if(msg.source().isMonster())
                    CMLib.commands().postSay(msg.source(),null,"I already worship "+msg.source().getMyDeity().name()+".");
				return false;
			}
			if(msg.source().charStats().getCurrentClass().baseClass().equalsIgnoreCase("Cleric"))
			{
				if(!CMLib.masking().maskCheck(getClericRequirements(),msg.source(),true))
				{
					msg.source().tell("You are unworthy of serving "+name()+".");
                    if(msg.source().isMonster())
                        CMLib.commands().postSay(msg.source(),null,"I am unworthy of serving "+name()+".");
					return false;
				}
			}
			else
			if(!CMLib.masking().maskCheck(getWorshipRequirements(),msg.source(),true))
			{
				msg.source().tell("You are unworthy of "+name()+".");
                if(msg.source().isMonster())
                    CMLib.commands().postSay(msg.source(),null,"I am unworthy of "+name()+".");
				return false;
			}
			break;
		case CMMsg.TYP_REBUKE:
			if(!msg.source().getWorshipCharID().equals(Name()))
			{
				msg.source().tell("You do not worship "+name()+".");
				return false;
			}
			break;
		}
		return true;
	}

	public synchronized void bestowBlessing(MOB mob, Ability Blessing)
	{
		Room prevRoom=location();
		mob.location().bringMobHere(this,false);
		if(Blessing!=null)
		{
			Vector V=new Vector();
			if(Blessing.canTarget(Ability.CAN_MOBS))
			{
				V.addElement(mob.name()+"$");
				Blessing.invoke(this,V,mob,true,mob.envStats().level());
			}
			else
			if(Blessing.canTarget(Ability.CAN_ITEMS))
			{
				Item I=mob.fetchWieldedItem();
				if(I==null) I=mob.fetchFirstWornItem(Wearable.WORN_HELD);
				if(I==null) I=mob.fetchWornItem("all");
				if(I==null) I=mob.fetchCarried(null,"all");
				if(I==null) return;
				V.addElement("$"+I.name()+"$");
				addInventory(I);
				Blessing.invoke(this,V,I,true,mob.envStats().level());
				delInventory(I);
				if(!mob.isMine(I)) mob.addInventory(I);
			}
			else
				Blessing.invoke(this,mob,true,mob.envStats().level());
		}
		prevRoom.bringMobHere(this,false);
		if(mob.location()!=prevRoom)
		{
			if(mob.getVictim()==this)
				mob.makePeace();
			if(getVictim()==mob)
				makePeace();
		}
	}

	public synchronized void bestowPower(MOB mob, Ability Power)
	{
		if((mob.fetchAbility(Power.ID())==null)
		&&(CMLib.ableMapper().qualifyingLevel(mob,Power)<=0))
		{
			Power=(Ability)Power.copyOf();
			Power.setProficiency(CMLib.ableMapper().getMaxProficiency(mob,true,Power.ID()));
			Power.setSavable(false);
			mob.addAbility(Power);
		}
	}

	public synchronized void bestowCurse(MOB mob, Ability Curse)
	{
		Room prevRoom=location();
		mob.location().bringMobHere(this,false);
		if(Curse!=null)
		{
			Vector V=new Vector();
			if(Curse.canTarget(Ability.CAN_MOBS))
			{
				V.addElement(mob.location().getContextName(mob));
				Curse.invoke(this,V,mob,true,mob.envStats().level());
			}
			else
			if(Curse.canTarget(Ability.CAN_ITEMS))
			{
				Item I=mob.fetchWieldedItem();
				if(I==null) I=mob.fetchFirstWornItem(Wearable.WORN_HELD);
				if(I==null) I=mob.fetchWornItem("all");
				if(I==null) I=mob.fetchCarried(null,"all");
				if(I==null) return;
				V.addElement("$"+I.name()+"$");
				addInventory(I);
				Curse.invoke(this,V,I,true,mob.envStats().level());
				delInventory(I);
				if(!mob.isMine(I)) mob.addInventory(I);
			}
			else
				Curse.invoke(this,mob,true,mob.envStats().level());
		}
		prevRoom.bringMobHere(this,false);
		if(mob.location()!=prevRoom)
		{
			if(mob.getVictim()==this)
				mob.makePeace();
			if(getVictim()==mob)
				makePeace();
		}
	}

	public synchronized void bestowBlessings(MOB mob)
	{
		norecurse=true;
		try
		{
			if((!alreadyBlessed(mob))&&(numBlessings()>0))
			{
				mob.location().show(this,mob,CMMsg.MSG_OK_VISUAL,"You feel the presence of <S-NAME> in <T-NAME>.");
				if((mob.charStats().getCurrentClass().baseClass().equals("Cleric"))
                ||(CMSecurity.isASysOp(mob)))
				{
					for(int b=0;b<numBlessings();b++)
					{
						Ability Blessing=fetchBlessing(b);
						if(Blessing!=null)
							bestowBlessing(mob,Blessing);
					}
				}
				else
				{
                    int randNum=CMLib.dice().roll(1,numBlessings(),-1);
					Ability Blessing=fetchBlessing(randNum);
					if((Blessing!=null)&&(!fetchBlessingCleric(randNum)))
						bestowBlessing(mob,Blessing);
				}
			}
		}
		catch(Exception e)
		{
			Log.errOut("StdDeity",e);
		}
		norecurse=false;
	}

	public synchronized void bestowPowers(MOB mob)
	{
		norecurse=true;
		try
		{
			if((!alreadyPowered(mob))&&(numPowers()>0))
			{
				mob.location().show(this,mob,CMMsg.MSG_OK_VISUAL,"You feel the power of <S-NAME> in <T-NAME>.");
				Ability Power=fetchPower(CMLib.dice().roll(1,numPowers(),-1));
				if(Power!=null)
					bestowPower(mob,Power);
			}
		}
		catch(Exception e)
		{
			Log.errOut("StdDeity",e);
		}
		norecurse=false;
	}

	public synchronized void bestowCurses(MOB mob)
	{
		norecurse=true;
		try
		{
			if(numCurses()>0)
			{
				mob.location().show(this,mob,CMMsg.MSG_OK_VISUAL,"You feel the wrath of <S-NAME> in <T-NAME>.");
				if(mob.charStats().getCurrentClass().baseClass().equals("Cleric")
                ||(CMSecurity.isASysOp(mob)))
				{
					for(int b=0;b<numCurses();b++)
					{
						Ability Curse=fetchCurse(b);
						if(Curse!=null)
							bestowCurse(mob,Curse);
					}
				}
				else
				{
                    int randNum=CMLib.dice().roll(1,numCurses(),-1);
					Ability Curse=fetchCurse(randNum);
					if((Curse!=null)&&(!fetchBlessingCleric(randNum)))
						bestowCurse(mob,Curse);
				}
			}
		}
		catch(Exception e)
		{
			Log.errOut("StdDeity",e);
		}
		norecurse=false;
	}

	public void removeBlessings(MOB mob)
	{
		if((alreadyBlessed(mob))&&(mob.location()!=null))
		{
			mob.location().show(this,mob,CMMsg.MSG_OK_VISUAL,"<S-NAME> remove(s) <S-HIS-HER> blessings from <T-NAME>.");
			for(int a=mob.numEffects()-1;a>=0;a--)
			{
				Ability A=mob.fetchEffect(a);
				if((A!=null)&&(A.invoker()==this))
				{
					A.unInvoke();
					mob.delEffect(A);
				}
			}
		}
	}

	public void removePowers(MOB mob)
	{
		if((alreadyPowered(mob))&&(mob.location()!=null))
		{
			mob.location().show(this,mob,CMMsg.MSG_OK_VISUAL,"<S-NAME> remove(s) <S-HIS-HER> powers from <T-NAME>.");
			for(int a=mob.numLearnedAbilities()-1;a>=0;a--)
			{
				Ability A=mob.fetchAbility(a);
				if((A!=null)&&(!A.savable()))
				{
					mob.delAbility(A);
					A=mob.fetchEffect(A.ID());
					if(A!=null)
					{
						A.unInvoke();
						mob.delEffect(A);
					}
				}
			}
		}
	}

	public boolean alreadyBlessed(MOB mob)
	{
		for(int a=0;a<mob.numEffects();a++)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)&&(A.invoker()==this))
				return true;
		}
		return false;
	}

	public boolean alreadyPowered(MOB mob)
	{
		if(numPowers()>0)
		for(int a=0;a<mob.numLearnedAbilities();a++)
		{
			Ability A=mob.fetchAbility(a);
			if((A!=null)&&(!A.savable()))
				return true;
		}
		return false;
	}

	public boolean triggerCheck(CMMsg msg,
							    Vector V,
							    Hashtable trigParts,
							    Hashtable trigTimes)
	{
		boolean recheck=false;
		for(int v=0;v<V.size();v++)
		{
			boolean yup=false;
			DeityTrigger DT=(DeityTrigger)V.elementAt(v);
			if((msg.sourceMinor()==TRIG_WATCH[DT.triggerCode])
			||(TRIG_WATCH[DT.triggerCode]==-999))
			{
				switch(DT.triggerCode)
				{
				case TRIGGER_SAY:
					if((msg.sourceMessage()!=null)&&(msg.sourceMessage().toUpperCase().indexOf(DT.parm1)>0))
						yup=true;
					break;
				case TRIGGER_TIME:
					if((msg.source().location()!=null)
					&&(msg.source().location().getArea().getTimeObj().getTimeOfDay()==CMath.s_int(DT.parm1)))
					   yup=true;
					break;
				case TRIGGER_RANDOM:
					if(CMLib.dice().rollPercentage()<=CMath.s_int(DT.parm1))
					   yup=true;
					break;
                case TRIGGER_YOUSAY:
                    if(v<=0)
                        yup=true;
                    else
                    {
                        boolean[] checks=(boolean[])trigParts.get(msg.source().Name());
                        if((checks!=null)&&(checks[v-1])&&(!checks[v]))
                        {
                            yup=true;
                            norecurse=true;
                            CMLib.commands().postSay(msg.source(),null,CMStrings.capitalizeAndLower(DT.parm1));
                            norecurse=false;
                        }
                        else
                        if((checks!=null)&&checks[v])
                            continue;
                    }
                    break;
                case TRIGGER_ALLSAY:
                    if(v<=0)
                        yup=true;
                    else
                    {
                        boolean[] checks=(boolean[])trigParts.get(msg.source().Name());
                        Room R=msg.source().location();
                        if((checks!=null)&&(checks[v-1])&&(!checks[v])&&(R!=null))
                        {
                            yup=true;
                            for(int m=0;m<R.numInhabitants();m++)
                            {
                                MOB M=R.fetchInhabitant(m);
                                if(M!=null)
                                {
                                    norecurse=true;
                                    CMLib.commands().postSay(M,null,CMStrings.capitalizeAndLower(DT.parm1));
                                    norecurse=false;
                                }
                            }
                        }
                        else
                        if((checks!=null)&&checks[v])
                            continue;
                    }
                    break;
                case TRIGGER_OTHERSAY:
                    if(v<=0)
                        yup=true;
                    else
                    {
                        boolean[] checks=(boolean[])trigParts.get(msg.source().Name());
                        Room R=msg.source().location();
                        if((checks!=null)&&(checks[v-1])&&(!checks[v])&&(R!=null))
                        {
                            yup=true;
                            for(int m=0;m<R.numInhabitants();m++)
                            {
                                MOB M=R.fetchInhabitant(m);
                                if((M!=null)&&(M!=msg.source()))
                                {
                                    norecurse=true;
                                    CMLib.commands().postSay(M,null,CMStrings.capitalizeAndLower(DT.parm1));
                                    norecurse=false;
                                }
                            }
                        }
                        else
                        if((checks!=null)&&checks[v])
                            continue;
                    }
                    break;
                case TRIGGER_WAIT:
                {
                    if(v<=0)
                        yup=true;
                    else
                    {
                        boolean[] checks=(boolean[])trigParts.get(msg.source().Name());
                        if((checks!=null)&&(checks[v-1])&&(!checks[v])&&(trigTimes.get(msg.source().Name())!=null))
                        {
                            boolean proceed=true;
                            for(int t=v+1;t<checks.length;t++)
                                if(checks[t]) proceed=false;
                            if(proceed)
                            {
                                if(System.currentTimeMillis()>(((Long)trigTimes.get(msg.source().Name())).longValue()+(CMath.s_int(DT.parm1)*Tickable.TIME_TICK)))
                                {
                                   yup=true;
                                   waitingFor.removeElement(msg.source());
                                }
                                else
                                {
                                    waitingFor.addElement(msg.source());
                                    return false;
                                }
                            }
                        }
                        else
                        if((checks!=null)&&(checks[v]))
                            continue;
                    }
                    break;
                }
				case TRIGGER_CHECK:
					if(CMLib.masking().maskCheck(DT.parm1,msg.source(),true))
					   yup=true;
					break;
				case TRIGGER_PUTTHING:
					if((msg.target()!=null)
					&&(msg.target() instanceof Container)
					&&(msg.tool()!=null)
					&&(msg.tool() instanceof Item)
					&&(CMLib.english().containsString(msg.tool().name(),DT.parm1))
					&&(CMLib.english().containsString(msg.target().name(),DT.parm2)))
						yup=true;
					break;
				case TRIGGER_BURNTHING:
				case TRIGGER_READING:
				case TRIGGER_DRINK:
				case TRIGGER_EAT:
					if((msg.target()!=null)
					&&(DT.parm1.equals("0")||CMLib.english().containsString(msg.target().name(),DT.parm1)))
					   yup=true;
					break;
				case TRIGGER_INROOM:
					if(msg.source().location()!=null)
                    {
                        if(DT.parm1.equalsIgnoreCase("holy")||DT.parm1.equalsIgnoreCase("unholy")||DT.parm1.equalsIgnoreCase("balance"))
                            yup=CMLib.law().getClericInfused(msg.source().location())==this;
                        else
    					if(msg.source().location().roomID().equalsIgnoreCase(DT.parm1))
    						yup=true;
                    }
					break;
				case TRIGGER_RIDING:
					if((msg.source().riding()!=null)
					&&(CMLib.english().containsString(msg.source().riding().name(),DT.parm1)))
					   yup=true;
					break;
				case TRIGGER_CAST:
					if((msg.tool()!=null)
					&&((msg.tool().ID().equalsIgnoreCase(DT.parm1))
					||(CMLib.english().containsString(msg.tool().name(),DT.parm1))))
						yup=true;
					break;
				case TRIGGER_EMOTE:
					if((msg.sourceMessage()!=null)&&(msg.sourceMessage().toUpperCase().indexOf(DT.parm1)>0))
						yup=true;
					break;
				case TRIGGER_PUTVALUE:
					if((msg.tool()!=null)
					&&(msg.tool() instanceof Item)
					&&(((Item)msg.tool()).baseGoldValue()>=CMath.s_int(DT.parm1))
					&&(msg.target()!=null)
					&&(msg.target() instanceof Container)
					&&(CMLib.english().containsString(msg.target().name(),DT.parm2)))
						yup=true;
					break;
				case TRIGGER_PUTMATERIAL:
					if((msg.tool()!=null)
					&&(msg.tool() instanceof Item)
					&&(((((Item)msg.tool()).material()&RawMaterial.RESOURCE_MASK)==CMath.s_int(DT.parm1))
						||((((Item)msg.tool()).material()&RawMaterial.MATERIAL_MASK)==CMath.s_int(DT.parm1)))
					&&(msg.target()!=null)
					&&(msg.target() instanceof Container)
					&&(CMLib.english().containsString(msg.target().name(),DT.parm2)))
						yup=true;
					break;
				case TRIGGER_BURNMATERIAL:
					if((msg.target()!=null)
					&&(msg.target() instanceof Item)
					&&(((((Item)msg.target()).material()&RawMaterial.RESOURCE_MASK)==CMath.s_int(DT.parm1))
						||((((Item)msg.target()).material()&RawMaterial.MATERIAL_MASK)==CMath.s_int(DT.parm1))))
							yup=true;
					break;
				case TRIGGER_BURNVALUE:
					if((msg.target()!=null)
					&&(msg.target() instanceof Item)
					&&(((Item)msg.target()).baseGoldValue()>=CMath.s_int(DT.parm1)))
						yup=true;
					break;
				case TRIGGER_SITTING:
					yup=CMLib.flags().isSitting(msg.source());
					break;
				case TRIGGER_STANDING:
					yup=(CMLib.flags().isStanding(msg.source()));
					break;
				case TRIGGER_SLEEPING:
					yup=CMLib.flags().isSleeping(msg.source());
					break;
				}
			}
			if((yup)||(TRIG_WATCH[DT.triggerCode]==-999))
			{
				boolean[] checks=(boolean[])trigParts.get(msg.source().Name());
				if(yup)
				{
					recheck=true;
					trigTimes.remove(msg.source().Name());
					trigTimes.put(msg.source().Name(),Long.valueOf(System.currentTimeMillis()));
					if((checks==null)||(checks.length!=V.size()))
					{
						checks=new boolean[V.size()];
						trigParts.put(msg.source().Name(),checks);
					}
				}
				if(checks!=null) checks[v]=yup;
			}
		}
		return recheck;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(norecurse) return;

		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_SERVE:
				msg.source().setWorshipCharID(name());
				break;
			case CMMsg.TYP_REBUKE:
				if(msg.source().getWorshipCharID().equals(Name()))
				{
					msg.source().setWorshipCharID("");
					removeBlessings(msg.source());
					if(msg.source().charStats().getCurrentClass().baseClass().equals("Cleric"))
					{
						removePowers(msg.source());
						msg.source().tell("You feel the wrath of "+name()+"!");
						if(!CMSecurity.isDisabled("LEVELS"))
							msg.source().charStats().getCurrentClass().unLevel(msg.source());
					}
					else
					{
						msg.source().tell(name()+" takes "+xpwrath+" of experience from you.");
						CMLib.leveler().postExperience(msg.source(),null,null,-xpwrath,false);
					}
				}
				break;
			}
		}
		else
		if(msg.source().getWorshipCharID().equals(name()))
		{
			if(numBlessings()>0)
			{
				Vector V=worshipTriggers;
				if(msg.source().charStats().getCurrentClass().baseClass().equals("Cleric"))
					V=clericTriggers;
				if((V!=null)&&(V.size()>0))
				{
					boolean recheck=triggerCheck(msg,V,trigBlessingParts,trigBlessingTimes);

					if((recheck)&&(!norecurse)&&(!alreadyBlessed(msg.source())))
					{
						boolean[] checks=(boolean[])trigBlessingParts.get(msg.source().Name());
						if((checks!=null)&&(checks.length==V.size())&&(checks.length>0))
						{
							boolean rollingTruth=checks[0];
							for(int v=1;v<V.size();v++)
							{
								DeityTrigger DT=(DeityTrigger)V.elementAt(v);
								if(DT.previousConnect==CONNECT_AND)
									rollingTruth=rollingTruth&&checks[v];
								else
									rollingTruth=rollingTruth||checks[v];
							}
							if(rollingTruth)
								bestowBlessings(msg.source());
						}
					}
				}
			}
			if(numCurses()>0)
			{
				Vector V=worshipCurseTriggers;
				if(msg.source().charStats().getCurrentClass().baseClass().equals("Cleric"))
					V=clericCurseTriggers;
				if((V!=null)&&(V.size()>0))
				{
					boolean recheck=triggerCheck(msg,V,trigCurseParts,trigCurseTimes);
					if((recheck)&&(!norecurse))
					{
						boolean[] checks=(boolean[])trigCurseParts.get(msg.source().Name());
						if((checks!=null)&&(checks.length==V.size())&&(checks.length>0))
						{
							boolean rollingTruth=checks[0];
							for(int v=1;v<V.size();v++)
							{
								DeityTrigger DT=(DeityTrigger)V.elementAt(v);
								if(DT.previousConnect==CONNECT_AND)
									rollingTruth=rollingTruth&&checks[v];
								else
									rollingTruth=rollingTruth||checks[v];
							}
							if(rollingTruth)
								bestowCurses(msg.source());
						}
					}
				}
			}
			if((numPowers()>0)
            &&(msg.source().charStats().getCurrentClass().baseClass().equals("Cleric")
                ||(CMSecurity.isASysOp(msg.source()))))
			{
				Vector V=clericPowerTriggers;
				if((V!=null)&&(V.size()>0))
				{
					boolean recheck=triggerCheck(msg,V,trigPowerParts,trigPowerTimes);

					if((recheck)&&(!norecurse)&&(!alreadyPowered(msg.source())))
					{
						boolean[] checks=(boolean[])trigPowerParts.get(msg.source().Name());
						if((checks!=null)&&(checks.length==V.size())&&(checks.length>0))
						{
							boolean rollingTruth=checks[0];
							for(int v=1;v<V.size();v++)
							{
								DeityTrigger DT=(DeityTrigger)V.elementAt(v);
								if(DT.previousConnect==CONNECT_AND)
									rollingTruth=rollingTruth&&checks[v];
								else
									rollingTruth=rollingTruth||checks[v];
							}
							if(rollingTruth)
								bestowPowers(msg.source());
						}
					}
				}
			}

            if((msg.source().charStats().getCurrentClass().baseClass().equals("Cleric")
                ||(CMSecurity.isASysOp(msg.source())))
            &&(CMLib.law().getClericInfused(msg.source().location())==this))
            {
                Vector V=serviceTriggers;
				if((V!=null)&&(V.size()>0))
				{
	                boolean recheck=triggerCheck(msg,V,trigServiceParts,trigServiceTimes);

	                if((recheck)&&(!norecurse)&&(!alreadyServiced(msg.source(),msg.source().location())))
	                {
	                    boolean[] checks=(boolean[])trigServiceParts.get(msg.source().Name());
	                    if((checks!=null)&&(checks.length==V.size())&&(checks.length>0))
	                    {
	                        boolean rollingTruth=checks[0];
	                        for(int v=1;v<V.size();v++)
	                        {
	                            DeityTrigger DT=(DeityTrigger)V.elementAt(v);
	                            if(rollingTruth) startServiceIfNecessary(msg.source(),msg.source().location());
	                            if(DT.previousConnect==CONNECT_AND)
	                                rollingTruth=rollingTruth&&checks[v];
	                            else
	                                rollingTruth=rollingTruth||checks[v];
	                        }
	                        if(rollingTruth)
	                            finishService(msg.source(),msg.source().location());
	                    }
	                }
				}
            }
		}
	}

    protected void startServiceIfNecessary(MOB mob, Room room)
    {
        if((mob==null)||(room==null)) return;
        Vector parishaners=new Vector();
        synchronized(services)
        {
            for(Enumeration<WorshipService> e = DVector.s_enum(services);e.hasMoreElements();)
            	if(e.nextElement().room==room)
            		return;
            WorshipService service = new WorshipService();
            service.room=room;
            service.parishaners = parishaners;
            service.startTime = System.currentTimeMillis();
            service.cleric = mob;
            service.serviceCompleted = false;
            services.add(service);
            Ability A=CMLib.law().getClericInfusion(room);
            if(A!=null) A.setAbilityCode(1);
        }
        Room R=null;
        MOB M=null;
		TrackingLibrary.TrackingFlags flags=new TrackingLibrary.TrackingFlags();
        Vector V=CMLib.tracking().getRadiantRooms(room,flags,5+(mob.envStats().level()/5));
        for(int v=0;v<V.size();v++)
        {
            R=(Room)V.elementAt(v);
            if(CMLib.law().getClericInfused(R)!=this)
            for(int m=0;m<R.numInhabitants();m++)
            {
                M=R.fetchInhabitant(m);
                if(M==null) continue;
                if(M.getWorshipCharID().equals(Name()))
                {
                    if(!M.isMonster())
                        M.tell("Services for "+Name()+" are now starting at "+room.roomTitle(null)+".");
                    else
                    if(!CMLib.flags().isATrackingMonster(M))
                    {
                        Ability TRACKA=CMClass.getAbility("Skill_Track");
                        if(TRACKA!=null)
                        {
                            TRACKA.invoke(M,CMParms.parse("\""+CMLib.map().getExtendedRoomID(room)+"\""),room,true,0);
                            parishaners.addElement(M);
                        }
                    }
                }
            }
        }
    }

    protected void undoService(Vector V)
    {
        MOB M=null;
        Ability A=null;
        for(int m=0;m<V.size();m++)
        {
            M=(MOB)V.elementAt(m);
            if(M==null) continue;
            A=M.fetchEffect("Skill_Track");
            if(A!=null) A.unInvoke();
            M.delEffect(A);
            CMLib.tracking().wanderAway(M,false,true);
        }
    }

    protected boolean alreadyServiced(MOB mob, Room room)
    {
        synchronized(services)
        {
            for(int d=services.size()-1;d>=0;d--)
            {
            	WorshipService service = services.elementAt(d);
                if(System.currentTimeMillis()-service.startTime>(1000*60*30))
                {
                    undoService(service.parishaners);
                    services.removeElementAt(d);
                    Ability A=CMLib.law().getClericInfusion(service.room);
                    if(A!=null) A.setAbilityCode(0);
                }
                else
                if((service.room != null)
                &&(service.room.getArea()==room.getArea())
                &&(service.serviceCompleted))
                    return true;
            }
        }
        return false;
    }

    public boolean finishService(MOB mob, Room room)
    {
        if((mob==null)||(room==null)) return false;
        MOB M=null;
        int totalLevels=0;
        WorshipService service = null;
        synchronized(services)
        {
            for(WorshipService s : services)
                if((s.room==room) && (s.cleric == mob))
                	service = s;
            if(service == null)
                for(WorshipService s : services)
                    if(s.room==room)
                    	service = s;
        }
        if(service == null) return false;
        service.serviceCompleted = true;
        for(int m=0;m<room.numInhabitants();m++)
        {
            M=room.fetchInhabitant(m);
            if(M==null) continue;
            if(M.getWorshipCharID().equals(Name()))
            {
                if((!M.isMonster())&&(M!=mob))
                    CMLib.leveler().postExperience(M,null,null,50,false);
                totalLevels+=M.envStats().level();
                if(!M.isMonster())
                    totalLevels+=(M.envStats().level()*2);
                Ability A=M.fetchEffect("Skill_Convert");
                if(A!=null) A.makeLongLasting();
            }
        }
        undoService(service.parishaners);
        int exp=(int)Math.round(CMath.div(totalLevels,mob.envStats().level())*10.0);
        CMLib.leveler().postExperience(mob,null,null,exp,false);
        trigServiceParts.remove(mob.Name());
        trigServiceTimes.remove(mob.Name());
        return true;
    }

    public boolean cancelService(WorshipService service)
    {
        if(service == null) return false;
        Room room = service.room;
        MOB mob = service.cleric;
        MOB M=null;
        for(int m=0;m<room.numInhabitants();m++)
        {
            M=room.fetchInhabitant(m);
            if(M==null) continue;
            if(M.getWorshipCharID().equals(Name()))
            {
                Ability A=M.fetchEffect("Skill_Convert");
                if(A!=null) A.unInvoke();
            }
        }
		room.showHappens(CMMsg.MASK_ALWAYS, "The service conducted by "+mob.Name()+" has been cancelled.");
		if(mob.location()!=room)
			mob.tell("Your service has been cancelled.");
        undoService(service.parishaners);
        synchronized(services)
        {
        	services.remove(service);
            Ability A=CMLib.law().getClericInfusion(service.room);
            if(A!=null) A.setAbilityCode(0);
        }
        trigServiceParts.remove(mob.Name());
        trigServiceTimes.remove(mob.Name());
        return true;
    }

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		&&((--rebukeCheckDown)<0))
		{
			rebukeCheckDown=10;
			for(Enumeration p=CMLib.players().players();p.hasMoreElements();)
			{
				MOB M=(MOB)p.nextElement();
                if((lastBlackmark>0)
                &&(blacklist!=null)
                &&(blacklist!=M)
                &&((System.currentTimeMillis()-lastBlackmark)<120000))
                    continue;
				if((!M.isMonster())&&(M.getWorshipCharID().equals(name()))&&(CMLib.flags().isInTheGame(M,true)))
				{
                    if(M.charStats().getCurrentClass().baseClass().equalsIgnoreCase("Cleric"))
                    {
                        if(!CMLib.masking().maskCheck(getClericRequirements(),M,true))
                        {
                            if((blacklist==M)&&((++blackmarks)>30))
                            {
                                CMMsg msg=CMClass.getMsg(M,this,null,CMMsg.MSG_REBUKE,"<S-NAME> <S-HAS-HAVE> been rebuked by <T-NAME>!!");
                                if((M.location()!=null)&&(M.okMessage(M,msg)))
                                    M.location().send(M,msg);
                                blackmarks=0;
                                blacklist=null;
                                lastBlackmark=0;
                            }
                            else
                            {
                                if(blacklist!=M)
                                    blackmarks=0;
                                blacklist=M;
                                blackmarks++;
                                lastBlackmark=System.currentTimeMillis();
                                if((blackmarks%5)==0)
                                    M.tell("You feel dirtied by the disappointment of "+name()+".");
                            }
                        }
                        else
                            if(blacklist==M){ blackmarks=0; blacklist=null; lastBlackmark=0;}
                    }
                    else
                    if(!CMLib.masking().maskCheck(getWorshipRequirements(),M,true))
                    {
                        if((blacklist==M)&&((++blackmarks)>30))
                        {
                            CMMsg msg=CMClass.getMsg(M,this,null,CMMsg.MSG_REBUKE,"<S-NAME> <S-HAS-HAVE> been rebuked by <T-NAME>!!");
                            if((M.location()!=null)&&(M.okMessage(M,msg)))
                                M.location().send(M,msg);
                        }
                        else
                        {
                            if(blacklist!=M)
                                blackmarks=0;
                            blacklist=M;
                            blackmarks++;
                            lastBlackmark=System.currentTimeMillis();
                            if(blackmarks==1)
                                M.tell("Woshipper, you have disappointed "+name()+". Make amends or face my wrath!");
                        }
                    }
                    else
                        if(blacklist==M){ blackmarks=0; blacklist=null; lastBlackmark=0;}
				}
                else
                    if(blacklist==M){ blackmarks=0; blacklist=null; lastBlackmark=0;}
			}
			long curTime=System.currentTimeMillis()-60000;
            Long L=null;
            String key=null;
			for(Enumeration e=trigBlessingTimes.keys();e.hasMoreElements();)
			{
				key=(String)e.nextElement();
				L=(Long)trigBlessingTimes.get(key);
				if((L!=null)&&(L.longValue()<curTime))
				{
					trigBlessingTimes.remove(key);
					trigBlessingParts.remove(key);
				}
			}
			for(Enumeration e=trigPowerTimes.keys();e.hasMoreElements();)
			{
				key=(String)e.nextElement();
				L=(Long)trigPowerTimes.get(key);
                if((L!=null)&&(L.longValue()<curTime))
				{
					trigPowerTimes.remove(key);
					trigPowerParts.remove(key);
				}
			}
			for(Enumeration e=trigCurseTimes.keys();e.hasMoreElements();)
			{
                key=(String)e.nextElement();
                L=(Long)trigPowerTimes.get(key);
                if((L!=null)&&(L.longValue()<curTime))
				{
					trigCurseTimes.remove(key);
					trigCurseParts.remove(key);
				}
			}
            for(Enumeration e=trigServiceTimes.keys();e.hasMoreElements();)
            {
                key=(String)e.nextElement();
                L=(Long)trigServiceTimes.get(key);
                if((L!=null)&&(L.longValue()<curTime))
                {
                    synchronized(services)
                    {
                    	WorshipService service = null;
                    	for(int s=services.size()-1;s>=0;s--)
                    	{
                    		service = services.elementAt(s);
                    		if((service.cleric!=null)
                    		&&(service.cleric.Name().equalsIgnoreCase(key))
                    		&&(!service.serviceCompleted))
                    			cancelService(service);
                    	}
                    }
                }
            }
		}
        for(int w=waitingFor.size();w>=0;w--)
        {
            try{
                MOB M=(MOB)waitingFor.elementAt(w);
                waitingFor.removeElement(M);
                executeMsg(this,CMClass.getMsg(M,null,null,CMMsg.MSG_OK_VISUAL,null));
            }catch(Exception e){}
        }
        waitingFor.trimToSize();
		return true;
	}

	public void addBlessing(Ability to, boolean clericOnly)
	{
		if(to==null) return;
		for(int a=0;a<numBlessings();a++)
		{
			Ability A=fetchBlessing(a);
			if((A!=null)&&(A.ID().equals(to.ID())))
				return;
		}
		blessings.addElement(to,Boolean.valueOf(clericOnly));
	}
	public void delBlessing(Ability to)
	{
		blessings.removeElement(to);
	}
	public int numBlessings()
	{
		return blessings.size();
	}
	public Ability fetchBlessing(int index)
	{
		try
		{
			return (Ability)blessings.elementAt(index,1);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
    public boolean fetchBlessingCleric(int index)
    {
        try
        {
            return ((Boolean)blessings.elementAt(index,2)).booleanValue();
        }
        catch(java.lang.ArrayIndexOutOfBoundsException x){}
        return false;
    }
    public boolean fetchBlessingCleric(String ID)
    {
        for(int a=0;a<numBlessings();a++)
        {
            Ability A=fetchBlessing(a);
            if((A!=null)&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
                return fetchBlessingCleric(a);
        }
        return false;
    }
	public Ability fetchBlessing(String ID)
	{
		for(int a=0;a<numBlessings();a++)
		{
			Ability A=fetchBlessing(a);
			if((A!=null)&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
				return A;
		}
		return (Ability)CMLib.english().fetchEnvironmental(blessings.getDimensionVector(1),ID,false);
	}

    protected void parseTriggers(Vector putHere, String trigger)
	{
		putHere.clear();
		trigger=trigger.toUpperCase().trim();
		int previousConnector=CONNECT_AND;
		if(!trigger.equals("-"))
		while(trigger.length()>0)
		{
			int div1=trigger.indexOf("&");
			int div2=trigger.indexOf("|");
			int div=div1;

			if((div2>=0)&&((div<0)||(div2<div)))
				div=div2;
			String trig=null;
			if(div<0)
			{
				trig=trigger;
				trigger="";
			}
			else
			{
				trig=trigger.substring(0,div).trim();
				trigger=trigger.substring(div+1);
			}
			if(trig.length()>0)
			{
				Vector V=CMParms.parse(trig);
				if(V.size()>1)
				{
					String cmd=(String)V.firstElement();
					DeityTrigger DT=new DeityTrigger();
					DT.previousConnect=previousConnector;
					if(cmd.equals("SAY"))
					{
						DT.triggerCode=TRIGGER_SAY;
						DT.parm1=CMParms.combine(V,1);
					}
					else
					if(cmd.equals("TIME"))
					{
						DT.triggerCode=TRIGGER_TIME;
						DT.parm1=""+CMath.s_int(CMParms.combine(V,1));
					}
                    else
                    if(cmd.equals("WAIT"))
                    {
                        DT.triggerCode=TRIGGER_WAIT;
                        DT.parm1=""+CMath.s_int(CMParms.combine(V,1));
                    }
                    else
                    if(cmd.equals("YOUSAY"))
                    {
                        DT.triggerCode=TRIGGER_YOUSAY;
                        DT.parm1=CMParms.combine(V,1);
                    }
					else
                    if(cmd.equals("OTHERSAY"))
                    {
                        DT.triggerCode=TRIGGER_OTHERSAY;
                        DT.parm1=CMParms.combine(V,1);
                    }
                    else
                    if(cmd.equals("ALLSAY"))
                    {
                        DT.triggerCode=TRIGGER_ALLSAY;
                        DT.parm1=CMParms.combine(V,1);
                    }
                    else
					if((cmd.equals("PUTTHING"))||(cmd.equals("PUT")))
					{
						DT.triggerCode=TRIGGER_PUTTHING;
						if(V.size()<3)
						{
							Log.errOut("StdDeity",Name()+"- Illegal trigger: "+trig);
							break;
						}
						DT.parm1=CMParms.combine(V,1,V.size()-2);
						DT.parm2=(String)V.lastElement();
					}
					else
					if(cmd.equals("BURNTHING"))
					{
						DT.triggerCode=TRIGGER_BURNTHING;
						DT.parm1=CMParms.combine(V,1);
					}
					else
					if(cmd.equals("PUTVALUE"))
					{
						DT.triggerCode=TRIGGER_PUTVALUE;
						if(V.size()<3)
						{
							Log.errOut("StdDeity",Name()+"- Illegal trigger: "+trig);
							break;
						}
						DT.parm1=""+CMath.s_int((String)V.elementAt(1));
						DT.parm2=CMParms.combine(V,2);
					}
					else
					if(cmd.equals("BURNVALUE"))
					{
						DT.triggerCode=TRIGGER_BURNVALUE;
						if(V.size()<3)
						{
							Log.errOut("StdDeity",Name()+"- Illegal trigger: "+trig);
							break;
						}
						DT.parm1=""+CMath.s_int(CMParms.combine(V,1));
					}
					else
					if((cmd.equals("BURNMATERIAL"))||(cmd.equals("BURN")))
					{
						DT.triggerCode=TRIGGER_BURNMATERIAL;
						DT.parm1=CMParms.combine(V,1);
						int cd = RawMaterial.CODES.FIND_StartsWith(DT.parm1);
						boolean found=cd>=0;
						if(found) 
							DT.parm1=""+cd;
						else
						for(int i=0;i<RawMaterial.MATERIAL_DESCS.length;i++)
						{
							if(RawMaterial.MATERIAL_DESCS[i].startsWith(DT.parm1))
							{
								DT.parm1=""+(i<<8);
								found=true;
							}
						}
						if(!found)
						{
							Log.errOut("StdDeity",Name()+"- Unknown material: "+trig);
							break;
						}
					}
					else
					if(cmd.equals("PUTMATERIAL"))
					{
						DT.triggerCode=TRIGGER_PUTMATERIAL;
						if(V.size()<3)
						{
							Log.errOut("StdDeity",Name()+"- Illegal trigger: "+trig);
							break;
						}
						DT.parm1=(String)V.elementAt(1);
						DT.parm2=CMParms.combine(V,2);
						int cd = RawMaterial.CODES.FIND_StartsWith(DT.parm1);
						boolean found=cd>=0;
						if(found)
							DT.parm1=""+cd;
						else
						if(!found)
						for(int i=0;i<RawMaterial.MATERIAL_DESCS.length;i++)
						{
							if(RawMaterial.MATERIAL_DESCS[i].startsWith(DT.parm1))
							{
								DT.parm1=""+(i<<8);
								found=true;
							}
						}
						if(!found)
						{
							Log.errOut("StdDeity",Name()+"- Unknown material: "+trig);
							break;
						}
					}
					else
					if(cmd.equals("EAT"))
					{
						DT.triggerCode=TRIGGER_EAT;
						DT.parm1=CMParms.combine(V,1);
					}
					else
					if(cmd.equals("READ"))
					{
						DT.triggerCode=TRIGGER_READING;
						DT.parm1=CMParms.combine(V,1);
					}
					else
					if(cmd.equals("RANDOM"))
					{
						DT.triggerCode=TRIGGER_RANDOM;
						DT.parm1=CMParms.combine(V,1);
					}
					else
					if(cmd.equals("CHECK"))
					{
						DT.triggerCode=TRIGGER_CHECK;
						DT.parm1=CMParms.combine(V,1);
					}
					else
					if(cmd.equals("DRINK"))
					{
						DT.triggerCode=TRIGGER_DRINK;
						DT.parm1=CMParms.combine(V,1);
					}
					else
					if(cmd.equals("INROOM"))
					{
						DT.triggerCode=TRIGGER_INROOM;
						DT.parm1=CMParms.combine(V,1);
					}
					else
					if(cmd.equals("RIDING"))
					{
						DT.triggerCode=TRIGGER_RIDING;
						DT.parm1=CMParms.combine(V,1);
					}
					else
					if(cmd.equals("CAST"))
					{
						DT.triggerCode=TRIGGER_CAST;
						DT.parm1=CMParms.combine(V,1);
						if(CMClass.findAbility(DT.parm1)==null)
						{
							Log.errOut("StdDeity",Name()+"- Illegal SPELL in: "+trig);
							break;
						}
					}
					else
					if(cmd.equals("EMOTE"))
					{
						DT.triggerCode=TRIGGER_EMOTE;
						DT.parm1=CMParms.combine(V,1);
					}
					else
					if(cmd.startsWith("SIT"))
					{
						DT.triggerCode=TRIGGER_SITTING;
					}
					else
					if(cmd.startsWith("STAND"))
					{
						DT.triggerCode=TRIGGER_STANDING;
					}
					else
					if(cmd.startsWith("SLEEP"))
					{
						DT.triggerCode=TRIGGER_SLEEPING;
					}
					else
					{
						Log.errOut("StdDeity",Name()+"- Illegal trigger: '"+cmd+"','"+trig+"'");
						break;
					}
					putHere.addElement(DT);
				}
				else
				{
					Log.errOut("StdDeity",Name()+"- Illegal trigger (need more parameters): "+trig);
					break;
				}
			}
			if(div==div1)
				previousConnector=CONNECT_AND;
			else
				previousConnector=CONNECT_OR;
		}
	}

    protected static final int TRIGGER_SAY=0;
    protected static final int TRIGGER_TIME=1;
    protected static final int TRIGGER_PUTTHING=2;
    protected static final int TRIGGER_BURNTHING=3;
    protected static final int TRIGGER_EAT=4;
    protected static final int TRIGGER_DRINK=5;
    protected static final int TRIGGER_INROOM=6;
    protected static final int TRIGGER_RIDING=7;
    protected static final int TRIGGER_CAST=8;
    protected static final int TRIGGER_EMOTE=9;
    protected static final int TRIGGER_PUTVALUE=10;
    protected static final int TRIGGER_PUTMATERIAL=11;
    protected static final int TRIGGER_BURNMATERIAL=12;
    protected static final int TRIGGER_BURNVALUE=13;
    protected static final int TRIGGER_SITTING=14;
    protected static final int TRIGGER_STANDING=15;
    protected static final int TRIGGER_SLEEPING=16;
    protected static final int TRIGGER_READING=17;
    protected static final int TRIGGER_RANDOM=18;
    protected static final int TRIGGER_CHECK=19;
    protected static final int TRIGGER_WAIT=20;
    protected static final int TRIGGER_YOUSAY=21;
    protected static final int TRIGGER_OTHERSAY=22;
    protected static final int TRIGGER_ALLSAY=23;
    protected static final int[] TRIG_WATCH={
		CMMsg.TYP_SPEAK,		//0
		-999,					//1
		CMMsg.TYP_PUT,			//2
		CMMsg.TYP_FIRE,		//3
		CMMsg.TYP_EAT,			//4
		CMMsg.TYP_DRINK,		//5
		CMMsg.TYP_LOOK,		//6
		-999,					//7
		CMMsg.TYP_CAST_SPELL,  //8
		CMMsg.TYP_EMOTE,		//9
		CMMsg.TYP_PUT,			//10
		CMMsg.TYP_PUT,			//11
		CMMsg.TYP_FIRE,		//12
		CMMsg.TYP_FIRE,		//13
		-999,					//14
		-999,					//15
		-999,					//16
		CMMsg.TYP_READ,//17
		-999,					//18
		-999,					//19
        -999,                   //20
        -999,                   //21
        -999,                   //22
        -999,                   //23
	};

    protected static final int CONNECT_AND=0;
    protected static final int CONNECT_OR=1;

    protected static class DeityTrigger
	{
		public int triggerCode=TRIGGER_SAY;
		public int previousConnect=CONNECT_AND;
		public String parm1=null;
		public String parm2=null;
	}

	/** Manipulation of curse objects, which includes spells, traits, skills, etc.*/
	public void addCurse(Ability to, boolean clericOnly)
	{
		if(to==null) return;
		for(int a=0;a<numCurses();a++)
		{
			Ability A=fetchCurse(a);
			if((A!=null)&&(A.ID().equals(to.ID())))
				return;
		}
		curses.addElement(to, Boolean.valueOf(clericOnly));
	}
	public void delCurse(Ability to)
	{
		curses.removeElement(to);
	}
	public int numCurses()
	{
		return curses.size();
	}
	public Ability fetchCurse(int index)
	{
		try
		{
			return (Ability)curses.elementAt(index,1);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchCurse(String ID)
	{
		for(int a=0;a<numCurses();a++)
		{
			Ability A=fetchCurse(a);
			if((A!=null)&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
				return A;
		}
		return (Ability)CMLib.english().fetchEnvironmental(curses.getDimensionVector(1),ID,false);
	}

    public boolean fetchCurseCleric(int index)
    {
        try
        {
            return ((Boolean)curses.elementAt(index,2)).booleanValue();
        }
        catch(java.lang.ArrayIndexOutOfBoundsException x){}
        return false;
    }
    public boolean fetchCurseCleric(String ID)
    {
        for(int a=0;a<numCurses();a++)
        {
            Ability A=fetchCurse(a);
            if((A!=null)&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
                return fetchCurseCleric(a);
        }
        return false;
    }

	public String getClericSin()
	{ return clericSin;}
	public void setClericSin(String ritual)
	{
		clericSin=ritual;
		parseTriggers(clericCurseTriggers,ritual);
	}
	public String getClericSinDesc()
	{
		if(numCurses()>0)
			return "The curses of "+name()+" are placed upon "+charStats().hisher()+" clerics whenever the cleric does the following: "+getTriggerDesc(clericCurseTriggers)+".";
		return "";
	}

	public String getWorshipSin()
	{ return worshipSin;}
	public void setWorshipSin(String ritual)
	{
		worshipSin=ritual;
		parseTriggers(worshipCurseTriggers,ritual);
	}
	public String getWorshipSinDesc()
	{
		if(numCurses()>0)
			return "The curses of "+name()+" are placed upon "+charStats().hisher()+" worshippers whenever the worshipper does the following: "+getTriggerDesc(clericCurseTriggers)+".";
		return "";
	}

	/** Manipulation of granted clerical powers, which includes spells, traits, skills, etc.*/
	/** Make sure that none of these can really be qualified for by the cleric!*/
	/** Manipulation of curse objects, which includes spells, traits, skills, etc.*/
	public void addPower(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numPowers();a++)
		{
			Ability A=fetchPower(a);
			if((A!=null)&&(A.ID().equals(to.ID())))
				return;
		}
		powers.addElement(to);
	}
	public void delPower(Ability to)
	{
		powers.removeElement(to);
	}
	public int numPowers()
	{
		return powers.size();
	}
	public Ability fetchPower(int index)
	{
		try
		{
			return (Ability)powers.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchPower(String ID)
	{
		for(int a=0;a<numPowers();a++)
		{
			Ability A=fetchPower(a);
			if((A!=null)&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
				return A;
		}
		return (Ability)CMLib.english().fetchEnvironmental(powers,ID,false);
	}

	public String getClericPowerup()
	{
		return clericPowerup;
	}
	public void setClericPowerup(String ritual){
		clericPowerup=ritual;
		parseTriggers(clericPowerTriggers,ritual);
	}
	public String getClericPowerupDesc()
	{
		if(numPowers()>0)
			return "Special powers of "+name()+" are bestowed to "+charStats().hisher()+" clerics whenever the cleric does the following: "+getTriggerDesc(clericPowerTriggers)+".";
		return "";
	}
}
