package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

	private String[] access={"DEITIES","GODS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String str=Util.combine(commands,1).toUpperCase();
		StringBuffer msg=new StringBuffer("");
		if(str.length()==0)
			msg.append("\n\r^xThe known deities:^.^? \n\r\n\r");
		else
			msg.append("\n\r^HThe known deities named '"+str+"':^? \n\r");
		int col=0;
		for(Enumeration d=CMMap.deities();d.hasMoreElements();)
		{
			Deity D=(Deity)d.nextElement();
			if((str.length()>0)&&(EnglishParser.containsString(D.name(),str)))
			{
				msg.append("\n\r^x"+D.name()+"^.^?\n\r");
				msg.append(D.description()+"\n\r");
				msg.append(D.getWorshipRequirementsDesc()+"\n\r");
				msg.append(D.getClericRequirementsDesc()+"\n\r");
				if(D.numBlessings()>0)
				{
					msg.append("\n\rBlessings: ");
					for(int b=0;b<D.numBlessings();b++)
						msg.append(D.fetchBlessing(b).name()+" ");
					msg.append("\n\r");
					msg.append(D.getWorshipTriggerDesc()+"\n\r");
					msg.append(D.getClericTriggerDesc()+"\n\r");
				}
				if(D.numPowers()>0)
				{
					msg.append("\n\rGranted Powers: ");
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
				msg.append(Util.padRight("^H"+D.name()+"^?",18));
			}
		}
		if(str.length()==0)
			msg.append("\n\r\n\r^xUse DEITIES <NAME> to see important details on each deity!^.^N\n\r");
		mob.tell(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
