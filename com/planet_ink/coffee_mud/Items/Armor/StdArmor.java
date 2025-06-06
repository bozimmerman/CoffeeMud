package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.Items.Basic.StdContainer;
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
import com.planet_ink.coffee_mud.Items.interfaces.Armor.SizeDeviation;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2025 Bo Zimmerman

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
public class StdArmor extends StdContainer implements Armor
{
	@Override
	public String ID()
	{
		return "StdArmor";
	}

	int		sheath			= 0;
	short	layer			= 0;
	short	layerAttributes	= 0;

	public StdArmor()
	{
		super();

		setName("a shirt of armor");
		setDisplayText("a thick armored shirt sits here.");
		setDescription("Thick padded leather with strips of metal interwoven.");
		properWornBitmap=Wearable.WORN_TORSO;
		wornLogicalAnd=false;
		basePhyStats().setArmor(10);
		basePhyStats().setAbility(0);
		baseGoldValue=150;
		setCapacity(0);
		setDoorsNLocks(false,true,false,false,false,false);
		setUsesRemaining(100);
		recoverPhyStats();
	}

	@Override
	public void setUsesRemaining(int newUses)
	{
		if(newUses==Integer.MAX_VALUE)
			newUses=100;
		super.setUsesRemaining(newUses);
	}

	@Override
	public short getClothingLayer()
	{
		return layer;
	}

	@Override
	public void setClothingLayer(final short newLayer)
	{
		layer = newLayer;
	}

	@Override
	public short getLayerAttributes()
	{
		return layerAttributes;
	}

	@Override
	public void setLayerAttributes(final short newAttributes)
	{
		layerAttributes = newAttributes;
	}

	@Override
	public boolean canWear(final MOB mob, final long where)
	{
		if(where==0)
			return (whereCantWear(mob)==0);
		if((rawProperLocationBitmap()&where)!=where)
			return false;
		return mob.freeWearPositions(where,getClothingLayer(),getLayerAttributes())>0;
	}

