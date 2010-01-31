package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
public class StdTitle extends StdItem implements LandTitle
{
	public String ID(){	return "StdTitle";}
	public String displayText() {return "an official looking document sits here";}
	public int baseGoldValue() {return landPrice();}
	public int value()
	{
		if(name().indexOf("(Copy)")>=0)
			baseGoldValue=10;
		else
			baseGoldValue=landPrice();
		return baseGoldValue;
	}
	public void setBaseGoldValue(int newValue) {setLandPrice(newValue);}

	public StdTitle()
	{
		super();
		setName("a standard title");
		setDescription("Give or Sell this title to transfer ownership. **DON`T LOSE THIS!**");
		baseGoldValue=10000;
		baseEnvStats().setSensesMask(EnvStats.SENSE_ITEMREADABLE);
		setMaterial(RawMaterial.RESOURCE_PAPER);
		recoverEnvStats();
	}

	public int landPrice()
	{
		LandTitle A=fetchALandTitle();
		if(A==null)	return 0;
		return A.landPrice()+A.backTaxes();
	}

	public void setLandPrice(int price)
	{
		LandTitle A=fetchALandTitle();
		if(A==null)	return;
		A.setLandPrice(price);
		A.updateTitle();
	}

	public void setBackTaxes(int amount)
	{
		LandTitle A=fetchALandTitle();
		if(A==null)	return;
		A.setBackTaxes(amount);
		A.updateTitle();
	}
	public int backTaxes()
	{
		LandTitle A=fetchALandTitle();
		if(A==null)	return 0;
		return A.backTaxes();
	}
	public boolean rentalProperty()
	{
		LandTitle A=fetchALandTitle();
		if(A==null)	return false;
		return A.rentalProperty();
	}

	public void setRentalProperty(boolean truefalse)
	{
		LandTitle A=fetchALandTitle();
		if(A==null)	return;
		A.setRentalProperty(truefalse);
		A.updateTitle();
	}

	public CMObject landOwnerObject()
	{
		LandTitle A=fetchALandTitle();
		if(A==null)	return null;
		String owner=A.landOwner();
		if(owner.length()==0) return null;
		Clan C=CMLib.clans().getClan(owner);
		if(C!=null) return C;
		return CMLib.players().getLoadPlayer(owner);
	}
	
	public String landOwner()
	{
		LandTitle A=fetchALandTitle();
		if(A==null)	return "";
		return A.landOwner();
	}
	public void setLandOwner(String owner)
	{
		LandTitle A=fetchALandTitle();
		if(A==null)	return;
		A.setLandOwner(owner);
		A.updateTitle();
	}

	public LandTitle fetchALandTitle()
	{
		Vector V=getPropertyRooms();
		if((V!=null)&&(V.size()>0))
			return CMLib.law().getLandTitle((Room)V.firstElement());
		return null;
	}

	public String landPropertyID()
	{
		return text();
	}

	public void updateTitleName()
	{
		if(!name.startsWith("the title to"))
		{
			Vector V=getPropertyRooms();
			if((V.size()<2)
			||(CMLib.map().getArea(landPropertyID())!=null))
				setName("the title to "+landPropertyID());
			else
				setName("the title to rooms around "+CMLib.map().getExtendedRoomID((Room)V.firstElement()));
		}
	}

	public void setLandPropertyID(String landID)
	{
		setMiscText(landID);
		updateTitleName();
	}

	public void updateLot(List optPlayerList)
	{
		Vector V=getPropertyRooms();
		for(int v=0;v<V.size();v++)
		{
			Room R=(Room)V.elementAt(v);
			LandTitle T=CMLib.law().getLandTitle(R);
			if(T!=null) T.updateLot(optPlayerList);
		}
	}

	public void updateTitle()
	{
		LandTitle T=fetchALandTitle();
		if(T!=null) T.updateTitle();
	}

	public Vector getPropertyRooms()
	{
		Room R=CMLib.map().getRoom(landPropertyID());
		if(R!=null)
		{
			LandTitle A=CMLib.law().getLandTitle(R);
			if(A!=null) return A.getPropertyRooms();
		}
		Area area=CMLib.map().getArea(landPropertyID());
		if(area!=null)
		{
			LandTitle A=CMLib.law().getLandTitle(area);
			if(A!=null) return A.getPropertyRooms();
		}
		return new Vector();
	}

