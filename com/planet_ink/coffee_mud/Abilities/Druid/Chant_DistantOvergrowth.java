package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_DistantOvergrowth extends Chant
{
	public String ID() { return "Chant_DistantOvergrowth"; }
	public String name(){ return "Distant Overgrowth";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_ROOMS;}
	public Environmental newInstance(){	return new Chant_DistantOvergrowth();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(commands.size()<1)
		{
			mob.tell("Create overgrowth where?");
			return false;
		}
		
		String areaName=Util.combine(commands,0).trim().toUpperCase();
		Room anyRoom=null;
		Room newRoom=null;
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if((CoffeeUtensils.containsString(R.displayText().toUpperCase(),areaName))
			&&(((!Sense.isHidden(R.getArea()))&&(!Sense.isHidden(R)))
			   ||(mob.isASysOp(R))))
			{
			   anyRoom=R;
				if((R.domainType()&Room.INDOORS)==0)
				{
				    newRoom=R;
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
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),"^S<S-NAME> chant(s) about a far away place.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				Item newItem=null;
				if(newRoom.domainType()==Room.DOMAIN_INDOORS_CAVE)
					newItem=Chant_SummonFungus.buildFungus(mob,newRoom);
				else
				if((newRoom.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
				||(newRoom.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
					newItem=Chant_SummonSeaweed.buildSeaweed(mob,newRoom);
				else
				if((newRoom.domainType()==Room.DOMAIN_INDOORS_STONE)
				||(newRoom.domainType()==Room.DOMAIN_INDOORS_WOOD))
					newItem=Chant_SummonHouseplant.buildHouseplant(mob,newRoom);
				else
					newItem=Chant_SummonPlants.buildPlant(mob,newRoom);
				mob.tell("You feel a new connection with "+newItem.displayName());
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) about a far away place, but the magic fades.");


		// return whether it worked
		return success;
	}
}
