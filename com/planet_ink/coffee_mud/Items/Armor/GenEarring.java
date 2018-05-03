package com.planet_ink.coffee_mud.Items.Armor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2014-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "GenEarring";
	}
	
	private String wearLocDesc = null;
	private final Map<Long,String> wearLocs = new TreeMap<Long,String>();
	
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
		layer=(short)-10;
		layerAttributes=Armor.LAYERMASK_MULTIWEAR;
		recoverPhyStats();
		material=RawMaterial.RESOURCE_GOLD;
	}

	protected int numWorn(final MOB mob, long wornCode)
	{
		int numWorn = 0;
		for(Item I : mob.fetchWornItems(wornCode, layer, layerAttributes))
		{
			if(I instanceof GenEarring)
				numWorn++;
		}
		return numWorn;
	}
	
	protected boolean hasFreePiercing(final MOB mob, long wornCode)
	{
		if(mob==null) 
			return false;
		final Wearable.CODES codes = Wearable.CODES.instance();
		final String wearLocName = codes.nameup(wornCode);
		int availablePiercings=0;
		for(final Enumeration<Tattoo> e=mob.tattoos();e.hasMoreElements();)
		{
			final String tattooName=e.nextElement().getTattooName().toUpperCase();
			if(tattooName.startsWith(wearLocName+":") 
			&& (tattooName.substring(wearLocName.length()+1).indexOf("PIERCE")>=0))
				availablePiercings++;
		}
		if(availablePiercings==0)
			return false;
		return availablePiercings > numWorn(mob,wornCode);
	}
	
	protected boolean hasFreePiercingFor(final MOB mob, long wornCodes)
	{
		final Wearable.CODES codes = Wearable.CODES.instance();
		if(super.wornLogicalAnd)
		{
			for(long code : codes.all())
			{
				if((code != 0) 
				&& (code != Wearable.WORN_HELD)
				&& CMath.bset(wornCodes,code)
				&& (!hasFreePiercing(mob, code)))
					return false;
			}
			return true;
		}
		else
		{
			for(long code : codes.all())
			{
				if((code != 0) 
				&& CMath.bset(wornCodes,code)
				&&((code == Wearable.WORN_HELD)
					||(hasFreePiercing(mob, code))))
					return true;
			}
			return false;
		}
	}
	
	@Override
	public boolean canWear(MOB mob, long where)
	{
		if(!super.canWear(mob, where))
			return false;
		if(where==0)
			return true;
		return hasFreePiercingFor(mob,where);
	}
	
	@Override
	public long whereCantWear(MOB mob)
	{
		long where=super.whereCantWear(mob);
		final Wearable.CODES codes = Wearable.CODES.instance();
		if(where == 0)
		{
			for(long code : codes.all())
			{
				if((code != 0)
				&& fitsOn(code)
				&&(code!=Item.WORN_HELD)
				&&(!CMath.bset(where,code)))
				{
					if(hasFreePiercing(mob, code))
						return 0;
					else
						where = where | code;
				}
			}
		}
		return where;
	}
	
	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		if((owner instanceof MOB)&&(!super.amWearingAt(Wearable.IN_INVENTORY)))
		{
			if(wearLocDesc == null)
			{
				synchronized(this)
				{
					if(wearLocDesc == null)
					{
						wearLocs.clear();
						final List<String> dispWearLocs=new LinkedList<String>();
						final MOB mob=(MOB)owner();
						final Wearable.CODES codes = Wearable.CODES.instance();
						final List<GenEarring> wornStuff = new ArrayList<GenEarring>(2);
						for(final Enumeration<Item> i = mob.items(); i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if((I instanceof GenEarring)
							&& (I!=this)
							&& (!I.amWearingAt(Item.IN_INVENTORY))
							&& ((I.rawWornCode() & this.rawWornCode()) != 0))
								wornStuff.add((GenEarring)I);
						}
						for(long wornCode : CMath.getSeperateBitMasks(myWornCode))
						{
							final List<String> availablePiercingsThisLoc = new ArrayList<String>(2);
							final String wearLocName = codes.nameup(wornCode);
							for(final Enumeration<Tattoo> e=mob.tattoos();e.hasMoreElements();)
							{
								final String tattooName=e.nextElement().getTattooName().toUpperCase();
								if(tattooName.startsWith(wearLocName+":") 
								&& (tattooName.substring(wearLocName.length()+1).indexOf("PIERCE")>=0))
									availablePiercingsThisLoc.add(tattooName.substring(wearLocName.length()+1).toLowerCase());
							}
							final Long wornCodeL=Long.valueOf(wornCode);
							for(final GenEarring I : wornStuff)
							{
								if((I.wearLocs!=null) && ((I.rawWornCode() & wornCode)!=0) 
								&& (I.wearLocs.containsKey(wornCodeL)))
									availablePiercingsThisLoc.remove(I.wearLocs.remove(wornCodeL));
							}
							if(availablePiercingsThisLoc.size()>0)
							{
								final String loc=availablePiercingsThisLoc.get(0);
								if(!CMLib.english().startsWithAnArticle(loc))
									dispWearLocs.add("both "+loc);
								else
									dispWearLocs.add(loc);
								wearLocs.put(wornCodeL, loc);
							}
						}
						if(wearLocs.size() > 0)
							wearLocDesc = " on "+CMLib.english().toEnglishStringList(dispWearLocs); 
					}
				}
			}
			if((wearLocDesc != null) && (wearLocDesc.length()>0))
				phyStats().setName(name + wearLocDesc);
		}
		else
			this.wearLocDesc = null;
	}
}
