package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.interfaces.*;

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

public class Disease_Aids extends Disease
{
	public String ID() { return "Disease_Aids"; }
	public String name(){ return "Aids";}
	public String displayText(){ return "(Aids)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public int difficultyLevel(){return 9;}

	protected int DISEASE_TICKS(){return 999999;}
	protected int DISEASE_DELAY(){return 50;}
	protected String DISEASE_DONE(){return "Your aids goes into remission.";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) like <S-HE-SHE> <S-IS-ARE> sick.^?";}
	protected String DISEASE_AFFECT(){return "";}
	public int abilityCode(){return DiseaseAffect.SPREAD_STD;}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if(affected==null) return;
		affectableStats.setStat(CharStats.SAVE_DISEASE,affectableStats.getStat(CharStats.SAVE_DISEASE)-200);
	}

}
