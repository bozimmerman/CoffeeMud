package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdInnKey extends StdKey implements InnKey
{
	public String ID(){	return "StdInnKey";}
	public ShopKeeper myShopkeeper=null;

	public StdInnKey()
	{
		super();
		setName("a metal key");
		setDisplayText("a small metal key sits here.");
		setDescription("It says it goes to room 1.");

		material=EnvResource.RESOURCE_STEEL;
		baseGoldValue=10;
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new StdInnKey();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.ITEM_BOUNCEBACK)
		{
			this.destroyed=false;
			this.setContainer(null);
			if((owner()!=null)&&(owner()==myShopkeeper))
				return false;
			if(owner()!=null)
				removeFromOwnerContainer();
			if(myShopkeeper!=null)
			{
				myShopkeeper.addStoreInventory(this); // makes a copy
				destroy();
			}
			return false;
		}
		return true;
	}

	public void hangOnRack(ShopKeeper sk)
	{
		if(myShopkeeper==null)
		{
			myShopkeeper=sk;
			int y=sk.numberInStock(this);
			setName("key to room "+(y+1));
			setDescription("The key goes to room "+(y+1)+", but will expire soon, so you better use it quickly! Give the key to your innkeeper, "+sk.name()+", when you leave.");
			setMiscText("INN"+(y+1));
		}
	}

	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		if(((affect.targetMinor()==Affect.TYP_GIVE)
		||(affect.targetMinor()==Affect.TYP_SELL))
		&&(myShopkeeper!=null)
		&&(affect.target()==myShopkeeper)
		&&(affect.tool()==this))
		{
			ExternalPlay.deleteTick(this,Host.ITEM_BOUNCEBACK);
			myShopkeeper.addStoreInventory(this); //makes a copy
			destroy();
		}
	}
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if(((affect.targetMinor()==Affect.TYP_GIVE)
		||(affect.targetMinor()==Affect.TYP_SELL))
		&&(affect.target() instanceof ShopKeeper)
		&&(myShopkeeper!=null)
		&&(affect.target()!=myShopkeeper)
		&&(affect.tool()==this))
		{
			ExternalPlay.quickSay((MOB)affect.target(),affect.source(),"I'm not interested.",false,false);
			return false;
		}
		else
		if((affect.sourceMinor()==Affect.TYP_GET)
		&&(myShopkeeper!=null)
		&&(affect.tool()==myShopkeeper)
		&&(affect.target()==this))
			ExternalPlay.startTickDown(this,Host.ITEM_BOUNCEBACK,(int)Host.TICKS_PER_DAY);
		return true;
	}
}
