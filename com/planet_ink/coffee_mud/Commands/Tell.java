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
import com.planet_ink.coffee_mud.Common.interfaces.PlayerStats.TellMsg;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2020 Bo Zimmerman

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
public class Tell extends StdCommand
{
	public Tell()
	{
	}

	private final String[]	access	= I(new String[] { "TELL", "T" });

	private final static Class<?>[][] internalParameters=new Class<?>[][]
	{
		{String.class,String.class},
		{String.class,String.class,Long.class}
	};
	
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
		if((!mob.isMonster())&&mob.isAttributeSet(MOB.Attrib.QUIET))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You have QUIET mode on.  You must turn it off first."));
			return false;
		}

		if(commands.size()<3)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Tell whom what?"));
			return false;
		}
		commands.remove(0);

		if(commands.get(0).equalsIgnoreCase("last")
		   &&(CMath.isNumber(CMParms.combine(commands,1)))
		   &&(mob.playerStats()!=null))
		{
			final java.util.List<PlayerStats.TellMsg> V=mob.playerStats().getTellStack();
			final long now=System.currentTimeMillis();
			if((V.size()==0)
			||(CMath.bset(metaFlags,MUDCmdProcessor.METAFLAG_AS))
			||(CMath.bset(metaFlags,MUDCmdProcessor.METAFLAG_POSSESSED)))
				CMLib.commands().postCommandFail(mob,origCmds,L("No telling."));
			else
			{
				int num=CMath.s_int(CMParms.combine(commands,1));
				if(num>V.size())
					num=V.size();
				final Session S=mob.session();
				try
				{
					if(S!=null)
						S.snoopSuspension(1);
					for(int i=V.size()-num;i<V.size();i++)
					{
						final TellMsg T=V.get(i);
						long elapsedTime=now-T.time();
						elapsedTime=Math.round(elapsedTime/1000L)*1000L;
						if(elapsedTime<0)
						{
							Log.errOut("Channel","Wierd elapsed time: now="+now+", then="+T.time());
							elapsedTime=0;
						}
						final String timeAgo = "^.^N ("+CMLib.time().date2SmartEllapsedTime(elapsedTime,false)+" ago)";
						mob.tell("^t"+T.message()+timeAgo);
					}
				}
				finally
				{
					if(S!=null)
						S.snoopSuspension(-1);
				}
			}
			return false;
		}

		MOB targetM=null;
		String targetName=commands.get(0).toUpperCase();
		targetM=CMLib.players().findPlayerOnline(targetName,true);
		if(targetM==null)
			targetM=CMLib.players().findPlayerOnline(targetName,false);
		if(targetM==null)
			targetM=CMLib.sessions().findPlayerOnline(targetName,true);
		if(targetM==null)
			targetM=CMLib.sessions().findPlayerOnline(targetName,false);
		if((targetM==null)&&(CMProps.isUsingAccountSystem()))
		{
			final PlayerAccount P=CMLib.players().getAccount(targetName);
			if(P!=null)
			{
				for(final Enumeration<String> p = P.getPlayers(); p.hasMoreElements(); )
				{
					final String playerName=p.nextElement();
					targetM=CMLib.sessions().findPlayerOnline(playerName,true);
					if(targetM!=null)
					{
						targetName=playerName;
						break;
					}
				}
			}
		}
		for(int i=1;i<commands.size();i++)
		{
			final String s=commands.get(i);
			if(s.indexOf(' ')>=0)
				commands.set(i,"\""+s+"\"");
		}
		String combinedCommands=CMParms.combine(commands,1);
		if(combinedCommands.equals(""))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Tell them what?"));
			return false;
		}
		combinedCommands=CMProps.applyINIFilter(combinedCommands,CMProps.Str.SAYFILTER);
		if(mob.isPlayer()
		|| CMath.bset(metaFlags, MUDCmdProcessor.METAFLAG_FORCED)
		|| CMath.bset(metaFlags, MUDCmdProcessor.METAFLAG_ORDER))
			combinedCommands=CMLib.coffeeFilter().secondaryUserInputFilter(combinedCommands);
		if(targetM==null)
		{
			if(targetName.indexOf('@')>=0)
			{
				final String mudName=targetName.substring(targetName.indexOf('@')+1);
				targetName=targetName.substring(0,targetName.indexOf('@'));
				if(CMLib.intermud().i3online()||CMLib.intermud().imc2online())
					CMLib.intermud().i3tell(mob,targetName,mudName,combinedCommands);
				else
					CMLib.commands().postCommandFail(mob,origCmds,L("Intermud is unavailable."));
				return false;
			}
			CMLib.commands().postCommandFail(mob,origCmds,L("That person doesn't appear to be online."));
			return false;
		}

		if(targetM.isAttributeSet(MOB.Attrib.QUIET))
		{
			if(CMLib.flags().isCloaked(targetM))
				CMLib.commands().postCommandFail(mob,origCmds,L("That person doesn't appear to be online."));
			else
				CMLib.commands().postCommandFail(mob,origCmds,L("That person can not hear you."));
			return false;
		}

		final Session ts=targetM.session();
		try
		{
			if(ts!=null)
				ts.snoopSuspension(1);
			CMLib.commands().postSay(mob,targetM,combinedCommands,true,true);
		}
		finally
		{
			if(ts!=null)
				ts.snoopSuspension(-1);
		}

		if((targetM.session()!=null)&&(targetM.session().isAfk()))
		{
			mob.tell(targetM.session().getAfkMessage());
			if(CMLib.flags().isCloaked(targetM))
				CMLib.commands().postCommandFail(mob,origCmds,L("That person doesn't appear to be online."));
		}
		return false;
	}

	@Override
	public Object executeInternal(final MOB mob, final int metaFlags, final Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE;
		int index = getArgumentSetIndex(internalParameters, args);
		if(index == 0)
		{
			final String targetName=(String)args[0];
			final String message=(String)args[1];
			this.execute(mob, new XVector<String>(this.access[0],targetName,message), metaFlags);
			return Boolean.TRUE;
		}
		else
		if(index == 1)
		{
			final String fromName=(String)args[0];
			final String toName=(String)args[1];
			final Long since=(Long)args[2];
			int ct = 0;
			if(mob.playerStats() != null)
			{
				for(final PlayerStats.TellMsg M : mob.playerStats().getTellStack())
				{
					if(((since == null)||(M.time()>=since.longValue()))
					&&((fromName==null)||(M.from().equalsIgnoreCase(fromName)))
					&&((toName==null)||(M.to().equalsIgnoreCase(toName))))
						ct++;
				}
			}
			return Integer.valueOf(ct);
		}
		return Boolean.FALSE;
	}


	// the reason this is not 0ed is because of combat -- we want the players to
	// use SAY, and pay for it when coordinating.
	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID());
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
