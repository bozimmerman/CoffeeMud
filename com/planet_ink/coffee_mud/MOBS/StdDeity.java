package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.interfaces.*;
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
@SuppressWarnings({"unchecked","rawtypes"})
public class StdDeity extends StdMOB implements Deity
{
	@Override public String ID(){return "StdDeity";}

	protected int 		xpwrath=100;
	protected String 	clericReqs="";
	protected String 	worshipReqs="";
	protected String 	serviceRitual="";
	protected String 	clericRitual="";
	protected String 	clericSin="";
	protected String 	clericPowerup="";
	protected String 	worshipRitual="";
	protected String 	worshipSin="";
	protected int 		rebukeCheckDown=0;
	protected boolean 	norecurse=false;
	protected MOB 		blacklist=null;
	protected int 		blackmarks=0;
	protected long 		lastBlackmark=0;

	protected List<DeityTrigger> 	worshipTriggers=new Vector<DeityTrigger>();
	protected List<DeityTrigger> 	worshipCurseTriggers=new Vector<DeityTrigger>();
	protected List<DeityTrigger> 	clericTriggers=new Vector<DeityTrigger>();
	protected List<DeityTrigger> 	serviceTriggers=new Vector<DeityTrigger>();
	protected List<DeityTrigger> 	clericPowerTriggers=new Vector<DeityTrigger>();
	protected List<DeityTrigger> 	clericCurseTriggers=new Vector<DeityTrigger>();
	protected List<DeityPower> 	 	blessings=new SVector<DeityPower>();
	protected List<DeityPower>	 	curses=new SVector<DeityPower>();
	protected List<Ability> 	 	powers=new SVector<Ability>();
	protected Map<String,boolean[]> trigBlessingParts=new SHashtable();
	protected Map<String,Long> 		trigBlessingTimes=new SHashtable();
	protected Map<String,boolean[]> trigPowerParts=new SHashtable();
	protected Map<String,Long> 		trigPowerTimes=new SHashtable();
	protected Map<String,boolean[]> trigCurseParts=new SHashtable();
	protected Map<String,Long> 		trigCurseTimes=new SHashtable();
	protected Map<String,boolean[]> trigServiceParts=new SHashtable();
	protected Map<String,Long> 		trigServiceTimes=new SHashtable();
	protected List<WorshipService> 	services=new SVector<WorshipService>();
	protected List<MOB> 			waitingFor=new SLinkedList();

	public StdDeity()
	{
		super();
		username="a Mighty Deity";
		setDescription("He is Mighty.");
		setDisplayText("A Mighty Deity stands here!");
		basePhyStats().setWeight(700);
		basePhyStats().setAbility(200);
		basePhyStats().setArmor(0);
		basePhyStats().setAttackAdjustment(1000);
		basePhyStats().setDamage(1000);
		baseCharStats().setMyRace(CMClass.getRace("Spirit"));
		recoverPhyStats();
	}

	private static class WorshipService
	{
		public MOB cleric = null;
		public Room room = null;
		public boolean serviceCompleted = false;
		public long startTime = System.currentTimeMillis();
		public List<MOB> parishaners = new Vector<MOB>();
	}

	private static class DeityPower
	{
		public Ability power;
		public boolean clericOnly;
		public DeityPower(Ability A, boolean clericsOnly)
		{ power=A; clericOnly=clericsOnly;}
	}

	@Override
	protected void cloneFix(MOB E)
	{
		super.cloneFix(E);
		if(E instanceof StdDeity)
		{
			worshipTriggers=new XVector<DeityTrigger>(((StdDeity)E).worshipTriggers);
			worshipCurseTriggers=new XVector<DeityTrigger>(((StdDeity)E).worshipCurseTriggers);
			clericTriggers=new XVector<DeityTrigger>(((StdDeity)E).clericTriggers);
			clericPowerTriggers=new XVector<DeityTrigger>(((StdDeity)E).clericPowerTriggers);
			clericCurseTriggers=new XVector<DeityTrigger>(((StdDeity)E).clericCurseTriggers);
			blessings=new XVector<DeityPower>(((StdDeity)E).blessings);
			curses=new XVector<DeityPower>(((StdDeity)E).curses);
			powers=new XVector(((StdDeity)E).powers);
			trigBlessingParts=new XHashtable<String,boolean[]>(((StdDeity)E).trigBlessingParts);
			trigBlessingTimes=new XHashtable<String,Long>(((StdDeity)E).trigBlessingTimes);
			trigPowerParts=new XHashtable<String,boolean[]>(((StdDeity)E).trigPowerParts);
			trigPowerTimes=new XHashtable<String,Long>(((StdDeity)E).trigPowerTimes);
			trigCurseParts=new XHashtable<String,boolean[]>(((StdDeity)E).trigCurseParts);
			trigCurseTimes=new XHashtable<String,Long>(((StdDeity)E).trigCurseTimes);
			trigServiceParts=new XHashtable<String,boolean[]>(((StdDeity)E).trigServiceParts);
			trigServiceTimes=new XHashtable<String,Long>(((StdDeity)E).trigServiceTimes);
		}
	}

