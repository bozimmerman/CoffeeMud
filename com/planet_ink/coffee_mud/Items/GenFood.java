package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class GenFood extends Food
{
	protected String	readableText="";
	public GenFood()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a generic blob of food";
		baseEnvStats.setWeight(2);
		displayText="a generic blob of food sits here.";
		description="Looks like some mystery meat";
		baseGoldValue=5;
		amountOfNourishment=300;
		recoverEnvStats();
	}
	
	public GenFood(String newName, 
				   String newDisplayText, 
				   String newDescription,
				   int newBaseGoldValue,
				   int newWeight,
				   int newAmountOfNourishment)
	{
		name=newName;
		baseEnvStats.setWeight(newWeight);
		displayText=newDisplayText;
		description=newDescription;
		baseGoldValue=newBaseGoldValue;
		amountOfNourishment=newAmountOfNourishment;
		recoverEnvStats();
	}
	
	public Environmental newInstance()
	{
		return new GenFood();
	}
	
	public String text()
	{
		return Generic.getPropertiesStr(this);
	}
	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
	
	public void setMiscText(String newText)
	{
		miscText="";
		Generic.setPropertiesStr(this,newText);
		recoverEnvStats();
	}
}
