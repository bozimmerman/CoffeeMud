package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/*
* <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
* <p>Portions Copyright (c) 2004 Bo Zimmerman</p>

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
public class Announce extends StdCommand
{
	public Announce(){}

	private String[] access={getScr("Announce","cmd")};
	public String[] getAccessWords(){return access;}

	public void sendAnnounce(String announcement, Session S)
	{
	  	StringBuffer Message=new StringBuffer("");
	  	int alignType=2;
        if (Sense.isEvil(S.mob()))
            alignType = 0;
        else
        if (Sense.isGood(S.mob()))
            alignType = 1;
	  	switch(alignType)
	  	{
	  	  case 0:
	  	    Message.append(getScr("Announce","evil",announcement));
	  	    break;
	  	  case 1:
	  	    Message.append(getScr("Announce","good",announcement));
	  	    break;
	  	  case 2:
	  	    Message.append(getScr("Announce","neutral",announcement));
	  	    break;
	  	}
	  	S.stdPrintln(Message.toString());
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()>1)
		{
			if(((String)commands.elementAt(1)).toUpperCase().equals(getScr("Announce","all")))
			{
				for(int s=0;s<Sessions.size();s++)
				{
					Session S=Sessions.elementAt(s);
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(CMSecurity.isAllowed(mob,S.mob().location(),"ANNOUNCE")))
						sendAnnounce(Util.combine(commands,2),S);
				}
			}
			else
			{
				boolean found=false;
				for(int s=0;s<Sessions.size();s++)
				{
					Session S=Sessions.elementAt(s);
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(CMSecurity.isAllowed(mob,S.mob().location(),"ANNOUNCE"))
					&&(EnglishParser.containsString(S.mob().name(),(String)commands.elementAt(1))))
					{
						sendAnnounce(Util.combine(commands,2),S);
						found=true;
						break;
					}
				}
				if(!found)
					mob.tell(getScr("Announce","notfound"));
			}
		}
		else
			mob.tell(getScr("Announce","exp"));
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"ANNOUNCE");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
