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
public class Possess extends StdCommand
{
	public Possess(){}

	private String[] access={"POSSESS","POSS"};
	public String[] getAccessWords(){return access;}

	public MOB getTarget(MOB mob, Vector commands, boolean quiet)
	{
		String targetName=Util.combine(commands,0);
		MOB target=null;
		if(targetName.length()>0)
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName,Item.WORN_REQ_UNWORNONLY);
				if((t!=null)&&(!(t instanceof MOB)))
				{
					if(!quiet)
						mob.tell(mob,t,null,"You can't do that to <T-NAMESELF>.");
					return null;
				}
			}
		}

		if(target!=null)
			targetName=target.name();

		if((target==null)||((!Sense.canBeSeenBy(target,mob))&&((!Sense.canBeHeardBy(target,mob))||(!target.isInCombat()))))
		{
			if(!quiet)
			{
				if(targetName.trim().length()==0)
					mob.tell("You don't see them here.");
				else
					mob.tell("You don't see '"+targetName+"' here.");
			}
			return null;
		}

		return target;
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(mob.soulMate()!=null)
		{
			mob.tell("You are already possessing someone.  Quit back to your body first!");
			return false;
		}
		commands.removeElementAt(0);
		String MOBname=Util.combine(commands,0);
		MOB target=getTarget(mob,commands,true);
		if((target==null)||((target!=null)&&(!target.isMonster())))
			target=mob.location().fetchInhabitant(MOBname);
		if((target==null)||((target!=null)&&(!target.isMonster())))
		{
			Enumeration r=mob.location().getArea().getProperMap();
			for(;r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				MOB mob2=R.fetchInhabitant(MOBname);
				if((mob2!=null)&&(mob2.isMonster()))
				{
					target=mob2;
					break;
				}
			}
		}
		if((target==null)||((target!=null)&&(!target.isMonster())))
		{
			Enumeration r=CMMap.rooms();
			for(;r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				MOB mob2=R.fetchInhabitant(MOBname);
				if((mob2!=null)&&(mob2.isMonster())&&(CMSecurity.isAllowed(mob,R,"POSSESS")))
				{
					target=mob2;
					break;
				}
			}
		}
		if((target==null)||(!target.isMonster()))
		{
			mob.tell("You can't possess '"+MOBname+"' right now.");
			return false;
		}
		if(!CMSecurity.isAllowed(mob,target.location(),"POSSESS"))
		{
			mob.tell("You can not possess "+target.Name()+".");
			return false;
		}

		mob.location().showOthers(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> get(s) a far away look, then seem(s) to fall limp.");

		Session s=mob.session();
		s.setMob(target);
		target.setSession(s);
		target.setSoulMate(mob);
		mob.setSession(null);
		CommonMsgs.look(target,true);
		target.tell("^HYour spirit has changed bodies...");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"POSSESS");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
