package com.planet_ink.coffee_mud.Items.Armor;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenArmor extends StdArmor
{
	protected String	readableText="";
	public GenArmor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a generic armor piece";
		baseEnvStats.setWeight(25);
		displayText="a generic piece of armor sits here.";
		description="Looks like armor";
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
}

