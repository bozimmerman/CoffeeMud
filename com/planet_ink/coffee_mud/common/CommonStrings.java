package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
public class CommonStrings extends Scriptable
{
	public static String[] clookup=null;
	public static final int SYSTEM_PKILL=0;
	public static final int SYSTEM_MULTICLASS=1;
	public static final int SYSTEM_PLAYERDEATH=2;
	public static final int SYSTEM_PLAYERFLEE=3;
	public static final int SYSTEM_SHOWDAMAGE=4;
	public static final int SYSTEM_EMAILREQ=5;
	public static final int NUM_SYSTEM=6;
	
	public static final int SYSTEMI_EXPRATE=0;
	public static final int SYSTEMI_SKYSIZE=1;
	public static final int SYSTEMI_MAXSTAT=2;
	public static final int SYSTEMI_EDITORTYPE=3;
	public static final int SYSTEMI_MINCLANMEMBERS=4;
	public static final int SYSTEMI_DAYSCLANDEATH=5;
	public static final int SYSTEMI_MINCLANLEVEL=6;
	public static final int NUMI_SYSTEM=7;
	
	private static String[] sysVars=new String[NUM_SYSTEM];
	private static Integer[] sysInts=new Integer[NUMI_SYSTEM];

	public static int pkillLevelDiff=26;
	
	public static int getPKillLevelDiff(){return pkillLevelDiff;}
	
	public static String getVar(int varNum)
	{
		if((varNum<0)||(varNum>=NUM_SYSTEM)) return "";
		if(sysVars[varNum]==null) return "";
		return sysVars[varNum];
	}
	
	public static int getIntVar(int varNum)
	{
		if((varNum<0)||(varNum>=NUMI_SYSTEM)) return -1;
		if(sysInts[varNum]==null) return -1;
		return sysInts[varNum].intValue();
	}
	
	public static void setIntVar(int varNum, int val)
	{
		if((varNum<0)||(varNum>=NUMI_SYSTEM)) return ;
		sysInts[varNum]=new Integer(val);
	}
	
	public static void setIntVar(int varNum, String val)
	{
		if((varNum<0)||(varNum>=NUMI_SYSTEM)) return ;
		if(val==null) val="0";
		sysInts[varNum]=new Integer(Util.s_int(val));
	}
	
	public static void setVar(int varNum, String val)
	{
		if((varNum<0)||(varNum>=NUM_SYSTEM)) return ;
		if(val==null) val="";
		sysVars[varNum]=val.toUpperCase();
		switch(varNum)
		{
		case SYSTEM_PKILL:
			{
				int x=val.indexOf("-");
				if(x>0)
					pkillLevelDiff=Util.s_int(val.substring(x+1));
			}
			break;
		}
	}
	
