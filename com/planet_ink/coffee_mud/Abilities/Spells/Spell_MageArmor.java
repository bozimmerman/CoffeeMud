package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_MageArmor extends Spell
{
	Item theArmor=null;

	public Spell_MageArmor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mage Armor";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Mage Armor)";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;

		quality=Ability.BENEFICIAL_SELF;
		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(5);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_MageArmor();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ABJURATION;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked)
		if(theArmor!=null)
		{
			theArmor.destroyThis();
			mob.location().recoverRoomStats();
		}
		super.unInvoke();
		if(canBeUninvoked)
			mob.tell("Your magical armor vanishes!");
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;		if(target==null) return false;

		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already wearing mage armor.");
			return false;
		}

		if(target.amWearingSomethingHere(Item.ON_TORSO))
		{
			mob.tell(mob,null,"You are already wearing something on your torso!");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType,"^S<S-NAME> invoke(s) a magical glowing breast plate!^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				theArmor=CMClass.getArmor("GlowingMageArmor");
				mob.addInventory(theArmor);
				theArmor.wearAt(Item.ON_TORSO);
				success=beneficialAffect(mob,target,0);
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke magical protection, but fail(s).");

		// return whether it worked
		return success;
	}
}
