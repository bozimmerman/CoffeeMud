package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
	public String ID() { return "Prop_RoomForSale"; }
	public String name(){ return "Putting a room up for sale";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}

	private final static String SALESTR=" This lot is for sale (look id).";
	private final static String RENTSTR=" This lot (look id) is for rent on a monthly basis.";
	protected int lastItemNums=-1;
	protected int lastDayDone=-1;

	public String accountForYourself()
	{ return "For Sale";	}

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
		int price=Util.s_int(s.substring(index+1).trim());
			    
		if(price<=0) price=100000;
		return price;
	}
	
	public void setLandPrice(int price)
	{   
	    setMiscText(landOwner()+"/"
	        +(rentalProperty()?"RENTAL ":"")
	        +((backTaxes()!=0)?"TAX"+backTaxes()+"X ":"")
	        +price);
	}
	
	public String landOwner()
	{
		if(text().indexOf("/")<0) return "";
		return text().substring(0,text().indexOf("/"));
	}

	public void setLandOwner(String owner)
	{   
	    setMiscText(owner+"/"
		        +(rentalProperty()?"RENTAL ":"")
		        +((backTaxes()!=0)?"TAX"+backTaxes()+"X ":"")
		        +landPrice());
    }

	public int backTaxes()
	{
		if(text().indexOf("/")<0) return 0;
		int x=text().indexOf("TAX",text().indexOf("/"));
		if(x<0) return 0;
		String s=(String)Util.parse(text().substring(x+3)).firstElement();
		return Util.s_int(s.substring(0,s.length()-1));
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
		if(text().indexOf("/")<0) return text().indexOf("RENTAL")>=0;
	    return text().indexOf("RENTAL",text().indexOf("/"))>0;
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

	
	public static boolean robberyCheck(LandTitle A, CMMsg msg)
	{
		if(msg.targetMinor()==CMMsg.TYP_GET)
		{
			if((msg.target() instanceof Item)
			&&(((Item)msg.target()).owner() ==msg.source().location())
			&&(!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
			&&(A.landOwner().length()>0)
			&&(msg.source().location()!=null)
		    &&(msg.othersMessage()!=null)
		    &&(msg.othersMessage().length()>0)
			&&(!CoffeeUtensils.doesHavePriviledgesHere(msg.source(),msg.source().location())))
		    {
			    Room R=msg.source().location();
				Behavior B=CoffeeUtensils.getLegalBehavior(R);
				if(B!=null)
				{
				    for(int m=0;m<R.numInhabitants();m++)
				    {
				        MOB M=R.fetchInhabitant(m);
				        if(CoffeeUtensils.doesHavePriviledgesHere(M,R))
				            return true;
				    }
					Vector V=new Vector();
					V.addElement(new Integer(Law.MOD_CRIMEACCUSE));
					MOB D=null;
				    Clan C=Clans.getClan(A.landOwner());
				    if(C!=null)
				        D=C.getResponsibleMember();
				    else
				        D=CMMap.getLoadPlayer(A.landOwner());
				    if(D==null) return true;
					V.addElement(D);//victim first
					V.addElement("PROPERTYROB");
					V.addElement("THIEF_ROBBERY");
					B.modifyBehavior(CoffeeUtensils.getLegalObject(R),msg.source(),V);
				}
		    }
			return true;
        }
		return false;
	}
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET))
		&&(affected instanceof Room))
		{
			updateLot();
			Vector mobs=new Vector();
			Room R=(Room)affected;
			if(R!=null)
			{
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if((M!=null)
					&&(M.isEligibleMonster())
					&&(M.getStartRoom()==R)
					&&((M.baseEnvStats().rejuv()==0)||(M.baseEnvStats().rejuv()==Integer.MAX_VALUE)))
						mobs.addElement(M);
				}
				if(!CMSecurity.isSaveFlag("NOPROPERTYMOBS"))
					CMClass.DBEngine().DBUpdateTheseMOBs(R,mobs);
			}
		}
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
	    if(!super.okMessage(myHost,msg)) return false;
		Prop_RoomForSale.robberyCheck(this,msg);
		return true;
	}
	
	public static void colorForSale(Room R, boolean rental, boolean reset)
	{
	    String theStr=rental?RENTSTR:SALESTR;
	    String otherStr=rental?SALESTR:RENTSTR;
		int x=R.description().indexOf(otherStr);
		if(x>=0)
		{
			R.setDescription(R.description().substring(0,x));
			CMClass.DBEngine().DBUpdateRoom(R);
		}
		if(R.description().indexOf(theStr.trim())<0)
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
				Sense.setReadable(I,true);
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
			colorForSale(R,T.rentalProperty(),clearRoomIfUnsold);
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

			int x=R.description().indexOf(SALESTR);
			if(x>=0)
			{
				R.setDescription(R.description().substring(0,x));
				CMClass.DBEngine().DBUpdateRoom(R);
			}
			x=R.description().indexOf(RENTSTR);
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
			if((!CMSecurity.isSaveFlag("NOPROPERTYITEMS"))
			&&(updateItems))
				CMClass.DBEngine().DBUpdateItems(R);
			return lastNumItems;
		}
	}

	public static boolean doRentalProperty(Area A, String ID, String owner, int rent)
	{
	    if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
	        return false;
	    int month=A.getTimeObj().getMonth();
	    int day=A.getTimeObj().getDayOfMonth();
	    int year=A.getTimeObj().getYear();
	    Object O=Resources.getResource("RENTAL INFO/"+owner);
	    Vector V=null;
	    if(O instanceof Vector)
	        V=(Vector)O;
	    else
	        V=CMClass.DBEngine().DBReadData(owner,"RENTAL INFO");
	    if(V==null)
	        V=new Vector();
	    if(V.size()==0)
	    {
		    V=new Vector();
	        V.addElement(owner);
	        V.addElement("RENTAL INFO");
	        V.addElement("RENTAL INFO/"+owner);
	        V.addElement(ID+"|~>|"+day+" "+month+" "+year+"|~;|");
	        CMClass.DBEngine().DBCreateData(owner,"RENTAL INFO","RENTAL INFO/"+owner,(String)V.lastElement());
	        Vector V2=new Vector();
	        V2.addElement(V);
	        Resources.submitResource("RENTAL INFO/"+owner,V2);
	        return false;
	    }
	    else
	    if(V.firstElement() instanceof Vector)
	    {
	        V=(Vector)V.firstElement();
	        if(V.size()>2)
	        {
		        String parse=(String)V.lastElement();
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
		                V=Util.parse(thisOne);
		                if(V.size()==3)
		                {
		                    int lastYear=Util.s_int((String)V.lastElement());
		                    int lastMonth=Util.s_int((String)V.elementAt(1));
		                    int lastDay=Util.s_int((String)V.firstElement());
		                    while(!needsToPay)
		                    {
			                    if(lastYear<year) 
			                        needsToPay=true;
			                    else
			                    if((lastYear==year)&&(lastMonth<month)&&(day>=lastDay)) 
			                        needsToPay=true;
			                    if(needsToPay)
			                    {
			                        if(MoneyUtils.modifyLocalBankGold(A,owner,CoffeeUtensils.getFormattedDate(A)+":Withdrawl of "+rent+": Rent for "+ID,-rent))
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
			        CMClass.DBEngine().DBDeleteData(owner,"RENTAL INFO","RENTAL INFO/"+owner);
			        CMClass.DBEngine().DBCreateData(owner,"RENTAL INFO","RENTAL INFO/"+owner,reparse.toString());
				    V=new Vector();
			        V.addElement(owner);
			        V.addElement("RENTAL INFO");
			        V.addElement("RENTAL INFO/"+owner);
			        V.addElement(reparse.toString());
			        Vector V2=new Vector();
			        V2.addElement(V);
			        Resources.updateResource("RENTAL INFO/"+owner,V2);
		        }
			    return needsToPay;
	        }
		    return false;
	    }
	    return false;
	}
	
	// update lot, since its called by the savethread, ONLY worries about itself
	public void updateLot()
	{
		if(affected instanceof Room)
		{
			lastItemNums=updateLotWithThisData((Room)affected,this,false,lastItemNums);
			if((lastDayDone!=((Room)affected).getArea().getTimeObj().getDayOfMonth())
			&&(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED)))
			{
			    Room R=(Room)affected;
			    lastDayDone=R.getArea().getTimeObj().getDayOfMonth();
			    if((landOwner().length()>0)&&rentalProperty()&&(R.roomID().length()>0))
			        if(doRentalProperty(R.getArea(),R.roomID(),landOwner(),landPrice()))
			        {
			            setLandOwner("");
						CMClass.DBEngine().DBUpdateRoom(R);
						lastItemNums=updateLotWithThisData((Room)affected,this,false,lastItemNums);
			        }
			}
		}
	}
}
