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
public class Possess extends StdCommand
{
	public Possess(){}

	private String[] access={"POSSESS","POSS"};
	public String[] getAccessWords(){return access;}

	public MOB getTarget(MOB mob, Vector commands, boolean quiet)
	{
		String targetName=CMParms.combine(commands,0);
		MOB target=null;
		if(targetName.length()>0)
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName,Wearable.FILTER_UNWORNONLY);
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

		if((target==null)||((!CMLib.flags().canBeSeenBy(target,mob))&&((!CMLib.flags().canBeHeardBy(target,mob))||(!target.isInCombat()))))
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

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(mob.soulMate()!=null)
		{
			mob.tell("You are already possessing someone.  Quit back to your body first!");
			return false;
		}
		commands.removeElementAt(0);
		String MOBname=CMParms.combine(commands,0);
		MOB target=getTarget(mob,commands,true);
		if((target==null)||(!target.isMonster()))
			target=mob.location().fetchInhabitant(MOBname);
		if((target==null)||(!target.isMonster()))
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
		if((target==null)||(!target.isMonster()))
		{
		    try
		    {
		    	Vector inhabs=CMLib.map().findInhabitants(CMLib.map().rooms(), mob,MOBname,100);
				for(Enumeration m=inhabs.elements();m.hasMoreElements();)
				{
					MOB mob2=(MOB)m.nextElement();
					if((mob2.isMonster())&&(CMSecurity.isAllowed(mob,mob2.location(),"POSSESS")))
					{
						target=mob2;
						break;
					}
				}
		    }catch(NoSuchElementException e){}
		}
		if((target==null)||(!target.isMonster())||(!CMLib.flags().isInTheGame(target,true)))
		{
			mob.tell("You can't possess '"+MOBname+"' right now.");
			return false;
		}
		if(!CMSecurity.isAllowed(mob,target.location(),"POSSESS"))
		{
			mob.tell("You can not possess "+target.Name()+".");
			return false;
		}

		if((!CMSecurity.isASysOp(mob))&&(CMSecurity.isASysOp(target)))
		{
			mob.tell("You may not possess '"+MOBname+"'.");
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> get(s) a far away look, then seem(s) to fall limp.");

		Session s=mob.session();
		s.setMob(target);
		target.setSession(s);
		target.setSoulMate(mob);
		mob.setSession(null);
		CMLib.commands().postLook(target,true);
		target.tell("^HYour spirit has changed bodies"
						+(CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS)?" and SECURITY mode is ON":"")
						+", use QUIT to return to yours.");
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"POSSESS");}

	
}
