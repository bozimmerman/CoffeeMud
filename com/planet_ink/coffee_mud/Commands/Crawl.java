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
   Copyright 2004-2018 Bo Zimmerman

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

public class Crawl extends Go
{
	public Crawl(){}

	private final String[] access=I(new String[]{"CRAWL","CR"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean preExecute(MOB mob, List<String> commands, int metaFlags, int secondsElapsed, double actionsRemaining)
		throws java.io.IOException
	{
		if(secondsElapsed==0)
		{
			final int direction=CMLib.directions().getGoodDirectionCode(CMParms.combine(commands,1));
			if(direction<0)
			{
				CMLib.commands().postCommandFail(mob,new StringXVector(commands),L("Crawl which way?\n\rTry north, south, east, west, up, or down."));
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final int direction=CMLib.directions().getGoodDirectionCode(CMParms.combine(commands,1));
		if(direction>=0)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_SIT,null);
			if(CMLib.flags().isSitting(mob)||(mob.location().okMessage(mob,msg)))
			{
				if(!CMLib.flags().isSitting(mob))
					mob.location().send(mob,msg);
				CMLib.tracking().walk(mob,direction,false,false,false);
			}
		}
		else
		{
			CMLib.commands().postCommandFail(mob,new StringXVector(commands),L("Crawl which way?\n\rTry north, south, east, west, up, or down."));
			return false;
		}
		return false;
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID(), CMath.greater(CMath.div(CMProps.getIntVar(CMProps.Int.DEFCMDTIME),50.0),1.0));
	}

	@Override
	public double combatActionsCost(MOB mob, List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID(), CMath.greater(CMath.div(CMProps.getIntVar(CMProps.Int.DEFCOMCMDTIME),50.0),2.0));
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
