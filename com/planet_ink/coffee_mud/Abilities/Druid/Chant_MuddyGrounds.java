package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_MuddyGrounds extends Chant
{
	public String ID() { return "Chant_MuddyGrounds"; }
	public String name(){return "Muddy Grounds";}
	protected int canTargetCode(){return 0;}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	public Environmental newInstance(){	return new Chant_MuddyGrounds();}

	public void unInvoke()
	{
		if((canBeUninvoked())&&(affected!=null)&&(affected instanceof Room))
			((Room)affected).showHappens(CMMsg.MSG_OK_VISUAL,"The mud in '"+((Room)affected).displayText()+"' dries up.");
		super.unInvoke();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if((affected!=null)&&(affected instanceof Room))
			affectableStats.setWeight((affectableStats.weight()*2)+1);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof Room))
		{
			Room R=(Room)affected;
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(M.isInCombat()))
				   M.curState().adjMovement(-1,M.maxState());
			}
		}
		return super.tick(ticking,tickID);

	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		int type=mob.location().domainType();
		if(((type&Room.INDOORS)>0)
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

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,mob.location(),this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the ground.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"The ground here turns to MUD!");
				if((CoffeeUtensils.doesOwnThisProperty(mob,mob.location()))
				||((mob.amFollowing()!=null)&&(CoffeeUtensils.doesOwnThisProperty(mob.amFollowing(),mob.location()))))
					mob.location().addNonUninvokableEffect(this);
				else
					beneficialAffect(mob,mob.location(),0);
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the ground, but nothing happens.");


		// return whether it worked
		return success;
	}
}