	@Override
	public String getClericRequirements()
	{
		return clericReqs;
	}
	@Override
	public void setClericRequirements(String reqs)
	{
		clericReqs=reqs;
	}
	@Override
	public String getWorshipRequirements()
	{
		return worshipReqs;
	}
	@Override
	public void setWorshipRequirements(String reqs)
	{
		worshipReqs=reqs;
	}

	@Override
	public String getClericRitual()
	{
		if(clericRitual.trim().length()==0) return "SAY Bless me "+name();
		return clericRitual;
	}
	@Override
	public void setClericRitual(String ritual)
	{
		clericRitual=ritual;
		parseTriggers(clericTriggers,ritual);
	}
	@Override
	public String getWorshipRitual()
	{
		if(worshipRitual.trim().length()==0)
			return "SAY Bless me "+name();
		return worshipRitual;
	}
	@Override
	public void setWorshipRitual(String ritual)
	{
		worshipRitual=ritual;
		parseTriggers(worshipTriggers,ritual);
	}
	@Override
	public String getServiceRitual()
	{
		return serviceRitual;
	}
	@Override
	public void setServiceRitual(String ritual)
	{
		if((ritual==null)||(ritual.length()==0))
			ritual="SAY Bless us "+name()+"&wait 10&wait 10&SAY May "+name()+" bless you all&ALLSAY Amen.&SAY Go in peace";
		serviceRitual=ritual;
		parseTriggers(serviceTriggers,ritual);
	}

	public String getTriggerDesc(List<DeityTrigger> V)
	{
		if((V==null)||(V.size()==0)) return "Never";
		final StringBuffer buf=new StringBuffer("");
		for(int v=0;v<V.size();v++)
		{
			final DeityTrigger DT=V.get(v);
			if(v>0) buf.append(", "+((DT.previousConnect==CONNECT_AND)?"and ":"or "));
			switch(DT.triggerCode)
			{
			case TRIGGER_SAY:
				buf.append(_("the player should say '@x1'",DT.parm1.toLowerCase()));
				break;
			case TRIGGER_READING:
				if(DT.parm1.equals("0"))
					buf.append(_("the player should read something"));
				else
					buf.append(_("the player should read '@x1'",DT.parm1.toLowerCase()));
				break;
			case TRIGGER_TIME:
				buf.append(_("the hour of the day is @x1",DT.parm1.toLowerCase()));
				break;
			case TRIGGER_PUTTHING:
				buf.append(_("the player should put @x1 in @x2",DT.parm1.toLowerCase(),DT.parm2.toLowerCase()));
				break;
			case TRIGGER_BURNTHING:
				buf.append(_("the player should burn @x1",DT.parm1.toLowerCase()));
				break;
			case TRIGGER_DRINK:
				buf.append(_("the player should drink @x1",DT.parm1.toLowerCase()));
				break;
			case TRIGGER_EAT:
				buf.append(_("the player should eat @x1",DT.parm1.toLowerCase()));
				break;
			case TRIGGER_INROOM:
				{
				if(DT.parm1.equalsIgnoreCase("holy")
				||DT.parm1.equalsIgnoreCase("unholy")
				||DT.parm1.equalsIgnoreCase("balance"))
					buf.append(_("the player should be in the deities room of infused @x1-ness.",DT.parm1.toLowerCase()));
				else
				{
					final Room R=CMLib.map().getRoom(DT.parm1);
					if(R==null)
						buf.append(_("the player should be in some unknown place"));
					else
						buf.append(_("the player should be in '@x1'",R.displayText(null)));
				}
				}
				break;
			case TRIGGER_RIDING:
				buf.append(_("the player should be on @x1",DT.parm1.toLowerCase()));
				break;
			case TRIGGER_CAST:
				{
				final Ability A=CMClass.findAbility(DT.parm1);
				if(A==null)
					buf.append(_("the player should cast '@x1'",DT.parm1));
				else
					buf.append(_("the player should cast '@x1'",A.name()));
				}
				break;
			case TRIGGER_EMOTE:
				buf.append(_("the player should emote '@x1'",DT.parm1.toLowerCase()));
				break;
			case TRIGGER_RANDOM:
				buf.append(DT.parm1+"% of the time");
				break;
			case TRIGGER_WAIT:
				buf.append(_("wait @x1 seconds",""+((CMath.s_int(DT.parm1)*CMProps.getTickMillis())/1000)));
				break;
			case TRIGGER_YOUSAY:
				buf.append(_("then you will automatically say '@x1'",DT.parm1.toLowerCase()));
				break;
			case TRIGGER_OTHERSAY:
				buf.append(_("then all others will say '@x1'",DT.parm1.toLowerCase()));
				break;
			case TRIGGER_ALLSAY:
				buf.append(_("then all will say '@x1'",DT.parm1.toLowerCase()));
				break;
			case TRIGGER_CHECK:
				buf.append(CMLib.masking().maskDesc(DT.parm1));
				break;
			case TRIGGER_PUTVALUE:
				buf.append(_("the player should put an item worth at least @x1 in @x2",DT.parm1.toLowerCase(),DT.parm2.toLowerCase()));
				break;
			case TRIGGER_PUTMATERIAL:
				{
					String material="something";
					final int t=CMath.s_int(DT.parm1);
					RawMaterial.Material m;
					if(((t&RawMaterial.RESOURCE_MASK)==0)
					&&((m=RawMaterial.Material.findByMask(t))!=null))
						material=m.desc().toLowerCase();
					else
					if(RawMaterial.CODES.IS_VALID(t))
						material=RawMaterial.CODES.NAME(t).toLowerCase();
					buf.append(_("the player puts an item made of @x1 in @x2",material,DT.parm2.toLowerCase()));
				}
				break;
			case TRIGGER_BURNMATERIAL:
				{
					String material="something";
					final int t=CMath.s_int(DT.parm1);
					RawMaterial.Material m;
					if(((t&RawMaterial.RESOURCE_MASK)==0)
					&&((m=RawMaterial.Material.findByMask(t))!=null))
						material=m.desc().toLowerCase();
					else
					if(RawMaterial.CODES.IS_VALID(t))
						material=RawMaterial.CODES.NAME(t).toLowerCase();
					buf.append(_("the player should burn an item made of @x1",material));
				}
				break;
			case TRIGGER_BURNVALUE:
				buf.append(_("the player should burn an item worth at least @x1",DT.parm1.toLowerCase()));
				break;
			case TRIGGER_SITTING:
				buf.append(_("the player should sit down"));
				break;
			case TRIGGER_STANDING:
				buf.append(_("the player should stand up"));
				break;
			case TRIGGER_SLEEPING:
				buf.append(_("the player should go to sleep"));
				break;
			}
		}
		return buf.toString();
	}

