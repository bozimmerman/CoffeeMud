package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Rockskin extends Prayer
{
	public String ID() { return "Prayer_Rockskin"; }
	public String name(){return "Rockskin";}
	public String displayText(){return "(Rockskin)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int holyQuality(){ return HOLY_NEUTRAL;}
	public Environmental newInstance(){	return new Prayer_Rockskin();}

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
			mob.tell("Your skins softens.");
	}


	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		   &&(Util.bset(affect.targetCode(),Affect.MASK_HURT))
		   &&((affect.targetCode()-Affect.MASK_HURT)>0))
		{
			if((affect.tool()!=null)
			&&(!mob.amDead())
			&&(affect.tool() instanceof Weapon))
			{
				affect.modify(affect.source(),affect.target(),affect.tool(),Affect.NO_EFFECT,null,Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
				affect.addTrailerMsg(new FullMsg((MOB)affect.target(),affect.source(),Affect.MSG_OK_VISUAL,"The rock skin around <S-NAME> absorbs the attack from <T-NAME>."));
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

			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" wave(s) <S-HIS-HER> hands around <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> skin turn hard as rock!");
				HitsRemaining=5+(int)Math.round(adjustedLevel(mob)/2);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+", but fail(s) miserably.");

		// return whether it worked
		return success;
	}
}