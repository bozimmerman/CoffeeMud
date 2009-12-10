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
public class Wimpy extends StdCommand
{
	public Wimpy(){}

	private String[] access={"WIMPY"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Change your wimp level to what?");
			return false;
		}
		String amt=CMParms.combine(commands,1);
		int newWimp = mob.baseState().getHitPoints();
		if(CMath.isPct(amt))
			newWimp = (int)Math.round(CMath.s_pct(amt) * (double)newWimp);
		else
		if(CMath.isInteger(amt))
			newWimp=CMath.s_int(amt);
		else
		{
			mob.tell("You can't change your wimp level to '"+amt+"'");
			return false;
		}
		mob.setWimpHitPoint(newWimp);
		mob.tell("Your wimp level has been changed to "+mob.getWimpHitPoint()+" hit points.");
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
