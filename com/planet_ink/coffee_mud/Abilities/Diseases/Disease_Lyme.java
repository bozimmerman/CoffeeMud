package com.planet_ink.coffee_mud.Abilities.Diseases;
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

public class Disease_Lyme extends Disease
{
	public String ID() { return "Disease_Lyme"; }
	public String name(){ return "Lyme Disease";}
	public String displayText(){ return "(Lyme Disease)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}

	protected int DISEASE_TICKS(){return new Long(9*CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)).intValue();}
	protected int DISEASE_DELAY(){return new Long(CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)).intValue();}
	protected String DISEASE_DONE(){return "Your lyme disease goes away.";}
	protected String DISEASE_START(){return "^G<S-NAME> get(s) lyme disease!^?";}
	protected String DISEASE_AFFECT(){return "";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION|DiseaseAffect.SPREAD_DAMAGE;}
	public int difficultyLevel(){return 5;}
	int days=0;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(mob))
		&&(days>0)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(mob.fetchAbility(msg.tool().ID())==msg.tool())
		&&(Dice.rollPercentage()>(mob.charStats().getSave(CharStats.SAVE_MIND)+25)))
		{
			mob.tell("Your headaches make you forget "+msg.tool().name()+"!");
			return false;
		}

		return super.okMessage(myHost,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((!mob.amDead())&&(getTickDownRemaining()==1))
		{
			MOB diseaser=invoker;
			if(diseaser==null) diseaser=mob;
			Ability A=null;
			if(Dice.rollPercentage()>50)
				A=CMClass.getAbility("Disease_Fever");
			else
			if(Dice.rollPercentage()>50)
				A=CMClass.getAbility("Disease_Amnesia");
			else
			if(Dice.rollPercentage()>50)
				A=CMClass.getAbility("Disease_Arthritis");
			else
				A=CMClass.getAbility("Disease_Fever");
			if(A!=null) A.invoke(diseaser,mob,true,0);
			A=mob.fetchEffect(A.ID());
			if(A!=null) A.makeLongLasting();
		}
		else
		if((!mob.amDead())&&((--diseaseTick)<=0))
		{
			days++;
			diseaseTick=DISEASE_DELAY();
			if(Dice.rollPercentage()<mob.charStats().getSave(CharStats.SAVE_DISEASE))
			{
				unInvoke();
				return false;
			}
			return true;
		}
		return true;
	}
}
