package com.planet_ink.coffee_mud.Abilities.Songs;
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
public class Skill_FireBreathing extends BardSkill
{
	public String ID() { return "Skill_FireBreathing"; }
	public String name(){ return "Fire Breathing";}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	private static final String[] triggerStrings = {"FIREBREATHING","FIREBREATH"};
	public String[] triggerStrings(){return triggerStrings;}
	public int maxRange(){return adjustedMaxInvokerRange(5);}
	public int minRange(){return 0;}

	public Item getFireSource(MOB mob)
	{
        for(int i=0;i<mob.inventorySize();i++)
        {
            Item I=mob.fetchInventory(i);
            if((CMLib.flags().isOnFire(I))
            &&(!I.amWearingAt(Wearable.IN_INVENTORY))
            &&(I.container()==null))
                return I;
        }
        return null;
	}
	
    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(getFireSource(mob)==null)
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		Item fireSource=getFireSource(mob);
		if((!auto)&&(fireSource==null))
		{
			mob.tell("You need to be holding some fire source to breathe fire.");
			return false;
		}
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_NOISYMOVEMENT|(auto?CMMsg.MASK_ALWAYS:0),(auto?"Suddenly flames come up and attack <T-HIM-HER>!^?":((fireSource!=null)?"^S<S-NAME> hold(s) "+fireSource.name()+" up and puff(s) fire at <T-NAMESELF>!^?":"<S-NAME> breath(es) fire at <T-NAMESELF>!^?"))+CMProps.msp("fireball.wav",40));
			CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_FIRE|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((mob.location().okMessage(mob,msg))&&((mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
                int numDice = (int)Math.round(CMath.div(adjustedLevel(mob,asLevel)+(2*super.getX1Level(mob)),2.0))+1;
				int damage = CMLib.dice().roll(3, numDice, 0);
				if(msg2.value()>0)
					damage = (int)Math.round(CMath.div(damage,2.0));

				if(target.location()==mob.location())
					CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The flames <DAMAGE> <T-NAME>!");
			}
			if(fireSource!=null)
				fireSource.destroy();
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to breathe fire at <T-NAMESELF>, but only puff(s) smoke.");


		// return whether it worked
		return success;
	}
}
