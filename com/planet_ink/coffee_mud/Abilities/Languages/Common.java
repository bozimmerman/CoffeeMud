package com.planet_ink.coffee_mud.Abilities.Languages;
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

@SuppressWarnings("unchecked")
public class Common extends StdLanguage
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
					CMLib.ableMapper().addCharAbilityMapping("All",1,ID(),100,true);}
		proficiency=100;
	}
	public int proficiency(){return 100;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		boolean anythingDone=false;
		for(int a=0;a<mob.numAllEffects();a++)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)&&(A instanceof Language))
				if(((Language)A).beingSpoken(ID()))
				{
					anythingDone=true;
					((Language)A).setBeingSpoken(ID(),false);
				}

		}
		isAnAutoEffect=false;
		if(!auto)
		{
			String msg=null;
			if(!anythingDone)
				msg="already speaking "+name()+".";
			else
				msg="now speaking "+name()+".";
			mob.tell("You are "+msg);
			if((mob.isMonster())&&(mob.amFollowing()!=null))
				CMLib.commands().postSay(mob,"I am "+msg);
		}
		return true;
	}
}
