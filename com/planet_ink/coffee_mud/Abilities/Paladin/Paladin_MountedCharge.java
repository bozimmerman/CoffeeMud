package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Paladin_MountedCharge extends StdAbility
{
	public String ID() { return "Paladin_MountedCharge"; }
	public String name(){ return "Mounted Charge";}
	private static final String[] triggerStrings = {"MOUNTEDCHARGE","MCHARGE"};
	public int quality(){return Ability.MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int classificationCode(){return Ability.SKILL;}
	public int usageType(){return USAGE_MOVEMENT;}
	public int minRange(){return 1;}
	public int maxRange(){return 99;}
	public boolean done=false;

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK))
			done=true;
		super.executeMsg(myHost,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
			if(done) unInvoke();
		return super.tick(ticking,tickID);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(4*affected.envStats().level()));
		affectableStats.setArmor(affectableStats.armor()+(4*affected.envStats().level()));
		affectableStats.setDamage(affectableStats.damage()+(affected.envStats().level()));
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		boolean notInCombat=!mob.isInCombat();
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((mob.isInCombat())
		&&(mob.rangeToTarget()<=0))
		{
			mob.tell("You can not charge while in melee!");
			return false;
		}

		if(mob.riding()==null)
		{
			mob.tell("You must be mounted to use this skill.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_ADVANCE,"<S-NAME> ride(s) hard at <T-NAMESELF>!");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(mob.getVictim()==target)
				{
					mob.setAtRange(0);
					target.setAtRange(0);
					beneficialAffect(mob,mob,2);
					mob.recoverEnvStats();
					if(notInCombat)
					{
						done=true;
						MUDFight.postAttack(mob,target,mob.fetchWieldedItem());
					}
					else
						done=false;
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> ride(s) at <T-NAMESELF>, but miss(es).");

		// return whether it worked
		return success;
	}
}
