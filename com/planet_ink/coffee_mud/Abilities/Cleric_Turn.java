package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Cleric_Turn extends StdAbility
{
	public Cleric_Turn()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Turn Undead";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Turned)";
		
		malicious=true;

		baseEnvStats().setLevel(1);

		addQualifyingClass(new Cleric().ID(),1);
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+4);

		canBeUninvoked=true;
		isAutoinvoked=false;

		triggerStrings.addElement("TURN");
		
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Cleric_Turn();
	}
	
	public int classificationCode()
	{
		return Ability.PRAYER;
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		
		if(!(target instanceof Undead))
		{
			mob.tell("You can only turn the undead.");
			return false;
		}
		
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck((mob.envStats().level()-target.envStats().level())*20);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> turn(s) <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					if((mob.envStats().level()-target.envStats().level())>6)
					{
						mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> wither(s) under <S-HIS-HER> holy power!");
						TheFight.doDamage(target,target.curState().getHitPoints());
					}
					else
					{
						if(mob.getAlignment()<500)
						{
							mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> become(s) submissive.");
							target.makePeace();
							Movement.flee(target,"");
						}
						else
						{
							mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> shake(s) in fear!");
							Movement.flee(target,"");
						}
					}
					invoker=mob;
					Movement.flee(target,"");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to turn <T-NAME>, but fail(s).");


		// return whether it worked
		return success;
	}
}