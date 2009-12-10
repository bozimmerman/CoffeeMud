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
public class Spell_Frost extends Spell
{
	public String ID() { return "Spell_Frost"; }
	public String name(){return "Frost";}
	public String displayText(){return "(Frost)";}
	public int maxRange(){return adjustedMaxInvokerRange(2);}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;}
    public long flags(){return Ability.FLAG_WATERBASED;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
        Room R=CMLib.map().roomLocation(target);
        if(R==null) R=mob.location();

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),((auto?"A ":"^S<S-NAME> incant(s) and point(s) at <T-NAMESELF>. A ")+"blast of frost erupts!^?")+CMProps.msp("spelldam1.wav",40));
			CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_COLD|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((R.okMessage(mob,msg))&&(R.okMessage(mob,msg2)))
			{
				R.send(mob,msg);
				invoker=mob;

				int damage = 0;
				int maxDie =  (adjustedLevel(mob,asLevel)+(2*super.getX1Level(mob)))/4;
				damage += CMLib.dice().roll(maxDie,6,5);
				R.send(mob,msg2);
				if((msg2.value()>0)||(msg.value()>0))
					damage = (int)Math.round(CMath.div(damage,2.0));

				if(target.location()==R)
					CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,"The frost <DAMAGE> <T-NAME>!");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) and point(s) at <T-NAMESELF>, but flub(s) the spell.");


		// return whether it worked
		return success;
	}
}
