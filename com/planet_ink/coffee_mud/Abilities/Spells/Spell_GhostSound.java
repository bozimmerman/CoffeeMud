package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_GhostSound extends Spell
{
	public Spell_GhostSound()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Ghost Sound";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Ghost Sound spell)";

		canAffectCode=Ability.CAN_ROOMS;
		canTargetCode=0;
		

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(7);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_GhostSound();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ILLUSION;
	}

	public boolean tick(int tickID)
	{
		if((tickID==Host.MOB_TICK)
		&&(Dice.rollPercentage()<10)
		&&(affected!=null)
		&&(invoker!=null)
		&&(affected instanceof Room))
		switch(Dice.roll(1,14,0))
		{
		case 1:	((Room)affected).showHappens(Affect.MSG_NOISE,
				"You hear something coming up behind you.");
				break;
		case 2:	((Room)affected).showHappens(Affect.MSG_NOISE,
				"You hear somebody screaming in the distance.");
				break;
		case 3:	((Room)affected).showHappens(Affect.MSG_NOISE,
				"You hear the snarl of a large ferocious beast.");
				break;
		case 4:	((Room)affected).showHappens(Affect.MSG_NOISE,
				"You hear complete silence.");
				break;
		case 5:	((Room)affected).showHappens(Affect.MSG_NOISE,
				"CLANK! Someone just dropped their sword.");
				break;
		case 6:	((Room)affected).showHappens(Affect.MSG_NOISE,
				"You hear a bird singing.");
				break;
		case 7:	((Room)affected).showHappens(Affect.MSG_NOISE,
				"You hear a cat dying.");
				break;
		case 8:	((Room)affected).showHappens(Affect.MSG_NOISE,
				"You hear some people talking.");
				break;
		case 9:	((Room)affected).showHappens(Affect.MSG_NOISE,
				"You hear singing.");
				break;
		case 10:((Room)affected).showHappens(Affect.MSG_NOISE,
				"You hear a cow mooing.");
				break;
		case 11:((Room)affected).showHappens(Affect.MSG_NOISE,
				"You hear your shadow.");
				break;
		case 12:((Room)affected).showHappens(Affect.MSG_NOISE,
				"You hear someone trying to sneak by you.");
				break;
		case 13:((Room)affected).showHappens(Affect.MSG_NOISE,
				"You hear an annoying beeping sound.");
				break;
		case 14:((Room)affected).showHappens(Affect.MSG_NOISE,
				"You hear your heart beating in your chest.");
				break;
		}
		return super.tick(tickID);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Environmental target = mob.location();

		if(target.fetchAffect(this.ID())!=null)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"^S<S-NAME> scream(s) loudly, then fall(s) silent.^?");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
			return false;
		}


		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, target, this, affectType,auto?"":"^S<S-NAME> scream(s) loudly, then fall(s) silent.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob.location(),0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> scream(s) loudly, but then feel(s) disappointed.");

		// return whether it worked
		return success;
	}
}
