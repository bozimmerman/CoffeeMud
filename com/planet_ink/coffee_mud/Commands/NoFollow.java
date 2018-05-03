package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.Follower;
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

public class NoFollow extends Follow
{
	public NoFollow(){}

	private final String[] access=I(new String[]{"NOFOLLOW","NOFOL"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		Vector<String> origCmds=new XVector<String>(commands);
		if(commands.size()>1)
		{
			if(commands.get(0).equalsIgnoreCase("UNFOLLOW"))
			{
				unfollow(mob,((commands.size()>1)&&(commands.get(1).equalsIgnoreCase("QUIETLY"))));
				return false;
			}
			final String name=CMParms.combine(commands,1);
			if(name.equalsIgnoreCase("ALL"))
			{
				if(mob.numFollowers()==0)
					CMLib.commands().postCommandFail(mob,origCmds,L("No one is following you!"));
				else
					unfollow(mob,((commands.size()>1)&&(commands.get(1).equalsIgnoreCase("QUIETLY"))));
				return false; 
			}
			MOB M=mob.fetchFollower(name);
			if((M==null)&&(mob.location()!=null))
			{
				M=mob.location().fetchInhabitant(name);
				if(M!=null)
					CMLib.commands().postCommandFail(mob,origCmds,L("@x1 is not following you!",M.name(mob)));
				else
					CMLib.commands().postCommandFail(mob,origCmds,L("There is noone here called '@x1' following you!",name));
				return false;
			}
			if((mob.location()!=null)&&(M!=null)&&(M.amFollowing()==mob))
			{
				nofollow(M,true,false);
				return true;
			}
			CMLib.commands().postCommandFail(mob,origCmds,L("There is noone called '@x1' following you!",name));
			return false;
		}
		if(!mob.isAttributeSet(MOB.Attrib.NOFOLLOW))
		{
			mob.setAttribute(MOB.Attrib.NOFOLLOW,true);
			//unfollow(mob,false);
			mob.tell(L("You are no longer accepting new followers."));
		}
		else
		{
			mob.setAttribute(MOB.Attrib.NOFOLLOW,false);
			mob.tell(L("You are now accepting new followers."));
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
