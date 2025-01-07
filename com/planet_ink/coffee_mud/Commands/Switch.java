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
   Copyright 2015-2024 Bo Zimmerman

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
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		if(mob.soulMate()!=null)
		{
			mob.tell(L("You are possessing someone.  Quit back to your body first!"));
			return false;
		}
		final PlayerStats mobPStats=mob.playerStats();
		final Session sess=mob.session();
		if((mobPStats==null)||(sess==null))
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
		final Session session=mob.session();
		if((session!=null)
		&&(session.getLastPKFight()>0)
		&&((System.currentTimeMillis()-session.getLastPKFight())<(5*60*1000))
		&&(!CMSecurity.isASysOp(mob)))
		{
			mob.tell(L("You must wait a few more minutes before you are allowed to switch."));
			return false;
		}
		if((!CMLib.masking().maskCheck(CMProps.getVar(CMProps.Str.LOGOUTMASK), mob, true))
		&&(!CMSecurity.isASysOp(mob)))
		{
			mob.tell(L("You are not permitted to switch at this time."));
			mob.tell(L("Switching requires: ")+CMLib.masking().maskDesc(CMProps.getVar(CMProps.Str.LOGOUTMASK), false));
			return false;
		}
		if((commands.size()>1)&&(CMath.isInteger(commands.get(1))))
		{
			final int port=CMath.s_int(commands.get(1));
			MudHost switchToHost = null;
			for(int i=0;i<CMLib.hosts().size();i++)
			{
				final MudHost host = CMLib.hosts().get(i);
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
					sess.logout(true); // this should call prelogout and later loginlogoutthread to cause msg SEND
					//s1.stopSession(false,false, false);
					//s1.setMob(null);
					//mob.setSession(null);
					final MudHost newHost = switchToHost;
					final ThreadGroup newG=((Thread)newHost).getThreadGroup();
					final String newName = newG.getName();
					if(CMLib.library(sess.getGroupName().charAt(0), CMLib.Library.SESSIONS)
					!= CMLib.library(newName.charAt(0), CMLib.Library.SESSIONS))
					{
						((Sessions)CMLib.library(sess.getGroupName().charAt(0), CMLib.Library.SESSIONS)).remove(sess);
						((Sessions)CMLib.library(newName.charAt(0), CMLib.Library.SESSIONS)).add(sess);
					}
					sess.setGroupName(newName);
					CMLib.commands().monitorGlobalMessage(room, msg);
					sess.setMob(null);
					sess.setAccount(null);
					sess.autoLogin(null, null);
				}
			}
			return false;
		}
		final String MOBname=CMParms.combine(commands,1);
		MOB target=CMLib.players().getPlayer(MOBname); // should definitely stay in this t-grp
		boolean resetStats = false;
		if(target == null)
		{
			target=CMLib.players().getLoadPlayer(MOBname); // should definitely stay in this t-grp
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

				sess.setMob(target);
				s2.setMob(mob);
				target.setSession(sess);
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
				sess.logout(true); // this should call prelogout and later loginlogoutthread to cause msg SEND
				CMLib.commands().monitorGlobalMessage(room, msg);
				target.setSession(sess);
				sess.setMob(target);
				if(CMLib.login().finishLogin(sess, target, target.location(), resetStats) != CharCreationLibrary.LoginResult.NORMAL_LOGIN)
					sess.stopSession(true, true, true);
				else
				{
					sess.setStat("CPING", "true");
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
	public boolean securityCheck(final MOB mob)
	{
		return CMProps.isUsingAccountSystem()||CMSecurity.isASysOp(mob);
	}
}
