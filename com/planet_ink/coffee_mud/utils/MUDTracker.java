package com.planet_ink.coffee_mud.utils;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class MUDTracker extends Scriptable
{
	private MUDTracker(){};

	protected final static int TRACK_ATTEMPTS=25;
	protected final static int TRACK_DEPTH=500;


	public static boolean findTheRoom(Room location,
									  Room destRoom,
									  int tryCode,
									  Vector dirVec,
									  Vector theTrail,
									  Hashtable lookedIn,
									  int depth,
									  boolean noWater)
	{
		if(lookedIn==null) return false;
		if(lookedIn.get(location)!=null) return false;
		if(depth>TRACK_DEPTH) return false;

		lookedIn.put(location,location);
		for(int x=0;x<dirVec.size();x++)
		{
			int i=((Integer)dirVec.elementAt(x)).intValue();
			Room nextRoom=location.getRoomInDir(i);
			Exit nextExit=location.getExitInDir(i);
			if((nextRoom!=null)
			&&(nextExit!=null)
			&&((!noWater)||(
			  (nextRoom.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
			&&(nextRoom.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
			&&(nextRoom.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
			&&(nextRoom.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE))))
			{
				if((nextRoom==destRoom)
				||(findTheRoom(nextRoom,destRoom,tryCode,dirVec,theTrail,lookedIn,depth+1,noWater)))
				{
					theTrail.addElement(nextRoom);
					return true;
				}
			}
		}
		return false;
	}

	public static Vector findBastardTheBestWay(Room location,
											   Vector destRooms,
											   boolean noWater)
	{

		Vector trailArray[] = new Vector[TRACK_ATTEMPTS];
		Room trackArray[] = new Room[TRACK_ATTEMPTS];

		for(int t=0;t<TRACK_ATTEMPTS;t++)
		{
			Vector dirVec=new Vector();
			while(dirVec.size()<Directions.NUM_DIRECTIONS)
			{
				int direction=Dice.roll(1,Directions.NUM_DIRECTIONS,-1);
				for(int x=0;x<dirVec.size();x++)
					if(((Integer)dirVec.elementAt(x)).intValue()==direction)
						continue;
				dirVec.addElement(new Integer(direction));
			}
			Room roomToTry=(Room)destRooms.elementAt(Dice.roll(1,destRooms.size(),-1));
			Hashtable lookedIn=new Hashtable();
			Vector theTrail=new Vector();
			if(findTheRoom(location,roomToTry,2,dirVec,theTrail,lookedIn,0,noWater))
			{
				trailArray[t]=theTrail;
				trackArray[t]=roomToTry;
			}
		}
		int winner=-1;
		int winningTotal=Integer.MAX_VALUE;
		for(int t=0;t<TRACK_ATTEMPTS;t++)
		{
			Vector V=trailArray[t];
			Room which=trackArray[t];
			if((V!=null)&&(which!=null)&&(V.size()<winningTotal))
			{
				winningTotal=V.size();
				winner=t;
			}
		}

		if(winner<0)
			return null;
		else
			return trailArray[winner];
	}

	public static int trackNextDirectionFromHere(Vector theTrail,
												 Room location,
												 boolean noWaterOrAir)
	{
		if((theTrail==null)||(location==null))
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
			if((thisRoom!=null)
			&&(thisExit!=null)
			&&((!noWaterOrAir)||(
			 	  (thisRoom.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
			 	&&(thisRoom.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
			 	&&(thisRoom.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
			 	&&(thisRoom.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
			 	&&(thisRoom.domainType()!=Room.DOMAIN_INDOORS_AIR)
			 	&&(thisRoom.domainType()!=Room.DOMAIN_OUTDOORS_AIR))))
			{
				for(int trail=0;trail<theTrail.size();trail++)
				{
					if((theTrail.elementAt(trail)==thisRoom)
					&&(trail<trailLength))
					{
						bestDirection=dirs;
						trailLength=trail;
						nextRoom=thisRoom;
						break;
					}
				}
			}
		}
		return bestDirection;
	}

	public static int radiatesFromDir(Room room, Vector rooms)
	{
		for(int i=0;i<rooms.size();i++)
		{
			Room R=(Room)rooms.elementAt(i);

			if(R==room) return -1;
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				if(R.getRoomInDir(d)==room)
					return Directions.getOpDirectionCode(d);
		}
		return -1;
	}
	public static void getRadiantRooms(Room room,
									   Vector rooms,
									   boolean openOnly,
									   boolean areaOnly,
									   boolean noSkyPlease,
									   Room radiateTo,
									   int maxDepth)
	{
		int depth=0;
		if(room==null) return;
		if(rooms.contains(room)) return;
		HashSet H=new HashSet();
		rooms.addElement(room);
		for(int r=0;r<rooms.size();r++)
			H.add(rooms.elementAt(r));
		int min=0;
		int size=rooms.size();
		boolean radiateToSomewhere=(radiateTo!=null);
		while(depth<maxDepth)
		{
			for(int r=min;r<size;r++)
			{
				Room R1=(Room)rooms.elementAt(r);
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					if((noSkyPlease)
					&&(CMMap.getExtendedRoomID(R1).length()>0)
					&&(R1.getRoomInDir(d)!=null)
					&&(CMMap.getExtendedRoomID(R1.getRoomInDir(d)).length()==0))
						continue;

					Room R=R1.getRoomInDir(d);
					Exit E=R1.getExitInDir(d);
					if((R!=null)
					&&(E!=null)
					&&((!areaOnly)||(R.getArea()==room.getArea()))
					&&((!openOnly)||(E.isOpen()))
					&&(!H.contains(R)))
					{
						rooms.addElement(R);
						H.add(R);
						if((radiateToSomewhere)
						&&(R==radiateTo))
							return;
					}
				}
			}
			min=size;
			size=rooms.size();
			if(min==size) return;
			depth++;
		}
	}

	public static boolean beMobile(MOB mob,
								   boolean dooropen,
								   boolean wander,
								   boolean roomprefer, boolean roomobject, Vector rooms)
	{
		// ridden and following things aren't mobile!
		if(((mob instanceof Rideable)&&(((Rideable)mob).numRiders()>0))
		||((mob.amFollowing()!=null)&&(mob.location()==mob.amFollowing().location())))
			return false;

		Room oldRoom=mob.location();

		for(int m=0;m<oldRoom.numInhabitants();m++)
		{
			MOB inhab=oldRoom.fetchInhabitant(m);
			if((inhab!=null)
			&&(!inhab.isMonster())
			&&(CMSecurity.isAllowed(inhab,oldRoom,"CMDMOBS")
			   ||CMSecurity.isAllowed(inhab,oldRoom,"CMDROOMS")))
				return false;
		}

		if(oldRoom instanceof GridLocale)
		{
			Vector V=((GridLocale)oldRoom).getAllRooms();
			Room R=(Room)(V.elementAt(Dice.roll(1,V.size(),-1)));
			if(R!=null) R.bringMobHere(mob,true);
			oldRoom=mob.location();
		}

		int tries=0;
		int direction=-1;
		while((tries++<10)&&(direction<0))
		{
			direction=Dice.roll(1,Directions.NUM_DIRECTIONS,-1);
			Room nextRoom=oldRoom.getRoomInDir(direction);
			Exit nextExit=oldRoom.getExitInDir(direction);
			if((nextRoom!=null)&&(nextExit!=null))
			{
				Exit opExit=nextRoom.getExitInDir(Directions.getOpDirectionCode(direction));
				for(int a=0;a<nextExit.numEffects();a++)
				{
					Ability aff=nextExit.fetchEffect(a);
					if((aff!=null)&&(aff instanceof Trap))
						direction=-1;
				}

				if(opExit!=null)
				{
					for(int a=0;a<opExit.numEffects();a++)
					{
						Ability aff=opExit.fetchEffect(a);
						if((aff!=null)&&(aff instanceof Trap))
							direction=-1;
					}
				}

				if((oldRoom.domainType()!=nextRoom.domainType())
				&&(!Sense.isInFlight(mob))
				&&((nextRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
				||(nextRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)))
					direction=-1;
				else
				if((oldRoom.domainType()!=nextRoom.domainType())
				&&(!Sense.isSwimming(mob))
				&&((nextRoom.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
				||(nextRoom.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)))
					direction=-1;
				else
				if((!wander)&&(!oldRoom.getArea().Name().equals(nextRoom.getArea().Name())))
					direction=-1;
				else
				if((roomobject)&&(rooms!=null)&&(rooms.contains(nextRoom)))
					direction=-1;
				else
				if((roomprefer)&&(rooms!=null)&&(!rooms.contains(nextRoom)))
					direction=-1;
				else
					break;
			}
			else
				direction=-1;
		}

		if(direction<0)
			return false;

		Room nextRoom=oldRoom.getRoomInDir(direction);
		Exit nextExit=oldRoom.getExitInDir(direction);
		int opDirection=Directions.getOpDirectionCode(direction);
		if((nextRoom==null)||(nextExit==null))
			return false;
		
		if((CoffeeUtensils.getLandTitle(nextRoom)!=null)
		&&(CoffeeUtensils.getLandTitle(nextRoom).landOwner().length()>0))
			dooropen=false;

		boolean reclose=false;
		boolean relock=false;
		// handle doors!
		if(nextExit.hasADoor()&&(!nextExit.isOpen())&&(dooropen))
		{
			if((nextExit.hasALock())&&(nextExit.isLocked()))
			{
				FullMsg msg=new FullMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
				if(oldRoom.okMessage(mob,msg))
				{
					relock=true;
					msg=new FullMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,"<S-NAME> unlock(s) <T-NAMESELF>.");
					if(oldRoom.okMessage(mob,msg))
						CoffeeUtensils.roomAffectFully(msg,oldRoom,direction);
				}
			}
			if(!nextExit.isOpen())
			{
				mob.doCommand(Util.parse("OPEN "+Directions.getDirectionName(direction)));
				if(nextExit.isOpen())
					reclose=true;
			}
		}
		if(!nextExit.isOpen())
			return false;

		if(((nextRoom.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
		||(nextRoom.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		   &&(!Sense.isWaterWorthy(mob))
		   &&(!Sense.isInFlight(mob))
		   &&(mob.fetchAbility("Skill_Swim")!=null))
		{
			Ability A=mob.fetchAbility("Skill_Swim");
			Vector V=new Vector();
			V.add(Directions.getDirectionName(direction));
			if(A.profficiency()<50)	A.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob)*15));
			CharState oldState=mob.curState().cloneCharState();
			A.invoke(mob,V,null,false);
			mob.curState().setMana(oldState.getMana());
			mob.curState().setMovement(oldState.getMovement());
		}
		else
		if((nextRoom.ID().indexOf("Surface")>0)
		&&(!Sense.isClimbing(mob))
		&&(!Sense.isInFlight(mob))
		&&(mob.fetchAbility("Skill_Climb")!=null))
		{
			Ability A=mob.fetchAbility("Skill_Climb");
			Vector V=new Vector();
			V.add(Directions.getDirectionName(direction));
			if(A.profficiency()<50)	A.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob)*15));
			CharState oldState=mob.curState().cloneCharState();
			A.invoke(mob,V,null,false);
			mob.curState().setMana(oldState.getMana());
			mob.curState().setMovement(oldState.getMovement());
		}
		else
		if(mob.fetchAbility("Thief_Sneak")!=null)
		{
			Ability A=mob.fetchAbility("Thief_Sneak");
			Vector V=new Vector();
			V.add(Directions.getDirectionName(direction));
			if(A.profficiency()<50)
			{
				A.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob)*15));
				Ability A2=mob.fetchAbility("Thief_Hide");
				if(A2!=null)
					A2.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob)*15));
			}
			CharState oldState=mob.curState().cloneCharState();
			A.invoke(mob,V,null,false);
			mob.curState().setMana(oldState.getMana());
			mob.curState().setMovement(oldState.getMovement());
		}
		else
			move(mob,direction,false,false);

		if((reclose)&&(mob.location()==nextRoom)&&(dooropen))
		{
			Exit opExit=nextRoom.getExitInDir(opDirection);
			if((opExit!=null)
			&&(opExit.hasADoor())
			&&(opExit.isOpen()))
			{
				mob.doCommand(Util.parse("CLOSE "+Directions.getDirectionName(opDirection)));
				if((opExit.hasALock())&&(relock))
				{
					FullMsg msg=new FullMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
					if(nextRoom.okMessage(mob,msg))
					{
						msg=new FullMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_LOCK,CMMsg.MSG_OK_VISUAL,"<S-NAME> lock(s) <T-NAMESELF>.");
						if(nextRoom.okMessage(mob,msg))
							CoffeeUtensils.roomAffectFully(msg,nextRoom,opDirection);
					}
				}
			}
		}
		return mob.location()!=oldRoom;
	}

	public static void wanderAway(MOB M, boolean mindPCs, boolean andGoHome)
	{
		Room R=M.location();
		if(R==null) return;
		int tries=0;
		while((M.location()==R)&&((++tries)<100)&&((!mindPCs)||(R.numPCInhabitants()>0)))
			beMobile(M,true,true,false,false,null);
		if((M.getStartRoom()!=null)&&(andGoHome))
			M.getStartRoom().bringMobHere(M,true);
	}

	public static boolean move(MOB mob,
							   int directionCode,
							   boolean flee,
							   boolean nolook,
							   boolean noriders)
	{
		try{
			Command C=CMClass.getCommand("Go");
			if(C!=null)
			{
				Vector V=new Vector();
				V.addElement(new Integer(directionCode));
				V.addElement(new Boolean(flee));
				V.addElement(new Boolean(nolook));
				V.addElement(new Boolean(noriders));
				return C.execute(mob,V);
			}
		}
		catch(Exception e)
		{
			Log.errOut("MUDTracker",e);
		}
		return false;
	}
	public static boolean move(MOB mob, int directionCode, boolean flee, boolean nolook)
	{
		return move(mob,directionCode,flee,nolook,false);
	}

	public static int findExitDir(MOB mob, Room R, String desc)
	{
		int dir=Directions.getGoodDirectionCode(desc);
		if(dir<0)
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Exit e=R.getExitInDir(d);
			Room r=R.getRoomInDir(d);
			if((e!=null)&&(r!=null))
			{
				if((Sense.canBeSeenBy(e,mob))
				&&((e.name().equalsIgnoreCase(desc))
				||(e.displayText().equalsIgnoreCase(desc))
				||(r.roomTitle().equalsIgnoreCase(desc))
				||(e.description().equalsIgnoreCase(desc))))
				{
					dir=d; break;
				}
			}
		}
		if(dir<0)
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Exit e=R.getExitInDir(d);
			Room r=R.getRoomInDir(d);
			if((e!=null)&&(r!=null))
			{
				if((Sense.canBeSeenBy(e,mob))
				&&(((EnglishParser.containsString(e.name(),desc))
				||(EnglishParser.containsString(e.displayText(),desc))
				||(EnglishParser.containsString(r.displayText(),desc))
				||(EnglishParser.containsString(e.description(),desc)))))
				{
					dir=d; break;
				}
			}
		}
		return dir;
	}
}
