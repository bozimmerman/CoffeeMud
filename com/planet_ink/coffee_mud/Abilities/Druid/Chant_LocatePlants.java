package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Chant_LocatePlants extends Chant
{
	Room lastRoom=null;
	private Vector theTrail=null;
	private Hashtable lookedIn=null;
	public int nextDirection=-2;
	protected final static int TRACK_ATTEMPTS=25;
	public Chant_LocatePlants()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Locate Plants";
		displayText="(locating plants)";
		miscText="";

		baseEnvStats().setLevel(1);

		canBeUninvoked=true;
		isAutoinvoked=false;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_LocatePlants();
	}
	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		if(canBeUninvoked)
			lastRoom=null;
		super.unInvoke();
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
			nextDirection=nextDirectionFromHere(mob.location());
	}

	public boolean findTheBastard(Room location, MOB mob, int tryCode, Vector dirVec)
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
				if((plantsHere(mob,nextRoom).length()>0)
				||(findTheBastard(nextRoom,mob,tryCode,dirVec)))
				{
					if(theTrail==null)
						theTrail=new Vector();
					theTrail.addElement(nextRoom);
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean findBastardTheBestWay(Room location, MOB mob)
	{
		
		Vector trailArray[] = new Vector[TRACK_ATTEMPTS];
		
		for(int t=0;t<TRACK_ATTEMPTS;t++)
		{
			lookedIn=new Hashtable();
			theTrail=null;
			Vector dirVec=new Vector();
			while(dirVec.size()<Directions.NUM_DIRECTIONS)
			{
				int direction=Dice.roll(1,Directions.NUM_DIRECTIONS,-1);
				for(int x=0;x<dirVec.size();x++)
					if(((Integer)dirVec.elementAt(x)).intValue()==direction)
						continue;
				dirVec.addElement(new Integer(direction));
			}
			findTheBastard(location,mob,2,dirVec);
			trailArray[t]=theTrail;
		}
		int winner=-1;
		int winningTotal=Integer.MAX_VALUE;
		for(int t=0;t<TRACK_ATTEMPTS;t++)
		{
			Vector V=trailArray[t];
			if((V!=null)&&(V.size()<winningTotal))
			{
				winningTotal=V.size();
				winner=t;
			}
		}
		if(winner<0)
		{
			theTrail=null;
			return false;
		}
		else
		{
			theTrail=trailArray[winner];
			return true;
		}
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

		lookedIn=new Hashtable();
		findBastardTheBestWay(mob.location(),mob);

		if((success)&&(theTrail!=null))
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"<S-NAME> begin(s) to sense plant life!":"^S<S-NAME> chant(s) for a route to plant life.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Chant_LocatePlants newOne=(Chant_LocatePlants)this.copyOf();
				if(mob.fetchAffect(newOne.ID())==null)
					mob.addAffect(newOne);
				mob.recoverEnvStats();
				newOne.nextDirection=newOne.nextDirectionFromHere(mob.location());
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) to find plant life, but fail(s).");

		return success;
	}
}