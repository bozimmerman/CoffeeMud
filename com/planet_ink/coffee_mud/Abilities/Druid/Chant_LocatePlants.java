package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Chant_LocatePlants extends Chant
{
	public String ID() { return "Chant_LocatePlants"; }
	public String name(){ return "Locate Plants";}
	public String displayText(){return "(Locating Plants)";}
	
	private Vector theTrail=null;
	public int nextDirection=-2;
	public Environmental newInstance(){	return new Chant_LocatePlants();}
	
	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;
		if(tickID==Host.MOB_TICK)
		{
			if(nextDirection==-999)
				return true;

			if((theTrail==null)
			||(affected == null)
			||(!(affected instanceof MOB)))
				return false;

			MOB mob=(MOB)affected;

			if(nextDirection==999)
			{
				mob.tell(plantsHere(mob,mob.location()));
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				if(plantsHere(mob,mob.location()).length()==0)
					mob.tell("The plant life trail fizzles out here.");
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell("Your sense plant life "+Directions.getDirectionName(nextDirection)+".");
				nextDirection=-2;
			}

		}
		return true;
	}

	public void affect(Affect affect)
	{
		super.affect(affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if((affect.amISource(mob))
		&&(affect.amITarget(mob.location()))
		&&(Sense.canBeSeenBy(mob.location(),mob))
		&&(affect.targetMinor()==Affect.TYP_EXAMINESOMETHING))
			nextDirection=ExternalPlay.trackNextDirectionFromHere(theTrail,mob.location(),false);
	}

	public String plantsHere(MOB mob, Room R)
	{
		StringBuffer msg=new StringBuffer("");
		if(R==null) return msg.toString();
		Room room=(Room)R;
		if((room.domainType()==Room.DOMAIN_OUTDOORS_WOODS)
		||(room.domainType()==Room.DOMAIN_OUTDOORS_PLAINS)
		||(room.domainType()==Room.DOMAIN_OUTDOORS_HILLS)
		||(room.domainType()==Room.DOMAIN_OUTDOORS_JUNGLE)
		||(room.domainType()==Room.DOMAIN_OUTDOORS_SWAMP))
			msg.append("There seem to be a large number of plants all around you!\n\r");
		return msg.toString();
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already trying to find plant life.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		String here=plantsHere(mob,mob.location());
		if(here.length()>0)
		{
			mob.tell(here);
			return true;
		}
		
		boolean success=profficiencyCheck(0,auto);

		Hashtable lookedIn=new Hashtable();
		Vector rooms=new Vector();
		Vector localMap=mob.location().getArea().getMyMap();
		for(int r=0;r<localMap.size();r++)
			if(plantsHere(mob,(Room)localMap.elementAt(r)).length()>0) 
				rooms.addElement((Room)localMap.elementAt(r));
		
		if(rooms.size()<=0)
		for(int r=0;r<CMMap.numRooms();r++)
			if(plantsHere(mob,(Room)CMMap.getRoom(r)).length()>0)
				rooms.addElement(CMMap.getRoom(r));
		
		if(rooms.size()>0)
			theTrail=ExternalPlay.findBastardTheBestWay(mob.location(),rooms,false);
		
		if((success)&&(theTrail!=null))
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"<S-NAME> begin(s) to sense plant life!":"^S<S-NAME> chant(s) for a route to plant life.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Chant_LocatePlants newOne=(Chant_LocatePlants)this.copyOf();
				if(mob.fetchAffect(newOne.ID())==null)
					mob.addAffect(newOne);
				mob.recoverEnvStats();
				newOne.nextDirection=ExternalPlay.trackNextDirectionFromHere(newOne.theTrail,mob.location(),false);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) to find plant life, but fail(s).");

		return success;
	}
}