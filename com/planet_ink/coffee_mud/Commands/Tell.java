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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2014 Bo Zimmerman

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
public class Tell extends StdCommand
{
	public Tell(){}

	private final String[] access=I(new String[]{"TELL","T"});
	@Override public String[] getAccessWords(){return access;}
	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if((!mob.isMonster())&&mob.isAttribute(MOB.Attrib.QUIET))
		{
			mob.tell(L("You have QUIET mode on.  You must turn it off first."));
			return false;
		}

		if(commands.size()<3)
		{
			mob.tell(L("Tell whom what?"));
			return false;
		}
		commands.removeElementAt(0);

		if(((String)commands.firstElement()).equalsIgnoreCase("last")
		   &&(CMath.isNumber(CMParms.combine(commands,1)))
		   &&(mob.playerStats()!=null))
		{
			final java.util.List<String> V=mob.playerStats().getTellStack();
			if((V.size()==0)
			||(CMath.bset(metaFlags,Command.METAFLAG_AS))
			||(CMath.bset(metaFlags,Command.METAFLAG_POSSESSED)))
				mob.tell(L("No telling."));
			else
			{
				int num=CMath.s_int(CMParms.combine(commands,1));
				if(num>V.size()) num=V.size();
				final Session S=mob.session();
				try
				{
					if(S!=null) S.snoopSuspension(1);
					for(int i=V.size()-num;i<V.size();i++)
						mob.tell(V.get(i));
				}
				finally
				{
					if(S!=null) S.snoopSuspension(-1);
				}
			}
			return false;
		}

		MOB targetM=null;
		String targetName=((String)commands.elementAt(0)).toUpperCase();
		targetM=CMLib.sessions().findPlayerOnline(targetName,true);
		if(targetM==null) targetM=CMLib.sessions().findPlayerOnline(targetName,false);
		for(int i=1;i<commands.size();i++)
		{
			final String s=(String)commands.elementAt(i);
			if(s.indexOf(' ')>=0)
				commands.setElementAt("\""+s+"\"",i);
		}
		String combinedCommands=CMParms.combine(commands,1);
		if(combinedCommands.equals(""))
		{
			mob.tell(L("Tell them what?"));
			return false;
		}
		combinedCommands=CMProps.applyINIFilter(combinedCommands,CMProps.Str.SAYFILTER);
		if(targetM==null)
		{
			if(targetName.indexOf('@')>=0)
			{
				final String mudName=targetName.substring(targetName.indexOf('@')+1);
				targetName=targetName.substring(0,targetName.indexOf('@'));
				if(CMLib.intermud().i3online()||CMLib.intermud().imc2online())
					CMLib.intermud().i3tell(mob,targetName,mudName,combinedCommands);
				else
					mob.tell(L("Intermud is unavailable."));
				return false;
			}
			mob.tell(L("That person doesn't appear to be online."));
			return false;
		}

		if(targetM.isAttribute(MOB.Attrib.QUIET))
		{
			mob.tell(L("That person can not hear you."));
			return false;
		}


		final Session ts=targetM.session();
		try
		{
			if(ts!=null) ts.snoopSuspension(1);
			CMLib.commands().postSay(mob,targetM,combinedCommands,true,true);
		}
		finally
		{
			if(ts!=null) ts.snoopSuspension(-1);
		}

		if((targetM.session()!=null)&&(targetM.session().isAfk()))
		{
			mob.tell(targetM.session().getAfkMessage());
		}
		return false;
	}
	// the reason this is not 0ed is because of combat -- we want the players to use SAY, and pay for it when coordinating.
	@Override public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCommandCombatActionCost(ID());}
	@Override public boolean canBeOrdered(){return false;}


}
