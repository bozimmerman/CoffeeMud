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
public class Spell_MagicMissile extends Spell
{
	public String ID() { return "Spell_MagicMissile"; }
	public String name(){return "Magic Missile";}
	public String displayText(){return "(Magic Missile spell)";}
	public int maxRange(){return 1;}
	public int quality(){return MALICIOUS;};
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

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

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			int numMissiles=((int)Math.round(Math.floor(Util.div(adjustedLevel(mob,asLevel),5)))+1);
			for(int i=0;i<numMissiles;i++)
			{
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(i==0)?((auto?"A magic missle appears hurling full speed at <T-NAME>!":"^S<S-NAME> point(s) at <T-NAMESELF>, shooting forth a magic missile!^?")+CommonStrings.msp("spelldam2.wav",40)):null);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					if(msg.value()<=0)
					{
						int damage = 0;
						damage += Dice.roll(1,11,11/numMissiles);
						if(target.location()==mob.location())
							MUDFight.postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,((i==0)?"^SThe missile ":"^SAnother missile ")+"<DAMAGE> <T-NAME>!^?");
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
