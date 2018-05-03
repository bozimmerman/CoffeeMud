package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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
public class TimsLibrary extends StdLibrary implements ItemBalanceLibrary
{
	@Override
	public String ID()
	{
		return "TimsLibrary";
	}

	@Override
	public int timsLevelCalculator(Item I)
	{
		final int[] castMul=new int[1];
		final Ability[] RET=getTimsAdjResCast(I,castMul);
		final Ability ADJ=RET[0];
		final Ability RES=RET[1];
		final Ability CAST=RET[2];
		return timsLevelCalculator(I,ADJ,RES,CAST,castMul[0]);
	}

	protected double timsDmgModifier(int weaponClass)
	{
		double dmgModifier=1.0;
		switch(weaponClass)
		{
		case Weapon.CLASS_FLAILED:
			dmgModifier=1.1;
			break;
		case Weapon.CLASS_EDGED:
			dmgModifier=0.8;
			break;
		case Weapon.CLASS_DAGGER:
			dmgModifier=0.5;
			break;
		case Weapon.CLASS_BLUNT:
		case Weapon.CLASS_STAFF:
			dmgModifier=0.9;
			break;
		case Weapon.CLASS_THROWN:
			dmgModifier=1.5;
		}
		return dmgModifier;
	}
	
	protected double timsBaseAttackModifier(int weaponClass)
	{
		double baseattack=0.0;
		switch(weaponClass)
		{
		case Weapon.CLASS_FLAILED:
			baseattack=-5;
			break;
		case Weapon.CLASS_EDGED:
		case Weapon.CLASS_DAGGER:
		{
			baseattack = 10;
			break;
		}
		}
		return baseattack;
	}
	
	protected double timsAttackModifier(int weaponClass)
	{
		double attModifier=0.0;
		switch(weaponClass)
		{
		case Weapon.CLASS_FLAILED:
			attModifier=-0.3;
			break;
		case Weapon.CLASS_EDGED:
			attModifier=0.6;
			break;
		case Weapon.CLASS_DAGGER:
			attModifier=1.3;
			break;
		case Weapon.CLASS_BLUNT:
		case Weapon.CLASS_STAFF:
			attModifier=0.3;
		}
		return attModifier;
	}
	
	@Override
	public int timsLevelCalculator(Item I, Ability ADJ, Ability RES, Ability CAST, int castMul)
	{
		int level=0;
		final Item savedI=(Item)I.copyOf();
		savedI.recoverPhyStats();
		I=(Item)I.copyOf();
		I.recoverPhyStats();
		int otherDam=0;
		int otherAtt=0;
		int otherArm=0;
		if(ADJ!=null)
		{
			otherArm=-CMath.s_int(ADJ.getStat("STAT-ARMOR"));
			otherAtt=CMath.s_int(ADJ.getStat("STAT-ATTACK"));
			otherDam=CMath.s_int(ADJ.getStat("STAT-DAMAGE"));
		}
		final int curArmor=savedI.basePhyStats().armor()+otherArm;
		final double curAttack=savedI.basePhyStats().attackAdjustment()+otherAtt;
		final double curDamage=savedI.basePhyStats().damage()+otherDam;
		final Wearable.CODES codes = Wearable.CODES.instance();
		if(I instanceof Weapon)
		{
			double weight=8;
			if(weight<1.0)
				weight=1.0;
			final double range=savedI.maxRange();
			final int wclass=((Weapon)savedI).weaponClassification();
			final double dmgMod = this.timsDmgModifier(wclass);
			final double dmgLevel = Math.floor(((2.0*curDamage/(2.0*(I.rawLogicalAnd()?2.0:1.0)+1.0)+(curAttack-weight)/5.0+range)*(range/weight+2.0)/dmgMod))+1;
			final double attackLevel = (curAttack - this.timsBaseAttackModifier(wclass)) / this.timsAttackModifier(wclass);
			level = (int)Math.round((dmgLevel + attackLevel) / 2.0);
		}
		else
		{
			final long worndata=savedI.rawProperLocationBitmap();
			double weightpts=0;
			for(int i=0;i<codes.location_strength_points().length-1;i++)
			{
				if(CMath.isSet(worndata,i))
				{
					weightpts+=codes.location_strength_points()[i+1];
					if(!I.rawLogicalAnd())
						break;
				}
			}
			final int[] leatherPoints = { 0, 0, 1, 5, 10, 16, 23, 31, 40, 49, 58, 67, 76, 85, 94 };
			final int[] clothPoints = { 0, 3, 7, 12, 18, 25, 33, 42, 52, 62, 72, 82, 92, 102 };
			final int[] metalPoints = { 0, 0, 0, 0, 1, 3, 5, 8, 12, 17, 23, 30, 38, 46, 54, 62, 70, 78, 86, 94 };
			final int materialCode=savedI.material()&RawMaterial.MATERIAL_MASK;
			int[] useArray=null;
			switch(materialCode)
			{
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_MITHRIL:
			case RawMaterial.MATERIAL_PRECIOUS:
			case RawMaterial.MATERIAL_ENERGY:
				useArray=metalPoints;
				break;
			case RawMaterial.MATERIAL_SYNTHETIC:
			case RawMaterial.MATERIAL_LEATHER:
			case RawMaterial.MATERIAL_GLASS:
			case RawMaterial.MATERIAL_ROCK:
			case RawMaterial.MATERIAL_WOODEN:
				useArray=leatherPoints;
				break;
			case RawMaterial.MATERIAL_GAS:
			default:
				useArray=clothPoints;
				break;
			}
			int which=(int)Math.round(CMath.div(curArmor,weightpts)+1);
			if(which<0)
				which=0;
			if(which>=useArray.length)
				which=useArray.length-1;
			level=useArray[which];
		}
		level+=I.basePhyStats().ability()*5;
		if(CAST!=null)
		{
			final String ID=CAST.ID().toUpperCase();
			final Vector<Ability> theSpells=new Vector<Ability>();
			String names=CAST.text();
			int del=names.indexOf(';');
			while(del>=0)
			{
				final String thisOne=names.substring(0,del);
				final Ability A=CMClass.getAbility(thisOne);
				if(A!=null)
					theSpells.addElement(A);
				names=names.substring(del+1);
				del=names.indexOf(';');
			}
			Ability A=CMClass.getAbility(names);
			if(A!=null)
				theSpells.addElement(A);
			for(int v=0;v<theSpells.size();v++)
			{
				A=theSpells.elementAt(v);
				int mul=1;
				if(A.abstractQuality()==Ability.QUALITY_MALICIOUS)
					mul=-1;
				if(ID.indexOf("HAVE")>=0)
					level+=(mul*CMLib.ableMapper().lowestQualifyingLevel(A.ID()));
				else
					level+=(mul*CMLib.ableMapper().lowestQualifyingLevel(A.ID())/2);
			}
		}
		if(ADJ!=null)
			level += CMath.s_int(ADJ.getStat("LEVEL"));
		savedI.destroy();
		I.destroy(); // this was a copy
		return level;
	}

