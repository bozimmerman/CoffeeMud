package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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
public class WizEmote extends StdCommand
{
	public WizEmote(){}

	private String[] access={"WIZEMOTE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()>2)
		{
			String who=(String)commands.elementAt(1);
			String msg=Util.combine(commands,2);
            if(who.toUpperCase().equals("HERE"))
            {
                for(int s=0;s<Sessions.size();s++)
                {
                    Session S=Sessions.elementAt(s);
                    if((S.mob()!=null)
                    &&(S.mob().location()==mob.location())
                    &&(CMSecurity.isAllowed(mob,S.mob().location(),"WIZEMOTE")))
                        S.stdPrintln("^w"+msg+"^?");
                }
            }
            else
            if(CMMap.getRoom(who)!=null)
            {
                Room R=CMMap.getRoom(who);
                for(int s=0;s<Sessions.size();s++)
                {
                    Session S=Sessions.elementAt(s);
                    if((S.mob()!=null)
                    &&(S.mob().location()==R)
                    &&(CMSecurity.isAllowed(mob,R,"WIZEMOTE")))
                        S.stdPrintln("^w"+msg+"^?");
                }
            }
            else
			if(who.toUpperCase().equals("ALL"))
			{
				for(int s=0;s<Sessions.size();s++)
				{
					Session S=Sessions.elementAt(s);
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(CMSecurity.isAllowed(mob,S.mob().location(),"WIZEMOTE")))
	  					S.stdPrintln("^w"+msg+"^?");
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
					&&(CMSecurity.isAllowed(mob,S.mob().location(),"WIZEMOTE"))
					&&(EnglishParser.containsString(S.mob().name(),who)
						||EnglishParser.containsString(S.mob().location().getArea().name(),who)))
					{
	  					S.stdPrintln("^w"+msg+"^?");
						found=true;
						break;
					}
				}
				if(!found)
					mob.tell("You can't find anyone or anywhere by that name.");
			}
	    }
	    else
			mob.tell("You must specify either all, or an area/mob name, and an message.");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"WIZEMOTE");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
