package com.planet_ink.coffee_mud.Items.Weapons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class GenWeapon extends StdWeapon
{
	public String ID(){	return "GenWeapon";}
	protected String	readableText="";
	public GenWeapon()
	{
		super();

		setName("a generic weapon");
		baseEnvStats.setWeight(2);
		setDisplayText("a generic weapon sits here.");
		setDescription("");
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
	private static String[] MYCODES={"MINRANGE","MAXRANGE","WEAPONTYPE","WEAPONCLASS",
							  "AMMOTYPE","AMMOCAPACITY"};
	public String getStat(String code)
	{
		if(CoffeeMaker.getGenItemCodeNum(code)>=0)
			return CoffeeMaker.getGenItemStat(this,code);
		else
		switch(getCodeNum(code))
		{
		case 0: return ""+minRange();
		case 1: return ""+maxRange();
		case 2: return ""+weaponType();
		case 3: return ""+weaponClassification();
		case 4: return ammunitionType();
		case 5: return ""+ammunitionCapacity();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		if(CoffeeMaker.getGenItemCodeNum(code)>=0)
			CoffeeMaker.setGenItemStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0: setRanges(Util.s_int(val),maxRange()); break;
		case 1: setRanges(minRange(),Util.s_int(val)); break;
		case 2: setWeaponType(Util.s_int(val)); break;
		case 3: setWeaponClassification(Util.s_int(val)); break;
		case 4: setAmmunitionType(val); break;
		case 5: setAmmoCapacity(Util.s_int(val)); break;
		}
	}
	protected int getCodeNum(String code){
		for(int i=0;i<MYCODES.length;i++)
			if(code.equalsIgnoreCase(MYCODES[i])) return i;
		return -1;
	}
	private static String[] codes=null;
	public String[] getStatCodes()
	{
		if(codes!=null) return codes;
		String[] superCodes=CoffeeMaker.GENITEMCODES;
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenWeapon)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}

