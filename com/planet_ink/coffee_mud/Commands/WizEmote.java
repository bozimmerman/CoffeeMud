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
public class WizEmote extends StdCommand
{
	public WizEmote(){}

	private String[] access={"WIZEMOTE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()>2)
		{
			String who=(String)commands.elementAt(1);
			String msg=CMParms.combineWithQuotes(commands,2);
			Room R=CMLib.map().getRoom(who);
			if(who.toUpperCase().equals("HERE")) R=mob.location();
			Area A=CMLib.map().findAreaStartsWith(who);
			Clan C=CMLib.clans().findClan(who);
			if(who.toUpperCase().equals("ALL"))
			{
				for(int s=0;s<CMLib.sessions().size();s++)
				{
					Session S=CMLib.sessions().elementAt(s);
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(CMSecurity.isAllowed(mob,S.mob().location(),"WIZEMOTE")))
	  					S.stdPrintln("^w"+msg+"^?");
				}
			}
			else
            if(R!=null)
            {
                for(int s=0;s<CMLib.sessions().size();s++)
                {
                    Session S=CMLib.sessions().elementAt(s);
                    if((S.mob()!=null)
                    &&(S.mob().location()==R)
                    &&(CMSecurity.isAllowed(mob,R,"WIZEMOTE")))
                        S.stdPrintln("^w"+msg+"^?");
                }
            }
            else
            if(A!=null)
            {
                for(int s=0;s<CMLib.sessions().size();s++)
                {
                    Session S=CMLib.sessions().elementAt(s);
                    if((S.mob()!=null)
                    &&(S.mob().location()!=null)
                    &&(A.inMyMetroArea(S.mob().location().getArea()))
                    &&(CMSecurity.isAllowed(mob,S.mob().location(),"WIZEMOTE")))
                        S.stdPrintln("^w"+msg+"^?");
                }
            }
            else
            if(C!=null)
            {
                for(int s=0;s<CMLib.sessions().size();s++)
                {
                    Session S=CMLib.sessions().elementAt(s);
                    if((S.mob()!=null)
                    &&(S.mob().getClanID().equals(C.clanID()))
                    &&(CMSecurity.isAllowed(mob,S.mob().location(),"WIZEMOTE")))
                        S.stdPrintln("^w"+msg+"^?");
                }
            }
            else
			{
				boolean found=false;
				for(int s=0;s<CMLib.sessions().size();s++)
				{
					Session S=CMLib.sessions().elementAt(s);
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(CMSecurity.isAllowed(mob,S.mob().location(),"WIZEMOTE"))
					&&(CMLib.english().containsString(S.mob().name(),who)
						||CMLib.english().containsString(S.mob().location().getArea().name(),who)))
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
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"WIZEMOTE");}

	
}
