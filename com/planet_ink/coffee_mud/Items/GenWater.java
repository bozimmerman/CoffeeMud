package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenWater extends StdDrink
{
	public String ID(){	return "GenWater";}
	protected String	readableText="";
	public GenWater()
	{
		super();
		name="a generic puddle of water";
		baseEnvStats.setWeight(2);
		displayText="a generic puddle of water sits here.";
		description="";
		baseGoldValue=5;
		capacity=0;
		amountOfThirstQuenched=250;
		amountOfLiquidHeld=2000;
		amountOfLiquidRemaining=2000;
		setMaterial(EnvResource.RESOURCE_LEATHER);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new GenWater();
	}
	public boolean isGeneric(){return true;}

	public String text()
	{
		return Generic.getPropertiesStr(this,false);
	}
	public int liquidType(){
		if((material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LIQUID)
			return material();
		if(Util.s_int(readableText)==0) return EnvResource.RESOURCE_FRESHWATER;
		return Util.s_int(readableText);
	}
	public void setLiquidType(int newLiquidType){readableText=""+newLiquidType;}

	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
	public void setMiscText(String newText)
	{
		miscText="";
		Generic.setPropertiesStr(this,newText,false);
		recoverEnvStats();
	}
	private static String[] MYCODES={"HASLOCK","HASLID","CAPACITY","CONTAINTYPES",
							  "QUENCHED","LIQUIDHELD","LIQUIDTYPE"};
	public String getStat(String code)
	{
		if(Generic.getGenItemCodeNum(code)>=0)
			return Generic.getGenItemStat(this,code);
		else
		switch(getCodeNum(code))
		{
		case 0: return ""+hasALock();
		case 1: return ""+hasALid();
		case 2: return ""+capacity();
		case 3: return ""+containTypes();
		case 4: return ""+thirstQuenched();
		case 5: return ""+liquidHeld();
		case 6: return ""+liquidType();
		}
		return "";
	}
	public void setStat(String code, String val)
	{ 
		if(Generic.getGenItemCodeNum(code)>=0)
			Generic.setGenItemStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0: setLidsNLocks(hasALid(),isOpen(),Util.s_bool(val),false); break;
		case 1: setLidsNLocks(Util.s_bool(val),isOpen(),hasALock(),false); break;
		case 2: setCapacity(Util.s_int(val)); break;
		case 3: setContainTypes(Util.s_long(val)); break;
		case 4: setThirstQuenched(Util.s_int(val)); break;
		case 5: setLiquidHeld(Util.s_int(val)); break;
		case 6: setLiquidType(Util.s_int(val)); break;
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
		String[] superCodes=Generic.GENITEMCODES;
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
		if(!(E instanceof GenWater)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
