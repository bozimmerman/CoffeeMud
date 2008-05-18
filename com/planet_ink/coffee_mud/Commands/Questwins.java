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
   Copyright 2000-2008 Bo Zimmerman

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
public class Questwins extends StdCommand
{
	public Questwins(){}

	private String[] access={"QUESTWINS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		Vector qVec=new Vector();
		for(int q=0;q<CMLib.quests().numQuests();q++)
		{
			Quest Q=CMLib.quests().fetchQuest(q);
			if(Q.wasWinner(mob.Name()))
				qVec.addElement(Q.name());
		}
		Collections.sort(qVec);
		StringBuffer msg=new StringBuffer("^HQuests you are listed as having won:^?^N\n\r");
		for(int i=0;i<qVec.size();i++)
			msg.append(CMStrings.padRight((String)qVec.elementAt(i),22)+"^N"+((i<qVec.size()-1)?", ":""));
		if(!mob.isMonster())
			mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
