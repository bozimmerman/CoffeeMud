package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Charge extends StdAbility
{
	public String ID() { return "Fighter_Charge"; }
	public String name(){ return "Charge";}
	private static final String[] triggerStrings = {"CHARGE"};
	public int quality(){return Ability.MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Fighter_Charge();}
	public int classificationCode(){return Ability.SKILL;}
	public int minRange(){return 1;}
	public int maxRange(){return 2;}
	public boolean done=false;

	public void affect(Environmental myHost, Affect msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.targetMinor()==Affect.TYP_WEAPONATTACK))
			done=true;
		super.affect(myHost,msg);
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.MOB_TICK)
			if(done) unInvoke();
		return super.tick(ticking,tickID);
	}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(2*affected.envStats().level()));
		affectableStats.setArmor(affectableStats.armor()+(2*affected.envStats().level()));
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

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.MASK_MALICIOUS|Affect.MSG_ADVANCE,"<S-NAME> charge(s) at <T-NAMESELF>!");
			if(mob.location().okAffect(mob,msg))
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
						ExternalPlay.postAttack(mob,target,mob.fetchWieldedItem());
					}
					else
						done=false;
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to charge, but then give(s) up.");

		// return whether it worked
		return success;
	}
}
