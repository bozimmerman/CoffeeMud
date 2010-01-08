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
public class Spell_Enlarge extends Spell
{
	public String ID() { return "Spell_Enlarge"; }
	public String name(){return "Enlarge Object";}
	protected int canTargetCode(){return CAN_ITEMS;}
	private static final String addOnString=" of ENORMOUS SIZE!!!";
	public int classificationCode(){	return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setWeight(affectableStats.weight()+9999);
		affectableStats.setHeight(affectableStats.height()+9999);
		affectableStats.setName(affected.name()+addOnString);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((canBeUninvoked())&&(affected instanceof Item)&&(CMLib.flags().isInTheGame(affected,true)))
		{
			Item I=(Item)affected;
			if(I.owner() instanceof MOB)
				((MOB)I.owner()).tell(I.name()+" in your inventory shrinks back to size.");
			else
			{
				Room R=CMLib.map().roomLocation(I);
				if(R!=null)
					R.showHappens(CMMsg.MSG_OK_VISUAL,I.name()+" shrinks back to normal size.");
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;

		if(mob.isMine(target))
		{
			mob.tell("You'd better put it down first.");
			return false;
		}
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target.name()+" is already HUGE!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
                Ability A=target.fetchEffect("Spell_Shrink");
                if((A!=null)&&(A.canBeUninvoked()))
                    A.unInvoke();
                else
                {
    				mob.location().show(mob,target,CMMsg.MSG_OK_ACTION,"<T-NAME> grow(s) to an enormous size!");
    				beneficialAffect(mob,target,asLevel,100);
                }
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>, incanting but nothing happens.");


		// return whether it worked
		return success;
	}
}
