package com.planet_ink.coffee_mud.commands;

import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.db.*;
import java.io.*;
import java.util.*;
public class Movement
{
	public static void go(MOB mob, Vector commands)
	{
		int direction=Directions.getGoodDirectionCode(CommandProcessor.combine(commands,1));
		if(direction>=0)
			Movement.move(mob,direction,false);
		else
		{
			mob.tell("Try north, south, east, west, up, or down.");
			return;
		}
	}

	public static void move(MOB mob, int directionCode, boolean flee)
	{
		if(directionCode<0) return;
		if(mob==null) return;
		Room thisRoom=mob.location();
		if(thisRoom==null) return;
		Room destRoom=thisRoom.getRoom(directionCode);
		Exit exit=thisRoom.getExit(directionCode);
		if(destRoom==null)
		{
			mob.tell("You can't go that way.");
			return;
		}

		Exit opExit=destRoom.getExit(Directions.getOpDirectionCode(directionCode));

		String directionName=Directions.getDirectionName(directionCode);
		String otherDirectionName=Directions.getFromDirectionName(Directions.getOpDirectionCode(directionCode));

		int leaveCode=Affect.MOVE_LEAVE;
		if(flee)leaveCode=Affect.MOVE_FLEE;

		FullMsg enterMsg=new FullMsg(mob,destRoom,exit,Affect.MOVE_ENTER,null,Affect.MOVE_ENTER,null,Affect.MOVE_ENTER,"<S-NAME> "+Sense.dispositionString(mob,Sense.flag_arrives)+" from "+otherDirectionName);
		FullMsg leaveMsg=new FullMsg(mob,thisRoom,opExit,leaveCode,((flee)?"You flee "+directionName:null),leaveCode,null,leaveCode,"<S-NAME> "+((flee)?"flees":Sense.dispositionString(mob,Sense.flag_leaves))+" "+directionName);
		if((exit==null)&&(!mob.isASysOp()))
		{
			mob.tell("You can't go that way.");
			return;
		}
		else
		if(exit==null)
			thisRoom.show(mob,null,Affect.VISUAL_WNOISE,"The area to the "+directionName+" shimmers and becomes transparent.");
		else
		if((exit!=null)&&(!exit.okAffect(enterMsg)))
			return;
		else
		if(!leaveMsg.target().okAffect(leaveMsg))
			return;
		else
		if((opExit!=null)&&(!opExit.okAffect(leaveMsg)))
			return;
		else
		if(!enterMsg.target().okAffect(enterMsg))
			return;

		mob.curState().expendEnergy(mob,mob.maxState(),true);
		if(!mob.curState().adjMovement(-thisRoom.pointsPerMove(),mob.maxState()))
		{
			mob.tell("You are too tired.");
			return;
		}

		if(exit!=null) exit.affect(enterMsg);
		mob.location().delInhabitant(mob);
		((Room)leaveMsg.target()).send(mob,leaveMsg);

		mob.setLocation((Room)enterMsg.target());
		((Room)enterMsg.target()).addInhabitant(mob);
		((Room)enterMsg.target()).send(mob,enterMsg);

		if(opExit!=null) opExit.affect(leaveMsg);
		BasicSenses.look(mob,null,true);

		if(!flee)
		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB follower=mob.fetchFollower(f);
			if(follower.amFollowing()==mob)
			{
				follower.tell("You follow "+mob.name()+" "+Directions.getDirectionName(directionCode)+".");
				move(follower,directionCode,false);
			}
			else
				follower.setFollowing(null);
		}
	}

	public static void flee(MOB mob, String direction)
	{
		if((mob.location()==null)||(!mob.isInCombat()))
		{
			mob.tell("You can only flee while in combat.");
			return;
		}

		int directionCode=-1;
		if(!direction.equals("NOWHERE"))
		{
			if(direction.length()==0)
			{
				for(int i=0;i<7;i++)
				{
					Exit thisExit=mob.location().getExit(i);
					if(thisExit!=null)
					{
						if(thisExit.isOpen())
						{
							direction=Directions.getDirectionName(i);
							break;
						}
					}
				}
			}
			directionCode=Directions.getDirectionCode(direction);
			if(directionCode<0)
			{
				mob.tell("Flee where?!");
				return;
			}
		}
		int lostExperience=10+((mob.envStats().level()-mob.getVictim().envStats().level()))*5;
		if(lostExperience<10) lostExperience=10;
		mob.setExperience(mob.getExperience()-lostExperience);
		mob.tell("You lose "+lostExperience+" experience points for withdrawing.");
		mob.makePeace();
		if(directionCode>=0)
			move(mob,directionCode,true);
	}

	public static void open(MOB mob, String whatToOpen)
	{

		Integer cmd=(Integer)CommandProcessor.commandSet.get(whatToOpen.toUpperCase());
		Environmental openThis=null;
		if(cmd!=null)
		{
			int dir=cmd.intValue();
			if(
			  (dir==CommandSet.NORTH)
			||(dir==CommandSet.SOUTH)
			||(dir==CommandSet.EAST)
			||(dir==CommandSet.WEST)
			||(dir==CommandSet.UP)
			||(dir==CommandSet.DOWN))
			{
				int dirCode=Directions.getDirectionCode(whatToOpen);
				openThis=mob.location().getExit(dirCode);
			}
		}
		if(openThis==null)
			openThis=mob.location().fetchFromMOBRoom(mob,null,whatToOpen);

		if((openThis==null)||(!Sense.canBeSeenBy(openThis,mob)))
		{
			mob.tell("You don't see that here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,openThis,null,Affect.HANDS_OPEN,Affect.HANDS_OPEN,Affect.VISUAL_WNOISE,"<S-NAME> open(s) "+openThis.name());
		if(!mob.location().okAffect(msg))
			return;
		mob.location().send(mob,msg);
	}

	public static void unlock(MOB mob, String whatTounlock)
	{

		Integer cmd=(Integer)CommandProcessor.commandSet.get(whatTounlock.toUpperCase());
		Environmental unlockThis=null;
		if(cmd!=null)
		{
			int dir=cmd.intValue();
			if(
			  (dir==CommandSet.NORTH)
			||(dir==CommandSet.SOUTH)
			||(dir==CommandSet.EAST)
			||(dir==CommandSet.WEST)
			||(dir==CommandSet.UP)
			||(dir==CommandSet.DOWN))
			{
				int dirCode=Directions.getDirectionCode(whatTounlock);
				unlockThis=mob.location().getExit(dirCode);
			}
		}
		if(unlockThis==null)
			unlockThis=mob.location().fetchFromMOBRoom(mob,null,whatTounlock);

		if((unlockThis==null)||(!Sense.canBeSeenBy(unlockThis,mob)))
		{
			mob.tell("You don't see that here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,unlockThis,null,Affect.HANDS_UNLOCK,Affect.HANDS_UNLOCK,Affect.VISUAL_WNOISE,"<S-NAME> unlock(s) "+unlockThis.name());
		if(!mob.location().okAffect(msg))
			return;
		mob.location().send(mob,msg);
	}

	public static void close(MOB mob, String whatToClose)
	{

		Integer cmd=(Integer)CommandProcessor.commandSet.get(whatToClose.toUpperCase());
		Environmental closeThis=null;
		if(cmd!=null)
		{
			int dir=cmd.intValue();
			if(
			  (dir==CommandSet.NORTH)
			||(dir==CommandSet.SOUTH)
			||(dir==CommandSet.EAST)
			||(dir==CommandSet.WEST)
			||(dir==CommandSet.UP)
			||(dir==CommandSet.DOWN))
				closeThis=mob.location().getExit(Directions.getDirectionCode(whatToClose));
		}
		if(closeThis==null)
			closeThis=mob.location().fetchFromMOBRoom(mob,null,whatToClose);

		if((closeThis==null)||(!Sense.canBeSeenBy(closeThis,mob)))
		{
			mob.tell("You don't see that here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,closeThis,null,Affect.HANDS_CLOSE,Affect.HANDS_CLOSE,Affect.VISUAL_WNOISE,"<S-NAME> close(s) "+closeThis.name());
		if(!mob.location().okAffect(msg))
			return;
		mob.location().send(mob,msg);
	}

	public static void lock(MOB mob, String whatTolock)
	{
		Integer cmd=(Integer)CommandProcessor.commandSet.get(whatTolock.toUpperCase());
		Environmental lockThis=null;
		if(cmd!=null)
		{
			int dir=cmd.intValue();
			if(
			  (dir==CommandSet.NORTH)
			||(dir==CommandSet.SOUTH)
			||(dir==CommandSet.EAST)
			||(dir==CommandSet.WEST)
			||(dir==CommandSet.UP)
			||(dir==CommandSet.DOWN))
				lockThis=mob.location().getExit(Directions.getDirectionCode(whatTolock));
		}
		if(lockThis==null)
			lockThis=mob.location().fetchFromMOBRoom(mob,null,whatTolock);

		if((lockThis==null)||(!Sense.canBeSeenBy(lockThis,mob)))
		{
			mob.tell("You don't see that here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,lockThis,null,Affect.HANDS_LOCK,Affect.HANDS_LOCK,Affect.VISUAL_WNOISE,"<S-NAME> lock(s) "+lockThis.name());
		if(!mob.location().okAffect(msg))
			return;
		mob.location().send(mob,msg);
	}

	public static void sit(MOB mob)
	{
		FullMsg msg=new FullMsg(mob,null,null,Affect.MOVE_SIT,Affect.MOVE_SIT,Affect.MOVE_GENERAL,"<S-NAME> sit(s) down and take(s) a rest.");
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}
	public static void wake(MOB mob)
	{
		if(!Sense.isSleeping(mob))
		{
			mob.tell("You aren't sleeping!?");
			return;
		}
		FullMsg msg=new FullMsg(mob,null,null,Affect.MOVE_SIT,Affect.MOVE_SIT,Affect.MOVE_GENERAL,"<S-NAME> awake(s) and sit(s) up.");
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}
	public static void sleep(MOB mob)
	{
		FullMsg msg=new FullMsg(mob,null,null,Affect.MOVE_SLEEP,Affect.MOVE_SLEEP,Affect.MOVE_GENERAL,"<S-NAME> lay(s) down and take(s) a nap.");
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}

	public static void standIfNecessary(MOB mob)
	{
		if(Sense.isSleeping(mob)||Sense.isSitting(mob))
			stand(mob);
	}

	public static void stand(MOB mob)
	{
		FullMsg msg=new FullMsg(mob,null,null,Affect.MOVE_STAND,Affect.MOVE_STAND,Affect.MOVE_GENERAL,"<S-NAME> stand(s) up.");
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}
}
