package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2004-2018 Bo Zimmerman

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

public class Prayer_UnholyArmament extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_UnholyArmament";
	}

	private final static String localizedName = CMLib.lang().L("Unholy Armament");

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
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_HOLYPROTECTION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	public static final long[] checkOrder={
		Wearable.WORN_WIELD,
		Wearable.WORN_TORSO,
		Wearable.WORN_LEGS,
		Wearable.WORN_WAIST,
		Wearable.WORN_HEAD,
		Wearable.WORN_ARMS,
		Wearable.WORN_FEET,
		Wearable.WORN_HANDS,
		Wearable.WORN_LEFT_WRIST,
		Wearable.WORN_RIGHT_WRIST,
		Wearable.WORN_ABOUT_BODY,
		Wearable.WORN_HELD,
	};

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(mob.isInCombat())
		{
			mob.tell(L("Not during combat!"));
			return false;
		}

		long pos=-1;
		int i=0;
		Item I=null;
		while(i<checkOrder.length)
		{
			if(mob.freeWearPositions(checkOrder[i],(short)0,(short)0)<=0)
			{
				i++;
				continue;
			}
			pos=checkOrder[i];
			if(pos<0)
			{
				if(mob.getWorshipCharID().length()>0)
					mob.tell(L("@x1 can see that you are already completely armed.",mob.getWorshipCharID()));
				else
					mob.tell(L("The gods can see that you are already armed."));
				return false;
			}
			int numThatsOk=1;
			if(pos==Wearable.WORN_WIELD)
			{
				I=CMClass.getWeapon("GenWeapon");
				I.setName(L("an unholy blade"));
				I.setDisplayText(L("an wicked looking blade sits here."));
				((Weapon)I).setWeaponClassification(Weapon.CLASS_SWORD);
				((Weapon)I).setWeaponDamageType(Weapon.TYPE_SLASHING);
				I.setDescription(L("Whatever made this sharp twisted thing couldn`t have been good.."));
				I.basePhyStats().setLevel(mob.phyStats().level());
				I.basePhyStats().setWeight(20);
				I.setMaterial(RawMaterial.RESOURCE_MITHRIL);
				I.recoverPhyStats();
				final Map<String,String> H=CMLib.itemBuilder().timsItemAdjustments(I,mob.phyStats().level()+(2*getXLEVELLevel(mob)),I.material(),1,((Weapon)I).weaponClassification(),0,I.rawProperLocationBitmap());
				I.basePhyStats().setDamage(CMath.s_int(H.get("DAMAGE")));
				I.basePhyStats().setAttackAdjustment(CMath.s_int(H.get("ATTACK")));
				I.setBaseValue(0);
			}
			else
			if(pos==Wearable.WORN_HELD)
			{
				I=CMClass.getArmor("GenShield");
				I.setName(L("an unholy shield"));
				I.setDisplayText(L("an unholy shield sits here."));
				I.setDescription(L("Whatever made this hideous shield couldn`t have been good."));
				I.basePhyStats().setLevel(mob.phyStats().level());
				I.basePhyStats().setWeight(20);
				I.setMaterial(RawMaterial.RESOURCE_MITHRIL);
				I.recoverPhyStats();
				final Map<String,String> H=CMLib.itemBuilder().timsItemAdjustments(I,mob.phyStats().level()+(2*getXLEVELLevel(mob)),I.material(),1,0,0,I.rawProperLocationBitmap());
				I.basePhyStats().setArmor(CMath.s_int(H.get("ARMOR")));
				I.basePhyStats().setWeight(CMath.s_int(H.get("WEIGHT")));
				I.setBaseValue(0);
			}
			else
			{
				I=CMClass.getArmor("GenArmor");
				I.setRawProperLocationBitmap(pos);
				I.basePhyStats().setLevel(mob.phyStats().level());
				if(pos==Wearable.WORN_ABOUT_BODY)
					I.setMaterial(RawMaterial.RESOURCE_COTTON);
				else
					I.setMaterial(RawMaterial.RESOURCE_MITHRIL);
				I.recoverPhyStats();
				final Map<String,String> H=CMLib.itemBuilder().timsItemAdjustments(I,mob.phyStats().level()+(2*getXLEVELLevel(mob)),I.material(),1,0,0,I.rawProperLocationBitmap());
				I.basePhyStats().setArmor(CMath.s_int(H.get("ARMOR")));
				I.basePhyStats().setWeight(CMath.s_int(H.get("WEIGHT")));
				I.setBaseValue(0);
				if(pos==Wearable.WORN_TORSO)
				{
					I.setName(L("an unholy breast plate"));
					I.setDisplayText(L("a wicked looking breast plate sits here."));
					I.setDescription(L("Whatever made this black spiked armor couldn`t have been good."));
				}
				if(pos==Wearable.WORN_HEAD)
				{
					I.setName(L("an unholy helm"));
					I.setDisplayText(L("a wicked looking helmet sits here."));
					I.setDescription(L("Whatever made this spiked helmet couldn`t have been good."));
				}
				if(pos==Wearable.WORN_ABOUT_BODY)
				{
					I.setName(L("an unholy cape"));
					I.setDisplayText(L("a torn black cape sits here."));
					I.setDescription(L("Whatever made this cape couldn`t have been good."));
				}
				if(pos==Wearable.WORN_ARMS)
				{
					I.setName(L("some unholy arm cannons"));
					I.setDisplayText(L("a pair of wicked looking arm cannons sit here."));
					I.setDescription(L("Whatever made this couldn`t have been good."));
				}
				if((pos==Wearable.WORN_LEFT_WRIST)
				||(pos==Wearable.WORN_RIGHT_WRIST))
				{
					numThatsOk=2;
					I.setName(L("an unholy vambrace"));
					I.setDisplayText(L("a wicked looking spiked vambrace sit here."));
					I.setDescription(L("Whatever made this twisted black metal couldn`t have been good."));
				}
				if(pos==Wearable.WORN_HANDS)
				{
					I.setName(L("a pair of unholy gauntlets"));
					I.setDisplayText(L("some wicked looking gauntlets sit here."));
					I.setDescription(L("Whatever made this twisted black metal couldn`t have been good."));
				}
				if(pos==Wearable.WORN_WAIST)
				{
					I.setName(L("an unholy girdle"));
					I.setDisplayText(L("a wicked looking girdle sits here."));
					I.setDescription(L("Whatever made this twisted black metal couldn`t have been good."));
				}
				if(pos==Wearable.WORN_LEGS)
				{
					I.setName(L("a pair of unholy leg cannons"));
					I.setDisplayText(L("a wicked looking pair of leg cannons sits here."));
					I.setDescription(L("Whatever made this twisted and spiked black metal couldn`t have been good."));
				}
				if(pos==Wearable.WORN_FEET)
				{
					I.setName(L("a pair of unholy boots"));
					I.setDisplayText(L("a wicked looking pair of boots sits here."));
					I.setDescription(L("Whatever made this pair of twisted and spiked black metal boots couldn`t have been good."));
				}
			}
			Ability A=CMClass.getAbility("Prop_HaveZapper");
			if(A!=null)
			{
				A.setMiscText("ACTUAL -GOOD -NEUTRAL -NAMES \"+"+mob.Name()+"\"");
				I.addNonUninvokableEffect(A);
			}
			A=CMClass.getAbility("Prop_ScrapExplode");
			if(A!=null)
				I.addNonUninvokableEffect(A);
			I.recoverPhyStats();
			final int numFound=mob.findItems(null,"$"+I.name()+"$").size() + mob.location().findItems(null,"$"+I.name()+"$").size();
			if(numFound>=numThatsOk)
			{
				i++;
				I=null;
				continue;
			}
			break;
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		if((success)&&(I!=null))
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> @x1 to be provided armament!^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().addItem(I,ItemPossessor.Expire.Monster_EQ);
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 materializes out of the ground.",I.name()));
			}
		}
		else
			return beneficialWordsFizzle(mob, null,L("<S-NAME> @x1 for armament, but flub(s) it.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
