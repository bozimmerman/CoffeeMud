package com.planet_ink.coffee_mud.Abilities.Properties;
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
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "Prop_AreaForSale";
	}

	@Override
	public String name()
	{
		return "Putting an area up for sale";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_AREAS;
	}

	protected Hashtable<Room, Integer>	lastItemNums	= new Hashtable<Room, Integer>();

	@Override
	public String accountForYourself()
	{
		return "For Sale";
	}

	protected long	lastCall	= 0;
	protected long	lastMobSave	= 0;
	protected int	lastDayDone	= -1;

	@Override
	public boolean allowsExpansionConstruction()
	{
		return false;
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
	public String getUniqueLotID()
	{
		return "AREA_PROPERTY_"+landPropertyID();
	}

	@Override
	public void setPrice(int price)
	{
		setMiscText(getOwnerName()+"/"
			+(rentalProperty()?"RENTAL ":"")
			+((backTaxes()!=0)?"TAX"+backTaxes()+"X ":"")
			+price);
	}

	@Override
	public String getOwnerName()
	{
		if(text().indexOf('/')<0)
			return "";
		return text().substring(0,text().indexOf('/'));
	}

	@Override
	public String getTitleID()
	{
		if(affected != null)
			return "LAND_TITLE_FOR#"+affected.Name();
		return "";
	}

	@Override
	public CMObject getOwnerObject()
	{
		final String owner=getOwnerName();
		if(owner.length()==0)
			return null;
		final Clan C=CMLib.clans().getClan(owner);
		if(C!=null)
			return C;
		return CMLib.players().getLoadPlayer(owner);
	}

	@Override
	public void setOwnerName(String owner)
	{
		setMiscText(owner+"/"
				+(rentalProperty()?"RENTAL ":"")
				+((backTaxes()!=0)?"TAX"+backTaxes()+"X ":"")
				+getPrice());
	}

	@Override
	public int backTaxes()
	{
		if(text().indexOf('/')<0)
			return 0;
		final int x=text().indexOf("TAX",text().indexOf('/'));
		if(x<0)
			return 0;
		final String s=CMParms.parse(text().substring(x+3)).firstElement();
		return CMath.s_int(s.substring(0,s.length()-1));
	}

	@Override
	public void setBackTaxes(int tax)
	{
		setMiscText(getOwnerName()+"/"
				+(rentalProperty()?"RENTAL ":"")
				+((tax!=0)?"TAX"+tax+"X ":"")
				+getPrice());
	}

	@Override
	public boolean rentalProperty()
	{
		if(text().indexOf('/')<0)
			return text().indexOf("RENTAL")>=0;
		return text().indexOf("RENTAL",text().indexOf('/'))>0;
	}
	
	@Override
	public void setRentalProperty(boolean truefalse)
	{
		setMiscText(getOwnerName()+"/"
				+(truefalse?"RENTAL ":"")
				+((backTaxes()!=0)?"TAX"+backTaxes()+"X ":"")
				+getPrice());
	}

	// update title, since it may affect clusters, worries about ALL involved
	@Override
	public void updateTitle()
	{
		if(affected instanceof Area)
			CMLib.database().DBUpdateArea(((Area)affected).name(),(Area)affected);
		else
		if(affected instanceof Room)
			Log.errOut("Prop_AreaForSale","Prop_AreaForSale goes on an Area, NOT "+CMLib.map().getDescriptiveExtendedRoomID((Room)affected));
		else
		{
			final Area A=CMLib.map().getArea(landPropertyID());
			if(A!=null)
				CMLib.database().DBUpdateArea(A.Name(),A);
		}
	}

	@Override
	public String landPropertyID()
	{
		if((affected!=null)&&(affected instanceof Area))
			((Area)affected).Name();
		else
		if(affected instanceof Room)
			return CMLib.map().getExtendedRoomID((Room)affected);
		return "";
	}

	@Override
	public LandTitle generateNextRoomTitle()
	{
		final LandTitle newTitle=(LandTitle)this.copyOf();
		newTitle.setBackTaxes(0);
		return newTitle;
	}
	
	@Override
	public boolean gridLayout()
	{
		return false;
	}
	
	@Override
	public void setGridLayout(boolean gridLayout)
	{
	}
	
	@Override
	public void setLandPropertyID(String landID)
	{
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		CMLib.law().robberyCheck(this,msg);
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(((msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
				||((msg.targetMinor()==CMMsg.TYP_EXPIRE)&&(msg.target() instanceof Room))
				||(msg.sourceMinor()==CMMsg.TYP_ROOMRESET))
		&&(affected instanceof Area)
		&&((System.currentTimeMillis()-lastMobSave)>360000))
		{
			lastMobSave=System.currentTimeMillis();
			final List<Room> V=getAllTitledRooms();
			for(int v=0;v<V.size();v++)
			{
				Room R=V.get(v);
				synchronized(("SYNC"+R.roomID()).intern())
				{
					R=CMLib.map().getRoom(R);
					lastMobSave=System.currentTimeMillis();
					final Vector<MOB> mobs=new Vector<MOB>();
					for(int m=0;m<R.numInhabitants();m++)
					{
						final MOB M=R.fetchInhabitant(m);
						if((M!=null)
						&&(M.isSavable())
						&&(M.getStartRoom()==R)
						&&((M.basePhyStats().rejuv()==0)||(M.basePhyStats().rejuv()==PhyStats.NO_REJUV)))
							mobs.addElement(M);
					}
					if(!CMSecurity.isSaveFlag(CMSecurity.SaveFlag.NOPROPERTYMOBS))
						CMLib.database().DBUpdateTheseMOBs(R,mobs);
				}
			}
			lastMobSave=System.currentTimeMillis();
		}
	}

	@Override
	public List<Room> getAllTitledRooms()
	{
		final List<Room> V=new Vector<Room>();
		Area A=null;
		if(affected instanceof Area)
			A=(Area)affected;
		else
		if(affected instanceof Room)
			V.add((Room)affected);
		else
			A=CMLib.map().getArea(landPropertyID());
		if(A!=null)
		{
			for(final Enumeration<Room> e=A.getProperMap();e.hasMoreElements();)
				V.add(e.nextElement());
		}
		return V;
	}

	@Override
	public List<Room> getConnectedPropertyRooms()
	{
		return getAllTitledRooms();
	}

	// update lot, since its called by the savethread, ONLY worries about itself
	@Override
	public void updateLot(List<String> optPlayerList)
	{
		if(((System.currentTimeMillis()-lastCall)>360000)
		&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)))
		{
			final List<Room> V=getAllTitledRooms();
			for(int v=0;v<V.size();v++)
			{
				final Room R=V.get(v);
				lastCall=System.currentTimeMillis();
				final Integer lastItemNum=lastItemNums.get(R);
				lastItemNums.put(R,Integer.valueOf(Prop_RoomForSale.updateLotWithThisData(R,this,false,false,optPlayerList,(lastItemNum==null)?-1:lastItemNum.intValue())));
			}
			lastCall=System.currentTimeMillis();
			Area A=null;
			if(affected instanceof Area)
				A=(Area)affected;
			else
				A=CMLib.map().getArea(landPropertyID());
			if((A!=null)&&(lastDayDone!=A.getTimeObj().getDayOfMonth()))
			{
				lastDayDone=A.getTimeObj().getDayOfMonth();
				if((getOwnerName().length()>0)&&rentalProperty())
				{
					if(Prop_RoomForSale.doRentalProperty(A,A.Name(),getOwnerName(),getPrice()))
					{
						setOwnerName("");
						CMLib.database().DBUpdateArea(A.Name(),A);
					}
				}
			}
		}
	}
}
