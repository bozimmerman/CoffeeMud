package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_BackStab extends ThiefSkill
{
	public String ID() { return "Thief_BackStab"; }
	public String name(){ return "Back Stab";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"BACKSTAB"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_BackStab();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		int factor=(int)Math.round(Util.div(adjustedLevel((MOB)affected),2.0))+2;
		affectableStats.setDamage(affectableStats.damage()*factor);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+100);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Backstab whom?");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(Sense.canBeSeenBy(mob,target))
		{
			mob.tell(target.name()+" is watching you too closely to do that.");
			return false;
		}

		Weapon weapon=null;
		if((mob.fetchWieldedItem()!=null)&&(mob.fetchWieldedItem() instanceof Weapon))
			weapon=(Weapon)mob.fetchWieldedItem();
		if(weapon==null)
		{
			mob.tell(mob,target,null,"Backstab <T-HIM-HER> with what? You need to wield a weapon!");
			return false;
		}
		if((weapon.weaponClassification()==Weapon.CLASS_BLUNT)
		||(weapon.weaponClassification()==Weapon.CLASS_HAMMER)
		||(weapon.weaponClassification()==Weapon.CLASS_RANGED)
		||(weapon.weaponClassification()==Weapon.CLASS_THROWN)
		||(weapon.weaponClassification()==Weapon.CLASS_STAFF))
		{
			mob.tell(mob,target,weapon,"You cannot stab anyone with <O-NAME>.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		FullMsg msg=new FullMsg(mob,target,this,(auto?Affect.MSG_OK_ACTION:Affect.MSG_THIEF_ACT),auto?"":"<S-NAME> attempt(s) to stab <T-NAMESELF> in the back!");
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			if((!success)&&(Sense.canBeSeenBy(mob,target))&&(!Sense.isSleeping(target)))
				mob.location().show(target,mob,Affect.MSG_OK_VISUAL,auto?"":"<S-NAME> spot(s) <T-NAME>!");
			else
			{
				mob.addAffect(this);
				mob.recoverEnvStats();
			}
			ExternalPlay.postAttack(mob,target,weapon);
			mob.delAffect(this);
			mob.recoverEnvStats();
		}
		else
			success=false;
		return success;
	}

}
