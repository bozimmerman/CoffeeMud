package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.EnhancedCraftingSkill;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftorType;
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
   Copyright 2003-2025 Bo Zimmerman

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
public class Paladin_CraftHolyAvenger extends EnhancedCraftingSkill
{
	@Override
	public String ID()
	{
		return "Paladin_CraftHolyAvenger";
	}

	private final static String localizedName = CMLib.lang().L("Craft Holy Avenger");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"CRAFTHOLY","CRAFTHOLYAVENGER","CRAFTAVENGER"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public CraftorType getCraftorType()
	{
		return CraftorType.Weapons;
	}

	@Override
	public boolean canBeTaughtBy(final MOB teacher, final MOB student)
	{
		if(!super.canBeTaughtBy(teacher, student))
			return false;
		if(!this.appropriateToMyFactions(student))
		{
			teacher.tell(L("@x1 lacks the moral disposition to learn '@x2'.",student.name(), name()));
			student.tell(L("You lack the moral disposition to learn '@x1'.",name()));
		}
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((buildingI==null)
			||((getRequiredFire(mob,0)==null)
				&&(mob.location()==activityRoom)))
			{
				messedUp=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((buildingI!=null)&&(!aborted))
				{
					if(messedUp)
						commonEmote(mob,L("<S-NAME> mess(es) up crafting @x1.",buildingI.name(mob)));
					else
						mob.location().addItem(buildingI,ItemPossessor.Expire.Player_Drop);
				}
				buildingI=null;
			}
		}
		super.unInvoke();
	}

	protected Item qualifyMask(final Item I)
	{
		if(!(I instanceof Weapon))
			return null;
		/*
		if((I.material() & RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
			return I;
		if((I.material() & RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL)
			return I;
		*/
		return null;
	}

	protected Item bestQualifyMask(final Item I)
	{
		if(qualifyMask(I)==null)
			return null;
		if(I.rawLogicalAnd())
			return I;
		return null;
	}

	protected void addToSet(final Set<Item> set, final Item I)
	{
		if(I!=null)
			set.add(I);
	}

	protected String getItemName(final MOB mob, final Item I)
	{
		return L("the Holy Avenger");
	}

	protected Item getBaseModel(final MOB mob)
	{
		final Deity D = mob.charStats().getMyDeity();
		Item I=null;
		if(D != null)
		{
			final Set<Item> set=new HashSet<Item>();
			addToSet(set, bestQualifyMask(D.fetchWieldedItem()));
			addToSet(set, bestQualifyMask(D.fetchHeldItem()));
			for(final Enumeration<Item> i=D.items();i.hasMoreElements();)
				addToSet(set,bestQualifyMask(i.nextElement()));
			if(set.size()==0)
			{
				addToSet(set, qualifyMask(D.fetchWieldedItem()));
				addToSet(set, qualifyMask(D.fetchHeldItem()));
				for(final Enumeration<Item> i=D.items();i.hasMoreElements();)
					addToSet(set,qualifyMask(i.nextElement()));
			}
			if(set.size()==0)
				set.add(D.fetchWieldedItem());
			if(set.size()>0)
			{
				final List<Item> V = new XVector<Item>(set);
				I=V.get(CMLib.dice().roll(1, V.size(), -1));
				if(I!=null)
					I=(Item)I.copyOf();
			}
		}
		if(I == null)
		{
			I=CMClass.getWeapon("GenWeapon");
			final Weapon w=(Weapon)I;
			w.setWeaponClassification(Weapon.CLASS_SWORD);
			w.setWeaponDamageType(Weapon.TYPE_SLASHING);
			w.setRanges(w.minRange(),1);
			I.setRawLogicalAnd(true);
		}
		return I;
	}

	protected void finishModel(final MOB mob, final Item I, final int woodRequired, final int material)
	{
		final String itemName=getItemName(mob,I);
		I.setName(itemName);
		I.setDisplayText(L("@x1 lies here",itemName));
		I.setDescription(itemName+". ");
		I.basePhyStats().setAbility(5);
		I.basePhyStats().setWeight(woodRequired);
		I.setBaseValue(0);
		I.setMaterial(material);
		I.basePhyStats().setLevel(mob.phyStats().level());
		I.basePhyStats().setDisposition(I.basePhyStats().disposition()|PhyStats.IS_GOOD);
		I.recoverPhyStats();
		I.text();
		I.recoverPhyStats();
	}

	protected void applyItemRestrictions(final Item buildingI)
	{
		Ability A=CMClass.getAbility("Prop_HaveZapper");
		String mask="ACTUAL -CLASS +Paladin ";
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.GOOD))
			mask +="-ALIGNMENT +Good ";
		if(CMLib.factions().isAlignmentLoaded(Faction.Align.LAWFUL))
			mask +="-ALIGNMENT +Lawful ";
		A.setMiscText(mask);
		buildingI.addNonUninvokableEffect(A);
		A=CMClass.getAbility("Prop_Doppleganger");
		A.setMiscText("120%");
		buildingI.addNonUninvokableEffect(A);
		buildingI.basePhyStats().setDisposition(buildingI.basePhyStats().disposition()|PhyStats.IS_GOOD);
		buildingI.phyStats().setDisposition(buildingI.phyStats().disposition()|PhyStats.IS_GOOD);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!PaladinSkill.paladinAlignmentCheck(this, mob, auto))
			return false;

		int completion=16;
		final Item fire=getRequiredFire(mob,0);
		if(fire==null)
			return false;
		final PairVector<EnhancedExpertise,Integer> enhancedTypes=enhancedTypes(mob,commands);
		final int recipeLevel = 1;
		buildingI=null;
		messedUp=false;
		final Item baseI = getBaseModel(mob);
		int woodRequired=baseI.basePhyStats().weight()*2;
		final int[] pm;
		final int defResource;
		switch(baseI.material()&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_CLOTH:
			defResource = RawMaterial.RESOURCE_COTTON;
			pm=new int[]{baseI.material()&RawMaterial.MATERIAL_MASK};
			break;
		case RawMaterial.MATERIAL_LEATHER:
			defResource = RawMaterial.RESOURCE_LEATHER;
			pm=new int[]{baseI.material()&RawMaterial.MATERIAL_MASK};
			break;
		case RawMaterial.MATERIAL_METAL:
		case RawMaterial.MATERIAL_MITHRIL:
			defResource = RawMaterial.MATERIAL_MITHRIL;
			pm=new int[]{RawMaterial.MATERIAL_METAL, RawMaterial.MATERIAL_MITHRIL};
			break;
		case RawMaterial.MATERIAL_WOODEN:
			defResource = RawMaterial.RESOURCE_WOOD;
			pm=new int[]{baseI.material()&RawMaterial.MATERIAL_MASK};
			break;
		case RawMaterial.MATERIAL_ROCK:
			if((baseI.material()==RawMaterial.RESOURCE_BONE)||(baseI.material()==RawMaterial.RESOURCE_IVORY))
			{
				defResource=baseI.material();
				pm=new int[]{RawMaterial.RESOURCE_BONE, RawMaterial.RESOURCE_IVORY};
			}
			else
			{
				defResource = RawMaterial.RESOURCE_STONE;
				pm=new int[]{baseI.material()&RawMaterial.MATERIAL_MASK};
			}
			break;
		default:
			defResource = RawMaterial.MATERIAL_MITHRIL;
			pm=new int[]{RawMaterial.MATERIAL_METAL, RawMaterial.MATERIAL_MITHRIL};
			break;
		}
		final String typeName = RawMaterial.CODES.MAT_DESC(pm[0]).toLowerCase().trim();
		final int[][] data=fetchFoundResourceData(mob,
												  woodRequired,typeName,pm,
												  0,null,null,
												  false,
												  auto?defResource:0,
												  enhancedTypes);
		if(data==null)
		{
			baseI.destroy();
			return false;
		}
		woodRequired=data[0][FOUND_AMT];

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
		{
			baseI.destroy();
			return false;
		}
		if(!auto)
			CMLib.materials().destroyResourcesValue(mob.location(),woodRequired,data[0][FOUND_CODE],data[0][FOUND_SUB],0,0);
		buildingI = baseI;
		finishModel(mob, buildingI, woodRequired, data[0][FOUND_CODE]);
		applyItemRestrictions(buildingI);
		buildingI.recoverPhyStats();
		buildingI.text();
		buildingI.recoverPhyStats();

		messedUp=!proficiencyCheck(mob,0,auto);
		completion=50-CMLib.ableMapper().qualifyingClassLevel(mob,this);
		if(completion<6)
			completion=6;
		final String startStr=L("<S-NAME> start(s) crafting @x1.",buildingI.name());
		displayText=L("You are crafting @x1",buildingI.name());
		verb=L("crafting @x1",buildingI.name());
		final CMMsg msg=CMClass.getMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,completion);
			enhanceItem(mob,buildingI,recipeLevel,enhancedTypes);
		}
		return true;
	}
}
