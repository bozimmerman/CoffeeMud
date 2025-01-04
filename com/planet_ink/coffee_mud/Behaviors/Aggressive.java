package com.planet_ink.coffee_mud.Behaviors;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2025 Bo Zimmerman

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
public class Aggressive extends StdBehavior
{
	@Override
	public String ID()
	{
		return "Aggressive";
	}

	@Override
	public long flags()
	{
		return Behavior.FLAG_POTENTIALLYAGGRESSIVE | Behavior.FLAG_TROUBLEMAKING;
	}

	protected int		tickWait			= 0;
	protected int		tickDown			= 0;
	protected boolean	wander				= false;
	protected boolean	mobkill				= false;
	protected boolean	noGangUp			= false;
	protected boolean	misbehave			= false;
	protected boolean	levelcheck			= false;
	protected String	attackMessage		= null;
	protected Room		lastRoom			= null;
	protected int		lastRoomInhabCount	= 0;
	protected String	maskStr				= "";
	protected CompiledZMask mask 			= null;

	protected Quint<String,Double,Integer,Integer,
				Map<String,Pair<Double,Long>>>
					bribe 				= null;

	@Override
	public boolean okMessage(final Environmental affecting, final CMMsg msg)
	{
		if(!super.okMessage(affecting, msg))
			return false;
		if((bribe != null) && (affecting instanceof MOB))
		{
			final String currency=CMLib.beanCounter().getCurrency(affecting);
			if (msg.amITarget(affecting)
			&& (!msg.amISource((MOB)affecting))
			&& (msg.targetMinor() == CMMsg.TYP_GIVE)
			&& (msg.tool()instanceof Coins)
			&& (!CMLib.beanCounter().isCurrencyMatch(((Coins)msg.tool()).getCurrency(),currency)))
			{
				final double denomination=CMLib.beanCounter().getLowestDenomination(currency);
				CMLib.commands().postSay((MOB)affecting,msg.source(),L("I only accept @x1.",CMLib.beanCounter().getDenominationName(currency,denomination)),false,false);
				return false;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting, msg);
		if((msg.sourceMinor()==CMMsg.TYP_ENTER)
		||(msg.sourceMinor()==CMMsg.TYP_LEAVE))
			lastRoomInhabCount=-1;
		else
		if((bribe != null)
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(affecting instanceof MOB)
		&&(msg.tool() instanceof Coins)
		&& (!msg.amISource((MOB)affecting))
		&&(affecting == msg.target())
		&&(!msg.source().isMonster()))
		{
			final double paidAmount=((Coins)msg.tool()).getTotalValue();
			if(paidAmount<=0.0)
				return;
			final Map<String,Pair<Double,Long>> paid = bribe.fifth;
			if(!paid.containsKey(msg.source().Name()))
			{
				paid.put(msg.source().Name(),new Pair<Double,Long>(Double.valueOf(0.0),
						Long.valueOf(System.currentTimeMillis()+(CMProps.getTickMillis()*bribe.fourth.longValue()))));
			}
			final Pair<Double,Long> p = paid.get(msg.source().Name());
			p.first = Double.valueOf(p.first.doubleValue() + paidAmount);
			final double paidRevolver = p.getKey().doubleValue() / bribe.second.doubleValue();
			final long timeToAdd = Math.round(CMath.mul(CMProps.getTickMillisD(),CMath.mul(bribe.third.longValue(),paidRevolver)));
			if(System.currentTimeMillis() < p.second.longValue())
				p.second = Long.valueOf(p.second.longValue() + timeToAdd);
			else
				p.second = Long.valueOf(System.currentTimeMillis() + timeToAdd);
			if(p.first.doubleValue() >= bribe.second.doubleValue())
			{
				String bribeOkMsg = CMParms.getParmStr(getParms(), "BRIBEPAIDMSG", null);
				if(bribeOkMsg == null)
					bribeOkMsg = "Ok then, you are safe for @x1.";
				final long safeTime = p.second.longValue() - System.currentTimeMillis();
				final TimeClock C = CMLib.time().homeClock((MOB)affecting);
				final String mtm = CMLib.time().date2SmartEllapsedMudTime(C,safeTime, false);
				final String tm = CMLib.time().date2SmartEllapsedTime(safeTime, false);
				final String say = CMStrings.replaceVariables(bribeOkMsg,
						new String[] { mtm, tm } );
				CMLib.commands().postSay((MOB)affecting,msg.source(),say);
			}
		}
	}

	@Override
	public boolean grantsAggressivenessTo(final MOB M)
	{
		if(M==null)
			return true;
		return CMLib.masking().maskCheck(this.mask,M,false);
	}

	@Override
	public String accountForYourself()
	{
		if(maskStr.trim().length()>0)
			return "aggression against "+CMLib.masking().maskDesc(maskStr,true).toLowerCase();
		else
			return "aggressiveness";
	}

	@Override
	public void setParms(final String newParms)
	{
		super.setParms(newParms);
		tickWait=CMParms.getParmInt(newParms,"delay",0);
		attackMessage=CMParms.getParmStr(newParms,"MESSAGE",null);
		final String bribeStr = CMParms.getParmStr(newParms,"BRIBE",null);
		bribe = null;
		if(bribeStr != null)
		{
			final String bribeMsg = CMParms.getParmStr(newParms, "BRIBEPAYMSG", "");
			final List<String> bribeParts = CMParms.parseCommas(bribeStr,true);
			final double amt = CMath.s_double((bribeParts.size()<1)?"1":bribeParts.get(0));
			final int ticks = CMath.s_int((bribeParts.size()<2)?"100":bribeParts.get(1));
			final int wait = CMath.s_int((bribeParts.size()<3)?"20000":bribeParts.get(1));
			bribe = new Quint<String,Double,Integer,Integer,Map<String,Pair<Double,Long>>>(
					bribeMsg,Double.valueOf(amt),Integer.valueOf(ticks),Integer.valueOf(wait),
					new Hashtable<String,Pair<Double,Long>>());
		}
		final Vector<String> V=CMParms.parse(newParms.toUpperCase());
		wander=V.contains("WANDER");
		levelcheck=V.contains("CHECKLEVEL");
		mobkill=V.contains("MOBKILL")||(V.contains("MOBKILLER"));
		noGangUp=V.contains("NOGANG")||V.contains("NOGANGUP");
		misbehave=V.contains("MISBEHAVE");
		tickDown=tickWait;
		maskStr = CMLib.masking().separateZapperMask(V);
		this.mask=null;
		if(maskStr.length()>0)
			this.mask=CMLib.masking().getPreCompiledMask(maskStr);
	}

	public static boolean startFight(final MOB monster, final MOB mob, final boolean fightMOBs, final boolean misBehave, final String attackMsg)
	{
		if((mob!=null)&&(monster!=null)&&(mob!=monster))
		{
			final Room R=monster.location();
			if((R!=null)
			&&((!mob.isMonster())||(fightMOBs))
			&&(R.isInhabitant(mob))
			&&(R.getArea().getAreaState()==Area.State.ACTIVE)
			&&((misBehave&&(!monster.isInCombat()))||canFreelyBehaveNormal(monster))
			&&(CMLib.flags().canBeSeenBy(mob,monster))
			&&(!CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.ORDER))
			&&(!CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.CMDROOMS))
			&&(!CMLib.flags().isATrackingMonster(mob))
			&&(!CMLib.flags().isATrackingMonster(monster))
			&&(!monster.getGroupMembers(new HashSet<MOB>()).contains(mob)))
			{
				// special backstab sneak attack!
				if(CMLib.flags().isHidden(monster))
				{
					final Ability A=monster.fetchAbility("Thief_BackStab");
					if(A!=null)
					{
						A.setProficiency(CMLib.dice().roll(1,50,A.adjustedLevel(mob,0)*15));
						monster.enqueCommand(new XVector<String>(A.triggerStrings()[0],R.getContextName(mob)),MUDCmdProcessor.METAFLAG_FORCED,0);
					}
				}
				if((attackMsg!=null)&&(monster.getVictim()!=mob))
					monster.enqueCommand(new XVector<String>("SAY",attackMsg),MUDCmdProcessor.METAFLAG_FORCED,0);
				// normal attack
				monster.enqueCommand(new XVector<String>("KILL",R.getContextName(mob)),MUDCmdProcessor.METAFLAG_FORCED,0);
				return true;
			}
		}
		return false;
	}

	protected boolean preFightCheck(final MOB observer, final MOB mob)
	{
		if((bribe != null) && (!mob.isMonster()))
		{
			final Map<String,Pair<Double,Long>> paid = bribe.fifth;
			synchronized(paid)
			{
				for(final Iterator<String> k = paid.keySet().iterator();k.hasNext();)
				{
					final String key = k.next();
					final Pair<Double, Long> chk = paid.get(key);
					if((chk.first.doubleValue() >= bribe.second.doubleValue()) // paid!
					&&(System.currentTimeMillis() >= chk.second.longValue()))
					{
						paid.remove(key);
						break;
					}
				}
			}
			if(!paid.containsKey(mob.Name()))
			{
				final String baseMsg = (bribe.first.trim().length()>0) ?
						bribe.first.trim() : "Pay me @x1 or I will kill you.";
				final String currency=CMLib.beanCounter().getCurrency(observer);
				final double denomination=CMLib.beanCounter().getLowestDenomination(currency);
				final String thePrice=CMLib.beanCounter().getDenominationName(currency,denomination,Math.round(bribe.second.doubleValue()/denomination));
				final String say = CMStrings.replaceVariables(baseMsg, new String[] { thePrice } );
				CMLib.commands().postSay(observer,  mob, say);
				final Pair<Double,Long> demand = new Pair<Double,Long>(Double.valueOf(0.0),Long.valueOf(System.currentTimeMillis() + (bribe.fourth.longValue()*CMProps.getTickMillis())));
				synchronized(paid)
				{
					paid.put(mob.Name(), demand);
				}
				return false;
			}
			else
			{
				final Pair<Double,Long> rev = paid.get(mob.Name());
				if(rev == null)
					return false;
				if(System.currentTimeMillis() < rev.second.longValue())
				{
					// still have paid (or unpaid) grace
					return false;
				}
				if(rev.first.doubleValue() >= bribe.second.doubleValue()) // paid!
				{
					synchronized(paid)
					{
						paid.remove(mob.Name());
					}
					return preFightCheck(observer, mob);
				}
				else
				{
					// have not paid in allotted time
					return true;
				}
			}
		}
		return true;
	}

	public boolean pickAFight(final MOB observer, final MaskingLibrary.CompiledZMask mask, final boolean mobKiller, final boolean misBehave,
							  final boolean levelCheck, final String attackMsg, final boolean noGangUp)
	{
		if(!canFreelyBehaveNormal(observer))
			return false;
		final Room R=observer.location();
		if((R!=null)
		&&(R.getArea().getAreaState()==Area.State.ACTIVE))
		{
			if((R!=lastRoom)||(lastRoomInhabCount!=R.numInhabitants()))
			{
				lastRoom=R;
				lastRoomInhabCount=R.numInhabitants();
				final Set<MOB> groupMembers=observer.getGroupMembers(new HashSet<MOB>());
				for(int i=0;i<R.numInhabitants();i++)
				{
					final MOB mob=R.fetchInhabitant(i);
					if((mob!=null)
					&&(mob!=observer)
					&&((!levelCheck)||(observer.phyStats().level()<(mob.phyStats().level()+CMProps.getIntVar(CMProps.Int.EXPRATE))))
					&&((!mob.isMonster())||(mobKiller))
					&&(CMLib.masking().maskCheck(mask,mob,false))
					&&(!groupMembers.contains(mob))
					&&((!noGangUp)||(!mob.isInCombat()))
					&&(preFightCheck(observer, mob))
					&&(startFight(observer,mob,mobKiller,misBehave,attackMsg)))
						return true;
				}
			}
		}
		return false;
	}

	public void tickAggressively(final Tickable ticking, final int tickID, final boolean mobKiller, final boolean misBehave,
								 final boolean levelCheck, final MaskingLibrary.CompiledZMask mask, final String attackMsg,
								 final boolean noGangUp)
	{
		if(tickID!=Tickable.TICKID_MOB)
			return;
		if(ticking==null)
			return;
		if(!(ticking instanceof MOB))
			return;
		pickAFight((MOB)ticking,mask,mobKiller,misBehave,levelCheck,attackMsg,noGangUp);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=Tickable.TICKID_MOB)
			return true;
		if((--tickDown)<0)
		{
			tickDown=tickWait;
			tickAggressively(ticking,
							 tickID,
							 mobkill,
							 misbehave,
							 this.levelcheck,
							 this.mask,
							 attackMessage,
							 this.noGangUp);
		}
		return true;
	}
}
