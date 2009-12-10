package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
@SuppressWarnings("unchecked")
public class Spell extends StdAbility
{
	public String ID() { return "Spell"; }
	public String name(){ return "a Spell";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"CAST","CA","C"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SPELL;}
	
	public boolean maliciousAffect(MOB mob,
								   Environmental target,
								   int asLevel,
								   int tickAdjustmentFromStandard,
								   int additionAffectCheckCode)
	{
		boolean truefalse=super.maliciousAffect(mob,target,asLevel,tickAdjustmentFromStandard,additionAffectCheckCode);
		if(truefalse
		&&(target!=null)
		&&(target instanceof MOB)
		&&(mob!=target)
		&&(!((MOB)target).isMonster())
		&&(CMLib.dice().rollPercentage()==1)
		&&(((MOB)target).charStats().getCurrentClass().baseClass().equals("Mage")))
		{
			MOB tmob=(MOB)target;
			int num=0;
			for(int i=0;i<tmob.numEffects();i++)
			{
				Ability A=tmob.fetchEffect(i);
				if((A!=null)
				&&(A instanceof Spell)
				&&(A.abstractQuality()==Ability.QUALITY_MALICIOUS))
				{
					num++;
					if((num>5)&&(!CMSecurity.isDisabled("AUTODISEASE")))
					{
						Ability A2=CMClass.getAbility("Disease_Magepox");
						if((A2!=null)&&(target.fetchEffect(A2.ID())==null))
							A2.invoke(mob,target,true,asLevel);
						break;
					}
				}
			}
		}
		return truefalse;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
        if((!auto)&&(mob.isMine(this))&&(mob.location()!=null))
        {
    		if((!mob.isMonster())
    		&&(!disregardsArmorCheck(mob))
    		&&(!CMLib.utensils().armorCheck(mob,CharClass.ARMOR_CLOTH))
    		&&(CMLib.dice().rollPercentage()<50))
    		{
    			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!");
    			return false;
    		}
        }
        return true;
	}
}
