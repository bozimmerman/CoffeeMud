package com.planet_ink.coffee_mud.Commands.base;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;
public class Movement extends Scriptable
{
	private Movement(){}

	public static void go(MOB mob, Vector commands)
	{
		int direction=Directions.getGoodDirectionCode(Util.combine(commands,1));
		String doing=(String)commands.elementAt(0);
		if(direction>=0)
			move(mob,direction,false,false,false);
		else
		{
			boolean doneAnything=false;
			if(commands.size()>2)
				for(int v=1;v<commands.size();v++)
				{
					int num=1;
					String s=(String)commands.elementAt(v);
					if(Util.s_int(s)>0)
					{
						num=Util.s_int(s);
						v++;
						s=(String)commands.elementAt(v);
					}
					direction=Directions.getGoodDirectionCode(s);
					if(direction>=0)
					{
						doneAnything=true;
						for(int i=0;i<num;i++)
						{
							if(mob.isMonster())
							{
								if(!move(mob,direction,false,false,false))
									return;
							}
							else
							{
								Vector V=new Vector();
								V.addElement(Directions.getDirectionName(direction));
								mob.session().enque(0,V);
							}
						}
					}
					else
						break;
				}
			if(!doneAnything)
				mob.tell(Util.capitalize(doing)+" "+getScr("Movement","goerr"));
		}
	}

