package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_ReadMagic extends Spell
{
	public String ID() { return "Spell_ReadMagic"; }
	public String name(){return "Read Magic";}
	public String displayText(){return "(Ability to read magic)";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Spell_ReadMagic();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_DIVINATION;}


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
		if((success)&&(mob.fetchEffect(this.ID())==null))
		{
			Ability thisNewOne=(Ability)this.copyOf();
			mob.addEffect(thisNewOne);
			CommonMsgs.doStandardCommand(mob,"Read",Util.makeVector("READ",target));
			mob.delEffect(thisNewOne);
		}
		else
			return beneficialWordsFizzle(mob,target,"^S<S-NAME> incant(s) and gazes over <T-NAMESELF>, but nothing more happens.^?");

		// return whether it worked
		return success;
	}
}
