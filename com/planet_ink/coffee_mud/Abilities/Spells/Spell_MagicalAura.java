package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Spell_MagicalAura extends Spell
{

	public Spell_MagicalAura()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Magical Aura";
		displayText="(Magical Aura)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);
		quality=Ability.BENEFICIAL_SELF;

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_MagicalAura();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ILLUSION;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_BONUS);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null))
			return;
		if(affected instanceof MOB)
			((MOB)affected).tell("Your magical aura fades.");

		super.unInvoke();

	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(target.fetchAffect(this.ID())!=null)
		{
			mob.tell("There is already a magical aura around "+target.name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"A magical aura appears around <T-NAME>.":"<S-NAME> invoke(s) a magical aura around <T-NAMESELF>.");
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