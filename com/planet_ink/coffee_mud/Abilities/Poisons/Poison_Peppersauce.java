package com.planet_ink.coffee_mud.Abilities.Poisons;
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

public class Poison_Peppersauce extends Poison
{
	public String ID() { return "Poison_Peppersauce"; }
	public String name(){ return "Peppersauce";}
	private static final String[] triggerStrings = {"POISONSAUCE"};
	public String[] triggerStrings(){return triggerStrings;}

	protected int POISON_TICKS(){return 10;} // 0 means no adjustment!
	protected int POISON_DELAY(){return 1;}
	protected String POISON_DONE(){return "Your eyes clear up.";}
	protected String POISON_START(){return "^G<S-NAME> go(es) blind!^?";}
	protected String POISON_AFFECT(){return "";}
	protected String POISON_CAST(){return "^F^<FIGHT^><S-NAME> poison(s) <T-NAMESELF>!^</FIGHT^>^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return 0;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected instanceof MOB)
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-1);
		if(affectableStats.getStat(CharStats.CONSTITUTION)<=0)
			affectableStats.setStat(CharStats.CONSTITUTION,1);
	}
}
