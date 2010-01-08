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
public class Spell_FloatingDisc extends Spell
{
	public String ID() { return "Spell_FloatingDisc"; }
	public String name(){return "Floating Disc";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	boolean wasntMine=false;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof Item)))
			return;
		if(invoker==null)
			return;

		MOB mob=invoker;
		Item item=(Item)affected;
		super.unInvoke();


		if(canBeUninvoked())
		{
			if(item.amWearingAt(Wearable.WORN_FLOATING_NEARBY))
			{
				mob.location().show(mob,item,CMMsg.MSG_OK_VISUAL,"<T-NAME> floating near <S-NAME> now floats back "+((wasntMine)?"down to the ground":"into <S-HIS-HER> hands."));
				item.unWear();
			}
			if(wasntMine)
				CMLib.commands().postDrop(mob,item,true,false);
			wasntMine=false;

			item.recoverEnvStats();
			mob.recoverMaxState();
			mob.recoverCharStats();
			mob.recoverEnvStats();
		}
	}

    public void executeMsg(Environmental host, CMMsg msg)
    {
        if((msg.target()==affected)
        &&(msg.targetMinor()==CMMsg.TYP_REMOVE))
            unInvoke();
    }

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setWeight(0);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;
		if((!(target instanceof Item))
		||(!CMLib.flags().isGettable(((Item)target))))
		{
			mob.tell("You cannot float "+target.name()+"!");
			return false;
		}

		if(mob.freeWearPositions(Wearable.WORN_FLOATING_NEARBY,(short)0,(short)0)==0)
		{
			mob.tell("There is no more room around you to float anything!");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			wasntMine=false;
			if(!mob.isMine(target))
			{
				target.addNonUninvokableEffect(this);
				target.recoverEnvStats();
				wasntMine=true;
				if(target instanceof Coins)
				{
					mob.location().delItem((Item)target);
					mob.addInventory((Item)target);
				}
				else
				if(!CMLib.commands().postGet(mob,null,(Item)target,true))
				{
					target.delEffect(this);
					target.recoverEnvStats();
					return false;
				}
				target.delEffect(this);
				target.recoverEnvStats();
			}
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> begin(s) to float around.":"^S<S-NAME> invoke(s) a floating disc underneath <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				long properWornCode=((Item)target).rawProperLocationBitmap();
				boolean properWornLogical=((Item)target).rawLogicalAnd();
				((Item)target).setRawLogicalAnd(false);
				((Item)target).setRawProperLocationBitmap(Wearable.WORN_FLOATING_NEARBY);
				((Item)target).wearAt(Wearable.WORN_FLOATING_NEARBY);
				((Item)target).setRawLogicalAnd(properWornLogical);
				((Item)target).setRawProperLocationBitmap(properWornCode);
				((Item)target).recoverEnvStats();
				beneficialAffect(mob,target,asLevel,(mob.envStats().level()+(2*getXLEVELLevel(mob)))*30);
				mob.recoverEnvStats();
				mob.recoverMaxState();
				mob.recoverCharStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a floating disc, but fail(s).");



		// return whether it worked
		return success;
	}
}
