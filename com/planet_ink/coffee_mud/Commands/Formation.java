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
   Copyright 2004-2024 Bo Zimmerman

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
public class Formation extends StdCommand
{
	public Formation()
	{
	}

	private final String[] access=I(new String[]{"FORMATION"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final Vector<String> origCmds=new XVector<String>(commands);
		commands.remove(0);
		final MOB leader=mob.getGroupLeader();
		final List<MOB>[] done=CMLib.combat().getFormation(mob);
		if(commands.size()==0)
		{
			final StringBuffer str=new StringBuffer("");
			for(int i=0;i<done.length;i++)
			{
				if(done[i]!=null)
				{
					if(i==0)
						str.append(L("^xfront  - ^.^?"));
					else
						str.append(L("^xrow +@x1 - ^.^?",""+i));
					for(int i2=0;i2<done[i].size();i2++)
						str.append(((i2>0)?", ":"")+done[i].get(i2).name());
					str.append("\n\r");
				}
			}
			mob.session().colorOnlyPrintln(str.toString());
		}
		else
		if(commands.size()==1)
			CMLib.commands().postCommandFail(mob,origCmds,L("Put whom in what row?"));
		else
		if(mob.numFollowers()==0)
			CMLib.commands().postCommandFail(mob,origCmds,L("No one is following you!"));
		else
		{
			String row=commands.get(commands.size()-1);
			if("FRONT".startsWith(row.toUpperCase()))
				row="0";
			commands.remove(commands.size()-1);
			final String name=CMParms.combine(commands,0);
			MOB who=null;
			if(CMLib.english().containsString(mob.name(),name)
			   ||CMLib.english().containsString(mob.Name(),name))
				who=mob;
			else
			{
				for(int f=0;f<mob.numFollowers();f++)
				{
					final MOB M=mob.fetchFollower(f);
					if(M==null)
						continue;
					if(CMLib.english().containsString(M.name(),name)
					   ||CMLib.english().containsString(M.Name(),name))
					{
						who=M;
						break;
					}
				}
			}
			if(who==null)
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("There is no one following you called @x1.",name));
				return false;
			}
			if((!CMath.isNumber(row))||(CMath.s_int(row)<0))
				CMLib.commands().postCommandFail(mob,origCmds,L("'@x1' is not a valid row in which to put @x2.  Try number greater than or equal to 0.",row,who.name()));
			else
			{
				int leaderRow=-1;
				for(int f=0;f<done.length;f++)
				{
					if((done[f]!=null)&&(done[f].contains(leader)))
					{
						leaderRow=f;
						break;
					}
				}
				if(leaderRow<0)
					CMLib.commands().postCommandFail(mob,origCmds,L("You do not exist."));
				else
				{
					mob.addFollower(who,CMath.s_int(row));
					mob.tell(L("You have positioned @x1 to row @x2",who.name(),""+CMath.s_int(row)));
				}
			}
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
