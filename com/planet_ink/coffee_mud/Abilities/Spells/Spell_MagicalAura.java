package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_MagicalAura extends Spell
{
	public String ID() { return "Spell_MagicalAura"; }
	public String name(){return "Magical Aura";}
	public String displayText(){return "(Magical Aura)";}
	public int quality(){return BENEFICIAL_SELF;};
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	protected int canTargetCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	public Environmental newInstance(){	return new Spell_MagicalAura();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_BONUS);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null))
			return;
		if(canBeUninvoked)
			if(affected instanceof MOB)
				((MOB)affected).tell("Your magical aura fades.");

		super.unInvoke();

	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if(target.fetchAffect(this.ID())!=null)
		{
			mob.tell("There is already a magical aura around "+target.name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"A magical aura appears around <T-NAME>.":"^S<S-NAME> invoke(s) a magical aura around <T-NAMESELF>.^?");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,0);
			target.recoverEnvStats();
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a magical aura, but fail(s).");

		return success;
	}
}