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
public class Prop_FightSpellCast extends Prop_SpellAdder
{
	public String ID() { return "Prop_FightSpellCast"; }
	public String name(){ return "Casting spells when properly used during combat";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}

	public String accountForYourself()
	{ return spellAccountingsWithMask("Casts "," during combat.");}

    public void affectEnvStats(Environmental affected, EnvStats affectableStats)
    {}
    
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(processing) return;

		if(!(affected instanceof Item)) return;
		processing=true;

		Item myItem=(Item)affected;

		if((myItem!=null)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0)
		&&(!myItem.amWearingAt(Item.INVENTORY))
		&&(myItem.owner()!=null)
		&&(myItem.owner() instanceof MOB)
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB))
		{
			MOB mob=(MOB)myItem.owner();
			if((mob.isInCombat())
			&&(mob.location()!=null)
			&&(!mob.amDead()))
			{
				if((myItem instanceof Weapon)
				&&(msg.tool()==myItem)
				&&(myItem.amWearingAt(Item.WIELD))
				&&(msg.amISource(mob)))
					addMeIfNeccessary(msg.source(),msg.target());
				else
				if((msg.amITarget(mob))
				&&(!myItem.amWearingAt(Item.WIELD))
				&&(!(myItem instanceof Weapon)))
					addMeIfNeccessary(msg.target(),msg.target());
			}
		}
		processing=false;
	}
}
