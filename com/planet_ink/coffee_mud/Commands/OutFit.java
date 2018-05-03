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
   Copyright 2004-2018 Bo Zimmerman

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

public class OutFit extends StdCommand
{
	public OutFit(){}

	private final String[] access=I(new String[]{"OUTFIT"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean preExecute(MOB mob, List<String> commands, int metaFlags, int secondsElapsed, double actionsRemaining)
	throws java.io.IOException
	{
		if(secondsElapsed>8.0)
			mob.tell(L("You feel your outfit plea is almost answered."));
		else
		if(secondsElapsed>4.0)
			mob.tell(L("Your plea swirls around you."));
		else
		if(actionsRemaining>0.0)
			mob.tell(L("You invoke a plea for mystical outfitting and await the answer."));
		return true;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if(mob==null)
			return false;
		if(mob.charStats()==null)
			return false;
		final CharClass C=mob.charStats().getCurrentClass();
		final Race R=mob.charStats().getMyRace();
		if(C!=null)
			CMLib.utensils().outfit(mob,C.outfit(mob));
		if(R!=null)
			CMLib.utensils().outfit(mob,R.outfit(mob));
		mob.tell(L("\n\r"));
		final Command C2=CMClass.getCommand("Equipment");
		if(C2!=null)
			C2.executeInternal(mob, metaFlags);
		mob.tell(L("\n\rUseful equipment appears mysteriously out of the Java Plane."));
		mob.recoverCharStats();
		mob.recoverMaxState();
		mob.recoverPhyStats();
		return false;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID(),CMath.div(CMProps.getIntVar(CMProps.Int.DEFCOMCMDTIME),25.0));
	}

	@Override
	public double actionsCost(MOB mob, List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID(),CMath.div(CMProps.getIntVar(CMProps.Int.DEFCMDTIME),25.0));
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
