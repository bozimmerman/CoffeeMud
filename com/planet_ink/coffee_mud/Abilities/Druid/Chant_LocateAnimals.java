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

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Host.TICK_MOB)
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

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.amITarget(mob.location()))
		&&(Sense.canBeSeenBy(mob.location(),mob))
		&&(msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING))
			nextDirection=SaucerSupport.trackNextDirectionFromHere(theTrail,mob.location(),false);
	}

	public MOB animalHere(Room room)
	{
		if(room==null) return null;
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB mob=room.fetchInhabitant(i);
			if(Sense.isAnimalIntelligence(mob))
				return mob;
		}
		return null;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.fetchEffect(this.ID())!=null)
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

		Vector rooms=new Vector();
		for(Enumeration r=mob.location().getArea().getMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(animalHere(R)!=null)
				rooms.addElement(R);
		}

		if(rooms.size()<=0)
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(Sense.canAccess(mob,R))
				if(animalHere(R)!=null)
					rooms.addElement(R);
		}

		if(rooms.size()>0)
			theTrail=SaucerSupport.findBastardTheBestWay(mob.location(),rooms,false);

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
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				displayText="(seeking "+target.name()+")";
				Chant_LocateAnimals newOne=(Chant_LocateAnimals)this.copyOf();
				if(mob.fetchEffect(newOne.ID())==null)
					mob.addEffect(newOne);
				mob.recoverEnvStats();
				newOne.nextDirection=SaucerSupport.trackNextDirectionFromHere(newOne.theTrail,mob.location(),false);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) for the animals, but nothing happens.");


		// return whether it worked
		return success;
	}
}