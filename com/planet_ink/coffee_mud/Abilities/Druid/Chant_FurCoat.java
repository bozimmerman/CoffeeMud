package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_FurCoat extends Chant
{
	public String ID() { return "Chant_FurCoat"; }
	public String name(){return "Fur Coat";}
	public String displayText(){return "(Fur Coat)";}
	public int quality(){return BENEFICIAL_SELF;};
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Chant_FurCoat();}

	Item theArmor=null;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
		if(theArmor!=null)
		{
			theArmor.destroy();
			mob.location().recoverRoomStats();
		}
		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("Your fur coat vanishes!");
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;
		if(theArmor==null) return true;

		if((theArmor.amWearingAt(Item.INVENTORY)
		||(theArmor.owner()==null)
		||(theArmor.owner() instanceof Room)))
			unInvoke();

		MOB mob=affect.source();
		if(!affect.amITarget(theArmor))
			return true;
		else
		if((affect.targetMinor()==Affect.TYP_REMOVE)
		||(affect.targetMinor()==Affect.TYP_GET))
		{
			mob.tell("The fur coat cannot be removed from where it is.");
			return false;
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		
		if(target.fetchAffect(this.ID())!=null)
		{
			target.tell("You already have a fur coat.");
			return false;
		}
		
		if(Druid_ShapeShift.isShapeShifted(target))
		{
			mob.tell("You cannot invoke this chant in your present form.");
			return false;
		}

		if(target.amWearingSomethingHere(Item.ON_TORSO))
		{
			mob.tell("You are already wearing something on your torso!");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"A thick coat of fur appears on <T-NAME>.":"^S<S-NAME> chant(s) for a thick coat of fur!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				theArmor=CMClass.getArmor("GenArmor");
				theArmor.setName("a fur coat");
				theArmor.setDisplayText("");
				theArmor.setDescription("The coat is made of thick black fur.");
				theArmor.setMaterial(EnvResource.RESOURCE_FUR);
				theArmor.baseEnvStats().setArmor(2*CMAble.qualifyingClassLevel(mob,this));
				long wornCode=(Item.ON_TORSO|Item.ON_ARMS|Item.ON_FEET|Item.ON_WAIST|Item.ON_LEGS);
				theArmor.setRawProperLocationBitmap(wornCode);
				theArmor.setRawLogicalAnd(true);
				for(int i=target.inventorySize()-1;i>=0;i--)
				{
					Item I=mob.fetchInventory(i);
					if((I.rawWornCode()&wornCode)>0)
						I.unWear();
				}
				Ability A=CMClass.getAbility("Prop_WearResister");
				A.setMiscText("cold");
				if(A!=null) theArmor.addNonUninvokableAffect(A);
				theArmor.recoverEnvStats();
				theArmor.text();
				target.addInventory(theArmor);
				theArmor.wearAt(wornCode);
				success=beneficialAffect(mob,target,0);
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) for a thick coat of fur, but nothing happen(s).");

		// return whether it worked
		return success;
	}
}
