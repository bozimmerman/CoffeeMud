package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.Items.Basic.StdContainer;
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
import com.planet_ink.coffee_mud.Items.interfaces.Armor.SizeDeviation;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

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
public class StdArmor extends StdContainer implements Armor
{
	@Override
	public String ID()
	{
		return "StdArmor";
	}

	int		sheath			= 0;
	short	layer			= 0;
	short	layerAttributes	= 0;

	public StdArmor()
	{
		super();

		setName("a shirt of armor");
		setDisplayText("a thick armored shirt sits here.");
		setDescription("Thick padded leather with strips of metal interwoven.");
		properWornBitmap=Wearable.WORN_TORSO;
		wornLogicalAnd=false;
		basePhyStats().setArmor(10);
		basePhyStats().setAbility(0);
		baseGoldValue=150;
		setCapacity(0);
		setDoorsNLocks(false,true,false,false,false,false);
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
	public void setClothingLayer(final short newLayer)
	{
		layer = newLayer;
	}

	@Override
	public short getLayerAttributes()
	{
		return layerAttributes;
	}

	@Override
	public void setLayerAttributes(final short newAttributes)
	{
		layerAttributes = newAttributes;
	}

	@Override
	public boolean canWear(final MOB mob, final long where)
	{
		if(where==0)
			return (whereCantWear(mob)==0);
		if((rawProperLocationBitmap()&where)!=where)
			return false;
		return mob.freeWearPositions(where,getClothingLayer(),getLayerAttributes())>0;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(!StdWearable.doSizeCheck(this, myHost, msg))
			return false;
		return true;
	}


	@Override
	public SizeDeviation getSizingDeviation(final MOB mob)
	{
		return StdWearable.getSizingDeviation(this, mob);
	}

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		if((abilityImbuesMagic()&&(phyStats().ability()>0))||(this instanceof MiscMagic))
			phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_BONUS);
		if((basePhyStats().height()==0)
		&&(!amWearingAt(Wearable.IN_INVENTORY))
		&&(owner() instanceof MOB))
			basePhyStats().setHeight(((MOB)owner()).phyStats().height());
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		StdWearable.handleArmorConditionMsgs(this, msg);
	}

	@Override
	protected boolean abilityImbuesMagic()
	{
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		StdWearable.applyMagicArmorBonus(this, affected, affectableStats);
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected, affectableStats);
		StdWearable.applyMaterialSaves(this, affected, affectableStats);
	}

	@Override
	public int value()
	{
		if(usesRemaining()<1000)
			return (int)Math.round(CMath.mul(super.value(),CMath.div(usesRemaining(),100)));
		return super.value();
	}

	@Override
	public boolean subjectToWearAndTear()
	{
		if((usesRemaining()<=1000)&&(usesRemaining()>=0))
			return true;
		return false;
	}

	@Override
	public String secretIdentity()
	{
		return StdWearable.getSecretWearableIdentity(super.secretIdentity(), this);
	}
}
