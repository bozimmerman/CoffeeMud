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
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
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
		final Session mySession=mob.session();
		MOB M=CMLib.players().getLoadPlayer(cmd);
		if(M==null)
			M=mob.location().fetchInhabitant(cmd);
		if(M==null)
		{
			try
			{
				final List<MOB> targets=CMLib.map().findInhabitantsFavorExact(CMLib.map().rooms(), mob, cmd, false, 50);
				if(targets.size()>0)
					M=targets.get(CMLib.dice().roll(1,targets.size(),-1));
			}
			catch (final NoSuchElementException e)
			{
			}
		}
		if(M==null)
		{
			mob.tell(L("You don't know of anyone by that name."));
			return false;
		}
		if(M.soulMate()!=null)
		{
			mob.tell(L("@x1 is being possessed at the moment.",M.Name()));
			return false;
		}
		if((CMSecurity.isASysOp(M))&&(!CMSecurity.isASysOp(mob)))
		{
			mob.tell(L("You aren't powerful enough to do that."));
			return false;
		}
		if(!M.isMonster())
		{
			if(!CMSecurity.isAllowedEverywhere(mob,CMSecurity.SecFlag.ORDER))
			{
				mob.tell(L("You can't do things as players if you can't order them."));
				return false;
			}
		}
		if(M==mob)
		{
			if(commands.get(0).equalsIgnoreCase("here")
			   ||commands.get(0).equalsIgnoreCase("."))
			{
				commands.remove(0);
			}
			M.doCommand(new XVector<String>(CMParms.toStringArray(commands)),metaFlags|MUDCmdProcessor.METAFLAG_AS);
			return false;
		}
		final Room oldRoom=M.location();
		boolean inside=(oldRoom!=null)?oldRoom.isInhabitant(M):false;
		final boolean dead=M.amDead();
		final Session hisSession=M.session();
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
		M.doCommand(new XVector<String>(CMParms.toStringArray(commands)),metaFlags|MUDCmdProcessor.METAFLAG_AS);
		synchronized(mySession)
		{
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
		return CMSecurity.isAllowedAnywhere(mob, CMSecurity.SecFlag.AS);
	}

}
