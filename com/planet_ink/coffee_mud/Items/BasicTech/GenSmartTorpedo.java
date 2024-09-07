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

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2022-2024 Bo Zimmerman

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
public class GenSmartTorpedo extends StdSmartTorpedo
{
	@Override
	public String ID()
	{
		return "GenSmartTorpedo";
	}

	protected String	readableText	= "";

	public GenSmartTorpedo()
	{
		super();
		setName("a generic smart torpedo");
		setDisplayText("a generic smart torpedo is sitting here");
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getEnvironmentalMiscTextXML(this, false);
	}

	@Override
	public String readableText()
	{
		return readableText;
	}

	@Override
	public void setReadableText(final String text)
	{
		readableText = text;
	}

	@Override
	public void setMiscText(final String newText)
	{
		miscText = "";
		CMLib.coffeeMaker().unpackEnvironmentalMiscTextXML(this, newText, false);
		recoverPhyStats();
	}

	private final static String[] MYCODES=
	{
		"TECHLEVEL","COORDS","RADIUS","DIRECTION","SPEED",
		"MINRANGE","MAXRANGE","WEAPONTYPE","WEAPONCLASS",
		"MANUFACTURER","TECHLEVEL"
	};

	@Override
	public String getStat(final String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getInternalCodeNum(code))
		{
		case 0:
			return "" + techLevel();
		case 1:
			return CMParms.toListString(coordinates().toLongs());
		case 2:
			return "" + radius();
		case 3:
			return CMParms.toListString(direction());
		case 4:
			return "" + speed();
		case 5:
			return "" + minRange();
		case 6:
			return "" + maxRange();
		case 7:
			return CMStrings.s_indexStr(Weapon.TYPE_DESCS,weaponDamageType(),"");
		case 8:
			return CMStrings.s_indexStr(Weapon.CLASS_DESCS,weaponClassification(),"");
		case 9:
			return "" + getManufacturerName();
		case 10:
			return "" + techLevel();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		else
		switch(getInternalCodeNum(code))
		{
		case 0:
			setTechLevel(CMath.s_parseIntExpression(val));
			break;
		case 1:
			setCoords(new Coord3D(CMParms.toLongArray(CMParms.parseCommas(val, true))));
			coordinates.x(coordinates.x().longValue() % SpaceObject.Distance.GalaxyRadius.dm);
			coordinates.y(coordinates.y().longValue() % SpaceObject.Distance.GalaxyRadius.dm);
			coordinates.z(coordinates.z().longValue() % SpaceObject.Distance.GalaxyRadius.dm);
			break;
		case 2:
			setRadius(CMath.s_long(val));
			break;
		case 3:
			setDirection(CMParms.toDoubleArray(CMParms.parseCommas(val, true)));
			break;
		case 4:
			setSpeed(CMath.s_double(val));
			break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	private int getInternalCodeNum(final String code)
	{
		for(int i=0;i<MYCODES.length;i++)
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}

	private static String[] codes=null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(GenSmartTorpedo.MYCODES,this);
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
		if(!(E instanceof GenSmartTorpedo))
			return false;
		final String[] theCodes=getStatCodes();
		for(int i=0;i<theCodes.length;i++)
		{
			if(!E.getStat(theCodes[i]).equals(getStat(theCodes[i])))
				return false;
		}
		return true;
	}
}
