package com.planet_ink.coffee_mud.Abilities.Properties;
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
   Copyright 2004-2020 Bo Zimmerman

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
public class Prop_WearEnabler extends Prop_HaveEnabler
{
	@Override
	public String ID()
	{
		return "Prop_WearEnabler";
	}

	@Override
	public String name()
	{
		return "Granting skills when worn/wielded";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	public boolean	checked		= false;
	public boolean	disabled	= false;
	public boolean	layered		= false;

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_WEAR_WIELD;
	}

	public void check(final MOB mob, final Armor A)
	{
		if (!layered)
		{
			checked = true;
			disabled = false;
		}
		final boolean oldDisabled=disabled;
		if(!A.amBeingWornProperly())
		{
			checked=false;
			return;
		}
		if(checked)
			return;
		Item I=null;
		disabled=false;
		for(int i=0;i<mob.numItems();i++)
		{
			I=mob.getItem(i);
			if((I instanceof Armor)
			&&(I.amBeingWornProperly())
			&&((I.rawWornCode()&A.rawWornCode())>0)
			&&(I!=A)
			&&((I.basePhyStats().armor()>0)
				||(I.numEffects()>0)
				||(I.material()!=RawMaterial.RESOURCE_PAPER)))
			{
				disabled=A.getClothingLayer()<=((Armor)I).getClothingLayer();
				if(disabled)
				{
					break;
				}
			}
		}
		if((!oldDisabled)&&(disabled))
			this.removeMyAffectsFromLastMOB();
		checked=true;
	}

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
		layered=CMParms.parseSemicolons(newText.toUpperCase(),true).indexOf("LAYERED")>=0;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if((affected instanceof Armor)&&(msg.source()==((Item)affected).owner()))
		{
			if((msg.targetMinor()==CMMsg.TYP_REMOVE)
			||(msg.sourceMinor()==CMMsg.TYP_WEAR)
			||(msg.sourceMinor()==CMMsg.TYP_WIELD)
			||(msg.sourceMinor()==CMMsg.TYP_HOLD)
			||(msg.sourceMinor()==CMMsg.TYP_DROP))
				checked=false;
			else
			{
				check(msg.source(),(Armor)affected);
				super.executeMsg(host,msg);
			}
		}
		else
			super.executeMsg(host,msg);
	}

	@Override
	public boolean addMeIfNeccessary(final Environmental source, final Environmental target, final short maxTicks)
	{
		if(disabled&&checked)
			return false;
		return super.addMeIfNeccessary(source,target,maxTicks);
	}

	@Override
	public String accountForYourself()
	{
		return spellAccountingsWithMask("Grants ", " to the wearer/wielder.");
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		if((this.affected != null)&&(P==null))
			removeMyAffectsFromLastMob();
		super.setAffectedOne(P);
	}

	@Override
	public void affectPhyStats(final Physical host, final PhyStats affectableStats)
	{
		if(processing)
			return;
		try
		{
			processing=true;
			if(host instanceof Item)
				myItem=(Item)host;
			else
			if(affected instanceof Item)
				myItem=(Item)affected;

			final Item I=myItem;
			if(I!=null)
			{
				final boolean worn=(!I.amWearingAt(Wearable.IN_INVENTORY))
				&&((!I.amWearingAt(Wearable.WORN_FLOATING_NEARBY))||(I.fitsOn(Wearable.WORN_FLOATING_NEARBY)));

				if((lastMOB instanceof MOB)
				&&(((MOB)lastMOB).location()!=null)
				&&((I.owner()!=lastMOB)||(!worn)))
					removeMyAffectsFromLastMob();

				if((lastMOB==null)
				&&(worn)
				&&(I.owner()!=null)
				&&(I.owner() instanceof MOB)
				&&(((MOB)I.owner()).location()!=null))
				{
					if(I instanceof Armor)
						check((MOB)I.owner(),((Armor)I));
					addMeIfNeccessary(I.owner(),I.owner(),maxTicks);
				}
			}
		}
		finally
		{
			processing=false;
		}
	}

	@Override
	public String getStat(final String code)
	{
		if(code == null)
			return "";
		if(code.equalsIgnoreCase("STAT-LEVEL"))
		{
			int level = 0;
			for(final Pair<Ability,Integer> p : this.getMySpellsV())
			{
				final Ability A=p.first;
				if(A!=null)
				{
					final int mul=1;
					level += (mul*CMLib.ableMapper().lowestQualifyingLevel(A.ID()));
				}
			}
			return ""+level;
		}
		else
		if(code.toUpperCase().startsWith("STAT-"))
			return "";
		return super.getStat(code);
	}

}
