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
@SuppressWarnings({"unchecked","rawtypes"})
public class Prop_RoomForSale extends Property implements LandTitle
{
	public String ID() { return "Prop_RoomForSale"; }
	public String name(){ return "Putting a room up for sale";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}

	public final static String SALESTR=" This lot is for sale (look id).";
	public final static String RENTSTR=" This lot (look id) is for rent on a monthly basis.";
	public final static String INDOORSTR=" An empty room";
	public final static String OUTDOORSTR=" An empty plot";
	protected int lastItemNums=-1;
	protected int lastDayDone=-1;
	protected boolean scheduleReset=false;

	public String accountForYourself()
	{ return "For Sale";	}

	public boolean allowsExpansionConstruction(){ return false;}
	
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
	}

	public int landPrice()
	{
		if(text().length()==0)
			return 100000;
		String s=text();
		int index=s.length();
		while((--index)>=0)
		{
			if((!Character.isDigit(s.charAt(index)))
			&&(!Character.isWhitespace(s.charAt(index))))
				break;
		}
		int price=CMath.s_int(s.substring(index+1).trim());

		if(price<=0) price=100000;
		return price;
	}

	public List<Room> getConnectedPropertyRooms() { return getAllTitledRooms();}

	public void setLandPrice(int price)
	{
		setMiscText(landOwner()+"/"
			+(rentalProperty()?"RENTAL ":"")
			+((backTaxes()!=0)?"TAX"+backTaxes()+"X ":"")
			+price);
	}

	public String landOwner()
	{
		final int dex=text().indexOf('/');
		if(dex<0) return "";
		return text().substring(0,dex);
	}

	public CMObject landOwnerObject()
	{
		String owner=landOwner();
		if(owner.length()==0) return null;
		Clan C=CMLib.clans().getClan(owner);
		if(C!=null) return C;
		return CMLib.players().getLoadPlayer(owner);
	}
	
	public void setLandOwner(String owner)
	{
		if((owner.length()==0)&&(landOwner().length()>0))
			scheduleReset=true;
		setMiscText(owner+"/"
				+(rentalProperty()?"RENTAL ":"")
				+((backTaxes()!=0)?"TAX"+backTaxes()+"X ":"")
				+landPrice());
	}

	public int backTaxes()
	{
		final int dex=text().indexOf('/');
		if(dex<0) return 0;
		final int x=text().indexOf("TAX",dex);
		if(x<0) return 0;
		String s=CMParms.parse(text().substring(x+3)).firstElement();
		return CMath.s_int(s.substring(0,s.length()-1));
	}
	public void setBackTaxes(int tax)
	{
		setMiscText(landOwner()+"/"
				+(rentalProperty()?"RENTAL ":"")
				+((tax!=0)?"TAX"+tax+"X ":"")
				+landPrice());
	}

	public boolean rentalProperty()
	{
		final String upperText=text().toUpperCase();
		final int dex=upperText.indexOf('/');
		if(dex<0) return upperText.indexOf("RENTAL")>=0;
		return upperText.indexOf("RENTAL",dex)>0;
	}

	public void setRentalProperty(boolean truefalse)
	{
		setMiscText(landOwner()+"/"
				+(truefalse?"RENTAL ":"")
				+((backTaxes()!=0)?"TAX"+backTaxes()+"X ":"")
				+landPrice());
	}

	// update title, since it may affect clusters, worries about ALL involved
	public void updateTitle()
	{
		if(affected instanceof Room)
			CMLib.database().DBUpdateRoom((Room)affected);
		else
		{
			Room R=CMLib.map().getRoom(landPropertyID());
			if(R!=null) CMLib.database().DBUpdateRoom(R);
		}
	}

	public String getTitleID()
	{
		if(affected instanceof Room)
			return "LAND_TITLE_FOR#"+CMLib.map().getExtendedRoomID((Room)affected);
		else
		{
			Room R=CMLib.map().getRoom(landPropertyID());
			if(R!=null) 
				return "LAND_TITLE_FOR#"+CMLib.map().getExtendedRoomID(R);
		}
		return "";
	}
	
	public String getUniqueLotID(){ return "ROOM_PROPERTY_"+landPropertyID();}

	public String landPropertyID()
	{
		if((affected!=null)&&(affected instanceof Room))
			return CMLib.map().getExtendedRoomID(((Room)affected));
		return "";
	}

	public void setLandPropertyID(String landID){}

	public static boolean shopkeeperMobPresent(Room R)
	{
		if(R==null) return false;
		MOB M=null;
		for(int i=0;i<R.numInhabitants();i++)
		{
			M=R.fetchInhabitant(i);
			if((M.getStartRoom()==R)
			&&(M.isMonster())
			&&(CMLib.coffeeShops().getShopKeeper(M)!=null))
				return true;
		}
		return false;
	}

	public static boolean robberyCheck(LandTitle A, CMMsg msg)
	{
		if(msg.targetMinor()==CMMsg.TYP_GET)
		{
			if((msg.target() instanceof Item)
			&&(((Item)msg.target()).owner() ==msg.source().location())
			&&((!(msg.tool() instanceof Item))||(msg.source().isMine(msg.tool())))
			&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
			&&(A.landOwner().length()>0)
			&&(msg.source().location()!=null)
			&&(msg.othersMessage()!=null)
			&&(msg.othersMessage().length()>0)
			&&(!shopkeeperMobPresent(msg.source().location()))
			&&(!CMLib.law().doesHavePriviledgesHere(msg.source(),msg.source().location())))
			{
				Room R=msg.source().location();
				LegalBehavior B=CMLib.law().getLegalBehavior(R);
				if(B!=null)
				{
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if(CMLib.law().doesHavePriviledgesHere(M,R))
							return true;
					}
					MOB D=null;
					Clan C=CMLib.clans().getClan(A.landOwner());
					if(C!=null)
						D=C.getResponsibleMember();
					else
						D=CMLib.players().getLoadPlayer(A.landOwner());
					if(D==null) return true;
					B.accuse(CMLib.law().getLegalObject(R),msg.source(),D,new String[]{"PROPERTYROB","THIEF_ROBBERY"});
				}
			}
			return true;
		}
		return false;
	}

	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
				||((msg.targetMinor()==CMMsg.TYP_EXPIRE)&&(msg.target()==affected))
				||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET))
		&&(affected instanceof Room))
		{
			updateLot(null);
			Vector mobs=new Vector();
			Room R=(Room)affected;
			if(R!=null)
			{
				synchronized(("SYNC"+R.roomID()).intern())
				{
					R=CMLib.map().getRoom(R);
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
						if((M!=null)
						&&(M.isSavable())
						&&(M.getStartRoom()==R)
						&&((M.basePhyStats().rejuv()==0)||(M.basePhyStats().rejuv()==PhyStats.NO_REJUV)))
						{
							CMLib.catalog().updateCatalogIntegrity(M);
							mobs.addElement(M);
						}
					}
					if(!CMSecurity.isSaveFlag("NOPROPERTYMOBS"))
						CMLib.database().DBUpdateTheseMOBs(R,mobs);
				}
			}
		}
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		Prop_RoomForSale.robberyCheck(this,msg);
		return true;
	}

	public static void colorForSale(Room R, boolean rental, boolean reset)
	{
		synchronized(("SYNC"+R.roomID()).intern())
		{
			R=CMLib.map().getRoom(R);
			String theStr=rental?RENTSTR:SALESTR;
			String otherStr=rental?SALESTR:RENTSTR;
			int x=R.description().indexOf(otherStr);
			while(x>=0)
			{
				R.setDescription(R.description().substring(0,x));
				CMLib.database().DBUpdateRoom(R);
				x=R.description().indexOf(otherStr);
			}
			if((R.description().indexOf(theStr.trim())<0)||(!R.displayText().equals(CMath.bset(R.domainType(), Room.INDOORS)?INDOORSTR:OUTDOORSTR)))
			{
				if(reset)
				{
					R.setDisplayText(CMath.bset(R.domainType(), Room.INDOORS)?INDOORSTR:OUTDOORSTR);
					R.setDescription("");
				}
				R.setDescription(R.description()+theStr);
				CMLib.database().DBUpdateRoom(R);
			}
			Item I=R.findItem(null,"$id$");
			if((I==null)||(!I.ID().equals("GenWallpaper")))
			{
				I=CMClass.getItem("GenWallpaper");
				CMLib.flags().setReadable(I,true);
				I.setName("id");
				I.setReadableText("This room is "+CMLib.map().getExtendedRoomID(R));
				I.setDescription("This room is "+CMLib.map().getExtendedRoomID(R));
				R.addItem(I);
				CMLib.database().DBUpdateItems(R);
			}
		}
	}

	public List<Room> getAllTitledRooms()
	{
		List<Room> V=new Vector();
		if(affected instanceof Room)
			V.add((Room)affected);
		else
		{
			Room R=CMLib.map().getRoom(landPropertyID());
			if(R!=null) V.add(R);
		}
		return V;
	}

	public static int updateLotWithThisData(Room R,
											LandTitle T,
											boolean resetRoomName,
											boolean clearAllItems,
											List optPlayerList,
											int lastNumItems)
	{
		boolean updateItems=false;
		boolean updateExits=false;
		boolean updateRoom=false;
		synchronized(("SYNC"+R.roomID()).intern())
		{
			R=CMLib.map().getRoom(R);
			if(T.landOwner().length()==0)
			{
				Item I=null;
				for(int i=R.numItems()-1;i>=0;i--)
				{
					I=R.getItem(i);
					if((I==null)||(I.Name().equalsIgnoreCase("id"))) continue;
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
							now+=(TimeManager.MILI_MINUTE*CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
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
					for(final Enumeration<Ability> a=R.effects();a.hasMoreElements();)
					{
						A=a.nextElement();
						if(((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_PROPERTY)))
						{
							A.unInvoke();
							R.delEffect(A);
							updateRoom=true;
						}
				}
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					Room R2=R.rawDoors()[d];
					Exit E=R.getRawExit(d);
					if((E!=null)&&(E.hasALock())&&(E.isGeneric()))
					{
						E.setKeyName("");
						E.setDoorsNLocks(E.hasADoor(),E.isOpen(),E.defaultsClosed(),false,false,false);
						updateExits=true;
						if(R2!=null)
						{
							E=R2.getRawExit(Directions.getOpDirectionCode(d));
							if((E!=null)&&(E.hasALock())&&(E.isGeneric()))
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
				colorForSale(R,T.rentalProperty(),resetRoomName);
				return -1;
			}

			if((lastNumItems<0)
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.PROPERTYOWNERCHECKS))
			&&(optPlayerList!=null))
			{
				boolean playerExists=(CMLib.players().getPlayer(T.landOwner())!=null);
				if(!playerExists) playerExists=(CMLib.clans().getClan(T.landOwner())!=null);
				if(!playerExists) playerExists=optPlayerList.contains(T.landOwner());
				if(!playerExists)
				for(int i=0;i<optPlayerList.size();i++)
					if(((String)optPlayerList.get(i)).equalsIgnoreCase(T.landOwner()))
					{ playerExists=true; break;}
				if(!playerExists)
				{
					T.setLandOwner("");
					T.updateLot(null);
					return -1;
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

			// this works on the priciple that
			// 1. if an item has ONLY been removed, the lastNumItems will be != current # items
			// 2. if an item has ONLY been added, the dispossessiontime will be != null
			// 3. if an item has been added AND removed, the dispossession time will be != null on the added
			if((lastNumItems>=0)&&(R.numItems()!=lastNumItems))
				updateItems=true;

			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.getItem(i);
				if((I.expirationDate()!=0)
				&&((I.isSavable())||(I.Name().equalsIgnoreCase("id")))
				&&((!(I instanceof DeadBody))||(((DeadBody)I).playerCorpse())))
				{
					I.setExpirationDate(0);
					updateItems=true;
				}

				if((I.phyStats().rejuv()!=Integer.MAX_VALUE)
				&&(I.phyStats().rejuv()!=0))
				{
					I.basePhyStats().setRejuv(PhyStats.NO_REJUV);
					I.recoverPhyStats();
					updateItems=true;
				}
			}
			lastNumItems=R.numItems();
			if((!CMSecurity.isSaveFlag("NOPROPERTYITEMS"))
			&&(updateItems))
				CMLib.database().DBUpdateItems(R);
		}
		return lastNumItems;
	}

	public static boolean doRentalProperty(Area A, String ID, String owner, int rent)
	{
		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return false;
		int month=A.getTimeObj().getMonth();
		int day=A.getTimeObj().getDayOfMonth();
		int year=A.getTimeObj().getYear();
		Object O=Resources.getResource("RENTAL INFO/"+owner);
		List<PlayerData> pDataV=null;
		if(O instanceof List)
			pDataV=(List<PlayerData>)O;
		else
			pDataV=CMLib.database().DBReadData(owner,"RENTAL INFO");
		if(pDataV==null)
			pDataV=new Vector();
		DatabaseEngine.PlayerData pData = null;
		if(pDataV.size()==0)
		{
			pData = new DatabaseEngine.PlayerData();
			pData.who=owner;
			pData.section="RENTAL INFO";
			pData.key="RENTAL INFO/"+owner;
			pData.xml=ID+"|~>|"+day+" "+month+" "+year+"|~;|";
			CMLib.database().DBCreateData(owner,"RENTAL INFO","RENTAL INFO/"+owner,pData.xml);
			pDataV.add(pData);
			Resources.submitResource("RENTAL INFO/"+owner,pDataV);
			return false;
		}
		else
		if(pDataV.get(0) != null)
		{
			pData=pDataV.get(0);
			String parse=pData.xml;
			int x=parse.indexOf("|~;|");
			StringBuffer reparse=new StringBuffer("");
			boolean changesMade=false;
			boolean needsToPay=false;
			while(x>=0)
			{
				String thisOne=parse.substring(0,x);
				if(thisOne.startsWith(ID+"|~>|"))
				{
					thisOne=thisOne.substring((ID+"|~>|").length());
					Vector dateV=CMParms.parse(thisOne);
					if(dateV.size()==3)
					{
						int lastYear=CMath.s_int((String)dateV.lastElement());
						int lastMonth=CMath.s_int((String)dateV.elementAt(1));
						int lastDay=CMath.s_int((String)dateV.firstElement());
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
										CMLib.beanCounter().getCurrency(A),
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
				CMLib.database().DBReCreateData(owner,"RENTAL INFO","RENTAL INFO/"+owner,reparse.toString());
				pData = new DatabaseEngine.PlayerData();
				pData.who=owner;
				pData.section="RENTAL INFO";
				pData.key="RENTAL INFO/"+owner;
				pData.xml=reparse.toString();
				pDataV.set(0,pData);
				Resources.removeResource("RENTAL INFO/"+owner);
				Resources.submitResource("RENTAL INFO/"+owner,pDataV);
			}
			return needsToPay;
		}
		return false;
	}

	// update lot, since its called by the savethread, ONLY worries about itself
	public void updateLot(List optPlayerList)
	{
		if(affected instanceof Room)
		{
			Room R=(Room)affected;
			synchronized(("SYNC"+R.roomID()).intern())
			{
				R=CMLib.map().getRoom(R);
				lastItemNums=updateLotWithThisData(R,this,false,scheduleReset,optPlayerList,lastItemNums);
				if((lastDayDone!=R.getArea().getTimeObj().getDayOfMonth())
				&&(CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED)))
				{
					lastDayDone=R.getArea().getTimeObj().getDayOfMonth();
					if((landOwner().length()>0)&&rentalProperty()&&(R.roomID().length()>0))
						if(doRentalProperty(R.getArea(),R.roomID(),landOwner(),landPrice()))
						{
							setLandOwner("");
							CMLib.database().DBUpdateRoom(R);
							lastItemNums=updateLotWithThisData(R,this,false,scheduleReset,optPlayerList,lastItemNums);
						}
				}
				scheduleReset=false;
			}
		}
	}
}
