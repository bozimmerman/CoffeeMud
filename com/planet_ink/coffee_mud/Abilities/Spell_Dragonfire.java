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

public class Spell_Dragonfire extends Spell
	implements EvocationDevotion, InvocationDevotion
{
	public Spell_Dragonfire()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Dragonfire";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Dragonfire)";

		malicious=true;


		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(23);

		addQualifyingClass(new Mage().ID(),23);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Dragonfire();
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		Hashtable h=null;
		if(mob.isInCombat())
			h=TheFight.allCombatants(mob);
		else
			h=TheFight.allPossibleCombatants(mob);

		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth burning.");
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

			mob.location().show(mob,null,Affect.SOUND_MAGIC,"<S-NAME> blast(s) flames from <S-HIS-HER> mouth!");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,null);
				FullMsg msg2=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_FIRE,Affect.SOUND_MAGIC,null);
				if((mob.location().okAffect(msg))&&((mob.location().okAffect(msg2))))
				{
					mob.location().send(mob,msg);
					if(!msg.wasModified())
					{
						mob.location().send(mob,msg2);
						invoker=mob;

						int damage = 0;
						int maxDie =  mob.envStats().level();
						if (maxDie > 10)
							maxDie = 10;
						damage += Dice.roll(maxDie,30,1);
						if(msg2.wasModified())
							damage = (int)Math.round(Util.div(damage,2.0));

						mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> "+TheFight.hitWord(Weapon.TYPE_BURNING,damage)+" horribly!");
						TheFight.doDamage(target, damage);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> puff(s) smoke from <S-HIS-HER> mouth.");


		// return whether it worked
		return success;
	}
}
