package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Reabsorb extends Chant
{
	public String ID() { return "Chant_Reabsorb"; }
	public String name(){return "Reabsorb";}
	protected int canTargetCode(){return CAN_MOBS|CAN_ITEMS;}
	protected int canAffectCode(){return 0;}
	public int quality(){return Ability.MALICIOUS;}
	public Environmental newInstance(){	return new Chant_Reabsorb();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=this.getTarget(mob,mob.location(),givenTarget,null,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;
		if(!(target.owner() instanceof Room))
		{
			mob.tell("You need to put "+target.name()+" on the ground first.");
			return false;
		}
		int type=mob.location().domainType();
		if((type==Room.DOMAIN_INDOORS_STONE)
		    ||(type==Room.DOMAIN_INDOORS_WOOD)
		    ||(type==Room.DOMAIN_INDOORS_MAGIC)
		    ||(type==Room.DOMAIN_INDOORS_UNDERWATER)
		    ||(type==Room.DOMAIN_INDOORS_WATERSURFACE)
		    ||(type==Room.DOMAIN_OUTDOORS_AIR)
			||(type==Room.DOMAIN_OUTDOORS_CITY)
			||(type==Room.DOMAIN_OUTDOORS_UNDERWATER)
			||(type==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("That magic won't work here.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> starts vibrating!":"^S<S-NAME> chant(s), causing <T-NAMESELF> to grow brittle!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"The ground reabsorbs "+target.name()+".");
					target.destroy();
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAME>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
