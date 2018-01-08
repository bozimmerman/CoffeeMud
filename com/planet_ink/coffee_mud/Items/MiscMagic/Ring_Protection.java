package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.Items.Basic.Ring_Ornamental;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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
public class Ring_Protection extends Ring_Ornamental implements MiscMagic
{
	@Override
	public String ID()
	{
		return "Ring_Protection";
	}

	public Ring_Protection()
	{
		super();

		lastLevel = -1;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((!this.amWearingAt(Wearable.IN_INVENTORY))&&(!this.amWearingAt(Wearable.WORN_HELD)))
			affectableStats.setArmor(affectableStats.armor()-phyStats().armor()-phyStats().ability());
	}

	@Override
	public void recoverPhyStats()
	{
		basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_BONUS);
		super.recoverPhyStats();
		if(lastLevel!=basePhyStats().level())
		{
			lastLevel = basePhyStats().level();
			setIdentity();
		}
	}

	protected int correctTargetMinor()
	{
		switch(this.phyStats().level())
		{
			case SILVER_RING:
				return CMMsg.TYP_COLD;
			case COPPER_RING:
				return CMMsg.TYP_ELECTRIC;
			case PLATINUM_RING:
				return CMMsg.TYP_GAS;
			case GOLD_RING_DIAMOND:
				return CMMsg.TYP_FIRE;
			case GOLD_RING:
				return CMMsg.TYP_ACID;
			case GOLD_RING_RUBY:
				return CMMsg.TYP_MIND;
			case BRONZE_RING:
				return CMMsg.TYP_PARALYZE;
			case GOLD_RING_OPAL:
				return CMMsg.TYP_CAST_SPELL;
			case GOLD_RING_TOPAZ:
				return -99;
			case GOLD_RING_SAPPHIRE:
				return CMMsg.TYP_JUSTICE;
			case MITHRIL_RING:
				return CMMsg.TYP_WEAPONATTACK;
			case GOLD_RING_PEARL:
				return CMMsg.TYP_WATER;
			case GOLD_RING_EMERALD:
				return -99;
			default:
				return -99;

		}
	}

	protected boolean rollChance()
	{
		switch(this.phyStats().level())
		{
			case GOLD_RING_OPAL:
				if(Math.random()>.15)
					return false;
				return true;
			case GOLD_RING_SAPPHIRE:
				if(Math.random()>.15)
					return false;
				return true;
			case MITHRIL_RING:
				if(Math.random()>.05)
					return false;
				return true;
			default:
				return true;

		}
	}

	private void setIdentity()
	{
		switch(this.phyStats().level())
		{
			case SILVER_RING:
				secretIdentity="The ring of Seven Winters. (Protection from Cold)";
				baseGoldValue+=1000;
				material=RawMaterial.RESOURCE_SILVER;
				break;
			case COPPER_RING:
				secretIdentity="The ring of Storms. (Protection from Electricity)";
				baseGoldValue+=1000;
				material=RawMaterial.RESOURCE_COPPER;
				break;
			case PLATINUM_RING:
				secretIdentity="The ring of Sweet Air. (Protection from Gas)";
				baseGoldValue+=1000;
				material=RawMaterial.RESOURCE_PLATINUM;
				break;
			case GOLD_RING_DIAMOND:
				secretIdentity="The ring of the Eternal Blaze. (Protection from Fire)";
				baseGoldValue+=1000;
				material=RawMaterial.RESOURCE_GOLD;
				break;
			case BRONZE_RING:
				secretIdentity="The ring of the Bronze Shield. (Protection from Paralysis)";
				baseGoldValue+=1000;
				material=RawMaterial.RESOURCE_BRONZE;
				break;
			case GOLD_RING:
				secretIdentity="The ring of Sweet Water. (Protection from Acid)";
				baseGoldValue+=1000;
				material=RawMaterial.RESOURCE_GOLD;
				break;
			case GOLD_RING_RUBY:
				secretIdentity="The ring of Focus. (Protection from Mind Attacks)";
				baseGoldValue+=5000;
				material=RawMaterial.RESOURCE_GEM;
				break;
			case GOLD_RING_OPAL:
				secretIdentity="Mages Bane. (15% Resistance to Magic)";
				baseGoldValue+=1000;
				material=RawMaterial.RESOURCE_GEM;
				break;
			case GOLD_RING_TOPAZ:
				basePhyStats().setAbility(60);
				secretIdentity="Zimmers Guard. (Ring of Protection +60)";
				baseGoldValue+=6000;
				material=RawMaterial.RESOURCE_GEM;
				break;
			case GOLD_RING_SAPPHIRE:
				secretIdentity="The ring of Justice. (15% Resistance to Criminal Behavior)";
				baseGoldValue+=1000;
				material=RawMaterial.RESOURCE_GEM;
				break;
			case MITHRIL_RING:
				secretIdentity="The ring of Fortitude. (5% Resistance to physical attacks)";
				baseGoldValue+=2000;
				material=RawMaterial.RESOURCE_MITHRIL;
				break;
			case GOLD_RING_PEARL:
				secretIdentity="The ring of the Wave. (Protection from Water Attacks)";
				baseGoldValue+=1000;
				material=RawMaterial.RESOURCE_PEARL;
				break;
			case GOLD_RING_EMERALD:
				if(basePhyStats().ability()==0)
					basePhyStats().setAbility(50);
				secretIdentity="Fox Guard. (Ring of Protection +50)";
				baseGoldValue+=5000;
				material=RawMaterial.RESOURCE_GEM;
				break;
			default:
				final double pct=Math.random();
				if(basePhyStats().ability()==0)
					basePhyStats().setAbility((int)Math.round(pct*49));
				baseGoldValue+=basePhyStats().ability()*100;
				secretIdentity="A ring of protection + "+basePhyStats().ability()+".";
				material=RawMaterial.RESOURCE_STEEL;
				break;
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.target()==null)||(!(msg.target() instanceof MOB)))
			return ;

		final MOB mob=(MOB)msg.target();
		if(mob!=this.owner())
			return;

		if((msg.targetMinor()==correctTargetMinor())
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(!this.amWearingAt(Wearable.IN_INVENTORY))
		&&(mob.isMine(this))
		&&(rollChance()))
			CMLib.combat().resistanceMsgs(msg.source(),mob,msg);
	}
}
