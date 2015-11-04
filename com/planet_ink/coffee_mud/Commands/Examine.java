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
   Copyright 2005-2015 Bo Zimmerman

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

public class Examine extends StdCommand
{
	public Examine(){}

	private final String[] access=I(new String[]{"EXAMINE","EXAM","EXA","LONGLOOK","LLOOK","LL"});
	@Override public String[] getAccessWords(){return access;}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		boolean quiet=false;
		Vector<String> origCmds=new XVector<String>(commands);
		if((commands!=null)&&(commands.size()>1)&&(commands.get(commands.size()-1).equalsIgnoreCase("UNOBTRUSIVELY")))
		{
			commands.remove(commands.size()-1);
			quiet=true;
		}
		final String textMsg="<S-NAME> examine(s) ";
		if(mob.location()==null)
			return false;
		if((commands!=null)&&(commands.size()>1))
		{
			Environmental thisThang=null;

			final String ID=CMParms.combine(commands,1);
			if(ID.length()==0)
				thisThang=mob.location();
			else
			if((ID.toUpperCase().startsWith("EXIT")&&(commands.size()==2)))
			{
				final CMMsg exitMsg=CMClass.getMsg(mob,thisThang,null,CMMsg.MSG_LOOK_EXITS,null);
				if((CMProps.getIntVar(CMProps.Int.EXVIEW)>=2)!=mob.isAttributeSet(MOB.Attrib.BRIEF))
					exitMsg.setValue(CMMsg.MASK_OPTIMIZE);
				if(mob.location().okMessage(mob, exitMsg))
					mob.location().send(mob, exitMsg);
				return false;
			}
			if(ID.equalsIgnoreCase("SELF")||ID.equalsIgnoreCase("ME"))
				thisThang=mob;

			if(thisThang==null)
				thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,ID,Wearable.FILTER_ANY);
			int dirCode=-1;
			if(thisThang==null)
			{
				dirCode=Directions.getGoodDirectionCode(ID);
				if(dirCode>=0)
				{
					final Room room=mob.location().getRoomInDir(dirCode);
					final Exit exit=mob.location().getExitInDir(dirCode);
					if((room!=null)&&(exit!=null))
						thisThang=exit;
					else
					{
						CMLib.commands().doCommandFail(mob,origCmds,L("You don't see anything that way."));
						return false;
					}
				}
			}
			if(thisThang!=null)
			{
				String name="<T-NAMESELF>";
				if((thisThang instanceof Room)||(thisThang instanceof Exit))
				{
					if(thisThang==mob.location())
						name="around";
					else
					if(dirCode>=0)
						name=((mob.location() instanceof BoardableShip)||(mob.location().getArea() instanceof BoardableShip))?
							Directions.getShipDirectionName(dirCode):Directions.getDirectionName(dirCode);
				}
				final CMMsg msg=CMClass.getMsg(mob,thisThang,null,CMMsg.MSG_EXAMINE,L("@x1@x2 closely.",textMsg,name));
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
				if((mob.isAttributeSet(MOB.Attrib.AUTOEXITS))&&(thisThang instanceof Room))
					msg.addTrailerMsg(CMClass.getMsg(mob,thisThang,null,CMMsg.MSG_LOOK_EXITS,null));
			}
			else
				CMLib.commands().doCommandFail(mob,origCmds,L("You don't see that here!"));
		}
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,mob.location(),null,CMMsg.MSG_EXAMINE,(quiet?null:textMsg+"around carefully."),CMMsg.MSG_EXAMINE,(quiet?null:textMsg+"at you."),CMMsg.MSG_EXAMINE,(quiet?null:textMsg+"around carefully."));
			if((mob.isAttributeSet(MOB.Attrib.AUTOEXITS))&&(CMLib.flags().canBeSeenBy(mob.location(),mob)))
				msg.addTrailerMsg(CMClass.getMsg(mob,mob.location(),null,CMMsg.MSG_LOOK_EXITS,null));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		return false;
	}
	@Override public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCommandCombatActionCost(ID());}
	@Override public double actionsCost(MOB mob, List<String> cmds){return 1.0;}
	@Override public boolean canBeOrdered(){return true;}
}
