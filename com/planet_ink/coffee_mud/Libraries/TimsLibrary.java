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
   Copyright 2006-2024 Bo Zimmerman

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
	@SuppressWarnings("rawtypes")
	private static TreeMap		cachedCalculations	= new TreeMap();
	@SuppressWarnings("rawtypes")
	private static TreeMap		cachedStats			= new TreeMap();
	private static Character	iTypeW				= Character.valueOf('W');
	private static Character	iTypeA				= Character.valueOf('A');

	@Override
	public String ID()
	{
		return "TimsLibrary";
	}

	@Override
	public int timsLevelCalculator(final Item I)
	{
		final List<Ability> props=getTimsAdjResCast(I);
		return timsLevelCalculator(I,props);
	}

	protected double getWeaponDmgModifierFromClass(final int weaponClass)
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

	protected double getWeaponAttackModifierFromClass(final int weaponClass)
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

	protected double getAttackModifierFromClass(final int weaponClass)
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected static TreeMap getOrAddToCache(final TreeMap m, final Object key)
	{
		TreeMap m1 = (TreeMap)m.get(key);
		if(m1 == null)
		{
			m1 = new TreeMap();
			m.put(key, m1);
		}
		return m1;
	}

	@SuppressWarnings("rawtypes")
	protected static TreeMap getLevelCache(final int... keys)
	{
		TreeMap m = TimsLibrary.cachedCalculations;
		for(final int key : keys)
			m = getOrAddToCache(m, Integer.valueOf(key));
		return m;
	}

	@SuppressWarnings("rawtypes")
	protected static TreeMap getStatCache(final int... keys)
	{
		TreeMap m = TimsLibrary.cachedStats;
		for(final int key : keys)
			m = getOrAddToCache(m, Integer.valueOf(key));
		return m;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int timsLevelCalculator(final Item itemI, final List<Ability> props)
	{
		int level=0;
		final Item savedI=itemI;
		savedI.recoverPhyStats();
		//IworkI=(Item)IworkI.copyOf();
		//IworkI.recoverPhyStats();
		// the item is only ever read, so why copy it?
		final int curArmor;
		final double curAttack;
		final double curDamage;
		final int[] adjustments = timsBaseAdjustments(savedI,getBaseAdjusterAbility(props));
		final Character ityp;
		@SuppressWarnings("rawtypes")
		final TreeMap cache;
		if(itemI instanceof Weapon)
		{
			ityp=TimsLibrary.iTypeW;
			final int wclass=((Weapon)savedI).weaponClassification();
			curArmor=0;
			curAttack=adjustments[1];
			curDamage=adjustments[2];
			final int iweight=CMath.minMax(savedI.basePhyStats().weight()<1?8:1, savedI.basePhyStats().weight(), 40);
			cache = getLevelCache(iweight,
								 wclass,
								 ((Weapon)savedI).getRanges()[1],
								 (itemI.rawLogicalAnd()?2:1),
								 adjustments[1],
								 adjustments[2]
								 );
			if(cache.containsKey(ityp))
				return ((Integer)cache.get(ityp)).intValue();
			level = timsBaseLevel(savedI,adjustments);
		}
		else
		{
			ityp=TimsLibrary.iTypeA;
			curArmor=adjustments[0];
			curAttack=0;
			curDamage=0;
			final long worndata=savedI.rawProperLocationBitmap();
			final int materialCode=savedI.material()&RawMaterial.MATERIAL_MASK;
			cache = getLevelCache(
								materialCode,
								(itemI.rawLogicalAnd()?2:1),
								curArmor,
								(int)(worndata & 0xffffffff),
								(int)((worndata >> 32) & 0xffffffff)
								);
			if(cache.containsKey(ityp))
				return ((Integer)cache.get(ityp)).intValue();
			level = timsBaseLevel(savedI,adjustments);
		}
		if(level < 1)
			level = 1;
		level+=itemI.basePhyStats().ability()*5;
		for(final Ability A : props)
			level += CMath.s_int(A.getStat("STAT-LEVEL"));
		/** begin */
		{
			int hands=0;
			int weaponClass=0;
			final int maxRange;
			if(savedI instanceof Weapon)
			{
				hands=savedI.rawLogicalAnd()?2:1;
				weaponClass=((Weapon)savedI).weaponClassification();
				maxRange=((Weapon)savedI).getRanges()[1];
			}
			else
				maxRange=savedI.maxRange();
			int tries = 60;
			double lastDiff=Double.MAX_VALUE;
			int diffCode = 0;
			boolean noDouble=false;
			boolean noHalf=false;
			final double curScore=(curAttack + curDamage);
			while(--tries>0)
			{
				final Map<String,String> H=timsItemAdjustments(savedI,level,savedI.material(),
															   hands,weaponClass,maxRange,savedI.rawProperLocationBitmap());
				if(savedI instanceof Armor)
				{
					final int newArmor = CMath.s_int(H.get("ARMOR"));
					final double newDiff = Math.abs(newArmor-curArmor);
					if(newArmor < curArmor)
					{
						if(diffCode == 1)
						{
							level = (lastDiff < newDiff) ? (level+1) : level;
							break;
						}
						else
						if((!noDouble)
						&&((newArmor+newArmor) < curArmor))
						{
							level *= 2;
							if(level > CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL)*1024)
								break;
							diffCode=0;
						}
						else
							diffCode = -1;
						noHalf=true;
						level += 1;
					}
					else
					if(newArmor > curArmor)
					{
						if(diffCode == -1)
						{
							level = (lastDiff < newDiff) ? (level-1) : level;
							break;
						}
						else
						if((!noHalf)
						&&((newArmor/2) > curArmor))
						{
							if(level < 2)
								break;
							level /= 2;
							if(level < 2)
								level = 1;
							diffCode=0;
						}
						else
							diffCode = 1;
						noDouble=true;
						level -= 1;
						if(level < 1)
						{
							level = 1;
							break;
						}
					}
					else
						break;
					lastDiff = newDiff;
				}
				else
				if(savedI instanceof Weapon)
				{
					final int newAttack = CMath.s_int(H.get("ATTACK"));
					final int newDmg = CMath.s_int(H.get("DAMAGE"));
					final double attackDiff = Math.abs(newAttack-curAttack);
					final double damageDiff = Math.abs(newDmg-curDamage);
					final double newDiff = attackDiff + damageDiff;
					final int newScore=(newAttack + newDmg);
					if(newScore < curScore)
					{
						if(diffCode == 1)
						{
							level = (lastDiff < newDiff) ? (level+1) : level;
							break;
						}
						else
						if((!noDouble)
						&&((newScore+newScore)) < curScore)
						{
							level *= 2;
							if(level > CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL)*1024)
								break;
							diffCode=0;
						}
						else
							diffCode = -1;
						noHalf=true;
						level += 1;
					}
					else
					if(newScore > curScore)
					{
						if(diffCode == -1)
						{
							level = (lastDiff < newDiff) ? (level-1) : level;
							break;
						}
						else
						if((!noHalf)
						&&((newScore/2) > curScore))
						{
							if(level < 2)
								break;
							level /= 2;
							if(level < 2)
								level = 1;
							diffCode=0;
						}
						else
							diffCode = 1;
						noDouble=true;
						level -= 1;
						if(level < 1)
						{
							level = 1;
							break;
						}
					}
					else
						break;
					lastDiff = newDiff;
				}
				else
					break;
			}
			cache.put(ityp, Integer.valueOf(level));
		}
		//savedI.destroy();
		//IworkI.destroy(); // this was a copy
		return level;
	}

	protected boolean fixRejuvItem(final Item I)
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
	public List<Ability> getTimsAdjResCast(final Item I)
	{
		final List<Ability> props=new Vector<Ability>();
		Ability A;
		for(int i=0;i<I.numEffects();i++)
		{
			A=I.fetchEffect( i );
			if(A instanceof TriggeredAffect)
			{
				final long flags=A.flags();
				final int triggers=((TriggeredAffect) A).triggerMask();
				if( CMath.bset( flags, Ability.FLAG_ADJUSTER )
				&& (( triggers&(TriggeredAffect.TRIGGER_ALWAYS|TriggeredAffect.TRIGGER_WEAR_WIELD|TriggeredAffect.TRIGGER_GET|TriggeredAffect.TRIGGER_MOUNT ))>0))
					props.add(A);
				else
				if( CMath.bset( flags, Ability.FLAG_RESISTER )
				&& (( triggers&(TriggeredAffect.TRIGGER_ALWAYS|TriggeredAffect.TRIGGER_WEAR_WIELD|TriggeredAffect.TRIGGER_GET|TriggeredAffect.TRIGGER_MOUNT ))>0))
					props.add(A);
				else
				if( CMath.bset( flags, Ability.FLAG_ZAPPER )
				&& (( triggers&(TriggeredAffect.TRIGGER_ALWAYS|TriggeredAffect.TRIGGER_WEAR_WIELD|TriggeredAffect.TRIGGER_GET|TriggeredAffect.TRIGGER_MOUNT ))>0))
					props.add(A);
				else
				if( CMath.bset( flags, Ability.FLAG_ENABLER )
				&& (( triggers&(TriggeredAffect.TRIGGER_ALWAYS|TriggeredAffect.TRIGGER_WEAR_WIELD|TriggeredAffect.TRIGGER_GET|TriggeredAffect.TRIGGER_MOUNT ))>0))
					props.add(A);
				else
				if( CMath.bset( flags, Ability.FLAG_CASTER )
				&& (triggers > 0))
					props.add(A);
			}
		}
		return props;
	}

	private void reportChangesDestroyOldI(final Item oldI, final Item newI, final StringBuffer changes,final int OTLVL, final int TLVL)
	{
		if((changes == null)||(oldI==null))
			return;
		final List<Ability> props=getTimsAdjResCast(newI);
		final Map<Object,Integer> levels=getItemLevels(newI,props);
		final int TLVL2=totalLevels(levels);

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
	public boolean itemFix(final Item I, final int lvlOr0, final boolean preferMagic, final StringBuffer changes)
	{
		final Item oldI = (changes!=null)?(Item)I.copyOf():null;

		if((I instanceof SpellHolder)
		||((I instanceof Wand)&&(lvlOr0<=0)))
		{
			final List<Ability> spells=new ArrayList<Ability>();
			if(I instanceof SpellHolder)
				spells.addAll(((SpellHolder)I).getSpells());
			else
			if((I instanceof Wand)&&(((Wand)I).getSpell()!=null))
				spells.add(((Wand)I).getSpell());
			if(spells.size()==0)
				return false;
			int levels=0;
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
			final List<Ability> props=getTimsAdjResCast(I);
			Map<Object,Integer> levelsMap=getItemLevels(I,props);
			int powLevel=totalLevels(levelsMap);
			final int OTLVL=powLevel;
			if(lvl<0)
			{
				if(powLevel<=0)
					lvl=1;
				else
					lvl=powLevel;
				I.basePhyStats().setLevel(lvl);
				I.recoverPhyStats();
				fixRejuvItem(I);
				if(CMLib.flags().isCataloged(I))
					CMLib.catalog().updateCatalog(I);
				reportChangesDestroyOldI(oldI,I,changes,OTLVL,powLevel);
				return true;
			}
			if((powLevel>0)&&(powLevel>Math.round(CMath.mul(lvl,1.1))))
			{
				//int FTLVL=TLVL;
				final Set<Object> illegalThings=new HashSet<Object>();
				//Log.sysOut("Reset",I.name()+"("+I.basePhyStats().level()+") "+TLVL+", "+I.basePhyStats().armor()+"/"+I.basePhyStats().attackAdjustment()+"/"+I.basePhyStats().damage()+"/"+((adjA!=null)?adjA.text():"null"));
				final long timeOut=System.currentTimeMillis()+5000;
				while((powLevel>Math.round(CMath.mul(lvl,1.1)))
				&&(illegalThings.size()<levelsMap.size())
				&&(System.currentTimeMillis()<timeOut))
				{
					Object highestObject=null;
					for(final Object o : levelsMap.keySet())
					{
						if((highestObject==null)
						||((levelsMap.get(o).intValue() > levelsMap.get(highestObject).intValue())
							&&(!preferMagic)||(highestObject instanceof Ability)))
						{
							if(!illegalThings.contains(o))
								highestObject=o;
						}
					}
					if(highestObject==null)
						break;
					if(highestObject instanceof Weapon)
					{
						final Ability adjA=getBaseAdjusterAbility(props);
						final String s=(adjA!=null)?adjA.text():"";
						final int oldAtt=I.basePhyStats().attackAdjustment();
						final int oldDam=I.basePhyStats().damage();
						toneDownWeapon((Weapon)I,adjA);
						if((I.basePhyStats().attackAdjustment()==oldAtt)
						&&(I.basePhyStats().damage()==oldDam)
						&&((adjA==null)||(adjA.text().equals(s))))
							illegalThings.add(highestObject);
					}
					else
					if(highestObject instanceof Item)
					{
						final Ability adjA=getBaseAdjusterAbility(props);
						final String s=(adjA!=null)?adjA.text():"";
						final int oldArm=I.basePhyStats().armor();
						toneDownArmor((Armor)I,adjA);
						if((I.basePhyStats().armor()==oldArm)
						&&((adjA==null)||(adjA.text().equals(s))))
							illegalThings.add(highestObject);
					}
					else
					if((highestObject instanceof String)
					&&(((String)highestObject).equalsIgnoreCase("ABILITY")))
					{
						if(I.basePhyStats().ability()>0)
							I.basePhyStats().setAbility(I.basePhyStats().ability()-1);
						else
							illegalThings.add("ABILITY");
					}
					else
					if(highestObject instanceof Ability)
					{
						final Ability A=(Ability)highestObject;
						final int prevLevel=CMath.s_int(A.getStat("STAT-LEVEL"));
						A.setStat("TONEDOWN-MISC", "90%");
						if(CMath.s_int(A.getStat("STAT-LEVEL"))==prevLevel)
						{
							A.setStat("TONEDOWN-MISC", "90%");
							if(CMath.s_int(A.getStat("STAT-LEVEL"))==prevLevel)
							{
								A.setStat("TONEDOWN-MISC", "50%");
								if(CMath.s_int(A.getStat("STAT-LEVEL"))==prevLevel)
									illegalThings.add(highestObject);
							}
						}
					}
					else
					{
						Log.errOut(ID(),"What is "+highestObject+"??");
						illegalThings.add(highestObject);
					}
					levelsMap=getItemLevels(I,props);
					powLevel=totalLevels(levelsMap);
				}
				//Log.sysOut("Reset",I.name()+"("+I.basePhyStats().level()+") "+FTLVL+"->"+TLVL+", "+I.basePhyStats().armor()+"/"+I.basePhyStats().attackAdjustment()+"/"+I.basePhyStats().damage()+"/"+((adjA!=null)?adjA.text():"null"));
				fixRejuvItem(I);
				if(CMLib.flags().isCataloged(I))
					CMLib.catalog().updateCatalog(I);
				reportChangesDestroyOldI(oldI,I,changes,OTLVL,powLevel);
				return true;
			}
			else
			if(powLevel<Math.round(CMath.mul(lvl,0.9)))
			{
				//int FTLVL=TLVL;
				final Set<Object> illegalThings=new HashSet<Object>();
				//Log.sysOut("Reset",I.name()+"("+I.basePhyStats().level()+") "+TLVL+", "+I.basePhyStats().armor()+"/"+I.basePhyStats().attackAdjustment()+"/"+I.basePhyStats().damage()+"/"+((adjA!=null)?adjA.text():"null"));
				final long timeOut=System.currentTimeMillis()+5000;
				while((powLevel<Math.round(CMath.mul(lvl,0.9)))
				&&(illegalThings.size()<levelsMap.size())
				&&(System.currentTimeMillis()<timeOut))
				{
					Object lowestObject=null;
					for(final Object o : levelsMap.keySet())
					{
						if((lowestObject==null)
						||((levelsMap.get(o).intValue() < levelsMap.get(lowestObject).intValue())
							&&((!preferMagic)||(lowestObject instanceof Ability))))
						{
							if(!illegalThings.contains(o))
								lowestObject=o;
						}
					}
					if(lowestObject==null)
						break;
					if(lowestObject instanceof Weapon)
					{
						final Ability adjA=getBaseAdjusterAbility(props);
						final String s=(adjA!=null)?adjA.text():"";
						final int oldAtt=I.basePhyStats().attackAdjustment();
						final int oldDam=I.basePhyStats().damage();
						toneUpWeapon((Weapon)I,adjA);
						if((I.basePhyStats().attackAdjustment()==oldAtt)
						&&(I.basePhyStats().damage()==oldDam)
						&&((adjA==null)||(adjA.text().equals(s))))
							illegalThings.add(lowestObject);
					}
					else
					if(lowestObject instanceof Item)
					{
						final Ability adjA=getBaseAdjusterAbility(props);
						final String s=(adjA!=null)?adjA.text():"";
						final int oldArm=I.basePhyStats().armor();
						toneUpArmor((Armor)I,adjA);
						if((I.basePhyStats().armor()==oldArm)
						&&((adjA==null)||(adjA.text().equals(s))))
							illegalThings.add(lowestObject);
						levelsMap=getItemLevels(I,props);
					}
					else
					if((lowestObject instanceof String)
					&&(((String)lowestObject).equalsIgnoreCase("ABILITY")))
					{
						if(I.basePhyStats().ability()>0)
							I.basePhyStats().setAbility(I.basePhyStats().ability()-1);
						else
							illegalThings.add("ABILITY");
					}
					else
					if(lowestObject instanceof Ability)
					{
						final Ability A=(Ability)lowestObject;
						final int prevLevel=CMath.s_int(A.getStat("STAT-LEVEL"));
						A.setStat("TONEUP-MISC", "110%");
						if(CMath.s_int(A.getStat("STAT-LEVEL"))==prevLevel)
						{
							A.setStat("TONEUP-MISC", "110%");
							if(CMath.s_int(A.getStat("STAT-LEVEL"))==prevLevel)
							{
								A.setStat("TONEUP-MISC", "150%");
								if(CMath.s_int(A.getStat("STAT-LEVEL"))==prevLevel)
									illegalThings.add(lowestObject);
							}
						}
					}
					else
					{
						Log.errOut(ID(),"What is "+lowestObject+"??");
						illegalThings.add(lowestObject);
					}
					levelsMap=getItemLevels(I,props);
					powLevel=totalLevels(levelsMap);
				}
				//Log.sysOut("Reset",I.name()+"("+I.basePhyStats().level()+") "+FTLVL+"->"+TLVL+", "+I.basePhyStats().armor()+"/"+I.basePhyStats().attackAdjustment()+"/"+I.basePhyStats().damage()+"/"+((adjA!=null)?adjA.text():"null"));
				fixRejuvItem(I);
				if(CMLib.flags().isCataloged(I))
					CMLib.catalog().updateCatalog(I);
				reportChangesDestroyOldI(oldI,I,changes,OTLVL,powLevel);
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
	public boolean toneDownValue(final Item I)
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
	public void balanceItemByLevel(final Item I)
	{
		int hands=0;
		int weaponClass=0;
		final int maxRange;
		if(I instanceof Weapon)
		{
			hands=I.rawLogicalAnd()?2:1;
			weaponClass=((Weapon)I).weaponClassification();
			maxRange=((Weapon)I).getRanges()[1];
		}
		else
			maxRange=I.maxRange();
		final Map<String,String> H=timsItemAdjustments(I,I.basePhyStats().level(),I.material(),hands,weaponClass,maxRange,I.rawProperLocationBitmap());
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

	protected int[] getArmorMaterialPointsArray(final int materialCode)
	{
		final int[] leatherPoints={ 0, 0, 1, 5,10,16,23,31,40,49,58,67,76,85,94};
		final int[] clothPoints=  { 0, 3, 7,12,18,25,33,42,52,62,72,82,92,102};
		final int[] metalPoints=  { 0, 0, 0, 0, 1, 3, 5, 8,12,17,23,30,38,46,54,62,70,78,86,94};
		int[] useArray=null;
		switch(materialCode & RawMaterial.MATERIAL_MASK)
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
		return useArray;
	}

	@Override
	public int calculateBaseValue(final Item I)
	{
		final int materialvalue=RawMaterial.CODES.VALUE(I.material());
		final int level = I.basePhyStats().level();
		final int hands = I.rawLogicalAnd()?2:1;
		if(I instanceof Weapon)
		{
			final int baseattack=I.basePhyStats().attackAdjustment();
			final int reach=((Weapon)I).maxRange();
			final int wclass = ((Weapon)I).weaponClassification();
			final int thrown = (wclass == Weapon.CLASS_THROWN) ? 1 : 0;
			final int weight = I.basePhyStats().weight();
			final int damage=I.basePhyStats().damage();
			final int cost=(int)Math.round(2.0*(((double)weight*(double)materialvalue)+((2.0*damage)+baseattack+(reach*10.0))*damage)/((hands+1.0)*(thrown+1.0)));
			return cost;
		}
		else
		if(I instanceof Armor)
		{
			final double pts=getMaterialArmorPoints(I.material(), level);
			final int cost=(int)Math.round(((pts*pts) + materialvalue) * ( I.basePhyStats().weight() / 2.0));
			return cost;
		}
		return I.baseGoldValue();
	}

	protected int getMaterialArmorAdj(final int material)
	{
		switch(material)
		{
			case RawMaterial.RESOURCE_BALSA:
			case RawMaterial.RESOURCE_LIMESTONE:
			case RawMaterial.RESOURCE_FLINT:
				return -1;
			case RawMaterial.RESOURCE_CLAY:
				return -2;
			case RawMaterial.RESOURCE_BONE:
				return 2;
			case RawMaterial.RESOURCE_GRANITE:
			case RawMaterial.RESOURCE_OBSIDIAN:
			case RawMaterial.RESOURCE_IRONWOOD:
				return 1;
			case RawMaterial.RESOURCE_SAND:
			case RawMaterial.RESOURCE_COAL:
				return -4;
			default:
				return 0;
		}
	}

	protected double getLocationArmorWeight(final long wornLocs, final int materialCode, final int hands)
	{
		double weightPts = 0;
		final Wearable.CODES codes = Wearable.CODES.instance();
		for(int i=0;i<codes.location_strength_points().length-1;i++)
		{
			if(CMath.isSet(wornLocs,i))
			{
				switch(materialCode)
				{
				case RawMaterial.MATERIAL_METAL:
				case RawMaterial.MATERIAL_MITHRIL:
				case RawMaterial.MATERIAL_PRECIOUS:
					weightPts+=codes.material_weight_item()[i+1][2];
					break;
				case RawMaterial.MATERIAL_LEATHER:
				case RawMaterial.MATERIAL_GLASS:
				case RawMaterial.MATERIAL_SYNTHETIC:
				case RawMaterial.MATERIAL_ROCK:
				case RawMaterial.MATERIAL_WOODEN:
					weightPts+=codes.material_weight_item()[i+1][1];
					break;
				case RawMaterial.MATERIAL_ENERGY:
					break;
				default:
					weightPts+=codes.material_weight_item()[i+1][0];
					break;
				}
				if(hands==1)
					break;
			}
		}
		return weightPts;
	}

	protected double getMaterialArmorPoints(final int material, final int level)
	{
		double matPoints=0.0;
		final int[] useArray = getArmorMaterialPointsArray(material);
		if(level>=useArray[useArray.length-1])
			matPoints=useArray.length-2 + ((level-useArray[useArray.length-1])/(useArray[useArray.length-1]-useArray[useArray.length-2]));
		else
		for(int i=0;i<useArray.length;i++)
		{
			final int lvl=useArray[i];
			if(lvl>level)
			{
				matPoints=i-1;
				break;
			}
		}
		return matPoints;
	}

	protected double getLocationArmorPoints(final long wornLocs, final double matPoints, final int hands)
	{
		double totalPts = 0;
		final Wearable.CODES codes = Wearable.CODES.instance();
		for(int i=0;i<codes.location_strength_points().length-1;i++)
		{
			if(CMath.isSet(wornLocs,i))
			{
				totalPts+=(matPoints*codes.location_strength_points()[i+1]);
				if(hands==1)
					break;
			}
		}
		return totalPts;
	}

	protected int getWeaponBaseMaterialFromClass(final int wclass)
	{
		switch(wclass)
		{
		case Weapon.CLASS_POLEARM:
			return RawMaterial.MATERIAL_METAL;
		case Weapon.CLASS_EDGED:
			return RawMaterial.MATERIAL_METAL;
		case Weapon.CLASS_DAGGER:
			return RawMaterial.MATERIAL_METAL;
		case Weapon.CLASS_SWORD:
			return RawMaterial.MATERIAL_METAL;
		default:
			return RawMaterial.MATERIAL_WOODEN;
		}
	}

	protected int getWeaponReachFromClass(final int wclass, final int reach, final Map<String,String> vals)
	{
		int baseReach = 0;
		switch(wclass)
		{
		case Weapon.CLASS_POLEARM:
			baseReach = 1;
			break;
		case Weapon.CLASS_RANGED:
			baseReach = 1;
			break;
		case Weapon.CLASS_THROWN:
			baseReach = 1;
			break;
		default:
			break;
		}
		int maxReach = 0;
		switch(wclass)
		{
		case Weapon.CLASS_RANGED:
			maxReach = 5;
			break;
		case Weapon.CLASS_THROWN:
			maxReach = 5;
			break;
		default:
			break;
		}
		if(baseReach>maxReach)
			maxReach=baseReach;
		if(reach>baseReach)
			baseReach=reach;
		else
		if(reach<baseReach)
		{
			if(vals != null)
			{
				vals.put("MINRANGE",""+baseReach);
				vals.put("MAXRANGE",""+maxReach);
			}
			return baseReach;
		}
		return reach;
	}

	protected int getWeaponDmgAdjFromClass(final int wclass, final int material)
	{
		final int baseMatTyp=getWeaponBaseMaterialFromClass(wclass);
		int damage = 0;
		if(baseMatTyp==RawMaterial.MATERIAL_METAL)
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
				break;
			case RawMaterial.MATERIAL_PRECIOUS:
				damage-=4;
				break;
			case RawMaterial.MATERIAL_LEATHER:
				damage-=6;
				break;
			case RawMaterial.MATERIAL_ROCK:
				damage-=2;
				break;
			case RawMaterial.MATERIAL_GLASS:
				damage-=4;
				break;
			case RawMaterial.MATERIAL_GAS:
			default:
				damage-=8;
				break;
			}
			switch(material)
			{
			case RawMaterial.RESOURCE_BALSA:
			case RawMaterial.RESOURCE_LIMESTONE:
			case RawMaterial.RESOURCE_FLINT:
				damage-=2;
				break;
			case RawMaterial.RESOURCE_CLAY:
				damage-=4;
				break;
			case RawMaterial.RESOURCE_BONE:
				damage+=4;
				break;
			case RawMaterial.RESOURCE_GRANITE:
			case RawMaterial.RESOURCE_OBSIDIAN:
			case RawMaterial.RESOURCE_IRONWOOD:
				damage+=2;
				break;
			case RawMaterial.RESOURCE_SAND:
			case RawMaterial.RESOURCE_COAL:
				damage-=8;
				break;
			}
		}
		if(baseMatTyp==RawMaterial.MATERIAL_WOODEN)
		{
			switch(material&RawMaterial.MATERIAL_MASK)
			{
			case RawMaterial.MATERIAL_WOODEN:
			case RawMaterial.MATERIAL_ENERGY:
				break;
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_MITHRIL:
				damage+=2;
				break;
			case RawMaterial.MATERIAL_PRECIOUS:
				damage+=2;
				break;
			case RawMaterial.MATERIAL_LEATHER:
			case RawMaterial.MATERIAL_SYNTHETIC:
				damage-=2;
				break;
			case RawMaterial.MATERIAL_ROCK:
				damage+=2;
				break;
			case RawMaterial.MATERIAL_GLASS:
				damage-=2;
				break;
			default:
				damage-=6;
				break;
			}
			switch(material)
			{
			case RawMaterial.RESOURCE_LIMESTONE:
			case RawMaterial.RESOURCE_FLINT:
				damage-=2;
				break;
			case RawMaterial.RESOURCE_CLAY:
				damage-=4;
				break;
			case RawMaterial.RESOURCE_BONE:
				damage+=4;
				break;
			case RawMaterial.RESOURCE_GRANITE:
			case RawMaterial.RESOURCE_OBSIDIAN:
				damage+=2;
				break;
			case RawMaterial.RESOURCE_SAND:
			case RawMaterial.RESOURCE_COAL:
				damage-=8;
				break;
			}
		}
		return damage;
	}

	protected int getWeaponAttackAdjFromClass(final int wclass, final int material)
	{
		final int baseMatTyp=getWeaponBaseMaterialFromClass(wclass);
		int baseAttack = 0;
		if(baseMatTyp==RawMaterial.MATERIAL_METAL)
		{
			switch(material&RawMaterial.MATERIAL_MASK)
			{
			case RawMaterial.MATERIAL_MITHRIL:
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_ENERGY:
				break;
			case RawMaterial.MATERIAL_WOODEN:
			case RawMaterial.MATERIAL_SYNTHETIC:
				baseAttack-=0;
				break;
			case RawMaterial.MATERIAL_PRECIOUS:
				baseAttack-=10;
				break;
			case RawMaterial.MATERIAL_LEATHER:
				baseAttack-=10;
				break;
			case RawMaterial.MATERIAL_ROCK:
				baseAttack-=10;
				break;
			case RawMaterial.MATERIAL_GLASS:
				baseAttack-=20;
				break;
			case RawMaterial.MATERIAL_GAS:
			default:
				baseAttack-=30;
				break;
			}
			switch(material)
			{
			case RawMaterial.RESOURCE_BALSA:
			case RawMaterial.RESOURCE_LIMESTONE:
			case RawMaterial.RESOURCE_FLINT:
				baseAttack-=10;
				break;
			case RawMaterial.RESOURCE_CLAY:
				baseAttack-=20;
				break;
			case RawMaterial.RESOURCE_BONE:
				baseAttack+=20;
				break;
			case RawMaterial.RESOURCE_GRANITE:
			case RawMaterial.RESOURCE_OBSIDIAN:
			case RawMaterial.RESOURCE_IRONWOOD:
				baseAttack+=10;
				break;
			case RawMaterial.RESOURCE_SAND:
			case RawMaterial.RESOURCE_COAL:
				baseAttack-=40;
				break;
			}
		}
		if(baseMatTyp==RawMaterial.MATERIAL_WOODEN)
		{
			switch(material&RawMaterial.MATERIAL_MASK)
			{
			case RawMaterial.MATERIAL_WOODEN:
			case RawMaterial.MATERIAL_ENERGY:
				break;
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_MITHRIL:
				baseAttack-=0;
				break;
			case RawMaterial.MATERIAL_PRECIOUS:
				baseAttack-=10;
				break;
			case RawMaterial.MATERIAL_LEATHER:
			case RawMaterial.MATERIAL_SYNTHETIC:
				baseAttack-=0;
				break;
			case RawMaterial.MATERIAL_ROCK:
				baseAttack-=10;
				break;
			case RawMaterial.MATERIAL_GLASS:
				baseAttack-=10;
				break;
			default:
				baseAttack-=30;
				break;
			}
			switch(material)
			{
			case RawMaterial.RESOURCE_LIMESTONE:
			case RawMaterial.RESOURCE_FLINT:
				baseAttack-=10;
				break;
			case RawMaterial.RESOURCE_CLAY:
				baseAttack-=20;
				break;
			case RawMaterial.RESOURCE_BONE:
				baseAttack+=20;
				break;
			case RawMaterial.RESOURCE_GRANITE:
			case RawMaterial.RESOURCE_OBSIDIAN:
				baseAttack+=10;
				break;
			case RawMaterial.RESOURCE_SAND:
			case RawMaterial.RESOURCE_COAL:
				baseAttack-=40;
				break;
			}
		}
		return baseAttack;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> timsItemAdjustments(final Item I,
												   int level,
												   final int material,
												   final int hands,
												   final int wclass,
												   int reach,
												   final long worndata)
	{
		final Hashtable<String,String> vals=new Hashtable<String,String>(); // return obj
		final int materialvalue=RawMaterial.CODES.VALUE(material);
		final List<Ability> props=getTimsAdjResCast(I);
		level-=levelsFromAbility(I);
		for(final Ability A : props)
			level-=CMath.s_int(A.getStat("STAT-LEVEL"));

		final int iweight = CMath.minMax(I.basePhyStats().weight()<1?8:1, I.basePhyStats().weight(), 40);
		@SuppressWarnings("rawtypes")
		final TreeMap m = TimsLibrary.getStatCache(material,wclass,reach,hands,iweight,
										(int)(worndata&0xffffff),(int)((worndata>>32)&0xffffff),level);
		if(I instanceof Weapon)
		{
			if(m.containsKey(TimsLibrary.iTypeW))
			{
				final Map<String,String> v=(Map<String,String>)m.get(TimsLibrary.iTypeW);
				return v;
			}
			int baseAttack=(int)Math.round(getWeaponAttackModifierFromClass(wclass));
			reach=getWeaponReachFromClass(wclass, reach, vals);
			final double thrown = (wclass == Weapon.CLASS_THROWN) ? 1 : 0;
			final double dmgModifier = getWeaponDmgModifierFromClass(wclass);
			final double weight = iweight;
			int damage=(int)Math.round((((level-1.0)/((reach/weight)+2.0) + (weight-baseAttack)/5.0 -reach)*(((hands*2.0)+1.0)/2.0))*dmgModifier);
			baseAttack += (int)Math.round(level * getAttackModifierFromClass(wclass));
			baseAttack += getWeaponAttackAdjFromClass(wclass, material);
			damage += getWeaponDmgAdjFromClass(wclass, material);
			if(damage<=0)
				damage=1;

			final int cost=(int)Math.round(2.0*((weight*materialvalue)+((2.0*damage)+baseAttack+(reach*10.0))*damage)/((hands+1.0)*(thrown+1.0)));

			vals.put("DAMAGE",""+damage);
			vals.put("ATTACK",""+baseAttack);
			vals.put("VALUE",""+cost);
			m.put(iTypeW, vals);
		}
		else
		if(I instanceof Armor)
		{
			if(level<0)
				level=0;
			if(m.containsKey(TimsLibrary.iTypeA))
			{
				final Map<String,String> v=(Map<String,String>)m.get(TimsLibrary.iTypeA);
				return v;
			}
			final double matPoints = getMaterialArmorPoints(material, level);
			final int materialCode=material&RawMaterial.MATERIAL_MASK;
			final double totalpts=getLocationArmorPoints(worndata, matPoints, hands);
			final double weightpts=getLocationArmorWeight(worndata, materialCode, hands);
			final int cost=(int)Math.round(((matPoints*matPoints) + materialvalue) * ( weightpts / 2.0));
			int armor=(int)Math.round(totalpts);
			armor -= getMaterialArmorAdj(material);
			vals.put("ARMOR",""+armor);
			vals.put("VALUE",""+cost);
			vals.put("WEIGHT",""+(int)Math.round(weightpts));
			m.put(iTypeA, vals);
		}
		return vals;
	}

	@Override
	public void toneDownWeapon(final Weapon W, final Ability adjA)
	{
		if(adjA!=null)
		{
			final String oldTxt=adjA.text();
			adjA.setStat("TONEDOWN-WEAPON", "90%");
			if(!adjA.text().equals(oldTxt))
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
	public void toneDownArmor(final Armor A, final Ability adjA)
	{
		boolean fixit=true;
		if(adjA!=null)
		{
			final String oldTxt=adjA.text();
			adjA.setStat("TONEDOWN-ARMOR", "90%");
			if(!adjA.text().equals(oldTxt))
				fixit=false;
		}
		if(fixit&&(A.basePhyStats().armor()>=10))
			A.basePhyStats().setArmor((int)Math.round(CMath.mul(A.basePhyStats().armor(),0.9)));
		else
		if(fixit&&(A.basePhyStats().armor()>1))
			A.basePhyStats().setArmor(A.basePhyStats().armor()-1);
		A.recoverPhyStats();
	}

	public void toneUpWeapon(final Weapon W, final Ability adjA)
	{
		if(adjA!=null)
		{
			final String oldTxt=adjA.text();
			adjA.setStat("TONEDOWN-WEAPON", "110%");
			if(!adjA.text().equals(oldTxt))
			{
				W.recoverPhyStats();
				return;
			}
		}
		if(W.basePhyStats().damage()>=10)
			W.basePhyStats().setDamage((int)Math.round(CMath.mul(W.basePhyStats().damage(),1.1)));
		else
		if(W.basePhyStats().damage()>1)
			W.basePhyStats().setDamage(W.basePhyStats().damage()+1);
		if(W.basePhyStats().attackAdjustment()>=10)
			W.basePhyStats().setAttackAdjustment((int)Math.round(CMath.mul(W.basePhyStats().attackAdjustment(),1.1)));
		else
		if(W.basePhyStats().attackAdjustment()>1)
			W.basePhyStats().setAttackAdjustment(W.basePhyStats().attackAdjustment()+1);
		W.recoverPhyStats();
	}

	public void toneUpArmor(final Armor A, final Ability adjA)
	{
		boolean fixit=true;
		if(adjA!=null)
		{
			final String oldTxt=adjA.text();
			adjA.setStat("TONEUP-ARMOR", "110%");
			if(!adjA.text().equals(oldTxt))
				fixit=false;
		}
		if(fixit&&(A.basePhyStats().armor()>=10))
			A.basePhyStats().setArmor((int)Math.round(CMath.mul(A.basePhyStats().armor(),1.1)));
		else
		if(fixit&&(A.basePhyStats().armor()>1))
			A.basePhyStats().setArmor(A.basePhyStats().armor()+1);
		A.recoverPhyStats();
	}

	public Map<Object,Integer> getItemLevels(final Item I, final List<Ability> props)
	{
		final Map<Object,Integer> map=new HashMap<Object,Integer>();
		map.put(I,Integer.valueOf(timsBaseLevel(I,timsBaseAdjustments(I,getBaseAdjusterAbility(props)))));
		map.put("ABILITY",Integer.valueOf(levelsFromAbility(I)));
		for(final Ability A : props)
			map.put(A, Integer.valueOf(CMath.s_int(A.getStat("STAT-LEVEL"))));
		return map;
	}

	public int totalLevels(final Map<Object,Integer> totalLevels)
	{
		int lvl=0;
		for(final Integer I : totalLevels.values())
			lvl += I.intValue();
		return lvl;
	}

	public Ability getBaseAdjusterAbility(final List<Ability> props)
	{
		final String[] stats=new String[] {"STAT-ARMOR", "STAT-ATTACK", "STAT-DAMAGE"};
		for(final Ability A : props)
		{
			if(CMath.bset(A.flags(), Ability.FLAG_ADJUSTER))
			{
				for(final String stat : stats)
				{
					final String val=A.getStat(stat);
					if((val.length()>0)&&(!val.equals("0")))
						return A;
				}
			}
		}
		return null;
	}

	@Override
	public int timsBaseLevel(final Item I)
	{
		final List<Ability> props=getTimsAdjResCast(I);
		return timsBaseLevel(I,timsBaseAdjustments(I,getBaseAdjusterAbility(props)));
	}

	protected int[] timsBaseAdjustments(final Item I, final Ability adjA)
	{
		int otherDam=0;
		int otherAtt=0;
		int otherArm=0;
		if(adjA!=null)
		{
			otherArm=-CMath.s_int(adjA.getStat("STAT-ARMOR"));
			otherAtt=CMath.s_int(adjA.getStat("STAT-ATTACK"));
			otherDam=CMath.s_int(adjA.getStat("STAT-DAMAGE"));
		}
		final int curArmor=I.basePhyStats().armor()+otherArm;
		final int curAttack=I.basePhyStats().attackAdjustment()+otherAtt;
		final int curDamage=I.basePhyStats().damage()+otherDam;
		final int[] ret = new int[3];
		ret[0] = curArmor;
		ret[1] = curAttack;
		ret[2] = curDamage;
		return ret;
	}
	
	protected int timsBaseLevel(final Item I, final int[] adjustments)
	{
		int level=0;
		final int curArmor=adjustments[0];
		final double curAttack=adjustments[1];
		final double curDamage=adjustments[2];
		if(I instanceof Weapon)
		{
			double weight=8;
			if(weight<1.0)
				weight=1.0;
			final double range=((Weapon)I).getRanges()[1];
			final int wclass = ((Weapon)I).weaponClassification();
			final double hands = (I.rawLogicalAnd()?2.0:1.0);
			final double dmgMod = this.getWeaponDmgModifierFromClass(wclass);
			final double attMod = this.getAttackModifierFromClass(wclass);
			final double dmgLevel = Math.floor(((2.0*curDamage/(2.0*hands+1.0)+(curAttack-weight)/5.0+range)*(range/weight+2.0)/dmgMod))+1;
			final double baseAttack = (curAttack - attMod);
			double attackLevel;
			if(baseAttack < 0)
				attackLevel = dmgLevel + baseAttack; // + == - when baseAttack is -
			else
			if(attMod>0.0)
				attackLevel = baseAttack / attMod;
			else
				attackLevel = dmgLevel + baseAttack;
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
			final int[] useArray = getArmorMaterialPointsArray(I.material());
			int which=(int)Math.round(CMath.div(curArmor,weightpts)+1);
			if(which<0)
				which=0;
			int rollOver=0;
			if(which>=useArray.length)
			{
				final int maxAmt=useArray[useArray.length-1];
				rollOver =  ((maxAmt-useArray[useArray.length-2]) * (which-useArray.length));
				which=useArray.length-1;
 			}
			level=useArray[which] + rollOver;
		}
		if(!CMLib.flags().isRemovable(I))
			level-=5;
		return level;
	}

	protected int levelsFromAbility(final Item savedI)
	{
		return savedI.basePhyStats().ability() * 5;
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

	public Ability getCombatSpell(final boolean malicious)
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
	public Item enchant(final Item I, final int pct)
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
					{
						for(int a=0;a<bump;a++)
						{
							final Ability A2=getCombatSpell(true);
							if(A2!=null)
								A.setMiscText(A.text()+";"+A2.ID());
						}
						I.basePhyStats().setLevel(I.basePhyStats().level()+CMath.s_int(A.getStat("STAT-LEVEL")));
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
					{
						for(int a=0;a<bump;a++)
						{
							final Ability A2=getCombatSpell(false);
							if(A2!=null)
								A.setMiscText(A.text()+";"+A2.ID());
						}
						I.basePhyStats().setLevel(I.basePhyStats().level()+CMath.s_int(A.getStat("STAT-LEVEL")));
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
		return I;
	}

}
