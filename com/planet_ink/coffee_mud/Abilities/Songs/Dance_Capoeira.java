package com.planet_ink.coffee_mud.Abilities.Songs;

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
public class Dance_Capoeira extends Dance
{
	public String ID() { return "Dance_Capoeira"; }
	public String name(){ return "Capoeira";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected String danceOf(){return name()+" Dance";}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if((affected instanceof MOB)&&(((MOB)affected).fetchWieldedItem()==null))
		{
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+invoker().charStats().getStat(CharStats.CHARISMA)+(prancerLevel()));
			affectableStats.setDamage(affectableStats.damage()+(prancerLevel()/3));
		}
	}
}
