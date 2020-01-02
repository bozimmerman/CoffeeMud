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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2019-2020 Bo Zimmerman

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
public class Prayer_MorphUnholyWeapon extends Prayer
{

	@Override
	public String ID()
	{
		return "Prayer_MorphUnholyWeapon";
	}

	private final static String localizedName = CMLib.lang().L("Morph Unholy Weapon");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_HOLYPROTECTION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected enum UnholyWeaponForm
	{
		sword,
		sword2h,
		mace,
		mace2h,
		flail,
		polearm,
		javelin,
		crossbow,
		staff,
		axe,
		axe2h,
		hammer,
		hammer2h,
		dagger
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final String[] validForms = CMParms.toStringArray(UnholyWeaponForm.values());
		UnholyWeaponForm form=UnholyWeaponForm.values()[CMLib.dice().roll(1, UnholyWeaponForm.values().length, -1)];
		if(!auto)
		{
			if(commands.size()<2)
			{
				mob.tell(L("Morph what into what?  Valid forms include: @x1",CMParms.toListString(validForms)));
				return false;
			}
			final String formStr=commands.remove(commands.size()-1).toLowerCase().trim();
			if(!CMParms.contains(validForms, formStr))
			{
				mob.tell(L("'@x1' is not a valid form. Valid forms include: @x2",formStr,CMParms.toListString(validForms)));
				return false;
			}
			form = UnholyWeaponForm.valueOf(formStr);
		}

		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		if(!(target instanceof Weapon))
		{
			mob.tell(mob,target,null,L("You can't morph <T-NAME>!"));
			return false;
		}
		final Ability unholyA=target.fetchEffect("Prayer_UnholyArmament");
		if((unholyA==null)||(unholyA.text().length()==0))
		{
			mob.tell(L("That's not a true unholy weapon."));
			return false;
		}
		if(!unholyA.text().equalsIgnoreCase(mob.Name()))
		{
			mob.tell(L("That's not YOUR unholy weapon."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> hold(s) <T-NAMESELF> above <S-HIS-HER> head and @x1.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				String finalName;
				int finalType;
				int finalClass;
				boolean finalHands;
				switch(form)
				{
				case axe:
					finalName = "axe";
					finalType = Weapon.TYPE_SLASHING;
					finalClass = Weapon.CLASS_AXE;
					finalHands = false;
					break;
				case axe2h:
					finalName = "two-handed axe";
					finalType = Weapon.TYPE_SLASHING;
					finalClass = Weapon.CLASS_AXE;
					finalHands = true;
					break;
				case crossbow:
					finalName = "crossbow";
					finalType = Weapon.TYPE_PIERCING;
					finalClass = Weapon.CLASS_RANGED;
					finalHands = true;
					break;
				case dagger:
					finalName = "dagger";
					finalType = Weapon.TYPE_PIERCING;
					finalClass = Weapon.CLASS_DAGGER;
					finalHands = false;
					break;
				case flail:
					finalName = "flail";
					finalType = Weapon.TYPE_BASHING;
					finalClass = Weapon.CLASS_FLAILED;
					finalHands = false;
					break;
				case hammer:
					finalName = "hammer";
					finalType = Weapon.TYPE_BASHING;
					finalClass = Weapon.CLASS_HAMMER;
					finalHands = false;
					break;
				case hammer2h:
					finalName = "two-handed hammer";
					finalType = Weapon.TYPE_BASHING;
					finalClass = Weapon.CLASS_HAMMER;
					finalHands = true;
					break;
				case javelin:
					finalName = "javelin";
					finalType = Weapon.TYPE_PIERCING;
					finalClass = Weapon.CLASS_THROWN;
					finalHands = false;
					break;
				case mace:
					finalName = "mace";
					finalType = Weapon.TYPE_BASHING;
					finalClass = Weapon.CLASS_BLUNT;
					finalHands = false;
					break;
				case mace2h:
					finalName = "two-handed mace";
					finalType = Weapon.TYPE_BASHING;
					finalClass = Weapon.CLASS_BLUNT;
					finalHands = true;
					break;
				case polearm:
					finalName = "polearm";
					finalType = Weapon.TYPE_PIERCING;
					finalClass = Weapon.CLASS_POLEARM;
					finalHands = true;
					break;
				case staff:
					finalName = "staff";
					finalType = Weapon.TYPE_BASHING;
					finalClass = Weapon.CLASS_STAFF;
					finalHands = true;
					break;
				case sword:
					finalName = "sword";
					finalType = Weapon.TYPE_SLASHING;
					finalClass = Weapon.CLASS_SWORD;
					finalHands = false;
					break;
				case sword2h:
					finalName = "two-handed sword";
					finalType = Weapon.TYPE_SLASHING;
					finalClass = Weapon.CLASS_SWORD;
					finalHands = true;
					break;
				default:
					finalName = "unknown";
					finalType = Weapon.TYPE_SLASHING;
					finalClass = Weapon.CLASS_SWORD;
					finalHands = false;
					break;
				}
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> morphs!"));
				target.setName(L("an unholy "+finalName));
				target.setDisplayText(L("@x1 has been left here",target.Name()));
				((Weapon)target).setWeaponClassification(finalClass);
				((Weapon)target).setWeaponDamageType(finalType);
				((Weapon)target).setRawProperLocationBitmap(Wearable.WORN_HELD|Wearable.WORN_WIELD);
				((Weapon)target).setRawLogicalAnd(finalHands);
				if(finalClass == Weapon.CLASS_RANGED)
				{
					((Weapon)target).setRanges(0, 5);
					((AmmunitionWeapon)target).setAmmunitionType("bolts");
					((AmmunitionWeapon)target).setAmmoCapacity(1);
					((AmmunitionWeapon)target).setAmmoRemaining(1);
				}
				else
				if(form.toString().endsWith("2h"))
					((Weapon)target).setRanges(0, 1);
				else
					((Weapon)target).setRanges(0, 0);
				target.recoverPhyStats();
				mob.recoverPhyStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> hold(s) <T-NAMESELF> above <S-HIS-HER> head and @x1, but nothing happens.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
