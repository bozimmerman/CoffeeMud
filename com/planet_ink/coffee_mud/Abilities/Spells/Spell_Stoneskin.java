package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Stoneskin extends Spell
{

	int HitsRemaining=0;
	int oldHP=-1;

	public Spell_Stoneskin()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Stoneskin";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Stoneskin)";

		quality=Ability.BENEFICIAL_OTHERS;

		baseEnvStats().setLevel(11);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Stoneskin();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;
	}


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

		mob.tell("Your skins softens.");
	}


	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))&&(Util.bset(affect.targetCode(),Affect.MASK_HURT)))
		{
			if((affect.tool()!=null)
			&&(!mob.amDead())
			&&(affect.tool() instanceof Weapon))
			{
				affect.modify(affect.source(),affect.target(),affect.tool(),Affect.NO_EFFECT,null,Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
				affect.addTrailerMsg(new FullMsg((MOB)affect.target(),affect.source(),Affect.MSG_OK_VISUAL,"The stone skin around <S-NAME> absorbs the attack from <T-NAME>."));
				if((--HitsRemaining)<=0)
					unInvoke();
			}
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

			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> skin turn hard as stone!");
				HitsRemaining=5+(int)Math.round(mob.envStats().level()/2);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell, but fail(s) miserably.");

		// return whether it worked
		return success;
	}
}