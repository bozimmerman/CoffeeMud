package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenCigar extends StdSmokable
{
	public String ID(){	return "GenCigar";}
	private String readableText = "";
	public GenCigar()
	{
		super();
		setName("a generic cigar");
		baseEnvStats.setWeight(1);
		setDisplayText("a generic cigar sits here.");
		setDescription("");
		baseGoldValue=5;
		capacity=0;
		durationTicks=200;
		recoverEnvStats();
	}

	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
	public String keyName()
	{
		return readableText;
	}
	public void setKeyName(String newKeyName)
	{
		readableText=newKeyName;
	}
	public Environmental newInstance()
	{
		return new GenCigar();
	}
	public boolean isGeneric(){return true;}

	public String text()
	{
		return Generic.getPropertiesStr(this,false);
	}

	public void setMiscText(String newText)
	{
		miscText="";
		Generic.setPropertiesStr(this,newText,false);
		recoverEnvStats();
	}
	public String getStat(String code)
	{ return Generic.getGenItemStat(this,code);}
	public void setStat(String code, String val)
	{ Generic.setGenItemStat(this,code,val);}
	public String[] getStatCodes(){return Generic.GENITEMCODES;}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenItem)) return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
}