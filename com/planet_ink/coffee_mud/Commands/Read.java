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
@SuppressWarnings("rawtypes")
public class Read extends StdCommand
{
	public Read()
	{
	}
	
	private final String[]	access	= I(new String[] { "READ" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private final static Class[][] internalParameters=new Class[][]{{Environmental.class,String.class,Boolean.class}};

	public boolean read(MOB mob, Environmental thisThang, String theRest, boolean quiet)
	{
		if((thisThang==null)||((!(thisThang instanceof Item)&&(!(thisThang instanceof Exit))))||(!CMLib.flags().canBeSeenBy(thisThang,mob)))
		{
			mob.tell(L("You don't seem to have that."));
			return false;
		}
		if(thisThang instanceof Item)
		{
			final Item thisItem=(Item)thisThang;
			if((CMLib.flags().isGettable(thisItem))&&(!mob.isMine(thisItem)))
			{
				mob.tell(L("You don't seem to be carrying that."));
				return false;
			}
		}
		final String srcMsg="<S-NAME> read(s) <T-NAMESELF>.";
		final String soMsg=(mob.isMine(thisThang)?srcMsg:null);
		String tMsg=theRest;
		if((tMsg==null)||(tMsg.trim().length()==0)||(thisThang instanceof MOB))
			tMsg=soMsg;
		final CMMsg newMsg=CMClass.getMsg(mob,thisThang,null,CMMsg.MSG_READ,quiet?srcMsg:null,CMMsg.MSG_READ,tMsg,CMMsg.MSG_READ,quiet?null:soMsg);
		if(mob.location().okMessage(mob,newMsg))
		{
			mob.location().send(mob,newMsg);
			return true;
		}
		return false;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,new StringXVector(commands),L("Read what?"));
			return false;
		}
		commands.remove(0);
		final int dir=CMLib.directions().getGoodDirectionCode(CMParms.combine(commands,0));
		Environmental thisThang=null;
		if(dir>=0)
			thisThang=mob.location().getExitInDir(dir);
		thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,commands.get(commands.size()-1), StdCommand.noCoinFilter);
		if(thisThang==null)
			thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,commands.get(commands.size()-1),Wearable.FILTER_ANY);
		String theRest=null;
		if(thisThang==null)
			thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,CMParms.combine(commands,0),Wearable.FILTER_ANY);
		else
		{
			commands.remove(commands.size()-1);
			theRest=CMParms.combine(commands,0);
		}
		read(mob,thisThang, theRest, false);
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

	@Override
	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE;
		return Boolean.valueOf(read(mob,(Environmental)args[0],(String)args[1],((Boolean)args[2]).booleanValue()));
	}
}
