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
	public String getStat(String code)
	{ return Generic.getGenItemStat(this,code);}
	public void setStat(String code, String val)
	{ Generic.setGenItemStat(this,code,val);}
	public String[] getStatCodes(){return Generic.GENITEMCODES;}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenArmor)) return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
}