	public static String[] standardColorLookups()
	{
		if(clookup==null)
		{
			clookup=new String[256];
			// N B H - normal bold highlight underline flash italic
			clookup[(int)'N']="^w\033[0m";
			clookup[(int)'!']="\033[1m";
			clookup[(int)'H']="^c";
			clookup[(int)'_']="\033[4m";
			clookup[(int)'*']="\033[5m";
			clookup[(int)'/']="\033[6m";
			// reset
			clookup[(int)'.']="\033[0m";
			clookup[(int)'^']="^";
			// F S - fight spell
			clookup[(int)'F']="^r";
			clookup[(int)'S']="^y";
			// E T Q - emote talk channeltalk
			clookup[(int)'E']="^p";
			clookup[(int)'T']="^b";
			clookup[(int)'Q']="\033[0;36;44m";
			// X Y Z - important messages
			clookup[(int)'x']="\033[1;36;44m";
			clookup[(int)'X']="\033[1;33;44m";
			clookup[(int)'Z']="\033[1;33;41m";
			//  R L D d - roomtitle roomdesc(look) Direction door
			clookup[(int)'O']="^c";
			clookup[(int)'L']="^w";
			clookup[(int)'D']="\033[1;36;44m";
			clookup[(int)'d']="^b";
			// I M - item, mob
			clookup[(int)'I']="^g";
			clookup[(int)'M']="^p";
			
			// h m v - prompt colors - deprecated!!
			clookup[(int)'h']="^c";
			clookup[(int)'m']="^c";
			clookup[(int)'v']="^c";
			
			// fixed system colors, 1= bright, 0=dark
			clookup[(int)'w']="\033[1;37m";
			clookup[(int)'g']="\033[1;32m";
			clookup[(int)'b']="\033[1;34m";
			clookup[(int)'r']="\033[1;31m";
			clookup[(int)'y']="\033[1;33m";
			clookup[(int)'c']="\033[1;36m";
			clookup[(int)'p']="\033[1;35m";
			clookup[(int)'W']="\033[0;37m";
			clookup[(int)'G']="\033[0;32m";
			clookup[(int)'B']="\033[0;34m";
			clookup[(int)'R']="\033[0;31m";
			clookup[(int)'Y']="\033[0;33m";
			clookup[(int)'C']="\033[0;36m";
			clookup[(int)'P']="\033[0;35m";
			for(int i=0;i<clookup.length;i++)
			{
				String s=clookup[i];
				if((s!=null)&&(s.startsWith("^"))&&(s.length()>1))
					clookup[i]=clookup[(int)s.charAt(1)];
			}
		}
		return clookup;
	}
	
	
	public static String standardHitWord(int type, int damage)
	{
		if(type<0) type=Weapon.TYPE_BURSTING;
		int damnCode=0;
			 if(damage<=3) damnCode=0;
		else if(damage<=6) damnCode=1;
		else if(damage<=10) damnCode=2;
		else if(damage<=15) damnCode=3;
		else if(damage<=25) damnCode=4;
		else if(damage<=35) damnCode=5;
		else if(damage<=45) damnCode=6;
		else if(damage<=65) damnCode=7;
		else if(damage<=95) damnCode=8;
		else if(damage<=115) damnCode=9;
		else if(damage<=145) damnCode=10;
		else if(damage<=175) damnCode=11;
		else if(damage<=225) damnCode=12;
		else if(damage<=300) damnCode=13;
		else damnCode=14;
		
		switch(damnCode)
		{
			case 7: return "massacre(s)";
			case 8: return "MASSACRE(S)";
			case 9: return "destroy(s)";
			case 10: return "DESTROY(S)";
			case 11: return "obliterate(s)";
			case 12: return "OBLITERATE(S)";
			case 13: return "**OBLITERATE(S)**";
			case 14: return "--==::OBLITERATE(S)::==--";
		default:
			break;
		}
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
			case 4: return "damage(s)";
			case 5: return "decimate(s)";
			case 6: return "murder(s)";
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
	
	
	public static String standardHitString(int weaponType, int weaponClass, int damageAmount,  String weaponName)
	{
		if((weaponName==null)||(weaponName.length()==0))
			weaponClass=Weapon.CLASS_NATURAL;
		boolean showDamn=CommonStrings.getVar(CommonStrings.SYSTEM_SHOWDAMAGE).equalsIgnoreCase("YES");
		switch(weaponClass)
		{
		case Weapon.CLASS_RANGED:
			return "<S-NAME> fire(s) "+weaponName+" at <T-NAMESELF> and "+standardHitWord(weaponType,damageAmount)+((showDamn)?" ("+damageAmount+")":"")+" <T-HIM-HER>";
		case Weapon.CLASS_THROWN:
			return "<S-NAME> throw(s) "+weaponName+" at <T-NAMESELF> and "+standardHitWord(weaponType,damageAmount)+((showDamn)?" ("+damageAmount+")":"")+" <T-HIM-HER>";
		default:
			return "<S-NAME> "+standardHitWord(weaponType,damageAmount)+((showDamn)?" ("+damageAmount+")":"")+" <T-NAMESELF> with "+weaponName;
		}
	}
	
	public static String standardMobCondition(MOB mob)
	{
		switch((int)Math.round(Math.floor((Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()))*10)))
		{
		case 0:	return "^r" + mob.displayName() + "^r is hovering on deaths door!^N";
		case 1:	return "^r" + mob.displayName() + "^r is covered in blood.^N";
		case 2:	return "^r" + mob.displayName() + "^r is bleeding badly from lots of wounds.^N";
		case 3:	return "^y" + mob.displayName() + "^y has numerous bloody wounds and gashes.^N";
		case 4:	return "^y" + mob.displayName() + "^y has some bloody wounds and gashes.^N";
		case 5:	return "^p" + mob.displayName() + "^p has a few bloody wounds.^N";
		case 6:	return "^p" + mob.displayName() + "^p is cut and bruised.^N";
		case 7:	return "^g" + mob.displayName() + "^g has some minor cuts and bruises.^N";
		case 8:	return "^g" + mob.displayName() + "^g has a few bruises and scratches.^N";
		case 9:	return "^g" + mob.displayName() + "^g has a few small bruises.^N";
		default: return "^c" + mob.displayName() + "^c is in perfect health^N";
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
	"death incarnate!"		//14
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
