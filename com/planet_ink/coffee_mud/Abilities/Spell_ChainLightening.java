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

public class Spell_ChainLightening extends Spell
	implements EvocationDevotion
{
	public Spell_ChainLightening()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Chain Lightening";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Chain Lightening)";


		malicious=true;

		baseEnvStats().setLevel(16);

		addQualifyingClass(new Mage().ID(),16);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_ChainLightening();
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_SITTING);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell("The movement under your feet stops.");
		Movement.standIfNecessary(mob);
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
			mob.tell("There doesn't appear to be anyone here worth electrocuting.");
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

			mob.location().show(mob,null,Affect.SOUND_MAGIC,"<S-NAME> invoke(s) a thunderous crack of lightening.");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,null);
				FullMsg msg2=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_ELECTRIC,Affect.SOUND_MAGIC,null);
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
						damage += Dice.roll(maxDie,6,1);
						if(msg2.wasModified())
							damage = (int)Math.round(Util.div(damage,2.0));
						mob.location().show(mob,target,Affect.VISUAL_WNOISE,"The strike "+TheFight.hitWord(Weapon.TYPE_BURNING,damage)+" <T-NAME>!");

						TheFight.doDamage(target, damage);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to invoke a ferocious spell, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}