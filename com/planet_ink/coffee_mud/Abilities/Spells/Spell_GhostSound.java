package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_GhostSound extends Spell
{
	public String ID() { return "Spell_GhostSound"; }
	public String name(){return "Ghost Sound";}
	public String displayText(){return "(Ghost Sound spell)";}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){ return new Spell_GhostSound();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==MudHost.TICK_MOB)
		&&(Dice.rollPercentage()<10)
		&&(affected!=null)
		&&(invoker!=null)
		&&(affected instanceof Room))
		switch(Dice.roll(1,14,0))
		{
		case 1:	((Room)affected).showHappens(CMMsg.MSG_NOISE,
				"You hear something coming up behind you.");
				break;
		case 2:	((Room)affected).showHappens(CMMsg.MSG_NOISE,
				"You hear somebody screaming in the distance.");
				break;
		case 3:	((Room)affected).showHappens(CMMsg.MSG_NOISE,
				"You hear the snarl of a large ferocious beast.");
				break;
		case 4:	((Room)affected).showHappens(CMMsg.MSG_NOISE,
				"You hear complete silence.");
				break;
		case 5:	((Room)affected).showHappens(CMMsg.MSG_NOISE,
				"CLANK! Someone just dropped their sword.");
				break;
		case 6:	((Room)affected).showHappens(CMMsg.MSG_NOISE,
				"You hear a bird singing.");
				break;
		case 7:	((Room)affected).showHappens(CMMsg.MSG_NOISE,
				"You hear a cat dying.");
				break;
		case 8:	((Room)affected).showHappens(CMMsg.MSG_NOISE,
				"You hear some people talking.");
				break;
		case 9:	((Room)affected).showHappens(CMMsg.MSG_NOISE,
				"You hear singing.");
				break;
		case 10:((Room)affected).showHappens(CMMsg.MSG_NOISE,
				"You hear a cow mooing.");
				break;
		case 11:((Room)affected).showHappens(CMMsg.MSG_NOISE,
				"You hear your shadow.");
				break;
		case 12:((Room)affected).showHappens(CMMsg.MSG_NOISE,
				"You hear someone trying to sneak by you.");
				break;
		case 13:((Room)affected).showHappens(CMMsg.MSG_NOISE,
				"You hear an annoying beeping sound.");
				break;
		case 14:((Room)affected).showHappens(CMMsg.MSG_NOISE,
				"You hear your heart beating in your chest.");
				break;
		}
		return super.tick(ticking,tickID);
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

		if(target.fetchEffect(this.ID())!=null)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> scream(s) loudly, then fall(s) silent.^?");
			if(mob.location().okMessage(mob,msg))
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

			FullMsg msg = new FullMsg(mob, target, this, affectType(auto),auto?"":"^S<S-NAME> scream(s) loudly, then fall(s) silent.^?");
			if(mob.location().okMessage(mob,msg))
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
