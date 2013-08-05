package com.planet_ink.coffee_mud.Libraries;

import com.planet_ink.coffee_mud.core.exceptions.ScriptParseException;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZapperMask;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZapperMaskEntry;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
/*
   Copyright 2000-2013 Bo Zimmerman

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
public class MUDZapper extends StdLibrary implements MaskingLibrary
{
	public String ID(){return "MUDZapper";}
	public Hashtable<String,Integer> zapCodes=new Hashtable<String,Integer>();

	protected MOB nonCrashingMOB=null;
	protected MOB nonCrashingMOB(){
		if(nonCrashingMOB!=null)
			return nonCrashingMOB;
		nonCrashingMOB=CMClass.getMOB("StdMOB");
		return nonCrashingMOB;
	}

	protected Item nonCrashingItem=null;
	protected Item nonCrashingItem(final MOB mob){
		if(mob.numItems()>0)
		{
			Item I = mob.getItem(0);
			if(I!=null) return I;
		}
		if(nonCrashingItem!=null)
			return nonCrashingItem;
		nonCrashingItem=CMClass.getItem("StdItem");
		return nonCrashingItem;
	}

	public String rawMaskHelp(){return DEFAULT_MASK_HELP;}

	protected volatile List<SavedClass> savedCharClasses = new Vector<SavedClass>(1);
	protected volatile List<SavedRace> savedRaces = new Vector<SavedRace>(1);
	protected volatile long savedClassUpdateTime=0;
	public synchronized void buildSavedClasses()
	{
		if(savedClassUpdateTime==CMClass.getLastClassUpdatedTime())
			return;
		List<SavedClass> tempSavedCharClasses=new LinkedList<SavedClass>();
		List<SavedRace> tempSavedRaces=new LinkedList<SavedRace>();
		for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
		{
			final CharClass C=c.nextElement();
			tempSavedCharClasses.add(new SavedClass(C,4));
		}
		for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
		{
			final Race R=r.nextElement();
			tempSavedRaces.add(new SavedRace(R,6));
		}
		savedCharClasses=tempSavedCharClasses;
		savedRaces=tempSavedRaces;
		savedClassUpdateTime=CMClass.getLastClassUpdatedTime();
	}
	
	public final List<SavedClass> charClasses()
	{
		if(savedClassUpdateTime!=CMClass.getLastClassUpdatedTime())
			buildSavedClasses();
		return savedCharClasses;
	}
	public final List<SavedRace> races()
	{
		if(savedClassUpdateTime!=CMClass.getLastClassUpdatedTime())
			buildSavedClasses();
		return savedRaces;
	}
	
	@SuppressWarnings("unchecked")
    public CompiledZapperMask getPreCompiledMask(final String str)
	{
		Map<String,CompiledZapperMask> H=(Map<String,CompiledZapperMask>)Resources.getResource("SYSTEM_HASHED_MASKS");
		if(H==null)
		{ 
			H=new PrioritizingLimitedMap<String,CompiledZapperMask>(200, 10*60*1000, Long.MAX_VALUE, 50); 
			Resources.submitResource("SYSTEM_HASHED_MASKS",H); 
		}
		final String lowerStr=(str==null)?"":str.toLowerCase().trim();
		CompiledZapperMask V=H.get(lowerStr);
		if(V==null)
		{
			V=maskCompile(str);
			H.put(lowerStr,V);
		}
		return V;
	}

	public Map<String,Integer> getMaskCodes()
	{
		if(zapCodes.size()==0)
		{
			zapCodes.put("-CLASS",Integer.valueOf(0));
			zapCodes.put("-CLASSES",Integer.valueOf(0));
			zapCodes.put("-BASECLASS",Integer.valueOf(1));
			zapCodes.put("-BASECLASSES",Integer.valueOf(1));
			zapCodes.put("-RACE",Integer.valueOf(2));
			zapCodes.put("-RACES",Integer.valueOf(2));
			zapCodes.put("-ALIGNMENT",Integer.valueOf(3));
			zapCodes.put("-ALIGNMENTS",Integer.valueOf(3));
			zapCodes.put("-ALIGN",Integer.valueOf(3));
			zapCodes.put("-GENDER",Integer.valueOf(4));
			zapCodes.put("-GENDERS",Integer.valueOf(4));
			zapCodes.put("-LEVEL",Integer.valueOf(5));
			zapCodes.put("-LEVELS",Integer.valueOf(5));
			zapCodes.put("-CLASSLEVEL",Integer.valueOf(6));
			zapCodes.put("-CLASSLEVELS",Integer.valueOf(6));
			zapCodes.put("-TATTOOS",Integer.valueOf(7));
			zapCodes.put("-TATTOO",Integer.valueOf(7));
			zapCodes.put("+TATTOOS",Integer.valueOf(8));
			zapCodes.put("+TATTOO",Integer.valueOf(8));
			zapCodes.put("-NAME",Integer.valueOf(9));
			zapCodes.put("-NAMES",Integer.valueOf(9));
			zapCodes.put("-PLAYER",Integer.valueOf(10));
			zapCodes.put("-NPC",Integer.valueOf(11));
			zapCodes.put("-MOB",Integer.valueOf(11));
			zapCodes.put("-RACECAT",Integer.valueOf(12));
			zapCodes.put("-RACECATS",Integer.valueOf(12));
			zapCodes.put("+RACECAT",Integer.valueOf(13));
			zapCodes.put("+RACECATS",Integer.valueOf(13));
			zapCodes.put("-CLAN",Integer.valueOf(14));
			zapCodes.put("-CLANS",Integer.valueOf(14));
			zapCodes.put("+CLAN",Integer.valueOf(15));
			zapCodes.put("+CLANS",Integer.valueOf(15));
			zapCodes.put("+NAME",Integer.valueOf(16));
			zapCodes.put("+NAMES",Integer.valueOf(16));
			zapCodes.put("-ANYCLASS",Integer.valueOf(17));
			zapCodes.put("+ANYCLASS",Integer.valueOf(18));
			zapCodes.put("+ADJSTR",Integer.valueOf(19));
			zapCodes.put("+ADJINT",Integer.valueOf(20));
			zapCodes.put("+ADJWIS",Integer.valueOf(21));
			zapCodes.put("+ADJDEX",Integer.valueOf(22));
			zapCodes.put("+ADJCON",Integer.valueOf(23));
			zapCodes.put("+ADJCHA",Integer.valueOf(24));
			zapCodes.put("+ADJSTRENGTH",Integer.valueOf(19));
			zapCodes.put("+ADJINTELLIGENCE",Integer.valueOf(20));
			zapCodes.put("+ADJWISDOM",Integer.valueOf(21));
			zapCodes.put("+ADJDEXTERITY",Integer.valueOf(22));
			zapCodes.put("+ADJCONSTITUTION",Integer.valueOf(23));
			zapCodes.put("+ADJCHARISMA",Integer.valueOf(24));
			zapCodes.put("-ADJSTR",Integer.valueOf(25));
			zapCodes.put("-ADJSTRENGTH",Integer.valueOf(25));
			zapCodes.put("-ADJINT",Integer.valueOf(26));
			zapCodes.put("-ADJINTELLIGENCE",Integer.valueOf(26));
			zapCodes.put("-ADJWIS",Integer.valueOf(27));
			zapCodes.put("-ADJWISDOM",Integer.valueOf(27));
			zapCodes.put("-ADJDEX",Integer.valueOf(28));
			zapCodes.put("-ADJDEXTERITY",Integer.valueOf(28));
			zapCodes.put("-ADJCON",Integer.valueOf(29));
			zapCodes.put("-ADJCONSTITUTION",Integer.valueOf(29));
			zapCodes.put("-ADJCHA",Integer.valueOf(30));
			zapCodes.put("-ADJCHARISMA",Integer.valueOf(30));
			zapCodes.put("-AREA",Integer.valueOf(31));
			zapCodes.put("-AREAS",Integer.valueOf(31));
			zapCodes.put("+AREA",Integer.valueOf(32));
			zapCodes.put("+AREAS",Integer.valueOf(32));
			zapCodes.put("+ITEM",Integer.valueOf(33));
			zapCodes.put("-ITEM",Integer.valueOf(33));
			zapCodes.put("+CLASS",Integer.valueOf(34));  // for compiled use ONLY
			zapCodes.put("+ALIGNMENT",Integer.valueOf(35));  // for compiled use ONLY
			zapCodes.put("+GENDER",Integer.valueOf(36));  // for compiled use ONLY
			zapCodes.put("+LVLGR",Integer.valueOf(37));  // for compiled use ONLY
			zapCodes.put("+LVLGE",Integer.valueOf(38));  // for compiled use ONLY
			zapCodes.put("+LVLLT",Integer.valueOf(39));  // for compiled use ONLY
			zapCodes.put("+LVLLE",Integer.valueOf(40));  // for compiled use ONLY
			zapCodes.put("+LVLEQ",Integer.valueOf(41));  // for compiled use ONLY
			zapCodes.put("+EFFECTS",Integer.valueOf(42));
			zapCodes.put("+EFFECT",Integer.valueOf(42));
			zapCodes.put("-EFFECTS",Integer.valueOf(43));
			zapCodes.put("-EFFECT",Integer.valueOf(43));
			zapCodes.put("-DEITY",Integer.valueOf(44));
			zapCodes.put("+DEITY",Integer.valueOf(45));
			zapCodes.put("-FACTION",Integer.valueOf(46));
			zapCodes.put("+FACTION",Integer.valueOf(47));
			zapCodes.put("+WORN",Integer.valueOf(48));
			zapCodes.put("-WORN",Integer.valueOf(48));
			zapCodes.put("+MATERIAL",Integer.valueOf(49));
			zapCodes.put("-MATERIAL",Integer.valueOf(50));
			zapCodes.put("+RESOURCE",Integer.valueOf(51));
			zapCodes.put("-RESOURCE",Integer.valueOf(52));
			zapCodes.put("-JAVACLASS",Integer.valueOf(53));
			zapCodes.put("+JAVACLASS",Integer.valueOf(54));
			zapCodes.put("+ABILITY",Integer.valueOf(55));
			zapCodes.put("-ABILITY",Integer.valueOf(56));
			zapCodes.put("+ABLE",Integer.valueOf(55));
			zapCodes.put("-ABLE",Integer.valueOf(56));
			zapCodes.put("+WORNON",Integer.valueOf(57));
			zapCodes.put("-WORNON",Integer.valueOf(58));
			zapCodes.put("+VALUE",Integer.valueOf(59));
			zapCodes.put("-VALUE",Integer.valueOf(60));
			zapCodes.put("+WEIGHT",Integer.valueOf(61));
			zapCodes.put("-WEIGHT",Integer.valueOf(62));
			zapCodes.put("+ARMOR",Integer.valueOf(63));
			zapCodes.put("-ARMOR",Integer.valueOf(64));
			zapCodes.put("+DAMAGE",Integer.valueOf(65));
			zapCodes.put("-DAMAGE",Integer.valueOf(66));
			zapCodes.put("+ATTACK",Integer.valueOf(67));
			zapCodes.put("-ATTACK",Integer.valueOf(68));
			zapCodes.put("+DISPOSITION",Integer.valueOf(69));
			zapCodes.put("-DISPOSITION",Integer.valueOf(70));
			zapCodes.put("+SENSES",Integer.valueOf(71));
			zapCodes.put("-SENSES",Integer.valueOf(72));
			zapCodes.put("+HOUR",Integer.valueOf(73));
			zapCodes.put("-HOUR",Integer.valueOf(74));
			zapCodes.put("+SEASON",Integer.valueOf(75));
			zapCodes.put("-SEASON",Integer.valueOf(76));
			zapCodes.put("+MONTH",Integer.valueOf(77));
			zapCodes.put("-MONTH",Integer.valueOf(78));
			zapCodes.put("-SECURITY",Integer.valueOf(79));
			zapCodes.put("+SECURITY",Integer.valueOf(80));
			zapCodes.put("-SECURITIES",Integer.valueOf(79));
			zapCodes.put("+SECURITIES",Integer.valueOf(80));
			zapCodes.put("-SEC",Integer.valueOf(79));
			zapCodes.put("+SEC",Integer.valueOf(80));
			zapCodes.put("-EXPERTISE",Integer.valueOf(81));
			zapCodes.put("+EXPERTISE",Integer.valueOf(82));
			zapCodes.put("-EXPERTISES",Integer.valueOf(81));
			zapCodes.put("+EXPERTISES",Integer.valueOf(82));
			zapCodes.put("-SKILL",Integer.valueOf(83));
			zapCodes.put("+SKILL",Integer.valueOf(84));
			zapCodes.put("-SKILLS",Integer.valueOf(83));
			zapCodes.put("+SKILLS",Integer.valueOf(84));
			zapCodes.put("+QUALLVL",Integer.valueOf(85));
			zapCodes.put("-QUALLVL",Integer.valueOf(86));
			zapCodes.put("+STR",Integer.valueOf(87));
			zapCodes.put("+INT",Integer.valueOf(88));
			zapCodes.put("+WIS",Integer.valueOf(89));
			zapCodes.put("+DEX",Integer.valueOf(90));
			zapCodes.put("+CON",Integer.valueOf(91));
			zapCodes.put("+CHA",Integer.valueOf(92));
			zapCodes.put("+STRENGTH",Integer.valueOf(87));
			zapCodes.put("+INTELLIGENCE",Integer.valueOf(88));
			zapCodes.put("+WISDOM",Integer.valueOf(89));
			zapCodes.put("+DEXTERITY",Integer.valueOf(90));
			zapCodes.put("+CONSTITUTION",Integer.valueOf(91));
			zapCodes.put("+CHARISMA",Integer.valueOf(92));
			zapCodes.put("-STR",Integer.valueOf(93));
			zapCodes.put("-STRENGTH",Integer.valueOf(93));
			zapCodes.put("-INT",Integer.valueOf(94));
			zapCodes.put("-INTELLIGENCE",Integer.valueOf(94));
			zapCodes.put("-WIS",Integer.valueOf(95));
			zapCodes.put("-WISDOM",Integer.valueOf(95));
			zapCodes.put("-DEX",Integer.valueOf(96));
			zapCodes.put("-DEXTERITY",Integer.valueOf(96));
			zapCodes.put("-CON",Integer.valueOf(97));
			zapCodes.put("-CONSTITUTION",Integer.valueOf(97));
			zapCodes.put("-CHA",Integer.valueOf(98));
			zapCodes.put("-CHARISMA",Integer.valueOf(98));
			zapCodes.put("+HOME",Integer.valueOf(99));
			zapCodes.put("-HOME",Integer.valueOf(100));
			zapCodes.put("-SKILLFLAG",Integer.valueOf(101));
			zapCodes.put("+SKILLFLAG",Integer.valueOf(102));
			zapCodes.put("-SKILLFLAGS",Integer.valueOf(101));
			zapCodes.put("+SKILLFLAGS",Integer.valueOf(102));
			zapCodes.put("-MAXCLASSLEVEL",Integer.valueOf(103));
			zapCodes.put("-MAXCLASSLEVELS",Integer.valueOf(103));
			zapCodes.put("+WEATHER",Integer.valueOf(104));
			zapCodes.put("-WEATHER",Integer.valueOf(105));
			zapCodes.put("+DAY",Integer.valueOf(106));
			zapCodes.put("-DAY",Integer.valueOf(107));
			zapCodes.put("+SYSOP",Integer.valueOf(108));
			zapCodes.put("-SYSOP",Integer.valueOf(109));
			zapCodes.put("+SUBOP",Integer.valueOf(110));
			zapCodes.put("-SUBOP",Integer.valueOf(111));
			zapCodes.put("+RACE",Integer.valueOf(112));  // for compiled use ONLY
			zapCodes.put("-QUESTWIN",Integer.valueOf(113));
			zapCodes.put("+QUESTWIN",Integer.valueOf(114));
			zapCodes.put("-GROUPSIZE",Integer.valueOf(115));
			zapCodes.put("+GROUPSIZE",Integer.valueOf(116));
			zapCodes.put("+BASECLASS",Integer.valueOf(117));
			zapCodes.put("-IF",Integer.valueOf(118));
			zapCodes.put("+IF",Integer.valueOf(119));
			zapCodes.put("-MOODS",Integer.valueOf(120));
			zapCodes.put("-MOOD",Integer.valueOf(120));
			zapCodes.put("+MOODS",Integer.valueOf(121));
			zapCodes.put("+MOOD",Integer.valueOf(121));
			zapCodes.put("-CHANCE",Integer.valueOf(122));
		}
		return zapCodes;
	}

	public String maskHelp(final String CR, final String word)
	{
		String copy=rawMaskHelp();
		if((CR!=null)&&(!CR.equalsIgnoreCase("<BR>")))
			copy=CMStrings.replaceAll(copy,"<BR>",CR);
		if((word==null)||(word.length()==0))
			copy=CMStrings.replaceAll(copy,"<WORD>","disallow");
		else
			copy=CMStrings.replaceAll(copy,"<WORD>",word);
		return copy;
	}

	protected Object makeSkillFlagObject(final String str)
	{
		Object o=null;
		int x=str.indexOf('&');
		if(x>=0)
		{
			Vector<Object> V=new Vector<Object>();
			V.addAll(CMParms.parseAny(str,'&',true));
			String s=null;
			for(int v=0;v<V.size();v++)
			{
				s=(String)V.elementAt(v);
				if(s.startsWith("!"))
				{
					V.setElementAt(s.substring(1),v);
					V.insertElementAt(Boolean.FALSE,v);
					v++;
				}
			}
			Object[] o2=new Object[V.size()];
			for(int v=0;v<V.size();v++)
				if(V.elementAt(v) instanceof String)
					o2[v]=makeSkillFlagObject((String)V.elementAt(v));
				else
					o2[v]=V.elementAt(v);
			for(int i=0;i<o2.length;i++)
				if((o2[i]!=null)&&(!(o2[i] instanceof Boolean)))
				{ o=o2; break;}
		}
		if(o==null)
		for(int d=0;d<Ability.ACODE_DESCS.length;d++)
			if(Ability.ACODE_DESCS[d].equals(str))
			{
				o=Integer.valueOf(d);
				break;
			}
		if(o==null)
		for(int d=0;d<Ability.DOMAIN_DESCS.length;d++)
			if(Ability.DOMAIN_DESCS[d].startsWith(str)||Ability.DOMAIN_DESCS[d].endsWith(str))
			{
				o=Integer.valueOf(d<<5);
				break;
			}
		if(o==null)
		for(int d=0;d<Ability.FLAG_DESCS.length;d++)
			if(Ability.FLAG_DESCS[d].startsWith(str))
			{
				o=Long.valueOf(1<<d);
				break;
			}
		if(o==null)
		for(short d=0;d<Ability.QUALITY_DESCS.length;d++)
			if(Ability.QUALITY_DESCS[d].startsWith(str)||Ability.QUALITY_DESCS[d].endsWith(str))
			{
				o=new Short(d);
				break;
			}
		return o;
	}

	protected boolean evaluateSkillFlagObject(final Object o, final Ability A)
	{
		if(A!=null)
		{
			if(o instanceof Object[])
			{
				final Object[] set=(Object[])o;
				for(int i=0;i<set.length;i++)
					if(set[i] instanceof Boolean)
					{
						if(evaluateSkillFlagObject(set[i+1],A))
							return false;
						i++;
					}
					else
					if(!evaluateSkillFlagObject(set[i],A))
						return false;
				return true;
			}
			else
			if(o instanceof Integer)
			{
				final int val=((Integer)o).intValue();
				if(((A.classificationCode()&Ability.ALL_ACODES)==val)
				||((A.classificationCode()&Ability.ALL_DOMAINS)==val))
					return true;
			}
			else
			if(o instanceof Short)
			{
				final int val=((Short)o).intValue();
				if(A.abstractQuality()==val)
					return true;
			}
			else
			if(o instanceof Long)
			{
				long val=((Long)o).longValue();
				if((A.flags()&val)==val)
					return true;
			}
		}
		return false;
	}


	protected boolean skillFlagCheck(final List<String> V, final char plusMinus, final int fromHere, final MOB mob)
	{
		for(int v=fromHere;v<V.size();v++)
		{
			final String str=V.get(v);
			if(str.length()==0) continue;
			if(getMaskCodes().containsKey(str))
				return false;
			final Object o=makeSkillFlagObject(str);
			if(o==null) continue;
			for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(evaluateSkillFlagObject(o,A))
					return true;
			}
		}
		return false;
	}

	protected CompiledZapperMaskEntry levelCompiledHelper(final String str, final char c)
	{
		final Vector<Object> parms=new Vector<Object>();
		Integer entryType=null;
		if(str.startsWith(c+">=")&&(CMath.isNumber(str.substring(3).trim())))
		{
			entryType=getMaskCodes().get("+LVLGE");
			parms.addElement(Integer.valueOf(CMath.s_int(str.substring(3).trim())));
		}
		else
		if(str.startsWith(c+"<=")&&(CMath.isNumber(str.substring(3).trim())))
		{
			entryType=getMaskCodes().get("+LVLLE");
			parms.addElement(Integer.valueOf(CMath.s_int(str.substring(3).trim())));
		}
		else
		if(str.startsWith(c+">")&&(CMath.isNumber(str.substring(2).trim())))
		{
			entryType=getMaskCodes().get("+LVLGR");
			parms.addElement(Integer.valueOf(CMath.s_int(str.substring(2).trim())));
		}
		else
		if(str.startsWith(c+"<")&&(CMath.isNumber(str.substring(2).trim())))
		{
			entryType=getMaskCodes().get("+LVLLT");
			parms.addElement(Integer.valueOf(CMath.s_int(str.substring(2).trim())));
		}
		else
		if(str.startsWith(c+"=")&&(CMath.isNumber(str.substring(2).trim())))
		{
			entryType=getMaskCodes().get("+LVLEQ");
			parms.addElement(Integer.valueOf(CMath.s_int(str.substring(2).trim())));
		}
		if((entryType==null)||(parms.size()==0))
			return null;
		return new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0]));
	}

	protected StringBuilder levelHelp(final String lvl, final char c, final String append)
	{
		if(lvl.startsWith(c+">=")&&(CMath.isNumber(lvl.substring(3).trim())))
			return new StringBuilder(append+"levels greater than or equal to "+lvl.substring(3).trim()+".  ");
		else
		if(lvl.startsWith(c+"<=")&&(CMath.isNumber(lvl.substring(3).trim())))
			return new StringBuilder(append+"levels less than or equal to "+lvl.substring(3).trim()+".  ");
		else
		if(lvl.startsWith(c+">")&&(CMath.isNumber(lvl.substring(2).trim())))
			return new StringBuilder(append+"levels greater than "+lvl.substring(2).trim()+".  ");
		else
		if(lvl.startsWith(c+"<")&&(CMath.isNumber(lvl.substring(2).trim())))
			return new StringBuilder(append+"levels less than "+lvl.substring(2).trim()+".  ");
		else
		if(lvl.startsWith(c+"=")&&(CMath.isNumber(lvl.substring(2).trim())))
			return new StringBuilder(append+"level "+lvl.substring(2).trim()+".  ");
		return new StringBuilder("");
	}

	protected int determineSeason(String str)
	{
		str=str.toUpperCase().trim();
		if(str.length()==0) return -1;
		for(int i=0;i<TimeClock.SEASON_DESCS.length;i++)
			if(TimeClock.SEASON_DESCS[i].startsWith(str))
				return i;
		return -1;
	}
	
	protected int levelMinHelp(final String lvl, final char c, final int minMinLevel, final boolean reversed)
	{
		if(lvl.startsWith(c+">=")&&(CMath.isNumber(lvl.substring(3).trim())))
			return reversed?minMinLevel:CMath.s_int(lvl.substring(3).trim());
		else
		if(lvl.startsWith(c+"<=")&&(CMath.isNumber(lvl.substring(3).trim())))
			return reversed?CMath.s_int(lvl.substring(3).trim())+1:minMinLevel;
		else
		if(lvl.startsWith(c+">")&&(CMath.isNumber(lvl.substring(2).trim())))
			return reversed?minMinLevel:CMath.s_int(lvl.substring(2).trim())+1;
		else
		if(lvl.startsWith(c+"<")&&(CMath.isNumber(lvl.substring(2).trim())))
			return reversed?CMath.s_int(lvl.substring(2).trim()):minMinLevel;
		else
		if(lvl.startsWith(c+"=")&&(CMath.isNumber(lvl.substring(2).trim())))
			return reversed?minMinLevel:CMath.s_int(lvl.substring(2).trim());
		return Integer.MIN_VALUE;
	}

	protected boolean fromHereEqual(final List<String> V, final char plusMinus, final int fromHere, final String find)
	{
		for(int v=fromHere;v<V.size();v++)
		{
			final String str=V.get(v);
			if(str.length()==0) continue;
			if(getMaskCodes().containsKey(str))
				return false;
			if(str.equalsIgnoreCase(plusMinus+find)) return true;
		}
		return false;
	}

	protected boolean fromHereStartsWith(final List<String> V, final char plusMinus, final int fromHere, final String find)
	{
		for(int v=fromHere;v<V.size();v++)
		{
			final String str=V.get(v);
			if(str.length()==0) continue;
			if(getMaskCodes().containsKey(str))
				return false;
			if(str.startsWith(plusMinus+find)) return true;
		}
		return false;
	}

	protected Faction.FRange getRange(final String s)
	{
		return CMLib.factions().getFactionRangeByCodeName(s);
	}

	protected boolean fromHereEndsWith(final List<String> V, final char plusMinus, final int fromHere, final String find)
	{
		for(int v=fromHere;v<V.size();v++)
		{
			final String str=V.get(v);
			if(str.length()==0) continue;
			if(getMaskCodes().containsKey(str))
				return false;
			if((str.charAt(0)==plusMinus)&&str.endsWith(find))
				return true;
		}
		return false;
	}

	public String maskDesc(final String text){return maskDesc(text,false);}

	public int countQuals(final List<String> V, final int v, final String startsWith)
	{
		int ct=0;
		for(int v2=v+1;v2<V.size();v2++)
		{
			final String str2=V.get(v2);
			if(zapCodes.containsKey(str2))
				break;
			if(str2.startsWith(startsWith))
				ct++;
		}
		return ct;
	}
	
	public boolean multipleQuals(final List<String> V, final int v, final String startsWith)
	{
		return countQuals(V,v,startsWith)>1;
	}
	
	public String maskDesc(final String text, final boolean skipFirstWord)
	{
		if(text.trim().length()==0) return "Anyone";
		StringBuilder buf=new StringBuilder("");
		final Map<String,Integer> zapCodes=getMaskCodes();
		final Vector<String> V=CMParms.parse(text.toUpperCase());
		int val=-1;
		for(int v=0;v<V.size();v++)
		{
			final String str=V.elementAt(v);
			val=-1;
			if(zapCodes.containsKey(str))
				switch(zapCodes.get(str).intValue())
				{
				case 0: // -class
					{
					buf.append(skipFirstWord?"Only ":"Allows only ");
					for(final SavedClass C : charClasses())
					{
						final String cstr=C.plusNameStart;
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(str2.length()==0) continue;
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith(cstr))
								buf.append(C.name+", ");
						}
					}
					if(buf.toString().endsWith(", "))
						buf=new StringBuilder(buf.substring(0,buf.length()-2));
					buf.append(".  ");
					}
					break;
				case 1: // -baseclass
					{
						buf.append(skipFirstWord?"Only ":"Allows only ");
						final HashSet<String> seenBase=new HashSet<String>();
						for(final SavedClass C : charClasses())
						{
							final String cstr=C.plusBaseClassStart;
							if(!seenBase.contains(C.baseClass))
							{
								seenBase.add(C.baseClass);
								for(int v2=v+1;v2<V.size();v2++)
								{
									final String str2=V.elementAt(v2);
									if(str2.length()==0) continue;
									if(zapCodes.containsKey(str2))
										break;
									if(str2.startsWith(cstr))
										buf.append(C.baseClass+" types, ");
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 2: // -Race
					{
						buf.append(skipFirstWord?"Only ":"Allows only ");
						LinkedList<String> cats=new LinkedList<String>();
						for(final SavedRace R : races())
						{
							if((!cats.contains(R.name)
							&&(fromHereStartsWith(V,'+',v+1,R.nameStart))))
							   cats.add(R.name);
						}
						for(final String s : cats)
							buf.append(s+", ");
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 12: // -Racecats
					{
						buf.append(skipFirstWord?"Only these racial categor"+(multipleQuals(V,v,"+")?"ies":"y")+" ":"Allows only these racial categor"+(multipleQuals(V,v,"+")?"ies":"y")+" ");
						final LinkedList<String> cats=new LinkedList<String>();
						for(final SavedRace R : races())
						{
							if((!cats.contains(R.racialCategory)
							&&(fromHereStartsWith(V,'+',v+1,R.upperCatName))))
							   cats.add(R.racialCategory);
						}
						for(final String s : cats)
							buf.append(s+", ");
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 3: // -Alignment
					{
						buf.append(skipFirstWord?"Only ":"Allows only ");
						if(fromHereStartsWith(V,'+',v+1,Faction.Align.EVIL.toString().substring(0,3)))
							buf.append(Faction.Align.EVIL.toString().toLowerCase()+", ");
						if(fromHereStartsWith(V,'+',v+1,Faction.Align.GOOD.toString().substring(0,3)))
							buf.append(Faction.Align.GOOD.toString().toLowerCase()+", ");
						if(fromHereStartsWith(V,'+',v+1,Faction.Align.NEUTRAL.toString().substring(0,3)))
							buf.append(Faction.Align.NEUTRAL.toString().toLowerCase()+", ");
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 4: // -Gender
					{
						buf.append(skipFirstWord?"Only ":"Allows only ");
						if(fromHereStartsWith(V,'+',v+1,"MALE"))
							buf.append("Male, ");
						if(fromHereStartsWith(V,'+',v+1,"FEMALE"))
							buf.append("Female, ");
						if(fromHereStartsWith(V,'+',v+1,"NEUTER"))
							buf.append("Neuter");
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 5: // -Levels
					{
						for(int v2=v+1;v2<V.size();v2++)
							buf.append(levelHelp(V.elementAt(v2),'+',skipFirstWord?"Only ":"Allows only "));
					}
					break;
				case 6: // -ClassLevels
					{
						for(int v2=v+1;v2<V.size();v2++)
							buf.append(levelHelp(V.elementAt(v2),'+',skipFirstWord?"Only class ":"Allows only class "));
					}
					break;
				case 103: // -MaxclassLevels
					{
						for(int v2=v+1;v2<V.size();v2++)
							buf.append(levelHelp(V.elementAt(v2),'+',skipFirstWord?"Only highest class ":"Allows only highest class "));
					}
					break;
				case 7: // -Tattoos
					{
						buf.append((skipFirstWord?"The":"Requires")+" the following tattoo"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 8: // +Tattoos
					{
						buf.append("Disallows the following tattoo"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 120: // -Mood
					{
						buf.append((skipFirstWord?"The":"Requires")+" the following mood"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
				break;
				case 121: // +Mood
					{
						buf.append("Disallows the following mood"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 79: // -Security
					{
						buf.append((skipFirstWord?"The":"Requires")+" following security flag"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
				break;
				case 80: // +security
					{
						buf.append("Disallows the following security flag"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 81: // -expertises
					{
						buf.append((skipFirstWord?"The":"Requires")+" following expertise"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								ExpertiseLibrary.ExpertiseDefinition E=CMLib.expertises().getDefinition(str2.substring(1).toUpperCase().trim());
								if(E!=null) buf.append(E.name+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
				break;
				case 82: // +expertises
					{
						buf.append("Disallows the following expertise"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								ExpertiseLibrary.ExpertiseDefinition E=CMLib.expertises().getDefinition(str2.substring(1).toUpperCase().trim());
								if(E!=null) buf.append(E.name+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 101: // -skillflags
					{
						buf.append((skipFirstWord?"A":"Requires a")+" skill of type: ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								Vector<String> V3=CMParms.parseAny(str2.substring(1),'&',true);
								String str3=null;
								for(int v3=0;v3<V3.size();v3++)
								{
									str3=CMStrings.replaceAll(CMStrings.capitalizeAndLower(V3.elementAt(v3)),"_"," ");
									if(str3.startsWith("!"))
										buf.append("not "+str3.substring(1));
									else
										buf.append(str3);
									if(v3<(V3.size()-1))
										buf.append(" and ");
									else
										buf.append(", ");
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
				break;
				case 83: // -skills
					{
						buf.append((skipFirstWord?"O":"Requires o")+"ne of the following skills: ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								int prof=0;
								str2=str2.substring(1);
								int x=str2.indexOf('(');
								if(x>0)
								{
									if(str2.endsWith(")"))
										prof=CMath.s_int(str2.substring(x+1,str2.length()-1));
									str2=str2.substring(0,x);
								}
								final Ability A=CMClass.getAbility(str2);
								if(A!=null)
								{
									if(prof<=0)
										buf.append(A.name()+", ");
									else
										buf.append(A.name()+" at "+prof+"% proficiency, ");
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
				break;
				case 84: // +skills
					{
						buf.append("Disallows the following skill"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								int prof=0;
								str2=str2.substring(1);
								int x=str2.indexOf('(');
								if(x>0)
								{
									if(str2.endsWith(")"))
										prof=CMath.s_int(str2.substring(x+1,str2.length()-1));
									str2=str2.substring(0,x);
								}
								final Ability A=CMClass.getAbility(str2);
								if(A!=null)
								{
									if(prof<=0)
										buf.append(A.name()+", ");
									else
										buf.append(A.name()+" at more than "+prof+"% proficiency, ");
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 102: // +skillflag
					{
						buf.append("Disallows the skill of type: ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								Vector<String> V3=CMParms.parseAny(str2.substring(1),'&',true);
								String str3=null;
								for(int v3=0;v3<V3.size();v3++)
								{
									str3=CMStrings.replaceAll(CMStrings.capitalizeAndLower(V3.elementAt(v3)),"_"," ");
									if(str3.startsWith("!"))
										buf.append("not "+str3.substring(1));
									else
										buf.append(str3);
									if(v3<(V3.size()-1))
										buf.append(" and ");
									else
										buf.append(", ");
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 14: // -Clan
					{
						buf.append((skipFirstWord?"M":"Requires m")+"embership in the following clan"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 15: // +Clan
					{
						buf.append("Disallows the following clan"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 50: // -Material
					{
						buf.append((skipFirstWord?"C":"Requires c")+"onstruction from the following material"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								final int code=CMLib.materials().getMaterialCode(str2.substring(1),false);
								if(code>=0)
									buf.append(RawMaterial.Material.findByMask(code).noun()+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 49: // +Material
					{
						buf.append("Disallows items of the following material"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								final int code=CMLib.materials().getMaterialCode(str2.substring(1),false);
								if(code>=0)
									buf.append(RawMaterial.Material.findByMask(code).noun()+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 58: // -wornon
					{
						buf.append((skipFirstWord?"A":"Requires a")+"bility to be worn: ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								long code=Wearable.CODES.FIND_endsWith(str2.substring(1));
								if(code>=0)
									buf.append(Wearable.CODES.NAME(code)+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 57: // +wornon
					{
						buf.append("Disallows items capable of being worn: ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								long code=Wearable.CODES.FIND_endsWith(str2.substring(1));
								if(code>=0)
									buf.append(Wearable.CODES.NAME(code)+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 72: // -senses
					{
						buf.append((skipFirstWord?"The":"Requires")+" following sense"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								final int code=CMLib.flags().getSensesCode(str2.substring(1));
								if(code>=0)
									buf.append(PhyStats.CAN_SEE_DESCS[code]+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 71: // +senses
					{
						buf.append("Disallows the following sense"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								final int code=CMLib.flags().getSensesCode(str2.substring(1));
								if(code>=0)
									buf.append(PhyStats.CAN_SEE_DESCS[code]+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 73: // +HOUR
					{
						buf.append("Disallowed during the following time"+(multipleQuals(V,v,"-")?"s":"")+" of the day: ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
								buf.append(CMath.s_int(str2.substring(1).trim())+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 74: // -HOUR
					{
						buf.append((skipFirstWord?"Only ":"Allowed only ")+"during the following time"+(multipleQuals(V,v,"+")?"s":"")+" of the day: ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
								buf.append(CMath.s_int(str2.substring(1).trim())+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 75: // +season
					{
						buf.append("Disallowed during the following season"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
								if(CMath.isInteger(str2.substring(1).trim()))
								{
									int season=CMath.s_int(str2.substring(1).trim());
									if((season>=0)&&(season<TimeClock.SEASON_DESCS.length))
										buf.append(TimeClock.SEASON_DESCS[season]+", ");
								}
								else
								{
									int season=determineSeason(str2.substring(1).trim());
									if((season>=0)&&(season<TimeClock.SEASON_DESCS.length))
										buf.append(TimeClock.SEASON_DESCS[season]+", ");
								}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 76: // -season
					{
						buf.append((skipFirstWord?"Only ":"Allowed only ")+"during the following season"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
								if(CMath.isInteger(str2.substring(1).trim()))
								{
									int season=CMath.s_int(str2.substring(1).trim());
									if((season>=0)&&(season<TimeClock.SEASON_DESCS.length))
										buf.append(TimeClock.SEASON_DESCS[season]+", ");
								}
								else
								{
									int season=determineSeason(str2.substring(1).trim());
									if((season>=0)&&(season<TimeClock.SEASON_DESCS.length))
										buf.append(TimeClock.SEASON_DESCS[season]+", ");
								}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;

				case 104: // +weather
				{
					buf.append("Disallowed during the following weather condition"+(multipleQuals(V,v,"-")?"s":"")+": ");
					for(int v2=v+1;v2<V.size();v2++)
					{
						final String str2=V.elementAt(v2);
						if(zapCodes.containsKey(str2))
							break;
						if(str2.startsWith("-"))
							if(CMath.isInteger(str2.substring(1).trim()))
							{
								int weather=CMath.s_int(str2.substring(1).trim());
								if((weather>=0)&&(weather<Climate.WEATHER_DESCS.length))
									buf.append(CMStrings.capitalizeAndLower(Climate.WEATHER_DESCS[weather])+", ");
							}
							else
							{
								int weather=CMParms.indexOf(Climate.WEATHER_DESCS,str2.substring(1).toUpperCase().trim());
								if((weather>=0)&&(weather<Climate.WEATHER_DESCS.length))
									buf.append(CMStrings.capitalizeAndLower(Climate.WEATHER_DESCS[weather])+", ");
							}
					}
					if(buf.toString().endsWith(", "))
						buf=new StringBuilder(buf.substring(0,buf.length()-2));
					buf.append(".  ");
				}
				break;
			case 105: // -weather
				{
					buf.append((skipFirstWord?"Only ":"Allowed only ")+"during the following weather condition"+(multipleQuals(V,v,"+")?"s":"")+": ");
					for(int v2=v+1;v2<V.size();v2++)
					{
						final String str2=V.elementAt(v2);
						if(zapCodes.containsKey(str2))
							break;
						if(str2.startsWith("+"))
							if(CMath.isInteger(str2.substring(1).trim()))
							{
								int weather=CMath.s_int(str2.substring(1).trim());
								if((weather>=0)&&(weather<Climate.WEATHER_DESCS.length))
									buf.append(CMStrings.capitalizeAndLower(Climate.WEATHER_DESCS[weather])+", ");
							}
							else
							{
								int weather=CMParms.indexOf(Climate.WEATHER_DESCS,str2.substring(1).toUpperCase().trim());
								if((weather>=0)&&(weather<Climate.WEATHER_DESCS.length))
									buf.append(CMStrings.capitalizeAndLower(Climate.WEATHER_DESCS[weather])+", ");
							}
					}
					if(buf.toString().endsWith(", "))
						buf=new StringBuilder(buf.substring(0,buf.length()-2));
					buf.append(".  ");
				}
				break;

				case 77: // +month
					{
						buf.append("Disallowed during the following month"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
								buf.append(CMath.s_int(str2.substring(1).trim())+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 78: // -month
					{
						buf.append((skipFirstWord?"Only ":"Allowed only ")+"during the following month"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
								buf.append(CMath.s_int(str2.substring(1).trim())+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 106: // +day
					{
						buf.append("Disallowed during the following day"+(multipleQuals(V,v,"-")?"s":"")+" of the month: ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
								buf.append(CMath.s_int(str2.substring(1).trim())+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 107: // -day
					{
						buf.append((skipFirstWord?"Only ":"Allowed only ")+"on the following day"+(multipleQuals(V,v,"+")?"s":"")+" of the month: ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
								buf.append(CMath.s_int(str2.substring(1).trim())+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;

				case 85: // +quallvl
					if((v+1)<V.size())
					{
						final Ability A=CMClass.getAbility(V.elementAt(v+1));
						if(A!=null)
						{
							int adjustment=0;
							if(((v+2)<V.size())&&(CMath.isInteger(V.elementAt(v+2))))
								adjustment=CMath.s_int(V.elementAt(v+2));
							buf.append(A.Name());
							if(adjustment!=0)
								buf.append("Qualifies for "+A.Name());
							else
							if(adjustment<0)
								buf.append((-adjustment)+" levels before qualifying for "+A.Name());
							else
								buf.append(adjustment+" levels after qualifying for "+A.Name());
							buf.append(".  ");
						}
					}
					break;
				case 86: // -quallvl
					if((v+1)<V.size())
					{
						final Ability A=CMClass.getAbility(V.elementAt(v+1));
						if(A!=null)
						{
							int adjustment=0;
							if(((v+2)<V.size())&&(CMath.isInteger(V.elementAt(v+2))))
								adjustment=CMath.s_int(V.elementAt(v+2));
							buf.append(A.Name());
							if(adjustment!=0)
								buf.append("Does not qualify for "+A.Name());
							else
							if(adjustment<0)
								buf.append("Still prior to "+(-adjustment)+" levels before qualifying for "+A.Name());
							else
								buf.append("Still prior to "+adjustment+" levels after qualifying for "+A.Name());
							buf.append(".  ");
						}
					}
					break;
				case 70: // -disposition
					{
						buf.append((skipFirstWord?"The":"Requires")+" following disposition"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								final int code=CMLib.flags().getDispositionCode(str2.substring(1));
								if(code>=0)
									buf.append(PhyStats.IS_DESCS[code]+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 69: // +disposition
					{
						buf.append("Disallows the following disposition"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								final int code=CMLib.flags().getDispositionCode(str2.substring(1));
								if(code>=0)
									buf.append(PhyStats.IS_DESCS[code]+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 52: // -Resource
					{
						buf.append((skipFirstWord?"C":"Requires c")+"onstruction from the following material"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								final int code=CMLib.materials().getResourceCode(str2.substring(1),false);
								if(code>=0)
									buf.append(CMStrings.capitalizeAndLower(RawMaterial.CODES.NAME(code))+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 51: // +Resource
					{
						buf.append("Disallows items of the following material"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								final int code=CMLib.materials().getResourceCode(str2.substring(1),false);
								if(code>=0)
									buf.append(CMStrings.capitalizeAndLower(RawMaterial.CODES.NAME(code))+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 53: // -JavaClass
					{
						buf.append((skipFirstWord?"B":"Requires b")+"eing of the following type"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 54: // +JavaClass
					{
						buf.append("Disallows being of the following type"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 44: // -Deity
					{
						buf.append((skipFirstWord?"W":"Requires w")+"orshipping the following deity"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 45: // +Deity
					{
						buf.append("Disallows the worshippers of: ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 16: // +Names
					{
						buf.append("Disallows the following mob/player name"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 9: // -Names
					{
						buf.append((skipFirstWord?"The":"Requires")+" following name"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 113: // -Questwin
					{
						buf.append((skipFirstWord?"Completing":"Requires completing")+" the following quest"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								Quest Q=CMLib.quests().fetchQuest(str2.substring(1));
								if(Q==null)
									buf.append(str2.substring(1)+", ");
								else
								if((Q.displayName()!=null)&&(Q.displayName().trim().length()>0))
									buf.append(Q.displayName()+", ");
								else
									buf.append(Q.name()+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 114: // +Questwin
					{
						buf.append("Disallows those who`ve won the following quest"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								Quest Q=CMLib.quests().fetchQuest(str2.substring(1));
								if(Q==null)
									buf.append(str2.substring(1)+", ");
								else
								if((Q.displayName()!=null)&&(Q.displayName().trim().length()>0))
									buf.append(Q.displayName()+", ");
								else
									buf.append(Q.name()+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 10: // -Player
					buf.append("Disallows players.  ");
					break;
				case 11: // -MOB
					buf.append("Disallows mobs/npcs.  ");
					break;
				case 112: // +races
					{
						buf.append("Disallows the following race"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 13: // +racecats
					{
						buf.append("Disallows the following racial category"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 17: // -anyclass
					{
						buf.append((skipFirstWord?"L":"Requires l")+"evels in one of the following:  ");
						for(final SavedClass C : charClasses())
						{
							if(fromHereStartsWith(V,'+',v+1,C.nameStart))
								buf.append(C.name+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 18: // +anyclass
					{
						buf.append("Disallows any levels in any of the following:  ");
						for(final SavedClass C : charClasses())
						{
							if(fromHereStartsWith(V,'-',v+1,C.nameStart))
								buf.append(C.name+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 19: // +adjstr
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" strength of at least "+val+".  ");
					break;
				case 20: // +adjint
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"An":"Requires an")+" intelligence of at least "+val+".  ");
					break;
				case 21: // +adjwis
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" wisdom of at least "+val+".  ");
					break;
				case 22: // +adjdex
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" dexterity of at least "+val+".  ");
					break;
				case 23: // -adjcha
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" constitution of at least "+val+".  ");
					break;
				case 24: // +adjcha
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" charisma of at least "+val+".  ");
					break;
				case 25: // -adjstr
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" strength of at most "+val+".  ");
					break;
				case 26: // -adjint
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"An":"Requires an")+" intelligence of at most "+val+".  ");
					break;
				case 27: // -adjwis
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" wisdom of at most "+val+".  ");
					break;
				case 28: // -adjdex
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" dexterity of at most "+val+".  ");
					break;
				case 29: // -adjcon
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" constitution of at most "+val+".  ");
					break;
				case 30: // -adjcha
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" charisma of at most "+val+".  ");
					break;
				case 87: // +str
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base strength of at least "+val+".  ");
					break;
				case 88: // +int
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base intelligence of at least "+val+".  ");
					break;
				case 89: // +wis
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base wisdom of at least "+val+".  ");
					break;
				case 90: // +dex
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base dexterity of at least "+val+".  ");
					break;
				case 91: // +con
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base constitution of at least "+val+".  ");
					break;
				case 92: // +cha
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base charisma of at least "+val+".  ");
					break;
				case 93: // -str
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base strength of at most "+val+".  ");
					break;
				case 94: // -int
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base intelligence of at most "+val+".  ");
					break;
				case 95: // -wis
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base wisdom of at most "+val+".  ");
					break;
				case 96: // -dex
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base dexterity of at most "+val+".  ");
					break;
				case 97: // -con
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base constitution of at most "+val+".  ");
					break;
				case 98: // -cha
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" base charisma of at most "+val+".  ");
					break;
				case 122: // -chance
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"":"Allowed ")+" "+(100-val)+"% of the time.  ");
					break;
				case 55: // +able
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" magic/ability of at most "+val+".  ");
					break;
				case 56: // -able
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" magic/ability of at least "+val+".  ");
					break;
				case 59: // +value
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" value of at most "+val+".  ");
					break;
				case 60: // -value
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" value of at least "+val+".  ");
					break;
				case 61: // +weight
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" weight/encumbrance of at most "+val+".  ");
					break;
				case 62: // -weight
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" weight/encumbrance of at least "+val+".  ");
					break;
				case 63: // +armor
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" armor rating of at most "+val+".  ");
					break;
				case 64: // -armor
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" armor rating of at least "+val+".  ");
					break;
				case 65: // +damage
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" damage ability of at most "+val+".  ");
					break;
				case 66: // -damage
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" damage ability of at least "+val+".  ");
					break;
				case 67: // +attack
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"An":"Requires an")+" attack bonus of at most "+val+".  ");
					break;
				case 68: // -attack
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"An":"Requires an")+" attack bonus of at least "+val+".  ");
					break;
				case 32: // +Area
					{
						buf.append("Disallows the following area"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 31: // -Area
					{
						buf.append((skipFirstWord?"The":"Requires the")+" following area"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 99: // +Home
				{
					buf.append("Disallows those whose home is the following area"+(multipleQuals(V,v,"-")?"s":"")+": ");
					for(int v2=v+1;v2<V.size();v2++)
					{
						final String str2=V.elementAt(v2);
						if(zapCodes.containsKey(str2))
							break;
						if(str2.startsWith("-"))
							buf.append(str2.substring(1)+", ");
					}
					if(buf.toString().endsWith(", "))
						buf=new StringBuilder(buf.substring(0,buf.length()-2));
					buf.append(".  ");
				}
				break;
				case 100: // -Home
					{
						buf.append((skipFirstWord?"From the":"Requires being from the")+" following area"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 33: // +Item
					{
						buf.append((skipFirstWord?"The":"Requires the")+" following item"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
								buf.append(str2.substring(1)+", ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 48: // -Worn
				{
					buf.append((skipFirstWord?"W":"Requires w")+"earing the following item"+(multipleQuals(V,v,"+")?"s":"")+": ");
					for(int v2=v+1;v2<V.size();v2++)
					{
						final String str2=V.elementAt(v2);
						if(zapCodes.containsKey(str2))
							break;
						if((str2.startsWith("+"))||(str2.startsWith("-")))
							buf.append(str2.substring(1)+", ");
					}
					if(buf.toString().endsWith(", "))
						buf=new StringBuilder(buf.substring(0,buf.length()-2));
					buf.append(".  ");
				}
				break;
				case 42: // +Effects
					{
						buf.append("Disallows the following activities/effect"+(multipleQuals(V,v,"-")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								final Ability A=CMClass.getAbility(str2.substring(1));
								if(A!=null)
									buf.append(A.name()+", ");
								else
									buf.append(str2.substring(1)+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 43: // -Effects
					{
						buf.append((skipFirstWord?"P":"Requires p")+"articipation in the following activities/effect"+(multipleQuals(V,v,"+")?"s":"")+": ");
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								final Ability A=CMClass.getAbility(str2.substring(1));
								if(A!=null)
									buf.append(A.name()+", ");
								else
									buf.append(str2.substring(1)+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case 46: // -faction
				{
					buf.append((skipFirstWord?"The":"Requires the")+" following: ");
					for(int v2=v+1;v2<V.size();v2++)
					{
						final String str2=V.elementAt(v2);
						if(zapCodes.containsKey(str2))
							break;
						if(str2.startsWith("+"))
						{
							Faction.FRange FR=getRange(str2.substring(1).toUpperCase().trim());
							if(FR!=null)
							{
								String desc=CMLib.factions().rangeDescription(FR,"or ");
								if(desc.length()>0) buf.append(desc+"; ");
							}
						}
					}
					if(buf.toString().endsWith(", "))
						buf=new StringBuilder(buf.substring(0,buf.length()-2));
					if(buf.toString().endsWith("; "))
						buf=new StringBuilder(buf.substring(0,buf.length()-2));
					buf.append(".  ");
				}
				break;
				case 115: // -groupsize
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" group size of at most "+val+".  ");
					break;
				case 116: // +groupsize
					val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
					buf.append((skipFirstWord?"A":"Requires a")+" group size of at least "+val+".  ");
					break;
				case 118: // -if
					buf.append((skipFirstWord?"n":"Requires n")+"ot meeting the following condition(s):");
					for(int v2=v+1;v2<V.size();v2++)
					{
						final String str2=V.elementAt(v2);
						if(zapCodes.containsKey(str2))
							break;
						buf.append(str2).append(" ");
					}
					break;
				case 119: // +if
					buf.append((skipFirstWord?"m":"Requires m")+"meets the following condition(s):");
					for(int v2=v+1;v2<V.size();v2++)
					{
						final String str2=V.elementAt(v2);
						if(zapCodes.containsKey(str2))
							break;
						buf.append(str2).append(" ");
					}
					break;
				case 117: // +baseclass
				{
					buf.append("Disallows the following types"+(multipleQuals(V,v,"-")?"s":"")+": ");
					HashSet<String> seenBase=new HashSet<String>();
					for(final SavedClass C : charClasses())
					{
						if(!seenBase.contains(C.baseClass))
						{
							seenBase.add(C.baseClass);
							if(fromHereStartsWith(V,'-',v+1,C.baseClassStart))
								buf.append(C.baseClass+" types, ");
						}
					}
					if(buf.toString().endsWith(", "))
						buf=new StringBuilder(buf.substring(0,buf.length()-2));
					buf.append(".  ");
					break;
				}
				}
			else
			{
				for(final SavedClass C : charClasses())
				{
					if(str.startsWith("-"+C.nameStart))
						buf.append("Disallows "+C.name+".  ");
				}
				final LinkedList<String> cats=new LinkedList<String>();
				for(final SavedRace R : races())
				{
					if((str.startsWith(R.minusCatNameStart))&&(!cats.contains(R.racialCategory)))
					{
						cats.add(R.racialCategory);
						buf.append("Disallows "+R.racialCategory+".  ");
					}
				}
				if(str.startsWith("-"+Faction.Align.EVIL.toString().substring(0,3)))
					buf.append("Disallows "+Faction.Align.EVIL.toString().toLowerCase()+".  ");
				if(str.startsWith("-"+Faction.Align.GOOD.toString().substring(0,3)))
					buf.append("Disallows "+Faction.Align.GOOD.toString().toLowerCase()+".  ");
				if(str.startsWith("-"+Faction.Align.NEUTRAL.toString().substring(0,3)))
					buf.append("Disallows "+Faction.Align.NEUTRAL.toString().toLowerCase()+".  ");
				if(str.startsWith("-MALE"))
					buf.append("Disallows Males.  ");
				if(str.startsWith("-FEMALE"))
					buf.append("Disallows Females.  ");
				if(str.startsWith("-NEUTER"))
					buf.append((skipFirstWord?"Only ":"Allows only ")+"Males and Females.  ");
				buf.append(levelHelp(str,'-',"Disallows "));
				if(str.startsWith("-"))
				{
					Faction.FRange FR=getRange(str.substring(1));
					String desc=CMLib.factions().rangeDescription(FR,"and ");
					if(desc.length()>0) buf.append("Disallows "+desc);
				}
			}
		}

		if(buf.length()==0) buf.append("Anyone.");
		return buf.toString();
	}

	public boolean syntaxCheck(final String mask, final List<String> errorSink)
	{
		if(mask.trim().length()==0) return true;
		Vector<String> V=CMParms.parse(mask.toUpperCase());
		for(int v=0;v<V.size();v++)
		{
			String str=V.elementAt(v);
			Map<String,Integer> zapCodes=getMaskCodes();
			if(zapCodes.containsKey(str)) return true;
			for(final SavedClass C : charClasses())
			{
				if(str.startsWith(C.minusNameStart))
					return true;
			}
			for(final SavedRace R : races())
			{
				if(str.startsWith(R.minusNameStart))
					return true;
				if(str.startsWith(R.minusCatNameStart))
					return true;
			}
			if(str.startsWith("-"+Faction.Align.EVIL.toString().substring(0,3)))
				return true;
			if(str.startsWith("-"+Faction.Align.GOOD.toString().substring(0,3)))
				return true;
			if(str.startsWith("-"+Faction.Align.NEUTRAL.toString().substring(0,3)))
				return true;
			if(str.startsWith("-MALE"))
				return true;
			if(str.startsWith("-FEMALE"))
				return true;
			if(str.startsWith("-NEUTER"))
				return true;
			if(levelHelp(str,'-',"").length()>0)
				return true;
			if((str.startsWith("-"))
			&&(CMLib.factions().isRangeCodeName(str.substring(1))))
				return true;
		}
		errorSink.add("No valid zapper codes found.");
		return false;
	}
	
	public List<String> getAbilityEduReqs(final String text)
	{
		final Vector<String> preReqs=new Vector<String>();
		if(text.trim().length()==0)
			return preReqs;
		final Map<String,Integer> zapCodes=getMaskCodes();
		final Vector<String> V=CMParms.parse(text.toUpperCase());
		String str2;
		for(int v=0;v<V.size();v++)
		{
			final String str=V.elementAt(v);
			if(zapCodes.containsKey(str))
			{
				switch(zapCodes.get(str).intValue())
				{
				case 81: // -expertises
					{
						for(int v2=v+1;v2<V.size();v2++)
						{
							str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								ExpertiseLibrary.ExpertiseDefinition E=CMLib.expertises().getDefinition(str2.substring(1).toUpperCase().trim());
								if(E!=null) preReqs.addElement(E.ID);
							}
						}
					}
					break;
				case 83: // -skills
					{
						for(int v2=v+1;v2<V.size();v2++)
						{
							str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								str2=str2.substring(1);
								int x=str2.indexOf('(');
								if(x>0) str2=str2.substring(0,x);
								final Ability A=CMClass.getAbility(str2);
								if((A!=null)&&(!preReqs.contains(A.ID())))
									preReqs.addElement(A.ID());
							}
						}
					}
					break;
				case 101: // -skillflag
				{
					final Vector<Object> objs=new Vector<Object>();
					Object o=null;
					for(int v2=v+1;v2<V.size();v2++)
					{
						str2=V.elementAt(v2);
						if(zapCodes.containsKey(str2))
							break;
						if(str2.startsWith("+"))
						{
							str2=str2.substring(1);
							o=this.makeSkillFlagObject(str2);
							if(o!=null)
								objs.addElement(o);
						}
					}
					for(final Object O : objs)
						for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
						{
							final Ability A=e.nextElement();
							if((evaluateSkillFlagObject(O,A))
							&&(!preReqs.contains(A.ID())))
							{
								preReqs.addElement(A.ID());
							}
						}
				}
				break;
				}
			}
		}
		return preReqs;
	}

	public int minMaskLevel(final String text, final int minMinLevel)
	{
		int level=minMinLevel;
		final CompiledZapperMask cset=getPreCompiledMask(text);
		for(final CompiledZapperMaskEntry entry : cset.entries)
		{
			switch(entry.maskType)
			{
			case 5: // -level
			{
				for(int v=0;v<entry.parms.length-1;v+=2)
					switch(((Integer)entry.parms[v]).intValue())
					{
						case 37: // +lvlgr
							level=((Integer)entry.parms[v+1]).intValue()+1;
							break;
						case 38: // +lvlge
							level=((Integer)entry.parms[v+1]).intValue();
							break;
						case 39: // +lvlt
							level=minMinLevel;
							break;
						case 40: // +lvlle
							level=minMinLevel;
							break;
						case 41: // +lvleq
							level=((Integer)entry.parms[v+1]).intValue();
							break;
					}
			}
			break;
			case 6: // -classlevel
				{
					for(int v=0;v<entry.parms.length-1;v+=2)
						switch(((Integer)entry.parms[v]).intValue())
						{
						case 37: // +lvlgr
							level=((Integer)entry.parms[v+1]).intValue()+1;
							break;
						case 38: // +lvlge
							level=((Integer)entry.parms[v+1]).intValue();
							break;
						case 39: // +lvlt
							level=minMinLevel;
							break;
						case 40: // +lvlle
							level=minMinLevel;
							break;
						case 41: // +lvleq
							level=((Integer)entry.parms[v+1]).intValue();
							break;
						}
				}
				break;
			case 103: // -maxclasslevel
				{
					for(int v=0;v<entry.parms.length-1;v+=2)
						switch(((Integer)entry.parms[v]).intValue())
						{
						case 37: // +lvlgr
							level=((Integer)entry.parms[v+1]).intValue()+1;
							break;
						case 38: // +lvlge
							level=((Integer)entry.parms[v+1]).intValue();
							break;
						case 39: // +lvlt
							level=minMinLevel;
							break;
						case 40: // +lvlle
							level=minMinLevel;
							break;
						case 41: // +lvleq
							level=((Integer)entry.parms[v+1]).intValue();
							break;
						}
				}
				break;
			case 37: // +lvlgr
				level=minMinLevel;
				break;
			case 38: // +lvlge
				level=minMinLevel;
				break;
			case 39: // +lvlt
				level=((Integer)entry.parms[0]).intValue();
				break;
			case 40: // +lvlle
				level=((Integer)entry.parms[0]).intValue()+1;
				break;
			case 41: // +lvleq
				level=minMinLevel;
				break;
			}
		}
		return level;
	}

	public CompiledZapperMask maskCompile(final String text)
	{
		final Vector<CompiledZapperMaskEntry> buf=new Vector<CompiledZapperMaskEntry>();
		if(text.trim().length()==0) return new CompiledZapperMask(new boolean[]{false,false},buf.toArray(new CompiledZapperMaskEntry[0]));
		final Map<String,Integer> zapCodes=getMaskCodes();
		final Vector<String> V=CMParms.parse(text.toUpperCase());
		boolean buildItemFlag=false;
		boolean buildRoomFlag=false;
		Integer entryType;
		for(int v=0;v<V.size();v++)
		{
			final String str=V.elementAt(v);
			int val=-1;
			entryType=zapCodes.get(str);
			if(entryType!=null)
				switch(entryType.intValue())
				{
				case 0: // -class
					{
						final Vector<Object> parms=new Vector<Object>();
						for(final SavedClass C : charClasses())
						{
							if(fromHereStartsWith(V,'+',v+1,C.nameStart))
								parms.addElement(C.name);
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 1: // -baseclass
					{
						final Vector<Object> parms=new Vector<Object>();
						final HashSet<String> seenBase=new HashSet<String>();
						for(final SavedClass C : charClasses())
						{
							if(!seenBase.contains(C.baseClass))
							{
								seenBase.add(C.baseClass);
								if(fromHereStartsWith(V,'+',v+1,C.baseClassStart))
									parms.addElement(C.baseClass);
							}
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 117: // +baseclass
					{
						final Vector<Object> parms=new Vector<Object>();
						final HashSet<String> seenBase=new HashSet<String>();
						for(final SavedClass C : charClasses())
						{
							if(!seenBase.contains(C.baseClass))
							{
								seenBase.add(C.baseClass);
								if(fromHereStartsWith(V,'-',v+1,C.baseClassStart))
									parms.addElement(C.baseClass);
							}
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 2: // -Race
					{
						final Vector<Object> parms=new Vector<Object>();
						final LinkedList<String> cats=new LinkedList<String>();
						for(final SavedRace R : races())
						{
							if((!cats.contains(R.name)
							&&(fromHereStartsWith(V,'+',v+1,R.nameStart))))
							   cats.add(R.name);
						}
						for(final String s : cats)
							parms.addElement(s);
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 12: // -Racecats
					{
						final Vector<Object> parms=new Vector<Object>();
						final LinkedList<String> cats=new LinkedList<String>();
						for(final SavedRace R : races())
						{
							if((!cats.contains(R.racialCategory)
							&&(fromHereStartsWith(V,'+',v+1,R.upperCatName))))
							   cats.add(R.racialCategory);
						}
						for(final String s : cats)
							parms.addElement(s);
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 13: // +Racecats
					{
						final Vector<Object> parms=new Vector<Object>();
						final LinkedList<String> cats=new LinkedList<String>();
						for(final SavedRace R : races())
						{
							if((!cats.contains(R.racialCategory)
							&&(fromHereStartsWith(V,'-',v+1,R.upperCatName))))
							   cats.add(R.racialCategory);
						}
						for(final String s : cats)
							parms.addElement(s);
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 112: // +Race
					{
						final Vector<Object> parms=new Vector<Object>();
						for(final SavedRace R : races())
						{
							if(fromHereStartsWith(V,'-',v+1,R.upperName))
							   parms.addElement(R.name);
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 3: // -Alignment
					{
						final Vector<Object> parms=new Vector<Object>();
						if(fromHereStartsWith(V,'+',v+1,Faction.Align.EVIL.toString().substring(0,3)))
							parms.addElement(Faction.Align.EVIL.toString());
						if(fromHereStartsWith(V,'+',v+1,Faction.Align.GOOD.toString().substring(0,3)))
							parms.addElement(Faction.Align.GOOD.toString());
						if(fromHereStartsWith(V,'+',v+1,Faction.Align.NEUTRAL.toString().substring(0,3)))
							parms.addElement(Faction.Align.NEUTRAL.toString());
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 4: // -Gender
					{
						final Vector<Object> parms=new Vector<Object>();
						if(fromHereStartsWith(V,'+',v+1,"MALE"))
							parms.addElement("M");
						if(fromHereStartsWith(V,'+',v+1,"FEMALE"))
							parms.addElement("F");
						if(fromHereStartsWith(V,'+',v+1,"NEUTER"))
							parms.addElement("N");
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 5: // -Levels
				case 6: // -ClassLevels
				case 103: // -MaxclassLevels
					{
						final Vector<Object> parms=new Vector<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							final CompiledZapperMaskEntry e = levelCompiledHelper(str2,'+');
							if(e!=null)
							{
								parms.add(Integer.valueOf(e.maskType));
								parms.add(e.parms[0]);
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 43: // -Effect
				case 42: // +Effect
					{
						final Vector<Object> parms=new Vector<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
							{
								str2=str2.substring(1);
								CMObject A=CMClass.getAbility(str2);
								if(A==null) A=CMClass.getBehavior(str2);
								if(A==null) A=CMClass.getAbilityByName(str2,true);
								if(A==null) A=CMClass.getBehaviorByName(str2,true);
								if(A==null) A=CMClass.getAbilityByName(str2,false);
								if(A==null) A=CMClass.getBehaviorByName(str2,false);
								if(A!=null)
									parms.addElement(A.ID());
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 80: // +security
				case 79: // -security
				{
					final String plusMinus=(entryType.intValue()==80)?"+":"-"; 
					final Vector<CMSecurity.SecFlag> parms=new Vector<CMSecurity.SecFlag>();
					for(int v2=v+1;v2<V.size();v2++)
					{
						final String str2=V.elementAt(v2);
						if(zapCodes.containsKey(str2))
						{
							v=v2-1;
							break;
						}
						else
						if(str2.startsWith(plusMinus))
						{
							CMSecurity.SecFlag flag=(CMSecurity.SecFlag)CMath.s_valueOf(CMSecurity.SecFlag.class,str2.substring(1).toUpperCase().trim().replace(' ','_'));
							if(flag == null)
								Log.errOut("MUDZapper","Illegal security flag '"+str2);
							else
								parms.addElement(flag);
						}
						v=V.size();
					}
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new CMSecurity.SecFlag[0])));
					break;
				}
				case 7: // -Tattoos
				case 31: // -Area
					buildRoomFlag=true;
				//$FALL-THROUGH$
				case 120: // -Mood
				case 81: // -expertise
				case 14: // -Clan
				case 44: // -Deity
				case 9: // -Names
				case 113: // -Questwin
				case 100: // -Home
				case 53: // -JavaClass
					{
						final Vector<Object> parms=new Vector<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if(str2.startsWith("+"))
								parms.addElement(str2.substring(1));
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 47: // +Faction
				case 46: // -Faction
				{
					final String plusMinus=(entryType.intValue()==46)?"+":"-"; 
					final Vector<Object> parms=new Vector<Object>();
					for(int v2=v+1;v2<V.size();v2++)
					{
						final String str2=V.elementAt(v2);
						if(zapCodes.containsKey(str2))
						{
							v=v2-1;
							break;
						}
						else
						if(str2.startsWith(plusMinus))
						{
							final String str3=str2.substring(1).toUpperCase().trim();
							Faction.FRange FR=getRange(str3);
							if(FR!=null)
								parms.addElement(str3);
						}
						v=V.size();
					}
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					break;
				}
				case 8: // +Tattoos
				case 32: // +Area
					buildRoomFlag=true;
				//$FALL-THROUGH$
				case 121: // +Mood
				case 82: // +expertise
				case 15: // +Clan
				case 45: // +Deity
				case 16: // +Names
				case 114: // +Questwin
				case 99: // +Home
				case 54: // +JavaClass
					{
						final Vector<Object> parms=new Vector<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if(str2.startsWith("-"))
								parms.addElement(str2.substring(1));
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 83: // +skills
				case 84: // -skills
					{
						final Vector<Object> parms=new Vector<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
							{
								str2=str2.substring(1);
								int prof=0;
								int x=str2.indexOf('(');
								if(x>0)
								{
									if(str2.endsWith(")"))
										prof=CMath.s_int(str2.substring(x+1,str2.length()-1));
									str2=str2.substring(0,x);
								}
								final Ability A=CMClass.getAbility(str2);
								if(A!=null)
								{
									parms.addElement(A.ID());
									parms.addElement(Integer.valueOf(prof));
								}
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 101: // -skillflag
				case 102: // +skillflag
					{
						final Vector<Object> parms=new Vector<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
							{
								str2=str2.substring(1).toUpperCase();
								final Object o=makeSkillFlagObject(str2);
								if(o!=null) parms.addElement(o);
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 33: // -Item
				case 48: // -Worn
					{
						final Vector<Object> parms=new Vector<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("+"))||(str2.startsWith("-")))
								parms.addElement(str2.substring(1));
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 49: // +Material
				case 50: // -Material
					{
						final Vector<Object> parms=new Vector<Object>();
						buildItemFlag=true;
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
							{
								final int code=CMLib.materials().getMaterialCode(str2.substring(1),false);
								if(code>=0)
									parms.addElement(RawMaterial.Material.findByMask(code&RawMaterial.MATERIAL_MASK).desc());
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 57: // -WornOn
				case 58: // +WornOn
					{
						final Vector<Object> parms=new Vector<Object>();
						buildItemFlag=true;
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
							{
								final long code=Wearable.CODES.FIND_endsWith(str2.substring(1));
								if(code>=0) parms.addElement(Long.valueOf(code));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 69: // +Disposition
				case 70: // -Disposition
					{
						final Vector<Object> parms=new Vector<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
							{
								final int code=CMLib.flags().getDispositionCode(str2.substring(1));
								if(code>=0) parms.addElement(Integer.valueOf((int)CMath.pow(2,code)));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 71: // +Senses
				case 72: // -Senses
					{
						final Vector<Object> parms=new Vector<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
							{
								final int code=CMLib.flags().getSensesCode(str2.substring(1));
								if(code>=0) parms.addElement(Integer.valueOf((int)CMath.pow(2,code)));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 75: // +Season
				case 76: // -Season
					{
						final Vector<Object> parms=new Vector<Object>();
						buildRoomFlag=true;
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
							{
								if(CMath.isInteger(str2.substring(1).trim()))
									parms.addElement(Integer.valueOf(CMath.s_int(str2.substring(1).trim())));
								else
								if(determineSeason(str2.substring(1).trim())>=0)
									parms.addElement(Integer.valueOf(determineSeason(str2.substring(1).trim())));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 104: // +weather
				case 105: // -weather
					{
						final Vector<Object> parms=new Vector<Object>();
						buildRoomFlag=true;
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
							{
								if(CMath.isInteger(str2.substring(1).trim()))
									parms.addElement(Integer.valueOf(CMath.s_int(str2.substring(1).trim())));
								else
								if(CMParms.indexOf(Climate.WEATHER_DESCS,str2.substring(1).trim())>=0)
									parms.addElement(Integer.valueOf(CMParms.indexOf(Climate.WEATHER_DESCS,str2.substring(1).trim())));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 73: // +HOUR
				case 74: // -HOUR
				case 77: // +MONTH
				case 78: // -MONTH
				case 106: // +DAY
				case 107: // -DAY
					{
						final Vector<Object> parms=new Vector<Object>();
						buildRoomFlag=true;
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
								parms.addElement(Integer.valueOf(CMath.s_int(str2.substring(1).trim())));
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 85: // +quallvl
				case 86: // -quallvl
					if((v+1)<V.size())
					{
						final Ability A=CMClass.getAbility(V.elementAt(v+1));
						if(A!=null)
						{
							int adjustment=0;
							if(((v+2)<V.size())&&(CMath.isInteger(V.elementAt(v+2))))
								adjustment=CMath.s_int(V.elementAt(v+2));
							final Vector<Object> parms=new Vector<Object>();
							parms.addElement(A.ID());
							parms.addElement(Integer.valueOf(adjustment));
							buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
						}
					}
					break;
				case 110: // +subop
				case 111: // +subop
					buildRoomFlag=true;
				//$FALL-THROUGH$
				case 108: // +sysop
				case 109: // +sysop
				{
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),new Object[0]));
					break;
				}
				case 51: // +Resource
				case 52: // -Resource
					{
						final Vector<Object> parms=new Vector<Object>();
						buildItemFlag=true;
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
							{
								final int code=CMLib.materials().getResourceCode(str2.substring(1),false);
								if(code>=0)
									parms.addElement(RawMaterial.CODES.NAME(code));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 10: // -Player
				case 11: // -MOB
					{
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),new Object[0]));
						break;
					}
				case 17: // -anyclass
					{
						final Vector<Object> parms=new Vector<Object>();
						for(final SavedClass C : charClasses())
						{
							if(fromHereStartsWith(V,'+',v+1,C.nameStart))
								parms.addElement(C.name);
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 18: // +anyclass
					{
						final Vector<Object> parms=new Vector<Object>();
						for(final SavedClass C : charClasses())
						{
							if(fromHereStartsWith(V,'-',v+1,C.nameStart))
								parms.addElement(C.name);
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
					break;
				case 59: // +value
				case 60: // -value
					buildItemFlag=true;
				//$FALL-THROUGH$
				case 19: // +adjstr
				case 20: // +adjint
				case 21: // +adjwis
				case 22: // +adjdex
				case 23: // -adjcha
				case 24: // +adjcha
				case 25: // -adjstr
				case 26: // -adjint
				case 27: // -adjwis
				case 28: // -adjdex
				case 29: // -adjcon
				case 30: // -adjcha
				case 87: // +str
				case 88: // +int
				case 89: // +wis
				case 90: // +dex
				case 91: // +con
				case 92: // +cha
				case 93: // -str
				case 94: // -int
				case 95: // -wis
				case 96: // -dex
				case 97: // -con
				case 98: // -cha
				case 122: // -chance
				case 55: // +able
				case 56: // -able
				case 61: // +weight
				case 62: // -weight
				case 63: // +armor
				case 64: // -armor
				case 65: // +damage
				case 66: // -damage
				case 67: // +attack
				case 68: // -attack
				case 115: // -groupsize
				case 116: // +groupsize
					{
						val=((++v)<V.size())?CMath.s_int(V.elementAt(v)):0;
						final Vector<Object> parms=new Vector<Object>();
						parms.addElement(Integer.valueOf(val));
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
						break;
					}
				case 118: // -if
				case 119: // +if
					{
						final Vector<Object> parms=new Vector<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.elementAt(v2);
							if(zapCodes.containsKey(str2)||(str2.startsWith("+"))||(str2.startsWith("-")))
							{
								v=v2-1;
								break;
							}
							else
							{
								ScriptingEngine SE = (ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
								SE.setSavable(false);
								SE.setVarScope("*");
								try {
									String[] tt = SE.parseEval(str2);
									parms.addElement(SE);
									String[][] EVAL={tt};
									parms.addElement(EVAL); // the compiled eval
									Object[] tmp = new Object[ScriptingEngine.SPECIAL_NUM_OBJECTS];
									parms.addElement(tmp);
								} catch(ScriptParseException spe) {
									Log.errOut("MUDZapper","Script parse Exception for "+str2);
									Log.errOut("MUDZapper",spe);
								}
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
						break;
					}
				}
			else
			{
				boolean found=false;
				if(!found)
				for(final SavedClass C : charClasses())
				{
					if(str.equals("-"+C.upperName))
					{
						final Vector<Object> parms=new Vector<Object>();
						entryType=zapCodes.get("+CLASS");
						parms.addElement(C.name);
						found=true;
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
						break;
					}
				}
				if(!found)
				for(final SavedRace R : races())
				{
					if(str.equals("-"+R.upperName))
					{
						final Vector<Object> parms=new Vector<Object>();
						entryType=zapCodes.get("+RACE");
						parms.addElement(R.name);
						found=true;
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
						break;
					}
				}
				if((!found)
				&&(str.equals("-"+Faction.Align.EVIL.toString())))
				{
					final Vector<Object> parms=new Vector<Object>();
					entryType=zapCodes.get("+ALIGNMENT");
					parms.addElement(Faction.Align.EVIL.toString());
					found=true;
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.equals("-"+Faction.Align.GOOD.toString())))
				{
					final Vector<Object> parms=new Vector<Object>();
					entryType=zapCodes.get("+ALIGNMENT");
					parms.addElement(Faction.Align.GOOD.toString());
					found=true;
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.equals("-"+Faction.Align.NEUTRAL.toString())))
				{
					final Vector<Object> parms=new Vector<Object>();
					entryType=zapCodes.get("+ALIGNMENT");
					parms.addElement(Faction.Align.NEUTRAL.toString());
					found=true;
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
				}
				if((!found)&&(str.equals("-MALE")))
				{
					final Vector<Object> parms=new Vector<Object>();
					entryType=zapCodes.get("+GENDER");
					parms.addElement("M");
					found=true;
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
				}
				if((!found)&&(str.equals("-FEMALE")))
				{
					final Vector<Object> parms=new Vector<Object>();
					entryType=zapCodes.get("+GENDER");
					parms.addElement("F");
					found=true;
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
				}
				if((!found)&&(str.equals("-NEUTER")))
				{
					final Vector<Object> parms=new Vector<Object>();
					entryType=zapCodes.get("+GENDER");
					parms.addElement("N");
					found=true;
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.startsWith("-"))
				&&(CMLib.factions().isRangeCodeName(str.substring(1))))
				{
					final Vector<Object> parms=new Vector<Object>();
					entryType=zapCodes.get("+FACTION");
					parms.addElement(str.substring(1));
					found=true;
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
				}
				if(!found)
				for(final SavedClass C : charClasses())
				{
					if(str.startsWith(C.minusNameStart))
					{
						final Vector<Object> parms=new Vector<Object>();
						entryType=zapCodes.get("+CLASS");
						parms.addElement(C.name);
						found=true;
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
						break;
					}
				}
				if(!found)
				for(final SavedRace R : races())
				{
					if(str.startsWith(R.minusNameStart))
					{
						final Vector<Object> parms=new Vector<Object>();
						entryType=zapCodes.get("+RACE");
						parms.addElement(R.name);
						found=true;
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
						break;
					}
				}
				if(!found)
				for(final SavedRace R : races())
				{
					if(str.startsWith(R.minusCatNameStart))
					{
						final Vector<Object> parms=new Vector<Object>();
						entryType=zapCodes.get("+RACECAT");
						parms.addElement(R.racialCategory);
						buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
					}
				}
				if((!found)
				&&(str.startsWith("-"+Faction.Align.EVIL.toString().substring(0,3))))
				{
					final Vector<Object> parms=new Vector<Object>();
					entryType=zapCodes.get("+ALIGNMENT");
					parms.addElement(Faction.Align.EVIL.toString());
					found=true;
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.startsWith("-"+Faction.Align.GOOD.toString().substring(0,3))))
				{
					final Vector<Object> parms=new Vector<Object>();
					entryType=zapCodes.get("+ALIGNMENT");
					parms.addElement(Faction.Align.GOOD.toString());
					found=true;
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.startsWith("-"+Faction.Align.NEUTRAL.toString().substring(0,3))))
				{
					final Vector<Object> parms=new Vector<Object>();
					entryType=zapCodes.get("+ALIGNMENT");
					parms.addElement(Faction.Align.NEUTRAL.toString());
					found=true;
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.startsWith("-MALE")))
				{
					final Vector<Object> parms=new Vector<Object>();
					entryType=zapCodes.get("+GENDER");
					parms.addElement("M");
					found=true;
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.startsWith("-FEMALE")))
				{
					final Vector<Object> parms=new Vector<Object>();
					entryType=zapCodes.get("+GENDER");
					parms.addElement("F");
					found=true;
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.startsWith("-NEUTER")))
				{
					final Vector<Object> parms=new Vector<Object>();
					entryType=zapCodes.get("+GENDER");
					parms.addElement("N");
					found=true;
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.startsWith("-"))
				 &&(CMLib.factions().isRangeCodeName(str.substring(1))))
				{
					final Vector<Object> parms=new Vector<Object>();
					entryType=zapCodes.get("+FACTION");
					parms.addElement(str.substring(1));
					found=true;
					buf.add(new CompiledZapperMaskEntry(entryType.intValue(),parms.toArray(new Object[0])));
				}
				if(!found)
				{
					CompiledZapperMaskEntry entry=levelCompiledHelper(str,'-');
					if(entry!=null)
						buf.addElement(entry);
				}
			}
		}
		return new CompiledZapperMask(new boolean[]{buildItemFlag,buildRoomFlag},buf.toArray(new CompiledZapperMaskEntry[0]));
	}

	protected Room outdoorRoom(Area A)
	{
		Room R=null;
		for(final Enumeration<Room> e=A.getMetroMap();e.hasMoreElements();)
		{
			R=e.nextElement();
			if((R.domainType()&Room.INDOORS)==0) return R;
		}
		return A.getRandomMetroRoom();
	}

	protected CharStats getBaseCharStats(CharStats base, MOB mob)
	{
		if(base==null)
		{
			base=(CharStats)mob.baseCharStats().copyOf();
			base.getMyRace().affectCharStats(mob,base);
		}
		return base;
	}

	public boolean maskCheck(final String text, final Environmental E, final boolean actual){ return maskCheck(getPreCompiledMask(text),E,actual);}
	public boolean maskCheck(final CompiledZapperMask cset, final Environmental E, final boolean actual)
	{
		if(E==null) return true;
		if((cset==null)||(cset.entries.length<1)) return true;
		getMaskCodes();
		CharStats base=null;
		final MOB mob=(E instanceof MOB)?(MOB)E:nonCrashingMOB();
		final boolean[] flags=cset.flags;
		final Item item=flags[0]?((E instanceof Item)?(Item)E:nonCrashingItem(mob)):null;
		final Room room = flags[1]?((E instanceof Area)?outdoorRoom((Area)E):CMLib.map().roomLocation(E)):null;
		final Physical P = (E instanceof Physical)?(Physical)E:null;
		if((mob==null)||(flags[0]&&(item==null))) 
			return false;
		for(CompiledZapperMaskEntry entry : cset.entries)
		{
			try
			{
			switch(entry.maskType)
			{
			case 108: // +sysop
				if(CMSecurity.isASysOp(mob))
					return true;
				break;
			case 109: // -sysop
				if(CMSecurity.isASysOp(mob))
					return false;
				break;
			case 110: // +subop
				if(CMSecurity.isASysOp(mob)
				||((room!=null)&&(room.getArea().amISubOp(mob.Name()))))
					return true;
				break;
			case 111: // -subop
				if(CMSecurity.isASysOp(mob)
				||((room!=null)&&(room.getArea().amISubOp(mob.Name()))))
					return false;
				break;
			case 0: // -class
			{
				if(!CMParms.contains(entry.parms,actual?mob.baseCharStats().getCurrentClass().name():mob.charStats().displayClassName()))
					return false;
				break;
			}
			case 1: // -baseclass
			{
				String baseClass=mob.baseCharStats().getCurrentClass().baseClass();
				if((!actual)&&(!baseClass.equals(mob.charStats().displayClassName())))
				{
					final CharClass C=CMClass.getCharClass(mob.charStats().displayClassName());
					if(C!=null) baseClass=C.baseClass();
				}
				if(!CMParms.contains(entry.parms,baseClass))
					return false;
				break;
			}
			case 117: // +baseclass
			{
				String baseClass=mob.baseCharStats().getCurrentClass().baseClass();
				if((!actual)&&(!baseClass.equals(mob.charStats().displayClassName())))
				{
					final CharClass C=CMClass.getCharClass(mob.charStats().displayClassName());
					if(C!=null) baseClass=C.baseClass();
				}
				if(CMParms.contains(entry.parms,baseClass))
					return false;
				break;
			}
			case 2: // -race
				if(!CMParms.contains(entry.parms,actual?mob.baseCharStats().getMyRace().name():mob.charStats().raceName()))
					return false;
				break;
			case 3: // -alignment
				if(!CMParms.contains(entry.parms,CMLib.flags().getAlignmentName(mob)))
					return false;
				break;
			case 4: // -gender
			{
				base=getBaseCharStats(base,mob);
				if(!CMParms.contains(entry.parms,actual?(""+((char)base.getStat(CharStats.STAT_GENDER))):(""+(Character.toUpperCase(mob.charStats().genderName().charAt(0))))))
					return false;
				break;
			}
			case 5: // -level
				if(P!=null)
				{
					final int level=actual?P.basePhyStats().level():P.phyStats().level();
					boolean found=false;
					for(int v=0;v<entry.parms.length-1;v+=2)
						switch(((Integer)entry.parms[v]).intValue())
						{
							case 37: // +lvlgr
								if(level>((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
							case 38: // +lvlge
								if(level>=((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
							case 39: // +lvlt
								if(level<((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
							case 40: // +lvlle
								if(level<=((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
							case 41: // +lvleq
								if(level==((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
						}
					if(!found) return false;
				}
				break;
			case 6: // -classlevel
				{
					boolean found=false;
					int cl=actual?mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass())
								 :mob.charStats().getClassLevel(mob.charStats().getCurrentClass());
					for(int v=0;v<entry.parms.length-1;v+=2)
						switch(((Integer)entry.parms[v]).intValue())
						{
							case 37: // +lvlgr
								if(cl>((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
							case 38: // +lvlge
								if(cl>=((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
							case 39: // +lvlt
								if(cl<((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
							case 40: // +lvlle
								if(cl<=((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
							case 41: // +lvleq
								if(cl==((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
						}
					if(!found) return false;
				}
				break;
			case 103: // -maxclasslevel
				{
					boolean found=false;
					int cl=0;
					int c2=0;
					if(actual)
					{
						cl=mob.baseCharStats().getClassLevel(mob.baseCharStats().getMyClass(0));
						for(int v=1;v<mob.baseCharStats().numClasses();v++)
						{
							c2=mob.baseCharStats().getClassLevel(mob.baseCharStats().getMyClass(v));
							if(c2>cl) cl=c2;
						}
					}
					else
					{
						cl=mob.charStats().getClassLevel(mob.charStats().getMyClass(0));
						for(int v=1;v<mob.charStats().numClasses();v++)
						{
							c2=mob.charStats().getClassLevel(mob.charStats().getMyClass(v));
							if(c2>cl) cl=c2;
						}
					}
					for(int v=0;v<entry.parms.length-1;v+=2)
						switch(((Integer)entry.parms[v]).intValue())
						{
							case 37: // +lvlgr
								if(cl>((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
							case 38: // +lvlge
								if(cl>=((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
							case 39: // +lvlt
								if(cl<((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
							case 40: // +lvlle
								if(cl<=((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
							case 41: // +lvleq
								if(cl==((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
						}
					if(!found) return false;
				}
				break;
			case 7: // -tattoo
				{
					boolean found=false;
					for(final Object o : entry.parms)
						if((mob.findTattoo((String)o)!=null)
						||((room!=null)&&(room.getArea().getBlurbFlag((String)o)!=null)))
						{ found=true; break;}

					if(!found) return false;
				}
				break;
			case 8: // +tattoo
				{
					for(final Object o : entry.parms)
						if((mob.findTattoo((String)o)!=null)
						||((room!=null)&&(room.getArea().getBlurbFlag((String)o)!=null)))
						{ return false;}
				}
				break;
			case 120: // -mood
				{
					String moodName = "NORMAL";
					final Ability A = mob.fetchEffect("Mood");
					if((A!=null)&&(A.text().trim().length()>0)) moodName=A.text().toUpperCase().trim();
					if(!CMParms.contains(entry.parms,moodName)) return false;
				}
				break;
			case 121: // +mood
				{
					final String moodName;
					final Ability A = mob.fetchEffect("Mood");
					if((A!=null)&&(A.text().trim().length()>0)) 
						moodName=A.text().toUpperCase().trim();
					else
						moodName= "NORMAL";
					if(CMParms.contains(entry.parms,moodName)) return false;
				}
				break;
			case 81: // -expertise
				{
					boolean found=false;
					for(final Object o : entry.parms)
						if(mob.fetchExpertise((String)o)!=null)
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 82: // +expertise
				{
					for(final Object o : entry.parms)
						if(mob.fetchExpertise((String)o)!=null)
						{ return false;}
				}
				break;
			case 113: // -questwin
				{
					boolean found=false;
					for(final Object o : entry.parms)
					{
						final Quest Q=CMLib.quests().fetchQuest((String)o);
						if((Q!=null)&&(Q.wasWinner(mob.Name())))
						{ found=true; break;}
					}
					if(!found) return false;
				}
				break;
			case 114: // +questwin
				{
					for(final Object o : entry.parms)
					{
						final Quest Q=CMLib.quests().fetchQuest((String)o);
						if((Q!=null)&&(Q.wasWinner(mob.Name())))
						{ return false;}
					}
				}
				break;
			case 83: // -skill
				{
					boolean found=false;
					for(int v=0;v<entry.parms.length-1;v+=2)
					{
						final Ability A=mob.fetchAbility((String)entry.parms[v]);
						if((A!=null)&&(A.proficiency()>=((Integer)entry.parms[v+1]).intValue()))
						{ found=true; break;}
					}
					if(!found) return false;
				}
				break;
			case 101: // -skillflag
				{
					boolean found=false;
					for(final Object o : entry.parms)
					{
						for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
						{
							final Ability A=a.nextElement();
							if(evaluateSkillFlagObject(o,A))
							{ found=true; break;}
						}
						if(found) break;
					}
					if(!found) return false;
				}
				break;
			case 84: // +skill
				{
					for(int v=0;v<entry.parms.length-1;v+=2)
					{
						final Ability A=mob.fetchAbility((String)entry.parms[v]);
						if((A!=null)&&(A.proficiency()>=((Integer)entry.parms[v+1]).intValue()))
						{ return false;}
					}
				}
				break;
			case 102: // +skillflag
				{
					for(final Object o : entry.parms)
					{
						for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
						{
							final Ability A=a.nextElement();
							if(evaluateSkillFlagObject(o,A))
								return false;
						}
					}
				}
				break;
			case 79: // -security
				{
					boolean found=false;
					for(final Object o : entry.parms)
						if(CMSecurity.isAllowed(mob,room,(CMSecurity.SecFlag)o))
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 80: // +security
				{
					for(final Object o : entry.parms)
						if(CMSecurity.isAllowed(mob,room,(CMSecurity.SecFlag)o))
						{ return false;}
				}
				break;
			case 9: // -names
				{
					boolean found=false;
					final String name=actual?E.Name():E.name();
					for(final Object o : entry.parms)
						if(name.equalsIgnoreCase((String)o))
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 10: // -player
				if(!mob.isMonster()) return false;
				break;
			case 11: // -npc
				if(mob.isMonster()) return false;
				break;
			case 12: // -racecat
			{
				String raceCat=mob.baseCharStats().getMyRace().racialCategory();
				if((!actual)&&(!mob.baseCharStats().getMyRace().name().equals(mob.charStats().raceName())))
				{
					final Race R2=CMClass.getRace(mob.charStats().raceName());
					if(R2!=null) raceCat=R2.racialCategory();
				}
				if(!CMParms.contains(entry.parms,raceCat))
					return false;
				break;
			}
			case 112: // +race
			{
				String race=mob.baseCharStats().getMyRace().name();
				if((!actual)&&(!mob.baseCharStats().getMyRace().name().equals(mob.charStats().raceName())))
					race=mob.charStats().raceName();
				if(CMParms.contains(entry.parms,race)) return false;
				break;
			}
			case 13: // +racecat
			{
				String raceCat=mob.baseCharStats().getMyRace().racialCategory();
				if((!actual)&&(!mob.baseCharStats().getMyRace().name().equals(mob.charStats().raceName())))
				{
					final Race R2=CMClass.getRace(mob.charStats().raceName());
					if(R2!=null) raceCat=R2.racialCategory();
				}
				if(CMParms.contains(entry.parms,raceCat))
					return false;
				break;
			}
			case 14: // -clan
				{
					boolean found=false;
					if(E instanceof ClanItem)
					{
						final String clanID=((ClanItem)E).clanID();
						for(final Object o : entry.parms)
							if(clanID.equalsIgnoreCase((String)o))
							{ found=true; break;}
					}
					else
					if(E instanceof MOB)
						for(Pair<Clan,Integer> c : ((MOB)E).clans())
						{
							for(final Object o : entry.parms)
								if(c.first.clanID().equalsIgnoreCase((String)o))
								{ found=true; break;}
						}
					if(!found) return false;
				}
				break;
			case 15: // +clan
				if(E instanceof ClanItem)
				{
					final String clanID=((ClanItem)E).clanID();
					for(final Object o : entry.parms)
						if(clanID.equalsIgnoreCase((String)o))
							return false;
				}
				else
				if(E instanceof MOB)
					for(Pair<Clan,Integer> c : ((MOB)E).clans())
					{
						for(final Object o : entry.parms)
							if(c.first.clanID().equalsIgnoreCase((String)o))
								return false;
					}
				break;
			case 49: // +material
				if((item!=null)&&CMParms.contains(entry.parms,RawMaterial.Material.findByMask(item.material()&RawMaterial.MATERIAL_MASK).desc()))
					return false;
				break;
			case 50: // -material
				if((item!=null)&&(!CMParms.contains(entry.parms,RawMaterial.Material.findByMask(item.material()&RawMaterial.MATERIAL_MASK).desc())))
					return false;
				break;
			case 57: // +wornOn
				if(item!=null)
					for(final Object o : entry.parms)
						if((item.rawProperLocationBitmap()&((Long)o).longValue())>0)
							return false;
				break;
			case 58: // -wornOn
				{
					boolean found=false;
					if(item!=null)
						for(final Object o : entry.parms)
							if((item.rawProperLocationBitmap()&((Long)o).longValue())>0)
							{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 69: // +disposition
				if(P!=null)
					for(final Object o : entry.parms)
						if((P.phyStats().disposition()&((Integer)o).intValue())>0)
							return false;
				break;
			case 70: // -disposition
				if(P!=null)
				{
					boolean found=false;
					for(final Object o : entry.parms)
						if((P.phyStats().disposition()&((Integer)o).intValue())>0)
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 71: // +senses
				if(P!=null)
					for(final Object o : entry.parms)
						if((P.phyStats().sensesMask()&((Integer)o).intValue())>0)
							return false;
				break;
			case 72: // -senses
				if(P!=null)
				{
					boolean found=false;
					for(final Object o : entry.parms)
						if((P.phyStats().sensesMask()&((Integer)o).intValue())>0)
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 73: // +HOUR
				{
					if(room!=null)
						for(final Object o : entry.parms)
							if(room.getArea().getTimeObj().getTimeOfDay()==((Integer)o).intValue())
								return false;
				}
				break;
			case 74: // -HOUR
				{
					boolean found=false;
					if(room!=null)
						for(final Object o : entry.parms)
							if(room.getArea().getTimeObj().getTimeOfDay()==((Integer)o).intValue())
							{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 75: // +season
				{
					if(room!=null)
						for(final Object o : entry.parms)
							if(room.getArea().getTimeObj().getSeasonCode()==((Integer)o).intValue())
								return false;
				}
				break;
			case 76: // -season
				{
					boolean found=false;
					if(room!=null)
						for(final Object o : entry.parms)
							if(room.getArea().getTimeObj().getSeasonCode()==((Integer)o).intValue())
							{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 104: // +weather
				{
					if(room!=null)
						for(final Object o : entry.parms)
							if(room.getArea().getClimateObj().weatherType(room)==((Integer)o).intValue())
								return false;
				}
				break;
			case 105: // -weather
				{
					boolean found=false;
					if(room!=null)
						for(final Object o : entry.parms)
							if(room.getArea().getClimateObj().weatherType(room)==((Integer)o).intValue())
							{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 77: // +month
				{
					if(room!=null)
						for(final Object o : entry.parms)
							if(room.getArea().getTimeObj().getMonth()==((Integer)o).intValue())
								return false;
				}
				break;
			case 78: // -month
				{
					boolean found=false;
					if(room!=null)
						for(final Object o : entry.parms)
						if(room.getArea().getTimeObj().getMonth()==((Integer)o).intValue())
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 106: // +day
				{
					if(room!=null)
						for(final Object o : entry.parms)
							if(room.getArea().getTimeObj().getDayOfMonth()==((Integer)o).intValue())
								return false;
				}
				break;
			case 107: // -day
				{
					boolean found=false;
					if(room!=null)
						for(final Object o : entry.parms)
							if(room.getArea().getTimeObj().getDayOfMonth()==((Integer)o).intValue())
							{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 85: // +quallvl
				if(entry.parms.length>1)
				{
					final Ability A=CMClass.getAbility((String)entry.parms[0]);
					final int adjustment=((Integer)entry.parms[1]).intValue();
					final int lvl=CMLib.ableMapper().qualifyingClassLevel(mob,A);
					final int clvl=CMLib.ableMapper().qualifyingLevel(mob,A)+adjustment;
					if(lvl<clvl) return false;
				}
				break;
			case 86: // -quallvl
				if(entry.parms.length>1)
				{
					final Ability A=CMClass.getAbility((String)entry.parms[0]);
					final int adjustment=((Integer)entry.parms[1]).intValue();
					final int lvl=CMLib.ableMapper().qualifyingClassLevel(mob,A);
					final int clvl=CMLib.ableMapper().qualifyingLevel(mob,A)+adjustment;
					if(lvl>clvl) return false;
				}
				break;
			case 51: // +resource
				if((item!=null)&&CMParms.contains(entry.parms,RawMaterial.CODES.NAME(item.material())))
					return false;
				break;
			case 52: // -resource
				if((item!=null)&&(!CMParms.contains(entry.parms,RawMaterial.CODES.NAME(item.material()))))
					return false;
				break;
			case 53: // -JavaClass
				{
					boolean found=false;
					for(final Object o : entry.parms)
						if(E.ID().equalsIgnoreCase((String)o))
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 54: // +JavaClass
				for(final Object o : entry.parms)
					if(E.ID().equalsIgnoreCase((String)o))
					{ return false;}
				break;
			case 44: // -deity
				{
					if(mob.getWorshipCharID().length()==0)
						return false;
					boolean found=false;
					for(final Object o : entry.parms)
						if(mob.getWorshipCharID().equalsIgnoreCase((String)o)||((String)o).equals("ANY"))
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 45: // +deity
				{
					if(mob.getWorshipCharID().length()>0)
						for(final Object o : entry.parms)
							if(mob.getWorshipCharID().equalsIgnoreCase((String)o))
							{ return false;}
				}
				break;
			case 43: // -effects
				{
					boolean found=false;
					if(E instanceof PhysicalAgent)
						for(final Object o : entry.parms)
							if(((Physical)E).fetchEffect((String)o)!=null)
							{   found=true; break;}
					if((!found)&&(E instanceof PhysicalAgent))
						for(final Object o : entry.parms)
							if(((PhysicalAgent)E).fetchBehavior((String)o)!=null)
							{   found=true; break;}
					if(!found) return false;
				}
				break;
			case 46: // -faction
				{
					boolean found=false;
					for(final Object o : entry.parms)
					{
						Faction.FRange FR=getRange((String)o);
						if((FR!=null)&&(CMLib.factions().isFactionedThisWay(mob,FR)))
						{ 
							found=true; 
							break;
						}
					}
					if(!found) return false;
				}
				break;
			case 47: // +faction
				{
					for(final Object o : entry.parms)
					{
						Faction.FRange FR=getRange((String)o);
						if((FR!=null)&&(CMLib.factions().isFactionedThisWay(mob,FR)))
							return false;
					}
				}
				break;
			case 42: // +effects
				{
					if(E instanceof Physical)
						for(final Object o : entry.parms)
							if(((Physical)E).fetchEffect((String)o)!=null)
								return false;
					if(E instanceof PhysicalAgent)
						for(final Object o : entry.parms)
							if(((PhysicalAgent)E).fetchBehavior((String)o)!=null)
								return false;
				}
				break;
			case 16: // +name
				{
					final String name=actual?E.Name():E.name();
					for(final Object o : entry.parms)
						if(name.equalsIgnoreCase((String)o))
						{ return false;}
				}
				break;
			case 17: // -anyclass
				{
					boolean found=false;
					if(actual)
					{
						for(final Object o : entry.parms)
							if(mob.baseCharStats().getClassLevel((String)o)>=0)
							{ found=true; break;}
					}
					else
					{
						for(final Object o : entry.parms)
							if((mob.charStats().getClassLevel((String)o)>=0)
							||(mob.charStats().displayClassName().equalsIgnoreCase((String)o)))
							{ found=true; break;}
					}
					if(!found) return false;
				}
				break;
			case 18: // +anyclass
				if(actual)
				{
					for(final Object o : entry.parms)
						if(mob.baseCharStats().getClassLevel((String)o)>=0)
						{ return false;}
				}
				else
				{
					for(final Object o : entry.parms)
						if((mob.charStats().getClassLevel((String)o)>=0)
						||(mob.charStats().displayClassName().equalsIgnoreCase((String)o)))
						{ return false; }
				}
				break;
			case 19: // +adjstr
				if((entry.parms.length>0)&&(mob.charStats().getStat(CharStats.STAT_STRENGTH)<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 20: // +adjint
				if((entry.parms.length>0)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 21: // +adjwis
				if((entry.parms.length>0)&&(mob.charStats().getStat(CharStats.STAT_WISDOM)<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 22: // +adjdex
				if((entry.parms.length>0)&&(mob.charStats().getStat(CharStats.STAT_DEXTERITY)<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 23: // -adjcha
				if((entry.parms.length>0)&&(mob.charStats().getStat(CharStats.STAT_CONSTITUTION)<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 24: // +adjcha
				if((entry.parms.length>0)&&(mob.charStats().getStat(CharStats.STAT_CHARISMA)<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 25: // -adjstr
				if((entry.parms.length>0)&&(mob.charStats().getStat(CharStats.STAT_STRENGTH)>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 26: // -adjint
				if((entry.parms.length>0)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 27: // -adjwis
				if((entry.parms.length>0)&&(mob.charStats().getStat(CharStats.STAT_WISDOM)>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 28: // -adjdex
				if((entry.parms.length>0)&&(mob.charStats().getStat(CharStats.STAT_DEXTERITY)>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 29: // -adjcon
				if((entry.parms.length>0)&&(mob.charStats().getStat(CharStats.STAT_CONSTITUTION)>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 30: // -adjcha
				if((entry.parms.length>0)&&(mob.charStats().getStat(CharStats.STAT_CHARISMA)>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 87: // +str
				base=getBaseCharStats(base,mob);
				if((entry.parms.length>0)&&(base.getStat(CharStats.STAT_STRENGTH)<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 88: // +int
				base=getBaseCharStats(base,mob);
				if((entry.parms.length>0)&&(base.getStat(CharStats.STAT_INTELLIGENCE)<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 89: // +wis
				base=getBaseCharStats(base,mob);
				if((entry.parms.length>0)&&(base.getStat(CharStats.STAT_WISDOM)<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 90: // +dex
				base=getBaseCharStats(base,mob);
				if((entry.parms.length>0)&&(base.getStat(CharStats.STAT_DEXTERITY)<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 91: // +con
				base=getBaseCharStats(base,mob);
				if((entry.parms.length>0)&&(base.getStat(CharStats.STAT_CONSTITUTION)<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 92: // +cha
				base=getBaseCharStats(base,mob);
				if((entry.parms.length>0)&&(base.getStat(CharStats.STAT_CHARISMA)<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 93: // -str
				base=getBaseCharStats(base,mob);
				if((entry.parms.length>0)&&(base.getStat(CharStats.STAT_STRENGTH)>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 94: // -int
				base=getBaseCharStats(base,mob);
				if((entry.parms.length>0)&&(base.getStat(CharStats.STAT_INTELLIGENCE)>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 95: // -wis
				base=getBaseCharStats(base,mob);
				if((entry.parms.length>0)&&(base.getStat(CharStats.STAT_WISDOM)>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 96: // -dex
				base=getBaseCharStats(base,mob);
				if((entry.parms.length>0)&&(base.getStat(CharStats.STAT_DEXTERITY)>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 97: // -con
				base=getBaseCharStats(base,mob);
				if((entry.parms.length>0)&&(base.getStat(CharStats.STAT_CONSTITUTION)>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 98: // -cha
				base=getBaseCharStats(base,mob);
				if((entry.parms.length>0)&&(base.getStat(CharStats.STAT_CHARISMA)>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 122: // -chance
				if((entry.parms.length>0)&&(CMLib.dice().rollPercentage()<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 55: // +able
				if((entry.parms.length>0)&&(P != null)&&(P.phyStats().ability()>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 56: // -able
				if((entry.parms.length>0)&&(P != null)&&(P.phyStats().ability()<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 61: // +weight
				if((entry.parms.length>0)&&(P != null)&&(P.phyStats().weight()>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 62: // -weight
				if((entry.parms.length>0)&&(P != null)&&(P.phyStats().weight()<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 63: // +armor
				if((entry.parms.length>0)&&(P != null)&&(P.phyStats().armor()>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 64: // -armor
				if((entry.parms.length>0)&&(P != null)&&(P.phyStats().armor()<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 65: // +damage
				if((entry.parms.length>0)&&(P != null)&&(P.phyStats().damage()>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 66: // -damage
				if((entry.parms.length>0)&&(P != null)&&(P.phyStats().damage()<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 67: // +attack
				if((entry.parms.length>0)&&(P != null)&&(P.phyStats().attackAdjustment()>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 68: // -attack
				if((entry.parms.length>0)&&(P != null)&&(P.phyStats().attackAdjustment()<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 59: // +value
				if(E instanceof MOB)
				{
					if((entry.parms.length>0)&&(CMLib.beanCounter().getTotalAbsoluteValueAllCurrencies(mob)>(((Integer)entry.parms[0]).intValue())))
					   return false;
				}
				else
				{
					if((entry.parms.length>0)&&(item!=null)&&(item.baseGoldValue()>(((Integer)entry.parms[0]).intValue())))
						return false;
				}
				break;
			case 60: // -value
				if(E instanceof MOB)
				{
					if((entry.parms.length>0)&&(CMLib.beanCounter().getTotalAbsoluteValueAllCurrencies(mob)<(((Integer)entry.parms[0]).intValue())))
					   return false;
				}
				else
				{
					if((entry.parms.length>0)&&(item!=null)&&(item.baseGoldValue()<(((Integer)entry.parms[0]).intValue())))
						return false;
				}
				break;
			case 31: // -area
				{
					boolean found=false;
					if(room!=null)
						for(final Object o : entry.parms)
							if(room.getArea().Name().equalsIgnoreCase((String)o))
							{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 32: // +area
			{
				if(room!=null)
					for(final Object o : entry.parms)
						if(room.getArea().Name().equalsIgnoreCase((String)o))
						{ return false;}
				break;
			}
			case 100: // -home
			{
				boolean found=false;
				final Area A=CMLib.map().getStartArea(E);
				if(A!=null)
					for(final Object o : entry.parms)
						if(A.Name().equalsIgnoreCase((String)o))
						{ found=true; break;}
				if(!found) return false;
			}
			break;
			case 99: // +home
			{
				final Area A=CMLib.map().getStartArea(E);
				if(A!=null)
					for(final Object o : entry.parms)
						if(A.Name().equalsIgnoreCase((String)o))
						{ return false;}
				break;
			}
			case 33: // -item
				{
					boolean found=false;
					for(final Object o : entry.parms)
						if(mob.findItem((String)o)!=null)
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 48: // -worn
				if(E instanceof MOB)
				{
					boolean found=false;
					for(final Object o : entry.parms)
					{
						final Item I=mob.findItem((String)o);
						if((I!=null)&&(!I.amWearingAt(Wearable.IN_INVENTORY)))
						{ found=true; break;}
					}
					if(!found) return false;
				}
				else
				if(E instanceof Item)
					if(((Item)E).amWearingAt(Wearable.IN_INVENTORY))
						return false;
				break;
			case 34: // +class
				if(CMParms.contains(entry.parms,actual?mob.baseCharStats().getCurrentClass().name():mob.charStats().displayClassName()))
					return false;
				break;
			case 35: // +alignment
				if(CMParms.contains(entry.parms,CMLib.flags().getAlignmentName(mob)))
					return false;
				break;
			case 36: // +gender
				base=getBaseCharStats(base,mob);
				if(CMParms.contains(entry.parms,actual?(""+((char)base.getStat(CharStats.STAT_GENDER))):(""+Character.toUpperCase(mob.charStats().genderName().charAt(0)))))
					return false;
				break;
			case 37: // +lvlgr
				if((entry.parms.length>0)&&(P!=null)&&((actual?P.basePhyStats().level():P.phyStats().level())>((Integer)entry.parms[0]).intValue()))
				   return false;
				break;
			case 38: // +lvlge
				if((entry.parms.length>0)&&(P!=null)&&((actual?P.basePhyStats().level():P.phyStats().level())>=((Integer)entry.parms[0]).intValue()))
				   return false;
				break;
			case 39: // +lvlt
				if((entry.parms.length>0)&&(P!=null)&&((actual?P.basePhyStats().level():P.phyStats().level())<((Integer)entry.parms[0]).intValue()))
				   return false;
				break;
			case 40: // +lvlle
				if((entry.parms.length>0)&&(P!=null)&&((actual?P.basePhyStats().level():P.phyStats().level())<=((Integer)entry.parms[0]).intValue()))
				   return false;
				break;
			case 41: // +lvleq
				if((entry.parms.length>0)&&(P!=null)&&((actual?P.basePhyStats().level():P.phyStats().level())==((Integer)entry.parms[0]).intValue()))
				   return false;
				break;
			case 116: // +groupsize
				if((entry.parms.length>0)&&(mob.getGroupMembers(new HashSet<MOB>(1)).size()<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 115: // -groupsize
				if((entry.parms.length>0)&&(mob.getGroupMembers(new HashSet<MOB>(1)).size()>(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 118: // -if
				{
					boolean oneIsOK = false;
					if(E instanceof PhysicalAgent)
						for(int v=0;v<entry.parms.length-2;v+=3)
						{
							final ScriptingEngine SE = (ScriptingEngine)entry.parms[v];
							final String[][] EVAL = (String[][])entry.parms[v+1];
							final Object[] tmp = (Object[])entry.parms[v+2];
							final MOB M = SE.getMakeMOB(E);
							final Item defaultItem=(E instanceof Item)?(Item)E:null;
							if(SE.eval((PhysicalAgent)E, M, null,M, defaultItem, null, "", tmp, EVAL, 0))
							{
								oneIsOK = true;
								break;
							}
						}
					if(!oneIsOK) return false;
					break;
				}
			case 119: // +if
				{
					if(E instanceof PhysicalAgent)
						for(int v=0;v<entry.parms.length-2;v+=3)
						{
							final ScriptingEngine SE = (ScriptingEngine)entry.parms[v];
							final String[][] EVAL = (String[][])entry.parms[v+1];
							final Object[] tmp = (Object[])entry.parms[v+2];
							final MOB M = SE.getMakeMOB(E);
							final Item defaultItem=(E instanceof Item)?(Item)E:null;
							if(E instanceof PhysicalAgent)
								if(SE.eval((PhysicalAgent)E, M, null,M, defaultItem, null, "", tmp, EVAL, 0))
									return true;
						}
					break;
				}
			}
			}catch(NullPointerException n){}
		}
		return true;
	}

	public boolean maskCheck(final String text, final PlayerLibrary.ThinPlayer E){ return maskCheck(getPreCompiledMask(text),E);}
	public boolean maskCheck(final CompiledZapperMask cset, final PlayerLibrary.ThinPlayer E)
	{
		if(E==null) return true;
		if((cset==null)||(cset.empty)||(cset.entries.length<1)) return true;
		getMaskCodes();
		//boolean[] flags=(boolean[])cset.firstElement();
		for(CompiledZapperMaskEntry entry : cset.entries)
		{
			try
			{
			switch(entry.maskType)
			{
			case 108: // +sysop
				if(CMSecurity.isASysOp(E))
					return true;
				break;
			case 109: // -sysop
				if(CMSecurity.isASysOp(E))
					return false;
				break;
			case 110: // +subop
				if(CMSecurity.isASysOp(E))
					return true;
				for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
					if(e.nextElement().amISubOp(E.name))
						return true;
				break;
			case 111: // -subop
				if(CMSecurity.isASysOp(E))
					return false;
				for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
					if(e.nextElement().amISubOp(E.name))
						return false;
				break;
			case 0: // -class
			{
				final CharClass C=CMClass.getCharClass(E.charClass);
				if((C==null)||(!CMParms.contains(entry.parms,C.name())))
					return false;
				break;
			}
			case 1: // -baseclass
			{
				final CharClass C=CMClass.getCharClass(E.charClass);
				if((C==null)||(!CMParms.contains(entry.parms,C.baseClass())))
					return false;
				break;
			}
			case 117: // +baseclass
			{
				final CharClass C=CMClass.getCharClass(E.charClass);
				if((C!=null)&&(CMParms.contains(entry.parms,C.baseClass())))
					return false;
				break;
			}
			case 2: // -race
			{
				final Race R=CMClass.getRace(E.race); 
				if((R==null)||(!CMParms.contains(entry.parms,R.name())))
					return false;
				break;
			}
			case 5: // -level
				{
					final int level=E.level;
					boolean found=false;
					for(int v=0;v<entry.parms.length-1;v+=2)
						switch(((Integer)entry.parms[v]).intValue())
						{
							case 37: // +lvlgr
								if(level>((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
							case 38: // +lvlge
								if(level>=((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
							case 39: // +lvlt
								if(level<((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
							case 40: // +lvlle
								if(level<=((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
							case 41: // +lvleq
								if(level==((Integer)entry.parms[v+1]).intValue())
								   found=true;
								break;
						}
					if(!found) return false;
				}
				break;
				case 113: // -questwin
				{
					boolean found=false;
					for(final Object o : entry.parms)
					{
						final Quest Q=CMLib.quests().fetchQuest((String)o);
						if((Q!=null)&&(Q.wasWinner(E.name)))
						{ found=true; break;}
					}
					if(!found) return false;
				}
				break;
			case 114: // +questwin
			{
				for(final Object o : entry.parms)
				{
					final Quest Q=CMLib.quests().fetchQuest((String)o);
					if((Q!=null)&&(Q.wasWinner(E.name)))
					{ return false;}
				}
			}
			break;
			case 9: // -names
				{
					boolean found=false;
					final String name=E.name;
					for(final Object o : entry.parms)
						if(name.equalsIgnoreCase((String)o))
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 11: // -npc
				break; // always true
			case 12: // -racecat
			{
				final Race R=CMClass.getRace(E.race); 
				if((R==null)||(!CMParms.contains(entry.parms,R.racialCategory())))
					return false;
				break;
			}
			case 112: // +race
			{
				final Race R=CMClass.getRace(E.race); 
				if((R!=null)&&(CMParms.contains(entry.parms,R.name())))
					return false;
				break;
			}
			case 13: // +racecat
			{
				final Race R=CMClass.getRace(E.race); 
				if((R!=null)&&(CMParms.contains(entry.parms,R.racialCategory())))
					return false;
				break;
			}
			case 53: // -JavaClass
				{
					boolean found=false;
					for(final Object o : entry.parms)
						if("StdMOB".equalsIgnoreCase((String)o))
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 54: // +JavaClass
					for(final Object o : entry.parms)
						if("StdMOB".equalsIgnoreCase((String)o))
						{ return false;}
				break;
			case 16: // +name
				{
					final String name=E.name;
					for(final Object o : entry.parms)
						if(name.equalsIgnoreCase((String)o))
						{ return false;}
				}
				break;
			case 17: // -anyclass
				{
					boolean found=false;
					final CharClass C=CMClass.getCharClass(E.charClass);
					if(C!=null)
					for(final Object o : entry.parms)
						if(C.name().equalsIgnoreCase((String)o))
						{ found=true; break;}
					if(!found) return false;
				}
				break;
			case 18: // +anyclass
			{
				final CharClass C=CMClass.getCharClass(E.charClass);
				if(C!=null)
					for(final Object o : entry.parms)
						if(C.name().equalsIgnoreCase((String)o))
						{ return false; }
				break;
			}
			case 3: // -alignment
			case 4: // -gender
			case 6: // -classlevel
			case 103: // -maxclasslevel
			case 7: // -tattoo
			case 8: // +tattoo
			case 120: // -mood
			case 121: // +mood
			case 81: // -expertise
			case 82: // +expertise
			case 83: // -skill
			case 101: // -skillflag
			case 84: // +skill
			case 102: // +skillflag
			case 79: // -security
			case 80: // +security
			case 10: // -player
			case 14: // -clan
			case 15: // +clan
			case 49: // +material
			case 50: // -material
			case 57: // +wornOn
			case 58: // -wornOn
			case 69: // +disposition
			case 70: // -disposition
			case 71: // +senses
			case 72: // -senses
			case 73: // +HOUR
			case 74: // -HOUR
			case 75: // +season
			case 76: // -season
			case 104: // +weather
			case 105: // -weather
			case 77: // +month
			case 78: // -month
			case 106: // +day
			case 107: // -day
			case 85: // +quallvl
			case 86: // -quallvl
			case 51: // +resource
			case 52: // -resource
			case 44: // -deity
			case 45: // +deity
			case 43: // -effects
			case 46: // -faction
			case 47: // +faction
			case 42: // +effects
			case 19: // +adjstr
			case 20: // +adjint
			case 21: // +adjwis
			case 22: // +adjdex
			case 23: // -adjcha
			case 24: // +adjcha
			case 25: // -adjstr
			case 26: // -adjint
			case 27: // -adjwis
			case 28: // -adjdex
			case 29: // -adjcon
			case 30: // -adjcha
			case 87: // +str
			case 88: // +int
			case 89: // +wis
			case 90: // +dex
			case 91: // +con
			case 92: // +cha
			case 93: // -str
			case 94: // -int
			case 95: // -wis
			case 96: // -dex
			case 97: // -con
			case 98: // -cha
			case 55: // +able
			case 56: // -able
			case 61: // +weight
			case 62: // -weight
			case 63: // +armor
			case 64: // -armor
			case 65: // +damage
			case 66: // -damage
			case 67: // +attack
			case 68: // -attack
			case 59: // +value
			case 60: // -value
			case 31: // -area
			case 32: // +area
			case 100: // -home
			case 99: // +home
			case 33: // -item
			case 48: // -worn
			case 35: // +alignment
			case 36: // +gender
			case 116: // +groupsize
			case 115: // -groupsize
			case 118: // -if
			case 119: // +if
				return false;
			case 37: // +lvlgr
				if((entry.parms.length>0)&&((E.level)>((Integer)entry.parms[0]).intValue()))
				   return false;
				break;
			case 38: // +lvlge
				if((entry.parms.length>0)&&((E.level)>=((Integer)entry.parms[0]).intValue()))
				   return false;
				break;
			case 39: // +lvlt
				if((entry.parms.length>0)&&((E.level)<((Integer)entry.parms[0]).intValue()))
				   return false;
				break;
			case 40: // +lvlle
				if((entry.parms.length>0)&&((E.level)<=((Integer)entry.parms[0]).intValue()))
				   return false;
				break;
			case 41: // +lvleq
				if((entry.parms.length>0)&&((E.level)==((Integer)entry.parms[0]).intValue()))
				   return false;
				break;
			case 122: // -chance
				if((entry.parms.length>0)&&(CMLib.dice().rollPercentage()<(((Integer)entry.parms[0]).intValue())))
				   return false;
				break;
			case 34: // +class
			{
				final CharClass C=CMClass.getCharClass(E.charClass);
				if(C!=null)
				if(CMParms.contains(entry.parms,C.name()))
					return false;
				break;
			}
			}
			}catch(NullPointerException n){}
		}
		return true;
	}
	
	public String[] separateMaskStrs(final String newText)
	{
		final String[] strs=new String[2];
		final int maskindex=newText.toUpperCase().indexOf("MASK=");
		if(maskindex>0)
		{
			strs[1]=newText.substring(maskindex+5).trim();
			strs[0]=newText.substring(0,maskindex).trim();
		}
		else
		{
			strs[0]=newText;
			strs[1]="";
		}
		return strs;
	}
}
