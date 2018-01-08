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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
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

public class Knock extends StdCommand
{
	public Knock(){}

	private final String[] access=I(new String[]{"KNOCK"});
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
		if(commands.size()<=1)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Knock on what?"));
			return false;
		}
		final String knockWhat=CMParms.combine(commands,1).toUpperCase();
		final int dir=CMLib.tracking().findExitDir(mob,mob.location(),knockWhat);
		if(dir<0)
		{
			final Environmental getThis=mob.location().fetchFromMOBRoomItemExit(mob,null,knockWhat,Wearable.FILTER_UNWORNONLY);
			if(getThis==null)
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("You don't see '@x1' here.",knockWhat.toLowerCase()));
				return false;
			}
			final CMMsg msg=CMClass.getMsg(mob,getThis,null,CMMsg.MSG_KNOCK,CMMsg.MSG_KNOCK,CMMsg.MSG_KNOCK,L("<S-NAME> knock(s) on <T-NAMESELF>.@x1",CMLib.protocol().msp("knock.wav",50)));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);

		}
		else
		{
			Exit E=mob.location().getExitInDir(dir);
			if(E==null)
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("Knock on what?"));
				return false;
			}
			if(!E.hasADoor())
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("You can't knock on @x1!",E.name()));
				return false;
			}
			final CMMsg msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_KNOCK,CMMsg.MSG_KNOCK,CMMsg.MSG_KNOCK,L("<S-NAME> knock(s) on <T-NAMESELF>.@x1",CMLib.protocol().msp("knock.wav",50)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				E=mob.location().getPairedExit(dir);
				final Room R=mob.location().getRoomInDir(dir);
				if((R!=null)&&(E!=null)&&(E.hasADoor())
				&&(R.showOthers(mob,E,null,CMMsg.MSG_KNOCK,L("You hear a knock on <T-NAMESELF>.@x1",CMLib.protocol().msp("knock.wav",50))))
				&&((R.domainType()&Room.INDOORS)==Room.INDOORS))
				{
					final Vector<Room> V=new Vector<Room>();
					V.add(mob.location());
					TrackingLibrary.TrackingFlags flags;
					flags = CMLib.tracking().newFlags()
							.plus(TrackingLibrary.TrackingFlag.OPENONLY);
					CMLib.tracking().getRadiantRooms(R,V,flags,null,5,null);
					V.removeElement(mob.location());
					for(int v=0;v<V.size();v++)
					{
						final Room R2=V.get(v);
						final int dir2=CMLib.tracking().radiatesFromDir(R2,V);
						if((dir2>=0)&&((R2.domainType()&Room.INDOORS)==Room.INDOORS))
						{
							final Room R3=R2.getRoomInDir(dir2);
							if(((R3!=null)&&(R3.domainType()&Room.INDOORS)==Room.INDOORS))
							{
								final boolean useShipDirs=(R2 instanceof BoardableShip)||(R2.getArea() instanceof BoardableShip);
								final String inDirName=useShipDirs?CMLib.directions().getShipInDirectionName(dir2):CMLib.directions().getInDirectionName(dir2);
								R2.showHappens(CMMsg.MASK_SOUND|CMMsg.TYP_KNOCK,L("You hear a knock @x1.@x2",inDirName,CMLib.protocol().msp("knock.wav",50)));
							}
						}
					}
				}
			}
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
