package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AutoAwardsLibrary.AutoProperties;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/*
   Copyright 2022-2025 Bo Zimmerman

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
public class AutoAwards extends StdAbility
{
	@Override
	public String ID()
	{
		return "AutoAwards";
	}

	private final static String	localizedName	= CMLib.lang().L("AutoAwards");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
	}

	protected volatile boolean		forceApply	= false;
	protected volatile int[]		savedHash	= null;
	protected TimeClock				lastClock	= null;
	protected volatile int[]		affectHash	= null;
	protected volatile Ability		holderA		= null;
	protected volatile Ability		suppressorA	= null;
	protected volatile Ability		reverserA	= null;

	protected final PairList<AutoProperties, CMObject>	affects		= new PairSVector<AutoProperties, CMObject>();
	protected final Map<TimePeriod, AutoProperties[]>	myEntries	= new Hashtable<TimePeriod, AutoProperties[]>();

	protected void gatherTimelyEntries(final Physical P, final AutoProperties[] entries, final List<AutoProperties> apply, final int[] hash)
	{
		if((entries == null)||(entries.length==0))
			return;
		final MaskingLibrary mlib = CMLib.masking();
		final Ability holdA = holderA;
		if(holdA != null)
		{
			if((holdA.affecting() != affected)
			||(affected.fetchEffect(holdA.ID())!=holdA))
				this.holderA = null;
		}
		final TimeClock C = CMLib.time().homeClock(P);
		final boolean debug = CMSecurity.isDebugging(DbgFlag.AUTOAWARDS);
		for(final AutoProperties E : entries)
		{
			if(((holderA != null) && (affects.containsFirst(E)))
			||(mlib.maskCheckDateEntries(E.getDateCMask(), C)))
			{
				apply.add(E);
				hash[0] ^= E.hashCode();
				if(debug
				&&(E.getProps()!=null)
				&&(E.getProps().length>0))
				{

					final String tpcs = C.toTimePeriodCodeString();
					final int tpcsx = tpcs.indexOf(' ');
					Log.debugOut(ID(),"Auto: "
									+ CMStrings.padRight(P.name(),8)
									+ ": " + CMStrings.padRight(E.getPlayerMask(),17)
									+ ": " + CMStrings.padRight(E.getProps()[0].second,17)
									+ ": " + CMStrings.padRight(E.getDateMask(),32)
									+ ": " + ((tpcsx>0)?tpcs.substring(0,tpcsx):tpcs));
				}
			}
		}
	}

	protected void applyAward(final AutoProperties autoProp, final Pair<String, String> award, final boolean reverse)
	{
		final Ability A = CMClass.getAbility(award.first);
		String parms = award.second;
		if(reverse)
		{
			final char[] cs = parms.toCharArray();
			for(int i=0;i<cs.length;i++)
			{
				if(cs[i]=='+')
					cs[i]='-';
				else
				if(cs[i]=='-')
					cs[i]='+';
			}
			parms = new String(cs);
		}
		if(A != null)
		{
			A.setMiscText(parms);
			A.setAffectedOne(affected);
			A.setProficiency(100);
			affects.add(autoProp,A);
		}
		else
		{
			final Behavior B=CMClass.getBehavior(award.first);
			if(B != null)
			{
				B.setParms(parms);
				if(affected instanceof PhysicalAgent)
					B.startBehavior((PhysicalAgent)affected);
				affects.add(autoProp,B);
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		final Physical affected=this.affected;
		if(affected != null)
		{
			TimeClock lastClock = this.lastClock;
			final int astroHash = CMLib.awards().getAutoPropertiesHash();
			if((savedHash==null)
			||(savedHash[0]!=astroHash))
			{
				if(savedHash == null)
					savedHash = new int[] { 0 };
				savedHash[0] = astroHash;
				assignMyEntries(affected);
			}
			if(lastClock == null)
			{
				lastClock = (TimeClock)CMLib.time().homeClock(affected).copyOf();
				lastClock.setYear(1);
				lastClock.setMonth(1);
				lastClock.setDayOfMonth(1);
				lastClock.setHourOfDay(0);
				this.forceApply=true;
				this.lastClock = lastClock;
			}
			if(myEntries.size()>0)
			{
				final Ability suppressA = suppressorA;
				if(suppressA != null)
				{
					if((suppressA.affecting() != affected)
					||(affected.fetchEffect(suppressA.ID()) != suppressA))
						this.suppressorA = null;
				}
				final TimeClock now = CMLib.time().homeClock(affected);
				if((now != null)
				&& ((now.getHourOfDay() != lastClock.getHourOfDay()) || forceApply ))
				{
					if(myEntries.containsKey(TimePeriod.HOUR)
					||((now.getDayOfYear() != lastClock.getDayOfYear()) && myEntries.containsKey(TimePeriod.DAY) )
					||((now.getWeekOfYear() != lastClock.getWeekOfYear()) && myEntries.containsKey(TimePeriod.WEEK) )
					||((now.getMonth() != lastClock.getMonth()) && myEntries.containsKey(TimePeriod.MONTH) )
					||((now.getSeasonCode() != lastClock.getSeasonCode()) && myEntries.containsKey(TimePeriod.SEASON) )
					||((now.getYear() != lastClock.getYear()) && myEntries.containsKey(TimePeriod.YEAR) )
					|| (forceApply))
					{
						forceApply = false;
						final List<AutoProperties> chk = new ArrayList<AutoProperties>();
						final int[] eHash = new int[] {0};
						for(final TimePeriod key : myEntries.keySet())
							gatherTimelyEntries(affected, myEntries.get(key), chk, eHash);
						if((affectHash==null)
						||(affectHash[0] != eHash[0]))
						{
							if(affectHash == null)
								affectHash = new int[1];
							affectHash[0] = eHash[0];
							// not terribly efficient for awards that overlap, but *shrug*
							affects.clear();
							final Ability reverseA = reverserA;
							if(reverseA != null)
							{
								if((reverseA.affecting() != affected)
								||(affected.fetchEffect(reverseA.ID()) != reverseA))
									this.reverserA = null;
							}
							for(final AutoProperties aE : chk)
							{
								for(final Pair<String,String> pE : aE.getProps())
									applyAward(aE, pE, reverserA!=null);
							}
						}
						affected.recoverPhyStats();
						if(affected instanceof MOB)
						{
							((MOB)affected).recoverCharStats();
							((MOB)affected).recoverMaxState();
						}
					}
					lastClock.setDateTime(now);
				}
				if(suppressorA == null)
				{
					for(final Pair<AutoProperties, CMObject> p : affects)
					{
						if(p.second instanceof Tickable)
							((Tickable)p.second).tick(ticking, tickID);
					}
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(suppressorA == null)
		{
			for(final Pair<AutoProperties, CMObject> p : affects)
			{
				if(p.second instanceof MsgListener)
				{
					((MsgListener)p.second).executeMsg(myHost, msg);
				}
			}
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(suppressorA == null)
		{
			for(final Pair<AutoProperties, CMObject> p : affects)
			{
				if(p.second instanceof MsgListener)
				{
					if(!((MsgListener)p.second).okMessage(myHost, msg))
						return false;
				}
			}
		}
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null)
			return;
		if((suppressorA == null)&&(affects.size()>0))
		{
			for(final Pair<AutoProperties, CMObject> p : affects)
			{
				if(p.second instanceof StatsAffecting)
					((StatsAffecting)p.second).affectPhyStats(affected, affectableStats);
			}
		}
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected, affectableStats);
		if(affected==null)
			return;
		if((suppressorA == null)&&(affects.size()>0))
		{
			for(final Pair<AutoProperties, CMObject> p : affects)
			{
				if(p.second instanceof StatsAffecting)
					((StatsAffecting)p.second).affectCharStats(affected, affectableStats);
			}
		}
	}

	@Override
	public void affectCharState(final MOB affected, final CharState affectableStats)
	{
		super.affectCharState(affected, affectableStats);
		if(affected==null)
			return;
		if((suppressorA == null)&&(affects.size()>0))
		{
			for(final Pair<AutoProperties, CMObject> p : affects)
			{
				if(p.second instanceof StatsAffecting)
					((StatsAffecting)p.second).affectCharState(affected, affectableStats);
			}
		}
	}

	protected void assignMyEntries(final Physical P)
	{
		final Map<TimePeriod,List<AutoProperties>> newMap = new HashMap<TimePeriod,List<AutoProperties>>();
		final Map<CompiledZMask,Boolean> tried = new HashMap<CompiledZMask,Boolean>();
		for(final Enumeration<AutoProperties> ap = CMLib.awards().getAutoProperties(); ap.hasMoreElements();)
		{
			final AutoProperties a = ap.nextElement();
			Boolean b;
			if((a.getPlayerCMask() == null)||(a.getPlayerCMask().empty()))
				b=Boolean.TRUE;
			else
			{
				b = tried.get(a.getPlayerCMask()); // for this specific test run, only do a mask on target once
				if(b == null)
				{
					b = Boolean.valueOf(CMLib.masking().maskCheck(a.getPlayerCMask(), P, true));
					tried.put(a.getPlayerCMask(), b);
				}
			}
			if(b.booleanValue())
			{
				List<AutoProperties> tuEntries = newMap.get(a.getPeriod());
				if(tuEntries == null)
				{
					tuEntries = new ArrayList<AutoProperties>();
					newMap.put(a.getPeriod(), tuEntries);
				}
				tuEntries.add(a);
			}
		}
		myEntries.clear();
		for(final TimePeriod k : newMap.keySet())
			myEntries.put(k, newMap.get(k).toArray(new AutoProperties[0]));
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		if((P!=null)&&(P != affected))
			savedHash = null;
		super.setAffectedOne(P);
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(code==null)
			return;
		if(code.equalsIgnoreCase("RESET"))
		{
			savedHash = null;
			lastClock = null;
			affectHash	= null;
			affects.clear();
			myEntries.clear();
		}
		else
		if(code.equalsIgnoreCase("SUPPRESSOR"))
		{
			if(val.trim().length()==0)
				this.suppressorA = null;
			else
			if(this.affected != null)
				this.suppressorA = this.affected.fetchEffect(val);
		}
		else
		if(code.equalsIgnoreCase("REVERSER"))
		{
			if(val.trim().length()==0)
				this.reverserA = null;
			else
			if(this.affected != null)
				this.reverserA = this.affected.fetchEffect(val);
			this.affectHash = null;
		}
		else
		if(code.equalsIgnoreCase("HOLDER"))
		{
			if(val.trim().length()==0)
				this.holderA = null;
			else
			if(this.affected != null)
				this.holderA = this.affected.fetchEffect(val);
		}
		else
		if(code.equalsIgnoreCase("FORCETICK"))
		{
			this.forceApply = CMath.s_bool(val);
		}
		else
		if(code.equalsIgnoreCase("AUTOAWARDS"))
		{
			final Physical affected = this.affected;
			if(affected == null)
				return;
			for(final String bp : CMParms.parseSemicolons(val, true))
			{
				final int awardID = CMath.s_int(bp);
				boolean found = false;
				for(final Pair<AutoProperties, CMObject> p : affects)
				{
					if(p.first.hashCode()==awardID)
					{
						found=true;
						break;
					}
				}
				if(!found)
				{
					if(affectHash == null)
						affectHash = new int[1];
					for(final Enumeration<AutoProperties> p = CMLib.awards().getAutoProperties(); p.hasMoreElements();)
					{
						final AutoProperties P = p.nextElement();
						if(P.hashCode() == awardID)
						{
							affectHash[0] ^= P.hashCode();
							for(final Pair<String, String> ps : P.getProps())
								applyAward(P, ps, reverserA!=null);
							break;
						}
					}
					affected.recoverPhyStats();
					if(affected instanceof MOB)
					{
						((MOB)affected).recoverCharStats();
						((MOB)affected).recoverMaxState();
					}
				}
			}
		}
		else
			super.setStat(code, val);
	}

	@Override
	public String getStat(final String code)
	{
		if(code==null)
			return "";
		if(code.equalsIgnoreCase("AUTOAWARDS"))
		{
			final StringBuilder str = new StringBuilder("");
			if(suppressorA == null)
			{
				for(final Pair<AutoProperties, CMObject> p : affects)
					str.append(p.first.hashCode()+";");
			}
			return str.toString();
		}
		return super.getStat(code);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);

		if(target==null)
			return false;

		if(target.fetchEffect(ID())!=null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,"");
			if(target.location()!=null)
			{
				if(target.location().okMessage(target,msg))
				{
					target.location().send(target,msg);
					final Ability A=(Ability)copyOf();
					target.addNonUninvokableEffect(A);
				}
			}
			else
			{
				final Ability A=(Ability)copyOf();
				target.addNonUninvokableEffect(A);
			}
		}
		return success;
	}
}
