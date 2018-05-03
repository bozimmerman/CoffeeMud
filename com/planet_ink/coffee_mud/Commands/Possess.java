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

public class Possess extends StdCommand
{
	public Possess()
	{
	}
	
	@SuppressWarnings("rawtypes")
	private final static Class[][] internalParameters=new Class[][]{{MOB.class, Boolean.class}};

	private final String[]	access	= I(new String[] { "POSSESS", "POSS" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public MOB getTarget(MOB mob, List<String> commands, boolean quiet)
	{
		String targetName=CMParms.combine(commands,0);
		MOB target=null;
		if(targetName.length()>0)
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				final Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName);
				if((t!=null)&&(!(t instanceof MOB)))
				{
					if(!quiet)
						mob.tell(mob,t,null,L("You can't do that to <T-NAMESELF>."));
					return null;
				}
			}
		}

		if(target!=null)
			targetName=target.name();

		if((target==null)||((!CMLib.flags().canBeSeenBy(target,mob))&&((!CMLib.flags().canBeHeardMovingBy(target,mob))||(!target.isInCombat()))))
		{
			if(!quiet)
			{
				if(targetName.trim().length()==0)
					mob.tell(L("You don't see them here."));
				else
					mob.tell(L("You don't see '@x1' here.",targetName));
			}
			return null;
		}

		return target;
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
		commands.remove(0);
		String MOBname=CMParms.combine(commands,0);
		MOB target=getTarget(mob,commands,true);
		if(target == null)
		{
			int x=MOBname.lastIndexOf('@');
			Enumeration<Room> targetRooms = null;
			if(x>0)
			{
				String place=MOBname.substring(x+1);
				MOBname=MOBname.substring(0,x);
				final Room tR=CMLib.map().getRoom(place);
				if(tR!=null)
					targetRooms=new XVector<Room>(tR).elements();
				else
				{
					final Area tA=CMLib.map().findArea(place);
					if(tA!=null)
						targetRooms=tA.getMetroMap();
				}
				if(targetRooms != null)
				{
					try
					{
						final List<MOB> inhabs=CMLib.map().findInhabitantsFavorExact(targetRooms, mob,MOBname,false,100);
						for(final MOB mob2 : inhabs)
						{
							if((mob2.isMonster())&&(CMSecurity.isAllowed(mob,mob2.location(),CMSecurity.SecFlag.POSSESS)))
							{
								target=mob2;
								break;
							}
						}
					}
					catch(final NoSuchElementException e)
					{
					}
				}
			}
			if((target==null)||(!target.isMonster()))
				target=mob.location().fetchInhabitant(MOBname);
			if((target==null)||(!target.isMonster()))
			{
				final Enumeration<Room> r=mob.location().getArea().getProperMap();
				for(;r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					final MOB mob2=R.fetchInhabitant(MOBname);
					if((mob2!=null)&&(mob2.isMonster()))
					{
						target=mob2;
						break;
					}
				}
			}
			if((target==null)||(!target.isMonster()))
			{
				try
				{
					final List<MOB> inhabs=CMLib.map().findInhabitants(CMLib.map().rooms(), mob,MOBname,100);
					for(final MOB mob2 : inhabs)
					{
						if((mob2.isMonster())&&(CMSecurity.isAllowed(mob,mob2.location(),CMSecurity.SecFlag.POSSESS)))
						{
							target=mob2;
							break;
						}
					}
				}
				catch(final NoSuchElementException e)
				{
				}
			}
		}
		if((target==null)||(!target.isMonster())||(!CMLib.flags().isInTheGame(target,true)))
		{
			mob.tell(L("You can't possess '@x1' right now.",MOBname));
			return false;
		}
		if(!CMSecurity.isAllowed(mob,target.location(),CMSecurity.SecFlag.POSSESS))
		{
			mob.tell(L("You can not possess @x1.",target.Name()));
			return false;
		}

		if((!CMSecurity.isASysOp(mob))&&(CMSecurity.isASysOp(target)))
		{
			mob.tell(L("You may not possess '@x1'.",MOBname));
			return false;
		}
		final CMMsg msg=CMClass.getMsg(mob,target,null, CMMsg.MSG_POSSESS, L("<S-NAME> get(s) a far away look, then seem(s) to fall limp."));
		final Room room=mob.location();
		if((room==null)||(room.okMessage(mob, msg)))
		{
			if(room!=null)
				room.send(mob, msg);
			final Session s=mob.session();
			s.setMob(target);
			target.setSession(s);
			target.setSoulMate(mob);
			mob.setSession(null);
			CMLib.commands().postLook(target,true);
			target.tell(L("^HYour spirit has changed bodies@x1, use QUIT to return to yours.",(mob.isAttributeSet(MOB.Attrib.SYSOPMSGS)?" and SECURITY mode is ON":"")));
		}
		return false;
	}

	protected boolean possess(MOB mob, MOB target, boolean quiet)
	{
		final CMMsg msg=CMClass.getMsg(mob,target,null, CMMsg.MSG_POSSESS, quiet?null:L("<S-NAME> get(s) a far away look, then seem(s) to fall limp."));
		final Room room=mob.location();
		if((room==null)||(room.okMessage(mob, msg)))
		{
			if(room!=null)
				room.send(mob, msg);
			final Session s=mob.session();
			s.setMob(target);
			target.setSession(s);
			target.setSoulMate(mob);
			mob.setSession(null);
			CMLib.commands().postLook(target,true);
			if(!quiet)
				target.tell(L("^HYour spirit has changed bodies@x1, use QUIT to return to yours.",(mob.isAttributeSet(MOB.Attrib.SYSOPMSGS)?" and SECURITY mode is ON":"")));
			return true;
		}
		return false;
	}
	
	@Override
	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE;
		return Boolean.valueOf(possess(mob, (MOB)args[0], ((Boolean)args[1]).booleanValue()));
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.POSSESS);
	}
}
