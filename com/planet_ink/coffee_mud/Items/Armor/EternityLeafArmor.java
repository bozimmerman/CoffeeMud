package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

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
public class EternityLeafArmor extends StdArmor
{
	public String ID(){	return "EternityLeafArmor";}
	public EternityLeafArmor()
	{
		super();

		setName("a suit of Eternity Tree Leaf Armor");
		setDisplayText("a suit of Eternity tree leaf armor sits here.");
		setDescription("This suit of armor is made from the leaves of the Eternity Tree, a true gift from the Fox god himself.  (armor:  50, grants a modest degree of stealth, and is as light as cloth.)");
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseGoldValue+=25000;
		baseEnvStats().setArmor(50);
		baseEnvStats().setAbility(0);
		baseEnvStats().setWeight(15);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();
		material=EnvResource.RESOURCE_SEAWEED;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((!this.amWearingAt(Item.INVENTORY))&&(!this.amWearingAt(Item.HELD)))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SNEAKING);
	}


}
