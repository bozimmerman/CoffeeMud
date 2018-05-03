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
public class Close extends StdCommand
{
	public Close()
	{
	}

	private final String[]	access	= I(new String[] { "CLOSE", "CLOS", "CLO", "CL" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private final static Class[][]	internalParameters	= new Class[][] { { Environmental.class, String.class, Integer.class } };

	public boolean closeMe(final MOB mob, final Environmental closeThis, final String whatToClose, int dirCode)
	{
		final boolean useShipDirs=(mob.location() instanceof BoardableShip)||(mob.location().getArea() instanceof BoardableShip);
		final String closeWord=(!(closeThis instanceof Exit))?"close":((Exit)closeThis).closeWord();
		final String closeMsg="<S-NAME> "+closeWord+"(s) <T-NAMESELF>."+CMLib.protocol().msp("dooropen.wav",10);
		final CMMsg msg=CMClass.getMsg(mob,closeThis,null,CMMsg.MSG_CLOSE,closeMsg,whatToClose,closeMsg);
		if(closeThis instanceof Exit)
		{
			final boolean open=((Exit)closeThis).isOpen();
			if((mob.location().okMessage(msg.source(),msg))
			&&(open))
			{
				mob.location().send(msg.source(),msg);
				if(dirCode<0)
				{
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						if(mob.location().getExitInDir(d)==closeThis)
						{
							dirCode = d;
							break;
						}
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
					if((opE!=null)
					&&(!opE.isOpen())
					&&(!((Exit)closeThis).isOpen()))
						opR.showHappens(CMMsg.MSG_OK_ACTION,L("@x1 @x2 closes.",opE.name(),(useShipDirs?CMLib.directions().getShipInDirectionName(opCode):CMLib.directions().getInDirectionName(opCode))));
				}
				return true;
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
		final String whatToClose=CMParms.combine(commands,1);
		if(whatToClose.length()==0)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Close what?"));
			return false;
		}
		Environmental closeThis=null;
		int dirCode=CMLib.directions().getGoodDirectionCode(whatToClose);
		if(dirCode>=0)
			closeThis=mob.location().getExitInDir(dirCode);
		if(closeThis==null)
			closeThis=mob.location().fetchFromMOBRoomItemExit(mob,null,whatToClose,Wearable.FILTER_ANY);

		if((closeThis==null)||(!CMLib.flags().canBeSeenBy(closeThis,mob)))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't see '@x1' here.",whatToClose));
			return false;
		}
		return closeMe(mob,closeThis,whatToClose,dirCode);
	}
	
	@Override
	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE;
		return Boolean.valueOf(closeMe(mob, (Environmental)args[0], (String)args[1], ((Integer)args[2]).intValue()));
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
