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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMaskEntry;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2004-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "MUDZapper";
	}

	public Map<String,ZapperKey> zapCodes=new Hashtable<String,ZapperKey>();

	private static class SavedRace
	{
		public final String name;
		public final String upperName;
		public final String racialCategory;
		public final String upperCatName;
		public final String nameStart;
		public final String minusNameStart;
		public final String catNameStart;
		public final String minusCatNameStart;
		public SavedRace(final Race race, final int startChars)
		{
			name=race.name();
			upperName=name.toUpperCase();
			nameStart=CMStrings.safeLeft(name.toUpperCase(),startChars);
			minusNameStart="-"+nameStart;
			racialCategory=race.racialCategory();
			upperCatName=racialCategory.toUpperCase();
			catNameStart=CMStrings.safeLeft(racialCategory.toUpperCase(),startChars);
			minusCatNameStart="-"+catNameStart;
		}
	}
	
	private static class SavedClass
	{
		public final String id;
		public final String name;
		public final String upperName;
		public final String baseClass;
		public final String nameStart;
		public final String plusNameStart;
		public final String minusNameStart;
		public final String baseClassStart;
		public final String plusBaseClassStart;

		public SavedClass(final CharClass charClass, final int startChars)
		{
			name=charClass.name();
			id=charClass.ID();
			upperName=name.toUpperCase();
			nameStart=CMStrings.safeLeft(name.toUpperCase(),startChars);
			plusNameStart="+"+nameStart;
			minusNameStart="-"+nameStart;
			baseClass=charClass.baseClass();
			baseClassStart=CMStrings.safeLeft(baseClass.toUpperCase(),startChars);
			plusBaseClassStart="+"+baseClassStart;
		}
	}
	
	public static class CompiledZapperMaskEntryImpl implements CompiledZMaskEntry
	{
		private final ZapperKey maskType;
		private final Object[] parms;
		
		@Override
		public ZapperKey maskType()
		{
			return maskType;
		}
		
		@Override
		public Object[] parms()
		{
			return parms;
		}
		
		public CompiledZapperMaskEntryImpl(final ZapperKey type, final Object[] parms)
		{
			maskType = type;
			this.parms = parms;
		}
	}

	public static class CompiledZapperMaskImpl implements CompiledZMask
	{
		private final boolean[] flags;
		private final boolean empty;
		private final CompiledZMaskEntry[] entries;

		@Override
		public boolean[] flags()
		{
			return flags;
		}
		
		@Override
		public boolean empty()
		{
			return empty;
		}
		
		@Override
		public CompiledZMaskEntry[] entries()
		{
			return entries;
		}

		public CompiledZapperMaskImpl(final boolean[] flags, final CompiledZMaskEntry[] entries)
		{
			this.flags = flags;
			this.entries = entries;
			this.empty = false;
		}

		public CompiledZapperMaskImpl(final boolean[] flags, final CompiledZMaskEntry[] entries, final boolean empty)
		{
			this.flags = flags;
			this.entries = entries;
			this.empty = empty;
		}
	}
	
	protected MOB nonCrashingMOB=null;
	protected MOB nonCrashingMOB()
	{
		if(nonCrashingMOB!=null)
			return nonCrashingMOB;
		nonCrashingMOB=CMClass.getMOB("StdMOB");
		return nonCrashingMOB;
	}

	protected Item nonCrashingItem=null;
	protected Item nonCrashingItem(final MOB mob)
	{
		if(mob.numItems()>0)
		{
			final Item I = mob.getItem(0);
			if(I!=null)
				return I;
		}
		if(nonCrashingItem!=null)
			return nonCrashingItem;
		nonCrashingItem=CMClass.getItem("StdItem");
		return nonCrashingItem;
	}

	@Override
	public String rawMaskHelp()
	{
		String maskHelp = (String)Resources.getResource("SYSTEM_ZAPPERMASK_HELP");
		if(maskHelp == null)
		{
			final CMFile F = new CMFile(Resources.makeFileResourceName("help/zappermasks.txt"),null,CMFile.FLAG_LOGERRORS);
			if((F.exists()) && (F.canRead()))
			{
				List<String> lines=Resources.getFileLineVector(F.text());
				StringBuilder str = new StringBuilder("");
				for(String line : lines)
				{
					str.append(line.trim()).append("<BR>");
				}
				maskHelp = str.toString();
			}
			else
				maskHelp = "ZAPPERMASK HELP NOT FOUND at /resources/help/zappermasks.txt!";
			Resources.submitResource("SYSTEM_ZAPPERMASK_HELP",maskHelp);
		}
		return maskHelp;
	}

	protected volatile List<SavedClass>	savedCharClasses		= new Vector<SavedClass>(1);
	protected volatile List<SavedRace>	savedRaces				= new Vector<SavedRace>(1);
	protected volatile long				savedClassUpdateTime	= 0;

	public synchronized void buildSavedClasses()
	{
		if(savedClassUpdateTime==CMClass.getLastClassUpdatedTime())
			return;
		final List<SavedClass> tempSavedCharClasses=new LinkedList<SavedClass>();
		final List<SavedRace> tempSavedRaces=new LinkedList<SavedRace>();
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

	@Override
	@SuppressWarnings("unchecked")
	public CompiledZMask getPreCompiledMask(final String str)
	{
		Map<String,CompiledZMask> H=(Map<String,CompiledZMask>)Resources.getResource("SYSTEM_HASHED_MASKS");
		if(H==null)
		{
			H=new PrioritizingLimitedMap<String,CompiledZMask>(200, 10*60*1000, Long.MAX_VALUE, 50);
			Resources.submitResource("SYSTEM_HASHED_MASKS",H);
		}
		final String lowerStr=(str==null)?"":str.toLowerCase().trim();
		CompiledZMask V=H.get(lowerStr);
		if(V==null)
		{
			V=maskCompile(str);
			H.put(lowerStr,V);
		}
		return V;
	}

	@Override
	public CompiledZMask createEmptyMask()
	{
		return new CompiledZapperMaskImpl(new boolean[2], new CompiledZMaskEntry[0], true);
	}
	
	@Override
	public Map<String,ZapperKey> getMaskCodes()
	{
		if(zapCodes.size()==0)
		{
			for(ZapperKey Z : ZapperKey.values())
			{
				for(String key : Z.keys())
					zapCodes.put(key, Z);
			}
		}
		return zapCodes;
	}

	@Override
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
		final int x=str.indexOf('&');
		if(x>=0)
		{
			final Vector<Object> V=new Vector<Object>();
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
			final Object[] o2=new Object[V.size()];
			for(int v=0;v<V.size();v++)
			{
				if(V.elementAt(v) instanceof String)
					o2[v]=makeSkillFlagObject((String)V.elementAt(v));
				else
					o2[v]=V.elementAt(v);
			}
			for(int i=0;i<o2.length;i++)
			{
				if ((o2[i] != null) && (!(o2[i] instanceof Boolean)))
				{
					o = o2;
					break;
				}
			}
		}
		if(o==null)
		{
			for(int d=0;d<Ability.ACODE_DESCS.length;d++)
			{
				if(Ability.ACODE_DESCS[d].equals(str))
				{
					o=Integer.valueOf(d);
					break;
				}
			}
		}
		if(o==null)
		{
			for(int d=0;d<Ability.DOMAIN_DESCS.length;d++)
			{
				if(Ability.DOMAIN_DESCS[d].startsWith(str)||Ability.DOMAIN_DESCS[d].endsWith(str))
				{
					o=Integer.valueOf(d<<5);
					break;
				}
			}
		}
		if(o==null)
		{
			for(int d=0;d<Ability.FLAG_DESCS.length;d++)
			{
				if(Ability.FLAG_DESCS[d].startsWith(str))
				{
					o=Long.valueOf(1<<d);
					break;
				}
			}
		}
		if(o==null)
		{
			for(short d=0;d<Ability.QUALITY_DESCS.length;d++)
			{
				if(Ability.QUALITY_DESCS[d].startsWith(str)||Ability.QUALITY_DESCS[d].endsWith(str))
				{
					o=new Short(d);
					break;
				}
			}
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
				{
					if(set[i] instanceof Boolean)
					{
						if(evaluateSkillFlagObject(set[i+1],A))
							return false;
						i++;
					}
					else
					if(!evaluateSkillFlagObject(set[i],A))
						return false;
				}
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
				final long val=((Long)o).longValue();
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
			if(str.length()==0)
				continue;
			if(getMaskCodes().containsKey(str))
				return false;
			final Object o=makeSkillFlagObject(str);
			if(o==null)
				continue;
			for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(evaluateSkillFlagObject(o,A))
					return true;
			}
		}
		return false;
	}

	protected CompiledZMaskEntry levelCompiledHelper(final String str, final char c)
	{
		final ArrayList<Object> parms=new ArrayList<Object>();
		ZapperKey entryType=null;
		if(str.startsWith(c+">=")&&(CMath.isNumber(str.substring(3).trim())))
		{
			entryType=getMaskCodes().get("+LVLGE");
			parms.add(Integer.valueOf(CMath.s_int(str.substring(3).trim())));
		}
		else
		if(str.startsWith(c+"<=")&&(CMath.isNumber(str.substring(3).trim())))
		{
			entryType=getMaskCodes().get("+LVLLE");
			parms.add(Integer.valueOf(CMath.s_int(str.substring(3).trim())));
		}
		else
		if(str.startsWith(c+">")&&(CMath.isNumber(str.substring(2).trim())))
		{
			entryType=getMaskCodes().get("+LVLGR");
			parms.add(Integer.valueOf(CMath.s_int(str.substring(2).trim())));
		}
		else
		if(str.startsWith(c+"<")&&(CMath.isNumber(str.substring(2).trim())))
		{
			entryType=getMaskCodes().get("+LVLLT");
			parms.add(Integer.valueOf(CMath.s_int(str.substring(2).trim())));
		}
		else
		if(str.startsWith(c+"=")&&(CMath.isNumber(str.substring(2).trim())))
		{
			entryType=getMaskCodes().get("+LVLEQ");
			parms.add(Integer.valueOf(CMath.s_int(str.substring(2).trim())));
		}
		if((entryType==null)||(parms.size()==0))
			return null;
		return new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0]));
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

	protected int determineSeasonCode(String str)
	{
		str=str.toUpperCase().trim();
		if(str.length()==0)
			return -1;
		final TimeClock.Season season=(TimeClock.Season)CMath.s_valueOf(TimeClock.Season.class, str);
		if(season != null)
			return season.ordinal();
		for(int i=0;i<TimeClock.Season.values().length;i++)
		{
			if(TimeClock.Season.values()[i].toString().startsWith(str))
				return i;
		}
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
			if(str.length()==0)
				continue;
			if(getMaskCodes().containsKey(str))
				return false;
			if(str.equalsIgnoreCase(plusMinus+find))
				return true;
		}
		return false;
	}

	protected boolean fromHereStartsWith(final List<String> V, final char plusMinus, final int fromHere, final String find)
	{
		for(int v=fromHere;v<V.size();v++)
		{
			final String str=V.get(v);
			if(str.length()==0)
				continue;
			if(getMaskCodes().containsKey(str))
				return false;
			if(str.startsWith(plusMinus+find))
				return true;
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
			if(str.length()==0)
				continue;
			if(getMaskCodes().containsKey(str))
				return false;
			if((str.charAt(0)==plusMinus)&&str.endsWith(find))
				return true;
		}
		return false;
	}

	@Override
	public String maskDesc(final String text)
	{
		return maskDesc(text, false);
	}

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

	@Override
	public String maskDesc(final String text, final boolean skipFirstWord)
	{
		if(text.trim().length()==0)
			return L("Anyone");
		StringBuilder buf=new StringBuilder("");
		final Map<String,ZapperKey> zapCodes=getMaskCodes();
		final List<String> V=CMParms.parse(text.toUpperCase());
		int val=-1;
		for(int v=0;v<V.size();v++)
		{
			final String str=V.get(v);
			val=-1;
			if(zapCodes.containsKey(str))
			{
				final ZapperKey key = zapCodes.get(str); 
				switch(key)
				{
				case CLASS: // +class
					{
						buf.append(L("Disallows the following class"+(multipleQuals(V,v,"-")?"es":"")+": "));
						for(final SavedClass C : charClasses())
						{
							final String cstr=C.minusNameStart;
							for(int v2=v+1;v2<V.size();v2++)
							{
								final String str2=V.get(v2);
								if(str2.length()==0)
									continue;
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
				case _CLASS: // -class
					{
						buf.append(L(skipFirstWord?"Only ":"Allows only "));
						for(final SavedClass C : charClasses())
						{
							final String cstr=C.plusNameStart;
							for(int v2=v+1;v2<V.size();v2++)
							{
								final String str2=V.get(v2);
								if(str2.length()==0)
									continue;
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
				case BASECLASS: // +baseclass
					{
						buf.append(L("Disallows the following types"+(multipleQuals(V,v,"-")?"s":"")+": "));
						final HashSet<String> seenBase=new HashSet<String>();
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
					}
					break;
				case _BASECLASS: // -baseclass
					{
						buf.append(L(skipFirstWord?"Only ":"Allows only "));
						final HashSet<String> seenBase=new HashSet<String>();
						for(final SavedClass C : charClasses())
						{
							final String cstr=C.plusBaseClassStart;
							if(!seenBase.contains(C.baseClass))
							{
								seenBase.add(C.baseClass);
								for(int v2=v+1;v2<V.size();v2++)
								{
									final String str2=V.get(v2);
									if(str2.length()==0)
										continue;
									if(zapCodes.containsKey(str2))
										break;
									if(str2.startsWith(cstr))
										buf.append(L("@x1 types, ",C.baseClass));
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case _RACE: // -Race
					{
						buf.append(L(skipFirstWord?"Only ":"Allows only "));
						final LinkedList<String> cats=new LinkedList<String>();
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
				case _RACECAT: // -Racecats
					{
						buf.append(L(skipFirstWord?"Only these racial categor"+(multipleQuals(V,v,"+")?"ies":"y")+" ":"Allows only these racial categor"+(multipleQuals(V,v,"+")?"ies":"y")+" "));
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
				case ALIGNMENT: // Alignment
					{
						buf.append(L("Disallows the following alignment"+(multipleQuals(V,v,"-")?"s":"")+": "));
						if(fromHereStartsWith(V,'-',v+1,Faction.Align.EVIL.toString().substring(0,3)))
							buf.append(L(Faction.Align.EVIL.toString().toLowerCase()+", "));
						if(fromHereStartsWith(V,'-',v+1,Faction.Align.GOOD.toString().substring(0,3)))
							buf.append(L(Faction.Align.GOOD.toString().toLowerCase()+", "));
						if(fromHereStartsWith(V,'-',v+1,Faction.Align.NEUTRAL.toString().substring(0,3)))
							buf.append(L(Faction.Align.NEUTRAL.toString().toLowerCase()+", "));
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case _ALIGNMENT: // -Alignment
					{
						buf.append(L(skipFirstWord?"Only ":"Allows only "));
						if(fromHereStartsWith(V,'+',v+1,Faction.Align.EVIL.toString().substring(0,3)))
							buf.append(L(Faction.Align.EVIL.toString().toLowerCase()+", "));
						if(fromHereStartsWith(V,'+',v+1,Faction.Align.GOOD.toString().substring(0,3)))
							buf.append(L(Faction.Align.GOOD.toString().toLowerCase()+", "));
						if(fromHereStartsWith(V,'+',v+1,Faction.Align.NEUTRAL.toString().substring(0,3)))
							buf.append(L(Faction.Align.NEUTRAL.toString().toLowerCase()+", "));
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case GENDER:
					{
						buf.append(L("Disallows the following gender"+(multipleQuals(V,v,"-")?"s":"")+": "));
						if(fromHereStartsWith(V,'-',v+1,"MALE"))
							buf.append(L("Male, "));
						if(fromHereStartsWith(V,'-',v+1,"FEMALE"))
							buf.append(L("Female, "));
						if(fromHereStartsWith(V,'-',v+1,"NEUTER"))
							buf.append(L("Neuter"));
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case _GENDER: // -Gender
					{
						buf.append(L(skipFirstWord?"Only ":"Allows only "));
						if(fromHereStartsWith(V,'+',v+1,"MALE"))
							buf.append(L("Male, "));
						if(fromHereStartsWith(V,'+',v+1,"FEMALE"))
							buf.append(L("Female, "));
						if(fromHereStartsWith(V,'+',v+1,"NEUTER"))
							buf.append(L("Neuter"));
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case _LEVEL: // -Levels
					{
						for(int v2=v+1;v2<V.size();v2++)
							buf.append(levelHelp(V.get(v2),'+',L(skipFirstWord?"Only ":"Allows only ")));
					}
					break;
				case _CLASSLEVEL: // -ClassLevels
					{
						for(int v2=v+1;v2<V.size();v2++)
							buf.append(levelHelp(V.get(v2),'+',L(skipFirstWord?"Only class ":"Allows only class ")));
					}
					break;
				case _MAXCLASSLEVEL: // -MaxclassLevels
					{
						for(int v2=v+1;v2<V.size();v2++)
							buf.append(levelHelp(V.get(v2),'+',L(skipFirstWord?"Only highest class ":"Allows only highest class ")));
					}
					break;
				case _CLASSTYPE: // -classtype
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" the following type"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case CLASSTYPE: // +classtype
					{
						buf.append(L("Disallows the following type"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _TATTOO: // -Tattoos
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" the following tattoo"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case TATTOO: // +Tattoos
					{
						buf.append(L("Disallows the following tattoo"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _WEAPONAMMO: // -WeaponAmmo
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" weapons that use: "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case WEAPONAMMO: // +weaponsmmo
					{
						buf.append(L("Disallows weapons that use : "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _MOOD: // -Mood
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" the following mood"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case MOOD: // +Mood
					{
						buf.append(L("Disallows the following mood"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _ACCCHIEVE: // -accchieves
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" the following account achievement"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								final AchievementLibrary.Achievement A=CMLib.achievements().getAchievement(str2.substring(1));
								if(A!=null)
									buf.append(A.getDisplayStr()+", ");
								else
									buf.append(str2.substring(1)+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case ACCCHIEVE: // +accchieves
					{
						buf.append(L("Disallows the following account achievement"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								final AchievementLibrary.Achievement A=CMLib.achievements().getAchievement(str2.substring(1));
								if(A!=null)
									buf.append(A.getDisplayStr()+", ");
								else
									buf.append(str2.substring(1)+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case _SECURITY: // -Security
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" following security flag"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case SECURITY: // +security
					{
						buf.append(L("Disallows the following security flag"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _EXPERTISE: // -expertises
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" following expertise"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								final ExpertiseLibrary.ExpertiseDefinition E=CMLib.expertises().getDefinition(str2.substring(1).toUpperCase().trim());
								if(E!=null)
									buf.append(E.name()+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case EXPERTISE: // +expertises
					{
						buf.append(L("Disallows the following expertise"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								final ExpertiseLibrary.ExpertiseDefinition E=CMLib.expertises().getDefinition(str2.substring(1).toUpperCase().trim());
								if(E!=null)
									buf.append(E.name()+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case _SKILLFLAG: // -skillflags
					{
						buf.append(L((skipFirstWord?"A":"Requires a")+" skill of type: "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								final List<String> V3=CMParms.parseAny(str2.substring(1),'&',true);
								String str3=null;
								for(int v3=0;v3<V3.size();v3++)
								{
									str3=CMStrings.replaceAll(CMStrings.capitalizeAndLower(V3.get(v3)),"_"," ");
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
				case _SKILL: // -skills
					{
						buf.append(L((skipFirstWord?"O":"Requires o")+"ne of the following skills: "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								int prof=0;
								str2=str2.substring(1);
								final int x=str2.indexOf('(');
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
										buf.append(L("@x1 at @x2% proficiency, ",A.name(),""+prof));
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case SKILL: // +skills
					{
						buf.append(L("Disallows the following skill"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								int prof=0;
								str2=str2.substring(1);
								final int x=str2.indexOf('(');
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
										buf.append(L("@x1 at more than @x2% proficiency, ",A.name(),""+prof));
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case SKILLFLAG: // +skillflag
					{
						buf.append(L("Disallows the skill of type: "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								final List<String> V3=CMParms.parseAny(str2.substring(1),'&',true);
								String str3=null;
								for(int v3=0;v3<V3.size();v3++)
								{
									str3=CMStrings.replaceAll(CMStrings.capitalizeAndLower(V3.get(v3)),"_"," ");
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
				case _CLAN: // -Clan
					{
						buf.append(L((skipFirstWord?"M":"Requires m")+"embership in the following clan"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case CLAN: // +Clan
					{
						buf.append(L("Disallows the following clan"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _MATERIAL: // -Material
					{
						buf.append(L((skipFirstWord?"C":"Requires c")+"onstruction from the following material"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case MATERIAL: // +Material
					{
						buf.append(L("Disallows items of the following material"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _WORNON: // -wornon
					{
						buf.append(L((skipFirstWord?"A":"Requires a")+"bility to be worn: "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								final long code=Wearable.CODES.FIND_endsWith(str2.substring(1));
								if(code>=0)
									buf.append(Wearable.CODES.NAME(code)+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case WORNON: // +wornon
					{
						buf.append(L("Disallows items capable of being worn: "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								final long code=Wearable.CODES.FIND_endsWith(str2.substring(1));
								if(code>=0)
									buf.append(Wearable.CODES.NAME(code)+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case _SENSES: // -senses
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" following sense"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								final int code=CMLib.flags().getSensesIndex(str2.substring(1));
								if(code>=0)
									buf.append(PhyStats.CAN_SEE_DESCS[code]+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case SENSES: // +senses
					{
						buf.append(L("Disallows the following sense"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								final int code=CMLib.flags().getSensesIndex(str2.substring(1));
								if(code>=0)
									buf.append(PhyStats.CAN_SEE_DESCS[code]+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case HOUR: // +HOUR
					{
						buf.append(L("Disallowed during the following time"+(multipleQuals(V,v,"-")?"s":"")+" of the day: "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _HOUR: // -HOUR
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"during the following time"+(multipleQuals(V,v,"+")?"s":"")+" of the day: "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case SEASON: // +season
					{
						buf.append(L("Disallowed during the following season"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								if(CMath.isInteger(str2.substring(1).trim()))
								{
									final int season=CMath.s_int(str2.substring(1).trim());
									if((season>=0)&&(season<TimeClock.Season.values().length))
										buf.append(TimeClock.Season.values()[season].toString()+", ");
								}
								else
								{
									final int season=determineSeasonCode(str2.substring(1).trim());
									if((season>=0)&&(season<TimeClock.Season.values().length))
										buf.append(TimeClock.Season.values()[season].toString()+", ");
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case _SEASON: // -season
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"during the following season"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								if(CMath.isInteger(str2.substring(1).trim()))
								{
									final int season=CMath.s_int(str2.substring(1).trim());
									if((season>=0)&&(season<TimeClock.Season.values().length))
										buf.append(TimeClock.Season.values()[season].toString()+", ");
								}
								else
								{
									final int season=determineSeasonCode(str2.substring(1).trim());
									if((season>=0)&&(season<TimeClock.Season.values().length))
										buf.append(TimeClock.Season.values()[season].toString()+", ");
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case WEATHER: // +weather
					{
						buf.append(L("Disallowed during the following weather condition"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								if(CMath.isInteger(str2.substring(1).trim()))
								{
									final int weather=CMath.s_int(str2.substring(1).trim());
									if((weather>=0)&&(weather<Climate.WEATHER_DESCS.length))
										buf.append(CMStrings.capitalizeAndLower(Climate.WEATHER_DESCS[weather])+", ");
								}
								else
								{
									final int weather=CMParms.indexOf(Climate.WEATHER_DESCS,str2.substring(1).toUpperCase().trim());
									if((weather>=0)&&(weather<Climate.WEATHER_DESCS.length))
										buf.append(CMStrings.capitalizeAndLower(Climate.WEATHER_DESCS[weather])+", ");
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case _WEATHER: // -weather
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"during the following weather condition"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								if(CMath.isInteger(str2.substring(1).trim()))
								{
									final int weather=CMath.s_int(str2.substring(1).trim());
									if((weather>=0)&&(weather<Climate.WEATHER_DESCS.length))
										buf.append(CMStrings.capitalizeAndLower(Climate.WEATHER_DESCS[weather])+", ");
								}
								else
								{
									final int weather=CMParms.indexOf(Climate.WEATHER_DESCS,str2.substring(1).toUpperCase().trim());
									if((weather>=0)&&(weather<Climate.WEATHER_DESCS.length))
										buf.append(CMStrings.capitalizeAndLower(Climate.WEATHER_DESCS[weather])+", ");
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case MONTH: // +month
					{
						buf.append(L("Disallowed during the following month"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _MONTH: // -month
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"during the following month"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case DAY: // +day
					{
						buf.append(L("Disallowed during the following day"+(multipleQuals(V,v,"-")?"s":"")+" of the month: "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _DAY: // -day
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"on the following day"+(multipleQuals(V,v,"+")?"s":"")+" of the month: "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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

				case QUALLVL: // +quallvl
					if((v+1)<V.size())
					{
						final Ability A=CMClass.getAbility(V.get(v+1));
						if(A!=null)
						{
							int adjustment=0;
							if(((v+2)<V.size())&&(CMath.isInteger(V.get(v+2))))
								adjustment=CMath.s_int(V.get(v+2));
							buf.append(A.Name());
							if(adjustment!=0)
								buf.append(L("Qualifies for @x1",A.Name()));
							else
							if(adjustment<0)
								buf.append(L("@x1 levels before qualifying for @x2",""+(-adjustment),A.Name()));
							else
								buf.append(L("@x1 levels after qualifying for @x2",""+adjustment,A.Name()));
							buf.append(".  ");
						}
					}
					break;
				case _QUALLVL: // -quallvl
					if((v+1)<V.size())
					{
						final Ability A=CMClass.getAbility(V.get(v+1));
						if(A!=null)
						{
							int adjustment=0;
							if(((v+2)<V.size())&&(CMath.isInteger(V.get(v+2))))
								adjustment=CMath.s_int(V.get(v+2));
							buf.append(A.Name());
							if(adjustment!=0)
								buf.append(L("Does not qualify for @x1",A.Name()));
							else
							if(adjustment<0)
								buf.append(L("Still prior to @x1 levels before qualifying for @x2",""+(-adjustment),A.Name()));
							else
								buf.append(L("Still prior to @x1 levels after qualifying for @x2",""+adjustment,A.Name()));
							buf.append(".  ");
						}
					}
					break;
				case _DISPOSITION: // -disposition
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" following disposition"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								final int code=CMLib.flags().getDispositionIndex(str2.substring(1));
								if(code>=0)
									buf.append(PhyStats.IS_DESCS[code]+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case DISPOSITION: // +disposition
					{
						buf.append(L("Disallows the following disposition"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								final int code=CMLib.flags().getDispositionIndex(str2.substring(1));
								if(code>=0)
									buf.append(PhyStats.IS_DESCS[code]+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case _RESOURCE: // -Resource
					{
						buf.append(L((skipFirstWord?"C":"Requires c")+"onstruction from the following material"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case RESOURCE: // +Resource
					{
						buf.append(L("Disallows items of the following material"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _JAVACLASS: // -JavaClass
					{
						buf.append(L((skipFirstWord?"B":"Requires b")+"eing of the following type"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case JAVACLASS: // +JavaClass
					{
						buf.append(L("Disallows being of the following type"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _DEITY: // -Deity
					{
						buf.append(L((skipFirstWord?"W":"Requires w")+"orshipping the following deity"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case DEITY: // +Deity
					{
						buf.append(L("Disallows the worshippers of: "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case NAME: // +Names
					{
						buf.append(L("Disallows the following mob/player name"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _NAME: // -Names
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" following name"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case ACCOUNT: // +Account
					{
						buf.append(L("Disallows the following player account"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _ACCOUNT: // -Account
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" following player account"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _QUESTWIN: // -Questwin
					{
						buf.append(L((skipFirstWord?"Completing":"Requires completing")+" the following quest"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								final Quest Q=CMLib.quests().fetchQuest(str2.substring(1));
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
				case QUESTWIN: // +Questwin
					{
						buf.append(L("Disallows those who`ve won the following quest"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								final Quest Q=CMLib.quests().fetchQuest(str2.substring(1));
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
				case _PLAYER: // -Player
					buf.append(L("Disallows players.  "));
					break;
				case _NPC: // -MOB
					buf.append(L("Disallows mobs/npcs.  "));
					break;
				case RACE: // +races
					{
						buf.append(L("Disallows the following race"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case RACECAT: // +racecats
					{
						buf.append(L("Disallows the following racial category"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _ANYCLASSLEVEL: // -anyclasslevel
					{
						String className = "";
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String s = V.get(v2);
							if(getMaskCodes().containsKey(s))
								break;
							boolean checkForClass = true;
							boolean found=false;
							if(className.length()>0)
							{
								StringBuilder lvlHelp = levelHelp(V.get(v2),'+',L(skipFirstWord?"Only "+className+" ":"Allows only "+className+" "));
								if(lvlHelp.length()>0)
								{
									buf.append(lvlHelp);
									checkForClass = false;
									found=true;
								}
							}
							if(checkForClass)
							{
								for(final SavedClass C : charClasses())
								{
									if(s.startsWith('+'+C.nameStart))
									{
										className = C.name;
										found=true;
										break;
									}
								}
							}
							if(!found)
								break;
						}
					}
					break;
				case ANYCLASSLEVEL: // +anyclasslevel
					{
						String className = "";
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String s = V.get(v2);
							if(getMaskCodes().containsKey(s))
							{
								v=v2-1;
								break;
							}
							boolean checkForClass = true;
							boolean found=false;
							if(className.length()>0)
							{
								StringBuilder lvlHelp = levelHelp(V.get(v2),'-',"Disallows "+className+" ");
								if(lvlHelp.length()>0)
								{
									buf.append(lvlHelp);
									checkForClass = false;
									found = true;
								}
							}
							if(checkForClass)
							{
								for(final SavedClass C : charClasses())
								{
									if(s.startsWith('-'+C.nameStart))
									{
										className = C.name;
										found = true;
										break;
									}
								}
							}
							if(!found)
							{
								v=v2-1;
								break;
							}
							else
							if(v2==V.size()-1)
							{
								v=v2;
								break;
							}
						}
					}
					break;
				case _ANYCLASS: // -anyclass
					{
						buf.append(L((skipFirstWord?"L":"Requires l")+"evels in one of the following:  "));
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
				case ANYCLASS: // +anyclass
					{
						buf.append(L("Disallows any levels in any of the following:  "));
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
				case ADJSTRENGTH: // +adjstr
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" strength of at least @x1.  ",""+val));
					break;
				case ADJINTELLIGENCE: // +adjint
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"An":"Requires an")+" intelligence of at least @x1.  ",""+val));
					break;
				case ADJWISDOM: // +adjwis
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" wisdom of at least @x1.  ",""+val));
					break;
				case ADJDEXTERITY: // +adjdex
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" dexterity of at least @x1.  ",""+val));
					break;
				case ADJCONSTITUTION: // -adjcha
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" constitution of at least @x1.  ",""+val));
					break;
				case ADJCHARISMA: // +adjcha
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" charisma of at least @x1.  ",""+val));
					break;
				case _ADJSTRENGTH: // -adjstr
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" strength of at most @x1.  ",""+val));
					break;
				case _ADJINTELLIGENCE: // -adjint
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"An":"Requires an")+" intelligence of at most @x1.  ",""+val));
					break;
				case _ADJWISDOM: // -adjwis
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" wisdom of at most @x1.  ",""+val));
					break;
				case _ADJDEXTERITY: // -adjdex
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" dexterity of at most @x1.  ",""+val));
					break;
				case _ADJCONSTITUTION: // -adjcon
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" constitution of at most @x1.  ",""+val));
					break;
				case _ADJCHARISMA: // -adjcha
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" charisma of at most @x1.  ",""+val));
					break;
				case STRENGTH: // +str
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" base strength of at least @x1.  ",""+val));
					break;
				case INTELLIGENCE: // +int
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" base intelligence of at least @x1.  ",""+val));
					break;
				case WISDOM: // +wis
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" base wisdom of at least @x1.  ",""+val));
					break;
				case DEXTERITY: // +dex
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" base dexterity of at least @x1.  ",""+val));
					break;
				case CONSTITUTION: // +con
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" base constitution of at least @x1.  ",""+val));
					break;
				case CHARISMA: // +cha
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" base charisma of at least @x1.  ",""+val));
					break;
				case _STRENGTH: // -str
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" base strength of at most @x1.  ",""+val));
					break;
				case _INTELLIGENCE: // -int
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" base intelligence of at most @x1.  ",""+val));
					break;
				case _WISDOM: // -wis
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" base wisdom of at most @x1.  ",""+val));
					break;
				case _DEXTERITY: // -dex
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" base dexterity of at most @x1.  ",""+val));
					break;
				case _CONSTITUTION: // -con
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" base constitution of at most @x1.  ",""+val));
					break;
				case _CHARISMA: // -cha
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" base charisma of at most @x1.  ",""+val));
					break;
				case _CHANCE: // -chance
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"":"Allowed ")+" "+(100-val)+"% of the time.  "));
					break;
				case ABILITY: // +ability
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" magic/ability of at most @x1.  ",""+val));
					break;
				case _ABILITY: // -ability
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" magic/ability of at least @x1.  ",""+val));
					break;
				case VALUE: // +value
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" value of at most @x1.  ",""+val));
					break;
				case _VALUE: // -value
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" value of at least @x1.  ",""+val));
					break;
				case WEIGHT: // +weight
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" weight/encumbrance of at most @x1.  ",""+val));
					break;
				case _WEIGHT: // -weight
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" weight/encumbrance of at least @x1.  ",""+val));
					break;
				case ARMOR: // +armor
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" armor rating of at most @x1.  ",""+val));
					break;
				case _ARMOR: // -armor
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" armor rating of at least @x1.  ",""+val));
					break;
				case DAMAGE: // +damage
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" damage ability of at most @x1.  ",""+val));
					break;
				case _DAMAGE: // -damage
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" damage ability of at least @x1.  ",""+val));
					break;
				case ATTACK: // +attack
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"An":"Requires an")+" attack bonus of at most @x1.  ",""+val));
					break;
				case _ATTACK: // -attack
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"An":"Requires an")+" attack bonus of at least @x1.  ",""+val));
					break;
				case AREA: // +Area
					{
						buf.append(L("Disallows the following area"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _AREA: // -Area
					{
						buf.append(L((skipFirstWord?"The":"Requires the")+" following area"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case ISHOME: // +isHome
					buf.append(L("Disallows those who are not in their home area.  "));
					break;
				case _ISHOME: // -isHome
					buf.append(L("Disallows those who are in their home area.  "));
					break;
				case HOME: // +Home
					{
						buf.append(L("Disallows those whose home is the following area"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _HOME: // -Home
					{
						buf.append(L((skipFirstWord?"From the":"Requires being from the")+" following area"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _IFSTAT:
					{
						buf.append(L("Allows only those with "+(multipleQuals(V,v,"-")?"one of the following values":"the following value")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case IFSTAT:
					{
						buf.append(L("Disallows those with "+(multipleQuals(V,v,"-")?"one of the following values":"the following value")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case SUBNAME:
					{
						buf.append(L("Disallows those with the following partial name"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _SUBNAME:
					{
						buf.append(L("Allows only those with the following partial name"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case ITEM: // +Item
					{
						buf.append(L("Disallows those with the following item"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case WORN:
					{
						buf.append(L((skipFirstWord?"The":"Requires the")+" following worn item"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _ITEM:
					{
						buf.append(L((skipFirstWord?"H":"Requires h")+"aving the following item"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _WORN: // -Worn
					{
						buf.append(L((skipFirstWord?"W":"Requires w")+"earing the following item"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case EFFECT: // +Effects
					{
						buf.append(L("Disallows the following activities/effect"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case _EFFECT: // -Effects
					{
						buf.append(L((skipFirstWord?"P":"Requires p")+"articipation in the following activities/effect"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
				case FACTION: // +faction
					{
						buf.append(L("Disallows the following: "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								final Faction.FRange FR=getRange(str2.substring(1).toUpperCase().trim());
								if(FR!=null)
								{
									final String desc=CMLib.factions().rangeDescription(FR,"or ");
									if(desc.length()>0)
										buf.append(desc+"; ");
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
				case _FACTION: // -faction
					{
						buf.append(L((skipFirstWord?"The":"Requires the")+" following: "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								final Faction.FRange FR=getRange(str2.substring(1).toUpperCase().trim());
								if(FR!=null)
								{
									final String desc=CMLib.factions().rangeDescription(FR,"or ");
									if(desc.length()>0)
										buf.append(desc+"; ");
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
				case _WEAPONTYPE: // -weapontype
				case WEAPONTYPE: // +weapontype
				case _WEAPONCLASS: // -weaponclass
				case WEAPONCLASS: // +weaponclass
					{
						if(key == ZapperKey._WEAPONTYPE )
							buf.append(L((skipFirstWord?"The":"Requires the")+" following type of weapon(s): "));
						else
							buf.append(L((skipFirstWord?"The":"Disallows the")+" following type of weapon(s): "));
						final String cw=(key == ZapperKey._WEAPONTYPE )?"+":"-";
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith(cw))
								buf.append(str2.substring(1).toUpperCase().trim()).append(" ");
						}
						if(buf.toString().endsWith(", "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						if(buf.toString().endsWith("; "))
							buf=new StringBuilder(buf.substring(0,buf.length()-2));
						buf.append(".  ");
					}
					break;
				case _GROUPSIZE: // -groupsize
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" group size of at most @x1.  ",""+val));
					break;
				case GROUPSIZE: // +groupsize
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append(L((skipFirstWord?"A":"Requires a")+" group size of at least @x1.  ",""+val));
					break;
				case _IF: // -if
					buf.append(L((skipFirstWord?"n":"Requires n")+"ot meeting the following condition(s):"));
					for(int v2=v+1;v2<V.size();v2++)
					{
						final String str2=V.get(v2);
						if(zapCodes.containsKey(str2))
							break;
						buf.append(str2).append(" ");
					}
					break;
				case IF: // +if
					buf.append(L((skipFirstWord?"m":"Requires m")+"meets the following condition(s):"));
					for(int v2=v+1;v2<V.size();v2++)
					{
						final String str2=V.get(v2);
						if(zapCodes.containsKey(str2))
							break;
						buf.append(str2).append(" ");
					}
					break;
				case SUBOP:
					// this line intentionally left blank
					break;
				case SYSOP:
					// this line intentionally left blank
					break;
				case _SUBOP:
					// this line intentionally left blank
					break;
				case _SYSOP:
					// this line intentionally left blank
					break;
				case LVLEQ:
					// this line intentionally left blank
					break;
				case LVLGE:
					// this line intentionally left blank
					break;
				case LVLGR:
					// this line intentionally left blank
					break;
				case LVLLE:
					// this line intentionally left blank
					break;
				case LVLLT:
					// this line intentionally left blank
					break;
				}
			}
			else
			{
				for(final SavedClass C : charClasses())
				{
					if(str.startsWith("-"+C.nameStart))
						buf.append(L("Disallows @x1.  ",C.name));
				}
				final LinkedList<String> cats=new LinkedList<String>();
				for(final SavedRace R : races())
				{
					if((str.startsWith(R.minusCatNameStart))&&(!cats.contains(R.racialCategory)))
					{
						cats.add(R.racialCategory);
						buf.append(L("Disallows @x1.  ",R.racialCategory));
					}
				}
				if(str.startsWith("-"+Faction.Align.EVIL.toString().substring(0,3)))
					buf.append(L("Disallows "+Faction.Align.EVIL.toString().toLowerCase()+".  "));
				if(str.startsWith("-"+Faction.Align.GOOD.toString().substring(0,3)))
					buf.append(L("Disallows "+Faction.Align.GOOD.toString().toLowerCase()+".  "));
				if(str.startsWith("-"+Faction.Align.NEUTRAL.toString().substring(0,3)))
					buf.append(L("Disallows "+Faction.Align.NEUTRAL.toString().toLowerCase()+".  "));
				if(str.startsWith("-MALE"))
					buf.append(L("Disallows Males.  "));
				if(str.startsWith("-FEMALE"))
					buf.append(L("Disallows Females.  "));
				if(str.startsWith("-NEUTER"))
					buf.append(L((skipFirstWord?"Only ":"Allows only ")+"Males and Females.  "));
				buf.append(levelHelp(str,'-',L("Disallows ")));
				if(str.startsWith("-"))
				{
					final Faction.FRange FR=getRange(str.substring(1));
					final String desc=CMLib.factions().rangeDescription(FR,"and ");
					if(desc.length()>0)
						buf.append(L("Disallows ")+desc);
				}
			}
		}

		if(buf.length()==0)
			buf.append(L("Anyone."));
		return buf.toString();
	}

	@Override
	public boolean syntaxCheck(final String mask, final List<String> errorSink)
	{
		if(mask.trim().length()==0)
			return true;
		final List<String> V=CMParms.parse(mask.toUpperCase());
		for(int v=0;v<V.size();v++)
		{
			final String str=V.get(v);
			final Map<String,ZapperKey> zapCodes=getMaskCodes();
			if(zapCodes.containsKey(str))
				return true;
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

	@Override
	public List<String> getAbilityEduReqs(final String text)
	{
		final Vector<String> preReqs=new Vector<String>();
		if(text.trim().length()==0)
			return preReqs;
		final Map<String,ZapperKey> zapCodes=getMaskCodes();
		final List<String> V=CMParms.parse(text.toUpperCase());
		String str2;
		for(int v=0;v<V.size();v++)
		{
			final String str=V.get(v);
			if(zapCodes.containsKey(str))
			{
				switch(zapCodes.get(str))
				{
				case _MAXCLASSLEVEL: // max class level...
					//TODO:?!
					break;
				case _BASECLASS: // huh?
					//TODO:?!
					break;
				case JAVACLASS: // +JAVACLASS
					for(int v2=v+1;v2<V.size();v2++)
					{
						str2=V.get(v2);
						if(zapCodes.containsKey(str2))
							break;
						if(str2.startsWith("-"))
						{
							str2=str2.substring(1);
							final int x=str2.indexOf('(');
							if(x>0)
								str2=str2.substring(0,x);
							final Ability A=CMClass.getAbilityPrototype(str2);
							if(A!=null)
								preReqs.remove(A.ID());
						}
					}
					break;
				case _JAVACLASS: // -JAVACLASS
					for(int v2=v+1;v2<V.size();v2++)
					{
						str2=V.get(v2);
						if(zapCodes.containsKey(str2))
							break;
						if(str2.startsWith("+"))
						{
							str2=str2.substring(1);
							final int x=str2.indexOf('(');
							if(x>0)
								str2=str2.substring(0,x);
							final Ability A=CMClass.getAbilityPrototype(str2);
							if((A!=null)&&(!preReqs.contains(A.ID())))
								preReqs.addElement(A.ID());
						}
					}
					break;
				case _EXPERTISE: // -expertises
					{
						for(int v2=v+1;v2<V.size();v2++)
						{
							str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								final ExpertiseLibrary.ExpertiseDefinition E=CMLib.expertises().getDefinition(str2.substring(1).toUpperCase().trim());
								if(E!=null)
									preReqs.addElement(E.ID());
							}
						}
					}
					break;
				case _SKILL: // -skills
					{
						for(int v2=v+1;v2<V.size();v2++)
						{
							str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								str2=str2.substring(1);
								final int x=str2.indexOf('(');
								if(x>0)
									str2=str2.substring(0,x);
								final Ability A=CMClass.getAbilityPrototype(str2);
								if((A!=null)&&(!preReqs.contains(A.ID())))
									preReqs.addElement(A.ID());
							}
						}
					}
					break;
				case _SKILLFLAG: // -skillflag
					{
						final ArrayList<Object> objs=new ArrayList<Object>();
						Object o=null;
						for(int v2=v+1;v2<V.size();v2++)
						{
							str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								str2=str2.substring(1);
								o=this.makeSkillFlagObject(str2);
								if(o!=null)
									objs.add(o);
							}
						}
						for(final Object O : objs)
						{
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
					}
					break;
				default:
					break;
				}
			}
		}
		return preReqs;
	}

	@Override
	public int minMaskLevel(final String text, final int minMinLevel)
	{
		int level=minMinLevel;
		final CompiledZMask cset=getPreCompiledMask(text);
		for(final CompiledZMaskEntry entry : cset.entries())
		{
			switch(entry.maskType())
			{
			case _LEVEL: // -level
				{
					for(int v=0;v<entry.parms().length-1;v+=2)
					{
						switch((ZapperKey)entry.parms()[v])
						{
						case LVLGR: // +lvlgr
							level=((Integer)entry.parms()[v+1]).intValue()+1;
							break;
						case LVLGE: // +lvlge
							level=((Integer)entry.parms()[v+1]).intValue();
							break;
						case LVLLT: // +lvlt
							level=minMinLevel;
							break;
						case LVLLE: // +lvlle
							level=minMinLevel;
							break;
						case LVLEQ: // +lvleq
							level=((Integer)entry.parms()[v+1]).intValue();
							break;
						default:
							break;
						}
					}
				}
				break;
			case _CLASSLEVEL: // -classlevel
				{
					for(int v=0;v<entry.parms().length-1;v+=2)
					{
						switch((ZapperKey)entry.parms()[v])
						{
						case LVLGR: // +lvlgr
							level=((Integer)entry.parms()[v+1]).intValue()+1;
							break;
						case LVLGE: // +lvlge
							level=((Integer)entry.parms()[v+1]).intValue();
							break;
						case LVLLT: // +lvlt
							level=minMinLevel;
							break;
						case LVLLE: // +lvlle
							level=minMinLevel;
							break;
						case LVLEQ: // +lvleq
							level=((Integer)entry.parms()[v+1]).intValue();
							break;
						default:
							break;
						}
					}
				}
				break;
			case _MAXCLASSLEVEL: // -maxclasslevel
				{
					for(int v=0;v<entry.parms().length-1;v+=2)
					{
						switch((ZapperKey)entry.parms()[v])
						{
						case LVLGR: // +lvlgr
							level=((Integer)entry.parms()[v+1]).intValue()+1;
							break;
						case LVLGE: // +lvlge
							level=((Integer)entry.parms()[v+1]).intValue();
							break;
						case LVLLT: // +lvlt
							level=minMinLevel;
							break;
						case LVLLE: // +lvlle
							level=minMinLevel;
							break;
						case LVLEQ: // +lvleq
							level=((Integer)entry.parms()[v+1]).intValue();
							break;
						default:
							break;
						}
					}
				}
				break;
			case LVLGR: // +lvlgr
				level=minMinLevel;
				break;
			case LVLGE: // +lvlge
				level=minMinLevel;
				break;
			case LVLLT: // +lvlt
				level=((Integer)entry.parms()[0]).intValue();
				break;
			case LVLLE: // +lvlle
				level=((Integer)entry.parms()[0]).intValue()+1;
				break;
			case LVLEQ: // +lvleq
				level=minMinLevel;
				break;
			default:
				break;
			}
		}
		return level;
	}

	@Override
	public CompiledZMask maskCompile(final String text)
	{
		final ArrayList<CompiledZMaskEntry> buf=new ArrayList<CompiledZMaskEntry>();
		if((text==null)||(text.trim().length()==0)) 
			return new CompiledZapperMaskImpl(new boolean[]{false,false},buf.toArray(new CompiledZMaskEntry[0]));
		final Map<String,ZapperKey> zapCodes=getMaskCodes();
		final List<String> V=CMParms.parse(text.toUpperCase());
		List<String> lV=null;
		boolean buildItemFlag=false;
		boolean buildRoomFlag=false;
		ZapperKey entryType;
		for(int v=0;v<V.size();v++)
		{
			final String str=V.get(v);
			int val=-1;
			entryType=zapCodes.get(str);
			if(entryType!=null)
			{
				switch(entryType)
				{
				case _CLASS: // -class
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(final SavedClass C : charClasses())
						{
							if(fromHereStartsWith(V,'+',v+1,C.nameStart))
								parms.add(C.name);
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case CLASS: // +class
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(final SavedClass C : charClasses())
						{
							if(fromHereStartsWith(V,'-',v+1,C.nameStart))
								parms.add(C.name);
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case CLASSTYPE: // +classtype
				case _CLASSTYPE: // -classtype
					{
						final String swc = (entryType == ZapperKey.CLASSTYPE) ? "-" : "+";
						final ArrayList<Object> parms=new ArrayList<Object>();
						if(lV==null)
							lV=CMParms.parse(text);
						for(int v2=v+1;v2<lV.size();v2++)
						{
							final String str2=lV.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if(str2.startsWith(swc))
							{
								final String possClassName = str2.substring(1);
								String ancestorStr = CMClass.findTypeAncestor(possClassName);
								Class<?> ancestorC = null;
								if((ancestorStr != null)&&(ancestorStr.length()>0))
								{
									try
									{
										ancestorC = Class.forName(ancestorStr);
									}
									catch(Exception e)
									{
									}
								}
								if(ancestorC == null)
								{
									try
									{
										ancestorC = Class.forName(possClassName);
									}
									catch(Exception e)
									{
									}
									if((ancestorC == null)&&(possClassName.indexOf('.')<0))
									{
										final String[] prefixes = 
										{
											"com.planet_ink.coffee_mud.core.interfaces.",
											"com.planet_ink.coffee_mud.MOBS.interfaces.",
											"com.planet_ink.coffee_mud.MOBS.",
											"com.planet_ink.coffee_mud.Items.interfaces.",
											"com.planet_ink.coffee_mud.Items.",
											"com.planet_ink.coffee_mud.Items.Armor.",
											"com.planet_ink.coffee_mud.Items.Basic.",
											"com.planet_ink.coffee_mud.Items.BasicTech.",
											"com.planet_ink.coffee_mud.Items.CompTech.",
											"com.planet_ink.coffee_mud.Items.MiscMagic.",
											"com.planet_ink.coffee_mud.Items.Software.",
											"com.planet_ink.coffee_mud.Items.Weapons.",
											"com.planet_ink.coffee_mud.Locales.interfaces.",
											"com.planet_ink.coffee_mud.Locales.",
											"com.planet_ink.coffee_mud.Exits.interfaces.",
											"com.planet_ink.coffee_mud.Exits.",
											"com.planet_ink.coffee_mud.Areas.interfaces.",
											"com.planet_ink.coffee_mud.Areas.",
											"com.planet_ink.coffee_mud.Abilities.interfaces.",
											"com.planet_ink.coffee_mud.Behaviors.interfaces.",
											"com.planet_ink.coffee_mud.CharClasses.interfaces.",
											"com.planet_ink.coffee_mud.Races.interfaces.",
											"com.planet_ink.coffee_mud.Commands.interfaces.",
											"com.planet_ink.coffee_mud.Libraries.interfaces.",
										};
										for(String prefix : prefixes)
										{
											try
											{
												ancestorC = Class.forName(prefix+possClassName);
												break;
											}
											catch(Throwable e2)
											{
											}
										}
										if(ancestorC != null)
										{
											parms.add(ancestorC);
										}
									}
								}
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _BASECLASS: // -baseclass
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						final HashSet<String> seenBase=new HashSet<String>();
						for(final SavedClass C : charClasses())
						{
							if(!seenBase.contains(C.baseClass))
							{
								seenBase.add(C.baseClass);
								if(fromHereStartsWith(V,'+',v+1,C.baseClassStart))
									parms.add(C.baseClass);
							}
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case BASECLASS: // +baseclass
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						final HashSet<String> seenBase=new HashSet<String>();
						for(final SavedClass C : charClasses())
						{
							if(!seenBase.contains(C.baseClass))
							{
								seenBase.add(C.baseClass);
								if(fromHereStartsWith(V,'-',v+1,C.baseClassStart))
									parms.add(C.baseClass);
							}
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case WEAPONTYPE: // +weapontype
				case WEAPONCLASS: // +weaponclass
				case _WEAPONTYPE: // -weapontype
				case _WEAPONCLASS: // -weaponclass
					{
						final String cw=((entryType == ZapperKey._WEAPONCLASS)
								||(entryType == ZapperKey._WEAPONTYPE)) ? "+":"-";
						final String[] arr = ((entryType == ZapperKey._WEAPONCLASS)
								||(entryType == ZapperKey.WEAPONCLASS)) ? Weapon.CLASS_DESCS : Weapon.TYPE_DESCS;
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if(str2.startsWith(cw))
							{
								str2=str2.substring(1).toUpperCase().trim();
								int x=CMParms.indexOf(arr,str2);
								if(x >= 0)
									parms.add(Integer.valueOf(x));
								else
								{
									v=v2-1;
									break;
								}
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _RACE: // -Race
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						final LinkedList<String> cats=new LinkedList<String>();
						for(final SavedRace R : races())
						{
							if((!cats.contains(R.name)
							&&(fromHereStartsWith(V,'+',v+1,R.nameStart))))
								cats.add(R.name);
						}
						for(final String s : cats)
							parms.add(s);
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _RACECAT: // -Racecats
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						final LinkedList<String> cats=new LinkedList<String>();
						for(final SavedRace R : races())
						{
							if((!cats.contains(R.racialCategory)
							&&(fromHereStartsWith(V,'+',v+1,R.upperCatName))))
								cats.add(R.racialCategory);
						}
						for(final String s : cats)
							parms.add(s);
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case RACECAT: // +Racecats
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						final LinkedList<String> cats=new LinkedList<String>();
						for(final SavedRace R : races())
						{
							if((!cats.contains(R.racialCategory)
							&&(fromHereStartsWith(V,'-',v+1,R.upperCatName))))
								cats.add(R.racialCategory);
						}
						for(final String s : cats)
							parms.add(s);
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case RACE: // +Race
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(final SavedRace R : races())
						{
							if(fromHereStartsWith(V,'-',v+1,R.upperName))
								parms.add(R.name);
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case ALIGNMENT: // +Alignment
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						if(fromHereStartsWith(V,'-',v+1,Faction.Align.EVIL.toString().substring(0,3)))
							parms.add(Faction.Align.EVIL.toString());
						if(fromHereStartsWith(V,'-',v+1,Faction.Align.GOOD.toString().substring(0,3)))
							parms.add(Faction.Align.GOOD.toString());
						if(fromHereStartsWith(V,'-',v+1,Faction.Align.NEUTRAL.toString().substring(0,3)))
							parms.add(Faction.Align.NEUTRAL.toString());
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _ALIGNMENT: // -Alignment
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						if(fromHereStartsWith(V,'+',v+1,Faction.Align.EVIL.toString().substring(0,3)))
							parms.add(Faction.Align.EVIL.toString());
						if(fromHereStartsWith(V,'+',v+1,Faction.Align.GOOD.toString().substring(0,3)))
							parms.add(Faction.Align.GOOD.toString());
						if(fromHereStartsWith(V,'+',v+1,Faction.Align.NEUTRAL.toString().substring(0,3)))
							parms.add(Faction.Align.NEUTRAL.toString());
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _GENDER: // -Gender
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						if(fromHereStartsWith(V,'+',v+1,"MALE"))
							parms.add("M");
						if(fromHereStartsWith(V,'+',v+1,"FEMALE"))
							parms.add("F");
						if(fromHereStartsWith(V,'+',v+1,"NEUTER"))
							parms.add("N");
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case GENDER: // +Gender
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						if(fromHereStartsWith(V,'-',v+1,"MALE"))
							parms.add("M");
						if(fromHereStartsWith(V,'-',v+1,"FEMALE"))
							parms.add("F");
						if(fromHereStartsWith(V,'-',v+1,"NEUTER"))
							parms.add("N");
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _LEVEL: // -Levels
				case _CLASSLEVEL: // -ClassLevels
				case _MAXCLASSLEVEL: // -MaxclassLevels
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							final CompiledZMaskEntry e = levelCompiledHelper(str2,'+');
							if(e!=null)
							{
								parms.add(e.maskType());
								parms.add(e.parms()[0]);
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case ANYCLASSLEVEL: // +anyclasslevel
				case _ANYCLASSLEVEL: // -anyclasslevel
					{
						final char plusMinus = (entryType == ZapperKey.ANYCLASSLEVEL) ? '-' : '+';
						SavedClass charClassC = null;
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							boolean checkForClass = true;
							boolean found=false;
							if(charClassC!=null)
							{
								final CompiledZMaskEntry e = levelCompiledHelper(str2,plusMinus);
								if(e!=null)
								{
									CharClass C=CMClass.getCharClass(charClassC.id);
									if(C!=null)
									{
										parms.add(C);
										parms.add(e.maskType());
										parms.add(e.parms()[0]);
										checkForClass = false;
										found=true;
									}
								}
							}
							if(checkForClass)
							{
								for(final SavedClass C : charClasses())
								{
									if(str2.startsWith(plusMinus+C.nameStart))
									{
										charClassC = C;
										found=true;
										break;
									}
								}
							}
							if(!found)
							{
								v=v2-1;
								break;
							}
							else
							if(v2==V.size()-1)
							{
								v=v2;
								break;
							}
						}
						if(parms.size()>0)
							buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _EFFECT: // -Effect
				case EFFECT: // +Effect
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=V.get(v2);
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
								if(A==null)
									A=CMClass.getBehavior(str2);
								if(A==null)
									A=CMClass.getAbilityByName(str2,true);
								if(A==null)
									A=CMClass.getBehaviorByName(str2,true);
								if(A==null)
									A=CMClass.getAbilityByName(str2,false);
								if(A==null)
									A=CMClass.getBehaviorByName(str2,false);
								if(A!=null)
									parms.add(A.ID());
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case SECURITY: // +security
				case _SECURITY: // -security
					{
						final String plusMinus=(entryType==ZapperKey._SECURITY)?"+":"-";
						final ArrayList<CMSecurity.SecFlag> parms=new ArrayList<CMSecurity.SecFlag>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if(str2.startsWith(plusMinus))
							{
								final CMSecurity.SecFlag flag=(CMSecurity.SecFlag)CMath.s_valueOf(CMSecurity.SecFlag.class,str2.substring(1).toUpperCase().trim().replace(' ','_'));
								if(flag == null)
									Log.errOut("MUDZapper","Illegal security flag '"+str2);
								else
									parms.add(flag);
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new CMSecurity.SecFlag[0])));
						break;
					}
				case _CLAN: // -Clan
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if(str2.startsWith("+"))
							{
								int x;
								if(((x=str2.lastIndexOf('('))>0)&&(str2.endsWith(")")))
									parms.add(new Pair<String,String>(str2.substring(1,x),str2.substring(x+1,str2.length()-1)));
								else
									parms.add(str2.substring(1));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _SUBNAME:
				case _WEAPONAMMO:
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if(str2.startsWith("+"))
								parms.add(str2.substring(1).toLowerCase());
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _AREA: // -Area
					buildRoomFlag=true;
				//$FALL-THROUGH$
				case _TATTOO: // -Tattoos
				case _MOOD: // -Mood
				case _ACCCHIEVE: // -Accchieves
				case _EXPERTISE: // -expertise
				case _DEITY: // -Deity
				case _NAME: // -Names
				case _ACCOUNT: // -Accounts
				case _QUESTWIN: // -Questwin
				case _HOME: // -Home
				case _JAVACLASS: // -JavaClass
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if(str2.startsWith("+"))
								parms.add(str2.substring(1));
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case FACTION: // +Faction
				case _FACTION: // -Faction
				{
					final String plusMinus=(entryType==ZapperKey._FACTION)?"+":"-";
					final ArrayList<Object> parms=new ArrayList<Object>();
					for(int v2=v+1;v2<V.size();v2++)
					{
						final String str2=V.get(v2);
						if(zapCodes.containsKey(str2))
						{
							v=v2-1;
							break;
						}
						else
						if(str2.startsWith(plusMinus))
						{
							final String str3=str2.substring(1).toUpperCase().trim();
							final Faction.FRange FR=getRange(str3);
							if(FR!=null)
								parms.add(str3);
						}
						v=V.size();
					}
					buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					break;
				}
				case CLAN: // +Clan
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if(str2.startsWith("-"))
							{
								int x;
								if(((x=str2.indexOf('('))>0)&&(str2.endsWith(")")))
									parms.add(new Pair<String,String>(str2.substring(1,x),str2.substring(x+1,str2.length()-1)));
								else
									parms.add(str2.substring(1));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case SUBNAME:
				case WEAPONAMMO:
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if(str2.startsWith("-"))
								parms.add(str2.substring(1).toLowerCase());
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case AREA: // +Area
					buildRoomFlag=true;
				//$FALL-THROUGH$
				case TATTOO: // +Tattoos
				case MOOD: // +Mood
				case ACCCHIEVE: // +Accchieves
				case EXPERTISE: // +expertise
				case DEITY: // +Deity
				case NAME: // +Names
				case ACCOUNT: // +Account
				case QUESTWIN: // +Questwin
				case HOME: // +Home
				case JAVACLASS: // +JavaClass
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if(str2.startsWith("-"))
								parms.add(str2.substring(1));
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _SKILL: // +skills
				case SKILL: // -skills
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=V.get(v2);
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
								final int x=str2.indexOf('(');
								if(x>0)
								{
									if(str2.endsWith(")"))
										prof=CMath.s_int(str2.substring(x+1,str2.length()-1));
									str2=str2.substring(0,x);
								}
								final Ability A=CMClass.getAbility(str2);
								if(A!=null)
								{
									parms.add(A.ID());
									parms.add(Integer.valueOf(prof));
								}
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _SKILLFLAG: // -skillflag
				case SKILLFLAG: // +skillflag
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							String str2=V.get(v2);
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
								if(o!=null)
									parms.add(o);
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case ITEM: // +Item
				case WORN: // +Worn
				case _ITEM: // -Item
				case _WORN: // -Worn
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("+"))||(str2.startsWith("-")))
								parms.add(str2.substring(1));
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case MATERIAL: // +Material
				case _MATERIAL: // -Material
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						buildItemFlag=true;
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
									parms.add(RawMaterial.Material.findByMask(code&RawMaterial.MATERIAL_MASK).desc());
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case WORNON: // -WornOn
				case _WORNON: // +WornOn
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						buildItemFlag=true;
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
							{
								final long code=Wearable.CODES.FIND_endsWith(str2.substring(1));
								if(code>=0)
									parms.add(Long.valueOf(code));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case DISPOSITION: // +Disposition
				case _DISPOSITION: // -Disposition
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
							{
								final int code=CMLib.flags().getDispositionIndex(str2.substring(1));
								if(code>=0)
									parms.add(Integer.valueOf((int)CMath.pow(2,code)));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case SENSES: // +Senses
				case _SENSES: // -Senses
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
							{
								final int code=CMLib.flags().getSensesIndex(str2.substring(1));
								if(code>=0)
									parms.add(Integer.valueOf((int)CMath.pow(2,code)));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case SEASON: // +Season
				case _SEASON: // -Season
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						buildRoomFlag=true;
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
							{
								if(CMath.isInteger(str2.substring(1).trim()))
									parms.add(Integer.valueOf(CMath.s_int(str2.substring(1).trim())));
								else
								{
									final int seasonCode=determineSeasonCode(str2.substring(1).trim());
									if(seasonCode>=0)
										parms.add(Integer.valueOf(seasonCode));
								}
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case WEATHER: // +weather
				case _WEATHER: // -weather
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						buildRoomFlag=true;
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
							{
								if(CMath.isInteger(str2.substring(1).trim()))
									parms.add(Integer.valueOf(CMath.s_int(str2.substring(1).trim())));
								else
								if(CMParms.indexOf(Climate.WEATHER_DESCS,str2.substring(1).trim())>=0)
									parms.add(Integer.valueOf(CMParms.indexOf(Climate.WEATHER_DESCS,str2.substring(1).trim())));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case HOUR: // +HOUR
				case _HOUR: // -HOUR
				case MONTH: // +MONTH
				case _MONTH: // -MONTH
				case DAY: // +DAY
				case _DAY: // -DAY
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						buildRoomFlag=true;
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
								parms.add(Integer.valueOf(CMath.s_int(str2.substring(1).trim())));
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case QUALLVL: // +quallvl
				case _QUALLVL: // -quallvl
					if((v+1)<V.size())
					{
						final Ability A=CMClass.getAbility(V.get(v+1));
						if(A!=null)
						{
							int adjustment=0;
							if(((v+2)<V.size())&&(CMath.isInteger(V.get(v+2))))
								adjustment=CMath.s_int(V.get(v+2));
							final ArrayList<Object> parms=new ArrayList<Object>();
							parms.add(A.ID());
							parms.add(Integer.valueOf(adjustment));
							buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
						}
					}
					break;
				case SUBOP: // +subop
				case _SUBOP: // +subop
					buildRoomFlag=true;
				//$FALL-THROUGH$
				case ISHOME: // +ishome
				case _ISHOME: // -ishome
				case SYSOP: // +sysop
				case _SYSOP: // +sysop
				{
					buf.add(new CompiledZapperMaskEntryImpl(entryType,new Object[0]));
					break;
				}
				case _IFSTAT:
				case IFSTAT:
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						buildItemFlag=true;
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							else
							if((str2.startsWith("-"))||(str2.startsWith("+")))
							{
								int x=str2.indexOf('=');
								if(x>0)
								{
									parms.add(str2.toUpperCase().substring(1, x));
									parms.add(str2.substring(x+1));
								}
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case RESOURCE: // +Resource
				case _RESOURCE: // -Resource
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						buildItemFlag=true;
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
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
									parms.add(RawMaterial.CODES.NAME(code));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _PLAYER: // -Player
				case _NPC: // -MOB
					{
						buf.add(new CompiledZapperMaskEntryImpl(entryType,new Object[0]));
						break;
					}
				case _ANYCLASS: // -anyclass
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(final SavedClass C : charClasses())
						{
							if(fromHereStartsWith(V,'+',v+1,C.nameStart))
								parms.add(C.name);
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case ANYCLASS: // +anyclass
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(final SavedClass C : charClasses())
						{
							if(fromHereStartsWith(V,'-',v+1,C.nameStart))
								parms.add(C.name);
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case VALUE: // +value
				case _VALUE: // -value
					buildItemFlag=true;
				//$FALL-THROUGH$
				case ADJSTRENGTH: // +adjstr
				case ADJINTELLIGENCE: // +adjint
				case ADJWISDOM: // +adjwis
				case ADJDEXTERITY: // +adjdex
				case ADJCONSTITUTION: // -adjcha
				case ADJCHARISMA: // +adjcha
				case _ADJSTRENGTH: // -adjstr
				case _ADJINTELLIGENCE: // -adjint
				case _ADJWISDOM: // -adjwis
				case _ADJDEXTERITY: // -adjdex
				case _ADJCONSTITUTION: // -adjcon
				case _ADJCHARISMA: // -adjcha
				case STRENGTH: // +str
				case INTELLIGENCE: // +int
				case WISDOM: // +wis
				case DEXTERITY: // +dex
				case CONSTITUTION: // +con
				case CHARISMA: // +cha
				case _STRENGTH: // -str
				case _INTELLIGENCE: // -int
				case _WISDOM: // -wis
				case _DEXTERITY: // -dex
				case _CONSTITUTION: // -con
				case _CHARISMA: // -cha
				case _CHANCE: // -chance
				case ABILITY: // +ability
				case _ABILITY: // -ability
				case WEIGHT: // +weight
				case _WEIGHT: // -weight
				case ARMOR: // +armor
				case _ARMOR: // -armor
				case DAMAGE: // +damage
				case _DAMAGE: // -damage
				case ATTACK: // +attack
				case _ATTACK: // -attack
				case _GROUPSIZE: // -groupsize
				case GROUPSIZE: // +groupsize
					{
						val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
						final ArrayList<Object> parms=new ArrayList<Object>();
						parms.add(Integer.valueOf(val));
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
						break;
					}
				case _IF: // -if
				case IF: // +if
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2)||(str2.startsWith("+"))||(str2.startsWith("-")))
							{
								v=v2-1;
								break;
							}
							else
							{
								final ScriptingEngine SE = (ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
								SE.setSavable(false);
								SE.setVarScope("*");
								try
								{
									final String[] tt = SE.parseEval(str2);
									parms.add(SE);
									final String[][] EVAL={tt};
									parms.add(EVAL); // the compiled eval
									final Object[] tmp = new Object[ScriptingEngine.SPECIAL_NUM_OBJECTS];
									parms.add(tmp);
								}
								catch(final ScriptParseException spe)
								{
									Log.errOut("MUDZapper","Script parse Exception for "+str2);
									Log.errOut("MUDZapper",spe);
								}
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
						break;
					}
				case LVLEQ:
					// intentially left blank
					break;
				case LVLGE:
					// intentially left blank
					break;
				case LVLGR:
					// intentially left blank
					break;
				case LVLLE:
					// intentially left blank
					break;
				case LVLLT:
					// intentially left blank
					break;
				}
			}
			else
			{
				boolean found=false;
				if(!found)
				{
					for(final SavedClass C : charClasses())
					{
						if(str.equals("-"+C.upperName))
						{
							final ArrayList<Object> parms=new ArrayList<Object>();
							entryType=ZapperKey.CLASS;
							parms.add(C.name);
							found=true;
							buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
							break;
						}
					}
				}
				if(!found)
				{
					for(final SavedRace R : races())
					{
						if(str.equals("-"+R.upperName))
						{
							final ArrayList<Object> parms=new ArrayList<Object>();
							entryType=ZapperKey.RACE;
							parms.add(R.name);
							found=true;
							buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
							break;
						}
					}
				}
				if((!found)
				&&(str.equals("-"+Faction.Align.EVIL.toString())))
				{
					final ArrayList<Object> parms=new ArrayList<Object>();
					entryType=ZapperKey.ALIGNMENT;
					parms.add(Faction.Align.EVIL.toString());
					found=true;
					buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.equals("-"+Faction.Align.GOOD.toString())))
				{
					final ArrayList<Object> parms=new ArrayList<Object>();
					entryType=ZapperKey.ALIGNMENT;
					parms.add(Faction.Align.GOOD.toString());
					found=true;
					buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.equals("-"+Faction.Align.NEUTRAL.toString())))
				{
					final ArrayList<Object> parms=new ArrayList<Object>();
					entryType=ZapperKey.ALIGNMENT;
					parms.add(Faction.Align.NEUTRAL.toString());
					found=true;
					buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
				}
				if((!found)&&(str.equals("-MALE")))
				{
					final ArrayList<Object> parms=new ArrayList<Object>();
					entryType=ZapperKey.GENDER;
					parms.add("M");
					found=true;
					buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
				}
				if((!found)&&(str.equals("-FEMALE")))
				{
					final ArrayList<Object> parms=new ArrayList<Object>();
					entryType=ZapperKey.GENDER;
					parms.add("F");
					found=true;
					buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
				}
				if((!found)&&(str.equals("-NEUTER")))
				{
					final ArrayList<Object> parms=new ArrayList<Object>();
					entryType=ZapperKey.GENDER;
					parms.add("N");
					found=true;
					buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.startsWith("-"))
				&&(CMLib.factions().isRangeCodeName(str.substring(1))))
				{
					final ArrayList<Object> parms=new ArrayList<Object>();
					entryType=ZapperKey.FACTION;
					parms.add(str.substring(1));
					found=true;
					buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
				}
				if(!found)
				{
					for(final SavedClass C : charClasses())
					{
						if(str.startsWith(C.minusNameStart))
						{
							final ArrayList<Object> parms=new ArrayList<Object>();
							entryType=ZapperKey.CLASS;
							parms.add(C.name);
							found=true;
							buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
							break;
						}
					}
				}
				if(!found)
				{
					for(final SavedRace R : races())
					{
						if(str.startsWith(R.minusNameStart))
						{
							final ArrayList<Object> parms=new ArrayList<Object>();
							entryType=ZapperKey.RACE;
							parms.add(R.name);
							found=true;
							buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
							break;
						}
					}
				}
				if(!found)
				{
					for(final SavedRace R : races())
					{
						if(str.startsWith(R.minusCatNameStart))
						{
							final ArrayList<Object> parms=new ArrayList<Object>();
							entryType=ZapperKey.RACECAT;
							parms.add(R.racialCategory);
							buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
						}
					}
				}
				if((!found)
				&&(str.startsWith("-"+Faction.Align.EVIL.toString().substring(0,3))))
				{
					final ArrayList<Object> parms=new ArrayList<Object>();
					entryType=ZapperKey.ALIGNMENT;
					parms.add(Faction.Align.EVIL.toString());
					found=true;
					buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.startsWith("-"+Faction.Align.GOOD.toString().substring(0,3))))
				{
					final ArrayList<Object> parms=new ArrayList<Object>();
					entryType=ZapperKey.ALIGNMENT;
					parms.add(Faction.Align.GOOD.toString());
					found=true;
					buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.startsWith("-"+Faction.Align.NEUTRAL.toString().substring(0,3))))
				{
					final ArrayList<Object> parms=new ArrayList<Object>();
					entryType=ZapperKey.ALIGNMENT;
					parms.add(Faction.Align.NEUTRAL.toString());
					found=true;
					buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.startsWith("-MALE")))
				{
					final ArrayList<Object> parms=new ArrayList<Object>();
					entryType=ZapperKey.GENDER;
					parms.add("M");
					found=true;
					buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.startsWith("-FEMALE")))
				{
					final ArrayList<Object> parms=new ArrayList<Object>();
					entryType=ZapperKey.GENDER;
					parms.add("F");
					found=true;
					buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.startsWith("-NEUTER")))
				{
					final ArrayList<Object> parms=new ArrayList<Object>();
					entryType=ZapperKey.GENDER;
					parms.add("N");
					found=true;
					buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
				}
				if((!found)
				&&(str.startsWith("-"))
				&&(CMLib.factions().isRangeCodeName(str.substring(1))))
				{
					final ArrayList<Object> parms=new ArrayList<Object>();
					entryType=ZapperKey.FACTION;
					parms.add(str.substring(1));
					found=true;
					buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
				}
				if(!found)
				{
					final CompiledZMaskEntry entry=levelCompiledHelper(str,'-');
					if(entry!=null)
						buf.add(entry);
				}
			}
		}
		return new CompiledZapperMaskImpl(new boolean[]{buildItemFlag,buildRoomFlag},buf.toArray(new CompiledZMaskEntry[0]));
	}

	protected Room outdoorRoom(Area A)
	{
		Room R=null;
		for(final Enumeration<Room> e=A.getMetroMap();e.hasMoreElements();)
		{
			R=e.nextElement();
			if((R.domainType()&Room.INDOORS)==0)
				return R;
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

	@Override
	public boolean maskCheck(final String text, final Environmental E, final boolean actual)
	{
		return maskCheck(getPreCompiledMask(text), E, actual);
	}

	@SuppressWarnings("unchecked")

	@Override
	public boolean maskCheck(final CompiledZMask cset, final Environmental E, final boolean actual)
	{
		if(E==null)
			return true;
		if((cset==null)||(cset.entries().length<1))
			return true;
		getMaskCodes();
		CharStats base=null;
		final MOB mob=(E instanceof MOB)?(MOB)E:nonCrashingMOB();
		final boolean[] flags=cset.flags();
		final Item item=flags[0]?((E instanceof Item)?(Item)E:nonCrashingItem(mob)):null;
		final Room room = flags[1]?((E instanceof Area)?outdoorRoom((Area)E):CMLib.map().roomLocation(E)):null;
		final Physical P = (E instanceof Physical)?(Physical)E:null;
		if((mob==null)||(flags[0]&&(item==null)))
			return false;
		for(final CompiledZMaskEntry entry : cset.entries())
		{
			try
			{
				switch(entry.maskType())
				{
				case SYSOP: // +sysop
					if(CMSecurity.isASysOp(mob))
						return true;
					break;
				case _SYSOP: // -sysop
					if(CMSecurity.isASysOp(mob))
						return false;
					break;
				case SUBOP: // +subop
					if(CMSecurity.isASysOp(mob)
					||((room!=null)&&(room.getArea().amISubOp(mob.Name()))))
						return true;
					break;
				case _SUBOP: // -subop
					if(CMSecurity.isASysOp(mob)
					||((room!=null)&&(room.getArea().amISubOp(mob.Name()))))
						return false;
					break;
				case CLASS: // +class
					if(CMParms.contains(entry.parms(),actual?mob.baseCharStats().getCurrentClass().name():mob.charStats().displayClassName()))
						return false;
					break;
				case _CLASS: // -class
				{
					if(!CMParms.contains(entry.parms(),actual?mob.baseCharStats().getCurrentClass().name():mob.charStats().displayClassName()))
						return false;
					break;
				}
				case _BASECLASS: // -baseclass
				{
					String baseClass=mob.baseCharStats().getCurrentClass().baseClass();
					if((!actual)&&(!baseClass.equals(mob.charStats().displayClassName())))
					{
						final CharClass C=CMClass.getCharClass(mob.charStats().displayClassName());
						if(C!=null)
							baseClass=C.baseClass();
					}
					if(!CMParms.contains(entry.parms(),baseClass))
						return false;
					break;
				}
				case BASECLASS: // +baseclass
				{
					String baseClass=mob.baseCharStats().getCurrentClass().baseClass();
					if((!actual)&&(!baseClass.equals(mob.charStats().displayClassName())))
					{
						final CharClass C=CMClass.getCharClass(mob.charStats().displayClassName());
						if(C!=null)
							baseClass=C.baseClass();
					}
					if(CMParms.contains(entry.parms(),baseClass))
						return false;
					break;
				}
				case _CLASSTYPE: // -classtype
				{
					boolean found=false;
					final Class<?> eC=E.getClass();
					for(final Object o : entry.parms())
					{
						if(CMClass.checkAncestry(eC, (Class<?>)o))
						{
							found=true;
							break;
						}
					}
					if(!found)
						return false;
					break;
				}
				case CLASSTYPE: // +classtype
				{
					final Class<?> eC=E.getClass();
					for(final Object o : entry.parms())
					{
						if(CMClass.checkAncestry(eC, (Class<?>)o))
							return false;
					}
					break;
				}
				case _RACE: // -race
					if(!CMParms.contains(entry.parms(),actual?mob.baseCharStats().getMyRace().name():mob.charStats().raceName()))
						return false;
					break;
				case _ALIGNMENT: // -alignment
					if(!CMParms.contains(entry.parms(),CMLib.flags().getAlignmentName(mob)))
						return false;
					break;
				case _GENDER: // -gender
				{
					base=getBaseCharStats(base,mob);
					if(!CMParms.contains(entry.parms(),actual?(""+((char)base.getStat(CharStats.STAT_GENDER))):(""+(Character.toUpperCase(mob.charStats().genderName().charAt(0))))))
						return false;
					break;
				}
				case _LEVEL: // -level
					if(P!=null)
					{
						final int level=actual?P.basePhyStats().level():P.phyStats().level();
						boolean found=false;
						for(int v=0;v<entry.parms().length-1;v+=2)
						{
							switch((ZapperKey)entry.parms()[v])
							{
							case LVLGR: // +lvlgr
								if(level>((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							case LVLGE: // +lvlge
								if(level>=((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							case LVLLT: // +lvlt
								if(level<((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							case LVLLE: // +lvlle
								if(level<=((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							case LVLEQ: // +lvleq
								if(level==((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							default:
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case _CLASSLEVEL: // -classlevel
					{
						boolean found=false;
						final int cl=actual?mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass())
									 :mob.charStats().getClassLevel(mob.charStats().getCurrentClass());
						for(int v=0;v<entry.parms().length-1;v+=2)
						{
							switch((ZapperKey)entry.parms()[v])
							{
							case LVLGR: // +lvlgr
								if(cl>((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							case LVLGE: // +lvlge
								if(cl>=((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							case LVLLT: // +lvlt
								if(cl<((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							case LVLLE: // +lvlle
								if(cl<=((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							case LVLEQ: // +lvleq
								if(cl==((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							default:
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case _ANYCLASSLEVEL: // -anyclasslevel
					{
						boolean allFound = false;
						for(int i=0;i<entry.parms().length;i+=3)
						{
							boolean found=false;
							final CharClass C = (CharClass)entry.parms()[i];
							final int cl=actual?mob.baseCharStats().getClassLevel(C)
												:mob.charStats().getClassLevel(C);
							if(cl < 0)
								found = false;
							else
							switch((ZapperKey)entry.parms()[i+1])
							{
							case LVLGR: // +lvlgr
								if(cl>((Integer)entry.parms()[i+2]).intValue())
									found=true;
								break;
							case LVLGE: // +lvlge
								if(cl>=((Integer)entry.parms()[i+2]).intValue())
									found=true;
								break;
							case LVLLT: // +lvlt
								if(cl<((Integer)entry.parms()[i+2]).intValue())
									found=true;
								break;
							case LVLLE: // +lvlle
								if(cl<=((Integer)entry.parms()[i+2]).intValue())
									found=true;
								break;
							case LVLEQ: // +lvleq
								if(cl==((Integer)entry.parms()[i+2]).intValue())
									found=true;
								break;
							default:
								break;
							}
							allFound = allFound || found;
						}
						if(!allFound)
							return false;
					}
					break;
				case ANYCLASSLEVEL: // +classlevel
					{
						for(int i=0;i<entry.parms().length;i+=3)
						{
							final CharClass C = (CharClass)entry.parms()[i+0];
							final int cl=actual?mob.baseCharStats().getClassLevel(C)
												:mob.charStats().getClassLevel(C);
							if(cl >= 0)
							{
								switch((ZapperKey)entry.parms()[i+1])
								{
								case LVLGR: // lvlgr
									if(cl>((Integer)entry.parms()[i+2]).intValue())
										return false;
									break;
								case LVLGE: // lvlge
									if(cl>=((Integer)entry.parms()[i+2]).intValue())
										return false;
									break;
								case LVLLT: // lvlt
									if(cl<((Integer)entry.parms()[i+2]).intValue())
										return false;
									break;
								case LVLLE: // lvlle
									if(cl<=((Integer)entry.parms()[i+2]).intValue())
										return false;
									break;
								case LVLEQ: // +lvleq
									if(cl==((Integer)entry.parms()[i+2]).intValue())
										return false;
									break;
								default:
									break;
								}
							}
						}
					}
					break;
				case _MAXCLASSLEVEL: // -maxclasslevel
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
								if(c2>cl)
									cl=c2;
							}
						}
						else
						{
							cl=mob.charStats().getClassLevel(mob.charStats().getMyClass(0));
							for(int v=1;v<mob.charStats().numClasses();v++)
							{
								c2=mob.charStats().getClassLevel(mob.charStats().getMyClass(v));
								if(c2>cl)
									cl=c2;
							}
						}
						for(int v=0;v<entry.parms().length-1;v+=2)
						{
							switch((ZapperKey)entry.parms()[v])
							{
							case LVLGR: // +lvlgr
								if(cl>((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							case LVLGE: // +lvlge
								if(cl>=((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							case LVLLT: // +lvlt
								if(cl<((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							case LVLLE: // +lvlle
								if(cl<=((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							case LVLEQ: // +lvleq
								if(cl==((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							default:
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case _TATTOO: // -tattoo
					{
						boolean found=false;
						for(final Object o : entry.parms())
						{
							if((mob.findTattoo((String)o)!=null)
							||((room!=null)&&(room.getArea().getBlurbFlag((String)o)!=null)))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case TATTOO: // +tattoo
					{
						for(final Object o : entry.parms())
						{
							if((mob.findTattoo((String)o)!=null)
							||((room!=null)&&(room.getArea().getBlurbFlag((String)o)!=null)))
								return false;
						}
					}
					break;
				case WEAPONTYPE: // +weapontype
					{
						final Environmental W=(E instanceof MOB) ? ((MOB)E).fetchWieldedItem() : E;
						if(W instanceof Weapon)
						{
							if(CMParms.indexOf(entry.parms(), Integer.valueOf(((Weapon)E).weaponDamageType())) >= 0)
								return false;
						}
					}
					break;
				case _WEAPONTYPE: // -weapontype
					{
						final Environmental W=(E instanceof MOB) ? ((MOB)E).fetchWieldedItem() : E;
						if(W instanceof Weapon)
						{
							if(CMParms.indexOf(entry.parms(), Integer.valueOf(((Weapon)E).weaponDamageType())) < 0)
								return false;
						}
					}
					break;
				case WEAPONAMMO: // +weaponammo
					{
						final Environmental W=(E instanceof MOB) ? ((MOB)E).fetchWieldedItem() : E;
						if((W instanceof AmmunitionWeapon)&&(((AmmunitionWeapon)W).requiresAmmunition()))
						{
							if(CMParms.indexOf(entry.parms(), ((AmmunitionWeapon)W).ammunitionType()) >= 0)
								return false;
						}
						else
						{
							if(CMParms.indexOf(entry.parms(), "") >= 0)
								return false;
						}
					}
					break;
				case _WEAPONAMMO: // -weaponammo
					{
						final Environmental W=(E instanceof MOB) ? ((MOB)E).fetchWieldedItem() : E;
						if((W instanceof AmmunitionWeapon)&&(((AmmunitionWeapon)W).requiresAmmunition()))
						{
							if(CMParms.indexOf(entry.parms(), ((AmmunitionWeapon)W).ammunitionType()) < 0)
								return false;
						}
						else
						{
							if(CMParms.indexOf(entry.parms(), "") < 0)
								return false;
						}
					}
					break;
				case WEAPONCLASS: // +weaponclass
					{
						final Environmental W=(E instanceof MOB) ? ((MOB)E).fetchWieldedItem() : E;
						if(W instanceof Weapon)
						{
							if(CMParms.indexOf(entry.parms(), Integer.valueOf(((Weapon)E).weaponClassification())) >= 0)
								return false;
						}
					}
					break;
				case _WEAPONCLASS: // -weaponclass
					{
						final Environmental W=(E instanceof MOB) ? ((MOB)E).fetchWieldedItem() : E;
						if(W instanceof Weapon)
						{
							if(CMParms.indexOf(entry.parms(), Integer.valueOf(((Weapon)E).weaponClassification())) < 0)
								return false;
						}
					}
					break;
				case _MOOD: // -mood
					{
						String moodName = "NORMAL";
						final Ability A = mob.fetchEffect("Mood");
						if((A!=null)&&(A.text().trim().length()>0))
							moodName=A.text().toUpperCase().trim();
						if(!CMParms.contains(entry.parms(),moodName))
							return false;
					}
					break;
				case MOOD: // +mood
					{
						final String moodName;
						final Ability A = mob.fetchEffect("Mood");
						if((A!=null)&&(A.text().trim().length()>0))
							moodName=A.text().toUpperCase().trim();
						else
							moodName= "NORMAL";
						if(CMParms.contains(entry.parms(),moodName))
							return false;
					}
					break;
				case _ACCCHIEVE: // -accchieves
					{
						boolean found=false;
						final PlayerStats playerStats = mob.playerStats();
						if((playerStats != null) && (mob.playerStats().getAccount()!=null))
						{
							final PlayerAccount acct = playerStats.getAccount();
							for(final Object o : entry.parms())
							{
								if((acct.findTattoo((String)o)!=null)
								||((room!=null)&&(room.getArea().getBlurbFlag((String)o)!=null)))
								{
									found = true;
									break;
								}
							}
						}
						if(!found)
							return false;
					}
					break;
				case ACCCHIEVE: // +accchieves
					{
						final PlayerStats playerStats = mob.playerStats();
						if((playerStats != null) && (mob.playerStats().getAccount()!=null))
						{
							final PlayerAccount acct = playerStats.getAccount();
							for(final Object o : entry.parms())
							{
								if((acct.findTattoo((String)o)!=null)
								||((room!=null)&&(room.getArea().getBlurbFlag((String)o)!=null)))
									return false;
							}
						}
					}
					break;
				case _EXPERTISE: // -expertise
					{
						boolean found=false;
						for(final Object o : entry.parms())
						{
							if(mob.fetchExpertise((String)o)!=null)
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case EXPERTISE: // +expertise
					{
						for(final Object o : entry.parms())
						{
							if(mob.fetchExpertise((String)o)!=null)
								return false;
						}
					}
					break;
				case _QUESTWIN: // -questwin
					{
						boolean found=false;
						for(final Object o : entry.parms())
						{
							final Quest Q=CMLib.quests().fetchQuest((String)o);
							if((Q!=null)&&(Q.wasWinner(mob.Name())))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case QUESTWIN: // +questwin
					{
						for(final Object o : entry.parms())
						{
							final Quest Q=CMLib.quests().fetchQuest((String)o);
							if((Q!=null)&&(Q.wasWinner(mob.Name())))
								return false;
						}
					}
					break;
				case _SKILL: // -skill
					{
						boolean found=false;
						for(int v=0;v<entry.parms().length-1;v+=2)
						{
							final Ability A=mob.fetchAbility((String)entry.parms()[v]);
							if((A!=null)&&(A.proficiency()>=((Integer)entry.parms()[v+1]).intValue()))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case _SKILLFLAG: // -skillflag
					{
						boolean found=false;
						for(final Object o : entry.parms())
						{
							for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
							{
								final Ability A=a.nextElement();
								if(evaluateSkillFlagObject(o,A))
								{
									found = true;
									break;
								}
							}
							if(found)
								break;
						}
						if(!found)
							return false;
					}
					break;
				case SKILL: // +skill
					{
						for(int v=0;v<entry.parms().length-1;v+=2)
						{
							final Ability A=mob.fetchAbility((String)entry.parms()[v]);
							if((A!=null)&&(A.proficiency()>=((Integer)entry.parms()[v+1]).intValue()))
								return false;
						}
					}
					break;
				case SKILLFLAG: // +skillflag
					{
						for(final Object o : entry.parms())
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
				case _SECURITY: // -security
					{
						boolean found=false;
						for(final Object o : entry.parms())
						{
							if(CMSecurity.isAllowed(mob,room,(CMSecurity.SecFlag)o))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case SECURITY: // +security
					{
						for(final Object o : entry.parms())
						{
							if(CMSecurity.isAllowed(mob,room,(CMSecurity.SecFlag)o))
								return false;
						}
					}
					break;
				case _NAME: // -names
					{
						boolean found=false;
						final String name=actual?E.Name():E.name();
						for(final Object o : entry.parms())
						{
							if(name.equalsIgnoreCase((String)o))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case _ACCOUNT: // -accounts
					{
						boolean found=false;
						final String name=((mob.playerStats()!=null)
											&&(mob.playerStats().getAccount()!=null))?
												mob.playerStats().getAccount().getAccountName():
											E.Name();
						for(final Object o : entry.parms())
						{
							if(name.equalsIgnoreCase((String)o))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case SUBNAME: // +subname
					{
						final String name=(actual?E.Name():E.name()).toLowerCase();
						for(final Object o : entry.parms())
						{
							final String s = (String)o; // already lowercased
							if(s.startsWith("*"))
							{
								if(s.endsWith("*"))
								{
									if(name.indexOf(s.substring(1,s.length()-1))>=0)
									{
										return false;
									}
								}
								else
								if(name.endsWith(s.substring(1)))
								{
									return false;
								}
							}
							else
							if(s.endsWith("*"))
							{
								if(name.startsWith(s.substring(0,s.length()-1)))
								{
									return false;
								}
							}
							else
							if(name.indexOf(s)>=0)
							{
								return false;
							}
						}
					}
					break;
				case _SUBNAME: // -subname
					{
						boolean found=false;
						final String name=(actual?E.Name():E.name()).toLowerCase();
						for(final Object o : entry.parms())
						{
							final String s = (String)o; // already lowercased
							if(s.startsWith("*"))
							{
								if(s.endsWith("*"))
								{
									if(name.indexOf(s.substring(1,s.length()-1))>=0)
									{
										found=true;
										break;
									}
								}
								else
								if(name.endsWith(s.substring(1)))
								{
									found=true;
									break;
								}
							}
							else
							if(s.endsWith("*"))
							{
								if(name.startsWith(s.substring(0,s.length()-1)))
								{
									found=true;
									break;
								}
							}
							else
							if(name.indexOf(s)>=0)
							{
								found=true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case _PLAYER: // -player
					if(!mob.isMonster())
						return false;
					break;
				case _NPC: // -npc
					if(mob.isMonster())
						return false;
					break;
				case _RACECAT: // -racecat
				{
					String raceCat=mob.baseCharStats().getMyRace().racialCategory();
					if((!actual)&&(!mob.baseCharStats().getMyRace().name().equals(mob.charStats().raceName())))
					{
						final Race R2=CMClass.getRace(mob.charStats().raceName());
						if(R2!=null)
							raceCat=R2.racialCategory();
					}
					if(!CMParms.contains(entry.parms(),raceCat))
						return false;
					break;
				}
				case RACE: // +race
				{
					String race=mob.baseCharStats().getMyRace().name();
					if((!actual)&&(!mob.baseCharStats().getMyRace().name().equals(mob.charStats().raceName())))
						race=mob.charStats().raceName();
					if(CMParms.contains(entry.parms(),race))
						return false;
					break;
				}
				case RACECAT: // +racecat
				{
					String raceCat=mob.baseCharStats().getMyRace().racialCategory();
					if((!actual)&&(!mob.baseCharStats().getMyRace().name().equals(mob.charStats().raceName())))
					{
						final Race R2=CMClass.getRace(mob.charStats().raceName());
						if(R2!=null)
							raceCat=R2.racialCategory();
					}
					if(CMParms.contains(entry.parms(),raceCat))
						return false;
					break;
				}
				case _CLAN: // -clan
					{
						boolean found=false;
						if(E instanceof ClanItem)
						{
							final String clanID=((ClanItem)E).clanID();
							for(final Object o : entry.parms())
							{
								if(o instanceof String)
								{
									if((clanID.equalsIgnoreCase((String)o))
									||(((String)o).equals("*")))
									{ 
										found=true; 
										break;
									}
								}
								else
								if(o instanceof Pair)
								{
									if(clanID.equalsIgnoreCase(((Pair<String,String>)o).first)
									||((((Pair<String,String>)o).first).equals("*")))
									{ 
										found=true; 
										break;
									}
								}
							}
						}
						else
						if(E instanceof MOB)
						{
							for(final Pair<Clan,Integer> c : ((MOB)E).clans())
							{
								for(final Object o : entry.parms())
								{
									if(o instanceof String)
									{
										if(c.first.clanID().equalsIgnoreCase((String)o)
										||(((String)o).equals("*")))
										{ 
											found=true; 
											break;
										}
									}
									else
									if(o instanceof Pair)
									{
										if(c.first.clanID().equalsIgnoreCase(((Pair<String,String>)o).first)
										||((((Pair<String,String>)o).first).equals("*")))
										{ 
											if((((Pair<String,String>)o).second).equals("*"))
											{
												found=true; 
												break;
											}
											else
											{
												ClanPosition cP=c.first.getGovernment().getPosition(((Pair<String,String>)o).second);
												if((cP==null)||(cP.getRoleID()==c.second.intValue()))
												{
													found=true; 
													break;
												}
											}
										}
									}
								}
							}
						}
						if(!found)
							return false;
					}
					break;
				case CLAN: // +clan
					if(E instanceof ClanItem)
					{
						final String clanID=((ClanItem)E).clanID();
						for(final Object o : entry.parms())
						{
							if(o instanceof String)
							{
								if((clanID.equalsIgnoreCase((String)o))
								||(((String)o).equals("*")))
									return false;
							}
							else
							if(o instanceof Pair)
							{
								if(clanID.equalsIgnoreCase(((Pair<String,String>)o).first)
								||((((Pair<String,String>)o).first).equals("*")))
									return false;
							}
						}
					}
					else
					if(E instanceof MOB)
					{
						for(final Pair<Clan,Integer> c : ((MOB)E).clans())
						{
							for(final Object o : entry.parms())
							{
								if(o instanceof String)
								{
									if(c.first.clanID().equalsIgnoreCase((String)o)
									||(((String)o).equals("*")))
										return false;
								}
								else
								if(o instanceof Pair)
								{
									if(c.first.clanID().equalsIgnoreCase(((Pair<String,String>)o).first)
									||((((Pair<String,String>)o).first).equals("*")))
									{ 
										if((((Pair<String,String>)o).second).equals("*"))
											return false;
										else
										{
											ClanPosition cP=c.first.getGovernment().getPosition(((Pair<String,String>)o).second);
											if((cP!=null)&&(cP.getRoleID()==c.second.intValue()))
												return false;
										}
									}
								}
							}
						}
					}
					break;
				case MATERIAL: // +material
					if((item!=null)&&CMParms.contains(entry.parms(),RawMaterial.Material.findByMask(item.material()&RawMaterial.MATERIAL_MASK).desc()))
						return false;
					break;
				case _MATERIAL: // -material
					if((item!=null)&&(!CMParms.contains(entry.parms(),RawMaterial.Material.findByMask(item.material()&RawMaterial.MATERIAL_MASK).desc())))
						return false;
					break;
				case WORNON: // +wornOn
					if(item!=null)
					{
						for(final Object o : entry.parms())
						{
							if((item.rawProperLocationBitmap()&((Long)o).longValue())>0)
								return false;
						}
					}
					break;
				case _WORNON: // -wornOn
					{
						boolean found=false;
						if(item!=null)
						{
							for(final Object o : entry.parms())
							{
								if((item.rawProperLocationBitmap()&((Long)o).longValue())>0)
								{
									found = true;
									break;
								}
							}
						}
						if(!found)
							return false;
					}
					break;
				case DISPOSITION: // +disposition
					if(P!=null)
					{
						for(final Object o : entry.parms())
						{
							if((P.phyStats().disposition()&((Integer)o).intValue())>0)
								return false;
						}
					}
					break;
				case _DISPOSITION: // -disposition
					if(P!=null)
					{
						boolean found=false;
						for(final Object o : entry.parms())
						{
							if((P.phyStats().disposition()&((Integer)o).intValue())>0)
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case SENSES: // +senses
					if(P!=null)
					{
						for(final Object o : entry.parms())
						{
							if((P.phyStats().sensesMask()&((Integer)o).intValue())>0)
								return false;
						}
					}
					break;
				case _SENSES: // -senses
					if(P!=null)
					{
						boolean found=false;
						for(final Object o : entry.parms())
						{
							if((P.phyStats().sensesMask()&((Integer)o).intValue())>0)
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case HOUR: // +HOUR
					{
						if(room!=null)
						{
							for(final Object o : entry.parms())
							{
								if(room.getArea().getTimeObj().getHourOfDay()==((Integer)o).intValue())
									return false;
							}
						}
					}
					break;
				case _HOUR: // -HOUR
					{
						boolean found=false;
						if(room!=null)
						{
							for(final Object o : entry.parms())
							{
								if(room.getArea().getTimeObj().getHourOfDay()==((Integer)o).intValue())
								{
									found = true;
									break;
								}
							}
						}
						if(!found)
							return false;
					}
					break;
				case SEASON: // +season
					{
						if(room!=null)
						{
							for(final Object o : entry.parms())
							{
								if(room.getArea().getTimeObj().getSeasonCode().ordinal()==((Integer)o).intValue())
									return false;
							}
						}
					}
					break;
				case _SEASON: // -season
					{
						boolean found=false;
						if(room!=null)
						{
							for(final Object o : entry.parms())
							{
								if(room.getArea().getTimeObj().getSeasonCode().ordinal()==((Integer)o).intValue())
								{
									found = true;
									break;
								}
							}
						}
						if(!found)
							return false;
					}
					break;
				case WEATHER: // +weather
					{
						if(room!=null)
						{
							for(final Object o : entry.parms())
							{
								if(room.getArea().getClimateObj().weatherType(room)==((Integer)o).intValue())
									return false;
							}
						}
					}
					break;
				case _WEATHER: // -weather
					{
						boolean found=false;
						if(room!=null)
						{
							for(final Object o : entry.parms())
							{
								if(room.getArea().getClimateObj().weatherType(room)==((Integer)o).intValue())
								{
									found = true;
									break;
								}
							}
						}
						if(!found)
							return false;
					}
					break;
				case MONTH: // +month
					{
						if(room!=null)
						{
							for(final Object o : entry.parms())
							{
								if(room.getArea().getTimeObj().getMonth()==((Integer)o).intValue())
									return false;
							}
						}
					}
					break;
				case _MONTH: // -month
					{
						boolean found=false;
						if(room!=null)
						{
							for(final Object o : entry.parms())
							{
								if(room.getArea().getTimeObj().getMonth()==((Integer)o).intValue())
								{
									found = true;
									break;
								}
							}
						}
						if(!found)
							return false;
					}
					break;
				case DAY: // +day
					{
						if(room!=null)
						{
							for(final Object o : entry.parms())
							{
								if(room.getArea().getTimeObj().getDayOfMonth()==((Integer)o).intValue())
									return false;
							}
						}
					}
					break;
				case _DAY: // -day
					{
						boolean found=false;
						if(room!=null)
						{
							for(final Object o : entry.parms())
							{
								if(room.getArea().getTimeObj().getDayOfMonth()==((Integer)o).intValue())
								{
									found = true;
									break;
								}
							}
						}
						if(!found)
							return false;
					}
					break;
				case QUALLVL: // +quallvl
					if(entry.parms().length>1)
					{
						final Ability A=CMClass.getAbility((String)entry.parms()[0]);
						final int adjustment=((Integer)entry.parms()[1]).intValue();
						final int lvl=CMLib.ableMapper().qualifyingClassLevel(mob,A);
						final int clvl=CMLib.ableMapper().qualifyingLevel(mob,A)+adjustment;
						if(lvl<clvl)
							return false;
					}
					break;
				case _QUALLVL: // -quallvl
					if(entry.parms().length>1)
					{
						final Ability A=CMClass.getAbility((String)entry.parms()[0]);
						final int adjustment=((Integer)entry.parms()[1]).intValue();
						final int lvl=CMLib.ableMapper().qualifyingClassLevel(mob,A);
						final int clvl=CMLib.ableMapper().qualifyingLevel(mob,A)+adjustment;
						if(lvl>clvl)
							return false;
					}
					break;
				case RESOURCE: // +resource
					if((item!=null)&&CMParms.contains(entry.parms(),RawMaterial.CODES.NAME(item.material())))
						return false;
					break;
				case _RESOURCE: // -resource
					if((item!=null)&&(!CMParms.contains(entry.parms(),RawMaterial.CODES.NAME(item.material()))))
						return false;
					break;
				case _JAVACLASS: // -JavaClass
					{
						boolean found=false;
						for(final Object o : entry.parms())
						{
							if(E.ID().equalsIgnoreCase((String)o))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case JAVACLASS: // +JavaClass
					for(final Object o : entry.parms())
					{
						if(E.ID().equalsIgnoreCase((String)o))
							return false;
					}
					break;
				case _DEITY: // -deity
					{
						if(mob.getWorshipCharID().length()==0)
							return false;
						boolean found=false;
						for(final Object o : entry.parms())
						{
							if(mob.getWorshipCharID().equalsIgnoreCase((String)o)||((String)o).equals("ANY"))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case DEITY: // +deity
					{
						if(mob.getWorshipCharID().length()>0)
						{
							for(final Object o : entry.parms())
							{
								if(mob.getWorshipCharID().equalsIgnoreCase((String)o))
									return false;
							}
						}
					}
					break;
				case _EFFECT: // -effects
					{
						boolean found=false;
						if(E instanceof PhysicalAgent)
						{
							for(final Object o : entry.parms())
							{
								if(((Physical)E).fetchEffect((String)o)!=null)
								{
									found = true;
									break;
								}
							}
						}
						if((!found)&&(E instanceof PhysicalAgent))
						{
							for(final Object o : entry.parms())
							{
								if(((PhysicalAgent)E).fetchBehavior((String)o)!=null)
								{
									found = true;
									break;
								}
							}
						}
						if(!found)
							return false;
					}
					break;
				case _FACTION: // -faction
					{
						boolean found=false;
						for(final Object o : entry.parms())
						{
							final Faction.FRange FR=getRange((String)o);
							if((FR!=null)&&(CMLib.factions().isFactionedThisWay(mob,FR)))
							{
								found=true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case FACTION: // +faction
					{
						for(final Object o : entry.parms())
						{
							final Faction.FRange FR=getRange((String)o);
							if((FR!=null)&&(CMLib.factions().isFactionedThisWay(mob,FR)))
								return false;
						}
					}
					break;
				case EFFECT: // +effects
					{
						if(E instanceof Physical)
						{
							for(final Object o : entry.parms())
							{
								if(((Physical)E).fetchEffect((String)o)!=null)
									return false;
							}
						}
						if(E instanceof PhysicalAgent)
						{
							for(final Object o : entry.parms())
							{
								if(((PhysicalAgent)E).fetchBehavior((String)o)!=null)
									return false;
							}
						}
					}
					break;
				case NAME: // +name
					{
						final String name=actual?E.Name():E.name();
						for(final Object o : entry.parms())
						{
							if(name.equalsIgnoreCase((String)o))
								return false;
						}
					}
					break;
				case ACCOUNT: // +account
					{
						final String name=((mob.playerStats()!=null)
											&&(mob.playerStats().getAccount()!=null))?
												mob.playerStats().getAccount().getAccountName():
											E.Name();
						for(final Object o : entry.parms())
						{
							if(name.equalsIgnoreCase((String)o))
								return false;
						}
					}
					break;
				case _ANYCLASS: // -anyclass
					{
						boolean found=false;
						if(actual)
						{
							for(final Object o : entry.parms())
							{
								if(mob.baseCharStats().getClassLevel((String)o)>=0)
								{
									found = true;
									break;
								}
							}
						}
						else
						{
							for(final Object o : entry.parms())
							{
								if((mob.charStats().getClassLevel((String)o)>=0)
								||(mob.charStats().displayClassName().equalsIgnoreCase((String)o)))
								{
									found = true;
									break;
								}
							}
						}
						if(!found)
							return false;
					}
					break;
				case ANYCLASS: // +anyclass
					if(actual)
					{
						for(final Object o : entry.parms())
						{
							if(mob.baseCharStats().getClassLevel((String)o)>=0)
								return false;
						}
					}
					else
					{
						for(final Object o : entry.parms())
						{
							if((mob.charStats().getClassLevel((String)o)>=0)
							||(mob.charStats().displayClassName().equalsIgnoreCase((String)o)))
								return false;
						}
					}
					break;
				case ADJSTRENGTH: // +adjstr
					if((entry.parms().length>0)&&(mob.charStats().getStat(CharStats.STAT_STRENGTH)<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case ADJINTELLIGENCE: // +adjint
					if((entry.parms().length>0)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case ADJWISDOM: // +adjwis
					if((entry.parms().length>0)&&(mob.charStats().getStat(CharStats.STAT_WISDOM)<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case ADJDEXTERITY: // +adjdex
					if((entry.parms().length>0)&&(mob.charStats().getStat(CharStats.STAT_DEXTERITY)<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case ADJCONSTITUTION: // -adjcha
					if((entry.parms().length>0)&&(mob.charStats().getStat(CharStats.STAT_CONSTITUTION)<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case ADJCHARISMA: // +adjcha
					if((entry.parms().length>0)&&(mob.charStats().getStat(CharStats.STAT_CHARISMA)<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _ADJSTRENGTH: // -adjstr
					if((entry.parms().length>0)&&(mob.charStats().getStat(CharStats.STAT_STRENGTH)>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _ADJINTELLIGENCE: // -adjint
					if((entry.parms().length>0)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _ADJWISDOM: // -adjwis
					if((entry.parms().length>0)&&(mob.charStats().getStat(CharStats.STAT_WISDOM)>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _ADJDEXTERITY: // -adjdex
					if((entry.parms().length>0)&&(mob.charStats().getStat(CharStats.STAT_DEXTERITY)>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _ADJCONSTITUTION: // -adjcon
					if((entry.parms().length>0)&&(mob.charStats().getStat(CharStats.STAT_CONSTITUTION)>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _ADJCHARISMA: // -adjcha
					if((entry.parms().length>0)&&(mob.charStats().getStat(CharStats.STAT_CHARISMA)>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case STRENGTH: // +str
					base=getBaseCharStats(base,mob);
					if((entry.parms().length>0)&&(base.getStat(CharStats.STAT_STRENGTH)<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case INTELLIGENCE: // +int
					base=getBaseCharStats(base,mob);
					if((entry.parms().length>0)&&(base.getStat(CharStats.STAT_INTELLIGENCE)<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case WISDOM: // +wis
					base=getBaseCharStats(base,mob);
					if((entry.parms().length>0)&&(base.getStat(CharStats.STAT_WISDOM)<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case DEXTERITY: // +dex
					base=getBaseCharStats(base,mob);
					if((entry.parms().length>0)&&(base.getStat(CharStats.STAT_DEXTERITY)<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case CONSTITUTION: // +con
					base=getBaseCharStats(base,mob);
					if((entry.parms().length>0)&&(base.getStat(CharStats.STAT_CONSTITUTION)<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case CHARISMA: // +cha
					base=getBaseCharStats(base,mob);
					if((entry.parms().length>0)&&(base.getStat(CharStats.STAT_CHARISMA)<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _STRENGTH: // -str
					base=getBaseCharStats(base,mob);
					if((entry.parms().length>0)&&(base.getStat(CharStats.STAT_STRENGTH)>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _INTELLIGENCE: // -int
					base=getBaseCharStats(base,mob);
					if((entry.parms().length>0)&&(base.getStat(CharStats.STAT_INTELLIGENCE)>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _WISDOM: // -wis
					base=getBaseCharStats(base,mob);
					if((entry.parms().length>0)&&(base.getStat(CharStats.STAT_WISDOM)>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _DEXTERITY: // -dex
					base=getBaseCharStats(base,mob);
					if((entry.parms().length>0)&&(base.getStat(CharStats.STAT_DEXTERITY)>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _CONSTITUTION: // -con
					base=getBaseCharStats(base,mob);
					if((entry.parms().length>0)&&(base.getStat(CharStats.STAT_CONSTITUTION)>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _CHARISMA: // -cha
					base=getBaseCharStats(base,mob);
					if((entry.parms().length>0)
					&&(base.getStat(CharStats.STAT_CHARISMA)>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _CHANCE: // -chance
					if((entry.parms().length>0)
					&&(CMLib.dice().rollPercentage()<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case ABILITY: // +ability
					if((entry.parms().length>0)
					&&(P != null)
					&&(P.phyStats().ability()>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _ABILITY: // -ability
					if((entry.parms().length>0)
					&&(P != null)
					&&(P.phyStats().ability()<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case WEIGHT: // +weight
					if((entry.parms().length>0)
					&&(P != null)
					&&(P.phyStats().weight()>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _WEIGHT: // -weight
					if((entry.parms().length>0)
					&&(P != null)
					&&(P.phyStats().weight()<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case ARMOR: // +armor
					if((entry.parms().length>0)
					&&(P != null)
					&&(P.phyStats().armor()>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _ARMOR: // -armor
					if((entry.parms().length>0)
					&&(P != null)
					&&(P.phyStats().armor()<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case DAMAGE: // +damage
					if((entry.parms().length>0)
					&&(P != null)
					&&(P.phyStats().damage()>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _DAMAGE: // -damage
					if((entry.parms().length>0)
					&&(P != null)
					&&(P.phyStats().damage()<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case ATTACK: // +attack
					if((entry.parms().length>0)
					&&(P != null)
					&&(P.phyStats().attackAdjustment()>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _ATTACK: // -attack
					if((entry.parms().length>0)
					&&(P != null)
					&&(P.phyStats().attackAdjustment()<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case VALUE: // +value
					if(E instanceof MOB)
					{
						if((entry.parms().length>0)
						&&(CMLib.beanCounter().getTotalAbsoluteValueAllCurrencies(mob)>(((Integer)entry.parms()[0]).intValue())))
							return false;
					}
					else
					{
						if((entry.parms().length>0)
						&&(item!=null)&&(item.baseGoldValue()>(((Integer)entry.parms()[0]).intValue())))
							return false;
					}
					break;
				case _VALUE: // -value
					if(E instanceof MOB)
					{
						if((entry.parms().length>0)
						&&(CMLib.beanCounter().getTotalAbsoluteValueAllCurrencies(mob)<(((Integer)entry.parms()[0]).intValue())))
							return false;
					}
					else
					{
						if((entry.parms().length>0)
						&&(item!=null)&&(item.baseGoldValue()<(((Integer)entry.parms()[0]).intValue())))
							return false;
					}
					break;
				case _AREA: // -area
					{
						boolean found=false;
						if(room!=null)
						{
							for(final Object o : entry.parms())
							{
								if(room.getArea().Name().equalsIgnoreCase((String)o))
								{
									found = true;
									break;
								}
							}
						}
						if(!found)
							return false;
					}
					break;
				case AREA: // +area
					{
						if(room!=null)
						{
							for(final Object o : entry.parms())
							{
								if(room.getArea().Name().equalsIgnoreCase((String)o))
									return false;
							}
						}
						break;
					}
				case _ISHOME: // -ishome
					{
						final Area homeA=CMLib.map().getStartArea(E);
						final Area isA=CMLib.map().areaLocation(E);
						if(homeA == isA)
							return false;
						break;
					}
				case ISHOME: // +ishome
				{
					final Area homeA=CMLib.map().getStartArea(E);
					final Area isA=CMLib.map().areaLocation(E);
					if(homeA != isA)
						return false;
					break;
				}
				case _HOME: // -home
					{
						boolean found=false;
						final Area A=CMLib.map().getStartArea(E);
						if(A!=null)
						{
							for(final Object o : entry.parms())
							{
								if(A.Name().equalsIgnoreCase((String)o))
								{
									found = true;
									break;
								}
							}
						}
						if(!found)
							return false;
					}
					break;
				case HOME: // +home
					{
						final Area A=CMLib.map().getStartArea(E);
						if(A!=null)
						{
							for(final Object o : entry.parms())
							{
								if(A.Name().equalsIgnoreCase((String)o))
									return false;
							}
						}
					}
					break;
				case _IFSTAT:
					{
						boolean found=false;
						if(E instanceof Physical)
						{
							for(int i=0;i<entry.parms().length;i+=2)
							{
								if(CMLib.coffeeMaker().getAnyGenStat((Physical)E,(String)entry.parms()[i]).equalsIgnoreCase((String)entry.parms()[i+1]))
								{
									found=true;
									break;
								}
							}
						}
						else
						{
							for(int i=0;i<entry.parms().length;i+=2)
							{
								if(E.getStat((String)entry.parms()[i]).equalsIgnoreCase((String)entry.parms()[i+1]))
								{
									found=true;
									break;
								}
							}
						}
						if(!found)
							return false;
					}
					break;
				case IFSTAT:
					{
						if(E instanceof Physical)
						{
							for(int i=0;i<entry.parms().length;i+=2)
							{
								if(CMLib.coffeeMaker().getAnyGenStat((Physical)E,(String)entry.parms()[i]).equalsIgnoreCase((String)entry.parms()[i+1]))
									return false;
							}
						}
						else
						{
							for(int i=0;i<entry.parms().length;i+=2)
							{
								if(E.getStat((String)entry.parms()[i]).equalsIgnoreCase((String)entry.parms()[i+1]))
									return false;
							}
						}
					}
					break;
				case ITEM: // +item
					{
						for(final Object o : entry.parms())
						{
							if(mob.findItem((String)o)!=null)
								return false;
						}
					}
					break;
				case _ITEM: // -item
					{
						boolean found=false;
						for(final Object o : entry.parms())
						{
							if(mob.findItem((String)o)!=null)
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case WORN: // +worn
					{
						if(E instanceof MOB)
						{
							for(final Object o : entry.parms())
							{
								Item I = mob.findItem((String)o);
								if((I!=null)&&(!I.amWearingAt(Wearable.IN_INVENTORY)))
									return false;
							}
						}
						else
						if(E instanceof Item)
						{
							if(((Item)E).amWearingAt(Wearable.IN_INVENTORY))
								return false;
						}
					}
					break;
				case _WORN: // -worn
					if(E instanceof MOB)
					{
						boolean found=false;
						for(final Object o : entry.parms())
						{
							final Item I=mob.findItem((String)o);
							if((I!=null)&&(!I.amWearingAt(Wearable.IN_INVENTORY)))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					else
					if(E instanceof Item)
					{
						if(((Item)E).amWearingAt(Wearable.IN_INVENTORY))
							return false;
					}
					break;
				case ALIGNMENT: // +alignment
					if(CMParms.contains(entry.parms(),CMLib.flags().getAlignmentName(mob)))
						return false;
					break;
				case GENDER: // +gender
					base=getBaseCharStats(base,mob);
					if(CMParms.contains(entry.parms(),actual?(""+((char)base.getStat(CharStats.STAT_GENDER))):(""+Character.toUpperCase(mob.charStats().genderName().charAt(0)))))
						return false;
					break;
				case LVLGR: // +lvlgr
					if((entry.parms().length>0)
					&&(P!=null)
					&&((actual?P.basePhyStats().level():P.phyStats().level())>((Integer)entry.parms()[0]).intValue()))
						return false;
					break;
				case LVLGE: // +lvlge
					if((entry.parms().length>0)
					&&(P!=null)
					&&((actual?P.basePhyStats().level():P.phyStats().level())>=((Integer)entry.parms()[0]).intValue()))
						return false;
					break;
				case LVLLT: // +lvlt
					if((entry.parms().length>0)
					&&(P!=null)
					&&((actual?P.basePhyStats().level():P.phyStats().level())<((Integer)entry.parms()[0]).intValue()))
						return false;
					break;
				case LVLLE: // +lvlle
					if((entry.parms().length>0)
					&&(P!=null)
					&&((actual?P.basePhyStats().level():P.phyStats().level())<=((Integer)entry.parms()[0]).intValue()))
						return false;
					break;
				case LVLEQ: // +lvleq
					if((entry.parms().length>0)
					&&(P!=null)
					&&((actual?P.basePhyStats().level():P.phyStats().level())==((Integer)entry.parms()[0]).intValue()))
						return false;
					break;
				case GROUPSIZE: // +groupsize
					if((entry.parms().length>0)
					&&(mob.getGroupMembers(new HashSet<MOB>(1)).size()<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _GROUPSIZE: // -groupsize
					if((entry.parms().length>0)
					&&(mob.getGroupMembers(new HashSet<MOB>(1)).size()>(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case _IF: // -if
					{
						boolean oneIsOK = false;
						if(E instanceof PhysicalAgent)
						{
							for(int v=0;v<entry.parms().length-2;v+=3)
							{
								final ScriptingEngine SE = (ScriptingEngine)entry.parms()[v];
								final String[][] EVAL = (String[][])entry.parms()[v+1];
								final Object[] tmp = (Object[])entry.parms()[v+2];
								final MOB M = SE.getMakeMOB(E);
								final Item defaultItem=(E instanceof Item)?(Item)E:null;
								if(SE.eval((PhysicalAgent)E, M, null,M, defaultItem, null, "", tmp, EVAL, 0))
								{
									oneIsOK = true;
									break;
								}
							}
						}
						if(!oneIsOK)
							return false;
						break;
					}
				case IF: // +if
					{
						if(E instanceof PhysicalAgent)
						{
							for(int v=0;v<entry.parms().length-2;v+=3)
							{
								final ScriptingEngine SE = (ScriptingEngine)entry.parms()[v];
								final String[][] EVAL = (String[][])entry.parms()[v+1];
								final Object[] tmp = (Object[])entry.parms()[v+2];
								final MOB M = SE.getMakeMOB(E);
								final Item defaultItem=(E instanceof Item)?(Item)E:null;
								if(E instanceof PhysicalAgent)
								{
									if(SE.eval((PhysicalAgent)E, M, null,M, defaultItem, null, "", tmp, EVAL, 0))
										return true;
								}
							}
						}
						break;
					}
				}
			}
			catch (final NullPointerException n)
			{
			}
		}
		return true;
	}

	@Override
	public boolean maskCheck(final String text, final PlayerLibrary.ThinPlayer E)
	{
		return maskCheck(getPreCompiledMask(text), E);
	}

	@Override
	public boolean maskCheck(final CompiledZMask cset, final PlayerLibrary.ThinPlayer E)
	{
		if(E==null)
			return true;
		if((cset==null)||(cset.empty())||(cset.entries().length<1))
			return true;
		getMaskCodes();
		//boolean[] flags=(boolean[])cset.firstElement();
		for(final CompiledZMaskEntry entry : cset.entries())
		{
			try
			{
				switch(entry.maskType())
				{
				case SYSOP: // +sysop
					if(CMSecurity.isASysOp(E))
						return true;
					break;
				case _SYSOP: // -sysop
					if(CMSecurity.isASysOp(E))
						return false;
					break;
				case SUBOP: // +subop
					if(CMSecurity.isASysOp(E))
						return true;
					for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
					{
						if(e.nextElement().amISubOp(E.name()))
							return true;
					}
					break;
				case _SUBOP: // -subop
					if(CMSecurity.isASysOp(E))
						return false;
					for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
					{
						if(e.nextElement().amISubOp(E.name()))
							return false;
					}
					break;
				case _CLASS: // -class
					{
						final CharClass C=CMClass.getCharClass(E.charClass());
						if((C==null)||(!CMParms.contains(entry.parms(),C.name())))
							return false;
					}
					break;
				case _BASECLASS: // -baseclass
					{
						final CharClass C=CMClass.getCharClass(E.charClass());
						if((C==null)||(!CMParms.contains(entry.parms(),C.baseClass())))
							return false;
					}
					break;
				case BASECLASS: // +baseclass
					{
						final CharClass C=CMClass.getCharClass(E.charClass());
						if((C!=null)&&(CMParms.contains(entry.parms(),C.baseClass())))
							return false;
					}
					break;
				case _RACE: // -race
					{
						final Race R=CMClass.getRace(E.race());
						if((R==null)||(!CMParms.contains(entry.parms(),R.name())))
							return false;
					}
					break;
				case _LEVEL: // -level
					{
						final int level=E.level();
						boolean found=false;
						for(int v=0;v<entry.parms().length-1;v+=2)
						{
							switch((ZapperKey)entry.parms()[v])
							{
							case LVLGR: // +lvlgr
								if(level>((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							case LVLGE: // +lvlge
								if(level>=((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							case LVLLT: // +lvlt
								if(level<((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							case LVLLE: // +lvlle
								if(level<=((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							case LVLEQ: // +lvleq
								if(level==((Integer)entry.parms()[v+1]).intValue())
									found=true;
								break;
							default:
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case _QUESTWIN: // -questwin
					{
						boolean found=false;
						for(final Object o : entry.parms())
						{
							final Quest Q=CMLib.quests().fetchQuest((String)o);
							if((Q!=null)&&(Q.wasWinner(E.name())))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case QUESTWIN: // +questwin
					{
						for(final Object o : entry.parms())
						{
							final Quest Q=CMLib.quests().fetchQuest((String)o);
							if((Q!=null)&&(Q.wasWinner(E.name())))
								return false;
						}
					}
					break;
				case _NAME: // -names
					{
						boolean found=false;
						final String name=E.name();
						for(final Object o : entry.parms())
						{
							if(name.equalsIgnoreCase((String)o))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case _NPC: // -npc
					break; // always true
				case _RACECAT: // -racecat
					{
						final Race R=CMClass.getRace(E.race());
						if((R==null)||(!CMParms.contains(entry.parms(),R.racialCategory())))
							return false;
					}
					break;
				case RACE: // +race
					{
						final Race R=CMClass.getRace(E.race());
						if((R!=null)&&(CMParms.contains(entry.parms(),R.name())))
							return false;
					}
					break;
				case RACECAT: // +racecat
					{
						final Race R=CMClass.getRace(E.race());
						if((R!=null)&&(CMParms.contains(entry.parms(),R.racialCategory())))
							return false;
					}
					break;
				case _JAVACLASS: // -JavaClass
					{
						boolean found=false;
						for(final Object o : entry.parms())
						{
							if("StdMOB".equalsIgnoreCase((String)o))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case JAVACLASS: // +JavaClass
					for(final Object o : entry.parms())
					{
						if("StdMOB".equalsIgnoreCase((String)o))
						{
							return false;
						}
					}
					break;
				case NAME: // +name
					{
						final String name=E.name();
						for(final Object o : entry.parms())
						{
							if(name.equalsIgnoreCase((String)o))
							{
								return false;
							}
						}
					}
					break;
				case SUBNAME: // +subname
					{
						final String name=E.name().toLowerCase();
						for(final Object o : entry.parms())
						{
							final String s = (String)o; // already lowercased
							if(s.startsWith("*"))
							{
								if(s.endsWith("*"))
								{
									if(name.indexOf(s.substring(1,s.length()-1))>=0)
									{
										return false;
									}
								}
								else
								if(name.endsWith(s.substring(1)))
								{
									return false;
								}
							}
							else
							if(s.endsWith("*"))
							{
								if(name.startsWith(s.substring(0,s.length()-1)))
								{
									return false;
								}
							}
							else
							if(name.indexOf(s)>=0)
							{
								return false;
							}
						}
					}
					break;
				case _SUBNAME: //-subname
					{
						boolean found=false;
						final String name=E.name().toLowerCase();
						for(final Object o : entry.parms())
						{
							final String s = (String)o; // already lowercased
							if(s.startsWith("*"))
							{
								if(s.endsWith("*"))
								{
									if(name.indexOf(s.substring(1,s.length()-1))>=0)
									{
										found=true;
										break;
									}
								}
								else
								if(name.endsWith(s.substring(1)))
								{
									found=true;
									break;
								}
							}
							else
							if(s.endsWith("*"))
							{
								if(name.startsWith(s.substring(0,s.length()-1)))
								{
									found=true;
									break;
								}
							}
							else
							if(name.indexOf(s)>=0)
							{
								found=true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case _ANYCLASS: // -anyclass
					{
						boolean found=false;
						final CharClass C=CMClass.getCharClass(E.charClass());
						if(C!=null)
						{
							for(final Object o : entry.parms())
							{
								if(C.name().equalsIgnoreCase((String)o))
								{
									found = true;
									break;
								}
							}
						}
						if(!found)
							return false;
					}
					break;
				case ANYCLASS: // +anyclass
					{
						final CharClass C=CMClass.getCharClass(E.charClass());
						if(C!=null)
						{
							for(final Object o : entry.parms())
							{
								if(C.name().equalsIgnoreCase((String)o))
								{
									return false;
								}
							}
						}
					}
					break;
				case ACCOUNT:// +accounts
				case _ACCOUNT:// -accounts
				case _ALIGNMENT: // -alignment
				case _GENDER: // -gender
				case _CLASSLEVEL: // -classlevel
				case _MAXCLASSLEVEL: // -maxclasslevel
				case ANYCLASSLEVEL: // +anyclasslevel
				case _ANYCLASSLEVEL: // -anyclasslevel
				case _TATTOO: // -tattoo
				case TATTOO: // +tattoo
				case _MOOD: // -mood
				case MOOD: // +mood
				case _ACCCHIEVE: // -accchieves
				case ACCCHIEVE: // +accchieves
				case _EXPERTISE: // -expertise
				case EXPERTISE: // +expertise
				case _SKILL: // -skill
				case _SKILLFLAG: // -skillflag
				case SKILL: // +skill
				case SKILLFLAG: // +skillflag
				case _SECURITY: // -security
				case SECURITY: // +security
				case _PLAYER: // -player
				case _CLAN: // -clan
				case CLAN: // +clan
				case MATERIAL: // +material
				case _MATERIAL: // -material
				case WORNON: // +wornOn
				case _WORNON: // -wornOn
				case DISPOSITION: // +disposition
				case _DISPOSITION: // -disposition
				case SENSES: // +senses
				case _SENSES: // -senses
				case HOUR: // +HOUR
				case _HOUR: // -HOUR
				case SEASON: // +season
				case _SEASON: // -season
				case WEATHER: // +weather
				case _WEATHER: // -weather
				case MONTH: // +month
				case _MONTH: // -month
				case DAY: // +day
				case _DAY: // -day
				case QUALLVL: // +quallvl
				case _QUALLVL: // -quallvl
				case RESOURCE: // +resource
				case _RESOURCE: // -resource
				case _DEITY: // -deity
				case DEITY: // +deity
				case _EFFECT: // -effects
				case _FACTION: // -faction
				case FACTION: // +faction
				case EFFECT: // +effects
				case ADJSTRENGTH: // +adjstr
				case ADJINTELLIGENCE: // +adjint
				case ADJWISDOM: // +adjwis
				case ADJDEXTERITY: // +adjdex
				case ADJCONSTITUTION: // +adjcon
				case ADJCHARISMA: // +adjcha
				case _ADJSTRENGTH: // -adjstr
				case _ADJINTELLIGENCE: // -adjint
				case _ADJWISDOM: // -adjwis
				case _ADJDEXTERITY: // -adjdex
				case _ADJCONSTITUTION: // -adjcon
				case _ADJCHARISMA: // -adjcha
				case STRENGTH: // +str
				case INTELLIGENCE: // +int
				case WISDOM: // +wis
				case DEXTERITY: // +dex
				case CONSTITUTION: // +con
				case CHARISMA: // +cha
				case _STRENGTH: // -str
				case _INTELLIGENCE: // -int
				case _WISDOM: // -wis
				case _DEXTERITY: // -dex
				case _CONSTITUTION: // -con
				case _CHARISMA: // -cha
				case ABILITY: // +ability
				case _ABILITY: // -ability
				case WEIGHT: // +weight
				case _WEIGHT: // -weight
				case ARMOR: // +armor
				case _ARMOR: // -armor
				case DAMAGE: // +damage
				case _DAMAGE: // -damage
				case ATTACK: // +attack
				case _ATTACK: // -attack
				case VALUE: // +value
				case _VALUE: // -value
				case _AREA: // -area
				case AREA: // +area
				case _HOME: // -home
				case HOME: // +home
				case _ISHOME: // -ishome
				case ISHOME: // +ishome
				case _ITEM: // -item
				case _WORN: // -worn
				case ITEM: // +item
				case WORN: // +worn
				case ALIGNMENT: // +alignment
				case GENDER: // +gender
				case GROUPSIZE: // +groupsize
				case _GROUPSIZE: // -groupsize
				case _IFSTAT: // -ifstat
				case IFSTAT: // +ifstat
				case _CLASSTYPE: // -classtype
				case CLASSTYPE: // +classtype
				case WEAPONTYPE: // +weapontype
				case WEAPONCLASS: // +weaponclass
				case _WEAPONTYPE: // -weapontype
				case _WEAPONCLASS: // -weaponclass
				case WEAPONAMMO: // +weaponammo
				case _WEAPONAMMO: // -weaponammo
					break;
				case _IF: // -if
				case IF: // +if
					return false;
				case LVLGR: // +lvlgr
					if((entry.parms().length>0)&&((E.level())>((Integer)entry.parms()[0]).intValue()))
						return false;
					break;
				case LVLGE: // +lvlge
					if((entry.parms().length>0)&&((E.level())>=((Integer)entry.parms()[0]).intValue()))
						return false;
					break;
				case LVLLT: // +lvlt
					if((entry.parms().length>0)&&((E.level())<((Integer)entry.parms()[0]).intValue()))
						return false;
					break;
				case LVLLE: // +lvlle
					if((entry.parms().length>0)&&((E.level())<=((Integer)entry.parms()[0]).intValue()))
						return false;
					break;
				case LVLEQ: // +lvleq
					if((entry.parms().length>0)&&((E.level())==((Integer)entry.parms()[0]).intValue()))
						return false;
					break;
				case _CHANCE: // -chance
					if((entry.parms().length>0)&&(CMLib.dice().rollPercentage()<(((Integer)entry.parms()[0]).intValue())))
						return false;
					break;
				case CLASS: // +class
				{
					final CharClass C=CMClass.getCharClass(E.charClass());
					if(C!=null)
					if(CMParms.contains(entry.parms(),C.name()))
						return false;
					break;
				}
				}
			}
			catch (final NullPointerException n)
			{
			}
		}
		return true;
	}

	@Override
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
