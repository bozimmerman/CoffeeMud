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
public class ATopics extends StdCommand
{
	public ATopics(){}

	private String[] access={"ARCTOPICS","ATOPICS"};
	public String[] getAccessWords(){return access;}

	public static void doTopics(MOB mob, Properties rHelpFile, String helpName, String resName)
	{
		StringBuffer topicBuffer=(StringBuffer)Resources.getResource(resName);
		if(topicBuffer==null)
		{
			topicBuffer=new StringBuffer();

			Vector reverseList=new Vector();
			for(Enumeration e=rHelpFile.keys();e.hasMoreElements();)
			{
				String ptop = (String)e.nextElement();
				String thisTag=rHelpFile.getProperty(ptop);
				if ((thisTag==null)||(thisTag.length()==0)||(thisTag.length()>=35)
					|| (rHelpFile.getProperty(thisTag)== null) )
						reverseList.addElement(ptop);
			}

			Collections.sort(reverseList);
			topicBuffer=new StringBuffer("Help topics: \n\r\n\r");
			topicBuffer.append(CMLib.lister().fourColumns(reverseList,"HELP"));
			topicBuffer=new StringBuffer(topicBuffer.toString().replace('_',' '));
			Resources.submitResource(resName,topicBuffer);
		}
		if((mob!=null)&&(!mob.isMonster()))
			mob.session().colorOnlyPrintln(topicBuffer.toString()+"\n\r\n\rEnter "+helpName+" (TOPIC NAME) for more information.",false);
	}


	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		Properties arcHelpFile=CMLib.help().getArcHelpFile();
		if(arcHelpFile.size()==0)
		{
			if(mob!=null)
				mob.tell("No archon help is available.");
			return false;
		}

		doTopics(mob,arcHelpFile,"AHELP", "ARCHON TOPICS");
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"AHELP");}

	
}
