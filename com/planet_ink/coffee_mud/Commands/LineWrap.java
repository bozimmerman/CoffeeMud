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
   Copyright 2005-2018 Bo Zimmerman

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

public class LineWrap extends StdCommand
{
	public LineWrap(){}

	private final String[] access=I(new String[]{"LINEWRAP"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if((mob==null)||(mob.playerStats()==null))
			return false;

		if(commands.size()<2)
		{
			final String wrap=(mob.playerStats().getWrap()!=0)?(""+mob.playerStats().getWrap()):"Disabled";
			mob.tell(L("Change your line wrap to what? Your current line wrap setting is: @x1. Enter a number larger than 10 or 'disable'.",wrap));
			return false;
		}
		final String newWrap=CMParms.combine(commands,1);
		int newVal=mob.playerStats().getWrap();
		if((CMath.isInteger(newWrap))&&(CMath.s_int(newWrap)>10))
			newVal=CMath.s_int(newWrap);
		else
		if("DISABLED".startsWith(newWrap.toUpperCase()))
			newVal=0;
		else
		{
			mob.tell(L("'@x1' is not a valid setting. Enter a number larger than 10 or 'disable'.",newWrap));
			return false;
		}
		mob.playerStats().setWrap(newVal);
		final String wrap=(mob.playerStats().getWrap()!=0)?(""+mob.playerStats().getWrap()):"Disabled";
		mob.tell(L("Your new line wrap setting is: @x1.",wrap));
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}

