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
   Copyright 2001-2020 Bo Zimmerman

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
	protected Map<String, boolean[]>trigBlessingParts	= new SHashtable<String, boolean[]>();
	protected Map<String, Long>		trigBlessingTimes	= new SHashtable<String, Long>();
	protected Map<String, boolean[]>trigPowerParts		= new SHashtable<String, boolean[]>();
	protected Map<String, Long>		trigPowerTimes		= new SHashtable<String, Long>();
	protected Map<String, boolean[]>trigCurseParts		= new SHashtable<String, boolean[]>();
	protected Map<String, Long>		trigCurseTimes		= new SHashtable<String, Long>();
	protected Map<String, boolean[]>trigServiceParts	= new SHashtable<String, boolean[]>();
	protected Map<String, Long>		trigServiceTimes	= new SHashtable<String, Long>();
	protected List<WorshipService>	services			= new SVector<WorshipService>();
	protected List<MOB>				waitingFor			= new SLinkedList<MOB>();
	protected Set<Places>			holyPlaces			= new TreeSet<Places>(Places.placeComparator);

	protected Set<Integer>			neverTriggers		= new XHashSet<Integer>(new Integer[] {
		Integer.valueOf(CMMsg.TYP_ENTER),
		Integer.valueOf(CMMsg.TYP_LEAVE),
		Integer.valueOf(CMMsg.TYP_LOOK)
	});

	protected final Map<String,int[]> areaPiety	= new TreeMap<String,int[]>();

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
		public DeityPower(final Ability A, final boolean clericsOnly)
		{
			power=A;
			clericOnly=clericsOnly;
		}
	}

	@Override
	protected void cloneFix(final MOB E)
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
	public void setClericRequirements(final String reqs)
	{
		clericReqs=reqs;
	}

	@Override
	public String getWorshipRequirements()
	{
		return worshipReqs;
	}

	@Override
	public void setWorshipRequirements(final String reqs)
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
	public void setClericRitual(final String ritual)
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
	public void setWorshipRitual(final String ritual)
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
			ritual="SAY Bless us "+name()+"&wait 10&wait 10&ALLSAY Amen.&SAY Go in peace";
		serviceRitual=ritual;
		parseTriggers(serviceTriggers,ritual);
	}

	public String getTriggerDesc(final List<DeityTrigger> V)
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
			{
				if((msg.source().baseCharStats().getMyDeity()==this)
				||(msg.source().charStats().getMyDeity()==this))
				{
					msg.source().tell(L("You already worship @x1.",name()));
					if(msg.source().isMonster())
						CMLib.commands().postSay(msg.source(),null,L("I already worship @x1.",msg.source().charStats().getWorshipCharID()));
					return false;
				}
				if((msg.source().baseCharStats().getWorshipCharID().length()>0)
				||(msg.source().charStats().getWorshipCharID().length()>0))
				{
					msg.source().tell(L("You already worship @x1.",msg.source().charStats().getWorshipCharID()));
					if(msg.source().isMonster())
						CMLib.commands().postSay(msg.source(),null,L("I already worship @x1.",msg.source().charStats().getWorshipCharID()));
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
			}
			break;
		case CMMsg.TYP_REBUKE:
			if((!msg.source().charStats().getWorshipCharID().equals(Name()))
			&&(!msg.source().baseCharStats().getWorshipCharID().equals(Name())))
			{
				msg.source().tell(L("You do not worship @x1.",name()));
				return false;
			}
			break;
		}
		return true;
	}

	public synchronized void bestowBlessing(final MOB mob, final Ability blesingA)
	{
		final Room prevRoom;
		synchronized(this)
		{
			prevRoom=location();
		}
		final Room targetRoom;
		synchronized(mob)
		{
			targetRoom=mob.location();
		}
		targetRoom.bringMobHere(this,false);
		if(blesingA!=null)
		{
			final Vector<String> V=new Vector<String>();
			if(blesingA.canTarget(Ability.CAN_MOBS))
			{
				V.addElement(mob.name()+"$");
				blesingA.invoke(this,V,mob,true,mob.phyStats().level());
			}
			else
			if(blesingA.canTarget(Ability.CAN_ITEMS))
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
				blesingA.invoke(this,V,I,true,mob.phyStats().level());
				delItem(I);
				if(!mob.isMine(I))
					mob.addItem(I);
			}
			else
				blesingA.invoke(this,mob,true,mob.phyStats().level());
		}
		prevRoom.bringMobHere(this,false);
		if((prevRoom != location())
		||(!prevRoom.isInhabitant(this))
		||(targetRoom.isInhabitant(this)))
		{
			this.setLocation(prevRoom);
			prevRoom.addInhabitant(this);
			targetRoom.delInhabitant(this);
		}
		if(targetRoom!=prevRoom)
		{
			if(mob.getVictim()==this)
				mob.makePeace(true);
			if(getVictim()==mob)
				makePeace(true);
		}
	}

	public synchronized void bestowPower(final MOB mob, Ability Power)
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

	public synchronized void bestowCurse(final MOB mob, final Ability Curse)
	{
		final Room prevRoom;
		synchronized(this)
		{
			prevRoom=location();
		}
		final Room targetRoom;
		synchronized(mob)
		{
			targetRoom=mob.location();
		}
		targetRoom.bringMobHere(this,false);
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
		if((prevRoom != location())
		||(!prevRoom.isInhabitant(this))
		||(targetRoom.isInhabitant(this)))
		{
			this.setLocation(prevRoom);
			prevRoom.addInhabitant(this);
			targetRoom.delInhabitant(this);
		}
		if(mob.location()!=prevRoom)
		{
			if(mob.getVictim()==this)
				mob.makePeace(true);
			if(getVictim()==mob)
				makePeace(true);
		}
	}

	public synchronized void bestowBlessings(final MOB mob)
	{
		norecurse=true;
		try
		{
			final Room R=mob.location();
			if((!alreadyBlessed(mob))
			&&(numBlessings()>0)
			&&(R!=null))
			{
				final CMMsg eventMsg=CMClass.getMsg(this, mob, null,
						CMMsg.MSG_HOLYEVENT, null,
						CMMsg.MSG_HOLYEVENT, null,
						CMMsg.NO_EFFECT, HolyEvent.BLESSING.toString());
				R.send(this, eventMsg);
				R.show(this,mob,CMMsg.MSG_OK_VISUAL,L("You feel the presence of <S-NAME> in <T-NAME>."));
				if((mob.charStats().getStat(CharStats.STAT_FAITH)>=100)
				||(mob.isPlayer() && mob.isAttributeSet(Attrib.SYSOPMSGS)))
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

	public synchronized void bestowPowers(final MOB mob)
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

	public synchronized void bestowCurses(final MOB mob)
	{
		norecurse=true;
		try
		{
			final Room R=mob.location();
			if((numCurses()>0)
			&&(R!=null))
			{
				final CMMsg eventMsg=CMClass.getMsg(this, mob, null,
						CMMsg.MSG_HOLYEVENT, null,
						CMMsg.MSG_HOLYEVENT, null,
						CMMsg.NO_EFFECT, HolyEvent.CURSING.toString());
				R.send(this, eventMsg);
				R.show(this,mob,CMMsg.MSG_OK_VISUAL,L("You feel the wrath of <S-NAME> in <T-NAME>."));
				if((mob.charStats().getStat(CharStats.STAT_FAITH)>=100)
				||(mob.isPlayer() && mob.isAttributeSet(Attrib.SYSOPMSGS)))
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

	public void removeBlessings(final MOB mob)
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

	public void removePowers(final MOB mob)
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

	public boolean alreadyBlessed(final MOB mob)
	{
		for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A.invoker()==this))
				return true;
		}
		return false;
	}

	public boolean alreadyPowered(final MOB mob)
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

	public CMMsg generateNextTrigger(final MOB mob, final List<DeityTrigger> svcTriggsV, final Map<String, boolean[]> trigParts, final Map<String, Long> trigTimes)
	{
		final boolean[] checks=trigParts.get(mob.Name());
		for(int v=0;v<svcTriggsV.size();v++)
		{
			if((checks!=null)&&(checks.length>=v)&&(checks[v]))
				continue;
			final DeityTrigger DT=svcTriggsV.get(v);
			switch(DT.triggerCode)
			{
			case SAY:
				return CMClass.getMsg(mob, DT.triggerCode.getCMMsgCode(), L("^T<S-NAME> say(s) '@x1'.^N",DT.parm1));
			case TIME:
				if(checks != null)
					checks[v]=true;
				return null;
			case RANDOM:
				if(checks != null)
					checks[v]=true;
				return null;
			case YOUSAY:
				return null;
			case ALLSAY:
				return null;
			case OTHERSAY:
				return null;
			case WAIT:
			{
				if((checks!=null)
				&&(checks[v-1])
				&&(trigTimes.get(mob.Name())!=null))
				{
					boolean proceed=true;
					for(int t=v+1;t<checks.length;t++)
					{
						if(checks[t])
							proceed=false;
					}
					if(proceed)
					{
						final long waitDuration=CMath.s_long(DT.parm1)*CMProps.getTickMillis();
						if(System.currentTimeMillis()>(trigTimes.get(mob.Name()).longValue()+waitDuration))
							return CMClass.getMsg(mob, CMMsg.MSG_OK_ACTION, null); // force the wait to be evaluated
					}
				}
				return null;
			}
			case CHECK:
				if(checks != null)
					checks[v]=true;
				return null;
			case PUTTHING:
			{
				final Item I=CMClass.getBasicItem("GenItem");
				final Item cI=CMClass.getBasicItem("GenContainer");
				I.setName(DT.parm1);
				cI.setName(DT.parm2);
				return CMClass.getMsg(mob, cI, I, DT.triggerCode.getCMMsgCode(), L("<S-NAME> put(s) <O-NAME> into <T-NAME>."));
			}
			case BURNTHING:
			{
				final Item I=CMClass.getBasicItem("GenItem");
				if(DT.parm1.equals("0"))
					I.setName(L("Something"));
				else
					I.setName(DT.parm1);
				return CMClass.getMsg(mob, I, null, DT.triggerCode.getCMMsgCode(), L("<S-NAME> burn(s) <T-NAME>."));
			}
			case READING:
			{
				final Item I=CMClass.getBasicItem("GenItem");
				if(DT.parm1.equals("0"))
					I.setName(L("Something"));
				else
					I.setName(DT.parm1);
				return CMClass.getMsg(mob, I, null, DT.triggerCode.getCMMsgCode(), L("<S-NAME> read(s) <T-NAME>."));
			}
			case DRINK:
			{
				final Item I=CMClass.getBasicItem("GenItem");
				if(DT.parm1.equals("0"))
					I.setName(L("Something"));
				else
					I.setName(DT.parm1);
				return CMClass.getMsg(mob, I, null, DT.triggerCode.getCMMsgCode(), L("<S-NAME> drink(s) <T-NAME>."));
			}
			case EAT:
			{
				final Item I=CMClass.getBasicItem("GenItem");
				if(DT.parm1.equals("0"))
					I.setName(L("Something"));
				else
					I.setName(DT.parm1);
				return CMClass.getMsg(mob, I, null, DT.triggerCode.getCMMsgCode(), L("<S-NAME> eat(s) <T-NAME>."));
			}
			case INROOM:
				if(checks != null)
					checks[v]=true;
				return null;
			case RIDING:
				if(checks != null)
					checks[v]=true;
				return null;
			case CAST:
			{
				final Ability A=CMClass.getAbility(DT.parm1);
				if(A!=null)
					return CMClass.getMsg(mob, null, A, DT.triggerCode.getCMMsgCode(), L("<S-NAME> do(es) '@x1'",A.name()));
				return null;
			}
			case EMOTE:
				return CMClass.getMsg(mob, null, null, DT.triggerCode.getCMMsgCode(), L("<S-NAME> do(es) '@x1'",DT.parm1));
			case PUTVALUE:
			{
				final Item cI=CMClass.getBasicItem("GenContainer");
				if(DT.parm2.equals("0"))
					cI.setName(L("Something"));
				else
					cI.setName(DT.parm2);
				final Item I=CMClass.getBasicItem("GenItem");
				I.setName(L("valuables"));
				I.setBaseValue(CMath.s_int(DT.parm1));
				return CMClass.getMsg(mob, cI, I, DT.triggerCode.getCMMsgCode(), L("<S-NAME> put(s) <O-NAME> in <T-NAME>."));
			}
			case PUTMATERIAL:
			case BURNMATERIAL:
			{
				final Item cI=CMClass.getBasicItem("GenContainer");
				if(DT.parm2.equals("0"))
					cI.setName(L("Something"));
				else
					cI.setName(DT.parm2);
				final Item I=CMLib.materials().makeItemResource(CMath.s_int(DT.parm1));
				return CMClass.getMsg(mob, cI, I, DT.triggerCode.getCMMsgCode(), L("<S-NAME> put(s) <O-NAME> in <T-NAME>."));
			}
			case BURNVALUE:
			{
				final Item I=CMClass.getBasicItem("GenItem");
				I.setName(L("valuables"));
				I.setBaseValue(CMath.s_int(DT.parm1));
				return CMClass.getMsg(mob, I, null, DT.triggerCode.getCMMsgCode(), L("<S-NAME> burn(s) <T-NAME>."));
			}
			case SITTING:
				if(!CMLib.flags().isSitting(mob))
					return CMClass.getMsg(mob, CMMsg.MSG_SIT, L("<S-NAME> sit(s)."));
				return null;
			case STANDING:
				if(!CMLib.flags().isStanding(mob))
					return CMClass.getMsg(mob, CMMsg.MSG_STAND, L("<S-NAME> stand(s)."));
				return null;
			case SLEEPING:
				if(!CMLib.flags().isSleeping(mob))
					return CMClass.getMsg(mob, CMMsg.MSG_STAND, L("<S-NAME> sleep(s)."));
				return null;
			}
		}
		return null;
	}

	public boolean triggerCheck(final CMMsg msg, final List<DeityTrigger> trigsV, final Map<String, boolean[]> trigParts, final Map<String, Long> trigTimes)
	{
		boolean recheck=false;
		for(int v=0;v<trigsV.size();v++)
		{
			boolean yup=false;
			final DeityTrigger DT=trigsV.get(v);
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
									yup=true;
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
									yup=true;
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
								final long waitDuration=CMath.s_long(DT.parm1)*CMProps.getTickMillis();
								if(System.currentTimeMillis()>(trigTimes.get(msg.source().Name()).longValue()+waitDuration))
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
									return recheck;
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
							yup=Name().equalsIgnoreCase(CMLib.law().getClericInfused(msg.source().location()));
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
					if((checks==null)||(checks.length!=trigsV.size()))
					{
						checks=new boolean[trigsV.size()];
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
				{
					msg.source().baseCharStats().setWorshipCharID(name());
					msg.source().recoverCharStats();
					final Room startRoom = msg.source().getStartRoom();
					final Area startArea = (startRoom!=null)?startRoom.getArea():null;
					if((startArea != null)
					&&(!CMath.bset(startArea.flags(), Area.FLAG_INSTANCE_CHILD)))
					{
						if(!areaPiety.containsKey(startArea.Name()))
							areaPiety.put(startArea.Name(), new int[1]);
						areaPiety.get(startArea.Name())[0]++;
					}
				}
				break;
			case CMMsg.TYP_REBUKE:
				if(msg.source().baseCharStats().getWorshipCharID().equals(Name())
				||msg.source().charStats().getWorshipCharID().equals(Name()))
				{
					if(msg.source().baseCharStats().getWorshipCharID().equals(Name()))
					{
						msg.source().baseCharStats().setWorshipCharID("");
						msg.source().recoverCharStats();
						final Room startRoom = msg.source().getStartRoom();
						final Area startArea = (startRoom!=null)?startRoom.getArea():null;
						if((startArea != null)
						&&(!CMath.bset(startArea.flags(), Area.FLAG_INSTANCE_CHILD))
						&&(areaPiety.containsKey(startArea.Name()))
						&&(areaPiety.get(startArea.Name())[0]>0))
							areaPiety.get(startArea.Name())[0]--;
					}
					removeBlessings(msg.source());
					if(msg.source().charStats().getStat(CharStats.STAT_FAITH)>=100)
					{
						removePowers(msg.source());
						msg.source().tell(L("You feel the wrath of @x1!",name()));
						if(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
							msg.source().charStats().getCurrentClass().unLevel(msg.source());
					}
					else
					{
						xpwrath=-CMLib.leveler().postExperience(msg.source(),null,null,-xpwrath,false);
						msg.source().tell(L("@x1 takes @x2 of experience from you.",name(),""+xpwrath));
					}
					final Ability doubtA=CMClass.getAbility("Prayer_ReligiousDoubt");
					if((doubtA!=null)&&(msg.source().fetchEffect(doubtA.ID())==null))
						doubtA.startTickDown(this, msg.source(), (int)CMProps.getTicksPerDay());
				}
				break;
			case CMMsg.TYP_HOLYEVENT:
				if((msg.targetMajor(CMMsg.MASK_ALWAYS))
				&&(msg.source()==myHost)
				&&(msg.othersMessage()!=null))
				{
					final Deity.HolyEvent event=(Deity.HolyEvent)CMath.s_valueOf(Deity.HolyEvent.class, msg.othersMessage().toUpperCase().trim());
					if(event == null)
						break;
					switch(event)
					{
					case SERVICE:
						{
							final List<DeityTrigger> svcTriggsV=serviceTriggers;
							if((svcTriggsV!=null)&&(svcTriggsV.size()>0))
							{
								final CMMsg msg2=this.generateNextTrigger(msg.source(), svcTriggsV, trigServiceParts, trigServiceTimes);
								if(msg2 != null)
									msg.addTrailerMsg(msg2);
							}
							break;
						}
					case SERVICE_CANCEL:
						{
							final WorshipService service=findService(msg.source(),null);
							this.cancelService(service);
							break;
						}
					case CURSING:
					{
						msg.addTrailerRunnable(new Runnable()
						{
							final MOB M=msg.source();
							@Override
							public void run()
							{
								bestowCurses(M);
							}
						});
						break;
					}
					default:
						break;
					}
					break;
				}
			}
		}
		else
		if(msg.source()==myHost) // since deities might see a message through many many eyes
		{
			if((!neverTriggers.contains(Integer.valueOf(msg.sourceMinor())))
			&&((name().equals(msg.source().baseCharStats().getWorshipCharID()))
				||((msg.source().charStats().getStat(CharStats.STAT_FAITH)>=1000)
					&&(Name().equals(msg.source().charStats().getWorshipCharID())))))
			{
				if((msg.sourceMinor()==CMMsg.TYP_DEATH)
				&&(msg.source().isMonster())
				&&(msg.source().baseCharStats().getWorshipCharID().equals(Name()))
				&&(msg.source().basePhyStats().rejuv()>0)
				&&(msg.source().basePhyStats().rejuv() != PhyStats.NO_REJUV)
				&&(msg.source().getStartRoom()!=null))
				{
					final Room startRoom = msg.source().getStartRoom();
					final Area startArea = (startRoom!=null)?startRoom.getArea():null;
					if((startArea != null)
					&&(!CMath.bset(startArea.flags(), Area.FLAG_INSTANCE_CHILD))
					&&(areaPiety.containsKey(startArea.Name()))
					&&(areaPiety.get(startArea.Name())[0]>0))
						areaPiety.get(startArea.Name())[0]--;
				}

				if(numBlessings()>0)
				{
					List<DeityTrigger> triggsV=worshipTriggers;
					if(msg.source().charStats().getStat(CharStats.STAT_FAITH)>=100)
						triggsV=clericTriggers;
					if((triggsV!=null)&&(triggsV.size()>0))
					{
						final boolean recheck=triggerCheck(msg,triggsV,trigBlessingParts,trigBlessingTimes);

						if((recheck)&&(!norecurse)&&(!alreadyBlessed(msg.source())))
						{
							final boolean[] checks=trigBlessingParts.get(msg.source().Name());
							if((checks!=null)&&(checks.length==triggsV.size())&&(checks.length>0))
							{
								boolean rollingTruth=checks[0];
								for(int v=1;v<triggsV.size();v++)
								{
									final DeityTrigger DT=triggsV.get(v);
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
					List<DeityTrigger> triggsV=worshipCurseTriggers;
					if(msg.source().charStats().getStat(CharStats.STAT_FAITH)>=100)
						triggsV=clericCurseTriggers;
					if((triggsV!=null)&&(triggsV.size()>0))
					{
						final boolean recheck=triggerCheck(msg,triggsV,trigCurseParts,trigCurseTimes);
						if((recheck)&&(!norecurse))
						{
							final boolean[] checks=trigCurseParts.get(msg.source().Name());
							if((checks!=null)&&(checks.length==triggsV.size())&&(checks.length>0))
							{
								boolean rollingTruth=checks[0];
								for(int v=1;v<triggsV.size();v++)
								{
									final DeityTrigger DT=triggsV.get(v);
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
				&&((msg.source().charStats().getStat(CharStats.STAT_FAITH)>=100)
					||(msg.source().isPlayer() && msg.source().isAttributeSet(Attrib.SYSOPMSGS))))
				{
					final List<DeityTrigger> triggsV=clericPowerTriggers;
					if((triggsV!=null)&&(triggsV.size()>0))
					{
						final boolean recheck=triggerCheck(msg,triggsV,trigPowerParts,trigPowerTimes);

						if((recheck)&&(!norecurse)&&(!alreadyPowered(msg.source())))
						{
							final boolean[] checks=trigPowerParts.get(msg.source().Name());
							if((checks!=null)&&(checks.length==triggsV.size())&&(checks.length>0))
							{
								boolean rollingTruth=checks[0];
								for(int v=1;v<triggsV.size();v++)
								{
									final DeityTrigger DT=triggsV.get(v);
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

				if(((msg.source().charStats().getStat(CharStats.STAT_FAITH)>=100)
					||(msg.source().isPlayer() && msg.source().isAttributeSet(Attrib.SYSOPMSGS)))
				&&((Name().equalsIgnoreCase(CMLib.law().getClericInfused(msg.source().location())))
					||((msg.source().charStats().getStat(CharStats.STAT_FAITH)>=1000)
						&&(Name().equals(msg.source().charStats().getWorshipCharID())))))
				{
					final List<DeityTrigger> trigsV=serviceTriggers;
					if((trigsV!=null)&&(trigsV.size()>0))
					{
						final boolean recheck=triggerCheck(msg,trigsV,trigServiceParts,trigServiceTimes);
						if((recheck)
						&&(!norecurse)
						&&(!alreadyServiced(msg.source(),msg.source().location())))
						{
							final boolean[] checks=trigServiceParts.get(msg.source().Name());
							if((checks!=null)&&(checks.length==trigsV.size())&&(checks.length>0))
							{
								boolean rollingTruth=checks[0];
								for(int v=1;v<trigsV.size();v++)
								{
									final DeityTrigger DT=trigsV.get(v);
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
	}

	@Override
	public void registerHolyPlace(final Places newOne)
	{
		if(newOne != null)
		{
			synchronized(holyPlaces)
			{
				if (!holyPlaces.contains(newOne))
					holyPlaces.add(newOne);
			}
		}
	}

	@Override
	public void deregisterHolyPlace(final Places newOne)
	{
		if(newOne != null)
		{
			synchronized(holyPlaces)
			{
				holyPlaces.remove(newOne);
			}
		}
	}

	@Override
	public Enumeration<Places> holyPlaces()
	{
		final ArrayList<Places> placesCopy=new ArrayList<Places>(holyPlaces.size());
		synchronized(holyPlaces)
		{
			for(final Iterator<Places> i=holyPlaces.iterator();i.hasNext();)
			{
				final Places place = i.next();
				if(place.amDestroyed())
					i.remove();
				else
					placesCopy.add(place);
			}
		}
		return new IteratorEnumeration<Places>(placesCopy.iterator());
	}

	protected void startServiceIfNecessary(final MOB mob, final Room room)
	{
		if((mob==null)||(room==null))
			return;
		final List<MOB> parishaners=new ArrayList<MOB>();
		synchronized(services)
		{
			for(final WorshipService w : services)
			{
				if(w.room==room)
					return;
			}
			final CMMsg msg=CMClass.getMsg(this,mob,null,
					CMMsg.MSG_HOLYEVENT, L("<T-NAME> begin(s) to hold services for @x1 here.",mob.Name()),
					CMMsg.MSG_HOLYEVENT,null,
					CMMsg.MSG_HOLYEVENT,HolyEvent.SERVICE_BEGIN.toString());
			if(!room.okMessage(this, msg))
				return;
			room.send(this, msg);
			final WorshipService service = new WorshipService();
			service.room=room;
			service.parishaners = parishaners;
			service.startTime = System.currentTimeMillis();
			service.cleric = mob;
			service.serviceCompleted = false;
			services.add(service);
			final Deity.DeityWorshipper A=CMLib.law().getClericInfusion(room);
			if(A instanceof Ability)
				((Ability)A).setAbilityCode(1);
		}
		Room R=null;
		MOB M=null;
		final int maxMobs = 5+(mob.phyStats().level()/2);
		for(final Enumeration<Room> r=CMLib.tracking().getRadiantRoomsEnum(room, null, null, 5+(mob.phyStats().level()/5), null);r.hasMoreElements();)
		{
			R=r.nextElement();
			if(!Name().equalsIgnoreCase(CMLib.law().getClericInfused(R)))
			{
				for(int m=0;m<R.numInhabitants();m++)
				{
					M=R.fetchInhabitant(m);
					if((M==null)||(M==mob)||(CMLib.flags().isAnimalIntelligence(M)))
						continue;
					if(M.charStats().getWorshipCharID().equals(Name()))
					{
						if(!M.isMonster())
							M.tell(L("Services for @x1 are now starting at @x2.",Name(),room.displayText(null)));
						else
						if((!CMLib.flags().isATrackingMonster(M))
						&&(!M.isInCombat())
						&&(CMLib.flags().isAliveAwakeMobileUnbound(M, true)))
						{
							final Ability TRACKA=CMClass.getAbility("Skill_Track");
							if(TRACKA!=null)
							{
								TRACKA.invoke(M,CMParms.parse("\""+CMLib.map().getExtendedRoomID(room)+"\""),room,true,0);
								parishaners.add(M);
								if(parishaners.size()>maxMobs)
									break;
							}
						}
					}
				}
				if(parishaners.size()>maxMobs)
					break;
			}
		}
	}

	protected void undoService(final List<MOB> V)
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

	protected boolean alreadyServiced(final MOB mob, final Room room)
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
					final Deity.DeityWorshipper A=CMLib.law().getClericInfusion(service.room);
					if(A instanceof Ability)
						((Ability)A).setAbilityCode(0);
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

	public WorshipService findService(final MOB mob, final Room room)
	{
		WorshipService service=null;
		synchronized(services)
		{
			for(final WorshipService s : services)
			{
				if((s.room==room) && (s.cleric == mob))
					service = s;
			}
			if(service == null)
			{
				if(room != null)
				{
					for(final WorshipService s : services)
					{
						if(s.room==room)
							service = s;
					}
				}
				else
				{
					for(final WorshipService s : services)
					{
						if(s.cleric==mob)
							service = s;
					}
				}
			}
		}
		return service;
	}

	public boolean finishService(final MOB mob, final Room room)
	{
		if((mob==null)||(room==null))
			return false;
		MOB M=null;
		int totalLevels=0;
		final WorshipService service = findService(mob,room);
		if(service == null)
			return false;
		if(service.parishaners.size()==0)
		{
			return this.cancelService(service);
		}
		final CMMsg eventMsg=CMClass.getMsg(this, null, null,
				CMMsg.MSG_HOLYEVENT, null,
				CMMsg.MSG_HOLYEVENT, null,
				CMMsg.NO_EFFECT, HolyEvent.SERVICE.toString());
		eventMsg.setValue(service.parishaners.size());
		service.serviceCompleted = true;
		for(int m=0;m<room.numInhabitants();m++)
		{
			M=room.fetchInhabitant(m);
			if(M==null)
				continue;
			eventMsg.setTarget(M);
			if(M.charStats().getWorshipCharID().equals(mob.baseCharStats().getWorshipCharID()))
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
			room.send(this, eventMsg);
		}
		undoService(service.parishaners);
		final int exp=(int)Math.round(CMath.div(totalLevels,mob.phyStats().level())*10.0);
		CMLib.leveler().postExperience(mob,null,null,exp,false);
		trigServiceParts.remove(mob.Name());
		trigServiceTimes.remove(mob.Name());
		return true;
	}

	public boolean cancelService(final WorshipService service)
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
			if(M.charStats().getWorshipCharID().equals(Name()))
			{
				final Ability A=M.fetchEffect("Skill_Convert");
				if(A!=null)
					A.unInvoke();
			}
		}
		final CMMsg msg=CMClass.getMsg(this,mob,null,
				CMMsg.MSG_HOLYEVENT, L("The service conducted by @x1 has been cancelled.",mob.Name()),
				CMMsg.MSG_HOLYEVENT, null,
				CMMsg.MSG_HOLYEVENT, HolyEvent.SERVICE_CANCEL.toString());
		final CMMsg msg2=CMClass.getMsg(this,null,null,
				CMMsg.NO_EFFECT, null,CMMsg.NO_EFFECT,null,CMMsg.MSG_OK_ACTION,L("The service conducted by @x1 has been cancelled.",mob.Name()));
		if(room.okMessage(this, msg)
		&&room.okMessage(this, msg2))
		{
			room.send(this, msg);
			room.send(this, msg2);
		}
		if(mob.location()!=room)
			mob.executeMsg(this, msg);
		undoService(service.parishaners);
		synchronized(services)
		{
			services.remove(service);
			final Deity.DeityWorshipper A=CMLib.law().getClericInfusion(service.room);
			if(A instanceof Ability)
				((Ability)A).setAbilityCode(0);
		}
		trigServiceParts.remove(mob.Name());
		trigServiceTimes.remove(mob.Name());
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		&&((--rebukeCheckDown)<0))
		{
			rebukeCheckDown=10;
			for(final Enumeration<MOB> p=CMLib.players().players();p.hasMoreElements();)
			{
				final MOB M=p.nextElement();
				if((lastBlackmark>0)
				&&(blacklist!=null)
				&&(blacklist!=M)
				&&((System.currentTimeMillis()-lastBlackmark)<120000))
					continue;
				if((!M.isMonster())
				&&(M.charStats().getWorshipCharID().equals(name()))
				&&(CMLib.flags().isInTheGame(M,true)))
				{
					final Room R=M.location();
					if(R==null)
						continue;
					if(M.charStats().getStat(CharStats.STAT_FAITH)>=100)
					{
						if(!CMLib.masking().maskCheck(getClericRequirements(),M,true))
						{
							if((blacklist==M)&&((++blackmarks)>30))
							{
								final CMMsg eventMsg=CMClass.getMsg(this, M, null,
										CMMsg.MSG_HOLYEVENT, null,
										CMMsg.MSG_HOLYEVENT, null,
										CMMsg.NO_EFFECT, HolyEvent.REBUKE.toString());
								R.send(this, eventMsg);
								final CMMsg msg=CMClass.getMsg(M,this,null,CMMsg.MSG_REBUKE,L("<S-NAME> <S-HAS-HAVE> been rebuked by <T-NAME>!!"));
								if(M.okMessage(M,msg))
									R.send(M,msg);
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
								final CMMsg eventMsg=CMClass.getMsg(this, M, null,
										CMMsg.MSG_HOLYEVENT, null,
										CMMsg.MSG_HOLYEVENT, null,
										CMMsg.NO_EFFECT, HolyEvent.DISAPPOINTED.toString());
								eventMsg.setValue(blackmarks);
								R.send(this, eventMsg);
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
							final CMMsg eventMsg=CMClass.getMsg(this, M, null,
									CMMsg.MSG_HOLYEVENT, null,
									CMMsg.MSG_HOLYEVENT, null,
									CMMsg.NO_EFFECT, HolyEvent.REBUKE.toString());
							R.send(this, eventMsg);
							final CMMsg msg=CMClass.getMsg(M,this,null,CMMsg.MSG_REBUKE,L("<S-NAME> <S-HAS-HAVE> been rebuked by <T-NAME>!!"));
							if(M.okMessage(M,msg))
								M.location().send(M,msg);
						}
						else
						{
							if(blacklist!=M)
								blackmarks=0;
							blacklist=M;
							blackmarks++;
							final CMMsg eventMsg=CMClass.getMsg(this, M, null,
									CMMsg.MSG_HOLYEVENT, null,
									CMMsg.MSG_HOLYEVENT, null,
									CMMsg.NO_EFFECT, HolyEvent.DISAPPOINTED.toString());
							eventMsg.setValue(blackmarks);
							R.send(this, eventMsg);
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
					executeMOBList=new ArrayList<MOB>(waitingFor);
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
	public void addBlessing(final Ability to, final boolean clericOnly)
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
	public void delBlessing(final Ability to)
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
	public Ability fetchBlessing(final int index)
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
	public boolean fetchBlessingCleric(final int index)
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
	public boolean fetchBlessingCleric(final String ID)
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
	public Ability fetchBlessing(final String ID)
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
			public Ability convert(final DeityPower obj)
			{
				return obj.power;
			}
		}),ID,false);
	}

	protected void parseTriggers(final List<DeityTrigger> putHere, String trigger)
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
						for(final RitualTrigger RT : RitualTrigger.values())
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
						for(final RitualTrigger RT : RitualTrigger.values())
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
	public void addCurse(final Ability to, final boolean clericOnly)
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
	public void delCurse(final Ability to)
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
	public Ability fetchCurse(final int index)
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
	public Ability fetchCurse(final String ID)
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
			public Ability convert(final DeityPower obj)
			{
				return obj.power;
			}
		}),ID,false);
	}

	@Override
	public boolean fetchCurseCleric(final int index)
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
	public boolean fetchCurseCleric(final String ID)
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
	public void setClericSin(final String ritual)
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
	public void setWorshipSin(final String ritual)
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
	public void addPower(final Ability to)
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
	public void delPower(final Ability to)
	{
		powers.remove(to);
	}

	@Override
	public int numPowers()
	{
		return powers.size();
	}

	@Override
	public Ability fetchPower(final int index)
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
	public Ability fetchPower(final String ID)
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
	public void setClericPowerup(final String ritual)
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

	@Override
	public int getAreaPiety(final String areaName)
	{
		if(areaPiety.containsKey(areaName))
			return areaPiety.get(areaName)[0];
		return 0;
	}
}
