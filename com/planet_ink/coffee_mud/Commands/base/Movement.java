package com.planet_ink.coffee_mud.Commands.base;

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
			String doing=(String)commands.elementAt(0);
			mob.tell(Character.toUpperCase(doing.charAt(0))+doing.substring(1)+" which direction?");
			mob.tell("Try north, south, east, west, up, or down.");
			return;
		}
	}

	public void enter(MOB mob, Vector commands)
	{
		if(commands.size()<=1)
		{
			mob.tell("Enter what or where? Try EXITS.");
			return;
		}
		String enterWhat=Util.combine(commands,1).toUpperCase();
		int dir=-1;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Exit e=mob.location().getExitInDir(d);
			if((e!=null)&&(mob.location().getRoomInDir(d)!=null))
			{
				if((Sense.canBeSeenBy(e,mob))
				&&((e.name().equalsIgnoreCase(enterWhat))
				||(e.displayText().equalsIgnoreCase(enterWhat))
				||(e.description().equalsIgnoreCase(enterWhat))))
				{
					dir=d; break;
				}
			}
		}
		if(dir<0)
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Exit e=mob.location().getExitInDir(d);
			if((e!=null)&&(mob.location().getRoomInDir(d)!=null))
			{
				if((Sense.canBeSeenBy(e,mob))
				&&(((CoffeeUtensils.containsString(e.name().toUpperCase(),enterWhat))
				||(CoffeeUtensils.containsString(e.displayText().toUpperCase(),enterWhat))
				||(CoffeeUtensils.containsString(e.description().toUpperCase(),enterWhat)))))
				{
					dir=d; break;
				}
			}
		}
		if(dir<0)
		{
			Environmental getThis=mob.location().fetchFromRoomFavorItems(null,enterWhat,Item.WORN_REQ_UNWORNONLY);
			if((getThis!=null)&&(getThis instanceof Rideable))
			{
				mount(mob,commands);
				return;
			}
			mob.tell("You don't see '"+enterWhat.toLowerCase()+"' here.");
			return;
		}
		move(mob,dir,false);
	}
	
	public void crawl(MOB mob, Vector commands)
	{
		boolean tagged=false;
		if((commands.size()>2)&&(((String)commands.elementAt(2)).equals(""+mob)))
		{
		   tagged=true;
		   commands.removeElementAt(2);
		}
		int direction=Directions.getGoodDirectionCode(Util.combine(commands,1));
		if(direction>=0)
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_SIT,null);
			if(Sense.isSitting(mob)||(mob.location().okAffect(msg)))
			{
				if(!Sense.isSitting(mob))
					mob.location().send(mob,msg);
				if((mob.isMonster())||(tagged))
					move(mob,direction,false);
				else
				{
					commands.addElement(""+mob);
					mob.session().enque(2,commands);
				}
			}
		}
		else
		{
			mob.tell("Crawl which way?  Try north, south, east, west, up, or down.");
			return;
		}
	}

	public void standAndGo(MOB mob, int directionCode)
	{
		standIfNecessary(mob);
		if(Sense.isSitting(mob))
		{
			mob.tell("You need to stand up first.");
			return;
		}
		move(mob,directionCode,false);
	}

	public boolean move(MOB mob, int directionCode, boolean flee)
	{
		if(directionCode<0) return false;
		if(mob==null) return false;
		Room thisRoom=mob.location();
		if(thisRoom==null) return false;
		Room destRoom=thisRoom.getRoomInDir(directionCode);
		Exit exit=thisRoom.getExitInDir(directionCode);
		if(destRoom==null)
		{
			mob.tell("You can't go that way.");
			return false;
		}

		Exit opExit=thisRoom.getReverseExit(directionCode);
		String directionName=Directions.getDirectionName(directionCode);
		String otherDirectionName=Directions.getFromDirectionName(Directions.getOpDirectionCode(directionCode));

		int leaveCode=Affect.MSG_LEAVE;
		if(flee) 
			leaveCode=Affect.MSG_FLEE;

		FullMsg enterMsg=null;
		FullMsg leaveMsg=null;
		if((mob.riding()!=null)&&(mob.riding().mobileRideBasis()))
		{
			enterMsg=new FullMsg(mob,destRoom,exit,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,"<S-NAME> ride(s) "+mob.riding().name()+" in from "+otherDirectionName);
			leaveMsg=new FullMsg(mob,thisRoom,opExit,leaveCode,((flee)?"You flee "+directionName:null),leaveCode,null,leaveCode,"<S-NAME> "+((flee)?"flee(s) with ":"ride(s) ")+mob.riding().name()+" "+directionName);
		}
		else
		{
			enterMsg=new FullMsg(mob,destRoom,exit,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,"<S-NAME> "+Sense.dispositionString(mob,Sense.flag_arrives)+" from "+otherDirectionName);
			leaveMsg=new FullMsg(mob,thisRoom,opExit,leaveCode,((flee)?"You flee "+directionName:null),leaveCode,null,leaveCode,"<S-NAME> "+((flee)?"flees":Sense.dispositionString(mob,Sense.flag_leaves))+" "+directionName);
		}
		if((exit==null)&&(!mob.isASysOp(destRoom)))
		{
			mob.tell("You can't go that way.");
			return false;
		}
		else
		if(exit==null)
			thisRoom.showHappens(Affect.MSG_OK_VISUAL,"The area to the "+directionName+" shimmers and becomes transparent.");
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

		if(mob.riding()!=null)
		{
			if((!mob.riding().okAffect(enterMsg)))
				return false;
		}
		else
		{
			mob.curState().expendEnergy(mob,mob.maxState(),true);
			if((!flee)&&(!mob.curState().adjMovement(-1,mob.maxState())))
			{
				mob.tell("You are too tired.");
				return false;
			}
		}

		Vector riders=null;
		Rideable riding=mob.riding();
		if((riding!=null)&&(riding.mobileRideBasis()))
		{
			riders=new Vector();
			for(int r=0;r<riding.numRiders();r++)
			{
				MOB rider=riding.fetchRider(r);
				if((rider!=null)&&(rider!=mob))
					riders.addElement(rider);
			}
			if((riding instanceof Item)
			   &&(((Room)leaveMsg.target()).isContent((Item)riding)))
				((Room)enterMsg.target()).bringItemHere((Item)riding);
			else
			if((riding instanceof MOB)
			   &&(((Room)leaveMsg.target()).isInhabitant((MOB)riding)))
			{
				((MOB)riding).tell("You are ridden "+Directions.getDirectionName(directionCode)+".");
				move(((MOB)riding),directionCode,false);
			}
			else
				riders=null;
		}
		
		if(exit!=null) exit.affect(enterMsg);
		mob.location().delInhabitant(mob);
		((Room)leaveMsg.target()).send(mob,leaveMsg);
		
		mob.setLocation((Room)enterMsg.target());
		((Room)enterMsg.target()).addInhabitant(mob);
		((Room)enterMsg.target()).send(mob,enterMsg);

		if(opExit!=null) opExit.affect(leaveMsg);
		
		ExternalPlay.look(mob,null,true);

		if((riders!=null)&&(riders.size()>0))
		{
			for(int r=0;r<riders.size();r++)
			{
				MOB rMOB=(MOB)riders.elementAt(r);
				if((rMOB.location()==thisRoom)||(rMOB.location()==destRoom))
				{
					boolean fallOff=false;
					if(rMOB.location()==thisRoom)
					{
						rMOB.tell("You ride "+riding.name()+" "+Directions.getDirectionName(directionCode)+".");
						if(!move(rMOB,directionCode,flee))
							fallOff=true;
					}
					if(fallOff)
					{
						rMOB.tell("You fall off "+riding.name()+"!");
						rMOB.setRiding(null);
					}
					else
						rMOB.setRiding(riding);
				}
				else
					rMOB.setRiding(null);
			}
		}
		
		if(!flee)
		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB follower=mob.fetchFollower(f);
			if(follower!=null)
			{
				if((follower.amFollowing()==mob)
				&&((follower.location()==thisRoom)||(follower.location()==destRoom)))
				{
					if(follower.location()==thisRoom)
					{
						follower.tell("You follow "+mob.name()+" "+Directions.getDirectionName(directionCode)+".");
						if(!move(follower,directionCode,false))
						{
							//follower.setFollowing(null);
						}
					}
				}
				else
					follower.setFollowing(null);
			}
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
					Exit thisExit=mob.location().getExitInDir(i);
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
		if((direction.equals("NOWHERE"))||((directionCode>=0)&&(move(mob,directionCode,true))))
		{
			mob.makePeace();
			mob.tell("You lose "+lostExperience+" experience points for withdrawing.");
			mob.charStats().getMyClass().loseExperience(mob,lostExperience);
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
			openThis=mob.location().getExitInDir(dirCode);
		if(openThis==null)
			openThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,whatToOpen,Item.WORN_REQ_ANY);

		if((openThis==null)||(!Sense.canBeSeenBy(openThis,mob)))
		{
			mob.tell("You don't see '"+whatToOpen+"' here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,openThis,null,Affect.MSG_OPEN,"<S-NAME> open(s) "+openThis.name()+".");
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
			unlockThis=mob.location().getExitInDir(dirCode);
		if(unlockThis==null)
			unlockThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,whatTounlock,Item.WORN_REQ_ANY);

		if((unlockThis==null)||(!Sense.canBeSeenBy(unlockThis,mob)))
		{
			mob.tell("You don't see '"+whatTounlock+"' here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,unlockThis,null,Affect.MSG_UNLOCK,"<S-NAME> unlock(s) "+unlockThis.name()+".");
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
			closeThis=mob.location().getExitInDir(dirCode);
		if(closeThis==null)
			closeThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,whatToClose,Item.WORN_REQ_ANY);

		if((closeThis==null)||(!Sense.canBeSeenBy(closeThis,mob)))
		{
			mob.tell("You don't see '"+whatToClose+"' here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,closeThis,null,Affect.MSG_CLOSE,"<S-NAME> close(s) "+closeThis.name()+".");
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
			if(room.getExitInDir(d)==exit) return d;
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
			lockThis=mob.location().getExitInDir(dirCode);
		if(lockThis==null)
			lockThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,whatTolock,Item.WORN_REQ_ANY);

		if((lockThis==null)||(!Sense.canBeSeenBy(lockThis,mob)))
		{
			mob.tell("You don't see '"+whatTolock+"' here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,lockThis,null,Affect.MSG_LOCK,"<S-NAME> lock(s) "+lockThis.name()+".");
		if(lockThis instanceof Exit)
			roomOkAndAffectFully(msg,mob.location(),dirCode);
		else
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}

	public void sit(MOB mob, Vector commands)
	{
		if(Sense.isSitting(mob))
		{
			mob.tell("You are already sitting!");
			return;
		}
		if(commands.size()<=1){ sit(mob); return;}
		String possibleRideable=Util.combine(commands,1);
		Environmental E=mob.location().fetchFromRoomFavorItems(null,possibleRideable,Item.WORN_REQ_UNWORNONLY);
		if((E==null)||(!Sense.canBeSeenBy(E,mob)))
		{
			mob.tell("You don't see '"+possibleRideable+"' here.");
			return;
		}
		if(E instanceof MOB)
		{
			mount(mob,commands);
			return;
		}
		String mountStr=null;
		if(E instanceof Rideable)
			mountStr=((Rideable)E).mountString(Affect.TYP_SIT);
		else
			mountStr="sit(s) on";
		FullMsg msg=new FullMsg(mob,E,null,Affect.MSG_SIT,"<S-NAME> "+mountStr+" "+E.name()+".");
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}
	public void sleep(MOB mob, Vector commands)
	{
		if(Sense.isSleeping(mob))
		{
			mob.tell("You are already asleep!");
			return;
		}
		if(commands.size()<=1){ sleep(mob); return;}
		String possibleRideable=Util.combine(commands,1);
		Environmental E=mob.location().fetchFromRoomFavorItems(null,possibleRideable,Item.WORN_REQ_UNWORNONLY);
		if((E==null)||(!Sense.canBeSeenBy(E,mob)))
		{
			mob.tell("You don't see '"+possibleRideable+"' here.");
			return;
		}
		String mountStr=null;
		if(E instanceof Rideable)
			mountStr=((Rideable)E).mountString(Affect.TYP_SLEEP);
		else
			mountStr="sleep(s) on";
		FullMsg msg=new FullMsg(mob,E,null,Affect.MSG_SLEEP,"<S-NAME> "+mountStr+" "+E.name()+".");
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
	
	public void mount(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell(((String)commands.elementAt(0))+" what?");
			return;
		}
		commands.removeElementAt(0);
		Environmental recipient=null;
		Vector possRecipients=new Vector();
		for(int m=0;m<mob.location().numInhabitants();m++)
		{
			MOB M=mob.location().fetchInhabitant(m);
			if((M!=null)&&(M instanceof Rideable))
				possRecipients.addElement(M);
		}
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I!=null)&&(I instanceof Rideable))
				possRecipients.addElement(I);
		}
		recipient=CoffeeUtensils.fetchEnvironmental(possRecipients,Util.combine(commands,0),true);
		if(recipient==null)
			recipient=CoffeeUtensils.fetchEnvironmental(possRecipients,Util.combine(commands,0),false);
		if(recipient==null)
			recipient=mob.location().fetchFromRoomFavorMOBs(null,Util.combine(commands,0),Item.WORN_REQ_UNWORNONLY);
		if((recipient==null)||((recipient!=null)&&(!Sense.canBeSeenBy(recipient,mob))))
		{
			mob.tell("I don't see "+Util.combine(commands,0)+" here.");
			return;
		}
		String mountStr="mount(s)";
		if(recipient instanceof Rideable)
			mountStr=((Rideable)recipient).mountString(Affect.TYP_MOUNT);
		FullMsg msg=new FullMsg(mob,recipient,null,Affect.MSG_MOUNT,"<S-NAME> "+mountStr+" <T-NAMESELF>.");
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}
	public void dismount(MOB mob, Vector commands)
	{
		if(mob.riding()==null)
		{
			mob.tell("But you aren't riding anything?!");
			return;
		}
		FullMsg msg=new FullMsg(mob,mob.riding(),null,Affect.MSG_DISMOUNT,"<S-NAME> "+mob.riding().dismountString()+" <T-NAMESELF>.");
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}
}
