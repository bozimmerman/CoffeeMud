package com.planet_ink.coffee_mud.Abilities.Poisons;
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

public class Poison_Hives extends Poison
{
	public String ID() { return "Poison_Hives"; }
	public String name(){ return "Hives";}
	public String displayText(){ return "(Hives)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}

	private static final String[] triggerStrings = {"POISONHIVES"};
	public String[] triggerStrings(){return triggerStrings;}

	protected int POISON_TICKS(){return 60;} // 0 means no adjustment!
	protected int POISON_DELAY(){return 5;}
	protected String POISON_DONE(){return "The hives clear up.";}
	protected String POISON_START(){return "^G<S-NAME> break(s) out in hives!^?";}
	protected String POISON_AFFECT(){return "^G<S-NAME> scratch(es) <S-HIM-HERSELF> as more hives break out.";}
	protected String POISON_CAST(){return "^F^<FIGHT^><S-NAME> poison(s) <T-NAMESELF>!^</FIGHT^>^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return 0;}
	private int poisonTick=0;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))	return false;
		if(affected==null) return false;
		if(!(affected instanceof MOB)) return true;

		MOB mob=(MOB)affected;
		if((!mob.amDead())&&((--poisonTick)<=0))
		{
		    poisonTick=POISON_DELAY();
			mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,POISON_AFFECT());
			return true;
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if(affected==null) return;
		affectableStats.setStat(CharStats.CHARISMA,affectableStats.getStat(CharStats.CHARISMA)-1);
		affectableStats.setStat(CharStats.DEXTERITY,affectableStats.getStat(CharStats.DEXTERITY)-3);
		if(affectableStats.getStat(CharStats.CHARISMA)<=0)
			affectableStats.setStat(CharStats.CHARISMA,1);
		if(affectableStats.getStat(CharStats.DEXTERITY)<=0)
			affectableStats.setStat(CharStats.DEXTERITY,1);
	}
}

