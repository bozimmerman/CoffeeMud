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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;
		if((!(target instanceof Drink))||((target.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID))
		{
			mob.tell("You can not create water inside "+target.name()+".");
			return false;
		}
		Drink D=(Drink)target;
		if(D.containsDrink()&&(D.liquidType()!=EnvResource.RESOURCE_FRESHWATER))
		{
			mob.tell(target.name()+" already contains another liquid, and must be emptied first.");
			return false;
		}
		if(D.containsDrink()&&(D.liquidRemaining()>=D.liquidHeld()))
		{
			mob.tell(target.name()+" is full.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" over <T-NAME> for water.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().recoverEnvStats();
				D.setLiquidType(EnvResource.RESOURCE_FRESHWATER);
				D.setLiquidRemaining(D.liquidHeld());
				if(target.owner() instanceof Room)
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,target.name()+" fills up with water!");
				else
					mob.tell(target.name()+" fills up with water!");
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" over <T-NAME> for water, but there is no answer.");

		// return whether it worked
		return success;
	}
}
