package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

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
public class StdThinInstance extends StdThinArea
{
	public String ID(){	return "StdThinInstance";}
	private long flags=Area.FLAG_THIN|Area.FLAG_INSTANCE_PARENT;
	public long flags(){return flags;}
	
	private DVector children = new DVector(2);
	private volatile int instanceCounter=0;
	private static final long CHILD_CHECK_INTERVAL=TimeClock.TIME_MILIS_PER_MUDHOUR/TimeClock.TIME_TICK ;
	private long childCheckDown=CHILD_CHECK_INTERVAL;
	
	protected String getStrippedRoomID(String roomID)
	{
		int x=roomID.indexOf("#");
		if(x<0) return null;
		return roomID.substring(x);
	}
	
	protected String convertToMyArea(String roomID)
	{
		String strippedID=getStrippedRoomID(roomID);
		if(strippedID==null) return null;
		return Name()+strippedID;
	}
	
	protected Area getParentArea()
	{
		int x=Name().indexOf("_");
		if(x<0) return null;
		if(!CMath.isNumber(Name().substring(0,x))) return null;
		Area parentA = CMLib.map().getArea(Name().substring(x+1));
		if((parentA==null)
		||(!CMath.bset(parentA.flags(),Area.FLAG_INSTANCE_PARENT))
		||(CMath.bset(parentA.flags(),Area.FLAG_INSTANCE_CHILD)))
			return null;
		return parentA;
	}
	
