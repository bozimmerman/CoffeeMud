package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class GenRideable extends StdRideable
{
	public String ID(){return "GenRideable";}
	public GenRideable()
	{
		super();
		Username="a generic horse";
		setDescription("");
		setDisplayText("A generic horse stands here.");
		baseEnvStats().setAbility(11); // his only off-default

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

	public boolean isGeneric(){return true;}

	public String text()
	{
		if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBCOMPRESS))
			miscText=Util.compressString(CoffeeMaker.getPropertiesStr(this,false));
		else
			miscText=CoffeeMaker.getPropertiesStr(this,false).getBytes();
		return super.text();
	}

	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		CoffeeMaker.resetGenMOB(this,newText);
	}
	private static String[] MYCODES={"RIDEBASIS","MOBSHELD"};
	public String getStat(String code)
	{
		if(CoffeeMaker.getGenMobCodeNum(code)>=0)
			return CoffeeMaker.getGenMobStat(this,code);
		else
		switch(getCodeNum(code))
		{
		case 0: return ""+rideBasis();
		case 1: return ""+riderCapacity();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		if(CoffeeMaker.getGenMobCodeNum(code)>=0)
			CoffeeMaker.setGenMobStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0: setRideBasis(Util.s_int(val)); break;
		case 1: setRiderCapacity(Util.s_int(val)); break;
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
		String[] superCodes=CoffeeMaker.GENMOBCODES;
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
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
