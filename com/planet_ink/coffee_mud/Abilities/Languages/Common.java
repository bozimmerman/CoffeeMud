package com.planet_ink.coffee_mud.Abilities.Languages;
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

public class Common extends Language
{
	public String ID() { return "Common"; }
	public String name(){ return "Common";}
	public boolean isAutoInvoked(){return false;}
	public boolean canBeUninvoked(){return canBeUninvoked;}
	private static boolean mapped=false;
	public Common()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),100,true);}
		profficiency=100;
	}
	public int profficiency(){return 100;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		for(int a=0;a<mob.numAllEffects();a++)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)&&(A instanceof Language))
				((Language)A).setBeingSpoken(false);

		}
		isAnAutoEffect=false;
		if(!auto)
			mob.tell("You are now speaking "+name()+".");
		return true;
	}
}
