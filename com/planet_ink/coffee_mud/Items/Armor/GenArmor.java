package com.planet_ink.coffee_mud.Items.Armor;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenArmor extends StdArmor
{
	public String ID(){	return "GenArmor";}
	protected String	readableText="";
	public GenArmor()
	{
		super();

		name="a generic armor piece";
		baseEnvStats.setWeight(25);
		displayText="a generic piece of armor sits here.";
		description="";
		baseGoldValue=5;
		properWornBitmap=Item.INVENTORY;
		wornLogicalAnd=false;
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(10);
		recoverEnvStats();
		material=EnvResource.RESOURCE_LEATHER;
	}

	public Environmental newInstance()
	{
		return new GenArmor();
	}
	public boolean isGeneric(){return true;}


	public String text()
	{
		return Generic.getPropertiesStr(this,false);
	}
	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}

	public void setMiscText(String newText)
	{
		miscText="";
		Generic.setPropertiesStr(this,newText,false);
		recoverEnvStats();
	}
	private static String[] MYCODES={"HASLOCK","HASLID","CAPACITY","CONTAINTYPES"};
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
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenArmor)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}

