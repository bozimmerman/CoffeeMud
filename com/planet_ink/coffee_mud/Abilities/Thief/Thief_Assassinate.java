package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Assassinate extends ThiefSkill
{
	public String ID() { return "Thief_Assassinate"; }
	public String name(){ return "Assassinate";}
	private String displayText="(Tracking)";
	public String displayText(){ return displayText;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"ASSASSINATE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Thief_Assassinate();}
	private Vector theTrail=null;
	public int nextDirection=-2;
	protected MOB tracking=null;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
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

			
			if(mob.location()==null) return false;
			if(mob.location().isInhabitant(tracking))
			{
				if(Sense.isHidden(mob))
				{
					Ability A=mob.fetchAbility("Thief_BackStab");
					if(A!=null)
						A.invoke(mob,tracking,false);
				}
				else
					ExternalPlay.postAttack(mob,tracking,mob.fetchWieldedItem());
				return false;
			}
			
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				Room nextRoom=mob.location().getRoomInDir(d);
				Exit nextExit=mob.location().getExitInDir(d);
				if((nextRoom!=null)
				   &&(nextExit!=null)
				   &&(nextExit.isOpen())
				   &&(nextRoom.isInhabitant(tracking)))
				{
					nextDirection=d; break;
				}
			}
			
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
					{
						if(!nextRoom.isInhabitant(tracking))
						{
							Ability A=mob.fetchAbility("Thief_Sneak");
							if(A!=null)
							{
								Vector V=new Vector();
								V.addElement(Directions.getDirectionName(nextDirection));
								A.invoke(mob,V,null,false);
							}
							else
								ExternalPlay.move(mob,nextDirection,false,false);
						}
						else
							ExternalPlay.move(mob,nextDirection,false,false);
					}
					else
						unInvoke();
				}
				nextDirection=-2;
			}

		}
		return true;
	}

	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);

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
		if(oldTrack==null) oldTrack=mob.fetchAffect("Thief_Assassinate");
		if(oldTrack!=null)
		{
			mob.tell("You stop tracking.");
			oldTrack.unInvoke();
			if(commands.size()==0) return true;
		}

		theTrail=null;
		nextDirection=-2;

		tracking=null;
		String mobName="";
		if((mob.fetchAffect("Thief_Mark")!=null)&&(!mob.isMonster()))
		{
			Thief_Mark A=(Thief_Mark)mob.fetchAffect("Thief_Mark");
			if(A!=null) tracking=A.mark;
			if(tracking==null)
			{
				mob.tell("You'll need to Mark someone first.");
				return false;
			}
		}
		else
		{
			if(givenTarget!=null)
				mobName=givenTarget.name();
			else
				mobName=Util.combine(commands,0);
			if(mobName.length()==0)
			{
				mob.tell("Assassinate whom?");
				return false;
			}
			MOB M=mob.location().fetchInhabitant(mobName);
			if(M!=null)
			{
				ExternalPlay.postAttack(mob,M,mob.fetchWieldedItem());
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(0,auto);

		Vector rooms=new Vector();
		if(tracking!=null)
		{
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((R!=null)&&(R.isInhabitant(tracking)))
				{
					rooms.addElement(R);
					break;
				}
			}
		}
		else
		if(mobName.length()>0)
		{
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
		}
		
		if(rooms.size()>0)
			theTrail=ExternalPlay.findBastardTheBestWay(mob.location(),rooms,true);
		
		if((tracking==null)&&(theTrail!=null)&&(theTrail.size()>0))
			tracking=((Room)theTrail.firstElement()).fetchInhabitant(mobName);

		if((success)&&(theTrail!=null)&&(tracking!=null))
		{
			theTrail.addElement(mob.location());
			
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,tracking,this,Affect.MSG_THIEF_ACT,mob.isMonster()?null:"<S-NAME> begin(s) to track <T-NAMESELF> for assassination.",Affect.NO_EFFECT,null,Affect.NO_EFFECT,null);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				displayText="(tracking "+tracking.name()+")";
				Thief_Assassinate newOne=(Thief_Assassinate)this.copyOf();
				if(mob.fetchAffect(newOne.ID())==null)
					mob.addAffect(newOne);
				mob.recoverEnvStats();
				newOne.nextDirection=ExternalPlay.trackNextDirectionFromHere(theTrail,mob.location(),true);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to track "+tracking.name()+" for assassination, but fail(s).");


		// return whether it worked
		return success;
	}
}