package com.planet_ink.coffee_mud.Commands.sysop;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.Commands.base.*;
import java.io.*;
import java.util.*;


public class Import
{
	private Import(){}

	private static String getAreaName(Vector V)
	{
		// find area line first
		String areaName="";
		if((nextLine(V).indexOf("~")>=0)&&(nextLine(V).indexOf("}")>=0))
		{
			String areaLine=nextLine(V);
			areaLine=areaLine.substring(0,areaLine.length()-1);
			int x=areaLine.indexOf("}");
			areaLine=areaLine.substring(x+1).trim();
			x=areaLine.indexOf("  ");
			if(x>0)
				areaLine=areaLine.substring(x+1).trim();
			areaName=areaLine;
		}
		else
		if(V.size()>1)
		{
			String lineAfter=(String)V.elementAt(1);
			if(lineAfter.indexOf("~")<0)
				return "";
			lineAfter=lineAfter.substring(0,lineAfter.length()-1);
			if((lineAfter.indexOf(".are")>=0)&&(V.size()>2)&&(lineAfter.indexOf("@@")<0))
			{
				lineAfter=(String)V.elementAt(2);
				if(lineAfter.indexOf("~")<0)
					return "";
				lineAfter=lineAfter.substring(0,lineAfter.length()-1);
				areaName=lineAfter.trim();
			}
			else
				areaName=removeAtAts(lineAfter).trim();
		}
		return Util.safetyFilter(areaName);
	}

	private static String removeAtAts(String str)
	{
		int x=str.indexOf("@@");
		while(x>=0)
		{
			str=str.substring(0,x)+str.substring(x+3);
			x=str.indexOf("@@");
		}
		return str;
	}

	private static String nextLine(Vector V)
	{
		if(V.size()==0) return "";
		return (String)V.elementAt(0);
	}
	private static String eatLine(Vector V)
	{
		if(V.size()==0) return "";
		String s=(String)V.elementAt(0);
		V.removeElementAt(0);
		return s;
	}
	private static String eatNextLine(Vector V)
	{
		String s="";
		while((s.trim().length()==0)&&(V.size()>0))
			s=eatLine(V);
		return s;
	}

	private static Room changeRoomClass(Room R, String newClass)
	{
		Room R2=CMClass.getLocale(newClass);
		if(R2==null)
		{
			Log.errOut("Import","Cannot find room class "+newClass+".");
			return R;
		}
		R2.setRoomID(R.roomID());
		R2.setArea(R.getArea());
		R2.setDescription(R.description());
		R2.setDisplayText(R.displayText());
		R2.setName(R.name());
		R2.setBaseEnvStats(R.baseEnvStats());
		R2.setMiscText(R.text());
		return R2;
	}

	private static int getBitMask(String str, int which)
	{
		String s=Util.getBit(str,which);
		if(s.length()==0)
			return 0;
		int x=s.indexOf("|");
		if((x<0)&&(s.length()>0)&&(Util.s_int(s)==0))
		{
			boolean otherStyle=true;
			int num=0;
			for(int z=0;z<s.length();z++)
				if(!Character.isLetter(s.charAt(z)))
				{
				   otherStyle=false;
				   break;
				}
				else
				if(Character.isUpperCase(s.charAt(z)))
					num=num|(1<<((int)s.charAt(z))-((int)'A'));
				else
				if(Character.isLowerCase(s.charAt(z)))
					num=num|(1<<(26+(((int)s.charAt(z))-((int)'a'))));

			if(otherStyle)
				return num;
		}

		int num=0;
		while(x>0)
		{
			num=num|Util.s_int(s.substring(0,x));
			s=s.substring(x+1);
			x=s.indexOf("|");
		}

		return (num|Util.s_int(s));
	}

	private static String eatLineSquiggle(Vector V)
	{
		if(V.size()==0) return "";
		String s=eatLine(V);
		while(s.indexOf("~")<0)
		{
			String l=eatLine(V);
			if(l.startsWith(" "))
				s+="%0D"+l;
			else
			if(l.length()==0)
				s+="%0D";
			else
				s+=" "+l;
		}
		s=s.trim();
		if(s.endsWith("~"))
			s=s.substring(0,s.length()-1).trim();
		s=Util.replaceAll(s,"^","^^");
		return s;
	}

	private static boolean hasReadableContent(String objectName)
	{
		objectName=objectName.toUpperCase();
		if((objectName.indexOf("SIGN")>=0)
			||(objectName.indexOf("PLAQUE")>=0)
		    ||(objectName.indexOf("NOTICE")>=0)
		    ||(objectName.indexOf("PAPER")>=0)
		    ||(objectName.indexOf("WRITING")>=0)
		    ||(objectName.indexOf("CARVING")>=0)
		    ||(objectName.indexOf("LETTER")>=0)
		    ||(objectName.indexOf("INSCRIPTION")>=0)
		    ||(objectName.indexOf("NOTE")>=0)
		    ||(objectName.indexOf("POST")>=0))
				return true;
		return false;
	}

	private static String fixReadableContent(String text)
	{
		while(text.startsWith("%0D"))
			text=text.substring(3);
		if((text.toUpperCase().trim().startsWith("IT SAYS `"))
		||(text.toUpperCase().trim().startsWith("IT SAYS '")))
		{
			text=text.trim().substring(9).trim();
			if((text.endsWith("'"))||(text.endsWith("`")))
				text=text.substring(0,text.length()-1);
		}
		if(text.toUpperCase().trim().startsWith("IT SAYS:"))
			text=text.trim().substring(8).trim();
		if(text.toUpperCase().trim().startsWith("IT SAYS"))
			text=text.trim().substring(7).trim();
		return text;
	}

	private static boolean returnAnError(MOB mob, String str)
	{
		Log.errOut("Import",str);
		mob.tell(str);
		return false;
	}

	private static String getSpell(String word, int i)
	{
		if((word.trim().length()>0)&&((Character.isLetter(word.trim().charAt(0)))||(word.trim().startsWith("'"))||(word.trim().startsWith("`"))))
		{
			word=word.toUpperCase().trim();
			if((word.startsWith("'"))||(word.startsWith("`")))
			   word=word.substring(1);
			if(word.length()<3)	return "";

			if(word.startsWith("ACID B")) i=70;
			else
			if(word.startsWith("ARMOR")) i=1;
			else
			if(word.startsWith("BLESS")) i=3;
			else
			if(word.startsWith("BLINDNE")) i=4;
			else
			if(word.startsWith("BURNING H")) i=5;
			else
			if(word.startsWith("HASTE")) i=84;
			else
			if(word.startsWith("CALL LIGH")) i=6;
			else
			if(word.startsWith("GENERAL PURPOSE")) i=-1;
			else
			if(word.startsWith("CANCELLA")) i=59;
			else
			if(word.startsWith("CAUSE CRI")) i=63;
			else
			if(word.startsWith("CAUSE LI")) i=62;
			else
			if(word.startsWith("CHANGE SE")) i=82;
			else
			if(word.startsWith("CHARM PER")) i=7;
			else
			if(word.startsWith("CHILL TOU")) i=8;
			else
			if(word.startsWith("COLO")) i=10;
			else
			if(word.startsWith("COLO")) i=10;
			else
			if(word.startsWith("FIREBA")) i=26;
			else
			if(word.startsWith("FLAMESTR")) i=65;
			else
			if(word.startsWith("FLY")) i=56;
			else
			if(word.startsWith("GATE")) i=83;
			else
			if(word.startsWith("GIANT")) i=39;
			else
			if(word.startsWith("HARM")) i=27;
			else
			if(word.startsWith("HEAL")) i=28;
			else
			if(word.startsWith("IDENTIFY")) i=53;
			else
			if(word.startsWith("INFRAVISION")) i=77;
			else
			if(word.startsWith("INVIS")) i=29;
			else
			if(word.startsWith("KNOW")) i=58;
			else
			if(word.startsWith("LIGHTNING BOLT")) i=30;
			else
			if(word.startsWith("LIGHTENING BOLT")) i=30;
			else
			if(word.startsWith("LIGHTNINGBOLT")) i=30;
			else
			if(word.startsWith("LIGHTENINGBOLT")) i=30;
			else
			if(word.startsWith("LOCATE")) i=31;
			else
			if(word.startsWith("CANCELL")) i=57;
			else
			if(word.startsWith("CONTINU")) i=57;
			else
			if(word.startsWith("CONTROL")) i=11;
			else
			if(word.startsWith("CREATE FO")) i=12;
			else
			if(word.startsWith("CREATE SP")) i=80;
			else
			if(word.startsWith("CREATE WA")) i=13;
			else
			if(word.startsWith("CURE BLI")) i=14;
			else
			if(word.startsWith("CURE CRI")) i=15;
			else
			if(word.startsWith("CURE LI")) i=16;
			else
			if(word.startsWith("CURE PO")) i=43;
			else
			if(word.startsWith("CURE SE")) i=61;
			else
			if(word.startsWith("CURE D")) i=45;
			else
			if(word.startsWith("DETECT E")) i=18;
			else
			if(word.startsWith("DETECT HI")) i=44;
			else
			if(word.startsWith("DETECT I")) i=19;
			else
			if(word.startsWith("DETECT M")) i=20;
			else
			if(word.startsWith("DETECT P")) i=21;
			else
			if(word.startsWith("DISPEL E")) i=22;
			else
			if(word.startsWith("DISPEL M")) i=59;
			else
			if(word.startsWith("EARTHQ")) i=23;
			else
			if(word.startsWith("ENCHANT W")) i=24;
			else
			if(word.startsWith("ENERGY DRA")) i=25;
			else
			if(word.startsWith("FAERIE F")) i=72;
			else
			if(word.startsWith("MAGIC MI")) i=32;
			else
			if(word.startsWith("MASS INV")) i=69;
			else
			if(word.startsWith("PASS D")) i=74;
			else
			if(word.startsWith("POISON")) i=33;
			else
			if(word.startsWith("PROTECTION")) i=34;
			else
			if(word.startsWith("REFRESH")) i=81;
			else
			if(word.startsWith("REMOVE CU")) i=35;
			else
			if(word.startsWith("SANCTUARY")) i=36;
			else
			if(word.startsWith("SHIELD")) i=67;
			else
			if(word.startsWith("SHOCKING G")) i=37;
			else
			if(word.startsWith("SLEEP")) i=38;
			else
			if(word.startsWith("STONE SK")) i=66;
			else
			if(word.startsWith("SUMMON")) i=40;
			else
			if(word.startsWith("TELEPORT")) i=2;
			else
			if(word.startsWith("VENTRI")) i=41;
			else
			if(word.startsWith("WEAKEN")) i=68;
			else
			if(word.startsWith("WORD OF R")) i=42;
			else
			if(word.startsWith("ACID BR")) i=200;
			else
			if(word.startsWith("FIRE BR")) i=201;
			else
			if(word.startsWith("FROST BR")) i=202;
			else
			if(word.startsWith("GAS BR")) i=203;
			else
			if(word.startsWith("LIGHTNING BR")) i=204;
			else
			if(word.startsWith("LIGHTENING BR")) i=204;
			else
			if(word.startsWith("FRENZY")) i=205;
			else
			if(word.startsWith("DISPEL G")) i=206;
			else
			if(word.startsWith("CURSE")) i=17;
			else
			if(word.startsWith("ENCHANT W")) i=207;
			else
			if(word.startsWith("ENCHANT A")) i=208;
			else
			if(word.startsWith("REJUV")) i=209;
			else
			if(word.startsWith("HEAT M")) i=210;
			else
			if(word.startsWith("HIGH EXP")) i=26;
			else
			if(word.startsWith("FARSIGHT")) i=211;
			else
			{
				Log.sysOut("Unknown spell: "+word);
				return "";
			}
		}

		switch(i)
		{
		case -1: break;
		case 0: break;
		case 1: return "Spell_GraceOfTheCat"; // armor
		case 2: return "Spell_Teleport";
		case 3: return "Prayer_Bless";
		case 4: return "Spell_Blindness";
		case 5: return "Spell_BurningHands";
		case 6: return "Spell_Clog"; // call lightening, dumb
		case 7: return "Spell_Charm";
		case 8: return "Spell_Frost"; // chill touch
		case 9: return "Spell_MirrorImage"; // clone
		case 10: return "Spell_Feeblemind"; // color spray
		case 11: return "Chant_CallRain";
		case 12: return "Prayer_CreateFood";
		case 13: return "Prayer_CreateWater";
		case 14: return "Prayer_CureBlindness";
		case 15: return "Prayer_CureCritical";
		case 16: return "Prayer_CureLight";
		case 17: return "Prayer_Curse";
		case 18: return "Prayer_SenseEvil";
		case 19: return "Spell_DetectInvisible";
		case 20: return "Spell_DetectMagic";
		case 21: return "Thief_DetectTraps"; // detect poison actually
		case 22: return "Prayer_DispelEvil";
		case 23: return "Spell_Earthquake";
		case 24: return "Spell_EnchantWeapon";
		case 25: return "Prayer_Drain";
		case 26: return "Spell_Fireball";
		case 27: return "Prayer_Harm";
		case 28: return "Prayer_Heal";
		case 29: return "Spell_Invisibility";
		case 30: return "Spell_Lightning";
		case 31: return "Spell_LocateObject";
		case 32: return "Spell_MagicMissile";
		case 33: return "Thief_Poison";
		case 34: return "Prayer_ProtEvil";
		case 35: return "Prayer_RemoveCurse";
		case 36: return "Prayer_Sanctuary";
		case 37: return "Spell_ShockingGrasp";
		case 38: return "Spell_Sleep";
		case 39: return "Spell_GiantStrength";
		case 40: return "Spell_Summon";
		case 41: return "Spell_Ventriloquate";
		case 42: return "Skill_Recall";
		case 43: return "Prayer_RemovePoison";
		case 44: return "Spell_DetectHidden";
		case 45: return "Prayer_CureDisease"; // not the real ###
		case 53: return "Spell_IdentifyObject";
		case 54: return "Prayer_AnimateDead";
		case 55: return "Spell_Fear";
		case 56: return "Spell_Fly";
		case 57: return "Spell_Light";
		case 58: return "Spell_KnowAlignment";
		case 59: return "Spell_DispelMagic";
		case 61: return "Prayer_CureSerious";
		case 62: return "Prayer_CauseLight";
		case 63: return "Spell_WaterBreathing"; // water of lifew
		case 64: return "Prayer_CauseSerious";
		case 65: return "Spell_Dragonfire"; // flamestrike
		case 66: return "Spell_Stoneskin";
		case 67: return "Spell_Shield";
		case 68: return "Spell_Weaken";
		case 69: return "Spell_MassInvisibility";
		case 70: return "Spell_AcidArrow"; // acid blast
		case 71: return "Spell_Portal"; // actually mass teleport
		case 72: return "Spell_FaerieFog";
		case 73: return "Spell_IceStorm";
		case 74: return "Spell_PassDoor";
		case 76: return "Spell_StoneFlesh"; // stone
		case 77: return "Spell_Infravision";
		case 80: return "Prayer_CreateWater";
		case 81: return "Prayer_Calm"; // refresh
		case 82: return "Spell_ChangeSex";
		case 83: return "Spell_Gate";
		case 84: return "Spell_Haste";
		case 97: return "Spell_Web";
		case 98: return "Spell_EnchantArmor";
		case 99: return "Spell_Teleport";
		case 122: return "Chant_SummonElemental"; // summon elemental
		case 201: return "Firebreath";
		case 203: return "Gasbreath";
		case 202: return "Frostbreath";
		case 200: return "Acidbreath";
		case 204: return "Lighteningbreath";
		case 205: return "Spell_Frenzy";
		case 206: return "Prayer_DispelGood";
		case 207: return "Spell_EnchantWeapon";
		case 208: return "Spell_EnchantArmor";
		case 209: return "Prayer_Restoration";
		case 210: return "Spell_HeatMetal";
		case 211: return "Spell_Farsight";
		default:
			Log.sysOut("Unknown spell num: "+i);
			break;
		}
		return "";
	}

	private static void readBlocks(Vector buf,
						   Vector areaData,
						   Vector roomData,
						   Vector mobData,
						   Vector resetData,
						   Vector objectData,
						   Vector mobProgData,
						   Vector objProgData,
						   Vector shopData,
						   Vector specialData,
						   Vector socialData)
	{
		Vector helpsToEat=new Vector();

		Vector wasUsingThisOne=null;
		Vector useThisOne=null;
		while(buf.size()>0)
		{
			String s=((String)buf.elementAt(0)).toUpperCase().trim();
			if(s.startsWith("#")&&((String)buf.elementAt(0)).startsWith(" "))
				s=((String)buf.elementAt(0)).toUpperCase();
			boolean okString=true;
			if(s.startsWith("#"))
			{
				s=s.substring(1).trim();
				if(s.startsWith("AREA"))
				{
					wasUsingThisOne=null;
					useThisOne=areaData;
				}
				else
				if(s.startsWith("HELPS"))
				{
					wasUsingThisOne=null;
					useThisOne=helpsToEat;
				}
				else
				if(s.startsWith("MOBILES"))
				{
					wasUsingThisOne=mobData;
					useThisOne=mobData;
				}
				else
				if(s.startsWith("OBJECTS"))
				{
					wasUsingThisOne=objectData;
					useThisOne=objectData;
				}
				else
				if(s.startsWith("MOBPROG"))
				{
					wasUsingThisOne=objectData;
					useThisOne=mobProgData;
				}
				else
				if(s.startsWith("OBJFUNS"))
				{
					wasUsingThisOne=objectData;
					useThisOne=mobProgData;
				}
				else
				if(s.startsWith("ROOMS"))
				{
					wasUsingThisOne=roomData;
					useThisOne=roomData;
				}
				else
				if(s.startsWith("RESETS"))
				{
					wasUsingThisOne=null;
					useThisOne=resetData;
				}
				else
				if(s.startsWith("SHOP"))
				{
					wasUsingThisOne=null;
					useThisOne=shopData;
				}
				else
				if(s.startsWith("SPECIALS"))
				{
					wasUsingThisOne=null;
					useThisOne=specialData;
				}
				else
				if(s.startsWith("SOCIALS"))
				{
					wasUsingThisOne=null;
					useThisOne=socialData;
				}
				else
				if((Util.s_int(s)>0)&&(wasUsingThisOne!=null))
				{
					Vector V=new Vector();
					wasUsingThisOne.addElement(V);
					useThisOne=V;
				}
				else
				if(s.equals("0")||s.equals("$")||s.equals("O"))
				{
					okString=false;
				}
				else
				if((s.equals("")||s.equals("~"))&&(useThisOne==socialData))
					okString=true;
				else
				{
					//useThisOne=null;
					Log.sysOut("Import","Suspect line: "+s);
				}
			}
			if(useThisOne!=null)
			{
				if(okString)
				{
					String s2=Util.safetyFilter(removeAtAts((String)buf.elementAt(0)));
					useThisOne.addElement(s2);
				}
				buf.removeElementAt(0);
			}
			else
			{
				Log.sysOut("Import","Just eating: "+s);
				buf.removeElementAt(0);
			}
		}
		if(helpsToEat.size()>0)
			Log.sysOut("Import","Ate "+helpsToEat.size()+" help lines.");
	}

