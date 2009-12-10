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
public class Areas extends StdCommand
{
	public Areas(){}

	private String[] access={"AREAS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("^HComplete areas list:^?^N\n\r");
		String expression=null;
		if(commands.size()>1)
		{
			expression=CMParms.combineWithQuotes(commands,1);
			msg=new StringBuffer("^HFiltered areas list:^?^N\n\r");
		}
		Vector areasVec=new Vector();
        boolean sysop=(mob!=null)&&CMSecurity.isASysOp(mob);
		for(Enumeration a=CMLib.map().sortedAreas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			if(CMLib.flags().canAccess(mob,A)&&(!CMath.bset(A.flags(),Area.FLAG_INSTANCE_CHILD)))
            {
                String name=(!CMLib.flags().isHidden(A))?" "+A.name():"("+A.name()+")";
                if(sysop)
                switch(A.getAreaState())
                {
                case Area.STATE_ACTIVE: name="^w"+name+"^?";break;
                case Area.STATE_PASSIVE: name="^W"+name+"^?"; break;
                case Area.STATE_FROZEN: name="^b"+name+"^?"; break;
                case Area.STATE_STOPPED: name="^r"+name+"^?"; break;
                }
                if(expression!=null)
                {
                	int[] stats=A.getAreaIStats();
                	if(stats!=null)
                	{
                    	Hashtable H=new Hashtable();
	                	for(int i=0;i<stats.length;i++)
	                		H.put(Area.AREASTAT_DESCS[i],Integer.toString(stats[i]));
	                	try {
	                		if(!CMStrings.parseStringExpression(expression, H,false))
	                			continue;
	                	}catch(Exception e){
	                		if(mob!=null)
		                		mob.tell("There was an error in your AREA qualifier parameters. See help on AREA for more information. The error was: "+e.getMessage());
	                		return false;
	                	}
                	}
                }
				areasVec.addElement(name);
            }
		}
		int col=0;
		for(int i=0;i<areasVec.size();i++)
		{
			if((++col)>3)
			{
				msg.append("\n\r");
				col=1;
			}
			msg.append(CMStrings.padRight((String)areasVec.elementAt(i),22)+"^N");
		}
		msg.append("\n\r\n\r^HEnter 'HELP (AREA NAME) for more information.^?");
		if((mob!=null)&&(!mob.isMonster()))
			mob.session().colorOnlyPrintln(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
