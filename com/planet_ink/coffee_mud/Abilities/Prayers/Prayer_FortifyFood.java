package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_FortifyFood extends Prayer
{
	public String ID() { return "Prayer_FortifyFood"; }
	public String name(){ return "Nourishing Food";}
	public int quality(){return Ability.INDIFFERENT;}
	public int holyQuality(){ return HOLY_NEUTRAL;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){	return new Prayer_FortifyFood();	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;


		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(!(target instanceof Food))
		{
			mob.tell(target.name()+" is not edible.");
			return false;
		}

		boolean success=profficiencyCheck(0,auto);

		if(((Food)target).nourishment()>1000)
		{
			mob.tell(target.name()+" is already well fortified.");
			return false;
		}

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to fortify <T-NAMESELF>.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.MSG_OK_ACTION,"<T-NAME> look(s) much more nutritious!");
				((Food)target).setNourishment(((Food)target).nourishment()+1000);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}