package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RoomForSale extends Property implements LandTitle
{
	public String ID() { return "Prop_RoomForSale"; }
	public String name(){ return "Putting a room up for sale";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	public Environmental newInstance(){	return new Prop_RoomForSale();}

	private final static String theStr=" This lot is for sale (look id).";
	protected int lastItemNums=-1;

	public String accountForYourself()
	{ return "For Sale";	}

	public int landPrice()
	{
		int price=0;
		if(text().indexOf("/")<0)
			price=Util.s_int(text());
		else
			price=Util.s_int(text().substring(text().indexOf("/")+1));
		if(price<=0) price=100000;
		return price;
	}
	public void setLandPrice(int price){
		String owner=landOwner();
		if(owner.length()>0)
			setMiscText(owner+"/"+price);
		else
			setMiscText(""+price);
	}
	public String landOwner()
	{
		if(text().indexOf("/")<0) return "";
		return text().substring(0,text().indexOf("/"));
	}

	public void setLandOwner(String owner)
	{
		int price=landPrice();
		setMiscText(owner+"/"+price);
	}

	// update title, since it may affect clusters, worries about ALL involved
	public void updateTitle()
	{
		if(affected instanceof Room)
			CMClass.DBEngine().DBUpdateRoom((Room)affected);
		else
		{
			Room R=CMMap.getRoom(landPropertyID());
			if(R!=null) CMClass.DBEngine().DBUpdateRoom(R);
		}
	}

	public String landPropertyID(){
		if((affected!=null)&&(affected instanceof Room))
			return CMMap.getExtendedRoomID(((Room)affected));
		return "";
	}

	public void setLandPropertyID(String landID){}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET))
		&&(affected!=null))
		{
			updateLot();
			Vector mobs=new Vector();
			Room R=(Room)affected;
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=(MOB)R.fetchInhabitant(m);
				if((M!=null)
				&&(M.isEligibleMonster())
				&&(M.getStartRoom()==R)
				&&((M.baseEnvStats().rejuv()==0)||(M.baseEnvStats().rejuv()==Integer.MAX_VALUE)))
					mobs.addElement(M);
			}
			CMClass.DBEngine().DBUpdateTheseMOBs(R,mobs);
		}
	}

	public static void colorForSale(Room R, boolean reset)
	{
		if(R.description().indexOf(theStr)<0)
		{
			if(reset)
			{
				R.setDisplayText("An empty plot");
				R.setDescription("");
			}
			R.setDescription(R.description()+theStr);
			CMClass.DBEngine().DBUpdateRoom(R);

			Item I=R.fetchItem(null,"id$");
			if((I==null)||(!I.ID().equals("GenWallpaper")))
			{
				I=CMClass.getItem("GenWallpaper");
				I.setReadable(true);
				I.setName("id");
				I.setReadableText("This room is "+CMMap.getExtendedRoomID(R));
				I.setDescription("This room is "+CMMap.getExtendedRoomID(R));
				R.addItem(I);
				CMClass.DBEngine().DBUpdateItems(R);
			}
		}
	}

	public Vector getPropertyRooms()
	{
		Vector V=new Vector();
		if(affected instanceof Room)
			V.addElement(affected);
		else
		{
			Room R=CMMap.getRoom(landPropertyID());
			if(R!=null) V.addElement(R);
		}
		return V;
	}

	public static int updateLotWithThisData(Room R, 
											LandTitle T, 
											boolean clearRoomIfUnsold,
											int lastNumItems)
	{
		if(T.landOwner().length()==0)
		{
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if(I.dispossessionTime()==0)
				{
					long now=System.currentTimeMillis();
					now+=(IQCalendar.MILI_HOUR*Item.REFUSE_PLAYER_DROP);
					I.setDispossessionTime(now);
				}
				if((I.envStats().rejuv()!=Integer.MAX_VALUE)
				&&(I.envStats().rejuv()!=0))
				{
					I.baseEnvStats().setRejuv(Integer.MAX_VALUE);
					I.recoverEnvStats();
				}
			}
			colorForSale(R,clearRoomIfUnsold);
			return -1;
		}
		else
		{
			boolean updateItems=false;
			if(lastNumItems<0)
			{
				if((!CMClass.DBEngine().DBUserSearch(null,T.landOwner()))
				&&(Clans.getClan(T.landOwner())==null))
				{
					T.setLandOwner("");
					T.updateLot();
					return -1;
				}
			}

			int x=R.description().indexOf(theStr);
			if(x>=0)
			{
				R.setDescription(R.description().substring(0,x));
				CMClass.DBEngine().DBUpdateRoom(R);
			}

			// this works on the priciple that
			// 1. if an item has ONLY been removed, the lastNumItems will be != current # items
			// 2. if an item has ONLY been added, the dispossessiontime will be != null
			// 3. if an item has been added AND removed, the dispossession time will be != null on the added
			if((lastNumItems>=0)&&(R.numItems()!=lastNumItems))
				updateItems=true;
				
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I.dispossessionTime()!=0)
				&&(!(I instanceof DeadBody)))
				{
					I.setDispossessionTime(0);
					updateItems=true;
				}
						
				if((I.envStats().rejuv()!=Integer.MAX_VALUE)
				&&(I.envStats().rejuv()!=0))
				{
					I.baseEnvStats().setRejuv(Integer.MAX_VALUE);
					I.recoverEnvStats();
					updateItems=true;
				}
			}
			lastNumItems=R.numItems();
			if(updateItems) 
				CMClass.DBEngine().DBUpdateItems(R);
			return lastNumItems;
		}
	}
	
	// update lot, since its called by the savethread, ONLY worries about itself
	public void updateLot()
	{
		if(affected instanceof Room)
			lastItemNums=updateLotWithThisData((Room)affected,this,false,lastItemNums);
	}
}
