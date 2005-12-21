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
   Copyright 2000-2005 Bo Zimmerman

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
public class Areas extends StdCommand
{
	public Areas(){}

	private String[] access={getScr("Areas","cmd")};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		Vector areasVec=new Vector();
		for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			if(CMLib.flags().canAccess(mob,A))
				if(!CMLib.flags().isHidden(A))
					areasVec.addElement(" "+A.name());
				else
					areasVec.addElement("("+A.name()+")");
		}
		Collections.sort(areasVec);
		StringBuffer msg=new StringBuffer(getScr("Areas","cal"));
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
		msg.append(getScr("Areas","help"));
		if(!mob.isMonster())
			mob.session().colorOnlyPrintln(msg.toString());
		return false;
	}
	public double actionsCost(){return 0.0;}
	public boolean canBeOrdered(){return true;}

	
}
