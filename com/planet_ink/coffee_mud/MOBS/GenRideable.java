package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class GenRideable extends StdRideable
{

	public GenRideable()
	{
		super();
		Username="a generic horse";
		setDescription("He looks ordinary to me.");
		setDisplayText("A generic horse stands here.");

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new GenRideable();
	}
	public boolean isGeneric(){return true;}

	public String text()
	{
		miscText=Generic.getPropertiesStr(this,false);
		return super.text();
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		Generic.setPropertiesStr(this,newText,false);
		recoverEnvStats();
		recoverCharStats();
		baseState().setHitPoints((10*baseEnvStats().level())+Dice.roll(baseEnvStats().level(),baseEnvStats().ability(),1));
		baseState().setMana(baseCharStats().getCurrentClass().getLevelMana(this));
		baseState().setMovement(baseCharStats().getCurrentClass().getLevelMove(this));
		recoverMaxState();
		resetToMaxState();
		if(getWimpHitPoint()>0) setWimpHitPoint((int)Math.round(Util.mul(curState().getHitPoints(),.10)));
	}
}
