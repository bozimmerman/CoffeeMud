package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_KnowAlignment extends Spell
{
	public String ID() { return "Spell_KnowAlignment"; }
	public String name(){return "Know Alignment";}
	public Environmental newInstance(){	return new Spell_KnowAlignment();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target==mob) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		// it worked, so build a copy of this ability,
		// and add it to the affects list of the
		// affected MOB.  Then tell everyone else
		// what happened.
		FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^SYou draw out <T-NAME>s disposition.^?",affectType(auto),auto?"":"^S<S-NAME> draw(s) out your disposition.^?",affectType(auto),auto?"":"^S<S-NAME> draws out <T-NAME>s disposition.^?");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(success)
				mob.tell(mob,target,null,"<T-NAME> seem(s) like <T-HE-SHE> is "+CommonStrings.alignmentStr(target.getAlignment())+".");
			else
			{
				MOB newMOB=(MOB)CMClass.getMOB("StdMOB");
				newMOB.setAlignment(Dice.rollPercentage()*10);
				mob.tell(mob,target,null,"<T-NAME> seem(s) like <T-HE-SHE> is "+CommonStrings.alignmentStr(newMOB.getAlignment())+".");
			}
		}


		// return whether it worked
		return success;
	}
}
