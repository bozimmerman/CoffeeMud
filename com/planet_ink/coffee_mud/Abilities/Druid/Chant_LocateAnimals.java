package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Chant_LocateAnimals extends Chant
{
	public String ID() { return "Chant_LocateAnimals"; }
	public String name(){ return "Locate Animals";}
	private String displayText="(Locating Animals)";
	public String displayText(){return displayText;}
	
	private Vector theTrail=null;
	public int nextDirection=-2;
	public Environmental newInstance(){	return new Chant_LocateAnimals();}
	
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
				mob.tell("The trail seems to pause here.");
				nextDirection=-2;
				unInvoke();
			}
			else
			if(nextDirection==-1)
			{
				mob.tell("The trail dries up here.");
				nextDirection=-999;
				unInvoke();
			}
			else
			if(nextDirection>=0)
			{
				mob.tell("The trail seems to continue "+Directions.getDirectionName(nextDirection)+".");
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

	public MOB animalHere(Room room)
	{
		if(room==null) return null;
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB mob=room.fetchInhabitant(i);
			if(mob.charStats().getStat(CharStats.INTELLIGENCE)<2)
				return mob;
		}
		return null;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.fetchAffect(this.ID())!=null)
		{
			mob.tell("You are already trying to locate animals.");
			return false;
		}

		theTrail=null;
		nextDirection=-2;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(animalHere(mob.location())!=null)
		{
			mob.tell("Try 'look'.");
			return false;
		}

		boolean success=profficiencyCheck(0,auto);

		Hashtable lookedIn=new Hashtable();
		Vector rooms=new Vector();
		Vector localMap=mob.location().getArea().getMyMap();
		for(int r=0;r<localMap.size();r++)
			if(animalHere((Room)localMap.elementAt(r))!=null) 
				rooms.addElement((Room)localMap.elementAt(r));
		
		if(rooms.size()<=0)
		for(int r=0;r<CMMap.numRooms();r++)
			if(animalHere((Room)CMMap.getRoom(r))!=null)
				rooms.addElement(CMMap.getRoom(r));
		
		if(rooms.size()>0)
			theTrail=ExternalPlay.findBastardTheBestWay(mob.location(),rooms,false);
		
		MOB target=null;
		if((theTrail!=null)&&(theTrail.size()>0))
			target=animalHere((Room)theTrail.firstElement());

		if((success)&&(theTrail!=null)&&(target!=null))
		{
			theTrail.addElement(mob.location());
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> chant(s) for the animals.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				displayText="(seeking "+target.name()+")";
				Chant_LocateAnimals newOne=(Chant_LocateAnimals)this.copyOf();
				if(mob.fetchAffect(newOne.ID())==null)
					mob.addAffect(newOne);
				mob.recoverEnvStats();
				newOne.nextDirection=ExternalPlay.trackNextDirectionFromHere(newOne.theTrail,mob.location(),false);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) for the animals, but nothing happens.");


		// return whether it worked
		return success;
	}
}