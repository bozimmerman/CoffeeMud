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
public class Spell_Sonar extends Spell
{
	public String ID() { return "Spell_Sonar"; }
	public String name(){return "Sonar";}
	public String displayText(){return "(Sonar)";}
	public int quality(){ return BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> sonar ears return to normal.");
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			MOB victim=mob.getVictim();
			if((victim==null)||(Sense.canBeHeardBy(victim,mob)))
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_VICTIM);
			if((victim==null)&&(Sense.canHear(mob)))
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_DARK);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> already <S-HAS-HAVE> sonar.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"<T-NAME> gain(s) sonar capability!":"^S<S-NAME> incant(s) softly, and <S-HIS-HER> ears become capable of sonar!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> incant(s) softly and listen(s), but the spell fizzles.");

		return success;
	}
}
