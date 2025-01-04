package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

import java.io.IOException;
import java.util.*;
import java.util.regex.*;

/*
   Copyright 2006-2025 Bo Zimmerman

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
public class RawCMaterial extends StdLibrary implements MaterialLibrary
{
	@Override
	public String ID()
	{
		return "RawCMaterial";
	}

	@Override
	public int getRandomResourceOfMaterial(int material)
	{
		material=material&RawMaterial.MATERIAL_MASK;
		if((material<0)||(material>=RawMaterial.Material.values().length))
			return -1;
		final RawMaterial.CODES codes = RawMaterial.CODES.instance();
		final PairList<Integer, Double> rscs = codes.getValueSortedResources(material);
		if((rscs == null)||(rscs.size()==0))
			return -1;
		final int rscIndex=CMLib.dice().roll(1,rscs.size(),-1);
		return rscs.getFirst(rscIndex).intValue();
	}

	@Override
	public boolean quickDestroy(final Item I)
	{
		if(I==null)
			return false;
		final ItemPossessor O=I.owner();
		if(O instanceof MOB)
			((MOB)O).delItem(I);
		else
		if(O instanceof Room)
			((Room)O).delItem(I);
		I.setOwner(null);
		I.destroy();
		return true;
	}

	@Override
	public boolean rebundle(final Item item)
	{
		if((item==null)||(item.amDestroyed()))
			return false;
		final List<Item> found=new ArrayList<Item>();
		found.add(item);
		Item I=null;
		final Environmental owner=item.owner();
		long lowestNonZeroFoodNumber=Long.MAX_VALUE;
		final String subType=(item instanceof RawMaterial)?((RawMaterial)item).getSubType():"";
		if(subType.equals(RawMaterial.ResourceSubType.SEED.name()))
			return false;
		final CMFlagLibrary flags = CMLib.flags();
		if(owner instanceof Room)
		{
			final Room R=(Room)owner;
			for(int i=0;i<R.numItems();i++)
			{
				I=R.getItem(i);
				if((I instanceof RawMaterial)
				&&(I.material()==item.material())
				&&(I!=item)
				&&(I.container()==item.container())
				&&(((RawMaterial)I).getSubType().equals(subType))
				&&(I.rawSecretIdentity().equals(item.rawSecretIdentity()))
				&&(!flags.isOnFire(I))
				&&(!flags.isEnchanted(I)))
					found.add(I);
			}
		}
		else
		if(owner instanceof MOB)
		{
			final MOB M=(MOB)owner;
			for(int i=0;i<M.numItems();i++)
			{
				I=M.getItem(i);
				if((I instanceof RawMaterial)
				&&(I.material()==item.material())
				&&(I!=item)
				&&(I.container()==item.container())
				&&(((RawMaterial)I).getSubType().equals(subType))
				&&(I.rawSecretIdentity().equals(item.rawSecretIdentity()))
				&&(!flags.isOnFire(I))
				&&(!flags.isEnchanted(I)))
					found.add(I);
			}
		}
		else
			return false;
		if(found.size()<2)
			return false;
		Item bundle=null;
		int maxFound=1;
		int totalWeight=0;
		int totalValue=0;
		int totalNourishment=0;
		int totalThirstHeld=0;
		int totalThirstRemain=0;
		for(int i=0;i<found.size();i++)
		{
			I=found.get(i);
			final int weight=I.basePhyStats().weight();
			totalWeight+=weight;
			totalValue+=I.baseGoldValue();
			if(weight>maxFound)
			{
				maxFound=weight;
				bundle=I;
			}
			if((I instanceof Decayable)
			&&(((Decayable)I).decayTime()>0)
			&&(((Decayable)I).decayTime()<lowestNonZeroFoodNumber))
				lowestNonZeroFoodNumber=((Decayable)I).decayTime();
			if(I instanceof Food)
				totalNourishment+=((Food)I).nourishment();
			if(I instanceof Drink)
			{
				totalThirstHeld+=((Drink)I).liquidHeld();
				totalThirstRemain+=((Drink)I).liquidRemaining();
			}
		}
		if(bundle==null)
			bundle=item;
		found.remove(bundle);
		if(lowestNonZeroFoodNumber==Long.MAX_VALUE)
			lowestNonZeroFoodNumber=0;
		final Map<String,Ability> foundAblesH=new HashMap<String,Ability>();
		Ability A=null;
		for(int i=0;i<found.size();i++)
		{
			I=found.get(i);
			for(final Enumeration<Ability> a=I.effects();a.hasMoreElements();)
			{
				A=a.nextElement();
				if((A!=null)
				&&(!A.canBeUninvoked())
				&&(!foundAblesH.containsKey(A.ID())))
					foundAblesH.put(A.ID(),A);
			}
		}
		bundle.basePhyStats().setWeight(totalWeight);
		bundle.setBaseValue(totalValue);
		adjustResourceName(bundle);
		if(bundle instanceof Food)
			((Food)bundle).setNourishment(totalNourishment);
		if(bundle instanceof Drink)
		{
			((Drink)bundle).setLiquidHeld(totalThirstHeld);
			((Drink)bundle).setLiquidRemaining(totalThirstRemain);
		}
		if(bundle instanceof Decayable)
			((Decayable)bundle).setDecayTime(lowestNonZeroFoodNumber);
		for(final String key  : foundAblesH.keySet())
		{
			A=foundAblesH.get(key);
			if(bundle.fetchEffect(A.ID())==null)
				bundle.addNonUninvokableEffect((Ability)A.copyOf());
		}
		for(int i=0;i<found.size();i++)
			((RawMaterial)found.get(i)).quickDestroy();
		if((owner instanceof Room)
		&&(((Room)owner).numItems()>0)
		&&(((Room)owner).getItem(((Room)owner).numItems()-1)!=bundle))
		{
			final Container C=bundle.container();
			((Room)owner).delItem(bundle);
			((Room)owner).moveItemTo(bundle,ItemPossessor.Expire.Player_Drop);
			bundle.setContainer(C);
		}
		if((owner instanceof MOB)
		&&(((MOB)owner).numItems()>0)
		&&(((MOB)owner).getItem(((MOB)owner).numItems()-1)!=bundle))
		{
			final Container C=bundle.container();
			((MOB)owner).delItem(bundle);
			((MOB)owner).moveItemTo(bundle);
			bundle.setContainer(C);
		}
		final Room R=CMLib.map().roomLocation(bundle);
		if(R!=null)
			R.recoverRoomStats();
		return true;
	}

	@Override
	public Item splitBundle(final Item I, final int size, final Container C)
	{
		final List<Item> set=disBundle(I,1,size,C);
		if((set==null)||(set.size()==0))
			return null;
		return set.get(0);
	}

	@Override
	public Item unbundle(final Item I, final int number, final Container C)
	{
		final List<Item> set=disBundle(I,number,1,C);
		if((set==null)||(set.size()==0))
			return null;
		return set.get(0);
	}

	protected List<Item> disBundle(Item I, int number, final int bundleSize, final Container C)
	{
		if((I==null)||(I.amDestroyed())||(bundleSize<1))
			return null;
		if((I instanceof PackagedItems)
		&&(I.container()==C)
		&&(!CMLib.flags().isOnFire(I)))
		{
			final PackagedItems pkg=(PackagedItems)I;
			if(number<=0)
				number=pkg.numberOfItemsInPackage();
			if(number<=0)
				number=1;
			if(number>pkg.numberOfItemsInPackage())
				number=pkg.numberOfItemsInPackage();
			final Environmental owner=I.owner();
			final List<Item> parts=((PackagedItems)I).unPackage(number);
			if(parts.size()==0)
				return new XVector<Item>(I);
			final List<Item> bundle=new XVector<Item>();
			for(int p=0;p<parts.size();p+=bundleSize)
			{
				I=parts.get(p);
				if(bundleSize>1)
				{
					final PackagedItems thePackage=(PackagedItems)CMClass.getItem(pkg.ID());
					thePackage.packageMe(I, bundleSize);
					for(int pp=p;(pp<p+bundleSize) && (pp<parts.size());pp++)
						parts.get(pp).destroy();
					I=thePackage;
				}
				if(owner instanceof Room)
					((Room)owner).addItem(I,ItemPossessor.Expire.Player_Drop);
				else
				if(owner instanceof MOB)
					((MOB)owner).addItem(I);
				I.setContainer(C);
				bundle.add(I);
			}
			if(!pkg.amDestroyed())
			{
				if(owner instanceof Room)
				{
					((Room)owner).delItem(pkg);
					((Room)owner).addItem(pkg,ItemPossessor.Expire.Player_Drop);
					bundle.add(pkg);
				}
				else
				if(owner instanceof MOB)
				{
					((MOB)owner).delItem(pkg);
					((MOB)owner).addItem(pkg);
					bundle.add(pkg);
				}
				else
					pkg.destroy();
			}
			return bundle;
		}
		else
		if((I instanceof RawMaterial)
		&&(I.container()==C)
		&&(!CMLib.flags().isOnFire(I))
		&&(!CMLib.flags().isABonusItems(I))
		&&(!CMLib.flags().isEnchanted(I)))
		{
			final Ability rott=I.fetchEffect("Poison_Rotten");
			final Ability purt=I.fetchEffect("Poison_Purify");
			number = number * bundleSize;
			if(I.basePhyStats().weight()>1)
			{
				final Environmental owner=I.owner();
				if(number<=0)
					number=I.basePhyStats().weight();
				if(number<=0)
					number=1;
				if(number>I.basePhyStats().weight())
					number=I.basePhyStats().weight();
				final List<Item> bundle=new XVector<Item>();
				if(bundleSize >= I.basePhyStats().weight())
				{
					bundle.add(I);
					return bundle;
				}
				I.basePhyStats().setWeight(I.basePhyStats().weight()); // wtf?
				int loseValue=0;
				int loseNourishment=0;
				int loseThirstHeld=0;
				int loseThirstRemain=0;
				Item E=null;
				for(int x=0;x<number;x+=bundleSize)
				{
					E=makeItemResource(I.material(),null,true,I.rawSecretIdentity(), ((RawMaterial)I).getSubType());
					if(E instanceof Item)
					{
						E.setContainer(C);
						loseValue+=E.baseGoldValue();
						for(final Enumeration<Ability> a=I.effects();a.hasMoreElements();)
						{
							final Ability A=a.nextElement();
							if((!A.canBeUninvoked())
							&&(!A.ID().equalsIgnoreCase("Poison_Rotten"))
							&&(!A.ID().equalsIgnoreCase("Poison_Purify")))
							{
								final Ability A2=(Ability)A.copyOf();
								E.addNonUninvokableEffect(A2);
							}
						}
						if((E instanceof RawMaterial)
						&&(I instanceof RawMaterial))
							((RawMaterial)E).setSubType(((RawMaterial)I).getSubType());
						if((E instanceof Decayable)
						&&(I instanceof Decayable))
							((Decayable)E).setDecayTime(((Decayable)I).decayTime());
						if((E instanceof Food)
						&&(I instanceof Food))
							loseNourishment+=((Food)E).nourishment();
						if((E instanceof Drink)
						&&(I instanceof Drink))
						{
							loseThirstHeld+=((Drink)E).liquidHeld();
							loseThirstRemain+=((Drink)E).liquidRemaining();
						}
						if((rott!=null)
						&&(!rott.canBeUninvoked())
						&&(!CMSecurity.isDisabled(DisFlag.FOODROT)))
							E.addNonUninvokableEffect((Ability)rott.copyOf());
						if((purt!=null)
						&&(!purt.canBeUninvoked())
						&&(!CMSecurity.isDisabled(DisFlag.FOODROT)))
							E.addNonUninvokableEffect((Ability)purt.copyOf());
						if(bundleSize>1)
						{
							E.basePhyStats().setWeight(bundleSize);
							E.phyStats().setWeight(bundleSize);
							adjustResourceName(E);
						}
						if(owner instanceof Room)
						{
							((Room)owner).addItem(E,ItemPossessor.Expire.Player_Drop);
							bundle.add(E);
						}
						else
						if(owner instanceof MOB)
						{
							((MOB)owner).addItem(E);
							bundle.add(E);
						}
					}
					else
						E=null;
				}
				if((I.basePhyStats().weight()-number)>0)
				{
					I.basePhyStats().setWeight(I.basePhyStats().weight()-number);
					I.setBaseValue(I.baseGoldValue()-loseValue);
					this.adjustResourceName(I);
					if(I instanceof Food)
					{
						((Food)I).setNourishment(((Food)I).nourishment()-loseNourishment);
						if(((Food)I).nourishment()<=0)
							((Food)I).setNourishment(0);
					}
					if(I instanceof Drink)
					{
						((Drink)I).setLiquidHeld(((Drink)I).liquidHeld()-loseThirstHeld);
						if(((Drink)I).liquidHeld()<=0)
							((Drink)I).setLiquidHeld(0);
						((Drink)I).setLiquidRemaining(((Drink)I).liquidRemaining()-loseThirstRemain);
						if(((Drink)I).liquidRemaining()<=0)
							((Drink)I).setLiquidRemaining(0);
					}
					I.recoverPhyStats();
					// now move it to the end!
					if(owner instanceof Room)
					{
						((Room)owner).delItem(I);
						((Room)owner).addItem(I,ItemPossessor.Expire.Player_Drop);
						bundle.add(I);
					}
					else
					if(owner instanceof MOB)
					{
						((MOB)owner).delItem(I);
						((MOB)owner).addItem(I);
						bundle.add(I);
					}
					else
						I.destroy();
				}
				else
					I.destroy();
				return bundle;
			}
			else
				return new XVector<Item>(I);
		}
		return null;
	}

	@Override
	public String getMaterialDesc(final int materialCode)
	{
		final RawMaterial.Material m=RawMaterial.Material.findByMask(materialCode);
		if(m!=null)
			return m.desc();
		return "";
	}

	@Override
	public String getResourceDesc(final int resourceCode)
	{
		if(RawMaterial.CODES.IS_VALID(resourceCode))
			return RawMaterial.CODES.NAME(resourceCode);
		return "";
	}

	@Override
	public int findMaterialRelativeInt(final String s)
	{
		final RawMaterial.Material m=RawMaterial.Material.findIgnoreCase(s);
		if(m!=null)
			return m.ordinal();
		return -1;
	}

	@Override
	public int findMaterialCode(String s, final boolean exact)
	{
		RawMaterial.Material m=RawMaterial.Material.findIgnoreCase(s);
		if(m!=null)
			return m.mask();
		if(exact)
			return -1;
		s=s.toUpperCase();
		m=RawMaterial.Material.startsWith(s);
		if(m!=null)
			return m.mask();
		return -1;
	}

	@Override
	public int findResourceCode(String s, final boolean exact)
	{
		int code = RawMaterial.CODES.FIND_IgnoreCase(s);
		if(code>=0)
			return code;
		if(exact)
			return -1;
		s=s.toUpperCase();
		code = RawMaterial.CODES.FIND_StartsWith(s);
		return code;
	}

	@Override
	public void addEffectsToResource(final Item I)
	{
		if(I==null)
			return;
		final Ability[] As=RawMaterial.CODES.EFFECTA(I.material());
		if((As==null)||(As.length==0))
			return;
		for(final Ability A : As)
		{
			if(I.fetchEffect(A.ID())==null)
				I.addNonUninvokableEffect((Ability)A.copyOf());
		}
	}

	protected MOB makeMOBResource(final int myResource)
	{
		if(myResource<0)
			return null;
		switch(myResource)
		{
		case RawMaterial.RESOURCE_MUTTON:
		case RawMaterial.RESOURCE_WOOL:
			return CMClass.getMOB("Sheep");
		case RawMaterial.RESOURCE_LEATHER:
			switch(CMLib.dice().roll(1,10,0))
			{
			case 1:
			case 2:
			case 3:
				return CMClass.getMOB("Cow");
			case 4:
				return CMClass.getMOB("Bull");
			case 5:
			case 6:
			case 7:
				return CMClass.getMOB("Doe");
			case 8:
			case 9:
			case 10:
				return CMClass.getMOB("Buck");
			}
			break;
		case RawMaterial.RESOURCE_HIDE:
			switch(CMLib.dice().roll(1,10,0))
			{
			case 1:
			case 2:
				return CMClass.getMOB("Gorilla");
			case 3:
				return CMClass.getMOB("Lion");
			case 4:
				return CMClass.getMOB("Cheetah");
			case 5:
			case 6:
				return CMClass.getMOB("Ape");
			case 7:
			case 8:
				return CMClass.getMOB("Fox");
			case 9:
			case 10:
				return CMClass.getMOB("Monkey");
			}
			break;
		case RawMaterial.RESOURCE_PORK:
			return CMClass.getMOB("Pig");
		case RawMaterial.RESOURCE_FUR:
		case RawMaterial.RESOURCE_MEAT:
			switch(CMLib.dice().roll(1,10,0))
			{
			case 1:
			case 2:
			case 3:
			case 4:
				return CMClass.getMOB("Wolf");
			case 5:
			case 6:
			case 7:
				return CMClass.getMOB("Buffalo");
			case 8:
			case 9:
				return CMClass.getMOB("BrownBear");
			case 10:
				return CMClass.getMOB("BlackBear");
			}
			break;
		case RawMaterial.RESOURCE_SCALES:
			switch(CMLib.dice().roll(1,10,0))
			{
			case 1:
			case 2:
			case 3:
			case 4:
				return CMClass.getMOB("Lizard");
			case 5:
			case 6:
			case 7:
				return CMClass.getMOB("GardenSnake");
			case 8:
			case 9:
				return CMClass.getMOB("Cobra");
			case 10:
				return CMClass.getMOB("Python");
			}
			break;
		case RawMaterial.RESOURCE_POULTRY:
		case RawMaterial.RESOURCE_EGGS:
			return CMClass.getMOB("Chicken");
		case RawMaterial.RESOURCE_BEEF:
			switch(CMLib.dice().roll(1,5,0))
			{
			case 1:
			case 2:
			case 3:
			case 4:
				return CMClass.getMOB("Cow");
			case 5:
				return CMClass.getMOB("Bull");
			}
			break;
		case RawMaterial.RESOURCE_FEATHERS:
			switch(CMLib.dice().roll(1,4,0))
			{
			case 1:
				return CMClass.getMOB("WildEagle");
			case 2:
				return CMClass.getMOB("Falcon");
			case 3:
				return CMClass.getMOB("Chicken");
			case 4:
				return CMClass.getMOB("Parakeet");
			}
			break;
		}
		return null;
	}

	protected Item makeItemResource(final int myResource, final String localeCode, final boolean noAnimals, final String fullName, final String subType)
	{
		RawMaterial I=null;
		int material=(myResource&RawMaterial.MATERIAL_MASK);
		if(!noAnimals)
		{
			if((myResource==RawMaterial.RESOURCE_WOOL)
			||(myResource==RawMaterial.RESOURCE_FEATHERS)
			||(myResource==RawMaterial.RESOURCE_SCALES)
			||(myResource==RawMaterial.RESOURCE_HIDE)
			||(myResource==RawMaterial.RESOURCE_FUR))
			   material=RawMaterial.MATERIAL_LEATHER;
			if(CMParms.contains(RawMaterial.CODES.FISHES(), myResource))
				material=RawMaterial.MATERIAL_VEGETATION;
		}
		switch(material)
		{
		case RawMaterial.MATERIAL_FLESH:
			I=(RawMaterial)CMClass.getItem("GenFoodResource");
			break;
		case RawMaterial.MATERIAL_VEGETATION:
		{
			if((myResource==RawMaterial.RESOURCE_VINE)
			||(myResource==RawMaterial.RESOURCE_FLOWERS))
				I=(RawMaterial)CMClass.getItem("GenResource");
			else
			{
				I=(RawMaterial)CMClass.getItem("GenFoodResource");
				if(myResource==RawMaterial.RESOURCE_HERBS)
					((Food)I).setNourishment(1);
			}
			break;
		}
		case RawMaterial.MATERIAL_GAS:
		case 0:
		{
			//TODO: are gasses and nothing really liquids?
			I=(RawMaterial)CMClass.getItem("GenLiquidResource");
			break;
		}
		case RawMaterial.MATERIAL_LIQUID:
		case RawMaterial.MATERIAL_ENERGY:
		{
			I=(RawMaterial)CMClass.getItem("GenLiquidResource");
			break;
		}
		case RawMaterial.MATERIAL_LEATHER:
		case RawMaterial.MATERIAL_CLOTH:
		case RawMaterial.MATERIAL_PAPER:
		case RawMaterial.MATERIAL_WOODEN:
		case RawMaterial.MATERIAL_GLASS:
		case RawMaterial.MATERIAL_SYNTHETIC:
		case RawMaterial.MATERIAL_ROCK:
		case RawMaterial.MATERIAL_PRECIOUS:
		{
			I=(RawMaterial)CMClass.getItem("GenResource");
			break;
		}
		case RawMaterial.MATERIAL_METAL:
		case RawMaterial.MATERIAL_MITHRIL:
		{
			I=(RawMaterial)CMClass.getItem("GenResource");
			break;
		}
		}
		switch(myResource)
		{
		case RawMaterial.RESOURCE_SALT:
			I=(RawMaterial)CMClass.getItem("GenFoodResource");
			break;
		default:
			break;
		}
		if(I!=null)
		{
			if((fullName!=null)&&(fullName.length()>0))
				I.setSecretIdentity(fullName);
			I.setMaterial(myResource);
			if(I instanceof LiquidHolder)
				((LiquidHolder)I).setLiquidType(myResource);
			I.setBaseValue(RawMaterial.CODES.VALUE(myResource));
			I.basePhyStats().setWeight(1);
			I.setDomainSource(localeCode);
			if(subType != null)
				I.setSubType(subType.toUpperCase().trim());
			adjustResourceName(I);
			I.setDescription("");
			addEffectsToResource(I);
			I.recoverPhyStats();
			return I;
		}
		return null;
	}

	@Override
	public PhysicalAgent makeResource(final int myResource, final String localeCode, final boolean noAnimals, final String fullName, final String subType)
	{
		if(myResource<0)
			return null;
		if(!noAnimals)
		{
			final MOB M = makeMOBResource(myResource);
			if(M!=null)
				return M;
		}
		return this.makeItemResource(myResource, localeCode, noAnimals, fullName, subType);
	}

	@Override
	public String getGeneralItemType(final Item I)
	{
		if(I instanceof RawMaterial)
			return CMStrings.capitalizeAndLower(getMaterialDesc(I.material()));
		if(I instanceof Weapon)
			return "weapons";
		if(I instanceof Armor)
			return "armor";
		if(I instanceof Coins)
			return "currency";
		if(I instanceof Drink)
			return "liquid";
		if(I instanceof Food)
			return "food";
		if(I instanceof Pill)
			return "pills";
		if(I instanceof Light)
			return "light sources";
		if(I instanceof com.planet_ink.coffee_mud.Items.interfaces.RoomMap)
			return "papers";
		if(I instanceof Scroll)
			return "papers";
		if(I instanceof Electronics)
			return "technology";
		if(I instanceof DoorKey)
			return "keys";
		if((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PAPER)
			return "papers";
		if(I instanceof DeadBody)
			return "corpses";
		if((I instanceof Container)&&(((Container)I).capacity()>0))
			return "containers";
		return "items";
	}

	@Override
	public String makeResourceBetterName(final int rscCode, String subType, final boolean plural)
	{
		String name=RawMaterial.CODES.NAME(rscCode).toLowerCase();
		final int iMat = rscCode&RawMaterial.MATERIAL_MASK;
		if((subType!=null)&&(subType.length()>0))
		{
			subType = subType.toLowerCase();
			if(subType.equals(name)
			&& ((iMat==RawMaterial.MATERIAL_CLOTH)||(iMat==RawMaterial.MATERIAL_PAPER)))
				name=L("@x1 bolt", name);
			else
				name=subType;
		}
		else
		if((iMat==RawMaterial.MATERIAL_MITHRIL)
		||(iMat==RawMaterial.MATERIAL_METAL))
		{
			if((iMat!=RawMaterial.RESOURCE_ADAMANTITE)
			&&(iMat!=RawMaterial.RESOURCE_BRASS)
			&&(iMat!=RawMaterial.RESOURCE_BRONZE)
			&&(iMat!=RawMaterial.RESOURCE_STEEL))
				name=L("@x1 ore",name);
		}

		if(!plural)
		{
			if((rscCode&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
				return L("some @x1",name);
			else
				return L("a pound of @x1",name);
		}
		else
		{
			if((rscCode&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
				return L("a pool of @x1", name);
			else
				return L("a bundle of @x1", name);
		}
	}

	@Override
	public String makeResourceSimpleName(final int rscCode, String subType)
	{
		String name=RawMaterial.CODES.NAME(rscCode).toLowerCase();
		final int iMat = rscCode&RawMaterial.MATERIAL_MASK;
		if((subType!=null)&&(subType.length()>0))
		{
			subType = subType.toLowerCase();
			if(subType.equals(name)
			&& ((iMat==RawMaterial.MATERIAL_CLOTH)||(iMat==RawMaterial.MATERIAL_PAPER)))
				name=L("@x1 bolt", name);
			else
				name=subType;
		}
		else
		if((iMat==RawMaterial.MATERIAL_MITHRIL)
		||(iMat==RawMaterial.MATERIAL_METAL))
		{
			if((iMat!=RawMaterial.RESOURCE_ADAMANTITE)
			&&(iMat!=RawMaterial.RESOURCE_BRASS)
			&&(iMat!=RawMaterial.RESOURCE_BRONZE)
			&&(iMat!=RawMaterial.RESOURCE_STEEL))
				name=L("@x1 ore",name);
		}
		return name;
	}

	@Override
	public void adjustResourceName(final Item I)
	{
		String name=RawMaterial.CODES.NAME(I.material()).toLowerCase();
		I.setDescription(L("It's just @x1.",name));
		final int iMat=I.material()&RawMaterial.MATERIAL_MASK;
		if((I instanceof RawMaterial)
		&&(((RawMaterial)I).getSubType().length()>0))
		{
			final String subType = ((RawMaterial)I).getSubType().toLowerCase();
			if(subType.equals(name)
			&& ((iMat==RawMaterial.MATERIAL_CLOTH)||(iMat==RawMaterial.MATERIAL_PAPER)))
				name=L("@x1 bolt", name);
			else
			{
				name=subType;
				I.setDescription(L("'@x1' is a special type of @x2 that retains its uniqueness.",name,RawMaterial.CODES.NAME(I.material()).toLowerCase()));
			}
		}
		else
		if((I.rawSecretIdentity()!=null)
		&&(I.rawSecretIdentity().length()>0))
			name = CMLib.english().removeArticleLead(I.rawSecretIdentity().toLowerCase());
		else
		if((iMat==RawMaterial.MATERIAL_MITHRIL)
		||(iMat==RawMaterial.MATERIAL_METAL))
		{
			if((I.material()!=RawMaterial.RESOURCE_ADAMANTITE)
			&&(I.material()!=RawMaterial.RESOURCE_BRASS)
			&&(I.material()!=RawMaterial.RESOURCE_BRONZE)
			&&(I.material()!=RawMaterial.RESOURCE_STEEL))
				name=L("@x1 ore",name);
		}

		if(I.basePhyStats().weight()==1)
		{
			if((I.rawSecretIdentity()!=null)
			&&(I.rawSecretIdentity().length()>0))
			{
				I.setName(I.rawSecretIdentity());
				I.setDisplayText(L("@x1 sits here.", I.rawSecretIdentity()));
			}
			else
			{
				if(I instanceof Drink)
				{
					I.setName(L("some @x1",name));
					I.setDisplayText(L("some @x1 sits here.", name));
				}
				else
				{
					I.setName(L("a pound of @x1",name));
					I.setDisplayText(L("some @x1 sits here.", name));
				}
			}
		}
		else
		{
			if(I instanceof Drink)
				I.setName(L(CMLib.english().startWithAorAn(L("@x1# pool of @x2",""+I.basePhyStats().weight(), name))));
			else
				I.setName(L(CMLib.english().startWithAorAn(L("@x1# @x2 bundle",""+I.basePhyStats().weight(), name))));
			I.setDisplayText(L("@x1 is here.",I.name()));
		}
	}

	@Override
	public Item makeItemResource(final int type)
	{
		return makeItemResource(type, "");
	}
	@Override
	public Item makeItemResource(final int type, final String subType)
	{
		Item I=null;
		if(((type&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH)
		||((type&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION))
			I=CMClass.getItem("GenFoodResource");
		else
		if((type&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
			I=CMClass.getItem("GenLiquidResource");
		else
			I=CMClass.getItem("GenResource");
		((RawMaterial)I).setSubType(subType.toUpperCase().trim());
		I.basePhyStats().setWeight(1);
		I.setMaterial(type);
		adjustResourceName(I);
		I.setBaseValue(RawMaterial.CODES.VALUE(type));
		addEffectsToResource(I);
		I.recoverPhyStats();
		return I;
	}

	@Override
	public int destroyResourcesAmt(final MOB E, final int howMuch, final int finalMaterial, final String subType, final Container C)
	{
		return destroyResourcesAmt(getAllItems(E), howMuch, finalMaterial, subType, C);
	}

	@Override
	public int destroyResourcesAmt(final Room E, final int howMuch, final int finalMaterial, final String subType, final Container C)
	{
		return destroyResourcesAmt(getAllItems(E), howMuch, finalMaterial, subType, C);
	}

	@Override
	public int destroyResourcesAmt(final List<Item> V, final int howMuch, final int resourceCode, final String subType, final Container C)
	{
		if((V==null)||(V.size()==0))
			return 0;
		if(howMuch<=0)
			return 0;
		final RawMaterial firstMaterialI = (resourceCode>0)?this.findFirstResource(V, resourceCode, C, subType):null;
		return destroyResources(V, howMuch, resourceCode, -1, null, C, firstMaterialI, null).getLostAmt();
	}

	@Override
	public int destroyResourcesValue(final Room E, final int howMuch, final int finalMaterial, final int finalSubHash, final int otherMaterial, final int otherSubHash)
	{
		final List<Item> materials = this.getSomeRawMaterial(E, finalMaterial, finalSubHash, otherMaterial, otherSubHash);
		return destroyResourcesValue(materials, howMuch, finalMaterial, otherMaterial, null, null);
	}

	@Override
	public int destroyResourcesValue(final MOB E, final int howMuch, final int finalMaterial, final int finalSubHash, final int otherMaterial, final int otherSubHash)
	{
		final List<Item> materials = this.getSomeRawMaterial(E, finalMaterial, finalSubHash, otherMaterial, otherSubHash);
		return destroyResourcesValue(materials, howMuch, finalMaterial,  otherMaterial, null, null);
	}

	@Override
	public int destroyResourcesValue(final List<Item> V, final int howMuch, final int finalMaterial, final int otherMaterial, final Item never, final Container C)
	{
		return destroyResources(V, howMuch, finalMaterial, otherMaterial, never, C, null, null).getLostValue();
	}

	@Override
	public DeadResourceRecord destroyResources(final Room R, final int howMuch, final int finalMaterial, final int finalSubHash, final int otherMaterial, final int otherSubHash)
	{
		final List<Item> materials = this.getSomeRawMaterial(R, finalMaterial, finalSubHash, otherMaterial, otherSubHash);
		return destroyResources(materials,howMuch,finalMaterial,otherMaterial,null, null, null, null);
	}

	protected DeadResourceRecord destroyResources(final List<Item> V, int howMuch, int finalMaterial, int otherMaterial, final Item never,
												  final Container C, RawMaterial firstMaterialI, RawMaterial otherMaterialI)
	{
		int lostValue=0;
		int lostAmt=0;
		int resCode=-1;
		String subType="";
		XVector<CMObject> lostProps = null;
		final CMFlagLibrary flags=CMLib.flags();
		if((V!=null)
		&&(V.size()>0)
		&&((howMuch>0)||(otherMaterial>0)))
		{
			lostProps = new XVector<CMObject>();
			if(firstMaterialI == null)
				firstMaterialI = (finalMaterial>0)?this.findFirstResource(V, finalMaterial, C, null):null;
			if(otherMaterialI == null)
				otherMaterialI = (otherMaterial>0)?this.findFirstResource(V, otherMaterial, C, null):null;
			final int firstResourceType = (firstMaterialI != null)?firstMaterialI.material():-1;
			final int otherResourceType = (otherMaterialI != null)?otherMaterialI.material():-1;
			for(int i=V.size()-1;i>=0;i--)
			{
				final Item I=V.get(i);
				if(!(I instanceof RawMaterial)
				||(I==never))
					continue;
				final RawMaterial rI=(RawMaterial)I;
				if((otherResourceType>0)
				&&(rI.material()==otherResourceType)
				&&(rI.container()==C)
				&&(otherMaterialI!=null)
				&&(rI.getSubType().equals(otherMaterialI.getSubType()))
				&&(rI.rawSecretIdentity().equals(otherMaterialI.rawSecretIdentity()))
				&&(!flags.isOnFire(I))
				&&(!flags.isEnchanted(I)))
				{
					if(I.basePhyStats().weight()>1)
					{
						final List<Item> set=disBundle(I,1,1,C);
						if((set==null)||(set.size()==0))
							continue;
						final Item uI=set.get(0);
						lostValue+=uI.value();
						lostProps.addAll(uI.effects());
						lostProps.addAll(uI.behaviors());
						uI.destroy();
					}
					else
					{
						lostValue+=I.value();
						lostProps.addAll(I.effects());
						lostProps.addAll(I.behaviors());
						((RawMaterial)I).quickDestroy();
					}
					otherMaterial=-1;
					if((finalMaterial<0)||(howMuch<=0))
						break;
				}
				else
				if((rI.material()==firstResourceType)
				&&(howMuch>0)
				&&(I.container()==C)
				&&(firstMaterialI!=null)
				&&(rI.getSubType().equals(firstMaterialI.getSubType()))
				&&(rI.rawSecretIdentity().equals(firstMaterialI.rawSecretIdentity()))
				&&(!flags.isOnFire(I))
				&&(!flags.isEnchanted(I)))
				{
					if(I.basePhyStats().weight()>howMuch)
					{
						final List<Item> set=disBundle(I,1,howMuch,C);
						if((set==null)||(set.size()==0))
							continue;
						final Item uI=set.get(0);
						lostAmt+=howMuch;
						resCode=I.material();
						lostValue+=(uI.value());
						subType=rI.getSubType();
						lostProps.addAll(uI.effects());
						lostProps.addAll(uI.behaviors());
						uI.destroy();
						howMuch=0;
					}
					else
					{
						lostAmt+=I.basePhyStats().weight();
						subType=rI.getSubType();
						resCode=I.material();
						howMuch-=I.basePhyStats().weight();
						lostProps.addAll(I.effects());
						lostValue+=I.value();
						((RawMaterial)I).quickDestroy();
					}
					if(howMuch<=0)
					{
						finalMaterial=-1;
						if(otherMaterial<=0)
							break;
					}
				}
			}
		}
		return new DeadResourceRecord()
		{
			int lostValue=0;
			int lostAmt=0;
			int resCode=-1;
			String subType="";
			List<CMObject> lostProps = null;

			public DeadResourceRecord set(final int lostValue, final int lostAmt, final int resCode, final String subType, final List<CMObject> lostProps)
			{
				this.lostValue = lostValue;
				this.lostAmt = lostAmt;
				this.resCode = resCode;
				this.subType = subType;
				this.lostProps = lostProps;
				return this;
			}

			@Override
			public int getLostValue()
			{
				return lostValue;
			}

			@Override
			public int getLostAmt()
			{
				return lostAmt;
			}

			@Override
			public int getResCode()
			{
				return resCode;
			}

			@Override
			public String getSubType()
			{
				return subType;
			}

			@Override
			public List<CMObject> getLostProps()
			{
				return lostProps;
			}
		}.set(lostValue, lostAmt, resCode, subType, lostProps);
	}

	@Override
	public RawMaterial findFirstResource(final Room E, final String other)
	{
		return findFirstResource(getAllItems(E), other);
	}

	@Override
	public RawMaterial findFirstResource(final MOB E, final String other)
	{
		return findFirstResource(getAllItems(E), other);
	}

	public RawMaterial findFirstResource(final List<Item> V, String other)
	{
		if((other==null)||(other.length()==0))
			return null;
		String subType = "";
		final int x=other.indexOf('(');
		if((x>0)&&(other.endsWith(")")))
		{
			subType=other.substring(x+1,other.length()-1).toUpperCase().trim();
			other=other.substring(0, x);
		}
		final int code = RawMaterial.CODES.FIND_IgnoreCase(other);
		if(code >=0 )
			return findFirstResource(V,code, null,subType);
		return null;
	}

	@Override
	public RawMaterial findFirstResource(final Room E, final int resource)
	{
		return findFirstResource(getAllItems(E), resource, null, null);
	}

	@Override
	public RawMaterial findFirstResource(final MOB E, final int resource)
	{
		return findFirstResource(getAllItems(E), resource, null, null);
	}

	protected RawMaterial findFirstResource(final List<Item> V, final int resource, final Container C, final String subType)
	{
		final CMFlagLibrary flags=CMLib.flags();
		for(int i=0;i<V.size();i++)
		{
			final Item I=V.get(i);
			if((I instanceof RawMaterial)
			&&(I.material()==resource)
			&&((subType==null)||(((RawMaterial)I).getSubType().equalsIgnoreCase(subType)))
			&&(I.container()==C)
			&&(!flags.isOnFire(I))
			&&(!flags.isEnchanted(I)))
				return (RawMaterial)I;
		}
		return null;
	}

	protected RawMaterial findMostResource(final List<Item> V, final int resource, final Container C)
	{
		int most=0;
		int mostMaterial=-1;
		RawMaterial mostItem=null;
		final CMFlagLibrary flags=CMLib.flags();
		for(int i=0;i<V.size();i++)
		{
			final Item I=V.get(i);
			if((I instanceof RawMaterial)
			&&(I.material()==resource)
			&&(I.material()!=mostMaterial)
			&&(I.container()==C)
			&&(!flags.isOnFire(I))
			&&(!flags.isEnchanted(I)))
			{
				final int num=findNumberOfResource(V,i,(RawMaterial)I);
				if(num>most)
				{
					mostItem=(RawMaterial)I;
					most=num;
					mostMaterial=I.material();
				}
			}
		}
		return mostItem;
	}

	@Override
	public RawMaterial findMostOfMaterial(final Room E, final String other)
	{
		return findMostOfMaterial(getAllItems(E), other);
	}

	@Override
	public RawMaterial findMostOfMaterial(final MOB E, final String other)
	{
		return findMostOfMaterial(getAllItems(E), other);
	}

	protected RawMaterial findMostOfMaterial(final List<Item> V, String other)
	{
		if((other==null)||(other.length()==0))
			return null;
		String subType="";
		final int x=other.indexOf('(');
		if((x>0)&&(other.endsWith(")")))
		{
			subType=other.substring(x+1,other.length()-1).toUpperCase().trim();
			other=other.substring(0, x);
		}
		final RawMaterial.Material m=RawMaterial.Material.findIgnoreCase(other);
		if(m!=null)
			return findMostOfMaterial(V,m.mask(),subType);
		return null;
	}

	@Override
	public int findNumberOfResourceLike(final Room E, final RawMaterial resource)
	{
		return findNumberOfResource(getAllItems(E), 0, resource);
	}

	@Override
	public int findNumberOfResourceLike(final MOB E, final RawMaterial resource)
	{
		return findNumberOfResource(getAllItems(E), 0, resource);
	}

	protected int findNumberOfResource(final List<Item> V, final int startAt, final RawMaterial resource)
	{
		int foundWood=0;
		final CMFlagLibrary flags=CMLib.flags();
		for(int i=startAt;i<V.size();i++)
		{
			final Item I=V.get(i);
			if((I instanceof RawMaterial)
			&&(I.material()==resource.material())
			&&(((RawMaterial)I).getSubType().equals(resource.getSubType()))
			&&(I.rawSecretIdentity().equals(resource.rawSecretIdentity()))
			&&(I.container()==null)
			&&(!flags.isOnFire(I))
			&&(!flags.isEnchanted(I)))
				foundWood+=I.phyStats().weight();
		}
		return foundWood;
	}

	@Override
	public RawMaterial findMostOfMaterial(final Room E, final int material)
	{
		return findMostOfMaterial(getAllItems(E), material, null);
	}

	@Override
	public RawMaterial findMostOfMaterial(final MOB E, final int material)
	{
		return findMostOfMaterial(getAllItems(E), material, null);
	}

	protected RawMaterial findMostOfMaterial(final List<Item> V, final int material, final String subType)
	{
		int most=0;
		int mostMaterial=-1;
		RawMaterial mostItem=null;
		final CMFlagLibrary flags=CMLib.flags();
		for(int i=0;i<V.size();i++)
		{
			final Item I=V.get(i);
			if((I instanceof RawMaterial)
			&&(((I.material()&RawMaterial.MATERIAL_MASK)==material)
				||(I.material()==material))
			&&(I.material()!=mostMaterial)
			&&((subType==null)||(((RawMaterial)I).getSubType().equalsIgnoreCase(subType)))
			&&(I.container()==null)
			&&(!flags.isOnFire(I))
			&&(!flags.isEnchanted(I)))
			{
				final int num=findNumberOfResource(V,i,(RawMaterial)I);
				if(num>most)
				{
					mostItem=(RawMaterial)I;
					most=num;
					mostMaterial=I.material();
				}
			}
		}
		return mostItem;
	}

	@Override
	public boolean isResourceCodeRoomMapped(final int resourceCode)
	{
		final Integer I=Integer.valueOf(resourceCode);
		for(final Enumeration<Room> e=CMClass.locales();e.hasMoreElements();)
		{
			final Room R=e.nextElement();
			if(!(R instanceof GridLocale))
			{
				if((R.resourceChoices()!=null)&&(R.resourceChoices().contains(I)))
					return true;
			}
		}
		return false;
	}

	protected List<Item> getSomeRawMaterial(final Room R, final int material1, final int subTypeHash1, final int material2, final int subTypeHash2)
	{
		final List<Item> V;
		Item I=null;
		if(R!=null)
		{
			V=new ArrayList<Item>(R.numItems());
			for(int r=0;r<R.numItems();r++)
			{
				I=R.getItem(r);
				if(I instanceof RawMaterial)
				{
					final int rawMaterialCode=((RawMaterial)I).material();
					final int subHashCode = ((RawMaterial)I).getSubType().hashCode();
					if((material1 > 0)
					&&(material1==rawMaterialCode)
					&&(subHashCode == subTypeHash1))
						V.add(I);
					else
					if((material2 > 0)
					&&(material2==rawMaterialCode)
					&&(subHashCode == subTypeHash2))
						V.add(I);
				}
			}
		}
		else
			V=new ArrayList<Item>(1);
		return V;
	}

	protected List<Item> getAllRawMaterial(final Room R, final int material1, final int material2)
	{
		final List<Item> V;
		Item I=null;
		if(R!=null)
		{
			V=new ArrayList<Item>(R.numItems());
			for(int r=0;r<R.numItems();r++)
			{
				I=R.getItem(r);
				if(I instanceof RawMaterial)
				{
					final int rawMaterialCode=((RawMaterial)I).material();
					if((material1 > 0)
					&&(material1==rawMaterialCode))
						V.add(I);
					else
					if((material2 > 0)
					&&(material2==rawMaterialCode))
						V.add(I);
				}
			}
		}
		else
			V=new ArrayList<Item>(1);
		return V;
	}

	protected List<Item> getAllItems(final ItemPossessor P)
	{
		final List<Item> V;
		Item I=null;
		if(P!=null)
		{
			V=new ArrayList<Item>(P.numItems());
			for(int i=0;i<P.numItems();i++)
			{
				I=P.getItem(i);
				if(I!=null)
					V.add(I);
			}
		}
		else
			V=new ArrayList<Item>(1);
		return V;
	}

	protected List<Item> getSomeRawMaterial(final MOB M, final int material1, final int subTypeHash1, final int material2, final int subTypeHash2)
	{
		final List<Item> V=new Vector<Item>();
		Item I=null;
		if(M!=null)
		{
			for(int r=0;r<M.numItems();r++)
			{
				I=M.getItem(r);
				if((I instanceof RawMaterial)
				&&((material1==0)
					||((I.material()==material1)&&(((RawMaterial)I).getSubType().hashCode()==subTypeHash1)))
				&&((material2==0)
					||((I.material()==material2)&&(((RawMaterial)I).getSubType().hashCode()==subTypeHash2)))
				)
					V.add(I);
			}
		}
		return V;
	}

	protected List<Item> getAllRawMaterial(final MOB M, final int material1, final int material2)
	{
		final List<Item> V=new Vector<Item>();
		Item I=null;
		if(M!=null)
		{
			for(int r=0;r<M.numItems();r++)
			{
				I=M.getItem(r);
				if((I instanceof RawMaterial)
				&&((material1==0)||(I.material()==material1))
				&&((material2==0)||(I.material()==material2))
				)
					V.add(I);
			}
		}
		return V;
	}

	@Override
	public RawMaterial fetchFoundOtherEncoded(final Room E, final String otherRequired)
	{
		return fetchFoundOtherEncoded(getAllItems(E), otherRequired);
	}

	@Override
	public RawMaterial fetchFoundOtherEncoded(final MOB E, final String otherRequired)
	{
		return fetchFoundOtherEncoded(getAllItems(E), otherRequired);
	}

	protected RawMaterial fetchFoundOtherEncoded(final List<Item> V, String otherRequired)
	{
		if((otherRequired==null)||(otherRequired.trim().length()==0))
			return null;
		final boolean resourceOther=otherRequired.startsWith("!");
		if(resourceOther)
			otherRequired=otherRequired.substring(1);
		final RawMaterial firstOther;
		if(otherRequired.length()>0)
		{
			if(!resourceOther)
				firstOther=findMostOfMaterial(V,otherRequired);
			else
				firstOther=findFirstResource(V,otherRequired);
		}
		else
			firstOther=null;
		return firstOther;
	}

	@Override
	public int getBurnDuration(final Environmental E)
	{
		if(E instanceof Item)
		{
			final Item lighting=(Item)E;
			switch(lighting.material())
			{
			case RawMaterial.RESOURCE_COAL:
				if(E instanceof RawMaterial)
					return 40;
				return 20*(1+lighting.phyStats().weight());
			case RawMaterial.RESOURCE_LAMPOIL:
				return 5+lighting.phyStats().weight();
			default:
				break;
			}
			switch(lighting.material()&RawMaterial.MATERIAL_MASK)
			{
			case RawMaterial.MATERIAL_LEATHER:
				return 20+lighting.phyStats().weight();
			case RawMaterial.MATERIAL_CLOTH:
			case RawMaterial.MATERIAL_PAPER:
			case RawMaterial.MATERIAL_SYNTHETIC:
				return 5+lighting.phyStats().weight();
			case RawMaterial.MATERIAL_WOODEN:
				if(E instanceof RawMaterial)
					return 20;
				return 20*(1+lighting.phyStats().weight());
			case RawMaterial.MATERIAL_VEGETATION:
			case RawMaterial.MATERIAL_FLESH:
				return -1;
			case RawMaterial.MATERIAL_UNKNOWN:
			case RawMaterial.MATERIAL_GLASS:
			case RawMaterial.MATERIAL_LIQUID:
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_ENERGY:
			case RawMaterial.MATERIAL_GAS:
			case RawMaterial.MATERIAL_MITHRIL:
			case RawMaterial.MATERIAL_ROCK:
			case RawMaterial.MATERIAL_PRECIOUS:
				return 0;
			}
		}
		return 0;
	}
}
