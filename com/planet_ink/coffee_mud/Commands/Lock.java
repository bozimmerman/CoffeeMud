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
public class Lock extends StdCommand
{
	public Lock()
	{
	}

	private final String[] access=I(new String[]{"LOCK","LOC"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final Vector<String> origCmds=new XVector<String>(commands);
		final String whatTolock=CMParms.combine(commands,1);
		if(whatTolock.length()==0)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Lock what?"));
			return false;
		}
		Environmental lockThis=null;
		int dirCode=CMLib.directions().getGoodDirectionCode(whatTolock);
		if(dirCode>=0)
			lockThis=mob.location().getExitInDir(dirCode);
		if(lockThis==null)
			lockThis=mob.location().fetchFromMOBRoomItemExit(mob,null,whatTolock,Wearable.FILTER_ANY);

		if((lockThis==null)||(!CMLib.flags().canBeSeenBy(lockThis,mob)))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't see '@x1' here.",whatTolock));
			return false;
		}
		if(lockThis instanceof CloseableLockable)
		{
			final CloseableLockable cLock=(CloseableLockable)lockThis;
			if(cLock.hasADoor() && cLock.isOpen())
			{
				final Command C=CMClass.getCommand("Close");
				if(!((Boolean)C.executeInternal(mob, metaFlags, lockThis, whatTolock, Integer.valueOf(dirCode))).booleanValue())
					return false;
			}
		}
		final String lockMsg="<S-NAME> lock(s) <T-NAMESELF><O-WITHNAME>."+CMLib.protocol().msp("doorlock.wav",10);
		final CMMsg msg=CMClass.getMsg(mob,lockThis,null,CMMsg.MSG_LOCK,lockMsg,whatTolock,lockMsg);
		if(lockThis instanceof Exit)
		{
			final boolean locked=((Exit)lockThis).isLocked();
			final Room R=mob.location();
			if((R!=null)
			&&(R.okMessage(msg.source(),msg))
			&&(!locked))
			{
				R.send(msg.source(),msg);
				if(dirCode<0)
				{
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						if(R.getExitInDir(d)==lockThis)
						{
							dirCode=d;
							break;
						}
					}
				}
				final Room opR=R.getRoomInDir(dirCode);
				if((dirCode>=0)
				&&(opR!=null))
				{
					final Exit opE=R.getPairedExit(dirCode);
					if(opE!=null)
					{
						final CMMsg altMsg=CMClass.getMsg(msg.source(),opE,msg.tool(),msg.sourceCode(),null,msg.targetCode(),null,msg.othersCode(),null);
						opE.executeMsg(msg.source(),altMsg);
					}
					final int opCode=R.getReverseDir(dirCode);
					if((opE!=null)
					&&(opE.isLocked())
					&&(((Exit)lockThis).isLocked()))
					{
						final Directions.DirType dirType=CMLib.flags().getDirType(opR);
						final String inDirName=CMLib.directions().getInDirectionName(opCode, dirType);
						opR.showHappens(CMMsg.MSG_OK_ACTION,L("@x1 @x2 is locked from the other side.",opE.name(),inDirName));
					}
				}
			}
		}
		else
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		else
			CMLib.commands().postCommandRejection(msg.source(),msg.target(),msg.tool(),origCmds);
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
