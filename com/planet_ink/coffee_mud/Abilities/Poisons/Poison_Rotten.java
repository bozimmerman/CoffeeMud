package com.planet_ink.coffee_mud.Abilities.Poisons;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
	    if(((affected instanceof Food)&&((((Food)affected).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_FLESH))
	    ||((affected instanceof Drink)
	    	&&(affected instanceof Item)
    		&&(((Item)affected).material()!=RawMaterial.RESOURCE_MILK)
    		&&(((Item)affected).material()!=RawMaterial.RESOURCE_BLOOD)))
			    return false;
        return true;
	}
	protected String POISON_START_TARGETONLY()
	{
	    if(((affected instanceof Food)&&((((Food)affected).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_FLESH))
	    ||((affected instanceof Drink)
	    	&&(affected instanceof Item)
    		&&(((Item)affected).material()!=RawMaterial.RESOURCE_MILK)
    		&&(((Item)affected).material()!=RawMaterial.RESOURCE_BLOOD)))
			    return "^G"+affected.name()+" was rotten! Blech!^?";
	    return "";
	}
	protected String POISON_START()
	{
	    if((affected instanceof Food)||(affected instanceof Drink))
		    return "^G"+affected.name()+" was rotten! <S-NAME> bend(s) over with horrid stomach pains!^?";
	    return "^G<S-NAME> bend(s) over with horrid stomach pains!^?";
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.amITarget(affected))
		&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
		&&(affected instanceof Item)
		&&(CMLib.flags().canSmell(msg.source())))
			msg.source().tell(msg.source(),affected,null,"<T-NAME> smell(s) rotten!");
	}
	protected String POISON_AFFECT(){return "^G<S-NAME> moan(s) and clutch(es) <S-HIS-HER> stomach.";}
	protected String POISON_CAST(){return "^F^<FIGHT^><S-NAME> poison(s) <T-NAMESELF>!^</FIGHT^>^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return CMLib.dice().roll(1,3,1);}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STAT_CONSTITUTION,affectableStats.getStat(CharStats.STAT_CONSTITUTION)-10);
		if(affectableStats.getStat(CharStats.STAT_CONSTITUTION)<=0)
			affectableStats.setStat(CharStats.STAT_CONSTITUTION,1);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)-8);
		if(affectableStats.getStat(CharStats.STAT_STRENGTH)<=0)
			affectableStats.setStat(CharStats.STAT_STRENGTH,1);
	}
}

