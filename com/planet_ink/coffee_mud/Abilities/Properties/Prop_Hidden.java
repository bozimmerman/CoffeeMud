package com.planet_ink.coffee_mud.Abilities.Properties;

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
public class Prop_Hidden extends Property
{
	public String ID() { return "Prop_Hidden"; }
	public String name(){ return "Persistant Hiddenness";}
	protected int canAffectCode(){return Ability.CAN_MOBS
										 |Ability.CAN_ITEMS
										 |Ability.CAN_EXITS
										 |Ability.CAN_AREAS;}
	private int ticksSinceLoss=100;

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		if(msg.amISource(mob))
		{

			if(((!Util.bset(msg.sourceMajor(),CMMsg.MASK_SOUND)
				 ||(msg.sourceMinor()==CMMsg.TYP_SPEAK)
				 ||(msg.sourceMinor()==CMMsg.TYP_ENTER)
				 ||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
				 ||(msg.sourceMinor()==CMMsg.TYP_RECALL)))
			 &&(!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
			 &&(msg.sourceMinor()!=CMMsg.TYP_LOOK)
			 &&(msg.sourceMajor()>0))
			{
				ticksSinceLoss=0;
				mob.recoverEnvStats();
			}
		}
		return;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(ticksSinceLoss<9999)
			ticksSinceLoss++;
		return super.tick(ticking,tickID);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_HIDDEN);
			if(ticksSinceLoss>30)
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_HIDDEN);
		}
		else
		if(affected instanceof Item)
		{
			if((((Item)affected).owner()!=null)
			&&(((Item)affected).owner() instanceof Room))
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_HIDDEN);
		}
		else
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_HIDDEN);

	}
}
