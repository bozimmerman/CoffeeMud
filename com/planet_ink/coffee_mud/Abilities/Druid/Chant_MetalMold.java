package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2003-2025 Bo Zimmerman

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
public class Chant_MetalMold extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_MetalMold";
	}

	private final static String localizedName = CMLib.lang().L("Metal Mold");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS|CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	private Item findMobTargetItem(final MOB mobTarget)
	{
		final List<Item> goodPossibilities=new ArrayList<Item>();
		final List<Item> possibilities=new ArrayList<Item>();
		for(int i=0;i<mobTarget.numItems();i++)
		{
			final Item item=mobTarget.getItem(i);
			if((item!=null) && (item.subjectToWearAndTear()) && (CMLib.flags().isMetal(item)))
			{
				if(item.amWearingAt(Wearable.IN_INVENTORY))
					possibilities.add(item);
				else
					goodPossibilities.add(item);
			}
		}
		if(goodPossibilities.size()>0)
			return goodPossibilities.get(CMLib.dice().roll(1,goodPossibilities.size(),-1));
		else
		if(possibilities.size()>0)
			return possibilities.get(CMLib.dice().roll(1,possibilities.size(),-1));
		return null;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if((target instanceof MOB)&&(target!=mob))
			{
				if(findMobTargetItem((MOB)target)==null)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB mobTarget=getTarget(mob,commands,givenTarget,true,false);
		Item target=null;
		if(mobTarget!=null)
			target=findMobTargetItem(mobTarget);

		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success && (target!=null) && CMLib.flags().isMetal(target) && target.subjectToWearAndTear())
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> grow(s) moldy!"):L("^S<S-NAME> chant(s), causing <T-NAMESELF> to get eaten by mold.^?"));
			final CMMsg msg2=CMClass.getMsg(mob,mobTarget,this,verbalCastCode(mob,mobTarget,auto),null);
			if((mob.location().okMessage(mob,msg))&&((mobTarget==null)||(mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if(mobTarget!=null)
					mob.location().send(mob,msg2);
				if(msg.value()<=0)
				{
					int damage=2;
					final int num=(mob.phyStats().level()+super.getX1Level(mob)+(2*getXLEVELLevel(mob)))/2;
					for(int i=0;i<num;i++)
						damage+=CMLib.dice().roll(1,2,2);
					if(CMLib.flags().isABonusItems(target))
						damage=(int)Math.round(CMath.div(damage,2.0));
					if(target.phyStats().ability()>0)
						damage=(int)Math.round(CMath.div(damage,1+target.phyStats().ability()));
					CMLib.combat().postItemDamage(mob, target, null, damage, CMMsg.TYP_ACID, null);
				}
			}
		}
		else
		if(mobTarget!=null)
			return maliciousFizzle(mob,mobTarget,L("<S-NAME> chant(s) at <T-NAME> for mold, but nothing happens."));
		else
		if(target!=null)
			return maliciousFizzle(mob,target,L("<S-NAME> chant(s) at <T-NAME> for mold, but nothing happens."));
		else
			return maliciousFizzle(mob,null,L("<S-NAME> chant(s) for mold, but nothing happens."));

		// return whether it worked
		return success;
	}
}
