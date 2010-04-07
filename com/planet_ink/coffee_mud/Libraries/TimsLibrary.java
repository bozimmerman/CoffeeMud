package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
public class TimsLibrary extends StdLibrary implements ItemBalanceLibrary
{
    public String ID(){return "TimsLibrary";}

	public int timsLevelCalculator(Item I)
	{
		int[] castMul=new int[1];
		Ability[] RET=getTimsAdjResCast(I,castMul);
		Ability ADJ=RET[0];
		Ability RES=RET[1];
		Ability CAST=RET[2];
		return timsLevelCalculator(I,ADJ,RES,CAST,castMul[0]);
	}
	public int timsLevelCalculator(Item I, Ability ADJ, Ability RES, Ability CAST, int castMul)
	{
		int level=0;
		Item savedI=(Item)I.copyOf();
		savedI.recoverEnvStats();
		I=(Item)I.copyOf();
		I.recoverEnvStats();
		int otherDam=0;
		int otherAtt=0;
		int otherArm=0;
		if(ADJ!=null)
		{
			otherArm=CMParms.getParmPlus(ADJ.text(),"arm")*-1;
			otherAtt=CMParms.getParmPlus(ADJ.text(),"att");
			otherDam=CMParms.getParmPlus(ADJ.text(),"dam");
		}
		int curArmor=savedI.baseEnvStats().armor()+otherArm;
		double curAttack=(double)(savedI.baseEnvStats().attackAdjustment()+otherAtt);
		double curDamage=(double)(savedI.baseEnvStats().damage()+otherDam);
		Wearable.CODES codes = Wearable.CODES.instance();
		if(I instanceof Weapon)
		{
			double weight=(double)8;
			if(weight<1.0) weight=1.0;
			double range=(double)savedI.maxRange();
			level=(int)Math.round(Math.floor((2.0*curDamage/(2.0*(I.rawLogicalAnd()?2.0:1.0)+1.0)+(curAttack-weight)/5.0+range)*(range/weight+2.0)))+1;
		}
		else
		{
			long worndata=savedI.rawProperLocationBitmap();
			double weightpts=0;
			for(int i=0;i<codes.location_strength_points().length-1;i++)
			{
				if(CMath.isSet(worndata,i))
				{
					weightpts+=codes.location_strength_points()[i+1];
					if(!I.rawLogicalAnd()) break;
				}
			}
			int[] leatherPoints={ 0, 0, 1, 5,10,16,23,31,40,49,58,67,76,85,94};
			int[] clothPoints=  { 0, 3, 7,12,18,25,33,42,52,62,72,82,92,102};
			int[] metalPoints=  { 0, 0, 0, 0, 1, 3, 5, 8,12,17,23,30,38,46,54,62,70,78,86,94};
			int materialCode=savedI.material()&RawMaterial.MATERIAL_MASK;
			int[] useArray=null;
			switch(materialCode)
			{
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_MITHRIL:
			case RawMaterial.MATERIAL_PRECIOUS:
			case RawMaterial.MATERIAL_ENERGY:
				useArray=metalPoints;
				break;
			case RawMaterial.MATERIAL_PLASTIC:
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
			int which=(int)Math.round(CMath.div(curArmor,weightpts)+1);
			if(which<0) which=0;
			if(which>=useArray.length)
				which=useArray.length-1;
			level=useArray[which];
		}
		level+=I.baseEnvStats().ability()*5;
		if(CAST!=null)
		{
			String ID=CAST.ID().toUpperCase();
			Vector theSpells=new Vector();
			String names=CAST.text();
			int del=names.indexOf(";");
			while(del>=0)
			{
				String thisOne=names.substring(0,del);
				Ability A=CMClass.getAbility(thisOne);
				if(A!=null)	theSpells.addElement(A);
				names=names.substring(del+1);
				del=names.indexOf(";");
			}
			Ability A=CMClass.getAbility(names);
			if(A!=null) theSpells.addElement(A);
			for(int v=0;v<theSpells.size();v++)
			{
				A=(Ability)theSpells.elementAt(v);
				int mul=1;
				if(A.abstractQuality()==Ability.QUALITY_MALICIOUS) mul=-1;
				if(ID.indexOf("HAVE")>=0)
					level+=(mul*CMLib.ableMapper().lowestQualifyingLevel(A.ID()));
				else
					level+=(mul*CMLib.ableMapper().lowestQualifyingLevel(A.ID())/2);
			}
		}
		if(ADJ!=null)
		{
			String newText=ADJ.text();
			int ab=CMParms.getParmPlus(newText,"abi");
			int arm=CMParms.getParmPlus(newText,"arm")*-1;
			int att=CMParms.getParmPlus(newText,"att");
			int dam=CMParms.getParmPlus(newText,"dam");
			if(savedI instanceof Weapon)
				level+=(arm*2);
			else
			if(savedI instanceof Armor)
			{
				level+=(att/2);
				level+=(dam*3);
			}
			level+=ab*5;


			int dis=CMParms.getParmPlus(newText,"dis");
			if(dis!=0) level+=5;
			int sen=CMParms.getParmPlus(newText,"sen");
			if(sen!=0) level+=5;
			level+=(int)Math.round(5.0*CMParms.getParmDoublePlus(newText,"spe"));
			for(int i : CharStats.CODES.BASE())
			{
				int stat=CMParms.getParmPlus(newText,CMStrings.limit(CharStats.CODES.NAME(i),3).toLowerCase());
				int max=CMParms.getParmPlus(newText,("max"+(CMStrings.limit(CharStats.CODES.NAME(i),3).toLowerCase())));
				level+=(stat*10);
				level+=(max*15);
			}

			int hit=CMParms.getParmPlus(newText,"hit");
			int man=CMParms.getParmPlus(newText,"man");
			int mv=CMParms.getParmPlus(newText,"mov");
			level+=(hit/5);
			level+=(man/5);
			level+=(mv/5);
		}
		savedI.destroy();
		I.destroy(); // this was a copy
		return level;
	}

	public boolean fixRejuvItem(Item I)
	{
		Ability A=I.fetchEffect("ItemRejuv");
		if(A!=null)
		{
			A=I.fetchEffect("ItemRejuv");
			if(A.savable())
				return false;
			A.setSavable(false);
			return true;
		}
		return false;
	}

	public Ability[] getTimsAdjResCast(Item I, int[] castMul)
	{
		Ability ADJ=I.fetchEffect("Prop_WearAdjuster");
		if(ADJ==null) ADJ=I.fetchEffect("Prop_HaveAdjuster");
		if(ADJ==null) ADJ=I.fetchEffect("Prop_RideAdjuster");
		Ability RES=I.fetchEffect("Prop_WearResister");
		if(RES==null) RES=I.fetchEffect("Prop_HaveResister");
		Ability CAST=I.fetchEffect("Prop_WearSpellCast");
		castMul[0]=1;
		if(CAST==null) CAST=I.fetchEffect("Prop_UseSpellCast");
		if(CAST==null) CAST=I.fetchEffect("Prop_UseSpellCast2");
		if(CAST==null) CAST=I.fetchEffect("Prop_HaveSpellCast");
		if(CAST==null){ CAST=I.fetchEffect("Prop_FightSpellCast"); castMul[0]=-1;}
		Ability[] RET=new Ability[3];
		RET[0]=ADJ;
		RET[1]=RES;
		RET[2]=CAST;
		return RET;
	}

	private void reportChangesDestroyOldI(Item oldI, Item newI, StringBuffer changes,int OTLVL, int TLVL)
	{
		if((changes == null)||(oldI==null)) return;
		Ability[] RET=getTimsAdjResCast(newI,new int[1]);
		Ability ADJ=RET[0];
		Ability RES=RET[1];
		Ability CAST=RET[2];
		int[] LVLS=getItemLevels(newI,ADJ,RES,CAST);
		int TLVL2=totalLevels(LVLS);
		
		changes.append(newI.name()+":"+newI.baseEnvStats().level()+"("+OTLVL+")=>"+TLVL2+"("+TLVL+"), ");
        for(int i=0;i<oldI.getStatCodes().length;i++)
            if((!oldI.getStat(oldI.getStatCodes()[i]).equals(newI.getStat(newI.getStatCodes()[i]))))
            	changes.append(oldI.getStatCodes()[i]+"("+oldI.getStat(newI.getStatCodes()[i])+"->"+newI.getStat(newI.getStatCodes()[i])+"), ");
        changes.append("\n\r");
        oldI.destroy(); // this was a copy
	}
	
	public boolean itemFix(Item I, int lvlOr0, StringBuffer changes)
	{
		Item oldI = (changes!=null)?(Item)I.copyOf():null;
		
		if((I instanceof SpellHolder)
		||((I instanceof Wand)&&(lvlOr0<=0)))
		{
			Vector spells=new Vector();
			if(I instanceof SpellHolder)
				spells=((SpellHolder)I).getSpells();
			else
			if((I instanceof Wand)&&(((Wand)I).getSpell()!=null))
				spells.add(((Wand)I).getSpell());
			if(spells.size()==0) return false;
			int levels=0;
			spells=(Vector<Ability>)spells.clone();
			for(Enumeration<Ability> e=spells.elements();e.hasMoreElements();)
				levels+=CMLib.ableMapper().lowestQualifyingLevel(e.nextElement().ID());
			int level=(int)Math.round(CMath.div(levels, spells.size()));
			if(level==I.baseEnvStats().level()) return false;
			I.baseEnvStats().setLevel(level);
			I.envStats().setLevel(level);
			if(CMLib.flags().isCataloged(I))
				CMLib.catalog().updateCatalog(I);
			reportChangesDestroyOldI(oldI,I,changes,level,level);
			return true;
		}
		else
		if((I instanceof Weapon)||(I instanceof Armor))
		{
			int lvl=lvlOr0;
			if(lvl <=0) lvl=I.baseEnvStats().level();
			I.baseEnvStats().setLevel(lvl);
			I.envStats().setLevel(lvl);
			Ability[] RET=getTimsAdjResCast(I,new int[1]);
			Ability ADJ=RET[0];
			Ability RES=RET[1];
			Ability CAST=RET[2];
			int[] LVLS=getItemLevels(I,ADJ,RES,CAST);
			int TLVL=totalLevels(LVLS);
			int OTLVL=TLVL;
			if(lvl<0)
			{
				if(TLVL<=0)
					lvl=1;
				else
					lvl=TLVL;
				I.baseEnvStats().setLevel(lvl);
				I.recoverEnvStats();
				fixRejuvItem(I);
				if(CMLib.flags().isCataloged(I))
					CMLib.catalog().updateCatalog(I);
				reportChangesDestroyOldI(oldI,I,changes,OTLVL,TLVL);
				return true;
			}
			if((TLVL>0)&&(TLVL>Math.round(CMath.mul(lvl,1.1))))
			{
				//int FTLVL=TLVL;
				Vector illegalNums=new Vector();
				//Log.sysOut("Reset",I.name()+"("+I.baseEnvStats().level()+") "+TLVL+", "+I.baseEnvStats().armor()+"/"+I.baseEnvStats().attackAdjustment()+"/"+I.baseEnvStats().damage()+"/"+((ADJ!=null)?ADJ.text():"null"));
				while((TLVL>Math.round(CMath.mul(lvl,1.1)))&&(illegalNums.size()<4))
				{
					int highIndex=-1;
					for(int i=0;i<LVLS.length;i++)
						if(((highIndex<0)||(LVLS[i]>LVLS[highIndex]))
						&&(!illegalNums.contains(Integer.valueOf(i))))
							highIndex=i;
					if(highIndex<0) break;
					switch(highIndex)
					{
					case 0:
						if(I instanceof Weapon)
						{
							String s=(ADJ!=null)?ADJ.text():"";
							int oldAtt=I.baseEnvStats().attackAdjustment();
							int oldDam=I.baseEnvStats().damage();
							toneDownWeapon((Weapon)I,ADJ);
							if((I.baseEnvStats().attackAdjustment()==oldAtt)
							&&(I.baseEnvStats().damage()==oldDam)
							&&((ADJ==null)||(ADJ.text().equals(s))))
								illegalNums.addElement(Integer.valueOf(0));
						}
						else
						{
							String s=(ADJ!=null)?ADJ.text():"";
							int oldArm=I.baseEnvStats().armor();
							toneDownArmor((Armor)I,ADJ);
							if((I.baseEnvStats().armor()==oldArm)
							&&((ADJ==null)||(ADJ.text().equals(s))))
								illegalNums.addElement(Integer.valueOf(0));
						}
						break;
					case 1:
						if(I.baseEnvStats().ability()>0)
							I.baseEnvStats().setAbility(I.baseEnvStats().ability()-1);
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
							String oldTxt=ADJ.text();
							toneDownAdjuster(I,ADJ);
							if(ADJ.text().equals(oldTxt))
								illegalNums.addElement(Integer.valueOf(3));
						}
						break;
					}
					LVLS=getItemLevels(I,ADJ,RES,CAST);
					TLVL=totalLevels(LVLS);
				}
				//Log.sysOut("Reset",I.name()+"("+I.baseEnvStats().level()+") "+FTLVL+"->"+TLVL+", "+I.baseEnvStats().armor()+"/"+I.baseEnvStats().attackAdjustment()+"/"+I.baseEnvStats().damage()+"/"+((ADJ!=null)?ADJ.text():"null"));
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
			if(oldI!=null) oldI.destroy();
			return true;
		}
		if(oldI!=null) oldI.destroy();
		return false;
	}

    public boolean toneDownValue(Item I)
    {
        int hands=0;
        int weaponClass=0;
        if(I instanceof Coins) return false;
        if(I instanceof Weapon)
        {
            hands=I.rawLogicalAnd()?2:1;
            weaponClass=((Weapon)I).weaponClassification();
        }
        else
        if(!(I instanceof Armor))
            return false;
        Hashtable H=timsItemAdjustments(I,I.envStats().level(),I.material(),hands,weaponClass,I.maxRange(),I.rawProperLocationBitmap());
        int newValue=CMath.s_int((String)H.get("VALUE"));
        if((I.baseGoldValue()>newValue)&&(newValue>0))
        {
            I.setBaseValue(newValue);
            return true;
        }
        return false;
    }

	public void balanceItemByLevel(Item I)
	{
		int hands=0;
		int weaponClass=0;
		if(I instanceof Weapon)
		{
			hands=I.rawLogicalAnd()?2:1;
			weaponClass=((Weapon)I).weaponClassification();
		}
		Hashtable H=timsItemAdjustments(I,I.baseEnvStats().level(),I.material(),hands,weaponClass,I.maxRange(),I.rawProperLocationBitmap());
		if(I instanceof Weapon)
		{
			I.baseEnvStats().setDamage(CMath.s_int((String)H.get("DAMAGE")));
			I.baseEnvStats().setAttackAdjustment(CMath.s_int((String)H.get("ATTACK")));
			I.setBaseValue(CMath.s_int((String)H.get("VALUE")));
			I.recoverEnvStats();
		}
		else
		if(I instanceof Armor)
		{
			I.baseEnvStats().setArmor(CMath.s_int((String)H.get("ARMOR")));
			I.setBaseValue(CMath.s_int((String)H.get("VALUE")));
			I.baseEnvStats().setWeight(CMath.s_int((String)H.get("WEIGHT")));
			I.recoverEnvStats();
		}
	}

	public Hashtable timsItemAdjustments(Item I,
										 int level,
										 int material,
										 int hands,
										 int wclass,
										 int reach,
										 long worndata)
	{
		Hashtable vals=new Hashtable();
		int materialvalue=RawMaterial.CODES.VALUE(material);
		int[] castMul=new int[1];
		Ability[] RET=getTimsAdjResCast(I,castMul);
		Ability ADJ=RET[0];
		Ability CAST=RET[2];
		level-=levelsFromAbility(I);
		level-=levelsFromAdjuster(I,ADJ);
		level-=levelsFromCaster(I,CAST);

		if(I instanceof Weapon)
		{
			int baseattack=0;
			int basereach=0;
			int maxreach=0;
			int basematerial=RawMaterial.MATERIAL_WOODEN;
			if(wclass==Weapon.CLASS_FLAILED) baseattack=-5;
			if(wclass==Weapon.CLASS_POLEARM){ basereach=1; basematerial=RawMaterial.MATERIAL_METAL;}
			if(wclass==Weapon.CLASS_RANGED){ basereach=1; maxreach=5;}
			if(wclass==Weapon.CLASS_THROWN){ basereach=1; maxreach=5;}
			if(wclass==Weapon.CLASS_EDGED){ baseattack=10; basematerial=RawMaterial.MATERIAL_METAL;}
			if(wclass==Weapon.CLASS_DAGGER){ baseattack=10; basematerial=RawMaterial.MATERIAL_METAL;}
			if(wclass==Weapon.CLASS_SWORD){ basematerial=RawMaterial.MATERIAL_METAL;}
			int weight = 8;
			if(basereach>maxreach) maxreach=basereach;
			if(reach<basereach)
			{
				reach=basereach;
				vals.put("MINRANGE",""+basereach);
				vals.put("MAXRANGE",""+maxreach);
			}
			else
			if(reach>basereach)
				basereach=reach;
			int damage=((level-1)/((reach/weight)+2) + (weight-baseattack)/5 -reach)*(((hands*2)+1)/2);
			int cost=2*((weight*materialvalue)+((2*damage)+baseattack+(reach*10))*damage)/(hands+1);

			if(basematerial==RawMaterial.MATERIAL_METAL)
			{
				switch(material&RawMaterial.MATERIAL_MASK)
				{
				case RawMaterial.MATERIAL_MITHRIL:
				case RawMaterial.MATERIAL_METAL:
				case RawMaterial.MATERIAL_ENERGY:
					break;
				case RawMaterial.MATERIAL_WOODEN:
				case RawMaterial.MATERIAL_PLASTIC:
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
				case RawMaterial.MATERIAL_PLASTIC:
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
			if(damage<=0) damage=1;

			vals.put("DAMAGE",""+damage);
			vals.put("ATTACK",""+baseattack);
			vals.put("VALUE",""+cost);
		}
		else
		if(I instanceof Armor)
		{
			int[] leatherPoints={ 0, 0, 1, 5,10,16,23,31,40,49,58,67,76,85,94};
			int[] clothPoints=  { 0, 3, 7,12,18,25,33,42,52,62,72,82,92,102};
			int[] metalPoints=  { 0, 0, 0, 0, 1, 3, 5, 8,12,17,23,30,38,46,54,62,70,78,86,94};
			double pts=0.0;
			if(level<0) level=0;
			int materialCode=material&RawMaterial.MATERIAL_MASK;
			int[] useArray=null;
			switch(materialCode)
			{
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_MITHRIL:
			case RawMaterial.MATERIAL_PRECIOUS:
			case RawMaterial.MATERIAL_ENERGY:
				useArray=metalPoints;
				break;
			case RawMaterial.MATERIAL_PLASTIC:
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
				pts=(double)(useArray.length-2);
			else
			for(int i=0;i<useArray.length;i++)
			{
				int lvl=useArray[i];
				if(lvl>level)
				{
					pts=(double)(i-1);
					break;
				}
			}

			double totalpts=0.0;
			double weightpts=0.0;
			double wornweights=0.0;
			Wearable.CODES codes = Wearable.CODES.instance();
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
					case RawMaterial.MATERIAL_PLASTIC:
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
					if(hands==1) break;
				}
			}
			int cost=(int)Math.round(((pts*pts) + (double)materialvalue)
									 * ( weightpts / 2));
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
			vals.put("WEIGHT",""+(int)Math.round(((double)armor)/wornweights*weightpts));
		}
		return vals;
	}

	public void toneDownWeapon(Weapon W, Ability ADJ)
	{
		boolean fixdam=true;
		boolean fixatt=true;
		if((ADJ!=null)&&(ADJ.text().toUpperCase().indexOf("DAMAGE+")>=0))
		{
			int a=ADJ.text().toUpperCase().indexOf("DAMAGE+");
			int a2=ADJ.text().toUpperCase().indexOf(" ",a+4);
			if(a2<0) a2=ADJ.text().length();
			int num=CMath.s_int(ADJ.text().substring(a+7,a2));
			int newNum = (int)Math.round(CMath.mul(num,0.9));
			if((newNum == num) && (newNum > 1))
				newNum--;
			if(newNum != 0)
			{
				fixdam=false;
				ADJ.setMiscText(ADJ.text().substring(0,a+7)+newNum+ADJ.text().substring(a2));
			}
		}
		if((ADJ!=null)&&(ADJ.text().toUpperCase().indexOf("ATTACK+")>=0))
		{
			int a=ADJ.text().toUpperCase().indexOf("ATTACK+");
			int a2=ADJ.text().toUpperCase().indexOf(" ",a+4);
			if(a2<0) a2=ADJ.text().length();
			int num=CMath.s_int(ADJ.text().substring(a+7,a2));
			int newNum = (int)Math.round(CMath.mul(num,0.9));
			if((newNum == num) && (newNum > 1))
				newNum--;
			if(newNum != 0)
			{
				fixatt=false;
				ADJ.setMiscText(ADJ.text().substring(0,a+7)+newNum+ADJ.text().substring(a2));
			}
		}
		if(fixdam&&(W.baseEnvStats().damage()>=10))
			W.baseEnvStats().setDamage((int)Math.round(CMath.mul(W.baseEnvStats().damage(),0.9)));
		else
		if(fixatt&&(W.baseEnvStats().damage()>1))
			W.baseEnvStats().setDamage(W.baseEnvStats().damage()-1);
		if(fixatt&&(W.baseEnvStats().attackAdjustment()>=10))
			W.baseEnvStats().setAttackAdjustment((int)Math.round(CMath.mul(W.baseEnvStats().attackAdjustment(),0.9)));
		else
		if(fixatt&&(W.baseEnvStats().attackAdjustment()>1))
			W.baseEnvStats().setAttackAdjustment(W.baseEnvStats().attackAdjustment()-1);
		W.recoverEnvStats();
	}
	public void toneDownArmor(Armor A, Ability ADJ)
	{
		boolean fixit=true;
		if((ADJ!=null)&&(ADJ.text().toUpperCase().indexOf("ARMOR-")>=0))
		{
			int a=ADJ.text().toUpperCase().indexOf("ARMOR-");
			int a2=ADJ.text().toUpperCase().indexOf(" ",a+4);
			if(a2<0) a2=ADJ.text().length();
			int num=CMath.s_int(ADJ.text().substring(a+6,a2));
			int newNum = (int)Math.round(CMath.mul(num,0.9));
			if((newNum == num) && (newNum > 1))
				newNum--;
			if(newNum != 0)
			{
				fixit=false;
				ADJ.setMiscText(ADJ.text().substring(0,a+6)+newNum+ADJ.text().substring(a2));
			}
		}
		if(fixit&&(A.baseEnvStats().armor()>=10))
			A.baseEnvStats().setArmor((int)Math.round(CMath.mul(A.baseEnvStats().armor(),0.9)));
		else
		if(fixit&&(A.baseEnvStats().armor()>1))
			A.baseEnvStats().setArmor(A.baseEnvStats().armor()-1);
		A.recoverEnvStats();
	}

	public void toneDownAdjuster(Item I, Ability ADJ)
	{
		String s=ADJ.text();
		int plusminus=s.indexOf("+");
		int minus=s.indexOf("-");
		if((minus>=0)&&((plusminus<0)||(minus<plusminus)))
			plusminus=minus;
		while(plusminus>=0)
		{
			int spaceafter=s.indexOf(" ",plusminus+1);
			if(spaceafter<0) spaceafter=s.length();
			if(spaceafter>plusminus)
			{
				String number=s.substring(plusminus+1,spaceafter).trim();
				if(CMath.isNumber(number))
				{
					int num=CMath.s_int(number);
					int spacebefore=s.lastIndexOf(" ",plusminus);
					if(spacebefore<0) spacebefore=0;
					if(spacebefore<plusminus)
					{
						boolean proceed=true;
						String wd=s.substring(spacebefore,plusminus).trim().toUpperCase();
						if(wd.startsWith("DIS"))
							proceed=false;
						else
						if(wd.startsWith("SEN"))
							proceed=false;
						else
						if(wd.startsWith("ARM")&&(I instanceof Armor))
							proceed=false;
						else
						if(wd.startsWith("ATT")&&(I instanceof Weapon))
						   proceed=false;
						else
						if(wd.startsWith("DAM")&&(I instanceof Weapon))
						   proceed=false;
						else
						if(wd.startsWith("ARM")&&(s.charAt(plusminus)=='+'))
							proceed=false;
						else
						if((!wd.startsWith("ARM"))&&(s.charAt(plusminus)=='-'))
							proceed=false;
						if(proceed)
						{
							if((num!=1)&&(num!=-1))
							{
								int newNum = (int)Math.round(CMath.mul(num,0.9));
								if((newNum == num) && (newNum > 1))
									newNum--;
								if(newNum != 0)
									s=s.substring(0,plusminus+1)+newNum+s.substring(spaceafter);
							}
						}
					}
				}
			}
			minus=s.indexOf("-",plusminus+1);
			plusminus=s.indexOf("+",plusminus+1);
			if((minus>=0)&&((plusminus<0)||(minus<plusminus)))
				plusminus=minus;
		}
		ADJ.setMiscText(s);
	}

	public int[] getItemLevels(Item I, Ability ADJ, Ability RES, Ability CAST)
	{
		int[] LVLS=new int[4];
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

	public int timsBaseLevel(Item I)
	{
		Ability[] RET=getTimsAdjResCast(I,new int[1]);
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
			otherArm=CMParms.getParmPlus(ADJ.text(),"arm")*-1;
			otherAtt=CMParms.getParmPlus(ADJ.text(),"att");
			otherDam=CMParms.getParmPlus(ADJ.text(),"dam");
		}
		int curArmor=I.baseEnvStats().armor()+otherArm;
		double curAttack=(double)(I.baseEnvStats().attackAdjustment()+otherAtt);
		double curDamage=(double)(I.baseEnvStats().damage()+otherDam);
		if(I instanceof Weapon)
		{
			double weight=(double)8;
			if(weight<1.0) weight=1.0;
			double range=(double)(I.maxRange());
			level=(int)Math.round(Math.floor((2.0*curDamage/(2.0*(I.rawLogicalAnd()?2.0:1.0)+1.0)+(curAttack-weight)/5.0+range)*(range/weight+2.0)))+1;
		}
		else
		{
			long worndata=I.rawProperLocationBitmap();
			double weightpts=0;
			Wearable.CODES codes = Wearable.CODES.instance();
			for(int i=0;i<codes.location_strength_points().length-1;i++)
			{
				if(CMath.isSet(worndata,i))
				{
					weightpts+=codes.location_strength_points()[i+1];
					if(!I.rawLogicalAnd()) break;
				}
			}
			int[] leatherPoints={ 0, 0, 1, 5,10,16,23,31,40,49,58,67,76,85,94};
			int[] clothPoints=  { 0, 3, 7,12,18,25,33,42,52,62,72,82,92,102};
			int[] metalPoints=  { 0, 0, 0, 0, 1, 3, 5, 8,12,17,23,30,38,46,54,62,70,78,86,94};
			int materialCode=I.material()&RawMaterial.MATERIAL_MASK;
			int[] useArray=null;
			switch(materialCode)
			{
			case RawMaterial.MATERIAL_METAL:
			case RawMaterial.MATERIAL_MITHRIL:
			case RawMaterial.MATERIAL_PRECIOUS:
			case RawMaterial.MATERIAL_ENERGY:
				useArray=metalPoints;
				break;
			case RawMaterial.MATERIAL_PLASTIC:
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
			int which=(int)Math.round(CMath.div(curArmor,weightpts)+1);
			if(which<0) which=0;
			if(which>=useArray.length)
				which=useArray.length-1;
			level=useArray[which];
		}
		return level;
	}

	public int levelsFromAbility(Item savedI)
	{ return savedI.baseEnvStats().ability()*5;}

	public int levelsFromAdjuster(Item savedI, Ability ADJ)
	{
		int level=0;
		if(ADJ!=null)
		{
			String newText=ADJ.text();
			int ab=CMParms.getParmPlus(newText,"abi");
			int arm=CMParms.getParmPlus(newText,"arm")*-1;
			int att=CMParms.getParmPlus(newText,"att");
			int dam=CMParms.getParmPlus(newText,"dam");
			if(savedI instanceof Weapon)
				level+=arm;
			else
			if(savedI instanceof Armor)
			{
				level+=att;
				level+=(dam*3);
			}
			level+=ab*5;
			int dis=CMParms.getParmPlus(newText,"dis");
			if(dis!=0) level+=10;
			int sen=CMParms.getParmPlus(newText,"sen");
			if(sen!=0) level+=10;
			level+=(int)Math.round(5.0*CMParms.getParmDoublePlus(newText,"spe"));
			for(int i: CharStats.CODES.BASE())
			{
				int stat=CMParms.getParmPlus(newText,CMStrings.limit(CharStats.CODES.NAME(i),3).toLowerCase());
				int max=CMParms.getParmPlus(newText,("max"+(CMStrings.limit(CharStats.CODES.NAME(i),3).toLowerCase())));
				level+=(stat*10);
				level+=(max*15);
			}

			int hit=CMParms.getParmPlus(newText,"hit");
			int man=CMParms.getParmPlus(newText,"man");
			int mv=CMParms.getParmPlus(newText,"mov");
			level+=(hit/5);
			level+=(man/5);
			level+=(mv/5);
		}
		return level;
	}

	public int levelsFromCaster(Item savedI, Ability CAST)
	{
		int level=0;
		if(CAST!=null)
		{
			String ID=CAST.ID().toUpperCase();
			Vector theSpells=new Vector();
			String names=CAST.text();
			int del=names.indexOf(";");
			while(del>=0)
			{
				String thisOne=names.substring(0,del);
				Ability A=CMClass.getAbility(thisOne);
				if(A!=null)	theSpells.addElement(A);
				names=names.substring(del+1);
				del=names.indexOf(";");
			}
			Ability A=CMClass.getAbility(names);
			if(A!=null) theSpells.addElement(A);
			for(int v=0;v<theSpells.size();v++)
			{
				A=(Ability)theSpells.elementAt(v);
				int mul=1;
				if(A.abstractQuality()==Ability.QUALITY_MALICIOUS) mul=-1;
				if(ID.indexOf("HAVE")>=0)
					level+=(mul*CMLib.ableMapper().lowestQualifyingLevel(A.ID()));
				else
					level+=(mul*CMLib.ableMapper().lowestQualifyingLevel(A.ID())/2);
			}
		}
		return level;
	}

	public synchronized Vector getCombatSpellSet()
	{
		Vector spellSet=(Vector)Resources.getResource("COMPLETE_SPELL_SET");
		if(spellSet==null)
		{
			spellSet=new Vector();
			Ability A=null;
			for(Enumeration e=CMClass.abilities();e.hasMoreElements();)
			{
				A=(Ability)e.nextElement();
				if(((A.classificationCode()&(Ability.ALL_ACODES))==Ability.ACODE_SPELL))
				{
					int lowLevel=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
					if((lowLevel>0)&&(lowLevel<25))
						spellSet.addElement(A);
				}
			}
			Resources.submitResource("COMPLETE_SPELL_SET",spellSet);
		}
		return spellSet;
	}

	public Ability getCombatSpell(boolean malicious)
	{
		Vector spellSet=getCombatSpellSet();
		int tries=0;
		while(((++tries)<1000))
		{
			Ability A=(Ability)spellSet.elementAt(CMLib.dice().roll(1,spellSet.size(),-1));
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

	public Item enchant(Item I, int pct)
	{
		if(CMLib.dice().rollPercentage()>pct) return I;
		int bump=0;
		while((CMLib.dice().rollPercentage()<=10)||(bump==0))
			bump=bump+((CMLib.dice().rollPercentage()<=80)?1:-1);
		if(bump<0) CMLib.flags().setRemovable(I,false);
		I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_BONUS);
		I.recoverEnvStats();
		if(I instanceof Ammunition)
		{
			int lvlChange=bump*3;
			if(lvlChange<0) lvlChange=lvlChange*-1;
			I.baseEnvStats().setLevel(I.baseEnvStats().level()+lvlChange);
			switch(CMLib.dice().roll(1,2,0))
			{
			case 1:
			{
				Ability A=CMClass.getAbility("Prop_WearAdjuster");
				if(A==null) return I;
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
						Ability A2=getCombatSpell(true);
						if(A2!=null) A.setMiscText(A.text()+";"+A2.ID());
					}
				}
				if(A==null) return I;
				I.addNonUninvokableEffect(A);
				break;
			}
			}
			I.recoverEnvStats();
		}
		else
		if(I instanceof Weapon)
		{
			switch(CMLib.dice().roll(1,2,0))
			{
			case 1:
			{
				I.baseEnvStats().setAbility(bump);
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
						Ability A2=getCombatSpell(true);
						if(A2!=null) A.setMiscText(A.text()+";"+A2.ID());
					}
					I.baseEnvStats().setLevel(I.baseEnvStats().level()+levelsFromCaster(I,A));
				}
				if(A==null) return I;
				I.addNonUninvokableEffect(A);
				break;
			}
			}
			I.recoverEnvStats();
		}
		else
		if(I instanceof Armor)
		{
			switch(CMLib.dice().roll(1,2,0))
			{
			case 1:
			{
				I.baseEnvStats().setAbility(bump);
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
						Ability A2=getCombatSpell(false);
						if(A2!=null) A.setMiscText(A.text()+";"+A2.ID());
					}
					I.baseEnvStats().setLevel(I.baseEnvStats().level()+levelsFromCaster(I,A));
				}
				if(A==null) return I;
				I.addNonUninvokableEffect(A);
				break;
			}
			}
			I.recoverEnvStats();
		}
		return I;
	}

}
