package com.planet_ink.coffee_mud.Items.Weapons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenSpear extends StdSpear
{
	public String ID(){	return "GenSpear";}
	protected String	readableText="";
	public GenSpear()
	{
		super();

		setName("a generic spear");
		setDisplayText("a generic spear sits here.");
		setDescription("");
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenSpear();
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

