package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Chant_LocateAnimals extends Chant
{
	Room lastRoom=null;
	private MOB trackingWhom=null;
	private Vector theTrail=null;
	private Hashtable lookedIn=null;
	public int nextDirection=-2;
	protected final static int TRACK_ATTEMPTS=25;
	public Chant_LocateAnimals()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Locate Animals";
		displayText="(locating animals)";
		miscText="";

		baseEnvStats().setLevel(1);

		canBeUninvoked=true;
		isAutoinvoked=false;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_LocateAnimals();
	}
	public int nextDirectionFromHere(Room location)
	{
		if(theTrail==null)
			return -1;
		if(location==theTrail.elementAt(0))
			return 999;

		Room nextRoom=null;
		int bestDirection=-1;
		int trailLength=Integer.MAX_VALUE;
		for(int dirs=0;dirs<Directions.NUM_DIRECTIONS;dirs++)
		{
			Room thisRoom=location.getRoomInDir(dirs);
			Exit thisExit=location.getExitInDir(dirs);
			if((thisRoom!=null)&&(thisExit!=null))
			{
				for(int trail=0;trail<theTrail.size();trail++)
				{
					if((theTrail.elementAt(trail)==thisRoom)
					&&(trail<trailLength))
					{
						bestDirection=dirs;
						trailLength=trail;
						nextRoom=thisRoom;
					}
				}
			}
		}
		return bestDirection;
	}

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
				nextDirection=nextDirectionFromHere(mob.location());
	}

	public boolean findTheBastard(Room location, int tryCode, Vector dirVec)
	{
		if(lookedIn==null) return false;
		if(lookedIn.get(location)!=null)
			return false;
		lookedIn.put(location,location);
		for(int x=0;x<dirVec.size();x++)
		{
			int i=((Integer)dirVec.elementAt(x)).intValue();
			Room nextRoom=location.getRoomInDir(i);
			Exit nextExit=location.getExitInDir(i);
			if((nextRoom!=null)&&(nextExit!=null))
			{
				MOB mobCheck=animalHere(nextRoom);
				if((mobCheck!=null)
				||(findTheBastard(nextRoom,tryCode,dirVec)))
				{
					if(mobCheck!=null)
						trackingWhom=mobCheck;

					if(theTrail==null)
						theTrail=new Vector();
					theTrail.addElement(nextRoom);
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean findBastardTheBestWay(Room location)
	{
		Vector trailArray[] = new Vector[TRACK_ATTEMPTS];
		MOB trackArray[] = new MOB[TRACK_ATTEMPTS];
		
		for(int t=0;t<TRACK_ATTEMPTS;t++)
		{
			lookedIn=new Hashtable();
			theTrail=null;
			trackingWhom=null;
			Vector dirVec=new Vector();
			while(dirVec.size()<Directions.NUM_DIRECTIONS)
			{
				int direction=Dice.roll(1,Directions.NUM_DIRECTIONS,-1);
				for(int x=0;x<dirVec.size();x++)
					if(((Integer)dirVec.elementAt(x)).intValue()==direction)
						continue;
				dirVec.addElement(new Integer(direction));
			}
			findTheBastard(location,2,dirVec);
			trailArray[t]=theTrail;
			trackArray[t]=trackingWhom;
		}
		int winner=-1;
		int winningTotal=Integer.MAX_VALUE;
		for(int t=0;t<TRACK_ATTEMPTS;t++)
		{
			Vector V=trailArray[t];
			MOB whom=trackArray[t];
			if((V!=null)&&(whom!=null)&&(V.size()<winningTotal))
			{
				winningTotal=V.size();
				winner=t;
			}
		}
		if(winner<0)
		{
			trackingWhom=null;
			theTrail=null;
			return false;
		}
		else
		{
			trackingWhom=trackArray[winner];
			theTrail=trailArray[winner];
			return true;
		}
	}

	public MOB animalHere(Room room)
	{
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

		trackingWhom=null;
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

		lookedIn=new Hashtable();
		findBastardTheBestWay(mob.location());

		if((success)&&(trackingWhom!=null)&&(theTrail!=null))
		{
			theTrail.addElement(mob.location());
			MOB target=trackingWhom;
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,"<S-NAME> chant(s) for the animals.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				displayText="(seeking "+trackingWhom.name()+")";
				Chant_LocateAnimals newOne=(Chant_LocateAnimals)this.copyOf();
				if(mob.fetchAffect(newOne.ID())==null)
					mob.addAffect(newOne);
				mob.recoverEnvStats();
				newOne.nextDirection=newOne.nextDirectionFromHere(mob.location());
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) for the animals, but nothing happens.");


		// return whether it worked
		return success;
	}
}