package com.planet_ink.coffee_mud.Items.Weapons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenSling extends StdSling
{
	public String ID(){	return "GenSling";}
	protected String	readableText="";
	public GenSling()
	{
		super();

		setName("a generic sling");
		setDisplayText("a generic sling sits here.");
		setDescription("");
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenSling();
	}
	public boolean isGeneric(){return true;}


	public String text()
	{
		return CoffeeMaker.getPropertiesStr(this,false);
	}
	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}

	public void setMiscText(String newText)
	{
		miscText="";
		CoffeeMaker.setPropertiesStr(this,newText,false);
		recoverEnvStats();
	}
}

