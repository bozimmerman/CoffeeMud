package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Spell_MinorGlobe extends Spell
{
	public String ID() { return "Spell_MinorGlobe"; }
	public String name(){return "Globe";}
	public String displayText(){return "(Invulnerability Globe)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ABJURATION;}

	int amountAbsorbed=0;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> great anti-magic globe fades.");

		super.unInvoke();

	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.SPELL)
			||((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.CHANT)
			||((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.PRAYER))
		&&(!mob.amDead())
		&&(CMAble.lowestQualifyingLevel(msg.tool().ID())<=8)
		&&((mob.fetchAbility(ID())==null)||profficiencyCheck(null,0,false)))
		{
			amountAbsorbed+=CMAble.lowestQualifyingLevel(msg.tool().ID());
			mob.location().show(mob,msg.source(),null,CMMsg.MSG_OK_VISUAL,"The absorbing globe around <S-NAME> absorbs the "+msg.tool().name()+" from <T-NAME>.");
			return false;
		}
		if((invoker!=null)&&(amountAbsorbed>(invoker.envStats().level()*2)))
			unInvoke();
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),(auto?"An anti-magic field envelopes <T-NAME>!":"^S<S-NAME> invoke(s) an anti-magic globe of protection around <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				amountAbsorbed=0;
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke an anti-magic globe, but fail(s).");

		return success;
	}
}
