package com.planet_ink.coffee_mud.Items.Weapons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenWeapon extends StdWeapon
{
	public String ID(){	return "GenWeapon";}
	protected String	readableText="";
	public GenWeapon()
	{
		super();

		name="a generic weapon";
		baseEnvStats.setWeight(2);
		displayText="a generic weapon sits here.";
		description="";
		baseGoldValue=5;
		properWornBitmap=Item.WIELD|Item.HELD;
		wornLogicalAnd=false;
		weaponType=Weapon.TYPE_BASHING;
		material=EnvResource.RESOURCE_STEEL;
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(5);
		baseEnvStats().setLevel(5);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenWeapon();
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

