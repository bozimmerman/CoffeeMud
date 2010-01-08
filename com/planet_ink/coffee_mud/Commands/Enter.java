package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Enter extends Go
{
	public Enter(){}

	private String[] access={"ENTER","EN"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<=1)
		{
			mob.tell("Enter what or where? Try EXITS.");
			return false;
		}
		String enterWhat=CMParms.combine(commands,1).toUpperCase();
		int dir=Directions.getGoodDirectionCode(enterWhat);
		if(dir<0)
		{
			Environmental getThis=mob.location().fetchFromRoomFavorItems(null,enterWhat,Wearable.FILTER_UNWORNONLY);
			if(getThis!=null)
			{
				if(getThis instanceof Rideable)
				{
					Command C=CMClass.getCommand("Sit");
					if(C!=null) return C.execute(mob,commands,metaFlags);
				}
				else
				if((getThis instanceof DeadBody)
				&&(mob.envStats().height()<=0)
				&&(mob.envStats().weight()<=0))
				{
					String mountStr="<S-NAME> enter(s) <T-NAME>.";
					CMMsg msg=CMClass.getMsg(mob,getThis,null,CMMsg.MSG_SIT,mountStr);
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
		move(mob,dir,false,false,false);
		return false;
	}
	public boolean canBeOrdered(){return true;}

	
}
