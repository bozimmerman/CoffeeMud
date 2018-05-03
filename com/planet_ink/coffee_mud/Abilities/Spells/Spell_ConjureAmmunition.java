package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2018-2018 Bo Zimmerman

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

public class Spell_ConjureAmmunition extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_ConjureAmmunition";
	}

	private final static String localizedName = CMLib.lang().L("Conjure Ammunition");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS|CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=super.getAnyTarget(mob, commands, givenTarget, Wearable.FILTER_UNWORNONLY, false);
		if(target==null)
			return false;
		
		Ammunition ammoI = null;
		Item adjustedTarget = (target instanceof Item)?(Item)target:null;
		if(target instanceof MOB)
		{
			List<Item> choices=new ArrayList<Item>(1);
			Item wieldI = ((MOB)target).fetchWieldedItem();
			if((wieldI instanceof AmmunitionWeapon)
			&&(((AmmunitionWeapon)wieldI).ammunitionType().length()>0)
			&&(((AmmunitionWeapon)wieldI).ammunitionCapacity()>0))
				choices.add(wieldI);
			else
			for(Enumeration<Item> e=((MOB)target).items();e.hasMoreElements();)
			{
				Item I = e.nextElement();
				if((I instanceof AmmunitionWeapon)
				&&(((AmmunitionWeapon)I).ammunitionType().length()>0)
				&&(((AmmunitionWeapon)I).ammunitionCapacity()>0))
					choices.add(I);
			}
			if(choices.size()>0)
				adjustedTarget=choices.get(CMLib.dice().roll(1, choices.size(), -1));
		}
		if(adjustedTarget instanceof AmmunitionWeapon)
		{
			String ammoType = ((AmmunitionWeapon)adjustedTarget).ammunitionType();
			if((ammoType.length()>0)&&(((AmmunitionWeapon)adjustedTarget).ammunitionCapacity()>0))
			{
				int ammoAmount = 1;
				if(!(adjustedTarget instanceof Rideable))
				{
					int level=adjustedLevel(mob,asLevel);
					if(level < 6)
						ammoAmount = 5;
					else
					if(level < 11)
						ammoAmount = 10;
					else
					if(level < 21)
						ammoAmount = 25;
					else
					if(level < 31)
						ammoAmount = 50;
					else
						ammoAmount = 100;
				}
				ammoI=CMLib.coffeeMaker().makeAmmunition(ammoType,ammoAmount);
				ammoI.setBaseValue(0);
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success && (ammoI != null))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L("^S<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, incanting softly.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,ammoI,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> appears!"));
				mob.location().addItem(ammoI, Expire.Player_Drop);
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> move(s) <S-HIS-HER> fingers around <T-NAMESELF>, incanting softly, and looking very frustrated."));
		// return whether it worked
		return success;
	}
}
