package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Snoop extends StdCommand
{
	public Snoop(){}

	private final String[] access={"SNOOP"};
	public String[] getAccessWords(){return access;}
	
	protected List<Session> snoopingOn(Session S)
	{
		List<Session> V=new Vector();
		for(Session S2 : CMLib.sessions().allIterable())
			if(S2.isBeingSnoopedBy(S))
				V.add(S2);
		return V;
	}
	
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(mob.session()==null) return false;
		boolean doneSomething=false;
		for(Session S : CMLib.sessions().allIterable())
			if(S.isBeingSnoopedBy(mob.session()))
			{
				if(S.mob()!=null)
					mob.tell("You stop snooping on "+S.mob().name()+".");
				else
					mob.tell("You stop snooping on someone.");
				doneSomething=true;
				S.setBeingSnoopedBy(mob.session(),false);
			}
		if(commands.size()==0)
		{
			if(!doneSomething)
				mob.tell("Snoop on whom?");
			return false;
		}
		String whom=CMParms.combine(commands,0);
		Session SnoopOn=null;
		Session S=CMLib.sessions().findPlayerSessionOnline(whom,false);
		if(S!=null)
		{
			if(S==mob.session())
			{
				mob.tell("no.");
				return false;
			}
			else
			if(CMSecurity.isAllowed(mob,S.mob().location(),CMSecurity.SecFlag.SNOOP))
				SnoopOn=S;
		}
		if(SnoopOn==null)
			mob.tell("You can't find anyone to snoop on by that name.");
		else
		if(!CMLib.flags().isInTheGame(SnoopOn.mob(),true))
			mob.tell(SnoopOn.mob().Name()+" is not yet fully in the game.");
		else
		if(CMSecurity.isASysOp(SnoopOn.mob())&&(!CMSecurity.isASysOp(mob)))
			mob.tell("Only another Archon can snoop on "+SnoopOn.mob().name()+".");
		else
		{
			Vector snoop=new Vector();
			snoop.addElement(SnoopOn);
			for(int v=0;v<snoop.size();v++)
			{
				if(snoop.elementAt(v)==mob.session())
				{
					mob.tell("This would create a snoop loop!");
					return false;
				}
				List<Session> V=snoopingOn((Session)snoop.elementAt(v));
				for(int v2=0;v2<V.size();v2++)
				{
					Session S2=V.get(v2);
					if(!snoop.contains(S2))
						snoop.addElement(S2);
				}
			}
			mob.tell("You start snooping on "+SnoopOn.mob().name()+".");
			SnoopOn.setBeingSnoopedBy(mob.session(), true);
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.SNOOP);}

	
}
