package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class GenContainer extends Container
{
	protected String	readableText="";
	public GenContainer()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a generic container";
		baseEnvStats.setWeight(2);
		displayText="a generic container sits here.";
		description="Looks like a container";
		baseGoldValue=5;
		capacity=50;
		recoverEnvStats();
	}
	
	public GenContainer(String newName, 
				   String newDisplayText, 
				   String newDescription,
				   boolean newIsGettable,
				   int newBaseGoldValue,
				   int newWeight,
				   int newCapacity,
				   boolean newHasALock,
				   boolean newHasALid,
				   boolean newIsTrapped,
				   String newKeyName)
	{
		name=newName;
		isGettable=newIsGettable;
		baseEnvStats.setWeight(newWeight);
		displayText=newDisplayText;
		description=newDescription;
		baseGoldValue=newBaseGoldValue;
		capacity=newCapacity;
		recoverEnvStats();
		isTrapped=newIsTrapped;
		Thief_Trap.setTrapped(this,isTrapped);
		keyName=newKeyName;
	}
	
	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
	public Environmental newInstance()
	{
		return new GenContainer();
	}
	
	public String text()
	{
		return Generic.getPropertiesStr(this);
	}
	
	public void setMiscText(String newText)
	{
		miscText="";
		Generic.setPropertiesStr(this,newText);
		recoverEnvStats();
	}
}
