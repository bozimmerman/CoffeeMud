package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenMap extends StdMap
{
	protected String	readableText="";
	public GenMap()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a generic map";
		baseEnvStats.setWeight(1);
		displayText="a generic map sits here.";
		description="Looks like a map of some place";
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
}
