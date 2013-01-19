package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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

import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2000-2012 Bo Zimmerman

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
public class StdThinInstance extends StdThinArea
{
	public String ID(){    return "StdThinInstance";}
	
	private long flags=Area.FLAG_THIN|Area.FLAG_INSTANCE_PARENT;
	public long flags(){return flags;}
	
	private SVector<AreaInstanceChild> instanceChildren = new SVector<AreaInstanceChild>();
	private volatile int instanceCounter=0;
	private long childCheckDown=CMProps.getMillisPerMudHour()/CMProps.getTickMillis();
	private WeakReference<Area> parentArea = null;
	
	protected String getStrippedRoomID(String roomID)
	{
		int x=roomID.indexOf('#');
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
		if((parentArea!=null)&&(parentArea.get()!=null))
			return parentArea.get();
		int x=Name().indexOf('_');
		if(x<0) return null;
		if(!CMath.isNumber(Name().substring(0,x))) return null;
		Area parentA = CMLib.map().getArea(Name().substring(x+1));
		if((parentA==null)
		||(!CMath.bset(parentA.flags(),Area.FLAG_INSTANCE_PARENT))
		||(CMath.bset(parentA.flags(),Area.FLAG_INSTANCE_CHILD)))
			return null;
		parentArea=new WeakReference<Area>(parentA);
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
			R=CMLib.database().DBReadRoomObject(R.roomID(), false);
			TreeMap<String,Room> V=new TreeMap<String,Room>();
			V.put(roomID,R);
			CMLib.database().DBReadRoomExits(R.roomID(), R, false);
			CMLib.database().DBReadContent(R.roomID(), R, true);
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
						if((myRID!=null)&&(myRID.length()>0)&&(dirR.getArea()==parentA))
						{
							String localDirRID=convertToMyArea(myRID);
							Room localDirR=getProperRoom(localDirRID);
							if(localDirR!=null)
								R.rawDoors()[d]=localDirR;
							else
							if(localDirRID==null)
								Log.errOut("StdThinInstance","Error in linked room ID "+origRoom.roomID()+", dir="+d);
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
			for(Enumeration<MOB> e=R.inhabitants();e.hasMoreElements();)
				e.nextElement().bringToLife(R,true);
			R.startItemRejuv();
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
			childCheckDown=CMProps.getMillisPerMudHour()/CMProps.getTickMillis();
			synchronized(instanceChildren)
			{
				for(int i=instanceChildren.size()-1;i>=0;i--) 
				{
					Area childA=instanceChildren.elementAt(i).A;
					if(childA.getAreaState() != Area.State.ACTIVE)
					{
						List<WeakReference<MOB>> V=instanceChildren.elementAt(i).mobs;
						boolean anyInside=false;
						for(WeakReference<MOB> wmob : V)
						{
							MOB M=wmob.get();
							if((M!=null)
							&&CMLib.flags().isInTheGame(M,true)
							&&(M.location()!=null)
							&&(M.location().getArea()==childA))
							{
								anyInside=true;
								break;
							}
						}
						if(!anyInside)
						{
							instanceChildren.remove(i);
							for(WeakReference<MOB> wmob : V)
							{
								MOB M=wmob.get();
								if((M!=null)
								&&(M.location()!=null)
								&&(M.location().getArea()==this))
									M.setLocation(M.getStartRoom());
							}
							MOB mob=CMClass.sampleMOB();
							for(Enumeration<Room> e=childA.getProperMap();e.hasMoreElements();)
							{
								Room R=e.nextElement();
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
	
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
			return true;
		setAreaState(Area.State.PASSIVE);
		if((msg.sourceMinor()==CMMsg.TYP_ENTER)
		&&(msg.target() instanceof Room)
		&&(CMath.bset(flags(),Area.FLAG_INSTANCE_PARENT))
		&&(isRoom((Room)msg.target()))
		&&(!CMSecurity.isAllowed(msg.source(),(Room)msg.target(),CMSecurity.SecFlag.CMDAREAS))
		&&(((msg.source().getStartRoom()==null)||(msg.source().getStartRoom().getArea()!=this))))
		{
			synchronized(instanceChildren)
			{
				int myDex=-1;
				for(int i=0;i<instanceChildren.size();i++) 
				{
					List<WeakReference<MOB>> V=instanceChildren.elementAt(i).mobs;
					for(Iterator<WeakReference<MOB>> vi = V.iterator();vi.hasNext();)
					if(msg.source() == vi.next().get())
					{  
						myDex=i; break;
					}
				}
				Set<MOB> grp = msg.source().getGroupMembers(new HashSet<MOB>());
				for(int i=0;i<instanceChildren.size();i++) {
					if(i!=myDex)
					{
						List<WeakReference<MOB>> V=instanceChildren.elementAt(i).mobs;
						for(int v=V.size()-1;v>=0;v--)
						{
							final WeakReference<MOB> wmob=V.get(v);
							if(wmob==null) continue;
							final MOB M=wmob.get();
							if(grp.contains(M))
							{
								if(myDex<0)
								{
									myDex=i;
									break;
								}
								else
								if((CMLib.flags().isInTheGame(M,true))
								&&(M.location().getArea()!=instanceChildren.elementAt(i).A))
								{
									V.remove(M);
									instanceChildren.get(myDex).mobs.add(new WeakReference<MOB>(M));
								}
							}
						}
					}
				}
				Area redirectA = null;
				if(myDex<0)
				{
					final StdThinInstance newA=(StdThinInstance)this.copyOf();
					newA.properRooms=new STreeMap<String, Room>(new Area.RoomIDComparator());
					newA.parentArea=null;
					newA.properRoomIDSet = null;
					newA.metroRoomIDSet = null;
					newA.blurbFlags=new STreeMap<String,String>();
					newA.setName((++instanceCounter)+"_"+Name());
					newA.flags |= Area.FLAG_INSTANCE_CHILD;
					for(final Enumeration<String> e=getProperRoomnumbers().getRoomIDs();e.hasMoreElements();)
						newA.addProperRoomnumber(newA.convertToMyArea(e.nextElement()));
					redirectA=newA;
					CMLib.map().addArea(newA);
					newA.setAreaState(Area.State.ACTIVE); // starts ticking
					final List<WeakReference<MOB>> newMobList = new SVector<WeakReference<MOB>>(5);
					newMobList.add(new WeakReference<MOB>(msg.source()));
					final AreaInstanceChild child = new AreaInstanceChild(redirectA,newMobList); 
					instanceChildren.add(child);
				}
				else
					redirectA=instanceChildren.get(myDex).A;
				if(redirectA instanceof StdThinInstance)
				{
					Room R=redirectA.getRoom(((StdThinInstance)redirectA).convertToMyArea(CMLib.map().getExtendedRoomID((Room)msg.target())));
					if(R!=null) msg.setTarget(R);
				}
			}
		}
		return true;
	}
}
