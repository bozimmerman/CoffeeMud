package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Spell extends StdAbility
{
	public String ID() { return "Spell"; }
	public String name(){ return "a Spell";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){ return INDIFFERENT;}
	private static final String[] triggerStrings = {"CAST","CA","C"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SPELL;}

	protected int affectType(boolean auto){
		int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
		if(quality()==Ability.MALICIOUS)
			affectType=CMMsg.MSG_CAST_ATTACK_VERBAL_SPELL;
		if(auto) affectType=affectType|CMMsg.MASK_GENERAL;
		return affectType;
	}

	public boolean maliciousAffect(MOB mob,
								   Environmental target,
								   int tickAdjustmentFromStandard,
								   int additionAffectCheckCode)
	{
		boolean truefalse=super.maliciousAffect(mob,target,tickAdjustmentFromStandard,additionAffectCheckCode);
		if(truefalse
		&&(target!=null)
		&&(target instanceof MOB)
		&&(mob!=target)
		&&(!((MOB)target).isMonster())
		&&(Dice.rollPercentage()==1)
		&&(((MOB)target).charStats().getCurrentClass().baseClass().equals("Mage")))
		{
			MOB tmob=(MOB)target;
			int num=0;
			for(int i=0;i<tmob.numEffects();i++)
			{
				Ability A=tmob.fetchEffect(i);
				if((A!=null)
				&&(A instanceof Spell)
				&&(A.quality()==Ability.MALICIOUS))
				{
					num++;
					if(num>5)
					{
						Ability A2=CMClass.getAbility("Disease_Magepox");
						if((A2!=null)&&(target.fetchEffect(A2.ID())==null))
							A2.invoke(mob,target,true);
						break;
					}
				}
			}
		}
		return truefalse;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if((!auto)
		&&(!mob.isMonster())
		&&(!disregardsArmorCheck(mob))
		&&(!CoffeeUtensils.armorCheck(mob,CharClass.ARMOR_CLOTH))
		&&(mob.isMine(this))
		&&(mob.location()!=null)
		&&(Dice.rollPercentage()<50))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!");
			return false;
		}
		return true;
	}
}
