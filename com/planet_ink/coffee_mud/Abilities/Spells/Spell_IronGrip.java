package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_IronGrip extends Spell
{

	public Spell_IronGrip()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Iron Grip";
		displayText="(Iron Grip)";
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
		return new Spell_IronGrip();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_TRANSMUTATION;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked)
			mob.tell("Your weapon hand turns back to flesh.");

		super.unInvoke();

	}
	
	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect)) return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((affect.amITarget(mob))
			&&(affect.tool()!=null)
			&&(affect.tool().ID().toUpperCase().indexOf("DISARM")>=0))
			{
				mob.location().show(affect.source(),mob,Affect.MSG_OK_ACTION,"<S-NAME> attempt(s) to disarm <T-NAME>, but the grip is too strong!");
				return false;
			}
			else
			if((affect.amISource(mob))
			&&((affect.targetMinor()==Affect.TYP_DROP)
				||(affect.targetMinor()==Affect.TYP_GET))
			&&(affect.target()!=null)
			&&(affect.target() instanceof Item)
			&&(mob.isMine((Item)affect.target()))
			&&(((Item)affect.target()).amWearingAt(Item.WIELD)))
			{
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> attempt(s) to let go of "+affect.target().name()+", but <S-HIS-HER> grip is too strong!");
				return false;
			}
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
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> watch(es) <T-HIS-HER> weapon hand turn to iron!":"<S-NAME> invoke(s) a spell on <T-NAMESELF> and <T-HIS-HER> weapon hand turns into iron!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell, but fail(s).");

		return success;
	}
}