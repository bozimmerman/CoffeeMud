package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;


/*
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class MUDTracker extends StdLibrary implements TrackingLibrary
{
    public String ID(){return "MUDTracker";}
    public Vector findBastardTheBestWay(Room location,
							            Room destRoom,
							            TrackingFlags flags,
							            int maxRadius)
    {
    	return findBastardTheBestWay(location,destRoom,flags,maxRadius,null);
    }
    public Vector findBastardTheBestWay(Room location,
                                        Room destRoom,
                                        TrackingFlags flags,
                                        int maxRadius,
                                        Vector<Room> radiant)
   {
        if((radiant==null)||(radiant.size()==0)){
        	radiant=new Vector();
            getRadiantRooms(location,radiant,flags,destRoom,maxRadius,null);
            if(!radiant.contains(location))
                radiant.insertElementAt(location,0);
        }
        else
        {
        	Vector radiant2=new Vector(radiant.size());
        	int r=0;
        	boolean foundLocation=false;
        	Object O;
        	for(;r<radiant.size();r++)
			{
        		O=radiant.elementAt(r);
        		radiant2.addElement(O);
        		if((!foundLocation)&&(O==location))
        			foundLocation=true;
        		if(O==destRoom) break;
			}
    		if(!foundLocation)
    		{
                radiant.insertElementAt(location,0);
                radiant2.insertElementAt(location,0);
    		}
        	if(r>=radiant.size()) return null;
        	radiant=radiant2;
        }
        if((radiant.size()>0)&&(destRoom==radiant.lastElement()))
        {
            Vector thisTrail=new Vector();
            HashSet tried=new HashSet();
            thisTrail.addElement(destRoom);
            tried.add(destRoom);
            Room R=null;
            int index=radiant.size()-2;
            if(destRoom!=location)
            while(index>=0)
            {
                int best=-1;
                for(int i=index;i>=0;i--)
                {
                    R=(Room)radiant.elementAt(i);
                    for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
                    {
                        if((R.getRoomInDir(d)==thisTrail.lastElement())
                        &&(R.getExitInDir(d)!=null)
                        &&(!tried.contains(R)))
                            best=i;
                    }
                }
                if(best>=0)
                {
                    R=(Room)radiant.elementAt(best);
                    thisTrail.addElement(R);
                    tried.add(R);
                    if(R==location) break;
                    index=best-1;
                }
                else
                {
                    thisTrail.clear();
                    thisTrail=null;
                    break;
                }
            }
            return thisTrail;
        }
        return null;
   }


	public Vector findBastardTheBestWay(Room location,
									    Vector<Room> destRooms,
									    TrackingFlags flags,
									    int maxRadius)
	{

	    Vector finalTrail=null;
        Room destRoom=null;
        int pick=0;
        Vector radiant=null;
        if(destRooms.size()>1)
        {
        	radiant=new Vector();
            getRadiantRooms(location,radiant,flags,null,maxRadius,null);
        }
        for(int i=0;(i<5)&&(destRooms.size()>0);i++)
        {
            pick=CMLib.dice().roll(1,destRooms.size(),-1);
            destRoom=(Room)destRooms.elementAt(pick);
            destRooms.removeElementAt(pick);
            Vector thisTrail=findBastardTheBestWay(location,destRoom,flags,maxRadius,radiant);
            if((thisTrail!=null)
            &&((finalTrail==null)||(thisTrail.size()<finalTrail.size())))
                finalTrail=thisTrail;
        }
        if(finalTrail==null)
	    for(int r=0;r<destRooms.size();r++)
	    {
	        destRoom=(Room)destRooms.elementAt(r);
            Vector thisTrail=findBastardTheBestWay(location,destRoom,flags,maxRadius);
            if((thisTrail!=null)
            &&((finalTrail==null)||(thisTrail.size()<finalTrail.size())))
                finalTrail=thisTrail;
	    }
	    return finalTrail;
	}

	public int trackNextDirectionFromHere(Vector<Room> theTrail,
										  Room location,
										  boolean openOnly)
	{
		if((theTrail==null)||(location==null))
			return -1;
		if(location==theTrail.elementAt(0))
			return 999;
		int locationLocation=theTrail.indexOf(location);
		if(locationLocation<0) locationLocation=Integer.MAX_VALUE;
		Room R=null;
		Exit E=null;
		int x=0;
		int winningDirection=-1;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
	        R=location.getRoomInDir(d);
			E=location.getExitInDir(d);
			if((R!=null)
			&&(E!=null)
			&&((!openOnly)||(E.isOpen())))
			{
			    x=theTrail.indexOf(R);
			    if((x>=0)&&(x<locationLocation))
			    {
			        locationLocation=x;
			        winningDirection=d;
			    }
			}
		}
		return winningDirection;
	}

	public int radiatesFromDir(Room room, Vector<Room> rooms)
	{
	    Room R=null;
		for(int i=0;i<rooms.size();i++)
		{
			R=(Room)rooms.elementAt(i);

			if(R==room) return -1;
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				if(R.getRoomInDir(d)==room)
					return Directions.getOpDirectionCode(d);
		}
		return -1;
	}

	public Vector getRadiantRooms(Room room,
								  TrackingFlags flags,
								  int maxDepth)
	{
		Vector V=new Vector();
		getRadiantRooms(room,V,flags,null,maxDepth,null);
		return V;
	}
	public void getRadiantRooms(Room room,
							    Vector<Room> rooms,
							    TrackingFlags flags,
							    Room radiateTo,
							    int maxDepth,
                                HashSet<Room> ignoreRooms)
	{
		int depth=0;
		if(room==null) return;
		if(rooms.contains(room)) return;
		HashSet H=new HashSet(1000);
		rooms.addElement(room);
        rooms.ensureCapacity(200);
		for(int r=0;r<rooms.size();r++)
			H.add(rooms.elementAt(r));
		int min=0;
		int size=rooms.size();
		boolean radiateToSomewhere=(radiateTo!=null);
		Room R1=null;
		Room R=null;
		Exit E=null;
		boolean nohomes = (flags!=null) && flags.contains(TrackingFlag.NOHOMES);
	    boolean openOnly = (flags!=null) && flags.contains(TrackingFlag.OPENONLY);
	    boolean areaOnly = (flags!=null) && flags.contains(TrackingFlag.AREAONLY);
	    boolean noEmptyGrids = (flags!=null) && flags.contains(TrackingFlag.NOEMPTYGRIDS);
	    boolean noAir = (flags!=null) && flags.contains(TrackingFlag.NOAIR);
	    boolean noWater = (flags!=null) && flags.contains(TrackingFlag.NOWATER);

        int r=0;
        int d=0;
		while(depth<maxDepth)
		{
			for(r=min;r<size;r++)
			{
				R1=(Room)rooms.elementAt(r);
				for(d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					R=R1.getRoomInDir(d);
					E=R1.getExitInDir(d);

					if((R==null)
					||(E==null)
                    ||((ignoreRooms!=null)&&(ignoreRooms.contains(R)))
                    ||(H.contains(R))
					||((areaOnly)&&(R.getArea()!=room.getArea()))
					||((openOnly)&&(!E.isOpen()))
					||((noAir)
						&&((R.domainType()==Room.DOMAIN_INDOORS_AIR)
						 	||(R.domainType()==Room.DOMAIN_OUTDOORS_AIR)))
					||((nohomes)&&(CMLib.law().getLandTitle(R)!=null))
					||((noWater)
						&&((R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
					       ||(R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
					       ||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
					        ||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)))
					||((noEmptyGrids)
						&&(R.getGridParent()!=null)
						&&(R.getGridParent().roomID().length()==0)))
					    continue;
					rooms.addElement(R);
					H.add(R);
					if((radiateToSomewhere)
					&&(R==radiateTo))
						return;
				}
			}
			min=size;
			size=rooms.size();
			if(min==size) return;
			depth++;
		}
	}

    public void stopTracking(MOB mob)
    {
        Vector V=CMLib.flags().flaggedAffects(mob,Ability.FLAG_TRACKING);
        for(int v=0;v<V.size();v++)
        { ((Ability)V.elementAt(v)).unInvoke(); mob.delEffect((Ability)V.elementAt(v));}
    }

    public boolean beMobile(MOB mob,
                            boolean dooropen,
                            boolean wander,
                            boolean roomprefer,
                            boolean roomobject,
                            long[] status,
                            Vector rooms)
    {
        return beMobile(mob,dooropen,wander,roomprefer,roomobject,true,status,rooms);

    }
	private boolean beMobile(MOB mob,
						     boolean dooropen,
						     boolean wander,
						     boolean roomprefer,
                             boolean roomobject,
                             boolean sneakIfAble,
                             long[] status,
                             Vector<Room> rooms)
	{
        if(status!=null)status[0]=Tickable.STATUS_MISC7+0;

		// ridden and following things aren't mobile!
		if(((mob instanceof Rideable)&&(((Rideable)mob).numRiders()>0))
		||((mob.amFollowing()!=null)&&(mob.location()==mob.amFollowing().location())))
        {
            if(status!=null)status[0]=Tickable.STATUS_NOT;
			return false;
        }

		Room oldRoom=mob.location();

        if(status!=null)status[0]=Tickable.STATUS_MISC7+1;
		for(int m=0;m<oldRoom.numInhabitants();m++)
		{
			MOB inhab=oldRoom.fetchInhabitant(m);
			if((inhab!=null)
			&&(!inhab.isMonster())
			&&(CMSecurity.isAllowed(inhab,oldRoom,"CMDMOBS")
			   ||CMSecurity.isAllowed(inhab,oldRoom,"CMDROOMS")))
            {
                if(status!=null)status[0]=Tickable.STATUS_NOT;
                return false;
            }
		}

        if(status!=null)status[0]=Tickable.STATUS_MISC7+2;
		if(oldRoom instanceof GridLocale)
		{
			Room R=((GridLocale)oldRoom).getRandomGridChild();
			if(R!=null) R.bringMobHere(mob,true);
			oldRoom=mob.location();
		}

        if(status!=null)status[0]=Tickable.STATUS_MISC7+3;
		int tries=0;
		int direction=-1;
		while(((tries++)<10)&&(direction<0))
		{
            if(status!=null)status[0]=Tickable.STATUS_MISC7+5;
			direction=CMLib.dice().roll(1,Directions.NUM_DIRECTIONS(),-1);
			Room nextRoom=oldRoom.getRoomInDir(direction);
			Exit nextExit=oldRoom.getExitInDir(direction);
			if((nextRoom!=null)&&(nextExit!=null))
			{
				Exit opExit=nextRoom.getExitInDir(Directions.getOpDirectionCode(direction));
                if(status!=null)status[0]=Tickable.STATUS_MISC7+6;
				for(int a=0;a<nextExit.numEffects();a++)
				{
					Ability aff=nextExit.fetchEffect(a);
					if((aff!=null)&&(aff instanceof Trap))
						direction=-1;
				}

                if(status!=null)status[0]=Tickable.STATUS_MISC7+7;
				if(opExit!=null)
				{
                    if(status!=null)status[0]=Tickable.STATUS_MISC7+8;
					for(int a=0;a<opExit.numEffects();a++)
					{
						Ability aff=opExit.fetchEffect(a);
						if((aff!=null)&&(aff instanceof Trap))
							direction=-1;
					}
				}
                if(status!=null)status[0]=Tickable.STATUS_MISC7+9;
				if((oldRoom.domainType()!=nextRoom.domainType())
				&&(!CMLib.flags().isInFlight(mob))
				&&((nextRoom.domainType()==Room.DOMAIN_INDOORS_AIR)
				||(nextRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)))
					direction=-1;
				else
				if((oldRoom.domainType()!=nextRoom.domainType())
				&&(!CMLib.flags().isSwimming(mob))
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

        if(status!=null)status[0]=Tickable.STATUS_MISC7+10;

		if(direction<0)
        {
            if(status!=null)status[0]=Tickable.STATUS_NOT;
            return false;
        }

		Room nextRoom=oldRoom.getRoomInDir(direction);
		Exit nextExit=oldRoom.getExitInDir(direction);
		int opDirection=Directions.getOpDirectionCode(direction);

		if((nextRoom==null)||(nextExit==null))
        {
            if(status!=null)status[0]=Tickable.STATUS_NOT;
            return false;
        }

        if(status!=null)status[0]=Tickable.STATUS_MISC7+11;

		if((CMLib.law().getLandTitle(nextRoom)!=null)
		&&(CMLib.law().getLandTitle(nextRoom).landOwner().length()>0))
			dooropen=false;

		boolean reclose=false;
		boolean relock=false;
		// handle doors!
		if(nextExit.hasADoor()&&(!nextExit.isOpen())&&(dooropen))
		{
            if(status!=null)status[0]=Tickable.STATUS_MISC7+12;
			if((nextExit.hasALock())&&(nextExit.isLocked()))
			{
                if(status!=null)status[0]=Tickable.STATUS_MISC7+13;
				CMMsg msg=CMClass.getMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
				if(oldRoom.okMessage(mob,msg))
				{
                    if(status!=null)status[0]=Tickable.STATUS_MISC7+14;
					relock=true;
					msg=CMClass.getMsg(mob,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,"<S-NAME> unlock(s) <T-NAMESELF>.");
					if(oldRoom.okMessage(mob,msg))
						CMLib.utensils().roomAffectFully(msg,oldRoom,direction);
				}
			}
            if(status!=null)status[0]=Tickable.STATUS_MISC7+15;
			if(!nextExit.isOpen())
			{
				mob.doCommand(CMParms.parse("OPEN "+Directions.getDirectionName(direction)),Command.METAFLAG_FORCED);
				if(nextExit.isOpen())
					reclose=true;
			}
		}
        if(status!=null)status[0]=Tickable.STATUS_MISC7+16;
		if(!nextExit.isOpen())
        {
            if(status!=null)status[0]=Tickable.STATUS_NOT;
            return false;
        }

		if(((nextRoom.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
		||(nextRoom.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		   &&(!CMLib.flags().isWaterWorthy(mob))
		   &&(!CMLib.flags().isInFlight(mob))
		   &&(mob.fetchAbility("Skill_Swim")!=null))
		{
            if(status!=null)status[0]=Tickable.STATUS_MISC7+17;
			Ability A=mob.fetchAbility("Skill_Swim");
			Vector V=new Vector();
			V.add(Directions.getDirectionName(direction));
			if(A.proficiency()<50)	A.setProficiency(CMLib.dice().roll(1,50,A.adjustedLevel(mob,0)*15));
			CharState oldState=(CharState)mob.curState().copyOf();
			A.invoke(mob,V,null,false,0);
			mob.curState().setMana(oldState.getMana());
			mob.curState().setMovement(oldState.getMovement());
		}
		else
		if(((nextRoom.ID().indexOf("Surface")>0)||(CMLib.flags().isClimbing(nextExit))||(CMLib.flags().isClimbing(nextRoom)))
		&&(!CMLib.flags().isClimbing(mob))
		&&(!CMLib.flags().isInFlight(mob))
		&&((mob.fetchAbility("Skill_Climb")!=null)||(mob.fetchAbility("Power_SuperClimb")!=null)))
		{
            if(status!=null)status[0]=Tickable.STATUS_MISC7+18;
			Ability A=mob.fetchAbility("Skill_Climb");
			if(A==null )A=mob.fetchAbility("Power_SuperClimb");
			Vector V=new Vector();
			V.add(Directions.getDirectionName(direction));
			if(A.proficiency()<50)	A.setProficiency(CMLib.dice().roll(1,50,A.adjustedLevel(mob,0)*15));
			CharState oldState=(CharState)mob.curState().copyOf();
			A.invoke(mob,V,null,false,0);
			mob.curState().setMana(oldState.getMana());
			mob.curState().setMovement(oldState.getMovement());
		}
		else
		if((mob.fetchAbility("Thief_Sneak")!=null)&&(sneakIfAble))
		{
            if(status!=null)status[0]=Tickable.STATUS_MISC7+19;
			Ability A=mob.fetchAbility("Thief_Sneak");
			Vector V=new Vector();
			V.add(Directions.getDirectionName(direction));
			if(A.proficiency()<50)
			{
				A.setProficiency(CMLib.dice().roll(1,50,A.adjustedLevel(mob,0)*15));
				Ability A2=mob.fetchAbility("Thief_Hide");
				if(A2!=null)
					A2.setProficiency(CMLib.dice().roll(1,50,A.adjustedLevel(mob,0)*15));
			}
			CharState oldState=(CharState)mob.curState().copyOf();
			A.invoke(mob,V,null,false,0);
			mob.curState().setMana(oldState.getMana());
			mob.curState().setMovement(oldState.getMovement());
		}
		else
        {
            if(status!=null)status[0]=Tickable.STATUS_MISC7+20;
			move(mob,direction,false,false);
        }
        if(status!=null)status[0]=Tickable.STATUS_MISC7+21;

		if((reclose)&&(mob.location()==nextRoom)&&(dooropen))
		{
			Exit opExit=nextRoom.getExitInDir(opDirection);
			if((opExit!=null)
			&&(opExit.hasADoor())
			&&(opExit.isOpen()))
			{
                if(status!=null)status[0]=Tickable.STATUS_MISC7+22;
				mob.doCommand(CMParms.parse("CLOSE "+Directions.getDirectionName(opDirection)),Command.METAFLAG_FORCED);
				if((opExit.hasALock())&&(relock))
				{
                    if(status!=null)status[0]=Tickable.STATUS_MISC7+23;
					CMMsg msg=CMClass.getMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
					if(nextRoom.okMessage(mob,msg))
					{
						msg=CMClass.getMsg(mob,opExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_LOCK,CMMsg.MSG_OK_VISUAL,"<S-NAME> lock(s) <T-NAMESELF>.");
						if(nextRoom.okMessage(mob,msg))
							CMLib.utensils().roomAffectFully(msg,nextRoom,opDirection);
					}
				}
			}
		}
        if(status!=null)status[0]=Tickable.STATUS_NOT;
		return mob.location()!=oldRoom;
	}

	public void wanderAway(MOB M, boolean mindPCs, boolean andGoHome)
	{
	    if(M==null) return;
		Room R=M.location();
		if(R==null) return;
		int tries=0;
		while((M.location()==R)&&((++tries)<100)&&((!mindPCs)||(R.numPCInhabitants()>0)))
			beMobile(M,true,true,false,false,false,null,null);
		if((M.getStartRoom()!=null)&&(andGoHome))
			M.getStartRoom().bringMobHere(M,true);
	}

	public void wanderFromTo(MOB M, Room toHere, boolean mindPCs)
	{
	    if(M==null) return;
	    if((M.location()!=null)&&(M.location().isInhabitant(M)))
		    wanderAway(M,mindPCs,false);
	    wanderIn(M,toHere);
	}

	public void wanderIn(MOB M, Room toHere)
	{
	    if(toHere==null) return;
	    if(M==null) return;
		int tries=0;
		int dir=-1;
		while((dir<0)&&((++tries)<100))
		{
		    dir=CMLib.dice().roll(1,Directions.NUM_DIRECTIONS(),-1);
		    Room R=toHere.getRoomInDir(dir);
		    if(R!=null)
		    {
		        if(((R.domainType()==Room.DOMAIN_INDOORS_AIR)&&(!CMLib.flags().isFlying(M)))
		        ||((R.domainType()==Room.DOMAIN_OUTDOORS_AIR)&&(!CMLib.flags().isFlying(M)))
		        ||((R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)&&(!CMLib.flags().isSwimming(M)))
		        ||((R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)&&(!CMLib.flags().isSwimming(M)))
		        ||((R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)&&(!CMLib.flags().isWaterWorthy(M)))
		        ||((R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)&&(!CMLib.flags().isWaterWorthy(M))))
		            dir=-1;
		    }
		    else
		        dir=-1;
		}
		if(dir<0)
			toHere.show(M,null,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wanders in.");
		else
			toHere.show(M,null,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wanders in from "+Directions.getDirectionName(dir)+".");
		toHere.bringMobHere(M,true);
	}

	public boolean move(MOB mob,
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
				V.addElement(Integer.valueOf(directionCode));
				V.addElement(Boolean.valueOf(flee));
				V.addElement(Boolean.valueOf(nolook));
				V.addElement(Boolean.valueOf(noriders));
				return C.execute(mob,V,Command.METAFLAG_FORCED);
			}
		}
		catch(Exception e)
		{
			Log.errOut("MUDTracker",e);
		}
		return false;
	}
	public boolean move(MOB mob, int directionCode, boolean flee, boolean nolook)
	{
		return move(mob,directionCode,flee,nolook,false);
	}

	public int findExitDir(MOB mob, Room R, String desc)
	{
		int dir=Directions.getGoodDirectionCode(desc);
		if(dir<0)
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			Exit e=R.getExitInDir(d);
			Room r=R.getRoomInDir(d);
			if((e!=null)&&(r!=null))
			{
				if((CMLib.flags().canBeSeenBy(e,mob))
				&&((e.name().equalsIgnoreCase(desc))
				||(e.displayText().equalsIgnoreCase(desc))
				||(r.roomTitle(mob).equalsIgnoreCase(desc))
				||(e.description().equalsIgnoreCase(desc))))
				{
					dir=d; break;
				}
			}
		}
		if(dir<0)
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			Exit e=R.getExitInDir(d);
			Room r=R.getRoomInDir(d);
			if((e!=null)&&(r!=null))
			{
				if((CMLib.flags().canBeSeenBy(e,mob))
				&&(((CMLib.english().containsString(e.name(),desc))
				||(CMLib.english().containsString(e.displayText(),desc))
				||(CMLib.english().containsString(r.displayText(),desc))
				||(CMLib.english().containsString(e.description(),desc)))))
				{
					dir=d; break;
				}
			}
		}
		return dir;
	}
	public int findRoomDir(MOB mob, Room R)
	{
	    if((mob==null)||(R==null))
	        return -1;
	    Room R2=mob.location();
	    if(R2==null)
	        return -1;
		int dir=-1;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		    if(R2.getRoomInDir(d)==R)
		        return d;
		return dir;
	}

	public Vector findAllTrails(Room from, Room to, Vector<Room> radiantTrail)
	{
		Vector finalSets=new Vector();
		if((from==null)||(to==null)||(from==to)) return finalSets;
		int index=radiantTrail.indexOf(to);
		if(index<0) return finalSets;
		Room R=null;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			R=to.getRoomInDir(d);
			if(R!=null)
			{
				if((R==from)&&(from.getRoomInDir(Directions.getOpDirectionCode(d))==to))
				{
					finalSets.addElement(CMParms.makeVector(Integer.valueOf(Directions.getOpDirectionCode(d))));
					return finalSets;
				}
				int dex=radiantTrail.indexOf(R);
				if((dex>=0)&&(dex<index)&&(R.getRoomInDir(Directions.getOpDirectionCode(d))==to))
				{
					Vector allTrailsBack=findAllTrails(from,R,radiantTrail);
					for(int a=0;a<allTrailsBack.size();a++)
					{
						Vector thisTrail=(Vector)allTrailsBack.elementAt(a);
						thisTrail.addElement(Integer.valueOf(Directions.getOpDirectionCode(d)));
						finalSets.addElement(thisTrail);
					}
				}
			}
		}
		return finalSets;
	}

	public Vector findAllTrails(Room from, Vector<Room> tos, Vector<Room> radiantTrail)
	{
		Vector finalSets=new Vector();
		if(from==null) return finalSets;
		Room to=null;
		for(int t=0;t<tos.size();t++)
		{
			to=(Room)tos.elementAt(t);
			finalSets.addAll(findAllTrails(from,to,radiantTrail));
		}
		return finalSets;
	}
	
	protected int getRoomDirection(Room R, Room toRoom, Vector<Room> ignore)
	{
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			if((R.getRoomInDir(d)==toRoom)
			&&(R!=toRoom)
			&&(!ignore.contains(R)))
				return d;
		return -1;
	}
	
	public String getTrailToDescription(Room R1, Vector<Room> set, String where, boolean areaNames, boolean confirm, int radius, HashSet<Room> ignoreRooms, int maxMins)
	{
		Room R2=CMLib.map().getRoom(where);
		if(R2==null)
			for(Enumeration a=CMLib.map().sortedAreas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				if(A.name().equalsIgnoreCase(where))
				{
					if(set.size()==0)
					{
						int lowest=Integer.MAX_VALUE;
						for(Enumeration r=A.getCompleteMap();r.hasMoreElements();)
						{
							Room R=(Room)r.nextElement();
							int x=R.roomID().indexOf("#");
							if((x>=0)&&(CMath.s_int(R.roomID().substring(x+1))<lowest))
								lowest=CMath.s_int(R.roomID().substring(x+1));
						}
						if(lowest<Integer.MAX_VALUE)
							R2=CMLib.map().getRoom(A.name()+"#"+lowest);
					}
					else
					{
						for(int i=0;i<set.size();i++)
						{
							Room R=(Room)set.elementAt(i);
							if(R.getArea()==A)
							{
								R2=R;
								break;
							}
						}
					}
					break;
				}
			}
		if(R2==null) return "Unable to determine '"+where+"'.";
		TrackingLibrary.TrackingFlags flags = new TrackingLibrary.TrackingFlags()
											.add(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS);
		if(set.size()==0)
			getRadiantRooms(R1,set,flags,R2,radius,ignoreRooms);
		int foundAt=-1;
		for(int i=0;i<set.size();i++)
		{
			Room R=(Room)set.elementAt(i);
			if(R==R2){ foundAt=i; break;}
		}
		if(foundAt<0) return "You can't get to '"+R2.roomID()+"' from here.";
		Room checkR=R2;
		Vector trailV=new Vector();
		trailV.addElement(R2);
        HashSet areasDone=new HashSet();
		boolean didSomething=false;
		long startTime = System.currentTimeMillis();
		while(checkR!=R1)
		{
			long waitTime = System.currentTimeMillis() - startTime;
			if(waitTime > (1000 * 60 * (maxMins)))
				return "You can't get there from here.";
			didSomething=false;
			for(int r=foundAt-1;r>=0;r--)
			{
				Room R=(Room)set.elementAt(r);
				if(getRoomDirection(R,checkR,trailV)>=0)
				{
					trailV.addElement(R);
                    if(!areasDone.contains(R.getArea()))
                        areasDone.add(R.getArea());
					foundAt=r;
					checkR=R;
					didSomething=true;
					break;
				}
			}
			if(!didSomething)
				return "You can't get there from here.";
		}
		Vector theDirTrail=new Vector();
		Vector empty=new Vector();
		for(int s=trailV.size()-1;s>=1;s--)
		{
			Room R=(Room)trailV.elementAt(s);
			Room RA=(Room)trailV.elementAt(s-1);
			theDirTrail.addElement(Character.toString(Directions.getDirectionName(getRoomDirection(R,RA,empty)).charAt(0))+" ");
		}
		StringBuffer theTrail=new StringBuffer("");
		if(confirm)	theTrail.append("\n\r"+CMStrings.padRight("Trail",30)+": ");
		char lastDir='\0';
		int lastNum=0;
		while(theDirTrail.size()>0)
		{
			String s=(String)theDirTrail.elementAt(0);
			if(lastNum==0)
			{
				lastDir=s.charAt(0);
				lastNum=1;
			}
			else
			if(s.charAt(0)==lastDir)
				lastNum++;
			else
			{
				if(lastNum==1)
					theTrail.append(Character.toString(lastDir)+" ");
				else
					theTrail.append(Integer.toString(lastNum)+Character.toString(lastDir)+" ");
				lastDir=s.charAt(0);
				lastNum=1;
			}
			theDirTrail.removeElementAt(0);
		}
		if(lastNum==1)
			theTrail.append(Character.toString(lastDir));
		else
		if(lastNum>0)
			theTrail.append(Integer.toString(lastNum)+Character.toString(lastDir));

		if((confirm)&&(trailV.size()>1))
		{
			for(int i=0;i<trailV.size();i++)
			{
				Room R=(Room)trailV.elementAt(i);
				if(R.roomID().length()==0)
				{
					theTrail.append("*");
					break;
				}
			}
			Room R=(Room)trailV.elementAt(1);
			theTrail.append("\n\r"+CMStrings.padRight("From",30)+": "+Directions.getDirectionName(getRoomDirection(R,R2,empty))+" <- "+R.roomID());
			theTrail.append("\n\r"+CMStrings.padRight("Room",30)+": "+R.displayText()+"/"+R.description());
			theTrail.append("\n\r\n\r");
		}
        if((areaNames)&&(areasDone.size()>0))
        {
            theTrail.append("\n\r"+CMStrings.padRight("Areas",30)+":");
            for(Iterator i=areasDone.iterator();i.hasNext();)
            {
                Area A=(Area)i.next();
                theTrail.append(" \""+A.name()+"\",");
            }
        }
        if(theTrail.toString().trim().length()==0)
        	return "You can't get there from here.";
		return theTrail.toString();
	}


}
