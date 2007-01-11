package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2000-2007 Bo Zimmerman

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
	public String ID() { return "Prayer_UnholyArmament"; }
	public String name(){ return "Unholy Armament";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_HOLYPROTECTION;}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	public static final long[] checkOrder={
		Item.WORN_WIELD,
		Item.WORN_TORSO,
		Item.WORN_LEGS,
		Item.WORN_WAIST,
		Item.WORN_HEAD,
		Item.WORN_ARMS,
		Item.WORN_FEET,
		Item.WORN_HANDS,
		Item.WORN_LEFT_WRIST,
		Item.WORN_RIGHT_WRIST,
		Item.WORN_ABOUT_BODY,
	};

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(mob.isInCombat())
		{
			mob.tell("Not during combat!");
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
					mob.tell(mob.getWorshipCharID()+" can see that you are already completely armed.");
				else
					mob.tell("The gods can see that you are already armed.");
				return false;
			}
			if(pos==Item.WORN_WIELD)
			{
				I=CMClass.getWeapon("GenWeapon");
				I.setName("an unholy blade");
				I.setDisplayText("an wicked looking blade sits here.");
				((Weapon)I).setWeaponClassification(Weapon.CLASS_SWORD);
				((Weapon)I).setWeaponType(Weapon.TYPE_SLASHING);
				I.setDescription("Whatever made this sharp twisted thing couldn`t have been good..");
				I.baseEnvStats().setLevel(mob.envStats().level());
				I.baseEnvStats().setWeight(20);
				I.setMaterial(RawMaterial.RESOURCE_MITHRIL);
				I.recoverEnvStats();
				Hashtable H=CMLib.itemBuilder().timsItemAdjustments(I,mob.envStats().level()+(2*super.getXLEVELLevel(mob)),I.material(),I.baseEnvStats().weight(),1,((Weapon)I).weaponClassification(),0,I.rawProperLocationBitmap());
				I.baseEnvStats().setDamage(CMath.s_int((String)H.get("DAMAGE")));
				I.baseEnvStats().setAttackAdjustment(CMath.s_int((String)H.get("ATTACK")));
				I.setBaseValue(0);
			}
			else
			{
				I=CMClass.getArmor("GenArmor");
				I.setRawProperLocationBitmap(pos);
				I.baseEnvStats().setLevel(mob.envStats().level());
				if(pos==Item.WORN_ABOUT_BODY)
					I.setMaterial(RawMaterial.RESOURCE_COTTON);
				else
					I.setMaterial(RawMaterial.RESOURCE_MITHRIL);
				I.recoverEnvStats();
				Hashtable H=CMLib.itemBuilder().timsItemAdjustments(I,mob.envStats().level()+(2*super.getXLEVELLevel(mob)),I.material(),I.baseEnvStats().weight(),1,0,0,I.rawProperLocationBitmap());
				I.baseEnvStats().setArmor(CMath.s_int((String)H.get("ARMOR")));
				I.baseEnvStats().setWeight(CMath.s_int((String)H.get("WEIGHT")));
				I.setBaseValue(0);
				if(pos==Item.WORN_TORSO)
				{
					I.setName("an unholy breast plate");
					I.setDisplayText("a wicked looking breast plate sits here.");
					I.setDescription("Whatever made this black spiked armor couldn`t have been good.");
				}
				if(pos==Item.WORN_HEAD)
				{
					I.setName("an unholy helm");
					I.setDisplayText("a wicked looking helmet sits here.");
					I.setDescription("Whatever made this spiked helmet couldn`t have been good.");
				}
				if(pos==Item.WORN_ABOUT_BODY)
				{
					I.setName("an unholy cape");
					I.setDisplayText("a torn black cape sits here.");
					I.setDescription("Whatever made this cape couldn`t have been good.");
				}
				if(pos==Item.WORN_ARMS)
				{
					I.setName("some unholy arm cannons");
					I.setDisplayText("a pair of wicked looking arm cannons sit here.");
					I.setDescription("Whatever made this couldn`t have been good.");
				}
				if((pos==Item.WORN_LEFT_WRIST)
				||(pos==Item.WORN_RIGHT_WRIST))
				{
					I.setName("an unholy vambrace");
					I.setDisplayText("a wicked looking spiked vambrace sit here.");
					I.setDescription("Whatever made this twisted black metal couldn`t have been good.");
				}
				if(pos==Item.WORN_HANDS)
				{
					I.setName("a pair of unholy gauntlets");
					I.setDisplayText("some wicked looking gauntlets sit here.");
					I.setDescription("Whatever made this twisted black metal couldn`t have been good.");
				}
				if(pos==Item.WORN_WAIST)
				{
					I.setName("an unholy girdle");
					I.setDisplayText("a wicked looking girdle sits here.");
					I.setDescription("Whatever made this twisted black metal couldn`t have been good.");
				}
				if(pos==Item.WORN_LEGS)
				{
					I.setName("a pair of unholy leg cannons");
					I.setDisplayText("a wicked looking pair of leg cannons sits here.");
					I.setDescription("Whatever made this twisted and spiked black metal couldn`t have been good.");
				}
				if(pos==Item.WORN_FEET)
				{
					I.setName("a pair of unholy boots");
					I.setDisplayText("a wicked looking pair of boots sits here.");
					I.setDescription("Whatever made this pair of twisted and spiked black metal boots couldn`t have been good.");
				}
			}
			Ability A=CMClass.getAbility("Prop_HaveZapper");
			if(A!=null)
			{
				A.setMiscText("-GOOD -NEUTRAL -NAMES \"+"+mob.Name()+"\"");
				I.addNonUninvokableEffect(A);
			}
			I.recoverEnvStats();
			if((mob.fetchInventory(null,"$"+I.name()+"$")!=null)
			||(mob.location().fetchItem(null,"$"+I.name()+"$")!=null))
			{
				i++;
				I=null;
				continue;
			}
			break;
		}


		boolean success=proficiencyCheck(mob,0,auto);

		if((success)&&(I!=null))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" to be provided armament!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().addItemRefuse(I,Item.REFUSE_MONSTER_EQ);
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,I.name()+" materializes out of the ground.");
			}
		}
		else
			return beneficialWordsFizzle(mob, null,"<S-NAME> "+prayWord(mob)+" for armament, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
