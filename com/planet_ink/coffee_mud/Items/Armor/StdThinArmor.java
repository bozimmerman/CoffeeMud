package com.planet_ink.coffee_mud.Items.Armor;
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
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.Armor.SizeDeviation;
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
public class StdThinArmor extends StdItem implements Armor
{
	@Override
	public String ID()
	{
		return "StdThinArmor";
	}
	int sheath=0;
	short layer=0;
	short layerAttributes=0;

	public StdThinArmor()
	{
		super();

		setName("a piece of armor");
		setDisplayText("a piece of armor here.");
		setDescription("Thick padded leather with strips of metal interwoven.");
		properWornBitmap=Wearable.WORN_EYES;
		wornLogicalAnd=false;
		basePhyStats().setArmor(1);
		basePhyStats().setAbility(0);
		baseGoldValue=150;
		setUsesRemaining(100);
		recoverPhyStats();
	}

	@Override
	public void setUsesRemaining(int newUses)
	{
		if(newUses==Integer.MAX_VALUE)
			newUses=100;
		super.setUsesRemaining(newUses);
	}

	@Override
	public short getClothingLayer()
	{
		return layer;
	}

	@Override
	public void setClothingLayer(short newLayer)
	{
		layer=newLayer;
	}

	@Override
	public short getLayerAttributes()
	{
		return layerAttributes;
	}

	@Override
	public void setLayerAttributes(short newAttributes)
	{
		layerAttributes=newAttributes;
	}

	@Override
	public boolean canWear(MOB mob, long where)
	{
		if(where==0) 
			return (whereCantWear(mob)==0);
		if((rawProperLocationBitmap()&where)!=where)
			return false;
		return mob.freeWearPositions(where,getClothingLayer(),getLayerAttributes())>0;
	}

	@Override
	public SizeDeviation getSizingDeviation(MOB mob)
	{
		return SizeDeviation.FITS;
	}

	@Override
	public boolean subjectToWearAndTear()
	{
		return false;
	}

	@Override
	public String secretIdentity()
	{
		String id=super.secretIdentity();
		if(phyStats().ability()>0)
			id=name()+" +"+phyStats().ability()+((id.length()>0)?"\n\r":"")+id;
		else
		if(phyStats().ability()<0)
			id=name()+" "+phyStats().ability()+((id.length()>0)?"\n\r":"")+id;
		return id+"\n\r"+L("Base Protection: @x1",""+phyStats().armor());
	}
}
