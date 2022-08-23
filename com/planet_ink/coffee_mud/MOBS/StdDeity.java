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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityComponents.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.DatabaseTables;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.*;
import java.util.*;

/*
   Copyright 2001-2022 Bo Zimmerman

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

	protected final PrioritizingLimitedMap<String,int[]> blacklist=new PrioritizingLimitedMap<String,int[]>(20,30*60000L,60*60000L,100);

	protected Triggerer				rituals		= null;
	protected List<WorshipService>	services	= new SVector<WorshipService>();
	protected List<DeityPower>		blessings	= new SVector<DeityPower>();
	protected List<DeityPower>		curses		= new SVector<DeityPower>();
	protected List<Ability>			powers		= new SVector<Ability>();

	protected final Set<Integer>	neverTriggers		= new XHashSet<Integer>(new Integer[] {
		Integer.valueOf(CMMsg.TYP_ENTER),
		Integer.valueOf(CMMsg.TYP_LEAVE),
		Integer.valueOf(CMMsg.TYP_LOOK)
	});

	public StdDeity()
	{
		super();
		_name="a Mighty Deity";
		setDescription("He is Mighty.");
		setDisplayText("A Mighty Deity stands here!");
		rituals = ((Triggerer)CMClass.getCommon("DefaultTriggerer")).setName(_name);
		basePhyStats().setWeight(700);
		basePhyStats().setAbility(200);
		basePhyStats().setArmor(0);
		basePhyStats().setAttackAdjustment(1000);
		basePhyStats().setDamage(1000);
		baseCharStats().setMyRace(CMClass.getRace("Spirit"));
		recoverPhyStats();
	}

	@Override
	public CMObject newInstance()
	{
		final StdDeity deity = (StdDeity)super.newInstance();
		deity.rituals.setName(rituals.name());
		return deity;
	}

	private static class WorshipService
	{
		public MOB			cleric				= null;
		public Room			room				= null;
		public boolean		serviceCompleted	= false;
		public long			startTime			= System.currentTimeMillis();
		public List<MOB>	parishaners			= new Vector<MOB>();
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
			rituals	= ((Triggerer)((StdDeity)E).rituals.copyOf()).setName(Name());
			blessings=new XVector<DeityPower>(((StdDeity)E).blessings);
			curses=new XVector<DeityPower>(((StdDeity)E).curses);
			powers=new XVector<Ability>(((StdDeity)E).powers);
			services = new SVector<WorshipService>();
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

	protected void setRitual(final RitualType type, final String ritual)
	{
		final List<String> errors = new ArrayList<String>(1);
		rituals.addTrigger(type, ritual, errors);
		if(errors.size()>0)
		{
			for(final String error : errors)
				Log.errOut(name(),error);
		}
	}

	@Override
	public void setClericRitual(final String ritual)
	{
		clericRitual=ritual;
		setRitual(RitualType.CLERIC_BLESSING, ritual);
	}

	@Override
	public void setName(final String name)
	{
		super.setName(name);
		rituals.setName(name);
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
		setRitual(RitualType.WORSHIP_BLESSING, ritual);
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
		setRitual(RitualType.SERVICE, ritual);
	}


	@Override
	public String getClericRequirementsDesc()
	{
		return L("The following may be priests of @x1: @x2",name(),CMLib.masking().maskDesc(getClericRequirements()));
	}

	@Override
	public String getClericTriggerDesc()
	{
		if(numBlessings()>0)
		{
			return L("The blessings of @x1 are placed upon @x2 clerics whenever the cleric does the following: @x3.",
					name(),charStats().hisher(),rituals.getTriggerDesc(RitualType.CLERIC_BLESSING));
		}
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
		{
			return L("The blessings of @x1 are placed upon @x2 worshippers whenever they do the following: @x3.",
					name(), charStats().hisher(), rituals.getTriggerDesc(RitualType.WORSHIP_BLESSING));
		}
		return "";
	}

	@Override
	public String getServiceTriggerDesc()
	{
		return L("The services of @x1 requires using an Infused place, and are the following: @x2.",
				name(),rituals.getTriggerDesc(RitualType.SERVICE));
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
				if(msg.source().charStats().getStat(CharStats.STAT_FAITH)>=100)
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

	public synchronized void bestowBlessing(final MOB mob, final Ability blessingA)
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
		try
		{
			final String infusedStr=CMLib.law().getClericInfused(targetRoom);
			targetRoom.bringMobHere(this,false);
			if(blessingA!=null)
			{
				final Vector<String> V=new Vector<String>();
				final Ability blessedA;
				if(blessingA.canTarget(Ability.CAN_MOBS))
				{
					V.addElement(mob.name()+"$");
					blessingA.invoke(this,V,mob,true,mob.phyStats().level());
					blessedA=mob.fetchEffect(blessingA.ID());
				}
				else
				if(blessingA.canTarget(Ability.CAN_ITEMS))
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
					blessingA.invoke(this,V,I,true,mob.phyStats().level());
					delItem(I);
					if(!mob.isMine(I))
						mob.addItem(I);
					blessedA=I.fetchEffect(blessingA.ID());
				}
				else
				{
					blessingA.invoke(this,mob,true,mob.phyStats().level());
					blessedA=mob.fetchEffect(blessingA.ID());
				}
				if((blessedA!=null)
				&&(blessedA.invoker()==this)
				&&(blessedA.affecting()!=null)
				&&(blessedA.affecting().fetchEffect(blessedA.ID())!=null)
				&&(infusedStr!=null)
				&&(Name().equalsIgnoreCase(infusedStr))
				&&(blessedA.expirationDate() > 0))
					blessedA.setExpirationDate(Math.round(CMath.mul((double)blessedA.expirationDate(), 1.5)));
			}
		}
		finally
		{
			prevRoom.bringMobHere(this,false);
			if((prevRoom != location())
			||(!prevRoom.isInhabitant(this))
			||(targetRoom.isInhabitant(this)))
			{
				this.setLocation(prevRoom);
				prevRoom.addInhabitant(this);
				targetRoom.delInhabitant(this);
			}
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
		try
		{
			rituals.setIgnoreTracking(mob, true);
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
		finally
		{
			rituals.setIgnoreTracking(mob, false);
		}
	}

	public synchronized void bestowPowers(final MOB mob)
	{
		rituals.setIgnoreTracking(mob, true);
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
		finally
		{
			rituals.setIgnoreTracking(mob, false);
		}
	}

	public synchronized void bestowCurses(final MOB mob)
	{
		rituals.setIgnoreTracking(mob, true);
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
		finally
		{
			rituals.setIgnoreTracking(mob, false);
		}
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
					for(final Ability pA : this.powers)
					{
						if(pA.ID().equals(A.ID()))
						{
							mob.delAbility(A);
							A=mob.fetchEffect(A.ID());
							if(A!=null)
							{
								A.unInvoke();
								mob.delEffect(A);
							}
							break;
						}
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
		{
			for(int a=0;a<mob.numAbilities();a++)
			{
				final Ability A=mob.fetchAbility(a);
				if((A!=null)
				&&(!A.isSavable()))
				{
					for(final Ability pA : this.powers)
					{
						if(pA.ID().equals(A.ID()))
							return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void recoverCharStats()
	{
		super.recoverCharStats();
		if(charStats.getMyDeity()==this)
		{
			if(baseCharStats.getMyDeity()==this)
				baseCharStats.setWorshipCharID("");
			charStats.setWorshipCharID("");
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_SERVE:
				{
					msg.source().baseCharStats().setWorshipCharID(name());
					msg.source().recoverCharStats();
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
							final CMMsg msg2=rituals.genNextAbleTrigger(msg.source(), RitualType.SERVICE);
							if(msg2 != null)
								msg.addTrailerMsg(msg2);
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
				if(numBlessings()>0)
				{
					RitualType type = RitualType.WORSHIP_BLESSING;
					if(msg.source().charStats().getStat(CharStats.STAT_FAITH)>=100)
						type = RitualType.CLERIC_BLESSING;
					if(rituals.isTracking(type, msg))
					{
						if((rituals.isCompleted(type, msg))
						&&(!alreadyBlessed(msg.source())))
							bestowBlessings(msg.source());
					}
				}
				if(numCurses()>0)
				{
					RitualType type = RitualType.WORSHIP_CURSE;
					if(msg.source().charStats().getStat(CharStats.STAT_FAITH)>=100)
						type = RitualType.CLERIC_CURSE;
					if(rituals.isTracking(type, msg))
					{
						if(rituals.isCompleted(type, msg))
							bestowCurses(msg.source());
					}
				}
				if(numPowers()>0)
				{
					if(((msg.source().charStats().getStat(CharStats.STAT_FAITH)>=100)
							||(msg.source().isPlayer() && msg.source().isAttributeSet(Attrib.SYSOPMSGS)))
					&&(rituals.isTracking(RitualType.POWER, msg)))
					{
						if((rituals.isCompleted(RitualType.POWER, msg))
						&&(!alreadyPowered(msg.source())))
							bestowPowers(msg.source());
					}
				}

				if(((msg.source().charStats().getStat(CharStats.STAT_FAITH)>=100)
					||(msg.source().isPlayer() && msg.source().isAttributeSet(Attrib.SYSOPMSGS)))
				&&((Name().equalsIgnoreCase(CMLib.law().getClericInfused(msg.source().location())))
					||((msg.source().charStats().getStat(CharStats.STAT_FAITH)>=1000)
						&&(Name().equals(msg.source().charStats().getWorshipCharID())))))
				{
					if(rituals.isTracking(RitualType.SERVICE, msg))
					{
						if(rituals.isCompleted(RitualType.SERVICE, msg))
						{
							if(!alreadyServiced(msg.source(),msg.source().location()))
								finishService(msg.source(),msg.source().location());
						}
						else
							startServiceIfNecessary(msg.source(),msg.source().location());
					}
				}
			}
		}
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
			rituals.deleteTracking(mob, RitualType.SERVICE);
		}
		return true;
	}

	protected int[] getBlackmarks(final MOB M)
	{
		final int[] blackmarks;
		synchronized(blacklist)
		{
			if(!blacklist.containsKey(M.Name()))
			{
				blackmarks=new int[]{0};
				blacklist.put(M.Name(), blackmarks);
			}
			else
				blackmarks=blacklist.get(M.Name());
		}
		return blackmarks;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)
		&&((--rebukeCheckDown)<0))
		{
			rebukeCheckDown=30;
			for(final Enumeration<MOB> p=CMLib.players().players();p.hasMoreElements();)
			{
				final MOB M=p.nextElement();
				if((!M.isMonster())
				&&(M.charStats().getWorshipCharID().equals(name()))
				&&(CMLib.flags().isInTheGame(M,true)))
				{
					final Room R=M.location();
					if(R==null)
						continue;
					if((M.charStats().getStat(CharStats.STAT_FAITH)>=100)
					&&(M.baseCharStats().getWorshipCharID().equals(M.charStats().getWorshipCharID())))
					{
						if(!CMLib.masking().maskCheck(getClericRequirements(),M,true))
						{
							final int[] blackmarks=getBlackmarks(M);
							if((++blackmarks[0]>30))
							{
								final CMMsg eventMsg=CMClass.getMsg(this, M, null,
										CMMsg.MSG_HOLYEVENT, null,
										CMMsg.MSG_HOLYEVENT, null,
										CMMsg.NO_EFFECT, HolyEvent.REBUKE.toString());
								R.send(this, eventMsg);
								final CMMsg msg=CMClass.getMsg(M,this,null,CMMsg.MSG_REBUKE,L("<S-NAME> <S-HAS-HAVE> been rebuked by <T-NAME>!!"));
								if(M.okMessage(M,msg))
									R.send(M,msg);
								blacklist.remove(M.Name());
							}
							else
							{
								final CMMsg eventMsg=CMClass.getMsg(this, M, null,
										CMMsg.MSG_HOLYEVENT, null,
										CMMsg.MSG_HOLYEVENT, null,
										CMMsg.NO_EFFECT, HolyEvent.DISAPPOINTED.toString());
								eventMsg.setValue(blackmarks[0]);
								if(M.okMessage(M,eventMsg))
								{
									R.send(this, eventMsg);
									if(blackmarks[0]==0)
										M.tell(L("You feel dirtied by the disappointment of @x1.",name()));
								}
							}
						}
						else
						{
							synchronized(blacklist)
							{
								if(blacklist.containsKey(M.Name()))
									blacklist.remove(M.Name());
							}
						}
					}
					else
					if(!CMLib.masking().maskCheck(getWorshipRequirements(),M,true))
					{
						final int[] blackmarks=getBlackmarks(M);
						if((++blackmarks[0]>30))
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
							final CMMsg eventMsg=CMClass.getMsg(this, M, null,
									CMMsg.MSG_HOLYEVENT, null,
									CMMsg.MSG_HOLYEVENT, null,
									CMMsg.NO_EFFECT, HolyEvent.DISAPPOINTED.toString());
							eventMsg.setValue(blackmarks[0]);
							if(M.okMessage(M,eventMsg))
							{
								R.send(this, eventMsg);
								if(blackmarks[0]==1)
									M.tell(L("Worshipper, you have disappointed @x1. Make amends or face my wrath!",name()));
							}
						}
					}
					else
					{
						synchronized(blacklist)
						{
							if(blacklist.containsKey(M.Name()))
								blacklist.remove(M.Name());
						}
					}
				}
			}
			if(services.size()>0)
			{
				LinkedList<WorshipService> delThese = null;
				synchronized(services)
				{
					for(final WorshipService service : services)
					{
						if((service.cleric!=null)
						&&(!service.serviceCompleted)
						&&(!rituals.isTracking(service.cleric, RitualType.SERVICE)))
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
		for(final MOB M : rituals.whosDoneWaiting())
		{
			final CMMsg msg = CMClass.getMsg(M,null,null,CMMsg.MSG_OK_VISUAL,null);
			try
			{
				executeMsg(M, msg);
			}
			catch(final Exception e)
			{
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
		setRitual(RitualType.CLERIC_CURSE, ritual);
	}

	@Override
	public String getClericSinDesc()
	{
		if(numCurses()>0)
		{
			return L("The curses of @x1 are placed upon @x2 clerics whenever the cleric does the following: @x3.",
					name(),charStats().hisher(),rituals.getTriggerDesc(RitualType.CLERIC_CURSE));
		}
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
		setRitual(RitualType.WORSHIP_CURSE, ritual);
	}

	@Override
	public String getWorshipSinDesc()
	{
		if(numCurses()>0)
		{
			return L("The curses of @x1 are placed upon @x2 worshippers whenever the worshipper does the following: @x3.",
					name(),charStats().hisher(),rituals.getTriggerDesc(RitualType.WORSHIP_CURSE));
		}
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
		setRitual(RitualType.POWER, ritual);
	}

	@Override
	public String getClericPowerupDesc()
	{
		if(numPowers()>0)
		{
			return L("Special powers of @x1 are placed upon @x2 clerics whenever the cleric does the following: @x3.",
					name(),charStats().hisher(),rituals.getTriggerDesc(RitualType.POWER));
		}
		return "";
	}

}
