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

public class Disease_Vampirism extends Disease
{
	public String ID() { return "Disease_Vampirism"; }
	public String name(){ return "Vampirism";}
	public String displayText(){ return "(Vampirism)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}

	protected int DISEASE_TICKS(){return new Long(CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)*6).intValue();}
	protected int DISEASE_DELAY(){return new Long(CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)).intValue();}
	protected String DISEASE_DONE(){return "Your vampirism lifts.";}
	protected String DISEASE_START(){return "^G<S-NAME> seem(s) pale and cold.^?";}
	protected String DISEASE_AFFECT(){return "";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION;}
	public int difficultyLevel(){return 9;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!(affected instanceof MOB)) return;
		if(((MOB)affected).location()==null) return;
		if(Sense.isInDark(((MOB)affected).location()))
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_DARK);
		else
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(msg.amISource(mob)
			   &&(msg.tool()!=null)
			   &&(msg.tool().ID().equals("Skill_Swim")))
			{
				mob.tell("You can't swim!");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setStat(CharStats.CHARISMA,affectableStats.getStat(CharStats.CHARISMA)+1);
	}
}