package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
public class CommonStrings
{
	public static String standardHitWord(int type, int damage)
	{
		if(type<0) type=Weapon.TYPE_BURSTING;
		int damnCode=9;
			 if(damage<=3) damnCode=0;
		else if(damage<=6) damnCode=1;
		else if(damage<=10) damnCode=2;
		else if(damage<=15) damnCode=3;
		else if(damage<=25) damnCode=4;
		else if(damage<=35) damnCode=5;
		else if(damage<=45) damnCode=6;
		else if(damage<=65) damnCode=7;
		else if(damage<=95) damnCode=8;
		
		switch(type)
		{
		case Weapon.TYPE_NATURAL:
			switch(damnCode)
			{
			case 0: return "scratch(es)";
			case 1: return "graze(s)";
			case 2: return "hit(s)";
			case 3: return "cut(s)";
			case 4: return "hurt(s)";
			case 5: return "rip(s)";
			case 6: return "crunch(es)";
			case 7: return "MASSACRE(S)";
			case 8: return "DESTROY(S)";
			case 9: return "OBLITERATE(S)";
			}
			break;
		case Weapon.TYPE_SLASHING:
			switch(damnCode)
			{
			case 0: return "scratch(es)";
			case 1: return "graze(s)";
			case 2: return "wound(s)";
			case 3: return "cut(s)";
			case 4: return "slice(s)";
			case 5: return "gut(s)";
			case 6: return "murder(s)";
			case 7: return "MASSACRE(S)";
			case 8: return "DESTROY(S)";
			case 9: return "OBLITERATE(S)";
			}
			break;
		case Weapon.TYPE_PIERCING:
			switch(damnCode)
			{
			case 0: return "scratch(es)";
			case 1: return "graze(s)";
			case 2: return "prick(s)";
			case 3: return "cut(s)";
			case 4: return "stab(s)";
			case 5: return "pierce(s)";
			case 6: return "murder(s)";
			case 7: return "MASSACRE(S)";
			case 8: return "DESTROY(S)";
			case 9: return "OBLITERATE(S)";
			}
			break;
		case Weapon.TYPE_BASHING:
			switch(damnCode)
			{
			case 0: return "scratch(es)";
			case 1: return "graze(s)";
			case 2: return "hit(s)";
			case 3: return "smash(es)";
			case 4: return "bash(es)";
			case 5: return "crush(es)";
			case 6: return "crunch(es)";
			case 7: return "MASSACRE(S)";
			case 8: return "DESTROY(S)";
			case 9: return "OBLITERATE(S)";
			}
			break;
		case Weapon.TYPE_BURNING:
			switch(damnCode)
			{
			case 0: return "warm(s)";
			case 1: return "heat(s)";
			case 2: return "singe(s)";
			case 3: return "burn(s)";
			case 4: return "flame(s)";
			case 5: return "scorch(es)";
			case 6: return "incinerate(s)";
			case 7: return "MASSACRE(S)";
			case 8: return "DESTROY(S)";
			case 9: return "OBLITERATE(S)";
			}
			break;
		case Weapon.TYPE_SHOOT:
			switch(damnCode)
			{
			case 0: return "scratch(es)";
			case 1: return "graze(s)";
			case 2: return "hit(s)";
			case 3: return "pierce(s)";
			case 4: return "pierce(s)";
			case 5: return "decimate(s)";
			case 6: return "murder(s)";
			case 7: return "MASSACRE(S)";
			case 8: return "DESTROY(S)";
			case 9: return "OBLITERATE(S)";
			}
			break;
		case Weapon.TYPE_FROSTING:
			switch(damnCode)
			{
			case 0: return "chill(s)";
			case 1: return "cool(s)";
			case 2: return "ice(s)";
			case 3: return "frost(s)";
			case 4: return "blister(s)";
			case 5: return "blast(s)";
			case 6: return "incinerate(s)";
			case 7: return "MASSACRE(S)";
			case 8: return "DESTROY(S)";
			case 9: return "OBLITERATE(S)";
			}
			break;
		case Weapon.TYPE_GASSING:
			switch(damnCode)
			{
			case 0: return "annoy(s)";
			case 1: return "gass(es)";
			case 2: return "gass(es)";
			case 3: return "choke(s)";
			case 4: return "choke(s)";
			case 5: return "decimate(s)";
			case 6: return "murder(s)";
			case 7: return "MASSACRE(S)";
			case 8: return "DESTROY(S)";
			case 9: return "OBLITERATE(S)";
			}
			break;
		case Weapon.TYPE_MELTING:
			switch(damnCode)
			{
			case 0: return "sting(s)";
			case 1: return "sting(s)";
			case 2: return "burn(s)";
			case 3: return "burn(s)";
			case 4: return "scorch(es)";
			case 5: return "melt(s)";
			case 6: return "melt(s)";
			case 7: return "MASSACRE(S)";
			case 8: return "DESTROY(S)";
			case 9: return "OBLITERATE(S)";
			}
			break;
		case Weapon.TYPE_STRIKING:
			switch(damnCode)
			{
			case 0: return "sting(s)";
			case 1: return "charge(s)";
			case 2: return "singe(s)";
			case 3: return "burn(s)";
			case 4: return "scorch(es)";
			case 5: return "blast(s)";
			case 6: return "incinerate(s)";
			case 7: return "MASSACRE(S)";
			case 8: return "DESTROY(S)";
			case 9: return "OBLITERATE(S)";
			}
			break;
		case Weapon.TYPE_BURSTING:
		default:
			switch(damnCode)
			{
			case 0: return "scratch(es)";
			case 1: return "graze(s)";
			case 2: return "wound(s)";
			case 3: return "cut(s)";
			case 4: return "badly damage(s)";
			case 5: return "decimate(s)";
			case 6: return "murder(s)";
			case 7: return "MASSACRE(S)";
			case 8: return "DESTROY(S)";
			case 9: return "OBLITERATE(S)";
			}
			break;
		}
		return "";
	}
	
