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
public class Dance_Macabre extends Dance
{
	public String ID() { return "Dance_Macabre"; }
	public String name(){ return "Macabre";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected String danceOf(){return name()+" Dance";}

	private boolean activated=false;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(activated)
		{
			affectableStats.setDamage(affectableStats.damage()+10);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+75);
		}
		else
		if((affected instanceof MOB)
		&&(((MOB)affected).isInCombat())
		&&(((MOB)affected).getVictim().isInCombat())
		&&(((MOB)affected).getVictim()!=affected))
		{
			affectableStats.setDamage(affectableStats.damage()+5);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+50);
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(Sense.isHidden(affected))
		{
			if(!activated)
			{
				activated=true;
				affected.recoverEnvStats();
			}
		}
		else
		if(activated)
		{
			activated=false;
			affected.recoverEnvStats();
		}
		return super.tick(ticking,tickID);
	}


}
