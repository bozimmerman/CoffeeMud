package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Camelback extends Chant
{
	public String ID() { return "Chant_Camelback"; }
	public String name(){return "Camelback";}
	public String displayText(){return "(Camelback)";}
	public int quality(){ return BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Chant_Camelback();}

	public void affectCharState(MOB affected, CharState affectableMaxState)
	{
		super.affectCharState(affected,affectableMaxState);
		affectableMaxState.setThirst(999999);
		affected.curState().setThirst(affectableMaxState.getThirst());
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> camelback disappears.");
			mob.curState().setThirst(0);
		}
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(ID())!=null)
		{
			target.tell("You already have a camel's back.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chants(s) thirstily.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> grow(s) a camelback hump!");
				target.tell("You feel quenched!");
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) thirstily, but nothing more happens.");

		// return whether it worked
		return success;
	}
}