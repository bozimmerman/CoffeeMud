package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_MassFly extends Spell
{
	public String ID() { return "Spell_MassFly"; }
	public String name(){return "Mass Fly";}
	public String displayText(){return "";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return 0;}
	public Environmental newInstance(){	return new Spell_MassFly();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=properTargets(mob,givenTarget,false);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth making fly.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> arms and speak(s).^?"))
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				if(mob.location().okAffect(mob,msg))
				{
					mob.location().send(mob,msg);
					if(mob.location()==target.location())
						target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> start(s) to fly around!");
					Spell_Fly fly=new Spell_Fly();
					fly.setProfficiency(profficiency());
					fly.beneficialAffect(mob,target,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> wave(s) <S-HIS-HER> arms and speak(s), but the spell fizzles.");


		// return whether it worked
		return success;
	}
}