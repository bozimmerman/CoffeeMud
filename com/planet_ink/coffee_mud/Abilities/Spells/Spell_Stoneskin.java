package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Stoneskin extends Spell
{
	public String ID() { return "Spell_Stoneskin"; }
	public String name(){return "Stoneskin";}
	public String displayText(){return "(Stoneskin)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_Stoneskin();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;}

	int HitsRemaining=0;
	int oldHP=-1;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setArmor(affectableStats.armor() - 10);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> skin softens.");
	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(msg.amITarget(mob)
		&&(msg.tool()!=null)
		&&(!mob.amDead())
		&&(Dice.rollPercentage()>75)
		&&(Util.bset(msg.targetCode(),CMMsg.MASK_HURT)&&((msg.targetCode()-CMMsg.MASK_HURT)>0)&&(msg.tool() instanceof Weapon)))
		{
			msg.modify(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
			msg.addTrailerMsg(new FullMsg((MOB)msg.target(),msg.source(),CMMsg.MSG_OK_VISUAL,"The stone skin around <S-NAME> absorbs the attack from <T-NAME>."));
			if((--HitsRemaining)<=0)
				unInvoke();
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;

			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> skin turn hard as stone!");
				HitsRemaining=5+(int)Math.round(adjustedLevel(mob)/2);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell, but fail(s) miserably.");

		// return whether it worked
		return success;
	}
}