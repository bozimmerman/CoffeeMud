package com.planet_ink.coffee_mud.commands.base.sysop;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.commands.*;
import java.io.*;
import java.util.*;


public class Import
{
	Rooms myRooms=new Rooms();
	private String getAreaName(Vector V)
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

	private String removeAtAts(String str)
	{
		int x=str.indexOf("@@");
		while(x>=0)
		{
			str=str.substring(0,x)+str.substring(x+3);
			x=str.indexOf("@@");
		}
		return str;
	}

	private String nextLine(Vector V)
	{
		if(V.size()==0) return "";
		return (String)V.elementAt(0);
	}
	private String eatLine(Vector V)
	{
		if(V.size()==0) return "";
		String s=(String)V.elementAt(0);
		V.removeElementAt(0);
		return s;
	}
	private String eatNextLine(Vector V)
	{
		String s="";
		while((s.trim().length()==0)&&(V.size()>0))
			s=eatLine(V);
		return s;
	}

	private Room changeRoomClass(Room R, String newClass)
	{
		Room R2=CMClass.getLocale(newClass);
		if(R2==null)
		{
			Log.errOut("Import","Cannot find room class "+newClass+".");
			return R;
		}
		R2=(Room)R2.newInstance();
		R2.setID(R.ID());
		R2.setArea(R.getArea());
		R2.setDescription(R.description());
		R2.setDisplayText(R.displayText());
		R2.setName(R.name());
		R2.setBaseEnvStats(R.baseEnvStats());
		R2.setMiscText(R.text());
		return R2;
	}

	private int getBitMask(String str, int which)
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
					num=num|(Util.pow(2,((int)s.charAt(z))-((int)'A')));
				else
				if(Character.isLowerCase(s.charAt(z)))
					num=num|(Util.pow(2,26+(((int)s.charAt(z))-((int)'a'))));

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

