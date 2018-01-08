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
   Copyright 2001-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "StdDeity";
	}

	protected int		xpwrath			= 100;
	protected String	clericReqs		= "";
	protected String	worshipReqs		= "";
	protected String	serviceRitual	= "";
	protected String	clericRitual	= "";
	protected String	clericSin		= "";
	protected String	clericPowerup	= "";
	protected String	worshipRitual	= "";
	protected String	worshipSin		= "";
	protected int		rebukeCheckDown	= 0;
	protected boolean	norecurse		= false;
	protected MOB		blacklist		= null;
	protected int		blackmarks		= 0;
	protected long		lastBlackmark	= 0;

	protected List<DeityTrigger>	worshipTriggers		= new Vector<DeityTrigger>();
	protected List<DeityTrigger>	worshipCurseTriggers= new Vector<DeityTrigger>();
	protected List<DeityTrigger>	clericTriggers		= new Vector<DeityTrigger>();
	protected List<DeityTrigger>	serviceTriggers		= new Vector<DeityTrigger>();
	protected List<DeityTrigger>	clericPowerTriggers	= new Vector<DeityTrigger>();
	protected List<DeityTrigger>	clericCurseTriggers	= new Vector<DeityTrigger>();
	protected List<DeityPower>		blessings			= new SVector<DeityPower>();
	protected List<DeityPower>		curses				= new SVector<DeityPower>();
	protected List<Ability>			powers				= new SVector<Ability>();
	protected Map<String, boolean[]>trigBlessingParts	= new SHashtable();
	protected Map<String, Long>		trigBlessingTimes	= new SHashtable();
	protected Map<String, boolean[]>trigPowerParts		= new SHashtable();
	protected Map<String, Long>		trigPowerTimes		= new SHashtable();
	protected Map<String, boolean[]>trigCurseParts		= new SHashtable();
	protected Map<String, Long>		trigCurseTimes		= new SHashtable();
	protected Map<String, boolean[]>trigServiceParts	= new SHashtable();
	protected Map<String, Long>		trigServiceTimes	= new SHashtable();
	protected List<WorshipService>	services			= new SVector<WorshipService>();
	protected List<MOB>				waitingFor			= new SLinkedList();

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
		{
			power=A;
			clericOnly=clericsOnly;
		}
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
			powers=new XVector<Ability>(((StdDeity)E).powers);
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
		if(clericRitual.trim().length()==0)
			return "SAY Bless me "+name();
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
		if((V==null)||(V.size()==0))
			return L("Never");
		final StringBuffer buf=new StringBuffer("");
		for(int v=0;v<V.size();v++)
		{
			final DeityTrigger DT=V.get(v);
			if(v>0)
				buf.append(", "+((DT.previousConnect==RitualConnector.AND)?L("and "):L("or ")));
			switch(DT.triggerCode)
			{
			case SAY:
				buf.append(L("the player should say '@x1'",DT.parm1.toLowerCase()));
				break;
			case READING:
				if(DT.parm1.equals("0"))
					buf.append(L("the player should read something"));
				else
					buf.append(L("the player should read '@x1'",DT.parm1.toLowerCase()));
				break;
			case TIME:
				buf.append(L("the hour of the day is @x1",DT.parm1.toLowerCase()));
				break;
			case PUTTHING:
				buf.append(L("the player should put @x1 in @x2",DT.parm1.toLowerCase(),DT.parm2.toLowerCase()));
				break;
			case BURNTHING:
				buf.append(L("the player should burn @x1",DT.parm1.toLowerCase()));
				break;
			case DRINK:
				buf.append(L("the player should drink @x1",DT.parm1.toLowerCase()));
				break;
			case EAT:
				buf.append(L("the player should eat @x1",DT.parm1.toLowerCase()));
				break;
			case INROOM:
				{
				if(DT.parm1.equalsIgnoreCase("holy")
				||DT.parm1.equalsIgnoreCase("unholy")
				||DT.parm1.equalsIgnoreCase("balance"))
					buf.append(L("the player should be in the deities room of infused @x1-ness.",DT.parm1.toLowerCase()));
				else
				{
					final Room R=CMLib.map().getRoom(DT.parm1);
					if(R==null)
						buf.append(L("the player should be in some unknown place"));
					else
						buf.append(L("the player should be in '@x1'",R.displayText(null)));
				}
				}
				break;
			case RIDING:
				buf.append(L("the player should be on @x1",DT.parm1.toLowerCase()));
				break;
			case CAST:
				{
				final Ability A=CMClass.findAbility(DT.parm1);
				if(A==null)
					buf.append(L("the player should cast '@x1'",DT.parm1));
				else
					buf.append(L("the player should cast '@x1'",A.name()));
				}
				break;
			case EMOTE:
				buf.append(L("the player should emote '@x1'",DT.parm1.toLowerCase()));
				break;
			case RANDOM:
				buf.append(DT.parm1+"% of the time");
				break;
			case WAIT:
				buf.append(L("wait @x1 seconds",""+((CMath.s_int(DT.parm1)*CMProps.getTickMillis())/1000)));
				break;
			case YOUSAY:
				buf.append(L("then you will automatically say '@x1'",DT.parm1.toLowerCase()));
				break;
			case OTHERSAY:
				buf.append(L("then all others will say '@x1'",DT.parm1.toLowerCase()));
				break;
			case ALLSAY:
				buf.append(L("then all will say '@x1'",DT.parm1.toLowerCase()));
				break;
			case CHECK:
				buf.append(CMLib.masking().maskDesc(DT.parm1));
				break;
			case PUTVALUE:
				buf.append(L("the player should put an item worth at least @x1 in @x2",DT.parm1.toLowerCase(),DT.parm2.toLowerCase()));
				break;
			case PUTMATERIAL:
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
					buf.append(L("the player puts an item made of @x1 in @x2",material,DT.parm2.toLowerCase()));
				}
				break;
			case BURNMATERIAL:
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
					buf.append(L("the player should burn an item made of @x1",material));
				}
				break;
			case BURNVALUE:
				buf.append(L("the player should burn an item worth at least @x1",DT.parm1.toLowerCase()));
				break;
			case SITTING:
				buf.append(L("the player should sit down"));
				break;
			case STANDING:
				buf.append(L("the player should stand up"));
				break;
			case SLEEPING:
				buf.append(L("the player should go to sleep"));
				break;
			}
		}
		return buf.toString();
	}

	@Override
	public String getClericRequirementsDesc()
	{
		return L("The following may be clerics of @x1: @x2",name(),CMLib.masking().maskDesc(getClericRequirements()));
	}

	@Override
	public String getClericTriggerDesc()
	{
		if(numBlessings()>0)
			return L("The blessings of @x1 are placed upon @x2 clerics whenever the cleric does the following: @x3.",name(),charStats().hisher(),getTriggerDesc(clericTriggers));
		return "";
	}

	@Override
	public String getWorshipRequirementsDesc()
	{
		return L("The following are acceptable worshipers of @x1: @x2",name(),CMLib.masking().maskDesc(getWorshipRequirements()));
	}

	@Override
	public String getWorshipTriggerDesc()
	{
		if(numBlessings()>0)
			return L("The blessings of @x1 are placed upon @x2 worshippers whenever they do the following: @x3.",name(),charStats().hisher(),getTriggerDesc(worshipTriggers));
		return "";
	}

	@Override
	public String getServiceTriggerDesc()
	{
		return L("The services of @x1 are the following: @x2.",name(),getTriggerDesc(serviceTriggers));
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
				msg.source().tell(L("You already worship @x1.",name()));
				if(msg.source().isMonster())
					CMLib.commands().postSay(msg.source(),null,L("I already worship @x1.",msg.source().getMyDeity().name()));
				return false;
			}
			if(msg.source().getMyDeity()!=null)
			{
				msg.source().tell(L("You already worship @x1.",msg.source().getMyDeity().name()));
				if(msg.source().isMonster())
					CMLib.commands().postSay(msg.source(),null,L("I already worship @x1.",msg.source().getMyDeity().name()));
				return false;
			}
			if(msg.source().charStats().getCurrentClass().baseClass().equalsIgnoreCase("Cleric"))
			{
				if(!CMLib.masking().maskCheck(getClericRequirements(),msg.source(),true))
				{
					msg.source().tell(L("You are unworthy of serving @x1.",name()));
					if(msg.source().isMonster())
						CMLib.commands().postSay(msg.source(),null,L("I am unworthy of serving @x1.",name()));
					return false;
				}
			}
			else
			if(!CMLib.masking().maskCheck(getWorshipRequirements(),msg.source(),true))
			{
				msg.source().tell(L("You are unworthy of @x1.",name()));
				if(msg.source().isMonster())
					CMLib.commands().postSay(msg.source(),null,L("I am unworthy of @x1.",name()));
				return false;
			}
			break;
		case CMMsg.TYP_REBUKE:
			if(!msg.source().getWorshipCharID().equals(Name()))
			{
				msg.source().tell(L("You do not worship @x1.",name()));
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
			final Vector<String> V=new Vector<String>();
			if(Blessing.canTarget(Ability.CAN_MOBS))
			{
				V.addElement(mob.name()+"$");
				Blessing.invoke(this,V,mob,true,mob.phyStats().level());
			}
			else
			if(Blessing.canTarget(Ability.CAN_ITEMS))
			{
				Item I=mob.fetchWieldedItem();
				if(I==null)
					I=mob.fetchHeldItem();
				if(I==null)
					I=mob.fetchItem(null,Wearable.FILTER_WORNONLY,"all");
				if(I==null)
					I=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,"all");
				if(I==null)
					return;
				V.addElement("$"+I.name()+"$");
				addItem(I);
				Blessing.invoke(this,V,I,true,mob.phyStats().level());
				delItem(I);
				if(!mob.isMine(I))
					mob.addItem(I);
			}
			else
				Blessing.invoke(this,mob,true,mob.phyStats().level());
		}
		prevRoom.bringMobHere(this,false);
		if(mob.location()!=prevRoom)
		{
			if(mob.getVictim()==this)
				mob.makePeace(true);
			if(getVictim()==mob)
				makePeace(true);
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
			final Vector<String> V=new Vector<String>();
			if(Curse.canTarget(Ability.CAN_MOBS))
			{
				V.addElement(mob.location().getContextName(mob));
				Curse.invoke(this,V,mob,true,mob.phyStats().level());
			}
			else
			if(Curse.canTarget(Ability.CAN_ITEMS))
			{
				Item I=mob.fetchWieldedItem();
				if(I==null)
					I=mob.fetchHeldItem();
				if(I==null)
					I=mob.fetchItem(null,Wearable.FILTER_WORNONLY,"all");
				if(I==null)
					I=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,"all");
				if(I==null)
					return;
				V.addElement("$"+I.name()+"$");
				addItem(I);
				Curse.invoke(this,V,I,true,mob.phyStats().level());
				delItem(I);
				if(!mob.isMine(I))
					mob.addItem(I);
			}
			else
				Curse.invoke(this,mob,true,mob.phyStats().level());
		}
		prevRoom.bringMobHere(this,false);
		if(mob.location()!=prevRoom)
		{
			if(mob.getVictim()==this)
				mob.makePeace(true);
			if(getVictim()==mob)
				makePeace(true);
		}
	}

	public synchronized void bestowBlessings(MOB mob)
	{
		norecurse=true;
		try
		{
			if((!alreadyBlessed(mob))&&(numBlessings()>0))
			{
				mob.location().show(this,mob,CMMsg.MSG_OK_VISUAL,L("You feel the presence of <S-NAME> in <T-NAME>."));
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
				mob.location().show(this,mob,CMMsg.MSG_OK_VISUAL,L("You feel the power of <S-NAME> in <T-NAME>."));
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
				mob.location().show(this,mob,CMMsg.MSG_OK_VISUAL,L("You feel the wrath of <S-NAME> in <T-NAME>."));
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
			mob.location().show(this,mob,CMMsg.MSG_OK_VISUAL,L("<S-NAME> remove(s) <S-HIS-HER> blessings from <T-NAME>."));
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
			mob.location().show(this,mob,CMMsg.MSG_OK_VISUAL,L("<S-NAME> remove(s) <S-HIS-HER> powers from <T-NAME>."));
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
			if((msg.sourceMinor()==DT.triggerCode.getCMMsgCode())
			||(DT.triggerCode.getCMMsgCode()==-999))
			{
				switch(DT.triggerCode)
				{
				case SAY:
					if((msg.sourceMessage()!=null)&&(msg.sourceMessage().toUpperCase().indexOf(DT.parm1)>0))
						yup=true;
					break;
				case TIME:
					if((msg.source().location()!=null)
					&&(msg.source().location().getArea().getTimeObj().getHourOfDay()==CMath.s_int(DT.parm1)))
					   yup=true;
					break;
				case RANDOM:
					if(CMLib.dice().rollPercentage()<=CMath.s_int(DT.parm1))
						yup=true;
					break;
				case YOUSAY:
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
				case ALLSAY:
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
				case OTHERSAY:
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
				case WAIT:
				{
					if(v<=0)
						yup=true;
					else
					{
						final boolean[] checks=trigParts.get(msg.source().Name());
						if((checks!=null)
						&&(checks[v-1])
						&&(!checks[v])
						&&(trigTimes.get(msg.source().Name())!=null))
						{
							boolean proceed=true;
							for(int t=v+1;t<checks.length;t++)
							{
								if(checks[t])
									proceed=false;
							}
							if(proceed)
							{
								if(System.currentTimeMillis()>(trigTimes.get(msg.source().Name()).longValue()+(CMath.s_int(DT.parm1)*CMProps.getTickMillis())))
								{
									yup=true;
									synchronized(waitingFor)
									{
										waitingFor.remove(msg.source());
									}
								}
								else
								{
									synchronized(waitingFor)
									{
										waitingFor.add(msg.source());
									}
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
				case CHECK:
					if(CMLib.masking().maskCheck(DT.parm1,msg.source(),true))
						yup=true;
					break;
				case PUTTHING:
					if((msg.target() instanceof Container)
					&&(msg.tool() instanceof Item)
					&&(CMLib.english().containsString(msg.tool().name(),DT.parm1))
					&&(CMLib.english().containsString(msg.target().name(),DT.parm2)))
						yup=true;
					break;
				case BURNTHING:
				case READING:
				case DRINK:
				case EAT:
					if((msg.target()!=null)
					&&(DT.parm1.equals("0")||CMLib.english().containsString(msg.target().name(),DT.parm1)))
					   yup=true;
					break;
				case INROOM:
					if(msg.source().location()!=null)
					{
						if(DT.parm1.equalsIgnoreCase("holy")||DT.parm1.equalsIgnoreCase("unholy")||DT.parm1.equalsIgnoreCase("balance"))
							yup=CMLib.law().getClericInfused(msg.source().location())==this;
						else
						if(msg.source().location().roomID().equalsIgnoreCase(DT.parm1))
							yup=true;
					}
					break;
				case RIDING:
					if((msg.source().riding()!=null)
					&&(CMLib.english().containsString(msg.source().riding().name(),DT.parm1)))
					   yup=true;
					break;
				case CAST:
					if((msg.tool()!=null)
					&&((msg.tool().ID().equalsIgnoreCase(DT.parm1))
					||(CMLib.english().containsString(msg.tool().name(),DT.parm1))))
						yup=true;
					break;
				case EMOTE:
					if((msg.sourceMessage()!=null)&&(msg.sourceMessage().toUpperCase().indexOf(DT.parm1)>0))
						yup=true;
					break;
				case PUTVALUE:
					if((msg.tool() instanceof Item)
					&&(((Item)msg.tool()).baseGoldValue()>=CMath.s_int(DT.parm1))
					&&(msg.target() instanceof Container)
					&&(CMLib.english().containsString(msg.target().name(),DT.parm2)))
						yup=true;
					break;
				case PUTMATERIAL:
					if((msg.tool() instanceof Item)
					&&(((((Item)msg.tool()).material()&RawMaterial.RESOURCE_MASK)==CMath.s_int(DT.parm1))
						||((((Item)msg.tool()).material()&RawMaterial.MATERIAL_MASK)==CMath.s_int(DT.parm1)))
					&&(msg.target() instanceof Container)
					&&(CMLib.english().containsString(msg.target().name(),DT.parm2)))
						yup=true;
					break;
				case BURNMATERIAL:
					if((msg.target() instanceof Item)
					&&(((((Item)msg.target()).material()&RawMaterial.RESOURCE_MASK)==CMath.s_int(DT.parm1))
						||((((Item)msg.target()).material()&RawMaterial.MATERIAL_MASK)==CMath.s_int(DT.parm1))))
							yup=true;
					break;
				case BURNVALUE:
					if((msg.target() instanceof Item)
					&&(((Item)msg.target()).baseGoldValue()>=CMath.s_int(DT.parm1)))
						yup=true;
					break;
				case SITTING:
					yup=CMLib.flags().isSitting(msg.source());
					break;
				case STANDING:
					yup=(CMLib.flags().isStanding(msg.source()));
					break;
				case SLEEPING:
					yup=CMLib.flags().isSleeping(msg.source());
					break;
				}
			}
			if((yup)||(DT.triggerCode.getCMMsgCode()==-999))
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
				if(checks!=null)
					checks[v]=yup;
			}
		}
		return recheck;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(norecurse)
			return;

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
						msg.source().tell(L("You feel the wrath of @x1!",name()));
						if(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
							msg.source().charStats().getCurrentClass().unLevel(msg.source());
					}
					else
					{
						msg.source().tell(L("@x1 takes @x2 of experience from you.",name(),""+xpwrath));
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
								if(DT.previousConnect==RitualConnector.AND)
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
								if(DT.previousConnect==RitualConnector.AND)
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
								if(DT.previousConnect==RitualConnector.AND)
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
								if(rollingTruth)
									startServiceIfNecessary(msg.source(),msg.source().location());
								if(DT.previousConnect==RitualConnector.AND)
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
		if((mob==null)||(room==null))
			return;
		final Vector<MOB> parishaners=new Vector<MOB>();
		synchronized(services)
		{
			for(final WorshipService w : services)
			{
				if(w.room==room)
					return;
			}
			final WorshipService service = new WorshipService();
			service.room=room;
			service.parishaners = parishaners;
			service.startTime = System.currentTimeMillis();
			service.cleric = mob;
			service.serviceCompleted = false;
			services.add(service);
			final Ability A=CMLib.law().getClericInfusion(room);
			if(A!=null)
				A.setAbilityCode(1);
		}
		Room R=null;
		MOB M=null;
		final TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags();
		final List<Room> V=CMLib.tracking().getRadiantRooms(room,flags,5+(mob.phyStats().level()/5));
		for(int v=0;v<V.size();v++)
		{
			R=V.get(v);
			if(CMLib.law().getClericInfused(R)!=this)
			for(int m=0;m<R.numInhabitants();m++)
			{
				M=R.fetchInhabitant(m);
				if(M==null)
					continue;
				if(M.getWorshipCharID().equals(Name()))
				{
					if(!M.isMonster())
						M.tell(L("Services for @x1 are now starting at @x2.",Name(),room.displayText(null)));
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
			if(M==null)
				continue;
			A=M.fetchEffect("Skill_Track");
			if(A!=null)
				A.unInvoke();
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
					if(A!=null)
						A.setAbilityCode(0);
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
		if((mob==null)||(room==null))
			return false;
		MOB M=null;
		int totalLevels=0;
		WorshipService service = null;
		synchronized(services)
		{
			for(final WorshipService s : services)
			{
				if((s.room==room) && (s.cleric == mob))
					service = s;
			}
			if(service == null)
			{
				for(final WorshipService s : services)
				{
					if(s.room==room)
						service = s;
				}
			}
		}
		if(service == null)
			return false;
		service.serviceCompleted = true;
		for(int m=0;m<room.numInhabitants();m++)
		{
			M=room.fetchInhabitant(m);
			if(M==null)
				continue;
			if(M.getWorshipCharID().equals(Name()))
			{
				if((!M.isMonster())&&(M!=mob))
					CMLib.leveler().postExperience(M,null,null,50,false);
				totalLevels+=M.phyStats().level();
				if(!M.isMonster())
					totalLevels+=(M.phyStats().level()*2);
				final Ability A=M.fetchEffect("Skill_Convert");
				if(A!=null)
					A.makeLongLasting();
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
		if(service == null)
			return false;
		final Room room = service.room;
		final MOB mob = service.cleric;
		MOB M=null;
		for(int m=0;m<room.numInhabitants();m++)
		{
			M=room.fetchInhabitant(m);
			if(M==null)
				continue;
			if(M.getWorshipCharID().equals(Name()))
			{
				final Ability A=M.fetchEffect("Skill_Convert");
				if(A!=null)
					A.unInvoke();
			}
		}
		room.showHappens(CMMsg.MASK_ALWAYS, L("The service conducted by @x1 has been cancelled.",mob.Name()));
		if(mob.location()!=room)
			mob.tell(L("Your service has been cancelled."));
		undoService(service.parishaners);
		synchronized(services)
		{
			services.remove(service);
			final Ability A=CMLib.law().getClericInfusion(service.room);
			if(A!=null)
				A.setAbilityCode(0);
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
								final CMMsg msg=CMClass.getMsg(M,this,null,CMMsg.MSG_REBUKE,L("<S-NAME> <S-HAS-HAVE> been rebuked by <T-NAME>!!"));
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
									M.tell(L("You feel dirtied by the disappointment of @x1.",name()));
							}
						}
						else
						if(blacklist==M)
						{
							blackmarks=0;
							blacklist=null;
							lastBlackmark=0;
						}
					}
					else
					if(!CMLib.masking().maskCheck(getWorshipRequirements(),M,true))
					{
						if((blacklist==M)&&((++blackmarks)>30))
						{
							final CMMsg msg=CMClass.getMsg(M,this,null,CMMsg.MSG_REBUKE,L("<S-NAME> <S-HAS-HAVE> been rebuked by <T-NAME>!!"));
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
								M.tell(L("Worshipper, you have disappointed @x1. Make amends or face my wrath!",name()));
						}
					}
					else
					if(blacklist==M)
					{
						blackmarks=0;
						blacklist=null;
						lastBlackmark=0;
					}
				}
				else
				if(blacklist==M)
				{
					blackmarks=0;
					blacklist=null;
					lastBlackmark=0;
				}
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
						{
							if((service.cleric!=null)
							&&(service.cleric.Name().equalsIgnoreCase(key))
							&&(!service.serviceCompleted))
							{
								if(delThese == null)
									delThese = new LinkedList<WorshipService>();
								delThese.add(service);
							}
						}
					}
					if(delThese != null)
					{
						for(final WorshipService w : delThese)
							cancelService(w);
					}
				}
			}
		}
		if(waitingFor.size()>0)
		{
			final List<MOB> executeMOBList; 
			synchronized(waitingFor)
			{
				if(waitingFor.size()>0)
				{
					executeMOBList=new ArrayList(waitingFor);
					waitingFor.clear();
				}
				else
					executeMOBList=null;
			}
			if(executeMOBList != null)
			{
				for (final MOB M : executeMOBList)
				{
					try
					{
						executeMsg(this,CMClass.getMsg(M,null,null,CMMsg.MSG_OK_VISUAL,null));
					}
					catch(final Exception e)
					{
					}
				}
			}
		}
		return true;
	}

	@Override
	public void addBlessing(Ability to, boolean clericOnly)
	{
		if(to==null)
			return;
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
		if(blessings.size()==0)
			return;
		for(final DeityPower P : blessings)
		{
			if(P.power==to)
				blessings.remove(P);
		}
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
		catch(final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
		return null;
	}

	@Override
	public boolean fetchBlessingCleric(int index)
	{
		try
		{
			return blessings.get(index).clericOnly;
		}
		catch(final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
		return false;
	}

	@Override
	public boolean fetchBlessingCleric(String ID)
	{
		for(int a=0;a<numBlessings();a++)
		{
			final Ability A=fetchBlessing(a);
			if((A!=null)
			&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
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
			if((A!=null)
			&&((A.ID().equalsIgnoreCase(ID))||(A.Name().equalsIgnoreCase(ID))))
				return A;
		}
		return (Ability)CMLib.english().fetchEnvironmental(new ConvertingList<DeityPower,Ability>(blessings,new Converter<DeityPower,Ability>()
		{
			@Override
			public Ability convert(DeityPower obj)
			{
				return obj.power;
			}
		}),ID,false);
	}

	protected void parseTriggers(List<DeityTrigger> putHere, String trigger)
	{
		putHere.clear();
		trigger=trigger.toUpperCase().trim();
		RitualConnector previousConnector=RitualConnector.AND;
		if(trigger.equals("-"))
			return;
		
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
					DeityTrigger DT=new DeityTrigger();
					RitualTrigger T = (RitualTrigger)CMath.s_valueOf(RitualTrigger.class, cmd);
					if(T==null)
					{
						for(RitualTrigger RT : RitualTrigger.values())
						{
							if(RT.getShortName().equals(cmd))
							{
								T=RT;
								break;
							}
						}
					}
					if(T==null)
					{
						for(RitualTrigger RT : RitualTrigger.values())
						{
							if(RT.name().startsWith(cmd))
							{
								T=RT;
								break;
							}
						}
					}
					DT.previousConnect=previousConnector;
					if(T==null)
					{
						Log.errOut("StdDeity",Name()+"- Illegal trigger: '"+cmd+"','"+trig+"'");
						break;
					}
					else
					switch(T)
					{
					case SAY:
					{
						DT.triggerCode=T;
						DT.parm1=CMParms.combine(V,1);
					}
					break;
					case TIME:
					{
						DT.triggerCode=T;
						DT.parm1=""+CMath.s_int(CMParms.combine(V,1));
					}
					break;
					case WAIT:
					{
						DT.triggerCode=T;
						DT.parm1=""+CMath.s_int(CMParms.combine(V,1));
					}
					break;
					case YOUSAY:
					{
						DT.triggerCode=T;
						DT.parm1=CMParms.combine(V,1);
					}
					break;
					case OTHERSAY:
					{
						DT.triggerCode=T;
						DT.parm1=CMParms.combine(V,1);
					}
					break;
					case ALLSAY:
					{
						DT.triggerCode=T;
						DT.parm1=CMParms.combine(V,1);
					}
					break;
					case PUTTHING:
					{
						DT.triggerCode=T;
						if(V.size()<3)
						{
							Log.errOut("StdDeity",Name()+"- Illegal trigger: "+trig);
							DT=null;
							break;
						}
						DT.parm1=CMParms.combine(V,1,V.size()-2);
						DT.parm2=V.lastElement();
					}
					break;
					case BURNTHING:
					{
						DT.triggerCode=T;
						DT.parm1=CMParms.combine(V,1);
					}
					break;
					case PUTVALUE:
					{
						DT.triggerCode=T;
						if(V.size()<3)
						{
							Log.errOut("StdDeity",Name()+"- Illegal trigger: "+trig);
							DT=null;
							break;
						}
						DT.parm1=""+CMath.s_int(V.elementAt(1));
						DT.parm2=CMParms.combine(V,2);
					}
					break;
					case BURNVALUE:
					{
						DT.triggerCode=T;
						if(V.size()<3)
						{
							Log.errOut("StdDeity",Name()+"- Illegal trigger: "+trig);
							DT=null;
							break;
						}
						DT.parm1=""+CMath.s_int(CMParms.combine(V,1));
					}
					break;
					case BURNMATERIAL:
					{
						DT.triggerCode=T;
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
							DT=null;
							break;
						}
					}
					break;
					case PUTMATERIAL:
					{
						DT.triggerCode=T;
						if(V.size()<3)
						{
							Log.errOut("StdDeity",Name()+"- Illegal trigger: "+trig);
							DT=null;
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
							DT=null;
							break;
						}
					}
					break;
					case EAT:
					{
						DT.triggerCode=T;
						DT.parm1=CMParms.combine(V,1);
					}
					break;
					case READING:
					{
						DT.triggerCode=T;
						DT.parm1=CMParms.combine(V,1);
					}
					break;
					case RANDOM:
					{
						DT.triggerCode=T;
						DT.parm1=CMParms.combine(V,1);
					}
					break;
					case CHECK:
					{
						DT.triggerCode=T;
						DT.parm1=CMParms.combine(V,1);
					}
					break;
					case DRINK:
					{
						DT.triggerCode=T;
						DT.parm1=CMParms.combine(V,1);
					}
					break;
					case INROOM:
					{
						DT.triggerCode=T;
						DT.parm1=CMParms.combine(V,1);
					}
					break;
					case RIDING:
					{
						DT.triggerCode=T;
						DT.parm1=CMParms.combine(V,1);
					}
					break;
					case CAST:
					{
						DT.triggerCode=T;
						DT.parm1=CMParms.combine(V,1);
						if(CMClass.findAbility(DT.parm1)==null)
						{
							Log.errOut("StdDeity",Name()+"- Illegal SPELL in: "+trig);
							DT=null;
							break;
						}
					}
					break;
					case EMOTE:
					{
						DT.triggerCode=T;
						DT.parm1=CMParms.combine(V,1);
					}
					break;
					case SITTING:
					{
						DT.triggerCode=T;
					}
					break;
					case STANDING:
					{
						DT.triggerCode=T;
					}
					break;
					case SLEEPING:
					{
						DT.triggerCode=T;
					}
					break;
					default:
					{
						Log.errOut("StdDeity",Name()+"- Illegal trigger: '"+cmd+"','"+trig+"'");
						DT=null;
						break;
					}
					}
					if(DT==null)
						break;
					putHere.add(DT);
				}
				else
				{
					Log.errOut("StdDeity",Name()+"- Illegal trigger (need more parameters): "+trig);
					break;
				}
			}
			if(div==div1)
				previousConnector=RitualConnector.AND;
			else
				previousConnector=RitualConnector.OR;
		}
	}

	protected static class DeityTrigger
	{
		public RitualTrigger triggerCode=RitualTrigger.SAY;
		public RitualConnector previousConnect=RitualConnector.AND;
		public String parm1=null;
		public String parm2=null;
	}

	/** Manipulation of curse objects, which includes spells, traits, skills, etc.*/
	@Override
	public void addCurse(Ability to, boolean clericOnly)
	{
		if(to==null)
			return;
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
		if((curses.size()==0)||(to==null))
			return;
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
		catch(final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
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
			@Override
			public Ability convert(DeityPower obj)
			{
				return obj.power;
			}
		}),ID,false);
	}

	@Override
	public boolean fetchCurseCleric(int index)
	{
		try
		{
			return curses.get(index).clericOnly;
		}
		catch(final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
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
			return L("The curses of @x1 are placed upon @x2 clerics whenever the cleric does the following: @x3.",name(),charStats().hisher(),getTriggerDesc(clericCurseTriggers));
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
			return L("The curses of @x1 are placed upon @x2 worshippers whenever the worshipper does the following: @x3.",name(),charStats().hisher(),getTriggerDesc(worshipCurseTriggers));
		return "";
	}

	/** Manipulation of granted clerical powers, which includes spells, traits, skills, etc.*/
	/** Make sure that none of these can really be qualified for by the cleric!*/
	/** Manipulation of curse objects, which includes spells, traits, skills, etc.*/
	@Override
	public void addPower(Ability to)
	{
		if(to==null)
			return;
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
		catch(final java.lang.ArrayIndexOutOfBoundsException x)
		{
		}
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
			return L("Special powers of @x1 are placed upon @x2 clerics whenever the cleric does the following: @x3.",name(),charStats().hisher(),getTriggerDesc(clericPowerTriggers));
		return "";
	}
}