	private static void doWeapon(Weapon I, String name, int val1, String str1, int val2, int val3, int val4, String str4)
	{
		if((str1.trim().length()>0)&&((Character.isLetter(str1.trim().charAt(0)))||(str1.trim().startsWith("'"))))
		{
			str1=str1.toUpperCase().trim();
			if(str1.startsWith("'"))
			   str1=str1.substring(1);
			if(str1.startsWith("EXOTIC")) val1=0;
			else
			if(str1.startsWith("SWORD")) val1=1;
			else
			if(str1.startsWith("DAGGER")) val1=9;
			else
			if(str1.startsWith("SPEAR")) val1=3;
			else
			if(str1.startsWith("MACE"))
			{
				val1=4;
				if(name.toUpperCase().endsWith("HAMMER"))
					val1=11;
			}
			else
			if(str1.startsWith("AXE")) val1=5;
			else
			if(str1.startsWith("FLAIL")) val1=6;
			else
			if(str1.startsWith("WHIP")) val1=7;
			else
			if(str1.startsWith("POLE")) val1=8;
			else
			if(str1.startsWith("STAFF")) val1=10;
		}

		switch(val1)
		{
		case 0: ((Weapon)I).setWeaponClassification(Weapon.CLASS_RANGED);
				if(name.toUpperCase().indexOf("BOW")>=0)
				{
					((Weapon)I).setAmmoCapacity(20);
					((Weapon)I).setAmmoRemaining(20);
					((Weapon)I).setAmmunitionType("arrows");
					((Weapon)I).setRanges(1,3);
					((Weapon)I).setRawLogicalAnd(true);
				}
				break;
		case 1: ((Weapon)I).setWeaponClassification(Weapon.CLASS_SWORD); break;
		case 2: ((Weapon)I).setWeaponClassification(Weapon.CLASS_EDGED); break;
		case 3: ((Weapon)I).setWeaponClassification(Weapon.CLASS_POLEARM);
				((Weapon)I).setRanges(0,1);
				((Weapon)I).setRawLogicalAnd(true);
				break;
		case 4: ((Weapon)I).setWeaponClassification(Weapon.CLASS_BLUNT); break;
		case 5: ((Weapon)I).setWeaponClassification(Weapon.CLASS_AXE); break;
		case 6: ((Weapon)I).setWeaponClassification(Weapon.CLASS_FLAILED);
				((Weapon)I).setRanges(0,1);
				break;
		case 7: ((Weapon)I).setWeaponClassification(Weapon.CLASS_FLAILED);
				((Weapon)I).setRanges(0,1);
				break;
		case 8: ((Weapon)I).setWeaponClassification(Weapon.CLASS_POLEARM);
				((Weapon)I).setRanges(0,1);
				((Weapon)I).setRawLogicalAnd(true);
				break;
		case 9: ((Weapon)I).setWeaponClassification(Weapon.CLASS_DAGGER); break;
		case 10: ((Weapon)I).setWeaponClassification(Weapon.CLASS_STAFF); break;
		case 11: ((Weapon)I).setWeaponClassification(Weapon.CLASS_HAMMER); break;
		}
		if(val2>=1)
			((Weapon)I).baseEnvStats().setDamage(val2*val3);
		else
			((Weapon)I).baseEnvStats().setDamage(val3);
		if((str4.trim().length()>0)&&((Character.isLetter(str4.trim().charAt(0)))||(str4.trim().startsWith("'"))))
		{
			str4=str4.toUpperCase().trim();
			if(str4.startsWith("'"))
			   str4=str4.substring(1);
			if(str4.startsWith("POUND")) val4=7;
			else
			if(str4.startsWith("CRUSH")) val4=7;
			else
			if(str4.startsWith("SMASH")) val4=7;
			else
			if(str4.startsWith("FLAMI")) val4=57;
			else
			if(str4.startsWith("SCORC")) val4=57;
			else
			if(str4.startsWith("SEARI")) val4=57;
			else
			if(str4.startsWith("GOUT")) val4=57;
			else
			if(str4.startsWith("SCRATCH")) val4=22;
			else
			if(str4.startsWith("CLAW")) val4=22;
			else
			if(str4.startsWith("BITE")) val4=22;
			else
			if(str4.startsWith("PECK")) val4=22;
			else
			if(str4.startsWith("STING")) val4=22;
			else
			if(str4.startsWith("BEAT")) val4=22;
			else
			if(str4.startsWith("SLAP")) val4=22;
			else
			if(str4.startsWith("PUNC")) val4=22;
			else
			if(str4.startsWith("WHALL")) val4=22;
			else
			if(str4.startsWith("STAB")) val4=2;
			else
			if(str4.startsWith("PIERCE")) val4=2;
			else
			if(str4.startsWith("CHOP")) val4=25;
			else
			if(str4.startsWith("CLEA")) val4=25;
			else
			if(str4.startsWith("SLIC")) val4=25;
			else
			if(str4.startsWith("SLAS")) val4=25;
			else
			if(str4.startsWith("WHIP")) val4=25;
		}
		switch(val4)
		{
		case 7:
		case 8:
		case 27:
				((Weapon)I).setWeaponType(Weapon.TYPE_BASHING); break;
		case 29:
		case 55:
		case 56:
		case 57:
				((Weapon)I).setWeaponType(Weapon.TYPE_BURNING); break;
		case 22:
		case 5:
		case 10:
		case 23:
		case 26:
		case 32:
		case 13:
		case 16:
		case 17:
		case 24:
				((Weapon)I).setWeaponType(Weapon.TYPE_NATURAL); break;
		case 2:
		case 11:
				((Weapon)I).setWeaponType(Weapon.TYPE_PIERCING); break;
		case 25:
		case 21:
		case 4:
		case 3:
		case 1:
				((Weapon)I).setWeaponType(Weapon.TYPE_SLASHING); break;

		default: ((Weapon)I).setWeaponType(Weapon.TYPE_BURSTING); break;
		}
	}

	public static int getDRoll(String str)
	{
		int i=str.indexOf("d");
		if(i<0) return 11;
		int roll=Util.s_int(str.substring(0,i).trim());
		str=str.substring(i+1).trim();

		i=str.indexOf("+");
		int dice=0;
		int plus=0;
		if(i<0)
		{
			i=str.indexOf("-");
			if(i<0)
				dice=Util.s_int(str.trim());
			else
			{
				dice=Util.s_int(str.substring(0,i).trim());
				plus=Util.s_int(str.substring(i));
			}
		}
		else
		{
			dice=Util.s_int(str.substring(0,i).trim());
			plus=Util.s_int(str.substring(i+1));
		}
		return (roll*dice)+plus;
	}
	
