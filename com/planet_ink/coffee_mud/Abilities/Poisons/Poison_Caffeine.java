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

public class Poison_Caffeine extends Poison {
	public String ID() { return "Poison_Caffeine"; }
	public String name(){ return "Poison_Hyper";}
	public String displayText(){ return "(CAFFEINATED!!)";}
	private static final String[] triggerStrings = {"POISONHYPER"};
	public String[] triggerStrings(){return triggerStrings;}

	protected int POISON_TICKS(){return 30;} // 0 means no adjustment!
	protected int POISON_DELAY(){return 5;}
	protected String POISON_DONE(){return "The caffeine runs its course.";}
	protected String POISON_START(){return "^G<S-NAME> seem(s) wired!^?";}
	protected String POISON_AFFECT(){return "^G<S-NAME> twitch(es) spastically.";}
	protected String POISON_CAST(){return "^F^<FIGHT^><S-NAME> caffeinate(s) <T-NAMESELF>!^</FIGHT^>^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to caffinate <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return 0;}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
	    affectableStats.setStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY)+1);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
	    super.affectEnvStats(affected,affectableStats);
		affectableStats.setSpeed(affectableStats.speed() + 0.25);
	    int oldDisposition=affectableStats.disposition();
	    oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
	    affectableStats.setDisposition(oldDisposition);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
	    if((affected==null)||(!(affected instanceof MOB)))
	            return true;

	    MOB mob=(MOB)affected;
	    if(msg.amISource(mob)&&((msg.sourceMinor()==CMMsg.TYP_SIT)||(msg.sourceMinor()==CMMsg.TYP_SLEEP))) {
	        mob.tell("You're too caffeinated for that!");
	        return false;
	    }
	    return true;
	}
}
