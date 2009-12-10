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
public class Spell_ElementalStorm extends Spell
{
	public String ID() { return "Spell_ElementalStorm"; }
	public String name(){return "Elemental Storm";}
	public String displayText(){return "";}
	public int maxRange(){return adjustedMaxInvokerRange(1);}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		int[] types={CMMsg.TYP_FIRE,
					 CMMsg.TYP_COLD,
					 CMMsg.TYP_ACID,
					 CMMsg.TYP_WATER,
					 CMMsg.TYP_ELECTRIC,
					 CMMsg.TYP_GAS};
		int[] dames={Weapon.TYPE_BURNING,
					 Weapon.TYPE_FROSTING,
					 Weapon.TYPE_MELTING,
					 Weapon.TYPE_BURSTING,
					 Weapon.TYPE_STRIKING,
					 Weapon.TYPE_GASSING};
		String[] ds={"A flame",
					 "Some frost",
					 "Drops of acid",
					 "Stream of water",
					 "A spark",
					 "A puff of gas"};
		if(success)
		{
			int numMissiles=types.length;
			for(int i=0;i<numMissiles;i++)
			{
				CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),(i==0)?((auto?"An elemental storm assaults <T-NAME>!":"^S<S-NAME> point(s) at <T-NAMESELF>, evoking an elemental storm!^?")+CMProps.msp("spelldam1.wav",40)):null);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					if(msg.value()<=0)
					{
						int damage = 0;
						damage += CMLib.dice().roll(1,3+(adjustedLevel(mob,asLevel)+(2*super.getX1Level(mob)))/10,0);
						if(target.location()==mob.location())
							CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|types[i],dames[i],"^S"+ds[i]+" <DAMAGE> <T-NAME>!^?");
					}
				}
				if(target.amDead())
				{
					target=this.getTarget(mob,commands,givenTarget,true,false);
					if(target==null)
						break;
					if(target.amDead())
						break;
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF>, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
