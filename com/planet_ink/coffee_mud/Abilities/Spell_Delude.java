package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Spell_Delude extends Spell
	implements AlterationDevotion
{
	int previousAlignment=500;

	public Spell_Delude()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Delude";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Delude spell)";


		baseEnvStats().setLevel(18);

		addQualifyingClass(new Mage().ID(),18);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Delude();
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.setAlignment(previousAlignment);
		mob.tell("Your attitude returns to normal.");
	}


	public boolean invoke(MOB mob, Vector commands)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);


		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> chant(s) and meditate(s).");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					int alignment = mob.getAlignment();
					previousAlignment=alignment;

					mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<T-NAME> undergo(es) a change of attitude");
					success=beneficialAffect(mob,mob,0);
					if(success)
					{
						if(alignment < 350)
						{
							mob.setAlignment(1000);
							return true;
						}
						else
						if(alignment > 650)
						{
							mob.setAlignment(0);
							return true;
						}
						else
						{
							if(Dice.rollPercentage()>50)
								mob.setAlignment(1000);
							else
								mob.setAlignment(0);
						}

					}
				}
			}
		}
		else
			return beneficialFizzle(mob,null,"<S-NAME> chant(s) and meditate(s), but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
