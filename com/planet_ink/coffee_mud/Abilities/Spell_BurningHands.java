package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Weapons.Weapon;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Spell_BurningHands extends Spell
	implements AlterationDevotion
{
	public Spell_BurningHands()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Burning Hands";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Burning Hands spell)";

		malicious=true;


		uses=Integer.MAX_VALUE;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(4);

		addQualifyingClass(new Mage().ID(),4);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_BurningHands();
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

		// now see if it worked
		boolean success=profficiencyCheck(0);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> reach(es) for <T-NAME>, and a fan of flames erupts.");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_FIRE,Affect.SOUND_MAGIC,null);
			if((mob.location().okAffect(msg))&&(mob.location().okAffect(msg2)))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					invoker=mob;
					mob.location().send(mob,msg2);
					int damage = 0;
					int maxDie =  mob.envStats().level();
					if (maxDie > 10)
						maxDie = 10;
					damage += Dice.roll(1,maxDie*2,3);
					if(!msg2.wasModified())
						damage = (int)Math.round(Util.div(damage,2.0));
					mob.location().show(mob,target,Affect.VISUAL_WNOISE,"The flame "+TheFight.hitWord(Weapon.TYPE_BURNING,damage)+" <T-NAME>!");
					TheFight.doDamage(target, damage);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> point(s) and incant(s) at <T-NAME>, but flubs the spell.");


		// return whether it worked
		return success;
	}
}
