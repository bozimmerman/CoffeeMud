package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.db.*;
public class GenShopkeeper extends ShopKeeper
{
	
	public GenShopkeeper()
	{
		super();
		Username="a generic shopkeeper";
		setDescription("He looks like he wants to sell something to you.");
		setDisplayText("A generic shopkeeper stands here.");
	}
	public Environmental newInstance()
	{
		return new GenShopkeeper();
	}
	public void bringToLife(Room newLocation)
	{
		setMiscText(text());
		super.bringToLife(newLocation);
	}
	
	public String text()
	{
		return Generic.getPropertiesStr(this);
	}
	
	public void setMiscText(String newText)
	{
		miscText="";
		Generic.setPropertiesStr(this,newText);
		maxState().setHitPoints((10*baseEnvStats().level())+Dice.roll(baseEnvStats().level(),baseEnvStats().ability(),1));
		recoverEnvStats();
		recoverCharStats();
		recoverMaxState();
	}
}