	public static String armorStr(int armor){
		return (armor<0)?armorStrs[0]:(
			   (armor>200)?armorStrs[armorStrs.length-1]+" ("+armor+")":(
				armorStrs[(int)Math.round(Math.floor(Util.div(armor,15.0)))]+" ("+armor+")"));}
	public static String fightingProwessStr(int prowess){
		return (prowess<0)?fightStrs[0]:(
			   (prowess>200)?fightStrs[fightStrs.length-1]+" ("+prowess+")":(
				fightStrs[(int)Math.round(Math.floor(Util.div(prowess,15.0)))]+" ("+prowess+")"));}
	public static String standardMissString(int weaponType, int weaponClassification, String weaponName, boolean useExtendedMissString)
	{
		int dex=3;
		switch(weaponClassification)
		{
		case Weapon.CLASS_RANGED: dex=0; break;
		case Weapon.CLASS_THROWN: dex=1; break;
		default:
			switch(weaponType)
			{
			case Weapon.TYPE_SLASHING:
			case Weapon.TYPE_BASHING:
				dex=2; break;
			case Weapon.TYPE_PIERCING:
				dex=4; break;
			case Weapon.TYPE_SHOOT:
				dex=0; break;
			default:
				dex=3;
				break;
			}
			break;
		}
		if(!useExtendedMissString) return missStrs2[dex];
		String str=missStrs1[dex];
		int dexTool=str.indexOf("<TOOLNAME>");
		return str.substring(0,dexTool)+weaponName+str.substring(dexTool+10);
	}
	public static String standardHitString(int weaponType, int weaponClass, int damageAmount, String weaponName)
	{
		if((weaponName==null)||(weaponName.length()==0))
			weaponClass=Weapon.CLASS_NATURAL;
		switch(weaponClass)
		{
		case Weapon.CLASS_RANGED:
			return "<S-NAME> fire(s) "+weaponName+" at <T-NAMESELF> and "+standardHitWord(weaponType,damageAmount)+" <T-HIM-HER>";
		case Weapon.CLASS_THROWN:
			return "<S-NAME> throw(s) "+weaponName+" at <T-NAMESELF> and "+standardHitWord(weaponType,damageAmount)+" <T-HIM-HER>";
		default:
			return "<S-NAME> "+standardHitWord(weaponType,damageAmount)+" <T-NAMESELF> with "+weaponName;
		}
	}
	
