package com.planet_ink.coffee_mud.Abilities.Songs;

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
public class Song_Lightness extends Song
{
	public String ID() { return "Song_Lightness"; }
	public String name(){ return "Lightness";}
	public int quality(){ return MALICIOUS;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setWeight(0);
	}

	public int mobWeight(MOB mob)
	{
		int weight=mob.baseEnvStats().weight();
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)&&(!I.amWearingAt(Item.FLOATING_NEARBY)))
				weight+=I.envStats().weight();
		}
		return weight;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.targetMinor()==CMMsg.TYP_GET)
		&&(msg.target()!=null)
		&&(msg.target() instanceof Item)
		&&(((msg.tool()==null)||(msg.tool() instanceof MOB))))
		{
			MOB mob=msg.source();
			if((msg.target().envStats().weight()>(mob.maxCarry()-mobWeight(mob)))&&(!mob.isMine(msg.target())))
			{
				mob.tell(msg.target().name()+" is too heavy.");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your normal weight returns.");
	}
}
