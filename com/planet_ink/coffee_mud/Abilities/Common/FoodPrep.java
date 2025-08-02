package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftedItem;
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
   Copyright 2005-2025 Bo Zimmerman

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
public class FoodPrep extends Cooking
{
	@Override
	public String ID()
	{
		return "FoodPrep";
	}

	private final static String	localizedName	= CMLib.lang().L("Food Prep");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "FOODPREPPING", "FPREP" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String cookWordShort()
	{
		return "make";
	}

	@Override
	public String cookWord()
	{
		return "making";
	}

	@Override
	public boolean honorHerbs()
	{
		return false;
	}

	@Override
	public boolean requireFire()
	{
		return false;
	}

	@Override
	public String getRecipeFilename()
	{
		return "foodprep.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(getRecipeFilename());
	}

	public FoodPrep()
	{
		super();

		defaultFoodSound = "chopchop.wav";
	}

	@Override
	public void stirThePot(final MOB mob)
	{
		if(buildingI!=null)
		{
			if((tickUp % 5)==1)
			{
				final Room R=mob.location();
				if(R==activityRoom)
				{
					R.show(mob,cookingPot,buildingI,CMMsg.MASK_ALWAYS|getActivityMessageType(),
							L("<S-NAME> do(es) prep work for <O-NAME>."));
				}
			}
		}
	}

	protected final static String[] sliceDownPrefixes = new String[]
	{
		CMLib.lang().L("a slice of "),
		CMLib.lang().L("a small slice of "),
		CMLib.lang().L("a smaller slice of "),
		CMLib.lang().L("a tiny slice of ")
	};

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((!auto)
		&& (commands.size()>1)
		&& ("slice".startsWith(commands.get(0).toLowerCase())))
		{
			commands.remove(0);
			final Item target=getTarget(mob,mob.location(),givenTarget,null,commands,Wearable.FILTER_UNWORNONLY);
			if(target==null)
			{
				commonTelL(mob,"The syntax is @x1 SLICE [FOOD].",triggerStrings()[0]);
				return false;
			}
			if((!(target instanceof Food))
			||(target instanceof MiscMagic)
			||(target instanceof RawMaterial)
			||(target.numberOfItems()>1)
			||(CMLib.flags().isABonusItems(target)))
			{
				commonTelL(mob,"You can't slice @x1.",target.name(mob));
				return false;
			}
			final Food F = (Food)target;
			if(F.basePhyStats().weight()<=1)
			{
				commonTelL(mob,"You can't slice @x1, as it's already as small as you can slice it.",target.name(mob));
				return false;
			}
			final CMMsg msg = CMClass.getMsg(mob, target, this,
					CMMsg.MSG_HANDS,L("<S-NAME> slice(s) <T-NAME>."));
			if(mob.location().okMessage(mob, msg))
			{
				mob.location().send(mob, msg);
				if(!F.Name().startsWith(L(sliceDownPrefixes[sliceDownPrefixes.length-1])))
				{
					String chosen = sliceDownPrefixes[0];
					for(int i=0;i<sliceDownPrefixes.length-1;i++)
						if(F.Name().startsWith(L(sliceDownPrefixes[i])))
							chosen=sliceDownPrefixes[i+1];
					final String name = F.Name();
					F.setName(chosen+F.Name());
					F.setDisplayText(CMStrings.replaceAll(F.displayText(), name, F.Name()));
				}
				final Food F2 = (Food)F.copyOf();
				final int halfWeight = (int)Math.round(Math.floor(CMath.div(F.basePhyStats().weight(),2)));
				F.basePhyStats().setWeight(halfWeight);
				F2.basePhyStats().setWeight(halfWeight);
				if((F.phyStats().weight() % 2)==1)
					F.basePhyStats().setWeight(halfWeight+1);
				F.recoverPhyStats();
				F2.recoverPhyStats();
				final int oldNourishment = F.nourishment();
				final int halfFood =  (int)Math.round(Math.floor(CMath.div(oldNourishment,2)));
				F.setNourishment(halfFood);
				F2.setNourishment(halfFood);
				if((oldNourishment % 2)==1)
					F.setNourishment(halfFood+1);
				if(F.bite() > F.nourishment())
					F.setBite(F.nourishment());
				if(F2.bite() > F2.nourishment())
					F2.setBite(F2.nourishment());
				F.owner().addItem(F2);
			}
			return false;
		}
		return autoGenInvoke(mob,commands,givenTarget,auto,asLevel,0,false,new ArrayList<CraftedItem>(0));
	}

}
