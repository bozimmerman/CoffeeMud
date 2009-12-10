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
public class Retire extends StdCommand
{
	public Retire(){}

	private String[] access={"RETIRE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		Session session=mob.session();
		if(mob.isMonster()) return false;
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;

		mob.tell("^HThis will delete your player from the system FOREVER!");
		String pwd=session.prompt("If that's what you want, re-enter your password:","",30000);
		if(pwd.length()==0) return false;
		if(!pwd.equalsIgnoreCase(pstats.password()))
		{
			mob.tell("Password incorrect.");
			return false;
		}
		if(!CMSecurity.isDisabled("RETIREREASON"))
		{
			String reason=session.prompt("OK.  Please leave us a short message as to why you are deleting this"
											  +" character.  Your answers will be kept confidential, "
											  +"and are for administrative purposes only.\n\r: ","",120000);
			Log.sysOut("Retire",mob.Name()+" retiring: "+reason);
		}
		CMLib.players().obliteratePlayer(mob,false);
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return false;}

	
}