	@Override
	public boolean fixRejuvItem(Item I)
	{
		Ability A=I.fetchEffect("ItemRejuv");
		if(A!=null)
		{
			A=I.fetchEffect("ItemRejuv");
			if(A.isSavable())
				return false;
			A.setSavable(false);
			return true;
		}
		return false;
	}

	@Override
	public Ability[] getTimsAdjResCast(Item I, int[] castMul)
	{
		Ability A;
		final Ability[] RET=new Ability[3]; // adj, res, cast
		castMul[0]=1;
		for(int i=0;i<I.numEffects();i++)
		{
			A=I.fetchEffect( i );
			if(A instanceof TriggeredAffect)
			{
				final long flags=A.flags();
				final int triggers=((TriggeredAffect) A).triggerMask();
				if( CMath.bset( flags, Ability.FLAG_ADJUSTER )
				&& (( triggers&(TriggeredAffect.TRIGGER_WEAR_WIELD|TriggeredAffect.TRIGGER_GET|TriggeredAffect.TRIGGER_MOUNT ))>0))
					RET[0]=A;
				else
				if( CMath.bset( flags, Ability.FLAG_RESISTER )
				&& (( triggers&(TriggeredAffect.TRIGGER_WEAR_WIELD|TriggeredAffect.TRIGGER_GET|TriggeredAffect.TRIGGER_MOUNT ))>0))
					RET[1]=A;
				else
				if( CMath.bset( flags, Ability.FLAG_CASTER )
				&& (triggers > 0))
				{
					RET[2]=A;
					if((triggers & TriggeredAffect.TRIGGER_HITTING_WITH)>0)
						castMul[0]=-1;
				}
			}
		}
		return RET;
	}