	public Room getRoom(String roomID)
    {
    	if(!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
    		return super.getRoom(roomID);
    	
    	if(!isRoom(roomID)) return null;
    	Room R=super.getRoom(roomID);
    	if(((R==null)||(R.amDestroyed()))&&(roomID!=null))
    	{
    		Area parentA=getParentArea();
    		if(parentA==null) return null;
    		
    		if(roomID.toUpperCase().startsWith(Name().toUpperCase()+"#"))
	    		roomID=Name()+roomID.substring(Name().length()); // for case sensitive situations
    		R=parentA.getRoom(parentA.Name()+getStrippedRoomID(roomID));
    		if(R==null) return null;
    		
    		Room origRoom=R;
    		R=(Room)R.copyOf();
			R.clearSky();
			if(R instanceof GridLocale)
				((GridLocale)R).clearGrid(null);
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				R.rawDoors()[d]=null;
    		R.setRoomID(roomID);
			R.setArea(this);
			addProperRoom(R);
			
			synchronized(("SYNC"+roomID).intern())
			{
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					Room dirR=origRoom.rawDoors()[d];
					if(dirR!=null)
					{
						String myRID=dirR.roomID();
						if(dirR.getArea()==parentA)
						{
							String localDirRID=convertToMyArea(myRID);
							Room localDirR=getProperRoom(localDirRID);
							if(localDirR!=null)
								R.rawDoors()[d]=localDirR;
							else
							{
	            				R.rawDoors()[d]=CMClass.getLocale("ThinRoom");
	            				R.rawDoors()[d].setRoomID(localDirRID);
	            				R.rawDoors()[d].setArea(this);
							}
						}
						else
							R.rawDoors()[d]=dirR;
					}
				}
			}
			fillInAreaRoom(R);
			R.setExpirationDate(System.currentTimeMillis()+WorldMap.ROOM_EXPIRATION_MILLIS);
    	}
    	return R;
    }
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
			return true;
		if((--childCheckDown)<=0)
		{
			childCheckDown=CHILD_CHECK_INTERVAL;
	    	synchronized(children)
	    	{
        		for(int i=children.size()-1;i>=0;i--) 
        		{
        			StdThinInstance childA=(StdThinInstance)children.elementAt(i,2);
        			if(childA.getAreaState() > Area.STATE_ACTIVE)
        			{
        				Vector V=(Vector)children.elementAt(i,3);
        				boolean anyInside=false;
        				for(int v=0;v<V.size();v++)
        				{
        					MOB M=(MOB)V.elementAt(v);
        					if(CMLib.flags().isInTheGame(M,true)
        					&&(M.location()!=null)
        					&&(M.location().getArea()==childA))
        						anyInside=true;
        				}
        				if(!anyInside)
        				{
        					children.removeElementsAt(i);
            				for(int v=0;v<V.size();v++)
            				{
            					MOB M=(MOB)V.elementAt(v);
            					if((M.location()!=null)
            					&&(M.location().getArea()==this))
            						M.setLocation(M.getStartRoom());
            				}
        					MOB mob=CMClass.sampleMOB();
        					for(Enumeration e=childA.getProperMap();e.hasMoreElements();)
        					{
        						Room R=(Room)e.nextElement();
        				    	R.executeMsg(mob,CMClass.getMsg(mob,R,null,CMMsg.MSG_EXPIRE,null));
        					}
        					CMLib.map().delArea(childA);
        					childA.destroy();
        				}
        			}
        		}
	    	}
		}
		return true;
	}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
			return true;
    	setAreaState(Area.STATE_PASSIVE);
        if((msg.sourceMinor()==CMMsg.TYP_ENTER)
        &&(msg.target() instanceof Room)
        &&(CMath.bset(flags(),Area.FLAG_INSTANCE_PARENT))
        &&(isRoom((Room)msg.target()))
        &&(!CMSecurity.isAllowed(msg.source(),(Room)msg.target(),"CMDAREAS"))
        &&(((msg.source().getStartRoom()==null)||(msg.source().getStartRoom().getArea()!=this))))
        {
        	synchronized(children)
        	{
        		int myDex=-1;
        		for(int i=0;i<children.size();i++) {
        			Vector V=(Vector)children.elementAt(i,1);
        			if(V.contains(msg.source())){  myDex=i; break;}
        		}
            	HashSet grp = msg.source().getGroupMembers(new HashSet());
        		for(int i=0;i<children.size();i++) {
        			if(i!=myDex)
        			{
            			Vector V=(Vector)children.elementAt(i,1);
	        			for(int v=V.size()-1;v>=0;v--)
	        			{
	        				MOB M=(MOB)V.elementAt(v);
	        				if(grp.contains(M))
		        			{
		        				if(myDex<0)
		        				{
		        					myDex=i;
		        					break;
		        				}
		        				else
		        				if((CMLib.flags().isInTheGame(M,true))
	        					&&(M.location().getArea()!=(Area)children.elementAt(i,2)))
		        				{
		        					V.remove(M);
		        					((Vector)children.elementAt(myDex,1)).addElement(M);
		        				}
		        			}
	        			}
        			}
        		}
        		StdThinInstance redirectA = null;
        		if(myDex<0)
        		{
        			StdThinInstance newA=(StdThinInstance)this.copyOf();
        			newA.properRooms=new Vector<Room>(1);
        			newA.properRoomIDSet = null;
        			newA.metroRoomIDSet = null;
        		    newA.blurbFlags=new Vector(1);
        			newA.setName((++instanceCounter)+"_"+Name());
        			newA.flags |= Area.FLAG_INSTANCE_CHILD;
        			for(Enumeration e=getProperRoomnumbers().getRoomIDs();e.hasMoreElements();)
        				newA.addProperRoomnumber(newA.convertToMyArea((String)e.nextElement()));
        			redirectA=newA;
        			CMLib.map().addArea(newA);
        			newA.setAreaState(Area.STATE_ACTIVE); // starts ticking
        			children.addElement(CMParms.makeVector(msg.source()),redirectA);
        		}
        		else
        			redirectA=(StdThinInstance)children.elementAt(myDex,2);
        		Room R=redirectA.getRoom(redirectA.convertToMyArea(CMLib.map().getExtendedRoomID((Room)msg.target())));
        		if(R!=null) msg.setTarget(R);
        	}
        }
        return true;
	}
}
