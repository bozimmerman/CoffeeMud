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
public class Back extends Go
{
	public Back()
	{
	}

	private final String[] access=I(new String[]{"BACK"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		int direction=Directions.SOUTH;
		if((commands!=null)&&(commands.size()>1))
		{
			final int nextDir=CMLib.directions().getDirectionCode(commands.get(1));
			if(nextDir == Directions.EAST)
				direction=Directions.SOUTHEAST;
			else
			if(nextDir == Directions.WEST)
				direction=Directions.SOUTHWEST;
		}
		if(!standIfNecessary(mob,commands, metaFlags, true))
			return false;
		if(mob.isAttributeSet(MOB.Attrib.AUTORUN))
			CMLib.tracking().run(mob, direction, false,false,false);
		else
			CMLib.tracking().walk(mob, direction, false,false,false);
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		if((mob==null) || (mob.location()==null))
			return false;
		if(mob.isMonster())
			return true;
		return (CMLib.flags().getInDirType(mob) == Directions.DirType.CARAVAN);
	}
	
	@Override
	public boolean putInCommandlist()
	{
		return false;
	}
}
