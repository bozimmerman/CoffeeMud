package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Sleep extends Spell
{
	public String ID() { return "Spell_Sleep"; }
	public String name(){return "Sleep";}
	public String displayText(){return "(Sleep spell)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_Sleep();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((msg.amISource(mob))
		&&(!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
		&&(msg.sourceMajor()>0))
		{
			mob.tell("You are way too drowsy.");
			return false;
		}
		return super.okMessage(myHost,msg);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SLEEPING);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			if((!mob.amDead())&&(mob.location()!=null))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> do(es)n't seem so drowsy any more.");
			ExternalPlay.standIfNecessary(mob);
		}
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// sleep has a 3 level difference for PCs, so check for this.
		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff>=3)
		{
			mob.tell(target.charStats().HeShe()+" looks too powerful.");
			return false;
		}

		if(target.isInCombat())
		{
			mob.tell(target.name()+" is in combat, and would not be affected.");
			return false;
		}

		// if they can't hear the sleep spell, it
		// won't happen
		if((!auto)&&(!Sense.canBeHeardBy(mob,target)))
		{
			mob.tell(target.charStats().HeShe()+" can't hear your words.");
			return false;
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(-((target.charStats().getStat(CharStats.INTELLIGENCE)*2)),auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> whisper(s) to <T-NAMESELF>.^?");
			MOB oldVictim=mob.getVictim();
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					success=maliciousAffect(mob,target,3,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0));
					if(success)
						if(target.location()==mob.location())
							target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> fall(s) asleep!!");
				}
				if(oldVictim==null)	mob.setVictim(null);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> whisper(s) to <T-NAMESELF>, but the spell fades.");

		// return whether it worked
		return success;
	}
}
