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
public class Play_Ditty extends Play
{
	public String ID() { return "Play_Ditty"; }
	public String name(){ return "Ditty";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected String songOf(){return "a "+name();}
	public long flags(){return Ability.FLAG_HEALING;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected!=null)&&(affected instanceof MOB)&&(invoker()!=null))
		{
			MOB mob=(MOB)affected;
			int healing=invoker().charStats().getStat(CharStats.CHARISMA)/4;
			MUDFight.postHealing(invoker(),mob,this,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,healing,null);
		}
		return true;
	}
}
