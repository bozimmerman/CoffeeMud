package com.planet_ink.coffee_mud.commands;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;
public class Movement
{
	public void go(MOB mob, Vector commands)
	{
		int direction=Directions.getGoodDirectionCode(Util.combine(commands,1));
		if(direction>=0)
			move(mob,direction,false);
		else
		{
			mob.tell("Try north, south, east, west, up, or down.");
			return;
		}
	}

	public boolean move(MOB mob, int directionCode, boolean flee)
	{
		if(directionCode<0) return false;
		if(mob==null) return false;
		Room thisRoom=mob.location();
		if(thisRoom==null) return false;
		Room destRoom=thisRoom.doors()[directionCode];
		Exit exit=thisRoom.exits()[directionCode];
		if(destRoom==null)
		{
			mob.tell("You can't go that way.");
			return false;
		}

		Exit opExit=thisRoom.getReverseExit(directionCode);
		String directionName=Directions.getDirectionName(directionCode);
		String otherDirectionName=Directions.getFromDirectionName(Directions.getOpDirectionCode(directionCode));

		int leaveCode=Affect.MSG_LEAVE;
		if(flee) leaveCode=Affect.MSG_FLEE;

		
		FullMsg enterMsg=new FullMsg(mob,destRoom,exit,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,"<S-NAME> "+Sense.dispositionString(mob,Sense.flag_arrives)+" from "+otherDirectionName);
		FullMsg leaveMsg=new FullMsg(mob,thisRoom,opExit,leaveCode,((flee)?"You flee "+directionName:null),leaveCode,null,leaveCode,"<S-NAME> "+((flee)?"flees":Sense.dispositionString(mob,Sense.flag_leaves))+" "+directionName);
		if((exit==null)&&(!mob.isASysOp()))
		{
			mob.tell("You can't go that way.");
			return false;
		}
		else
		if(exit==null)
			thisRoom.show(mob,null,Affect.MSG_OK_VISUAL,"The area to the "+directionName+" shimmers and becomes transparent.");
		else
		if((exit!=null)&&(!exit.okAffect(enterMsg)))
			return false;
		else
		if(!leaveMsg.target().okAffect(leaveMsg))
			return false;
		else
		if((opExit!=null)&&(!opExit.okAffect(leaveMsg)))
			return false;
		else
		if(!enterMsg.target().okAffect(enterMsg))
			return false;
		else
		if(!mob.okAffect(enterMsg))
			return false;

		mob.curState().expendEnergy(mob,mob.maxState(),true);
		if((!flee)&&(!mob.curState().adjMovement(-thisRoom.pointsPerMove(),mob.maxState())))
		{
			mob.tell("You are too tired.");
			return false;
		}

		if(exit!=null) exit.affect(enterMsg);
		mob.location().delInhabitant(mob);
		((Room)leaveMsg.target()).send(mob,leaveMsg);

		mob.setLocation((Room)enterMsg.target());
		((Room)enterMsg.target()).addInhabitant(mob);
		((Room)enterMsg.target()).send(mob,enterMsg);

		if(opExit!=null) opExit.affect(leaveMsg);
		ExternalPlay.look(mob,null,true);

		if(!flee)
		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB follower=mob.fetchFollower(f);
			if((follower!=null)
			&&(follower.amFollowing()==mob)
			&&((follower.location()==thisRoom)||(follower.location()==destRoom)))
			{
				if(follower.location()==thisRoom)
				{
					follower.tell("You follow "+mob.name()+" "+Directions.getDirectionName(directionCode)+".");
					move(follower,directionCode,false);
				}
			}
			else
				follower.setFollowing(null);
		}
		return true;
	}

	public void flee(MOB mob, String direction)
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
					Exit thisExit=mob.location().exits()[i];
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
		int lostExperience=10;
		if(mob.getVictim()!=null)
		{
			lostExperience=10+((mob.envStats().level()-mob.getVictim().envStats().level()))*5;
			if(lostExperience<10) lostExperience=10;
		}
		if(((directionCode>=0)&&(move(mob,directionCode,true)))||(direction.equals("NOWHERE")))
		{
			mob.makePeace();
			mob.setExperience(mob.getExperience()-lostExperience);
			mob.tell("You lose "+lostExperience+" experience points for withdrawing.");
		}
	}

	public void open(MOB mob, String whatToOpen)
	{
		if(whatToOpen.length()==0)
		{
			mob.tell("Open what?");
			return;
		}
		Environmental openThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(dirCode>=0)
			openThis=mob.location().exits()[dirCode];
		if(openThis==null)
			openThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,whatToOpen);

		if((openThis==null)||(!Sense.canBeSeenBy(openThis,mob)))
		{
			mob.tell("You don't see '"+whatToOpen+"' here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,openThis,null,Affect.MSG_OPEN,"<S-NAME> open(s) "+openThis.name());
		if(openThis instanceof Exit)
			roomOkAndAffectFully(msg,mob.location(),dirCode);
		else
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}

	public void unlock(MOB mob, String whatTounlock)
	{
		if(whatTounlock.length()==0)
		{
			mob.tell("Unlock what?");
			return;
		}
		Environmental unlockThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatTounlock);
		if(dirCode>=0)
			unlockThis=mob.location().exits()[dirCode];
		if(unlockThis==null)
			unlockThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,whatTounlock);

		if((unlockThis==null)||(!Sense.canBeSeenBy(unlockThis,mob)))
		{
			mob.tell("You don't see '"+whatTounlock+"' here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,unlockThis,null,Affect.MSG_UNLOCK,"<S-NAME> unlock(s) "+unlockThis.name());
		if(unlockThis instanceof Exit)
			roomOkAndAffectFully(msg,mob.location(),dirCode);
		else
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}

	public void close(MOB mob, String whatToClose)
	{
		if(whatToClose.length()==0)
		{
			mob.tell("Close what?");
			return;
		}
		Environmental closeThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatToClose);
		if(dirCode>=0)
			closeThis=mob.location().exits()[dirCode];
		if(closeThis==null)
			closeThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,whatToClose);

		if((closeThis==null)||(!Sense.canBeSeenBy(closeThis,mob)))
		{
			mob.tell("You don't see '"+whatToClose+"' here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,closeThis,null,Affect.MSG_CLOSE,"<S-NAME> close(s) "+closeThis.name());
		if(closeThis instanceof Exit)
			roomOkAndAffectFully(msg,mob.location(),dirCode);
		else
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}

	public void roomAffectFully(Affect msg, Room room, int dirCode)
	{
		room.send(msg.source(),msg);
		if((msg.target()==null)||(!(msg.target() instanceof Exit)))
			return;
		dirCode=getMyDirCode((Exit)msg.target(),room,dirCode);
		if(dirCode<0) return;
		Exit pair=room.getPairedExit(dirCode);
		FullMsg altMsg=new FullMsg(msg.source(),pair,msg.tool(),msg.sourceCode(),null,msg.targetCode(),null,msg.othersCode(),null);
		if(pair!=null)
			pair.affect(altMsg);
	}

	public int getMyDirCode(Exit exit, Room room, int testCode)
	{
		if(testCode>=0) return testCode;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			if(room.exits()[d]==exit) return d;
		return -1;
	}

	public boolean roomOkAndAffectFully(FullMsg msg, Room room, int dirCode)
	{
		if((msg.target()==null)||(!(msg.target() instanceof Exit)))
			return room.okAffect(msg);
		
		Exit thisExit=(Exit)msg.target();
		if(!room.okAffect(msg))
			return false;
		dirCode=getMyDirCode(thisExit,room,dirCode);
		if(dirCode<0) return true;
		Exit pair=room.getPairedExit(dirCode);
		if(pair!=null)
		{
			//FullMsg altMsg=new FullMsg(msg.source(),pair,msg.tool(),msg.sourceCode(),null,msg.targetCode(),null,msg.othersCode(),null);
			//if(!pair.okAffect(altMsg))
			//	return false;
		}
		roomAffectFully(msg,room,dirCode);
		return true;
	}

	public void lock(MOB mob, String whatTolock)
	{
		if(whatTolock.length()==0)
		{
			mob.tell("Lock what?");
			return;
		}
		Environmental lockThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatTolock);
		if(dirCode>=0)
			lockThis=mob.location().exits()[dirCode];
		if(lockThis==null)
			lockThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,whatTolock);

		if((lockThis==null)||(!Sense.canBeSeenBy(lockThis,mob)))
		{
			mob.tell("You don't see '"+whatTolock+"' here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,lockThis,null,Affect.MSG_LOCK,"<S-NAME> lock(s) "+lockThis.name());
		if(lockThis instanceof Exit)
			roomOkAndAffectFully(msg,mob.location(),dirCode);
		else
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}

	public void sit(MOB mob)
	{
		if(Sense.isSitting(mob))
			mob.tell("You are already sitting!");
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_SIT,"<S-NAME> sit(s) down and take(s) a rest.");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
	}
	public void wake(MOB mob)
	{
		if(!Sense.isSleeping(mob))
			mob.tell("You aren't sleeping!?");
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_STAND,"<S-NAME> awake(s) and stand(s) up.");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
	}
	public void sleep(MOB mob)
	{
		if(Sense.isSleeping(mob))
			mob.tell("You are already asleep!");
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_SLEEP,"<S-NAME> lay(s) down and take(s) a nap.");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
	}

	public void standIfNecessary(MOB mob)
	{
		if((!mob.amDead())&&(Sense.isSleeping(mob)||Sense.isSitting(mob)))
			stand(mob);
	}

	public void stand(MOB mob)
	{
		if((!Sense.isSitting(mob))&&(!Sense.isSleeping(mob)))
			mob.tell("You are already standing!");
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_STAND,"<S-NAME> stand(s) up.");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
	}
}
