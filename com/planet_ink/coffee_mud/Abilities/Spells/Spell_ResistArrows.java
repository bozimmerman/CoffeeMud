package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ResistArrows extends Spell
{
	public Spell_ResistArrows()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Resist Arrows";
		displayText="(Resist Arrows)";
		miscText="";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.BENEFICIAL_OTHERS;

		baseEnvStats().setLevel(9);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_ResistArrows();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ABJURATION;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked)
			mob.tell("Your arrow protection dissipates.");

		super.unInvoke();

	}


	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
		&&(affect.tool()!=null)
		&&(affect.source().getVictim()==mob)
		&&(affect.source().rangeToTarget()>0)
		&&(affect.tool() instanceof Weapon)
		&&(((Weapon)affect.tool()).weaponClassification()==Weapon.CLASS_RANGED)
		&&(((Weapon)affect.tool()).requiresAmmunition())
		&&(!mob.amDead())
		&&(profficiencyCheck(0,false)))
		{
			affect.addTrailerMsg(new FullMsg(mob,null,Affect.MSG_OK_VISUAL,"The absorbing barrier around <S-NAME> absorbs the "+((Weapon)affect.tool()).ammunitionType()+"."));
			return false;
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<S-NAME> feel(s) absorbantly protected.":"<S-NAME> invoke(s) a non-porous barrier of protection around <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a non-porous barrier, but fail(s).");

		return success;
	}
}
