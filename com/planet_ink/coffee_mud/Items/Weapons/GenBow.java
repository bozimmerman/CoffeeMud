package com.planet_ink.coffee_mud.Items.Weapons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenBow extends StdBow
{
	public String ID(){	return "GenBow";}
	protected String	readableText="";
	public GenBow()
	{
		super();
		setName("a generic short bow");
		setDisplayText("a generic short bow sits here.");
		setDescription("");
		setAmmunitionType("arrows");
		setAmmoCapacity(20);
		setAmmoRemaining(20);
		recoverEnvStats();
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

