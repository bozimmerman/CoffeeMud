package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_MassSlow extends Spell
{
	public String ID() { return "Spell_MassSlow"; }
	public String name(){return "Mass Slow";}
	public String displayText(){return "";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return 0;}
	public Environmental newInstance(){	return new Spell_MassSlow();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ALTERATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		HashSet h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth slowing down.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,-20,auto);

		if(success)
		{
			if(mob.location().show(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> whisper(s) and wave(s) <S-HIS-HER> arms.^?"))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// if they can't hear the sleep spell, it
				// won't happen
				if(Sense.canBeHeardBy(mob,target))
				{
					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					MOB oldVictim=mob.getVictim();
					FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
					if((mob.location().okMessage(mob,msg))&&(target.fetchEffect(this.ID())==null))
					{
						mob.location().send(mob,msg);
						if(msg.value()<=0)
						{
							Spell_Slow spell=new Spell_Slow();
							spell.setProfficiency(profficiency());
							success=spell.maliciousAffect(mob,target,2,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0));
							if(success)
								target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> move(s) more slowly!!");
						}
					}
					if(oldVictim==null) mob.setVictim(null);
				}
				else
					maliciousFizzle(mob,target,"<T-NAME> seem(s) unaffected by the Slow spell from <S-NAME>.");
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> whisper(s) a spell slowly, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}