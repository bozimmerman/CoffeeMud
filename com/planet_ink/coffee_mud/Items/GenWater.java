package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenWater extends StdDrink
{
	protected String	readableText="";
	public GenWater()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a generic puddle of water";
		baseEnvStats.setWeight(2);
		displayText="a generic puddle of water sits here.";
		description="Looks like a puddle";
		baseGoldValue=5;
		capacity=0;
		amountOfThirstQuenched=200;
		amountOfLiquidHeld=500;
		amountOfLiquidRemaining=500;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new GenWater();
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
