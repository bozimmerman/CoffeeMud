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

public class Disease_Yawning extends Disease
{
	public String ID() { return "Disease_Yawning"; }
	public String name(){ return "Yawning";}
	public String displayText(){ return "(Yawning)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}

	protected int DISEASE_TICKS(){return 30;}
	protected int DISEASE_DELAY(){return 3;}
	protected String DISEASE_DONE(){return "You stop yawning.";}
	protected String DISEASE_START(){return "^G<S-NAME> feel(s) really tired.^?";}
	protected String DISEASE_AFFECT(){return "<S-NAME> stretch(es) and yawn(s).";}
	public int abilityCode(){return DiseaseAffect.SPREAD_PROXIMITY;}
	public int difficultyLevel(){return 0;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(affected==null)
			return false;

		if(!(affected instanceof MOB))
			return true;

		MOB mob=(MOB)affected;
		MOB diseaser=invoker;
		if(diseaser==null) diseaser=mob;
		if((getTickDownRemaining()==1)
		&&(!mob.amDead())
		&&(!Sense.isSleeping(mob))
		&&(Dice.rollPercentage()>mob.charStats().getSave(CharStats.SAVE_DISEASE)))
		{
			mob.delEffect(this);
			Ability A=CMClass.getAbility("Disease_Yawning");
			A.invoke(diseaser,mob,true,0);
		}
		else
		if((!mob.amDead())
		&&((--diseaseTick)<=0)
		&&(!Sense.isSleeping(mob)))
		{
			diseaseTick=DISEASE_DELAY();
			FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_NOISE,DISEASE_AFFECT());
			if((mob.location()!=null)&&(mob.location().okMessage(mob,msg)))
				mob.location().send(mob,msg);
			catchIt(mob);
			return true;
		}
		return true;
	}
}
