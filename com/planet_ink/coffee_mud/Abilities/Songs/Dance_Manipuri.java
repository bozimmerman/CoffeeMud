package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Dance_Manipuri extends Dance
{
	public String ID() { return "Dance_Manipuri"; }
	public String name(){ return "Manipuri";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected String danceOf(){return name()+" Dance";}

	private Room lastRoom=null;
	private int count=3;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return true;
		MOB mob=(MOB)affected;
		if(mob.location()!=lastRoom)
		{
			count=3;
			lastRoom=mob.location();
		}
		else
			count--;
		return true;
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		MOB mob=(MOB)affected;
		if(((msg.targetCode()&CMMsg.MASK_MALICIOUS)>0)
		&&(mob.location()!=null)
		&&((msg.amITarget(mob)))
		&&((count>0)||(lastRoom==null)||(lastRoom!=mob.location())))
		{
			MOB target=(MOB)msg.target();
			if((!target.isInCombat())&&(msg.source().getVictim()!=target))
			{
				msg.source().tell("You feel like letting "+target.name()+" be for awhile.");
				if(target.getVictim()==msg.source())
				{
					target.makePeace();
					target.setVictim(null);
				}
				return false;
			}

		}
		return super.okMessage(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		count=3;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		count=3;
		return true;
	}

}
