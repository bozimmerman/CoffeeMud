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
public class Prop_AreaForSale extends Property implements LandTitle
{
	public String ID() { return "Prop_AreaForSale"; }
	public String name(){ return "Putting an area up for sale";}
	protected int canAffectCode(){return Ability.CAN_AREAS;}
	protected Hashtable lastItemNums=new Hashtable();
	public String accountForYourself()
	{ return "For Sale";	}
	private long lastCall=0;
	private long lastMobSave=0;

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
		if(affected instanceof Area)
			CMClass.DBEngine().DBUpdateArea(((Area)affected).name(),(Area)affected);
		else
		{
			Area A=CMMap.getArea(landPropertyID());
			if(A!=null) CMClass.DBEngine().DBUpdateArea(A.Name(),A);
		}
	}

	public String landPropertyID(){
		if((affected!=null)&&(affected instanceof Area))
			((Area)affected).Name();
		return "";
	}

	public void setLandPropertyID(String landID){}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET))
		&&(affected instanceof Area)
		&&((System.currentTimeMillis()-lastMobSave)>360000))
		{
			lastMobSave=System.currentTimeMillis();
			Vector V=getPropertyRooms();
			for(int v=0;v<V.size();v++)
			{
				Room R=(Room)V.elementAt(v);
				lastMobSave=System.currentTimeMillis();
				Vector mobs=new Vector();
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if((M!=null)
					&&(M.isEligibleMonster())
					&&(M.getStartRoom()==R)
					&&((M.baseEnvStats().rejuv()==0)||(M.baseEnvStats().rejuv()==Integer.MAX_VALUE)))
						mobs.addElement(M);
				}
				CMClass.DBEngine().DBUpdateTheseMOBs(R,mobs);
			}
			lastMobSave=System.currentTimeMillis();
		}
	}

	public Vector getPropertyRooms()
	{
		Vector V=new Vector();
		Area A=null;
		if(affected instanceof Area)
			A=(Area)affected;
		else
			A=CMMap.getArea(landPropertyID());
		for(Enumeration e=A.getProperMap();e.hasMoreElements();)
			V.addElement(e.nextElement());
		return V;
	}

	// update lot, since its called by the savethread, ONLY worries about itself
	public void updateLot()
	{
		if((System.currentTimeMillis()-lastCall)>360000)
		{
			Vector V=getPropertyRooms();
			for(int v=0;v<V.size();v++)
			{
				Room R=(Room)V.elementAt(v);
				lastCall=System.currentTimeMillis();
				Integer lastItemNum=(Integer)lastItemNums.get(R);
				lastItemNums.put(R,new Integer(Prop_RoomForSale.updateLotWithThisData(R,this,false,(lastItemNum==null)?-1:lastItemNum.intValue())));
			}
			lastCall=System.currentTimeMillis();
		}
	}
}