	public static String standardMobCondition(MOB mob)
	{
		switch((int)Math.round(Math.floor((Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()))*10)))
		{
		case 0:	return "^r" + mob.name() + "^r is hovering on deaths door!^N";
		case 1:	return "^r" + mob.name() + "^r is covered in blood.^N";
		case 2:	return "^r" + mob.name() + "^r is bleeding badly from lots of wounds.^N";
		case 3:	return "^y" + mob.name() + "^y has numerous bloody wounds and gashes.^N";
		case 4:	return "^y" + mob.name() + "^y has some bloody wounds and gashes.^N";
		case 5:	return "^p" + mob.name() + "^p has a few bloody wounds.^N";
		case 6:	return "^p" + mob.name() + "^p is cut and bruised.^N";
		case 7:	return "^g" + mob.name() + "^g has some minor cuts and bruises.^N";
		case 8:	return "^g" + mob.name() + "^g has a few bruises and scratches.^N";
		case 9:	return "^g" + mob.name() + "^g has a few small bruises.^N";
		default: return "^c" + mob.name() + "^c is in perfect health^N";
		}
	}

	public static String shortAlignmentStr(int al)
	{
		if(al<350) return "evil";
		else if(al<650)	return "neutral";
		else return "good";
	}

	public static String alignmentStr(int al)
	{
		if(al<50) return "pure evil";
		else if(al<300) return "evil";
		else if(al<425)	return "somewhat evil";
		else if(al<575)	return "pure neutral";
		else if(al<700)	return "somewhat good";
		else if(al<950)	return "good";
		else return "pure goodness";

	}
	public static void resistanceMsgs(Affect affect, MOB source, MOB target)
	{
		if(affect.wasModified()) return;
		
		if(target.amDead()) return;

		String tool=null;
		String endPart=" from <T-NAME>.";
		if(source==target)
		{
			source=null;
			endPart=".";
		}
		if(affect.tool()!=null)
		{
			if(affect.tool() instanceof Trap)
				endPart=".";
			else
		    if(affect.tool() instanceof Ability)
				tool=((Ability)affect.tool()).name();
		}
		String tackOn=null;
		switch(affect.targetMinor())
		{
		case Affect.TYP_MIND: tackOn="<S-NAME> shake(s) off the "+((tool==null)?"mental attack":tool)+endPart; break;
		case Affect.TYP_GAS: tackOn="<S-NAME> resist(s) the "+((tool==null)?"noxious fumes":tool)+endPart; break;
		case Affect.TYP_COLD: tackOn="<S-NAME> shake(s) off the "+((tool==null)?"cold blast":tool)+endPart;	break;
		case Affect.TYP_ELECTRIC: tackOn="<S-NAME> shake(s) off the "+((tool==null)?"electrical attack":tool)+endPart; break;
		case Affect.TYP_FIRE: tackOn="<S-NAME> resist(s) the "+((tool==null)?"blast of heat":tool)+endPart; break;
		case Affect.TYP_WATER: tackOn="<S-NAME> dodge(s) the "+((tool==null)?"wet blast":tool)+endPart;	break;
		case Affect.TYP_UNDEAD:	tackOn="<S-NAME> shake(s) off the "+((tool==null)?"evil attack":tool)+endPart; break;
		case Affect.TYP_POISON:	tackOn="<S-NAME> shake(s) off the "+((tool==null)?"poison":tool)+endPart; break;
		case Affect.TYP_DISEASE: tackOn="<S-NAME> resist(s) the "+((tool==null)?"disease":tool); break;
		case Affect.TYP_JUSTICE:break;
		case Affect.TYP_CAST_SPELL:	tackOn="<S-NAME> resist(s) the "+((tool==null)?"magical attack":tool)+endPart; break;
		case Affect.TYP_PARALYZE: tackOn="<S-NAME> resist(s) the "+((tool==null)?"paralysis":tool)+endPart; break;
		}
		if(tackOn!=null)
			affect.addTrailerMsg(new FullMsg(target,source,Affect.MSG_OK_ACTION,tackOn));
		affect.tagModified(true);
	}
																														
	public static String[] armorStrs={
	"nonexistant",				//0
	"covered",					//1
	"padded",					//2
	"heavily padded",			//3
	"protected",				//4
	"well protected",			//5
	"armored",					//6
	"well armored",				//7
	"heavily armored",			//8
	"completely armored",		//9
	"divinely armored",			//10
	"practically unhittable",	//11	
	"almost impenetrable",		//12
	"almost invincible",		//13
	"invincible!"				//14
	};
	public final static String[] fightStrs={
	"none",					//0
	"hardly any",			//1
	"very little",			//2
	"a little",				//3
	"some skill",			//4
	"skilled",				//5
	"very skilled",			//6
	"a master",				//7
	"dangerous",			//8
	"extremely dangerous",	//9
	"deadly",				//10
	"extremely deadly",		//11
	"a dealer of death",	//12	
	"a master of death",	//13
	"you are death incarnate!"//14
	};
	public final static String[] missStrs1={
		"<S-NAME> fire(s) at <T-NAMESELF> with <TOOLNAME> and miss(es).", // 0
		"<S-NAME> throw(s) <TOOLNAME> at <T-NAMESELF> and miss(es).", // 1
		"<S-NAME> swing(s) at <T-NAMESELF> with <TOOLNAME> and miss(es).", //2
		"<S-NAME> attack(s) <T-NAMESELF> with <TOOLNAME> and miss(es).", //3
		"<S-NAME> lunge(s) at <T-NAMESELF> with <TOOLNAME> and miss(es)." //4
	};
	public final static String[] missStrs2={
		"<S-NAME> fire(s) at <T-NAMESELF> and miss(es).", //0
		"<S-NAME> throw(s) at <T-NAMESELF> and miss(es).", //1
		"<S-NAME> swing(s) at <T-NAMESELF> and miss(es).", //2
		"<S-NAME> attack(s) <T-NAMESELF> and miss(es).",  //3
		"<S-NAME> lunge(s) at <T-NAMESELF> and miss(es)." //4
	};
}
