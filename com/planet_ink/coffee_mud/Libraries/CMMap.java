package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject.BoundedCube;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
/*
   Copyright 2000-2013 Bo Zimmerman

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
public class CMMap extends StdLibrary implements WorldMap
{
	public String ID(){return "CMMap";}
	public final int			QUADRANT_WIDTH   		= 10;
	public static MOB   		deityStandIn	 		= null;
	public long 				lastVReset  	 		= 0;
	public List<Area>   		areasList   	 		= new SVector<Area>();
	public List<Area>   		sortedAreas 	 		= null;
	public List<Deity>  		deitiesList 	 		= new SVector<Deity>();
	public List<PostOffice> 	postOfficeList   		= new SVector<PostOffice>();
	public List<Auctioneer> 	auctionHouseList 		= new SVector<Auctioneer>();
	public List<Banker> 		bankList		 		= new SVector<Banker>();
	public RTree<SpaceObject>	space		 			= new RTree<SpaceObject>();
	protected Map<String,Object>SCRIPT_HOST_SEMAPHORES	= new Hashtable<String,Object>();
	
	public Map<Integer,List<WeakReference<MsgListener>>> 
								globalHandlers   		= new SHashtable<Integer,List<WeakReference<MsgListener>>>();
	public Map<String,SLinkedList<LocatedPair>>
								scriptHostMap			= new STreeMap<String,SLinkedList<LocatedPair>>();

	private static final long EXPIRE_1MIN	= 1*60*1000;
	private static final long EXPIRE_5MINS	= 5*60*1000;
	private static final long EXPIRE_10MINS	= 10*60*1000;
	private static final long EXPIRE_20MINS	= 20*60*1000;
	private static final long EXPIRE_30MINS	= 30*60*1000;
	private static final long EXPIRE_1HOUR	= 60*60*1000;
	
	protected int getGlobalIndex(List<Environmental> list, String name)
	{
		if(list.size()==0) return -1;
		int start=0;
		int end=list.size()-1;
		while(start<=end)
		{
			int mid=(end+start)/2;
			int comp=list.get(mid).Name().compareToIgnoreCase(name);
			if(comp==0)
				return mid;
			else
			if(comp>0)
				end=mid-1;
			else
				start=mid+1;

		}
		return -1;
	}

	// areas
	public int numAreas() { return areasList.size(); }
	public void addArea(Area newOne)
	{
		sortedAreas=null;
		areasList.add(newOne);
	}

	public void delArea(Area oneToDel)
	{
		sortedAreas=null;
		areasList.remove(oneToDel);
		Resources.removeResource("SYSTEM_AREA_FINDER_CACHE");
	}

	public Enumeration<Area> sortedAreas()
	{
		if(sortedAreas==null)
		{
			Vector<Area> V=new Vector<Area>();
			Area A=null;
			for(Enumeration<Area> e=areas();e.hasMoreElements();)
			{
				A=e.nextElement();
				String upperName=A.Name().toUpperCase();
				for(int v=0;v<=V.size();v++)
					if(v==V.size())
					{ V.addElement(A); break;}
					else
					if(upperName.compareTo(V.elementAt(v).Name().toUpperCase())<=0)
					{ V.insertElementAt(A,v); break;}
			}
			sortedAreas=V;
		}
		return (sortedAreas==null)?sortedAreas():new IteratorEnumeration<Area>(sortedAreas.iterator());
	}

	public Area getArea(String calledThis)
	{
		final boolean disableCaching=CMProps.getBoolVar(CMProps.Bool.MAPFINDSNOCACHE);
		final Map<String,Area> finder=getAreaFinder();
		Area A=finder.get(calledThis.toLowerCase());
		if((A!=null)&&(!A.amDestroyed()))
			return A;
		for(Enumeration<Area> a=areas();a.hasMoreElements();)
		{
			A=a.nextElement();
			if(A.Name().equalsIgnoreCase(calledThis))
			{
				if(!disableCaching)
					finder.put(calledThis.toLowerCase(), A);
				return A;
			}
		}
		return null;
	}
	public Area findAreaStartsWith(String calledThis)
	{
		final boolean disableCaching=CMProps.getBoolVar(CMProps.Bool.MAPFINDSNOCACHE);
		Area A=getArea(calledThis);
		if(A!=null) return A;
		final Map<String,Area> finder=getAreaFinder();
		A=finder.get(calledThis.toLowerCase());
		if((A!=null)&&(!A.amDestroyed()))
			return A;
		for(Enumeration<Area> a=areas();a.hasMoreElements();)
		{
			A=a.nextElement();
			if(A.Name().toUpperCase().startsWith(calledThis))
			{
				if(!disableCaching)
					finder.put(calledThis.toLowerCase(), A);
				return A;
			}
		}
		return null;
	}

	public Area findArea(String calledThis)
	{
		final boolean disableCaching=CMProps.getBoolVar(CMProps.Bool.MAPFINDSNOCACHE);
		Area A=findAreaStartsWith(calledThis);
		if(A!=null) return A;
		final Map<String,Area> finder=getAreaFinder();
		A=finder.get(calledThis.toLowerCase());
		if((A!=null)&&(!A.amDestroyed()))
			return A;
		for(Enumeration<Area> a=areas();a.hasMoreElements();)
		{
			A=a.nextElement();
			if(CMLib.english().containsString(A.Name(),calledThis))
			{
				if(!disableCaching)
					finder.put(calledThis.toLowerCase(), A);
				return A;
			}
		}
		return null;
	}
	public Enumeration<Area> areas() { return new IteratorEnumeration<Area>(areasList.iterator()); }
	public Enumeration<String> roomIDs(){ return new WorldMap.CompleteRoomIDEnumerator(this);}
	public Area getFirstArea()
	{
		if (areas().hasMoreElements())
			return areas().nextElement();
		return null;
	}
	public Area getDefaultParentArea()
	{
		String defaultParentAreaName=CMProps.getVar(CMProps.Str.DEFAULTPARENTAREA);
		if((defaultParentAreaName!=null)&&(defaultParentAreaName.trim().length()>0))
			return getArea(defaultParentAreaName.trim());
		return null;
	}
	public Area getRandomArea()
	{
		Area A=null;
		while((numAreas()>0)&&(A==null))
		{
			try{
				A=areasList.get(CMLib.dice().roll(1,numAreas(),-1));
			}catch(ArrayIndexOutOfBoundsException e){}
		}
		return A;
	}

	public void addGlobalHandler(MsgListener E, int category)
	{
		if(E==null) return;
		List<WeakReference<MsgListener>> V=globalHandlers.get(Integer.valueOf(category));
		if(V==null)
		{
			V=new SLinkedList<WeakReference<MsgListener>>();
			globalHandlers.put(Integer.valueOf(category),V);
		}
		synchronized(V)
		{
			for(Iterator<WeakReference<MsgListener>> i=V.iterator();i.hasNext();)
			{
				final WeakReference<MsgListener> W=i.next();
				if(W.get()==E)
					return;
			}
			V.add(new WeakReference<MsgListener>(E));
		}
	}

	public void delGlobalHandler(MsgListener E, int category)
	{
		List<WeakReference<MsgListener>> V=globalHandlers.get(Integer.valueOf(category));
		if((E==null)||(V==null)) 
			return;
		synchronized(V)
		{
			for(Iterator<WeakReference<MsgListener>> i=V.iterator();i.hasNext();)
			{
				final WeakReference<MsgListener> W=i.next();
				if(W.get()==E)
					V.remove(W);
			}
		}
	}
	public MOB deity() 
	{
		if(deities().hasMoreElements())
			return deities().nextElement();
		if((deityStandIn==null)
		||(deityStandIn.amDestroyed())
		||(deityStandIn.amDead())
		||(deityStandIn.location()==null)
		||(deityStandIn.location().isInhabitant(deityStandIn)))
		{
			if(deityStandIn!=null) deityStandIn.destroy();
			MOB everywhereMOB=CMClass.getMOB("StdMOB");
			everywhereMOB.setName("god");
			everywhereMOB.setLocation(this.getRandomRoom());
			deityStandIn=everywhereMOB;
		}
		return deityStandIn;
	}
	
	public MOB getFactoryMOBInAnyRoom() 
	{ 
		return getFactoryMOB(this.getRandomRoom());
	}
	
	public MOB getFactoryMOB(Room R)
	{
		MOB everywhereMOB=CMClass.getFactoryMOB();
		everywhereMOB.setName("somebody");
		everywhereMOB.setLocation(R);
		return everywhereMOB;
	}

	public boolean isObjectInSpace(SpaceObject O)
	{
		synchronized(space)
		{
			return space.contains(O);
		}
	}

	public void delObjectInSpace(SpaceObject O)
	{ 
		synchronized(space)
		{
			space.remove(O);
		}
	}

	public void addObjectToSpace(SpaceObject O, long[] coords)
	{
		synchronized(space)
		{
			O.coordinates()[0]=coords[0];
			O.coordinates()[1]=coords[1];
			O.coordinates()[2]=coords[2];
			space.insert(O); // won't accept dups, so is ok
		}
	}

	public long getDistanceFrom(SpaceObject O1, SpaceObject O2)
	{
		return Math.round(Math.sqrt(CMath.mul((O1.coordinates()[0]-O2.coordinates()[0]),(O1.coordinates()[0]-O2.coordinates()[0]))
									+CMath.mul((O1.coordinates()[1]-O2.coordinates()[1]),(O1.coordinates()[1]-O2.coordinates()[1]))
									+CMath.mul((O1.coordinates()[2]-O2.coordinates()[2]),(O1.coordinates()[2]-O2.coordinates()[2]))));
	}

	public double[] getDirectionChange()
	{
		//magnitude=sqrt(oldveloc^2 + newveloc^2);
		return null;
	}
	
	public double[] getDirection(SpaceObject FROM, SpaceObject TO)
	{
		double[] dir=new double[2];
		double x=TO.coordinates()[0]-FROM.coordinates()[0];
		double y=TO.coordinates()[1]-FROM.coordinates()[1];
		double z=TO.coordinates()[2]-FROM.coordinates()[2];
		dir[0]=Math.acos(x/Math.sqrt((x*x)+(y*y)));
		dir[1]=Math.acos(z/Math.sqrt((z*z)+(y*y)));
		return dir;
	}

	protected void moveSpaceObject(SpaceObject O, long x, long y, long z)
	{
		synchronized(space)
		{
			space.remove(O);
			O.coordinates()[0]=x;
			O.coordinates()[1]=y;
			O.coordinates()[2]=z;
			space.insert(O);
		}
	}
	
	public void moveSpaceObject(SpaceObject O, long[] coords)
	{
		moveSpaceObject(O, coords[0], coords[1], coords[2]);
	}
	
	public void moveSpaceObject(SpaceObject O)
	{
		double x1=Math.cos(O.direction()[0])*Math.sin(O.direction()[1]);
		double y1=Math.sin(O.direction()[0])*Math.sin(O.direction()[1]);
		double z1=Math.cos(O.direction()[1]);
		moveSpaceObject(O,O.coordinates()[0]+Math.round(CMath.mul(O.speed(),x1)),
						O.coordinates()[1]+Math.round(CMath.mul(O.speed(),y1)),
						O.coordinates()[2]=O.coordinates()[2]+Math.round(CMath.mul(O.speed(),z1)));
	}

	public long[] getLocation(long[] oldLocation, double[] direction, long distance)
	{
		double x1=Math.cos(direction[0])*Math.sin(direction[1]);
		double y1=Math.sin(direction[0])*Math.sin(direction[1]);
		double z1=Math.cos(direction[1]);
		long[] location=new long[3];
		location[0]=oldLocation[0]+Math.round(CMath.mul(distance,x1));
		location[1]=oldLocation[1]+Math.round(CMath.mul(distance,y1));
		location[2]=oldLocation[2]+Math.round(CMath.mul(distance,z1));
		return location;
	}

	public long getRelativeSpeed(SpaceObject O1, SpaceObject O2)
	{
		return Math.round(Math.sqrt(((O1.speed()*O1.coordinates()[0])-(O2.speed()*O2.coordinates()[0])*(O1.speed()*O1.coordinates()[0])-(O2.speed()*O2.coordinates()[0]))
									+((O1.speed()*O1.coordinates()[1])-(O2.speed()*O2.coordinates()[1])*(O1.speed()*O1.coordinates()[1])-(O2.speed()*O2.coordinates()[1]))
									+((O1.speed()*O1.coordinates()[2])-(O2.speed()*O2.coordinates()[2])*(O1.speed()*O1.coordinates()[2])-(O2.speed()*O2.coordinates()[2]))));
	}

	public SpaceObject getSpaceObject(CMObject o, boolean ignoreMobs)
	{
		if(o instanceof SpaceObject)
			return (SpaceObject)o;
		if(o instanceof Item)
		{
			if(((Item)o).container()!=null)
				return getSpaceObject(((Item)o).container(),ignoreMobs);
			else
				return getSpaceObject(((Item)o).owner(),ignoreMobs);
		}
		if(o instanceof MOB)
			return ignoreMobs?null:getSpaceObject(((MOB)o).location(),false);
		if(o instanceof Room)
			return getSpaceObject(((Room)o).getArea(),ignoreMobs);
		if(o instanceof Area)
			for(Enumeration<Area> a=((Area)o).getParents();a.hasMoreElements();)
			{
				SpaceObject obj=getSpaceObject(a.nextElement(),ignoreMobs);
				if(obj != null)
					return obj;
			}
		return null;
	}

	public List<SpaceObject> getSpaceObjectsWithin(final SpaceObject ofObj, long minDistance, long maxDistance)
	{
		List<SpaceObject> within=new Vector<SpaceObject>(1);
		if(ofObj==null)
			return within;
		synchronized(space)
		{
			space.query(within, new BoundedObject.BoundedCube(ofObj.coordinates(), maxDistance));
		}
		for(Iterator<SpaceObject> i=within.iterator();i.hasNext();)
		{
			SpaceObject o=i.next();
			if(o!=ofObj)
			{
				long dist=getDistanceFrom(o,ofObj);
				if((dist>=minDistance)&&(dist<=maxDistance))
				{
					within.add(o);
				}
			}
		}
		Collections.sort(within, new Comparator<SpaceObject>(){
			@Override public int compare(SpaceObject o1, SpaceObject o2) {
				final long distTo1=getDistanceFrom(o1,ofObj);
				final long distTo2=getDistanceFrom(o2,ofObj);
				if(distTo1==distTo2)
					return 0;
				return distTo1>distTo2?1:-1;
			}
		});
		return within;
	}
	
	public String createNewExit(Room from, Room room, int direction)
	{
		Room opRoom=from.rawDoors()[direction];
		if((opRoom!=null)&&(opRoom.roomID().length()==0))
			opRoom=null;
		Room reverseRoom=null;
		if(opRoom!=null)
			reverseRoom=opRoom.rawDoors()[Directions.getOpDirectionCode(direction)];

		if((reverseRoom!=null)&&(reverseRoom==from))
			return "Opposite room already exists and heads this way.  One-way link created.";

		Exit thisExit=null;
		synchronized(("SYNC"+from.roomID()).intern())
		{
			from=getRoom(from);
			if(opRoom!=null)
				from.rawDoors()[direction]=null;

			from.rawDoors()[direction]=room;
			thisExit=from.getRawExit(direction);
			if(thisExit==null)
			{
				thisExit=CMClass.getExit("StdOpenDoorway");
				from.setRawExit(direction,thisExit);
			}
			CMLib.database().DBUpdateExits(from);
		}
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=getRoom(room);
			if(room.rawDoors()[Directions.getOpDirectionCode(direction)]==null)
			{
				room.rawDoors()[Directions.getOpDirectionCode(direction)]=from;
				room.setRawExit(Directions.getOpDirectionCode(direction),thisExit);
				CMLib.database().DBUpdateExits(room);
			}
		}
		return "";
	}

	public int numRooms()
	{
		int total=0;
		for(Enumeration<Area> e=areas();e.hasMoreElements();)
			total+=e.nextElement().properSize();
		return total;
	}

	public boolean sendGlobalMessage(MOB host, int category, CMMsg msg)
	{
		List<WeakReference<MsgListener>> V=globalHandlers.get(Integer.valueOf(category));
		if(V==null) 
			return true;
		synchronized(V)
		{
			try
			{
				MsgListener O=null;
				Environmental E=null;
				for(WeakReference<MsgListener> W : V)
				{
					O=W.get();
					if(O instanceof Environmental)
					{
						E=(Environmental)O;
						if(!CMLib.flags().isInTheGame(E,true))
						{
							if(!CMLib.flags().isInTheGame(E,false))
								delGlobalHandler(E,category);
						}
						else
						if(!E.okMessage(host,msg))
							return false;
					}
					else
					if(O!=null)
					{
						if(!O.okMessage(host, msg))
							return false;
					}
					else
						V.remove(W);
				}
				for(WeakReference<MsgListener> W : V)
				{
					O=W.get();
					if(O !=null)
						O.executeMsg(host,msg);
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException xx){}
			catch(Exception x){Log.errOut("CMMap",x);}
		}
		return true;
	}

	public String getExtendedRoomID(final Room R)
	{
		if(R==null) return "";
		if(R.roomID().length()>0) return R.roomID();
		final Area A=R.getArea();
		if(A==null) return "";
		final GridLocale GR=R.getGridParent();
		if(GR!=null) return GR.getGridChildCode(R);
		return R.roomID();
	}

	public String getExtendedTwinRoomIDs(final Room R1,final Room R2)
	{
		final String R1s=getExtendedRoomID(R1);
		final String R2s=getExtendedRoomID(R2);
		if(R1s.compareTo(R2s)>0)
			return R1s+"_"+R2s;
		else
			return R2s+"_"+R1s;
	}
	
	public Room getRoom(Enumeration<Room> roomSet, String calledThis)
	{
		try
		{
			if(calledThis==null) return null;
			if(calledThis.endsWith(")"))
			{
				int child=calledThis.lastIndexOf("#(");
				if(child>1)
				{
					Room R=getRoom(roomSet,calledThis.substring(0,child));
					if((R!=null)&&(R instanceof GridLocale))
					{
						R=((GridLocale)R).getGridChild(calledThis);
						if(R!=null) return R;
					}
				}
			}
			Room R=null;
			if(roomSet==null)
			{
				int x=calledThis.indexOf('#');
				if(x>=0)
				{
					Area A=getArea(calledThis.substring(0,x));
					if(A!=null) R=A.getRoom(calledThis);
					if(R!=null) return R;
				}
				for(Enumeration<Area> e=areas();e.hasMoreElements();)
				{
					R = e.nextElement().getRoom(calledThis);
					if(R!=null) return R;
				}
			}
			else
			for(Enumeration<Room> e=roomSet;e.hasMoreElements();)
			{
				R=e.nextElement();
				if(R.roomID().equalsIgnoreCase(calledThis))
					return R;
			}
		}
		catch(java.util.NoSuchElementException x){}
		return null;
	}
	
	public List<Room> findRooms(Enumeration<Room> rooms, MOB mob, String srchStr, boolean displayOnly, int timePct)
	{ 
		final Vector<Room> roomsV=new Vector<Room>();
		if((srchStr.charAt(0)=='#')&&(mob!=null)&&(mob.location()!=null))
			addWorldRoomsLiberally(roomsV,getRoom(mob.location().getArea().Name()+srchStr));
		else
			addWorldRoomsLiberally(roomsV,getRoom(srchStr));
		addWorldRoomsLiberally(roomsV,findRooms(rooms,mob,srchStr,displayOnly,false,timePct));
		return roomsV;
	}
	
	public Room findFirstRoom(Enumeration<Room> rooms, MOB mob, String srchStr, boolean displayOnly, int timePct)
	{ 
		final Vector<Room> roomsV=new Vector<Room>();
		if((srchStr.charAt(0)=='#')&&(mob!=null)&&(mob.location()!=null))
			addWorldRoomsLiberally(roomsV,getRoom(mob.location().getArea().Name()+srchStr));
		else
			addWorldRoomsLiberally(roomsV,getRoom(srchStr));
		if(roomsV.size()>0) return roomsV.firstElement();
		addWorldRoomsLiberally(roomsV,findRooms(rooms,mob,srchStr,displayOnly,true,timePct));
		if(roomsV.size()>0) return roomsV.firstElement();
		return null;
	}
	
	public List<Room> findRooms(Enumeration<Room> rooms, MOB mob, String srchStr, boolean displayOnly, boolean returnFirst, int timePct)
	{
		final List<Room> foundRooms=new Vector<Room>();
		Vector<Room> completeRooms=null;
		try 
		{ 
			completeRooms=new XVector<Room>(rooms); 
		}
		catch(Exception nse)
		{
			Log.errOut("CMMap",nse);
			completeRooms=new Vector<Room>();	
		}
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		
		Enumeration<Room> enumSet;
		enumSet=completeRooms.elements();
		while(enumSet.hasMoreElements())
		{
			findRoomsByDisplay(mob,enumSet,foundRooms,srchStr,returnFirst,delay);
			if((returnFirst)&&(foundRooms.size()>0)) return foundRooms;
			if(enumSet.hasMoreElements()) try{Thread.sleep(1000 - delay);}catch(Exception e){}
		}
		if(!displayOnly)
		{
			enumSet=completeRooms.elements();
			while(enumSet.hasMoreElements())
			{
				findRoomsByDesc(mob,enumSet,foundRooms,srchStr,returnFirst,delay);
				if((returnFirst)&&(foundRooms.size()>0)) return foundRooms;
				if(enumSet.hasMoreElements()) try{Thread.sleep(1000 - delay);}catch(Exception e){}
			}
		}
		return foundRooms;
	}
	
	protected void findRoomsByDisplay(MOB mob, Enumeration<Room> rooms, List<Room> foundRooms, String srchStr, boolean returnFirst, long maxTime)
	{
		long startTime=System.currentTimeMillis();
		try
		{
			srchStr=srchStr.toUpperCase();
			final boolean useTimer=maxTime>1;
			Room room;
			for(;rooms.hasMoreElements();)
			{
				room=rooms.nextElement();
				if((CMLib.english().containsString(CMStrings.removeColors(room.displayText(mob)),srchStr))
				&&((mob==null)||CMLib.flags().canAccess(mob,room)))
					foundRooms.add(room);
				if((useTimer)&&((System.currentTimeMillis()-startTime)>maxTime))
					return;
			}
		}catch(NoSuchElementException nse){}
	}

	protected void findRoomsByDesc(MOB mob, Enumeration<Room> rooms, List<Room> foundRooms, String srchStr, boolean returnFirst, long maxTime)
	{
		long startTime=System.currentTimeMillis();
		try
		{
			srchStr=srchStr.toUpperCase();
			boolean useTimer=maxTime>1;
			for(;rooms.hasMoreElements();)
			{
				Room room=rooms.nextElement();
				if((CMLib.english().containsString(CMStrings.removeColors(room.description()),srchStr))
				&&((mob==null)||CMLib.flags().canAccess(mob,room)))
					foundRooms.add(room);
				if((useTimer)&&((System.currentTimeMillis()-startTime)>maxTime))
					return;
			}
		}catch(NoSuchElementException nse){}
	}

	public List<MOB> findInhabitants(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct)
	{ return findInhabitants(rooms,mob,srchStr,false,timePct);}
	public MOB findFirstInhabitant(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct)
	{ 
		final List<MOB> found=findInhabitants(rooms,mob,srchStr,true,timePct);
		if(found.size()>0) return found.get(0);
		return null;
	}
	public List<MOB> findInhabitants(Enumeration<Room> rooms, MOB mob, String srchStr, boolean returnFirst, int timePct)
	{
		final Vector<MOB> found=new Vector<MOB>();
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		if(delay>1000) delay=1000;
		final boolean useTimer = delay>1;
		final boolean allRoomsAllowed=(mob==null);
		long startTime=System.currentTimeMillis();
		Room room;
		for(;rooms.hasMoreElements();)
		{
			room=rooms.nextElement();
			if((room != null) && (allRoomsAllowed || CMLib.flags().canAccess(mob,room)))
			{
				found.addAll(room.fetchInhabitants(srchStr));
				if((returnFirst)&&(found.size()>0)) return found;
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>delay)) 
				try{Thread.sleep(1000 - delay); startTime=System.currentTimeMillis();}catch(Exception e){}
		}
		return found;
	}
	
	public List<Item> findInventory(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct)
	{ return findInventory(rooms,mob,srchStr,false,timePct);}
	public Item findFirstInventory(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct)
	{ 
		final List<Item> found=findInventory(rooms,mob,srchStr,true,timePct);
		if(found.size()>0) return found.get(0);
		return null;
	}
	public List<Item> findInventory(Enumeration<Room> rooms, MOB mob, String srchStr, boolean returnFirst, int timePct)
	{
		final List<Item> found=new Vector<Item>();
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		if(delay>1000) delay=1000;
		final boolean useTimer = delay>1;
		long startTime=System.currentTimeMillis();
		MOB M;
		Room room;
		if(rooms==null)
		{
			for(Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
			{
				M=e.nextElement();
				if(M!=null)
					found.addAll(M.findItems(srchStr));
				if((returnFirst)&&(found.size()>0)) return found;
			}
		}
		else
		for(;rooms.hasMoreElements();)
		{
			room=rooms.nextElement();
			if((room != null) && ((mob==null)||CMLib.flags().canAccess(mob,room)))
			{
				for(int m=0;m<room.numInhabitants();m++)
				{
					M=room.fetchInhabitant(m);
					if(M!=null)
						found.addAll(M.findItems(srchStr));
				}
				if((returnFirst)&&(found.size()>0)) return found;
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>delay)) 
				try{Thread.sleep(1000 - delay); startTime=System.currentTimeMillis();}catch(Exception e){}
		}
		return found;
	}
	public List<Environmental> findShopStock(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct)
	{ return findShopStock(rooms,mob,srchStr,false,false,timePct);}
	public Environmental findFirstShopStock(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct)
	{ 
		final List<Environmental> found=findShopStock(rooms,mob,srchStr,true,false,timePct);
		if(found.size()>0) return found.get(0);
		return null;
	}
	public List<Environmental> findShopStockers(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct)
	{ return findShopStock(rooms,mob,srchStr,false,true,timePct);}
	public Environmental findFirstShopStocker(Enumeration<Room> rooms, MOB mob, String srchStr, int timePct)
	{ 
		final List<Environmental> found=findShopStock(rooms,mob,srchStr,true,true,timePct);
		if(found.size()>0) return found.get(0);
		return null;
	}
	public List<Environmental> findShopStock(Enumeration<Room> rooms, MOB mob, String srchStr, boolean returnFirst, boolean returnStockers, int timePct)
	{
		final XVector<Environmental> found=new XVector<Environmental>();
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		if(delay>1000) delay=1000;
		final boolean useTimer = delay>1;
		long startTime=System.currentTimeMillis();
		MOB M=null;
		Item I=null;
		final HashSet<ShopKeeper> stocks=new HashSet<ShopKeeper>(1);
		final HashSet<Area> areas=new HashSet<Area>();
		ShopKeeper SK=null;
		final boolean allRoomsAllowed=(mob==null);
		if(rooms==null)
		{
			for(Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
			{
				M=e.nextElement();
				if(M!=null)
				{
					SK=CMLib.coffeeShops().getShopKeeper(M);
					if((SK!=null)&&(!stocks.contains(SK)))
					{
						stocks.add(SK);
						Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
						if(ei.hasNext()) 
						{
							if(returnFirst)
								return (returnStockers)?new XVector<Environmental>(M):new XVector<Environmental>(ei);
							if(returnStockers)
								found.add(M);
							else
								found.addAll(ei);
						}
					}
					for(int i=0;i<M.numItems();i++)
					{
						I=M.getItem(i);
						if(I!=null)
						{
							SK=CMLib.coffeeShops().getShopKeeper(I);
							if((SK!=null)&&(!stocks.contains(SK))){
								stocks.add(SK);
								Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
								if(ei.hasNext()) 
								{
									if(returnFirst)
										return (returnStockers)?new XVector<Environmental>(I):new XVector<Environmental>(ei);
									if(returnStockers)
										found.add(I);
									else
										found.addAll(ei);
								}
							}
						}
					}
				}
				if((useTimer)&&((System.currentTimeMillis()-startTime)>delay)) 
					try{Thread.sleep(1000 - delay); startTime=System.currentTimeMillis();}catch(Exception ex){}
			}
		}
		else
		for(;rooms.hasMoreElements();)
		{
			Room room=rooms.nextElement();
			if((room != null) && (allRoomsAllowed||CMLib.flags().canAccess(mob,room)))
			{
				if(!areas.contains(room.getArea()))
					areas.add(room.getArea());
				SK=CMLib.coffeeShops().getShopKeeper(room);
				if((SK!=null)&&(!stocks.contains(SK))) {
					stocks.add(SK);
					Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
					if(ei.hasNext()) 
					{
						if(returnFirst)
							return (returnStockers)?new XVector<Environmental>(room):new XVector<Environmental>(ei);
						if(returnStockers)
							found.add(room);
						else
							found.addAll(ei);
					}
				}
				for(int m=0;m<room.numInhabitants();m++)
				{
					M=room.fetchInhabitant(m);
					if(M!=null)
					{
						SK=CMLib.coffeeShops().getShopKeeper(M);
						if((SK!=null)&&(!stocks.contains(SK))){
							stocks.add(SK);
							Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
							if(ei.hasNext()) 
							{
								if(returnFirst)
									return (returnStockers)?new XVector<Environmental>(M):new XVector<Environmental>(ei);
								if(returnStockers)
									found.add(M);
								else
									found.addAll(ei);
							}
						}
					}
				}
				for(int i=0;i<room.numItems();i++)
				{
					I=room.getItem(i);
					if(I!=null)
					{
						SK=CMLib.coffeeShops().getShopKeeper(I);
						if((SK!=null)&&(!stocks.contains(SK))){
							stocks.add(SK);
							Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
							if(ei.hasNext()) 
							{
								if(returnFirst)
									return (returnStockers)?new XVector<Environmental>(I):new XVector<Environmental>(ei);
								if(returnStockers)
									found.add(I);
								else
									found.addAll(ei);
							}
						}
					}
				}
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>delay)) 
				try{Thread.sleep(1000 - delay); startTime=System.currentTimeMillis();}catch(Exception e){}
		}
		for(Iterator<Area> i=areas.iterator();i.hasNext();)
		{
			Area A=i.next();
			SK=CMLib.coffeeShops().getShopKeeper(A);
			if((SK!=null)&&(!stocks.contains(SK)))
			{
				stocks.add(SK);
				Iterator<Environmental> ei=SK.getShop().getStoreInventory(srchStr);
				if(ei.hasNext()) 
				{
					if(returnFirst)
						return (returnStockers)?new XVector<Environmental>(A):new XVector<Environmental>(ei);
					if(returnStockers)
						found.add(A);
					else
						found.addAll(ei);
				}
			}
		}
		return found;
	}
	
	public List<Item> findRoomItems(Enumeration<Room> rooms, MOB mob, String srchStr, boolean anyItems, int timePct)
	{ return findRoomItems(rooms,mob,srchStr,anyItems,false,timePct);}
	public Item findFirstRoomItem(Enumeration<Room> rooms, MOB mob, String srchStr, boolean anyItems, int timePct)
	{ 
		List<Item> found=findRoomItems(rooms,mob,srchStr,anyItems,true,timePct);
		if(found.size()>0) return found.get(0);
		return null;
	}
	public List<Item> findRoomItems(Enumeration<Room> rooms, MOB mob, String srchStr, boolean anyItems, boolean returnFirst, int timePct)
	{
		final Vector<Item> found=new Vector<Item>();
		long delay=Math.round(CMath.s_pct(timePct+"%") * 1000);
		if(delay>1000) delay=1000;
		final boolean useTimer = delay>1;
		long startTime=System.currentTimeMillis();
		final boolean allRoomsAllowed=(mob==null);
		Room room;
		for(;rooms.hasMoreElements();)
		{
			room=rooms.nextElement();
			if((room != null) && (allRoomsAllowed||CMLib.flags().canAccess(mob,room)))
			{
				found.addAll(anyItems?room.findItems(srchStr):room.findItems(null,srchStr));
				if((returnFirst)&&(found.size()>0)) return found;
			}
			if((useTimer)&&((System.currentTimeMillis()-startTime)>delay)) 
				try{Thread.sleep(1000 - delay); startTime=System.currentTimeMillis();}catch(Exception e){}
		}
		return found;
	}
	
	public Room getRoom(Room room)
	{
		if(room==null)
			return null;
		if(room.amDestroyed())
			return getRoom(getExtendedRoomID(room));
		return room;
	}

	public Room getRoom(String calledThis){ return getRoom(null,calledThis); }
	public Enumeration<Room> rooms(){ return new AreaEnumerator(false); }
	public Enumeration<Room> roomsFilled(){ return new AreaEnumerator(true); }
	public Room getRandomRoom()
	{
		Room R=null;
		int numRooms=-1;
		while((R==null)&&((numRooms=numRooms())>0))
		{
			try
			{
				int which=CMLib.dice().roll(1,numRooms,-1);
				int total=0;
				for(Enumeration<Area> e=areas();e.hasMoreElements();)
				{
					Area A=e.nextElement();
					if(which<(total+A.properSize()))
					{ R=A.getRandomProperRoom(); break;}
					total+=A.properSize();
				}
			}catch(NoSuchElementException e){}
		}
		return R;
	}

	public int numDeities() { return deitiesList.size(); }
	
	protected void addDeity(Deity newOne)
	{
		if (!deitiesList.contains(newOne))
			deitiesList.add(newOne);
	}
	
	protected void delDeity(Deity oneToDel)
	{
		deitiesList.remove(oneToDel);
	}
	
	public Deity getDeity(String calledThis)
	{
		for (Deity D : deitiesList)
			if (D.Name().equalsIgnoreCase(calledThis))
				return D;
		return null;
	}
	public Enumeration<Deity> deities() { return new IteratorEnumeration<Deity>(deitiesList.iterator()); }

	public int numPostOffices() { return postOfficeList.size(); }
	protected void addPostOffice(PostOffice newOne)
	{
		if(!postOfficeList.contains(newOne))
			postOfficeList.add(newOne);
	}
	protected void delPostOffice(PostOffice oneToDel)
	{
		postOfficeList.remove(oneToDel);
	}
	public PostOffice getPostOffice(String chain, String areaNameOrBranch)
	{
		for (PostOffice P : postOfficeList)
			if((P.postalChain().equalsIgnoreCase(chain))
			&&(P.postalBranch().equalsIgnoreCase(areaNameOrBranch)))
				return P;
		
		Area A=findArea(areaNameOrBranch);
		if(A==null) 
			return null;
		
		for (PostOffice P : postOfficeList)
			if((P.postalChain().equalsIgnoreCase(chain))
			&&(getStartArea(P)==A))
				return P;
		return null;
	}
	public Enumeration<PostOffice> postOffices() { return new IteratorEnumeration<PostOffice>(postOfficeList.iterator()); }

	public Enumeration<Auctioneer> auctionHouses() { return new IteratorEnumeration<Auctioneer>(auctionHouseList.iterator()); }
	
	public int numAuctionHouses() { return auctionHouseList.size(); }
	
	protected void addAuctionHouse(Auctioneer newOne)
	{
		if (!auctionHouseList.contains(newOne))
		{
			auctionHouseList.add(newOne);
		}
	}
	protected void delAuctionHouse(Auctioneer oneToDel)
	{
		auctionHouseList.remove(oneToDel);
	}
	public Auctioneer getAuctionHouse(String chain, String areaNameOrBranch)
	{
		for (Auctioneer C : auctionHouseList)
			if((C.auctionHouse().equalsIgnoreCase(chain))
			&&(C.auctionHouse().equalsIgnoreCase(areaNameOrBranch)))
				return C;
		
		Area A=findArea(areaNameOrBranch);
		if(A==null) return null;
		
		for (Auctioneer C : auctionHouseList)
			if((C.auctionHouse().equalsIgnoreCase(chain))
			&&(getStartArea(C)==A))
				return C;
		
		return null;
	}

	public int numBanks() { return bankList.size(); }
	protected void addBank(Banker newOne)
	{
		if (!bankList.contains(newOne))
			bankList.add(newOne);
	}
	protected void delBank(Banker oneToDel)
	{
		bankList.remove(oneToDel);
	}
	public Banker getBank(String chain, String areaNameOrBranch)
	{
		for (Banker B : bankList)
			if((B.bankChain().equalsIgnoreCase(chain))
			&&(B.bankChain().equalsIgnoreCase(areaNameOrBranch)))
				return B;
		
		Area A=findArea(areaNameOrBranch);
		if(A==null) 
			return null;
		
		for (Banker B : bankList)
			if((B.bankChain().equalsIgnoreCase(chain))
			&&(getStartArea(B)==A))
				return B;
		return null;
	}
	
	public Enumeration<Banker> banks() { return new IteratorEnumeration<Banker>(bankList.iterator());}
	
	public Iterator<String> bankChains(Area AreaOrNull)
	{
		HashSet<String> H=new HashSet<String>();
		for (Banker B : bankList)
			if((!H.contains(B.bankChain()))
			&&((AreaOrNull==null)
				||(getStartArea(B)==AreaOrNull)
				||(AreaOrNull.isChild(getStartArea(B)))))
					H.add(B.bankChain());
		return H.iterator();
	}

	public void renameRooms(Area A, String oldName, List<Room> allMyDamnRooms)
	{
		List<Room> onesToRenumber=new Vector<Room>();
		for(Room R : allMyDamnRooms)
		{
			synchronized(("SYNC"+R.roomID()).intern())
			{
				R=getRoom(R);
				R.setArea(A);
				if(oldName!=null)
				{
					if(R.roomID().toUpperCase().startsWith(oldName.toUpperCase()+"#"))
					{
						Room R2=getRoom(A.Name()+"#"+R.roomID().substring(oldName.length()+1));
						if((R2==null)||(!R2.roomID().startsWith(A.Name()+"#")))
						{
							String oldID=R.roomID();
							R.setRoomID(A.Name()+"#"+R.roomID().substring(oldName.length()+1));
							CMLib.database().DBReCreate(R,oldID);
						}
						else
							onesToRenumber.add(R);
					}
					else
						CMLib.database().DBUpdateRoom(R);
				}
			}
		}
		if(oldName!=null)
		{
			for(final Room R: onesToRenumber)
			{
				String oldID=R.roomID();
				R.setRoomID(A.getNewRoomID(R,-1));
				CMLib.database().DBReCreate(R,oldID);
			}
		}
	}

	public int getRoomDir(Room from, Room to)
	{
		if((from==null)||(to==null)) return -1;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			if(from.getRoomInDir(d)==to)
				return d;
		return -1;
	}

	public int getExitDir(Room from, Exit to)
	{
		if((from==null)||(to==null)) 
			return -1;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if(from.getExitInDir(d)==to)
				return d;
			if(from.getRawExit(d)==to)
				return d;
			if(from.getReverseExit(d)==to)
				return d;
		}
		return -1;
	}

	public Room findConnectingRoom(Room room)
	{
		if(room==null) return null;
		Room R=null;
		Vector<Room> otherChoices=new Vector<Room>();
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			R=room.getRoomInDir(d);
			if(R!=null)
				for(int d1=Directions.NUM_DIRECTIONS()-1;d1>=0;d1--)
					if(R.getRoomInDir(d1)==room)
					{
						if(R.getArea()==room.getArea())
							return R;
						otherChoices.addElement(R);
					}
		}
		for(Enumeration<Room> e=rooms();e.hasMoreElements();)
		{
			R=e.nextElement();
			if(R==room) continue;
			for(int d1=Directions.NUM_DIRECTIONS()-1;d1>=0;d1--)
				if(R.getRoomInDir(d1)==room)
				{
					if(R.getArea()==room.getArea())
						return R;
					otherChoices.addElement(R);
				}
		}
		if(otherChoices.size()>0)
			return otherChoices.firstElement();
		return null;
	}

	public boolean isClearableRoom(Room R)
	{
		if((R==null)||(R.amDestroyed())) return true;
		MOB M=null;
		Room sR=null;
		for(int i=0;i<R.numInhabitants();i++)
		{
			M=R.fetchInhabitant(i);
			if(M==null) continue;
			sR=M.getStartRoom();
			if((sR!=null)
			&&(!sR.roomID().equals(R.roomID())))
				return false;
			if(M.session()!=null)
				return false;
		}
		Item I=null;
		for(int i=0;i<R.numItems();i++)
		{
			I=R.getItem(i);
			if((I!=null)
			&&((I.expirationDate()!=0)
					||((I instanceof DeadBody)&&(((DeadBody)I).playerCorpse()))))
				return false;
		}
		for(final Enumeration<Ability> a=R.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(!A.isSavable()))
				return false;
		}
		return true;
	}
	
	public boolean explored(Room R)
	{
		if((R==null)
		||(CMath.bset(R.phyStats().sensesMask(),PhyStats.SENSE_ROOMUNEXPLORABLE))
		||(R.getArea()==null))
			return false;
		return false;
	}

	public static class AreaEnumerator implements Enumeration<Room>
	{
		private Enumeration<Area> curAreaEnumeration=null;
		private Enumeration<Room> curRoomEnumeration=null;
		private boolean addSkys = false;
		public AreaEnumerator(boolean includeSkys) {
			addSkys = includeSkys;
		}
		public boolean hasMoreElements()
		{
			if(curAreaEnumeration==null) curAreaEnumeration=CMLib.map().areas();
			while((curRoomEnumeration==null)||(!curRoomEnumeration.hasMoreElements()))
			{
				if(!curAreaEnumeration.hasMoreElements()) return false;
				if(addSkys)
					curRoomEnumeration=curAreaEnumeration.nextElement().getFilledProperMap();
				else
					curRoomEnumeration=curAreaEnumeration.nextElement().getProperMap();
			}
			return curRoomEnumeration.hasMoreElements();
		}
		public Room nextElement()
		{
			if(curAreaEnumeration==null) curAreaEnumeration=CMLib.map().areas();
			while((curRoomEnumeration==null)||(!curRoomEnumeration.hasMoreElements()))
			{
				if(!curAreaEnumeration.hasMoreElements()) return null;
				if(addSkys)
					curRoomEnumeration=curAreaEnumeration.nextElement().getFilledProperMap();
				else
					curRoomEnumeration=curAreaEnumeration.nextElement().getProperMap();
			}
			return curRoomEnumeration.nextElement();
		}
	}

	public void obliterateRoom(Room deadRoom)
	{
		for(final Enumeration<Ability> a=deadRoom.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null)
			{
				A.unInvoke();
				deadRoom.delEffect(A);
			}
		}
		try
		{
			for(Enumeration<Room> r=rooms();r.hasMoreElements();)
			{
				Room R=r.nextElement();
				synchronized(("SYNC"+R.roomID()).intern())
				{
					R=getRoom(R);
					if(R==null) continue;
					boolean changes=false;
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						Room thatRoom=R.rawDoors()[d];
						if(thatRoom==deadRoom)
						{
							R.rawDoors()[d]=null;
							changes=true;
							if((R.getRawExit(d)!=null)&&(R.getRawExit(d).isGeneric()))
							{
								Exit GE=R.getRawExit(d);
								GE.setTemporaryDoorLink(deadRoom.roomID());
							}
						}
					}
					if(changes)
						CMLib.database().DBUpdateExits(R);
				}
			}
		}catch(NoSuchElementException e){}
		for(int m=deadRoom.numInhabitants()-1;m>=0;m--)
		{
			MOB M=deadRoom.fetchInhabitant(m);
			if((M!=null)&&(M.playerStats()!=null))
				M.getStartRoom().bringMobHere(M,true);
		}
		emptyRoom(deadRoom,null);
		deadRoom.destroy();
		if(deadRoom instanceof GridLocale)
			((GridLocale)deadRoom).clearGrid(null);
		CMLib.database().DBDeleteRoom(deadRoom);
	}

	public void emptyArea(Area area)
	{
		for(final Enumeration<Ability> a=area.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null)
			{
				A.unInvoke();
				area.delEffect(A);
			}
		}
		for(Enumeration<Room> e=area.getProperMap();e.hasMoreElements();)
		{
			Room R=e.nextElement();
			emptyRoom(R,null);
			R.destroy();
		}
	}
	
	public Room roomLocation(Environmental E)
	{
		if(E==null) return null;
		if((E instanceof Area)&&(!((Area)E).isProperlyEmpty()))
			return ((Area)E).getRandomProperRoom();
		else
		if(E instanceof Room)
			return (Room)E;
		else
		if(E instanceof MOB)
			return ((MOB)E).location();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof Room))
			return (Room)((Item)E).owner();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof MOB))
		   return ((MOB)((Item)E).owner()).location();
		else
		if(E instanceof Ability)
			return roomLocation(((Ability)E).affecting());
		return null;
	}
	public Area getStartArea(Environmental E)
	{
		if(E instanceof Area) return (Area)E;
		Room R=getStartRoom(E);
		if(R==null) return null;
		return R.getArea();
	}

	public Room getStartRoom(Environmental E)
	{
		if(E ==null) return null;
		if(E instanceof MOB)
			return ((MOB)E).getStartRoom();
		if(E instanceof Item)
		{
			if(((Item)E).owner() instanceof MOB)
				return getStartRoom(((Item)E).owner());
			if(CMLib.flags().isGettable((Item)E))
				return null;
		}
		if(E instanceof Ability)
			return getStartRoom(((Ability)E).affecting());
		if((E instanceof Area)&&(!((Area)E).isProperlyEmpty())) 
			return ((Area)E).getRandomProperRoom();
		if(E instanceof Room) return (Room)E;
		return roomLocation(E);
	}

	public Area areaLocation(CMObject E)
	{
		if(E==null) return null;
		if(E instanceof Area)
			return (Area)E;
		else
		if(E instanceof Room)
			return ((Room)E).getArea();
		else
		if(E instanceof MOB)
			return ((MOB)E).location().getArea();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof Room))
			return ((Room)((Item)E).owner()).getArea();
		else
		if((E instanceof Item)&&(((Item)E).owner() instanceof MOB))
		   return (((MOB)((Item)E).owner()).location()).getArea();
		return null;
	}

	public void emptyRoom(Room room, Room bringBackHere)
	{
		if(room==null) return;
		List<MOB> inhabs=new Vector<MOB>();
		MOB M=null;
		for(int m=0;m<room.numInhabitants();m++)
		{
			M=room.fetchInhabitant(m);
			if(M!=null) inhabs.add(M);
		}
		for(int m=0;m<inhabs.size();m++)
		{
			M=inhabs.get(m);
			if(bringBackHere!=null)
				bringBackHere.bringMobHere(M,false);
			else
			if(!M.isSavable())
				continue;
			else
			if((M.getStartRoom()==null)
			||(M.getStartRoom()==room)
			||(M.getStartRoom().ID().length()==0))
				M.destroy();
			else
				M.getStartRoom().bringMobHere(M,false);
		}
		Item I=null;
		inhabs = null;
		
		Vector<Item> contents = new Vector<Item>();
		
		for(int i=0;i<room.numItems();i++)
		{
			I=room.getItem(i);
			if(I!=null) contents.addElement(I);
		}
		for(int i=0;i<contents.size();i++)
		{
			I=contents.elementAt(i);
			if(bringBackHere!=null)
				bringBackHere.moveItemTo(I,ItemPossessor.Expire.Player_Drop);
			else
				I.destroy();
		}
		room.clearSky();
		CMLib.threads().clearDebri(room,0);
		if(room instanceof GridLocale)
			for(Iterator<Room> r=((GridLocale)room).getExistingRooms();r.hasNext();)
				emptyRoom(r.next(), bringBackHere);
	}


	public void obliterateArea(Area A)
	{
		if(A==null) return;
		LinkedList<Room> rooms=new LinkedList<Room>();
		Room R=null;
		Enumeration<Room> e=A.getCompleteMap();
		while(e.hasMoreElements())
		{
			for(int i=0;(i<100)&&e.hasMoreElements();i++)
			{
				R=e.nextElement();
				if((R!=null)&&(R.roomID()!=null))
					rooms.add(R);
			}
			if(rooms.size()==0) break;
			for(Iterator<Room> e2=rooms.iterator();e2.hasNext();)
			{
				R=e2.next();
				if((R!=null)&&(R.roomID().length()>0))
					obliterateRoom(R);
				e2.remove();
			}
			e=A.getCompleteMap();
		}
		CMLib.database().DBDeleteArea(A);
		delArea(A);
	}

	public CMMsg resetMsg=null;
	public void resetRoom(Room room)
	{
		resetRoom(room,false);
	}
	
	public void resetRoom(Room room, boolean rebuildGrids)
	{
		if(room==null) return;
		if(room.roomID().length()==0) return;
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=getRoom(room);
			if((rebuildGrids)&&(room instanceof GridLocale))
				((GridLocale)room).clearGrid(null);
			boolean mobile=room.getMobility();
			room.toggleMobility(false);
			if(resetMsg==null) resetMsg=CMClass.getMsg(CMClass.sampleMOB(),room,CMMsg.MSG_ROOMRESET,null);
			resetMsg.setTarget(room);
			room.executeMsg(room,resetMsg);
			emptyRoom(room,null);
			for(final Enumeration<Ability> a=room.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)&&(A.canBeUninvoked()))
					A.unInvoke();
			}
			CMLib.database().DBReReadRoomObject(room);
			CMLib.database().DBReadContent(room.roomID(),room,true);
			room.startItemRejuv();
			room.setResource(-1);
			room.toggleMobility(mobile);
		}
	}

	public Room findWorldRoomLiberally(MOB mob, String cmd, String srchWhatAERIPMVK, int timePct, long maxMillis)
	{
		List<Room> rooms=findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,null,true,timePct, maxMillis);
		if((rooms!=null)&&(rooms.size()!=0)) return rooms.get(0);
		return null;
	}
	
	public List<Room> findWorldRoomsLiberally(MOB mob, String cmd, String srchWhatAERIPMVK, int timePct, long maxMillis)
	{ return findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,null,false,timePct,maxMillis); }
	
	public Room findAreaRoomLiberally(MOB mob, Area A,String cmd, String srchWhatAERIPMVK, int timePct)
	{
		List<Room> rooms=findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,A,true,timePct,120);
		if((rooms!=null)&&(rooms.size()!=0)) return rooms.get(0);
		return null;
	}
	
	public List<Room> findAreaRoomsLiberally(MOB mob, Area A,String cmd, String srchWhatAERIPMVK, int timePct)
	{ return findWorldRoomsLiberally(mob,cmd,srchWhatAERIPMVK,A,false,timePct,120); }
	
	protected Room addWorldRoomsLiberally(List<Room> rooms, List<? extends Environmental> choicesV)
	{
		if(choicesV==null) return null;
		if(rooms!=null)
		{
			for(Environmental E : choicesV)
				addWorldRoomsLiberally(rooms,roomLocation(E));
			return null;
		}
		else
		{
			Room room=null;
			int tries=0;
			while(((room==null)||(room.roomID().length()==0))&&((++tries)<200))
				room=roomLocation(choicesV.get(CMLib.dice().roll(1,choicesV.size(),-1)));
			return room;
		}
	}
	
	protected Room addWorldRoomsLiberally(List<Room> rooms, Room room)
	{
		if(room==null) return null;
		if(rooms!=null)
		{ 
			if(!rooms.contains(room))
				rooms.add(room);
			return null;
		}
		return room;
	}
	
	protected Room addWorldRoomsLiberally(List<Room>rooms, Area area)
	{
		if((area==null)||(area.isProperlyEmpty()))
			return null;
		return addWorldRoomsLiberally(rooms,area.getRandomProperRoom()); 
	}
	
	protected Enumeration<Room> rightLiberalMap(Area A) {
		if(A==null) return roomsFilled();
		return A.getProperMap();
	}

	protected List<Room> returnResponse(List<Room> rooms, Room room)
	{
		if(rooms!=null) return rooms;
		if(room==null) return new Vector<Room>(1);
		return new XVector<Room>(room);
	}
	
	protected boolean enforceTimeLimit(final long startTime,  final long maxMillis)
	{
		if(maxMillis<=0) return false;
		return ((System.currentTimeMillis() - startTime)) > maxMillis;
	}

	protected List<MOB> checkMOBCachedList(List<MOB> list)
	{
		if (list != null)
		{
			for(Environmental E : list)
				if(E.amDestroyed())
					return null;
		}
		return list;
	}

	protected List<Item> checkInvCachedList(List<Item> list)
	{
		if (list != null)
		{
			for(Item E : list)
				if((E.amDestroyed())||(!(E.owner() instanceof MOB)))
					return null;
		}
		return list;
	}
	
	protected List<Item> checkRoomItemCachedList(List<Item> list)
	{
		if (list != null)
		{
			for(Item E : list)
				if((E.amDestroyed())||(!(E.owner() instanceof Room)))
					return null;
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,List<MOB>> getMOBFinder()
	{
		Map<String,List<MOB>> finder=(Map<String,List<MOB>>)Resources.getResource("SYSTEM_MOB_FINDER_CACHE");
		if(finder==null)
		{
			finder=new PrioritizingLimitedMap<String,List<MOB>>(10,EXPIRE_5MINS,EXPIRE_10MINS,100);
			Resources.submitResource("SYSTEM_MOB_FINDER_CACHE",finder);
		}
		return finder;
	}
	@SuppressWarnings("unchecked")
	public Map<String,Area> getAreaFinder()
	{
		Map<String,Area> finder=(Map<String,Area>)Resources.getResource("SYSTEM_AREA_FINDER_CACHE");
		if(finder==null)
		{
			finder=new PrioritizingLimitedMap<String,Area>(50,EXPIRE_30MINS,EXPIRE_1HOUR,100);
			Resources.submitResource("SYSTEM_AREA_FINDER_CACHE",finder);
		}
		return finder;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,List<Item>> getRoomItemFinder()
	{
		Map<String,List<Item>> finder=(Map<String,List<Item>>)Resources.getResource("SYSTEM_RITEM_FINDER_CACHE");
		if(finder==null)
		{
			finder=new PrioritizingLimitedMap<String,List<Item>>(10,EXPIRE_5MINS,EXPIRE_10MINS,100);
			Resources.submitResource("SYSTEM_RITEM_FINDER_CACHE",finder);
		}
		return finder;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,List<Item>> getInvItemFinder()
	{
		Map<String,List<Item>> finder=(Map<String,List<Item>>)Resources.getResource("SYSTEM_IITEM_FINDER_CACHE");
		if(finder==null)
		{
			finder=new PrioritizingLimitedMap<String,List<Item>>(10,EXPIRE_1MIN,EXPIRE_10MINS,100);
			Resources.submitResource("SYSTEM_IITEM_FINDER_CACHE",finder);
		}
		return finder;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,List<Environmental>> getStockFinder()
	{
		Map<String,List<Environmental>> finder=(Map<String,List<Environmental>>)Resources.getResource("SYSTEM_STOCK_FINDER_CACHE");
		if(finder==null)
		{
			finder=new PrioritizingLimitedMap<String,List<Environmental>>(10,EXPIRE_10MINS,EXPIRE_1HOUR,100);
			Resources.submitResource("SYSTEM_STOCK_FINDER_CACHE",finder);
		}
		return finder;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,List<Room>> getRoomFinder()
	{
		Map<String,List<Room>> finder=(Map<String,List<Room>>)Resources.getResource("SYSTEM_ROOM_FINDER_CACHE");
		if(finder==null)
		{
			finder=new PrioritizingLimitedMap<String,List<Room>>(20,EXPIRE_20MINS,EXPIRE_1HOUR,100);
			Resources.submitResource("SYSTEM_ROOM_FINDER_CACHE",finder);
		}
		return finder;
	}
	
	protected List<Room> findWorldRoomsLiberally(MOB mob, 
												 String cmd, 
												 String srchWhatAERIPMVK, 
												 Area A, 
												 boolean returnFirst, 
												 int timePct, 
												 long maxMillis)
	{
		Room room=null;
		// wish this stuff could be cached, even temporarily, however,
		// far too much of the world is dynamic, and far too many searches
		// are looking for dynamic things.  the cached results would be useless
		// as soon as they are put away -- that's why the limited caches time them out!
		final boolean disableCaching=CMProps.getBoolVar(CMProps.Bool.MAPFINDSNOCACHE);
		
		final Vector<Room> rooms=(returnFirst)?null:new Vector<Room>();
		
		final Room curRoom=(mob!=null)?mob.location():null;
		
		boolean searchWeakAreas=false;
		boolean searchStrictAreas=false;
		boolean searchRooms=false;
		boolean searchPlayers=false;
		boolean searchItems=false;
		boolean searchInhabs=false;
		boolean searchInventories=false;
		boolean searchStocks=false;
		char[] flags = srchWhatAERIPMVK.toUpperCase().toCharArray();
		for(int c=0;c<flags.length;c++)
			switch(flags[c])
			{
				case 'E': searchWeakAreas=true;   break;
				case 'A': searchStrictAreas=true; break;
				case 'R': searchRooms=true; 	  break;
				case 'P': searchPlayers=true;	 break;
				case 'I': searchItems=true; 	  break;
				case 'M': searchInhabs=true;	  break;
				case 'V': searchInventories=true; break;
				case 'K': searchStocks=true;	  break;
			}
		final long startTime = System.currentTimeMillis();
		if(searchRooms)
		{
			final int dirCode=Directions.getGoodDirectionCode(cmd);
			if((dirCode>=0)&&(curRoom!=null))
				room=addWorldRoomsLiberally(rooms,curRoom.rawDoors()[dirCode]);
			if(room==null)
				room=addWorldRoomsLiberally(rooms,getRoom(cmd));
		}

		if(room==null)
		{
			// first get room ids
			if((cmd.charAt(0)=='#')&&(curRoom!=null)&&(searchRooms))
				room=addWorldRoomsLiberally(rooms,getRoom(curRoom.getArea().Name()+cmd));
			else
			{
				String srchStr=cmd;
				
				if(searchPlayers)
				{
					// then look for players
					MOB M=CMLib.sessions().findPlayerOnline(srchStr,false);
					if(M!=null)
						room=addWorldRoomsLiberally(rooms,M.location());
				}
				if(enforceTimeLimit(startTime,maxMillis)) return returnResponse(rooms,room);
				
				// search areas strictly
				if(searchStrictAreas && room==null && (A==null))
				{
					A=getArea(srchStr);
					if((A!=null) &&(A.properSize()>0) &&(A.getProperRoomnumbers().roomCountAllAreas()>0))
						room=addWorldRoomsLiberally(rooms,A);
					A=null;
				}
				if(enforceTimeLimit(startTime,maxMillis)) return returnResponse(rooms,room);
				
				// no good, so look for room inhabitants
				if(searchInhabs && room==null)
				{
					final Map<String,List<MOB>> finder=getMOBFinder();
					List<MOB> candidates=null;
					
					if((mob==null)||(mob.isMonster()))
					{
						candidates=checkMOBCachedList(finder.get(srchStr.toLowerCase()));
						if(returnFirst&&(candidates!=null)&&(candidates.size()>1))
							candidates=new XVector<MOB>(candidates.get(0));
					}
					if(candidates==null)
					{
						candidates=findInhabitants(rightLiberalMap(A), mob, srchStr,returnFirst, timePct);
						if((!disableCaching)&&(!returnFirst)&&((mob==null)||(mob.isMonster())))
							finder.put(srchStr.toLowerCase(), candidates);
							
					}
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxMillis)) return returnResponse(rooms,room);
				
				// now check room text
				if(searchRooms && room==null)
				{
					final Map<String,List<Room>> finder=getRoomFinder();
					List<Room> candidates=null;
					if((mob==null)||(mob.isMonster()))
					{
						candidates=finder.get(srchStr.toLowerCase());
						if(returnFirst&&(candidates!=null)&&(candidates.size()>1))
							candidates=new XVector<Room>(candidates.get(0));
					}
					if(candidates==null)
					{
						candidates=findRooms(rightLiberalMap(A), mob, srchStr, false,returnFirst, timePct);
						if((!disableCaching)&&(!returnFirst)&&((mob==null)||(mob.isMonster())))
							finder.put(srchStr.toLowerCase(), candidates);
					}
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxMillis)) return returnResponse(rooms,room);
				
				// check floor items
				if(searchItems && room==null)
				{
					final Map<String,List<Item>> finder=getRoomItemFinder();
					List<Item> candidates=null;
					if((mob==null)||(mob.isMonster()))
					{
						candidates=checkRoomItemCachedList(finder.get(srchStr.toLowerCase()));
						if(returnFirst&&(candidates!=null)&&(candidates.size()>1))
							candidates=new XVector<Item>(candidates.get(0));
					}
					if(candidates==null)
					{
						candidates=findRoomItems(rightLiberalMap(A), mob, srchStr, false,returnFirst,timePct);
						if((!disableCaching)&&(!returnFirst)&&((mob==null)||(mob.isMonster())))
							finder.put(srchStr.toLowerCase(), candidates);
					}
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxMillis)) return returnResponse(rooms,room);
				
				// check inventories
				if(searchInventories && room==null)
				{
					final Map<String,List<Item>> finder=getInvItemFinder();
					List<Item> candidates=null;
					if((mob==null)||(mob.isMonster()))
					{
						candidates=checkInvCachedList(finder.get(srchStr.toLowerCase()));
						if(returnFirst&&(candidates!=null)&&(candidates.size()>1))
							candidates=new XVector<Item>(candidates.get(0));
					}
					if(candidates==null)
					{
						candidates=findInventory(rightLiberalMap(A), mob, srchStr, returnFirst,timePct);
						if((!disableCaching)&&(!returnFirst)&&((mob==null)||(mob.isMonster())))
							finder.put(srchStr.toLowerCase(), candidates);
					}
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxMillis)) return returnResponse(rooms,room);
				
				// check stocks
				if(searchStocks && room==null)
				{
					final Map<String,List<Environmental>> finder=getStockFinder();
					List<Environmental> candidates=null;
					if((mob==null)||(mob.isMonster()))
					{
						candidates=finder.get(srchStr.toLowerCase());
						if(returnFirst&&(candidates!=null)&&(candidates.size()>1))
							candidates=new XVector<Environmental>(candidates.get(0));
					}
					if(candidates==null)
					{
						candidates=findShopStock(rightLiberalMap(A), mob, srchStr, returnFirst,false,timePct);
						if((!disableCaching)&&(!returnFirst)&&((mob==null)||(mob.isMonster())))
							finder.put(srchStr.toLowerCase(), candidates);
					}
					if(candidates.size()>0)
						room=addWorldRoomsLiberally(rooms,candidates);
				}
				if(enforceTimeLimit(startTime,maxMillis)) return returnResponse(rooms,room);
				
				// search areas weakly
				if(searchWeakAreas && room==null && (A==null))
				{
					A=findArea(srchStr);
					if((A!=null) &&(A.properSize()>0) &&(A.getProperRoomnumbers().roomCountAllAreas()>0))
						room=addWorldRoomsLiberally(rooms,A);
					A=null;
				}
			}
		}
		final List<Room> responseSet = returnResponse(rooms,room);
		return responseSet;
	}

	protected DVector getAllPlayersHere(Area area, boolean includeLocalFollowers)
	{
		DVector playersHere=new DVector(2);
		MOB M=null;
		Room R=null;
		for(Session S : CMLib.sessions().localOnlineIterable())
		{
			M=S.mob();
			R=(M!=null)?M.location():null;
			if((R!=null)&&(R.getArea()==area)&&(M!=null))
			{
				playersHere.addElement(M,getExtendedRoomID(R));
				if(includeLocalFollowers)
				{
					MOB M2=null;
					Set<MOB> H=M.getGroupMembers(new HashSet<MOB>());
					for(Iterator<MOB> i=H.iterator();i.hasNext();)
					{
						M2=i.next();
						if((M2!=M)&&(M2.location()==R))
							playersHere.addElement(M2,getExtendedRoomID(R));
					}
				}
			}
		}
		return playersHere;

	}
	
	public void resetArea(Area area)
	{
		Area.State oldFlag=area.getAreaState();
		area.setAreaState(Area.State.FROZEN);
		DVector playersHere=getAllPlayersHere(area,true);
		for(int p=0;p<playersHere.size();p++)
		{
			MOB M=(MOB)playersHere.elementAt(p,1);
			Room R=M.location();
			R.delInhabitant(M);
		}
		for(Enumeration<Room> r=area.getProperMap();r.hasMoreElements();)
			resetRoom(r.nextElement());
		area.fillInAreaRooms();
		for(int p=0;p<playersHere.size();p++)
		{
			MOB M=(MOB)playersHere.elementAt(p,1);
			Room R=getRoom((String)playersHere.elementAt(p,2));
			if(R==null) R=M.getStartRoom();
			if(R==null) R=getStartRoom(M);
			if(R!=null) 
				R.bringMobHere(M,false);
		}
		CMLib.database().DBReadArea(area);
		area.setAreaState(oldFlag);
	}

	public boolean hasASky(Room room)
	{
		if((room==null)
		||(room.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		||((room.domainType()&Room.INDOORS)>0))
			return false;
		return true;
	}

	
	public void registerWorldObjectDestroyed(Area area, Room room, CMObject o)
	{
		if(o instanceof Deity)
			delDeity((Deity)o);
		
		if(o instanceof PostOffice)
			delPostOffice((PostOffice)o);
		
		if(o instanceof Banker)
			delBank((Banker)o);
		
		if(o instanceof Auctioneer)
			delAuctionHouse((Auctioneer)o);
		
		if(o instanceof PhysicalAgent)
		{
			PhysicalAgent AE=(PhysicalAgent)o;
			if((area == null) && (room!=null)) area = room.getArea();
			if(area == null) area =getStartArea(AE);
			delScriptHost(area, AE);
		}
	}
	
	public void registerWorldObjectLoaded(Area area, Room room, final CMObject o)
	{
		if(o instanceof Deity)
			addDeity((Deity)o);
		
		if(o instanceof PostOffice)
			addPostOffice((PostOffice)o);
		
		if(o instanceof Banker)
			addBank((Banker)o);
		
		if(o instanceof Auctioneer)
			addAuctionHouse((Auctioneer)o);
		
		if(o instanceof PhysicalAgent)
		{
			PhysicalAgent AE=(PhysicalAgent)o;
			if(room == null) room = getStartRoom(AE);
			if((area == null) && (room!=null)) area = room.getArea();
			if(area == null) area = getStartArea(AE);
			addScriptHost(area, room, AE);
			if(o instanceof MOB)
				for(final Enumeration<Item> i=((MOB)o).items();i.hasMoreElements();)
					addScriptHost(area, room, i.nextElement());
		}
	}
	
	protected void cleanScriptHosts(final SLinkedList<LocatedPair> hosts, final PhysicalAgent oneToDel, final boolean fullCleaning)
	{
		PhysicalAgent PA;
		for(final Iterator<LocatedPair> w=hosts.iterator();w.hasNext();)
		{
			final LocatedPair W=w.next();
			if(W==null)
				hosts.remove(W);
			else
			{
				PA=W.obj();
				if((PA==null)
				||(PA==oneToDel)
				||(PA.amDestroyed())
				||((fullCleaning)&&(!isAQualifyingScriptHost(PA))))
					hosts.remove(W);
			}
		}
	}

	protected boolean isAQualifyingScriptHost(final PhysicalAgent host)
	{
		if(host==null) return false;
		for(final Enumeration<Behavior> e = host.behaviors();e.hasMoreElements();)
		{
			final Behavior B=e.nextElement();
			if((B!=null) && B.isSavable() && (B instanceof ScriptingEngine))
				return true;
		}
		for(final Enumeration<ScriptingEngine> e = host.scripts();e.hasMoreElements();)
		{
			final ScriptingEngine SE=e.nextElement();
			if((SE!=null) && SE.isSavable())
				return true;
		}
		return false;
	}
	
	protected boolean isAScriptHost(final Area area, final PhysicalAgent host)
	{
		if(area == null) return false;
		return isAScriptHost(scriptHostMap.get(area.Name()), host);
	}
	
	protected boolean isAScriptHost(final SLinkedList<LocatedPair> hosts, final PhysicalAgent host)
	{
		if((hosts==null)||(host==null)||(hosts.size()==0)) return false;
		for(final Iterator<LocatedPair> w=hosts.iterator();w.hasNext();)
		{
			final LocatedPair W=w.next();
			if(W.obj()==host)
				return true;
		}
		return false;
	}
	
	protected final Object getScriptHostSemaphore(final Area area)
	{
		final Object semaphore;
		if(SCRIPT_HOST_SEMAPHORES.containsKey(area.Name()))
			semaphore=SCRIPT_HOST_SEMAPHORES.get(area.Name());
		else
		{
			synchronized(SCRIPT_HOST_SEMAPHORES)
			{
				semaphore=new Object();
				SCRIPT_HOST_SEMAPHORES.put(area.Name(), semaphore);
			}
		}
		return semaphore;
	}

	protected void addScriptHost(final Area area, final Room room, final PhysicalAgent host)
	{
		if((area==null) || (host == null))
			return;
		if(!isAQualifyingScriptHost(host))
			return;
		synchronized(getScriptHostSemaphore(area))
		{
			SLinkedList<LocatedPair> hosts = scriptHostMap.get(area.Name());
			if(hosts == null)
			{
				hosts=new SLinkedList<LocatedPair>();
				scriptHostMap.put(area.Name(), hosts);
			}
			else
			{
				cleanScriptHosts(hosts, null, false);
				if(isAScriptHost(hosts,host))
					return;
			}
			hosts.add(new LocatedPair(room, host));
		}
	}
	protected void delScriptHost(Area area, final PhysicalAgent oneToDel)
	{
		if(oneToDel == null)
			return;
		if(area == null)
			for(final Area A : areasList)
				if(isAScriptHost(A,oneToDel))
				{
					area = A;
					break;
				}
		if(area == null)
			return;
		synchronized(getScriptHostSemaphore(area))
		{
			final SLinkedList<LocatedPair> hosts = scriptHostMap.get(area.Name());
			if(hosts==null) return;
			cleanScriptHosts(hosts, oneToDel, false);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Enumeration<LocatedPair> scriptHosts(final Area area)
	{
		final LinkedList<List<LocatedPair>> V = new LinkedList<List<LocatedPair>>();
		if(area == null)
		{
			for(final String areaKey : scriptHostMap.keySet())
				V.add(scriptHostMap.get(areaKey));
		}
		else
		{
			final SLinkedList<LocatedPair> hosts = scriptHostMap.get(area.Name());
			if(hosts==null) return EmptyEnumeration.INSTANCE;
			V.add(hosts);
		}
		if(V.size()==0) return EmptyEnumeration.INSTANCE;
		final MultiListEnumeration<LocatedPair> me=new MultiListEnumeration<LocatedPair>(V,true);
		return new Enumeration<LocatedPair>()
		{
			public boolean hasMoreElements() { return me.hasMoreElements();}
			public LocatedPair nextElement() {
				final LocatedPair W = me.nextElement();
				final PhysicalAgent E = W.obj();
				if(((E==null) || (E.amDestroyed())) && hasMoreElements())
					return nextElement();
				return W;
			}
		};
	}
	
	public boolean activate() 
	{
		if(serviceClient==null)
		{
			name="THMap"+Thread.currentThread().getThreadGroup().getName().charAt(0);
			serviceClient=CMLib.threads().startTickDown(this, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK, MudHost.TIME_SAVETHREAD_SLEEP, 1);
		}
		return true;
	}
	
	@Override public boolean tick(Tickable ticking, int tickID) 
	{
		try
		{
			if((!CMSecurity.isDisabled(CMSecurity.DisFlag.SAVETHREAD))
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MAPTHREAD)))
			{
				isDebugging=CMSecurity.isDebugging(DbgFlag.MAPTHREAD);
				tickStatus=Tickable.STATUS_ALIVE;
				if(checkDatabase())
					roomMaintSweep();
			}
			Resources.savePropResources();
		}
		finally
		{
			tickStatus=Tickable.STATUS_NOT;
			setThreadStatus(serviceClient,"sleeping");
		}
		return true;
	}

	public boolean shutdown() 
	{
		areasList.clear();
		deitiesList.clear();
		space.clear();
		globalHandlers.clear();
		if(CMLib.threads().isTicking(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK))
		{
			CMLib.threads().deleteTick(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK);
			serviceClient=null;
		}
		return true;
	}
	
	public void roomMaintSweep()
	{
		final boolean corpsesOnly=CMSecurity.isSaveFlag("ROOMITEMS");
		final boolean noMobs=CMSecurity.isSaveFlag("ROOMMOBS");
		setThreadStatus(serviceClient,"expiration sweep");
		final long currentTime=System.currentTimeMillis();
		final boolean debug=CMSecurity.isDebugging(CMSecurity.DbgFlag.VACUUM);
		final MOB expireM=getFactoryMOB(null);
		try
		{
			Vector<Environmental> stuffToGo=new Vector<Environmental>();
			Item I=null;
			MOB M=null;
			Room R=null;
			Vector<Room> roomsToGo=new Vector<Room>();
			CMMsg expireMsg=CMClass.getMsg(expireM,R,null,CMMsg.MSG_EXPIRE,null);
			for(Enumeration<Room> r=rooms();r.hasMoreElements();)
			{
				R=r.nextElement();
				expireM.setLocation(R);
				expireMsg.setTarget(R);
				if((R.expirationDate()!=0)
				&&(currentTime>R.expirationDate())
				&&(R.okMessage(R,expireMsg)))
					roomsToGo.addElement(R);
				else
				if(!R.amDestroyed())
				{
					stuffToGo.clear();
					for(int i=0;i<R.numItems();i++)
					{
						I=R.getItem(i);
						if((I!=null)
						&&((!corpsesOnly)||(I instanceof DeadBody))
						&&(I.expirationDate()!=0)
						&&(I.owner()==R)
						&&(currentTime>I.expirationDate()))
							stuffToGo.add(I);
					}
					for(int i=0;i<R.numInhabitants();i++)
					{
						M=R.fetchInhabitant(i);
						if((M!=null)
						&&(!noMobs)
						&&(M.expirationDate()!=0)
						&&(currentTime>M.expirationDate()))
							stuffToGo.add(M);
					}
				}
				if(stuffToGo.size()>0)
				{
					boolean success=true;
					for(int s=0;s<stuffToGo.size();s++)
					{
						Environmental E=stuffToGo.elementAt(s);
						setThreadStatus(serviceClient,"expiring "+E.Name());
						expireMsg.setTarget(E);
						if(R.okMessage(expireM,expireMsg))
							R.sendOthers(expireM,expireMsg);
						else
							success=false;
						if(debug) Log.sysOut("UTILITHREAD","Expired "+E.Name()+" in "+getExtendedRoomID(R)+": "+success);
					}
					stuffToGo.clear();
				}
			}
			for(int r=0;r<roomsToGo.size();r++)
			{
				R=roomsToGo.elementAt(r);
				expireM.setLocation(R);
				expireMsg.setTarget(R);
				setThreadStatus(serviceClient,"expirating room "+getExtendedRoomID(R));
				if(debug)
				{
					String roomID=getExtendedRoomID(R);
					if(roomID.length()==0) roomID="(unassigned grid room, probably in the air)";
					if(debug) Log.sysOut("UTILITHREAD","Expired "+roomID+".");
				}
				R.sendOthers(expireM,expireMsg);
			}
			
		}
		catch(java.util.NoSuchElementException e){}
		setThreadStatus(serviceClient,"title sweeping");
		List<String> playerList=CMLib.database().getUserList();
		try
		{
			for(Enumeration<Room> r=rooms();r.hasMoreElements();)
			{
				Room R=r.nextElement();
				LandTitle T=CMLib.law().getLandTitle(R);
				if(T!=null)
				{
					setThreadStatus(serviceClient,"checking title in "+R.roomID()+": "+Runtime.getRuntime().freeMemory());
					T.updateLot(playerList);
					setThreadStatus(serviceClient,"title sweeping");
				}
			}
		}catch(NoSuchElementException nse){}
		
		setThreadStatus(serviceClient,"cleaning scripts");
		for(String areaKey : scriptHostMap.keySet())
			cleanScriptHosts(scriptHostMap.get(areaKey), null, true);
		
		long lastDateTime=System.currentTimeMillis()-(5*TimeManager.MILI_MINUTE);
		setThreadStatus(serviceClient,"checking");
		try
		{
			for(Enumeration<Room> r=rooms();r.hasMoreElements();)
			{
				Room R=r.nextElement();
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB mob=R.fetchInhabitant(m);
					if((mob!=null)&&(mob.lastTickedDateTime()>0)&&(mob.lastTickedDateTime()<lastDateTime))
					{
						boolean ticked=CMLib.threads().isTicking(mob,Tickable.TICKID_MOB);
						boolean isDead=mob.amDead();
						String wasFrom=((mob.getStartRoom()!=null)?mob.getStartRoom().roomID():"NULL");
						if(!ticked)
						{
							if(CMLib.players().getPlayer(mob.Name())==null)
							{
								if(ticked)
								{
									// we have a dead group.. let the group handler deal with it.
									Log.errOut(serviceClient.getName(),mob.name()+" in room "+R.roomID()+" unticked in dead group (Home="+wasFrom+") since: "+CMLib.time().date2String(mob.lastTickedDateTime())+".");
									continue;
								}
								else
									Log.errOut(serviceClient.getName(),mob.name()+" in room "+R.roomID()+" unticked (is ticking="+(ticked)+", dead="+isDead+", Home="+wasFrom+") since: "+CMLib.time().date2String(mob.lastTickedDateTime())+"."+(ticked?"":"  This mob has been destroyed. May he rest in peace."));
							}
							else
								Log.errOut(serviceClient.getName(),"Player "+mob.name()+" in room "+R.roomID()+" unticked (is ticking="+(ticked)+", dead="+isDead+", Home="+wasFrom+") since: "+CMLib.time().date2String(mob.lastTickedDateTime())+"."+(ticked?"":"  This mob has been put aside."));
							setThreadStatus(serviceClient,"destroying unticked mob "+mob.name());
							if(CMLib.players().getPlayer(mob.Name())==null) mob.destroy();
							R.delInhabitant(mob);
							setThreadStatus(serviceClient,"checking");
						}
					}
				}
			}
		}
		catch(java.util.NoSuchElementException e){}
		finally {
			if(expireM!=null)
				expireM.destroy();
		}
	}
	
	public CMFile.CMVFSDir getMapRoot(final CMFile.CMVFSDir root)
	{
		return new CMFile.CMVFSDir(root,root.path+"map/") {
			@Override protected CMFile.CMVFSFile[] getFiles() {
				List<CMFile.CMVFSFile> myFiles=new Vector<CMFile.CMVFSFile>(numAreas());
				for(Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
				{
					final Area A=a.nextElement();
					myFiles.add(new CMFile.CMVFSFile(this.path+A.Name().replace(' ', '_')+".cmare",48,System.currentTimeMillis(),"SYS")
					{
						@Override public Object readData()
						{
							return CMLib.coffeeMaker().getAreaXML(A, null, null, null, true);
						}
					});
					myFiles.add(new CMFile.CMVFSDir(this,this.path+A.Name().toLowerCase()+"/") {
						@Override protected CMFile.CMVFSFile[] getFiles() {
							List<CMFile.CMVFSFile> myFiles=new Vector<CMFile.CMVFSFile>();
							for(Enumeration<Room> r=A.getFilledProperMap();r.hasMoreElements();)
							{
								final Room R=r.nextElement();
								if(R.roomID().length()>0)
								{
									String roomID=R.roomID();
									if(roomID.startsWith(A.Name()+"#"))
										roomID=roomID.substring(A.Name().length()+1);
									myFiles.add(new CMFile.CMVFSFile(this.path+R.roomID().replace(' ', '_')+".cmare",48,System.currentTimeMillis(),"SYS")
									{
										@Override public Object readData()
										{
											return CMLib.coffeeMaker().getRoomXML(R, null, null, true);
										}
									});
									myFiles.add(new CMFile.CMVFSDir(this,this.path+roomID+"/") {
										@Override protected CMFile.CMVFSFile[] getFiles() {
											List<CMFile.CMVFSFile> myFiles=new Vector<CMFile.CMVFSFile>();
											String[] stats=R.getStatCodes();
											for(int i=0;i<stats.length;i++)
											{
												final String statName=stats[i];
												final String statValue=R.getStat(statName);
												//todo: make these writeable
												myFiles.add(new CMFile.CMVFSFile(this.path+statName,256,System.currentTimeMillis(),"SYS")
												{
													@Override public Object readData()
													{
														return statValue;
													}
													@Override public void saveData(String filename, int vfsBits, String author, Object O)
													{
														R.setStat(statName, O.toString());
														CMLib.database().DBUpdateRoom(R);
													}
												});
											}
											Collections.sort(myFiles,CMFile.CMVFSDir.fcomparator);
											return myFiles.toArray(new CMFile.CMVFSFile[0]);
										}
									});
								}
							}
							Collections.sort(myFiles,CMFile.CMVFSDir.fcomparator);
							return myFiles.toArray(new CMFile.CMVFSFile[0]);
						}
					});
				}
				Collections.sort(myFiles,CMFile.CMVFSDir.fcomparator);
				return myFiles.toArray(new CMFile.CMVFSFile[0]);
			}
		};
	}
	
}
