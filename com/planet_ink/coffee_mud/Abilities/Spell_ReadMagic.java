package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spell_ReadMagic extends Spell
	implements DivinationDevotion
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

		addQualifyingClass(new Mage().ID(),1);
		addQualifyingClass(new Thief().ID(),16);
		addQualifyingClass(new Bard().ID(),16);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_ReadMagic();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		// first, using the commands vector, determine
		// the target of the spell.  If no target is specified,
		// the system will assume your combat target.
		Environmental target=mob.location().fetchFromMOBRoom(mob,null,CommandProcessor.combine(commands,0));
		if((target==null)||(!Sense.canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see a '"+CommandProcessor.combine(commands,0)+"' here.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;


		boolean success=profficiencyCheck(0);

		if(success)
		{
			Ability thisNewOne=(Ability)this.copyOf();
			mob.addAffect(thisNewOne);
			commands.insertElementAt("read",0);
			ItemUsage.read(mob,commands);
			mob.delAffect(thisNewOne);
		}
		else
			return beneficialFizzle(mob,target,"<S-NAME> chant(s) and gazes over <T-NAME>, but nothing more happens.");

		// return whether it worked
		return success;
	}
}
