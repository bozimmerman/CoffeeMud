package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Frenzy extends Spell
{
	public int hpAdjustment=0;

	public Spell_Frenzy()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Frenzy";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Frenzy spell)";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.BENEFICIAL_OTHERS;

		baseEnvStats().setLevel(13);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Frenzy();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((invoker==null)&&(affected instanceof MOB))
			invoker=(MOB)affected;
		affectableStats.setDamage(affectableStats.damage()+(int)Math.round(Util.div(affectableStats.damage(),6.0)));
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round(Util.div(affectableStats.attackAdjustment(),6.0)));
		affectableStats.setArmor(affectableStats.armor()+20);
	}

	public void affectCharState(MOB affectedMOB, CharState affectedMaxState)
	{
		super.affectCharState(affectedMOB,affectedMaxState);
		if(affectedMOB!=null)
			affectedMaxState.setHitPoints(affectedMaxState.getHitPoints()+hpAdjustment);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked)
		{
			if(mob.curState().getHitPoints()<=hpAdjustment)
				mob.curState().setHitPoints(1);
			else
				mob.curState().adjHitPoints(-hpAdjustment,mob.maxState());
			mob.tell(mob,null,"You feel calmer.");
			mob.recoverMaxState();
		}
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
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> chant(s) to <T-NAMESELF>!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(target.location()==mob.location())
				{
					target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> go(es) wild!");
					hpAdjustment=(int)Math.round(Util.div(target.maxState().getHitPoints(),5.0));
					beneficialAffect(mob,target,0);
					target.curState().setHitPoints(target.curState().getHitPoints()+hpAdjustment);
					target.recoverMaxState();
					target.recoverEnvStats();
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) wildly to <T-NAMESELF>, but nothing more happens.");

		// return whether it worked
		return success;
	}
}
