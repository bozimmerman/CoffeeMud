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
public class GenUndead extends Undead
{
	
	public GenUndead()
	{
		super();
		Username="a generic undead being";
		setDescription("He looks dead to me.");
		setDisplayText("A generic undead mob stands here.");
		setAlignment(0);
		setMoney(10);
		baseEnvStats.setWeight(150);
		setWimpHitPoint(0);
		
		baseCharStats().setIntelligence(6);
		baseCharStats().setCharisma(2);
		
		baseEnvStats().setAbility(10);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(50);
		
		maxState.setHitPoints((10*baseEnvStats().level())+(int)Math.round(Math.random()*baseEnvStats().level()*baseEnvStats().ability()));
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new GenUndead();
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
