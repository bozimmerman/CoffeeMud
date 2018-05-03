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

public class DrinkCmd extends StdCommand
{
	public DrinkCmd()
	{
	}

	private final String[]	access	= I(new String[] { "DRINK", "DR", "DRI" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		Vector<String> origCmds=new XVector<String>(commands);
		if((commands.size()<2)&&(!(mob.location() instanceof Drink)))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Drink what?"));
			return false;
		}
		commands.remove(0);
		if((commands.size()>1)&&(commands.get(0).equalsIgnoreCase("from")))
			commands.remove(0);

		Environmental thisThang=null;
		if((commands.size()==0)&&(mob.location() instanceof Drink))
			thisThang=mob.location();
		else
		{
			String drinkWhat = CMParms.combine(commands,0);
			thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,drinkWhat,Wearable.FILTER_ANY);
			if((thisThang == null)&&(drinkWhat.equalsIgnoreCase("here")||drinkWhat.equalsIgnoreCase("room")))
				thisThang=mob.location();
			if((thisThang==null)
			||((!mob.isMine(thisThang))
			   &&(!CMLib.flags().canBeSeenBy(thisThang,mob))))
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("You don't see '@x1' here.",CMParms.combine(commands,0)));
				return false;
			}
		}
		String str=L("<S-NAME> take(s) a drink from <T-NAMESELF>.");
		Environmental tool=null;
		if((thisThang instanceof Drink)
		&&(((Drink)thisThang).liquidRemaining()>0)
		&&(((Drink)thisThang).liquidType()!=RawMaterial.RESOURCE_FRESHWATER))
			str=L("<S-NAME> take(s) a drink of @x1 from <T-NAMESELF>.",RawMaterial.CODES.NAME(((Drink)thisThang).liquidType()).toLowerCase());
		else
		if(thisThang instanceof Container)
		{
			final List<Item> V=((Container)thisThang).getContents();
			for(int v=0;v<V.size();v++)
			{
				final Item I=V.get(v);
				if((I instanceof Drink)&&(I instanceof RawMaterial))
				{
					tool=thisThang;
					thisThang=I;
					str=L("<S-NAME> take(s) a drink of <T-NAMESELF> from <O-NAMESELF>.");
					break;
				}
			}
		}
		final CMMsg newMsg=CMClass.getMsg(mob,thisThang,tool,CMMsg.MSG_DRINK,str+CMLib.protocol().msp("drink.wav",10));
		if(mob.location().okMessage(mob,newMsg))
			mob.location().send(mob,newMsg);
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
