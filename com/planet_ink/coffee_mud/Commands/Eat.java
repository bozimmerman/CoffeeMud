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

public class Eat extends StdCommand
{
	public Eat(){}

	private final String[] access=I(new String[]{"EAT"});
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
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Eat what?"));
			return false;
		}
		commands.remove(0);
		
		Item eatFromThis=null;
		int fromDex=-1;
		for(int i=commands.size()-2;i>=1;i--)
		{
			if(commands.get(i).equalsIgnoreCase("from"))
			{
				fromDex=i;
				commands.remove(i);
			}
		}
		if(fromDex>0)
		{
			final String thingToEatFrom=CMParms.combine(commands,fromDex);
			Physical maybeEatFromThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,thingToEatFrom,Wearable.FILTER_ANY);
			if((maybeEatFromThis==null)
			||(!CMLib.flags().canBeSeenBy(maybeEatFromThis,mob)))
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("I don't see @x1 here.",thingToEatFrom));
				return false;
			}
			if(maybeEatFromThis instanceof Container)
			{
				while(commands.size()>=(fromDex+1))
					commands.remove(commands.size()-1);
				eatFromThis=(Item)maybeEatFromThis;
				if(!((Container)eatFromThis).isOpen())
				{
					CMLib.commands().postCommandFail(mob,origCmds,L("@x1 isn't open.",eatFromThis.name()));
					return false;
				}
			}
		}
		
		Environmental thisThang=null;
		thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,eatFromThis,CMParms.combine(commands,0),Wearable.FILTER_ANY);
		if((thisThang==null)
		||(!CMLib.flags().canBeSeenBy(thisThang,mob)))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't see '@x1' here.",CMParms.combine(commands,0)));
			return false;
		}
		final boolean hasHands=mob.charStats().getBodyPart(Race.BODY_HAND)>0;
		if((thisThang instanceof Food)&&(!mob.isMine(thisThang))&&(hasHands))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't seem to have '@x1'.",CMParms.combine(commands,0)));
			return false;
		}
		final String eatSound=CMLib.protocol().msp("gulp.wav",10);
		final String eatMsg;
		if(eatFromThis != null)
			eatMsg=L("<S-NAME> eat(s) <T-NAMESELF> from @x1.",eatFromThis.name())+eatSound;
		else
			eatMsg=L("<S-NAME> eat(s) <T-NAMESELF>.")+eatSound;
		final CMMsg newMsg=CMClass.getMsg(mob,thisThang,null,hasHands?CMMsg.MSG_EAT:CMMsg.MSG_EAT_GROUND,eatMsg);
		if(mob.location().okMessage(mob,newMsg))
		{
			if((thisThang instanceof Food)
			&&(newMsg.value()>0)
			&&(newMsg.value()<((Food)thisThang).nourishment())
			&&(newMsg.othersMessage()!=null)
			&&(newMsg.othersMessage().startsWith(eatMsg))
			&&(newMsg.sourceMessage().equalsIgnoreCase(newMsg.othersMessage()))
			&&(newMsg.targetMessage().equalsIgnoreCase(newMsg.othersMessage())))
			{
				final String biteMsg;
				if(eatFromThis != null)
					biteMsg=L("<S-NAME> take(s) a bite of <T-NAMESELF> in @x1.",eatFromThis.name())+eatSound;
				else
					biteMsg=L("<S-NAME> take(s) a bite of <T-NAMESELF>.")+eatSound;
				newMsg.setSourceMessage(biteMsg);
				newMsg.setTargetMessage(biteMsg);
				newMsg.setOthersMessage(biteMsg);
			}
			mob.location().send(mob,newMsg);
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
