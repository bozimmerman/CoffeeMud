package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2001-2024 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "GenWeapon";
	}

	protected String	readableText="";

	public GenWeapon()
	{
		super();

		setName("a generic weapon");
		basePhyStats.setWeight(2);
		setDisplayText("a generic weapon sits here.");
		setDescription("");
		baseGoldValue=5;
		properWornBitmap=Wearable.WORN_WIELD|Wearable.WORN_HELD;
		wornLogicalAnd=false;
		weaponDamageType=Weapon.TYPE_BASHING;
		material=RawMaterial.RESOURCE_STEEL;
		basePhyStats().setAttackAdjustment(0);
		basePhyStats().setDamage(5);
		basePhyStats().setLevel(5);
		recoverPhyStats();
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getEnvironmentalMiscTextXML(this,false);
	}

	@Override
	public String readableText()
	{
		return readableText;
	}

	@Override
	public void setReadableText(final String text)
	{
		readableText=text;
	}

	@Override
	public void setMiscText(final String newText)
	{
		miscText="";
		CMLib.coffeeMaker().unpackEnvironmentalMiscTextXML(this,newText,false);
		recoverPhyStats();
	}

	protected final static String[] GENWEAPONCODES={"MINRANGE","MAXRANGE","WEAPONTYPE","WEAPONCLASS", "AMMOTYPE","AMMOCAPACITY"};

	private static String[] codes=null;

	protected static int getGenWeaponCodeNum(final String code)
	{
		for(int i=0;i<GENWEAPONCODES.length;i++)
		{
			if(code.equalsIgnoreCase(GENWEAPONCODES[i]))
				return i;
		}
		return -1;
	}

	protected static String getGenWeaponStat(final AmmunitionWeapon W, final String code)
	{
		switch(getGenWeaponCodeNum(code))
		{
		case 0:
			return "" + W.getRanges()[0];
		case 1:
			return "" + W.getRanges()[1];
		case 2:
			return CMStrings.s_indexStr(Weapon.TYPE_DESCS,W.weaponDamageType(),"");
		case 3:
			return CMStrings.s_indexStr(Weapon.CLASS_DESCS,W.weaponClassification(),"");
		case 4:
			return W.ammunitionType();
		case 5:
			return "" + W.rawAmmunitionCapacity();
		default:
			return "";
		}
	}

	public static void setGenWeaponStat(final AmmunitionWeapon W, final String code, final String val)
	{
		switch(getGenWeaponCodeNum(code))
		{
		case 0:
			W.setRanges(CMath.s_parseIntExpression(val), W.getRanges()[1]);
			break;
		case 1:
			W.setRanges(W.getRanges()[0], CMath.s_parseIntExpression(val));
			break;
		case 2:
			W.setWeaponDamageType(CMath.s_parseListIntExpression(Weapon.TYPE_DESCS, val));
			break;
		case 3:
			W.setWeaponClassification(CMath.s_parseListIntExpression(Weapon.CLASS_DESCS, val));
			break;
		case 4:
			W.setAmmunitionType(val);
			break;
		case 5:
			W.setAmmoCapacity(CMath.s_parseIntExpression(val));
			break;
		}
	}

	@Override
	public String getStat(final String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		if(GenWeapon.getGenWeaponCodeNum(code)>=0)
			return GenWeapon.getGenWeaponStat(this,code);
		return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		else
		if(GenWeapon.getGenWeaponCodeNum(code)>=0)
			GenWeapon.setGenWeaponStat(this,code,val);
		else
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
	}

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(GenWeapon.GENWEAPONCODES,this);
		final String[] superCodes=CMParms.toStringArray(GenericBuilder.GenItemCode.values());
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenWeapon))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}
}

