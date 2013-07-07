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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Enter extends Go
{
	public Enter(){}

	private final String[] access={"ENTER","EN","="};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		boolean consoleMode=(mob.riding() instanceof Electronics.Computer);
		if(commands.size()<=1)
		{
			if(consoleMode)
				mob.tell("Enter what into this console?  Have you read the screen?");
			else
				mob.tell("Enter what or where? Try LOOK or EXITS.");
			return false;
		}
		Environmental enterThis=(consoleMode)?mob.riding():null;
		if(commands.size()>1)
		{
			String enterWhere=(String)commands.lastElement();
			Environmental tryThis=mob.location().fetchFromRoomFavorItems(null,enterWhere);
			if(tryThis instanceof Electronics.Computer)
			{
				enterThis=tryThis;
				commands.removeElementAt(commands.size()-1);
			}
		}
		String enterWhat=CMParms.combine(commands,1);
		if(consoleMode)
		{
			String enterStr="^W<S-NAME> enter(s) '"+enterWhat+"' into <T-NAME>.^?";
			CMMsg msg=CMClass.getMsg(mob,enterThis,null,CMMsg.MSG_WRITE,enterStr,CMMsg.MSG_WRITE,enterWhat,CMMsg.MSG_WRITE,null);
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			return true;
		}
		else
		{
			int dir=Directions.getGoodDirectionCode(enterWhat.toUpperCase());
			if(dir<0)
			{
				enterThis=mob.location().fetchFromRoomFavorItems(null,enterWhat.toUpperCase());
				if(enterThis!=null)
				{
					if(enterThis instanceof Rideable)
					{
						Command C=CMClass.getCommand("Sit");
						if(C!=null) return C.execute(mob,commands,metaFlags);
					}
					else
					if((enterThis instanceof DeadBody)
					&&(mob.phyStats().height()<=0)
					&&(mob.phyStats().weight()<=0))
					{
						String enterStr="<S-NAME> enter(s) <T-NAME>.";
						CMMsg msg=CMClass.getMsg(mob,enterThis,null,CMMsg.MSG_SIT,enterStr);
						if(mob.location().okMessage(mob,msg))
							mob.location().send(mob,msg);
						return true;
					}
				}
				dir=CMLib.tracking().findExitDir(mob,mob.location(),enterWhat);
				if(dir<0)
				{
					mob.tell("You don't see '"+enterWhat.toLowerCase()+"' here.");
					return false;
				}
			}
			CMLib.tracking().walk(mob,dir,false,false,false);
		}
		return false;
	}
	public boolean canBeOrdered(){return true;}

	
}
