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

public class Spell_MagicMissile extends Spell
	implements ConjurationDevotion
{
	public Spell_MagicMissile()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Magic Missile";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Magic Missile spell)";


		malicious=true;

		baseEnvStats().setLevel(1);

		addQualifyingClass(new Mage().ID(),1);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_MagicMissile();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			for(int i=0;i<((int)Math.round(Math.floor(Util.div(mob.envStats().level(),5)))+1);i++)
			{
				FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> point(s) at <T-NAME>, shooting forth a magic missile!");
				if(mob.location().okAffect(msg))
				{
					mob.location().send(mob,msg);
					if(!msg.wasModified())
					{
						int damage = 0;
						damage += Dice.roll(1,10,1);
						mob.location().show(mob,target,Affect.VISUAL_WNOISE,"The missile "+TheFight.hitWord(-1,damage)+" <T-NAME>!");
						TheFight.doDamage(target, damage);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) at <T-NAME>, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