	private static MOB getMOB(String OfThisID,
					  Room putInRoom,
					  MOB mob,
					  Vector mobData,
					  Vector mobProgData,
					  Vector specialData,
					  Vector shopData,
					  Hashtable doneMOBS,
					  String areaFileName)
	{
		if(OfThisID.startsWith("#"))
		{
			if(doneMOBS.containsKey(OfThisID.substring(1)))
			{
				MOB M=(MOB)((MOB)doneMOBS.get(OfThisID.substring(1))).copyOf();
				M.setStartRoom(putInRoom);
				M.setLocation(putInRoom);
				return M;
			}
		}
		else
		{
			if(doneMOBS.containsKey(OfThisID))
			{
				MOB M=(MOB)((MOB)doneMOBS.get(OfThisID)).copyOf();
				M.setStartRoom(putInRoom);
				M.setLocation(putInRoom);
				return M;
			}
		}


		for(int m=0;m<mobData.size();m++)
		{
			Vector objV=null;
			if(mobData.elementAt(m) instanceof Vector)
				objV=(Vector)mobData.elementAt(m);
			else
			if(mobData.elementAt(m) instanceof String)
			{
				String s=(String)mobData.elementAt(m);
				if((!s.toUpperCase().trim().startsWith("#MOB"))&&(s.length()>0))
					returnAnError(mob,"Eating mob immaterial line: "+mobData.elementAt(m));
				continue;
			}
			else
				continue;

			String mobID=eatNextLine(objV);


			if(!mobID.equals(OfThisID))
				continue;

			String simpleName=Util.safetyFilter(eatLineSquiggle(objV));
			String mobName=Util.safetyFilter(eatLineSquiggle(objV));
			String mobDisplay=Util.safetyFilter(eatLineSquiggle(objV));
			String mobDescription=Util.safetyFilter(eatLineSquiggle(objV));
			Race R=null;
			boolean circleFormat=false;
			if(nextLine(objV).endsWith("~"))
			{
				String raceName=eatLineSquiggle(objV);
				R=CMClass.getRace(raceName);
				circleFormat=true;
			}
			if(R==null)
				R=CMClass.getRace("StdRace");

			String codeStr1=eatNextLine(objV);
			String codeStr2=eatNextLine(objV);
			String codeStr3=eatNextLine(objV);
			String codeStr4=eatNextLine(objV);
			String codeStr5="";
			if(circleFormat)
			{
				codeStr3=codeStr4;
				codeStr4=eatNextLine(objV);
				codeStr5=eatNextLine(objV);
			}


			if((!mobID.startsWith("#"))
			||(mobName.length()==0)
			||(Util.numBits(codeStr1)<3)
			||(Util.numBits(codeStr1)>4)
			||(Util.numBits(codeStr2)<2)
			||(Util.numBits(codeStr3)<2))
			{
				returnAnError(mob,"Malformed mob! Aborting this mob "+mobID+", display="+mobDisplay+", simple="+simpleName+", name="+mobName+", codeStr1="+codeStr1+", codeStr2="+codeStr2+", codeStr3="+codeStr3+"!");
				continue;
			}
			int actFlag=getBitMask(codeStr1,0);
			int affFlag=getBitMask(codeStr1,1);
			int aliFlag=Util.s_int(Util.getBit(codeStr1,2));
			MOB M=CMClass.getMOB("GenMob");
			String checkName=mobName.trim().toUpperCase();
			if(Util.isSet(actFlag,14)
			||(checkName.indexOf("GHOUL")>=0)
			||(checkName.indexOf("GHAST")>=0)
			||(checkName.indexOf("SKELETON")>=0)
			||(checkName.indexOf("ZOMBIE")>=0)
			||(checkName.indexOf("VAMPIRE")>=0)
			||(checkName.indexOf("LICH")>=0)
			||(checkName.indexOf("MUMMY")>=0)
			||(checkName.indexOf("GHOST")>=0)
			||(checkName.indexOf("GEIST")>=0))
				M=CMClass.getMOB("GenUndead");
			else
			if(simpleName.toUpperCase().indexOf("HORSE")>=0)
				M=CMClass.getMOB("GenRideable");
			for(int i=0;i<shopData.size();i++)
			{
				String s=((String)shopData.elementAt(i)).trim();
				if(("#"+s).startsWith(OfThisID+" ")||("#"+s).startsWith(OfThisID+"\t")||("#"+s).startsWith(OfThisID+"	"))
				{
					M=CMClass.getMOB("GenShopkeeper");
					int i1=Util.s_int(Util.getBit(s,1));
					int i2=Util.s_int(Util.getBit(s,2));
					int i3=Util.s_int(Util.getBit(s,3));
					//int i4=Util.s_int(Util.getBit(s,4));
					int whatIsell=ShopKeeper.DEAL_INVENTORYONLY;
					if((i1>4)&&(i1<8)&&(i2>4)&&(i2<8)&&(i3>4)&&(i3<8))
						whatIsell=ShopKeeper.DEAL_WEAPONS;
					else
					if((((i1>1)&&(i1<5))||(i1==10)||(i1==26))
					&&(((i2>1)&&(i2<5))||(i2==10)||(i2==26))
					&&(((i3>1)&&(i3<5))||(i3==10)||(i3==26)))
						whatIsell=ShopKeeper.DEAL_MAGIC;
					else
					if(((i1==9)||(i1==0))&&((i2==9)||(i2==0))&&((i3==9)||(i3==0)))
						whatIsell=ShopKeeper.DEAL_ARMOR;
					else
					if(mobName.toUpperCase().indexOf("LEATHER")>=0)
						whatIsell=ShopKeeper.DEAL_LEATHER;
					else
					if((mobName.toUpperCase().indexOf("PET ")>=0)||(mobName.toUpperCase().indexOf("PETS ")>=0))
						whatIsell=ShopKeeper.DEAL_PETS;
					((ShopKeeper)M).setWhatIsSold(whatIsell);
					break;
				}
			}
			M.setName(mobName);
			M.setDisplayText(mobDisplay);
			if(!mobDescription.trim().equalsIgnoreCase("OLDSTYLE"))
				M.setDescription(mobDescription);
			aliFlag=(int)Math.round(Util.div(aliFlag,2));
			M.setAlignment(500+aliFlag);
			M.setStartRoom(putInRoom);
			M.setLocation(putInRoom);
			M.baseCharStats().setMyRace(R);

			Behavior behavior=CMClass.getBehavior("Mobile");
			if(Util.isSet(actFlag,5))
				behavior=CMClass.getBehavior("MobileAggressive");
			//if(!Util.isSet(actFlag,6))
			//	behavior.setParms("WANDER");
			if(!Util.isSet(actFlag,1))
				M.addBehavior(behavior);
			if(Util.isSet(actFlag,2))
				M.addBehavior(CMClass.getBehavior("Scavenger"));
			if(Util.isSet(actFlag,4))
				M.addNonUninvokableAffect(CMClass.getAbility("Prop_Invisibility"));
			if(Util.isSet(actFlag,5)&&Util.isSet(actFlag,1))
				M.addBehavior(CMClass.getBehavior("Aggressive"));
			M.setWimpHitPoint(0);
			if(Util.isSet(actFlag,7)) // this needs to be adjusted further down!
				M.setWimpHitPoint(2);
			if(Util.isSet(actFlag,8))
				M.addNonUninvokableAffect(CMClass.getAbility("Prop_SafePet"));

			if(Util.isSet(actFlag,9))
				M.addNonUninvokableAffect(CMClass.getAbility("Prop_StatTrainer"));
			if(Util.isSet(actFlag,10))
				M.addBehavior(CMClass.getBehavior("MOBTeacher"));

			if(Util.isSet(actFlag,12))
				M.addBehavior(CMClass.getBehavior("Mageness"));
			if(Util.isSet(actFlag,13))
				M.addBehavior(CMClass.getBehavior("Mageness"));
			if(Util.isSet(actFlag,17))
				M.addBehavior(CMClass.getBehavior("Mageness"));
			if(Util.isSet(actFlag,16))
				M.addBehavior(CMClass.getBehavior("Clericness"));
			if(Util.isSet(actFlag,18))
				M.addBehavior(CMClass.getBehavior("Thiefness"));
			if(Util.isSet(actFlag,11))
				M.addBehavior(CMClass.getBehavior("Fighterness"));
			if(Util.isSet(actFlag,19))
				M.addBehavior(CMClass.getBehavior("Fighterness"));
			if(Util.isSet(actFlag,26))
				M.addBehavior(CMClass.getBehavior("Healer"));
			if(Util.isSet(actFlag,27))
				M.addBehavior(CMClass.getBehavior("MOBTeacher"));

			if(Util.isSet(affFlag,0))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_NOT_SEE);
			if(Util.isSet(affFlag,1))
				M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|EnvStats.IS_INVISIBLE);
			if(Util.isSet(affFlag,2))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_EVIL);
			if(Util.isSet(affFlag,3))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_INVISIBLE);
			if(Util.isSet(affFlag,4))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_BONUS);
			if(Util.isSet(affFlag,5))
			{
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_HIDDEN);
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_SNEAKERS);
			}
			if(Util.isSet(affFlag,6))
			{
				if(M.getAlignment()<350)
				   M.addNonUninvokableAffect(CMClass.getAbility("Prayer_UnholyWord"));
				else
				   M.addNonUninvokableAffect(CMClass.getAbility("Prayer_HolyWord"));
				M.addNonUninvokableAffect(CMClass.getAbility("Prayer_Sanctuary"));
			}
			else
			if(Util.isSet(affFlag,7))
				M.addNonUninvokableAffect(CMClass.getAbility("Prayer_Sanctuary"));

			if(Util.isSet(affFlag,8))
				M.addNonUninvokableAffect(CMClass.getAbility("Spell_FaerieFire"));
			if(Util.isSet(affFlag,9))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_INFRARED);
			if(Util.isSet(affFlag,10))
				M.addNonUninvokableAffect(CMClass.getAbility("Prayer_Curse"));
			if(Util.isSet(affFlag,11))
			{
				for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
				{
					Ability A=(Ability)a.nextElement();
					if(A.ID().startsWith("Specialization"))
						M.addNonUninvokableAffect((Ability)A.newInstance());
				}
			}

			//if(Util.isSet(affFlag,12)) really dumb
			//  M.addNonUninvokableAffect(new Poison());

			if(Util.isSet(affFlag,13))
				M.addNonUninvokableAffect(CMClass.getAbility("Prayer_ProtEvil"));

			if(Util.isSet(affFlag,14))
				M.addNonUninvokableAffect(CMClass.getAbility("Prayer_ProtGood"));

			if(Util.isSet(affFlag,15))
			{
				Ability A=(Ability)CMClass.getAbility("Thief_Sneak").copyOf();
				A.setProfficiency(100);
				M.addAbility(A);
			}

			if(Util.isSet(affFlag,16))
				M.addNonUninvokableAffect(CMClass.getAbility("Prop_Hidden"));

			if(Util.isSet(affFlag,17))
				M.addNonUninvokableAffect(CMClass.getAbility("Spell_Sleep"));

			if(Util.isSet(affFlag,18))
				M.addNonUninvokableAffect(CMClass.getAbility("Spell_Charm"));

			if(Util.isSet(affFlag,20))
				M.addNonUninvokableAffect(CMClass.getAbility("Spell_PassDoor"));

			if(Util.isSet(affFlag,21))
				M.addNonUninvokableAffect(CMClass.getAbility("Spell_Haste"));

			//if(Util.isSet(affFlag,22)) no effect anyway
			//	M.addNonUninvokableAffect(new Prayer_Calm());

			if(Util.isSet(affFlag,23))
				M.addNonUninvokableAffect(CMClass.getAbility("Prayer_Plague"));

			if(Util.isSet(affFlag,24))
				M.addNonUninvokableAffect(CMClass.getAbility("Prop_SafePet"));

			if(Util.isSet(affFlag,25))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_DARK);

			if(Util.isSet(affFlag,26))
				M.addNonUninvokableAffect(CMClass.getAbility("Fighter_Berzerk"));

			if(Util.isSet(affFlag,27))
				M.addNonUninvokableAffect(CMClass.getAbility("Regeneration"));

			if(Util.isSet(affFlag,28))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|EnvStats.CAN_SEE_GOOD);

			if(Util.isSet(affFlag,29))
				M.addNonUninvokableAffect(CMClass.getAbility("Spell_Slow"));

			// start ROM type
			int positionCode=8;
			int sexCode=1;
			if(Util.numBits(codeStr2)>=4)
			{
				M.baseEnvStats().setLevel(Util.s_int(Util.getBit(codeStr2,0)));
				if(M.baseEnvStats().level()==0)
					M.baseEnvStats().setLevel(1);
				int baseHP=11;
				if(circleFormat)
					baseHP=getDRoll(Util.getBit(codeStr2,2));
				else
					baseHP=getDRoll(Util.getBit(codeStr2,3));
				baseHP=baseHP-10;
				baseHP=baseHP-((int)Math.round(Util.mul(M.baseEnvStats().level()*M.baseEnvStats().level(),0.85)));
				baseHP=baseHP/M.baseEnvStats().level();
				M.baseEnvStats().setAbility(baseHP);
				
				if(circleFormat)
				{
					if(Util.getBit(codeStr4,2).toUpperCase().equals("MALE"))
						sexCode=1;
					else
					if(Util.getBit(codeStr4,2).toUpperCase().equals("FEMALE"))
						sexCode=2;
					else
					if(Util.getBit(codeStr4,2).toUpperCase().equals("EITHER"))
						sexCode=(Dice.rollPercentage()>50)?1:2;
					else
						sexCode=3;

					if(Util.getBit(codeStr4,0).trim().startsWith("STAND"))
						positionCode=8;
					else
					if(Util.getBit(codeStr4,0).trim().startsWith("SIT"))
						positionCode=5;
					else
					if(Util.getBit(codeStr4,0).trim().startsWith("SLEEP"))
						positionCode=1;

				}
				else
				{
					positionCode=Util.s_int(Util.getBit(codeStr4,0));
					sexCode=Util.s_int(Util.getBit(codeStr4,2));
				}
				if(Dice.rollPercentage()>75)
					M.addBehavior(CMClass.getBehavior("MudChat"));
			}
			else
			{
				M.baseEnvStats().setAbility(11);
				int baseLevel=Util.s_int(Util.getBit(codeStr2,0));
				while(baseLevel>25)
					baseLevel=(int)Math.round(Util.div(baseLevel,2.0));
			}

			if(M.baseEnvStats().level()==0)
				M.baseEnvStats().setLevel(1);
			if(M.getWimpHitPoint()==2)
				M.setWimpHitPoint(((int)Math.round(Util.div(M.baseEnvStats().level()*(11+M.baseEnvStats().ability()),8.0)))+1);

			M.baseEnvStats().setArmor(CMClass.getCharClass("StdCharClass").getLevelArmor(M));
			M.baseEnvStats().setAttackAdjustment(CMClass.getCharClass("StdCharClass").getLevelAttack(M));
			M.baseEnvStats().setDamage(CMClass.getCharClass("StdCharClass").getLevelDamage(M));
			if(circleFormat)
				M.setMoney(Util.s_int(Util.getBit(codeStr4,3)));
			else
				M.setMoney((2*M.baseEnvStats().level())+10);
			M.baseEnvStats().setWeight(50);

			switch(positionCode)
			{
			case 1:
			case 2:
			case 3:
			case 4:
				M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|EnvStats.IS_SLEEPING);
				break;
			case 5:
				M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|EnvStats.IS_SITTING);
				break;
			case 6:
				M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|EnvStats.IS_SITTING);
				break;
			}

			M.baseCharStats().setStat(CharStats.GENDER,(int)'M');
			switch(sexCode)
			{
			case 2: M.baseCharStats().setStat(CharStats.GENDER,(int)'F'); break;
			case 3: M.baseCharStats().setStat(CharStats.GENDER,(int)'N'); break;
			}

			if(circleFormat)
			{
				int off=getBitMask(codeStr3,0);
				int imm=getBitMask(codeStr3,1);
				int res=getBitMask(codeStr3,2);
				int size=Util.s_int(Util.getBit(codeStr5,2));
				switch(size)
				{
				case 0: M.baseEnvStats().setWeight(1); break;
				case 1: M.baseEnvStats().setWeight(15); break;
				case 2: M.baseEnvStats().setWeight(150); break;
				case 3: M.baseEnvStats().setWeight(350); break;
				case 4: M.baseEnvStats().setWeight(850); break;
				case 5: M.baseEnvStats().setWeight(2000); break;
				}
				// ignore the above, coffeemud does it better!
				M.baseCharStats().getMyRace().startRacing(M,false);
				int numAbiles=M.numAbilities();
				//if(Util.isSet(off,0)) // no area killers in coffeemud
				//if(Util.isSet(off,1)) // no circling in coffeemud

				if(Util.isSet(off,2)) // bash them off their feet?
					M.addAbility(CMClass.getAbility("Skill_Trip"));
				if(Util.isSet(off,3))
					M.addAbility(CMClass.getAbility("Fighter_Berzerk"));
				if(Util.isSet(off,4))
					M.addAbility(CMClass.getAbility("Skill_Disarm"));
				if(Util.isSet(off,5))
					M.addAbility(CMClass.getAbility("Skill_Dodge"));
				//if(Util.isSet(off,6)) is missing
				if(Util.isSet(off,7))
					M.baseEnvStats().setSpeed(M.baseEnvStats().speed()+1);
				if(Util.isSet(off,8))
					M.addAbility(CMClass.getAbility("Fighter_Kick"));
				if(Util.isSet(off,9))
					M.addAbility(CMClass.getAbility("Skill_Dirt"));
				if(Util.isSet(off,10))
					M.addAbility(CMClass.getAbility("Skill_Parry"));
				//if(Util.isSet(off,11)) rescue is irrelevant
				//if(Util.isSet(off,12)) is missing
				if(Util.isSet(off,13))
					M.addAbility(CMClass.getAbility("Skill_Trip"));
				if(Util.isSet(off,14))
					M.addAbility(CMClass.getAbility("Fighter_Whomp"));
				if(Util.isSet(off,15))
					M.addBehavior(CMClass.getBehavior("MOBHelper"));
				if(Util.isSet(off,16))
					M.addBehavior(CMClass.getBehavior("AlignHelper"));
				if(Util.isSet(off,17))
					M.addBehavior(CMClass.getBehavior("RaceHelper"));
				if(Util.isSet(off,18))
					M.addBehavior(CMClass.getBehavior("PlayerHelper"));
				if(Util.isSet(off,19))
				{
					Behavior guardian=CMClass.getBehavior("GoodGuardian");
					for(int b=M.numBehaviors()-1;b>=0;b--)
					{
						Behavior B=M.fetchBehavior(b);
						if((B!=null)&&(Util.bset(B.flags(),Behavior.FLAG_MOBILITY)))
						{
							if(guardian.ID().equals("GoodGuardian"))
								guardian=CMClass.getBehavior("MobileGoodGuardian");
							if(B.getParms().length()>0)
								guardian.setParms(B.getParms());
							M.delBehavior(B);
						}
					}
					M.addBehavior(guardian);
					M.addBehavior(CMClass.getBehavior("AntiVagrant"));
				}
				if(Util.isSet(off,20))
					M.addBehavior(CMClass.getBehavior("BrotherHelper"));
				//if(Util.isSet(off,21)) is missing
				if(Util.isSet(off,22))
					M.addAbility(CMClass.getAbility("Fighter_Sweep"));

				if(M.numAbilities()>numAbiles)
					M.addBehavior(CMClass.getBehavior("CombatAbilities"));

				Ability resist=CMClass.getAbility("Prop_Resistance");
				if((Util.isSet(res,0))||(Util.isSet(imm,0)))
					resist.setMiscText(resist.text()+" teleport");
				if((Util.isSet(res,1))||(Util.isSet(imm,1)))
					resist.setMiscText(resist.text()+" mind");
				if((Util.isSet(res,2))||(Util.isSet(imm,2)))
					resist.setMiscText(resist.text()+" magic");
				if((Util.isSet(res,3))||(Util.isSet(imm,3)))
					resist.setMiscText(resist.text()+" weapons");
				if((Util.isSet(res,4))||(Util.isSet(imm,4)))
					resist.setMiscText(resist.text()+" blunt");
				if((Util.isSet(res,5))||(Util.isSet(imm,5)))
					resist.setMiscText(resist.text()+" pierce");
				if((Util.isSet(res,6))||(Util.isSet(imm,6)))
					resist.setMiscText(resist.text()+" slash");
				if((Util.isSet(res,7))||(Util.isSet(imm,7)))
					resist.setMiscText(resist.text()+" fire");
				if((Util.isSet(res,8))||(Util.isSet(imm,8)))
					resist.setMiscText(resist.text()+" cold");
				if((Util.isSet(res,9))||(Util.isSet(imm,9)))
					resist.setMiscText(resist.text()+" elec");
				if((Util.isSet(res,10))||(Util.isSet(imm,10)))
					resist.setMiscText(resist.text()+" acid");
				if((Util.isSet(res,11))||(Util.isSet(imm,11)))
					resist.setMiscText(resist.text()+" poison");
				if((Util.isSet(res,12))||(Util.isSet(imm,12)))
					resist.setMiscText(resist.text()+" evil");
				if((Util.isSet(res,13))||(Util.isSet(imm,13)))
					resist.setMiscText(resist.text()+" holy");
				if(Util.isSet(res,14))
					M.addNonUninvokableAffect(CMClass.getAbility("Spell_ResistMagicMissiles"));
				if((Util.isSet(res,15))||(Util.isSet(imm,15)))
					resist.setMiscText(resist.text()+" mind");
				if((Util.isSet(res,16))||(Util.isSet(imm,16)))
					resist.setMiscText(resist.text()+" disease");
				if((Util.isSet(res,17))||(Util.isSet(imm,17)))
					resist.setMiscText(resist.text()+" water gas");
				//if(Util.isSet(res,18)) no light resistance
				//if(Util.isSet(res,18)) no sound resistance
				if(resist.text().length()>0)
				{
					resist.setMiscText(resist.text()+" "+(10+M.baseEnvStats().level())+"%");
					M.addNonUninvokableAffect(resist);
				}
			}

			String scriptStuff="";
			while(objV.size()>0)
			{
				String s=nextLine(objV);
				if(s.startsWith(">"))
				{
					s=eatLineSquiggle(objV);
					if(!s.substring(1).trim().toUpperCase().startsWith("IN_FILE_PROG"))
					{
						scriptStuff+=s.substring(1).trim()+";";
						s=nextLine(objV);
						while(s.indexOf("~")<0)
						{
							scriptStuff+=s.trim()+";";
							eatLine(objV);
							s=nextLine(objV);
						}
						s=eatLineSquiggle(objV).trim();
						scriptStuff+=s+"~";
					}
				}
				else
				if(s.startsWith("X "))
				{
					String codeLine=eatLineSquiggle(objV);
					Behavior B=M.fetchBehavior("Sounder");
					if(B==null)
					{
						B=CMClass.getBehavior("Sounder");
						if(B!=null) M.addBehavior(B);
					}
					if(B.getParms().length()==0)
						B.setParms(codeLine.substring(1).trim());
					else
						B.setParms(B.getParms()+";"+codeLine.substring(1).trim());
				}
				else
					eatNextLine(objV);
			}
			for(int mp=0;mp<mobProgData.size();mp++)
			{
				String s=(String)mobProgData.elementAt(mp);
				if(s.startsWith("M "))
				{
					String MOBID=Util.getBit(s,1);
					if(!("#"+MOBID).equals(OfThisID))
						continue;

					String mobprg=Util.getBit(s,2).toUpperCase().trim();
					if(mobprg.equals("JANITOR.PRG"))
						M.addBehavior(CMClass.getBehavior("Scavenger"));
					else
					if(mobprg.equals("VAGABOND.PRG"))
						M.addBehavior(CMClass.getBehavior("Vagrant"));
					else
					if(mobprg.equals("DRUNK.PRG"))
					{
						if(M.fetchAffect("Inebriation")==null)
							M.addNonUninvokableAffect(CMClass.getAbility("Inebriation"));
					}
					else
					if(mobprg.equals("MID_CIT.PRG"))
					{
						//
					}
					else
					if(mobprg.equals("BEGGAR.PRG"))
						M.addBehavior(CMClass.getBehavior("Beggar"));
					else
					if(mobprg.equals("GATEGRD.PRG"))
						M.addBehavior(CMClass.getBehavior("GateGuard"));
					else
					if(mobprg.equals("GATEGRD2.PRG"))
						M.addBehavior(CMClass.getBehavior("GateGuard"));
					else
					if(mobprg.equals("CRIER.PRG"))
					{
					}
					else
					{
						try{
							File F2=new File(areaFileName);
							if((F2.exists())&&(!F2.isDirectory()))
							{
								int x=F2.getAbsolutePath().lastIndexOf(File.separatorChar);
								String path=F2.getAbsolutePath().substring(0,x)+File.separatorChar+mobprg;
								StringBuffer buf=Resources.getFile(path);
								if((buf==null)||(buf.length()==0))
									returnAnError(mob,"Unknown MobPrg: "+mobprg);
								else
								{
									Vector V=Resources.getFileLineVector(buf);
									while(V.size()>0)
									{
										s=nextLine(V);
										if(s.startsWith(">"))
										{
											s=eatLineSquiggle(V).substring(1).trim();
											scriptStuff+=s+";";
											s=nextLine(V);
											while(s.indexOf("~")<0)
											{
												scriptStuff+=s+";";
												eatLine(V);
												s=nextLine(V);
											}
											s=eatLineSquiggle(V).trim();
											scriptStuff+=s+"~";
										}
										else
											eatLine(V);
									}
								}
							}
						}catch(Exception e){
							returnAnError(mob,"Unknown MobPrg: "+mobprg);
						}
					}
				}
				else
				if((s.startsWith("#M"))||(s.startsWith("S")))
				{
				}
				else
				if(s.trim().length()>0)
					returnAnError(mob,"MobPrg line: "+s);
			}
			if(scriptStuff.length()>0)
			{
				Behavior S=CMClass.getBehavior("Scriptable");
				S.setParms(scriptStuff);
				M.addBehavior(S);
			}

			for(int mp=0;mp<specialData.size();mp++)
			{
				String s=(String)specialData.elementAt(mp);
				if(s.startsWith("M "))
				{
					String MOBID=Util.getBit(s,1);
					if(!("#"+MOBID).equals(OfThisID))
						continue;

					String special=Util.getBit(s,2).toUpperCase().trim();
					if(special.equals("SPEC_CAST_MAGE"))
						M.addBehavior(CMClass.getBehavior("Mageness"));
					else
					if(special.equals("SPEC_THIEF"))
						M.addBehavior(CMClass.getBehavior("Thiefness"));
					else
					if(special.equals("SPEC_EXECUTIONER"))
						M.addBehavior(CMClass.getBehavior("GoodExecutioner"));
					else
					if(special.equals("SPEC_CAST_ADEPT"))
						M.addBehavior(CMClass.getBehavior("Healer"));
					else
					if(special.equals("SPEC_CAST_CLERIC"))
						M.addBehavior(CMClass.getBehavior("Clericness"));
					else
					if(special.equals("SPEC_NASTY"))
						M.addBehavior(CMClass.getBehavior("FightFlee"));
					else
					if(special.equals("SPEC_CAST_UNDEAD"))
					{
						M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|EnvStats.IS_GOLEM);
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Undead_ColdTouch"));
						M.addAbility(CMClass.getAbility("Undead_LifeDrain"));
						M.baseCharStats().setMyRace(CMClass.getRace("Undead"));
						M.baseCharStats().getMyRace().startRacing(M,false);
					}
					else
					if(special.equals("SPEC_GUARD"))
						M.addBehavior(CMClass.getBehavior("GoodGuardian"));
					else
					if(special.equals("SPEC_FIDO"))
						M.addBehavior(CMClass.getBehavior("CorpseEater"));
					else
					if(special.equals("SPEC_MAYOR"))
						M.addBehavior(CMClass.getBehavior("MudChat"));
					else
					if(special.equals("SPEC_JANITOR"))
						M.addBehavior(CMClass.getBehavior("Scavenger"));
					else
					if(special.equals("SPEC_BREATH_ANY"))
						M.addAbility(CMClass.getAbility("Dragonbreath"));
					else
					if(special.equals("SPEC_BREATH_ACID"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Acidbreath"));
					}
					else
					if(special.equals("SPEC_CAST_JUDGE"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Skill_Explosive"));
					}
					else
					if(special.equals("SPEC_BREATH_FIRE"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Firebreath"));
					}
					else
					if(special.equals("SPEC_BREATH_FROST"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Frostbreath"));
					}
					else
					if(special.equals("SPEC_BREATH_GAS"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Gasbreath"));
					}
					else
					if(special.equals("SPEC_BREATH_LIGHTNING"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Lighteningbreath"));
					}
					else
					if(special.equals("SPEC_POISON"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Poison"));
					}
					else
					if(special.equals("SPEC_OGRE_MEMBER"))
					{
						Behavior B=CMClass.getBehavior("ROMGangMember");
						B.setParms("Ogre");
						M.addBehavior(B);
					}
					else
					if(special.equals("SPEC_TROLL_MEMBER"))
					{
						Behavior B=CMClass.getBehavior("ROMGangMember");
						B.setParms("Troll");
						M.addBehavior(B);
					}
					else
					if(special.equals("SPEC_PATROLMAN"))
						M.addBehavior(CMClass.getBehavior("ROMPatrolman"));
					else
						returnAnError(mob,"Unknown mob special: "+special);
				}
				else
				if((s.startsWith("#SPE"))||(s.startsWith("S"))||(s.startsWith("*")||(s.startsWith("#$"))))
				{
				}
				else
				if(s.trim().length()>0)
					returnAnError(mob,"Unknown mob special line: "+s);
			}
			for(int a=0;a<M.numAbilities();a++)
			{
				Ability A=M.fetchAbility(a);
				if(A!=null)
					A.autoInvocation(M);
			}
			long rejuv=Host.TICKS_PER_MIN+Host.TICKS_PER_MIN+(Host.TICKS_PER_MIN*M.baseEnvStats().level()/2);
			if(rejuv>(30*Host.TICKS_PER_MIN)) rejuv=(30*Host.TICKS_PER_MIN);
			M.baseEnvStats().setRejuv((int)rejuv);
			if(M.displayText().toUpperCase().indexOf("MONEY CHANGER")>=0)
				M.addBehavior(CMClass.getBehavior("MoneyChanger"));
			M.recoverCharStats();
			M.recoverEnvStats();
			M.recoverMaxState();
			M.resetToMaxState();
			M.text();
			if(OfThisID.startsWith("#"))
				doneMOBS.put(OfThisID.substring(1),M.copyOf());
			else
				doneMOBS.put(OfThisID,M.copyOf());
			return M;
		}
		return null;
	}

	private static Item getItem(String OfThisID,
						 MOB mob,
						 String areaName,
						 Vector objectData,
						 Vector objProgData,
						 Hashtable doneItems)
	{
		if(OfThisID.startsWith("#"))
		{
			if(doneItems.containsKey(OfThisID.substring(1)))
				return (Item)((Item)doneItems.get(OfThisID.substring(1))).copyOf();
		}
		else
		{
			if(doneItems.containsKey(OfThisID))
				return (Item)((Item)doneItems.get(OfThisID)).copyOf();
		}
		for(int o=0;o<objectData.size();o++)
		{
			Vector objV=null;
			if(objectData.elementAt(o) instanceof Vector)
				objV=(Vector)objectData.elementAt(o);
			else
			if(objectData.elementAt(o) instanceof String)
			{
				String s=(String)objectData.elementAt(o);
				if((!s.toUpperCase().trim().startsWith("#OBJ"))&&(s.length()>0))
					returnAnError(mob,"Eating immaterial line: "+objectData.elementAt(o)+", area="+areaName);
				continue;
			}
			else
				continue;
			String objectID=eatNextLine(objV);

			if(!objectID.equals(OfThisID))
				continue;

			String simpleName=Util.safetyFilter(eatLineSquiggle(objV));
			String objectName=Util.safetyFilter(eatLineSquiggle(objV));
			String objectDisplay=Util.safetyFilter(eatLineSquiggle(objV));
			String objectDescription="";
			if((nextLine(objV).indexOf("~")>=0)||(!Character.isDigit(nextLine(objV).charAt(0))))
				objectDescription=Util.safetyFilter(eatLineSquiggle(objV));
			String codeStr1=eatNextLine(objV);
			String codeStr2=eatNextLine(objV);
			String codeStr3=eatNextLine(objV);

			if((!objectID.startsWith("#"))
			||(objectName.length()==0)
			||(Util.numBits(codeStr1)<3)
			||(Util.numBits(codeStr1)>4)
			||(Util.numBits(codeStr2)<4)
			||(codeStr3.length()==0))
			{
				returnAnError(mob,"Malformed object! Aborting this object "+objectID+", display="+objectDisplay+", simple="+simpleName+", name="+objectName+", codeStr1="+codeStr1+", codeStr2="+codeStr2+", codeStr3="+codeStr3+", area="+areaName);
				continue;
			}
			boolean circleForm=false;
			String obj=Util.getBit(codeStr1,0);
			if((obj.trim().length()>1)&&(Character.isLetter(obj.charAt(0))))
				circleForm=true;
			int objType=0;
			if(circleForm)
			{
				if(obj.equalsIgnoreCase("light")) objType=1;
				else
				if(obj.equalsIgnoreCase("scroll")) objType=2;
				else
				if(obj.equalsIgnoreCase("wand")) objType=3;
				else
				if(obj.equalsIgnoreCase("staff")) objType=4;
				else
				if(obj.equalsIgnoreCase("weapon")) objType=5;
				else
				if(obj.equalsIgnoreCase("treasure")) objType=8;
				else
				if(obj.equalsIgnoreCase("armor")) objType=9;
				else
				if(obj.equalsIgnoreCase("potion")) objType=10;
				else
				if(obj.equalsIgnoreCase("clothing")) objType=11;
				else
				if(obj.equalsIgnoreCase("furniture")) objType=12;
				else
				if(obj.equalsIgnoreCase("trash")) objType=13;
				else
				if(obj.equalsIgnoreCase("container")) objType=15;
				else
				if(obj.equalsIgnoreCase("drink")) objType=17;
				else
				if(obj.equalsIgnoreCase("key")) objType=18;
				else
				if(obj.equalsIgnoreCase("food")) objType=19;
				else
				if(obj.equalsIgnoreCase("money")) objType=20;
				else
				if(obj.equalsIgnoreCase("boat")) objType=22;
				else
				if(obj.equalsIgnoreCase("fountain")) objType=25;
				else
				if(obj.equalsIgnoreCase("pill")) objType=26;
				else
				if(obj.equalsIgnoreCase("map")) objType=28;
				else
				if(obj.equalsIgnoreCase("pipe")) objType=32;
				else
				if(obj.toUpperCase().endsWith("CORPSE")) objType=99;
				else
				if(obj.equalsIgnoreCase("jukebox"))
					continue;// NO JUKE BOXES!
			}
			else
				objType=Util.s_int(obj);
			int extraFlag=getBitMask(codeStr1,1);
			int wearFlag=getBitMask(codeStr1,2);

			Ability adjuster=CMClass.getAbility("Prop_HaveAdjuster");
			String str1=Util.getBit(codeStr2,0);
			String str2=Util.getBit(codeStr2,1);
			String str3=Util.getBit(codeStr2,2);
			String str4=Util.getBit(codeStr2,3);
			int val1=getBitMask(codeStr2,0);
			int val2=getBitMask(codeStr2,1);
			int val3=getBitMask(codeStr2,2);
			int val4=getBitMask(codeStr2,3);
			Item I=null;
			switch(objType)
			{
			case 1: if(objectName.toUpperCase().indexOf("LANTERN")>=0)
						I=CMClass.getStdItem("GenLantern");
					else
						I=CMClass.getStdItem("GenLightSource");
					((Light)I).setDuration(val3*20);
					break;
			case 2: I=CMClass.getMiscMagic("GenScroll");
					I.baseEnvStats().setLevel(val1);
					I.setUsesRemaining(3);
					((Scroll)I).setScrollText(getSpell(str2,val2)+";"+getSpell(str3,val3)+";"+getSpell(str4,val4));
					break;
			case 3: I=CMClass.getMiscMagic("GenWand");
					I.baseEnvStats().setLevel(val1);
					I.setUsesRemaining(val2);
					((Wand)I).setSpell(CMClass.getAbility(getSpell(str4,val4)));
					break;
			case 4: I=CMClass.getWeapon("GenStaff");
					I.baseEnvStats().setLevel(val1);
					I.setUsesRemaining(val2);
					((Wand)I).setSpell(CMClass.getAbility(getSpell(str4,val4)));
					adjuster=CMClass.getAbility("Prop_WearAdjuster");
					break;
			case 5: I=CMClass.getWeapon("GenWeapon");
					doWeapon((Weapon)I,objectName,val1,str1,val2,val3,val4,str4);
					adjuster=CMClass.getAbility("Prop_WearAdjuster");
					break;
			case 6: I=CMClass.getWeapon("GenWeapon");
					doWeapon((Weapon)I,objectName,val1,str1,val2,val3,val4,str4);
					adjuster=CMClass.getAbility("Prop_WearAdjuster");
					break;
			case 7: I=CMClass.getWeapon("GenWeapon");
					doWeapon((Weapon)I,objectName,val1,str1,val2,val3,val4,str4);
					adjuster=CMClass.getAbility("Prop_WearAdjuster");
					break;
			case 8: I=CMClass.getStdItem("GenItem");
					break;
			case 9: if(objectName.toUpperCase().indexOf("SHIELD")>=0)
						I=(Item)CMClass.getArmor("GenShield");
					else
						I=(Item)CMClass.getArmor("GenArmor");
					I.baseEnvStats().setArmor((int)Math.round(Util.div((val1+val2+val3+val4+1),4.0)+1));
					adjuster=CMClass.getAbility("Prop_WearAdjuster");
					break;
			case 10: I=CMClass.getMiscMagic("GenPotion");
					I.baseEnvStats().setLevel(val1);
					((Potion)I).setSpellList(getSpell(str2,val2)+";"+getSpell(str3,val3)+";"+getSpell(str4,val4));
					 break;
			case 11: I=(Item)CMClass.getArmor("GenArmor");
					 I.baseEnvStats().setArmor(0);
					 adjuster=CMClass.getAbility("Prop_WearAdjuster");
					 break;
			case 12: I=(Item)CMClass.getStdItem("GenItem");
					 if(hasReadableContent(objectName))
						I=CMClass.getStdItem("GenReadable");
					 break;
			case 13: I=(Item)CMClass.getStdItem("GenItem");
					 if(hasReadableContent(objectName))
						I=CMClass.getStdItem("GenReadable");
					 break;
			case 14: I=CMClass.getStdItem("GenItem"); break;
			case 15: I=CMClass.getStdItem("GenContainer");
					 ((Container)I).setCapacity(val1);
					 boolean lid=false;
					 boolean open=true;
					 boolean lock=false;
					 boolean locked=false;
					 if((val2&1)==1)
						 lid=true;
					 if((val2&2)==2)
					 {
						 lock=true;
						 locked=true;
						 open=false;
						 lid=true;
						 I.baseEnvStats().setLevel(100);
					 }
					 if((val2&4)==4)
					 {
						 lid=true;
						 open=false;
					 }
					 if((val2&8)==8)
					 {
						 lock=true;
						 locked=true;
						 open=false;
						 lid=true;
					 }
					 ((Container)I).setLidsNLocks(lid,open,lock,locked);
					 if(((Container)I).hasALid()&&((Container)I).hasALock())
						 ((Container)I).setKeyName(areaName+"#"+val3);
					 break;
			case 16: I=CMClass.getStdItem("GenItem"); break;
			case 17: I=CMClass.getStdItem("GenWater");
					 str3=str3.toUpperCase().trim();
					 if(((val3>0)&&(val3<6))
					 ||(str3.indexOf("BEER")>=0)
					 ||(str3.indexOf("ALE")>=0)
					 ||(str3.indexOf("BREW")>=0)
					 ||(str3.indexOf("WINE")>=0))
					 {
						((Drink)I).setLiquidType(EnvResource.RESOURCE_LIQUOR);
						I.addAffect(CMClass.getAbility("Poison_Beer"));
						((Drink)I).setLiquidHeld(val1*10);
						((Drink)I).setLiquidRemaining(val2);
					 }
					 else
					 if(str3.indexOf("FIREBREATHER")>=0)
					 {
						((Drink)I).setLiquidType(EnvResource.RESOURCE_LIQUOR);
						I.addAffect(CMClass.getAbility("Poison_Firebreather"));
						((Drink)I).setLiquidHeld(val1*10);
						((Drink)I).setLiquidRemaining(val2);
					 }
					 else
					 if(str3.indexOf("LOCAL SPECIALTY")>=0)
					 {
						((Drink)I).setLiquidType(EnvResource.RESOURCE_LIQUOR);
						I.addAffect(CMClass.getAbility("Poison_Liquor"));
						((Drink)I).setLiquidHeld(val1*10);
						((Drink)I).setLiquidRemaining(val2);
					 }
					 else
					 if(str3.indexOf("WHISKEY")>=0)
					 {
						((Drink)I).setLiquidType(EnvResource.RESOURCE_LIQUOR);
						I.addAffect(CMClass.getAbility("Poison_Liquor"));
						((Drink)I).setLiquidHeld(val1*10);
						((Drink)I).setLiquidRemaining(val2);
					 }
					 else
					 if((val4>0)||(str3.indexOf("POISON")>=0))
					 {
						((Drink)I).setLiquidType(EnvResource.RESOURCE_POISON);
						I.addAffect(CMClass.getAbility("Poison"));
						((Drink)I).setLiquidHeld(val1*10);
						((Drink)I).setLiquidRemaining(val2);
					 }
					 else
					 {
						((Drink)I).setLiquidHeld(val1*30);
						((Drink)I).setLiquidRemaining(val2*10);
					 }
					 break;
			case 18: I=CMClass.getStdItem("GenKey");
					 ((Key)I).setKey(areaName+objectID);
					 break;
			case 19: I=CMClass.getStdItem("GenFood");
					 if(val4>0)
					 {
						 I=CMClass.getMiscMagic("GenPill");
						 ((Pill)I).setSpellList("Poison"+";");
					 }
					 ((Food)I).setNourishment(20*val1);
					 break;
			case 20: I=CMClass.getStdItem("StdCoins");
					 I.baseEnvStats().setAbility(val1);
					 break;
			case 21: I=CMClass.getStdItem("GenItem"); break;
			case 22: I=CMClass.getStdItem("GenBoat");
					 break;
			case 23: I=CMClass.getStdItem("GenCorpse"); break;
			case 24: I=CMClass.getStdItem("GenCorpse"); break;
			case 25: I=CMClass.getStdItem("GenWater");
					 I.setGettable(false);
					 ((Drink)I).setLiquidHeld(Integer.MAX_VALUE-5000);
					 ((Drink)I).setLiquidRemaining(((Drink)I).liquidHeld());
					 break;
			case 26: I=CMClass.getMiscMagic("GenPill");
					I.baseEnvStats().setLevel(val1);
					((Pill)I).setSpellList(getSpell(str2,val2)+";"+getSpell(str3,val3)+";"+getSpell(str4,val4));
					 break;
			case 27: I=CMClass.getStdItem("GenItem"); break;
			case 28: I=CMClass.getStdItem("GenReadable"); // don't use GemMaps any more...
					 break;
			case 29: I=CMClass.getStdItem("GenItem"); break;
			case 99: I=CMClass.getStdItem("GenCorpse"); break;
			case -1: I=CMClass.getStdItem("GenWallpaper"); break;
			default:
					I=CMClass.getStdItem("GenItem"); break;
			}

			if(!Util.isSet(wearFlag,0))
				I.setGettable(false);
			if(Util.isSet(wearFlag,1))
				I.setRawProperLocationBitmap(Item.ON_LEFT_FINGER|Item.ON_RIGHT_FINGER|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,2))
				I.setRawProperLocationBitmap(Item.ON_NECK|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,3))
				I.setRawProperLocationBitmap(Item.ON_TORSO|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,4))
				I.setRawProperLocationBitmap(Item.ON_HEAD|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,5))
				I.setRawProperLocationBitmap(Item.ON_LEGS|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,6))
				I.setRawProperLocationBitmap(Item.ON_FEET|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,7))
				I.setRawProperLocationBitmap(Item.ON_HANDS|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,8))
				I.setRawProperLocationBitmap(Item.ON_ARMS|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,9))
				I.setRawProperLocationBitmap(Item.HELD|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,10))
				I.setRawProperLocationBitmap(Item.ABOUT_BODY|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,11))
				I.setRawProperLocationBitmap(Item.ON_WAIST|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,12))
				I.setRawProperLocationBitmap(Item.ON_LEFT_WRIST|Item.ON_RIGHT_WRIST|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,13))
				I.setRawProperLocationBitmap(Item.WIELD|Item.HELD|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,14))
				I.setRawProperLocationBitmap(Item.HELD|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,15))
				I.setRawLogicalAnd(true);
			if(Util.isSet(wearFlag,17))
				I.setRawProperLocationBitmap(Item.ON_EARS|I.rawProperLocationBitmap());
			if(Util.isSet(wearFlag,18)) // ankles
				I.setRawProperLocationBitmap(Item.ON_FEET|I.rawProperLocationBitmap());

			// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			// the relation of this stuff is vital!  must follow properlocation setting
			// and the getttable setting ONLY!!!
			// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			if((adjuster.ID().equals("Prop_HaveAdjuster"))
			&&(I.rawProperLocationBitmap()>0)
			)
			{
				adjuster=CMClass.getAbility("Prop_WearAdjuster");
				if(I.ID().equals("GenItem"))
				{
					long wear=I.rawProperLocationBitmap();
					boolean bool=I.rawLogicalAnd();
					boolean gettable=I.isGettable();
					I=(Item)CMClass.getArmor("GenArmor");
					I.setRawProperLocationBitmap(wear);
					I.setRawLogicalAnd(bool);
					I.baseEnvStats().setArmor(0);
					I.setGettable(gettable);
				}
			}

			Ability resister=CMClass.getAbility("Prop_HaveResister");
			Ability caster=CMClass.getAbility("Prop_HaveSpellCast");
			if(adjuster.ID().equals("Prop_WearAdjuster"))
			{
				resister=CMClass.getAbility("Prop_WearResister");
				caster=CMClass.getAbility("Prop_WearSpellCast");
			}
			if(Util.numBits(codeStr3)>2)
			{
				I.baseEnvStats().setLevel(Util.s_int(Util.getBit(codeStr3,0)));
				I.baseEnvStats().setWeight(Util.s_int(Util.getBit(codeStr3,1)) / 10);
				if(I.baseEnvStats().weight()<1) I.baseEnvStats().setWeight(1);
				if(I instanceof Rideable)
					I.baseEnvStats().setWeight(Util.s_int(Util.getBit(codeStr3,1)) * 10);
				I.setBaseValue(Util.s_int(Util.getBit(codeStr3,2)));
			}
			else
			{
				I.baseEnvStats().setLevel(Util.s_int(codeStr3));
			}


			I.setName(objectName);
			I.setDisplayText(objectDisplay);
			if(!objectDescription.trim().equalsIgnoreCase("OLDSTYLE"))
				I.setDescription(objectDescription);
			if((I instanceof Weapon)
			&&((objectName.toUpperCase().indexOf("TWO-HANDED")>=0)
			||(objectName.toUpperCase().indexOf("TWO HANDED")>=0)))
			{
				I.setRawLogicalAnd(true);
				I.setRawProperLocationBitmap(Item.HELD|I.rawProperLocationBitmap());
			}

			boolean materialchange=true;
			if(objectDescription.equalsIgnoreCase("steel"))
				I.setMaterial(EnvResource.RESOURCE_STEEL);
			else
			if(objectDescription.equalsIgnoreCase("cloth"))
				I.setMaterial(EnvResource.RESOURCE_COTTON);
			else
			if(objectDescription.equalsIgnoreCase("leather"))
				I.setMaterial(EnvResource.RESOURCE_LEATHER);
			else
			if(objectDescription.equalsIgnoreCase("metal"))
				I.setMaterial(EnvResource.RESOURCE_TIN);
			else
			if(objectDescription.equalsIgnoreCase("glass"))
				I.setMaterial(EnvResource.RESOURCE_GLASS);
			else
			if(objectDescription.equalsIgnoreCase("mithril"))
				I.setMaterial(EnvResource.RESOURCE_MITHRIL);
			else
			if(objectDescription.equalsIgnoreCase("adamantite"))
				I.setMaterial(EnvResource.RESOURCE_ADAMANTITE);
			else
			if(objectDescription.equalsIgnoreCase("wood"))
				I.setMaterial(EnvResource.RESOURCE_OAK);
			else
			if(objectDescription.equalsIgnoreCase("iron"))
				I.setMaterial(EnvResource.RESOURCE_IRON);
			else
			if(objectDescription.equalsIgnoreCase("brass"))
				I.setMaterial(EnvResource.RESOURCE_BRASS);
			else
			if(objectDescription.equalsIgnoreCase("vellum"))
				I.setMaterial(EnvResource.RESOURCE_HIDE);
			else
			if(objectDescription.equalsIgnoreCase("silver"))
				I.setMaterial(EnvResource.RESOURCE_SILVER);
			else
			if(objectDescription.equalsIgnoreCase("gold"))
				I.setMaterial(EnvResource.RESOURCE_GOLD);
			else
			if(objectDescription.equalsIgnoreCase("copper"))
				I.setMaterial(EnvResource.RESOURCE_COPPER);
			else
			if(objectDescription.equalsIgnoreCase("bronze"))
				I.setMaterial(EnvResource.RESOURCE_BRONZE);
			else
			if(objectDescription.equalsIgnoreCase("crystal"))
				I.setMaterial(EnvResource.RESOURCE_CRYSTAL);
			else
			if(objectDescription.equalsIgnoreCase("clay"))
				I.setMaterial(EnvResource.RESOURCE_CLAY);
			else
			if(objectDescription.equalsIgnoreCase("china"))
				I.setMaterial(EnvResource.RESOURCE_CHINA);
			else
			if(objectDescription.equalsIgnoreCase("diamond"))
				I.setMaterial(EnvResource.RESOURCE_DIAMOND);
			else
			if(objectDescription.equalsIgnoreCase("pearl"))
				I.setMaterial(EnvResource.RESOURCE_PEARL);
			else
			if(objectDescription.equalsIgnoreCase("gem"))
				I.setMaterial(EnvResource.RESOURCE_STEEL);
			else
			if(objectDescription.equalsIgnoreCase("paper"))
				I.setMaterial(EnvResource.RESOURCE_PAPER);
			else
				materialchange=false;

			// correction for certain rings
			if((((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_CLOTH)
				||((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_PAPER))
			   &&(I.canBeWornAt(Armor.ON_LEFT_FINGER)))
			{
				I.setMaterial(EnvResource.RESOURCE_SILVER);
				materialchange=true;
			}

			if(materialchange)
			    I.setDescription("");

			if(Util.isSet(extraFlag,0))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_GLOWING);

			//if((extraFlag&2)==2) coffeemud has no hummers
			if(Util.isSet(extraFlag,2))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_DARK);

			if(Util.isSet(extraFlag,4))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_EVIL);

			if(Util.isSet(extraFlag,5))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_INVISIBLE);

			if(Util.isSet(extraFlag,6))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_BONUS);

			if(Util.isSet(extraFlag,7))
				I.setDroppable(false);

			if(Util.isSet(extraFlag,8))
				I.addNonUninvokableAffect(CMClass.getAbility("Prayer_Bless"));

			Ability prop_WearZapper = CMClass.getAbility("Prop_WearZapper");

			if(Util.isSet(extraFlag,9))
				prop_WearZapper.setMiscText(prop_WearZapper.text()+" -good");

			if(Util.isSet(extraFlag,10))
				prop_WearZapper.setMiscText(prop_WearZapper.text()+" -evil");

			if(Util.isSet(extraFlag,11))
				prop_WearZapper.setMiscText(prop_WearZapper.text()+" -neutral");

			if(prop_WearZapper.text().length()>0)
				I.addNonUninvokableAffect(prop_WearZapper);

			if(Util.isSet(extraFlag,12))
				I.setRemovable(false);

			//if(extraFlag&4096)==4096) coffeemud doesn't support rotting cargo

			if(Util.isSet(extraFlag,14))
				I.setGettable(false);

			//if(extraFlag&16384)==16384) coffeemud doesn't support rotting cargo

			if(Util.isSet(extraFlag,16))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_INVISIBLE);

			if(Util.isSet(extraFlag,17))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_GOOD);

			if(Util.isSet(extraFlag,18))
				if((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
					I.setMaterial(EnvResource.RESOURCE_GLASS);

			if(Util.isSet(extraFlag,20))
				I.baseEnvStats().setSensesMask(I.baseEnvStats().sensesMask()|EnvStats.CAN_NOT_SEE);

			//if(Util.isSet(extraFlag,22))
			//nothing is unidentifiable

			// now all those funny tags
			while(objV.size()>0)
			{
				String codeLine=nextLine(objV).trim().toUpperCase();
				if(codeLine.equals("E"))
				{
					eatNextLine(objV);
					if((CMClass.getItem(I.ID())!=null)
					&&(I.description().equals(CMClass.getItem(I.ID()).description())))
					   I.setDescription("");
					else
					if(I.description().length()>0)
						I.setDescription(I.description()+"%0D");
					eatLineSquiggle(objV);
					I.setDescription(I.description()+Util.safetyFilter(eatLineSquiggle(objV)));
					if(I.ID().equals("GenReadable"))
						I.setReadableText(fixReadableContent(I.description()));
				}
				else
				if(codeLine.equals("L"))
				{
					eatNextLine(objV);
					// need to figure this one out.
					eatLine(objV);
				}
				else
				if(codeLine.startsWith("X "))
				{
					codeLine=eatLineSquiggle(objV);
					Behavior B=I.fetchBehavior("Sounder");
					if(B==null)
					{
						B=CMClass.getBehavior("Sounder");
						if(B!=null) I.addBehavior(B);
					}
					if(B.getParms().length()==0)
						B.setParms(codeLine.substring(1).trim());
					else
						B.setParms(B.getParms()+";"+codeLine.substring(1).trim());
				}
				else
				if(codeLine.equals("A"))
				{
					eatNextLine(objV);
					String codesLine=eatNextLine(objV);
					if(Util.numBits(codesLine)!=2)
						returnAnError(mob,"Malformed 'A' code for item "+objectID+", "+I.Name()+": "+codesLine+", area="+areaName);
					else
					{
						int num=Util.s_int(Util.getBit(codesLine,0));
						int val=Util.s_int(Util.getBit(codesLine,1));
						switch(num)
						{
						case 1:
							adjuster.setMiscText(adjuster.text()+" str"+((val>=0)?("+"+val):(""+val)));
							break;
						case 2:
							adjuster.setMiscText(adjuster.text()+" dex"+((val>=0)?("+"+val):(""+val)));
							break;
						case 3:
							adjuster.setMiscText(adjuster.text()+" int"+((val>=0)?("+"+val):(""+val)));
							break;
						case 4:
							adjuster.setMiscText(adjuster.text()+" wis"+((val>=0)?("+"+val):(""+val)));
							break;
						case 5:
							adjuster.setMiscText(adjuster.text()+" con"+((val>=0)?("+"+val):(""+val)));
							break;
						case 6:
							// coffeemud don't play with sex
							break;
						case 7:
							adjuster.setMiscText(adjuster.text()+" cha"+((val>=0)?("+"+val):(""+val)));
							break;
						case 8: 	break;
						case 9: 	break;
						case 10: 	break;
						case 11: 	break;
						case 12:
							adjuster.setMiscText(adjuster.text()+" mana"+((val>=0)?("+"+val):(""+val)));
							break;
						case 13:
							adjuster.setMiscText(adjuster.text()+" hit"+((val>=0)?("+"+val):(""+val)));
							break;
						case 14:
							adjuster.setMiscText(adjuster.text()+" move"+((val>=0)?("+"+val):(""+val)));
							break;
						case 15: 	break;
						case 16: 	break;
						case 17:
							if((val>0)&&(I instanceof Armor))
								I.baseEnvStats().setArmor(I.baseEnvStats().armor()+(val*5));
							else
								adjuster.setMiscText(adjuster.text()+" armor"+((val>=0)?("+"+(val*5)):(""+(val*5))));
							break;
						case 18:
							if((val>0)&&(I instanceof Weapon))
								I.baseEnvStats().setAttackAdjustment(I.baseEnvStats().attackAdjustment()+(val*5));
							else
								adjuster.setMiscText(adjuster.text()+" attack"+((val>=0)?("+"+(val*5)):(""+(val*5))));
							break;
						case 19:
							if((val>0)&&(I instanceof Weapon))
								I.baseEnvStats().setDamage(I.baseEnvStats().damage()+val);
							else
								adjuster.setMiscText(adjuster.text()+" damage"+((val>=0)?("+"+(val)):(""+(val))));
							break;
						case 20: // spells, but with a numeric value.. ?!?!
							break;
						case 21: 	break;
						case 22: 	break;
						case 23: 	break;
						case 24:
							resister.setMiscText(resister.text()+" magic "+((-val)*2)+"%");
							break;
						case 25:
							// i have no idea what a power up is
							break;
						case 30:
							switch(val)
							{
							case 6:
								caster.setMiscText(caster.text()+("Prayer_Curse")+";");
								break;
							case 9:
								caster.setMiscText(caster.text()+("Poison")+";");
								break;
							case 10:
								caster.setMiscText(caster.text()+("Prayer_Plague")+";");
								break;
							case 11:
								caster.setMiscText(caster.text()+("Spell_Blindness")+";");
								break;
							}
							break;
						}
					}
				}
				else
				if(codeLine.equals("F"))
				{
					eatNextLine(objV);
					String codesLine=eatNextLine(objV);
					if(Util.numBits(codesLine)!=4)
						returnAnError(mob,"Malformed 'F' code for item "+objectID+", "+I.Name()+": "+codesLine+", area="+areaName);
					else
					{
						String codeType=Util.getBit(codesLine,0);
						if(codeType.equals("V"))
						{
							int res=getBitMask(codesLine,3);
							int imm=getBitMask(codesLine,3);
							String[] resistances={
								" teleport",
								" mind",
								" magic",
								" weapons",
								" blunt",
								" pierce",
								" slash",
								" fire",
								" cold",
								" elec",
								" acid",
								" poison",
								" evil",
								" holy",
								"",
								" mind",
								" disease",
								" gas"};
							for(int rei=0;rei<resistances.length;rei++)
							{
								if((Util.isSet(res,rei))&&(resistances[rei].length()>0))
									resister.setMiscText(resister.text()+resistances[rei]+" -25%");
								else
								if((Util.isSet(imm,rei))&&(resistances[rei].length()>0))
									resister.setMiscText(resister.text()+resistances[rei]+" -100%");
							}

						}
						if(codeType.equals("A"))
						{
							int dis=0;
							int sense=0;
							int codeBits=getBitMask(codesLine,3);
							if(Util.isSet(codeBits,0))
								sense=sense|EnvStats.CAN_NOT_SEE;
							if(Util.isSet(codeBits,1))
								dis=dis|EnvStats.IS_INVISIBLE;
							if(Util.isSet(codeBits,2))
								sense=sense|EnvStats.CAN_SEE_EVIL;
							if(Util.isSet(codeBits,3))
								sense=sense|EnvStats.CAN_SEE_INVISIBLE;
							if(Util.isSet(codeBits,4))
								sense=sense|EnvStats.CAN_SEE_BONUS;
							if(Util.isSet(codeBits,5))
								sense=sense|EnvStats.CAN_SEE_HIDDEN|EnvStats.CAN_SEE_SNEAKERS;
							if(Util.isSet(codeBits,6))
								caster.setMiscText(caster.text()+("Prayer_Sanctuary")+";");
							if(Util.isSet(codeBits,7))
								caster.setMiscText(caster.text()+("Prayer_Sanctuary")+";");
							if(Util.isSet(codeBits,8))
								caster.setMiscText(caster.text()+("Spell_FaerieFire")+";");
							if(Util.isSet(codeBits,9))
								sense=sense|EnvStats.CAN_SEE_INFRARED;
							if(Util.isSet(codeBits,10))
								caster.setMiscText(caster.text()+("Prayer_Curse")+";");
							if(Util.isSet(codeBits,11))
								caster.setMiscText(caster.text()+"Specialization_Weapon;");
							if(Util.isSet(codeBits,12))
								caster.setMiscText(caster.text()+("Poison")+";");
							if(Util.isSet(codeBits,13))
								caster.setMiscText(caster.text()+("Prayer_ProtEvil")+";");
							if(Util.isSet(codeBits,14))
								caster.setMiscText(caster.text()+("Prayer_ProtGood")+";");
							if(Util.isSet(codeBits,15))
								dis=dis|EnvStats.IS_SNEAKING;
							if(Util.isSet(codeBits,16))
								dis=dis|EnvStats.IS_HIDDEN;
							if(Util.isSet(codeBits,17))
							{
								dis=dis|EnvStats.IS_SLEEPING;
								caster.setMiscText(caster.text()+("Spell_Sleep")+";");
							}
							//if(Util.isSet(codeBits,18)) item cannot charm you
							//	caster.setMiscText(caster.text()+(new Poison().ID())+";");
							if(Util.isSet(codeBits,19))
								dis=dis|EnvStats.IS_FLYING;
							if(Util.isSet(codeBits,20))
								caster.setMiscText(caster.text()+("Spell_PassDoor")+";");
							if(Util.isSet(codeBits,21))
								caster.setMiscText(caster.text()+("Spell_Haste")+";");
							if(Util.isSet(codeBits,22))
								caster.setMiscText(caster.text()+("Prayer_Calm")+";");
							if(Util.isSet(codeBits,23))
								caster.setMiscText(caster.text()+("Prayer_Plague")+";");
							if(Util.isSet(codeBits,24))
								caster.setMiscText(caster.text()+("Spell_Awe")+";");
							if(Util.isSet(codeBits,25))
								sense=sense|EnvStats.CAN_SEE_DARK;
							if(Util.isSet(codeBits,26))
								caster.setMiscText(caster.text()+("Fighter_Berzerk")+";");
							if(Util.isSet(codeBits,27))
								caster.setMiscText(caster.text()+("Regeneration")+";");
							if(Util.isSet(codeBits,28))
								sense=sense|EnvStats.CAN_SEE_GOOD;
							if(Util.isSet(codeBits,29))
								caster.setMiscText(caster.text()+("Spell_Slow")+";");
							if(sense>0)
								adjuster.setMiscText(adjuster.text()+" sen+"+sense);
							if(dis>0)
								adjuster.setMiscText(adjuster.text()+" dis+"+dis);
						}
						else
						{
							int res=getBitMask(codesLine,3);
							int imm=getBitMask(codesLine,3);
							String[] resistances={
								" teleport",
								" mind",
								" magic",
								" weapons",
								" blunt",
								" pierce",
								" slash",
								" fire",
								" cold",
								" elec",
								" acid",
								" poison",
								" evil",
								" holy",
								"",
								" mind",
								" disease",
								" gas"};
							for(int rei=0;rei<resistances.length;rei++)
							{
								if((Util.isSet(res,rei))&&(resistances[rei].length()>0))
									resister.setMiscText(resister.text()+resistances[rei]+" 25%");
								else
								if((Util.isSet(imm,rei))&&(resistances[rei].length()>0))
									resister.setMiscText(resister.text()+resistances[rei]+" 100%");
							}

							if(Util.isSet(res,14))
								caster.setMiscText(caster.text()+"Spell_ResistMagicMissiles;");
							//if(Util.isSet(res,18)) no light resistance
							//if(Util.isSet(res,18)) no sound resistance
						}


					}
				}
				else
				if((codeLine.startsWith("#"))||(codeLine.length()==0))
				{
					eatNextLine(objV);
				}
				else
				{
					eatNextLine(objV);
					returnAnError(mob,"Unknown code for item "+objectID+", "+I.Name()+": "+codeLine+", area="+areaName);
				}
			}
			if(adjuster.text().length()>0)
				I.addNonUninvokableAffect(adjuster);
			if(caster.text().length()>0)
				I.addNonUninvokableAffect(caster);
			if(resister.text().length()>0)
				I.addNonUninvokableAffect(resister);
			I.recoverEnvStats();
			I.text();
			I.recoverEnvStats();
			if(OfThisID.startsWith("#"))
				doneItems.put(OfThisID.substring(1),I);
			else
				doneItems.put(OfThisID,I);
			return I;
		}
		return null;
	}

	public static String socialFix(String str)
	{

		str=Util.replaceAll(str,"$n","<S-NAME>");
		str=Util.replaceAll(str,"$N","<T-NAMESELF>");
		str=Util.replaceAll(str,"$m","<S-HIM-HER>");
		str=Util.replaceAll(str,"$M","<T-HIM-HER>");
		str=Util.replaceAll(str,"$s","<S-HIS-HER>");
		str=Util.replaceAll(str,"$S","<T-HIS-HER>");
		str=Util.replaceAll(str,"$e","<S-HE-SHE>");
		str=Util.replaceAll(str,"$E","<T-HE-SHE>");
		str=Util.replaceAll(str,"`","\'");
		if(str.equals("$")) return "";
		return str.trim();
	}

	public static Room findRoomSomewhere(String roomID, String areaName, Hashtable doneRooms)
	{
		if(roomID.startsWith("#"))
		{
			if(doneRooms.containsKey(roomID.substring(1)))
				return (Room)doneRooms.get(roomID.substring(1));
		}
		else
		{
			if(doneRooms.containsKey(roomID))
				return (Room)doneRooms.get(roomID);
		}
		Room R=CMMap.getRoom(roomID);
		if(R!=null) return R;
		return CMMap.getRoom(areaName+"#"+roomID);
	}

	public static boolean localDeleteArea(MOB mob, Vector reLinkTable, String areaName)
	{
		if(mob.location().getArea().Name().equalsIgnoreCase(areaName))
		{
			mob.tell("You dip!  You are IN that area!  Leave it first...");
			return false;
		}
		else
		{
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(!R.getArea().Name().equalsIgnoreCase(areaName))
					for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
					{
						Room dirR=R.rawDoors()[d];
						if((dirR!=null)&&(dirR.getArea().Name().equalsIgnoreCase(areaName)))
							reLinkTable.addElement(R.roomID()+"/"+d+"/"+dirR.roomID());
					}
			}
			while(true)
			{
				Room foundOne=null;
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(R.getArea().Name().equalsIgnoreCase(areaName))
					{
						foundOne=R;
						break;
					}
				}
				if(foundOne==null)
					break;
				else
					Rooms.obliterateRoom(foundOne);
			}
		}
		Area A1=CMMap.getArea(areaName);
		if(A1!=null)
		{
			ExternalPlay.DBDeleteArea(A1);
			CMMap.delArea(A1);
		}
		return true;
	}

	public static void areimport(MOB mob, Vector commands)
	{
		boolean prompt=true;
		Hashtable doneItems=new Hashtable();
		Hashtable doneRooms=new Hashtable();
		Hashtable doneMOBS=new Hashtable();

		commands.removeElementAt(0);

		// verify that the process can start
		if(commands.size()<1)
		{
			mob.tell("Import what?  Specify the path/filename!");
			return;
		}
		if(mob.isMonster())
		{
			mob.tell("No can do.");
			return;
		}

		if(((String)commands.elementAt(0)).equalsIgnoreCase("noprompt"))
		{
			commands.removeElementAt(0);
			prompt=false;
		}

		// continue pre-processing
		for(int areaFile=commands.size()-1;areaFile>=0;areaFile--)
		{
			String areaFileName=(String)commands.elementAt(areaFile);
			File F=new File(areaFileName);
			File[] FF=F.listFiles();
			if((FF!=null)&&(FF.length>0))
			{
				for(int f=0;f<FF.length;f++)
					commands.addElement(FF[f].getAbsolutePath());
				commands.removeElementAt(areaFile);
			}
		}

		Vector mobData=new Vector();
		Vector objectData=new Vector();

		for(int areaFile=0;areaFile<commands.size();areaFile++)
		{
		Vector areaData=new Vector();
		Vector roomData=new Vector();
		Vector resetData=new Vector();
		Vector mobProgData=new Vector();
		Vector objProgData=new Vector();
		Vector shopData=new Vector();
		Vector specialData=new Vector();
		Vector newRooms=new Vector();
		Vector socialData=new Vector();
		Vector reLinkTable=null;

		String areaFileName=(String)commands.elementAt(areaFile);
		// read in the .are file
		StringBuffer buf=Resources.getFile(areaFileName);
		if((buf==null)||((buf!=null)&&(buf.length()==0)))
		{
			mob.tell("File not found at: '"+areaFileName+"'!");
			return;
		}
		try
		{
			if(areaFileName.toUpperCase().trim().endsWith(".LST"))
			{
				mob.tell("Unpacking areas lists from file : '"+areaFileName+"'...");
				String filePrefix="";
				int c=areaFileName.lastIndexOf(File.separator);
				if(c>=0) filePrefix=areaFileName.substring(0,c+1);
				c=0;
				String fn="";
				while((buf.length()>0)&&(c<buf.length()))
				{
					switch(buf.charAt(c))
					{
					case '\n':
					case '\r':
						if((fn.length()>0)&&(!fn.startsWith("#"))&&(!fn.startsWith("$")))
							commands.addElement(filePrefix+fn);
						buf.delete(0,c+1);
						c=0;
						fn="";
						break;
					default:
						fn+=(char)buf.charAt(c);
						c++;
						break;
					}
				}
				if((fn.length()>0)&&(!fn.startsWith("#"))&&(!fn.startsWith("$")))
					commands.addElement(filePrefix+fn);
				continue;
			}
			if((buf.length()>20)&&(buf.substring(0,20).indexOf("<AREAS>")>=0))
			{
				Vector areas=new Vector();
				if(mob.session()!=null)
					mob.session().rawPrint("Unpacking area(s) from file: '"+areaFileName+"'...");
				String error=com.planet_ink.coffee_mud.common.Generic.fillAreasVectorFromXML(buf.toString(),areas);
				if(error.length()>0) return;
				if(mob.session()!=null)
					mob.session().rawPrintln("!");
				mob.tell("Found "+areas.size()+" areas.");
				int num=areas.size();
				int a=0;
				while(areas.size()>0)
				{
					if(mob.session()!=null)
						mob.session().rawPrint("Unpacking area #"+(a+1)+"/"+num+"...");
					Vector area=(Vector)areas.firstElement();
					error=com.planet_ink.coffee_mud.common.Generic.unpackAreaFromXML(area,mob.session(),true);
					if(mob.session()!=null)
						mob.session().rawPrintln("!");
					if(error.startsWith("Area Exists: "))
					{
						String areaName=error.substring(13).trim();
						if((!prompt)
						||(mob.session().confirm("Area: \""+areaName+"\" exists, obliterate first?","N")))
						{
							if(reLinkTable==null) reLinkTable=new Vector();
							if(mob.location().getArea().Name().equalsIgnoreCase(areaName))
								for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
								{
									Room R=(Room)r.nextElement();
									if((R!=null)&&(!R.getArea().Name().equalsIgnoreCase(areaName)))
									{
										R.bringMobHere(mob,true);
										break;
									}
								}
							if(!localDeleteArea(mob,reLinkTable,areaName))
								return;
						}
						else
							return;
					}
					else
					if(error.length()>0)
					{
						mob.tell("An error occurred on import: "+error);
						mob.tell("Please correct the problem and try the import again.");
						return;
					}
					else
					{
						areas.removeElement(area);
						a++;
					}
				}
				Log.sysOut("Import",mob.Name()+" imported "+areaFileName);
				mob.tell("Area(s) successfully imported!");
				continue;
			}
			else
			if((buf.length()>20)&&(buf.substring(0,20).indexOf("<AREA>")>=0))
			{
				if(mob.session()!=null)
					mob.session().rawPrint("Unpacking area from file: '"+areaFileName+"'...");
				String error=com.planet_ink.coffee_mud.common.Generic.unpackAreaFromXML(buf.toString(),mob.session(),true);
				if(mob.session()!=null)
					mob.session().rawPrintln("!");
				if(error.startsWith("Area Exists: "))
				{
					String areaName=error.substring(13).trim();
					if((!prompt)
					||(mob.session().confirm("Area: \""+areaName+"\" exists, obliterate first?","N")))
					{
						reLinkTable=new Vector();
						if(!localDeleteArea(mob,reLinkTable,areaName))
							return;
					}
					else
						return;
					if(mob.session()!=null)
						mob.session().rawPrint("Unpacking area from file: '"+areaFileName+"'...");
					error=com.planet_ink.coffee_mud.common.Generic.unpackAreaFromXML(buf.toString(),mob.session(),true);
					if(mob.session()!=null)
						mob.session().rawPrintln("!");
				}
				if(error.length()>0)
				{
					mob.tell("An error occurred on import: "+error);
					mob.tell("Please correct the problem and try the import again.");
					return;
				}
				else
				{
					Log.sysOut("Import",mob.Name()+" imported "+areaFileName);
					mob.tell("Area successfully imported!");
					continue;
				}
			}
			else
			if((buf.length()>20)&&(buf.substring(0,20).indexOf("<AROOM>")>=0))
			{
				mob.tell("Unpacking room from file: '"+areaFileName+"'...");
				String error=com.planet_ink.coffee_mud.common.Generic.unpackRoomFromXML(buf.toString(),true);
				if(error.startsWith("Room Exists: "))
				{
					Room R=CMMap.getRoom(error.substring(13).trim());
					if(R!=null)
					{
						reLinkTable=new Vector();
						for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
						{
							Room R2=(Room)r.nextElement();
							if(R2!=R)
							for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
							{
								Room dirR=R2.rawDoors()[d];
								if((dirR!=null)&&(dirR==R))
									reLinkTable.addElement(R2.roomID()+"/"+d+"/"+dirR.roomID());
							}
						}
						Rooms.obliterateRoom(R);
					}
					error=com.planet_ink.coffee_mud.common.Generic.unpackRoomFromXML(buf.toString(),true);
				}
				if(error.length()>0)
				{
					mob.tell("An error occurred on import: "+error);
					mob.tell("Please correct the problem and try the import again.");
					return;
				}
				else
				{
					Log.sysOut("Import",mob.Name()+" imported "+areaFileName);
					mob.tell("Room successfully imported!");
					continue;
				}
			}
			else
			if((buf.length()>20)&&(buf.substring(0,20).indexOf("<MOBS>")>=0))
			{
				if(mob.session()!=null)
					mob.session().rawPrint("Unpacking mobs from file: '"+areaFileName+"'...");
				Vector mobs=new Vector();
				String error=com.planet_ink.coffee_mud.common.Generic.addMOBsFromXML(buf.toString(),mobs,mob.session());
				if(mob.session()!=null)	mob.session().rawPrintln("!");
				if(error.length()>0)
				{
					mob.tell("An error occurred on import: "+error);
					mob.tell("Please correct the problem and try the import again.");
					return;
				}
				else
				{
					for(int m=0;m<mobs.size();m++)
					{
						MOB M=(MOB)mobs.elementAt(m);
						M.setStartRoom(mob.location());
						M.setLocation(mob.location());
						M.bringToLife(mob.location(),true);
					}
					mob.location().recoverRoomStats();
					Log.sysOut("Import",mob.Name()+" imported "+areaFileName);
					mob.tell("MOB(s) successfully imported!");
					continue;
				}
			}
			else
			if((buf.length()>20)&&(buf.substring(0,20).indexOf("<ITEMS>")>=0))
			{
				if(mob.session()!=null)
					mob.session().rawPrint("Unpacking items from file: '"+areaFileName+"'...");
				Vector items=new Vector();
				String error=com.planet_ink.coffee_mud.common.Generic.addItemsFromXML(buf.toString(),items,mob.session());
				if(mob.session()!=null)	mob.session().rawPrintln("!");
				if(error.length()>0)
				{
					mob.tell("An error occurred on import: "+error);
					mob.tell("Please correct the problem and try the import again.");
					return;
				}
				else
				{
					for(int i=0;i<items.size();i++)
					{
						Item I=(Item)items.elementAt(i);
						mob.location().addItemRefuse(I,Item.REFUSE_PLAYER_DROP);
					}
					mob.location().recoverRoomStats();
					Log.sysOut("Import",mob.Name()+" imported "+areaFileName);
					mob.tell("Item(s) successfully imported!");
					continue;
				}
			}
		}
		catch(Exception e)
		{
			Log.errOut("Import-",e);
			mob.tell(e.getMessage());
			return;
		}


		Vector V=Resources.getFileLineVector(buf);

		// sort the data into general blocks, and identify area
		mob.tell("\n\rSorting data from file '"+areaFileName+"'...");
		readBlocks(V,areaData,roomData,mobData,resetData,objectData,mobProgData,objProgData,shopData,specialData,socialData);
		boolean didSocials=false;
		try
		{
			while(socialData.size()>0)
			{
				String codeLine=eatNextLine(socialData);
				if((!codeLine.startsWith("#"))&&(codeLine.trim().length()>0))
				{
					didSocials=true;
					String word=codeLine.trim().toUpperCase();
					int x=word.indexOf(" ");
					if(x>0) word=word.substring(0,x).trim();

					Social S1=Socials.FetchSocial(word,true);
					Social S2=Socials.FetchSocial(word+" <T-NAME>",true);
					Social S3=Socials.FetchSocial(word+" SELF",true);
					boolean changing=true;
					if((S1==null)||(!S1.name().toUpperCase().equals(word)))
					{
						S1=new Social();
						S1.setName(word);
						Socials.addSocial(S1);
						changing=false;
					}

					String str=socialFix(eatNextLine(socialData));
					if(str.startsWith("#")) continue;

					if((S1.You_see()==null)||(!S1.You_see().equals(str)))
					{
						if(changing)
						mob.session().rawPrint("Change '"+S1.name()+"' from '"+S1.You_see()+"', you see, to: '"+str+"'");
						if((!changing)||(mob.session().confirm("?","Y")))
							S1.setYou_see(str);
					}

					str=socialFix(eatNextLine(socialData));
					if(str.startsWith("#")) continue;

					if((S1.Third_party_sees()==null)||(!S1.Third_party_sees().equals(str)))
					{
						if(changing)
						mob.session().rawPrint("Change '"+S1.name()+"' from '"+S1.Third_party_sees()+"', others see, to: '"+str+"'");
						if((!changing)||(mob.session().confirm("?","Y")))
							S1.setThird_party_sees(str);
					}

					changing=true;
					str=socialFix(eatNextLine(socialData));
					if(str.startsWith("#")) continue;
					if(S2==null)
					{
						S2=new Social();
						S2.setName(word+" <T-NAME>");
						Socials.addSocial(S2);
						changing=false;
					}

					if((S2.You_see()==null)||(!S2.You_see().equals(str)))
					{
						if(changing)
						mob.session().rawPrint("Change '"+S2.name()+"' from '"+S2.You_see()+"', you see, to: '"+str+"'");
						if((!changing)||(mob.session().confirm("?","Y")))
							S2.setYou_see(str);
					}

					str=socialFix(eatNextLine(socialData));
					if(str.startsWith("#")) continue;

					if((S2.Third_party_sees()==null)||(!S2.Third_party_sees().equals(str)))
					{
						if(changing)
						mob.session().rawPrint("Change '"+S2.name()+"', others see from '"+S2.Third_party_sees()+"', to: '"+str+"'");
						if((!changing)||(mob.session().confirm("?","Y")))
							S2.setThird_party_sees(str);
					}

					str=socialFix(eatNextLine(socialData));
					if(str.startsWith("#")) continue;

					if((S2.Target_sees()==null)||(!S2.Target_sees().equals(str)))
					{
						if(changing)
						mob.session().rawPrint("Change '"+S2.name()+"', target sees from '"+S2.Target_sees()+"', to: '"+str+"'");
						if((!changing)||(mob.session().confirm("?","Y")))
							S2.setTarget_sees(str);
					}

					str=socialFix(eatNextLine(socialData));
					if(str.startsWith("#")) continue;

					if((S2.See_when_no_target()==null)||(!S2.See_when_no_target().equals(str)))
					{
						if(changing)
						mob.session().rawPrint("Change '"+S2.name()+"', no target sees from '"+S2.See_when_no_target()+"', to: '"+str+"'");
						if((!changing)||(mob.session().confirm("?","Y")))
							S2.setSee_when_no_target(str);
					}

					changing=true;
					str=socialFix(eatNextLine(socialData));
					if(str.startsWith("#")) continue;
					if(S3==null)
					{
						S3=new Social();
						S3.setName(word+" SELF");
						Socials.addSocial(S3);
						changing=false;
					}

					if((S3.You_see()==null)||(!S3.You_see().equals(str)))
					{
						if(changing)
						mob.session().rawPrint("Change '"+S3.name()+"', you see from '"+S3.You_see()+"', to: '"+str+"''");
						if((!changing)||(mob.session().confirm("?","Y")))
							S3.setYou_see(str);
					}

					str=socialFix(eatNextLine(socialData));
					if(str.startsWith("#")) continue;

					if((S3.Third_party_sees()==null)||(!S3.Third_party_sees().equals(str)))
					{
						if(changing)
						mob.session().rawPrint("Change '"+S3.name()+"', others see from '"+S3.Third_party_sees()+"', to: '"+str+"'");
						if((!changing)||(mob.session().confirm("?","Y")))
							S3.setThird_party_sees(str);
					}

				}
			}
			if(didSocials)
			{
				Log.sysOut("Import",mob.Name()+" imported socials from "+areaFileName);
				Socials.save();
			}
		}
		catch(Exception e)
		{
			Log.errOut("Import",e);
			mob.tell(e.getMessage());
			return;
		}

		if((roomData.size()==0)||(areaData.size()==0))
		{
			if(!didSocials)
				mob.tell("Missing data! It is very unlikely this is an .are file.");
			return;
		}
		String areaName=getAreaName(areaData);
		if((areaName==null)||((areaName!=null)&&(areaName.length()==0)))
		{
			if(!didSocials)
				mob.tell("#AREA tag not found!");
			return;
		}

		try
		{
			// confirm area creation/overwrite
			boolean exists=false;
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.getArea().Name().equalsIgnoreCase(areaName))
				{
					exists=true;
					break;
				}
			}
			if(exists)
			{
				if((!prompt)
				||(mob.session().confirm("Area: \""+areaName+"\" exists, obliterate first?","N")))
				{
					reLinkTable=new Vector();
					if(!localDeleteArea(mob,reLinkTable,areaName))
						return;
				}
				else
					return;
			}
			else
			if((prompt)&&(!mob.session().confirm("Found area: \""+areaName+"\", is this ok?","Y")))
				return;

			mob.tell("Loading and Linking Rooms...");
			Log.sysOut("Import",mob.Name()+" imported "+areaName+" from "+areaFileName);
			// begin initial room-read
			// build first room structures, leaving rest for later.
			Room lastRoom=null;
			Hashtable petShops=new Hashtable();
			for(int r=0;r<roomData.size();r++)
			{
				Vector roomV=null;
				if(roomData.elementAt(r) instanceof Vector)
					roomV=(Vector)roomData.elementAt(r);
				else
				if(roomData.elementAt(r) instanceof String)
				{
					String s=(String)roomData.elementAt(r);
					if(!s.toUpperCase().trim().startsWith("#ROOM"))
						returnAnError(mob,"Eating immaterial line: "+roomData.elementAt(r)+", area="+areaName);
					continue;
				}
				else
					continue;

				Area A=CMMap.getArea(areaName);
				if(A==null)
					A=ExternalPlay.DBCreateArea(areaName,"StdArea");
				else
					A.toggleMobility(false);

				Room R=CMClass.getLocale("StdRoom");
				String plainRoomID=eatNextLine(roomV);
				R.setRoomID(plainRoomID);
				R.setDisplayText(Util.safetyFilter(eatLineSquiggle(roomV)));
				R.setDescription(Util.safetyFilter(eatLineSquiggle(roomV)));
				R.setArea(A);
				String codeLine=eatNextLine(roomV);
				if((!R.roomID().startsWith("#"))
				||(R.displayText().length()==0)
				||(Util.numBits(codeLine)<2)
				||(Util.numBits(codeLine)>3))
				{
					returnAnError(mob,"Malformed room! Aborting this room "+R.roomID()+", display="+R.displayText()+", description="+R.description()+", numBits="+Util.numBits(codeLine)+", area="+areaName);
					continue;
				}
				else
					R.setRoomID(areaName+R.roomID());
				int codeBits=getBitMask(codeLine,0);
				int sectorType=getBitMask(codeLine,1);
				if(Util.numBits(codeLine)==3)
				{
					codeBits=sectorType;
					sectorType=getBitMask(codeLine,2);
				}
				if((codeBits&8)==0)
				{
					switch(sectorType)
					{
					case 0:	R=changeRoomClass(R,"CityStreet"); break;
					case 1:	R=changeRoomClass(R,"CityStreet"); break;
					case 2:	R=changeRoomClass(R,"Plains"); break;
					case 3:	R=changeRoomClass(R,"Woods"); break;
					case 4:	R=changeRoomClass(R,"Hills"); break;
					case 5:	R=changeRoomClass(R,"Mountains"); break;
					case 6:	R=changeRoomClass(R,"ShallowWater"); break;
					case 7:	R=changeRoomClass(R,"WaterSurface"); break;
					case 8:	R=changeRoomClass(R,"FrozenPlains"); break;
					case 9:	R=changeRoomClass(R,"InTheAir"); break;
					case 10: R=changeRoomClass(R,"Desert"); break;
					case 11: R=changeRoomClass(R,"FrozenPlains"); break;
					case 12: R=changeRoomClass(R,"FrozenMountains"); break;
					}
				}
				else
				{
					switch(sectorType)
					{
 					case 0:	R=changeRoomClass(R,"StoneRoom"); break;
					case 1:	R=changeRoomClass(R,"StoneRoom"); break;
					case 2:	R=changeRoomClass(R,"WoodRoom"); break;
					case 3:	R=changeRoomClass(R,"WoodRoom"); break;
					case 4:	R=changeRoomClass(R,"StoneRoom"); break;
					case 5:	R=changeRoomClass(R,"StoneRoom"); break;
					case 6:	R=changeRoomClass(R,"IndoorShallowWater"); break;
					case 7:	R=changeRoomClass(R,"IndoorWaterSurface"); break;
					case 8:	R=changeRoomClass(R,"IceRoom"); break;
					case 9:	R=changeRoomClass(R,"IndoorInTheAir"); break;
					case 10: R=changeRoomClass(R,"HotRoom"); break;
					case 11: R=changeRoomClass(R,"IceRoom"); break;
					case 12: R=changeRoomClass(R,"IceRoom"); break;
					}
					switch(sectorType)
					{
					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
					case 10:
					case 11:
					case 12:
						if((R.displayText().toUpperCase().indexOf("CAVE")>=0)
						||(R.description().toUpperCase().indexOf("CAVE")>=0))
							R=changeRoomClass(R,"CaveRoom");
						break;
					}

				}

				Ability prop_RoomCapacity=CMClass.getAbility("Prop_ReqCapacity");
				Ability prop_RoomLevels=CMClass.getAbility("Prop_ReqLevels");


				if(Util.isSet(codeBits,21)) // underwater room
					R=changeRoomClass(R,"UnderWater");

				//if(Util.isSet(codeBits,1)) //BANKS are forked up in the ROM files, who knows WHAT this is...

				if(Util.isSet(codeBits,0)) // dark room
					R.addNonUninvokableAffect(CMClass.getAbility("Prop_RoomDark"));

				if(Util.isSet(codeBits,2)) // no mobs room
					R.addNonUninvokableAffect(CMClass.getAbility("Prop_ReqNoMOB"));

				if(Util.isSet(codeBits,9)) // two people only room
				{
					prop_RoomCapacity.setMiscText("2");
					if(R.fetchAffect(prop_RoomCapacity.ID())==null)
						R.addNonUninvokableAffect(prop_RoomCapacity);
				}
				if(Util.isSet(codeBits,10)) // no fighting
					R.addNonUninvokableAffect(CMClass.getAbility("Prop_PeaceMaker"));

				if(Util.isSet(codeBits,11)) // solitaire room
				{
					prop_RoomCapacity.setMiscText("1");
					if(R.fetchAffect(prop_RoomCapacity.ID())==null)
						R.addNonUninvokableAffect(prop_RoomCapacity);
				}
				if(Util.isSet(codeBits,12))
					petShops.put(R,R);
				else
				if((lastRoom!=null)&&(petShops.get(lastRoom)!=null)&&(petShops.get(lastRoom)==lastRoom))
				{
					petShops.remove(lastRoom);
					petShops.put(R,lastRoom); // now ready to plop stuff!
				}

				if(Util.isSet(codeBits,13))
					R.addNonUninvokableAffect(CMClass.getAbility("Prop_NoRecall"));

				if(Util.isSet(codeBits,14))
				{
					prop_RoomLevels.setMiscText("SYSOP");
					if(R.fetchAffect(prop_RoomLevels.ID())==null)
						R.addNonUninvokableAffect(prop_RoomLevels);
				}
				if(Util.isSet(codeBits,15))
				{
					prop_RoomLevels.setMiscText(">=93");
					if(R.fetchAffect(prop_RoomLevels.ID())==null)
						R.addNonUninvokableAffect(prop_RoomLevels);
				}
				if(Util.isSet(codeBits,16))
				{
					prop_RoomLevels.setMiscText(">=91");
					if(R.fetchAffect(prop_RoomLevels.ID())==null)
						R.addNonUninvokableAffect(prop_RoomLevels);
				}
				if(Util.isSet(codeBits,17))
				{
					prop_RoomLevels.setMiscText("<=5");
					if(R.fetchAffect(prop_RoomLevels.ID())==null)
						R.addNonUninvokableAffect(prop_RoomLevels);
				}
				
				if(Util.isSet(codeBits,18))
				{
					R.addNonUninvokableAffect(CMClass.getAbility("Prop_NoSummon"));
					R.addNonUninvokableAffect(CMClass.getAbility("Prop_NoCharm"));
				}

				if(Util.isSet(codeBits,19))
					R.addNonUninvokableAffect(CMClass.getAbility("Prop_reqPKill"));

				if(Util.isSet(codeBits,20))
					R.addNonUninvokableAffect(CMClass.getAbility("Prop_NoTeleportOut"));

				// if(Util.isSet(codeBits,23)) No "dirt" in CoffeeMud, so this doesn't matter

				if(Util.isSet(codeBits,24))
					R.addNonUninvokableAffect(CMClass.getAbility("Prop_NoChannel"));

				roomV.insertElementAt(R.roomID(),0);
				CMMap.addRoom(R);
				newRooms.addElement(R);
				if(plainRoomID.startsWith("#"))
					doneRooms.put(plainRoomID.substring(1),R);
				else
					doneRooms.put(plainRoomID.substring(1),R);

				lastRoom=R;
			}

			// begin second pass through rooms
			// handle exits, mobs, objects, etc.
			for(int r=0;r<roomData.size();r++)
			{
				Vector roomV=null;
				if(roomData.elementAt(r) instanceof Vector)
					roomV=(Vector)roomData.elementAt(r);
				else
					continue;
				String roomID=eatLine(roomV);
				Room R=findRoomSomewhere(roomID,areaName,doneRooms);
				if(R==null)
				{
					Log.errOut("Import","Unhashed room "+roomID+"! Aborting!");
					return;
				}

				// handle exits, and 'E' tags
				while(roomV.size()>0)
				{
					String nextLine=eatNextLine(roomV);
					if(nextLine.toUpperCase().startsWith("S"))
						continue;
					else
					if(nextLine.toUpperCase().startsWith("E"))
					{
						String nameString=Util.safetyFilter(eatLineSquiggle(roomV));
						String descString=Util.safetyFilter(eatLineSquiggle(roomV));
						Item I=null;
						if(hasReadableContent(nameString))
						{
							I=CMClass.getStdItem("GenWallpaper");
							I.setReadable(true);
							I.setReadableText(fixReadableContent(descString));
						}
						else
							I=CMClass.getStdItem("GenWallpaper");
						I.setName(nameString);
						I.setDisplayText("");
						I.setDescription(descString);
						R.addItem(I);
					}
					else
					if(nextLine.toUpperCase().startsWith("D"))
					{
						int dirCode=Util.s_int(nextLine.substring(1));
						String descStr=Util.safetyFilter(eatLineSquiggle(roomV));
						String nameStr=Util.safetyFilter(eatLineSquiggle(roomV));
						String codeStr=eatLine(roomV);
						switch(dirCode)
						{
						case 0: dirCode=0; break;
						case 1: dirCode=2; break;
						case 2: dirCode=1; break;
						case 3: dirCode=3; break;
						case 4: dirCode=4; break;
						case 5: dirCode=5; break;
						default:
								returnAnError(mob,"Room: "+R.roomID()+", Unknown direction code: "+dirCode+", aborting exit, area="+areaName);
								continue;
						}
						if(Util.numBits(codeStr)!=3)
						{
							returnAnError(mob,"Room: "+R.roomID()+", Malformed exit codeStr "+codeStr+".  Aborting exit, area="+areaName);
							continue;
						}
						if((R.rawExits()[dirCode]!=null)||(R.rawDoors()[dirCode]!=null))
						{
							returnAnError(mob,"Room: "+R.roomID()+", Redundant exit codeStr "+codeStr+".  Aborting exit, area="+areaName);
							continue;
						}
						int exitFlag=( Util.s_int(Util.getBit(codeStr,0)) & 31);
						int doorState=Util.s_int(Util.getBit(codeStr,1));
						int linkRoomID=Util.s_int(Util.getBit(codeStr,2));
						Exit E=CMClass.getExit("GenExit");
						Room linkRoom=findRoomSomewhere(""+linkRoomID,areaName,doneRooms);
						if(linkRoomID>=0)
						{
							boolean hasDoor=false;
							boolean hasLock=false;
							boolean defaultsClosed=false;
							boolean defaultsLocked=false;

							if((exitFlag==1)||(exitFlag==6))
							{
								hasDoor=true;
								defaultsClosed=true;
								if(exitFlag==6)
								{
									E.baseEnvStats().setLevel(100);
									E.recoverEnvStats();
								}
							}
							if(doorState<0)
								defaultsClosed=false;
							else
							if(doorState>0)
							{
								hasDoor=true;
								defaultsClosed=true;
								if(doorState>1)
								{
									hasLock=true;
									defaultsLocked=true;
									E.setKeyName(areaName+"#"+doorState);
								}
							}
							E.setDoorsNLocks(hasDoor,!defaultsClosed,defaultsClosed,
											 hasLock,defaultsLocked,defaultsLocked);
						}
						E.setDisplayText(descStr);
						String name=((nameStr.indexOf(" ")<=0)?nameStr:(nameStr.substring(0,nameStr.indexOf(" ")))).trim();
						if(name.equalsIgnoreCase("SECRET"))
						{
							name="secret door";
							E.baseEnvStats().setDisposition(E.baseEnvStats().disposition()|EnvStats.IS_HIDDEN);
							E.recoverEnvStats();
						}

						if(name.length()>0)
							name=Util.startWithAorAn(name);
						else
						{
							if(E.hasADoor())
							{
								E.setName("a door");
								name="door";
							}
							else
							{
								E.setName("the ground");
								name="ground";
							}
						}
						E.setExitParams(name,E.closeWord(),E.openWord(),E.Name()+", closed");
						E.setDescription(descStr);
						R.rawExits()[dirCode]=E;
						Exit opExit=null;
						if(((linkRoom==null)||(linkRoom.getArea().Name()!=R.getArea().Name()))&&(linkRoomID>=0))
						{
							for(Enumeration r2=CMMap.rooms();r2.hasMoreElements();)
							{
								Room R2=(Room)r2.nextElement();
								if((R2.roomID().endsWith("#"+linkRoomID))&&(R2!=R))
								{
									for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
									{
										Exit E3=R2.rawExits()[d];
										if((E3!=null)
										&&(E3.temporaryDoorLink().length()>0)
										&&(R.roomID().endsWith(E3.temporaryDoorLink())))
										{
											opExit=E3;
											R2.rawDoors()[d]=R;
										}
									}
									if(opExit==null)
										if((prompt)&&
										  (!mob.session().confirm(R.roomID()+" links to #"+linkRoomID+". Found "+R2.roomID()+". Link?","Y")))
											continue;
									linkRoom=R2;
									if(opExit!=null) opExit.setTemporaryDoorLink("");
									if((!doneRooms.containsValue(linkRoom)))
										ExternalPlay.DBUpdateExits(linkRoom);
									break;
								}
							}
							if(linkRoom==null)
								E.setTemporaryDoorLink("#"+linkRoomID);
							else
								E.setTemporaryDoorLink("");

						}
						R.rawDoors()[dirCode]=linkRoom;
						if((linkRoom==null)&&(linkRoomID>=0))
							returnAnError(mob,"Room: "+R.roomID()+" links "+Directions.getDirectionName(dirCode)+"ward to unknown room #"+linkRoomID+", area="+areaName);
					}
					else
					if(nextLine.toUpperCase().startsWith("H"))
					{
						// not important enough to generate an error from
					}
					else
					if((!nextLine.equalsIgnoreCase("#0"))&&(nextLine.trim().length()>0))
						returnAnError(mob,"Unknown room code: "+nextLine+", area="+areaName);
				}
			}

			mob.session().print("Loading objects..");
			Hashtable containerHash=new Hashtable();
			MOB M=null;
			Room R=null;
			while(resetData.size()>0)
			{
				mob.session().print(".");
				String s=eatNextLine(resetData);
				if((s.startsWith("#RE"))||(s.startsWith("*"))||(s.startsWith("S")))
				{
				}
				else
				if(s.startsWith("M "))
				{
					String mobID=Util.getBit(s,2);
					String roomID=Util.getBit(s,4);
					R=findRoomSomewhere(roomID,areaName,doneRooms);
					if(R==null)
						returnAnError(mob,"Reset error (no room) on line: "+s+", area="+areaName);
					else
					{
						M=getMOB("#"+mobID,R,mob,Util.copyVector(mobData),Util.copyVector(mobProgData),Util.copyVector(specialData),Util.copyVector(shopData),doneMOBS,areaFileName);
						if(M==null)
							returnAnError(mob,"Reset error (no mob) on line: "+s+", area="+areaName);
						else
							M.bringToLife(R,true);

					}
				}
				else
				if(s.startsWith("G "))
				{
					if(M==null)
						returnAnError(mob,"Reset error (no mob) on line: "+s+", area="+areaName);
					else
					{
						String itemID=Util.getBit(s,2);
						Item I=getItem("#"+itemID,mob,areaName,Util.copyVector(objectData),Util.copyVector(objProgData),doneItems);
						if(I==null)
							returnAnError(mob,"Reset error (no item) on line: "+s+", area="+areaName);
						else
						{
							I.recoverEnvStats();
							if(M instanceof ShopKeeper)
							{
								int num=Util.s_int(Util.getBit(s,3));
								if(num<0) num=100;
								((ShopKeeper)M).addStoreInventory(I,num);
								if((I instanceof Light)&&(!((ShopKeeper)M).doIHaveThisInStock("OilFlask",null)))
									((ShopKeeper)M).addStoreInventory(CMClass.getStdItem("OilFlask"),num*2);
								else
								if(((I.ID().equals("GenReadable"))
								||(I instanceof com.planet_ink.coffee_mud.interfaces.Map))
								&&(!((ShopKeeper)M).doIHaveThisInStock("Parchment",null)))
								{
									((ShopKeeper)M).setWhatIsSold(ShopKeeper.DEAL_INVENTORYONLY);
									((ShopKeeper)M).addStoreInventory(CMClass.getStdItem("Parchment"),num);
									Item journal1=CMClass.getStdItem("GenJournal");
									journal1.setName("the bug journal");
									journal1.setBaseValue(250);
									journal1.recoverEnvStats();
									journal1.text();
									((ShopKeeper)M).addStoreInventory(journal1,num);
									Item journal2=CMClass.getStdItem("GenJournal");
									journal2.setName("the adventurers journal");
									journal2.setBaseValue(250);
									journal2.recoverEnvStats();
									journal2.text();
									((ShopKeeper)M).addStoreInventory(journal2,num);
									Item journal3=CMClass.getStdItem("GenJournal");
									journal3.setName("a feature guide");
									journal3.setBaseValue(500);
									journal3.recoverEnvStats();
									journal3.text();
									((ShopKeeper)M).addStoreInventory(journal3,num);
								}
								else
								if(((ShopKeeper)M).whatIsSold()==ShopKeeper.DEAL_WEAPONS)
								{
									Item arrows=CMClass.getStdItem("GenItem");
									arrows.setSecretIdentity("arrows");
									arrows.setName("a pack of 20 arrows");
									arrows.setUsesRemaining(20);
									arrows.setBaseValue(50);
									arrows.setDescription("They are sturdy and wooden, but probably not much use without a bow.");
									arrows.setDisplayText("Some arrows have been left here.");
									arrows.recoverEnvStats();
									arrows.text();
									((ShopKeeper)M).addStoreInventory(arrows,num);
								}
							}
							else
								M.addInventory(I);
							I.recoverEnvStats();
							M.recoverCharStats();
							M.recoverEnvStats();
							M.recoverMaxState();
							M.text();
							if(I instanceof Container)
								containerHash.put(itemID,I);
						}
					}
				}
				else
				if(s.startsWith("E "))
				{
					if(M==null)
						returnAnError(mob,"Reset error (no mob) on line: "+s+", area="+areaName);
					else
					{
						String itemID=Util.getBit(s,2);
						Item I=getItem("#"+itemID,mob,areaName,Util.copyVector(objectData),Util.copyVector(objProgData),doneItems);
						if(I==null)
							returnAnError(mob,"Reset error (no item) on line: "+s+", area="+areaName);
						else
						{
							M.addInventory(I);
							I.wearIfPossible(M);
							I.recoverEnvStats();
							M.recoverCharStats();
							M.recoverEnvStats();
							M.recoverMaxState();
							M.text();
							if(I instanceof Container)
								containerHash.put(itemID,I);
						}
					}
				}
				else
				if(s.startsWith("O "))
				{
					String itemID=Util.getBit(s,2);
					String roomID=Util.getBit(s,4);
					R=findRoomSomewhere(roomID,areaName,doneRooms);
					if(R==null)
						returnAnError(mob,"Reset error (no room) on line: "+s+"/"+roomID+"/"+roomID.length()+", area="+areaName);
					else
					{
						Item I=getItem("#"+itemID,mob,areaName,Util.copyVector(objectData),Util.copyVector(objProgData),doneItems);
						if(I==null)
							returnAnError(mob,"Reset error (no item) on line: "+s+", area="+areaName);
						else
						{
							R.addItem(I);
							if(I.isGettable())
							{
								int rejuv=(int)Math.round(Util.div((long)60000,Host.TICK_TIME)*4.0);
								I.baseEnvStats().setRejuv(rejuv*I.baseEnvStats().level());
							}
							I.recoverEnvStats();
							if(I instanceof Container)
								containerHash.put(itemID,I);
						}
					}
				}
				else
				if(s.startsWith("P "))
				{
					String itemID=Util.getBit(s,2);
					String containerID=Util.getBit(s,4);
					Item I=getItem("#"+itemID,mob,areaName,Util.copyVector(objectData),Util.copyVector(objProgData),doneItems);
					if(I==null)
						returnAnError(mob,"Reset error (no item) on line: "+s+", area="+areaName);
					else
					{
						Container C=(Container)containerHash.get(containerID);
						if(C==null)
							returnAnError(mob,"Reset error (no container) on line: "+s+", area="+areaName);
						else
						if(C.owner()==null)
							returnAnError(mob,"Reset error (no container owner) on line: "+s+", area="+areaName);
						else
						if(C.owner() instanceof Room)
						{
							Room RR=(Room)C.owner();
							RR.addItem(I);
							I.setContainer(C);
							if(I.isGettable())
								I.baseEnvStats().setRejuv(1000);
							I.recoverEnvStats();
							if(I instanceof Container)
								containerHash.put(itemID,I);
						}
						else
						if(C.owner() instanceof MOB)
						{
							MOB MM=(MOB)C.owner();
							MM.addInventory(I);
							I.setContainer(C);
							M.text();
							I.recoverEnvStats();
							if(I instanceof Container)
								containerHash.put(itemID,I);
						}
					}
				}
				else
				if(s.startsWith("D "))
				{
					String roomID=Util.getBit(s,2);
					int dirCode=getBitMask(s,3);
					R=findRoomSomewhere(roomID,areaName,doneRooms);
					if(R==null)
						returnAnError(mob,"Reset error (no room) on line: "+s+", area="+areaName);
					else
					{
						switch(dirCode)
						{
						case 0: dirCode=0; break;
						case 1: dirCode=2; break;
						case 2: dirCode=1; break;
						case 3: dirCode=3; break;
						case 4: dirCode=4; break;
						case 5: dirCode=5; break;
						default:
							returnAnError(mob,"Room: "+R.roomID()+", Unknown direction code: "+dirCode+" (not so bad at this point, it was probably aborted earlier, area="+areaName);
						}
						if(dirCode<Directions.NUM_DIRECTIONS)
						{
							Exit E=R.rawExits()[dirCode];
							int lockBit=getBitMask(s,4);
							boolean HasDoor=E.hasADoor();
							boolean HasLock=E.hasALock();
							boolean DefaultsClosed=E.defaultsClosed();
							boolean DefaultsLocked=E.defaultsLocked();
							boolean Open=E.isOpen();
							boolean Locked=E.isLocked();
							switch(lockBit)
							{
							case 0:
								HasDoor=true;
								Locked=false;
								DefaultsLocked=false;
								Open=true;
								DefaultsClosed=false;
								break;
							case 1:
								HasDoor=true;
								Locked=false;
								DefaultsLocked=false;
								Open=false;
								DefaultsClosed=true;
								break;
							case 2:
								HasDoor=true;
								Locked=true;
								DefaultsLocked=true;
								Open=false;
								DefaultsClosed=true;
								break;
							default:
								returnAnError(mob,"Room: "+R.roomID()+", Unknown door code: "+lockBit+", area="+areaName);
								break;
							}
							E.setDoorsNLocks(HasDoor,Open,DefaultsClosed,HasLock,Locked,DefaultsLocked);
						}
					}
				}
				else
				if(s.startsWith("R "))
				{
					// have no idea what this is, but its not important.
				}
				else
				if(s.length()>0)
					returnAnError(mob,"Reset, unknown command: "+s+", area="+areaName);

			}
			// now fix the pet shops!
			for(Enumeration e=petShops.keys();e.hasMoreElements();)
			{
				Room storeRoom=(Room)e.nextElement();
				Room shopRoom=(Room)petShops.get(storeRoom);
				ShopKeeper shopKeeper=null;
				if(shopRoom==null)
					returnAnError(mob,"Unknown store room: "+storeRoom.roomID()+", area="+areaName);
				else
				for(int i=0;i<shopRoom.numInhabitants();i++)
				{
					MOB sk=shopRoom.fetchInhabitant(i);
					if((sk!=null)&&(sk instanceof ShopKeeper))
					{ shopKeeper=(ShopKeeper)sk; break;	}
				}
				if(shopKeeper==null)
					returnAnError(mob,"Unknown shopkeeper not in room: "+storeRoom.roomID()+", area="+areaName);
				else
				while(storeRoom.numInhabitants()>0)
				{
					shopKeeper.setWhatIsSold(ShopKeeper.DEAL_PETS);
					MOB pet=storeRoom.fetchInhabitant(0);
					if(pet!=null)
					{
						shopKeeper.addStoreInventory(pet,20);
						pet.setFollowing(null);
						pet.destroy();
					}
				}
			}
			// now fix the smurfy wells
			for(int r=0;r<newRooms.size();r++)
			{
				Room smurfRoom=(Room)newRooms.elementAt(r);
				for(int ei=0;ei<smurfRoom.numItems();ei++)
				{
					Item lookItem=smurfRoom.fetchItem(ei);
					if((lookItem!=null)&&(lookItem.displayText().length()==0))
					{
						for(int i=0;i<smurfRoom.numItems();i++)
						{
							Item I=smurfRoom.fetchItem(i);
							if((I!=null)
							&&(I.displayText().length()>0)
							&&(I.displayText().indexOf(lookItem.Name())>=0))
							{
								String description=lookItem.description();
								smurfRoom.delItem(lookItem);

								Item testItem=CMClass.getItem(I.ID());
								if((testItem!=null)&&(testItem.description().equals(I.description())))
									I.setDescription(description);
								else
									I.setDescription(I.description()+"%0D"+description);
								ei=ei-1;
								break;
							}
						}
					}
				}
			}
			mob.session().print("\nResets...");

			// try to re-link olde room links
			if(reLinkTable!=null)
				for(int r=0;r<reLinkTable.size();r++)
				{
					String link=(String)reLinkTable.elementAt(r);
					String nextLink="";
					if(r<(reLinkTable.size()-1))
						nextLink=(String)reLinkTable.elementAt(r+1);
					int s1=link.indexOf("/");
					int s2=link.lastIndexOf("/");
					String sourceRoomID=link.substring(0,s1);
					int direction=Util.s_int(link.substring(s1+1,s2));
					String destRoomID=link.substring(s2+1);
					Room sourceRoom=findRoomSomewhere(sourceRoomID,areaName,doneRooms);
					Room destRoom=findRoomSomewhere(destRoomID,areaName,doneRooms);
					if((sourceRoom==null)||(destRoom==null))
						Log.errOut("Import","Relink error: "+sourceRoomID+"="+sourceRoom+"/"+destRoomID+"="+destRoom);
					else
					{
						sourceRoom.rawDoors()[direction]=destRoom;
						if(((!doneRooms.containsValue(sourceRoom)))
						&&((nextLink.length()==0)||(!nextLink.startsWith(sourceRoomID+"/"))))
							ExternalPlay.DBUpdateExits(sourceRoom);
					}
				}
			mob.session().println("\nDone!!!!!!  A good room to look at would be "+((Room)newRooms.elementAt(0)).roomID()+"\n\r");
		}
		catch(Exception e)
		{
			Log.errOut("Import",e);
			mob.tell(e.getMessage());
			return;
		}
		}
		mob.session().print("\n\nSaving all areas imported...");
		for(Enumeration e=doneRooms.elements();e.hasMoreElements();)
		{
			Room saveRoom=(Room)e.nextElement();
			ExternalPlay.DBCreateRoom(saveRoom,CMClass.className(saveRoom));
			ExternalPlay.DBUpdateExits(saveRoom);
			ExternalPlay.DBUpdateMOBs(saveRoom);
			ExternalPlay.DBUpdateItems(saveRoom);
			Rooms.clearDebriAndRestart(saveRoom,0);
			saveRoom.recoverRoomStats();
			mob.session().print(".");
		}
		for(Enumeration e=doneRooms.elements();e.hasMoreElements();)
		{
			Room saveRoom=(Room)e.nextElement();
			saveRoom.getArea().toggleMobility(true);
		}
		for(Enumeration a=CMMap.areas();a.hasMoreElements();)
			((Area)a.nextElement()).fillInAreaRooms();
		mob.session().println("done!");
	}

	public static void export(MOB mob, Vector commands)
	{
		String commandType="";
		String fileName="";
		int fileNameCode=-1; // -1=indetermined, 0=screen, 1=file, 2=path

		commands.removeElementAt(0);
		if(commands.size()>0)
		{
			commandType=((String)commands.elementAt(0)).toUpperCase();
			commands.removeElementAt(0);
		}
		if((!commandType.equalsIgnoreCase("ROOM"))
		&&(!commandType.equalsIgnoreCase("WORLD"))
		&&(!commandType.equalsIgnoreCase("AREA")))
		{
			mob.tell("Export what?  Room, or Area?");
			return;
		}

		String subType="DATA";
		if(commands.size()>0)
		{
			String sub=((String)commands.firstElement()).toUpperCase().trim();
			if(sub.equalsIgnoreCase("ITEMS")
			||sub.equalsIgnoreCase("MOBS")
			||sub.equalsIgnoreCase("WEAPONS")
			||sub.equalsIgnoreCase("ARMOR"))
			{
				subType=sub;
				commands.removeElementAt(0);
			}
			else
			if(sub.equalsIgnoreCase("data"))
				commands.removeElementAt(0);

			if(commands.size()==0)
			{
				mob.tell("You must specify a file name to create, or enter 'SCREEN' to have a screen dump.");
				return;
			}
			fileName=Util.combine(commands,0);
			if(fileName.equalsIgnoreCase("screen"))
				fileNameCode=0;
			else
			{
				if(!mob.isASysOp(null))
				{
					mob.tell("Only Archons may export to a file.");
					return;
				}
				File F=new File(fileName);
				if(F.isDirectory())
					fileNameCode=2;
			}
			if(fileNameCode<0)
				fileNameCode=1;
		}
		else
		{
			mob.tell("You must specify a file name to create, or enter 'SCREEN' to have a screen dump.");
			return;
		}

		String xml="";
		if(subType.equalsIgnoreCase("DATA"))
		{
			if(commandType.equalsIgnoreCase("ROOM"))
			{
				xml=com.planet_ink.coffee_mud.common.Generic.getRoomXML(mob.location(),true).toString();
				if(fileNameCode==2) fileName=fileName+File.separatorChar+"room";
			}
			else
			if(commandType.equalsIgnoreCase("AREA"))
			{
				if(mob.session()!=null)
					mob.session().rawPrint("Reading area '"+mob.location().getArea().Name()+"'...");
				xml=com.planet_ink.coffee_mud.common.Generic.getAreaXML(mob.location().getArea(),mob.session(),true).toString();
				if(fileNameCode==2){
					if(mob.location().getArea().getArchivePath().length()>0)
						fileName=fileName+File.separatorChar+mob.location().getArea().getArchivePath();
					else
						fileName=fileName+File.separatorChar+mob.location().getArea().Name();
				}
				if(mob.session()!=null)
					mob.session().rawPrintln("!");
			}
			else
			{
				StringBuffer buf=new StringBuffer("");
				if(fileNameCode!=2) buf.append("<AREAS>");
				for(Enumeration a=CMMap.areas();a.hasMoreElements();)
				{
					Area A=(Area)a.nextElement();
					if(A!=null)
					{
						if(mob.session()!=null)
							mob.session().rawPrint("Reading area '"+A.name()+"'...");
						buf.append(com.planet_ink.coffee_mud.common.Generic.getAreaXML(A,mob.session(),true).toString());
						if(mob.session()!=null)
							mob.session().rawPrintln("!");
						if(fileNameCode==2)
						{
							String name=fileName;
							if(A.getArchivePath().length()>0)
								name=fileName+File.separatorChar+A.getArchivePath();
							else
								name=fileName+File.separatorChar+A.Name();
							reallyExport(mob,name,buf.toString());
							buf=new StringBuffer("");
						}
					}
				}
				if(fileNameCode!=2) xml=buf.toString()+"</AREAS>";
			}
		}
		else
		if(subType.equalsIgnoreCase("MOBS"))
		{
			if(fileNameCode==2) fileName=fileName+File.separatorChar+"mobs";
			Hashtable found=new Hashtable();
			if(commandType.equalsIgnoreCase("ROOM"))
				xml="<MOBS>"+com.planet_ink.coffee_mud.common.Generic.getRoomMobs(mob.location(),found).toString()+"</MOBS>";
			else
			if(commandType.equalsIgnoreCase("AREA"))
			{
				if(mob.session()!=null)
					mob.session().rawPrint("Reading area mobs '"+mob.location().getArea().Name()+"'...");
				StringBuffer buf=new StringBuffer("<MOBS>");
				for(Enumeration r=mob.location().getArea().getMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(mob.session()!=null) mob.session().rawPrint(".");
					buf.append(com.planet_ink.coffee_mud.common.Generic.getRoomMobs(R,found).toString());
				}
				xml=buf.toString()+"</MOBS>";
				if(mob.session()!=null)
					mob.session().rawPrintln("!");
			}
			else
			{
				if(mob.session()!=null)
					mob.session().rawPrint("Reading world mobs ...");
				StringBuffer buf=new StringBuffer("<MOBS>");
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(mob.session()!=null) mob.session().rawPrint(".");
					buf.append(com.planet_ink.coffee_mud.common.Generic.getRoomMobs(R,found).toString());
				}
				xml=buf.toString()+"</MOBS>";
				if(mob.session()!=null)
					mob.session().rawPrintln("!");
			}
		}
		else
		if((subType.equalsIgnoreCase("ITEMS"))
		||(subType.equalsIgnoreCase("WEAPONS"))
		||(subType.equalsIgnoreCase("ARMOR")))
		{
			int type=0;
			if(subType.equalsIgnoreCase("WEAPONS"))
			{
				if(fileNameCode==2)
					fileName=fileName+File.separatorChar+"weapons";
				type=1;
			}
			else
			if(subType.equalsIgnoreCase("ARMOR"))
			{
				if(fileNameCode==2)
					fileName=fileName+File.separatorChar+"armor";
				type=2;
			}
			else
			if(fileNameCode==2)
			{
				fileName=fileName+File.separatorChar+"items";
			}

			Hashtable found=new Hashtable();
			if(commandType.equalsIgnoreCase("ROOM"))
				xml="<ITEMS>"+com.planet_ink.coffee_mud.common.Generic.getRoomItems(mob.location(),found,type).toString()+"</ITEMS>";
			else
			if(commandType.equalsIgnoreCase("AREA"))
			{
				if(mob.session()!=null)
					mob.session().rawPrint("Reading area "+subType.toLowerCase()+" '"+mob.location().getArea().Name()+"'...");
				StringBuffer buf=new StringBuffer("<ITEMS>");
				for(Enumeration r=mob.location().getArea().getMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(mob.session()!=null) mob.session().rawPrint(".");
					buf.append(com.planet_ink.coffee_mud.common.Generic.getRoomItems(R,found,type).toString());
				}
				xml=buf.toString()+"</ITEMS>";
				if(mob.session()!=null)
					mob.session().rawPrintln("!");
			}
			else
			{
				if(mob.session()!=null)
					mob.session().rawPrint("Reading world "+subType.toLowerCase()+" ...");
				StringBuffer buf=new StringBuffer("<ITEMS>");
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(mob.session()!=null) mob.session().rawPrint(".");
					buf.append(com.planet_ink.coffee_mud.common.Generic.getRoomItems(R,found,type).toString());
				}
				xml=buf.toString()+"</ITEMS>";
				if(mob.session()!=null)
					mob.session().rawPrintln("!");
			}
		}
		reallyExport(mob,fileName,xml);
	}

	public static void reallyExport(MOB mob, String fileName, String xml)
	{
		if(fileName==null) return;
		if(mob==null) return;
		if(xml==null) return;
		if(xml.length()==0) return;

		if(fileName.equalsIgnoreCase("SCREEN"))
		{
			mob.tell("Here it is:\n\r\n\r");
			xml=xml.replace('\n',' ');
			xml=xml.replace('\r',' ');
			if(mob.session()!=null)
				mob.session().rawPrintln(xml+"\n\r\n\r");
		}
		else
		{
			mob.tell("Exporting room(s)...");
			try
			{
				if(fileName.indexOf(".")<0)
					fileName=fileName+".cmare";
				File f=new File(fileName);
				FileOutputStream out=new FileOutputStream(f);
				out.write(xml.getBytes());
				out.close();
				mob.tell("File '"+fileName+"' written.");
			}
			catch(java.io.IOException e)
			{
				mob.tell("A file error occurred: "+e.getMessage());
			}
		}
	}
	
	
	public static String getStat(Environmental E, String stat)
	{
		if((stat!=null)&&(stat.length()>0)&&(stat.equalsIgnoreCase("REJUV")))
		{
			if(E.baseEnvStats().rejuv()==Integer.MAX_VALUE)
				return "0";
			else
				return ""+E.baseEnvStats().rejuv();
		}
		else
			return E.getStat(stat);
	}

	public static void setStat(Environmental E, String stat, String value)
	{
		if((stat!=null)&&(stat.length()>0)&&(stat.equalsIgnoreCase("REJUV")))
			E.baseEnvStats().setRejuv(Util.s_int(value));
		else
			E.setStat(stat,value);
	}

	public static void merge(MOB mob, Vector commands)
	{
		boolean noisy=false;
		Vector placesToDo=new Vector();
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
			mob.tell("Merge what file?");
			return;
		}
		if(mob.isMonster())
		{
			mob.tell("No can do.");
			return;
		}
		boolean prompt=true;
		if((commands.size()>0)&&
		   ((String)commands.elementAt(0)).equalsIgnoreCase("noprompt"))
		{
			commands.removeElementAt(0);
			prompt=false;
		}
		if((commands.size()>0)&&
		   ((String)commands.elementAt(0)).equalsIgnoreCase("?"))
		{
			StringBuffer allFieldsMsg=new StringBuffer("");
			Vector allKnownFields=new Vector();
			for(Enumeration m=CMClass.mobTypes();m.hasMoreElements();)
			{
				MOB M=(MOB)m.nextElement();
				String[] fields=M.getStatCodes();
				for(int x=0;x<fields.length;x++)
					if(!allKnownFields.contains(fields[x]))
					{
						allKnownFields.addElement(fields[x]);
						allFieldsMsg.append(fields[x]+" ");
					}
			}
			for(Enumeration i=CMClass.items();i.hasMoreElements();)
			{
				Item I=(Item)i.nextElement();
				String[] fields=I.getStatCodes();
				for(int x=0;x<fields.length;x++)
					if(!allKnownFields.contains(fields[x]))
					{
						allKnownFields.addElement(fields[x]);
						allFieldsMsg.append(fields[x]+" ");
					}
			}
			mob.tell("Valid field names are "+allFieldsMsg.toString());
			return;
		}
		if((commands.size()>0)&&
		   ((String)commands.elementAt(0)).equalsIgnoreCase("room"))
		{
			commands.removeElementAt(0);
			placesToDo.addElement(mob.location());
		}
		if((commands.size()>0)&&
		   ((String)commands.elementAt(0)).equalsIgnoreCase("area"))
		{
			commands.removeElementAt(0);
			placesToDo.addElement(mob.location().getArea());
		}
		if((commands.size()>0)&&
		   ((String)commands.elementAt(0)).equalsIgnoreCase("world"))
		{
			commands.removeElementAt(0);
			placesToDo=new Vector();
		}
		if(commands.size()==0)
		{
			mob.tell("Merge what file?");
			return;
		}
		String filename=(String)commands.lastElement();
		commands.remove(filename);
		StringBuffer buf=Resources.getFile(filename);
		if(buf==null)
		{
			mob.tell("File not found at: '"+filename+"'!");
			return;
		}

		Vector changes=new Vector();
		Vector onfields=new Vector();
		Vector ignore=new Vector();
		Vector use=null;
		Vector allKnownFields=new Vector();
		Vector things=new Vector();
		boolean aremobs=false;
		if((buf.length()>20)&&(buf.substring(0,20).indexOf("<MOBS>")>=0))
		{
			if(mob.session()!=null)
				mob.session().rawPrint("Unpacking mobs from file: '"+filename+"'...");
			String error=com.planet_ink.coffee_mud.common.Generic.addMOBsFromXML(buf.toString(),things,mob.session());
			if(mob.session()!=null)	mob.session().rawPrintln("!");
			if(error.length()>0)
			{
				mob.tell("An error occurred on merge: "+error);
				mob.tell("Please correct the problem and try the import again.");
				return;
			}
			aremobs=true;
		}
		else
		if((buf.length()>20)&&(buf.substring(0,20).indexOf("<ITEMS>")>=0))
		{
			if(mob.session()!=null)
				mob.session().rawPrint("Unpacking items from file: '"+filename+"'...");
			String error=com.planet_ink.coffee_mud.common.Generic.addItemsFromXML(buf.toString(),things,mob.session());
			if(mob.session()!=null)	mob.session().rawPrintln("!");
			if(error.length()>0)
			{
				mob.tell("An error occurred on merge: "+error);
				mob.tell("Please correct the problem and try the import again.");
				return;
			}
		}
		else
		{
			mob.tell("Files of this type are not yet supported by MERGE.  You must merge an ITEMS or MOBS file at this time.");
			return;
		}
		if(things.size()==0)
		{
			mob.tell("Nothing was found in the file to merge!");
			return;
		}
		StringBuffer allFieldsMsg=new StringBuffer("");
		if(aremobs)
			for(Enumeration m=CMClass.mobTypes();m.hasMoreElements();)
			{
				MOB M=(MOB)m.nextElement();
				String[] fields=M.getStatCodes();
				for(int x=0;x<fields.length;x++)
					if(!allKnownFields.contains(fields[x]))
					{
						allKnownFields.addElement(fields[x]);
						allFieldsMsg.append(fields[x]+" ");
					}
			}
		else
		{
			for(Enumeration i=CMClass.items();i.hasMoreElements();)
			{
				Item I=(Item)i.nextElement();
				String[] fields=I.getStatCodes();
				for(int x=0;x<fields.length;x++)
					if(!allKnownFields.contains(fields[x]))
					{
						allKnownFields.addElement(fields[x]);
						allFieldsMsg.append(fields[x]+" ");
					}
			}
			for(Enumeration i=CMClass.weapons();i.hasMoreElements();)
			{
				Item I=(Item)i.nextElement();
				String[] fields=I.getStatCodes();
				for(int x=0;x<fields.length;x++)
					if(!allKnownFields.contains(fields[x]))
					{
						allKnownFields.addElement(fields[x]);
						allFieldsMsg.append(fields[x]+" ");
					}
			}
			for(Enumeration i=CMClass.armor();i.hasMoreElements();)
			{
				Item I=(Item)i.nextElement();
				String[] fields=I.getStatCodes();
				for(int x=0;x<fields.length;x++)
					if(!allKnownFields.contains(fields[x]))
					{
						allKnownFields.addElement(fields[x]);
						allFieldsMsg.append(fields[x]+" ");
					}
			}
		}
		
		allKnownFields.addElement("REJUV");
		allFieldsMsg.append("REJUV ");
		
		for(int i=0;i<commands.size();i++)
		{
			String str=((String)commands.elementAt(i)).toUpperCase();
			if(str.startsWith("CHANGE="))
			{
				use=changes;
				str=str.substring(7).trim();
			}
			if(str.startsWith("ON="))
			{
				use=onfields;
				str=str.substring(3).trim();
			}
			if(str.startsWith("IGNORE="))
			{
				use=ignore;
				str=str.substring(7).trim();
			}
			int x=str.indexOf(",");
			while(x>=0)
			{
				String s=str.substring(0,x).trim();
				if(s.length()>0)
				{
					if(use==null)
					{
						mob.tell("'"+str+"' is an unknown parameter!");
						return;
					}
					if(allKnownFields.contains(s))
						use.addElement(s);
					else
					{
						mob.tell("'"+s+"' is an unknown field name.  Valid fields include: "+allFieldsMsg.toString());
						return;
					}
				}
				str=str.substring(x+1).trim();
				x=str.indexOf(",");
			}
			if(str.length()>0)
			{
				if(use==null)
				{
					mob.tell("'"+str+"' is an unknown parameter!");
					return;
				}
				if(allKnownFields.contains(str))
					use.addElement(str);
				else
				{
					mob.tell("'"+str+"' is an unknown field name.  Valid fields include: "+allFieldsMsg.toString());
					return;
				}
			}
		}
		if((onfields.size()==0)&&(ignore.size()==0)&&(changes.size()==0))
		{
			mob.tell("You must specify either an ON, CHANGES, or IGNORE parameter for valid matches to be made.");
			return;
		}
		if(placesToDo.size()==0)
		for(Enumeration a=CMMap.areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			placesToDo.addElement(A);
		}
		if(placesToDo.size()==0)
		{
			mob.tell("There are no rooms to merge into!");
			return;
		}
		for(int i=placesToDo.size()-1;i>=0;i--)
		{
			if(placesToDo.elementAt(i) instanceof Area)
			{
				Area A=(Area)placesToDo.elementAt(i);
				placesToDo.removeElement(A);
				for(Enumeration r=A.getMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					placesToDo.addElement(R);
				}
			}
			else
			if(placesToDo.elementAt(i) instanceof Room)
				if(mob.session()!=null)	mob.session().rawPrint(".");
			else
				return;
		}
		// now do the merge...
		if(mob.session()!=null)
			mob.session().rawPrint("Merging and saving...");
		if(noisy) mob.tell("Rooms to do: "+placesToDo.size());
		if(noisy) mob.tell("Things loaded: "+things.size());
		if(noisy) mob.tell("On fields="+Util.toStringList(onfields));
		if(noisy) mob.tell("Ignore fields="+Util.toStringList(ignore));
		if(noisy) mob.tell("Change fields="+Util.toStringList(changes));
		Log.sysOut("Import",mob.Name()+" merge '"+filename+"'.");
		for(int r=0;r<placesToDo.size();r++)
		{
			Room R=(Room)placesToDo.elementAt(r);
			boolean oldMobility=R.getArea().getMobility();
			R.getArea().toggleMobility(false);
			ExternalPlay.resetRoom(R);
			boolean savemobs=false;
			boolean saveitems=false;
			if(aremobs)
			{
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if((M!=null)&&(M.isEligibleMonster()))
						if(tryMerge(mob,R,M,things,changes,onfields,ignore,noisy))
							savemobs=true;
				}
			}
			else
			{
				for(int i=0;i<R.numItems();i++)
				{
					Item I=R.fetchItem(i);
					if((I!=null)&&(tryMerge(mob,R,I,things,changes,onfields,ignore,noisy)))
						saveitems=true;
				}
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if((M!=null)&&(M.isEligibleMonster()))
					{
						for(int i=0;i<M.inventorySize();i++)
						{
							Item I=M.fetchInventory(i);
							if((I!=null)&&(tryMerge(mob,R,I,things,changes,onfields,ignore,noisy)))
								savemobs=true;
						}
						if(CoffeeUtensils.getShopKeeper(M)!=null)
						{
							Vector V=CoffeeUtensils.getShopKeeper(M).getUniqueStoreInventory();
							for(int i=0;i<V.size();i++)
							{
								if(V.elementAt(i) instanceof Item)
								{
									Item I=(Item)V.elementAt(i);
									if((I!=null)&&(tryMerge(mob,R,I,things,changes,onfields,ignore,noisy)))
										savemobs=true;
								}
							}
						}
					}
				}
			}
			if(saveitems) ExternalPlay.DBUpdateItems(R);
			if(savemobs) ExternalPlay.DBUpdateMOBs(R);
			if(mob.session()!=null)	mob.session().rawPrint(".");
			R.getArea().toggleMobility(oldMobility);
		}

		if(mob.session()!=null)	mob.session().rawPrintln("!\n\rDone!");
		for(int i=0;i<placesToDo.size();i++)
			((Room)placesToDo.elementAt(i)).getArea().toggleMobility(true);
	}

	private static boolean tryMerge(MOB mob,
									Room room,
									Environmental E,
									Vector things,
									Vector changes,
									Vector onfields,
									Vector ignore,
									boolean noisy)
	{
		boolean didAnything=false;
		Vector efields=new Vector();
		Vector allMyFields=new Vector();
		String[] EFIELDS=E.getStatCodes();
		for(int i=0;i<EFIELDS.length;i++)
			if(!efields.contains(EFIELDS[i]))
				efields.addElement(EFIELDS[i]);
		efields.addElement("REJUV");
		allMyFields=(Vector)efields.clone();
		for(int v=0;v<ignore.size();v++)
			if(efields.contains(ignore.elementAt(v)))
				efields.removeElement(ignore.elementAt(v));
		for(int v=0;v<changes.size();v++)
			if(efields.contains(changes.elementAt(v)))
				efields.removeElement(changes.elementAt(v));
		if(noisy) mob.tell("AllMy-"+Util.toStringList(allMyFields));
		if(noisy) mob.tell("efields-"+Util.toStringList(efields));
		for(int t=0;t<things.size();t++)
		{
			Environmental E2=(Environmental)things.elementAt(t);
			if(noisy) mob.tell(E.name()+"/"+E2.name()+"/"+CMClass.className(E)+"/"+CMClass.className(E2));
			if(CMClass.className(E).equals(CMClass.className(E2)))
			{
				Vector fieldsToCheck=null;
				if(onfields.size()>0)
				{
					fieldsToCheck=new Vector();
					for(int v=0;v<onfields.size();v++)
						if(efields.contains(onfields.elementAt(v)))
							fieldsToCheck.addElement(onfields.elementAt(v));
				}
				else
					fieldsToCheck=(Vector)efields.clone();

				boolean checkedOut=fieldsToCheck.size()>0;
				if(noisy) mob.tell("fieldsToCheck-"+Util.toStringList(fieldsToCheck));
				if(checkedOut)
				for(int i=0;i<fieldsToCheck.size();i++)
				{
					String field=(String)fieldsToCheck.elementAt(i);
					if(noisy) mob.tell(field+"/"+getStat(E,field)+"/"+getStat(E2,field)+"/"+getStat(E,field).equals(getStat(E2,field)));
					if(!getStat(E,field).equals(getStat(E2,field)))
					{ checkedOut=false; break;}
				}
				if(checkedOut)
				{
					Vector fieldsToChange=null;
					if(changes.size()==0)
						fieldsToChange=(Vector)allMyFields.clone();
					else
					{
						fieldsToChange=new Vector();
						for(int v=0;v<changes.size();v++)
							if(allMyFields.contains(changes.elementAt(v)))
								fieldsToChange.addElement(changes.elementAt(v));
					}
					if(noisy) mob.tell("fieldsToChange-"+Util.toStringList(fieldsToChange));
					for(int i=0;i<fieldsToChange.size();i++)
					{
						String field=(String)fieldsToChange.elementAt(i);
						if(noisy) mob.tell(E.name()+" wants to change "+field+" value "+getStat(E,field)+" to "+getStat(E2,field)+"/"+(!getStat(E,field).equals(getStat(E2,field))));
						if(!getStat(E,field).equals(getStat(E2,field)))
						{
							setStat(E,field,getStat(E2,field));
							Log.sysOut("Merge","The "+Util.capitalize(field)+" field on "+E.Name()+" in "+room.roomID()+" was changed to "+getStat(E2,field)+".");
							didAnything=true;
						}
					}
				}
			}
		}
		if(didAnything)
		{
			E.recoverEnvStats();
			if(E instanceof MOB)
			{
				((MOB)E).recoverCharStats();
				((MOB)E).recoverMaxState();
			}
			E.text();
		}
		return didAnything;
	}
}

