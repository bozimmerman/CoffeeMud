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
		if(getWimpHitPoint()>0) setWimpHitPoint((int)Math.round(Util.mul(curState().getHitPoints(),.10)));
	}
	private static String[] MYCODES={"RIDEBASIS","MOBSHELD"};
	public String getStat(String code)
	{
		if(Generic.getGenMobCodeNum(code)>=0)
			return Generic.getGenMobStat(this,code);
		else
		switch(getCodeNum(code))
		{
		case 0: return ""+rideBasis();
		case 1: return ""+mobCapacity();
		}
		return "";
	}
	public void setStat(String code, String val)
	{ 
		if(Generic.getGenMobCodeNum(code)>=0)
			Generic.setGenMobStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0: setRideBasis(Util.s_int(val)); break;
		case 1: setMobCapacity(Util.s_int(val)); break;
		}
	}
	protected int getCodeNum(String code){
		for(int i=0;i<MYCODES.length;i++)
			if(code.equalsIgnoreCase(MYCODES[i])) return i;
		return -1;
	}
	private static String[] codes=null;
	public String[] getStatCodes()
	{
		if(codes!=null) return codes;
		String[] superCodes=Generic.GENMOBCODES;
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<=superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenRideable)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
