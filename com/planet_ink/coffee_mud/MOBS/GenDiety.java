package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class GenDiety extends StdDiety
{
	public GenDiety()
	{
		super();
		Username="a generic diety";
		setDescription("He looks like a run of the mill diety.");
		setDisplayText("A generic diety stands here.");

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new GenDiety();
	}
	public boolean isGeneric(){return true;}

	public String text()
	{
		miscText=Util.compressString(Generic.getPropertiesStr(this,false));
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
	}
}
