package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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

/*
   Copyright 2003-2025 Bo Zimmerman

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
public class Prop_RoomForSale extends Property implements LandTitle
{
	@Override
	public String ID()
	{
		return "Prop_RoomForSale";
	}

	@Override
	public String name()
	{
		return "Putting a room up for sale";
	}

	protected static String	INDOORSTR	= null;
	protected static String	OUTDOORSTR	= null;
	protected static String	SALESTR		= null;
	protected static String	RENTSTR		= null;

	protected int		lastItemNums	= -1;
	protected int		lastDayDone		= -1;
	protected int		daysWithNoChange= 0;
	protected boolean	scheduleReset	= false;

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	public String accountForYourself()
	{
		return "For Sale";
	}

	@Override
	public boolean allowsExpansionConstruction()
	{
		return false;
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
	}

	@Override
	public int getPrice()
	{
		if(text().length()==0)
			return 100000;
		final String s=text();
		int index=s.length();
		while((--index)>=0)
		{
			if((!Character.isDigit(s.charAt(index)))
			&&(!Character.isWhitespace(s.charAt(index))))
				break;
		}
		int price=CMath.s_int(s.substring(index+1).trim());

		if(price<=0)
			price=100000;
		return price;
	}

	@Override
	public Room getAConnectedPropertyRoom()
	{
		if(affected instanceof Room)
			return (Room)affected;
		return CMLib.map().getRoom(landPropertyID());
	}

	@Override
	public int getNumConnectedPropertyRooms()
	{
		return (getAConnectedPropertyRoom()!=null)?1:0;
	}

	@Override
	public void initializeClass()
	{
		if(INDOORSTR == null)
		{
			final String[] markers = CMProps.getListFileStringList(CMProps.ListFile.REALESTATE_MARKERS);
			INDOORSTR=" "+((markers.length>0)?markers[0].trim():"");
			OUTDOORSTR=" "+((markers.length>1)?markers[1].trim():"");
			SALESTR=" "+((markers.length>2)?markers[2].trim():"");
			RENTSTR=" "+((markers.length>3)?markers[3].trim():"");
		}
	}

	protected void saveData(final String owner, final int price, final boolean rental, final int backTaxes, final boolean grid, final boolean allowTheft)
	{
		setMiscText(owner+"/"
				+(rental?"RENTAL ":"")
				+(grid?"GRID ":"")
				+(allowTheft?"ALLOWTHEFT ":"")
				+((backTaxes>0)?"TAX"+backTaxes+"X ":"")
				+price);
	}

	@Override
	public void setPrice(final int price)
	{
		saveData(getOwnerName(), price, rentalProperty(), backTaxes(), gridLayout(), allowTheft());
	}

	@Override
	public String getOwnerName()
	{
		final int dex=text().indexOf('/');
		if(dex<0)
			return "";
		return text().substring(0,dex);
	}

	@Override
	public boolean isProperlyOwned()
	{
		final String owner=getOwnerName();
		if(owner.length()==0)
			return false;
		final Clan C=CMLib.clans().fetchClanAnyHost(owner);
		if(C!=null)
			return true;
		return CMLib.players().playerExistsAllHosts(owner);
	}

	@Override
	public void setOwnerName(final String owner)
	{
		if((owner.length()==0)&&(getOwnerName().length()>0))
			scheduleReset=true;
		saveData(owner, getPrice(), rentalProperty(), backTaxes(), gridLayout(), allowTheft());
	}

	@Override
	public int backTaxes()
	{
		final int dex=text().indexOf('/');
		if(dex<0)
			return 0;
		final int x=text().indexOf("TAX",dex);
		if(x<0)
			return 0;
		final String s=CMParms.parse(text().substring(x+3)).firstElement();
		return CMath.s_int(s.substring(0,s.length()-1)); // last char always X, so eat it
	}

	@Override
	public void setBackTaxes(final int tax)
	{
		saveData(getOwnerName(), getPrice(), rentalProperty(), tax, gridLayout(), allowTheft());
	}

	@Override
	public boolean rentalProperty()
	{
		final String upperText=text().toUpperCase();
		final int dex=upperText.indexOf('/');
		if(dex<0)
			return upperText.indexOf("RENTAL")>=0;
		return upperText.indexOf("RENTAL",dex)>0;
	}

	@Override
	public void setRentalProperty(final boolean truefalse)
	{
		saveData(getOwnerName(), getPrice(), truefalse, backTaxes(), gridLayout(), allowTheft());
	}

	@Override
	public boolean gridLayout()
	{
		final String upperText=text().toUpperCase();
		final int dex=upperText.indexOf('/');
		if(dex<0)
			return upperText.indexOf("GRID")>=0;
		return upperText.indexOf("GRID",dex)>0;
	}

	@Override
	public void setGridLayout(final boolean layout)
	{
		saveData(getOwnerName(), getPrice(), rentalProperty(), backTaxes(), layout, allowTheft());
	}

	@Override
	public boolean allowTheft()
	{
		final String upperText=text().toUpperCase();
		final int dex=upperText.indexOf('/');
		if(dex<0)
			return upperText.indexOf("ALLOWTHEFT")>=0;
		return upperText.indexOf("ALLOWTHEFT",dex)>=0;
	}

	@Override
	public void setAllowTheft(final boolean allow)
	{
		saveData(getOwnerName(), getPrice(), rentalProperty(), backTaxes(), gridLayout(), allow);
	}

	// update title, since it may affect clusters, worries about ALL involved
	@Override
	public void updateTitle()
	{
		if(affected instanceof Room)
			CMLib.database().DBUpdateRoom((Room)affected);
		else
		{
			final Room R=CMLib.map().getRoom(landPropertyID());
			if(R!=null)
				CMLib.database().DBUpdateRoom(R);
		}
	}

	@Override
	public String getTitleID()
	{
		if(affected instanceof Room)
			return "LAND_TITLE_FOR#"+CMLib.map().getExtendedRoomID((Room)affected);
		else
		{
			final Room R=CMLib.map().getRoom(landPropertyID());
			if(R!=null)
				return "LAND_TITLE_FOR#"+CMLib.map().getExtendedRoomID(R);
		}
		return "";
	}

	@Override
	public String getUniqueLotID()
	{
		return "ROOM_PROPERTY_" + landPropertyID();
	}

	@Override
	public String landPropertyID()
	{
		if((affected instanceof Room))
			return CMLib.map().getExtendedRoomID(((Room)affected));
		return "";
	}

	@Override
	public void setLandPropertyID(final String landID)
	{
	}

	@Override
	public LandTitle generateNextRoomTitle()
	{
		final LandTitle newTitle=(LandTitle)this.copyOf();
		newTitle.setOwnerName("");
		newTitle.setBackTaxes(0);
		return newTitle;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
			||((msg.targetMinor()==CMMsg.TYP_EXPIRE)
				&&(msg.target()==affected))
			||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET))
		&&(affected instanceof Room))
		{
			updateLot(null);
			final List<MOB> mobs=new ArrayList<MOB>();
			Room R=(Room)affected;
			if(R!=null)
			{
				synchronized(CMClass.getSync("SYNC"+R.roomID()))
				{
					R=CMLib.map().getRoom(R);
					for(int m=0;m<R.numInhabitants();m++)
					{
						final MOB M=R.fetchInhabitant(m);
						if((M!=null)
						&&(M.isSavable())
						&&(M.getStartRoom()==R)
						&&((M.basePhyStats().rejuv()==0)||(M.basePhyStats().rejuv()==PhyStats.NO_REJUV)))
						{
							CMLib.catalog().updateCatalogIntegrity(M);
							mobs.add(M);
						}
					}
					if(!CMSecurity.isSaveFlag(CMSecurity.SaveFlag.NOPROPERTYMOBS))
						CMLib.database().DBUpdateTheseMOBs(R,mobs);
				}
			}
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(!CMLib.law().robberyCheck(this,msg, false))
			return false;
		return true;
	}

	@Override
	public Room getATitledRoom()
	{
		if(affected instanceof Room)
			return (Room)affected;
		else
			return CMLib.map().getRoom(landPropertyID());
	}

	protected void fillCluster(final Room startR, final List<Room> roomList, final String owner, final boolean forceCache)
	{
		roomList.add(startR);
		int start =0;
		final Area baseA =startR.getArea();
		boolean foundEntrance=false;
		final boolean dontCache = CMath.bset(baseA.flags(), Area.FLAG_THIN) && (!CMath.bset(baseA.flags(), Area.FLAG_INSTANCE_CHILD));
		final Set<String> roomIDs = new TreeSet<String>();
		roomIDs.add(startR.roomID());
		final DatabaseEngine db = CMLib.database();
		Exit openE = null;
		while(start < roomList.size())
		{
			final Room dR = roomList.get(start++);
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				final Room nR=(dontCache?dR.rawDoors()[d]:dR.getRoomInDir(d));
				if((nR!=null)
				&&(nR.roomID().length()>0)
				&&(!roomIDs.contains(nR.roomID())))
				{
					roomIDs.add(nR.roomID());
					final Area nRarea=nR.getArea();
					Ability lotA=null;
					if(nR.ID().equals("ThinRoom")
					&&(nRarea == baseA))
					{
						if(!forceCache)
							continue;
						final Room nAR=db.DBReadRoomObject(nR.roomID(), true, false); // wont have an area!
						if(nAR==null)
							continue;
						lotA=nAR.fetchEffect(ID());
						final Pair<String,String>[] exits = db.DBReadRoomExitIDs(nR.roomID());
						for(int nd=0;nd<exits.length;nd++)
						{
							final Pair<String,String> p = exits[nd];
							if(p != null)
							{
								final String exitId = p.second;
								if((p.first!=null)
								&&(p.first.length()>0))
								{
									if(openE == null)
										openE = CMClass.getExit("Open");
									nR.setRawExit(nd, openE); // this makes the thin room modifiable
								}
								else
									nR.setRawExit(nd, null); // this makes the thin room modifiable
								final Room nnR = CMLib.map().getCachedRoom(exitId);
								if(nnR != null)
									nR.rawDoors()[nd]=nnR;
								else
								{
									final Area A = CMLib.map().findRoomIDArea(exitId);
									final Room tR = CMClass.getLocale("ThinRoom");
									tR.setRoomID(exitId);
									tR.setArea(A);
									nR.rawDoors()[nd]=tR;
								}
							}
						}
					}
					else
						lotA=nR.fetchEffect(ID());
					if(((nRarea==baseA)
					&&(lotA!=null)
					&&((owner==null)||((LandTitle)lotA).getOwnerName().equals(owner))))
						roomList.add(nR); // this will keep the list growing, as well as grow the list
					else
					if(!foundEntrance)
					{
						foundEntrance=true;
						roomList.remove(dR);// purpose here is to put the "front" door up front.
						roomList.add(0,dR);// purpose here is to put the "front" door up front.
					}
				}
			}
		}
	}

	@Override
	public List<Room> getTitledRooms()
	{
		final Room R = getATitledRoom();
		if(R!=null)
			return new XVector<Room>(R);
		return new Vector<Room>(1);
	}

	@Override
	public int getNumTitledRooms()
	{
		return getATitledRoom() != null ? 1 : 0;
	}

	public static int[] updateLotWithThisData(Room R,
											  final LandTitle T,
											  final boolean resetRoomName,
											  final boolean clearAllItems,
											  final Set<String> optPlayerList,
											  final int lastNumItems,
											  int daysSinceItemsSaved)
	{
		boolean updateItems=false;
		boolean updateExits=false;
		boolean updateRoom=false;
		synchronized(CMClass.getSync("SYNC"+R.roomID()))
		{
			R=CMLib.map().getRoom(R);
			if(R==null)
				return new int[] {-1,0};
			if(T.getOwnerName().length()==0)
			{
				Item I=null;
				for(int i=R.numItems()-1;i>=0;i--)
				{
					I=R.getItem(i);
					if((I==null)
					||(I.Name().equalsIgnoreCase("id")))
						continue;
					CMLib.catalog().updateCatalogIntegrity(I);
					if(clearAllItems)
					{
						I.destroy();
						updateItems=true;
					}
					else
					{
						if(I.expirationDate()==0)
						{
							long now=System.currentTimeMillis();
							now+=(TimeManager.MILI_MINUTE*CMProps.getIntVar(CMProps.Int.EXPIRE_PLAYER_DROP));
							I.setExpirationDate(now);
						}
						if((I.phyStats().rejuv()!=PhyStats.NO_REJUV)
						&&(I.phyStats().rejuv()!=0))
						{
							I.basePhyStats().setRejuv(PhyStats.NO_REJUV);
							I.recoverPhyStats();
						}
					}
				}
				Ability A=null;
				if(clearAllItems)
				{
					for(final Enumeration<Ability> a=R.effects();a.hasMoreElements();)
					{
						A=a.nextElement();
						if(((A!=null)
						&&((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_PROPERTY)))
						{
							A.unInvoke();
							R.delEffect(A);
							updateRoom=true;
						}
					}
				}
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room R2=R.rawDoors()[d];
					Exit E=R.getRawExit(d);
					if((E!=null)
					&&(E.hasALock())
					&&(E.isGeneric()))
					{
						E.setKeyName("");
						E.setDoorsNLocks(E.hasADoor(),E.isOpen(),E.defaultsClosed(),false,false,false);
						updateExits=true;
						if(R2!=null)
						{
							E=R2.getRawExit(Directions.getOpDirectionCode(d));
							if((E!=null)
							&&(E.hasALock())
							&&(E.isGeneric()))
							{
								E.setKeyName("");
								E.setDoorsNLocks(E.hasADoor(),E.isOpen(),E.defaultsClosed(),false,false,false);
								CMLib.database().DBUpdateExits(R2);
								R2.getArea().fillInAreaRoom(R2);
							}
						}
					}
				}
				if(updateExits)
				{
					CMLib.database().DBUpdateExits(R);
					R.getArea().fillInAreaRoom(R);
				}
				if(updateItems)
					CMLib.database().DBUpdateItems(R);
				if(updateRoom)
					CMLib.database().DBUpdateRoom(R);
				CMLib.law().colorRoomForSale(R,T,resetRoomName);
				return new int[] {-1, 0};
			}

			if((T.getOwnerName().length()>0)
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.PROPERTYOWNERCHECKS)))
			{
				boolean playerExists = false;
				if(optPlayerList != null)
					playerExists=optPlayerList.contains(T.getOwnerName());
				if(!playerExists)
				{
					playerExists=(CMLib.players().playerExistsAllHosts(T.getOwnerName()));
					if(!playerExists)
						playerExists=(CMLib.clans().getClanAnyHost(T.getOwnerName())!=null);
					if(playerExists && (optPlayerList != null))
						optPlayerList.add(T.getOwnerName()); // dynamic updating, whee!
				}
				if(!playerExists)
				{
					String id=T.getTitleID();
					if((id==null)||(id.equalsIgnoreCase("null")))
						id=CMLib.map().getExtendedRoomID(R);
					Log.warnOut("Property owned by non-existant player "+T.getOwnerName()+" is now lost: "+id);
					T.setOwnerName("");
					T.updateLot(null);
					CMLib.database().DBUpdateRoom(R);
					return new int[] {-1, 0};
				}
			}

			int x=R.description().indexOf(SALESTR);
			if(x>=0)
			{
				R.setDescription(R.description().substring(0,x));
				CMLib.database().DBUpdateRoom(R);
			}
			x=R.description().indexOf(RENTSTR);
			if(x>=0)
			{
				R.setDescription(R.description().substring(0,x));
				CMLib.database().DBUpdateRoom(R);
			}

			// this works on the principle that
			// 1. if an item has ONLY been removed, the lastNumItems will be != current # items
			// 2. if an item has ONLY been added, the dispossessiontime will be != null
			// 3. if an item has been added AND removed, the dispossession time will be != null on the added
			if((lastNumItems>=0)
			&&(R.numItems()!=lastNumItems))
				updateItems=true;

			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
				if(I!=null)
				{
					if((I.expirationDate()!=0)
					&&((I.isSavable())||(I.Name().equalsIgnoreCase("id")))
					&&((!(I instanceof DeadBody))||(((DeadBody)I).isPlayerCorpse())))
					{
						I.setExpirationDate(0);
						updateItems=true;
					}

					if((I.phyStats().rejuv()!=PhyStats.NO_REJUV)
					&&(I.phyStats().rejuv()!=0))
					{
						I.basePhyStats().setRejuv(PhyStats.NO_REJUV);
						I.recoverPhyStats();
						updateItems=true;
					}
				}
			}
			if(!updateItems)
			{
				if(daysSinceItemsSaved>1)
				{
					final TimeClock C=R.getArea().getTimeObj();
					if(C!=null)
					{
						final long t0 = C.getMonthsInYear() * C.getDaysInMonth() * C.getHoursInDay();
						final long t = t0 * CMProps.getMillisPerMudHour() / MudHost.TIME_SAVETHREAD_SLEEP;

						final CMFlagLibrary flags=CMLib.flags();
						if(daysSinceItemsSaved > t)
						{
							daysSinceItemsSaved=-1;
							if(!CMLib.flags().isWateryRoom(R))
							{
								for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
								{
									final Item I=i.nextElement();
									if((I!=null)
									&&(I.container()==null)
									&&(flags.isGettable(I))
									&&(flags.isSavable(I)))
									{
										if((I.numEffects()==0)||(I.fetchEffect("Dusty")==null))
										{
											final Ability A=CMClass.getAbility("Dusty");
											if(A!=null)
											{
												A.setMiscText("LEVEL=0 INTERVAL="+t0);
												I.addNonUninvokableEffect(A);
												updateItems=true;
											}
										}
									}
								}
							}
						}
					}
				}
			}
			if((!CMSecurity.isSaveFlag(CMSecurity.SaveFlag.NOPROPERTYITEMS))
			&&(updateItems))
				CMLib.database().DBUpdateItems(R);
		}
		return new int[] {R.numItems(), updateItems?0:(daysSinceItemsSaved+1)};
	}

	@SuppressWarnings("unchecked")
	public static boolean doRentalProperty(final Area A, final String ID, final String owner, final int rent)
	{
		if(!CMProps.isState(CMProps.HostState.RUNNING))
			return false;
		final int month=A.getTimeObj().getMonth();
		final int day=A.getTimeObj().getDayOfMonth();
		final int year=A.getTimeObj().getYear();
		final Object O=Resources.getResource("RENTAL INFO/"+owner);
		List<PlayerData> pDataV=null;
		if(O instanceof List)
			pDataV=(List<PlayerData>)O;
		else
			pDataV=CMLib.database().DBReadPlayerData(owner,"RENTAL INFO");
		if(pDataV==null)
			pDataV=new Vector<PlayerData>();
		DatabaseEngine.PlayerData pData = null;
		if(pDataV.size()==0)
		{
			final String section="RENTAL INFO";
			final String key="RENTAL INFO/"+owner;
			final String xml=ID+"|~>|"+day+" "+month+" "+year+"|~;|";
			pData = CMLib.database().DBCreatePlayerData(owner,section,key,xml);
			pDataV.add(pData);
			Resources.submitResource("RENTAL INFO/"+owner,pDataV);
			return false;
		}
		else
		if(pDataV.get(0) != null)
		{
			pData=pDataV.get(0);
			String parse=pData.xml();
			int x=parse.indexOf("|~;|");
			final StringBuffer reparse=new StringBuffer("");
			boolean changesMade=false;
			boolean needsToPay=false;
			while(x>=0)
			{
				String thisOne=parse.substring(0,x);
				if(thisOne.startsWith(ID+"|~>|"))
				{
					thisOne=thisOne.substring((ID+"|~>|").length());
					final Vector<String> dateV=CMParms.parse(thisOne);
					if(dateV.size()==3)
					{
						int lastYear=CMath.s_int(dateV.lastElement());
						int lastMonth=CMath.s_int(dateV.elementAt(1));
						final int lastDay=CMath.s_int(dateV.firstElement());
						while(!needsToPay)
						{
							if(lastYear<year)
								needsToPay=true;
							else
							if((lastYear==year)&&(lastMonth<month)&&(day>=lastDay))
								needsToPay=true;
							if(needsToPay)
							{
								if(CMLib.beanCounter().modifyLocalBankGold(A,
									owner,
									CMLib.utensils().getFormattedDate(A)+":Withdrawal of "+rent+": Rent for "+ID,
									(-rent)))
								{
									lastMonth++;
									if(lastMonth>A.getTimeObj().getMonthsInYear())
									{
										lastMonth=1;
										lastYear++;
									}
									changesMade=true;
									needsToPay=false;
								}
							}
							else
								break;
						}
						if(changesMade)
							reparse.append(ID+"|~>|"+lastDay+" "+lastMonth+" "+lastYear+"|~;|");
						if(needsToPay&&(!changesMade))
							return true;
					}
				}
				else
					reparse.append(thisOne+"|~;|");
				parse=parse.substring(x+4);
				x=parse.indexOf("|~;|");
			}
			if(changesMade)
			{
				pData = CMLib.database().DBReCreatePlayerData(owner,"RENTAL INFO","RENTAL INFO/"+owner,reparse.toString());
				Resources.removeResource("RENTAL INFO/"+owner);
				if(pData != null)
				{
					pDataV.set(0,pData);
					Resources.submitResource("RENTAL INFO/"+owner,pDataV);
				}
			}
			return needsToPay;
		}
		return false;
	}

	// update lot, since its called by the savethread, ONLY worries about itself
	@Override
	public void updateLot(final Set<String> optPlayerList)
	{
		if(affected instanceof Room)
		{
			Room R=(Room)affected;
			synchronized(CMClass.getSync("SYNC"+R.roomID()))
			{
				R=CMLib.map().getRoom(R);
				int[] data=updateLotWithThisData(R,this,false,scheduleReset,optPlayerList,lastItemNums,daysWithNoChange);
				lastItemNums=data[0];
				daysWithNoChange=data[1];

				// rentals are below
				if((lastDayDone!=R.getArea().getTimeObj().getDayOfMonth())
				&&(CMProps.isState(CMProps.HostState.RUNNING)))
				{
					lastDayDone=R.getArea().getTimeObj().getDayOfMonth();
					if((getOwnerName().length()>0)
					&&rentalProperty()
					&&(R.roomID().length()>0))
					{
						if(doRentalProperty(R.getArea(),R.roomID(),getOwnerName(),getPrice()))
						{
							setOwnerName("");
							CMLib.database().DBUpdateRoom(R);
							data=updateLotWithThisData(R,this,false,scheduleReset,optPlayerList,lastItemNums,daysWithNoChange);
							lastItemNums=data[0];
							daysWithNoChange=data[1];
						}
					}
				}
				scheduleReset=false;
			}
		}
	}
}
