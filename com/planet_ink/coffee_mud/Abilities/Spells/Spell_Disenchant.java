package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_Disenchant extends Spell
{
	public String ID() { return "Spell_Disenchant"; }
	public String name(){return "Disenchant";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;	}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public static int disenchantItem(Item target)
	{
		int level=target.baseEnvStats().level();
		boolean doneSomething=false;
		if(target instanceof Wand)
		{
			Ability A=((Wand)target).getSpell();
			if(A!=null)
				level=level-CMLib.ableMapper().lowestQualifyingLevel(A.ID())+2;
			((Wand)target).setSpell(null);
			((Wand)target).setUsesRemaining(0);
			doneSomething=true;
		}
		else
		if(target instanceof SpellHolder)
		{
			((SpellHolder)target).setSpellList("");
			doneSomething=true;
		}
		else
		if((target.envStats().ability()>0)
		&&(!(target instanceof Coins)))
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


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> hold(s) <T-NAMESELF> and cast(s) a spell.^?");
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
