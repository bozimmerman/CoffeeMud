package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.ItemKeyPair;
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
   Copyright 2004-2020 Bo Zimmerman

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
public class OutFit extends StdCommand
{
	public OutFit()
	{
	}

	private final String[] access=I(new String[]{"OUTFIT"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean preExecute(final MOB mob, final List<String> commands, final int metaFlags, final int secondsElapsed, final double actionsRemaining)
	throws java.io.IOException
	{
		if(secondsElapsed>8.0)
			mob.tell(L("You feel your outfit plea is almost answered."));
		else
		if(secondsElapsed>4.0)
			mob.tell(L("Your plea swirls around you."));
		else
		if(actionsRemaining>0.0)
			mob.tell(L("You invoke a plea for mystical outfitting and await the answer."));
		return true;
	}

	protected Item findArmorWinner(final MOB mob, final List<String> useSkills, final long wornCode, final int material,
									final long positionsFound, final Map<String,List<ItemKeyPair>> cache)
	{
		final int mlvl=mob.phyStats().level();
		Item winnerW=null;
		int bestArmor=Integer.MIN_VALUE;
		for(final String skillID : useSkills)
		{
			final ItemCraftor cA=(ItemCraftor)CMClass.getAbility(skillID);
			List<ItemKeyPair> set = cache.get(skillID);
			if(set == null)
			{
				set = new XVector<ItemKeyPair>(cA.craftAllItemSets(material, true));
				for(final Iterator<ItemKeyPair> i=set.iterator();i.hasNext();)
				{
					final ItemKeyPair p=i.next();
					if((!(p.item instanceof Armor))
					||(p.item.fetchEffect("Prop_WearOverride")!=null))
						i.remove();
				}
				cache.put(skillID, set);
			}
			for(final ItemKeyPair p : set)
			{
				if(((((Armor)p.item).rawProperLocationBitmap()&wornCode)>0)
				&&((!p.item.rawLogicalAnd())
					|| ((positionsFound&((Armor)p.item).rawProperLocationBitmap())==0)))
				{
					final int ilvl=p.item.phyStats().level();
					if((ilvl<=mlvl)
					&&(p.item.phyStats().armor()-(mlvl-ilvl)>bestArmor))
					{
						winnerW=p.item;
						bestArmor=p.item.phyStats().armor()-(mlvl-ilvl);
					}
				}
			}
		}
		return winnerW;
	}

	protected List<Item> metacraftArmorsFor(final MOB mob)
	{
		final CharClass C=mob.charStats().getCurrentClass();
		if(C==null)
			return null;
		final List<String> clothSkills = new ArrayList<String>();
		final Map<String,List<ItemKeyPair>> armorCache = new HashMap<String,List<ItemKeyPair>>();
		clothSkills.add("Tailoring");
		clothSkills.add("MasterTailoring");
		List<String> useSkills = new ArrayList<String>();
		int material=RawMaterial.RESOURCE_IRON;
		switch(C.allowedArmorLevel())
		{
		case CharClass.ARMOR_OREONLY:
			//$FALL-THROUGH$
		case CharClass.ARMOR_ANY:
		case CharClass.ARMOR_METALONLY:
			useSkills.add("Armorsmithing");
			useSkills.add("MasterArmorsmithing");
			//useSkills.add("LegendaryArmorsmithing");
			break;
		case CharClass.ARMOR_VEGAN:
			//$FALL-THROUGH$
		case CharClass.ARMOR_CLOTH:
			useSkills=clothSkills;
			material=RawMaterial.RESOURCE_COTTON;
			break;
		case CharClass.ARMOR_NONMETAL:
			//$FALL-THROUGH$
		case CharClass.ARMOR_LEATHER:
			useSkills.add("LeatherWorking");
			useSkills.add("MasterLeatherWorking");
			//useSkills.add("LegendaryLeatherWorking");
			material=RawMaterial.RESOURCE_LEATHER;
			break;
		}
		final List<Item> finalArmors = new ArrayList<Item>();
		long positionsFound=0;
		for(final long wornCode : Wearable.CODES.ALL())
		{
			if((wornCode == Wearable.IN_INVENTORY)
			|| (wornCode == Wearable.WORN_HELD)
			|| (wornCode == Wearable.WORN_FLOATING_NEARBY)
			|| (wornCode == Wearable.WORN_MOUTH))
				 continue;
			final Item wornI=mob.fetchFirstWornItem(wornCode);
			if(wornI!=null)
			{
				if(wornI.rawLogicalAnd())
					positionsFound |= wornI.rawProperLocationBitmap();
				else
					positionsFound |= wornCode;
			}
		}
		for(final long wornCode : Wearable.CODES.ALL())
		{
			if((wornCode == Wearable.IN_INVENTORY)
			|| (wornCode == Wearable.WORN_HELD)
			|| (wornCode == Wearable.WORN_FLOATING_NEARBY)
			|| (wornCode == Wearable.WORN_MOUTH))
				 continue;
			if((positionsFound&wornCode)==0)
			{
				Item armorI=findArmorWinner(mob,useSkills,wornCode,material,positionsFound,armorCache);
				if((armorI==null)&&(useSkills!=clothSkills))
					armorI=findArmorWinner(mob,useSkills,wornCode,RawMaterial.RESOURCE_COTTON,positionsFound,armorCache);
				if(armorI!=null)
				{
					if(armorI.rawLogicalAnd())
						positionsFound |= armorI.rawProperLocationBitmap();
					else
						positionsFound |= wornCode;
					finalArmors.add(armorI);
				}
			}
		}
		for(final String key : armorCache.keySet())
		{
			final List<ItemKeyPair> ps = armorCache.get(key);
			for(final ItemKeyPair p : ps)
			{
				if(p.key!=null)
					p.key.destroy();
				if(!finalArmors.contains(p.item))
					p.item.destroy();
			}
		}
		return finalArmors;
	}

	protected Item metacraftWeaponFor(final MOB mob)
	{
		if(mob.fetchWieldedItem()!=null)
			return null;
		final CharClass C=mob.charStats().getCurrentClass();
		if(C==null)
			return null;
		final int mlvl=mob.phyStats().level();
		final List<String> useSkills = new ArrayList<String>();
		int reqWeaponClass=Weapon.CLASS_SWORD;
		int material=RawMaterial.RESOURCE_IRON;
		switch(C.allowedWeaponLevel())
		{
		case CharClass.WEAPONS_FLAILONLY:
			reqWeaponClass=Weapon.CLASS_FLAILED;
			useSkills.add("Weaponsmithing");
			useSkills.add("MasterWeaponsmithing");
			useSkills.add("LegendaryWeaponsmithing");
			break;
		case CharClass.WEAPONS_ALLCLERIC:
			useSkills.add("Weaponsmithing");
			useSkills.add("MasterWeaponsmithing");
			useSkills.add("LegendaryWeaponsmithing");
			if(CMLib.flags().isGood(mob))
				reqWeaponClass=Weapon.CLASS_BLUNT;
			break;
		case CharClass.WEAPONS_GOODCLERIC:
			reqWeaponClass=Weapon.CLASS_BLUNT;
			useSkills.add("Weaponsmithing");
			useSkills.add("MasterWeaponsmithing");
			useSkills.add("LegendaryWeaponsmithing");
			break;
		case CharClass.WEAPONS_DAGGERONLY:
			reqWeaponClass=Weapon.CLASS_DAGGER;
			useSkills.add("Weaponsmithing");
			useSkills.add("MasterWeaponsmithing");
			useSkills.add("LegendaryWeaponsmithing");
			break;
		case CharClass.WEAPONS_ROCKY:
			//$FALL-THROUGH$
		case CharClass.WEAPONS_EVILCLERIC:
		case CharClass.WEAPONS_THIEFLIKE:
		case CharClass.WEAPONS_BURGLAR:
		case CharClass.WEAPONS_ANY:
		case CharClass.WEAPONS_NEUTRALCLERIC:
			useSkills.add("Weaponsmithing");
			useSkills.add("MasterWeaponsmithing");
			useSkills.add("LegendaryWeaponsmithing");
			break;
		case CharClass.WEAPONS_MAGELIKE:
			//$FALL-THROUGH$
		case CharClass.WEAPONS_NATURAL:
			reqWeaponClass=Weapon.CLASS_STAFF;
			material=RawMaterial.RESOURCE_OAK;
			useSkills.add("Carpentry");
			break;
		}
		Item winnerW=null;
		int closestDiff=Integer.MAX_VALUE;
		for(final String skillID : useSkills)
		{
			final ItemCraftor cA=(ItemCraftor)CMClass.getAbility(skillID);
			final List<ItemKeyPair> set = cA.craftAllItemSets(material, true);
			for(final ItemKeyPair p : set)
			{
				if((p.item instanceof Weapon)
				&&(((Weapon)p.item).weaponClassification() == reqWeaponClass))
				{
					final int ilvl=p.item.phyStats().level();
					if((ilvl<=mlvl)
					&&((mlvl-ilvl)<closestDiff))
					{
						if(winnerW!=null)
							winnerW.destroy();
						winnerW=p.item;
						closestDiff=mlvl-ilvl;
					}
					else
						p.item.destroy();
				}
				if(p.key!=null)
					p.key.destroy();
			}
		}
		return winnerW;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		if(mob==null)
			return false;
		if(mob.charStats()==null)
			return false;
		final Vector<String> origCmds=new XVector<String>(commands);
		final MOB targetM;
		int eqLevel=1;
		if(commands.size()>1)
		{
			int newLevel=0;
			if((commands.size()>2) && (CMath.isInteger(commands.get(1))))
				newLevel=CMath.s_int(commands.remove(1));
			final String possName=CMParms.combine(commands,1);
			MOB M=mob.location().fetchInhabitant(possName);
			if(M==null)
				M=CMLib.players().findPlayerOnline(possName, true);
			if(M!=null)
			{
				if((M.isPlayer()) && (!CMSecurity.isAllowed(mob, mob.location(),CMSecurity.SecFlag.CMDPLAYERS)))
				{
					CMLib.commands().postCommandFail(mob,origCmds,L("Not allowed."));
					return false;
				}
				if((!M.isPlayer()) && (!CMSecurity.isAllowed(mob, mob.location(),CMSecurity.SecFlag.CMDMOBS)))
				{
					CMLib.commands().postCommandFail(mob,origCmds,L("Not allowed."));
					return false;
				}
				eqLevel=newLevel;
				targetM=M;
			}
			else
			{
				if((!CMSecurity.isAllowed(mob, mob.location(),CMSecurity.SecFlag.CMDPLAYERS))
				&&(!CMSecurity.isAllowed(mob, mob.location(),CMSecurity.SecFlag.CMDMOBS)))
					targetM=mob;
				else
				{
					CMLib.commands().postCommandFail(mob,origCmds,L("No one named @x1 found.",possName));
					return false;
				}
			}
		}
		else
			targetM=mob;
		if(eqLevel==1)
		{
			final List<List<Item>> outfits = new ArrayList<List<Item>>();
			final CharClass C=targetM.charStats().getCurrentClass();
			if(C!=null)
				outfits.add(C.outfit(targetM));
			final Race R=targetM.charStats().getMyRace();
			if(R!=null)
				outfits.add(R.outfit(targetM));
			for(final List<Item> outfit : outfits)
				CMLib.utensils().outfit(targetM, outfit);
		}
		else
		{
			final List<Item> outfit = new ArrayList<Item>();
			final Item weapon=this.metacraftWeaponFor(targetM);
			if(weapon != null)
				outfit.add(weapon);
			outfit.addAll(this.metacraftArmorsFor(targetM));
			CMLib.utensils().outfit(targetM, outfit);
			for(final Item o : outfit)
				o.destroy();
		}
		mob.tell(L("\n\r"));
		final Command C2=CMClass.getCommand("Equipment");
		if(C2!=null)
			C2.executeInternal(targetM, metaFlags);
		targetM.tell(L("\n\rUseful equipment appears mysteriously out of the Java Plane."));
		targetM.recoverCharStats();
		targetM.recoverMaxState();
		targetM.recoverPhyStats();
		if(mob != targetM)
		{
			final CMMsg msg=CMClass.getMsg(mob,targetM,null,CMMsg.MSG_LOOK,null,CMMsg.MSG_LOOK,null,CMMsg.NO_EFFECT,null);
			targetM.executeMsg(mob, msg);
		}
		return false;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID(),CMath.div(CMProps.getIntVar(CMProps.Int.DEFCOMCMDTIME),25.0));
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		if((!CMSecurity.isAllowed(mob, mob.location(),CMSecurity.SecFlag.CMDPLAYERS))
		&&(!CMSecurity.isAllowed(mob, mob.location(),CMSecurity.SecFlag.CMDMOBS)))
			return CMProps.getCommandActionCost(ID(),CMath.div(CMProps.getIntVar(CMProps.Int.DEFCMDTIME),25.0));
		else
			return 0.0;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
