package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.ItemKeyPair;
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
   Copyright 2020-2020 Bo Zimmerman

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
public class Thief_Digsite extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Digsite";
	}

	private final static String localizedName = CMLib.lang().L("Digsite");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Digsite)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_PCT + 50;
	}

	private static final String[] triggerStrings =I(new String[] {"DIGSITE"});
	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_EDUCATIONLORE;
	}

	@Override
	public boolean disregardsArmorCheck(final MOB mob)
	{
		return true;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected static final Set<Room> lastRooms = new LimitedTreeSet<Room>(TimeManager.MILI_DAY,1000,false);
	protected static final Set<String> appropriateDiggingSkills = new XHashSet<String>(new String[] {
		"Mining",
		"Digging",
		"Foraging"
	});

	protected final List<ItemCraftor> craftingSkills = new Vector<ItemCraftor>();

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.tool() instanceof Ability)
		&&(appropriateDiggingSkills.contains(msg.tool().ID()))
		&&(affected instanceof Room)
		&&(msg.target() instanceof Item))
		{
			final Room R=(Room)affected;
			final Area A=(R==null)?null:R.getArea();
			if(A==null)
				return true;
			if((msg.value()==0)
			||(msg.target() instanceof RawMaterial))
			{
				final int minLevel = A.getAreaIStats()[Area.Stats.MIN_LEVEL.ordinal()];
				final int maxLevel = A.getAreaIStats()[Area.Stats.MAX_LEVEL.ordinal()];
				if(craftingSkills.size()==0)
				{
					for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
					{
						final Ability A1=e.nextElement();
						if(A1 instanceof ItemCraftor)
						{
							final ItemCraftor I=(ItemCraftor)A1;
							final int[] range = I.getCraftableLevelRange();
							if((maxLevel >= range[0])
							&&(minLevel <= range[1]))
								craftingSkills.add((ItemCraftor)I.copyOf());
						}
					}
					if(craftingSkills.size()==0)
					{
						for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
						{
							final Ability A1=e.nextElement();
							if(A1 instanceof ItemCraftor)
							{
								final ItemCraftor A2=(ItemCraftor)A1.copyOf();
								if((A2.getCraftorType()==ItemCraftor.CraftorType.General)
								||(A2.getCraftorType()==ItemCraftor.CraftorType.Magic)
								||(A2.getCraftorType()==ItemCraftor.CraftorType.Weapons)
								||(A2.getCraftorType()==ItemCraftor.CraftorType.Armor))
									craftingSkills.add(A2);
							}
						}
						if(craftingSkills.size()==0)
							return true;
					}
				}

				final ItemCraftor craftor = craftingSkills.get(CMLib.dice().roll(1, craftingSkills.size(), -1));
				final ItemKeyPair pair = craftor.craftAnyItemNearLevel(minLevel, maxLevel);
				if(pair != null)
				{
					Item item=pair.item;
					if(CMLib.dice().rollPercentage()==1)
						item=CMLib.itemBuilder().enchant(item,100);
					else
					if(CMLib.dice().rollPercentage()<50)
						item=CMLib.utensils().ruinItem(item);
					else
					{
						item.setBaseValue(1);
						if(item.subjectToWearAndTear())
							item.setUsesRemaining(1);
					}
					msg.setTarget(item);
				}
			}
			else
			if((msg.value()>1)
			&&(!(msg.target() instanceof RawMaterial)))
				msg.setValue(1);
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		final Physical P=affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if(P instanceof Room)
			{
				final Room R=(Room)P;
				R.show(CMLib.map().deity(), null, CMMsg.MSG_OK_VISUAL, L("This digsite has been sufficiently investigated."));
			}
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		Room target=mob.location();
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof Room))
			target=(Room)givenTarget;
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(L("This place is already set up as a digsite."));
			return false;
		}

		final int[] resources = new int[] {
			RawMaterial.MATERIAL_PRECIOUS,
			RawMaterial.MATERIAL_GLASS,
			RawMaterial.RESOURCE_SAND,
			RawMaterial.MATERIAL_ROCK,
			RawMaterial.MATERIAL_METAL,
			RawMaterial.MATERIAL_MITHRIL,
			RawMaterial.RESOURCE_STONE,
			RawMaterial.MATERIAL_VEGETATION,
			RawMaterial.RESOURCE_HEMP,
			RawMaterial.RESOURCE_SILK,
			RawMaterial.RESOURCE_SALT,
			RawMaterial.RESOURCE_COTTON
		};
		boolean foundOne=false;
		for(final int resource : resources)
		{
			final Integer I=Integer.valueOf(resource);
			final boolean isMaterial=(resource&RawMaterial.RESOURCE_MASK)==0;
			final int roomResourceType=target.myResource();
			if(((isMaterial&&(resource==(roomResourceType&RawMaterial.MATERIAL_MASK))))
			||(I.intValue()==roomResourceType))
				foundOne=true;
			if(!foundOne)
			{
				final List<Integer> resourcesV=target.resourceChoices();
				if(resourcesV!=null)
				{
					for(int i=0;i<resourcesV.size();i++)
					{
						if(isMaterial&&(resource==(resourcesV.get(i).intValue()&RawMaterial.MATERIAL_MASK)))
							foundOne=true;
						else
						if(resourcesV.get(i).equals(I))
							foundOne=true;
					}
				}
			}
		}
		if(!foundOne)
		{
			mob.tell(L("This doesn't appear to be a viable spot for a digsite."));
			return false;
		}

		if(lastRooms.contains(target))
		{
			mob.tell(L("This place has already been a recent digsite."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MSG_OK_ACTION:(CMMsg.MSG_DELICATE_HANDS_ACT|CMMsg.MASK_EYES),
					L("<S-NAME> establish(es) a digsite here."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final int duration=40 + (adjustedLevel(mob,asLevel)/3) + (super.getXLEVELLevel(mob)*3) + (super.getXTIMELevel(mob)*10);
				if(beneficialAffect(mob,target,asLevel,duration)!=null)
					lastRooms.add(target);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to establish a digsite, but fail(s)."));
		return success;
	}
}
