package com.planet_ink.coffee_mud.Abilities.Specializations;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Specialization_Armor extends StdAbility
{
	@Override
	public String ID()
	{
		return "Specialization_Armor";
	}

	private final static String	localizedName	= CMLib.lang().L("Armor Specialization");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	protected final static long	WORN_ARMOR	= Item.WORN_ARMS | Item.WORN_FEET | Item.WORN_HANDS | Item.WORN_HEAD 
											| Item.WORN_LEFT_WRIST | Item.WORN_LEGS | Item.WORN_RIGHT_WRIST | Item.WORN_TORSO 
											| Item.WORN_WAIST;

	protected double bonus=-1;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ARMORUSE;
	}

	private void recalculateBonus(MOB mob)
	{
		bonus=0;
		if(mob!=null)
		{
			for(int i=0;i<mob.numItems();i++)
			{
				final Item I=mob.getItem(i);
				if((I!=null)
				&&(I.basePhyStats().armor()>0)
				&&(!I.amWearingAt(Wearable.IN_INVENTORY))
				&&(!I.amWearingAt(Wearable.WORN_HELD))
				&&(!I.amWearingAt(Wearable.WORN_FLOATING_NEARBY))
				&&(CMath.banyset(I.rawProperLocationBitmap(), WORN_ARMOR)))
				{
					bonus+=1.0;
				}
			}
			if((bonus>0)&&(CMLib.dice().rollPercentage()<5))
				super.helpProficiency(mob, 0);
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.source()==affected)
		{
			if((msg.target() instanceof Armor)
			&&((msg.targetMinor()==CMMsg.TYP_WEAR)||(msg.targetMinor()==CMMsg.TYP_REMOVE)||(msg.targetMinor()==CMMsg.TYP_DROP)))
				bonus=-1;
			else
			if(msg.sourceMinor()==CMMsg.TYP_LIFE)
				bonus=-1;
		}
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			if((bonus<0)&&(((MOB)affected).numItems()>0))
				recalculateBonus((MOB)affected);
			if(bonus>0)
			{
				affectableStats.setArmor(affectableStats.armor()
						-(int)Math.round(bonus*CMath.div(proficiency(),100.0))
						-(getXLEVELLevel((MOB)affected)/2));
			}
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.source()==affected)
		&&(msg.target() instanceof Armor)
		&&(msg.targetMinor()==CMMsg.TYP_WEAR)
		&&(CMath.banyset(((Armor)msg.target()).rawProperLocationBitmap(), WORN_ARMOR))
		&&(((Armor)msg.target()).phyStats().level()>msg.source().phyStats().level()))
		{
			((Armor)msg.target()).phyStats().setLevel(((Armor)msg.target()).phyStats().level()-((1+getXLEVELLevel(msg.source()))/2));
		}
		return true;
	}

}
