package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ProtocolLibrary.InProto;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2025-2026 Bo Zimmerman

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
public class ProxyCtl extends StdCommand
{
	public ProxyCtl()
	{
	}

	private final String[]	access	= I(new String[] { "PROXYCTL" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		if((mob.isMonster())||(!mob.session().getClientTelnetMode(Session.TELNET_MPCP)))
		{
			mob.tell(L("Your session is not connected to the proxy server."));
			return false;
		}
		if(CMath.bset(metaFlags,MUDCmdProcessor.METAFLAG_ASMESSAGE))
		{
			if(commands.size()==3)
			{
				final String command = commands.get(1);
				final String jsonStr = commands.get(2);
				try
				{
					final MiniJSON.JSONObject obj = new MiniJSON().parseObject(jsonStr);
					if(command.equalsIgnoreCase("listsessions")
					&&(obj.containsKey("sessions")))
					{
						final Object[] objs = obj.getCheckedArray("sessions");
						final ArrayList<String> cols = new ArrayList<String>();
						cols.add("Client");
						cols.add("Server");
						for(final Object o : objs)
						{
							final MiniJSON.JSONObject jobj = (MiniJSON.JSONObject)o;
							if(jobj.containsKey("source")&&jobj.containsKey("target"))
							{
								cols.add(jobj.getCheckedString("source"));
								cols.add(jobj.getCheckedString("target"));
							}
						}
						mob.tell(CMLib.lister().build2ColTable(mob, new IteratorEnumeration<String>(cols.iterator())));
					}
					else
					if(command.equalsIgnoreCase("message") && obj.containsKey("message"))
						mob.tell(L("The proxy server said: @x1",obj.getCheckedString("message")));
					else
						mob.tell(L("The proxy server said: @x1",CMParms.combine(commands,1)));
				}
				catch (final MJSONException e)
				{
					Log.errOut(e);
					mob.tell(L("The proxy server said: @x1",CMParms.combine(commands,1)));
				}
				return false;
			}
		}
		else
		if((commands.size()<2)||(mob.isMonster()))
		{
			mob.tell(L("Send what? You probably need the password, a command, and any arguments"));
			return false;
		}
		else
		{
			final String password;
			if(CMath.isLong(commands.get(1)))
				password = commands.remove(1);
			else
				password = "1";
			final String command = commands.get(1);
			if(command.equalsIgnoreCase("transfer"))
			{
				final Map<String,String> eq = CMParms.parseEQParms(CMParms.combine(commands, 2));
				final String playerName = eq.get("PLAYER");
				final String targetStr = eq.get("TARGET");
				if((playerName == null) 
				|| (targetStr == null) 
				|| (targetStr.lastIndexOf(':') < 0))
				{
					mob.tell(L("Usage: PROXYCTL [password] TRANSFER PLAYER=<name> TARGET=<host>:<port>"));
					return false;
				}
				final String host = targetStr.substring(0, targetStr.lastIndexOf(':'));
				final int port = CMath.s_int(targetStr.substring(targetStr.lastIndexOf(':') + 1));
				MOB targetM = null;
				for(final Session S : CMLib.sessions().allIterable())
				{
					if((S.mob() != null)
					&& (S.mob().Name().equalsIgnoreCase(playerName))
					&& (CMLib.flags().isInTheGame(S.mob(), true)))
					{
						targetM = S.mob();
						break;
					}
				}
				if((targetM == null) || (targetM.playerStats() == null))
				{
					mob.tell(L("Player '@x1' is not online here.", playerName));
					return false;
				}
				final PlayerStats.IMudTransferData priorXfer = targetM.playerStats().getIMudTransferData();
				if((priorXfer != null) && (!priorXfer.isLocal))
				{
					mob.tell(L("Player '@x1' is foreign; return them home first.", playerName));
					return false;
				}
				if((targetM.session() == null) || (!targetM.session().getClientTelnetMode(Session.TELNET_MPCP)))
				{
					mob.tell(L("Player '@x1' is not connected through the proxy.", playerName));
					return false;
				}
				targetM.session().doPing(Session.SessionPing.PLAYERTRANSFER,
					new Pair<String,Integer>(host, Integer.valueOf(port)));
				mob.tell(L("Offering '@x1' to @x2:@x3.", playerName, host, "" + port));
				return false;
			}
			final MiniJSON.JSONObject obj = new MiniJSON.JSONObject();
			obj.put("password", password);
			obj.putAll(CMParms.parseEQParms(CMParms.combine(commands,2)));
			for(final String key : obj.keySet())
				if(CMStrings.isUpperCase(key))
				{
					obj.put(key.toLowerCase(),obj.get(key));
					obj.remove(key);
				}
			mob.session().sendInlineCommand(InProto.MPCP,command, obj.toString());
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
		return false;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.SHUTDOWN);
	}
}
