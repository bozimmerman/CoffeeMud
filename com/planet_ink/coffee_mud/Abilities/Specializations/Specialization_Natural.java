package com.planet_ink.coffee_mud.Abilities.Specializations;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;


/* 
   Copyright 2000-2010 Bo Zimmerman

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
public class Specialization_Natural extends Specialization_Weapon
{
	public String ID() { return "Specialization_Natural"; }
	public String name(){ return "Hand to hand combat";}
	public Specialization_Natural()
	{
		super();
		weaponType=Weapon.CLASS_NATURAL;
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((activated)
		&&(CMLib.dice().rollPercentage()<25)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&((!(msg.tool() instanceof Weapon))||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_NATURAL)))
			helpProficiency((MOB)affected);
	}


	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		activated=false;
		super.affectEnvStats(affected,affectableStats);
		if((affected instanceof MOB)&&(((MOB)affected).fetchWieldedItem()==null))
		{
			activated=true;
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()
					+(int)Math.round(15.0*(CMath.div(proficiency(),100.0)))
					+(10*(getXLEVELLevel((MOB)affected))));
		}
	}
}
