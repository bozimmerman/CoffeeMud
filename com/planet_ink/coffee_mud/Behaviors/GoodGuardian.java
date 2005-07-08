package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/*
   Copyright 2000-2005 Bo Zimmerman

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
	public String ID(){return "GoodGuardian";}


	public static MOB anyPeaceToMake(Room room, MOB observer)
	{
		if(room==null) return null;
		MOB victim=null;
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB inhab=room.fetchInhabitant(i);
			if((inhab!=null)&&(inhab.isInCombat()))
			{
				for(int b=0;b<inhab.numBehaviors();b++)
				{
					Behavior B=inhab.fetchBehavior(b);
					if((B!=null)&&(B.grantsAggressivenessTo(inhab.getVictim())))
						return inhab;
				}

				if((BrotherHelper.isBrother(inhab,observer))&&(victim==null))
					victim=inhab.getVictim();

				if((Sense.isEvil(inhab))
				||(inhab.charStats().getCurrentClass().baseClass().equalsIgnoreCase("Thief")))
					victim=inhab;
			}
		}
		return victim;
	}

	public static void keepPeace(MOB observer, MOB victim)
	{
		if(!canFreelyBehaveNormal(observer)) return;

		if(victim!=null)
		{
			if((!BrotherHelper.isBrother(victim,observer))
            &&(!victim.amDead())
            &&(victim.isInCombat())
            &&(!victim.getVictim().amDead())
            &&(victim.getVictim().isInCombat()))
			{
				boolean yep=Aggressive.startFight(observer,victim,true);
				if(yep)	CommonMsgs.say(observer,null,"PROTECT THE INNOCENT!",false,false);
			}
		}
		else
		{
			Room room=observer.location();
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB inhab=room.fetchInhabitant(i);
				if((inhab!=null)
				   &&(inhab.isInCombat())
				   &&(inhab.getVictim().isInCombat())
				&&((observer.envStats().level()>(inhab.envStats().level()+5))))
				{
					String msg="<S-NAME> stop(s) <T-NAME> from fighting with "+inhab.getVictim().name();
					FullMsg msgs=new FullMsg(observer,inhab,CMMsg.MSG_NOISYMOVEMENT,msg);
					if(observer.location().okMessage(observer,msgs))
					{
						observer.location().send(observer,msgs);
						if(inhab.getVictim()!=null)
							inhab.getVictim().makePeace();
						inhab.makePeace();
					}
				}
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=MudHost.TICK_MOB) return true;
		if(!canFreelyBehaveNormal(ticking)) return true;
		MOB mob=(MOB)ticking;
		MOB victim=anyPeaceToMake(mob.location(),mob);
		keepPeace(mob,victim);
		return true;
	}
}
