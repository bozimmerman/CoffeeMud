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
public class Crawl extends Go
{
	public Crawl(){}

	private String[] access={"CRAWL","CR"};
	public String[] getAccessWords(){return access;}
    
    public boolean preExecute(MOB mob, Vector commands, int metaFlags, int secondsElapsed, double actionsRemaining)
        throws java.io.IOException
    {
        if(secondsElapsed==0)
        {
            int direction=Directions.getGoodDirectionCode(CMParms.combine(commands,1));
            if(direction<0)
            {
                mob.tell("Crawl which way?\n\rTry north, south, east, west, up, or down.");
                return false;
            }
        }
        return true;
    }
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		int direction=Directions.getGoodDirectionCode(CMParms.combine(commands,1));
		if(direction>=0)
		{
			CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_SIT,null);
			if(CMLib.flags().isSitting(mob)||(mob.location().okMessage(mob,msg)))
			{
				if(!CMLib.flags().isSitting(mob))
					mob.location().send(mob,msg);
				move(mob,direction,false,false,false);
			}
		}
		else
		{
			mob.tell("Crawl which way?\n\rTry north, south, east, west, up, or down.");
			return false;
		}
		return false;
	}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),150.0);}
	public boolean canBeOrdered(){return true;}

	
}