	private void reportChangesDestroyOldI(Item oldI, Item newI, StringBuffer changes,int OTLVL, int TLVL)
	{
		if((changes == null)||(oldI==null))
			return;
		final Ability[] RET=getTimsAdjResCast(newI,new int[1]);
		final Ability ADJ=RET[0];
		final Ability RES=RET[1];
		final Ability CAST=RET[2];
		final int[] LVLS=getItemLevels(newI,ADJ,RES,CAST);
		final int TLVL2=totalLevels(LVLS);

		changes.append(newI.name()+":"+newI.basePhyStats().level()+"("+OTLVL+")=>"+TLVL2+"("+TLVL+"), ");
		for(int i=0;i<oldI.getStatCodes().length;i++)
		{
			if((!oldI.getStat(oldI.getStatCodes()[i]).equals(newI.getStat(newI.getStatCodes()[i]))))
				changes.append(oldI.getStatCodes()[i]+"("+oldI.getStat(newI.getStatCodes()[i])+"->"+newI.getStat(newI.getStatCodes()[i])+"), ");
		}
		changes.append("\n\r");
		oldI.destroy(); // this was a copy
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean itemFix(Item I, int lvlOr0, StringBuffer changes)
	{
		final Item oldI = (changes!=null)?(Item)I.copyOf():null;

		if((I instanceof SpellHolder)
		||((I instanceof Wand)&&(lvlOr0<=0)))
		{
			Vector<Ability> spells=new Vector<Ability>();
			if(I instanceof SpellHolder)
				spells.addAll(((SpellHolder)I).getSpells());
			else
			if((I instanceof Wand)&&(((Wand)I).getSpell()!=null))
				spells.add(((Wand)I).getSpell());
			if(spells.size()==0)
				return false;
			int levels=0;
			spells=(Vector<Ability>)spells.clone();
			for (final Ability ability : spells)
				levels+=CMLib.ableMapper().lowestQualifyingLevel(ability.ID());
			final int level=(int)Math.round(CMath.div(levels, spells.size()));
			if(level==I.basePhyStats().level())
				return false;
			I.basePhyStats().setLevel(level);
			I.phyStats().setLevel(level);
			if(CMLib.flags().isCataloged(I))
				CMLib.catalog().updateCatalog(I);
			reportChangesDestroyOldI(oldI,I,changes,level,level);
			return true;
		}
		else
		if((I instanceof Weapon)||(I instanceof Armor))
		{
			int lvl=lvlOr0;
			if(lvl <=0)
				lvl=I.basePhyStats().level();
			I.basePhyStats().setLevel(lvl);
			I.phyStats().setLevel(lvl);
			final Ability[] RET=getTimsAdjResCast(I,new int[1]);
			final Ability ADJ=RET[0];
			final Ability RES=RET[1];
			final Ability CAST=RET[2];
			int[] LVLS=getItemLevels(I,ADJ,RES,CAST);
			int TLVL=totalLevels(LVLS);
			final int OTLVL=TLVL;
			if(lvl<0)
			{
				if(TLVL<=0)
					lvl=1;
				else
					lvl=TLVL;
				I.basePhyStats().setLevel(lvl);
				I.recoverPhyStats();
				fixRejuvItem(I);
				if(CMLib.flags().isCataloged(I))
					CMLib.catalog().updateCatalog(I);
				reportChangesDestroyOldI(oldI,I,changes,OTLVL,TLVL);
				return true;
			}
			if((TLVL>0)&&(TLVL>Math.round(CMath.mul(lvl,1.1))))
			{
				//int FTLVL=TLVL;
				final Vector<Integer> illegalNums=new Vector<Integer>();
				//Log.sysOut("Reset",I.name()+"("+I.basePhyStats().level()+") "+TLVL+", "+I.basePhyStats().armor()+"/"+I.basePhyStats().attackAdjustment()+"/"+I.basePhyStats().damage()+"/"+((ADJ!=null)?ADJ.text():"null"));
				while((TLVL>Math.round(CMath.mul(lvl,1.1)))&&(illegalNums.size()<4))
				{
					int highIndex=-1;
					for(int i=0;i<LVLS.length;i++)
						if(((highIndex<0)||(LVLS[i]>LVLS[highIndex]))
						&&(!illegalNums.contains(Integer.valueOf(i))))
							highIndex=i;
					if(highIndex<0)
						break;
					switch(highIndex)
					{
					case 0:
						if(I instanceof Weapon)
						{
							final String s=(ADJ!=null)?ADJ.text():"";
							final int oldAtt=I.basePhyStats().attackAdjustment();
							final int oldDam=I.basePhyStats().damage();
							toneDownWeapon((Weapon)I,ADJ);
							if((I.basePhyStats().attackAdjustment()==oldAtt)
							&&(I.basePhyStats().damage()==oldDam)
							&&((ADJ==null)||(ADJ.text().equals(s))))
								illegalNums.addElement(Integer.valueOf(0));
						}
						else
						{
							final String s=(ADJ!=null)?ADJ.text():"";
							final int oldArm=I.basePhyStats().armor();
							toneDownArmor((Armor)I,ADJ);
							if((I.basePhyStats().armor()==oldArm)
							&&((ADJ==null)||(ADJ.text().equals(s))))
								illegalNums.addElement(Integer.valueOf(0));
						}
						break;
					case 1:
						if(I.basePhyStats().ability()>0)
							I.basePhyStats().setAbility(I.basePhyStats().ability()-1);
						else
							illegalNums.addElement(Integer.valueOf(1));
						break;
					case 2:
						illegalNums.addElement(Integer.valueOf(2));
						// nothing I can do!;
						break;
					case 3:
						if(ADJ==null)
							illegalNums.addElement(Integer.valueOf(3));
						else
						{
							final String oldTxt=ADJ.text();
							ADJ.setStat("TONEDOWN-MISC", "90%");
							if(ADJ.text().equals(oldTxt))
								illegalNums.addElement(Integer.valueOf(3));
						}
						break;
					}
					LVLS=getItemLevels(I,ADJ,RES,CAST);
					TLVL=totalLevels(LVLS);
				}
				//Log.sysOut("Reset",I.name()+"("+I.basePhyStats().level()+") "+FTLVL+"->"+TLVL+", "+I.basePhyStats().armor()+"/"+I.basePhyStats().attackAdjustment()+"/"+I.basePhyStats().damage()+"/"+((ADJ!=null)?ADJ.text():"null"));
				fixRejuvItem(I);
				if(CMLib.flags().isCataloged(I))
					CMLib.catalog().updateCatalog(I);
				reportChangesDestroyOldI(oldI,I,changes,OTLVL,TLVL);
				return true;
			}
		}
		if(fixRejuvItem(I))
		{
			if(CMLib.flags().isCataloged(I))
				CMLib.catalog().updateCatalog(I);
			if(oldI!=null)
				oldI.destroy();
			return true;
		}
		if(oldI!=null)
			oldI.destroy();
		return false;
	}

	@Override
	public boolean toneDownValue(Item I)
	{
		int hands=0;
		int weaponClass=0;
		if(I instanceof Coins)
			return false;
		if(I instanceof Weapon)
		{
			hands=I.rawLogicalAnd()?2:1;
			weaponClass=((Weapon)I).weaponClassification();
		}
		else
		if(!(I instanceof Armor))
			return false;
		final Map<String,String> H=timsItemAdjustments(I,I.phyStats().level(),I.material(),hands,weaponClass,I.maxRange(),I.rawProperLocationBitmap());
		final int newValue=CMath.s_int(H.get("VALUE"));
		if((I.baseGoldValue()>newValue)&&(newValue>0))
		{
			I.setBaseValue(newValue);
			return true;
		}
		return false;
	}

	@Override
	public void balanceItemByLevel(Item I)
	{
		int hands=0;
		int weaponClass=0;
		if(I instanceof Weapon)
		{
			hands=I.rawLogicalAnd()?2:1;
			weaponClass=((Weapon)I).weaponClassification();
		}
		final Map<String,String> H=timsItemAdjustments(I,I.basePhyStats().level(),I.material(),hands,weaponClass,I.maxRange(),I.rawProperLocationBitmap());
		if(I instanceof Weapon)
		{
			I.basePhyStats().setDamage(CMath.s_int(H.get("DAMAGE")));
			I.basePhyStats().setAttackAdjustment(CMath.s_int(H.get("ATTACK")));
			I.setBaseValue(CMath.s_int(H.get("VALUE")));
			I.recoverPhyStats();
		}
		else
		if(I instanceof Armor)
		{
			I.basePhyStats().setArmor(CMath.s_int(H.get("ARMOR")));
			I.setBaseValue(CMath.s_int(H.get("VALUE")));
			I.basePhyStats().setWeight(CMath.s_int(H.get("WEIGHT")));
			I.recoverPhyStats();
		}
	}

	@Override
	public Map<String, String> timsItemAdjustments(Item I,
												 int level,
												 int material,
												 int hands,
												 int wclass,
												 int reach,
												 long worndata)
	{
		final Hashtable<String,String> vals=new Hashtable<String,String>();
		final int materialvalue=RawMaterial.CODES.VALUE(material);
		final int[] castMul=new int[1];
		final Ability[] RET=getTimsAdjResCast(I,castMul);
		final Ability ADJ=RET[0];
		final Ability CAST=RET[2];
		level-=levelsFromAbility(I);
		level-=levelsFromAdjuster(I,ADJ);
		level-=levelsFromCaster(I,CAST);

		if(I instanceof Weapon)
		{
			int baseattack=(int)Math.round(this.timsBaseAttackModifier(wclass));
			int basereach=0;
			int maxreach=0;
			int basematerial=RawMaterial.MATERIAL_WOODEN;
			switch(wclass)
			{
			case Weapon.CLASS_POLEARM:
			{
				basereach = 1;
				basematerial = RawMaterial.MATERIAL_METAL;
				break;
			}
			case Weapon.CLASS_RANGED:
			{
				basereach = 1;
				maxreach = 5;
				break;
			}
			case Weapon.CLASS_THROWN:
			{
				basereach = 1;
				maxreach = 5;
				break;
			}
			case Weapon.CLASS_EDGED:
			{
				basematerial = RawMaterial.MATERIAL_METAL;
				break;
			}
			case Weapon.CLASS_DAGGER:
			{
				basematerial = RawMaterial.MATERIAL_METAL;
				break;
			}
			case Weapon.CLASS_SWORD:
			{
				basematerial = RawMaterial.MATERIAL_METAL;
				break;
			}
			}
			double dmgModifier = this.timsDmgModifier(wclass);
			int weight = I.basePhyStats().weight();
			if(weight<1)
				weight=8;
			if(weight>40)
				weight=40;
			if(basereach>maxreach)
				maxreach=basereach;
			if(reach<basereach)
			{
				reach=basereach;
				vals.put("MINRANGE",""+basereach);
				vals.put("MAXRANGE",""+maxreach);
			}
			else
			if(reach>basereach)
				basereach=reach;

			int damage=(int)Math.round((((level-1.0)/(((double)reach/(double)weight)+2.0) + ((double)weight-(double)baseattack)/5.0 -reach)*(((hands*2.0)+1.0)/2.0))*dmgModifier);
			final int cost=(int)Math.round(2.0*(((double)weight*(double)materialvalue)+((2.0*damage)+baseattack+(reach*10.0))*damage)/(hands+1.0));
			baseattack += (int)Math.round(level * this.timsAttackModifier(wclass));

			if(basematerial==RawMaterial.MATERIAL_METAL)
			{
				switch(material&RawMaterial.MATERIAL_MASK)
				{
				case RawMaterial.MATERIAL_MITHRIL:
				case RawMaterial.MATERIAL_METAL:
				case RawMaterial.MATERIAL_ENERGY:
					break;
				case RawMaterial.MATERIAL_WOODEN:
				case RawMaterial.MATERIAL_SYNTHETIC:
					damage-=4;
					baseattack-=0;
					break;
				case RawMaterial.MATERIAL_PRECIOUS:
					damage-=4;
					baseattack-=10;
					break;
				case RawMaterial.MATERIAL_LEATHER:
					damage-=6;
					baseattack-=10;
					break;
				case RawMaterial.MATERIAL_ROCK:
					damage-=2;
					baseattack-=10;
					break;
				case RawMaterial.MATERIAL_GLASS:
					damage-=4;
					baseattack-=20;
					break;
				case RawMaterial.MATERIAL_GAS:
				default:
					damage-=8;
					baseattack-=30;
					break;
				}
				switch(material)
				{
				case RawMaterial.RESOURCE_BALSA:
				case RawMaterial.RESOURCE_LIMESTONE:
				case RawMaterial.RESOURCE_FLINT:
					baseattack-=10;
					damage-=2;
					break;
				case RawMaterial.RESOURCE_CLAY:
					baseattack-=20;
					damage-=4;
					break;
				case RawMaterial.RESOURCE_BONE:
					baseattack+=20;
					damage+=4;
					break;
				case RawMaterial.RESOURCE_GRANITE:
				case RawMaterial.RESOURCE_OBSIDIAN:
				case RawMaterial.RESOURCE_IRONWOOD:
					baseattack+=10;
					damage+=2;
					break;
				case RawMaterial.RESOURCE_SAND:
				case RawMaterial.RESOURCE_COAL:
					baseattack-=40;
					damage-=8;
					break;
				}
			}
			if(basematerial==RawMaterial.MATERIAL_WOODEN)
			{
				switch(material&RawMaterial.MATERIAL_MASK)
				{
				case RawMaterial.MATERIAL_WOODEN:
				case RawMaterial.MATERIAL_ENERGY:
					break;
				case RawMaterial.MATERIAL_METAL:
				case RawMaterial.MATERIAL_MITHRIL:
					damage+=2;
					baseattack-=0;
					break;
				case RawMaterial.MATERIAL_PRECIOUS:
					damage+=2;
					baseattack-=10;
					break;
				case RawMaterial.MATERIAL_LEATHER:
				case RawMaterial.MATERIAL_SYNTHETIC:
					damage-=2;
					baseattack-=0;
					break;
				case RawMaterial.MATERIAL_ROCK:
					damage+=2;
					baseattack-=10;
					break;
				case RawMaterial.MATERIAL_GLASS:
					damage-=2;
					baseattack-=10;
					break;
				default:
					damage-=6;
					baseattack-=30;
					break;
				}
				switch(material)
				{
				case RawMaterial.RESOURCE_LIMESTONE:
				case RawMaterial.RESOURCE_FLINT:
					baseattack-=10;
					damage-=2;
					break;
				case RawMaterial.RESOURCE_CLAY:
					baseattack-=20;
					damage-=4;
					break;
				case RawMaterial.RESOURCE_BONE:
					baseattack+=20;
					damage+=4;
					break;
				case RawMaterial.RESOURCE_GRANITE:
				case RawMaterial.RESOURCE_OBSIDIAN:
					baseattack+=10;
					damage+=2;
					break;
				case RawMaterial.RESOURCE_SAND:
				case RawMaterial.RESOURCE_COAL:
					baseattack-=40;
					damage-=8;
					break;
				}
			}
			if(damage<=0)
				damage=1;

			vals.put("DAMAGE",""+damage);
			vals.put("ATTACK",""+baseattack);
			vals.put("VALUE",""+cost);
		}
		else
		if(I instanceof Armor)
		{
			final int[] leatherPoints={ 0, 0, 1, 5,10,16,23,31,40,49,58,67,76,85,94};
			final int[] clothPoints=  { 0, 3, 7,12,18,25,33,42,52,62,72,82,92,102};
			final int[] metalPoints=  { 0, 0, 0, 0, 1, 3, 5, 8,12,17,23,30,38,46,54,62,70,78,86,94};
			double pts=0.0;
			if(level<0)
				level=0;
			final int materialCode=material&RawMaterial.MATERIAL_MASK;
			int[] useArray=null;
			switch(materialCode)
			{
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_MITHRIL:
			case RawMaterial.MATERIAL_PRECIOUS:
			case RawMaterial.MATERIAL_ENERGY:
				useArray=metalPoints;
				break;
			case RawMaterial.MATERIAL_SYNTHETIC:
			case RawMaterial.MATERIAL_LEATHER:
			case RawMaterial.MATERIAL_GLASS:
			case RawMaterial.MATERIAL_ROCK:
			case RawMaterial.MATERIAL_WOODEN:
				useArray=leatherPoints;
				break;
			default:
				useArray=clothPoints;
				break;
			}
			if(level>=useArray[useArray.length-1])
				pts=useArray.length-2;
			else
			for(int i=0;i<useArray.length;i++)
			{
				final int lvl=useArray[i];
				if(lvl>level)
				{
					pts=i-1;
					break;
				}
			}

			double totalpts=0.0;
			double weightpts=0.0;
			double wornweights=0.0;
			final Wearable.CODES codes = Wearable.CODES.instance();
			for(int i=0;i<codes.location_strength_points().length-1;i++)
			{
				if(CMath.isSet(worndata,i))
				{
					totalpts+=(pts*codes.location_strength_points()[i+1]);
					wornweights+=codes.location_strength_points()[i+1];
					switch(materialCode)
					{
					case RawMaterial.MATERIAL_METAL:
					case RawMaterial.MATERIAL_MITHRIL:
					case RawMaterial.MATERIAL_PRECIOUS:
						weightpts+=codes.material_weight_points()[i+1][2];
						break;
					case RawMaterial.MATERIAL_LEATHER:
					case RawMaterial.MATERIAL_GLASS:
					case RawMaterial.MATERIAL_SYNTHETIC:
					case RawMaterial.MATERIAL_ROCK:
					case RawMaterial.MATERIAL_WOODEN:
						weightpts+=codes.material_weight_points()[i+1][1];
						break;
					case RawMaterial.MATERIAL_ENERGY:
						break;
					default:
						weightpts+=codes.material_weight_points()[i+1][0];
						break;
					}
					if(hands==1)
						break;
				}
			}
			final int cost=(int)Math.round(((pts*pts) + materialvalue)
									 * ( weightpts / 2.0));
			int armor=(int)Math.round(totalpts);
			switch(material)
			{
				case RawMaterial.RESOURCE_BALSA:
				case RawMaterial.RESOURCE_LIMESTONE:
				case RawMaterial.RESOURCE_FLINT:
					armor-=1;
					break;
				case RawMaterial.RESOURCE_CLAY:
					armor-=2;
					break;
				case RawMaterial.RESOURCE_BONE:
					armor+=2;
					break;
				case RawMaterial.RESOURCE_GRANITE:
				case RawMaterial.RESOURCE_OBSIDIAN:
				case RawMaterial.RESOURCE_IRONWOOD:
					armor+=1;
					break;
				case RawMaterial.RESOURCE_SAND:
				case RawMaterial.RESOURCE_COAL:
					armor-=4;
					break;
			}
			vals.put("ARMOR",""+armor);
			vals.put("VALUE",""+cost);
			vals.put("WEIGHT",""+(int)Math.round((armor)/wornweights*weightpts));
		}
		return vals;
	}

	@Override
	public void toneDownWeapon(Weapon W, Ability ADJ)
	{
		if(ADJ!=null)
		{
			final String oldTxt=ADJ.text();
			ADJ.setStat("TONEDOWN-WEAPON", "90%");
			if(!ADJ.text().equals(oldTxt))
			{
				W.recoverPhyStats();
				return;
			}
		}
		if(W.basePhyStats().damage()>=10)
			W.basePhyStats().setDamage((int)Math.round(CMath.mul(W.basePhyStats().damage(),0.9)));
		else
		if(W.basePhyStats().damage()>1)
			W.basePhyStats().setDamage(W.basePhyStats().damage()-1);
		if(W.basePhyStats().attackAdjustment()>=10)
			W.basePhyStats().setAttackAdjustment((int)Math.round(CMath.mul(W.basePhyStats().attackAdjustment(),0.9)));
		else
		if(W.basePhyStats().attackAdjustment()>1)
			W.basePhyStats().setAttackAdjustment(W.basePhyStats().attackAdjustment()-1);
		W.recoverPhyStats();
	}
	
	@Override
	public void toneDownArmor(Armor A, Ability ADJ)
	{
		boolean fixit=true;
		if(ADJ!=null)
		{
			final String oldTxt=ADJ.text();
			ADJ.setStat("TONEDOWN-ARMOR", "90%");
			if(!ADJ.text().equals(oldTxt))
				fixit=false;
		}
		if(fixit&&(A.basePhyStats().armor()>=10))
			A.basePhyStats().setArmor((int)Math.round(CMath.mul(A.basePhyStats().armor(),0.9)));
		else
		if(fixit&&(A.basePhyStats().armor()>1))
			A.basePhyStats().setArmor(A.basePhyStats().armor()-1);
		A.recoverPhyStats();
	}

	public int[] getItemLevels(Item I, Ability ADJ, Ability RES, Ability CAST)
	{
		final int[] LVLS=new int[4];
		LVLS[0]=timsBaseLevel(I,ADJ);
		LVLS[1]=levelsFromAbility(I);
		LVLS[2]=levelsFromCaster(I,CAST);
		LVLS[3]=levelsFromAdjuster(I,ADJ);
		return LVLS;
	}

	public int totalLevels(int[] levels)
	{
		int lvl=levels[0];
		for(int i=1;i<levels.length;i++)
			lvl+=levels[i];
		return lvl;
	}

	@Override
	public int timsBaseLevel(Item I)
	{
		final Ability[] RET=getTimsAdjResCast(I,new int[1]);
		return timsBaseLevel(I,RET[0]);
	}

	public int timsBaseLevel(Item I, Ability ADJ)
	{
		int level=0;
		int otherDam=0;
		int otherAtt=0;
		int otherArm=0;
		if(ADJ!=null)
		{
			otherArm=-CMath.s_int(ADJ.getStat("STAT-ARMOR"));
			otherAtt=CMath.s_int(ADJ.getStat("STAT-ATTACK"));
			otherDam=CMath.s_int(ADJ.getStat("STAT-DAMAGE"));
		}
		final int curArmor=I.basePhyStats().armor()+otherArm;
		final double curAttack=I.basePhyStats().attackAdjustment()+otherAtt;
		final double curDamage=I.basePhyStats().damage()+otherDam;
		if(I instanceof Weapon)
		{
			double weight=8;
			if(weight<1.0)
				weight=1.0;
			final double range=(I.maxRange());
			final int wclass = ((Weapon)I).weaponClassification();
			final double dmgMod = this.timsDmgModifier(wclass);
			final double dmgLevel = Math.floor(((2.0*curDamage/(2.0*(I.rawLogicalAnd()?2.0:1.0)+1.0)+(curAttack-weight)/5.0+range)*(range/weight+2.0)/dmgMod))+1;
			final double attackLevel = (curAttack - this.timsBaseAttackModifier(wclass)) / this.timsAttackModifier(wclass);
			level = (int)Math.round((dmgLevel + attackLevel) / 2.0);
		}
		else
		{
			final long worndata=I.rawProperLocationBitmap();
			double weightpts=0;
			final Wearable.CODES codes = Wearable.CODES.instance();
			for(int i=0;i<codes.location_strength_points().length-1;i++)
			{
				if(CMath.isSet(worndata,i))
				{
					weightpts+=codes.location_strength_points()[i+1];
					if(!I.rawLogicalAnd())
						break;
				}
			}
			final int[] leatherPoints={ 0, 0, 1, 5,10,16,23,31,40,49,58,67,76,85,94};
			final int[] clothPoints=  { 0, 3, 7,12,18,25,33,42,52,62,72,82,92,102};
			final int[] metalPoints=  { 0, 0, 0, 0, 1, 3, 5, 8,12,17,23,30,38,46,54,62,70,78,86,94};
			final int materialCode=I.material()&RawMaterial.MATERIAL_MASK;
			int[] useArray=null;
			switch(materialCode)
			{
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_MITHRIL:
			case RawMaterial.MATERIAL_PRECIOUS:
			case RawMaterial.MATERIAL_ENERGY:
				useArray=metalPoints;
				break;
			case RawMaterial.MATERIAL_SYNTHETIC:
			case RawMaterial.MATERIAL_LEATHER:
			case RawMaterial.MATERIAL_GLASS:
			case RawMaterial.MATERIAL_ROCK:
			case RawMaterial.MATERIAL_WOODEN:
				useArray=leatherPoints;
				break;
			case RawMaterial.MATERIAL_GAS:
			default:
				useArray=clothPoints;
				break;
			}
			int which=(int)Math.round(CMath.div(curArmor,weightpts)+1);
			if(which<0)
				which=0;
			if(which>=useArray.length)
				which=useArray.length-1;
			level=useArray[which];
		}
		return level;
	}

	@Override
	public int levelsFromAbility(Item savedI)
	{
		return savedI.basePhyStats().ability() * 5;
	}

	@Override
	public int levelsFromAdjuster(Item savedI, Ability ADJ)
	{
		int level=0;
		if(ADJ!=null)
		{
			level += CMath.s_int(ADJ.getStat("LEVEL"));
		}
		return level;
	}

	@Override
	public int levelsFromCaster(Item savedI, Ability CAST)
	{
		int level=0;
		if(CAST instanceof AbilityContainer)
		{
			final boolean haver = (CAST instanceof TriggeredAffect) ? CMath.bset(((TriggeredAffect)CAST).triggerMask(),TriggeredAffect.TRIGGER_GET) : false;
			for(Enumeration<Ability> a=((AbilityContainer)CAST).abilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				int mul=1;
				if(A.abstractQuality()==Ability.QUALITY_MALICIOUS)
					mul=-1;
				if(haver)
					level+=(mul*CMLib.ableMapper().lowestQualifyingLevel(A.ID()));
				else
					level+=(mul*CMLib.ableMapper().lowestQualifyingLevel(A.ID())/2);
			}
		}
		return level;
	}

	@SuppressWarnings("unchecked")
	public synchronized List<Ability> getCombatSpellSet()
	{
		List<Ability> spellSet=(List<Ability>)Resources.getResource("COMPLETE_SPELL_SET");
		if(spellSet==null)
		{
			spellSet=new Vector<Ability>();
			Ability A=null;
			for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
			{
				A=e.nextElement();
				if(((A.classificationCode()&(Ability.ALL_ACODES))==Ability.ACODE_SPELL))
				{
					final int lowLevel=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
					if((lowLevel>0)&&(lowLevel<25))
						spellSet.add(A);
				}
			}
			Resources.submitResource("COMPLETE_SPELL_SET",spellSet);
		}
		return spellSet;
	}

	public Ability getCombatSpell(boolean malicious)
	{
		final List<Ability> spellSet=getCombatSpellSet();
		int tries=0;
		while(((++tries)<1000))
		{
			final Ability A=spellSet.get(CMLib.dice().roll(1,spellSet.size(),-1));
			if(((malicious)&&(A.canTarget(Ability.CAN_MOBS))&&(A.enchantQuality()==Ability.QUALITY_MALICIOUS)))
				return A;
			if((!malicious)
			&&(A.canAffect(Ability.CAN_MOBS))
			&&(A.enchantQuality()!=Ability.QUALITY_MALICIOUS)
			&&(A.enchantQuality()!=Ability.QUALITY_INDIFFERENT))
				return A;
		}
		return null;
	}

	@Override
	public Item enchant(Item I, int pct)
	{
		if(CMLib.dice().rollPercentage()>pct)
			return I;
		int bump=0;
		while((CMLib.dice().rollPercentage()<=10)||(bump==0))
			bump=bump+((CMLib.dice().rollPercentage()<=80)?1:-1);
		if(bump<0)
			CMLib.flags().setRemovable(I,false);
		I.basePhyStats().setDisposition(I.basePhyStats().disposition()|PhyStats.IS_BONUS);
		I.recoverPhyStats();
		if(I instanceof Ammunition)
		{
			int lvlChange=bump*3;
			if(lvlChange<0)
				lvlChange=lvlChange*-1;
			I.basePhyStats().setLevel(I.basePhyStats().level()+lvlChange);
			switch(CMLib.dice().roll(1,2,0))
			{
			case 1:
			{
				final Ability A=CMClass.getAbility("Prop_WearAdjuster");
				if(A==null)
					return I;
				A.setMiscText("att"+((bump<0)?"":"+")+(bump*5)+" dam="+((bump<0)?"":"+")+bump);
				I.addNonUninvokableEffect(A);
				break;
			}
			case 2:
			{
				Ability A=null;
				if(bump<0)
					A=CMClass.getAbility("Prayer_CurseItem");
				else
				{
					A=CMClass.getAbility("Prop_FightSpellCast");
					if(A!=null)
					for(int a=0;a<bump;a++)
					{
						final Ability A2=getCombatSpell(true);
						if(A2!=null)
							A.setMiscText(A.text()+";"+A2.ID());
					}
				}
				if(A==null)
					return I;
				I.addNonUninvokableEffect(A);
				break;
			}
			}
			I.recoverPhyStats();
		}
		else
		if(I instanceof Weapon)
		{
			switch(CMLib.dice().roll(1,2,0))
			{
			case 1:
			{
				I.basePhyStats().setAbility(bump);
				break;
			}
			case 2:
			{
				Ability A=null;
				if(bump<0)
					A=CMClass.getAbility("Prayer_CurseItem");
				else
				{
					A=CMClass.getAbility("Prop_FightSpellCast");
					if(A!=null)
					for(int a=0;a<bump;a++)
					{
						final Ability A2=getCombatSpell(true);
						if(A2!=null)
							A.setMiscText(A.text()+";"+A2.ID());
					}
					I.basePhyStats().setLevel(I.basePhyStats().level()+levelsFromCaster(I,A));
				}
				if(A==null)
					return I;
				I.addNonUninvokableEffect(A);
				break;
			}
			}
			I.recoverPhyStats();
		}
		else
		if(I instanceof Armor)
		{
			switch(CMLib.dice().roll(1,2,0))
			{
			case 1:
			{
				I.basePhyStats().setAbility(bump);
				break;
			}
			case 2:
			{
				Ability A=null;
				if(bump<0)
					A=CMClass.getAbility("Prayer_CurseItem");
				else
				{
					A=CMClass.getAbility("Prop_WearSpellCast");
					if(A!=null)
					for(int a=0;a<bump;a++)
					{
						final Ability A2=getCombatSpell(false);
						if(A2!=null)
							A.setMiscText(A.text()+";"+A2.ID());
					}
					I.basePhyStats().setLevel(I.basePhyStats().level()+levelsFromCaster(I,A));
				}
				if(A==null)
					return I;
				I.addNonUninvokableEffect(A);
				break;
			}
			}
			I.recoverPhyStats();
		}
		return I;
	}

}
