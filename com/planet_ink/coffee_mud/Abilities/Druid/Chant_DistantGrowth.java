package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_DistantGrowth extends Chant
{
	public Chant_DistantGrowth()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Distant Growth";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.INDIFFERENT;
		canAffectCode=0;
		canTargetCode=Ability.CAN_ROOMS;

		baseEnvStats().setLevel(10);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_DistantGrowth();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(commands.size()<1)
		{
			mob.tell("Grow plants where?");
			return false;
		}
		
		String areaName=Util.combine(commands,0).trim().toUpperCase();
		Room anyRoom=null;
		Room newRoom=null;
		for(int m=0;m<CMMap.numRooms();m++)
		{
			Room room=CMMap.getRoom(m);
			if(CoffeeUtensils.containsString(room.displayText().toUpperCase(),areaName))
			{
			   anyRoom=room;
			   if((room.domainType()&Room.INDOORS)==0)
			   {
				   newRoom=room;
				   break;
			   }
			}
		}

		if(newRoom==null)
		{
			if(anyRoom==null)
				mob.tell("You don't know of an place called '"+Util.combine(commands,0)+"'.");
			else
				mob.tell("There IS such a place, but its not outdoors, so your magic would fail.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,"<S-NAME> chant(s) about a far away place.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Item newItem=Chant_SummonPlants.buildPlant(mob,newRoom);
				mob.tell("You feel a new connection with "+newItem.name());
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) about a far away place, but the magic fades.");


		// return whether it worked
		return success;
	}
}
