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

public class Poison_Rotten extends Poison
{
	public String ID() { return "Poison_Rotten"; }
	public String name(){ return "Rotten";}
	private static final String[] triggerStrings = {"POISONROTT"};
	public String[] triggerStrings(){return triggerStrings;}

	protected int POISON_TICKS(){return 50;} // 0 means no adjustment!
	protected int POISON_DELAY(){return 5;}
	protected boolean POISON_AFFECTTARGET()
	{
	    if((affected instanceof Food)&&((((Food)affected).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_FLESH))
		    return false;
	    else
	        return true;
	}
	protected String POISON_START_TARGETONLY()
	{
	    if((affected instanceof Food)&&((((Food)affected).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_FLESH))
		    return "^G"+affected.name()+" was rotten! Blech!^?";
	    else
		    return "";
	}
	protected String POISON_START()
	{
	    if(affected instanceof Food)
		    return "^G"+affected.name()+" was rotten! <S-NAME> bend(s) over with horrid stomach pains!^?";
	    else
		    return "^G<S-NAME> bend(s) over with horrid stomach pains!^?";
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.amITarget(affected))
		&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
		&&(affected instanceof Item)
		&&(Sense.canSmell(msg.source())))
			msg.source().tell(msg.source(),affected,null,"<T-NAME> smell(s) rotten!");
	}
	protected String POISON_AFFECT(){return "^G<S-NAME> moan(s) and clutch(es) <S-HIS-HER> stomach.";}
	protected String POISON_CAST(){return "^F^<FIGHT^><S-NAME> poison(s) <T-NAMESELF>!^</FIGHT^>^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return Dice.roll(1,3,1);}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.CONSTITUTION,affectableStats.getStat(CharStats.CONSTITUTION)-10);
		if(affectableStats.getStat(CharStats.CONSTITUTION)<=0)
			affectableStats.setStat(CharStats.CONSTITUTION,1);
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)-8);
		if(affectableStats.getStat(CharStats.STRENGTH)<=0)
			affectableStats.setStat(CharStats.STRENGTH,1);
	}
}

