package com.planet_ink.coffee_mud.Abilities.Prayers;

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

public class Prayer_Christen extends Prayer
{
	public String ID() { return "Prayer_Christen"; }
	public String name(){ return "Christen";}
	protected int canTargetCode(){return 0;}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell("Christen whom what?");
			return false;
		}
		String name=((String)commands.lastElement()).trim();
		commands.removeElementAt(commands.size()-1);
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if((!(target instanceof CagedAnimal))||(target.envStats().ability()<=0)||(!target.isGeneric()))
		{
			mob.tell("You may only christen a child.");
			return false;
		}
		if(name.length()==0)
		{
			mob.tell("Christen "+target.name()+" what?");
			return false;
		}
		if(name.indexOf(" ")>=0)
		{
			mob.tell("The name may not have a space in it.");
			return false;
		}

		if(CMClass.DBEngine().DBUserSearch(null,name))
		{
			mob.tell("That name is already taken.  Please choose another.");
			return false;
		}

		name=Util.capitalize(name);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> becomes "+name+".":"^S<S-NAME> christen(s) <T-NAMESELF> '"+name+"'.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				String oldName=target.Name();
				target.setName(name);
				target.setDisplayText(name+" is here.");
				String txt=((CagedAnimal)target).cageText();
				txt=Util.replaceFirst(txt,"<NAME>"+oldName+"</NAME>","<NAME>"+name+"</NAME>");
				txt=Util.replaceFirst(txt,"<DISP>"+oldName,"<DISP>"+name);
				((CagedAnimal)target).setCageText(txt);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" over <T-NAME>, but lose(s) <S-HIS-HER> concentration.");


		// return whether it worked
		return success;
	}
}
