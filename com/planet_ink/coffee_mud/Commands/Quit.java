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
public class Quit extends StdCommand
{
	public Quit(){}

	private String[] access={"QUIT","QUI","Q"};
	public String[] getAccessWords(){return access;}

	public static void dispossess(MOB mob)
	{
		if(mob.soulMate()==null)
		{
			mob.tell("Huh?");
			return;
		}
        mob.dispossess(true);
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(mob.soulMate()!=null)
			dispossess(mob);
		else
		if(!mob.isMonster())
		{
			Session session=mob.session();
            if((session!=null)
            &&(session.getLastPKFight()>0)
            &&((System.currentTimeMillis()-session.getLastPKFight())<(5*60*1000)))
            {
                mob.tell("You must wait a few more minutes before you are allowed to quit.");
                return false;
            }
            if((session!=null)&&(mob.getAgeHours()<=0)&&(!CMSecurity.isDisabled("QUITREASON")))
            {
        		String reason=session.prompt("Since your character is brand new, please leave a short"
        				 						  +" message as to why you are leaving so soon."
												  +" Your answers will be kept confidential,"
												  +" and are for administrative purposes only.\n\r: ","",120000);
        		Log.sysOut("Quit",mob.Name()+" L.W.O.: "+reason);
            }
			try
			{
				if ((session!=null)&&(session.confirm("\n\rQuit -- are you sure (y/N)?","N")))
				{
		            CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_QUIT,null);
		            Room R=mob.location();
	                if((R!=null)&&(R.okMessage(mob,msg))) 
	                {
	                    CMLib.map().sendGlobalMessage(mob,CMMsg.TYP_QUIT, msg);
	                    session.kill(false,false, false);
	                }
				}
			}
			catch(Exception e)
			{
				if(mob.session()!=null)
					mob.session().kill(false,false,false);
			}
		}
		return false;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
