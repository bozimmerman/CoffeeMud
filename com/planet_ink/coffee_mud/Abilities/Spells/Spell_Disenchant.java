package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_Disenchant extends Spell
{
	public String ID() { return "Spell_Disenchant"; }
	public String name(){return "Disenchant";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_EVOCATION;	}


	public static int disenchantItem(Item target)
	{
		int level=target.baseEnvStats().level();
		boolean doneSomething=false;
		if(target instanceof Wand)
		{
			Ability A=((Wand)target).getSpell();
			if(A!=null)
				level=level-CMAble.lowestQualifyingLevel(A.ID())+2;
			((Wand)target).setSpell(null);
			((Wand)target).setUsesRemaining(0);
			doneSomething=true;
		}
		else
		if(target instanceof Scroll)
		{
			((Scroll)target).setSpellList(new Vector());
			((Scroll)target).setScrollText("");
			doneSomething=true;
		}
		else
		if(target instanceof Potion)
		{
			((Potion)target).setSpellList("");
			doneSomething=true;
		}
		else
		if(target instanceof Pill)
		{
			((Pill)target).setSpellList("");
			doneSomething=true;
		}
		else
		if(target.envStats().ability()>0)
		{
			level=level-(target.baseEnvStats().ability()*3);
			target.baseEnvStats().setAbility(0);
			doneSomething=true;
		}

		Vector affects=new Vector();
		for(int a=target.numEffects()-1;a>=0;a--)
		{
			Ability A=target.fetchEffect(a);
			if(A!=null)
				affects.addElement(A);
		}
		for(int a=0;a<affects.size();a++)
		{
			Ability A=(Ability)affects.elementAt(a);
			A.unInvoke();
			level=level-1;
			target.delEffect(A);
			doneSomething=true;
		}
		if(doneSomething) return level;
		return -999;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> hold(s) <T-NAMESELF> and cast(s) a spell.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int level=disenchantItem(target);
				if(level>-999)
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> fades and becomes dull!");
					if((target.baseEnvStats().disposition()&EnvStats.IS_BONUS)==EnvStats.IS_BONUS)
						target.baseEnvStats().setDisposition(target.baseEnvStats().disposition()-EnvStats.IS_BONUS);
					if(level<=0) level=1;
					target.baseEnvStats().setLevel(level);
					target.recoverEnvStats();
				}
				else
					mob.tell(target.name()+" doesn't seem to be enchanted.");
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> hold(s) <T-NAMESELF> and whisper(s), but fail(s) to cast a spell.");


		// return whether it worked
		return success;
	}
}
