package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;

/*
	Written by Robert from The Looking Glass 2005
   Copyright 2005 Robert

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
public class Experience extends StdCommand
{
	public Experience(){}

	private final String[] access=I(new String[]{"EXPERIENCE","EXPER","XP","EXP"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public StringBuffer getScore(MOB mob)
	{
		final StringBuffer msg=new StringBuffer("^N");

		if(CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION)&&(mob.playerStats()!=null))
			msg.append(L("Your account is Registered and Active until: @x1!\n\r",CMLib.time().date2String(mob.playerStats().getAccountExpiration())));

		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.EXPERIENCE))
		&&!mob.charStats().getCurrentClass().expless()
		&&!mob.charStats().getMyRace().expless())
		{
			msg.append(L("\nYou have scored ^!@x1^? experience points and have been online for ^!@x2^? hours.\n\r",""+mob.getExperience(),""+Math.round(CMath.div(mob.getAgeMinutes(),60.0))));
			if((!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
			&&(!mob.charStats().getCurrentClass().leveless())
			&&(!mob.charStats().getMyRace().leveless()))
			{
				if((CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL)>0)
				&&(mob.basePhyStats().level()>CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL)))
					msg.append(L("You will not gain further levels through experience.\n\r"));
				else
				if(mob.getExpNeededLevel()==Integer.MAX_VALUE)
					msg.append(L("You will not gain further levels through experience.\n\r"));
				else
					msg.append(L("You need ^!@x1^? experience points to advance to the next level.\n\r",""+(mob.getExpNeededLevel())));
			}
		}

		return msg;
	}

	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final StringBuffer msg=getScore(mob);
		if(commands.size()==0)
		{
			commands.add(msg.toString());
			return false;
		}
		if(!mob.isMonster())
			mob.session().wraplessPrintln(msg.toString());
		return false;
	}

	public int ticksToExecute(){return 0;}
	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}
