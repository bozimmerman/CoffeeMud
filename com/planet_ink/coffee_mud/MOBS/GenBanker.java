package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class GenBanker extends StdBanker
{
	private String PrejudiceFactors="";
	private String bankChain="GenBank";
	
	public GenBanker()
	{
		super();
		Username="a generic banker";
		setDescription("He looks like he wants your money.");
		setDisplayText("A generic banker stands here.");
	}
	public Environmental newInstance()
	{
		return new GenBanker();
	}
	public boolean isGeneric(){return true;}

	public String text()
	{
		miscText=Generic.getPropertiesStr(this,false);
		return super.text();
	}

	public String prejudiceFactors(){return PrejudiceFactors;}
	public void setPrejudiceFactors(String factors){PrejudiceFactors=factors;}
	public String bankChain(){return bankChain;}
	public void setBankChain(String name){bankChain=name;}
	
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
