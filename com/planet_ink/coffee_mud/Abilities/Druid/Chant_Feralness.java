package com.planet_ink.coffee_mud.Abilities.Druid;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Feralness extends Chant
{
	public String ID() { return "Chant_Feralness"; }
	public String name(){ return "Feralness";}
	public String displayText(){return "(Feralness)";}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public Environmental newInstance(){	return new Chant_Feralness();}
	int hpAdjustment=0;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affected instanceof MOB)&&(((MOB)affected).charStats().getMyRace()!=((MOB)affected).baseCharStats().getMyRace()))
		{
			if((((MOB)affected).fetchWieldedItem()==null))
			{
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment() + 50);
				affectableStats.setDamage(affectableStats.damage()*2);
			}
			affectableStats.setDamage(affectableStats.damage()+(int)Math.round(Util.div(affectableStats.damage(),4.0)));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round(Util.div(affectableStats.attackAdjustment(),4.0)));
			affectableStats.setArmor(affectableStats.armor()+20);
		}
	}

	public void affectCharState(MOB affectedMOB, CharState affectedMaxState)
	{
		super.affectCharState(affectedMOB,affectedMaxState);
		if((affected instanceof MOB)&&(((MOB)affected).charStats().getMyRace()!=((MOB)affected).baseCharStats().getMyRace()))
			affectedMaxState.setHitPoints(affectedMaxState.getHitPoints()+hpAdjustment);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected instanceof MOB)
		&&(!Druid_ShapeShift.isShapeShifted((MOB)affected)))
			unInvoke();
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		int lostpoints=mob.maxState().getHitPoints()-mob.curState().getHitPoints();
		super.unInvoke();
		if(canBeUninvoked())
		{
			mob.tell("You don't feel quite so feral.");
			if(lostpoints>=mob.curState().getHitPoints())
				mob.curState().setHitPoints(1);
			else
				mob.curState().adjHitPoints(-lostpoints,mob.maxState());
		}
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			target.tell("You are already feral.");
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
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"<T-NAME> go(es) feral!":"^S<S-NAME> chant(s) to <S-NAMESELF> and become(s) feral!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!Druid_ShapeShift.isShapeShifted(mob))
				{
					Ability A=mob.fetchAbility("Druid_ShapeShift");
					if(A!=null) A.invoke(mob,new Vector(),null,false);
				}
				if(!Druid_ShapeShift.isShapeShifted(mob))
				{
					mob.tell("You failed to shapeshift.");
					return false;
				}
				hpAdjustment=(int)Math.round(Util.div(target.maxState().getHitPoints(),5.0));
				success=beneficialAffect(mob,target,0);
				target.curState().setHitPoints(target.curState().getHitPoints()+hpAdjustment);
				target.recoverMaxState();
				target.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to <S-NAMESELF>, but nothing happens");

		// return whether it worked
		return success;
	}
}
