package com.planet_ink.coffee_mud.Items.Basic;

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
		setDescription("Give or Sell this title to transfer ownership. **DON'T LOSE THIS!**");
		baseGoldValue=10000;
		baseEnvStats().setSensesMask(EnvStats.SENSE_ITEMREADABLE);
		setMaterial(EnvResource.RESOURCE_PAPER);
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
			return CoffeeUtensils.getLandTitle((Room)V.firstElement());
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
			||(CMMap.getArea(landPropertyID())!=null))
				setName("the title to "+landPropertyID());
			else
				setName("the title to rooms around "+CMMap.getExtendedRoomID((Room)V.firstElement()));
		}
	}

	public void setLandPropertyID(String landID)
	{
		setMiscText(landID);
		updateTitleName();
	}

	public void updateLot()
	{
		Vector V=getPropertyRooms();
		for(int v=0;v<V.size();v++)
		{
			Room R=(Room)V.elementAt(v);
			LandTitle T=CoffeeUtensils.getLandTitle(R);
			if(T!=null) T.updateLot();
		}
	}

	public void updateTitle()
	{
		LandTitle T=fetchALandTitle();
		if(T!=null) T.updateTitle();
	}

	public Vector getPropertyRooms()
	{
		Room R=CMMap.getRoom(landPropertyID());
		if(R!=null)
		{
			LandTitle A=CoffeeUtensils.getLandTitle(R);
			if(A!=null) return A.getPropertyRooms();
		}
		Area area=CMMap.getArea(landPropertyID());
		if(area!=null)
		{
			LandTitle A=CoffeeUtensils.getLandTitle(area);
			if(A!=null) return A.getPropertyRooms();
		}
		return new Vector();
	}

	public void recoverEnvStats(){Sense.setReadable(this,true); super.recoverEnvStats();}

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
				Area AREA=CMMap.getArea(A.landPropertyID());
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
			if(A.landOwner().length()>0)
			{
				ShopKeeper SK=CoffeeUtensils.getShopKeeper((MOB)msg.target());
			    if((((SK.whatIsSold()==ShopKeeper.DEAL_CLANBANKER)||(SK.whatIsSold()==ShopKeeper.DEAL_CLANDSELLER))
			            &&(!A.landOwner().equals(msg.source().getClanID())))
			    ||(((SK.whatIsSold()==ShopKeeper.DEAL_BANKER)||(SK.whatIsSold()==ShopKeeper.DEAL_CLANBANKER))
			            &&(!A.landOwner().equals(msg.source().Name()))))
			    {
			        String str="I'm sorry, '"+msg.tool().Name()+" is not for sale.  It already belongs to "+A.landOwner()+".  It should be destroyed.";
			        if(((MOB)msg.target()).isMonster())
				        CommonMsgs.say((MOB)msg.target(),msg.source(),str,false,false);
			        else
			            ((MOB)msg.target()).tell(str+" You might want to tell the customer.");
			        destroy();
					if(SK!=null) SK.removeStock(Name(),msg.source());
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
			if(A.landOwner().length()==0)
			{
		        String str="I'm sorry, '"+msg.tool().Name()+" must be destroyed.";
		        if(((MOB)msg.target()).isMonster())
			        CommonMsgs.say((MOB)msg.target(),msg.source(),str,false,false);
		        else
		            ((MOB)msg.target()).tell(str+" You might want to tell the customer.");
		        destroy();
				ShopKeeper SK=CoffeeUtensils.getShopKeeper((MOB)msg.target());
				if(SK!=null) SK.removeStock(Name(),msg.source());
		        return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.targetMinor()==CMMsg.TYP_SELL)
		&&(msg.tool()==this)
		&&(msg.target()!=null)
		&&(msg.target() instanceof ShopKeeper))
		{
			LandTitle A=fetchALandTitle();
			if(A==null)
			{
				destroy();
				Log.errOut("StdTitle","Unsellable room: "+landPropertyID());
				return;
			}
			A.setLandOwner("");
			updateTitle();
			updateLot();
			recoverEnvStats();
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.tool()==this)
		&&(msg.source()!=null)
		&&(landOwner().length()>0)
		&&((msg.source().Name().equals(landOwner()))
			||(msg.source().getLiegeID().equals(landOwner())&&msg.source().isMarriedToLiege()))
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB)
		&&(!(msg.target() instanceof Banker)))
		{
			LandTitle A=fetchALandTitle();
			if(A==null)
			{
				destroy();
				Log.errOut("StdTitle","Unsellable room: "+landPropertyID());
				return;
			}
			A.setLandOwner(msg.target().Name());
			A.setBackTaxes(0);
			updateTitle();
			updateLot();
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
				    Behavior B=CoffeeUtensils.getLegalBehavior(R);
				    if(B!=null)
				    {
						Vector VB=new Vector();
						Area A2=CoffeeUtensils.getLegalObject(R);
						VB.addElement(new Integer(Law.MOD_LEGALINFO));
						B.modifyBehavior(A2,(MOB)msg.target(),VB);
						Law theLaw=(Law)VB.firstElement();
						String taxs=(String)theLaw.taxLaws().get("PROPERTYTAX");
						if((taxs!=null)&&(taxs.length()==0)&&(Util.s_double(taxs)>0.0))
						    msg.source().tell("A property tax of "+Util.s_double(taxs)+"% of "+A.landPrice()+" will be paid monthly out of your bank account.");
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
				destroy();
				Log.errOut("StdTitle","Unsellable room: "+landPropertyID());
				return;
			}
			if(A.landOwner().length()==0)
			{
				Area AREA=CMMap.getArea(landPropertyID());
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
								String nn=Util.replaceAll(AREA.Name(),"UNNAMED",Util.capitalize(n.toLowerCase()));
								if(CMClass.DBEngine().DBUserSearch(null,nn))
									msg.source().tell("That name is already taken.  Please enter a different one.");
								else
								if(msg.source().session().confirm("If the name '"+nn+"' correct (y/N)?","N",60000))
								{
									name=Util.replaceAll(name,"UNNAMED",Util.capitalize(n.toLowerCase()));
									displayText=Util.replaceAll(displayText,"UNNAMED",Util.capitalize(n.toLowerCase()));
									setDescription(Util.replaceAll(description(),"UNNAMED",Util.capitalize(n.toLowerCase())));
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
					};
					AREA=CoffeeMaker.copyArea(AREA,newName);
					if(AREA==null)
					{
						msg.source().tell("Purchase failed.");
						return;
					}
					setLandPropertyID(AREA.Name());
					A=fetchALandTitle();
					if(A==null)
					{
						destroy();
						Log.errOut("StdTitle","Unsellable room: "+landPropertyID());
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
						if(choices.size()>0) spacePort=(Room)choices.elementAt(Dice.roll(1,choices.size(),-1));
						((SpaceShip)AREA).dockHere(spacePort);
						msg.source().tell("Your ship is located at "+spacePort.displayText()+".");
					}

				}
				if((((ShopKeeper)msg.tool()).whatIsSold()==ShopKeeper.DEAL_CLANDSELLER)
				&&(msg.source().getClanID().length()>0))
					A.setLandOwner(msg.source().getClanID());
				else
				if((((ShopKeeper)msg.tool()).whatIsSold()==ShopKeeper.DEAL_CSHIPSELLER)
				&&(msg.source().getClanID().length()>0))
					A.setLandOwner(msg.source().getClanID());
				else
					A.setLandOwner(msg.source().Name());
				setBackTaxes(0);
				updateTitle();
				updateLot();
				msg.source().tell(name()+" is now signed over to "+A.landOwner()+".");
			}
			recoverEnvStats();
		}
		else
		if((msg.amITarget(this))
		&&(msg.targetMinor()==CMMsg.TYP_READSOMETHING))
		{
			if(Sense.canBeSeenBy(this,msg.source()))
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
			return;
		}
	}
}
