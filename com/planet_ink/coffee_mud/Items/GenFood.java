package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenFood extends StdFood
{
	public String ID(){	return "GenFood";}
	protected String	readableText="";
	public GenFood()
	{
		super();

		name="a generic blob of food";
		baseEnvStats.setWeight(2);
		displayText="a generic blob of food sits here.";
		description="";
		baseGoldValue=5;
		amountOfNourishment=500;
		recoverEnvStats();
		setMaterial(EnvResource.RESOURCE_MEAT);
	}

	public Environmental newInstance()
	{
		return new GenFood();
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
}
