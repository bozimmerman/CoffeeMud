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
   Copyright 2000-2006 Bo Zimmerman

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
public class MUDTracker extends StdLibrary implements TrackingLibrary
{
    public String ID(){return "MUDTracker";}
    public Vector findBastardTheBestWay(Room location,
							            Room destRoom,
							            boolean openOnly,
							            boolean areaOnly,
							            boolean noEmptyGrids,
							            boolean noAir,
							            boolean noWater,
							            int maxRadius)
    {
    	return findBastardTheBestWay(location,destRoom,openOnly,areaOnly,noEmptyGrids,noAir,noWater,maxRadius,null);
    }
    public Vector findBastardTheBestWay(Room location,
                                        Room destRoom,
                                        boolean openOnly,
                                        boolean areaOnly,
                                        boolean noEmptyGrids,
                                        boolean noAir,
                                        boolean noWater,
                                        int maxRadius,
                                        Vector radiant)
   {
        if((radiant==null)||(radiant.size()==0)){
        	radiant=new Vector();
            getRadiantRooms(location,radiant,openOnly,areaOnly,noEmptyGrids,noAir,noWater,destRoom,maxRadius,null);
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
                    for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
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
									    Vector destRooms,
									    boolean openOnly,
									    boolean areaOnly,
									    boolean noEmptyGrids,
									    boolean noAir,
									    boolean noWater,
									    int maxRadius)
	{
	    
	    Vector finalTrail=null;
        Room destRoom=null;
        int pick=0;
        Vector radiant=null;
        if(destRooms.size()>1)
        {
        	radiant=new Vector();
            getRadiantRooms(location,radiant,openOnly,areaOnly,noEmptyGrids,noAir,noWater,null,maxRadius,null);
        }
        for(int i=0;(i<5)&&(destRooms.size()>0);i++)
        {
            pick=CMLib.dice().roll(1,destRooms.size(),-1);
            destRoom=(Room)destRooms.elementAt(pick);
            destRooms.removeElementAt(pick);
            Vector thisTrail=findBastardTheBestWay(location,destRoom,openOnly,areaOnly,noEmptyGrids,noAir,noWater,maxRadius,radiant);
            if((thisTrail!=null)
            &&((finalTrail==null)||(thisTrail.size()<finalTrail.size())))
                finalTrail=thisTrail;
        }
        if(finalTrail==null)
	    for(int r=0;r<destRooms.size();r++)
	    {
	        destRoom=(Room)destRooms.elementAt(r);
            Vector thisTrail=findBastardTheBestWay(location,destRoom,openOnly,areaOnly,noEmptyGrids,noAir,noWater,maxRadius);
            if((thisTrail!=null)
            &&((finalTrail==null)||(thisTrail.size()<finalTrail.size())))
                finalTrail=thisTrail;
	    }
	    return finalTrail;
	}

	public int trackNextDirectionFromHere(Vector theTrail,
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
		for(int dirs=0;dirs<Directions.NUM_DIRECTIONS;dirs++)
		{
	        R=location.getRoomInDir(dirs);
			E=location.getExitInDir(dirs);
			if((R!=null)
			&&(E!=null)
			&&((!openOnly)||(E.isOpen())))
			{
			    x=theTrail.indexOf(R);
			    if((x>=0)&&(x<locationLocation))
			    {
			        locationLocation=x;
			        winningDirection=dirs;
			    }
			}
		}
		return winningDirection;
	}

	public int radiatesFromDir(Room room, Vector rooms)
	{
	    Room R=null;
		for(int i=0;i<rooms.size();i++)
		{
			R=(Room)rooms.elementAt(i);

			if(R==room) return -1;
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				if(R.getRoomInDir(d)==room)
					return Directions.getOpDirectionCode(d);
		}
		return -1;
	}
	
	public Vector getRadiantRooms(Room room,
								  boolean openOnly,
								  boolean areaOnly,
								  boolean noEmptyGrids,
								  boolean noAir,
								  boolean noWater,
								  int maxDepth)
	{
		Vector V=new Vector();
		getRadiantRooms(room,V,openOnly,areaOnly,noEmptyGrids,noAir,noWater,null,maxDepth,null);
		return V;
	}
	public void getRadiantRooms(Room room,
							    Vector rooms,
							    boolean openOnly,
							    boolean areaOnly,
							    boolean noEmptyGrids,
							    boolean noAir,
							    boolean noWater,
							    Room radiateTo,
							    int maxDepth,
                                HashSet ignoreRooms)
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
        
        int r=0;
        int d=0;
		while(depth<maxDepth)
		{
			for(r=min;r<size;r++)
			{
				R1=(Room)rooms.elementAt(r);
				for(d=0;d<Directions.NUM_DIRECTIONS;d++)
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
			direction=CMLib.dice().roll(1,Directions.NUM_DIRECTIONS,-1);
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
        
		if((CMLib.utensils().getLandTitle(nextRoom)!=null)
		&&(CMLib.utensils().getLandTitle(nextRoom).landOwner().length()>0))
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
				mob.doCommand(CMParms.parse("OPEN "+Directions.getDirectionName(direction)));
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
			if(A.profficiency()<50)	A.setProfficiency(CMLib.dice().roll(1,50,A.adjustedLevel(mob,0)*15));
			CharState oldState=(CharState)mob.curState().copyOf();
			A.invoke(mob,V,null,false,0);
			mob.curState().setMana(oldState.getMana());
			mob.curState().setMovement(oldState.getMovement());
		}
		else
		if((nextRoom.ID().indexOf("Surface")>0)
		&&(!CMLib.flags().isClimbing(mob))
		&&(!CMLib.flags().isInFlight(mob))
		&&((mob.fetchAbility("Skill_Climb")!=null)||(mob.fetchAbility("Power_SuperClimb")!=null)))
		{
            if(status!=null)status[0]=Tickable.STATUS_MISC7+18;
			Ability A=mob.fetchAbility("Skill_Climb");
			if(A==null )A=mob.fetchAbility("Power_SuperClimb");
			Vector V=new Vector();
			V.add(Directions.getDirectionName(direction));
			if(A.profficiency()<50)	A.setProfficiency(CMLib.dice().roll(1,50,A.adjustedLevel(mob,0)*15));
			CharState oldState=(CharState)mob.curState().copyOf();
			A.invoke(mob,V,null,false,0);
			mob.curState().setMana(oldState.getMana());
			mob.curState().setMovement(oldState.getMovement());
		}
		else
		if(mob.fetchAbility("Thief_Sneak")!=null)
		{
            if(status!=null)status[0]=Tickable.STATUS_MISC7+19;
			Ability A=mob.fetchAbility("Thief_Sneak");
			Vector V=new Vector();
			V.add(Directions.getDirectionName(direction));
			if(A.profficiency()<50)
			{
				A.setProfficiency(CMLib.dice().roll(1,50,A.adjustedLevel(mob,0)*15));
				Ability A2=mob.fetchAbility("Thief_Hide");
				if(A2!=null)
					A2.setProfficiency(CMLib.dice().roll(1,50,A.adjustedLevel(mob,0)*15));
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
				mob.doCommand(CMParms.parse("CLOSE "+Directions.getDirectionName(opDirection)));
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
			beMobile(M,true,true,false,false,null,null);
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
		    dir=CMLib.dice().roll(1,Directions.NUM_DIRECTIONS,-1);
		    Room R=toHere.getRoomInDir(dir);
		    if(R!=null)
		    {
		        if(((R.domainType()==Room.DOMAIN_INDOORS_AIR)&&(!CMLib.flags().isFlying(M)))
		        ||((R.domainType()==Room.DOMAIN_OUTDOORS_AIR)&&(!CMLib.flags().isFlying(M)))
		        ||((R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)&&(!CMLib.flags().isSwimming(M)))
		        ||((R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)&&(!CMLib.flags().isSwimming(M)))
		        ||((R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)&&(!CMLib.flags().isSwimming(M)))
		        ||((R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)&&(!CMLib.flags().isSwimming(M))))
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
	public boolean move(MOB mob, int directionCode, boolean flee, boolean nolook)
	{
		return move(mob,directionCode,flee,nolook,false);
	}

	public int findExitDir(MOB mob, Room R, String desc)
	{
		int dir=Directions.getGoodDirectionCode(desc);
		if(dir<0)
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Exit e=R.getExitInDir(d);
			Room r=R.getRoomInDir(d);
			if((e!=null)&&(r!=null))
			{
				if((CMLib.flags().canBeSeenBy(e,mob))
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
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		    if(R2.getRoomInDir(d)==R)
		        return d;
		return dir;
	}
    
}
