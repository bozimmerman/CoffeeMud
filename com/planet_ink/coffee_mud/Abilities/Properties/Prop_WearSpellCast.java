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
public class Prop_WearSpellCast extends Prop_HaveSpellCast
{
	public String ID() { return "Prop_WearSpellCast"; }
	public String name(){ return "Casting spells when worn";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
    public String accountForYourself()
    { return spellAccountingsWithMask("Casts "," on the wearer.");}

	public void affectEnvStats(Environmental host, EnvStats affectableStats)
	{
		if(processing) return;
		processing=true;
		if((host!=null)&&(host instanceof Item))
		{
			myItem=(Item)host;

			boolean worn=(!myItem.amWearingAt(Item.INVENTORY))
			&&((!myItem.amWearingAt(Item.FLOATING_NEARBY))||(myItem.fitsOn(Item.FLOATING_NEARBY)));
			
			if((lastMOB instanceof MOB)
			&&(((MOB)lastMOB).location()!=null)
			&&((myItem.owner()!=lastMOB)||(!worn)))
				removeMyAffectsFromLastMOB();

			if((lastMOB==null)
			&&(worn)
			&&(myItem.owner()!=null)
			&&(myItem.owner() instanceof MOB)
			&&(((MOB)myItem.owner()).location()!=null))
				addMeIfNeccessary(myItem.owner(),myItem.owner());
		}
		processing=false;
	}
}
