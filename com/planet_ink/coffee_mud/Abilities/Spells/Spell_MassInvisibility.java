package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_MassInvisibility extends Spell
{
	public String ID() { return "Spell_MassInvisibility"; }
	public String name(){return "Mass Invisibility";}
	public String displayText(){return "";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return 0;}
	public Environmental newInstance(){	return new Spell_MassInvisibility();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		HashSet h=properTargets(mob,givenTarget,false);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth making invisible.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> arms and speak(s) softly.^?"))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fade(s) from view!");
					Spell_Invisibility spell=new Spell_Invisibility();
					spell.setProfficiency(profficiency());
					spell.beneficialAffect(mob,target,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> wave(s) <S-HIS-HER> arms and speak(s) softly, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}