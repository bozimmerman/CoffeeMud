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
   Copyright 2001-2018 Bo Zimmerman

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

public class Spell_Meld extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Meld";
	}

	private final static String localizedName = CMLib.lang().L("Meld");

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
		return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	public boolean shinBone(Item one, Item two, long locationOne, long locationTwo)
	{
		if((one.fitsOn(locationOne)&&two.fitsOn(locationTwo))
		   &&(!one.fitsOn(locationTwo))
		   &&(!two.fitsOn(locationOne)))
			return true;
		else
		if((two.fitsOn(locationOne)&&one.fitsOn(locationTwo))
		   &&(!two.fitsOn(locationTwo))
		   &&(!one.fitsOn(locationOne)))
			return true;
		return false;
	}
	int[] heiarchy={RawMaterial.MATERIAL_FLESH,
					RawMaterial.MATERIAL_PAPER,
					RawMaterial.MATERIAL_CLOTH,
					RawMaterial.MATERIAL_LEATHER,
					RawMaterial.MATERIAL_VEGETATION,
					RawMaterial.MATERIAL_WOODEN,
					RawMaterial.MATERIAL_SYNTHETIC,
					RawMaterial.MATERIAL_METAL,
					RawMaterial.MATERIAL_ROCK,
					RawMaterial.MATERIAL_PRECIOUS,
					RawMaterial.MATERIAL_ENERGY,
					RawMaterial.MATERIAL_GAS,
					RawMaterial.MATERIAL_MITHRIL,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99};

	protected int getHeiarchy(int material)
	{
		for(int i=0;i<heiarchy.length;i++)
		{
			if(heiarchy[i]==material)
				return i;
		}
		return 99;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		// add something to disable traps
		//
		if(commands.size()<2)
		{
			mob.tell(L("Meld what and what else together?"));
			return false;
		}
		final Item itemOne=mob.findItem(null,commands.get(0));
		if((itemOne==null)||(!CMLib.flags().canBeSeenBy(itemOne,mob)))
		{
			mob.tell(L("You don't seem to have a '@x1'.",(commands.get(0))));
			return false;
		}
		final Item itemTwo=mob.findItem(null,CMParms.combine(commands,1));
		if((itemTwo==null)||(!CMLib.flags().canBeSeenBy(itemTwo,mob)))
		{
			mob.tell(L("You don't seem to have a '@x1'.",CMParms.combine(commands,1)));
			return false;
		}

		Item melded=null;

		if((itemOne instanceof Armor)&&(itemTwo instanceof Armor))
		{
			final Armor armorOne=(Armor)itemOne;
			final Armor armorTwo=(Armor)itemTwo;
			if(armorOne.getClothingLayer()!=armorTwo.getClothingLayer())
			{
				mob.tell(L("This spell can only be cast on items worn at the same layer."));
				return false;
			}
			if(armorOne.getLayerAttributes()!=armorTwo.getLayerAttributes())
			{
				mob.tell(L("Those items are too different to meld together."));
				return false;
			}

			if(shinBone(itemOne,itemTwo,Wearable.WORN_HEAD,Wearable.WORN_NECK)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_HEAD,Wearable.WORN_EARS)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_HEAD,Wearable.WORN_EYES)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_HEAD,Wearable.WORN_TORSO)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_NECK,Wearable.WORN_TORSO)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_TORSO,Wearable.WORN_ARMS)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_TORSO,Wearable.WORN_WAIST)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_WAIST,Wearable.WORN_LEGS)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_ARMS,Wearable.WORN_LEFT_WRIST)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_ARMS,Wearable.WORN_HANDS)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_HANDS,Wearable.WORN_LEFT_WRIST)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_HANDS,Wearable.WORN_RIGHT_FINGER)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_TORSO,Wearable.WORN_LEGS)
			   ||shinBone(itemOne,itemTwo,Wearable.WORN_LEGS,Wearable.WORN_FEET))
			{

			}
			else
			{
				mob.tell(L("@x1 and @x2 aren't worn in compatible places, and thus can't be melded.",itemOne.name(),itemTwo.name()));
				return false;
			}
		}
		else
		if((itemOne instanceof Weapon)||(itemTwo instanceof Weapon))
		{
			if(!itemOne.fitsOn(Wearable.WORN_HELD))
			{
				mob.tell(L("@x1 can't be held, and thus can't be melded with @x2.",itemOne.name(),itemTwo.name()));
				return false;
			}
			if(!itemTwo.fitsOn(Wearable.WORN_HELD))
			{
				mob.tell(L("@x1 can't be held, and thus can't be melded with @x2.",itemTwo.name(),itemOne.name()));
				return false;
			}
			if(itemOne.rawLogicalAnd())
			{
				mob.tell(L("@x1 is two handed, and thus can't be melded with @x2.",itemOne.name(),itemTwo.name()));
				return false;
			}
			if(itemTwo.rawLogicalAnd())
			{
				mob.tell(L("@x1 is two handed, and thus can't be melded with @x2.",itemTwo.name(),itemOne.name()));
				return false;
			}
		}
		else
		if((itemOne instanceof Container)&&(itemTwo instanceof Container))
		{

		}
		else
		{
			mob.tell(L("You can't meld those together."));
			return false;
		}

		if(itemOne==itemTwo)
		{
			mob.tell(L("You can't meld something to itself."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,itemOne,this,verbalCastCode(mob,itemOne,auto),L("^S<S-NAME> meld(s) @x1 and @x2.^?",itemOne.name(),itemTwo.name()));
			final CMMsg msg2=CMClass.getMsg(mob,itemTwo,this,verbalCastCode(mob,itemOne,auto),null);
			if(mob.location().okMessage(mob,msg)&&mob.location().okMessage(mob,msg2))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);

				if((msg.value()>0)||(msg2.value()>0))
					return false;

				String itemOneName=itemOne.Name();
				String itemTwoName=itemTwo.Name();
				int x=itemOneName.indexOf("melded together");
				if(x>0)
					itemOneName=itemOneName.substring(0,x).trim();
				x=itemTwoName.indexOf("melded together");
				if(x>0)
					itemTwoName=itemTwoName.substring(0,x).trim();

				int material=itemOne.material();
				if(getHeiarchy(material&RawMaterial.MATERIAL_MASK)<getHeiarchy(itemTwo.material()&RawMaterial.MATERIAL_MASK))
					material=itemTwo.material();

				final String newName=itemOneName+" and "+itemTwoName+" melded together";
				if((itemOne instanceof Armor)&&(itemTwo instanceof Armor))
				{

					long wornLocation=itemOne.rawProperLocationBitmap()|itemTwo.rawProperLocationBitmap();
					if((wornLocation&Wearable.WORN_HELD)==(Wearable.WORN_HELD))
						wornLocation-=Wearable.WORN_HELD;
					if(((wornLocation&Wearable.WORN_LEFT_FINGER)==(Wearable.WORN_LEFT_FINGER))
					   &&((wornLocation&Wearable.WORN_RIGHT_FINGER)==(Wearable.WORN_RIGHT_FINGER)))
					{
						if(((wornLocation&Wearable.WORN_LEFT_WRIST)==(Wearable.WORN_LEFT_WRIST))
						&&((wornLocation&Wearable.WORN_RIGHT_WRIST)==0))
						   wornLocation-=Wearable.WORN_RIGHT_FINGER;
						else
						if(((wornLocation&Wearable.WORN_RIGHT_WRIST)==(Wearable.WORN_RIGHT_WRIST))
						&&((wornLocation&Wearable.WORN_LEFT_WRIST)==0))
						   wornLocation-=Wearable.WORN_LEFT_FINGER;
						else
						{
							if(CMLib.dice().rollPercentage()>50)
								wornLocation-=Wearable.WORN_RIGHT_FINGER;
							else
								wornLocation-=Wearable.WORN_LEFT_FINGER;
						}
					}

					if(((wornLocation&Wearable.WORN_LEFT_WRIST)==(Wearable.WORN_LEFT_WRIST))
					   &&((wornLocation&Wearable.WORN_RIGHT_WRIST)==(Wearable.WORN_RIGHT_WRIST)))
					{
						if(((wornLocation&Wearable.WORN_LEFT_FINGER)==(Wearable.WORN_LEFT_FINGER))
						&&((wornLocation&Wearable.WORN_RIGHT_FINGER)==0))
						   wornLocation-=Wearable.WORN_RIGHT_WRIST;
						else
						if(((wornLocation&Wearable.WORN_RIGHT_FINGER)==(Wearable.WORN_RIGHT_FINGER))
						&&((wornLocation&Wearable.WORN_LEFT_FINGER)==0))
						   wornLocation-=Wearable.WORN_LEFT_WRIST;
						else
						{
							if(CMLib.dice().rollPercentage()>50)
								wornLocation-=Wearable.WORN_RIGHT_WRIST;
							else
								wornLocation-=Wearable.WORN_LEFT_WRIST;
						}
					}

					final Armor gc=CMClass.getArmor("GenArmor");
					gc.setMaterial(material);
					gc.setName(newName);
					gc.setDisplayText(L("@x1 sits here.",newName));
					gc.setDescription(L("It looks like someone melded @x1 and @x2",itemOneName,itemTwoName));
					gc.setSecretIdentity(itemOne.rawSecretIdentity()+", "+itemTwo.rawSecretIdentity());
					gc.setBaseValue(itemOne.baseGoldValue()+itemTwo.baseGoldValue());
					gc.basePhyStats().setWeight(itemOne.basePhyStats().weight()+itemTwo.basePhyStats().weight());
					gc.basePhyStats().setArmor((itemOne.basePhyStats().armor()+itemTwo.basePhyStats().armor())/2);
					if(gc instanceof Container)
					{
						final Container cgc=(Container)gc;
						cgc.setCapacity(0);
						if(itemOne instanceof Container)
							cgc.setCapacity(cgc.capacity()+((Container)itemOne).capacity());
						if(itemTwo instanceof Container)
							cgc.setCapacity(cgc.capacity()+((Container)itemTwo).capacity());
					}
					gc.setRawLogicalAnd(true);
					gc.setRawProperLocationBitmap(wornLocation);

					gc.basePhyStats().setLevel(itemOne.basePhyStats().level());
					if(itemTwo.basePhyStats().level()>itemOne.basePhyStats().level())
						gc.basePhyStats().setLevel(itemTwo.basePhyStats().level());
					gc.basePhyStats().setAbility((itemOne.basePhyStats().ability()+itemTwo.basePhyStats().ability())/2);
					melded=gc;
					mob.addItem(gc);
				}
				else
				if((itemOne instanceof Weapon)||(itemTwo instanceof Weapon))
				{
					final Weapon gc=CMClass.getWeapon("GenWeapon");
					gc.setMaterial(material);
					gc.setName(newName);
					gc.setDisplayText(L("@x1 sits here.",newName));
					gc.setDescription(L("It looks like someone melded @x1 and @x2",itemOneName,itemTwoName));
					gc.setSecretIdentity(itemOne.rawSecretIdentity()+", "+itemTwo.rawSecretIdentity());
					gc.setBaseValue(itemOne.baseGoldValue()+itemTwo.baseGoldValue());
					gc.basePhyStats().setWeight(itemOne.basePhyStats().weight()+itemTwo.basePhyStats().weight());
					gc.basePhyStats().setAttackAdjustment((itemOne.basePhyStats().attackAdjustment()+itemTwo.basePhyStats().attackAdjustment())/2);
					gc.basePhyStats().setDamage((itemOne.basePhyStats().damage()+itemTwo.basePhyStats().damage())/2);
					if(gc instanceof AmmunitionWeapon)
					{
						if(itemOne instanceof AmmunitionWeapon)
							((AmmunitionWeapon)gc).setAmmoCapacity(((AmmunitionWeapon)itemOne).ammunitionCapacity());
						if(itemTwo instanceof AmmunitionWeapon)
							((AmmunitionWeapon)gc).setAmmoCapacity(((AmmunitionWeapon)itemTwo).ammunitionCapacity() + ((AmmunitionWeapon)gc).ammunitionCapacity());
						if((itemOne instanceof AmmunitionWeapon)&&(((AmmunitionWeapon)itemOne).ammunitionType().length()>0))
							((AmmunitionWeapon)gc).setAmmunitionType(((AmmunitionWeapon)itemOne).ammunitionType());
						if((itemTwo instanceof AmmunitionWeapon)&&(((AmmunitionWeapon)itemTwo).ammunitionType().length()>0))
							((AmmunitionWeapon)gc).setAmmunitionType(((AmmunitionWeapon)itemTwo).ammunitionType());
					}
					if(itemOne instanceof Weapon)
						gc.setWeaponDamageType(((Weapon)itemOne).weaponDamageType());
					else
						gc.setWeaponDamageType(((Weapon)itemTwo).weaponDamageType());
					if(itemTwo instanceof Weapon)
						gc.setWeaponClassification(((Weapon)itemTwo).weaponClassification());
					else
						gc.setWeaponClassification(((Weapon)itemOne).weaponClassification());
					gc.setRawLogicalAnd(true);
					gc.setRanges((itemOne.minRange()+itemTwo.minRange())/2, (itemOne.maxRange()+itemTwo.maxRange())/2);
					gc.basePhyStats().setLevel(itemOne.basePhyStats().level());
					if(itemTwo.basePhyStats().level()>itemOne.basePhyStats().level())
						gc.basePhyStats().setLevel(itemTwo.basePhyStats().level());
					gc.basePhyStats().setAbility((itemOne.basePhyStats().ability()+itemTwo.basePhyStats().ability())/2);
					melded=gc;
					mob.addItem(gc);
				}
				else
				if((itemOne instanceof Container)&&(itemTwo instanceof Container))
				{
					boolean isLocked=((Container)itemOne).hasALock();
					String keyName=((Container)itemOne).keyName();
					if(!isLocked)
					{
						isLocked=((Container)itemTwo).hasALock();
						keyName=((Container)itemTwo).keyName();
					}
					final Container gc=(Container)CMClass.getItem("GenContainer");
					gc.setMaterial(material);
					gc.setName(newName);
					gc.setDisplayText(L("@x1 sits here.",newName));
					gc.setDescription(L("It looks like someone melded @x1 and @x2",itemOneName,itemTwoName));
					CMLib.flags().setGettable(gc,CMLib.flags().isGettable(itemOne)&&CMLib.flags().isGettable(itemTwo));
					gc.setBaseValue(itemOne.baseGoldValue()+itemTwo.baseGoldValue());
					gc.basePhyStats().setWeight(itemOne.basePhyStats().weight()+itemTwo.basePhyStats().weight());
					gc.setCapacity(((Container)itemOne).capacity()+((Container)itemTwo).capacity());
					gc.setDoorsNLocks((((Container)itemOne).hasADoor()||((Container)itemTwo).hasADoor()),true,(((Container)itemOne).defaultsClosed()||((Container)itemTwo).defaultsClosed()),
										isLocked,false,isLocked);
					gc.setKeyName(keyName);

					gc.basePhyStats().setLevel(itemOne.basePhyStats().level());
					if(itemTwo.basePhyStats().level()>itemOne.basePhyStats().level())
						gc.basePhyStats().setLevel(itemTwo.basePhyStats().level());
					gc.basePhyStats().setAbility(itemOne.basePhyStats().ability()+itemTwo.basePhyStats().ability());
					melded=gc;
					mob.addItem(gc);
				}
				if(melded!=null)
				{
					for(final Enumeration<Ability> a=itemOne.effects();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if((A!=null)&&(A.isSavable())&&(melded.fetchEffect(A.ID())==null))
							melded.addEffect((Ability)A.copyOf());
					}
					for(final Enumeration<Ability> a=itemTwo.effects();a.hasMoreElements();)
					{
						final Ability A=a.nextElement();
						if((A!=null)&&(A.isSavable())&&(melded.fetchEffect(A.ID())==null))
							melded.addEffect((Ability)A.copyOf());
					}
					for(final Enumeration<Behavior> e=itemOne.behaviors();e.hasMoreElements();)
					{
						final Behavior B=e.nextElement();
						if((B!=null)&&(B.isSavable()))
							melded.addBehavior((Behavior)B.copyOf());
					}
					for(final Enumeration<Behavior> e=itemTwo.behaviors();e.hasMoreElements();)
					{
						final Behavior B=e.nextElement();
						if((B!=null)&&(B.isSavable()))
							melded.addBehavior((Behavior)B.copyOf());
					}
					melded.recoverPhyStats();
				}
				if((melded!=null)&&(melded.subjectToWearAndTear()))
					melded.setUsesRemaining(100);
				itemOne.destroy();
				itemTwo.destroy();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to meld @x1 and @x2, but fail(s).",itemOne.name(),itemTwo.name()));

		// return whether it worked
		return success;
	}
}
