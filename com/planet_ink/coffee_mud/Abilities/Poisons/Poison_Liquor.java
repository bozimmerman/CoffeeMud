package com.planet_ink.coffee_mud.Abilities.Poisons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Poison_Liquor extends Poison_Alcohol
{
	public String ID() { return "Poison_Liquor"; }
	public String name(){ return "Liquor";}
	private static final String[] triggerStrings = {"LIQUORUP"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.POISON;}

	protected int alchoholContribution(){return 2;}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected instanceof MOB)
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(int)Math.round((drunkness+((MOB)affected).envStats().level())*4));
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.DEXTERITY,(int)Math.round(affectableStats.getStat(CharStats.DEXTERITY)-(drunkness*2)));
		if(affectableStats.getStat(CharStats.DEXTERITY)<=0)
			affectableStats.setStat(CharStats.DEXTERITY,1);
	}

	public void unInvoke()
	{
		MOB mob=null;
		if((affected!=null)&&(affected instanceof MOB))
		{
			mob=(MOB)affected;
			if((Dice.rollPercentage()<(drunkness*10))&&(!((MOB)affected).isMonster()))
			{
				Ability A=CMClass.getAbility("Disease_Migraines");
				if(A!=null) A.invoke(mob,mob,true);
			}
			CommonMsgs.stand(mob,true);
		}
		super.unInvoke();
		if((mob!=null)&&(!mob.isInCombat()))
			mob.location().show(mob,null,CMMsg.MSG_SLEEP,"<S-NAME> curl(s) up on the ground and fall(s) asleep.");
	}
}