	public static void enter(MOB mob, Vector commands)
	{
		if(commands.size()<=1)
		{
			mob.tell(getScr("Movement","entererr1"));
			return;
		}
		String enterWhat=Util.combine(commands,1).toUpperCase();
		int dir=-1;
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Exit e=mob.location().getExitInDir(d);
			Room r=mob.location().getRoomInDir(d);
			if((e!=null)&&(r!=null))
			{
				if((Sense.canBeSeenBy(e,mob))
				&&((e.name().equalsIgnoreCase(enterWhat))
				||(e.displayText().equalsIgnoreCase(enterWhat))
				||(r.displayText().equalsIgnoreCase(enterWhat))
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
			Room r=mob.location().getRoomInDir(d);
			if((e!=null)&&(r!=null))
			{
				if((Sense.canBeSeenBy(e,mob))
				&&(((CoffeeUtensils.containsString(e.name().toUpperCase(),enterWhat))
				||(CoffeeUtensils.containsString(e.displayText().toUpperCase(),enterWhat))
				||(CoffeeUtensils.containsString(r.displayText().toUpperCase(),enterWhat))
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
			mob.tell(getScr("Movement","youdontsee",enterWhat.toLowerCase()));
			return;
		}
		move(mob,dir,false,false,false);
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
			if(Sense.isSitting(mob)||(mob.location().okAffect(mob,msg)))
			{
				if(!Sense.isSitting(mob))
					mob.location().send(mob,msg);
				if((mob.isMonster())||(tagged))
					move(mob,direction,false,false,false);
				else
				{
					commands.addElement(""+mob);
					mob.session().enque(2,commands);
				}
			}
		}
		else
		{
			mob.tell(getScr("Movement","crawlerr1"));
			return;
		}
	}

	public static void standAndGo(MOB mob, int directionCode)
	{
		standIfNecessary(mob);
		if(Sense.isSitting(mob))
		{
			mob.tell(getScr("Movement","standandgoerr1"));
			return;
		}
		move(mob,directionCode,false,false,false);
	}

	public static void ridersBehind(Vector riders,
									Room sourceRoom,
									Room destRoom,
									int directionCode,
									boolean flee)
	{
		if(riders!=null)
		for(int r=0;r<riders.size();r++)
		{
			Rider rider=(Rider)riders.elementAt(r);
			if(rider instanceof MOB)
			{
				MOB rMOB=(MOB)rider;

				if((rMOB.location()==sourceRoom)
				   ||(rMOB.location()==destRoom))
				{
					boolean fallOff=false;
					if(rMOB.location()==sourceRoom)
					{
						if(rMOB.riding()!=null)
							rMOB.tell(getScr("Movement","youride",rMOB.riding().name(),Directions.getDirectionName(directionCode)));
						if(!move(rMOB,directionCode,flee,false,true))
							fallOff=true;
					}
					if(fallOff)
					{
						if(rMOB.riding()!=null)
							rMOB.tell(getScr("Movement","youfalloff",rMOB.riding().name()));
						rMOB.setRiding(null);
					}
				}
				else
					rMOB.setRiding(null);
			}
			else
			if(rider instanceof Item)
			{
				Item rItem=(Item)rider;
				if((rItem.owner()==sourceRoom)
				||(rItem.owner()==destRoom))
					destRoom.bringItemHere(rItem);
				else
					rItem.setRiding(null);
			}
		}
	}

	public static Vector addRiders(Rider theRider,
								   Rideable riding,
								   Vector riders)
	{

		if((riding!=null)&&(riding.mobileRideBasis()))
			for(int r=0;r<riding.numRiders();r++)
			{
				Rider rider=riding.fetchRider(r);
				if((rider!=null)
				&&(rider!=theRider)
				&&(!riders.contains(rider)))
				{
					riders.addElement(rider);
					if(rider instanceof Rideable)
						addRiders(theRider,(Rideable)rider,riders);
				}
			}
		return riders;
	}

	public static Vector ridersAhead(Rider theRider,
									 Room sourceRoom,
									 Room destRoom,
									 int directionCode,
									 boolean flee)
	{
		Vector riders=new Vector();
		Rideable riding=theRider.riding();
		Vector rideables=new Vector();
		while((riding!=null)&&(riding.mobileRideBasis()))
		{
			rideables.addElement(riding);
			addRiders(theRider,riding,riders);
			if((riding instanceof Rider)&&((Rider)riding).riding()!=theRider.riding())
				riding=((Rider)riding).riding();
			else
				riding=null;
		}
		if(theRider instanceof Rideable)
			addRiders(theRider,(Rideable)theRider,riders);
		for(int r=riders.size()-1;r>=0;r--)
		{
			Rider R=(Rider)riders.elementAt(r);
			if((R instanceof Rideable)&&(((Rideable)R).numRiders()>0))
			{
				if(!rideables.contains(R))
					rideables.addElement(R);
				riders.removeElement(R);
			}
		}
		for(int r=0;r<rideables.size();r++)
		{
			riding=(Rideable)rideables.elementAt(r);
			if((riding instanceof Item)
			&&((sourceRoom).isContent((Item)riding)))
				destRoom.bringItemHere((Item)riding);
			else
			if((riding instanceof MOB)
			&&((sourceRoom).isInhabitant((MOB)riding)))
			{
				((MOB)riding).tell(getScr("Movement","youridden",Directions.getDirectionName(directionCode)));
				if(!move(((MOB)riding),directionCode,false,false,true))
				{
					if(theRider instanceof MOB)
						((MOB)theRider).tell(getScr("Movement","rideerr1",((MOB)riding).name()));
					r=r-1;
					for(;r>=0;r--)
					{
						riding=(Rideable)rideables.elementAt(r);
						if((riding instanceof Item)
						&&((destRoom).isContent((Item)riding)))
							sourceRoom.bringItemHere((Item)riding);
						else
						if((riding instanceof MOB)
						&&(((MOB)riding).isMonster())
						&&((destRoom).isInhabitant((MOB)riding)))
							sourceRoom.bringMobHere((MOB)riding,true);
					}
					return null;
				}
			}
		}
		return riders;
	}

	public static boolean move(MOB mob,
							   int directionCode,
							   boolean flee,
							   boolean nolook,
							   boolean noriders)
	{
		if(directionCode<0) return false;
		if(mob==null) return false;
		Room thisRoom=mob.location();
		if(thisRoom==null) return false;
		Room destRoom=thisRoom.getRoomInDir(directionCode);
		Exit exit=thisRoom.getExitInDir(directionCode);
		if(destRoom==null)
		{
			mob.tell(getScr("Movement","moveerr1"));
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
			enterMsg=new FullMsg(mob,destRoom,exit,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,getScr("Movement","sridesin",mob.riding().name(),otherDirectionName));
			leaveMsg=new FullMsg(mob,thisRoom,opExit,leaveCode,((flee)?getScr("Movement","youflee",directionName):null),leaveCode,null,leaveCode,((flee)?getScr("Movement","sfleeswith",mob.riding().name(),directionName):getScr("Movement","srides",mob.riding().name(),directionName)));
		}
		else
		{
			enterMsg=new FullMsg(mob,destRoom,exit,Affect.MSG_ENTER,null,Affect.MSG_ENTER,null,Affect.MSG_ENTER,getScr("Movement","senter",Sense.dispositionString(mob,Sense.flag_arrives),otherDirectionName));
			leaveMsg=new FullMsg(mob,thisRoom,opExit,leaveCode,((flee)?getScr("Movement","youflee",directionName):null),leaveCode,null,leaveCode,((flee)?getScr("Movement","sflees",directionName):getScr("Movement","sleaves",Sense.dispositionString(mob,Sense.flag_leaves),directionName)));
		}
		if((exit==null)&&(!mob.isASysOp(destRoom)))
		{
			mob.tell(getScr("Movement","moveerr1"));
			return false;
		}
		else
		if(exit==null)
			thisRoom.showHappens(Affect.MSG_OK_VISUAL,getScr("Movement","stwitch",directionName));
		else
		if((exit!=null)&&(!exit.okAffect(mob,enterMsg)))
			return false;
		else
		if(!leaveMsg.target().okAffect(mob,leaveMsg))
			return false;
		else
		if((opExit!=null)&&(!opExit.okAffect(mob,leaveMsg)))
			return false;
		else
		if(!enterMsg.target().okAffect(mob,enterMsg))
			return false;
		else
		if(!mob.okAffect(mob,enterMsg))
			return false;

		if(mob.riding()!=null)
		{
			if((!mob.riding().okAffect(mob,enterMsg)))
				return false;
		}
		else
		{
			mob.curState().expendEnergy(mob,mob.maxState(),true);
			if((!flee)&&(!mob.curState().adjMovement(-1,mob.maxState())))
			{
				mob.tell(getScr("Movement","tootired"));
				return false;
			}
		}

		Vector riders=null;
		if(!noriders)
		{
			riders=ridersAhead(mob,(Room)leaveMsg.target(),(Room)enterMsg.target(),directionCode,flee);
			if(riders==null) return false;
		}

		if(exit!=null) exit.affect(mob,enterMsg);
		mob.location().delInhabitant(mob);
		((Room)leaveMsg.target()).send(mob,leaveMsg);

		mob.setLocation((Room)enterMsg.target());
		((Room)enterMsg.target()).addInhabitant(mob);
		((Room)enterMsg.target()).send(mob,enterMsg);

		if(opExit!=null) opExit.affect(mob,leaveMsg);

		if(!nolook)
			ExternalPlay.look(mob,null,true);

		if(!noriders)
			ridersBehind(riders,(Room)leaveMsg.target(),(Room)enterMsg.target(),directionCode,flee);

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
					   &&(!Util.bset(follower.getBitmap(),MOB.ATT_AUTOGUARD)))
					{
						follower.tell(getScr("Movement","youfollow",mob.name(),Directions.getDirectionName(directionCode)));
						if(!move(follower,directionCode,false,false,false))
						{
							//follower.setFollowing(null);
						}
					}
				}
				//else
				//	follower.setFollowing(null);
			}
		}
		return true;
	}

	public static void flee(MOB mob, String direction)
	{
		if((mob.location()==null)||(!mob.isInCombat()))
		{
			mob.tell(getScr("Movement","fleeerr1"));
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
				mob.tell(getScr("Movement","fleeerr2"));
				return;
			}
		}
		int lostExperience=10;
		if(mob.getVictim()!=null)
		{
			String whatToDo=CommonStrings.getVar(CommonStrings.SYSTEM_PLAYERFLEE);
			if(whatToDo.startsWith("UNL"))
			{
				Vector V=Util.parse(whatToDo);
				int times=1;
				if((V.size()>1)&&(Util.s_int((String)V.lastElement())>1))
					times=Util.s_int((String)V.lastElement());
				for(int t=0;t<times;t++)
					mob.charStats().getCurrentClass().unLevel(mob);
			}
			else
			if(whatToDo.startsWith("PUR"))
			{
				MOB deadMOB=(MOB)CMClass.getMOB("StdMOB");
				boolean found=ExternalPlay.DBUserSearch(deadMOB,mob.Name());
				if(found)
				{
					ExternalPlay.destroyUser(deadMOB);
					return;
				}
			}
			else
			if((whatToDo.trim().equals("0"))||(Util.s_int(whatToDo)>0))
				lostExperience=Util.s_int(whatToDo);
			else
			{
				lostExperience=10+((mob.envStats().level()-mob.getVictim().envStats().level()))*5;
				if(lostExperience<10) lostExperience=10;
			}
		}
		if((direction.equals("NOWHERE"))||((directionCode>=0)&&(move(mob,directionCode,true,false,false))))
		{
			mob.makePeace();
			if(lostExperience>0)
			{
				mob.tell(getScr("Movement","fleeexp",""+lostExperience));
				mob.charStats().getCurrentClass().loseExperience(mob,lostExperience);
			}
		}
	}

