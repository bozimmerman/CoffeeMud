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
public class Dance_War extends Dance
{
	public String ID() { return "Dance_War"; }
	public String name(){ return "War";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected String danceOf(){return name()+" Dance";}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null)
			return false;
		mob.curState().setMana(0);
		return true;
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(prancerLevel()));
		affectableStats.setArmor(affectableStats.armor()-(prancerLevel()));
		affectableStats.setDamage(affectableStats.damage()+(prancerLevel()/3));
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		affectableStats.setStat(CharStats.CONSTITUTION,Math.round(affectableStats.getStat(CharStats.CONSTITUTION)+2));
		affectableStats.setStat(CharStats.DEXTERITY,Math.round(affectableStats.getStat(CharStats.DEXTERITY)+2));
		affectableStats.setStat(CharStats.INTELLIGENCE,Math.round(affectableStats.getStat(CharStats.INTELLIGENCE)+2));
		affectableStats.setStat(CharStats.WISDOM,Math.round(affectableStats.getStat(CharStats.WISDOM)+2));
		affectableStats.setStat(CharStats.STRENGTH,Math.round(affectableStats.getStat(CharStats.STRENGTH)+2));
		affectableStats.setStat(CharStats.CHARISMA,Math.round(affectableStats.getStat(CharStats.CHARISMA)+2));
	}
}
