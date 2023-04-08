package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimeOfDay;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.Map.Entry;

/*
   Copyright 2004-2023 Bo Zimmerman

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
public class Sleep extends StdCommand implements Tickable
{
	public Sleep()
	{
	}

	private final String[] access=I(new String[]{"SLEEP","SL"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private static enum WaitUntil
	{
		DAY, NIGHT, DAWN, DUSK, HEALED, FULL, RESTED
	}

	private final Map<MOB, WaitUntil> untilMap = new Hashtable<MOB, WaitUntil>();

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final Vector<String> origCmds=new XVector<String>(commands);
		if(CMLib.flags().isSleeping(mob))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You are already asleep!"));
			return false;
		}
		final Room R=mob.location();
		if(R==null)
			return false;

		if((commands.size()>2)
		&&(commands.get(commands.size()-2).equalsIgnoreCase("until")))
		{
			final String what = commands.get(commands.size()-1).toUpperCase().trim();
			commands.remove(commands.size()-2);
			commands.remove(commands.size()-1);
			WaitUntil wait=(WaitUntil)CMath.s_valueOf(WaitUntil.class, what);
			if(wait == null)
			{
				if(("DAYTIME").startsWith(what))
					wait=WaitUntil.DAY;
				else
				if(("NIGHTTIME").startsWith(what))
					wait=WaitUntil.NIGHT;
				else
				{
					mob.tell(L("Unknown until '@x1', try DAY, NIGHT, DAWN, DUSK, HEALED, RESTED, or FULL",what));
					return false;
				}
			}
			synchronized(untilMap)
			{
				if(untilMap.containsKey(mob))
					untilMap.remove(mob);
			}
			untilMap.put(mob, wait);
			if(!CMLib.threads().isTicking(this, Tickable.TICKID_MISCELLANEOUS))
				CMLib.threads().startTickDown(this, Tickable.TICKID_MISCELLANEOUS, 1);
		}
		if(commands.size()<=1)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_SLEEP,L("<S-NAME> lay(s) down and take(s) a nap."));
			if(R.okMessage(mob,msg))
				R.send(mob,msg);
			return false;
		}
		final String possibleRideable=CMParms.combine(commands,1);
		final Environmental E=R.fetchFromRoomFavorItems(null,possibleRideable);
		if((E==null)||(!CMLib.flags().canBeSeenBy(E,mob)))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't see '@x1' here.",possibleRideable));
			return false;
		}
		String mountStr=null;
		if(E instanceof Rideable)
			mountStr="<S-NAME> "+((Rideable)E).mountString(CMMsg.TYP_SLEEP,mob)+" <T-NAME>.";
		else
			mountStr=L("<S-NAME> sleep(s) on <T-NAME>.");
		String sourceMountStr=null;
		if(!CMLib.flags().canBeSeenBy(E,mob))
			sourceMountStr=mountStr;
		else
		{
			sourceMountStr=CMStrings.replaceAll(mountStr,"<T-NAME>",E.name());
			sourceMountStr=CMStrings.replaceAll(sourceMountStr,"<T-NAMESELF>",E.name());
		}
		final CMMsg msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_SLEEP,sourceMountStr,mountStr,mountStr);
		if(R.okMessage(mob,msg))
			R.send(mob,msg);
		return false;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID());
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID());
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	protected volatile int tickStatus = Tickable.STATUS_NOT;

	@Override
	public int getTickStatus()
	{
		return tickStatus;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		tickStatus = Tickable.STATUS_ALIVE;
		final Iterator<Entry<MOB,WaitUntil>> e;
		synchronized(untilMap)
		{
			e = untilMap.entrySet().iterator();
		}
		final TimeManager mgr = CMLib.time();
		for(;e.hasNext();)
		{
			final Entry<MOB,WaitUntil> E=e.next();
			final MOB M=E.getKey();
			final boolean isSleeping = CMLib.flags().isSleeping(M);
			boolean wakeMeUp = false;
			if(!wakeMeUp)
			{
				final TimeClock clock = mgr.localClock(M);
				switch(E.getValue())
				{
				case DAWN:
					wakeMeUp = clock.getTODCode()==TimeOfDay.DAWN;
					break;
				case DAY:
					wakeMeUp = clock.getTODCode()==TimeOfDay.DAY;
					break;
				case DUSK:
					wakeMeUp = clock.getTODCode()==TimeOfDay.DUSK;
					break;
				case NIGHT:
					wakeMeUp = clock.getTODCode()==TimeOfDay.NIGHT;
					break;
				case FULL:
					wakeMeUp = M.curState().getHitPoints() >= M.maxState().getHitPoints();
					wakeMeUp &= M.curState().getMana() >= M.maxState().getMana();
					wakeMeUp &= M.curState().getMovement() >= M.maxState().getMovement();
					break;
				case HEALED:
					wakeMeUp = M.curState().getHitPoints() >= M.maxState().getHitPoints();
					break;
				case RESTED:
					wakeMeUp = M.curState().getFatigue() <= 0;
					break;
				}
			}
			if(wakeMeUp || (!isSleeping))
			{
				e.remove();
			}
			if(wakeMeUp && isSleeping)
			{
				M.enqueCommand(new XVector<String>("WAKE"),0, 1);
			}
		}

		tickStatus = Tickable.STATUS_NOT;
		return untilMap.size()>0;
	}

}