	@Override
	public String getClericRequirementsDesc()
	{
		return "The following may be clerics of "+name()+": "+CMLib.masking().maskDesc(getClericRequirements());
	}
	@Override
	public String getClericTriggerDesc()
	{
		if(numBlessings()>0)
			return "The blessings of "+name()+" are bestowed to "+charStats().hisher()+" clerics whenever the cleric does the following: "+getTriggerDesc(clericTriggers)+".";
		return "";
	}
	@Override
	public String getWorshipRequirementsDesc()
	{
		return "The following are acceptable worshipers of "+name()+": "+CMLib.masking().maskDesc(getWorshipRequirements());
	}
	@Override
	public String getWorshipTriggerDesc()
	{
		if(numBlessings()>0)
			return "The blessings of "+name()+" are bestowed to "+charStats().hisher()+" worshippers whenever they do the following: "+getTriggerDesc(worshipTriggers)+".";
		return "";
	}

	@Override
	public String getServiceTriggerDesc()
	{
		return "The services of "+name()+" are the following: "+getTriggerDesc(serviceTriggers)+".";
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
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
				msg.source().tell(_("You already worship @x1.",name()));
				if(msg.source().isMonster())
					CMLib.commands().postSay(msg.source(),null,_("I already worship @x1.",msg.source().getMyDeity().name()));
				return false;
			}
			if(msg.source().getMyDeity()!=null)
			{
				msg.source().tell(_("You already worship @x1.",msg.source().getMyDeity().name()));
				if(msg.source().isMonster())
					CMLib.commands().postSay(msg.source(),null,_("I already worship @x1.",msg.source().getMyDeity().name()));
				return false;
			}
			if(msg.source().charStats().getCurrentClass().baseClass().equalsIgnoreCase("Cleric"))
			{
				if(!CMLib.masking().maskCheck(getClericRequirements(),msg.source(),true))
				{
					msg.source().tell(_("You are unworthy of serving @x1.",name()));
					if(msg.source().isMonster())
						CMLib.commands().postSay(msg.source(),null,_("I am unworthy of serving @x1.",name()));
					return false;
				}
			}
			else
			if(!CMLib.masking().maskCheck(getWorshipRequirements(),msg.source(),true))
			{
				msg.source().tell(_("You are unworthy of @x1.",name()));
				if(msg.source().isMonster())
					CMLib.commands().postSay(msg.source(),null,_("I am unworthy of @x1.",name()));
				return false;
			}
			break;
		case CMMsg.TYP_REBUKE:
			if(!msg.source().getWorshipCharID().equals(Name()))
			{
				msg.source().tell(_("You do not worship @x1.",name()));
				return false;
			}
			break;
		}
		return true;
	}

	public synchronized void bestowBlessing(MOB mob, Ability Blessing)
	{
		final Room prevRoom=location();
		mob.location().bringMobHere(this,false);
		if(Blessing!=null)
		{
			final Vector V=new Vector();
			if(Blessing.canTarget(Ability.CAN_MOBS))
			{
				V.addElement(mob.name()+"$");
				Blessing.invoke(this,V,mob,true,mob.phyStats().level());
			}
			else
			if(Blessing.canTarget(Ability.CAN_ITEMS))
			{
				Item I=mob.fetchWieldedItem();
				if(I==null) I=mob.fetchHeldItem();
				if(I==null) I=mob.fetchItem(null,Wearable.FILTER_WORNONLY,"all");
				if(I==null) I=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,"all");
				if(I==null) return;
				V.addElement("$"+I.name()+"$");
				addItem(I);
				Blessing.invoke(this,V,I,true,mob.phyStats().level());
				delItem(I);
				if(!mob.isMine(I)) mob.addItem(I);
			}
			else
				Blessing.invoke(this,mob,true,mob.phyStats().level());
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
		final Room prevRoom=location();
		mob.location().bringMobHere(this,false);
		if(Curse!=null)
		{
			final Vector V=new Vector();
			if(Curse.canTarget(Ability.CAN_MOBS))
			{
				V.addElement(mob.location().getContextName(mob));
				Curse.invoke(this,V,mob,true,mob.phyStats().level());
			}
			else
			if(Curse.canTarget(Ability.CAN_ITEMS))
			{
				Item I=mob.fetchWieldedItem();
				if(I==null) I=mob.fetchHeldItem();
				if(I==null) I=mob.fetchItem(null,Wearable.FILTER_WORNONLY,"all");
				if(I==null) I=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,"all");
				if(I==null) return;
				V.addElement("$"+I.name()+"$");
				addItem(I);
				Curse.invoke(this,V,I,true,mob.phyStats().level());
				delItem(I);
				if(!mob.isMine(I)) mob.addItem(I);
			}
			else
				Curse.invoke(this,mob,true,mob.phyStats().level());
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
				mob.location().show(this,mob,CMMsg.MSG_OK_VISUAL,_("You feel the presence of <S-NAME> in <T-NAME>."));
				if((mob.charStats().getCurrentClass().baseClass().equals("Cleric"))
				||(CMSecurity.isASysOp(mob)))
				{
					for(int b=0;b<numBlessings();b++)
					{
						final Ability Blessing=fetchBlessing(b);
						if(Blessing!=null)
							bestowBlessing(mob,Blessing);
					}
				}
				else
				{
					final int randNum=CMLib.dice().roll(1,numBlessings(),-1);
					final Ability Blessing=fetchBlessing(randNum);
					if((Blessing!=null)&&(!fetchBlessingCleric(randNum)))
						bestowBlessing(mob,Blessing);
				}
			}
		}
		catch(final Exception e)
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
				mob.location().show(this,mob,CMMsg.MSG_OK_VISUAL,_("You feel the power of <S-NAME> in <T-NAME>."));
				final Ability Power=fetchPower(CMLib.dice().roll(1,numPowers(),-1));
				if(Power!=null)
					bestowPower(mob,Power);
			}
		}
		catch(final Exception e)
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
				mob.location().show(this,mob,CMMsg.MSG_OK_VISUAL,_("You feel the wrath of <S-NAME> in <T-NAME>."));
				if(mob.charStats().getCurrentClass().baseClass().equals("Cleric")
				||(CMSecurity.isASysOp(mob)))
				{
					for(int b=0;b<numCurses();b++)
					{
						final Ability Curse=fetchCurse(b);
						if(Curse!=null)
							bestowCurse(mob,Curse);
					}
				}
				else
				{
					final int randNum=CMLib.dice().roll(1,numCurses(),-1);
					final Ability Curse=fetchCurse(randNum);
					if((Curse!=null)&&(!fetchBlessingCleric(randNum)))
						bestowCurse(mob,Curse);
				}
			}
		}
		catch(final Exception e)
		{
			Log.errOut("StdDeity",e);
		}
		norecurse=false;
	}

	public void removeBlessings(MOB mob)
	{
		if((alreadyBlessed(mob))&&(mob.location()!=null))
		{
			mob.location().show(this,mob,CMMsg.MSG_OK_VISUAL,_("<S-NAME> remove(s) <S-HIS-HER> blessings from <T-NAME>."));
			for(int a=mob.numEffects()-1;a>=0;a--) // reverse, and personal
			{
				final Ability A=mob.fetchEffect(a);
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
			mob.location().show(this,mob,CMMsg.MSG_OK_VISUAL,_("<S-NAME> remove(s) <S-HIS-HER> powers from <T-NAME>."));
			for(int a=mob.numAbilities()-1;a>=0;a--)
			{
				Ability A=mob.fetchAbility(a);
				if((A!=null)&&(!A.isSavable()))
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
		for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A.invoker()==this))
				return true;
		}
		return false;
	}

	public boolean alreadyPowered(MOB mob)
	{
		if(numPowers()>0)
		for(int a=0;a<mob.numAbilities();a++)
		{
			final Ability A=mob.fetchAbility(a);
			if((A!=null)&&(!A.isSavable()))
				return true;
		}
		return false;
	}

	public boolean triggerCheck(CMMsg msg, List<DeityTrigger> V, Map<String, boolean[]> trigParts, Map<String, Long> trigTimes)
	{
		boolean recheck=false;
		for(int v=0;v<V.size();v++)
		{
			boolean yup=false;
			final DeityTrigger DT=V.get(v);
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
					&&(msg.source().location().getArea().getTimeObj().getHourOfDay()==CMath.s_int(DT.parm1)))
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
						final boolean[] checks=trigParts.get(msg.source().Name());
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
						final boolean[] checks=trigParts.get(msg.source().Name());
						final Room R=msg.source().location();
						if((checks!=null)&&(checks[v-1])&&(!checks[v])&&(R!=null))
						{
							yup=true;
							for(int m=0;m<R.numInhabitants();m++)
							{
								final MOB M=R.fetchInhabitant(m);
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
						final boolean[] checks=trigParts.get(msg.source().Name());
						final Room R=msg.source().location();
						if((checks!=null)&&(checks[v-1])&&(!checks[v])&&(R!=null))
						{
							yup=true;
							for(int m=0;m<R.numInhabitants();m++)
							{
								final MOB M=R.fetchInhabitant(m);
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
						final boolean[] checks=trigParts.get(msg.source().Name());
						if((checks!=null)&&(checks[v-1])&&(!checks[v])&&(trigTimes.get(msg.source().Name())!=null))
						{
							boolean proceed=true;
							for(int t=v+1;t<checks.length;t++)
								if(checks[t]) proceed=false;
							if(proceed)
							{
								if(System.currentTimeMillis()>(trigTimes.get(msg.source().Name()).longValue()+(CMath.s_int(DT.parm1)*CMProps.getTickMillis())))
								{
								   yup=true;
								   waitingFor.remove(msg.source());
								}
								else
								{
									waitingFor.add(msg.source());
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
				boolean[] checks=trigParts.get(msg.source().Name());
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

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
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
						msg.source().tell(_("You feel the wrath of @x1!",name()));
						if(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
							msg.source().charStats().getCurrentClass().unLevel(msg.source());
					}
					else
					{
						msg.source().tell(_("@x1 takes @x2 of experience from you.",name(),""+xpwrath));
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
				List<DeityTrigger> V=worshipTriggers;
				if(msg.source().charStats().getCurrentClass().baseClass().equals("Cleric"))
					V=clericTriggers;
				if((V!=null)&&(V.size()>0))
				{
					final boolean recheck=triggerCheck(msg,V,trigBlessingParts,trigBlessingTimes);

					if((recheck)&&(!norecurse)&&(!alreadyBlessed(msg.source())))
					{
						final boolean[] checks=trigBlessingParts.get(msg.source().Name());
						if((checks!=null)&&(checks.length==V.size())&&(checks.length>0))
						{
							boolean rollingTruth=checks[0];
							for(int v=1;v<V.size();v++)
							{
								final DeityTrigger DT=V.get(v);
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
				List<DeityTrigger> V=worshipCurseTriggers;
				if(msg.source().charStats().getCurrentClass().baseClass().equals("Cleric"))
					V=clericCurseTriggers;
				if((V!=null)&&(V.size()>0))
				{
					final boolean recheck=triggerCheck(msg,V,trigCurseParts,trigCurseTimes);
					if((recheck)&&(!norecurse))
					{
						final boolean[] checks=trigCurseParts.get(msg.source().Name());
						if((checks!=null)&&(checks.length==V.size())&&(checks.length>0))
						{
							boolean rollingTruth=checks[0];
							for(int v=1;v<V.size();v++)
							{
								final DeityTrigger DT=V.get(v);
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
				final List<DeityTrigger> V=clericPowerTriggers;
				if((V!=null)&&(V.size()>0))
				{
					final boolean recheck=triggerCheck(msg,V,trigPowerParts,trigPowerTimes);

					if((recheck)&&(!norecurse)&&(!alreadyPowered(msg.source())))
					{
						final boolean[] checks=trigPowerParts.get(msg.source().Name());
						if((checks!=null)&&(checks.length==V.size())&&(checks.length>0))
						{
							boolean rollingTruth=checks[0];
							for(int v=1;v<V.size();v++)
							{
								final DeityTrigger DT=V.get(v);
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
				final List<DeityTrigger> V=serviceTriggers;
				if((V!=null)&&(V.size()>0))
				{
					final boolean recheck=triggerCheck(msg,V,trigServiceParts,trigServiceTimes);

					if((recheck)&&(!norecurse)&&(!alreadyServiced(msg.source(),msg.source().location())))
					{
						final boolean[] checks=trigServiceParts.get(msg.source().Name());
						if((checks!=null)&&(checks.length==V.size())&&(checks.length>0))
						{
							boolean rollingTruth=checks[0];
							for(int v=1;v<V.size();v++)
							{
								final DeityTrigger DT=V.get(v);
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
		final Vector parishaners=new Vector();
		synchronized(services)
		{
			for(final WorshipService w : services)
				if(w.room==room)
					return;
			final WorshipService service = new WorshipService();
			service.room=room;
			service.parishaners = parishaners;
			service.startTime = System.currentTimeMillis();
			service.cleric = mob;
			service.serviceCompleted = false;
			services.add(service);
			final Ability A=CMLib.law().getClericInfusion(room);
			if(A!=null) A.setAbilityCode(1);
		}
		Room R=null;
		MOB M=null;
		final TrackingLibrary.TrackingFlags flags=new TrackingLibrary.TrackingFlags();
		final List<Room> V=CMLib.tracking().getRadiantRooms(room,flags,5+(mob.phyStats().level()/5));
		for(int v=0;v<V.size();v++)
		{
			R=V.get(v);
			if(CMLib.law().getClericInfused(R)!=this)
			for(int m=0;m<R.numInhabitants();m++)
			{
				M=R.fetchInhabitant(m);
				if(M==null) continue;
				if(M.getWorshipCharID().equals(Name()))
				{
					if(!M.isMonster())
						M.tell(_("Services for @x1 are now starting at @x2.",Name(),room.displayText(null)));
					else
					if(!CMLib.flags().isATrackingMonster(M))
					{
						final Ability TRACKA=CMClass.getAbility("Skill_Track");
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

	protected void undoService(List<MOB> V)
	{
		MOB M=null;
		Ability A=null;
		for(int m=V.size()-1;m>=0;m--)
		{
			M=V.get(m);
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
				final WorshipService service = services.get(d);
				if(System.currentTimeMillis()-service.startTime>(1000*60*30))
				{
					undoService(service.parishaners);
					services.remove(d);
					final Ability A=CMLib.law().getClericInfusion(service.room);
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
			for(final WorshipService s : services)
				if((s.room==room) && (s.cleric == mob))
					service = s;
			if(service == null)
				for(final WorshipService s : services)
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
				totalLevels+=M.phyStats().level();
				if(!M.isMonster())
					totalLevels+=(M.phyStats().level()*2);
				final Ability A=M.fetchEffect("Skill_Convert");
				if(A!=null) A.makeLongLasting();
			}
		}
		undoService(service.parishaners);
		final int exp=(int)Math.round(CMath.div(totalLevels,mob.phyStats().level())*10.0);
		CMLib.leveler().postExperience(mob,null,null,exp,false);
		trigServiceParts.remove(mob.Name());
		trigServiceTimes.remove(mob.Name());
		return true;
	}

	public boolean cancelService(WorshipService service)
	{
		if(service == null) return false;
		final Room room = service.room;
		final MOB mob = service.cleric;
		MOB M=null;
		for(int m=0;m<room.numInhabitants();m++)
		{
			M=room.fetchInhabitant(m);
			if(M==null) continue;
			if(M.getWorshipCharID().equals(Name()))
			{
				final Ability A=M.fetchEffect("Skill_Convert");
				if(A!=null) A.unInvoke();
			}
		}
		room.showHappens(CMMsg.MASK_ALWAYS, _("The service conducted by @x1 has been cancelled.",mob.Name()));
		if(mob.location()!=room)
			mob.tell(_("Your service has been cancelled."));
		undoService(service.parishaners);
		synchronized(services)
		{
			services.remove(service);
			final Ability A=CMLib.law().getClericInfusion(service.room);
			if(A!=null) A.setAbilityCode(0);
		}
		trigServiceParts.remove(mob.Name());
		trigServiceTimes.remove(mob.Name());
		return true;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		&&((--rebukeCheckDown)<0))
		{
			rebukeCheckDown=10;
			for(final Enumeration p=CMLib.players().players();p.hasMoreElements();)
			{
				final MOB M=(MOB)p.nextElement();
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
								final CMMsg msg=CMClass.getMsg(M,this,null,CMMsg.MSG_REBUKE,_("<S-NAME> <S-HAS-HAVE> been rebuked by <T-NAME>!!"));
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
									M.tell(_("You feel dirtied by the disappointment of @x1.",name()));
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
							final CMMsg msg=CMClass.getMsg(M,this,null,CMMsg.MSG_REBUKE,_("<S-NAME> <S-HAS-HAVE> been rebuked by <T-NAME>!!"));
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
								M.tell(_("Woshipper, you have disappointed @x1. Make amends or face my wrath!",name()));
						}
					}
					else
						if(blacklist==M){ blackmarks=0; blacklist=null; lastBlackmark=0;}
				}
				else
					if(blacklist==M){ blackmarks=0; blacklist=null; lastBlackmark=0;}
			}
			final long curTime=System.currentTimeMillis()-60000;
			Long L=null;
			for(final String key : trigBlessingTimes.keySet())
			{
				L=trigBlessingTimes.get(key);
				if((L!=null)&&(L.longValue()<curTime))
				{
					trigBlessingTimes.remove(key);
					trigBlessingParts.remove(key);
				}
			}
			for(final String key : trigPowerTimes.keySet())
			{
				L=trigPowerTimes.get(key);
				if((L!=null)&&(L.longValue()<curTime))
				{
					trigPowerTimes.remove(key);
					trigPowerParts.remove(key);
				}
			}
			for(final String key : trigCurseTimes.keySet())
			{
				L=trigCurseTimes.get(key);
				if((L!=null)&&(L.longValue()<curTime))
				{
					trigCurseTimes.remove(key);
					trigCurseParts.remove(key);
				}
			}
			for(final String key : trigServiceTimes.keySet())
			{
				L=trigServiceTimes.get(key);
				if((L!=null)&&(L.longValue()<curTime))
				{
					LinkedList<WorshipService> delThese = null;
					synchronized(services)
					{
						for(final WorshipService service : services)
							if((service.cleric!=null)
							&&(service.cleric.Name().equalsIgnoreCase(key))
							&&(!service.serviceCompleted))
							{
								if(delThese == null)
									delThese = new LinkedList<WorshipService>();
								delThese.add(service);
							}
					}
					if(delThese != null)
						for(final WorshipService w : delThese)
							cancelService(w);
				}
			}
		}
		for (final MOB M : waitingFor)
		{
			try
			{
				executeMsg(this,CMClass.getMsg(M,null,null,CMMsg.MSG_OK_VISUAL,null));
			}catch(final Exception e){}
		}
		waitingFor.clear();
		return true;
	}

	@Override
	public void addBlessing(Ability to, boolean clericOnly)
	{
		if(to==null) return;
		for(int a=0;a<numBlessings();a++)
		{
			final Ability A=fetchBlessing(a);
			if((A!=null)&&(A.ID().equals(to.ID())))
				return;
		}
		blessings.add(new DeityPower(to,clericOnly));
	}
	@Override
	public void delBlessing(Ability to)
	{
		if(blessings.size()==0) return;
		for(final DeityPower P : blessings)
			if(P.power==to)
				blessings.remove(P);
	}
	@Override
	public int numBlessings()
	{
		return blessings.size();
	}
	@Override
	public Ability fetchBlessing(int index)
	{
		try
		{
			return blessings.get(index).power;
		}
		catch(final java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	@Override
	public boolean fetchBlessingCleric(int index)
	{
		try
		{
			return blessings.get(index).clericOnly;
		}
		catch(final java.lang.ArrayIndexOutOfBoundsException x){}
		return false;
	}
	@Override
	public boolean fetchBlessingCleric(String ID)
	{
		for(int a=0;a<numBlessings();a++)
		{
			final Ability A=fetchBlessing(a);
			if((A!=null)&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
				return fetchBlessingCleric(a);
		}
		return false;
	}
	@Override
	public Ability fetchBlessing(String ID)
	{
		for(int a=0;a<numBlessings();a++)
		{
			final Ability A=fetchBlessing(a);
			if((A!=null)&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
				return A;
		}
		return (Ability)CMLib.english().fetchEnvironmental(new ConvertingList<DeityPower,Ability>(blessings,new Converter<DeityPower,Ability>()
		{
			@Override public Ability convert(DeityPower obj) { return obj.power;}
		}),ID,false);
	}

	protected void parseTriggers(List<DeityTrigger> putHere, String trigger)
	{
		putHere.clear();
		trigger=trigger.toUpperCase().trim();
		int previousConnector=CONNECT_AND;
		if(!trigger.equals("-"))
		while(trigger.length()>0)
		{
			final int div1=trigger.indexOf('&');
			final int div2=trigger.indexOf('|');
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
				final Vector<String> V=CMParms.parse(trig);
				if(V.size()>1)
				{
					final String cmd=V.firstElement();
					final DeityTrigger DT=new DeityTrigger();
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
						DT.parm2=V.lastElement();
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
						DT.parm1=""+CMath.s_int(V.elementAt(1));
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
						final int cd = RawMaterial.CODES.FIND_StartsWith(DT.parm1);
						boolean found=cd>=0;
						if(found)
							DT.parm1=""+cd;
						else
						{
							final RawMaterial.Material m=RawMaterial.Material.startsWith(DT.parm1);
							if(m!=null)
							{
								DT.parm1=""+m.mask();
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
						DT.parm1=V.elementAt(1);
						DT.parm2=CMParms.combine(V,2);
						final int cd = RawMaterial.CODES.FIND_StartsWith(DT.parm1);
						boolean found=cd>=0;
						if(found)
							DT.parm1=""+cd;
						else
						if(!found)
						{
							final RawMaterial.Material m=RawMaterial.Material.startsWith(DT.parm1);
							if(m!=null)
							{
								DT.parm1=""+m.mask();
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
					putHere.add(DT);
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
		-999,   				//20
		-999,   				//21
		-999,   				//22
		-999,   				//23
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
	@Override
	public void addCurse(Ability to, boolean clericOnly)
	{
		if(to==null) return;
		for(int a=0;a<numCurses();a++)
		{
			final Ability A=fetchCurse(a);
			if((A!=null)&&(A.ID().equals(to.ID())))
				return;
		}
		curses.add(new DeityPower(to,clericOnly));
	}
	@Override
	public void delCurse(Ability to)
	{
		if((curses.size()==0)||(to==null)) return;
		for(int a=numCurses()-1;a>=0;a--)
		{
			final Ability A=fetchCurse(a);
			if(A==to)
				curses.remove(a);
		}
	}
	@Override
	public int numCurses()
	{
		return curses.size();
	}
	@Override
	public Ability fetchCurse(int index)
	{
		try
		{
			return curses.get(index).power;
		}
		catch(final java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	@Override
	public Ability fetchCurse(String ID)
	{
		for(int a=0;a<numCurses();a++)
		{
			final Ability A=fetchCurse(a);
			if((A!=null)&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
				return A;
		}
		return (Ability)CMLib.english().fetchEnvironmental(new ConvertingList<DeityPower,Ability>(curses,new Converter<DeityPower,Ability>()
		{
			@Override public Ability convert(DeityPower obj) { return obj.power;}
		}),ID,false);
	}

	@Override
	public boolean fetchCurseCleric(int index)
	{
		try
		{
			return curses.get(index).clericOnly;
		}
		catch(final java.lang.ArrayIndexOutOfBoundsException x){}
		return false;
	}
	@Override
	public boolean fetchCurseCleric(String ID)
	{
		for(int a=0;a<numCurses();a++)
		{
			final Ability A=fetchCurse(a);
			if((A!=null)&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
				return fetchCurseCleric(a);
		}
		return false;
	}

	@Override
	public String getClericSin()
	{
		return clericSin;
	}
	@Override
	public void setClericSin(String ritual)
	{
		clericSin=ritual;
		parseTriggers(clericCurseTriggers,ritual);
	}
	@Override
	public String getClericSinDesc()
	{
		if(numCurses()>0)
			return "The curses of "+name()+" are placed upon "+charStats().hisher()+" clerics whenever the cleric does the following: "+getTriggerDesc(clericCurseTriggers)+".";
		return "";
	}

	@Override
	public String getWorshipSin()
	{
		return worshipSin;
	}
	@Override
	public void setWorshipSin(String ritual)
	{
		worshipSin=ritual;
		parseTriggers(worshipCurseTriggers,ritual);
	}
	@Override
	public String getWorshipSinDesc()
	{
		if(numCurses()>0)
			return "The curses of "+name()+" are placed upon "+charStats().hisher()+" worshippers whenever the worshipper does the following: "+getTriggerDesc(clericCurseTriggers)+".";
		return "";
	}

	/** Manipulation of granted clerical powers, which includes spells, traits, skills, etc.*/
	/** Make sure that none of these can really be qualified for by the cleric!*/
	/** Manipulation of curse objects, which includes spells, traits, skills, etc.*/
	@Override
	public void addPower(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numPowers();a++)
		{
			final Ability A=fetchPower(a);
			if((A!=null)&&(A.ID().equals(to.ID())))
				return;
		}
		powers.add(to);
	}
	@Override
	public void delPower(Ability to)
	{
		powers.remove(to);
	}
	@Override
	public int numPowers()
	{
		return powers.size();
	}
	@Override
	public Ability fetchPower(int index)
	{
		try
		{
			return powers.get(index);
		}
		catch(final java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	@Override
	public Ability fetchPower(String ID)
	{
		for(int a=0;a<numPowers();a++)
		{
			final Ability A=fetchPower(a);
			if((A!=null)&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
				return A;
		}
		return (Ability)CMLib.english().fetchEnvironmental(powers,ID,false);
	}

	@Override
	public String getClericPowerup()
	{
		return clericPowerup;
	}
	@Override
	public void setClericPowerup(String ritual)
	{
		clericPowerup=ritual;
		parseTriggers(clericPowerTriggers,ritual);
	}
	@Override
	public String getClericPowerupDesc()
	{
		if(numPowers()>0)
			return "Special powers of "+name()+" are bestowed to "+charStats().hisher()+" clerics whenever the cleric does the following: "+getTriggerDesc(clericPowerTriggers)+".";
		return "";
	}
}
