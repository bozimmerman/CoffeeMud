package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Play_Tribal extends Play
{
	public String ID() { return "Play_Tribal"; }
	public String name(){ return "Tribal";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected String songOf(){return name()+" Music";}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		Room R=CoffeeUtensils.roomLocation(affected);
		if(R!=null)
		for(int m=0;m<R.numInhabitants();m++)
		{
			MOB mob=R.fetchInhabitant(m);
			if(mob!=null)
			for(int i=0;i<mob.numEffects();i++)
			{
				Ability A=mob.fetchEffect(i);
				if((A!=null)
				&&(A instanceof StdAbility)
				&&(A.quality()!=Ability.MALICIOUS)
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.CHANT)
				&&(((StdAbility)A).getTickDownRemaining()==1))
					((StdAbility)A).setTickDownRemaining(2);
			}
		}
		return true;
	}
}
