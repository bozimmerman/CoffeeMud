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
   Copyright 2008-2016 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "StdThinInstance";
	}

	protected long	flags	= Area.FLAG_THIN | Area.FLAG_INSTANCE_PARENT;

	@Override
	public long flags()
	{
		return flags;
	}

	protected final List<AreaInstanceChild>	instanceChildren	= new SVector<AreaInstanceChild>();
	protected volatile int					instanceCounter		= 0;
	protected long							childCheckDown		= CMProps.getMillisPerMudHour() / CMProps.getTickMillis();
	protected WeakReference<Area>			parentArea			= null;

	protected String getStrippedRoomID(String roomID)
	{
		final int x=roomID.indexOf('#');
		if(x<0)
			return null;
		return roomID.substring(x);
	}

	protected String convertToMyArea(String roomID)
	{
		final String strippedID=getStrippedRoomID(roomID);
		if(strippedID==null)
			return null;
		return Name()+strippedID;
	}
	
	protected boolean qualifiesToBeParentArea(Area parentA)
	{
		return (CMath.bset(parentA.flags(),Area.FLAG_INSTANCE_PARENT))
		&&(!CMath.bset(parentA.flags(),Area.FLAG_INSTANCE_CHILD));
	}

	protected Area getParentArea()
	{
		if((parentArea!=null)&&(parentArea.get()!=null))
			return parentArea.get();
		final int x=Name().indexOf('_');
		if(x<0)
			return null;
		if(!CMath.isNumber(Name().substring(0,x)))
			return null;
		final Area parentA = CMLib.map().getArea(Name().substring(x+1));
		if((parentA==null)
		||(!qualifiesToBeParentArea(parentA)))
			return null;
		parentArea=new WeakReference<Area>(parentA);
		return parentA;
	}

	@Override
	public Room getRoom(String roomID)
	{
		if(!CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
			return super.getRoom(roomID);

		if(!isRoom(roomID))
			return null;
		Room R=super.getRoom(roomID);
		if(((R==null)||(R.amDestroyed()))&&(roomID!=null))
		{
			final Area parentA=getParentArea();
			if(parentA==null)
				return null;

			if(roomID.toUpperCase().startsWith(Name().toUpperCase()+"#"))
				roomID=Name()+roomID.substring(Name().length()); // for case sensitive situations
			R=parentA.getRoom(parentA.Name()+getStrippedRoomID(roomID));
			if(R==null)
				return null;

			final Room origRoom=R;
			R=CMLib.database().DBReadRoomObject(R.roomID(), false);
			if(R==null)
				return null;
			final TreeMap<String,Room> V=new TreeMap<String,Room>();
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
					final Room dirR=origRoom.rawDoors()[d];
					if(dirR!=null)
					{
						final String myRID=dirR.roomID();
						if((myRID!=null)&&(myRID.length()>0)&&(dirR.getArea()==parentA))
						{
							final String localDirRID=convertToMyArea(myRID);
							final Room localDirR=getProperRoom(localDirRID);
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
			for(final Enumeration<MOB> e=R.inhabitants();e.hasMoreElements();)
				e.nextElement().bringToLife(R,true);
			R.startItemRejuv();
			fillInAreaRoom(R);
			R.setExpirationDate(System.currentTimeMillis()+WorldMap.ROOM_EXPIRATION_MILLIS);
		}
		return R;
	}

	protected Set<MOB> getProtectedMobSet(final Area childA, final List<WeakReference<MOB>> registeredMobList)
	{
		final Set<MOB> protectTheseMobsList=new HashSet<MOB>();
		for(final WeakReference<MOB> wmob : registeredMobList)
		{
			final MOB M=wmob.get();
			if(M!=null)
			{
				final Room R=M.location();
				if(CMLib.flags().isInTheGame(M,true)
				&&(R!=null)
				&&(R.getArea()==childA))
					protectTheseMobsList.add(M);
			}
		}
		for(final Enumeration<MOB> m = CMLib.players().players();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if(M!=null)
			{
				final Room R=M.location();
				if((R!=null)
				&&(R.getArea()==childA))
					protectTheseMobsList.add(M);
			}
		}
		final List<MOB> addFollowers=new ArrayList<MOB>(0);
		for(final MOB pmob : protectTheseMobsList)
		{
			final Set<MOB> grp=pmob.getGroupMembers(new HashSet<MOB>());
			for(final MOB M : grp)
			{
				if(pmob!=M)
				{
					final Room R=M.location();
					if((R!=null)
					&&(R.getArea()==childA))
						addFollowers.add(M);
				}
			}
		}
		protectTheseMobsList.addAll(addFollowers);
		return protectTheseMobsList;
	}

	protected boolean flushInstance(int index)
	{
		final Area childA=instanceChildren.get(index).A;
		if(childA.getAreaState() != Area.State.ACTIVE) // this is the one and only criteria -- if its not active, flush it.
		{
			final AreaInstanceChild child = instanceChildren.remove(index);
			final Set<MOB> protectedMobsList = getProtectedMobSet(childA, child.mobs);
			for(final MOB wmob : protectedMobsList)
			{
				if((wmob.location()!=null)
				&&(wmob.location().getArea()==childA))
				{
					final Room startRoom=wmob.getStartRoom();
					if(startRoom != null)
					{
						if(wmob.location().isInhabitant(wmob))
							startRoom.bringMobHere(wmob, true);
						if(wmob.location()!=startRoom)
							wmob.setLocation(startRoom);
					}
				}
			}
			final MOB mob=CMClass.getFactoryMOB();
			try
			{
				final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_EXPIRE,null);
				for(final Enumeration<Room> r=childA.getFilledProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					try
					{
						CMLib.map().emptyRoom(R, null, true);
					}
					catch(Exception e)
					{
						Log.errOut(e);
					}
				}
				for(final Enumeration<Room> r=childA.getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					try
					{
						try
						{
							R.clearSky();
							msg.setTarget(R);
							R.executeMsg(mob,msg);
						}
						catch(Exception e)
						{
							Log.errOut(e);
						}
						R.destroy();
					}
					catch(Exception e)
					{
						Log.errOut(e);
					}
				}
				CMLib.map().delArea(childA);
				childA.destroy();
			}
			finally
			{
				mob.destroy();
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void destroy()
	{
		super.destroy();
		if(this.doesManageChildAreas())
		{
			List<Area> cs = new ArrayList<Area>();
			synchronized(instanceChildren)
			{
				for(int i=instanceChildren.size()-1;i>=0;i--)
					cs.add(instanceChildren.get(i).A);
				instanceChildren.clear();
			}
			for(Area A : cs)
			{
				final MOB mob=CMClass.sampleMOB();
				for(final Enumeration<Room> e=A.getProperMap();e.hasMoreElements();)
				{
					final Room R=e.nextElement();
					R.executeMsg(mob,CMClass.getMsg(mob,R,null,CMMsg.MSG_EXPIRE,null));
				}
				CMLib.map().delArea(A);
				A.destroy();
			}
		}
	}

	protected boolean doesManageChildAreas()
	{
		if(CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
			return false;
		return true;
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(!doesManageChildAreas())
			return true;
		if((--childCheckDown)<=0)
		{
			childCheckDown=CMProps.getMillisPerMudHour()/CMProps.getTickMillis();
			synchronized(instanceChildren)
			{
				for(int i=instanceChildren.size()-1;i>=0;i--)
					flushInstance(i);
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
		{
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
			&&(msg.sourceMessage()!=null)
			&&((msg.sourceMajor()&CMMsg.MASK_MAGIC)==0))
			{
				final String said=CMStrings.getSayFromMessage(msg.sourceMessage());
				if("RESET INSTANCE".equalsIgnoreCase(said))
				{
					Room returnToRoom=null;
					final Room thisRoom=msg.source().location();
					if(thisRoom.getArea()==this)
					{
						for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
						{
							final Room R=thisRoom.getRoomInDir(d);
							if((R!=null)&&(R.getArea()!=null)&&(R.getArea()!=this))
								returnToRoom=R;
						}
					}
					if(returnToRoom==null)
					{
						msg.addTrailerMsg(CMClass.getMsg(msg.source(),null,null,CMMsg.MSG_OK_ACTION,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT, L("You must be at an entrance to reset the area.")));
						return;
					}
					final Area A=this.getParentArea();
					if(A instanceof StdThinInstance)
					{
						final StdThinInstance parentA=(StdThinInstance)A;
						synchronized(instanceChildren)
						{
							for(int i=0;i<parentA.instanceChildren.size();i++)
							{
								final List<WeakReference<MOB>> V=parentA.instanceChildren.get(i).mobs;
								if(parentA.instanceChildren.get(i).A==this)
								{
									for(final WeakReference<MOB> wM : V)
									{
										final MOB M=wM.get();
										if((M!=null)
										&&CMLib.flags().isInTheGame(M,true)
										&&(M.location()!=null)
										&&(M.location()!=returnToRoom)
										&&(M.location().getArea()==this))
										{
											returnToRoom.bringMobHere(M, true);
											CMLib.commands().postLook(M, true);
										}
									}
									setAreaState(Area.State.PASSIVE);
									if(flushInstance(i))
										msg.addTrailerMsg(CMClass.getMsg(msg.source(),CMMsg.MSG_OK_ACTION,L("The instance has been reset.")));
									else
										msg.addTrailerMsg(CMClass.getMsg(msg.source(),CMMsg.MSG_OK_ACTION,L("The instance was unable to be reset.")));
									return;
								}
							}
						}
					}
					msg.addTrailerMsg(CMClass.getMsg(msg.source(),CMMsg.MSG_OK_ACTION,L("The instance failed to reset.")));
				}
			}
			else
			if((msg.sourceMinor()==CMMsg.TYP_QUIT)&&(CMLib.map().isHere(msg.source(), this)))
			{
				final MOB mob = msg.source();
				CMLib.tracking().forceRecall(mob, true);
			}
		}
	}

	@Override 
	public int[] getAreaIStats()
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return emptyStats;
		final Area parentArea=getParentArea();
		final String areaName = (parentArea==null)?Name():parentArea.Name();
		int[] statData=(int[])Resources.getResource("STATS_"+areaName.toUpperCase());
		if(statData!=null)
			return statData;
		synchronized(("STATS_"+areaName).intern())
		{
			if(parentArea==null)
			{
				statData=super.getAreaIStats();
				if(statData==emptyStats)
				{
					final Iterator<AreaInstanceChild> childE=instanceChildren.iterator();
					int ct=0;
					if(childE.hasNext())
					{
						statData=new int[Area.Stats.values().length];
						for(;childE.hasNext();)
						{
							final int[] theseStats=childE.next().A.getAreaIStats();
							if(theseStats != emptyStats)
							{
								ct++;
								for(int i=0;i<theseStats.length;i++)
									statData[i]+=theseStats[i];
							}
						}
					}
					if(ct==0)
						return emptyStats;
					for(int i=0;i<statData.length;i++)
						statData[i]=statData[i]/ct;
				}
				Resources.removeResource("HELP_"+areaName.toUpperCase());
				Resources.submitResource("STATS_"+areaName.toUpperCase(),statData);
			}
			else
			{
				return super.getAreaIStats();
			}
		}
		return statData;
	}

	protected boolean doesManageMobLists()
	{
		return CMath.bset(flags(),Area.FLAG_INSTANCE_PARENT);
	}

	protected Area createRedirectArea(MOB mob)
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
		CMLib.map().addArea(newA);
		newA.setAreaState(Area.State.ACTIVE); // starts ticking
		final List<WeakReference<MOB>> newMobList = new SVector<WeakReference<MOB>>(5);
		newMobList.add(new WeakReference<MOB>(mob));
		final AreaInstanceChild child = new AreaInstanceChild(newA,newMobList);
		instanceChildren.add(child);
		return newA;
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(!this.doesManageChildAreas())
			return true;
		if(CMath.bset(flags(),Area.FLAG_INSTANCE_PARENT))
			setAreaState(Area.State.PASSIVE);
		if((msg.sourceMinor()==CMMsg.TYP_ENTER)
		&&(msg.target() instanceof Room)
		&&(doesManageMobLists())
		&&(isRoom((Room)msg.target()))
		//&&(!CMSecurity.isAllowed(msg.source(),(Room)msg.target(),CMSecurity.SecFlag.CMDAREAS)) //TODO: BZ: FiXME!
		&&(((msg.source().getStartRoom()==null)||(msg.source().getStartRoom().getArea()!=this))))
		{
			int myDex=-1;
			Area redirectA = null;
			final Set<MOB> grp = msg.source().getGroupMembers(new HashSet<MOB>());
			synchronized(instanceChildren)
			{
				for(int i=0;i<instanceChildren.size();i++)
				{
					final List<WeakReference<MOB>> V=instanceChildren.get(i).mobs;
					for (final WeakReference<MOB> weakReference : V)
					{
						if(msg.source() == weakReference.get())
						{
							myDex=i; break;
						}
					}
				}
				for(int i=0;i<instanceChildren.size();i++)
				{
					if(i!=myDex)
					{
						final List<WeakReference<MOB>> V=instanceChildren.get(i).mobs;
						for(int v=V.size()-1;v>=0;v--)
						{
							final WeakReference<MOB> wmob=V.get(v);
							if(wmob==null)
								continue;
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
								&&(M.location().getArea()!=instanceChildren.get(i).A))
								{
									V.remove(M);
									instanceChildren.get(myDex).mobs.add(new WeakReference<MOB>(M));
								}
							}
						}
					}
				}
				if(myDex>=0)
					redirectA=instanceChildren.get(myDex).A;
			}
			if(myDex<0)
				redirectA = createRedirectArea(msg.source());
			if(redirectA instanceof StdThinInstance)
			{
				Room R=redirectA.getRoom(((StdThinInstance)redirectA).convertToMyArea(CMLib.map().getExtendedRoomID((Room)msg.target())));
				int tries=1000;
				while((R==null)&&((--tries)>0))
				{
					R=redirectA.getRandomProperRoom();
					if(R!=null)
					{
						msg.setTarget(R);
						if((!CMLib.flags().canAccess(msg.source(),R))
						||(CMLib.law().getLandTitle(R)!=null)
						||(!R.okMessage(msg.source(), msg)))
							R=null;
					}
				}
				if(R!=null)
					msg.setTarget(R);
			}
		}
		return true;
	}
}