	private String eatLineSquiggle(Vector V)
	{
		if(V.size()==0) return "";
		String s=eatLine(V);
		while(s.indexOf("~")<0)
		{
			String l=eatLine(V);
			if(l.startsWith("  "))
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
		return s;
	}

	private boolean hasReadableContent(String objectName)
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

	private String fixReadableContent(String text)
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
	
	private boolean returnAnError(MOB mob, String str)
	{
		Log.errOut("Import",str);
		mob.tell(str);
		return false;
	}

	private String getSpell(String word, int i)
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
		case 11: return "Spell_DistantVision"; // control weather
		case 12: return "Prayer_CreateFood";
		case 13: return "Prayer_CreateWater";
		case 14: return "Prayer_CureBlindness";
		case 15: return "Prayer_CureCritical";
		case 16: return "Prayer_CureLight";
		case 17: return "Prayer_Curse";
		case 18: return "Prayer_DetectEvil";
		case 19: return "Spell_DetectInvisible";
		case 20: return "Spell_DetectMagic";
		case 21: return "Thief_DetectTraps"; // detect poison actually
		case 22: return "Prayer_DispelEvil";
		case 23: return "Spell_Earthquake";
		case 24: return "Spell_EnchantWeapon";
		case 25: return "Spell_Drain";
		case 26: return "Spell_Fireball";
		case 27: return "Prayer_Harm";
		case 28: return "Prayer_Heal";
		case 29: return "Spell_Invisibility";
		case 30: return "Spell_Lightning";
		case 31: return "Spell_LocateObject";
		case 32: return "Spell_MagicMissile";
		case 33: return "Thief_Poison";
		case 34: return "Prayer_ProtEvil";
		case 35: return "Prayer_Bless"; // actualy remove curse...
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
		case 54: return "Spell_AnimateDead";
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
		case 70: return "Spell_Dragonfire"; // acid blast
		case 71: return "Spell_Portal"; // actually mass teleport
		case 72: return "Spell_FaerieFog";
		case 73: return "Spell_Frost"; // ice storm
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
		case 122: return "Spell_SummonMonster"; // summon elemental
		case 201: return "Firebreath";
		case 203: return "Gasbreath";
		case 202: return "Frostbreath";
		case 200: return "Acidbreath";
		case 204: return "Lighteningbreath";
		default:
			Log.sysOut("Unknown spell num: "+i);
			break;
		}
		return "";
	}

	private void readBlocks(Vector buf,
						   Vector areaData,
						   Vector roomData,
						   Vector mobData,
						   Vector resetData,
						   Vector objectData,
						   Vector mobProgData,
						   Vector objProgData,
						   Vector shopData,
						   Vector specialData)
	{
		Vector helpsToEat=new Vector();

		Vector wasUsingThisOne=null;
		Vector useThisOne=null;
		while(buf.size()>0)
		{
			String s=((String)buf.elementAt(0)).toUpperCase().trim();
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
				if((Util.s_int(s)>0)&&(wasUsingThisOne!=null))
				{
					Vector V=new Vector();
					wasUsingThisOne.addElement(V);
					useThisOne=V;
				}
				else
				if(s.equals("0")||s.equals("$")||s.equals("O"))
				{
					// it's all good.
				}
				else
				{
					useThisOne=null;
					Log.sysOut("Import","Unknown line: "+s);
				}
			}
			if(useThisOne!=null)
			{
				useThisOne.addElement(Util.safetyFilter(removeAtAts((String)buf.elementAt(0))));
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

	private int getDRoll(String str)
	{
		int i=str.indexOf("d");
		if(i<0) return 0;
		int x=Util.s_int(str.substring(0,i).trim());
		str=str.substring(i+1).trim();

		i=str.indexOf("+");
		if(i<0)
			i=str.indexOf("-");
		else
			i++;
		if(i<0)
			i=str.length()-1;

		int y=Util.s_int(str.substring(0,i).trim());
		int z=Util.s_int(str.substring(i));
		return (x*y)+z;
	}

	private void doWeapon(Weapon I, String name, int val1, String str1, int val2, int val3, int val4, String str4)
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
		case 0: ((Weapon)I).setWeaponClassification(Weapon.CLASS_RANGED); break;
		case 1: ((Weapon)I).setWeaponClassification(Weapon.CLASS_SWORD); break;
		case 2: ((Weapon)I).setWeaponClassification(Weapon.CLASS_EDGED); break;
		case 3: ((Weapon)I).setWeaponClassification(Weapon.CLASS_POLEARM); break;
		case 4: ((Weapon)I).setWeaponClassification(Weapon.CLASS_BLUNT); break;
		case 5: ((Weapon)I).setWeaponClassification(Weapon.CLASS_AXE); break;
		case 6: ((Weapon)I).setWeaponClassification(Weapon.CLASS_FLAILED); break;
		case 7: ((Weapon)I).setWeaponClassification(Weapon.CLASS_FLAILED); break;
		case 8: ((Weapon)I).setWeaponClassification(Weapon.CLASS_POLEARM); break;
		case 9: ((Weapon)I).setWeaponClassification(Weapon.CLASS_DAGGER); break;
		case 10: ((Weapon)I).setWeaponClassification(Weapon.CLASS_STAFF); break;
		case 11: ((Weapon)I).setWeaponClassification(Weapon.CLASS_HAMMER); break;
		}
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

	private MOB getMOB(String OfThisID,
					  Room putInRoom,
					  String areaName,
					  MOB mob,
					  Vector mobData,
					  Vector mobProgData,
					  Vector specialData,
					  Vector shopData)
	{
		for(int m=0;m<mobData.size();m++)
		{
			Vector objV=null;
			if(mobData.elementAt(m) instanceof Vector)
				objV=(Vector)mobData.elementAt(m);
			else
			if(mobData.elementAt(m) instanceof String)
			{
				String s=(String)mobData.elementAt(m);
				if(!s.toUpperCase().trim().startsWith("#MOB"))
					returnAnError(mob,"Eating immaterial line: "+mobData.elementAt(m));
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
					int whatIsell=ShopKeeper.ONLYBASEINVENTORY;
					if((i1>4)&&(i1<8)&&(i2>4)&&(i2<8)&&(i3>4)&&(i3<8))
						whatIsell=ShopKeeper.WEAPONS;
					else
					if((((i1>1)&&(i1<5))||(i1==10)||(i1==26))
					&&(((i2>1)&&(i2<5))||(i2==10)||(i2==26))
					&&(((i3>1)&&(i3<5))||(i3==10)||(i3==26)))
						whatIsell=ShopKeeper.MAGIC;
					else
					if(((i1==9)||(i1==0))&&((i2==9)||(i2==0))&&((i3==9)||(i3==0)))
						whatIsell=ShopKeeper.ARMOR;
					else
					if(mobName.toUpperCase().indexOf("LEATHER")>=0)
						whatIsell=ShopKeeper.LEATHER;
					else
					if((mobName.toUpperCase().indexOf("PET ")>=0)||(mobName.toUpperCase().indexOf("PETS ")>=0))
						whatIsell=ShopKeeper.PETS;
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
			if(!Util.isSet(actFlag,6))
				behavior.setParms("WANDER");
			if(!Util.isSet(actFlag,1))
				M.addBehavior(behavior);
			if(Util.isSet(actFlag,2))
				M.addBehavior(CMClass.getBehavior("Scavenger"));
			if(Util.isSet(actFlag,4))
				M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|Sense.IS_INVISIBLE);
			if(Util.isSet(actFlag,5)&&Util.isSet(actFlag,1))
				M.addBehavior(CMClass.getBehavior("Aggressive"));
			M.setWimpHitPoint(0);
			if(Util.isSet(actFlag,7)) // this needs to be adjusted further down!
				M.setWimpHitPoint(2);
			if(Util.isSet(actFlag,8))
				M.addNonUninvokableAffect(CMClass.getAbility("Prop_SafePet"));

			if(Util.isSet(actFlag,9))
				M.addNonUninvokableAffect(CMClass.getAbility("Prop_Trainer"));
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
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|Sense.CAN_SEE);
			if(Util.isSet(affFlag,1))
				M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|Sense.IS_INVISIBLE);
			if(Util.isSet(affFlag,2))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|Sense.CAN_SEE_EVIL);
			if(Util.isSet(affFlag,3))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|Sense.CAN_SEE_INVISIBLE);
			if(Util.isSet(affFlag,4))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|Sense.CAN_SEE_BONUS);
			if(Util.isSet(affFlag,5))
			{
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|Sense.CAN_SEE_HIDDEN);
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|Sense.CAN_SEE_SNEAKERS);
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
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|Sense.CAN_SEE_INFRARED);
			if(Util.isSet(affFlag,10))
				M.addNonUninvokableAffect(CMClass.getAbility("Prayer_Curse"));
			if(Util.isSet(affFlag,11))
			{
				for(int i=0;i<CMClass.abilities.size();i++)
				{
					Ability A=(Ability)CMClass.abilities.elementAt(i);
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
				M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|Sense.IS_HIDDEN);

			if(Util.isSet(affFlag,17))
				M.addNonUninvokableAffect(CMClass.getAbility("Spell_Sleep"));

			if(Util.isSet(affFlag,18))
				M.addNonUninvokableAffect(CMClass.getAbility("Spell_Charm"));

			//if(Util.isSet(affFlag,20)) no door walking through abilities available
			//	M.addNonUninvokableAffect(new Spell_Charm());

			if(Util.isSet(affFlag,21))
				M.addNonUninvokableAffect(CMClass.getAbility("Spell_Haste"));

			//if(Util.isSet(affFlag,22)) no effect anyway
			//	M.addNonUninvokableAffect(new Prayer_Calm());

			//if(Util.isSet(affFlag,23))  dumb
			//	M.addNonUninvokableAffect(new Prayer_Plague());

			if(Util.isSet(affFlag,24))
				M.addNonUninvokableAffect(CMClass.getAbility("Prop_SafePet"));

			if(Util.isSet(affFlag,25))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|Sense.CAN_SEE_DARK);

			if(Util.isSet(affFlag,26))
				M.addNonUninvokableAffect(CMClass.getAbility("Fighter_Berzerk"));

			if(Util.isSet(affFlag,27))
				M.addNonUninvokableAffect(CMClass.getAbility("Regeneration"));

			if(Util.isSet(affFlag,28))
				M.baseEnvStats().setSensesMask(M.baseEnvStats().sensesMask()|Sense.CAN_SEE_GOOD);

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
					baseHP=(int)Math.round(Util.div(getDRoll(Util.getBit(codeStr2,2)),M.baseEnvStats().level()));
				else
					baseHP=(int)Math.round(Util.div(getDRoll(Util.getBit(codeStr2,3)),M.baseEnvStats().level()));
				if(baseHP>11)
					M.baseEnvStats().setAbility(baseHP-11);
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
				M.setMoney((10*M.baseEnvStats().level())+10);
			M.baseEnvStats().setWeight(50);

			switch(positionCode)
			{
			case 1:
			case 2:
			case 3:
			case 4:
				M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|Sense.IS_SLEEPING);
				break;
			case 5:
				M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|Sense.IS_SITTING);
				break;
			case 6:
				M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()|Sense.IS_SITTING);
				break;
			}

			M.baseCharStats().setGender('M');
			switch(sexCode)
			{
			case 2: M.baseCharStats().setGender('F'); break;
			case 3: M.baseCharStats().setGender('N'); break;
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
					M.addBehavior(CMClass.getBehavior("ClanHelper"));
				if(Util.isSet(off,18))
					M.addBehavior(CMClass.getBehavior("PlayerHelper"));
				if(Util.isSet(off,19))
				{
					Behavior guardian=CMClass.getBehavior("GoodGuardian");
					for(int b=M.numBehaviors()-1;b>=0;b--)
					{
						Behavior B=M.fetchBehavior(b);
						if((B!=null)&&(B.grantsMobility()))
						{
							if(guardian.ID().equals("GoodGuardian"))
								guardian=CMClass.getBehavior("MobileGoodGuardian");
							if(B.getParms().length()>0)
								guardian.setParms(B.getParms());
							M.delBehavior(B);
						}
					}
					M.addBehavior(guardian);
				}
				if(Util.isSet(off,20))
					M.addBehavior(CMClass.getBehavior("BrotherHelper"));
				//if(Util.isSet(off,21)) is missing
				//if(Util.isSet(off,22)) sweeps are not supported

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
				//if(Util.isSet(res,14)) nothing this specific (magic missile)
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
						scriptStuff+=s.substring(1).trim()+"|";
						s=nextLine(objV);
						while(s.indexOf("~")<0)
						{
							scriptStuff+=s.trim()+"|";
							eatLine(objV);
							s=nextLine(objV);
						}
						scriptStuff+=eatLineSquiggle(objV).trim()+"~";
					}
				}
				else
					eatNextLine(objV);
			}
			if(scriptStuff.length()>0)
			{
				Behavior S=CMClass.getBehavior("Scriptable");
				S.setParms(scriptStuff);
				M.addBehavior(S);
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
					{
						//
					}
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
					{
						//
					}
					else
					if(mobprg.equals("GATEGRD.PRG"))
					{
					}
					else
					if(mobprg.equals("GATEGRD2.PRG"))
					{
					}
					else
					if(mobprg.equals("CRIER.PRG"))
					{
					}
					else
						returnAnError(mob,"Unknown MobPrg: "+mobprg);
				}
				else
				if((s.startsWith("#M"))||(s.startsWith("S")))
				{
				}
				else
				if(s.trim().length()>0)
					returnAnError(mob,"MobPrg line: "+s);
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
						M.addBehavior(CMClass.getBehavior("Fighterness"));
					else
					if(special.equals("SPEC_CAST_UNDEAD"))
					{
						if((M.baseEnvStats().disposition()&Sense.IS_INFRARED)==Sense.IS_INFRARED)
							M.baseEnvStats().setDisposition(M.baseEnvStats().disposition()-Sense.IS_INFRARED);
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Undead_ColdTouch"));
						M.addAbility(CMClass.getAbility("Undead_LifeDrain"));
						M.baseCharStats().setMyRace(CMClass.getRace("Undead"));
					}
					else
					if(special.equals("SPEC_GUARD"))
						M.addBehavior(CMClass.getBehavior("Guard"));
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
						M.addAbility(CMClass.getAbility("Skill_AllBreathing"));
					else
					if(special.equals("SPEC_BREATH_ACID"))
					{
						M.addBehavior(CMClass.getBehavior("CombatAbilities"));
						M.addAbility(CMClass.getAbility("Acidbreath"));
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
						M.addAbility(CMClass.getAbility("Thief_Poison"));
					}
					else
					if(special.equals("SPEC_OGRE_MEMBER"))
					{
					}
					else
					if(special.equals("SPEC_TROLL_MEMBER"))
					{
					}
					else
					if(special.equals("SPEC_PATROLMAN"))
					{
					}
					else
						returnAnError(mob,"Unknown special: "+special);
				}
				else
				if((s.startsWith("#SPE"))||(s.startsWith("S"))||(s.startsWith("*")||(s.startsWith("#$"))))
				{
				}
				else
				if(s.trim().length()>0)
					returnAnError(mob,"Special line: "+s);
			}
			for(int a=0;a<M.numAbilities();a++)
			{
				Ability A=M.fetchAbility(a);
				if(A!=null)
					A.autoInvocation(M);
			}
			int rejuv=(int)Math.round(Util.div(60000,Host.TICK_TIME)*2.0);
			M.baseEnvStats().setRejuv(rejuv*M.baseEnvStats().level());
			return M;
		}
		return null;
	}

	private Item getItem(String OfThisID, MOB mob, String areaName, Vector objectData, Vector objProgData)
	{
		for(int o=0;o<objectData.size();o++)
		{
			Vector objV=null;
			if(objectData.elementAt(o) instanceof Vector)
				objV=(Vector)objectData.elementAt(o);
			else
			if(objectData.elementAt(o) instanceof String)
			{
				String s=(String)objectData.elementAt(o);
				if(!s.toUpperCase().trim().startsWith("#OBJ"))
					returnAnError(mob,"Eating immaterial line: "+objectData.elementAt(o));
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
				returnAnError(mob,"Malformed object! Aborting this object "+objectID+", display="+objectDisplay+", simple="+simpleName+", name="+objectName+", codeStr1="+codeStr1+", codeStr2="+codeStr2+", codeStr3="+codeStr3+"!");
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
					  ||(str3.indexOf("WINE")>=0)
					  ||(str3.indexOf("FIREBEATHER")>=0)
					  ||(str3.indexOf("LOCAL SPECIALTY")>=0)
					  ||(str3.indexOf("WHISKEY")>=0))
					 {
						 I=CMClass.getMiscMagic("GenMultiPotion");
						 ((Potion)I).setSpellList("Inebriation"+";");
					 }
					 else
					 if((val4>0)||(str3.indexOf("POISON")>=0))
					 {
						 I=CMClass.getMiscMagic("GenMultiPotion");
						 ((Potion)I).setSpellList("Poison"+";");
					 }
					 ((Drink)I).setLiquidHeld(val1);
					 ((Drink)I).setLiquidRemaining(val2);
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
			case 20: I=CMClass.getStdItem("GenCoins");
					 I.baseEnvStats().setAbility(val1);
					 break;
			case 21: I=CMClass.getStdItem("GenItem"); break;
			case 22: I=CMClass.getStdItem("GenBoat"); break;
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
			if(Util.isSet(wearFlag,10)) // about the body
				I.setRawProperLocationBitmap(Item.ON_NECK|I.rawProperLocationBitmap());
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
			if(Util.isSet(wearFlag,17)) //ears
				I.setRawProperLocationBitmap(Item.ON_HEAD|I.rawProperLocationBitmap());
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
				I.baseEnvStats().setWeight(Util.s_int(Util.getBit(codeStr3,1)) / 3);
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
				I.setMaterial(Item.METAL);
			else
			if(objectDescription.equalsIgnoreCase("cloth"))
				I.setMaterial(Item.CLOTH);
			else
			if(objectDescription.equalsIgnoreCase("leather"))
				I.setMaterial(Item.LEATHER);
			else
			if(objectDescription.equalsIgnoreCase("metal"))
				I.setMaterial(Item.METAL);
			else
			if(objectDescription.equalsIgnoreCase("glass"))
				I.setMaterial(Item.GLASS);
			else
			if(objectDescription.equalsIgnoreCase("mithril"))
				I.setMaterial(Item.MITHRIL);
			else
			if(objectDescription.equalsIgnoreCase("adamantite"))
				I.setMaterial(Item.MITHRIL);
			else
			if(objectDescription.equalsIgnoreCase("wood"))
				I.setMaterial(Item.WOODEN);
			else
			if(objectDescription.equalsIgnoreCase("iron"))
				I.setMaterial(Item.METAL);
			else
			if(objectDescription.equalsIgnoreCase("brass"))
				I.setMaterial(Item.METAL);
			else
			if(objectDescription.equalsIgnoreCase("vellum"))
				I.setMaterial(Item.CLOTH);
			else
			if(objectDescription.equalsIgnoreCase("silver"))
				I.setMaterial(Item.METAL);
			else
			if(objectDescription.equalsIgnoreCase("gold"))
				I.setMaterial(Item.METAL);
			else
			if(objectDescription.equalsIgnoreCase("copper"))
				I.setMaterial(Item.METAL);
			else
			if(objectDescription.equalsIgnoreCase("bronze"))
				I.setMaterial(Item.METAL);
			else
			if(objectDescription.equalsIgnoreCase("crystal"))
				I.setMaterial(Item.GLASS);
			else
			if(objectDescription.equalsIgnoreCase("clay"))
				I.setMaterial(Item.GLASS);
			else
			if(objectDescription.equalsIgnoreCase("china"))
				I.setMaterial(Item.GLASS);
			else
			if(objectDescription.equalsIgnoreCase("diamond"))
				I.setMaterial(Item.GLASS);
			else
			if(objectDescription.equalsIgnoreCase("pearl"))
				I.setMaterial(Item.GLASS);
			else
			if(objectDescription.equalsIgnoreCase("gem"))
				I.setMaterial(Item.GLASS);
			else
			if(objectDescription.equalsIgnoreCase("paper"))
				I.setMaterial(Item.CLOTH);
			else
				materialchange=false;

			if(materialchange)
			    I.setDescription("");

			if(Util.isSet(extraFlag,0))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|Sense.IS_LIGHT);

			//if((extraFlag&2)==2) coffeemud has no hummers
			if(Util.isSet(extraFlag,2))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|Sense.IS_DARK);

			if(Util.isSet(extraFlag,4))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|Sense.IS_EVIL);

			if(Util.isSet(extraFlag,5))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|Sense.IS_INVISIBLE);

			if(Util.isSet(extraFlag,6))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|Sense.IS_BONUS);

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
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|Sense.IS_INVISIBLE);

			if(Util.isSet(extraFlag,17))
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|Sense.IS_GOOD);

			if(Util.isSet(extraFlag,18))
				if(I.material()==Item.METAL)
					I.setMaterial(Item.GLASS);

			//if(Util.isSet(extraFlag,20))
			//nothing is unlocatable

			//if(Util.isSet(extraFlag,22))
			//nothing is unidentifiable

			// now all those funny tags
			while(objV.size()>0)
			{
				String codeLine=eatNextLine(objV).trim().toUpperCase();
				if(codeLine.equals("E"))
				{
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
					// need to figure this one out.
					eatLine(objV);
				}
				else
				if(codeLine.equals("A"))
				{
					String codesLine=eatNextLine(objV);
					if(Util.numBits(codesLine)!=2)
						returnAnError(mob,"Malformed 'A' code for item "+objectID+", "+I.name()+": "+codesLine);
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
					String codesLine=eatNextLine(objV);
					if(Util.numBits(codesLine)!=4)
						returnAnError(mob,"Malformed 'F' code for item "+objectID+", "+I.name()+": "+codesLine);
					else
					{
						String codeType=Util.getBit(codesLine,0);
						if(codeType.equals("V")) continue; // still no vulnerabilities support
						if(codeType.equals("A"))
						{
							int dis=0;
							int sense=0;
							int codeBits=getBitMask(codesLine,3);
							if(Util.isSet(codeBits,0))
								sense=sense|Sense.CAN_SEE;
							if(Util.isSet(codeBits,1))
								dis=dis|Sense.IS_INVISIBLE;
							if(Util.isSet(codeBits,2))
								sense=sense|Sense.CAN_SEE_EVIL;
							if(Util.isSet(codeBits,3))
								sense=sense|Sense.CAN_SEE_INVISIBLE;
							if(Util.isSet(codeBits,4))
								sense=sense|Sense.CAN_SEE_BONUS;
							if(Util.isSet(codeBits,5))
								sense=sense|Sense.CAN_SEE_HIDDEN|Sense.CAN_SEE_SNEAKERS;
							if(Util.isSet(codeBits,6))
								caster.setMiscText(caster.text()+("Prayer_Sanctuary")+";");
							if(Util.isSet(codeBits,7))
								caster.setMiscText(caster.text()+("Prayer_Sanctuary")+";");
							if(Util.isSet(codeBits,8))
								caster.setMiscText(caster.text()+("Spell_FaerieFire")+";");
							if(Util.isSet(codeBits,9))
								sense=sense|Sense.CAN_SEE_INFRARED;
							if(Util.isSet(codeBits,10))
								caster.setMiscText(caster.text()+("Prayer_Curse")+";");
							//if(Util.isSet(codeBits,11)) // what the heck is master weapon?
							//	caster.setMiscText(caster.text()+(new Spell_FaerieFire().ID())+";");
							if(Util.isSet(codeBits,12))
								caster.setMiscText(caster.text()+("Poison")+";");
							if(Util.isSet(codeBits,13))
								caster.setMiscText(caster.text()+("Prayer_ProtEvil")+";");
							if(Util.isSet(codeBits,14))
								caster.setMiscText(caster.text()+("Prayer_ProtGood")+";");
							if(Util.isSet(codeBits,15))
								dis=dis|Sense.IS_SNEAKING;
							if(Util.isSet(codeBits,16))
								dis=dis|Sense.IS_HIDDEN;
							if(Util.isSet(codeBits,17))
							{
								dis=dis|Sense.IS_SLEEPING;
								caster.setMiscText(caster.text()+("Spell_Sleep")+";");
							}
							//if(Util.isSet(codeBits,18)) item cannot charm you
							//	caster.setMiscText(caster.text()+(new Poison().ID())+";");
							if(Util.isSet(codeBits,19))
								dis=dis|Sense.IS_FLYING;
							//if(Util.isSet(codeBits,20)) no pass door irrelevancy, yet
							//	dis=dis|Sense.IS_FLYING;
							if(Util.isSet(codeBits,21))
								caster.setMiscText(caster.text()+("Spell_Haste")+";");
							if(Util.isSet(codeBits,22))
								caster.setMiscText(caster.text()+("Prayer_Calm")+";");
							if(Util.isSet(codeBits,23))
								caster.setMiscText(caster.text()+("Prayer_Plague")+";");
							if(Util.isSet(codeBits,24))
								caster.setMiscText(caster.text()+("Prop_SafePet")+";");
							if(Util.isSet(codeBits,25))
								sense=sense|Sense.CAN_SEE_DARK;
							if(Util.isSet(codeBits,26))
								caster.setMiscText(caster.text()+("Fighter_Berzerk")+";");
							if(Util.isSet(codeBits,27))
								caster.setMiscText(caster.text()+("Regeneration")+";");
							if(Util.isSet(codeBits,28))
								sense=sense|Sense.CAN_SEE_GOOD;
							if(Util.isSet(codeBits,29))
								caster.setMiscText(caster.text()+("Spell_Slow")+";");
							if(sense>0)
								adjuster.setMiscText(adjuster.text()+" sen+"+sense);
							if(dis>0)
								adjuster.setMiscText(adjuster.text()+" dis+"+dis);
						}
						else
						{
							Ability resist=resister;
							int res=getBitMask(codesLine,3);
							int imm=getBitMask(codesLine,3);

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
							//if(Util.isSet(res,14)) nothing this specific (magic missile)
							if((Util.isSet(res,15))||(Util.isSet(imm,15)))
								resist.setMiscText(resist.text()+" mind");
							if((Util.isSet(res,16))||(Util.isSet(imm,16)))
								resist.setMiscText(resist.text()+" disease");
							if((Util.isSet(res,17))||(Util.isSet(imm,17)))
								resist.setMiscText(resist.text()+" water gas");
							//if(Util.isSet(res,18)) no light resistance
							//if(Util.isSet(res,18)) no sound resistance
						}


					}
				}
				else
				if((codeLine.startsWith("#"))||(codeLine.length()==0))
				{
				}
				else
					returnAnError(mob,"Unknown code for item "+objectID+", "+I.name()+": "+codeLine);
			}
			if(adjuster.text().length()>0)
				I.addNonUninvokableAffect(adjuster);
			if(caster.text().length()>0)
				I.addNonUninvokableAffect(caster);
			if(resister.text().length()>0)
				I.addNonUninvokableAffect(resister);
			return I;
		}
		return null;
	}


	public void areimport(MOB mob, Vector commands)
	{
		Vector areaData=new Vector();
		Vector roomData=new Vector();
		Vector mobData=new Vector();
		Vector resetData=new Vector();
		Vector objectData=new Vector();
		Vector mobProgData=new Vector();
		Vector objProgData=new Vector();
		Vector shopData=new Vector();
		Vector specialData=new Vector();
		Vector newRooms=new Vector();
		Vector reLinkTable=null;
		
		boolean prompt=true;

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
		// read in the .are file
		StringBuffer buf=Resources.getFile(Util.combine(commands,0));
		if((buf==null)||((buf!=null)&&(buf.length()==0)))
		{
			mob.tell("File not found at: '"+Util.combine(commands,0)+"'!");
			return;
		}

		Vector V=Resources.getFileLineVector(buf);

		// sort the data into general blocks, and identify area
		mob.tell("Sorting data...");
		readBlocks(V,areaData,roomData,mobData,resetData,objectData,mobProgData,objProgData,shopData,specialData);
		if((roomData.size()==0)||(areaData.size()==0))
		{
			mob.tell("Missing data! It is very unlikely this is an .are file.");
			return;
		}
		String areaName=getAreaName(areaData);
		if((areaName==null)||((areaName!=null)&&(areaName.length()==0)))
		{
			mob.tell("#AREA tag not found!");
			return;
		}

		try
		{
			// confirm area creation/overwrite
			boolean exists=false;
			for(Enumeration e=CMMap.map.elements();e.hasMoreElements();)
			{
				Room r=(Room)e.nextElement();
				if(r.getArea().name().equalsIgnoreCase(areaName))
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
					if(mob.location().getArea().name().equalsIgnoreCase(areaName))
					{
						mob.tell("You dip!  You are IN that area!  Leave it first...");
						return;
					}
					else
					{
						reLinkTable=new Vector();
						for(Enumeration e=CMMap.map.elements();e.hasMoreElements();)
						{
							Room r=(Room)e.nextElement();
							if(!r.getArea().name().equalsIgnoreCase(areaName))
								for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
								{
									Room dirR=r.doors()[d];
									if((dirR!=null)&&(dirR.getArea().name().equalsIgnoreCase(areaName)))
										reLinkTable.addElement(r.ID()+"/"+d+"/"+dirR.ID());
								}
						}
						while(true)
						{
							Room foundOne=null;
							for(Enumeration e=CMMap.map.elements();e.hasMoreElements();)
							{
								Room r=(Room)e.nextElement();
								if(r.getArea().name().equalsIgnoreCase(areaName))
								{
									foundOne=r;
									break;
								}
							}
							if(foundOne==null)
								break;
							else
								myRooms.obliterateRoom(mob,foundOne);
						}
					}
				}
				else
					return;
			}
			else
			if((prompt)&&(!mob.session().confirm("Found area: \""+areaName+"\", is this ok?","Y")))
				return;

			Resources.removeResource("areasList");

			mob.tell("Loading and Linking rooms...");
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
						returnAnError(mob,"Eating immaterial line: "+roomData.elementAt(r));
					continue;
				}
				else
					continue;

				Area A=CMMap.getArea(areaName);
				if(A==null)
					A=ExternalPlay.DBCreateArea(areaName,"StdArea");
				
				Room R=CMClass.getLocale("StdRoom");
				R.setID(eatNextLine(roomV));
				R.setDisplayText(Util.safetyFilter(eatLineSquiggle(roomV)));
				R.setDescription(Util.safetyFilter(eatLineSquiggle(roomV)));
				R.setArea(A);
				String codeLine=eatNextLine(roomV);
				if((!R.ID().startsWith("#"))
				||(R.displayText().length()==0)
				||(Util.numBits(codeLine)<2)
				||(Util.numBits(codeLine)>3))
				{
					returnAnError(mob,"Malformed room! Aborting this room "+R.ID()+", display="+R.displayText()+", description="+R.description()+", numBits="+Util.numBits(codeLine)+"!");
					continue;
				}
				else
					R.setID(areaName+R.ID());
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
					case 4:	R=changeRoomClass(R,"Plains"); break;
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
					case 4:	R=changeRoomClass(R,"WoodRoom"); break;
					case 5:	R=changeRoomClass(R,"StoneRoom"); break;
					case 6:	R=changeRoomClass(R,"ShallowWater"); break;
					case 7:	R=changeRoomClass(R,"WaterSurface"); break;
					case 8:	R=changeRoomClass(R,"IceRoom"); break;
					case 9:	R=changeRoomClass(R,"InTheAir"); break;
					case 10: R=changeRoomClass(R,"HotRoom"); break;
					case 11: R=changeRoomClass(R,"IceRoom"); break;
					case 12: R=changeRoomClass(R,"IceRoom"); break;
					}
				}

				Ability prop_RoomCapacity=CMClass.getAbility("Prop_RoomCapacity");
				Ability prop_RoomLevels=CMClass.getAbility("Prop_RoomLevels");


				if(Util.isSet(codeBits,21)) // underwater room
					R=changeRoomClass(R,"UnderWater");

				if(Util.isSet(codeBits,1)) //BANKS ARE IRRELEVANT RIGHT NOW!
					returnAnError(mob,R.ID()+" is a Bank, but CoffeeMud doesn't care.");

				if(Util.isSet(codeBits,0)) // dark room
					R.addNonUninvokableAffect(CMClass.getAbility("Prop_RoomDark"));

				if(Util.isSet(codeBits,2)) // no mobs room
					R.addNonUninvokableAffect(CMClass.getAbility("Prop_RoomNoMOB"));

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
				Ability restrictor=CMClass.getAbility("Prop_RestrictSpells");
				if(Util.isSet(codeBits,18))
					restrictor.setMiscText(restrictor.text()+" Spell_Summon Spell_SummonMonster Spell_Charm Song_Friendship ");

				if(Util.isSet(codeBits,19))
					returnAnError(mob,R.ID()+" is a player-killing area, but CoffeeMud doesn't care.");

				if(Util.isSet(codeBits,20))
					restrictor.setMiscText(restrictor.text()+" Spell_Teleport Spell_Gate Spell_Portal ");

				// if(Util.isSet(codeBits,23)) No "dirt" in CoffeeMud, so this doesn't matter

				if(Util.isSet(codeBits,24))
					R.addNonUninvokableAffect(CMClass.getAbility("Prop_NoChannel"));

				if(restrictor.text().length()>0)
					R.addNonUninvokableAffect(restrictor);

				roomV.insertElementAt(R.ID(),0);
				CMMap.map.addElement(R);
				newRooms.addElement(R);

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
				Room R=(Room)CMMap.getRoom(roomID);
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
							I=CMClass.getStdItem("GenReadable");
							I.setReadableText(fixReadableContent(descString));
						}
						else
							I=CMClass.getStdItem("GenItem");
						I.setName(nameString);
						I.setDisplayText("");
						I.setDescription(descString);
						I.setGettable(false);
						I.baseEnvStats().setWeight(0);
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
								returnAnError(mob,"Room: "+R.ID()+", Unknown direction code: "+dirCode+", aborting exit!");
								continue;
						}
						if(Util.numBits(codeStr)!=3)
						{
							returnAnError(mob,"Room: "+R.ID()+", Malformed exit codeStr "+codeStr+".  Aborting exit!");
							continue;
						}
						if((R.exits()[dirCode]!=null)||(R.doors()[dirCode]!=null))
						{
							returnAnError(mob,"Room: "+R.ID()+", Redundant exit codeStr "+codeStr+".  Aborting exit!");
							continue;
						}
						int exitFlag=( Util.s_int(Util.getBit(codeStr,0)) & 31);
						int doorState=Util.s_int(Util.getBit(codeStr,1));
						int linkRoomID=Util.s_int(Util.getBit(codeStr,2));
						Exit E=CMClass.getExit("GenExit");
						Room linkRoom=(Room)CMMap.getRoom(areaName+"#"+linkRoomID);
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
							E.baseEnvStats().setDisposition(E.baseEnvStats().disposition()|Sense.IS_HIDDEN);
							E.recoverEnvStats();
						}
						
						if(name.length()>0)
						{
							if(("aeiouAEIOU").indexOf(name.charAt(0))>=0)
								E.setName("an "+name);
							else
								E.setName("a "+name);
						}
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
						E.setExitParams(name,E.closeWord(),E.openWord(),E.name()+", closed");
						E.setDescription(descStr);
						R.exits()[dirCode]=E;
						Exit opExit=null;
						if((linkRoom==null)&&(linkRoomID>=0))
						{
							for(Enumeration e=CMMap.map.elements();e.hasMoreElements();)
							{
								Room R2=(Room)e.nextElement();
								if((R2.ID().endsWith("#"+linkRoomID))&&(R2!=R))
								{
									for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
									{
										Exit E3=R2.exits()[d];
										if(E3!=null)
											if(R.ID().endsWith(E3.closeWord()))
											{
												opExit=E3;
												R2.doors()[d]=R;
											}
									}
									if(opExit==null)
										if((prompt)&&
										  (!mob.session().confirm(R.ID()+" links to #"+linkRoomID+". Found "+R2.ID()+". Link?","Y")))
											continue;
									linkRoom=R2;
									if(opExit!=null)
										opExit.setExitParams(opExit.doorName(),"close",opExit.openWord(),opExit.displayText());
									ExternalPlay.DBUpdateExits(linkRoom);
									break;
								}
							}
							if(linkRoom==null)
								E.setExitParams(E.doorName(),"#"+linkRoomID,E.openWord(),E.closedText());
							else
								E.setExitParams(E.doorName(),"close",E.openWord(),E.displayText());

						}
						R.doors()[dirCode]=linkRoom;
						if((linkRoom==null)&&(linkRoomID>=0))
							returnAnError(mob,"Room: "+R.ID()+" links "+Directions.getDirectionName(dirCode)+"ward to unknown room #"+linkRoomID+".");
					}
					else
					if((!nextLine.equalsIgnoreCase("#0"))&&(nextLine.trim().length()>0))
						returnAnError(mob,"Unknown room code: "+nextLine);
				}
			}

			mob.session().print("\n\nLoading objects..");
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
					R=CMMap.getRoom(areaName+"#"+roomID);
					if(R==null)
						returnAnError(mob,"Reset error (no room) on line: "+s);
					else
					{
						M=getMOB("#"+mobID,R,areaName,mob,Util.copyVector(mobData),Util.copyVector(mobProgData),Util.copyVector(specialData),Util.copyVector(shopData));
						if(M==null)
							returnAnError(mob,"Reset error (no mob) on line: "+s);
						else
						{
							M.recoverCharStats();
							M.recoverEnvStats();
							M.recoverMaxState();
							M.resetToMaxState();
							M.text();
							M.bringToLife(R);
						}
					}
				}
				else
				if(s.startsWith("G "))
				{
					if(M==null)
						returnAnError(mob,"Reset error (no mob) on line: "+s);
					else
					{
						String itemID=Util.getBit(s,2);
						Item I=getItem("#"+itemID,mob,areaName,Util.copyVector(objectData),Util.copyVector(objProgData));
						if(I==null)
							returnAnError(mob,"Reset error (no item) on line: "+s);
						else
						{
							I.recoverEnvStats();
							if(M instanceof ShopKeeper)
							{
								int num=Util.s_int(Util.getBit(s,3));
								if(num<0) num=100;
								((ShopKeeper)M).addStoreInventory(I,num);
								if((I instanceof Light)&&(!((ShopKeeper)M).doIHaveThisInStock("OilFlask")))
									((ShopKeeper)M).addStoreInventory(CMClass.getStdItem("OilFlask"),num*2);
								if(((I.ID().equals("GenReadable"))
								||(I instanceof com.planet_ink.coffee_mud.interfaces.Map))
								&&(!((ShopKeeper)M).doIHaveThisInStock("Parchment")))
									((ShopKeeper)M).addStoreInventory(CMClass.getStdItem("Parchment"),num);
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
						returnAnError(mob,"Reset error (no mob) on line: "+s);
					else
					{
						String itemID=Util.getBit(s,2);
						Item I=getItem("#"+itemID,mob,areaName,Util.copyVector(objectData),Util.copyVector(objProgData));
						if(I==null)
							returnAnError(mob,"Reset error (no item) on line: "+s);
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
					R=CMMap.getRoom(areaName+"#"+roomID);
					if(R==null)
						returnAnError(mob,"Reset error (no room) on line: "+s+"/"+roomID+"/"+roomID.length());
					else
					{
						Item I=getItem("#"+itemID,mob,areaName,Util.copyVector(objectData),Util.copyVector(objProgData));
						if(I==null)
							returnAnError(mob,"Reset error (no item) on line: "+s);
						else
						{
							R.addItem(I);
							if(I.isGettable())
							{
								int rejuv=(int)Math.round(Util.div(60000,Host.TICK_TIME)*4.0);
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
					Item I=getItem("#"+itemID,mob,areaName,Util.copyVector(objectData),Util.copyVector(objProgData));
					if(I==null)
						returnAnError(mob,"Reset error (no item) on line: "+s);
					else
					{
						Container C=(Container)containerHash.get(containerID);
						if(C==null)
							returnAnError(mob,"Reset error (no container) on line: "+s);
						else
						if(C.myOwner()==null)
							returnAnError(mob,"Reset error (no container owner) on line: "+s);
						else
						if(C.myOwner() instanceof Room)
						{
							Room RR=(Room)C.myOwner();
							RR.addItem(I);
							I.setLocation(C);
							if(I.isGettable())
								I.baseEnvStats().setRejuv(1000);
							I.recoverEnvStats();
							if(I instanceof Container)
								containerHash.put(itemID,I);
						}
						else
						if(C.myOwner() instanceof MOB)
						{
							MOB MM=(MOB)C.myOwner();
							MM.addInventory(I);
							I.setLocation(C);
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
					R=CMMap.getRoom(areaName+"#"+roomID);
					if(R==null)
						returnAnError(mob,"Reset error (no room) on line: "+s);
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
							returnAnError(mob,"Room: "+R.ID()+", Unknown direction code: "+dirCode+" (not so bad at this point, it was probably aborted earlier...");
						}
						if(dirCode<Directions.NUM_DIRECTIONS)
						{
							Exit E=R.exits()[dirCode];
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
								returnAnError(mob,"Room: "+R.ID()+", Unknown door code: "+lockBit);
								break;
							}
							E.setDoorsNLocks(HasDoor,Open,DefaultsClosed,HasLock,Locked,DefaultsLocked);
						}
					}
				}
				else
				if(s.length()>0)
					returnAnError(mob,"Reset, unknown command: "+s);

			}
			// now fix the pet shops!
			for(Enumeration e=petShops.keys();e.hasMoreElements();)
			{
				Room storeRoom=(Room)e.nextElement();
				Room shopRoom=(Room)petShops.get(storeRoom);
				ShopKeeper shopKeeper=null;
				if(shopRoom==null)
					returnAnError(mob,"Unknown store room: "+storeRoom.ID());
				else
				for(int i=0;i<shopRoom.numInhabitants();i++)
				{
					MOB sk=shopRoom.fetchInhabitant(i);
					if((sk!=null)&&(sk instanceof ShopKeeper))
					{ shopKeeper=(ShopKeeper)sk; break;	}
				}
				if(shopKeeper==null)
					returnAnError(mob,"Unknown shopkeeper not in room: "+storeRoom.ID());
				else
				while(storeRoom.numInhabitants()>0)
				{
					shopKeeper.setWhatIsSold(ShopKeeper.PETS);
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
							&&(I.displayText().indexOf(lookItem.name())>=0))
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
			mob.session().print("\n\nReset, and saving...");
			
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
					Room sourceRoom=CMMap.getRoom(sourceRoomID);
					Room destRoom=CMMap.getRoom(destRoomID);
					if((sourceRoom==null)||(destRoom==null))
						Log.errOut("Import","Relink error: "+sourceRoomID+"="+sourceRoom+"/"+destRoomID+"="+destRoom);
					else
					{
						sourceRoom.doors()[direction]=destRoom;
						if((nextLink.length()==0)||(!nextLink.startsWith(sourceRoomID+"/")))
							ExternalPlay.DBUpdateExits(sourceRoom);
					}
				}
			for(int r=0;r<newRooms.size();r++)
			{
				Room saveRoom=(Room)newRooms.elementAt(r);
				ExternalPlay.DBCreateRoom(saveRoom,CMClass.className(saveRoom));
				ExternalPlay.DBUpdateExits(saveRoom);
				myRooms.clearDebriAndRestart(saveRoom,0);
				saveRoom.recoverRoomStats();
			}
			mob.session().print("\n\nDone!!!!!!  A good room to look at would be "+((Room)newRooms.elementAt(0)).ID());

		}
		catch(Exception e)
		{
			Log.errOut("Import",e);
			mob.tell(e.getMessage());
			return;
		}
		return;
	}
}

