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
public class ClanDetails extends BaseClanner
{
	public ClanDetails(){}

	private String[] access={"CLANDETAILS","CLAN"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String qual=Util.combine(commands,1).toUpperCase();
		if(qual.length()==0) qual=mob.getClanID();
		StringBuffer msg=new StringBuffer("");
		if(qual.length()>0)
		{
			boolean found=false;
			for(Enumeration e=Clans.clans();e.hasMoreElements();)
			{
				Clan C=(Clans)e.nextElement();
				if(EnglishParser.containsString(C.ID(), qual))
				{
					msg.append(C.getDetail(mob));
					found=true;
				}
			}
			if(!found)
			{
				msg.append("No clan was found by the name of '"+qual+"'.\n\r");
			}
		}
		else
		{
			msg.append("You need to specify which clan you would like details on. Try 'CLANLIST'.\n\r");
		}
		mob.tell(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
