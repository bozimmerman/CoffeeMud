package com.planet_ink.coffee_mud.Abilities.Paladin;
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

public class Paladin_Goodness extends Paladin
{
	public String ID() { return "Paladin_Goodness"; }
	public String name(){ return "Paladin`s Goodness";}
	private boolean tickTock=false;
	public Paladin_Goodness()
	{
		super();
		paladinsGroup=new Vector();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		tickTock=!tickTock;
		if(tickTock)
		{
			MOB mob=invoker;
			for(int m=0;m<mob.location().numInhabitants();m++)
			{
				MOB target=mob.location().fetchInhabitant(m);
				if((target!=null)
				&&(Sense.isEvil(target))
				&&((paladinsGroup.contains(target))
					||((target.getVictim()==invoker)&&(target.rangeToTarget()==0)))
			    &&((invoker==null)||(invoker.fetchAbility(ID())==null)||profficiencyCheck(null,0,false)))
				{
					int harming=Dice.roll(1,15,0);
					if(Sense.isEvil(target))
						MUDFight.postDamage(invoker,target,this,harming,CMMsg.MASK_EYES|CMMsg.MASK_MALICIOUS|CMMsg.TYP_UNDEAD,Weapon.TYPE_BURSTING,"^SThe aura of goodness around <S-NAME> <DAMAGE> <T-NAME>!^?");
				}
			}
		}
		return true;
	}

}
