package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

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

public class Disease_Obesity extends Disease
{
	public String ID() { return "Disease_Obesity"; }
	public String name(){ return "Obesity";}
	public String displayText()
	{
	    int amount=Util.s_int(text());
	    int weight=baseEnvStats().weight();
	    if(amount<weight/10)
		    return "(Fat)";
	    else
	    if(amount<weight/50)
		    return "(Obese)";
	    else
		    return "(Morbose obesity)";
	}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public int difficultyLevel(){return 10;}

	protected int DISEASE_TICKS(){return 999999;}
	protected int DISEASE_DELAY(){return 50;}
	protected String DISEASE_DONE(){return "You've become fit and trim!";}
	protected String DISEASE_START(){return "^G<S-NAME> look(s) like <S-HE-SHE> <S-HAS-HAVE> been gaining some weight.^?";}
	protected String DISEASE_AFFECT(){return "";}
	public int abilityCode(){return 0;}
	public boolean canBeUninvoked(){canBeUninvoked=false;return super.canBeUninvoked();}
	
}
