package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2013-2025 Bo Zimmerman

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
public class MasterFoodPrep extends FoodPrep
{
	private String	cookingID	= "";

	@Override
	public String ID()
	{
		return "MasterFoodPrep" + cookingID;
	}

	@Override
	public String name()
	{
		return L("Master Food Prep@x1", cookingID);
	}

	private static final String[]	triggerStrings	= I(new String[] { "MFOODPREPPING", "MFPREP", "MASTERFOODPREPPING", "MASTERFPREP" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected List<String>	noUninvokes	= new ArrayList<String>(0);

	@Override
	protected List<String> getUninvokeException()
	{
		return noUninvokes;
	}

	@Override
	protected int getDuration(final MOB mob, final int level)
	{
		return getDuration(60, mob, 1, 8);
	}

	@Override
	protected int baseYield()
	{
		return 2;
	}

	@Override

	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((!auto)
		&& (commands.size()>1)
		&& ("chop".startsWith(commands.get(0).toLowerCase())))
		{
			commands.remove(0);
			final Item target=getTarget(mob,mob.location(),givenTarget,null,commands,Wearable.FILTER_UNWORNONLY);
			if(target==null)
			{
				commonTelL(mob,"The syntax is @x1 CHOP [FOOD].",triggerStrings()[0]);
				return false;
			}
			if((!(target instanceof Food))
			||(target instanceof MiscMagic)
			||(target instanceof RawMaterial)
			||(target.numberOfItems()>1)
			||(CMLib.flags().isABonusItems(target)))
			{
				commonTelL(mob,"You can't chop @x1.",target.name(mob));
				return false;
			}
			Food F = (Food)target;
			if(F.basePhyStats().weight()<=1)
			{
				commonTelL(mob,"You can't chop @x1, as it's already as small as you can chop it.",target.name(mob));
				return false;
			}
			final CMMsg msg = CMClass.getMsg(mob, target, this,
					CMMsg.MSG_HANDS,L("<S-NAME> chop(s) up <T-NAME>."));
			if(mob.location().okMessage(mob, msg))
			{
				mob.location().send(mob, msg);
				final Food existsF = F;
				final String name = F.Name();
				final List<Food> bits = new ArrayList<Food>();
				final List<Food> nonbits = new ArrayList<Food>();
				nonbits.add(F);
				while(nonbits.size()>0)
				{
					F = nonbits.remove(0);
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
					if(F.basePhyStats().weight()>1)
						nonbits.add(F);
					else
						bits.add(F);
					if(F2.basePhyStats().weight()>1)
						nonbits.add(F2);
					else
						bits.add(F2);
				}
				for(final Food f : bits)
				{
					f.setName(L("a bit of ")+f.Name());
					f.setDisplayText(CMStrings.replaceAll(f.displayText(), name, f.Name()));
					if(F != existsF)
						existsF.owner().addItem(f);
				}
			}
			return false;
		}
		try
		{
			cookingID="";
			int num=1;
			while(mob.fetchEffect("MasterFoodPrep"+cookingID)!=null)
				cookingID=Integer.toString(++num);
			num--;
			if(num>1)
				cookingID=Integer.toString(num);
			else
				cookingID="";
			if(super.checkStop(mob, commands))
				return true;

			cookingID="";
			num=1;
			while(mob.fetchEffect("MasterFoodPrep"+cookingID)!=null)
				cookingID=Integer.toString(++num);
			final List<String> noUninvokes=new Vector<String>(1);
			for(int i=0;i<mob.numEffects();i++)
			{
				final Ability A=mob.fetchEffect(i);
				if(((A instanceof MasterFoodPrep)||A.ID().equals("FoodPrep"))
				&&(noUninvokes.size()<5))
					noUninvokes.add(A.ID());
			}
			this.noUninvokes=noUninvokes;
			return super.invoke(mob, commands, givenTarget, auto, asLevel);
		}
		finally
		{
			cookingID="";
		}
	}
}
