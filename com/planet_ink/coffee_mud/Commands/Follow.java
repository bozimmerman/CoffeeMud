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
public class Follow extends StdCommand
{
	public Follow(){}

	private String[] access={"FOLLOW","FOL","FO","F"};
	public String[] getAccessWords(){return access;}


	public boolean nofollow(MOB mob, boolean errorsOk, boolean quiet)
	{
		if(mob.amFollowing()!=null)
		{
			FullMsg msg=new FullMsg(mob,mob.amFollowing(),null,CMMsg.MSG_NOFOLLOW,quiet?null:"<S-NAME> stop(s) following <T-NAMESELF>.");
			// no room OKaffects, since the damn leader may not be here.
			if(mob.okMessage(mob,msg))
				mob.location().send(mob,msg);
			else
				return false;
		}
		else
		if(errorsOk)
			mob.tell("You aren't following anyone!");
		return true;
	}

	public void unfollow(MOB mob, boolean quiet)
	{
		nofollow(mob,false,quiet);
		Vector V=new Vector();
		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB F=mob.fetchFollower(f);
			if(F!=null) V.addElement(F);
		}
		for(int v=0;v<V.size();v++)
		{
			MOB F=(MOB)V.elementAt(v);
			nofollow(F,false,quiet);
		}
	}


	public boolean processFollow(MOB mob, MOB tofollow, boolean quiet)
	{
		if(tofollow!=null)
		{
			if(tofollow==mob)
			{
				return nofollow(mob,true,false);
			}
			if(mob.getGroupMembers(new HashSet()).contains(tofollow))
			{
				if(!quiet)
					mob.tell("You are already a member of "+tofollow.name()+"'s group!");
				return false;
			}
			if(nofollow(mob,false,false))
			{
				FullMsg msg=new FullMsg(mob,tofollow,null,CMMsg.MSG_FOLLOW,quiet?null:"<S-NAME> follow(s) <T-NAMESELF>.");
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
				else
					return false;
			}
			else
				return false;
		}
		else
			return nofollow(mob,!quiet,quiet);
		return true;
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		boolean quiet=false;

		if((commands.size()>2)
		&&(commands.lastElement() instanceof String)
		&&(((String)commands.lastElement()).equalsIgnoreCase("UNOBTRUSIVELY")))
		{
			commands.removeElementAt(commands.size()-1);
			quiet=true;
		}
		if((commands.size()>1)&&(commands.elementAt(1) instanceof MOB))
			return processFollow(mob,(MOB)commands.elementAt(1),quiet);

		if(commands.size()<2)
		{
			mob.tell("Follow whom?");
			return false;
		}

		String whomToFollow=Util.combine(commands,1);
		if((whomToFollow.equalsIgnoreCase("self")||whomToFollow.equalsIgnoreCase("me"))
		   ||(mob.name().toUpperCase().startsWith(whomToFollow)))
		{
			nofollow(mob,true,quiet);
			return false;
		}
		MOB target=mob.location().fetchInhabitant(whomToFollow);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("I don't see them here.");
			return false;
		}
		if((target.isMonster())&&(!mob.isMonster()))
		{
			mob.tell("You cannot follow '"+target.name()+"'.");
			return false;
		}
		if(Util.bset(target.getBitmap(),MOB.ATT_NOFOLLOW))
		{
			mob.tell(target.name()+" is not accepting followers.");
			return false;
		}
		processFollow(mob,target,quiet);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
