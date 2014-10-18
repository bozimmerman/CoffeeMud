package com.planet_ink.coffee_mud.Items.Armor;
import java.util.Enumeration;

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
import com.planet_ink.coffee_mud.Items.Basic.GenItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


/*
   Copyright 2004-2014 Bo Zimmerman

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
public class GenEarring extends GenThinArmor
{
	@Override public String ID(){	return "GenEarring";}
	
	private final String wearLoc = null;
	
	public GenEarring()
	{
		super();

		setName("a pretty earring");
		setDisplayText("a pretty earring lies here");
		setDescription("It`s very pretty, and has a little clip for going in a pierced bodypart.");
		properWornBitmap=Wearable.WORN_EARS;
		wornLogicalAnd=true;
		basePhyStats().setArmor(0);
		basePhyStats().setWeight(1);
		basePhyStats().setAbility(0);
		baseGoldValue=40;
		layer=(short)-1;
		recoverPhyStats();
		material=RawMaterial.RESOURCE_GOLD;
	}

	protected boolean hasFreePiercing(final MOB mob, long wornCode)
	{
		if(mob==null) 
			return false;
		final Wearable.CODES codes = Wearable.CODES.instance();
		final String wearLocName = codes.nameup(wornCode);
		for(final Enumeration<MOB.Tattoo> e=mob.tattoos();e.hasMoreElements();)
		{
			final String tattooName=e.nextElement().tattooName.toUpperCase();
			if(tattooName.startsWith(wearLocName+":") 
			&& (tattooName.substring(wearLocName.length()+1).indexOf("PIERCE")<5))
				return true;
		}
		return false;
	}
	
	protected boolean hasFreePiercingFor(final MOB mob, long wornCodes)
	{
		final Wearable.CODES codes = Wearable.CODES.instance();
		if(super.wornLogicalAnd)
		{
			for(long code : codes.all())
				if((code != 0) 
				&& (code != Wearable.WORN_HELD)
				&& CMath.bset(wornCodes,code)
				&& (!hasFreePiercing(mob, code)))
					return false;
			return true;
		}
		else
		{
			for(long code : codes.all())
				if((code != 0) 
				&& CMath.bset(wornCodes,code)
				&&((code == Wearable.WORN_HELD)
					||(hasFreePiercing(mob, code))))
					return true;
			return false;
		}
	}
	
	@Override
	public boolean canWear(MOB mob, long where)
	{
		if(!super.canWear(mob, where))
			return false;
		return hasFreePiercingFor(mob,where);
	}
	
	@Override
	public long whereCantWear(MOB mob)
	{
		long where=super.whereCantWear(mob);
		final Wearable.CODES codes = Wearable.CODES.instance();
		for(long code : codes.all())
			if((code != 0)
			&& fitsOn(code)
			&&(code!=Item.WORN_HELD)
			&&(!CMath.bset(where,code))
			&&(!hasFreePiercing(mob, code)))
				where = where | code;
		return where;
	}
	
	//TODO: wearing this should alter the name so it is clear in which piercing it is being worn
}
