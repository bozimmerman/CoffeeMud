package com.planet_ink.coffee_mud.Items.Armor;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.*;
import java.util.*;

public class GenArmor extends Armor
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
		properWornBitmap=Item.ON_TORSO;
		wornLogicalAnd=false;
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(10);
		recoverEnvStats();
	}
	
	public GenArmor(String newName, 
				   String newDisplayText, 
				   String newDescription,
				   String newSecretIdentity,
				   int newBaseGoldValue,
				   int newMaterialCode,
				   int newWeight,
				   int newArmor,
				   int newCapacity,
				   boolean newLogicalAnd,
				   long newProperWornBitmap)
	{
		name=newName;
		baseEnvStats.setWeight(newWeight);
		displayText=newDisplayText;
		secretIdentity=newSecretIdentity;
		description=newDescription;
		baseGoldValue=newBaseGoldValue;
		capacity=newCapacity;
		wornLogicalAnd=newLogicalAnd;
		properWornBitmap=newProperWornBitmap;
		baseEnvStats().setArmor(newArmor);
		material=newMaterialCode;
		recoverEnvStats();
	}
	
	public Environmental newInstance()
	{
		return new GenArmor();
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

