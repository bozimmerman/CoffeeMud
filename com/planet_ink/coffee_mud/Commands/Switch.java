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
import com.planet_ink.coffee_mud.Common.interfaces.Session.SessionStatus;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.Sessions;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

/*
   Copyright 2015-2018 Bo Zimmerman

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

public class Switch extends StdCommand
{
	public Switch()
	{
	}

	private final String[]	access	= I(new String[] { "SWITCH" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if(mob.soulMate()!=null)
		{
			mob.tell(L("You are possessing someone.  Quit back to your body first!"));
			return false;
		}
		final PlayerStats mobPStats=mob.playerStats();
		final Session s1=mob.session();
		if((mobPStats==null)||(s1==null))
		{
			mob.tell(L("You are a mob! Go away!"));
			return false;
		}
		boolean sysopOverride = false;
		if((commands.size()>2)&&(commands.get(1).equals("override"))&&(CMSecurity.isASysOp(mob)))
		{
			sysopOverride = true;
			commands.remove(1);
		}
		if((commands.size()>1)&&(CMath.isInteger(commands.get(1))))
		{
			final int port=CMath.s_int(commands.get(1));
			MudHost switchToHost = null;
			for(int i=0;i<CMLib.hosts().size();i++)
			{
				MudHost host = CMLib.hosts().get(i);
				if(host.getPort()==port)
				{
					switchToHost=host;
					break;
				}
			}
			if((switchToHost == null)||(mob.session()==null))
				mob.tell(L("You can't switch to '@x1'.",commands.get(1)));
			else
			{
				mob.clearCommandQueue();
				final Room room=mob.location();
				final CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_QUIT,L("<S-NAME> get(s) a far away look, then fades away...."));
				if((room != null) && (room.okMessage(mob,msg)))
				{
					CMLib.map().sendGlobalMessage(mob,CMMsg.TYP_QUIT, msg);
					//s1.initializeSession(new Socket(), s1.getGroupName(),"");
					//s1.stopSession(false,false, false); // this should call prelogout and later loginlogoutthread to cause msg SEND
					s1.logout(true); // this should call prelogout and later loginlogoutthread to cause msg SEND
					//s1.stopSession(false,false, false);
					//s1.setMob(null);
					//mob.setSession(null);
					final MudHost newHost = switchToHost;
					final ThreadGroup newG=((Thread)newHost).getThreadGroup();
					final String newName = newG.getName();
					if(CMLib.library(s1.getGroupName().charAt(0), CMLib.Library.SESSIONS)
					!= CMLib.library(newName.charAt(0), CMLib.Library.SESSIONS))
					{
						((Sessions)CMLib.library(s1.getGroupName().charAt(0), CMLib.Library.SESSIONS)).remove(s1);
						((Sessions)CMLib.library(newName.charAt(0), CMLib.Library.SESSIONS)).add(s1);
					}
					s1.setGroupName(newName);
					CMLib.commands().monitorGlobalMessage(room, msg);
					s1.setMob(null);
					s1.setAccount(null);
					
					s1.autoLogin(null, null);
				}
			}
			return false;
		}
		final String MOBname=CMParms.combine(commands,1);
		MOB target=CMLib.players().getPlayer(MOBname);
		boolean resetStats = false;
		if(target == null)
		{
			target=CMLib.players().getLoadPlayer(MOBname);
			resetStats = true;
		}
		if(target == mob)
		{
			mob.tell(L("You are already '@x1'.",MOBname));
			return false;
		}
		if((target==null)||(target.playerStats()==null))
		{
			mob.tell(L("You can't switch to '@x1'.",MOBname));
			return false;
		}
		final PlayerStats targetPStats = target.playerStats();
		if(!sysopOverride)
		{
			if((targetPStats.getAccount() == null) || (targetPStats.getAccount() != mobPStats.getAccount()))
			{
				mob.tell(L("You are not allowed to switch to '@x1'.",MOBname));
				return false;
			}
		}
		final Session s2=target.session();
		if((s2 != null)&&(CMLib.flags().isInTheGame(target, true)))
		{
			// live switch
			final CMMsg msg=CMClass.getMsg(mob,target,null, CMMsg.MSG_POSSESS, null);
			final Room room=mob.location();
			if((room==null)||(room.okMessage(mob, msg)))
			{
				if(room!=null)
					room.send(mob, msg);
				
				s1.setMob(target);
				s2.setMob(mob);
				target.setSession(s1);
				mob.setSession(s2);
				CMLib.commands().postLook(target,true);
				CMLib.commands().postLook(mob,true);
			}
		}
		else
		{
			final Room room=mob.location();
			final CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_QUIT,L("<S-NAME> get(s) a far away look, then fades away...."));
			if((room != null) && (room.okMessage(mob,msg)))
			{
				CMLib.map().sendGlobalMessage(mob,CMMsg.TYP_QUIT, msg);
				s1.logout(true); // this should call prelogout and later loginlogoutthread to cause msg SEND
				CMLib.commands().monitorGlobalMessage(room, msg);
				target.setSession(s1);
				s1.setMob(target);
				if(CMLib.login().finishLogin(s1, target, target.location(), resetStats) != CharCreationLibrary.LoginResult.NORMAL_LOGIN)
					s1.stopSession(true, true, true);
				else
				{
					CMLib.login().showTheNews(target);
					
					Log.sysOut(mob.Name()+" switched login to: "+target.Name());
				}
			}
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMProps.isUsingAccountSystem();
	}
}
