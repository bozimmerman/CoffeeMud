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

public class Disease_Gonorrhea extends Disease
{
	public String ID() { return "Disease_Gonorrhea"; }
	public String name(){ return "Gonorrhea";}
	public String displayText(){ return "(Gonorrhea)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}

	protected int DISEASE_TICKS(){return 99999;}
	protected int DISEASE_DELAY(){return new Long(CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)).intValue();}
	protected String DISEASE_DONE(){return "Your gonorrhea clears up.";}
	protected String DISEASE_START(){return "^G<S-NAME> squeeze(s) <S-HIS-HER> privates uncomfortably.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> squeeze(s) <S-HIS-HER> privates uncomfortably.";}
	public int abilityCode(){return DiseaseAffect.SPREAD_STD;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((Dice.rollPercentage()==1)
		&&(Dice.rollPercentage()>mob.charStats().getSave(CharStats.SAVE_COLD))
		&&(!mob.amDead())
		&&(Dice.rollPercentage()<25-mob.charStats().getStat(CharStats.CONSTITUTION)))
		{
			MOB diseaser=invoker;
			if(diseaser==null) diseaser=mob;
			Ability A=CMClass.getAbility("Disease_Arthritis");
			A.invoke(diseaser,mob,true,0);
		}
		else
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			diseaseTick=DISEASE_DELAY();
			mob.location().show(mob,null,CMMsg.MSG_NOISE,DISEASE_AFFECT());
			return true;
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-5);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected==null) return super.okMessage(myHost,msg);
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(((msg.amITarget(mob))||(msg.amISource(mob)))
			&&(msg.tool()!=null)
			&&(msg.tool().ID().equals("Social"))
			&&(msg.tool().Name().equals("MATE <T-NAME>")
			||msg.tool().Name().equals("SEX <T-NAME>")))
			{
				msg.source().tell(mob,null,null,"<S-NAME> really do(es)n't feel like it.");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
