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

public class Whisper extends StdCommand
{
	public Whisper()
	{
	}

	private final String[]	access	= I(new String[] { "WHISPER" });

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
		if(commands.size()==1)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Whisper what?"));
			return false;
		}
		Environmental target=null;
		final Room R = mob.location();
		if(commands.size()>2)
		{
			final String possibleTarget=commands.get(1);
			target=R.fetchFromRoomFavorMOBs(null,possibleTarget);
			if((target!=null)&&(!target.name().equalsIgnoreCase(possibleTarget))&&(possibleTarget.length()<4))
				target=null;
			if((target!=null)
			&&(CMLib.flags().canBeSeenBy(target,mob))
			&&((!(target instanceof Rider))
			   ||(((Rider)target).riding()==mob.riding())))
				commands.remove(1);
			else
				target=null;
		}
		for(int i=1;i<commands.size();i++)
		{
			final String s=commands.get(i);
			if(s.indexOf(' ')>=0)
				commands.set(i,"\""+s+"\"");
		}
		final String combinedCommands=CMParms.combine(commands,1);
		if(combinedCommands.equals(""))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Whisper what?"));
			return false;
		}

		CMMsg msg=null;
		if(target==null)
		{
			final Rideable riddenR=mob.riding();
			if(riddenR==null)
			{
				msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_SPEAK,L("^T<S-NAME> whisper(s) to <S-HIM-HERSELF> '@x1'.^?@x2",combinedCommands,CMLib.protocol().msp("whisper.wav",40)),
											  CMMsg.NO_EFFECT,null,
											  CMMsg.MSG_QUIETMOVEMENT,L("^T<S-NAME> whisper(s) to <S-HIM-HERSELF>.^?@x1",CMLib.protocol().msp("whisper.wav",40)));
				if(R.okMessage(mob,msg))
					R.send(mob,msg);
			}
			else
			{
				msg=CMClass.getMsg(mob,riddenR,null,CMMsg.MSG_SPEAK,L("^T<S-NAME> whisper(s) around <T-NAMESELF> '@x1'.^?@x2",combinedCommands,CMLib.protocol().msp("whisper.wav",40)),
								CMMsg.MSG_SPEAK,L("^T<S-NAME> whisper(s) around <T-NAMESELF> '@x1'.^?@x2",combinedCommands,CMLib.protocol().msp("whisper.wav",40)),
								CMMsg.NO_EFFECT,null);
				if(R.okMessage(mob,msg))
				{
					R.send(mob,msg);
					final Vector<Environmental> targets = new Vector<Environmental>();
					for(int i=0;i<R.numInhabitants();i++)
						targets.add(R.fetchInhabitant(i));
					for (final Environmental E : targets)
					{
						if(E!=null)
						{
							if( (E instanceof MOB) && riddenR.amRiding((MOB)E))
							{
								msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_SPEAK,L("^T<S-NAME> whisper(s) around @x1 '@x2'.^?@x3",riddenR.name(),combinedCommands,CMLib.protocol().msp("whisper.wav",40)),
												CMMsg.MSG_SPEAK,L("^T<S-NAME> whisper(s) around @x1 '@x2'.^?@x3",riddenR.name(),combinedCommands,CMLib.protocol().msp("whisper.wav",40)),
												CMMsg.NO_EFFECT,null);
							}
							else
							{
								msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_SPEAK,L("^T<S-NAME> whisper(s) around @x1 '@x2'.^?@x3",riddenR.name(),combinedCommands,CMLib.protocol().msp("whisper.wav",40)),
												CMMsg.MSG_SPEAK,L("^T<S-NAME> whisper(s) something around @x1.^?@x2",riddenR.name(),CMLib.protocol().msp("whisper.wav",40)),
												CMMsg.NO_EFFECT,null);
							}
							if(R.okMessage(mob,msg))
								R.sendOthers(mob,msg);
						}
					}
				}
			}
		}
		else
		{
			msg=CMClass.getMsg(mob,target,null,
				CMMsg.MSG_SPEAK,L("^T^<WHISPER \"@x1\"^><S-NAME> whisper(s) to <T-NAMESELF> '@x2'.^</WHISPER^>^?@x3",CMStrings.removeColors(target.name()),combinedCommands,CMLib.protocol().msp("whisper.wav",40)),
				CMMsg.MSG_SPEAK,L("^T^<WHISPER \"@x1\"^><S-NAME> whisper(s) to <T-NAMESELF> '@x2'^</WHISPER^>.^?@x3",CMStrings.removeColors(target.name()),combinedCommands,CMLib.protocol().msp("whisper.wav",40)),
				CMMsg.MSG_QUIETMOVEMENT,L("^T<S-NAME> whisper(s) something to <T-NAMESELF>.^?@x1",CMLib.protocol().msp("whisper.wav",40)));
			if(R.okMessage(mob,msg))
				R.send(mob,msg);
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
