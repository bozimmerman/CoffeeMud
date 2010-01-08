package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Spell_Meld extends Spell
{
	public String ID() { return "Spell_Meld"; }
	public String name(){return "Meld";}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

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
					RawMaterial.MATERIAL_PLASTIC,
					RawMaterial.MATERIAL_METAL,
					RawMaterial.MATERIAL_ROCK,
					RawMaterial.MATERIAL_PRECIOUS,
					RawMaterial.MATERIAL_ENERGY,
					RawMaterial.MATERIAL_MITHRIL,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99,99};

	protected int getHeiarchy(int material)
	{
		for(int i=0;i<heiarchy.length;i++)
			if(heiarchy[i]==material) return i;
		return 99;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		// add something to disable traps
		//
		if(commands.size()<2)
		{
			mob.tell("Meld what and what else together?");
			return false;
		}
		Item itemOne=mob.fetchInventory(null,(String)commands.elementAt(0));
		if((itemOne==null)||(!CMLib.flags().canBeSeenBy(itemOne,mob)))
		{
			mob.tell("You don't seem to have a '"+((String)commands.elementAt(0))+"'.");
			return false;
		}
		Item itemTwo=mob.fetchInventory(null,CMParms.combine(commands,1));
		if((itemTwo==null)||(!CMLib.flags().canBeSeenBy(itemTwo,mob)))
		{
			mob.tell("You don't seem to have a '"+CMParms.combine(commands,1)+"'.");
			return false;
		}

		Environmental melded=null;

		if((itemOne instanceof Armor)&&(itemTwo instanceof Armor))
		{
			Armor armorOne=(Armor)itemOne;
			Armor armorTwo=(Armor)itemTwo;
			if(armorOne.getClothingLayer()!=armorTwo.getClothingLayer())
			{
				mob.tell("This spell can only be cast on items worn at the same layer.");
				return false;
			}
			if(armorOne.getLayerAttributes()!=armorTwo.getLayerAttributes())
			{
				mob.tell("Those items are too different to meld together.");
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
				mob.tell(itemOne.name()+" and "+itemTwo.name()+" aren't worn in compatible places, and thus can't be melded.");
				return false;
			}
		}
		else
		if((itemOne instanceof Weapon)||(itemTwo instanceof Weapon))
		{
			if(!itemOne.fitsOn(Wearable.WORN_HELD))
			{
				mob.tell(itemOne.name()+" can't be held, and thus can't be melded with "+itemTwo.name()+".");
				return false;
			}
			if(!itemTwo.fitsOn(Wearable.WORN_HELD))
			{
				mob.tell(itemTwo.name()+" can't be held, and thus can't be melded with "+itemOne.name()+".");
				return false;
			}
			if(itemOne.rawLogicalAnd())
			{
				mob.tell(itemOne.name()+" is two handed, and thus can't be melded with "+itemTwo.name()+".");
				return false;
			}
			if(itemTwo.rawLogicalAnd())
			{
				mob.tell(itemTwo.name()+" is two handed, and thus can't be melded with "+itemOne.name()+".");
				return false;
			}
		}
		else
		if((itemOne instanceof Container)&&(itemTwo instanceof Container))
		{

		}
		else
		{
			mob.tell("You can't meld those together.");
			return false;
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,itemOne,auto),"^S<S-NAME> meld(s) "+itemOne.name()+" and "+itemTwo.name()+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);

				String itemOneName=itemOne.Name();
				String itemTwoName=itemTwo.Name();
				int x=itemOneName.indexOf("melded together");
				if(x>0) itemOneName=itemOneName.substring(0,x).trim();
				x=itemTwoName.indexOf("melded together");
				if(x>0) itemTwoName=itemTwoName.substring(0,x).trim();

				String newName=itemOneName+" and "+itemTwoName+" melded together";
				if((itemOne instanceof Armor)&&(itemTwo instanceof Armor))
				{
					int material=((Armor)itemOne).material();
					if(getHeiarchy(material&RawMaterial.MATERIAL_MASK)<getHeiarchy(((Armor)itemTwo).material()&RawMaterial.MATERIAL_MASK))
						material=((Armor)itemTwo).material();

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


					Armor gc=CMClass.getArmor("GenArmor");
					gc.setName(newName);
					gc.setDisplayText(newName+" sits here.");
					gc.setDescription("It looks like someone melded "+itemOneName+" and "+itemTwoName);
					gc.setSecretIdentity(itemOne.rawSecretIdentity()+", "+itemTwo.rawSecretIdentity());
					gc.setBaseValue(itemOne.baseGoldValue()+itemTwo.baseGoldValue());
					gc.baseEnvStats().setWeight(itemOne.baseEnvStats().weight()+itemTwo.baseEnvStats().weight());
					gc.baseEnvStats().setArmor((itemOne.baseEnvStats().armor()+itemTwo.baseEnvStats().armor())/2);
					gc.setMaterial(material);
					gc.setCapacity(0);
					if(itemOne instanceof Container)
						gc.setCapacity(gc.capacity()+((Container)itemOne).capacity());
					if(itemTwo instanceof Container)
						gc.setCapacity(gc.capacity()+((Container)itemTwo).capacity());
					gc.setRawLogicalAnd(true);
					gc.setRawProperLocationBitmap(wornLocation);

					gc.baseEnvStats().setLevel(itemOne.baseEnvStats().level());
					if(itemTwo.baseEnvStats().level()>itemOne.baseEnvStats().level())
						gc.baseEnvStats().setLevel(itemTwo.baseEnvStats().level());
					gc.baseEnvStats().setAbility((itemOne.baseEnvStats().ability()+itemTwo.baseEnvStats().ability())/2);
					melded=gc;
					mob.addInventory(gc);
				}
				else
				if((itemOne instanceof Weapon)||(itemTwo instanceof Weapon))
				{
					Weapon gc=CMClass.getWeapon("GenWeapon");
					gc.setName(newName);
					gc.setDisplayText(newName+" sits here.");
					gc.setDescription("It looks like someone melded "+itemOneName+" and "+itemTwoName);
					gc.setSecretIdentity(itemOne.rawSecretIdentity()+", "+itemTwo.rawSecretIdentity());
					gc.setBaseValue(itemOne.baseGoldValue()+itemTwo.baseGoldValue());
					gc.baseEnvStats().setWeight(itemOne.baseEnvStats().weight()+itemTwo.baseEnvStats().weight());
					gc.baseEnvStats().setAttackAdjustment((itemOne.baseEnvStats().attackAdjustment()+itemTwo.baseEnvStats().attackAdjustment())/2);
					gc.baseEnvStats().setDamage((itemOne.baseEnvStats().damage()+itemTwo.baseEnvStats().damage())/2);
					if(itemOne instanceof Weapon)
						gc.setWeaponType(((Weapon)itemOne).weaponType());
					else
						gc.setWeaponType(((Weapon)itemTwo).weaponType());
					if(itemTwo instanceof Weapon)
						gc.setWeaponClassification(((Weapon)itemTwo).weaponClassification());
					else
						gc.setWeaponClassification(((Weapon)itemOne).weaponClassification());
					gc.setRawLogicalAnd(true);
					gc.baseEnvStats().setLevel(itemOne.baseEnvStats().level());
					if(itemTwo.baseEnvStats().level()>itemOne.baseEnvStats().level())
						gc.baseEnvStats().setLevel(itemTwo.baseEnvStats().level());
					gc.baseEnvStats().setAbility((itemOne.baseEnvStats().ability()+itemTwo.baseEnvStats().ability())/2);
					melded=gc;
					mob.addInventory(gc);
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
					Container gc=(Container)CMClass.getItem("GenContainer");
					gc.setName(newName);
					gc.setDisplayText(newName+" sits here.");
					gc.setDescription("It looks like someone melded "+itemOneName+" and "+itemTwoName);
					CMLib.flags().setGettable(gc,CMLib.flags().isGettable(itemOne)&&CMLib.flags().isGettable(itemTwo));
					gc.setBaseValue(itemOne.baseGoldValue()+itemTwo.baseGoldValue());
					gc.baseEnvStats().setWeight(itemOne.baseEnvStats().weight()+itemTwo.baseEnvStats().weight());
					gc.setCapacity(((Container)itemOne).capacity()+((Container)itemTwo).capacity());
					gc.setLidsNLocks((((Container)itemOne).hasALid()||((Container)itemTwo).hasALid()),true,isLocked,false);
					gc.setKeyName(keyName);

					gc.baseEnvStats().setLevel(itemOne.baseEnvStats().level());
					if(itemTwo.baseEnvStats().level()>itemOne.baseEnvStats().level())
						gc.baseEnvStats().setLevel(itemTwo.baseEnvStats().level());
					gc.baseEnvStats().setAbility(itemOne.baseEnvStats().ability()+itemTwo.baseEnvStats().ability());
					melded=gc;
					mob.addInventory(gc);
				}
				if(melded!=null)
				{
					for(int a=0;a<itemOne.numEffects();a++)
					{
						Ability aff=itemOne.fetchEffect(a);
						if((aff!=null)&&(melded.fetchEffect(aff.ID())==null))
							melded.addEffect(aff);
					}
					for(int a=0;a<itemTwo.numEffects();a++)
					{
						Ability aff=itemTwo.fetchEffect(a);
						if((aff!=null)&&(melded.fetchEffect(aff.ID())==null))
							melded.addEffect(aff);
					}
					for(int a=0;a<itemOne.numBehaviors();a++)
					{
						Behavior B=itemOne.fetchBehavior(a);
						if(B!=null)	melded.addBehavior(B);
					}
					for(int a=0;a<itemTwo.numBehaviors();a++)
					{
						Behavior B=itemTwo.fetchBehavior(a);
						if(B!=null)	melded.addBehavior(B);
					}
					melded.recoverEnvStats();
				}
				itemOne.destroy();
				itemTwo.destroy();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) "+itemOne.name()+" and "+itemTwo.name()+", but fail(s).");

		// return whether it worked
		return success;
	}
}
