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
public class Dance_Clog extends Dance
{
	public String ID() { return "Dance_Clog"; }
	public String name(){ return "Clog Dance";}
	public int quality(){ return MALICIOUS;}
	protected String danceOf(){return name();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;

		affectableStats.setAttackAdjustment((affectableStats.attackAdjustment()-invoker().charStats().getStat(CharStats.CHARISMA))-(Math.round(invoker.envStats().level())*2));
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;

		affectableStats.setStat(CharStats.DEXTERITY,(int)Math.round(Util.div(affectableStats.getStat(CharStats.DEXTERITY),2.0)));
	}
}