	public void recoverEnvStats(){CMLib.flags().setReadable(this,true); super.recoverEnvStats();}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_WRITE)
		&&(msg.amITarget(this)))
		{
			MOB mob=msg.source();
			mob.tell("You shouldn't write on "+name()+".");
			return false;
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_GET)
		&&(msg.amITarget(this))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof ShopKeeper))
		{
			LandTitle A=fetchALandTitle();
			if(A==null)
			{
				destroy();
				msg.source().tell("You can't buy that.");
				return false;
			}
			if(A.landOwner().length()==0)
			{
				Area AREA=CMLib.map().getArea(A.landPropertyID());
				if((AREA!=null)&&(AREA.Name().indexOf("UNNAMED")>=0)&&(msg.source().isMonster()))
					return false;
			}
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_BUY)
		&&(msg.target() instanceof MOB)
		&&(msg.tool()==this))
		{
			LandTitle A=fetchALandTitle();
			if((A!=null)&&(A.landOwner().length()>0))
			{
				ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(msg.target());
			    if((((SK.isSold(ShopKeeper.DEAL_CLANBANKER))||(SK.isSold(ShopKeeper.DEAL_CLANDSELLER)))
			            &&(!A.landOwner().equals(msg.source().getClanID())))
			    ||(((SK.isSold(ShopKeeper.DEAL_BANKER))||(SK.isSold(ShopKeeper.DEAL_CLANBANKER)))
			            &&(!A.landOwner().equals(msg.source().Name())))
                ||(((SK.isSold(ShopKeeper.DEAL_POSTMAN))||(SK.isSold(ShopKeeper.DEAL_CLANPOSTMAN)))
                        &&(!A.landOwner().equals(msg.source().Name()))))
			    {
			        String str="I'm sorry, '"+msg.tool().Name()+" is not for sale.  It already belongs to "+A.landOwner()+".  It should be destroyed.";
			        if(((MOB)msg.target()).isMonster())
				        CMLib.commands().postSay((MOB)msg.target(),msg.source(),str,false,false);
			        else
			            ((MOB)msg.target()).tell(str+" You might want to tell the customer.");
                    if(SK!=null) SK.getShop().removeStock(Name(),msg.source());
			        destroy();
			        return false;
			    }

			}
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_WITHDRAW)
		&&(msg.target() instanceof MOB)
		&&(msg.tool()==this))
		{
			LandTitle A=fetchALandTitle();
			if((A!=null)
            &&((A.landOwner().length()==0)
            ||((A.landOwner().length()>0)
                &&(!A.landOwner().equals(msg.source().Name()))
                &&(!((msg.source().isMarriedToLiege())&&(A.landOwner().equals(msg.source().getLiegeID()))))
                &&(!A.landOwner().equals(msg.source().getClanID())))))
			{
		        String str="I'm sorry, '"+msg.tool().Name()+" must be destroyed.";
		        if(((MOB)msg.target()).isMonster())
			        CMLib.commands().postSay((MOB)msg.target(),msg.source(),str,false,false);
		        else
		            ((MOB)msg.target()).tell(str+" You might want to tell the customer.");
				ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(msg.target());
				if(SK!=null) SK.getShop().removeStock(msg.tool().Name(),msg.source());
                destroy();
		        return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
        if((msg.amITarget(this))
        &&(msg.targetMinor()==CMMsg.TYP_READ))
        {
            if(CMLib.flags().canBeSeenBy(this,msg.source()))
            {
                if((landPropertyID()==null)||(landPropertyID().length()==0))
                    msg.source().tell("It appears to be a blank property title.");
                else
                if((landOwner()==null)||(landOwner().length()==0))
                    msg.source().tell("It states that the property herein known as '"+landPropertyID()+"' is available for ownership.");
                else
                    msg.source().tell("It states that the property herein known as '"+landPropertyID()+"' is deeded to "+landOwner()+".");
            }
            else
                msg.source().tell("You can't see that!");
            msg.modify(msg.source(),msg.target(),msg.tool(),msg.sourceCode(),msg.sourceMessage(),msg.targetCode(),"CANCEL",msg.othersCode(),msg.othersMessage());
        }

		super.executeMsg(myHost,msg);

		if((msg.targetMinor()==CMMsg.TYP_SELL)
		&&(msg.tool()==this)
		&&(msg.target()!=null)
		&&(msg.target() instanceof ShopKeeper))
		{
			LandTitle A=fetchALandTitle();
			if(A==null)
			{
				Log.errOut("StdTitle","Unsellable room: "+landPropertyID());
                destroy();
				return;
			}
			A.setLandOwner("");
			updateTitle();
			updateLot(null);
			recoverEnvStats();
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.tool()==this)
		&&(msg.source()!=null)
		&&(landOwner().length()>0)
		&&((msg.source().Name().equals(landOwner()))
			||(msg.source().getLiegeID().equals(landOwner())&&msg.source().isMarriedToLiege())
            ||(msg.source().getClanID().equals(landOwner())
                &&(CMLib.clans().getClan(msg.source().getClanID())!=null)
                &&(CMLib.clans().getClan(msg.source().getClanID()).allowedToDoThis(msg.source(),Clan.FUNC_CLANPROPERTYOWNER)>=0)))
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB)
		&&(!(msg.target() instanceof Banker))
		&&(!(msg.target() instanceof Auctioneer))
        &&(!(msg.target() instanceof PostOffice)))
		{
			LandTitle A=fetchALandTitle();
			if(A==null)
			{
				Log.errOut("StdTitle","Unsellable room: "+landPropertyID());
                destroy();
				return;
			}
            if(msg.source().getClanID().equals(landOwner())
            &&(CMLib.clans().getClan(msg.source().getClanID())!=null)
            &&(CMLib.clans().getClan(msg.source().getClanID()).allowedToDoThis(msg.source(),Clan.FUNC_CLANPROPERTYOWNER)>=0))
                A.setLandOwner(((MOB)msg.target()).getClanID());
            else
    			A.setLandOwner(msg.target().Name());
			A.setBackTaxes(0);
			updateTitle();
			updateLot(null);
			recoverEnvStats();
			msg.source().tell(name()+" is now signed over to "+A.landOwner()+".");
			if(A.rentalProperty())
			    msg.source().tell("This property is a rental.  Your rent will be paid every mud-month out of your bank account.");
			else
			{
			    Vector allRooms=getPropertyRooms();
			    if((allRooms!=null)&&(allRooms.size()>0))
			    {
			        Room R=(Room)allRooms.firstElement();
                    LegalBehavior B=CMLib.law().getLegalBehavior(R);
				    if(B!=null)
				    {
						Area A2=CMLib.law().getLegalObject(R);
						if(A2==null)
							Log.errOut("StdTitle",CMLib.map().getExtendedRoomID(R)+" has a legal behavior, but no area!");
						else
						{
							Law theLaw=B.legalInfo(A2);
							if(theLaw==null)
								Log.errOut("StdTitle",A2.Name()+" has no law.");
							else
							{
								String taxs=(String)theLaw.taxLaws().get("PROPERTYTAX");
								if((taxs!=null)&&(taxs.length()==0)&&(CMath.s_double(taxs)>0.0))
								    msg.source().tell("A property tax of "+CMath.s_double(taxs)+"% of "+A.landPrice()+" will be paid monthly out of your bank account.");
							}
						}
				    }
			    }
			}
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_GET)
		&&(msg.amITarget(this))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof ShopKeeper))
		{
			LandTitle A=fetchALandTitle();
			if(A==null)
			{
				Log.errOut("StdTitle","Unsellable room: "+landPropertyID());
                destroy();
				return;
			}
			if(A.landOwner().length()==0)
			{
				Area AREA=CMLib.map().getArea(landPropertyID());
				if((AREA!=null)&&(AREA.Name().indexOf("UNNAMED")>=0))
				{
					String newName="";
					try{
						while(newName.trim().length()==0)
						{
							String n=msg.source().session().prompt("What would you like to name your ship? ","",60000).trim().toLowerCase();
							if(n.indexOf(" ")>=0)
							{
								msg.source().tell("Spaces are not allowed in names! Please enter another one.");
								newName="";
							}
							else
							if(n.length()!=0)
							{
								String nn=CMStrings.replaceAll(AREA.Name(),"UNNAMED",CMStrings.capitalizeFirstLetter(n.toLowerCase()));
								if(CMLib.players().playerExists(nn))
									msg.source().tell("That name is already taken.  Please enter a different one.");
								else
								if(msg.source().session().confirm("If the name '"+nn+"' correct (y/N)?","N",60000))
								{
									name=CMStrings.replaceAll(name,"UNNAMED",CMStrings.capitalizeFirstLetter(n.toLowerCase()));
									displayText=CMStrings.replaceAll(displayText,"UNNAMED",CMStrings.capitalizeFirstLetter(n.toLowerCase()));
									setDescription(CMStrings.replaceAll(description(),"UNNAMED",CMStrings.capitalizeFirstLetter(n.toLowerCase())));
									newName=nn;
								}
								else
									newName="";
							}
						}
					}
					catch(Throwable t)
					{
						return;
					}
					AREA=CMLib.coffeeMaker().copyArea(AREA,newName);
					if(AREA==null)
					{
						msg.source().tell("Purchase failed.");
						return;
					}
					setLandPropertyID(AREA.Name());
					A=fetchALandTitle();
					if(A==null)
					{
						Log.errOut("StdTitle","Unsellable room: "+landPropertyID());
                        destroy();
						return;
					}
					A.setLandPropertyID(AREA.Name());
					if(AREA instanceof SpaceShip)
					{
						Room spacePort=msg.source().location();
						Vector choices=new Vector();
						for(Enumeration e=spacePort.getArea().getProperMap();e.hasMoreElements();)
						{
							Room R=(Room)e.nextElement();
							if(R.domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
							{ choices.addElement(R);}
						}
						if(choices.size()>0) spacePort=(Room)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
						((SpaceShip)AREA).dockHere(spacePort);
						msg.source().tell("Your ship is located at "+spacePort.displayText()+".");
					}

				}
				if((((ShopKeeper)msg.tool()).isSold(ShopKeeper.DEAL_CLANDSELLER))
				&&(msg.source().getClanID().length()>0))
					A.setLandOwner(msg.source().getClanID());
				else
				if((((ShopKeeper)msg.tool()).isSold(ShopKeeper.DEAL_CSHIPSELLER))
				&&(msg.source().getClanID().length()>0))
					A.setLandOwner(msg.source().getClanID());
				else
					A.setLandOwner(msg.source().Name());
				setBackTaxes(0);
				updateTitle();
				updateLot(null);
				msg.source().tell(name()+" is now signed over to "+A.landOwner()+".");
			}
			recoverEnvStats();
		}
	}
}
