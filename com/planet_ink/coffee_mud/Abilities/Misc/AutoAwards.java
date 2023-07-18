package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2022-2023 Bo Zimmerman

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
		if(newText.equals("RESET"))
		{
			savedHash = null;
			lastClock = null;
			affectHash	= null;
			affects.clear();
			myEntries.clear();
		}
		else
			super.setMiscText(newText);
	}

	protected volatile int[]		savedHash	= null;
	protected TimeClock				lastClock	= null;
	protected volatile int[]		affectHash	= null;
	protected final List<CMObject>	affects		= new Vector<CMObject>();

	protected final Map<TimePeriod, AutoProperties[]>	myEntries	= new Hashtable<TimePeriod, AutoProperties[]>();

	public void gatherEntries(final Physical P, final AutoProperties[] entries, final List<AutoProperties> apply, final int[] hash)
	{
		if((entries == null)||(entries.length==0))
			return;
		final MaskingLibrary mlib = CMLib.masking();
		for(final AutoProperties E : entries)
		{
			if(mlib.maskCheck(E.getDateCMask(), P, true))
			{
				apply.add(E);
				hash[0] ^= E.hashCode();
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
			final int astroHash = CMLib.awards().getAutoPropertiesHash();
			if((savedHash==null)
			||(savedHash[0]!=astroHash))
			{
				if(savedHash == null)
					savedHash = new int[] { 0 };
				savedHash[0] = astroHash;
				assignMyEntries(affected);
			}
			if(myEntries.size()>0)
			{
				final TimeClock now = CMLib.time().homeClock(affected);
				if((now != null)
				&& (now.getHourOfDay() != lastClock.getHourOfDay()))
				{
					if((myEntries.containsKey(TimePeriod.HOUR))
					||((now.getDayOfYear() != lastClock.getDayOfYear())&&(myEntries.containsKey(TimePeriod.DAY)))
					||((now.getWeekOfYear() != lastClock.getWeekOfYear())&&(myEntries.containsKey(TimePeriod.WEEK)))
					||((now.getMonth() != lastClock.getMonth())&&(myEntries.containsKey(TimePeriod.MONTH)))
					||((now.getSeasonCode() != lastClock.getSeasonCode())&&(myEntries.containsKey(TimePeriod.SEASON)))
					||((now.getYear() != lastClock.getYear())&&(myEntries.containsKey(TimePeriod.YEAR))))
					{
						final List<AutoProperties> chk = new ArrayList<AutoProperties>();
						final int[] eHash = new int[] {0};
						final Room R=CMLib.map().roomLocation(affected);
						for(final TimePeriod key : myEntries.keySet())
							gatherEntries(R, myEntries.get(key), chk, eHash);
						if((affectHash==null)
						||(affectHash[0] != eHash[0]))
						{
							if(affectHash == null)
								affectHash = new int[1];
							affectHash[0] = eHash[0];
							affects.clear();
							for(final AutoProperties aE : chk)
							{
								for(final Pair<String,String> pE : aE.getProps())
								{
									final Ability A = CMClass.getAbility(pE.first);
									if(A != null)
									{
										A.setMiscText(pE.second);
										A.setAffectedOne(affected);
										A.setProficiency(100);
										affects.add(A);
									}
									else
									{
										final Behavior B=CMClass.getBehavior(pE.first);
										if(B != null)
										{
											B.setParms(pE.second);
											if(affected instanceof PhysicalAgent)
												B.startBehavior((PhysicalAgent)affected);
											affects.add(B);
										}
									}
								}
							}
						}
						lastClock.setFromHoursSinceEpoc(now.toHoursSinceEpoc());
						affected.recoverPhyStats();
						if(affected instanceof MOB)
						{
							((MOB)affected).recoverCharStats();
							((MOB)affected).recoverMaxState();
						}
					}
				}
				for(final CMObject p : affects)
				{
					if(p instanceof Tickable)
						((Tickable)p).tick(ticking, tickID);
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		for(final CMObject p : affects)
		{
			if(p instanceof MsgListener)
			{
				((MsgListener)p).executeMsg(myHost, msg);
			}
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		for(final CMObject p : affects)
		{
			if(p instanceof MsgListener)
			{
				if(!((MsgListener)p).okMessage(myHost, msg))
					return false;
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
		for(final CMObject p : affects)
		{
			if(p instanceof StatsAffecting)
				((StatsAffecting)p).affectPhyStats(affected, affectableStats);
		}
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected, affectableStats);
		if(affected==null)
			return;
		for(final CMObject p : affects)
		{
			if(p instanceof StatsAffecting)
				((StatsAffecting)p).affectCharStats(affected, affectableStats);
		}
	}

	@Override
	public void affectCharState(final MOB affected, final CharState affectableStats)
	{
		super.affectCharState(affected, affectableStats);
		if(affected==null)
			return;
		for(final CMObject p : affects)
		{
			if(p instanceof StatsAffecting)
				((StatsAffecting)p).affectCharState(affected, affectableStats);
		}
	}
	protected void assignMyEntries(final Physical P)
	{
		final Map<TimePeriod,List<AutoProperties>> newMap = new HashMap<TimePeriod,List<AutoProperties>>();
		final HashMap<CompiledZMask,Boolean> tried = new HashMap<CompiledZMask,Boolean>();
		for(final Enumeration<AutoProperties> ap = CMLib.awards().getAutoProperties(); ap.hasMoreElements();)
		{
			final AutoProperties a = ap.nextElement();
			Boolean b;
			if((a.getPlayerCMask() == null)||(a.getPlayerCMask().empty()))
				b=Boolean.TRUE;
			else
			{
				b = tried.get(a.getPlayerCMask());
				if(b == null)
					b = Boolean.valueOf(CMLib.masking().maskCheck(a.getPlayerCMask(), P, true));
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
