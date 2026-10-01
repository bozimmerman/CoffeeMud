package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ShopKeeper.ViewType;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2026-2026 Bo Zimmerman

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
public class SnakeOilSelling extends BlackMarketeering
{
	@Override
	public String ID()
	{
		return "SnakeOilSelling";
	}

	private final static String localizedName = CMLib.lang().L("Snakeoil Selling");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings = I(new String[] {"SNAKEOIL"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return false;
	}

	private static final String[] fakeMessages = new String[]
	{
		"You feel a faint warmth spread through you.",
		"You feel slightly different.",
		"Something seems to shift inside you.",
		"A brief shiver runs down your spine.",
		"You feel a momentary lightness.",
		"You feel more aware of your surroundings.",
		"A vague sense of well-being settles over you.",
		"You feel a little steadier.",
		"You feel something stirring, then it passes.",
		"For a moment, everything seems very clear.",
		"You feel a curious calm.",
		"Your senses sharpen, then fade.",
		"A warm glow flickers at the edge of your vision.",
		"You feel a brief, pleasant numbness.",
		"Something is happening, but you can't say what.",
		"You feel quietly altered.",
		"Your thoughts drift for a moment, then settle.",
		"A whisper of something that isn't there passes through you.",
		"You feel an unexpected sense of ease.",
		"You feel a flicker of power you can't name.",
		"Your body tingles, then settles.",
		"You feel the world tilt, then steady.",
		"You feel a change you cannot describe.",
		"Something quick and bright passes through you."
	};

	@Override
	protected boolean canSell(final MOB mob, final Environmental E)
	{
		if(E instanceof Potion)
			return true;
		return false;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		makeActive(mob);
		if(commands.size()==0)
		{
			commonTelL(mob,"Snakeoil what? Enter \"snakeoil open\" to start selling or \"snakeoil close\" to stop.");
			return false;
		}
		final int MAX_POTIONS = 1+proficiency();
		final String cmd=commands.get(0).toUpperCase();

		if(cmd.equals("OPEN"))
		{
			final Room R=mob.location();
			if(R==null)
				return false;

			final Item blood=mob.findItem(null, L("snake blood"));
			if(blood==null)
			{
				commonFaiL(mob,commands,L("You need some snake blood to brew these vials."));
				return false;
			}

			final Ability alchemy=CMClass.getAbility("Alchemy");
			if((alchemy==null)||!(alchemy instanceof ItemCraftor))
				return false;
			final Ability herbalism=CMClass.getAbility("Herbalism");
			if((herbalism==null)||!(herbalism instanceof ItemCraftor))
				return false;
			final int diversifyRanks=getX1Level(mob);
			final int adjLevel=adjustedLevel(mob,asLevel);
			int numTypes=1+(adjLevel/10);
			if(numTypes<1)
				numTypes=1;
			if(numTypes>MAX_POTIONS)
				numTypes=MAX_POTIONS;
	
			final int[] potionLevels=new int[numTypes];
			for(int i=0;i<numTypes;i++)
				potionLevels[i]=1+(3*i);
	
			final ItemCraftor craftor=(ItemCraftor)alchemy;
			final ItemCraftor herbCraftor=(ItemCraftor)herbalism;
			int created=0;
			int nulls=0;
			while((created<MAX_POTIONS)&&(nulls<numTypes))
			{
				final int bandMax=potionLevels[created % numTypes];
				ItemCraftor chosenCraftor=craftor;
				if(diversifyRanks>=6 && (created % 3==0))
					chosenCraftor=herbCraftor;
				final ItemCraftor.CraftedItem pair=chosenCraftor.craftAnyItemNearLevel(1,bandMax);
				if((pair==null)||(pair.item==null))
				{
					nulls++;
					continue;
				}
				final Item real=pair.item;
				if(!(real instanceof Potion))
				{
					real.destroy();
					nulls++;
					continue;
				}
				final String realName=real.Name();
				final String realDisplay=real.displayText();
				final String realDesc=real.description();
				final String spellList=((Potion)real).getSpellList();
				// Populate value before destroying
				if(real instanceof Potion)
					((Potion)real).getSpells();
				final int price=Math.max(1,(int)(real.value()*0.10));
				final Ability spellA=CMClass.getAbility(spellList);
				int abilCode=0;
				if(spellA!=null)
					abilCode=spellA.classificationCode()&Ability.ALL_ACODES;
				boolean allow=false;
				if(abilCode==Ability.ACODE_SPELL)
					allow=true;
				else 
				if(diversifyRanks>=3 && abilCode==Ability.ACODE_PRAYER)
					allow=true;
				else 
				if(diversifyRanks>=6 && chosenCraftor==herbCraftor)
					allow=true;
				if(spellA==null || !allow || spellA.abstractQuality()==Ability.QUALITY_MALICIOUS)
				{
					real.destroy();
					nulls++;
					continue;
				}
				real.destroy();
				nulls=0;
				final Potion potion=(Potion)CMClass.getMiscMagic("GenPotion");
				potion.setName(realName);
				potion.setDisplayText(realDisplay);
				potion.setDescription(realDesc);
				potion.setBaseValue(price);
				final ExtendableAbility fake = (ExtendableAbility)CMClass.getAbility("ExtAbility");
				fake.setAbilityID(spellA.ID()+"_Fake");
				fake.setName(spellA.Name());
				fake.setDisplayText(spellA.displayText());
				fake.setSavable(false);
				fake.addInvoke(new CMCallback<Quint<MOB, List<String>, Physical, Boolean, Integer>>()
				{
					final Ability spell = spellA;
					@Override
					public void callback(final Quint<MOB, List<String>, Physical, Boolean, Integer> args)
					{
						final Physical t = args.third;
						if(!(t instanceof MOB))
							return;
						final MOB drinker = (MOB)t;
						drinker.tell(L(fakeMessages[CMLib.dice().rollInRange(0, fakeMessages.length-1)]));
						if(spell.canAffect(Ability.CAN_MOBS) && (spell.displayText().trim().length() > 0))
							fake.startTickDown(drinker, drinker, CMLib.dice().rollInRange(5,10));
					}
				});
				potion.setSpells(Collections.singletonList((Ability)fake));
				final CoffeeShop shop=getShop();
				if(price>0)
				{
					potion.setBaseValue(price);
					potion.phyStats().setAbility(0);
					potion.recoverPhyStats();
				}
				shop.addStoreInventory(potion,1,price);
				created++;
			}
	
			if(created==0)
			{
				commonFaiL(mob,commands,L("You fail to conjure any useful snake oil."));
				return false;
			}
	
			mob.delItem(blood);
			mob.addEffect(this);
			makeActive(mob);
			mob.tell(L("@x1 fake remedies are now up for sale.", ""+created));
			return true;
		}

		if(cmd.equals("CLOSE"))
		{
			final CoffeeShop shop=getShop();
			if(shop!=null)
			{
				final List<CoffeeShop.ShelfProduct> doomed=new ArrayList<CoffeeShop.ShelfProduct>();
				for(final Iterator<CoffeeShop.ShelfProduct> s=shop.getStoreShelves();s.hasNext();)
				{
					final CoffeeShop.ShelfProduct SP=s.next();
					doomed.add(SP);
				}
				for(final CoffeeShop.ShelfProduct SP : doomed)
				{
					shop.delAllStoreInventory(SP.product());
					if(SP.product()!=null)
						SP.product().destroy();
				}
				mob.tell(L("^HYou stop selling your snake oil."));
			}
			final Ability A=mob.fetchEffect(ID());
			if(A!=null)
				mob.delEffect(A);
			return true;
		}

		commonTelL(mob,"Snakeoil what? Enter \"snakeoil open\" to start selling or \"snakeoil close\" to stop.");
		return false;
	}
}
