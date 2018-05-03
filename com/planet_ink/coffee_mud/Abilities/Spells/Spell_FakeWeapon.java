package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2002-2018 Bo Zimmerman

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

public class Spell_FakeWeapon extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_FakeWeapon";
	}

	private final static String localizedName = CMLib.lang().L("Fake Weapon");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public void unInvoke()
	{
		Item item=null;
		if(affected instanceof Item)
			item=(Item)affected;
		super.unInvoke();
		if((item != null)&&(super.canBeUninvoked()))
			item.destroy();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)&&(affected instanceof Item))
		{
			if((msg.tool()==affected)
			&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
			{
				int damageType=Weapon.TYPE_BURSTING;
				if(affected instanceof Weapon)
					damageType=((Weapon)affected).weaponDamageType();
				if(msg.sourceMessage()!=null)
					msg.setSourceMessage(CMLib.combat().replaceDamageTag(msg.sourceMessage(), msg.value(), damageType, CMMsg.View.SOURCE));
				if(msg.targetMessage()!=null)
					msg.setTargetMessage(CMLib.combat().replaceDamageTag(msg.targetMessage(), msg.value(), damageType, CMMsg.View.TARGET));
				if(msg.othersMessage()!=null)
					msg.setOthersMessage(CMLib.combat().replaceDamageTag(msg.othersMessage(), msg.value(), damageType, CMMsg.View.OTHERS));
				msg.setValue(0);
			}
			else
			if((msg.target()!=null)
			&&((msg.target()==affected)
				||(msg.target()==((Item)affected).container())
				||(msg.target()==((Item)affected).ultimateContainer(null))))
			{
				if(((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MAGIC))
				||(CMath.bset(msg.targetMajor(),CMMsg.MASK_MAGIC))
				||(CMath.bset(msg.othersMajor(),CMMsg.MASK_MAGIC))))
				{
					Room room=null;
					if(msg.source().location()!=null)
						room=msg.source().location();
					if(room==null)
						room=CMLib.map().roomLocation(affected);
					if(room!=null)
						room.showHappens(CMMsg.MSG_OK_VISUAL,L("Magic energy fizzles around @x1 and is absorbed into the air.",affected.Name()));
					return false;
				}
				else
				if(msg.tool() instanceof Ability)
				{
					msg.source().tell(L("That doesn't appear to work on @x1",affected.name()));
					return false;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final String weaponName=CMParms.combine(commands,0);
		final String[] choices={"sword","dagger","mace","staff","axe","hammer", "flail"};
		int choice=-1;
		for(int i=0;i<choices.length;i++)
		{
			if(choices[i].equalsIgnoreCase(weaponName))
				choice=i;
		}
		if(choice<0)
		{
			mob.tell(L("You must specify what kind of weapon to create: sword, dagger, mace, flail, staff, axe, or hammer."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),auto?"":L("^S<S-NAME> wave(s) <S-HIS-HER> arms around dramatically.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Weapon weapon=(Weapon)CMClass.getItem("GenWeapon");
				weapon.basePhyStats().setAttackAdjustment(100 +(10 * super.getXLEVELLevel(mob)));
				weapon.basePhyStats().setDamage(75+(3 * super.getXLEVELLevel(mob)));
				weapon.basePhyStats().setDisposition(weapon.basePhyStats().disposition()|PhyStats.IS_BONUS);
				weapon.setMaterial(RawMaterial.RESOURCE_COTTON);
				switch(choice)
				{
				case 0:
					weapon.setName(L("a fancy sword"));
					weapon.setDisplayText(L("a fancy sword sits here"));
					weapon.setDescription(L("looks fit to cut something up!"));
					weapon.setWeaponClassification(Weapon.CLASS_SWORD);
					weapon.setWeaponDamageType(Weapon.TYPE_SLASHING);
					break;
				case 1:
					weapon.setName(L("a sharp dagger"));
					weapon.setDisplayText(L("a sharp dagger sits here"));
					weapon.setDescription(L("looks fit to cut something up!"));
					weapon.setWeaponClassification(Weapon.CLASS_DAGGER);
					weapon.setWeaponDamageType(Weapon.TYPE_PIERCING);
					break;
				case 2:
					weapon.setName(L("a large mace"));
					weapon.setDisplayText(L("a large mace sits here"));
					weapon.setDescription(L("looks fit to whomp on something with!"));
					weapon.setWeaponClassification(Weapon.CLASS_BLUNT);
					weapon.setWeaponDamageType(Weapon.TYPE_BASHING);
					break;
				case 3:
					weapon.setName(L("a quarterstaff"));
					weapon.setDisplayText(L("a quarterstaff sits here"));
					weapon.setDescription(L("looks like a reliable weapon"));
					weapon.setWeaponClassification(Weapon.CLASS_STAFF);
					weapon.setWeaponDamageType(Weapon.TYPE_BASHING);
					break;
				case 4:
					weapon.setName(L("a deadly axe"));
					weapon.setDisplayText(L("a deadly axe sits here"));
					weapon.setDescription(L("looks fit to shop something up!"));
					weapon.setWeaponClassification(Weapon.CLASS_AXE);
					weapon.setWeaponDamageType(Weapon.TYPE_SLASHING);
					break;
				case 5:
					weapon.setName(L("a large hammer"));
					weapon.setDisplayText(L("a large hammer sits here"));
					weapon.setDescription(L("looks fit to pound something into a pulp!"));
					weapon.setWeaponClassification(Weapon.CLASS_HAMMER);
					weapon.setWeaponDamageType(Weapon.TYPE_BASHING);
					break;
				case 6:
					weapon.setName(L("a large flail"));
					weapon.setDisplayText(L("a large flail sits here"));
					weapon.setDescription(L("looks fit to pound something into a pulp!"));
					weapon.setWeaponClassification(Weapon.CLASS_FLAILED);
					weapon.setWeaponDamageType(Weapon.TYPE_BASHING);
					break;
				}
				weapon.basePhyStats().setWeight(0);
				weapon.setBaseValue(0);
				weapon.recoverPhyStats();
				mob.addItem(weapon);
				mob.location().show(mob,null,weapon,CMMsg.MSG_OK_ACTION,L("Suddenly, <S-NAME> own(s) <O-NAME>!"));
				beneficialAffect(mob,weapon,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> dramatically wave(s) <S-HIS-HER> arms around, but fizzle(s) the spell."));

		// return whether it worked
		return success;
	}
}
