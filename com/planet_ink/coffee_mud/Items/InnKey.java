package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.MOBS.ShopKeeper;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class InnKey extends StdKey
{

	public ShopKeeper myShopkeeper=null;
	
	public InnKey()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a metal key";
		displayText="a small metal key sits here.";
		description="It says it goes to room 1.";
		
		baseGoldValue=10;
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new InnKey();
	}
	
	public boolean tick(int tickID)
	{
		if(tickID==ServiceEngine.ITEM_BOUNCEBACK)
		{
			this.destroyed=false;
			this.setLocation(null);
			if((myOwner()!=null)&&(myOwner()==myShopkeeper))
				return false;
			if(myOwner()!=null)
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
			int y=sk.numberInStock(INI.className(this));
			name="key to room "+(y+1);
			description="The key goes to room "+(y+1)+", but will expire soon, so you better use it quickly! Give the key to your innkeeper, "+sk.name()+", when you leave.";
			miscText="INN"+(y+1);
		}
	}
	
	public void affect(Affect affect)
	{
		super.affect(affect);
		if(((affect.targetCode()==Affect.HANDS_GIVE)
		||(affect.targetCode()==Affect.HANDS_SELL))
		&&(affect.target()==myShopkeeper)
		&&(affect.tool()==this))
			ServiceEngine.deleteTick(this,ServiceEngine.ITEM_BOUNCEBACK);
	}
	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		
		if((affect.sourceCode()==Affect.HANDS_BUY)
		&&(affect.target()==this)
		&&(affect.tool()!=null)
		&&(myShopkeeper!=null)
		&&(affect.tool()==myShopkeeper))
			ServiceEngine.startTickDown(this,ServiceEngine.ITEM_BOUNCEBACK,2000);
		return true;
	}
}
