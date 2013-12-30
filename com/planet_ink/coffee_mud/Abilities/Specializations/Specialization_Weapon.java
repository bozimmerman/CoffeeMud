package com.planet_ink.coffee_mud.Abilities.Specializations;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2014 Bo Zimmerman

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
public class Specialization_Weapon extends StdAbility
{
	public String ID() { return "Specialization_Weapon"; }
	public String name(){ return "Weapon Specialization";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}

	protected boolean activated=false;
	protected int weaponClass=-1;
	protected int secondWeaponClass=-1;
	
	protected short[] bonuses=null;
	protected Object cachePtr=null;

	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_WEAPON_USE;}

	protected int getDamageBonus(MOB mob, int dmgType)
	{
		switch(dmgType)
		{
		case Weapon.TYPE_SLASHING: return getX1Level(mob);
		case Weapon.TYPE_PIERCING: return getX2Level(mob);
		case Weapon.TYPE_BASHING: return getX3Level(mob);
		case Weapon.TYPE_SHOOT: return getX2Level(mob);
		default:
			return 0;
		}
	}
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((activated)
		&&(msg.source()==affected)
		&&(msg.target() instanceof MOB)
		&&(msg.tool() instanceof Weapon))
		{
			Weapon w=(Weapon)msg.tool();
			if((w.weaponClassification()==weaponClass)
			||(weaponClass<0)
			||(w.weaponClassification()==secondWeaponClass))
			{
				if((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)&&(CMLib.dice().rollPercentage()<10))
					helpProficiency((MOB)affected, 0);
				else
				if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
				&&((w.weaponClassification()==Weapon.CLASS_NATURAL)||(!w.amWearingAt(Wearable.IN_INVENTORY)))
				&&(msg.value()>0))
					msg.setValue(msg.value()+(this.getDamageBonus(msg.source(),w.weaponType())));
			}
		}
	}


	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		activated=false;
		if(affected instanceof MOB)
		{
			Item myWeapon=((MOB)affected).fetchWieldedItem();
			if((myWeapon instanceof Weapon)
			&&((((Weapon)myWeapon).weaponClassification()==weaponClass)
 			 ||(weaponClass<0)
			 ||(((Weapon)myWeapon).weaponClassification()==secondWeaponClass)))
			{
				activated=true;
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()
						+(int)Math.round(15.0*(CMath.div(proficiency(),100.0)))
						+(10*(getXLEVELLevel((MOB)affected))));
					
			}
		}
	}
}
