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
public class Play_Battlehymn extends Play
{
	public String ID() { return "Play_Battlehymn"; }
	public String name(){ return "Battlehymn";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected String songOf(){return "a "+name();}

	private int timesTicking=0;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		affectableStats.setDamage(affectableStats.damage()+1+(int)Math.round(Util.div(affectableStats.damage(),4.0)));
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected==null)||(invoker==null)||(!(affected instanceof MOB)))
			return false;
		if((!((MOB)affected).isInCombat())&&(++timesTicking>5))
			unInvoke();
		return true;
	}
}