	protected String armorHealth()
	{
		final int[] condSet = new int[]{95, 75, 50, 25, 10, 5, 0};
		int ordinal=0;
		for(int i=0;i<condSet.length;i++)
		{
			if(usesRemaining()>=condSet[i])
			{
				ordinal=i;
				break;
			}
		}
		final String message;
		switch(material()&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_PAPER:
		case RawMaterial.MATERIAL_CLOTH:
			message=CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.ARMOR_CONDITION_CLOTH, ordinal);
			break;
		case RawMaterial.MATERIAL_ROCK:
		case RawMaterial.MATERIAL_PRECIOUS:
		case RawMaterial.MATERIAL_SYNTHETIC:
		case RawMaterial.MATERIAL_GLASS:
			message=CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.ARMOR_CONDITION_GLASS, ordinal);
			break;
		case RawMaterial.MATERIAL_FLESH:
		case RawMaterial.MATERIAL_LEATHER:
			message=CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.ARMOR_CONDITION_LEATHER, ordinal);
			break;
		case RawMaterial.MATERIAL_METAL:
		case RawMaterial.MATERIAL_MITHRIL:
			message=CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.ARMOR_CONDITION_METAL, ordinal);
			break;
		case RawMaterial.MATERIAL_WOODEN:
			message=CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.ARMOR_CONDITION_WOODEN, ordinal);
			break;
		default:
			message=CMProps.getListFileChoiceFromIndexedList(CMProps.ListFile.ARMOR_CONDITION_OTHER, ordinal);
			break;
		}
		return L(message,name(),""+usesRemaining());
	}

	protected final static long STRANGE_DEVIATION_WEAR_LOCS=
		Wearable.WORN_FLOATING_NEARBY|
		Wearable.WORN_MOUTH|
		Wearable.WORN_EYES|
		Wearable.WORN_EARS|
		Wearable.WORN_NECK;

	protected final static long SMALL_DEVIATION_WEAR_LOCS=
		Wearable.WORN_TORSO|
		Wearable.WORN_LEGS|
		Wearable.WORN_WAIST|
		Wearable.WORN_ARMS|
		Wearable.WORN_HANDS|
		Wearable.WORN_FEET;

	@Override
	public SizeDeviation getSizingDeviation(final MOB mob)
	{
		if(mob == null)
			return SizeDeviation.FITS;

		if((phyStats().height()>0)
		&&(mob.phyStats().height()>0))
		{
			int devianceAllowed=200;
			if((rawProperLocationBitmap()&SMALL_DEVIATION_WEAR_LOCS)>0)
				devianceAllowed=20;
			else
			if((rawProperLocationBitmap()&STRANGE_DEVIATION_WEAR_LOCS)>0)
			{
				long wcode=rawProperLocationBitmap();

				if(CMath.bset(wcode,Wearable.WORN_HELD))
					wcode=wcode-Wearable.WORN_HELD;
				if(wcode==Wearable.WORN_FLOATING_NEARBY)
					devianceAllowed=-1;
				else
				if(wcode==Wearable.WORN_MOUTH)
					devianceAllowed=-1;
				else
				if(wcode==Wearable.WORN_EYES)
					devianceAllowed=1000;
				else
				if(wcode==Wearable.WORN_EARS)
					devianceAllowed=1000;
				else
				if(wcode==Wearable.WORN_NECK)
					devianceAllowed=5000;
			}
			if((devianceAllowed>0)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.EQUIPSIZE)))
			{
				if(mob.phyStats().height()<(phyStats().height()-devianceAllowed))
					return SizeDeviation.TOO_LARGE;
				if(mob.phyStats().height()>(phyStats().height()+devianceAllowed))
					return SizeDeviation.TOO_SMALL;
			}
		}
		return SizeDeviation.FITS;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((msg.amITarget(this))
		&&(msg.targetMinor()==CMMsg.TYP_WEAR))
		{
			final Armor.SizeDeviation deviation = getSizingDeviation(msg.source());
			if(deviation != Armor.SizeDeviation.FITS)
			{
				if(deviation == Armor.SizeDeviation.TOO_LARGE)
				{
					msg.source().tell(L("@x1 doesn't fit you -- it's too big.",name()));
					return false;
				}
				else
				if(deviation == Armor.SizeDeviation.TOO_SMALL)
				{
					msg.source().tell(L("@x1 doesn't fit you -- it's too small.",name()));
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		// lets do some damage!
		if((msg.amITarget(this))
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
		&&(subjectToWearAndTear())
		&&(usesRemaining()<100)
		&&(CMLib.flags().canBeSeenBy(this,msg.source())))
			msg.source().tell(armorHealth());
		else
		if((!amWearingAt(Wearable.IN_INVENTORY))
		&&(owner() instanceof MOB)
		&&(msg.amITarget(owner()))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0)
		&&(subjectToWearAndTear())
		&&(msg.sourceMinor()!=CMMsg.TYP_POISON)
		&&(msg.sourceMinor()!=CMMsg.TYP_DISEASE)
		&&((!(msg.tool() instanceof Ability))
			||(((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES) != Ability.ACODE_POISON)
				&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES) != Ability.ACODE_DISEASE)))
		&&(CMLib.dice().rollPercentage()>((phyStats().level()/2)+(10*phyStats().ability())+(CMLib.flags().isABonusItems(this)?20:0)))
		&&(CMLib.dice().rollPercentage()>(((MOB)owner()).charStats().getStat(CharStats.STAT_DEXTERITY))))
		{
			int weaponDamageType=-1;
			{
				if(msg.tool() instanceof Weapon)
					weaponDamageType=((Weapon)msg.tool()).weaponDamageType();
				else
				{
					final Integer weaponType = Weapon.MSG_TYPE_MAP.get(Integer.valueOf(msg.sourceMinor()));
					if(weaponType != null)
						weaponDamageType = weaponType.intValue();
				}
			}
			final int oldUses=usesRemaining();
			if(weaponDamageType>=0)
			{
				//TODO: make this into a chart in lists.ini .. somehow
				switch(material()&RawMaterial.MATERIAL_MASK)
				{
				case RawMaterial.MATERIAL_CLOTH:
				case RawMaterial.MATERIAL_PAPER:
					switch(weaponDamageType)
					{
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_FROSTING:
					case Weapon.TYPE_GASSING:
					case Weapon.TYPE_STULTIFYING:
					case Weapon.TYPE_CORRUPTING:
						break;
					case Weapon.TYPE_STRIKING:
						if(CMLib.dice().rollPercentage()<25)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,5,0));
						break;
					case Weapon.TYPE_LASERING:
						if(CMLib.dice().rollPercentage()<75)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,75,0));
						break;
					case Weapon.TYPE_MELTING:
					case Weapon.TYPE_BURNING:
					case Weapon.TYPE_DISRUPTING:
						if(CMLib.dice().rollPercentage()<25)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,15,0));
						break;
					case Weapon.TYPE_NATURAL:
					case Weapon.TYPE_SONICING:
					case Weapon.TYPE_SCRAPING:
						if(CMLib.dice().rollPercentage()==1)
							setUsesRemaining(usesRemaining()-1);
						break;
					case Weapon.TYPE_PIERCING:
					case Weapon.TYPE_SHOOT:
					case Weapon.TYPE_SLASHING:
						if(CMLib.dice().rollPercentage()<5)
							setUsesRemaining(usesRemaining()-2);
						break;
					}
					break;
				case RawMaterial.MATERIAL_GLASS:
					switch(weaponDamageType)
					{
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_GASSING:
					case Weapon.TYPE_MELTING:
					case Weapon.TYPE_BURNING:
					case Weapon.TYPE_LASERING:
					case Weapon.TYPE_CORRUPTING:
					case Weapon.TYPE_SCRAPING:
						break;
					case Weapon.TYPE_SONICING:
					case Weapon.TYPE_DISRUPTING:
						if(CMLib.dice().rollPercentage()<75)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,75,0));
						break;
					case Weapon.TYPE_STRIKING:
					case Weapon.TYPE_FROSTING:
					case Weapon.TYPE_STULTIFYING:
						if(CMLib.dice().rollPercentage()<5)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,20,0));
						break;
					case Weapon.TYPE_BASHING:
					case Weapon.TYPE_NATURAL:
						if(CMLib.dice().rollPercentage()<10)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,10,0));
						break;
					case Weapon.TYPE_PIERCING:
					case Weapon.TYPE_SHOOT:
					case Weapon.TYPE_SLASHING:
						if(CMLib.dice().rollPercentage()<5)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,15,0));
						break;
					}
					break;
				case RawMaterial.MATERIAL_LEATHER:
					switch(weaponDamageType)
					{
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_FROSTING:
					case Weapon.TYPE_GASSING:
						break;
					case Weapon.TYPE_STRIKING:
					case Weapon.TYPE_CORRUPTING:
						if(CMLib.dice().rollPercentage()<25)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,5,0));
						break;
					case Weapon.TYPE_LASERING:
					case Weapon.TYPE_DISRUPTING:
						if(CMLib.dice().rollPercentage()<50)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,50,0));
						break;
					case Weapon.TYPE_MELTING:
					case Weapon.TYPE_BURNING:
						if(CMLib.dice().rollPercentage()<25)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,15,0));
						break;
					case Weapon.TYPE_BASHING:
					case Weapon.TYPE_NATURAL:
					case Weapon.TYPE_SONICING:
					case Weapon.TYPE_STULTIFYING:
					case Weapon.TYPE_SCRAPING:
						if(CMLib.dice().rollPercentage()<5)
							setUsesRemaining(usesRemaining()-1);
						break;
					case Weapon.TYPE_PIERCING:
					case Weapon.TYPE_SHOOT:
						if(CMLib.dice().rollPercentage()<10)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,4,0));
						break;
					case Weapon.TYPE_SLASHING:
						if(CMLib.dice().rollPercentage()<5)
							setUsesRemaining(usesRemaining()-1);
						break;
					}
					break;
				case RawMaterial.MATERIAL_MITHRIL:
					if(CMLib.dice().rollPercentage()>1)
						break;
					//$FALL-THROUGH$
				case RawMaterial.MATERIAL_METAL:
					switch(weaponDamageType)
					{
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_FROSTING:
					case Weapon.TYPE_GASSING:
					case Weapon.TYPE_SCRAPING:
					case Weapon.TYPE_STULTIFYING:
					case Weapon.TYPE_CORRUPTING:
						break;
					case Weapon.TYPE_LASERING:
					case Weapon.TYPE_DISRUPTING:
						if(CMLib.dice().rollPercentage()<35)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,35,0));
						break;
					case Weapon.TYPE_MELTING:
						if(CMLib.dice().rollPercentage()<25)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,15,0));
						break;
					case Weapon.TYPE_BURNING:
						if(CMLib.dice().rollPercentage()==1)
							setUsesRemaining(usesRemaining()-1);
						break;
					case Weapon.TYPE_BASHING:
						if((rawWornCode()==Wearable.WORN_HEAD)
						&&(CMLib.dice().rollPercentage()==1)
						&&(CMLib.dice().rollPercentage()==1)
						&&((msg.value())>10)
						&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
						{
							final Ability A=CMClass.getAbility("Disease_Tinnitus");
							if((A!=null)&&(owner().fetchEffect(A.ID())==null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
								A.invoke((MOB)owner(),owner(),true,0);
						}
						if(CMLib.dice().rollPercentage()<5)
							setUsesRemaining(usesRemaining()-2);
						break;
					case Weapon.TYPE_STRIKING:
					case Weapon.TYPE_NATURAL:
					case Weapon.TYPE_SONICING:
						if(CMLib.dice().rollPercentage()<5)
							setUsesRemaining(usesRemaining()-2);
						break;
					case Weapon.TYPE_PIERCING:
					case Weapon.TYPE_SHOOT:
					case Weapon.TYPE_SLASHING:
						if(CMLib.dice().rollPercentage()<2)
							setUsesRemaining(usesRemaining()-1);
						break;
					}
					break;
				case RawMaterial.MATERIAL_SYNTHETIC:
					switch(weaponDamageType)
					{
					case Weapon.TYPE_FROSTING:
					case Weapon.TYPE_GASSING:
					case Weapon.TYPE_CORRUPTING:
						break;
					case Weapon.TYPE_LASERING:
					case Weapon.TYPE_DISRUPTING:
						if(CMLib.dice().rollPercentage()<20)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,25,0));
						break;
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_MELTING:
					case Weapon.TYPE_SCRAPING:
					case Weapon.TYPE_STULTIFYING:
						if(CMLib.dice().rollPercentage()<5)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,10,0));
						break;
					case Weapon.TYPE_BURNING:
					case Weapon.TYPE_SONICING:
						if(CMLib.dice().rollPercentage()==1)
							setUsesRemaining(usesRemaining()-1);
						break;
					case Weapon.TYPE_BASHING:
					case Weapon.TYPE_STRIKING:
					case Weapon.TYPE_NATURAL:
						if(CMLib.dice().rollPercentage()<5)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,4,0));
						break;
					case Weapon.TYPE_PIERCING:
					case Weapon.TYPE_SHOOT:
						if(CMLib.dice().rollPercentage()<5)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,8,0));
						break;
					case Weapon.TYPE_SLASHING:
						break;
					}
					break;
				case RawMaterial.MATERIAL_ROCK:
				case RawMaterial.MATERIAL_PRECIOUS:
					switch(weaponDamageType)
					{
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_FROSTING:
					case Weapon.TYPE_GASSING:
					case Weapon.TYPE_STULTIFYING:
					case Weapon.TYPE_CORRUPTING:
						break;
					case Weapon.TYPE_LASERING:
					case Weapon.TYPE_SCRAPING:
						if((CMLib.dice().rollPercentage()<50)&&((material&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PRECIOUS))
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,50,0));
						break;
					case Weapon.TYPE_SONICING:
					case Weapon.TYPE_DISRUPTING:
						if(CMLib.dice().rollPercentage()<35)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,35,0));
						break;
					case Weapon.TYPE_MELTING:
						if(CMLib.dice().rollPercentage()<5)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,5,0));
						break;
					case Weapon.TYPE_BURNING:
						if(CMLib.dice().rollPercentage()==1)
							setUsesRemaining(usesRemaining()-1);
						break;
					case Weapon.TYPE_BASHING:
					case Weapon.TYPE_STRIKING:
					case Weapon.TYPE_NATURAL:
						if(CMLib.dice().rollPercentage()<5)
							setUsesRemaining(usesRemaining()-2);
						break;
					case Weapon.TYPE_PIERCING:
					case Weapon.TYPE_SHOOT:
					case Weapon.TYPE_SLASHING:
						if(CMLib.dice().rollPercentage()<2)
							setUsesRemaining(usesRemaining()-1);
						break;
					}
					break;
				case RawMaterial.MATERIAL_WOODEN:
					switch(weaponDamageType)
					{
					case Weapon.TYPE_BURSTING:
					case Weapon.TYPE_FROSTING:
					case Weapon.TYPE_GASSING:
					case Weapon.TYPE_STULTIFYING:
						break;
					case Weapon.TYPE_STRIKING:
						if(CMLib.dice().rollPercentage()<20)
							setUsesRemaining(usesRemaining()-1);
						break;
					case Weapon.TYPE_MELTING:
					case Weapon.TYPE_BURNING:
					case Weapon.TYPE_SCRAPING:
						if(CMLib.dice().rollPercentage()<20)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,5,0));
						break;
					case Weapon.TYPE_LASERING:
					case Weapon.TYPE_DISRUPTING:
					case Weapon.TYPE_CORRUPTING:
						if(CMLib.dice().rollPercentage()<50)
							setUsesRemaining(usesRemaining()-CMLib.dice().roll(1,50,0));
						break;
					case Weapon.TYPE_BASHING:
					case Weapon.TYPE_NATURAL:
					case Weapon.TYPE_SONICING:
						if(CMLib.dice().rollPercentage()<5)
							setUsesRemaining(usesRemaining()-2);
						break;
					case Weapon.TYPE_PIERCING:
					case Weapon.TYPE_SHOOT:
					case Weapon.TYPE_SLASHING:
						if(CMLib.dice().rollPercentage()<2)
							setUsesRemaining(usesRemaining()-1);
						break;
					}
					break;
				case RawMaterial.MATERIAL_ENERGY:
				case RawMaterial.MATERIAL_GAS:
					break;
				default:
					if(CMLib.dice().rollPercentage()==1)
						setUsesRemaining(usesRemaining()-1);
					break;
				}
			}

			if(oldUses!=usesRemaining())
				recoverPhyStats();

			if((usesRemaining()<10)
			&&(oldUses!=usesRemaining())
			&&(owner()!=null)
			&&(owner() instanceof MOB)
			&&(usesRemaining()>0))
				((MOB)owner()).tell(L("@x1 is nearly destroyed! (@x2%)",name(),""+usesRemaining()));
			else
			if((usesRemaining()<=0)
			&&(owner()!=null)
			&&(owner() instanceof MOB))
			{
				final MOB owner=(MOB)owner();
				setUsesRemaining(100);
				msg.addTrailerMsg(CMClass.getMsg(((MOB)owner()),null,null,CMMsg.MSG_OK_VISUAL,L("^I@x1 is destroyed!!^?",name()),CMMsg.NO_EFFECT,null,CMMsg.MSG_OK_VISUAL,L("^I@x1 being worn by <S-NAME> is destroyed!^?",name())));
				if(this instanceof Container)
					((Container)this).emptyPlease(false);
				unWear();
				destroy();
				owner.recoverPhyStats();
				owner.recoverCharStats();
				owner.recoverMaxState();
				if(owner.location()!=null)
					owner.location().recoverRoomStats();
			}
		}
	}

	@Override
	protected boolean abilityImbuesMagic()
	{
		return true;
	}

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		if((abilityImbuesMagic()&&(phyStats().ability()>0))||(this instanceof MiscMagic))
			phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_BONUS);
		if((basePhyStats().height()==0)
		&&(!amWearingAt(Wearable.IN_INVENTORY))
		&&(owner() instanceof MOB))
			basePhyStats().setHeight(((MOB)owner()).phyStats().height());
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);

		if((!amWearingAt(Wearable.IN_INVENTORY))
		&&((!amWearingAt(Wearable.WORN_FLOATING_NEARBY))||(fitsOn(Wearable.WORN_FLOATING_NEARBY)))
		&&((!amWearingAt(Wearable.WORN_HELD))||(this instanceof Shield)))
		{
			affectableStats.setArmor(affectableStats.armor()-phyStats().armor());
			if((phyStats().ability()>0)&&((layerAttributes&LAYERMASK_MULTIWEAR)==0))
			{
				final int ability=super.wornLogicalAnd ? (phyStats().ability()*CMath.numberOfSetBits(super.myWornCode)) : phyStats().ability();
				if(amWearingAt(Wearable.WORN_TORSO))
					affectableStats.setArmor(affectableStats.armor()-(ability*5));
				else
				if((amWearingAt(Wearable.WORN_HEAD))||(amWearingAt(Wearable.WORN_HELD)))
					affectableStats.setArmor(affectableStats.armor()-(ability*2));
				else
				if(!amWearingAt(Wearable.WORN_FLOATING_NEARBY))
					affectableStats.setArmor(affectableStats.armor()-ability);
			}
		}
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(!amWearingAt(Wearable.IN_INVENTORY))
		switch(material()&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_METAL:
			if(affectableStats.getStat(CharStats.STAT_SAVE_ELECTRIC)>(Short.MIN_VALUE/2))
				affectableStats.setStat(CharStats.STAT_SAVE_ELECTRIC,affectableStats.getStat(CharStats.STAT_SAVE_ELECTRIC)-10);
			break;
		case RawMaterial.MATERIAL_LEATHER:
			if(affectableStats.getStat(CharStats.STAT_SAVE_ACID)<(Short.MAX_VALUE/2))
				affectableStats.setStat(CharStats.STAT_SAVE_ACID,affectableStats.getStat(CharStats.STAT_SAVE_ACID)+10);
			break;
		case RawMaterial.MATERIAL_MITHRIL:
			if(affectableStats.getStat(CharStats.STAT_SAVE_MAGIC)<(Short.MAX_VALUE/2))
				affectableStats.setStat(CharStats.STAT_SAVE_MAGIC,affectableStats.getStat(CharStats.STAT_SAVE_MAGIC)+10);
			break;
		case RawMaterial.MATERIAL_CLOTH:
		case RawMaterial.MATERIAL_PAPER:
			if(affectableStats.getStat(CharStats.STAT_SAVE_FIRE)>(Short.MIN_VALUE/2))
				affectableStats.setStat(CharStats.STAT_SAVE_FIRE,affectableStats.getStat(CharStats.STAT_SAVE_FIRE)-10);
			break;
		case RawMaterial.MATERIAL_GLASS:
		case RawMaterial.MATERIAL_ROCK:
		case RawMaterial.MATERIAL_PRECIOUS:
		case RawMaterial.MATERIAL_VEGETATION:
		case RawMaterial.MATERIAL_FLESH:
		case RawMaterial.MATERIAL_SYNTHETIC:
			if(affectableStats.getStat(CharStats.STAT_SAVE_FIRE)<(Short.MAX_VALUE/2))
				affectableStats.setStat(CharStats.STAT_SAVE_FIRE,affectableStats.getStat(CharStats.STAT_SAVE_FIRE)+10);
			break;
		case RawMaterial.MATERIAL_ENERGY:
			if(affectableStats.getStat(CharStats.STAT_SAVE_PARALYSIS)<(Short.MAX_VALUE/2))
				affectableStats.setStat(CharStats.STAT_SAVE_PARALYSIS,affectableStats.getStat(CharStats.STAT_SAVE_PARALYSIS)+10);
			break;
		}
	}

	@Override
	public int value()
	{
		if(usesRemaining()<1000)
			return (int)Math.round(CMath.mul(super.value(),CMath.div(usesRemaining(),100)));
		return super.value();
	}

	@Override
	public boolean subjectToWearAndTear()
	{
		if((usesRemaining()<=1000)&&(usesRemaining()>=0))
			return true;
		return false;
	}

	@Override
	public String secretIdentity()
	{
		String id=super.secretIdentity();
		if(phyStats().ability()>0)
			id=name()+" +"+phyStats().ability()+((id.length()>0)?"\n\r":"")+id;
		else
		if(phyStats().ability()<0)
			id=name()+" "+phyStats().ability()+((id.length()>0)?"\n\r":"")+id;
		final int timsLevel=CMLib.itemBuilder().timsLevelCalculator(this);
		if(timsLevel != phyStats.level())
			id += " (Power level: "+timsLevel+")";
		final PhyStats stats = (PhyStats)CMClass.getCommon("DefaultPhyStats");
		stats.setAllValues(0);
		if(amBeingWornProperly())
			affectPhyStats(owner(),stats);
		else
		{
			synchronized(this)
			{
				final long wornCode=rawWornCode();
				try
				{
					setRawWornCode(rawProperLocationBitmap());
					affectPhyStats(owner(),stats);
				}
				finally
				{
					setRawWornCode(wornCode);
				}
			}
		}
		id +="\n\r"+L("Base Protection: @x1",""+(phyStats().armor()-stats.armor()));
		return id;
	}
}
