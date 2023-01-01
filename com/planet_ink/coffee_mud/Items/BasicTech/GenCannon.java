package com.planet_ink.coffee_mud.Items.BasicTech;
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
import com.planet_ink.coffee_mud.Items.Software.GenSoftware;
import com.planet_ink.coffee_mud.Items.Weapons.GenSiegeWeapon;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2022-2023 Bo Zimmerman

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
public class GenCannon extends GenSiegeWeapon implements Technical
{
	@Override
	public String ID()
	{
		return "GenCannon";
	}

	protected String 		manufacturer	= "RANDOM";
	protected Manufacturer  cachedManufact  = null;

	public GenCannon()
	{
		super();
		setName("a generic cannon");
		setDisplayText("a generic cannon is sitting here.");
		setDescription("");
		setMaterial(RawMaterial.RESOURCE_IRON);
		weaponDamageType=Weapon.TYPE_PIERCING;
		setAmmunitionType("cannonballs");
	}

	@Override
	public int techLevel()
	{
		return phyStats().ability();
	}

	@Override
	public void setTechLevel(final int lvl)
	{
		basePhyStats.setAbility(lvl);
		recoverPhyStats();
	}

	@Override
	public TechType getTechType()
	{
		return TechType.PERSONAL_WEAPON;
	}

	@Override
	public String getManufacturerName()
	{
		return manufacturer;
	}

	@Override
	public void setManufacturerName(final String name)
	{
		cachedManufact = null;
		if (name != null)
			manufacturer = name;
	}

	@Override
	public Manufacturer getFinalManufacturer()
	{
		if(cachedManufact==null)
		{
			cachedManufact=CMLib.tech().getManufacturerOf(this,manufacturer.toUpperCase().trim());
			if(cachedManufact==null)
				cachedManufact=CMLib.tech().getDefaultManufacturer();
		}
		return cachedManufact;
	}

	@Override
	public String getStat(final String code)
	{
		if(CMParms.contains(super.getStatCodes(),code))
			return super.getStat(code);
		else
		switch(getInternalCodeNum(code))
		{
		case 0:
			return getManufacturerName();
		default:
			break;
		}
		return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(CMParms.contains(super.getStatCodes(),code))
			super.setStat(code, val);
		else
		switch(getInternalCodeNum(code))
		{
		case 0:
			setManufacturerName(val);
			break;
		default:
			break;
		}
		CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
	}

	private final static String[] MYCODES={"MANUFACTURER"};

	private static String[] codes=null;

	private int getInternalCodeNum(final String code)
	{
		for(int i=0;i<MYCODES.length;i++)
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(GenCannon.MYCODES,this);
		final String[] superCodes=super.getStatCodes();
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}
}
