package com.planet_ink.coffee_mud.Abilities.Specializations;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Specialization_Weapon extends StdAbility
{
	public String ID() { return "Specialization_Weapon"; }
	public String name(){ return "Weapon Specialization";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}

	protected boolean activated=false;
	protected int weaponType=-1;
	protected int secondWeaponType=-1;

	public int classificationCode(){return Ability.SKILL;}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((activated)
		&&(Dice.rollPercentage()<25)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&((msg.tool()!=null)
		&&(msg.tool() instanceof Weapon)
		&&((((Weapon)msg.tool()).weaponClassification()==weaponType)
 		 ||(weaponType<0)
		 ||(((Weapon)msg.tool()).weaponClassification()==secondWeaponType))))
			helpProfficiency((MOB)affected);
	}


	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		activated=false;
		if(affected instanceof MOB)
		{
			Item myWeapon=((MOB)affected).fetchWieldedItem();
			if((myWeapon!=null)
			&&(myWeapon instanceof Weapon)
			&&((((Weapon)myWeapon).weaponClassification()==weaponType)
 			 ||(weaponType<0)
			 ||(((Weapon)myWeapon).weaponClassification()==secondWeaponType)))
			{
				activated=true;
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round(15.0*(Util.div(profficiency(),100.0))));
			}
		}
	}
}
