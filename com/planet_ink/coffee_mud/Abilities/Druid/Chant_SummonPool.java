package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_SummonPool extends Chant
{
	public String ID() { return "Chant_SummonPool"; }
	public String name(){ return "Summon Pool";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	private Room SpringLocation=null;
	private Item littleSpring=null;
	public Environmental newInstance(){	return new Chant_SummonPool();	}

	public void unInvoke()
	{
		if(SpringLocation==null)
			return;
		if(littleSpring==null)
			return;
		if(canBeUninvoked())
			SpringLocation.showHappens(CMMsg.MSG_OK_VISUAL,"The little pool dries up.");
		super.unInvoke();
		if(canBeUninvoked())
		{
			Item spring=littleSpring; // protects against uninvoke loops!
			littleSpring=null;
			spring.destroy();
			SpringLocation.recoverRoomStats();
			SpringLocation=null;
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		{
			mob.tell("This magic only works in caves.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) for a pool.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				String itemID = "Spring";

				Item newItem=(Item)CMClass.getItem(itemID);
				if(newItem==null)
				{
					mob.tell("There's no such thing as a '"+itemID+"'.\n\r");
					return false;
				}
				newItem.setName("a magical pool");
				newItem.setDisplayText("a little magical pool flows here.");
				newItem.setDescription("The pool is coming magically from the ground.  The water looks pure and clean.");

				mob.location().addItem(newItem);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" starts flowing here.");
				SpringLocation=mob.location();
				littleSpring=newItem;
				beneficialAffect(mob,newItem,0);
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) for a pool, but nothing happens.");

		// return whether it worked
		return success;
	}
}