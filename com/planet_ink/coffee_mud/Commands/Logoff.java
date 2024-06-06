package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2010-2024 Bo Zimmerman

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
public class Logoff extends StdCommand
{
	public Logoff()
	{
	}

	private final String[] access=I(new String[]{"LOGOFF","LOGOUT"});
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
			Quit.dispossess(mob,CMParms.combine(commands).endsWith("!"));
		else
		if(!mob.isMonster())
		{
			final Session session=mob.session();
			if((session!=null)
			&&(session.getLastPKFight()>0)
			&&((System.currentTimeMillis()-session.getLastPKFight())<(5*60*1000))
			&&(!CMSecurity.isASysOp(mob)))
			{
				mob.tell(L("You must wait a few more minutes before you are allowed to logout."));
				return false;
			}
			if((!CMLib.masking().maskCheck(CMProps.getVar(CMProps.Str.LOGOUTMASK), mob, true))
			&&(!CMSecurity.isASysOp(mob)))
			{
				mob.tell(L("You are not permitted to logout at this time."));
				mob.tell(L("Logging out requires: ")+CMLib.masking().maskDesc(CMProps.getVar(CMProps.Str.LOGOUTMASK), false));
				return false;
			}
			try
			{
				if(session != null)
				{
					session.prompt(new InputCallback(InputCallback.Type.CONFIRM, "N", 30000)
					{
						@Override
						public void showPrompt()
						{
							session.promptPrint(L("\n\rLogout -- are you sure (y/N)?"));
						}

						@Override
						public void timedOut()
						{
						}

						@Override
						public void callBack()
						{
							if(this.confirmed)
							{
								final CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_QUIT,null);
								final Room R=mob.location();
								if((R!=null)&&(R.okMessage(mob,msg)))
								{
									CMLib.map().sendGlobalMessage(mob,CMMsg.TYP_QUIT, msg);
									if(CMSecurity.isDisabled(DisFlag.LOGOUTS))
										session.stopSession(false,false, false); // this should call prelogout and later loginlogoutthread to cause msg SEND
									else
										session.logout(true); // this should call prelogout and later loginlogoutthread to cause msg SEND
									CMLib.commands().monitorGlobalMessage(R, msg);
								}
							}
						}
					});
				}
			}
			catch(final Exception e)
			{
				Log.errOut("Logoff",e.getMessage());
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
		return false;
	}

}
