package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Track extends StdAbility
{
	public String ID() { return "Skill_Track"; }
	public String name(){ return "Tracking";}
	private String displayText="(Tracking)";
	public String displayText(){ return displayText;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS|CAN_ROOMS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"TRACKTO"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public long flags(){return Ability.FLAG_TRACKING;}
	
	private Vector theTrail=null;
	public int nextDirection=-2;
	public Environmental newInstance(){	return new Skill_Track();}

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
					Room oldRoom=mob.location();
					Room nextRoom=oldRoom.getRoomInDir(nextDirection);
					Exit nextExit=oldRoom.getExitInDir(nextDirection);
					int opDirection=Directions.getOpDirectionCode(nextDirection);
					if((nextRoom!=null)&&(nextExit!=null))
					{
						boolean reclose=false;
						boolean relock=false;
						// handle doors!
						if(nextExit.hasADoor()&&(!nextExit.isOpen()))
						{
							if((nextExit.hasALock())&&(nextExit.isLocked()))
							{
								FullMsg msg=new FullMsg(mob,nextExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,null);
								if(oldRoom.okAffect(mob,msg))
								{
									relock=true;
									msg=new FullMsg(mob,nextExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_UNLOCK,Affect.MSG_OK_VISUAL,"<S-NAME> unlock(s) <T-NAMESELF>.");
									ExternalPlay.roomAffectFully(msg,oldRoom,nextDirection);
								}
							}
							FullMsg msg=new FullMsg(mob,nextExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,null);
							if(oldRoom.okAffect(mob,msg))
							{
								reclose=true;
								msg=new FullMsg(mob,nextExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_OPEN,Affect.MSG_OK_VISUAL,"<S-NAME> open(s) <T-NAMESELF>.");
								ExternalPlay.roomAffectFully(msg,oldRoom,nextDirection);
							}
						}
						if(!nextExit.isOpen())
							unInvoke();
						else
						{
							int dir=nextDirection;
							nextDirection=-2;
							ExternalPlay.move(mob,dir,false,false);
							if((reclose)&&(mob.location()==nextRoom))
							{
								Exit opExit=nextRoom.getExitInDir(opDirection);
								if((opExit!=null)
								&&(opExit.hasADoor())
								&&(opExit.isOpen()))
								{
									FullMsg msg=new FullMsg(mob,opExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,null);
									if(nextRoom.okAffect(mob,msg))
									{
										msg=new FullMsg(mob,opExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_CLOSE,Affect.MSG_OK_VISUAL,"<S-NAME> close(s) <T-NAMESELF>.");
										ExternalPlay.roomAffectFully(msg,nextRoom,opDirection);
									}
									if((opExit.hasALock())&&(relock))
									{
										msg=new FullMsg(mob,opExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,null);
										if(nextRoom.okAffect(mob,msg))
										{
											msg=new FullMsg(mob,opExit,null,Affect.MSG_OK_VISUAL,Affect.MSG_LOCK,Affect.MSG_OK_VISUAL,"<S-NAME> lock(s) <T-NAMESELF>.");
											ExternalPlay.roomAffectFully(msg,nextRoom,opDirection);
										}
									}
								}
							}
						}
					}
					else
						unInvoke();
				}
				else
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
		&&(affect.targetMinor()==Affect.TYP_EXAMINESOMETHING))
			nextDirection=SaucerSupport.trackNextDirectionFromHere(theTrail,mob.location(),true);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!Sense.aliveAwakeMobile(mob,false))
			return false;

		boolean stoppedTracking=false;
		for(int a=mob.numAffects()-1;a>=0;a--)
		{
			Ability A=mob.fetchAffect(a);
			if((A!=null)&&(Util.bset(A.flags(),Ability.FLAG_TRACKING)))
			{ stoppedTracking=true; A.unInvoke();}
		}
		if(stoppedTracking)
		{
			mob.tell("You stop tracking.");
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
		if(givenTarget instanceof Room)
			rooms.addElement(givenTarget);
		else
		if((givenTarget instanceof MOB)&&(((MOB)givenTarget).location()!=null))
			rooms.addElement(((MOB)givenTarget).location());
		
		if(rooms.size()<=0)
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
			theTrail=SaucerSupport.findBastardTheBestWay(mob.location(),rooms,false);
		
		MOB target=null;
		if((theTrail!=null)&&(theTrail.size()>0))
			target=((Room)theTrail.firstElement()).fetchInhabitant(mobName);

		if((success)&&(theTrail!=null))
		{
			theTrail.addElement(mob.location());
			
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,Affect.MSG_QUIETMOVEMENT,mob.isMonster()?null:"<S-NAME> begin(s) to track.");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Skill_Track newOne=(Skill_Track)this.copyOf();
				if(mob.fetchAffect(newOne.ID())==null)
					mob.addAffect(newOne);
				mob.recoverEnvStats();
				newOne.nextDirection=SaucerSupport.trackNextDirectionFromHere(theTrail,mob.location(),false);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to track, but can't find the trail.");


		// return whether it worked
		return success;
	}
}