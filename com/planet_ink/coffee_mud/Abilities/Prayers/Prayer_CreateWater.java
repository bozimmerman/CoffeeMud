package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_CreateWater extends Prayer
{
	public String ID() { return "Prayer_CreateWater"; }
	public String name(){ return "Create Water";}
	public int quality(){ return INDIFFERENT;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Prayer_CreateWater();}

	public void unInvoke()
	{
		Item spring=(Item)affected; // protects against uninvoke loops!
		Room SpringLocation=CoffeeUtensils.roomLocation(spring);
		if((canBeUninvoked())&&(SpringLocation!=null))
			SpringLocation.showHappens(CMMsg.MSG_OK_VISUAL,"The little spring dries up.");
		super.unInvoke();
		if(canBeUninvoked())
		{
			spring.destroy();
			SpringLocation.recoverRoomStats();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for water.^?");
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

				mob.location().addItem(newItem);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" starts flowing here.");
				if((CoffeeUtensils.doesOwnThisProperty(mob,mob.location()))
				||((mob.amFollowing()!=null)&&(CoffeeUtensils.doesOwnThisProperty(mob.amFollowing(),mob.location()))))
				{
					Ability A=(Ability)copyOf();
					A.setInvoker(mob);
					newItem.addNonUninvokableEffect(A);
				}
				else
					beneficialAffect(mob,newItem,0);
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for water, but there is no answer.");

		// return whether it worked
		return success;
	}
}
