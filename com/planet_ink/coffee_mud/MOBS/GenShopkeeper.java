package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class GenShopkeeper extends StdShopKeeper
{
	private String PrejudiceFactors="";

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
	public boolean isGeneric(){return true;}

	public String prejudiceFactors(){return PrejudiceFactors;}
	public void setPrejudiceFactors(String factors){PrejudiceFactors=factors;}
	public String text()
	{
		miscText=Util.compressString(Generic.getPropertiesStr(this,false));
		return super.text();
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		Generic.setPropertiesStr(this,newText,false);
		baseState().setHitPoints((10*baseEnvStats().level())+Dice.roll(baseEnvStats().level(),baseEnvStats().ability(),1));
		recoverEnvStats();
		recoverCharStats();
		recoverMaxState();
		resetToMaxState();
		if(getWimpHitPoint()>0) setWimpHitPoint((int)Math.round(Util.mul(curState().getHitPoints(),.10)));
	}
}
