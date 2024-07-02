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
public class Stand extends StdCommand
{
	public Stand()
	{
	}

	private final String[] access=I(new String[]{"STAND","ST","STA","STAN"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final List<String> origCmds = new XVector<String>(commands);
		boolean ifnecessary=false;
		boolean quietly=(CMath.bset(metaFlags, MUDCmdProcessor.METAFLAG_QUIETLY));
		for(int i=1;i<commands.size();i++)
		{
			final String s=commands.get(i).toUpperCase();
			ifnecessary = ifnecessary || s.equals("IFNECESSARY");
			quietly = quietly || s.equals("QUIETLY");
		}
		final Room room = CMLib.map().roomLocation(mob);
		if(CMLib.flags().isStanding(mob))
		{
			if(!ifnecessary)
				CMLib.commands().postCommandFail(mob,new StringXVector(commands),L("You are already standing!"));
		}
		else
		if((mob.session()!=null)&&(mob.session().isStopped()))
			CMLib.commands().postCommandFail(mob,new StringXVector(commands),L("You may not stand up."));
		else
		if(room!=null)
		{
			final String standMsg;
			if(quietly || mob.amDead())
				standMsg = null;
			else
			if(CMLib.flags().isFlying(mob) && CMLib.flags().isSleeping(mob))
				standMsg = L("<S-NAME> wake(s) up.");
			else
				standMsg = L("<S-NAME> stand(s) up.");

			final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_STAND,standMsg);
			if(room.okMessage(mob,msg))
				room.send(mob,msg);
			else
				CMLib.commands().postCommandRejection(msg.source(),msg.target(),msg.tool(),origCmds);
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
