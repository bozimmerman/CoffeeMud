package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdInnKey extends StdKey implements InnKey
{

	public ShopKeeper myShopkeeper=null;

	public StdInnKey()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a metal key";
		displayText="a small metal key sits here.";
		description="It says it goes to room 1.";

		material=EnvResource.RESOURCE_STEEL;
		baseGoldValue=10;
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new StdInnKey();
	}

	public boolean tick(int tickID)
	{
		if(tickID==Host.ITEM_BOUNCEBACK)
		{
			this.destroyed=false;
			this.setContainer(null);
			if((owner()!=null)&&(owner()==myShopkeeper))
				return false;
			if(owner()!=null)
				removeThis();
			if(myShopkeeper!=null)
				myShopkeeper.addStoreInventory(this);
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
			name="key to room "+(y+1);
			description="The key goes to room "+(y+1)+", but will expire soon, so you better use it quickly! Give the key to your innkeeper, "+sk.name()+", when you leave.";
			miscText="INN"+(y+1);
		}
	}

	public void affect(Affect affect)
	{
		super.affect(affect);
		if(((affect.targetMinor()==Affect.TYP_GIVE)
		||(affect.targetMinor()==Affect.TYP_SELL))
		&&(affect.target()==myShopkeeper)
		&&(affect.tool()==this))
			ExternalPlay.deleteTick(this,Host.ITEM_BOUNCEBACK);
	}
	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affect.sourceMinor()==Affect.TYP_BUY)
		&&(affect.target()==this)
		&&(affect.tool()!=null)
		&&(myShopkeeper!=null)
		&&(affect.tool()==myShopkeeper))
			ExternalPlay.startTickDown(this,Host.ITEM_BOUNCEBACK,2000);
		return true;
	}
}
