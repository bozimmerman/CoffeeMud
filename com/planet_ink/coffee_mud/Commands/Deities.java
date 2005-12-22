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
   Copyright 2000-2006 Bo Zimmerman

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
public class Deities extends StdCommand
{
	public Deities(){}

	private String[] access={getScr("Deities","cmd"),getScr("Deities","cmd1")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String str=CMParms.combine(commands,1).toUpperCase();
		StringBuffer msg=new StringBuffer("");
		if(str.length()==0)
			msg.append(getScr("Deities","knowmsg"));
		else
			msg.append(getScr("Deities","knownames",str));
		int col=0;
		for(Enumeration d=CMLib.map().deities();d.hasMoreElements();)
		{
			Deity D=(Deity)d.nextElement();
			if((str.length()>0)&&(CMLib.english().containsString(D.name(),str)))
			{
				msg.append("\n\r^x"+D.name()+"^.^?\n\r");
				msg.append(D.description()+"\n\r");
				msg.append(D.getWorshipRequirementsDesc()+"\n\r");
				msg.append(D.getClericRequirementsDesc()+"\n\r");
				if(D.numBlessings()>0)
				{
					msg.append(getScr("Deities","blessings"));
					for(int b=0;b<D.numBlessings();b++)
						msg.append(D.fetchBlessing(b).name()+" ");
					msg.append("\n\r");
					msg.append(D.getWorshipTriggerDesc()+"\n\r");
					msg.append(D.getClericTriggerDesc()+"\n\r");
				}
				if(D.numPowers()>0)
				{
					msg.append(getScr("Deities","grpowers"));
					for(int b=0;b<D.numPowers();b++)
						msg.append(D.fetchPower(b).name()+" ");
					msg.append("\n\r");
					msg.append(D.getClericPowerupDesc()+"\n\r");
				}
			}
			else
			if(str.length()==0)
			{
				col++;
				if(col>4){ msg.append("\n\r"); col=0;}
				msg.append(CMStrings.padRight("^H"+D.name()+"^?",18));
			}
		}
		if(str.length()==0)
			msg.append(getScr("Deities","details"));
		mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
