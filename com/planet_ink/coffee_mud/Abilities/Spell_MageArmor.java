package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Armor.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Spell_MageArmor extends Spell
	implements EvocationDevotion
{

	public GlowingMageArmor theArmor=null;

	public Spell_MageArmor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mage Armor";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Mage Armor)";


		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(5);

		addQualifyingClass(new Mage().ID(),5);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_MageArmor();
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(theArmor!=null)
		{
			theArmor.destroyThis();
			mob.location().recoverRoomStats();
		}
		super.unInvoke();
		mob.tell("Your magical armor vanishes!");
	}



	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;


		if(target.amWearingSomethingHere(Item.ON_TORSO))
		{
			mob.tell(mob,target,"<T-NAME> is already wearing something on <T-HIS-HER> torso!");
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
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> invoke(s) a magical glowing breast plate!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				theArmor=new GlowingMageArmor();
				mob.addInventory(theArmor);
				theArmor.wear(Item.ON_TORSO);
				success=beneficialAffect(mob,target,0);
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialFizzle(mob,target,"<S-NAME> attempt(s) to invoke magical protection, but fail(s).");

		// return whether it worked
		return success;
	}
}
