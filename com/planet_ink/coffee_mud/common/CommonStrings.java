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
	public static final int SYSTEM_ESC0=6;
	public static final int SYSTEM_ESC1=7;
	public static final int SYSTEM_ESC2=8;
	public static final int SYSTEM_ESC3=9;
	public static final int SYSTEM_ESC4=10;
	public static final int SYSTEM_ESC5=11;
	public static final int SYSTEM_ESC6=12;
	public static final int SYSTEM_ESC7=13;
	public static final int SYSTEM_ESC8=14;
	public static final int SYSTEM_ESC9=15;
	public static final int SYSTEM_MSPPATH=16;
	public static final int NUM_SYSTEM=17;

	public static final int SYSTEMI_EXPRATE=0;
	public static final int SYSTEMI_SKYSIZE=1;
	public static final int SYSTEMI_MAXSTAT=2;
	public static final int SYSTEMI_EDITORTYPE=3;
	public static final int SYSTEMI_MINCLANMEMBERS=4;
	public static final int SYSTEMI_DAYSCLANDEATH=5;
	public static final int SYSTEMI_MINCLANLEVEL=6;
	public static final int SYSTEMI_MANACOST=7;
	public static final int SYSTEMI_COMMONTRAINCOST=8;
	public static final int SYSTEMI_LANGTRAINCOST=9;
	public static final int SYSTEMI_SKILLTRAINCOST=10;
	public static final int SYSTEMI_COMMONPRACCOST=11;
	public static final int SYSTEMI_LANGPRACCOST=12;
	public static final int SYSTEMI_SKILLPRACCOST=13;
	public static final int SYSTEMI_CLANCOST=14;
	public static final int NUMI_SYSTEM=15;
	  
	public static final int SYSTEMB_MOBCOMPRESS=0;
	public static final int SYSTEMB_ITEMDCOMPRESS=1;
	public static final int SYSTEMB_ROOMDCOMPRESS=2;
	public static final int SYSTEMB_MOBDCOMPRESS=3;
	public static final int NUMB_SYSTEM=4;

	private static String[] sysVars=new String[NUM_SYSTEM];
	private static Integer[] sysInts=new Integer[NUMI_SYSTEM];
	private static Boolean[] sysBools=new Boolean[NUMB_SYSTEM];

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

	public static boolean getBoolVar(int varNum)
	{
		if((varNum<0)||(varNum>=NUMB_SYSTEM)) return false;
		if(sysBools[varNum]==null) return false;
		return sysBools[varNum].booleanValue();
	}

	public static void setBoolVar(int varNum, boolean val)
	{
		if((varNum<0)||(varNum>=NUMB_SYSTEM)) return ;
		sysBools[varNum]=new Boolean(val);
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
			// E T Q - emote talk channeltalk channelfore
			clookup[(int)'E']="^p";
			clookup[(int)'T']="^b";
			clookup[(int)'Q']="\033[0;36;44m";
			clookup[(int)'q']="^c";
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
			 if(damage<=3) damnCode=0; //3
		else if(damage<=6) damnCode=1; //4
		else if(damage<=10) damnCode=2;//5
		else if(damage<=15) damnCode=3; //10
		else if(damage<=25) damnCode=4; //10
		else if(damage<=35) damnCode=5; //15
		else if(damage<=50) damnCode=6; //20
		else if(damage<=70) damnCode=7; //30
		else if(damage<=100) damnCode=8; //30
		else if(damage<=130) damnCode=9; //35
		else if(damage<=165) damnCode=10; //50
		else if(damage<=215) damnCode=11; //75
		else if(damage<=295) damnCode=12; //100
		else if(damage<=395) damnCode=13;
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

	public static final int ARMOR_CEILING=500;
	public static final int ATTACK_CEILING=600;
	
	public static String armorStr(int armor){
		return (armor<0)?armorStrs[0]:(
			   (armor>=ARMOR_CEILING)?armorStrs[armorStrs.length-1]+" ("+armor+")":(
				armorStrs[(int)Math.round(Math.floor(Util.mul(Util.div(armor,ARMOR_CEILING),armorStrs.length)))]+" ("+armor+")"));}
	public static String fightingProwessStr(int prowess){
		return (prowess<0)?fightStrs[0]:(
			   (prowess>=ATTACK_CEILING)?fightStrs[fightStrs.length-1]+" ("+prowess+")":(
				fightStrs[(int)Math.round(Math.floor(Util.mul(Util.div(prowess,ATTACK_CEILING),fightStrs.length)))]+" ("+prowess+")"));}
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
		return str.substring(0,dexTool)+weaponName+str.substring(dexTool+10)+msp("missed.wav",20);
	}


	public static String standardHitString(int weaponType, int weaponClass, int damageAmount,  String weaponName)
	{
		if((weaponName==null)||(weaponName.length()==0))
			weaponClass=Weapon.CLASS_NATURAL;
		boolean showDamn=CommonStrings.getVar(CommonStrings.SYSTEM_SHOWDAMAGE).equalsIgnoreCase("YES");
		switch(weaponClass)
		{
		case Weapon.CLASS_RANGED:
			return "<S-NAME> fire(s) "+weaponName+" at <T-NAMESELF> and "+standardHitWord(weaponType,damageAmount)+((showDamn)?" ("+damageAmount+")":"")+" <T-HIM-HER>."+msp("arrow.wav",20);
		case Weapon.CLASS_THROWN:
			return "<S-NAME> throw(s) "+weaponName+" at <T-NAMESELF> and "+standardHitWord(weaponType,damageAmount)+((showDamn)?" ("+damageAmount+")":"")+" <T-HIM-HER>."+msp("arrow.wav",20);
		default:
			return "<S-NAME> "+standardHitWord(weaponType,damageAmount)+((showDamn)?" ("+damageAmount+")":"")+" <T-NAMESELF> with "+weaponName+"."+msp("punch"+Dice.roll(1,4,0)+".wav",20);
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

	// this is the sound support method.
	// it builds a valid MSP sound code from built-in web server
	// info, and the info provided.
	public static String msp(String soundName, int volume, int priority)
	{
		if(getVar(SYSTEM_MSPPATH).length()>0)
			return " !!SOUND("+soundName+" V="+volume+" P="+priority+" U="+getVar(SYSTEM_MSPPATH)+soundName+") ";
		else
			return " !!SOUND("+soundName+" V="+volume+" P="+priority+") ";
	}
	
	public static String msp(String soundName, int priority)
	{ return msp(soundName,50,Dice.roll(1,50,priority));}

	public static String[] armorStrs={
	"vulnerable",
	"slightly covered",
	"somewhat covered",
	"covered",
	"well covered",
	"very covered",
	"slightly protected",
	"somewhat protected",
	"protected",
	"well protected",
	"very protected",
	"heavily protected",
	"slightly armored",
	"somewhat armored",
	"armored",
	"armored",
	"well armored",
	"very armored",
	"heavily armored",
	"completely armored",
	"totally armored",
	"divinely armored",
	"slightly unhittable",
	"somewhat unhittable",
	"practically unhittable",
	"unhittable",
	"unhittable",
	"totally unhittable",
	"slightly impenetrable",
	"somewhat impenetrable",
	"almost impenetrable",
	"impenetrable",
	"impenetrable",
	"slightly invincible",
	"slightly invincible",
	"slightly invincible",
	"somewhat invincible",
	"somewhat invincible",
	"somewhat invincible",
	"somewhat invincible",
	"almost invincible",
	"almost invincible",
	"almost invincible",
	"almost invincible",
	"almost invincible",
	"invincible!",
	};
	
	public final static String[] fightStrs={
	"none",
	"mostly unskilled",
	"a little skilled",
	"somewhat skilled",
	"almost skilled",
	"mostly skilled",
	"skilled",
	"skilled",
	"very skilled",
	"extremely skilled",
	"a little dangerous",
	"somewhat dangerous",
	"almost dangerous",
	"mostly dangerous",
	"dangerous",
	"dangerous",
	"very dangerous",
	"extremely dangerous",
	"a master I",
	"a master II",
	"a master III",
	"a master IV",
	"a master V",
	"a master VI",
	"a master VII",
	"a master VIII",
	"a master IX",
	"a master X",
	"a servant of death",
	"a bringer of death",
	"a bringer of death",
	"a giver of death",
	"a giver of death",
	"a giver of death",
	"a dealer of death",
	"a dealer of death",
	"a dealer of death",
	"a dealer of death",
	"a master of death",
	"a master of death",
	"a master of death",
	"a master of death",
	"a master of death",
	"a lord of death",
	"a lord of death",
	"a lord of death",
	"a lord of death",
	"a lord of death",
	"a lord of death",
	"death incarnate!"
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
