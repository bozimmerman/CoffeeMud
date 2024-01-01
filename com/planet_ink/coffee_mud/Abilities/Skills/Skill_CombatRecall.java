package com.planet_ink.coffee_mud.Abilities.Skills;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/*
   Copyright 2023-2024 Bo Zimmerman

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
public class Skill_CombatRecall extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_CombatRecall";
	}

	private final static String	localizedName	= CMLib.lang().L("Combat Recall");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected volatile long	logLast	= 0;
	protected volatile long	logStrt	= 0;
	protected Set<MOB>		logging	= Collections.synchronizedSet(new HashSet<MOB>());
	protected List<String>	log		= new Vector<String>(100);

	protected long LOG_EXPIRE = CMProps.getTickMillis() *2;

	protected List<Triad<Long,Long,List<String>>> logs = new Vector<Triad<Long,Long,List<String>>>();

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Combat Recalling)");

	@Override
	public String displayText()
	{
		if(logging.size()>0)
			return localizedStaticDisplay;
		else
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
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "COMBATRECALL" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_COMBATLORE;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(20);
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	protected String getReport(final Session session, final MOB mob, final int level)
	{
		final StringBuilder rep = new StringBuilder("");
		rep.append(L("\n\r^HCombat statistics for ^w@x1 \n\r",mob.name()));
		return rep.toString();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.othersMessage()!=null)
		&&(msg.sourceMajor(CMMsg.MASK_MALICIOUS)
			||msg.targetMajor(CMMsg.MASK_MALICIOUS)
			||(msg.sourceMinor()==CMMsg.TYP_DEATH)
			||(msg.targetMinor()==CMMsg.TYP_DAMAGE)
			||(logging.contains(msg.source()))
			||(msg.source().isInCombat())
			||((msg.target() instanceof MOB)
				&&(logging.contains(msg.target())))))
		{
			synchronized(this)
			{
				if(msg.sourceMajor(CMMsg.MASK_MALICIOUS)
				||(msg.sourceMinor()==CMMsg.TYP_DEATH)
				||(msg.targetMinor()==CMMsg.TYP_DAMAGE)
				||msg.targetMajor(CMMsg.MASK_MALICIOUS))
					logLast = System.currentTimeMillis();
				if((logging.size()==0)||(logStrt<=0))
					logStrt = System.currentTimeMillis();
				if((invoker == null)
				&&(affected instanceof MOB))
					invoker=(MOB)affected;
				final MOB mob = CMClass.getFactoryMOB("no one",1,msg.source().location());
				mob.basePhyStats().setSensesMask(invoker.basePhyStats().sensesMask());
				mob.phyStats().setSensesMask(invoker.phyStats().sensesMask());
				final String logMsg =
					CMLib.coffeeFilter().fullOutFilter(null, mob, msg.source(), msg.target(), msg.tool(), msg.othersMessage(), false);
				mob.destroy();
				log.add(logMsg);
				while(log.size()>500)
					log.remove(0);
				logging.add(msg.source());
				if(msg.target() instanceof MOB)
					logging.add((MOB)msg.target());
				if(msg.sourceMajor(CMMsg.MASK_MALICIOUS)
				||(msg.sourceMinor()==CMMsg.TYP_DEATH)
				||(msg.targetMinor()==CMMsg.TYP_DAMAGE)
				||msg.targetMajor(CMMsg.MASK_MALICIOUS))
					logLast = System.currentTimeMillis();
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(logging.size()>0)
		{
			if((logLast>0)
			&&((System.currentTimeMillis()-logLast)>=LOG_EXPIRE))
			{
				if((invoker == null)
				&&(affected instanceof MOB))
					invoker=(MOB)affected;

				synchronized(this)
				{
					if(logging.size()>0)
					{
						logs.add(new Triad<Long,Long,List<String>>(
								Long.valueOf(logStrt),
								Long.valueOf(System.currentTimeMillis()),
								new XVector<String>(log)));
						logging.clear();
						log.clear();
					}
				}
				while(logs.size()>2+(super.getXLEVELLevel(invoker)/2))
					logs.remove(0);
			}
		}
		return super.tick(ticking, tickID);
	}

	@Override
	protected boolean ignoreCompounding()
	{
		return true;
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
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()==0)
		{
			mob.tell(L("Recall what? Use LIST to show remembered fights, or a number to recall a previous fight."));
			return false;
		}
		Triad<Long,Long,List<String>> picked = null;
		final String cmd = CMParms.combine(commands,0);
		if((logs.size()==0)&&(log.size()==0))
		{
			mob.tell(L("You don't recall any fights.  Go witness some!"));
			return false;
		}
		final XVector<Triad<Long,Long,List<String>>> showLogs = new XVector<Triad<Long,Long,List<String>>>(logs);
		if(log.size()>0)
		{
			showLogs.add(new Triad<Long,Long,List<String>>(
				Long.valueOf(logStrt),
				Long.valueOf(System.currentTimeMillis()),
				log
			));
		}
		final TimeClock C = CMLib.time().homeClock(mob);
		if(cmd.equalsIgnoreCase("LIST"))
		{
			final int col1 = CMLib.lister().fixColWidth(35, mob.session());
			final StringBuilder lst = new StringBuilder("");
			lst.append("#  ^H"+
					CMStrings.padRight(L("Start"), col1)+
					L(" End\n\r"));
			for(int i=0;i<showLogs.size();i++)
			{
				final int idx = i;
				final long start = showLogs.get(idx).first.longValue();
				final long end = showLogs.get(idx).second.longValue();
				final String startTime = C.deriveClock(start).getShortestTimeDescription()
					+" ("+CMLib.time().date2SmartEllapsedTime(System.currentTimeMillis()-start, true)+" ago)";
				final String endTime = C.deriveClock(end).getShortestTimeDescription()
						+" ("+CMLib.time().date2SmartEllapsedTime(System.currentTimeMillis()-end, true)+" ago)";
				lst.append("^w"+CMStrings.padRight(""+(idx+1), 3)).append("^N");
				lst.append(CMStrings.padRight(startTime, col1)).append(" ");
				lst.append(CMStrings.limit(endTime, col1)).append("\n\r");
			}
			if(mob.isMonster())
				CMLib.commands().postSay(mob, lst.toString());
			else
				mob.tell(lst.toString());
			return true;
		}
		else
		if((CMath.s_int(cmd)>0)||(cmd.equalsIgnoreCase("0")))
		{
			final int dex = CMath.s_int(cmd)-1;
			if((dex<0)||(dex>=showLogs.size()))
			{
				mob.tell(L("@x1 is not a valid recollection.  Try LIST.",""+cmd));
				return false;
			}
			picked = showLogs.get(dex);
		}
		else
		{
			mob.tell(L("'@x1' isn't correct. Use LIST to show remembered fights, or a number to recall a previous fight."));
			return false;
		}
		final Room R=mob.location();
		if((R==null)||(picked==null))
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_THINK|(auto?CMMsg.MASK_ALWAYS:0),null);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				final long start = picked.first.longValue();
				final long end = picked.second.longValue();
				final StringBuilder lst = new StringBuilder(L("^NYou recall that fight:\n\r"));
				final String startTime = C.deriveClock(start).getShortestTimeDescription()
						+" ("+CMLib.time().date2SmartEllapsedTime(System.currentTimeMillis()-start, true)+" ago)";
				final String endTime = C.deriveClock(end).getShortestTimeDescription()
						+" ("+CMLib.time().date2SmartEllapsedTime(System.currentTimeMillis()-end, true)+" ago)";
				lst.append(L("^HStarted^N: ")).append(startTime).append("\n\r");
				lst.append(L("^HEnded^N  : ")).append(endTime).append("\n\r");
				for(int i=0;i<picked.third.size();i++)
					lst.append(picked.third.get(i)).append("\n\r");
				if(mob.isMonster())
					CMLib.commands().postSay(mob, lst.toString());
				else
					mob.tell(lst.toString());
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to recall a combat <S-HE-SHE> had seen, but fail(s)."));
		return success;
	}
}
