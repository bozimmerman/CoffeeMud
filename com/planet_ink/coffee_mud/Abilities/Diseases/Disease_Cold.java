package com.planet_ink.coffee_mud.Abilities.Diseases;
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

public class Disease_Cold extends Disease
{
	public String ID() { return "Disease_Cold"; }
	public String name(){ return "Cold";}
	public String displayText(){ return "(Cold Virus)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}

	protected int DISEASE_TICKS(){return 24;}
	protected int DISEASE_DELAY(){return 5;}
	protected String DISEASE_DONE(){return "Your cold clears up.";}
	protected String DISEASE_START(){return "^G<S-NAME> come(s) down with a cold.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> sneeze(s). AAAAAAAAAAAAAACHOOO!!!!";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION|DiseaseAffect.SPREAD_PROXIMITY|DiseaseAffect.SPREAD_CONTACT|DiseaseAffect.SPREAD_STD;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		MOB diseaser=invoker;
		if(diseaser==null) diseaser=mob;
		if((getTickDownRemaining()==1)
		&&(Dice.rollPercentage()>mob.charStats().getSave(CharStats.SAVE_COLD))
		&&(Dice.rollPercentage()<25-mob.charStats().getStat(CharStats.CONSTITUTION))
		&&(!mob.amDead())
		&&(!mob.isMonster()))
		{
			mob.delEffect(this);
			Ability A=CMClass.getAbility("Disease_Pneumonia");
			A.invoke(diseaser,mob,true,0);
		}
		else
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,CMMsg.MSG_NOISE,DISEASE_AFFECT());
			if(mob.curState().getHitPoints()>((2*diseaser.envStats().level())+1))
			{
				int damage=Dice.roll(2,diseaser.envStats().level(),1);
				MUDFight.postDamage(diseaser,mob,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_DISEASE,-1,null);
			}
			catchIt(mob);
			return true;
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if(affected==null) return;
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-2);
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)-3);
		if(affectableStats.getStat(CharStats.CONSTITUTION)<=0)
			affectableStats.setStat(CharStats.CONSTITUTION,1);
		if(affectableStats.getStat(CharStats.STRENGTH)<=0)
			affectableStats.setStat(CharStats.STRENGTH,1);
	}
}
