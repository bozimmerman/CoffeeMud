package com.planet_ink.coffee_mud.Commands.base;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;
public class Movement
{
	private Movement(){}
	
	public static void go(MOB mob, Vector commands)
	{
		int direction=Directions.getGoodDirectionCode(Util.combine(commands,1));
		if(direction>=0)
			move(mob,direction,false,false);
		else
		{
			String doing=(String)commands.elementAt(0);
			mob.tell(Character.toUpperCase(doing.charAt(0))+doing.substring(1)+" "+Scripts.get("Movement-goerr"));
			return;
		}
	}

	public static void enter(MOB mob, Vector commands)
	{
		if(commands.size()<=1)
		{
			mob.tell(Scripts.get("Movement-entererr1"));
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
				sit(mob,commands);
				return;
			}
			mob.tell(Scripts.get("All-youdontsee",enterWhat.toLowerCase()));
			return;
		}
		move(mob,dir,false,false);
	}
	
	public static void crawl(MOB mob, Vector commands)
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
					move(mob,direction,false,false);
				else
				{
					commands.addElement(""+mob);
					mob.session().enque(2,commands);
				}
			}
		}
		else
		{
			mob.tell(Scripts.get("Movement-crawlerr1"));
			return;
		}
	}

	public static void standAndGo(MOB mob, int directionCode)
	{
		standIfNecessary(mob);
		if(Sense.isSitting(mob))
		{
			mob.tell(Scripts.get("Movement-standandgoerr1"));
			return;
		}
		move(mob,directionCode,false,false);
	}

	public static boolean move(MOB mob, int directionCode, boolean flee, boolean nolook)
	{
		if(directionCode<0) return false;
		if(mob==null) return false;
		Room thisRoom=mob.location();
		if(thisRoom==null) return false;
		Room destRoom=thisRoom.getRoomInDir(directionCode);
		Exit exit=thisRoom.getExitInDir(directionCode);
		if(destRoom==null)
		{
			mob.tell(Scripts.get("Movement-moveerr1"));
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
			enterMsg=new FullMsg(mob,destRoom,exit,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,Scripts.get("Movement-sridesin",mob.riding().name(),otherDirectionName));
			leaveMsg=new FullMsg(mob,thisRoom,opExit,leaveCode,((flee)?Scripts.get("Movement-youflee",directionName):null),leaveCode,null,leaveCode,((flee)?Scripts.get("Movement-sfleeswith",mob.riding().name(),directionName):Scripts.get("Movement-srides",mob.riding().name(),directionName)));
		}
		else
		{
			enterMsg=new FullMsg(mob,destRoom,exit,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,Scripts.get("Movement-senter",Sense.dispositionString(mob,Sense.flag_arrives),otherDirectionName));
			leaveMsg=new FullMsg(mob,thisRoom,opExit,leaveCode,((flee)?Scripts.get("Movement-youflee",directionName):null),leaveCode,null,leaveCode,((flee)?Scripts.get("Movement-sflees",directionName):(Sense.dispositionString(mob,Sense.flag_leaves))+" "+directionName));
		}
		if((exit==null)&&(!mob.isASysOp(destRoom)))
		{
			mob.tell(Scripts.get("Movement-moveerr1"));
			return false;
		}
		else
		if(exit==null)
			thisRoom.showHappens(Affect.MSG_OK_VISUAL,Scripts.get("Movement-stwitch",directionName));
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
				mob.tell(Scripts.get("Movement-tootired"));
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
				((MOB)riding).tell(Scripts.get("Movement-youridden",Directions.getDirectionName(directionCode)));
				move(((MOB)riding),directionCode,false,false);
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
		
		if(!nolook)
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
						rMOB.tell(Scripts.get("Movement-youride",riding.name(),Directions.getDirectionName(directionCode)));
						if(!move(rMOB,directionCode,flee,false))
							fallOff=true;
					}
					if(fallOff)
					{
						rMOB.tell(Scripts.get("Movement-youfalloff",riding.name()));
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
					if((follower.location()==thisRoom)
					   &&((follower.getBitmap()&MOB.ATT_AUTOGUARD)==0))
					{
						follower.tell(Scripts.get("Movement-youfollow",mob.name(),Directions.getDirectionName(directionCode)));
						if(!move(follower,directionCode,false,false))
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
	
	public static void flee(MOB mob, String direction)
	{
		if((mob.location()==null)||(!mob.isInCombat()))
		{
			mob.tell(Scripts.get("Movement-fleeerr1"));
			return;
		}
		
		int directionCode=-1;
		if(!direction.equals("NOWHERE"))
		{
			if(direction.length()==0)
			{
				Vector directions=new Vector();
				for(int i=0;i<Directions.NUM_DIRECTIONS;i++)
				{
					Exit thisExit=mob.location().getExitInDir(i);
					Room thisRoom=mob.location().getRoomInDir(i);
					if((thisRoom!=null)&&(thisExit!=null)&&(thisExit.isOpen()))
						directions.addElement(new Integer(i));
				}
				// up is last resort
				if(directions.size()>1)
					directions.removeElement(new Integer(Directions.UP));
				if(directions.size()>0)
				{
					directionCode=((Integer)directions.elementAt(Dice.roll(1,directions.size(),-1))).intValue();
					direction=Directions.getDirectionName(directionCode);
				}
			}
			else
				directionCode=Directions.getGoodDirectionCode(direction);
			if(directionCode<0)
			{
				mob.tell(Scripts.get("Movement-fleeerr2"));
				return;
			}
		}
		int lostExperience=10;
		if(mob.getVictim()!=null)
		{
			lostExperience=10+((mob.envStats().level()-mob.getVictim().envStats().level()))*5;
			if(lostExperience<10) lostExperience=10;
		}
		if((direction.equals("NOWHERE"))||((directionCode>=0)&&(move(mob,directionCode,true,false))))
		{
			mob.makePeace();
			mob.tell(Scripts.get("Movement-fleeexp",""+lostExperience));
			mob.charStats().getCurrentClass().loseExperience(mob,lostExperience);
		}
	}

	public static void open(MOB mob, String whatToOpen)
	{
		if(whatToOpen.length()==0)
		{
			mob.tell(Scripts.get("Movement-openerr1"));
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
			mob.tell(Scripts.get("All-youdontsee",whatToOpen));
			return;
		}
		FullMsg msg=new FullMsg(mob,openThis,null,Affect.MSG_OPEN,Scripts.get("Movement-sopens",openThis.name()));
		if(openThis instanceof Exit)
			roomOkAndAffectFully(msg,mob.location(),dirCode);
		else
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}

	public static void unlock(MOB mob, String whatTounlock)
	{
		if(whatTounlock.length()==0)
		{
			mob.tell(Scripts.get("Movement-unlockerr1"));
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
			mob.tell(Scripts.get("All-youdontsee",whatTounlock));
			return;
		}
		FullMsg msg=new FullMsg(mob,unlockThis,null,Affect.MSG_UNLOCK,Scripts.get("Movement-sopens",unlockThis.name()));
		if(unlockThis instanceof Exit)
			roomOkAndAffectFully(msg,mob.location(),dirCode);
		else
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}

	public static void close(MOB mob, String whatToClose)
	{
		if(whatToClose.length()==0)
		{
			mob.tell(Scripts.get("Movement-closeerr1"));
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
			mob.tell(Scripts.get("All-youdontsee",whatToClose));
			return;
		}
		FullMsg msg=new FullMsg(mob,closeThis,null,Affect.MSG_CLOSE,Scripts.get("Movement-scloses",closeThis.name()));
		if(closeThis instanceof Exit)
			roomOkAndAffectFully(msg,mob.location(),dirCode);
		else
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}

	public static void roomAffectFully(Affect msg, Room room, int dirCode)
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

	public static int getMyDirCode(Exit exit, Room room, int testCode)
	{
		if(testCode>=0) return testCode;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			if(room.getExitInDir(d)==exit) return d;
		return -1;
	}

	public static boolean roomOkAndAffectFully(FullMsg msg, Room room, int dirCode)
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

	public static void lock(MOB mob, String whatTolock)
	{
		if(whatTolock.length()==0)
		{
			mob.tell(Scripts.get("Movement-lockerr1"));
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
			mob.tell(Scripts.get("All-youdontsee",whatTolock));
			return;
		}
		FullMsg msg=new FullMsg(mob,lockThis,null,Affect.MSG_LOCK,Scripts.get("Movement-slocks",lockThis.name()));
		if(lockThis instanceof Exit)
			roomOkAndAffectFully(msg,mob.location(),dirCode);
		else
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}

	public static void sit(MOB mob, Vector commands)
	{
		if(Sense.isSitting(mob))
		{
			mob.tell(Scripts.get("Movement-siterr1"));
			return;
		}
		if(commands.size()<=1){ sit(mob); return;}
		String possibleRideable=Util.combine(commands,1);
		Environmental E=mob.location().fetchFromRoomFavorItems(null,possibleRideable,Item.WORN_REQ_UNWORNONLY);
		if((E==null)||(!Sense.canBeSeenBy(E,mob)))
		{
			mob.tell(Scripts.get("All-youdontsee",possibleRideable));
			return;
		}
		if(E instanceof MOB)
		{
			mount(mob,commands);
			return;
		}
		String mountStr=null;
		if(E instanceof Rideable)
			mountStr=Scripts.get("Movement-sitmounton",((Rideable)E).mountString(Affect.TYP_SIT),E.name());
		else
			mountStr=Scripts.get("Movement-sitson",E.name());
		FullMsg msg=new FullMsg(mob,E,null,Affect.MSG_SIT,mountStr);
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}
	public static void sleep(MOB mob, Vector commands)
	{
		if(Sense.isSleeping(mob))
		{
			mob.tell(Scripts.get("Movement-sleeperr1"));
			return;
		}
		if(commands.size()<=1){ sleep(mob); return;}
		String possibleRideable=Util.combine(commands,1);
		Environmental E=mob.location().fetchFromRoomFavorItems(null,possibleRideable,Item.WORN_REQ_UNWORNONLY);
		if((E==null)||(!Sense.canBeSeenBy(E,mob)))
		{
			mob.tell(Scripts.get("All-youdontsee",possibleRideable));
			return;
		}
		String mountStr=null;
		if(E instanceof Rideable)
			mountStr=Scripts.get("Movement-sleepmounton",((Rideable)E).mountString(Affect.TYP_SLEEP),E.name());
		else
			mountStr=Scripts.get("Movement-sleepson",E.name());
		FullMsg msg=new FullMsg(mob,E,null,Affect.MSG_SLEEP,mountStr);
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}
	
	public static void sit(MOB mob)
	{
		if(Sense.isSitting(mob))
			mob.tell(Scripts.get("Movement-siterr1"));
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_SIT,Scripts.get("Movement-sitdown"));
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
	}
	public static void wake(MOB mob, Vector commands)
	{
		if(commands!=null)
			commands.removeElementAt(0);
		if((commands==null)||(commands.size()==0))
		{
			if(!Sense.isSleeping(mob))
				mob.tell(Scripts.get("Movement-wakeerr1"));
			else
			{
				FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_STAND,Scripts.get("Movement-wakeup"));
				if(mob.location().okAffect(msg))
					mob.location().send(mob,msg);
			}
		}
		else
		{
			String whom=Util.combine(commands,0);
			MOB M=mob.location().fetchInhabitant(whom);
			if((M==null)||(!Sense.canBeSeenBy(M,mob)))
			{
				mob.tell(Scripts.get("All-youdontsee",whom));
				return;
			}
			if(!Sense.isSleeping(M))
			{
				mob.tell(Scripts.get("Movement-wakeerr2",M.name()));
				return;
			}
			FullMsg msg=new FullMsg(mob,M,null,Affect.MSG_NOISYMOVEMENT,Scripts.get("Movement-wakeother"));
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				wake(M,null);
			}
		}
	}
	public static void sleep(MOB mob)
	{
		if(Sense.isSleeping(mob))
			mob.tell(Scripts.get("Movement-sleeperr1"));
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_SLEEP,Scripts.get("Movement-sleep"));
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
	}

	public static void standIfNecessary(MOB mob)
	{
		if((!mob.amDead())&&(Sense.isSleeping(mob)||Sense.isSitting(mob)))
			stand(mob);
	}

	public static void stand(MOB mob)
	{
		if((!Sense.isSitting(mob))&&(!Sense.isSleeping(mob)))
			mob.tell(Scripts.get("Movement-standerr1"));
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_STAND,Scripts.get("Movement-standup"));
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
	}
	
	public static void mount(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell(Scripts.get("Movement-mounterr1",((String)commands.elementAt(0))));
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
			mob.tell(Scripts.get("All-youdontsee",Util.combine(commands,0)));
			return;
		}
		String mountStr=null;
		if(recipient instanceof Rideable)
			mountStr=Scripts.get("Movement-mounton",((Rideable)recipient).mountString(Affect.TYP_MOUNT));
		else
			mountStr=Scripts.get("Movement-mounts");
		FullMsg msg=new FullMsg(mob,recipient,null,Affect.MSG_MOUNT,mountStr);
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}
	public static void dismount(MOB mob, Vector commands)
	{
		if(mob.riding()==null)
		{
			mob.tell(Scripts.get("Movement-dismounterr1"));
			return;
		}
		FullMsg msg=new FullMsg(mob,mob.riding(),null,Affect.MSG_DISMOUNT,Scripts.get("Movement-dismounts",mob.riding().dismountString()));
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}
}
