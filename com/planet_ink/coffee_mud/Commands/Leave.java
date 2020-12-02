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
   Copyright 2018-2020 Bo Zimmerman

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
public class Leave extends StdCommand
{
	public Leave()
	{
	}

	private final String[] access=I(new String[]{"LEAVE"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final String cmdWord = commands.size()> 0 ? commands.get(0): "";
		final Vector<String> origCmds=new XVector<String>(commands);
		commands.remove(0);
		final Rideable riding=mob.riding();
		if(riding==null)
		{
			if(cmdWord.toUpperCase().startsWith("L"))
			{
				if(commands.size()>0)
				{
					final List<String> subCommands=new XVector<String>(commands);
					final CMObject O = CMLib.english().findCommand(mob, commands);
					if((O instanceof Command)
					&&(CMLib.directions().getDirectionCode(((Command)O).name())>=0))
						return ((Command)O).execute(mob, subCommands, metaFlags);
				}
				else
				{
					final Room R=mob.location();
					if(R!=null)
					{
						int dirToGo=-1;
						for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
						{
							final Room nR=R.getRoomInDir(d);
							final Exit nE=R.getExitInDir(d);
							if((nR!=null)
							&&(nE!=null)
							&&(CMLib.flags().canBeSeenBy(nE, mob))
							&&(nE.isOpen())
							&&((nR.getGridParent() == null) || (nR.getGridParent().roomID().length() > 0)))
							{
								if(dirToGo<0)
									dirToGo=d;
								else
								{
									dirToGo=-1;
									break;
								}
							}
						}
						if(dirToGo>=0)
						{
							final Command C=CMClass.getCommand(CMLib.directions().getDirectionName(dirToGo));
							if(C!=null)
							{
								commands.add(C.getAccessWords()[0]);
								return C.execute(mob, commands, metaFlags);
							}
						}
					}
				}
				CMLib.commands().postCommandFail(mob,origCmds,L("Which way? Try EXITS."));
				return false;
			}
			else
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("But you aren't inside anything?!"));
				return false;
			}
		}
		final Room R=mob.location();
		if(R!=null)
		{
			final CMMsg msg=CMClass.getMsg(mob,riding,null,CMMsg.MSG_DISMOUNT,L("<S-NAME> @x1 <T-NAMESELF>.",riding.dismountString(mob)));
			if(R.okMessage(mob,msg))
				R.send(mob,msg);
		}
		return false;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID());
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID());
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
