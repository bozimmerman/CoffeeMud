package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_MageClaws extends Spell
{
	public String ID() { return "Spell_MageClaws"; }
	public String name(){return "Mage Claws";}
	public String displayText(){return "(Mage Claws spell)";}
	public int quality(){return BENEFICIAL_SELF;};
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance()	{ return new Spell_MageClaws();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if((affect.amISource(mob))
		&&(affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
		&&(affect.tool()==null))
		{
			Weapon w=(Weapon)CMClass.getItem("GenWeapon");
			w.setName("a pair of jagged claws");
			w.setWeaponType(Weapon.TYPE_SLASHING);
			w.setWeaponClassification(Weapon.CLASS_NATURAL);
			w.baseEnvStats().setDamage(15);
			w.baseEnvStats().setAttackAdjustment(20);
			w.recoverEnvStats();
			affect.modify(affect.source(),affect.target(),w,affect.sourceCode(),affect.sourceMessage(),affect.targetCode(),affect.targetMessage(),affect.othersCode(),affect.othersMessage());
		}
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your claws are restored to normal hands.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> invoke(s) a spell.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> hands turn into brutal claws!");
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell, but fail(s) miserably.");

		// return whether it worked
		return success;
	}
}
