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
public class As extends StdCommand
{
	public As()
	{
	}

	private final String[] access = I(new String[] { "AS" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		commands.remove(0);
		if(commands.size()<2)
		{
			mob.tell(L("As whom do what?"));
			return false;
		}
		final String cmd=commands.get(0);
		commands.remove(0);
		if((!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.AS))||(mob.isMonster()))
		{
			mob.tell(L("You aren't powerful enough to do that."));
			return false;
		}
		final Session useSession=mob.session();
		MOB targetM;
		final String asGroupName;
		final char asThreadId;
		if(cmd.equalsIgnoreCase("port"))
		{
			targetM=mob;
			if((commands.size()<2)||(!CMath.isInteger(commands.get(0))))
			{
				mob.tell(L("@x1 is not valid syntax.",commands.get(0)));
				return false;
			}
			final int asPort=CMath.s_int(commands.get(0));
			commands.remove(0);
			MudHost foundH=null;
			final StringBuilder options=new StringBuilder("");
			for(final MudHost h : CMLib.hosts())
			{
				if(h.getPort()==asPort)
					foundH=h;
				options.append(h.getPort()).append(", ");
			}
			if(foundH==null)
			{
				mob.tell(L("No host found with main port @x1.  Options include: @x2",""+asPort,options.substring(0,options.length()-2)));
				return false;
			}
			final MudHost asHost=foundH;
			asGroupName=asHost.threadGroup().getName();
			asThreadId=asGroupName.charAt(0);
			if(!CMSecurity.isASysOp(mob, asThreadId))
			{
				mob.tell(L("You do not qualify for sysop privileges on that port."));
				return false;
			}
		}
		else
		{
			targetM=CMLib.players().getLoadPlayer(cmd);
			asThreadId=(char)-1;
			asGroupName=null;
		}
		if(targetM==null)
			targetM=mob.location().fetchInhabitant(cmd);
		if(targetM==null)
		{
			try
			{
				final List<MOB> targets=CMLib.map().findInhabitantsFavorExact(CMLib.map().rooms(), mob, cmd, false, 50);
				if(targets.size()>0)
					targetM=targets.get(CMLib.dice().roll(1,targets.size(),-1));
			}
			catch (final NoSuchElementException e)
			{
			}
		}
		if(targetM==null)
		{
			mob.tell(L("You don't know of anyone by that name."));
			return false;
		}
		if(targetM.soulMate()!=null)
		{
			mob.tell(L("@x1 is being possessed at the moment.",targetM.Name()));
			return false;
		}
		if((CMSecurity.isASysOp(targetM))&&(!CMSecurity.isASysOp(mob)))
		{
			mob.tell(L("You aren't powerful enough to do that."));
			return false;
		}
		if(!targetM.isMonster())
		{
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.ORDER))
			{
				mob.tell(L("You can't do things as players if you can't order them."));
				return false;
			}
		}
		if((targetM==mob)
		&&(asGroupName==null))
		{
			if(commands.get(0).equalsIgnoreCase("here")
			   ||commands.get(0).equalsIgnoreCase("."))
			{
				commands.remove(0);
			}
			targetM.doCommand(new XVector<String>(CMParms.toStringArray(commands)),metaFlags|MUDCmdProcessor.METAFLAG_AS);
			return false;
		}
		final MOB finalTargetM=targetM;
		final Runnable run = new Runnable()
		{
			final MOB M=finalTargetM;
			final String grp=asGroupName;
			final Session mySession=useSession;

			@Override
			public void run()
			{
				final Room oldRoom=M.location();
				boolean inside=(oldRoom!=null)?oldRoom.isInhabitant(M):false;
				final boolean dead=M.amDead();
				final Session hisSession=M.session();
				final String myGroup=useSession.getGroupName();
				if(grp != null)
					mySession.setGroupName(grp);
				synchronized(mySession)
				{
					//int myBitmap=mob.getBitmap();
					//int oldBitmap=M.getBitmap();
					M.setSession(mySession);
					mySession.setMob(M);
					M.setSoulMate(mob);
					//mySession.initTelnetMode(oldBitmap);
					if(commands.get(0).equalsIgnoreCase("here")
					   ||commands.get(0).equalsIgnoreCase("."))
					{
						if((M.location()!=mob.location())&&(!mob.location().isInhabitant(M)))
							mob.location().bringMobHere(M,false);
						commands.remove(0);
					}
					if(dead)
						M.bringToLife();
					if((M.location()==null)&&(oldRoom==null)&&(mob.location()!=null))
					{
						inside=false;
						mob.location().bringMobHere(M,false);
					}
				}
				CMLib.s_sleep(100);
				try
				{
					M.doCommand(new XVector<String>(CMParms.toStringArray(commands)),metaFlags|MUDCmdProcessor.METAFLAG_AS);
				}
				finally
				{
					synchronized(mySession)
					{
						if(grp != null)
							mySession.setGroupName(myGroup);
						if(M.playerStats()!=null)
							M.playerStats().setLastUpdated(0);
						if((oldRoom!=null)&&(inside)&&(!oldRoom.isInhabitant(M)))
							oldRoom.bringMobHere(M,false);
						else
						if((oldRoom==null)||(!inside))
						{
							if(M.location()!=null)
								M.location().delInhabitant(M);
							M.setLocation(oldRoom);
						}
						M.setSoulMate(null);
						M.setSession(hisSession);
						mySession.setMob(mob);
					}
					CMLib.s_sleep(100);
					//mySession.initTelnetMode(myBitmap);
					if(dead)
						M.removeFromGame(true,true);
				}
			}

		};
		if(asThreadId != (char)-1)
			CMLib.threads().executeRunnable(asThreadId, run);
		else
			run.run();
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return CMSecurity.isAllowedAnywhere(mob, CMSecurity.SecFlag.AS);
	}

}
