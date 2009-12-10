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
public class Deities extends StdCommand
{
	public Deities(){}

	private String[] access={"DEITIES","GODS","DEITY"};
	public String[] getAccessWords(){return access;}

	public String getDeityInformation(MOB mob, Deity D)
	{
		StringBuffer msg = new StringBuffer("");
		msg.append("\n\r^x"+D.name()+"^.^?\n\r");
		msg.append(D.description()+"\n\r\n\r");
        if((mob==null)||(CMSecurity.isASysOp(mob)))
        {
            msg.append(D.getClericRequirementsDesc()+"\n\r\n\r");
            msg.append(D.getWorshipRequirementsDesc()+"\n\r");
        }
        else
        if(mob.charStats().getCurrentClass().baseClass().equals("Cleric"))
			msg.append(D.getClericRequirementsDesc()+"\n\r");
        else
            msg.append(D.getWorshipRequirementsDesc()+"\n\r");
		if(D.numBlessings()>0)
		{
			msg.append("\n\r^HBlessings: ^N");
			for(int b=0;b<D.numBlessings();b++)
			{
				msg.append(D.fetchBlessing(b).name());
                if(D.fetchBlessingCleric(b))
                    msg.append(" (Clerics only)");
				if(b<D.numBlessings()-1)
					msg.append(", ");
			}
			msg.append("\n\r^HBlessing Instructions: ^N");
            if((mob==null)||(CMSecurity.isASysOp(mob)))
            {
                msg.append(D.getClericTriggerDesc()+"\n\r");
                msg.append(D.getWorshipTriggerDesc()+"\n\r");
            }
            else
            if(mob.charStats().getCurrentClass().baseClass().equals("Cleric"))
                msg.append(D.getClericTriggerDesc()+"\n\r");
            else
                msg.append(D.getWorshipTriggerDesc()+"\n\r");
		}
        if((mob==null)||CMSecurity.isASysOp(mob)||mob.charStats().getCurrentClass().baseClass().equals("Cleric"))
        {
			if(D.numPowers()>0)
			{
				msg.append("\n\r^HGranted Powers: ^N");
				for(int b=0;b<D.numPowers();b++)
				{
					msg.append(D.fetchPower(b).name());
					if(b<D.numPowers()-1)
						msg.append(", ");
				}
				msg.append("\n\r^HPowers Instructions: ^N");
				msg.append(D.getClericPowerupDesc()+"\n\r");
			}
			msg.append("\n\r^HService Instructions: ^N");
            msg.append(D.getServiceTriggerDesc()+"\n\r");
        }
        return msg.toString();
	}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if((commands.size()>1)&&(commands.elementAt(1) instanceof Deity))
		{
			Deity D=(Deity)commands.elementAt(1);
			commands.clear();
			commands.addElement(this.getDeityInformation(mob, D));
		}
		
		String str=CMParms.combine(commands,1).toUpperCase();
		StringBuffer msg=new StringBuffer("");
		if(str.length()==0)
			msg.append("\n\r^xThe known deities:^.^? \n\r\n\r");
		else
			msg.append("\n\r^HThe known deities named '"+str+"':^? \n\r");
		int col=0;
		for(Enumeration d=CMLib.map().deities();d.hasMoreElements();)
		{
			Deity D=(Deity)d.nextElement();
			if((str.length()>0)&&(CMLib.english().containsString(D.name(),str)))
				msg.append(this.getDeityInformation(mob, D));
			else
			if(str.length()==0)
			{
				col++;
				if(col>4){ msg.append("\n\r"); col=0;}
				msg.append(CMStrings.padRight("^H"+D.name()+"^?",18));
			}
		}
		if(str.length()==0)
			msg.append("\n\r\n\r^xUse DEITIES <NAME> to see important details on each deity!^.^N\n\r");
		mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
