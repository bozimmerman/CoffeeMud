package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdTitle extends StdItem implements LandTitle
{
	public String ID(){	return "StdTitle";}
	public StdTitle()
	{
		super();
		name="a land title";
		baseEnvStats.setWeight(0);
		displayText="A title to somewhere sits here.";
		description="1. Take this title to your plot to claim possession.  2. Give or Sell this title to transfer ownership.  3. DON'T LOSE THIS!";
		baseGoldValue=10000;
		miscText="Who Knows?";
		setMaterial(EnvResource.RESOURCE_PAPER);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new StdTitle();
	}
	
	public int landPrice()
	{
		LandTitle A=fetchLandTitle(null);
		if(A==null)	return 0;
		return A.landPrice();
	}
	public void setLandPrice(int price)
	{ 
		LandTitle A=fetchLandTitle(null);
		if(A==null)	return;
		A.setLandPrice(price);
		Room R=CMMap.getRoom(landRoomID());
		if(R==null) return;
		ExternalPlay.DBUpdateRoom(R);
	}
	public String landOwner()
	{
		LandTitle A=fetchLandTitle(null);
		if(A==null)	return "";
		return A.landOwner();
	}
	public void setLandOwner(String owner)
	{
		LandTitle A=fetchLandTitle(null);
		if(A==null)	return;
		A.setLandOwner(owner);
		Room R=CMMap.getRoom(landRoomID());
		if(R==null) return;
		ExternalPlay.DBUpdateRoom(R);
	}
	public String landRoomID(){return text();}
	public void setLandRoomID(String landID){setMiscText(landID);}
	public void updateLot(Room R, LandTitle T)
	{
		if(T==null)
			T=fetchLandTitle(R);
		if(T==null) return;
		T.updateLot(R,T);
	}
	
	private LandTitle fetchLandTitle(Room R)
	{
		if(R==null)
		{
			if(landRoomID().length()==0) return null;
			R=CMMap.getRoom(landRoomID());
			if(R==null)
			{
				destroyThis();
				Log.errOut("StdTitle","Unknown room: "+landRoomID());
				return null;
			}
		}
		LandTitle A=null;
		for(int a=0;a<R.numAffects();a++)
			if(R.fetchAffect(a) instanceof LandTitle)
			{ A=(LandTitle)R.fetchAffect(a); break;}
		return A;
	}
	
	public void affect(Affect msg)
	{
		super.affect(msg);
		if((msg.targetMinor()==Affect.TYP_SELL)
		&&(msg.tool()==this)
		&&(msg.target()!=null)
		&&(msg.target() instanceof ShopKeeper))
		{
			Room R=CMMap.getRoom(landRoomID());
			if(R==null)
			{
				destroyThis();
				Log.errOut("StdTitle","Unknown room: "+landRoomID());
				return;
			}
			LandTitle A=fetchLandTitle(R);
			if(A==null)
			{
				destroyThis();
				Log.errOut("StdTitle","Unsellable room: "+landRoomID());
				return;
			}
			A.setLandOwner("");
			ExternalPlay.DBUpdateRoom(R);
			updateLot(R,A);
		}
		else
		if((msg.targetMinor()==Affect.TYP_GIVE)
		&&(msg.tool()==this)
		&&(msg.source()!=null)
		&&(msg.source().name().equals(landOwner()))
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB))
		{
			Room R=CMMap.getRoom(landRoomID());
			if(R==null)
			{
				destroyThis();
				Log.errOut("StdTitle","Unknown room: "+landRoomID());
				return;
			}
			LandTitle A=fetchLandTitle(R);
			if(A==null)
			{
				destroyThis();
				Log.errOut("StdTitle","Ungiveable room: "+landRoomID());
				return;
			}
			A.setLandOwner(msg.target().name());
			ExternalPlay.DBUpdateRoom(R);
			updateLot(R,A);
		}
		else
		if((msg.targetMinor()==Affect.TYP_GET)
		&&(msg.amITarget(this))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof ShopKeeper))
		{
			Room R=CMMap.getRoom(landRoomID());
			if(R==null)
			{
				destroyThis();
				Log.errOut("StdTitle","Unknown room: "+landRoomID());
				return;
			}
			LandTitle A=fetchLandTitle(R);
			if(A==null)
			{
				destroyThis();
				Log.errOut("StdTitle","Unbuyable room: "+landRoomID());
				return;
			}
			setBaseValue(landPrice());
			A.setLandOwner(msg.source().name());
			ExternalPlay.DBUpdateRoom(R);
			updateLot(R,A);
			recoverEnvStats();
		}
	}
}
