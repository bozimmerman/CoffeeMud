package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ReadMagic extends Spell
{
	public Spell_ReadMagic()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Read Magic";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Ability to read magical markings)";

		baseEnvStats().setLevel(1);

		canAffectCode=0;
		canTargetCode=Ability.CAN_ITEMS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_ReadMagic();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_DIVINATION;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		// first, using the commands vector, determine
		// the target of the spell.  If no target is specified,
		// the system will assume your combat target.
		Environmental target=getTarget(mob,null,givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(0,auto);
		if((success)&&(mob.fetchAffect(this.ID())==null))
		{
			Ability thisNewOne=(Ability)this.copyOf();
			mob.addAffect(thisNewOne);
			ExternalPlay.read(mob,target,"");
			mob.delAffect(thisNewOne);
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) and gazes over <T-NAMESELF>, but nothing more happens.");

		// return whether it worked
		return success;
	}
}
