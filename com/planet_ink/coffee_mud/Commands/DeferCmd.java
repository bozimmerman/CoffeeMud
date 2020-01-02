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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2018-2020 Bo Zimmerman

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
public class DeferCmd extends StdCommand
{
	public DeferCmd()
	{
	}

	private static final String[] access=new String[]{};
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private Command passThruC = null;

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final PlayerStats pStats = mob.playerStats();
		if(pStats != null)
		{
			final int amount = pStats.getDeferredXP() + pStats.getRolePlayXP();
			final int rp = pStats.getRolePlayXP();
			pStats.setDeferredXP(0);
			pStats.setRolePlayXP(0);
			mob.setExperience(mob.getExperience()+amount);
			pStats.setLastXPAwardMillis(System.currentTimeMillis());
			String homageMessage;
			if(rp == 0)
				homageMessage="";
			else
			if(rp == 1)
				homageMessage = L(", ^H@x1^N^! point came from roleplaying",""+rp);
			else
				homageMessage = L(", ^H@x1^N^! points came from roleplaying",""+rp);

			if(amount>1)
				mob.tell(L("^N^!You gain ^H@x1^N^! experience points@x2.^N",""+amount,homageMessage));
			else
			if(amount>0)
				mob.tell(L("^N^!You gain ^H@x1^N^! experience point@x2.^N",""+amount,homageMessage));

			if((mob.getExperience()>=mob.getExpNextLevel())
			&&(mob.getExpNeededLevel()<Integer.MAX_VALUE))
				CMLib.leveler().level(mob);
		}
		if(passThruC != null)
			return passThruC.execute(mob, commands, metaFlags);
		return false;
	}

	@Override
	public Object executeInternal(final MOB mob, final int metaFlags, final Object... args) throws java.io.IOException
	{
		if((args.length>0)&&(args[0] instanceof Command))
			passThruC=(Command)args[0];
		else
			return super.executeInternal(mob, metaFlags, args);
		return Boolean.valueOf(true);
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

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return mob.isPlayer() && CMProps.getIntVar(CMProps.Int.EXPDEFER_PCT) > 0;
	}

}
