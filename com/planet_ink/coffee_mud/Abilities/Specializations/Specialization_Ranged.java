package com.planet_ink.coffee_mud.Abilities.Specializations;

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
public class Specialization_Ranged extends Specialization_Weapon
{
	public String ID() { return "Specialization_Ranged"; }
	public String name(){ return "Ranged Weapon Specialization";}
	public Specialization_Ranged()
	{
		super();
		weaponType=Weapon.CLASS_RANGED;
		secondWeaponType=Weapon.CLASS_THROWN;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((activated)
		&&(Dice.rollPercentage()<25)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.sourceMinor()==CMMsg.TYP_THROW)
		&&(msg.tool() instanceof Item)
		&&(msg.target() instanceof MOB))
			helpProfficiency((MOB)affected);
		super.executeMsg(myHost,msg);
	}
}
