package com.planet_ink.coffee_mud.Items.Weapons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Abilities.*;
import java.util.*;

public class GenWeapon extends Weapon
{
	protected String	readableText="";
	public GenWeapon()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a generic weapon";
		baseEnvStats.setWeight(2);
		displayText="a generic weapon sits here.";
		description="Looks like a weapon";
		baseGoldValue=5;
		properWornBitmap=Item.WIELD|Item.HELD;
		wornLogicalAnd=false;
		weaponType=Weapon.TYPE_BASHING;
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(5);
		baseEnvStats().setLevel(5);
		recoverEnvStats();
	}
	
	public GenWeapon(String newName, 
				   String newDisplayText, 
				   String newDescription,
				   String newSecretIdentity,
				   int newBaseGoldValue,
				   int newWeight,
				   int newAttack,
				   int newDamage,
				   int newWeaponType,
				   int newWeaponClassification,
				   boolean twoHanded)
	{
		name=newName;
		baseEnvStats.setWeight(newWeight);
		displayText=newDisplayText;
		description=newDescription;
		secretIdentity=newSecretIdentity;
		baseGoldValue=newBaseGoldValue;
		wornLogicalAnd=false;
		properWornBitmap=Item.HELD|Item.WIELD;
		if(twoHanded)
			wornLogicalAnd=true;
		baseEnvStats().setAttackAdjustment(newAttack);
		baseEnvStats().setDamage(newDamage);
		weaponType=newWeaponType;
		weaponClassification=newWeaponClassification;
		recoverEnvStats();
	}
	
	public Environmental newInstance()
	{
		return new GenWeapon();
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

