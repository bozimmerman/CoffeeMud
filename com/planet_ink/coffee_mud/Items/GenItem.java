package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class GenItem extends StdItem
{
	protected String	readableText="";
	public GenItem()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a generic item";
		baseEnvStats.setWeight(2);
		displayText="a generic item sits here.";
		description="Looks like something";
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		recoverEnvStats();
	}
	
	public GenItem(String newName, 
				   String newDisplayText, 
				   String newDescription,
				   boolean newIsDroppable,
				   boolean newIsGettable,
				   boolean newIsRemovable,
				   int newBaseGoldValue,
				   boolean newIsTrapped,
				   int newWeight)
	{
		name=newName;
		baseEnvStats.setWeight(newWeight);
		displayText=newDisplayText;
		description=newDescription;
		baseGoldValue=newBaseGoldValue;
		isDroppable=newIsDroppable;
		isGettable=newIsGettable;
		isRemovable=newIsRemovable;
		isTrapped=newIsTrapped;
		Thief_Trap.setTrapped(this,isTrapped);
		recoverEnvStats();
	}
	
	public Environmental newInstance()
	{
		return new GenItem();
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
