package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
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
public class Song_Rage extends Song
{
	public String ID() { return "Song_Rage"; }
	public String name(){ return "Rage";}
	public int quality(){ return MALICIOUS;}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;
		affectableStats.setDamage(affectableStats.damage()+(int)Math.round(Util.div(affectableStats.damage(),2.0)));
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(int)Math.round(Util.div(affectableStats.attackAdjustment(),6.0)));
		affectableStats.setArmor(affectableStats.armor()+20);
	}


	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(msg.amISource(invoker)) return true;
		if(msg.sourceMinor()!=CMMsg.TYP_FLEE) return true;
		if(msg.source().fetchEffect(this.ID())==null) return true;

		msg.source().tell("You are too enraged to flee.");
		return false;
	}

}
