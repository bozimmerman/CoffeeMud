package com.planet_ink.coffee_mud.Abilities.Ranger;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Ranger_Track extends StdAbility
{
	public String ID() { return "Ranger_Track"; }
	public String name(){ return "Track";}
	private String displayText="(Tracking)";
	public String displayText(){ return displayText;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"TRACK"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	
	private Vector theTrail=null;
	public int nextDirection=-2;
	public Environmental newInstance(){	return new Ranger_Track();}

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
				if(mob.isMonster())
				{
					Room nextRoom=mob.location().getRoomInDir(nextDirection);
					if((nextRoom!=null)&&(nextRoom.getArea()==mob.location().getArea()))
						ExternalPlay.move(mob,nextDirection,false,false);
					else
						unInvoke();
				}
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
			nextDirection=ExternalPlay.trackNextDirectionFromHere(theTrail,mob.location(),true);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!Sense.aliveAwakeMobile(mob,false))
			return false;

		if(!Sense.canBeSeenBy(mob.location(),mob))
		{
			mob.tell("You can't see anything to track!");
			return false;
		}

		Ability oldTrack=mob.fetchAffect("Ranger_Track");
		if(oldTrack==null) oldTrack=mob.fetchAffect("Ranger_TrackAnimal");
		if(oldTrack!=null)
		{
			mob.tell(mob,null,"You stop tracking.");
			oldTrack.unInvoke();
			if(commands.size()==0) return true;
		}

		theTrail=null;
		nextDirection=-2;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		String mobName=Util.combine(commands,0);
		if(mobName.length()==0)
		{
			mob.tell("Track whom?");
			return false;
		}

		if(mob.location().fetchInhabitant(mobName)!=null)
		{
			mob.tell("Try 'look'.");
			return false;
		}

		boolean success=profficiencyCheck(0,auto);

		Vector rooms=new Vector();
		for(Enumeration r=mob.location().getArea().getMap();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(R.fetchInhabitant(mobName)!=null)
				rooms.addElement(R);
		}
		
		if(rooms.size()<=0)
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if((R!=null)&&(R.fetchInhabitant(mobName)!=null))
				rooms.addElement(R);
		}
		
		if(rooms.size()>0)
			theTrail=ExternalPlay.findBastardTheBestWay(mob.location(),rooms,true);
		
		MOB target=null;
		if((theTrail!=null)&&(theTrail.size()>0))
			target=((Room)theTrail.firstElement()).fetchInhabitant(mobName);

		if((success)&&(theTrail!=null)&&(target!=null))
		{
			theTrail.addElement(mob.location());
			
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSG_QUIETMOVEMENT,mob.isMonster()?null:"<S-NAME> begin(s) to track <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				displayText="(tracking "+target.name()+")";
				Ranger_Track newOne=(Ranger_Track)this.copyOf();
				if(mob.fetchAffect(newOne.ID())==null)
					mob.addAffect(newOne);
				mob.recoverEnvStats();
				newOne.nextDirection=ExternalPlay.trackNextDirectionFromHere(theTrail,mob.location(),true);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to track "+mobName+", but can't find the trail.");


		// return whether it worked
		return success;
	}
}