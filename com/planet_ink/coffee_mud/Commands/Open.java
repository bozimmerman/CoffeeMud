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
   Copyright 2001-2018 Bo Zimmerman

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
public class Open extends StdCommand
{
	public Open()
	{
	}

	private final String[]	access	= I(new String[] { "OPEN", "OP", "O" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private final static Class[][] internalParameters=new Class[][]{{Environmental.class,Boolean.class}};

	public boolean open(MOB mob, Environmental openThis, String openableWord, int dirCode, boolean quietly)
	{
		final String openWord=(!(openThis instanceof Exit))?"open":((Exit)openThis).openWord();
		final String openMsg=quietly?null:("<S-NAME> "+openWord+"(s) <T-NAMESELF>.")+CMLib.protocol().msp("dooropen.wav",10);
		final CMMsg msg=CMClass.getMsg(mob,openThis,null,CMMsg.MSG_OPEN,openMsg,openableWord,openMsg);
		if(openThis instanceof Exit)
		{
			final boolean open=((Exit)openThis).isOpen();
			if((mob.location().okMessage(msg.source(),msg))
			&&(!open))
			{
				mob.location().send(msg.source(),msg);

				if(dirCode<0)
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					if(mob.location().getExitInDir(d)==openThis)
					{
						dirCode = d;
						break;
					}
				}
				if((dirCode>=0)&&(mob.location().getRoomInDir(dirCode)!=null))
				{
					final Room opR=mob.location().getRoomInDir(dirCode);
					final Exit opE=mob.location().getPairedExit(dirCode);
					if(opE!=null)
					{
						final CMMsg altMsg=CMClass.getMsg(msg.source(),opE,msg.tool(),msg.sourceCode(),null,msg.targetCode(),null,msg.othersCode(),null);
						opE.executeMsg(msg.source(),altMsg);
					}
					final int opCode=Directions.getOpDirectionCode(dirCode);
					if((opE!=null)&&(opE.isOpen())&&(((Exit)openThis).isOpen()))
					{
						final boolean useShipDirs=(opR instanceof BoardableShip)||(opR.getArea() instanceof BoardableShip);
						final String inDirName=useShipDirs?CMLib.directions().getShipInDirectionName(opCode):CMLib.directions().getInDirectionName(opCode);
						opR.showHappens(CMMsg.MSG_OK_ACTION,L("@x1 @x2 opens.",opE.name(),inDirName));
					}
					return true;
				}
			}
		}
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			return true;
		}
		return false;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		Vector<String> origCmds=new XVector<String>(commands);
		final String whatToOpen=CMParms.combine(commands,1);
		if(whatToOpen.length()==0)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Open what?"));
			return false;
		}
		Environmental openThis=null;
		final int dirCode=CMLib.directions().getGoodDirectionCode(whatToOpen);
		if(dirCode>=0)
			openThis=mob.location().getExitInDir(dirCode);
		if(openThis==null)
			openThis=mob.location().fetchFromMOBRoomItemExit(mob,null,whatToOpen,Wearable.FILTER_ANY);

		if((openThis==null)||(!CMLib.flags().canBeSeenBy(openThis,mob)))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't see '@x1' here.",whatToOpen));
			return false;
		}
		open(mob,openThis,whatToOpen,dirCode,false);
		return false;
	}

	@Override
	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE;
		return Boolean.valueOf(open(mob,(Environmental)args[0],((Environmental)args[0]).name(),-1,((Boolean)args[1]).booleanValue()));
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
