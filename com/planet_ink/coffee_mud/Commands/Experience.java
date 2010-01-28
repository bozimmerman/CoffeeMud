package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.*;
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

	private String[] access={"EXPERIENCE","EXPER","XP","EXP"};
	public String[] getAccessWords(){return access;}

	public StringBuffer getScore(MOB mob)
	{
		StringBuffer msg=new StringBuffer("^N");

		if(CMProps.getBoolVar(CMProps.SYSTEMB_ACCOUNTEXPIRATION)&&(mob.playerStats()!=null))
            msg.append("Your account is Registered and Active until: "+CMLib.time().date2String(mob.playerStats().getAccountExpiration())+"!\n\r");

		if((!CMSecurity.isDisabled("EXPERIENCE"))
		&&!mob.charStats().getCurrentClass().expless()
		&&!mob.charStats().getMyRace().expless())
		{
			msg.append("\nYou have scored ^!"+mob.getExperience()+"^? experience points and have been online for ^!"+Math.round(CMath.div(mob.getAgeHours(),60.0))+"^? hours.\n\r");
			if((!CMSecurity.isDisabled("LEVELS"))
			&&(!mob.charStats().getCurrentClass().leveless())
			&&(!mob.charStats().getMyRace().leveless()))
			{
				if((CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL)>0)
				&&(mob.baseEnvStats().level()>CMProps.getIntVar(CMProps.SYSTEMI_LASTPLAYERLEVEL)))
					msg.append("You will not gain further levels through experience.\n\r");
				else
				if(mob.getExpNeededLevel()==Integer.MAX_VALUE)
					msg.append("You will not gain further levels through experience.\n\r");
				else
					msg.append("You need ^!"+(mob.getExpNeededLevel())+"^? experience points to advance to the next level.\n\r");
			}
		}
		

		return msg;
	}

	@SuppressWarnings("unchecked")
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		StringBuffer msg=getScore(mob);
		if(commands.size()==0)
		{
			commands.addElement(msg);
			return false;
		}
		if(!mob.isMonster())
			mob.session().wraplessPrintln(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
}
