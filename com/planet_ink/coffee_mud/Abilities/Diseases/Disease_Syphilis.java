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

public class Disease_Syphilis extends Disease
{
	public String ID() { return "Disease_Syphilis"; }
	public String name(){ return "Syphilis";}
	public String displayText(){ return "(Syphilis)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}

	protected int DISEASE_TICKS(){return 99999;}
	protected int DISEASE_DELAY(){return new Long(CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)).intValue();}
	protected String DISEASE_DONE(){return "Your syphilis clears up.";}
	protected String DISEASE_START(){return "^G<S-NAME> get(s) some uncomfortable red sores on <S-HIS-HER> privates.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> scratch(es) <S-HIS-HER> privates.";}
	public int abilityCode(){return DiseaseAffect.SPREAD_STD;}
	public int difficultyLevel(){return 0;}
	protected int conDown=0;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,CMMsg.MSG_NOISE,DISEASE_AFFECT());
			if(Dice.rollPercentage()>50)
				conDown++;
			if(Dice.rollPercentage()<10)
			{
				Ability A=null;
				if(Dice.rollPercentage()>50)
					A=CMClass.getAbility("Disease_Cold");
				else
					A=CMClass.getAbility("Disease_Fever");
				if(A!=null)A.invoke(mob,mob,true,0);
			}
			return true;
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		if(conDown<=0) return;
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-conDown);
		if(affectableStats.getStat(CharStats.CONSTITUTION)<=0)
		{
			conDown=-1;
			MOB diseaser=invoker;
			if(diseaser==null) diseaser=affected;
			MUDFight.postDeath(diseaser,affected,null);
		}
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		if(affected==null) return;
		int down=2;
		if(conDown>down) down=conDown;
		affectableState.setMovement(affectableState.getMovement()/down);
		affectableState.setMana(affectableState.getMana()/down);
		affectableState.setHitPoints(affectableState.getHitPoints()/down);
	}
}
