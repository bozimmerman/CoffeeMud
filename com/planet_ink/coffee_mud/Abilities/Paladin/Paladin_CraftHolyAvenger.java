package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.Common.EnhancedCraftingSkill;
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
public class Paladin_CraftHolyAvenger extends EnhancedCraftingSkill
{
	public String ID() { return "Paladin_CraftHolyAvenger"; }
	public String name(){ return "Craft Holy Avenger";}
	private static final String[] triggerStrings = {"CRAFTHOLY","CRAFTHOLYAVENGER","CRAFTAVENGER"};
	public String[] triggerStrings(){return triggerStrings;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if((building==null)
			||(getRequiredFire(mob,0)==null))
			{
				messedUp=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
						commonEmote(mob,"<S-NAME> mess(es) up crafting the Holy Avenger.");
					else
						mob.location().addItemRefuse(building,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
				}
				building=null;
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		int completion=16;
		Item fire=getRequiredFire(mob,0);
		if(fire==null) return false;
		DVector enhancedTypes=enhancedTypes(mob,commands);
		building=null;
		messedUp=false;
		int woodRequired=50;
		int[] pm={RawMaterial.MATERIAL_METAL,RawMaterial.MATERIAL_MITHRIL};
		int[][] data=fetchFoundResourceData(mob,
											woodRequired,"metal",pm,
											0,null,null,
											false,
											auto?RawMaterial.RESOURCE_MITHRIL:0,
											enhancedTypes);
		if(data==null) return false;
		woodRequired=data[0][FOUND_AMT];

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
        if(!auto)
            CMLib.materials().destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],0,null);
		building=CMClass.getWeapon("GenWeapon");
		completion=50-CMLib.ableMapper().qualifyingClassLevel(mob,this);
		String itemName="the Holy Avenger";
		building.setName(itemName);
		String startStr="<S-NAME> start(s) crafting "+building.name()+".";
		displayText="You are crafting "+building.name();
		verb="crafting "+building.name();
		building.setDisplayText(itemName+" lies here");
		building.setDescription(itemName+". ");
		building.baseEnvStats().setWeight(woodRequired);
		building.setBaseValue(0);
		building.setMaterial(data[0][FOUND_CODE]);
		building.baseEnvStats().setLevel(mob.envStats().level());
		building.baseEnvStats().setAbility(5);
		Weapon w=(Weapon)building;
		w.setWeaponClassification(Weapon.CLASS_SWORD);
		w.setWeaponType(Weapon.TYPE_SLASHING);
		w.setRanges(w.minRange(),1);
		building.setRawLogicalAnd(true);
		Ability A=CMClass.getAbility("Prop_HaveZapper");
		A.setMiscText("-CLASS +Paladin -ALIGNMENT +Good");
		building.addNonUninvokableEffect(A);
		A=CMClass.getAbility("Prop_Doppleganger");
		A.setMiscText("120%");
		building.addNonUninvokableEffect(A);

		building.recoverEnvStats();
		building.text();
		building.recoverEnvStats();

		messedUp=!proficiencyCheck(mob,0,auto);
		if(completion<6) completion=6;
		CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,completion);
			enhanceItem(mob,building,enhancedTypes);
		}
		return true;
	}
}
