package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
		isReadable=true;
		setMaterial(EnvResource.RESOURCE_PAPER);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new StdTitle();
	}

	public int landPrice()
	{
		LandTitle A=fetchALandTitle();
		if(A==null)	return 0;
		return A.landPrice();
	}
	public void setLandPrice(int price)
	{
		LandTitle A=fetchALandTitle();
		if(A==null)	return;
		A.setLandPrice(price);
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

	public boolean isReadable(){return true;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_WRITE)
		&&(msg.amITarget(this)))
		{
			MOB mob=msg.source();
			mob.tell("You shouldn't write on "+name()+".");
			return false;
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
			||(msg.source().getLeigeID().equals(landOwner())&&msg.source().isMarriedToLeige())
			||((msg.source().getClanID().equals(landOwner()))))
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
			updateTitle();
			updateLot();
			recoverEnvStats();
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
			if((((ShopKeeper)msg.tool()).whatIsSold()==ShopKeeper.DEAL_CLANDSELLER)
			&&(msg.source().getClanID().length()>0))
				A.setLandOwner(msg.source().getClanID());
			else
				A.setLandOwner(msg.source().Name());
			updateTitle();
			updateLot();
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