	public static void open(MOB mob, String whatToOpen)
	{
		if(whatToOpen.length()==0)
		{
			mob.tell(getScr("Movement","openerr1"));
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
			mob.tell(getScr("Movement","youdontsee",whatToOpen));
			return;
		}
		FullMsg msg=new FullMsg(mob,openThis,null,Affect.MSG_OPEN,(getScr("Movement","sopens"))+CommonStrings.msp("dooropen.wav",10));
		if(openThis instanceof Exit)
			roomOkAndAffectFully(msg,mob.location(),dirCode);
		else
		if(mob.location().okAffect(mob,msg))
			mob.location().send(mob,msg);
	}

	public static void unlock(MOB mob, String whatTounlock)
	{
		if(whatTounlock.length()==0)
		{
			mob.tell(getScr("Movement","unlockerr1"));
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
			mob.tell(getScr("Movement","youdontsee",whatTounlock));
			return;
		}
		FullMsg msg=new FullMsg(mob,unlockThis,null,Affect.MSG_UNLOCK,getScr("Movement","sunlocks")+CommonStrings.msp("doorunlock.wav",10));
		if(unlockThis instanceof Exit)
			roomOkAndAffectFully(msg,mob.location(),dirCode);
		else
		if(mob.location().okAffect(mob,msg))
			mob.location().send(mob,msg);
	}

