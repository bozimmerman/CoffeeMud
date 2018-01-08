package com.planet_ink.coffee_mud.Behaviors;
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
   Copyright 2001-2018 Bo Zimmerman

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
public class GoodGuardian extends StdBehavior
{
	@Override
	public String ID()
	{
		return "GoodGuardian";
	}

	protected long deepBreath=System.currentTimeMillis();

	@Override
	public String accountForYourself()
	{
		return "protective against aggression, evilness, or thieflyness";
	}

	public static MOB anyPeaceToMake(Room room, MOB observer)
	{
		if(room==null)
			return null;
		MOB victim=null;
		for(int i=0;i<room.numInhabitants();i++)
		{
			final MOB inhab=room.fetchInhabitant(i);
			if((inhab!=null)&&(inhab.isInCombat()))
			{
				if(inhab.isMonster())
				{
					for(final Enumeration<Behavior> e=inhab.behaviors();e.hasMoreElements();)
					{
						final Behavior B=e.nextElement();
						if((B!=null)&&(B.grantsAggressivenessTo(inhab.getVictim())))
							return inhab;
					}

				}
				if((BrotherHelper.isBrother(inhab,observer,false))&&(victim==null))
					victim=inhab.getVictim();

				if((CMLib.flags().isEvil(inhab))
				||(inhab.charStats().getCurrentClass().baseClass().equalsIgnoreCase("Thief")))
					victim=inhab;
			}
		}
		return victim;
	}

	public static void keepPeace(MOB observer, MOB victim)
	{
		if(!canFreelyBehaveNormal(observer))
			return;

		if(victim!=null)
		{
			final MOB victimVictim=victim.getVictim();
			if((!BrotherHelper.isBrother(victim,observer,false))
			&&(victimVictim!=null)
			&&(!victim.amDead())
			&&(victim.isInCombat())
			&&(!victimVictim.amDead())
			&&(victimVictim.isInCombat())
			&&((!victimVictim.isMonster())||(victimVictim.fetchEffect("QuestBound")==null)))
			{
				Aggressive.startFight(observer,victim,true,false,"PROTECT THE INNOCENT!");
			}
		}
		else
		{
			final Room room=observer.location();
			for(int i=0;i<room.numInhabitants();i++)
			{
				final MOB inhab=room.fetchInhabitant(i);
				if((inhab!=null)
				&&(inhab.isInCombat())
				&&(inhab.getVictim().isInCombat())
				&&((observer.phyStats().level()>(inhab.phyStats().level()+5))))
				{
					final String msg="<S-NAME> stop(s) <T-NAME> from fighting with "+inhab.getVictim().name();
					final CMMsg msgs=CMClass.getMsg(observer,inhab,CMMsg.MSG_NOISYMOVEMENT,msg);
					if(observer.location().okMessage(observer,msgs))
					{
						observer.location().send(observer,msgs);
						final MOB ivictim=inhab.getVictim();
						if(ivictim!=null)
							ivictim.makePeace(true);
						inhab.makePeace(true);
					}
				}
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Tickable.TICKID_MOB)
			return true;
		if(!canFreelyBehaveNormal(ticking))
		{
			deepBreath=System.currentTimeMillis();
			return true;
		}
		if((deepBreath==0)||(System.currentTimeMillis()-deepBreath)>6000)
		{
			deepBreath=0;
			final MOB mob=(MOB)ticking;
			final MOB victim=anyPeaceToMake(mob.location(),mob);
			keepPeace(mob,victim);
		}
		return true;
	}
}
