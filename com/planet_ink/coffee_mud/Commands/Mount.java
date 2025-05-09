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
   Copyright 2004-2025 Bo Zimmerman

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
public class Mount extends StdCommand
{
	public Mount()
	{
	}

	private final String[]	access	= I(new String[] { "MOUNT", "BOARD", "RIDE", "M" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final List<String> origCmds=new XVector<String>(commands);
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("@x1 what?",(commands.get(0))));
			return false;
		}
		final String cmd=commands.remove(0).toString();
		Environmental recipient=null;
		final List<Rideable> possRecipients=new ArrayList<Rideable>();
		for(int m=0;m<mob.location().numInhabitants();m++)
		{
			final MOB M=mob.location().fetchInhabitant(m);
			if((M!=null)&&(M instanceof Rideable))
				possRecipients.add((Rideable)M);
		}
		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I=mob.location().getItem(i);
			if((I!=null)&&(I instanceof Rideable))
				possRecipients.add((Rideable)I);
		}
		Rider RI=null;
		if(commands.size()>1)
		{
			final Item I=mob.location().findItem(null,commands.get(0));
			if(I!=null)
			{
				commands.remove(0);
				// this caused re-mounting to occur as if the item was never mounted to anything to begin with.
				//I.setRiding(null); // why do you get to dismount without a message every time you specify an item to mount?
				RI=I;
			}
			if(RI==null)
			{
				final MOB M=getVisibleRoomTarget(mob,commands.get(0));
				if(M!=null)
				{
					if(!CMLib.flags().canBeSeenBy(M,mob))
					{
						CMLib.commands().postCommandFail(mob,origCmds,L("You don't see @x1 here.",(commands.get(0))));
						return false;
					}
					if(((!CMLib.flags().isBoundOrHeld(M))&&(!M.willFollowOrdersOf(mob)))
					&&(M != mob))
					{
						CMLib.commands().postCommandFail(mob,origCmds,L("Only the bound or servants can be mounted unwillingly."));
						return false;
					}
					RI=M;
					RI.setRiding(null);
					commands.remove(0);
				}
			}
		}
		if(commands.get(0).equalsIgnoreCase("self")
		&&(mob instanceof Rideable))
			recipient=mob;
		if(recipient==null)
			recipient=CMLib.english().fetchEnvironmental(possRecipients,CMParms.combine(commands,0),true);
		if(recipient==null)
			recipient=CMLib.english().fetchEnvironmental(possRecipients,CMParms.combine(commands,0),false);
		if(recipient==null)
			recipient=mob.location().fetchFromRoomFavorMOBs(null,CMParms.combine(commands,0));
		if((recipient==null)||(!CMLib.flags().canBeSeenBy(recipient,mob)))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't see '@x1' here.",CMParms.combine(commands,0)));
			return false;
		}
		if((recipient instanceof Boardable)
		&&(cmd.toUpperCase().startsWith("B")))
		{
			final Command C=CMClass.getCommand("Enter");
			if(C!=null)
			{
				commands=new XVector<String>(origCmds);
				commands.set(0,"ENTER");
				return C.execute(mob, commands, metaFlags);
			}
		}

		String mountStr=null;
		if(recipient instanceof Rideable)
		{
			if(RI!=null)
				mountStr=L("<S-NAME> mount(s) <O-NAME> onto <T-NAMESELF>.");
			else
				mountStr="<S-NAME> "+((Rideable)recipient).mountString(CMMsg.TYP_MOUNT,mob)+" <T-NAMESELF>.";
		}
		else
		{
			if(RI!=null)
				mountStr=L("<S-NAME> mount(s) <O-NAME> to <T-NAMESELF>.");
			else
				mountStr=L("<S-NAME> mount(s) <T-NAMESELF>.");
		}
		final CMMsg msg=CMClass.getMsg(mob,recipient,RI,CMMsg.MSG_MOUNT,mountStr);
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		else
			CMLib.commands().postCommandRejection(msg.source(), msg.target(), msg.tool(), origCmds);
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