	public static void close(MOB mob, String whatToClose)
	{
		if(whatToClose.length()==0)
		{
			mob.tell(getScr("Movement","closeerr1"));
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
			mob.tell(getScr("Movement","youdontsee",whatToClose));
			return;
		}
		FullMsg msg=new FullMsg(mob,closeThis,null,Affect.MSG_CLOSE,getScr("Movement","scloses")+CommonStrings.msp("dooropen.wav",10));
		if(closeThis instanceof Exit)
			roomOkAndAffectFully(msg,mob.location(),dirCode);
		else
		if(mob.location().okAffect(mob,msg))
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
			pair.affect(msg.source(),altMsg);
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
			return room.okAffect(msg.source(),msg);

		Exit thisExit=(Exit)msg.target();
		if(!room.okAffect(msg.source(),msg))
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
			mob.tell(getScr("Movement","lockerr1"));
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
			mob.tell(getScr("Movement","youdontsee",whatTolock));
			return;
		}
		FullMsg msg=new FullMsg(mob,lockThis,null,Affect.MSG_LOCK,getScr("Movement","slocks")+CommonStrings.msp("doorunlock.wav",10));
		if(lockThis instanceof Exit)
			roomOkAndAffectFully(msg,mob.location(),dirCode);
		else
		if(mob.location().okAffect(mob,msg))
			mob.location().send(mob,msg);
	}

	public static void sit(MOB mob, Vector commands)
	{
		if(Sense.isSitting(mob))
		{
			mob.tell(getScr("Movement","siterr1"));
			return;
		}
		if(commands.size()<=1){ sit(mob); return;}
		String possibleRideable=Util.combine(commands,1);
		Environmental E=mob.location().fetchFromRoomFavorItems(null,possibleRideable,Item.WORN_REQ_UNWORNONLY);
		if((E==null)||(!Sense.canBeSeenBy(E,mob)))
		{
			mob.tell(getScr("Movement","youdontsee",possibleRideable));
			return;
		}
		if(E instanceof MOB)
		{
			mount(mob,commands);
			return;
		}
		String mountStr=null;
		if(E instanceof Rideable)
			mountStr=getScr("Movement","sitmounton",((Rideable)E).mountString(Affect.TYP_SIT,mob));
		else
			mountStr=getScr("Movement","sitson");
		FullMsg msg=new FullMsg(mob,E,null,Affect.MSG_SIT,mountStr);
		if(mob.location().okAffect(mob,msg))
			mob.location().send(mob,msg);
	}
	public static void sleep(MOB mob, Vector commands)
	{
		if(Sense.isSleeping(mob))
		{
			mob.tell(getScr("Movement","sleeperr1"));
			return;
		}
		if(commands.size()<=1){ sleep(mob); return;}
		String possibleRideable=Util.combine(commands,1);
		Environmental E=mob.location().fetchFromRoomFavorItems(null,possibleRideable,Item.WORN_REQ_UNWORNONLY);
		if((E==null)||(!Sense.canBeSeenBy(E,mob)))
		{
			mob.tell(getScr("Movement","youdontsee",possibleRideable));
			return;
		}
		String mountStr=null;
		if(E instanceof Rideable)
			mountStr=getScr("Movement","sleepmounton",((Rideable)E).mountString(Affect.TYP_SLEEP,mob));
		else
			mountStr=getScr("Movement","sleepson");
		FullMsg msg=new FullMsg(mob,E,null,Affect.MSG_SLEEP,mountStr);
		if(mob.location().okAffect(mob,msg))
			mob.location().send(mob,msg);
	}

	public static void sit(MOB mob)
	{
		if(Sense.isSitting(mob))
			mob.tell(getScr("Movement","siterr1"));
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_SIT,getScr("Movement","sitdown"));
			if(mob.location().okAffect(mob,msg))
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
				mob.tell(getScr("Movement","wakeerr1"));
			else
			{
				FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_STAND,getScr("Movement","wakeup"));
				if(mob.location().okAffect(mob,msg))
					mob.location().send(mob,msg);
			}
		}
		else
		{
			String whom=Util.combine(commands,0);
			MOB M=mob.location().fetchInhabitant(whom);
			if((M==null)||(!Sense.canBeSeenBy(M,mob)))
			{
				mob.tell(getScr("Movement","youdontsee",whom));
				return;
			}
			if(!Sense.isSleeping(M))
			{
				mob.tell(getScr("Movement","wakeerr2",M.name()));
				return;
			}
			FullMsg msg=new FullMsg(mob,M,null,Affect.MSG_NOISYMOVEMENT,getScr("Movement","wakeother"));
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				wake(M,null);
			}
		}
	}
	public static void sleep(MOB mob)
	{
		if(Sense.isSleeping(mob))
			mob.tell(getScr("Movement","sleeperr1"));
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_SLEEP,getScr("Movement","sleep"));
			if(mob.location().okAffect(mob,msg))
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
			mob.tell(getScr("Movement","standerr1"));
		else
		{
			FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_STAND,getScr("Movement","standup"));
			if(mob.location().okAffect(mob,msg))
				mob.location().send(mob,msg);
		}
	}

	public static void mount(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell(getScr("Movement","mounterr1",((String)commands.elementAt(0))));
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
		Rider RI=null;
		if(commands.size()>1)
		{
			Item I=mob.location().fetchItem(null,(String)commands.firstElement());
			if(I!=null)
			{
				commands.removeElementAt(0);
				I.setRiding(null);
				RI=I;
			}
		}
		recipient=CoffeeUtensils.fetchEnvironmental(possRecipients,Util.combine(commands,0),true);
		if(recipient==null)
			recipient=CoffeeUtensils.fetchEnvironmental(possRecipients,Util.combine(commands,0),false);
		if(recipient==null)
			recipient=mob.location().fetchFromRoomFavorMOBs(null,Util.combine(commands,0),Item.WORN_REQ_UNWORNONLY);
		if((recipient==null)||((recipient!=null)&&(!Sense.canBeSeenBy(recipient,mob))))
		{
			mob.tell(getScr("Movement","youdontsee",Util.combine(commands,0)));
			return;
		}
		String mountStr=null;
		if(recipient instanceof Rideable)
		{
			if(RI!=null)
				mountStr=getScr("Movement","mountonto");
			else
				mountStr=getScr("Movement","mounton",((Rideable)recipient).mountString(Affect.TYP_MOUNT,mob));
		}
		else
		{
			if(RI!=null)
				mountStr=getScr("Movement","mountsto");
			else
				mountStr=getScr("Movement","mounts");
		}
		FullMsg msg=new FullMsg(mob,recipient,RI,Affect.MSG_MOUNT,mountStr);
		if(mob.location().okAffect(mob,msg))
			mob.location().send(mob,msg);
	}

	public static void dismount(MOB mob, Vector commands)
	{
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
			if(mob.riding()==null)
			{
				mob.tell(getScr("Movement","dismounterr1"));
				return;
			}
			FullMsg msg=new FullMsg(mob,mob.riding(),null,Affect.MSG_DISMOUNT,getScr("Movement","dismounts",mob.riding().dismountString(mob)));
			if(mob.location().okAffect(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			Item RI=mob.location().fetchItem(null,Util.combine(commands,0));
			if(RI==null)
			{
				mob.tell(getScr("Movement","dismounterr2",Util.combine(commands,0)));
				return;
			}
			if((RI.riding()==null)
			   ||((RI.riding() instanceof MOB)&&(!mob.location().isInhabitant((MOB)RI.riding())))
			   ||((RI.riding() instanceof Item)&&(!mob.location().isContent((Item)RI.riding())))
			   ||(!Sense.canBeSeenBy(RI.riding(),mob)))
			{
				mob.tell(getScr("Movement","dismounterr3",RI.name()));
				return;
			}
			FullMsg msg=new FullMsg(mob,RI.riding(),RI,Affect.MSG_DISMOUNT,getScr("Movement","dismounts2"));
			if(mob.location().okAffect(mob,msg))
				mob.location().send(mob,msg);
		}
	}
}
