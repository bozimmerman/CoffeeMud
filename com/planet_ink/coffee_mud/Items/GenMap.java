package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenMap extends StdMap
{
	public String ID(){	return "GenMap";}
	protected String	readableText="";
	public GenMap()
	{
		super();
		name="a generic map";
		baseEnvStats.setWeight(1);
		displayText="a generic map sits here.";
		description="";
		baseGoldValue=5;
		setMaterial(EnvResource.RESOURCE_PAPER);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new GenMap();
	}
	public boolean isGeneric(){return true;}

	public String text()
	{
		return Generic.getPropertiesStr(this,false);
	}
	public String readableText(){return readableText;}
	public String getMapArea(){return readableText;}
	public void setMapArea(String mapName)
	{
		setReadableText(mapName);
	}

	public void setReadableText(String newReadableText)
	{
		String oldName=name();
		String oldDesc=description();
		readableText=newReadableText;
		doMapArea();
		setName(oldName);
		setDescription(oldDesc);
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
		if(!(E instanceof GenMap)) return false;
		for(int i=0;i<getStatCodes().length;i++)
			if(!E.getStat(getStatCodes()[i]).equals(getStat(getStatCodes()[i])))
				return false;
		return true;
	}
}
