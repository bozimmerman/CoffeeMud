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
public class WizInv extends StdCommand
{
	public WizInv(){}

	private String[] access={"WIZINVISIBLE","WIZINV","NOWIZINV","VISIBLE","VIS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String str=(String)commands.firstElement();
		if(Character.toUpperCase(str.charAt(0))!='W')
			commands.insertElementAt("OFF",1);
		commands.removeElementAt(0);
		int abilityCode=EnvStats.IS_NOT_SEEN|EnvStats.IS_CLOAKED;
		str="Prop_WizInvis";
		Ability A=mob.fetchEffect(str);
		if((commands.size()>0)&&("NOCLOAK".startsWith(Util.combine(commands,0).trim().toUpperCase())))
		    abilityCode=EnvStats.IS_NOT_SEEN;
		if(Util.combine(commands,0).trim().equalsIgnoreCase("OFF"))
		{
		   if(A!=null)
			   A.unInvoke();
		   else
			   mob.tell("You are not wizinvisible!");
		   return false;
		}
		else
		if(A!=null)
		{
		    if(Util.bset(A.abilityCode(),abilityCode))
		    {
				mob.tell("You have already faded from view!");
				return false;
		    }
		}

		// it worked, so build a copy of this ability,
		// and add it to the affects list of the
		// affected MOB.  Then tell everyone else
		// what happened.
		if(A==null)
			A=CMClass.getAbility(str);
		if(A!=null)
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fade(s) from view!");
			if(mob.fetchEffect(A.ID())==null)
				mob.addEffect((Ability)A.copyOf());
			A=mob.fetchEffect(A.ID());
			if(A!=null) A.setAbilityCode(abilityCode);
			
			mob.recoverEnvStats();
			mob.location().recoverRoomStats();
			mob.tell("You may uninvoke WIZINV with 'WIZINV OFF'.");
			return false;
		}
		else
		{
			mob.tell("Wizard invisibility is not available!");
			return false;
		}
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"WIZINV");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
