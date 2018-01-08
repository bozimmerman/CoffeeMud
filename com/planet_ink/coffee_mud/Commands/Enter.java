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

public class Enter extends Go
{
	public Enter()
	{
	}

	private final String[]	access	= I(new String[] { "ENTER", "EN" });

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
		if(commands.size()<=1)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Enter what or where? Try LOOK or EXITS."));
			return false;
		}
		Environmental enterThis=null;
		final String enterWhat=CMParms.combine(commands,1);
		int dir=CMLib.directions().getGoodDirectionCode(enterWhat.toUpperCase());
		final Room R=mob.location();
		if(dir<0)
		{
			enterThis=R.fetchFromRoomFavorItems(null,enterWhat.toUpperCase());
			if(enterThis == null)
				enterThis = R.fetchExit(enterWhat);
			if(enterThis!=null)
			{
				if(enterThis instanceof Rideable)
				{
					final Command C=CMClass.getCommand("Sit");
					if(C!=null)
						return C.execute(mob,commands,metaFlags);
				}
				else
				if((enterThis instanceof DeadBody)
				&&(mob.phyStats().height()<=0)
				&&(mob.phyStats().weight()<=0))
				{
					final String enterStr=L("<S-NAME> enter(s) <T-NAME>.");
					final CMMsg msg=CMClass.getMsg(mob,enterThis,null,CMMsg.MSG_SIT,enterStr);
					if(mob.location().okMessage(mob,msg))
						mob.location().send(mob,msg);
					return true;
				}
			}
			dir=CMLib.tracking().findExitDir(mob,R,enterWhat);
			if(dir<0)
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("You don't see '@x1' here.",enterWhat.toLowerCase()));
				return false;
			}
		}
		CMLib.tracking().walk(mob,dir,false,false,false);
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
