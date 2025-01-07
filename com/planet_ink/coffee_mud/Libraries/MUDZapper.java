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
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.Align;
import com.planet_ink.coffee_mud.Common.interfaces.ScriptingEngine.MPContext;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
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
   Copyright 2004-2024 Bo Zimmerman

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

	private final static CompiledZMask emptyZMask = new CompiledZapperMaskImpl(new CompiledZMaskEntry[0][0], false, false);

	protected final Map<String, ZapperKey>	zapCodes				= new Hashtable<String, ZapperKey>();
	protected volatile List<SavedClass>		savedCharClasses		= new Vector<SavedClass>(1);
	protected volatile List<SavedRace>		savedRaces				= new Vector<SavedRace>(1);
	protected volatile long					savedClassUpdateTime	= 0;

	protected final Map<ZapperKey,TreeMap<String,Object>>
											compiledCache			= new Hashtable<ZapperKey,TreeMap<String,Object>>();
	protected final TreeMap<String,CompiledZapperMaskEntryImpl>
											looseCodesCache			= new TreeMap<String,CompiledZapperMaskEntryImpl>();
	protected final String[] 				looseFinalLevels		= new String[]
	{
		"+=","+>=","+<=","+>","+<",
		"-=","->=","-<=","->","-<"
	};

	private static class SavedRace
	{
		public final String name;
		public final String minusUpperName;
		public final String racialCategory;
		public final String plusUpperCatName;
		public final String minusUpperCatName;
		public final String plusNameStart;
		public final String minusNameStart;
		public final String plusIdStart;
		public final String minusIdStart;
		public final String minusCatNameStart;

		public SavedRace(final Race race)
		{
			name=race.name();
			minusUpperName="-"+name.toUpperCase();
			plusNameStart="+"+name.toUpperCase();
			minusNameStart="-"+name.toUpperCase();
			plusIdStart="+"+race.ID().toUpperCase();
			minusIdStart="-"+race.ID().toUpperCase();
			racialCategory=race.racialCategory();
			plusUpperCatName="+"+racialCategory.toUpperCase();
			minusUpperCatName="-"+racialCategory.toUpperCase();
			minusCatNameStart="-"+racialCategory.toUpperCase();
		}
	}

	private static class SavedClass
	{
		public final String id;
		public final String name;
		public final String minusUpperName;
		public final String baseClass;
		public final String plusIdStart;
		public final String minusIdStart;
		public final String plusNameStart;
		public final String minusNameStart;
		public final String plusBaseClassStart;
		public final String minusBaseClassStart;

		public SavedClass(final CharClass charClass)
		{
			name=charClass.name();
			id=charClass.ID();
			minusUpperName="-"+name.toUpperCase();
			plusIdStart="+"+id.toUpperCase();
			minusIdStart="-"+id.toUpperCase();
			plusNameStart="+"+name.toUpperCase();
			minusNameStart="-"+name.toUpperCase();
			baseClass=charClass.baseClass();
			plusBaseClassStart="+"+baseClass.toUpperCase();
			minusBaseClassStart="-"+baseClass.toUpperCase();
		}
	}

	public static class CompiledZapperMaskEntryImpl implements CompiledZMaskEntry
	{
		private final ZapperKey maskType;
		private final Object[] parms;
		private final int hashCode;

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
			int hash = maskType.hashCode();
			for(final Object o : parms)
				hash = (hash << 8) ^ ((o==null)?0:o.hashCode());
			hashCode = hash;
		}

		@Override
		public int hashCode()
		{
			return hashCode;
		}

		@Override
		public boolean equals(final Object o)
		{
			if((o == null)||(!(o instanceof CompiledZapperMaskEntryImpl)))
				return false;
			if(hashCode != o.hashCode())
				return false;
			final CompiledZapperMaskEntryImpl me = (CompiledZapperMaskEntryImpl)o;
			if (maskType != me.maskType)
				return false;
			if(parms.length != me.parms.length)
				return false;
			for(int i=0;i<parms.length;i++)
			{
				if(!parms[i].equals(me.parms[i]))
					return false;
			}
			return true;
		}
	}

	private enum LocationType
	{
		OWNED,
		CLANOWNED,
		PRIV,
		ROOMID,
		ROOMSTR
	}

	public static class CompiledZapperMaskImpl implements CompiledZMask
	{
		private final boolean useItemFlag;
		private final boolean useRoomFlag;
		private final boolean empty;
		private final CompiledZMaskEntry[][] entries;
		private final int hashCode;

		@Override
		public boolean useItemFlag()
		{
			return useItemFlag;
		}

		@Override
		public boolean useRoomFlag()
		{
			return useRoomFlag;
		}

		@Override
		public boolean empty()
		{
			return empty;
		}

		@Override
		public CompiledZMaskEntry[][] entries()
		{
			return entries;
		}

		private int makeEntryHash()
		{
			int hash = 0;
			for(final CompiledZMaskEntry[] ce : entries)
			{
				for(final CompiledZMaskEntry e : ce)
					hash = (hash << 8) ^ e.hashCode();
			}
			return hash;
		}

		public CompiledZapperMaskImpl(final CompiledZMaskEntry[][] entries,
									  final boolean useItem, final boolean useRoom)
		{
			this.useItemFlag = useItem;
			this.useRoomFlag = useRoom;
			this.entries = entries;
			this.empty = entries == null || entries.length==0;
			hashCode = Boolean.valueOf(useItemFlag).hashCode()
					^ Boolean.valueOf(useRoomFlag).hashCode()
					^ Boolean.valueOf(empty).hashCode()
					^ makeEntryHash();
		}

		@Override
		public int hashCode()
		{
			return hashCode;
		}

		@Override
		public boolean equals(final Object o)
		{
			if((o == null)||(!(o instanceof CompiledZapperMaskImpl)))
				return false;
			if(hashCode != o.hashCode())
				return false;
			final CompiledZapperMaskImpl me = (CompiledZapperMaskImpl)o;
			if((useItemFlag != me.useItemFlag)
			||(useRoomFlag != me.useRoomFlag)
			||(empty != me.empty))
				return false;
			if(empty)
				return true;
			if(entries.length != me.entries.length)
				return false;
			for(int i=0;i<entries.length;i++)
			{
				if(!entries[i].equals(me.entries[i]))
					return false;
			}
			return true;
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

	protected String rawMaskHelp()
	{
		String maskHelp = (String)Resources.getResource("SYSTEM_ZAPPERMASK_HELP");
		if(maskHelp == null)
		{
			final CMFile F = new CMFile(Resources.makeFileResourceName("help/zappermasks.txt"),null,CMFile.FLAG_LOGERRORS);
			if((F.exists()) && (F.canRead()))
			{
				final List<String> lines=Resources.getFileLineVector(F.text());
				final StringBuilder str = new StringBuilder("");
				for(final String line : lines)
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

	protected void buildSavedClasses()
	{
		if(savedClassUpdateTime==CMClass.getLastClassUpdatedTime())
			return;
		compiledCache.clear();
		looseCodesCache.clear();

		final List<SavedClass> tempSavedCharClasses=new LinkedList<SavedClass>();
		final List<SavedRace> tempSavedRaces=new LinkedList<SavedRace>();
		final TreeMap<String,Object> pclassList = new TreeMap<String,Object>();
		final TreeMap<String,Object> mclassList = new TreeMap<String,Object>();
		final TreeMap<String,Object> pbaseClassList = new TreeMap<String,Object>();
		final TreeMap<String,Object> mbaseClassList = new TreeMap<String,Object>();
		for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
		{
			final CharClass C=c.nextElement();
			final SavedClass sC = new SavedClass(C);
			tempSavedCharClasses.add(sC);
			pclassList.put(sC.plusNameStart,sC.name);
			mclassList.put(sC.minusNameStart,sC.name);
			pclassList.put(sC.plusIdStart,sC.name);
			mclassList.put(sC.minusIdStart,sC.name);
			pbaseClassList.put(sC.plusBaseClassStart, sC.baseClass);
			mbaseClassList.put(sC.minusBaseClassStart, sC.baseClass);
			looseCodesCache.put(sC.minusUpperName, new CompiledZapperMaskEntryImpl(ZapperKey.CLASS,new Object[] {sC.name}));
		}
		compiledCache.put(ZapperKey.CLASS, mclassList);
		compiledCache.put(ZapperKey._CLASS, pclassList);
		compiledCache.put(ZapperKey.ANYCLASS, mclassList);
		compiledCache.put(ZapperKey._ANYCLASS, pclassList);
		compiledCache.put(ZapperKey.BASECLASS, mbaseClassList);
		compiledCache.put(ZapperKey._BASECLASS, pbaseClassList);
		final TreeMap<String,Object> pWeaponClassList = new TreeMap<String,Object>();
		final TreeMap<String,Object> mWeaponClassList = new TreeMap<String,Object>();
		compiledCache.put(ZapperKey.WEAPONCLASS, mWeaponClassList);
		compiledCache.put(ZapperKey._WEAPONCLASS, pWeaponClassList);
		for(int w=0; w<Weapon.CLASS_DESCS.length; w++)
		{
			mWeaponClassList.put("-"+Weapon.CLASS_DESCS[w],new Integer(w));
			pWeaponClassList.put("+"+Weapon.CLASS_DESCS[w],new Integer(w));
		}
		final TreeMap<String,Object> pWeaponTypeList = new TreeMap<String,Object>();
		final TreeMap<String,Object> mWeaponTypeList = new TreeMap<String,Object>();
		compiledCache.put(ZapperKey.WEAPONTYPE, mWeaponTypeList);
		compiledCache.put(ZapperKey._WEAPONTYPE, pWeaponTypeList);
		for(int w=0; w<Weapon.TYPE_DESCS.length; w++)
		{
			mWeaponTypeList.put("-"+Weapon.TYPE_DESCS[w],new Integer(w));
			pWeaponTypeList.put("+"+Weapon.TYPE_DESCS[w],new Integer(w));
		}
		savedCharClasses=tempSavedCharClasses;

		final TreeMap<String,Object> praceList = new TreeMap<String,Object>();
		final TreeMap<String,Object> mraceList = new TreeMap<String,Object>();
		compiledCache.put(ZapperKey.RACE, mraceList);
		compiledCache.put(ZapperKey._RACE, praceList);
		final TreeMap<String,Object> praceCatList = new TreeMap<String,Object>();
		final TreeMap<String,Object> mraceCatList = new TreeMap<String,Object>();
		compiledCache.put(ZapperKey.RACECAT, mraceCatList);
		compiledCache.put(ZapperKey._RACECAT, praceCatList);
		for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
		{
			final Race R=r.nextElement();
			final SavedRace sR = new SavedRace(R);
			mraceList.put(sR.minusNameStart,sR.name);
			praceList.put(sR.plusNameStart,sR.name);
			mraceList.put(sR.minusIdStart,sR.name);
			praceList.put(sR.plusIdStart,sR.name);
			mraceCatList.put(sR.minusUpperCatName,sR.racialCategory);
			praceCatList.put(sR.plusUpperCatName,sR.racialCategory);
			tempSavedRaces.add(sR);
			looseCodesCache.put(sR.minusUpperName, new CompiledZapperMaskEntryImpl(ZapperKey.RACE,new Object[] {sR.name}));
		}
		savedRaces=tempSavedRaces;
		final TreeMap<String,Object> palignList = new TreeMap<String,Object>();
		final TreeMap<String,Object> malignList = new TreeMap<String,Object>();
		compiledCache.put(ZapperKey.ALIGNMENT, malignList);
		compiledCache.put(ZapperKey._ALIGNMENT, palignList);
		for(final Align a : Align.values())
		{
			palignList.put("+"+a.toString(),a.toString());
			malignList.put("-"+a.toString(),a.toString());
			looseCodesCache.put("-"+a.toString(), new CompiledZapperMaskEntryImpl(ZapperKey.ALIGNMENT,new Object[] {a.toString()}));
		}
		final TreeMap<String,Object> pgendList = new TreeMap<String,Object>();
		final TreeMap<String,Object> mgendList = new TreeMap<String,Object>();
		compiledCache.put(ZapperKey.GENDER, mgendList);
		compiledCache.put(ZapperKey._GENDER, pgendList);
		for(final String gend : new String[] {"MALE","FEMALE","NEUTER"})
		{
			pgendList.put("+"+gend,gend);
			mgendList.put("-"+gend,gend);
			looseCodesCache.put("-"+gend, new CompiledZapperMaskEntryImpl(ZapperKey.GENDER,new Object[] {gend.substring(0,1)}));
		}

		savedClassUpdateTime=CMClass.getLastClassUpdatedTime();
	}

	protected final TreeMap<String,CompiledZapperMaskEntryImpl> getLooseCodes()
	{
		synchronized(compiledCache)
		{
			final TreeMap<String,CompiledZapperMaskEntryImpl> looseCodes = looseCodesCache;
			if(savedClassUpdateTime!=CMClass.getLastClassUpdatedTime())
				buildSavedClasses();
			return looseCodes;
		}
	}

	protected final List<SavedClass> charClasses()
	{
		synchronized(compiledCache)
		{
			if(savedClassUpdateTime!=CMClass.getLastClassUpdatedTime())
				buildSavedClasses();
			return savedCharClasses;
		}
	}

	protected final List<SavedRace> races()
	{
		synchronized(compiledCache)
		{
			if(savedClassUpdateTime!=CMClass.getLastClassUpdatedTime())
				buildSavedClasses();
			return savedRaces;
		}
	}

	protected final TreeMap<String,Object> getCompiledCache(final ZapperKey key)
	{
		synchronized(compiledCache)
		{
			if(savedClassUpdateTime!=CMClass.getLastClassUpdatedTime())
				buildSavedClasses();
			return compiledCache.get(key);
		}
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
		return new CompiledZapperMaskImpl(new CompiledZMaskEntry[0][0], false, false);
	}

	protected Map<String,ZapperKey> getMaskCodes()
	{
		if(zapCodes.size()==0)
		{
			synchronized(zapCodes)
			{
				if(zapCodes.size()==0)
				{
					final Map<String,ZapperKey> newZapCodes = new HashMap<String,ZapperKey>();
					for(final ZapperKey Z : ZapperKey.values())
					{
						for(final String key : Z.keys())
							newZapCodes.put(key, Z);
					}
					this.zapCodes.putAll(newZapCodes);
				}
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

	protected CompiledZMaskEntry[] fixEntrySet(final CompiledZMaskEntry entry)
	{
		final CompiledZMaskEntry[] sset;
		if(entry.parms()[0] instanceof List)
		{
			@SuppressWarnings("unchecked")
			final
			ArrayList<CompiledZMaskEntry> subSet = (ArrayList<CompiledZMaskEntry>)entry.parms()[0];
			sset=subSet.toArray(new CompiledZMaskEntry[subSet.size()]);
			entry.parms()[0] = sset;
		}
		else
			sset = (CompiledZMaskEntry[])entry.parms();
		return sset;
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
			for(int d=0;d<Ability.ACODE.DESCS.size();d++)
			{
				if(Ability.ACODE.DESCS.get(d).equals(str))
				{
					o=Integer.valueOf(d);
					break;
				}
			}
		}
		if(o==null)
		{
			for(int d=0;d<Ability.DOMAIN.DESCS.size();d++)
			{
				if(Ability.DOMAIN.DESCS.get(d).equals(str))
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
				if(Ability.FLAG_DESCS[d].equals(str))
				{
					o=Long.valueOf(1L<<d);
					break;
				}
			}
		}
		if(o==null)
		{
			for(short d=0;d<Ability.QUALITY_DESCS.length;d++)
			{
				if(Ability.QUALITY_DESCS[d].equals(str))
				{
					o=Short.valueOf(d);
					break;
				}
			}
		}
		if(o==null)
		{
			for(int d=0;d<Ability.DOMAIN.DESCS.size();d++)
			{
				if(Ability.DOMAIN.DESCS.get(d).startsWith(str)||Ability.DOMAIN.DESCS.get(d).endsWith(str))
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
					o=Long.valueOf(1L<<d);
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
					o=Short.valueOf(d);
					break;
				}
			}
		}
		return o;
	}

	protected boolean checkLocation(final Environmental E, final MOB M, final Room R, final Object o, final Object p)
	{
		if(!(o instanceof LocationType))
			return false;
		switch((LocationType)o)
		{
		case CLANOWNED:
		{
			if(E instanceof MOB)
			{
				for(final Pair<Clan,Integer> C : M.clans())
				{
					if(CMLib.law().isLandOwnersName(C.first.getName(), R) || CMLib.law().isPropertyOwnersName(C.first.getName(), R))
						return true;
				}
			}
			else
			{
				final String str = CMLib.law().getPropertyOwnerName(R);
				if((str.length()>0)&&(CMLib.clans().getClanAnyHost(str)!=null))
					return true;
			}
			break;
		}
		case OWNED:
		{
			if(E instanceof MOB)
				return CMLib.law().doesOwnThisLand(M, R) || CMLib.law().doesOwnThisProperty(M, R);
			else
				return CMLib.law().getPropertyOwnerName(R).length()>0;
		}
		case PRIV:
		{
			return CMLib.law().doesHavePriviledgesHere(M, R);
		}
		case ROOMID:
		{
			final String roomID=CMLib.map().getExtendedRoomID(R).toLowerCase();
			if((p!=null)&&(roomID.startsWith(p.toString().toLowerCase())))
				return true;
			break;
		}
		case ROOMSTR:
		{
			if(p!=null)
			{
				final String subStr=p.toString();
				if(CMLib.english().containsString(R.displayText(M), subStr))
					return true;
				if(CMLib.english().containsString(R.description(M), subStr))
					return true;
			}
			break;
		}
		}
		return false;
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
		if(lvl.startsWith(c+">="))
			return new StringBuilder(append+"levels greater than or equal to "+lvl.substring(3).trim()+".  ");
		else
		if(lvl.startsWith(c+"<="))
			return new StringBuilder(append+"levels less than or equal to "+lvl.substring(3).trim()+".  ");
		else
		if(lvl.startsWith(c+">"))
			return new StringBuilder(append+"levels greater than "+lvl.substring(2).trim()+".  ");
		else
		if(lvl.startsWith(c+"<"))
			return new StringBuilder(append+"levels less than "+lvl.substring(2).trim()+".  ");
		else
		if(lvl.startsWith(c+"="))
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

	protected boolean matchesLooseCode(final String str)
	{
		final TreeMap<String,CompiledZapperMaskEntryImpl> looseCodes = getLooseCodes();
		if((str.startsWith("+")||str.startsWith("-"))
		&&(str.length()>0))
		{
			if(looseCodes.containsKey(str))
				return true;
			if(CMLib.factions().isRangeCodeName(str.substring(1)))
				return true;
			final String lKey = looseCodes.ceilingKey(str);
			if((lKey != null) && (lKey.startsWith(str)))
				return true;
			for(final String start : looseFinalLevels)
			{
				if(str.startsWith(start)
				&&CMath.isNumber(str.substring(start.length())))
					return true;
			}
		}
		return false;
	}

	protected int fromHereStartsWith(final List<String> lV, int v,
			final List<Object> parms, final ZapperKey key)
	{
		final TreeMap<String,Object> ps = this.getCompiledCache(key);
		for(int v2=v+1;v2<lV.size();v2++)
		{
			final String str2=lV.get(v2);
			if(str2.length()==0)
				continue;
			else
			if(zapCodes.containsKey(str2))
				return v2-1;
			else
			{
				final String foundKey = ps.ceilingKey(str2);
				if((foundKey != null)&&(foundKey.startsWith(str2)))
				{
					final Object val = ps.get(foundKey);
					if(!parms.contains(val))
						parms.add(val);
				}
				else
				{
					if((str2.startsWith("+")||str2.startsWith("-"))
					&&(!matchesLooseCode(str2)))
					{
						Log.errOut("Bad ZapperMask parm '"+str2+"' @ "+CMParms.combine(lV,0));
						return v2;
					}
					return v2-1;
				}
				v=lV.size();
			}
		}
		return v;
	}

	protected int fromHereStartsWith(final List<String> lV, int v,
			final StringBuilder str, final ZapperKey key)
	{
		final TreeMap<String,Object> ps = this.getCompiledCache(key);
		for(int v2=v+1;v2<lV.size();v2++)
		{
			final String str2=lV.get(v2);
			if(str2.length()==0)
				continue;
			else
			if(zapCodes.containsKey(str2))
				return v2-1;
			else
			{
				final String foundKey = ps.ceilingKey(str2);
				if((foundKey != null)&&(foundKey.startsWith(str2)))
				{
					final Object val = ps.get(foundKey);
					str.append(CMStrings.capitalizeAllFirstLettersAndLower(
							val.toString())).append(", ");
				}
				else
					return v2-1;
				v=lV.size();
			}
		}
		return v;
	}

	protected Faction.FRange getRange(final String s)
	{
		return CMLib.factions().getFactionRangeByCodeName(s);
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

	protected boolean multipleQuals(final List<String> V, final int v, final String startsWith)
	{
		return countQuals(V,v,startsWith)>1;
	}


	protected int appendCommaList(final StringBuilder buf, final List<String> V, int v, final String startChar)
	{
		for(int v2=v+1;v2<V.size();v2++)
		{
			final String str2=V.get(v2);
			if(zapCodes.containsKey(str2))
				return v2-1;
			if(str2.startsWith(startChar))
			{
				v=v2;
				buf.append(str2.substring(1).trim()+", ");
			}
		}
		if(buf.toString().endsWith(", "))
			buf.delete(buf.length()-2, buf.length());
		buf.append(".  ");
		return v;
	}

	protected int appendCommaList(final StringBuilder buf, final List<String> V, int v)
	{
		for(int v2=v+1;v2<V.size();v2++)
		{
			final String str2=V.get(v2);
			if(zapCodes.containsKey(str2))
				return v2-1;
			if(str2.startsWith("+")||str2.startsWith("-"))
			{
				v=v2;
				buf.append(CMath.s_int(str2.substring(1).trim())+", ");
			}
		}
		if(buf.toString().endsWith(", "))
			buf.delete(buf.length()-2, buf.length());
		buf.append(".  ");
		return v;
	}

	@Override
	public String[] parseMaskKeys(final String maskStr)
	{
		final List<String> keys = new ArrayList<String>();
		if(maskStr.trim().length()==0)
			return new String[0];
		final Map<String,ZapperKey> zapCodes=getMaskCodes();
		final List<String> V=CMParms.parse(maskStr.toUpperCase());
		for(int v=0;v<V.size();v++)
		{
			String str=V.get(v);
			if(zapCodes.containsKey(str))
			{
				final ZapperKey z = zapCodes.get(str);
				keys.add(str);
				switch(z)
				{
				case _LEVEL: // -Levels
				case _CLASSLEVEL: // -ClassLevels
				case _MAXCLASSLEVEL: // -MaxclassLevels
					while(++v<V.size())
					{
						str=V.get(v);
						if(str.startsWith("+>")||str.startsWith("+<")||str.startsWith("+="))
						{ /* keep looking */ }
						else
						{
							v--;
							break;
						}
					}
					break;
				case LEVEL: // +Levels
				case CLASSLEVEL: // +ClassLevels
				case MAXCLASSLEVEL: // +MaxclassLevels
					while(++v<V.size())
					{
						str=V.get(v);
						if(str.startsWith("->")||str.startsWith("-<")||str.startsWith("-="))
						{ /* keep looking */ }
						else
						{
							v--;
							break;
						}
					}
					break;
				case ANYCLASSLEVEL: // +anyclasslevel
				case _ANYCLASSLEVEL: // -anyclasslevel
				case CLANLEVEL: // +clanlevel
				case _CLANLEVEL: // -clanlevel
					while(++v<V.size())
					{
						str=V.get(v);
						if(zapCodes.containsKey(str))
						{
							v--;
							break;
						}
					}
					break;
				default:
					break;
				}
			}
			else
			{
				boolean found=false;
				// look @ it in context.. not enough ELSE
				for(final SavedClass C : charClasses())
				{
					if(C.minusNameStart.startsWith(str))
					{
						keys.add(str);
						found=true;
						break;
					}
				}
				if(!found)
				{
					for(final SavedRace R : races())
					{
						if(R.minusCatNameStart.startsWith(str))
						{
							keys.add(str);
							found=true;
							break;
						}
					}
				}
				if(!found)
				{
					if(getLooseCodes().containsKey(str))
					{
						keys.add(str);
						found=true;
					}

				}
				if(!found)
				{
					final String lKey = getLooseCodes().ceilingKey(str);
					if((lKey != null) && (lKey.startsWith(str)))
					{
						keys.add(str);
						found=true;
					}
				}
				if(!found)
				{
					if(str.startsWith("+>")||str.startsWith("+<")||str.startsWith("+=")
					||str.startsWith("->")||str.startsWith("-<")||str.startsWith("-="))
					{
						if((str.length()>3)&&(str.charAt(2)=='='))
							keys.add(str.substring(0,3));
						else
							keys.add(str.substring(0,2));
					}
				}
				if(!found)
				{
					if(str.startsWith("-"))
					{
						final Faction.FRange FR=getRange(str.substring(1));
						final String desc=CMLib.factions().rangeDescription(FR,"and ");
						if(desc.length()>0)
							keys.add(str);
					}
				}
			}
		}
		return keys.toArray(new String[keys.size()]);
	}
	@Override
	public String maskDesc(final String text, final boolean skipFirstWord)
	{
		if(text.trim().length()==0)
			return L("Anyone");
		final StringBuilder buf=new StringBuilder("");
		final Map<String,ZapperKey> zapCodes=getMaskCodes();
		final List<String> V=CMParms.parse(text.toUpperCase());
		for(int v=0;v<V.size();v++)
		{
			final String str=V.get(v);
			if(zapCodes.containsKey(str))
			{
				final ZapperKey key = zapCodes.get(str);
				switch(key)
				{
				case CLASS: // +class
					{
						buf.append(L("Disallows the following class"+(multipleQuals(V,v,"-")?"es":"")+": "));
						v=fromHereStartsWith(V,v,buf,key);
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case _CLASS: // -class
					{
						buf.append(L(skipFirstWord?"Only ":"Allows only "));
						v=fromHereStartsWith(V,v,buf,key);
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case BASECLASS: // +baseclass
					{
						buf.append(L("Disallows the following types"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=fromHereStartsWith(V,v,buf,key);
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case _BASECLASS: // -baseclass
					{
						buf.append(L(skipFirstWord?"Only ":"Allows only "));
						v=fromHereStartsWith(V,v,buf,key);
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case _RACE: // -Race
					{
						buf.append(L(skipFirstWord?"Only ":"Allows only "));
						v=fromHereStartsWith(V,v,buf,key);
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case _RACECAT: // -Racecats
					{
						buf.append(L(skipFirstWord?"Only these racial categor"+(multipleQuals(V,v,"+")?"ies":"y")+" ":"Allows only these racial categor"+(multipleQuals(V,v,"+")?"ies":"y")+" "));
						v=fromHereStartsWith(V,v,buf,key);
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case ALIGNMENT: // Alignment
					{
						buf.append(L("Disallows the following alignment"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=fromHereStartsWith(V,v,buf,key);
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case _ALIGNMENT: // -Alignment
					{
						buf.append(L(skipFirstWord?"Only ":"Allows only "));
						v=fromHereStartsWith(V,v,buf,key);
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case GENDER:
					{
						buf.append(L("Disallows the following gender"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=fromHereStartsWith(V,v,buf,key);
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case _GENDER: // -Gender
					{
						buf.append(L(skipFirstWord?"Only ":"Allows only "));
						v=fromHereStartsWith(V,v,buf,key);
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case _LEVEL: // -Levels
					{
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(str2.length()==0)
								continue;
							if(zapCodes.containsKey(str2))
								break;
							v=v2;
							buf.append(levelHelp(str2,'+',L(skipFirstWord?"Only ":"Allows only ")));
						}
					}
					break;
				case _CLASSLEVEL: // -ClassLevels
					{
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(str2.length()==0)
								continue;
							if(zapCodes.containsKey(str2))
								break;
							v=v2;
							buf.append(levelHelp(str2,'+',L(skipFirstWord?"Only class ":"Allows only class ")));
						}
					}
					break;
				case _MAXCLASSLEVEL: // -MaxclassLevels
					{
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(str2.length()==0)
								continue;
							if(zapCodes.containsKey(str2))
								break;
							v=v2;
							buf.append(levelHelp(str2,'+',L(skipFirstWord?"Only highest class ":"Allows only highest class ")));
						}
					}
					break;
				case LEVEL: // +Levels
					{
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(str2.length()==0)
								continue;
							if(zapCodes.containsKey(str2))
								break;
							v=v2;
							buf.append(levelHelp(str2,'-',L("Disallows ")));
						}
					}
					break;
				case CLASSLEVEL: // +ClassLevels
					{
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(str2.length()==0)
								continue;
							if(zapCodes.containsKey(str2))
								break;
							v=v2;
							buf.append(levelHelp(str2,'-',L("Disallows class ")));
						}
					}
					break;
				case MAXCLASSLEVEL: // +MaxclassLevels
					{
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(str2.length()==0)
								continue;
							if(zapCodes.containsKey(str2))
								break;
							v=v2;
							buf.append(levelHelp(str2,'+',L("Disallows highest class ")));
						}
					}
					break;
				case _CLASSTYPE: // -classtype
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" the following type"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case CLASSTYPE: // +classtype
					{
						buf.append(L("Disallows the following type"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _TATTOO: // -Tattoos
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" the following tattoo"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case TATTOO: // +Tattoos
					{
						buf.append(L("Disallows the following tattoo"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _WEAPONAMMO: // -WeaponAmmo
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" weapons that use: "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case WEAPONAMMO: // +weaponsmmo
					{
						buf.append(L("Disallows weapons that use : "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case LOCATION: // +location
					{
						buf.append(L((skipFirstWord?"":"Disallows")+" being at places like : "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _LOCATION: // -location
					{
						buf.append(L((skipFirstWord?"":"Requires")+" being at places like : "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case _MOOD: // -Mood
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" the following mood"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case MOOD: // +Mood
					{
						buf.append(L("Disallows the following mood"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _ACCCHIEVE: // -accchieves
					{
						buf.append(L((skipFirstWord?"The":"Requires the")+" following account achievement"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								v=v2;
								final AchievementLibrary.Achievement A=CMLib.achievements().getAchievement(str2.substring(1));
								if(A!=null)
									buf.append(A.getDisplayStr()+", ");
								else
									buf.append(str2.substring(1)+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
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
								v=v2;
								final AchievementLibrary.Achievement A=CMLib.achievements().getAchievement(str2.substring(1));
								if(A!=null)
									buf.append(A.getDisplayStr()+", ");
								else
									buf.append(str2.substring(1)+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case _SECURITY: // -Security
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" following security flag"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case SECURITY: // +security
					{
						buf.append(L("Disallows the following security flag"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
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
								{
									v=v2;
									buf.append(E.name()+", ");
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case _OR: // -or
					buf.append(L("-OR other than the following:  "));
					break;
				case OR: // -or
					buf.append(L("-OR-  "));
					break;
				case _RIDE: // -ride
					buf.append(L((skipFirstWord?"The":"Requires")+" followed matches: "));
					break;
				case RIDE: // +ride
					buf.append(L("Disallows riding matches: "));
					break;
				case _FOLLOW: // -follow
					buf.append(L((skipFirstWord?"The":"Requires")+" followed matches: "));
					break;
				case FOLLOW: // +follow
					buf.append(L("Disallows riding matches: "));
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
								{
									v=v2;
									buf.append(E.name()+", ");
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
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
								v=v2;
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
							buf.delete(buf.length()-2, buf.length());
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
								v=v2;
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
							buf.delete(buf.length()-2, buf.length());
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
								v=v2;
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
							buf.delete(buf.length()-2, buf.length());
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
								v=v2;
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
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case _CLAN: // -Clan
					{
						buf.append(L((skipFirstWord?"M":"Requires m")+"embership in the following clan"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case CLAN: // +Clan
					{
						buf.append(L("Disallows the following clan"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
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
								v=v2;
								final int code=CMLib.materials().findMaterialCode(str2.substring(1),false);
								if(code>=0)
									buf.append(RawMaterial.Material.findByMask(code).noun()+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
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
								v=v2;
								final int code=CMLib.materials().findMaterialCode(str2.substring(1),false);
								if(code>=0)
									buf.append(RawMaterial.Material.findByMask(code).noun()+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
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
								v=v2;
								final long code=Wearable.CODES.FIND_endsWith(str2.substring(1));
								if(code>=0)
									buf.append(Wearable.CODES.NAME(code)+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
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
								v=v2;
								final long code=Wearable.CODES.FIND_endsWith(str2.substring(1));
								if(code>=0)
									buf.append(Wearable.CODES.NAME(code)+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
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
								v=v2;
								final int code=CMLib.flags().getSensesIndex(str2.substring(1));
								if(code>=0)
									buf.append(PhyStats.CAN_SEE_DESCS[code]+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
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
								v=v2;
								final int code=CMLib.flags().getSensesIndex(str2.substring(1));
								if(code>=0)
									buf.append(PhyStats.CAN_SEE_DESCS[code]+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case HOUR: // +HOUR
					{
						buf.append(L("Disallowed during the following time"+(multipleQuals(V,v,"-")?"s":"")+" of the day: "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _HOUR: // -HOUR
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"during the following time"+(multipleQuals(V,v,"+")?"s":"")+" of the day: "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case PORT: // +PORT
					{
						buf.append(L("Disallowed from the following ports: "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _PORT: // -PORT
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"from the following ports: "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case BIRTHSEASON: // +birthseason
				case SEASON: // +season
					{
						if(key == ZapperKey.SEASON)
							buf.append(L("Disallowed during the following season"+(multipleQuals(V,v,"-")?"s":"")+": "));
						else
							buf.append(L("Disallowing those born during the following season"+(multipleQuals(V,v,"-")?"s":"")+": "));
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
									{
										v=v2;
										buf.append(TimeClock.Season.values()[season].toString()+", ");
									}
								}
								else
								{
									final int season=determineSeasonCode(str2.substring(1).trim());
									if((season>=0)&&(season<TimeClock.Season.values().length))
									{
										v=v2;
										buf.append(TimeClock.Season.values()[season].toString()+", ");
									}
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case _BIRTHSEASON: // -birthseason
				case _SEASON: // -season
					{
						if(key == ZapperKey._SEASON)
							buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"during the following season"+(multipleQuals(V,v,"+")?"s":"")+": "));
						else
							buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"those born during the following season"+(multipleQuals(V,v,"+")?"s":"")+": "));
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
									{
										v=v2;
										buf.append(TimeClock.Season.values()[season].toString()+", ");
									}
								}
								else
								{
									final int season=determineSeasonCode(str2.substring(1).trim());
									if((season>=0)&&(season<TimeClock.Season.values().length))
									{
										v=v2;
										buf.append(TimeClock.Season.values()[season].toString()+", ");
									}
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
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
									{
										v=v2;
										buf.append(CMStrings.capitalizeAndLower(Climate.WEATHER_DESCS[weather])+", ");
									}
								}
								else
								{
									final int weather=CMParms.indexOf(Climate.WEATHER_DESCS,str2.substring(1).toUpperCase().trim());
									if((weather>=0)&&(weather<Climate.WEATHER_DESCS.length))
									{
										v=v2;
										buf.append(CMStrings.capitalizeAndLower(Climate.WEATHER_DESCS[weather])+", ");
									}
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
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
									{
										v=v2;
										buf.append(CMStrings.capitalizeAndLower(Climate.WEATHER_DESCS[weather])+", ");
									}
								}
								else
								{
									final int weather=CMParms.indexOf(Climate.WEATHER_DESCS,str2.substring(1).toUpperCase().trim());
									if((weather>=0)&&(weather<Climate.WEATHER_DESCS.length))
									{
										v=v2;
										buf.append(CMStrings.capitalizeAndLower(Climate.WEATHER_DESCS[weather])+", ");
									}
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case DOMAIN: // +domain
					{
						buf.append(L("Disallowed in the following locale"+(multipleQuals(V,v,"-")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("-"))
							{
								final String str3=str2.substring(1).trim().toUpperCase();
								if(CMath.isInteger(str3))
								{
									final int domain=CMath.s_int(str3);
									if((domain>=0)&&(domain<Room.DOMAIN_OUTDOOR_DESCS.length))
									{
										v=v2;
										buf.append(CMStrings.capitalizeAndLower(Room.DOMAIN_OUTDOOR_DESCS[domain])+", ");
									}
									else
									if((domain>=Room.INDOORS)&&((domain-Room.INDOORS)<Room.DOMAIN_INDOORS_DESCS.length))
									{
										v=v2;
										buf.append(CMStrings.capitalizeAndLower(Room.DOMAIN_INDOORS_DESCS[domain-Room.INDOORS])+", ");
									}
								}
								else
								{
									int domain=CMParms.indexOf(Room.DOMAIN_OUTDOOR_DESCS,str3);
									if(domain < 0)
										domain = CMParms.indexOf(Room.DOMAIN_INDOORS_DESCS,str3);
									if(domain>=0)
									{
										v=v2;
										buf.append(CMStrings.capitalizeAndLower(str3)+", ");
									}
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case _DOMAIN: // -domain
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"in the following locale"+(multipleQuals(V,v,"+")?"s":"")+": "));
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
								break;
							if(str2.startsWith("+"))
							{
								final String str3=str2.substring(1).trim().toUpperCase();
								if(CMath.isInteger(str3))
								{
									final int domain=CMath.s_int(str3);
									if((domain>=0)&&(domain<Room.DOMAIN_OUTDOOR_DESCS.length))
									{
										v=v2;
										buf.append(CMStrings.capitalizeAndLower(Room.DOMAIN_OUTDOOR_DESCS[domain])+", ");
									}
									else
									if((domain>=Room.INDOORS)&&((domain-Room.INDOORS)<Room.DOMAIN_INDOORS_DESCS.length))
									{
										v=v2;
										buf.append(CMStrings.capitalizeAndLower(Room.DOMAIN_INDOORS_DESCS[domain-Room.INDOORS])+", ");
									}
								}
								else
								{
									int domain=CMParms.indexOf(Room.DOMAIN_OUTDOOR_DESCS,str3);
									if(domain < 0)
										domain = CMParms.indexOf(Room.DOMAIN_INDOORS_DESCS,str3);
									if(domain>=0)
									{
										v=v2;
										buf.append(CMStrings.capitalizeAndLower(str3)+", ");
									}
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case BIRTHDAY: //+birthday
					{
						buf.append(L("Disallow those born on the following day"+(multipleQuals(V,v,"-")?"s":"")+" of the month: "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _BIRTHDAY: // -birthday
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"those born on the following day"+(multipleQuals(V,v,"+")?"s":"")+" of the month: "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case _BIRTHDAYOFYEAR: // -birthdayofyear
					{
						buf.append(L("Disallowing those born on the following day"+(multipleQuals(V,v,"-")?"s":"")+" of the year: "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case BIRTHDAYOFYEAR: // +birthdayofyear
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"those born on the following day"+(multipleQuals(V,v,"+")?"s":"")+" of the year: "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case _BIRTHWEEK: // -birthweek
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"those born in the following week"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case BIRTHWEEK: // +birthweek
					{
						buf.append(L("Disallowing those born in the following week"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _BIRTHWEEKOFYEAR: // -birthweekofyear
					{
						buf.append(L("Disallowing those born during the following week"+(multipleQuals(V,v,"-")?"s":"")+" of the year: "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case BIRTHWEEKOFYEAR: // +birthweekofyear
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"those born during the following week"+(multipleQuals(V,v,"+")?"s":"")+" of the year: "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case _BIRTHYEAR: // -birthyear
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"those born during the following year"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case BIRTHYEAR: // +birthyear
					{
						buf.append(L("Disallowing those born during the following year"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _BIRTHMONTH: // -birthmonth
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"those born in the following month"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case BIRTHMONTH: // +birthmonth
					{
						buf.append(L("Disallowed for those born in the following month"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case MONTH: // +month
					{
						buf.append(L("Disallowed during the following month"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _MONTH: // -month
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"during the following month"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case WEEK: // +week
					{
						buf.append(L("Disallowed during the following week"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _WEEK: // -week
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"during the following week"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case WEEKOFYEAR: // +weekofyear
					{
						buf.append(L("Disallowed during the following week"+(multipleQuals(V,v,"-")?"s":"")+" of the year: "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _WEEKOFYEAR: // -weekofyear
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"during the following week"+(multipleQuals(V,v,"+")?"s":"")+" of the year: "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case YEAR: // +year
					{
						buf.append(L("Disallowed during the following year"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _YEAR: // -year
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"during the following year"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case DAY: // +day
					{
						buf.append(L("Disallowed during the following day"+(multipleQuals(V,v,"-")?"s":"")+" of the month: "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _DAY: // -day
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"on the following day"+(multipleQuals(V,v,"+")?"s":"")+" of the month: "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case DAYOFYEAR: // +dayofyear
					{
						buf.append(L("Disallowed during the following day"+(multipleQuals(V,v,"-")?"s":"")+" of the year: "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _DAYOFYEAR: // -dayofyear
					{
						buf.append(L((skipFirstWord?"Only ":"Allowed only ")+"on the following day"+(multipleQuals(V,v,"+")?"s":"")+" of the year: "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case QUALLVL: // +quallvl
					if((v+1)<V.size())
					{
						final Ability A=CMClass.getAbility(V.get(v+1));
						if(A!=null)
						{
							v++;
							int adjustment=0;
							if(((v+1)<V.size())&&(CMath.isInteger(V.get(v+1))))
							{
								adjustment=CMath.s_int(V.get(v+1));
								v++;
							}
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
							v++;
							int adjustment=0;
							if(((v+1)<V.size())&&(CMath.isInteger(V.get(v+1))))
							{
								adjustment=CMath.s_int(V.get(v+1));
								v++;
							}
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
								{
									v=v2;
									buf.append(PhyStats.IS_DESCS[code]+", ");
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
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
								{
									v=v2;
									buf.append(PhyStats.IS_DESCS[code]+", ");
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
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
								final int code=CMLib.materials().findResourceCode(str2.substring(1),false);
								if(code>=0)
								{
									buf.append(CMStrings.capitalizeAndLower(RawMaterial.CODES.NAME(code))+", ");
									v=v2;
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
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
								final int code=CMLib.materials().findResourceCode(str2.substring(1),false);
								if(code>=0)
								{
									v=v2;
									buf.append(CMStrings.capitalizeAndLower(RawMaterial.CODES.NAME(code))+", ");
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case _JAVACLASS: // -JavaClass
					{
						buf.append(L((skipFirstWord?"B":"Requires b")+"eing of the following type"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case JAVACLASS: // +JavaClass
					{
						buf.append(L("Disallows being of the following type"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _DEITY: // -Deity
					{
						buf.append(L((skipFirstWord?"W":"Requires w")+"orshipping the following deity"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case DEITY: // +Deity
					{
						buf.append(L("Disallows the worshippers of: "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case NAME: // +Names
					{
						buf.append(L("Disallows the following mob/player name"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _NAME: // -Names
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" following name"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case ACCOUNT: // +Account
					{
						buf.append(L("Disallows the following player account"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _ACCOUNT: // -Account
					{
						buf.append(L((skipFirstWord?"The":"Requires")+" following player account"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
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
								v=v2;
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
							buf.delete(buf.length()-2, buf.length());
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
								v=v2;
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
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case _PLAYER: // -Player
					buf.append(L("Disallows players.  "));
					break;
				case _NPC: // -MOB
					buf.append(L("Disallows mobs/npcs.  "));
					break;
				case PLAYER: // +Player
					buf.append(L("Allows players.  "));
					break;
				case NPC: // +MOB
					buf.append(L("Allows mobs/npcs.  "));
					break;
				case RACE: // +races
					{
						buf.append(L("Disallows the following race"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case RACECAT: // +racecats
					{
						buf.append(L("Disallows the following racial category"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
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
								final StringBuilder lvlHelp = levelHelp(V.get(v2),'+',L(skipFirstWord?"Only "+className+" ":"Allows only "+className+" "));
								if(lvlHelp.length()>0)
								{
									v=v2;
									buf.append(lvlHelp);
									checkForClass = false;
									found=true;
								}
							}
							if(checkForClass)
							{
								for(final SavedClass C : charClasses())
								{
									if(C.plusNameStart.startsWith(s))
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
								final StringBuilder lvlHelp = levelHelp(V.get(v2),'-',"Disallows "+className+" ");
								if(lvlHelp.length()>0)
								{
									v=v2;
									buf.append(lvlHelp);
									checkForClass = false;
									found = true;
								}
							}
							if(checkForClass)
							{
								for(final SavedClass C : charClasses())
								{
									if(C.minusNameStart.startsWith(s))
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
				case _CLANLEVEL: // -clanlevel
					{
						String clanName = "";
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String s = V.get(v2);
							if(getMaskCodes().containsKey(s))
								break;
							boolean getClan = true;
							boolean found=false;
							if(clanName.length()>0)
							{
								final StringBuilder lvlHelp = levelHelp(V.get(v2),'+',L(skipFirstWord?"Only "+clanName+" ":"Allows only "+clanName+" "));
								if(lvlHelp.length()>0)
								{
									v=v2;
									buf.append(lvlHelp);
									getClan = false;
									found=true;
								}
							}
							if(getClan)
							{
								if((s.length()>1)&&(s.charAt(0)=='+'))
								{
									clanName = s.substring(1);
									found=true;
								}
							}
							if(!found)
								break;
						}
					}
					break;
				case CLANLEVEL: // +clanlevel
					{
						String clanName = "";
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String s = V.get(v2);
							if(getMaskCodes().containsKey(s))
							{
								v=v2-1;
								break;
							}
							boolean getClan = true;
							boolean found=false;
							if(clanName.length()>0)
							{
								final StringBuilder lvlHelp = levelHelp(V.get(v2),'-',"Disallows "+clanName+" ");
								if(lvlHelp.length()>0)
								{
									v=v2;
									buf.append(lvlHelp);
									getClan = false;
									found = true;
								}
							}
							if(getClan)
							{
								if((s.length()>1) && (s.charAt(0)=='-'))
								{
									clanName = s.substring(1);
									found = true;
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
						v=fromHereStartsWith(V,v,buf,key);
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case ANYCLASS: // +anyclass
					{
						buf.append(L("Disallows any levels in any of the following:  "));
						v=fromHereStartsWith(V,v,buf,key);
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case ADJSTRENGTH: // +adjstr
					buf.append(L((skipFirstWord?"A":"Requires a")+" strength of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case ADJINTELLIGENCE: // +adjint
					buf.append(L((skipFirstWord?"An":"Requires an")+" intelligence of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case ADJWISDOM: // +adjwis
					buf.append(L((skipFirstWord?"A":"Requires a")+" wisdom of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case ADJDEXTERITY: // +adjdex
					buf.append(L((skipFirstWord?"A":"Requires a")+" dexterity of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case ADJCONSTITUTION: // -adjcha
					buf.append(L((skipFirstWord?"A":"Requires a")+" constitution of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case ADJCHARISMA: // +adjcha
					buf.append(L((skipFirstWord?"A":"Requires a")+" charisma of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _ADJSTRENGTH: // -adjstr
					buf.append(L((skipFirstWord?"A":"Requires a")+" strength of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _ADJINTELLIGENCE: // -adjint
					buf.append(L((skipFirstWord?"An":"Requires an")+" intelligence of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _ADJWISDOM: // -adjwis
					buf.append(L((skipFirstWord?"A":"Requires a")+" wisdom of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _ADJDEXTERITY: // -adjdex
					buf.append(L((skipFirstWord?"A":"Requires a")+" dexterity of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _ADJCONSTITUTION: // -adjcon
					buf.append(L((skipFirstWord?"A":"Requires a")+" constitution of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _ADJCHARISMA: // -adjcha
					buf.append(L((skipFirstWord?"A":"Requires a")+" charisma of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case STRENGTH: // +str
					buf.append(L((skipFirstWord?"A":"Requires a")+" base strength of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case INTELLIGENCE: // +int
					buf.append(L((skipFirstWord?"A":"Requires a")+" base intelligence of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case WISDOM: // +wis
					buf.append(L((skipFirstWord?"A":"Requires a")+" base wisdom of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case DEXTERITY: // +dex
					buf.append(L((skipFirstWord?"A":"Requires a")+" base dexterity of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case CONSTITUTION: // +con
					buf.append(L((skipFirstWord?"A":"Requires a")+" base constitution of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case CHARISMA: // +cha
					buf.append(L((skipFirstWord?"A":"Requires a")+" base charisma of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _STRENGTH: // -str
					buf.append(L((skipFirstWord?"A":"Requires a")+" base strength of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _INTELLIGENCE: // -int
					buf.append(L((skipFirstWord?"A":"Requires a")+" base intelligence of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _WISDOM: // -wis
					buf.append(L((skipFirstWord?"A":"Requires a")+" base wisdom of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _DEXTERITY: // -dex
					buf.append(L((skipFirstWord?"A":"Requires a")+" base dexterity of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _CONSTITUTION: // -con
					buf.append(L((skipFirstWord?"A":"Requires a")+" base constitution of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _CHARISMA: // -cha
					buf.append(L((skipFirstWord?"A":"Requires a")+" base charisma of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _CHANCE: // -chance
					buf.append(L((skipFirstWord?"":"Allowed ")+" "+(100-(((++v)<V.size())?CMath.s_int(V.get(v)):0))+"% of the time.  "));
					break;
				case ABILITY: // +ability
					buf.append(L((skipFirstWord?"A":"Requires a")+" magic/ability of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _ABILITY: // -ability
					buf.append(L((skipFirstWord?"A":"Requires a")+" magic/ability of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case VALUE: // +value
					buf.append(L((skipFirstWord?"A":"Requires a")+" value of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _VALUE: // -value
					buf.append(L((skipFirstWord?"A":"Requires a")+" value of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case WEIGHT: // +weight
					buf.append(L((skipFirstWord?"A":"Requires a")+" weight/encumbrance of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _WEIGHT: // -weight
					buf.append(L((skipFirstWord?"A":"Requires a")+" weight/encumbrance of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case ARMOR: // +armor
					buf.append(L((skipFirstWord?"A":"Requires a")+" armor rating of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _ARMOR: // -armor
					buf.append(L((skipFirstWord?"A":"Requires a")+" armor rating of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case DAMAGE: // +damage
					buf.append(L((skipFirstWord?"A":"Requires a")+" damage ability of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _DAMAGE: // -damage
					buf.append(L((skipFirstWord?"A":"Requires a")+" damage ability of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case ATTACK: // +attack
					buf.append(L((skipFirstWord?"An":"Requires an")+" attack bonus of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _ATTACK: // -attack
					buf.append(L((skipFirstWord?"An":"Requires an")+" attack bonus of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case AREA: // +Area
					{
						buf.append(L("Disallows the following area"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _AREA: // -Area
					{
						buf.append(L((skipFirstWord?"The":"Requires the")+" following area"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case _PARENTAREA:
					{
						buf.append(L((skipFirstWord?"The":"Requires the")+" following general area"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case PARENTAREA:
					{
						buf.append(L("Disallows the following general area"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case AREABLURB: // +Areablurb
					{
						buf.append(L("Disallows the following area blurb"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _AREABLURB: // -Areablurb
					{
						buf.append(L((skipFirstWord?"The":"Requires the")+" following area blurb"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case ISHOME: // +isHome
					buf.append(L("Disallows those who are not in their home area.  "));
					break;
				case _ISHOME: // -isHome
					buf.append(L("Disallows those who are in their home area.  "));
					break;
				case AREAINSTANCE: // +areainstance
					buf.append(L("Disallows those who are not in an area instance.  "));
					break;
				case _AREAINSTANCE: // -areainstance
					buf.append(L("Disallows those who are in an area instance.  "));
					break;
				case HOME: // +Home
					{
						buf.append(L("Disallows those whose home is the following area"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case PLANE: // +Plane
					{
						buf.append(L("Disallows those whose are on the following plane"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _HOME: // -Home
					{
						buf.append(L((skipFirstWord?"From the":"Requires being from the")+" following area"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case _PLANE: // -Plane
					{
						buf.append(L((skipFirstWord?"On the":"Requires being on the")+" following plane"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case _IFSTAT:
					{
						buf.append(L("Allows only those with "+(multipleQuals(V,v,"-")?"one of the following values":"the following value")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case IFSTAT:
					{
						buf.append(L("Disallows those with "+(multipleQuals(V,v,"-")?"one of the following values":"the following value")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case SUBNAME:
					{
						buf.append(L("Disallows those with the following partial name"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _SUBNAME:
					{
						buf.append(L("Allows only those with the following partial name"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"+");
					}
					break;
				case ITEM: // +Item
					{
						buf.append(L("Disallows those with the following item"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case WORN:
					{
						buf.append(L((skipFirstWord?"The":"Requires the")+" following worn item"+(multipleQuals(V,v,"-")?"s":"")+": "));
						v=appendCommaList(buf,V,v,"-");
					}
					break;
				case _ITEM:
					{
						buf.append(L((skipFirstWord?"H":"Requires h")+"aving the following item"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v);
					}
					break;
				case _WORN: // -Worn
					{
						buf.append(L((skipFirstWord?"W":"Requires w")+"earing the following item"+(multipleQuals(V,v,"+")?"s":"")+": "));
						v=appendCommaList(buf,V,v);
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
								v=v2;
								final Ability A=CMClass.getAbility(str2.substring(1));
								if(A!=null)
									buf.append(A.name()+", ");
								else
									buf.append(str2.substring(1)+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
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
								v=v2;
								final Ability A=CMClass.getAbility(str2.substring(1));
								if(A!=null)
									buf.append(A.name()+", ");
								else
									buf.append(str2.substring(1)+", ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
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
									{
										v=v2;
										buf.append(desc+"; ");
									}
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						if(buf.toString().endsWith("; "))
							buf.delete(buf.length()-2, buf.length());
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
									{
										v=v2;
										buf.append(desc+"; ");
									}
								}
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						if(buf.toString().endsWith("; "))
							buf.delete(buf.length()-2, buf.length());
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
							{
								v=v2;
								buf.append(str2.substring(1).toUpperCase().trim()).append(" ");
							}
						}
						if(buf.toString().endsWith(", "))
							buf.delete(buf.length()-2, buf.length());
						if(buf.toString().endsWith("; "))
							buf.delete(buf.length()-2, buf.length());
						buf.append(".  ");
					}
					break;
				case _GROUPSIZE: // -groupsize
					buf.append(L((skipFirstWord?"A":"Requires a")+" group size of at most @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case GROUPSIZE: // +groupsize
					buf.append(L((skipFirstWord?"A":"Requires a")+" group size of at least @x1.  ",((++v)<V.size())?V.get(v):"0"));
					break;
				case _IF: // -if
					buf.append(L((skipFirstWord?"n":"Requires n")+"ot meeting the following condition(s):"));
					for(int v2=v+1;v2<V.size();v2++)
					{
						final String str2=V.get(v2);
						if(zapCodes.containsKey(str2))
							break;
						v=v2;
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
						v=v2;
						buf.append(str2).append(" ");
					}
					break;
				case OFFICER:
					buf.append(L((skipFirstWord?"Being":"Requires being")+" an officer of the law "));
					break;
				case _OFFICER:
					buf.append(L((skipFirstWord?"Not":"Disallows being")+" an officer of the law "));
					break;
				case JUDGE:
					buf.append(L((skipFirstWord?"Being":"Requires being")+" a legal judge "));
					break;
				case _JUDGE:
					buf.append(L((skipFirstWord?"Not":"Disallows being")+" a lega judge "));
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
				if(str.startsWith("-"))
				{
					CompiledZapperMaskEntryImpl i = getLooseCodes().get(str);
					if(i != null)
						buf.append(L("Disallows @x1.  ",i.parms[0].toString().toLowerCase()));
					else
					{
						boolean found=false;
						for(final String s : getLooseCodes().keySet())
						{
							if(s.startsWith(str))
							{
								i = getLooseCodes().get(s);
								buf.append(L("Disallows @x1.  ",i.parms[0].toString().toLowerCase()));
								found=true;
							}
						}
						if(!found)
						{
							buf.append(levelHelp(str,'-',L("Disallows ")));
							final Faction.FRange FR=getRange(str.substring(1));
							final String desc=CMLib.factions().rangeDescription(FR,"and ");
							if(desc.length()>0)
								buf.append(L("Disallows ")+desc);
						}
					}
				}
			}
		}

		if(buf.length()==0)
			buf.append(L("Anyone."));
		return buf.toString();
	}

	protected final boolean isDateMatch(final Object o, final int num)
	{
		if(o instanceof Integer)
		{
			if(num==((Integer)o).intValue())
				return true;
		}
		else
		if(o instanceof Triad)
		{
			@SuppressWarnings("unchecked")
			final Triad<Integer,Integer,String> p=(Triad<Integer,Integer,String>)o;
			if((num % p.second.intValue())==p.first.intValue())
				return true;
		}
		else
		if(o instanceof Pair)
		{
			@SuppressWarnings("unchecked")
			final Pair<Integer,Integer> p=(Pair<Integer,Integer>)o;
			if((int)Math.round(CMath.floor(CMath.div(num,p.second.intValue())))==p.first.intValue())
				return true;
		}
		return false;
	}

	protected final void addDateValues(final Object o, final List<Integer> vals, final int min, final int max)
	{
		if(o instanceof Integer)
			vals.add((Integer)o);
		else
		if(o instanceof Triad)
		{
			@SuppressWarnings("unchecked")
			final Triad<Integer,Integer,String> p=(Triad<Integer,Integer,String>)o;
			if(min == 0)
			{
				for(int i=p.first.intValue();i<=max;i+=p.second.intValue())
					vals.add(Integer.valueOf(i));
			}
			else
			{
				int firstVal = min - (min % p.second.intValue()) + p.first.intValue() ;
				if(firstVal <= min)
					firstVal += p.second.intValue();
				for(int i=firstVal;i<=max;i+=p.second.intValue())
					vals.add(Integer.valueOf(i));
			}
		}
		else
		if(o instanceof Pair)
		{
			@SuppressWarnings("unchecked")
			final Pair<Integer,Integer> p=(Pair<Integer,Integer>)o;
			vals.add(Integer.valueOf(min+(p.second.intValue() * p.first.intValue())));
		}
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
			if(this.matchesLooseCode(str))
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
					//?!
					break;
				case _BASECLASS: // huh?
					//?!
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
		for(final CompiledZMaskEntry[] entries : cset.entries())
		{
			for(final CompiledZMaskEntry entry : entries)
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
				case LEVEL: // +level
					{
						for(int v=0;v<entry.parms().length-1;v+=2)
						{
							switch((ZapperKey)entry.parms()[v])
							{
							case LVLGR: // -lvlgr
								level=minMinLevel;
								break;
							case LVLGE: // -lvlge
								level=minMinLevel;
								break;
							case LVLLT: // -lvlt
								level=((Integer)entry.parms()[v+1]).intValue();
								break;
							case LVLLE: // -lvlle
								level=((Integer)entry.parms()[v+1]).intValue()+1;
								break;
							case LVLEQ: // -lvleq
								level=minMinLevel;
								break;
							default:
								break;
							}
						}
					}
					break;
				case CLASSLEVEL: // +classlevel
					{
						for(int v=0;v<entry.parms().length-1;v+=2)
						{
							switch((ZapperKey)entry.parms()[v])
							{
							case LVLGR: // -lvlgr
								level=minMinLevel;
								break;
							case LVLGE: // -lvlge
								level=minMinLevel;
								break;
							case LVLLT: // -lvlt
								level=((Integer)entry.parms()[v+1]).intValue();
								break;
							case LVLLE: // -lvlle
								level=((Integer)entry.parms()[v+1]).intValue()+1;
								break;
							case LVLEQ: // -lvleq
								level=minMinLevel;
								break;
							default:
								break;
							}
						}
					}
					break;
				case MAXCLASSLEVEL: // +maxclasslevel
					{
						for(int v=0;v<entry.parms().length-1;v+=2)
						{
							switch((ZapperKey)entry.parms()[v])
							{
							case LVLGR: // -lvlgr
								level=minMinLevel;
								break;
							case LVLGE: // -lvlge
								level=minMinLevel;
								break;
							case LVLLT: // -lvlt
								level=((Integer)entry.parms()[v+1]).intValue();
								break;
							case LVLLE: // -lvlle
								level=((Integer)entry.parms()[v+1]).intValue()+1;
								break;
							case LVLEQ: // -lvleq
								level=minMinLevel;
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
		}
		return level;
	}

	@Override
	public CompiledZMask maskCompile(final String text)
	{
		final ArrayList<ArrayList<CompiledZMaskEntry>> bufs=new ArrayList<ArrayList<CompiledZMaskEntry>>();
		if((text==null)||(text.trim().length()==0))
			return emptyZMask;
		ArrayList<CompiledZMaskEntry> buf=new ArrayList<CompiledZMaskEntry>();
		bufs.add(buf);
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
						v = fromHereStartsWith(V, v, parms, entryType);
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case CLASS: // +class
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						v = fromHereStartsWith(V, v, parms, entryType);
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
								final String ancestorStr = CMClass.findTypeAncestor(possClassName);
								Class<?> ancestorC = null;
								if((ancestorStr != null)&&(ancestorStr.length()>0))
								{
									try
									{
										ancestorC = Class.forName(ancestorStr);
									}
									catch(final Exception e)
									{
									}
								}
								if(ancestorC == null)
								{
									try
									{
										ancestorC = Class.forName(possClassName);
									}
									catch(final Exception e)
									{
									}
									if((ancestorC == null)
									&&(possClassName.indexOf('.')<0))
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
										for(final String prefix : prefixes)
										{
											try
											{
												ancestorC = Class.forName(prefix+possClassName);
												break;
											}
											catch(final Throwable e2)
											{
											}
										}
									}
								}
								if(ancestorC != null)
								{
									parms.add(ancestorC);
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
						v = fromHereStartsWith(V, v, parms, entryType);
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case BASECLASS: // +baseclass
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						v = fromHereStartsWith(V, v, parms, entryType);
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case WEAPONTYPE: // +weapontype
				case WEAPONCLASS: // +weaponclass
				case _WEAPONTYPE: // -weapontype
				case _WEAPONCLASS: // -weaponclass
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						v = fromHereStartsWith(V, v, parms, entryType);
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _RACE: // -Race
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						v = fromHereStartsWith(V, v, parms, entryType);
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _RACECAT: // -Racecats
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						v = fromHereStartsWith(V, v, parms, entryType);
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case RACECAT: // +Racecats
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						v = fromHereStartsWith(V, v, parms, entryType);
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case RACE: // +Race
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						v = fromHereStartsWith(V, v, parms, entryType);
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case ALIGNMENT: // +Alignment
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						v = fromHereStartsWith(V, v, parms, entryType);
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _ALIGNMENT: // -Alignment
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						v = fromHereStartsWith(V, v, parms, entryType);
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _GENDER: // -Gender
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						v = fromHereStartsWith(V, v, parms, entryType);
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case GENDER: // +Gender
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						v = fromHereStartsWith(V, v, parms, entryType);
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
				case LEVEL: // +Levels
				case CLASSLEVEL: // +ClassLevels
				case MAXCLASSLEVEL: // +MaxclassLevels
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
							final CompiledZMaskEntry e = levelCompiledHelper(str2,'-');
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
									final CharClass C=CMClass.getCharClass(charClassC.id);
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
								if(plusMinus=='+')
								{
									for(final SavedClass C : charClasses())
									{
										if(C.plusNameStart.startsWith(str2))
										{
											charClassC = C;
											found=true;
											break;
										}
									}
								}
								else
								{
									for(final SavedClass C : charClasses())
									{
										if(C.minusNameStart.startsWith(str2))
										{
											charClassC = C;
											found=true;
											break;
										}
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

				case CLANLEVEL: // +clanlevel
				case _CLANLEVEL: // -clanlevel
					{
						final char plusMinus = (entryType == ZapperKey.CLANLEVEL) ? '-' : '+';
						String clanName = null;
						final ArrayList<Object> parms=new ArrayList<Object>();
						for(int v2=v+1;v2<V.size();v2++)
						{
							final String str2=V.get(v2);
							if(zapCodes.containsKey(str2))
							{
								v=v2-1;
								break;
							}
							boolean getClan = true;
							boolean found=false;
							if(clanName!=null)
							{
								final CompiledZMaskEntry e = levelCompiledHelper(str2,plusMinus);
								if(e!=null)
								{
									parms.add(clanName);
									parms.add(e.maskType());
									parms.add(e.parms()[0]);
									getClan = false;
									found=true;
								}
							}
							if(getClan)
							{
								if((str2.length()>1)
								&&(str2.charAt(0)==plusMinus))
								{
									clanName = str2.substring(1);
									found=true;
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
				case _AREABLURB: // -Areablurb
					{
						buildRoomFlag=true;
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
								String rawParm=str2.substring(1).trim();
								final int x=rawParm.indexOf(' ');
								final String parmVal;
								if(x>1)
								{
									parmVal=rawParm.substring(x+1);
									rawParm=rawParm.substring(0,x);
								}
								else
									parmVal="";
								if(rawParm.startsWith("*"))
									parms.add(new Triad<Character,String,String>(Character.valueOf('s'),rawParm.substring(1).toUpperCase(),parmVal));
								else
								if(rawParm.endsWith("*"))
									parms.add(new Triad<Character,String,String>(Character.valueOf('e'),rawParm.substring(0,rawParm.length()-1).toUpperCase(),parmVal));
								else
									parms.add(new Triad<Character,String,String>(Character.valueOf(' '),rawParm.toUpperCase(),parmVal));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _AREA: // -Area
				case _PARENTAREA: // -parentarea
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
				case _PLANE: // -Plane
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
							if(str3.equalsIgnoreCase("EVIL")||str3.equalsIgnoreCase("NEUTRAL")||str3.equalsIgnoreCase("GOOD"))
							{
								parms.clear();
								v=v2-1;
								break;
							}
							else
							{
								//if(str3.startsWith("AREA_"))
								//	parms.add(str3);
								//else
								//final Faction.FRange FR=getRange(str3);
								//if(FR==null)
								//	Log.debugOut("Range not found in MUDZapper: "+str3);
								parms.add(str3);
							}
						}
						v=V.size();
					}
					if(parms.size()>0)
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
				case LOCATION:
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
							if(str2.startsWith("-"))
							{
								str2=str2.substring(1);
								final int x=str2.indexOf('(');
								String id=str2;
								String str2parms="";
								if(x>0)
								{
									id=str2.substring(0,x);
									if(str2.endsWith(")"))
										str2parms=str2.substring(x+1,str2.length()-1);
									else
										str2parms=str2.substring(x+1);
								}
								final LocationType T=(LocationType)CMath.s_valueOf(LocationType.class, id.toUpperCase());
								if(T != null)
								{
									parms.add(T);
									parms.add(str2parms);
								}
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _LOCATION:
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
							if(str2.startsWith("+"))
							{
								str2=str2.substring(1);
								final int x=str2.indexOf('(');
								String id=str2;
								String str2parms="";
								if(x>0)
								{
									id=str2.substring(0,x);
									if(str2.endsWith(")"))
										str2parms=str2.substring(x+1,str2.length()-1);
									else
										str2parms=str2.substring(x+1);
								}
								final LocationType T=(LocationType)CMath.s_valueOf(LocationType.class, id.toUpperCase());
								if(T != null)
								{
									parms.add(T);
									parms.add(str2parms);
								}
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case AREABLURB: // +Areablurb
					{
						buildRoomFlag=true;
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
								String rawParm=str2.substring(1).trim();
								final int x=rawParm.indexOf(' ');
								final String parmVal;
								if(x>1)
								{
									parmVal=rawParm.substring(x+1);
									rawParm=rawParm.substring(0,x);
								}
								else
									parmVal="";
								if(rawParm.startsWith("*"))
									parms.add(new Triad<Character,String,String>(Character.valueOf('s'),rawParm.substring(1).toUpperCase(),parmVal));
								else
								if(rawParm.endsWith("*"))
									parms.add(new Triad<Character,String,String>(Character.valueOf('e'),rawParm.substring(0,rawParm.length()-1).toUpperCase(),parmVal));
								else
									parms.add(new Triad<Character,String,String>(Character.valueOf(' '),rawParm.toUpperCase(),parmVal));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case AREA: // +Area
				case PARENTAREA: // +parentarea
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
				case PLANE: // +Plane
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
								final int code=CMLib.materials().findMaterialCode(str2.substring(1),false);
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
				case BIRTHSEASON: // +birthSeason
				case _BIRTHSEASON: // -birthSeason
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
				case DOMAIN: // +domain
				case _DOMAIN: // -domain
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
								final String str3=str2.substring(1).toUpperCase().trim();
								if(CMath.isInteger(str2.substring(1).trim()))
									parms.add(Integer.valueOf(CMath.s_int(str3)));
								else
								if(CMParms.indexOf(Room.DOMAIN_OUTDOOR_DESCS,str3)>=0)
									parms.add(Integer.valueOf(CMParms.indexOf(Room.DOMAIN_OUTDOOR_DESCS,str3)));
								else
								if(CMParms.indexOf(Room.DOMAIN_INDOORS_DESCS,str3)>=0)
									parms.add(Integer.valueOf(Room.INDOORS+CMParms.indexOf(Room.DOMAIN_INDOORS_DESCS,str3)));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case PORT: // +PORT
				case _PORT: // -PORT
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
				case BIRTHMONTH: // +BIRTHMONTH
				case _BIRTHMONTH: // -BIRTHMONTH
				case BIRTHWEEK: // +BIRTHWEEK
				case _BIRTHWEEK: // -BIRTHWEEK
				case BIRTHWEEKOFYEAR: // +BIRTHWEEKOFYEAR
				case _BIRTHWEEKOFYEAR: // -BIRTHWEEKOFYEAR
				case BIRTHYEAR: // +BIRTHYEAR
				case _BIRTHYEAR: // -BIRTHYEAR
				case BIRTHDAY: // +BIRTHDAY
				case _BIRTHDAY: // -BIRTHDAY
				case BIRTHDAYOFYEAR: // +BIRTHDAYOFYEAR
				case _BIRTHDAYOFYEAR: // -BIRTHDAYOFYEAR
				case HOUR: // +HOUR
				case _HOUR: // -HOUR
				case MONTH: // +MONTH
				case _MONTH: // -MONTH
				case WEEK: // +WEEK
				case _WEEK: // -WEEK
				case WEEKOFYEAR: // +WEEKOFYEAR
				case _WEEKOFYEAR: // -WEEKOFYEAR
				case YEAR: // +YEAR
				case _YEAR: // -YEAR
				case DAY: // +DAY
				case _DAY: // -DAY
				case DAYOFYEAR: // +DAYOFYEAR
				case _DAYOFYEAR: // -DAYOFYEAR
					{
						// three data formats supported:
						// -MONTH +5 (the month numbered 5)
						// -DAY +3rd 5 -- no earthly idea
						// -MONTH +3 of 5 - 3rd of every 5 months
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
								final String lstr2=str2.toLowerCase();
								final int x=lstr2.indexOf(' ');
								if(x>0)
								{
									final String lstr3=lstr2.substring(1,x).trim();
									final String nstr=lstr2.substring(x+1).trim();
									if(lstr3.endsWith("st")  || lstr3.endsWith("nd")
									|| lstr3.endsWith("rd") || lstr3.endsWith("th"))
									{
										final String str3=lstr3.substring(0,lstr3.length()-2);
										final int amt=CMath.s_int(str3.trim());
										if(amt > 0)
										{
											parms.add(new Pair<Integer,Integer>(
													Integer.valueOf(amt), //-1 because 0 is the first X -- so what?
													Integer.valueOf(CMath.s_int(nstr.trim()))));
										}
									}
									else
									if(nstr.startsWith("of"))
									{
										final int amt=CMath.s_int(nstr.substring(2).trim());
										if(amt > 0)
										{
											parms.add(new Triad<Integer,Integer,String>(
													Integer.valueOf(CMath.s_int(lstr3)),
													Integer.valueOf(amt), // -1 because 0 is the first in every X -- how is that an excuse?
													null));
										}
									}
								}
								else
									parms.add(Integer.valueOf(CMath.s_int(str2.substring(1).trim())));
							}
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
				case OFFICER: // +officer
				case _OFFICER: // -officer
				case JUDGE: // +judge
				case _JUDGE: // -judge
				case SUBOP: // +subop
				case _SUBOP: // -subop
				case AREAINSTANCE: // +areainstance
				case _AREAINSTANCE: // -areainstance-
					buildRoomFlag=true;
				//$FALL-THROUGH$
				case ISHOME: // +ishome
				case _ISHOME: // -ishome
				case SYSOP: // +sysop
				case _SYSOP: // -sysop
				{
					buf.add(new CompiledZapperMaskEntryImpl(entryType,new Object[0]));
					break;
				}
				case RIDE: // +ride
				case _RIDE: // -ride
				case FOLLOW: // +follow
				case _FOLLOW: // -follow
				{
					final Object[] subSet = new Object[] { new ArrayList<CompiledZMaskEntry>() };
					buf.add(new CompiledZapperMaskEntryImpl(entryType,subSet));
					@SuppressWarnings("unchecked")
					final ArrayList<CompiledZMaskEntry> newBuf = (ArrayList<CompiledZMaskEntry>)subSet[0];
					buf = newBuf;
					break;
				}
				case OR: // +or
				case _OR: // -or
				{
					buf=new ArrayList<CompiledZMaskEntry>(1);
					buf.add(new CompiledZapperMaskEntryImpl(entryType,new Object[0]));
					bufs.add(buf);
					buf=new ArrayList<CompiledZMaskEntry>();
					bufs.add(buf);
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
								final int x=str2.indexOf('=');
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
								String nm = str2.substring(1).trim();
								for(final RawMaterial.ResourceFlag f : RawMaterial.ResourceFlag.values())
								{
									if(nm.equalsIgnoreCase(f.name()) || CMParms.containsIgnoreCase(f.altNames, nm))
									{
										final int[] rscs = RawMaterial.CODES.flaggedResources(f);
										if(rscs.length>0)
										{
											for(int i=1;i<rscs.length;i++)
												parms.add(RawMaterial.CODES.NAME(rscs[i]));
											nm = RawMaterial.CODES.NAME(rscs[0]);
										}
										break;
									}
								}
								final int code=CMLib.materials().findResourceCode(nm,false);
								if(code>=0)
									parms.add(RawMaterial.CODES.NAME(code));
							}
							v=V.size();
						}
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case _PLAYER: // -Player
				case PLAYER: // +Player
				case NPC: // +MOB
				case _NPC: // -MOB
					{
						buf.add(new CompiledZapperMaskEntryImpl(entryType,new Object[0]));
						break;
					}
				case _ANYCLASS: // -anyclass
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						v = fromHereStartsWith(V, v, parms, entryType);
						buf.add(new CompiledZapperMaskEntryImpl(entryType,parms.toArray(new Object[0])));
					}
					break;
				case ANYCLASS: // +anyclass
					{
						final ArrayList<Object> parms=new ArrayList<Object>();
						v = fromHereStartsWith(V, v, parms, entryType);
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
				if(getLooseCodes().containsKey(str))
				{
					found=true;
					buf.add(getLooseCodes().get(str));
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
					final TreeMap<String,CompiledZapperMaskEntryImpl> looseCodes = getLooseCodes();
					for(final String start : looseCodes.keySet())
					{
						if(start.startsWith(str))
						{
							found=true;
							buf.add(looseCodes.get(start));
							break;
						}
					}
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
					else
					if(str.startsWith("+")||str.startsWith("-"))
						Log.errOut("Bad Zappermask key '"+str+"' @ "+CMParms.combine(V,0));
					else
						break;
				}
			}
		}
		final CompiledZMaskEntry[][] entrieses = new CompiledZMaskEntry[bufs.size()][];
		for(int i=0;i<bufs.size();i++)
			entrieses[i]=bufs.get(i).toArray(new CompiledZMaskEntry[0]);
		return new CompiledZapperMaskImpl(entrieses, buildItemFlag, buildRoomFlag);
	}

	protected Room outdoorRoom(final Area A)
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

	protected CharStats getBaseCharStats(CharStats base, final MOB mob)
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

	protected boolean maskCheck(final CompiledZMaskEntry[] cset, final Environmental E, final boolean actual)
	{
		if(E==null)
			return true;
		if((cset==null)||(cset.length<1))
			return true;
		getMaskCodes();
		final MOB mob=(E instanceof MOB)?(MOB)E:nonCrashingMOB();
		final Item item=((E instanceof Item)?(Item)E:nonCrashingItem(mob));
		final Room room =((E instanceof Area)?outdoorRoom((Area)E):CMLib.map().roomLocation(E));
		final Physical P = (E instanceof Physical)?(Physical)E:null;
		return maskCheckSubEntries(cset,E,actual,mob,item,room,P);
	}

	@Override
	public boolean maskCheck(final CompiledZMask cset, final Environmental E, final boolean actual)
	{
		if(E==null)
			return true;
		if((cset==null)||(cset.entries().length<1))
			return true;
		getMaskCodes();
		final MOB mob=(E instanceof MOB)?(MOB)E:nonCrashingMOB();
		final Item item=cset.useItemFlag()?((E instanceof Item)?(Item)E:nonCrashingItem(mob)):null;
		final Room room = cset.useRoomFlag()?((E instanceof Area)?outdoorRoom((Area)E):CMLib.map().roomLocation(E)):null;
		final Physical P = (E instanceof Physical)?(Physical)E:null;
		if((mob==null)||(cset.useItemFlag()&&(item==null)))
			return false;
		if(E instanceof Area)
			mob.addFaction(CMLib.factions().getAlignmentID(), ((Area)E).getIStat(Area.Stats.MED_ALIGNMENT));
		if(cset.entries().length<3)
			return maskCheckSubEntries(cset.entries()[0],E,actual,mob,item,room,P);
		else
		{
			boolean lastValue = false;
			boolean lastConnectorNot = false;
			for(int i=0;i<cset.entries().length;i+=2)
			{
				boolean subResult =  maskCheckSubEntries(cset.entries()[i],E,actual,mob,item,room,P);
				if(lastConnectorNot)
					subResult = !subResult;
				lastValue = lastValue || subResult;
				if(i==cset.entries().length-1)
					return lastValue;
				final CompiledZMaskEntry entry = cset.entries()[i+1][0];
				if(entry.maskType()==MaskingLibrary.ZapperKey._OR)
					lastConnectorNot=true;
				else
				if(entry.maskType()==MaskingLibrary.ZapperKey.OR)
					lastConnectorNot=false;
				else
					Log.errOut("Badly compiled zappermask @ "+E.Name()+"@"+CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(E)));
			}
			return lastValue;
		}
	}

	protected boolean doZapperCompare(final CompiledZMaskEntry entry, final int cl, final int i)
	{
		switch((ZapperKey)entry.parms()[i])
		{
		case LVLGR: // +lvlgr
			if(cl>((Integer)entry.parms()[i+1]).intValue())
				return true;
			break;
		case LVLGE: // +lvlge
			if(cl>=((Integer)entry.parms()[i+1]).intValue())
				return true;
			break;
		case LVLLT: // +lvlt
			if(cl<((Integer)entry.parms()[i+1]).intValue())
				return true;
			break;
		case LVLLE: // +lvlle
			if(cl<=((Integer)entry.parms()[i+1]).intValue())
				return true;
			break;
		case LVLEQ: // +lvleq
			if(cl==((Integer)entry.parms()[i+1]).intValue())
				return true;
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean maskCheckDateEntries(final CompiledZMask cset, final TimeClock C)
	{
		if(C==null)
			return true;
		if((cset==null)||(cset.empty())||(cset.entries().length<1))
			return true;
		getMaskCodes();
		if(cset.entries().length<3)
			return maskCheckDateEntries(cset.entries()[0], C);
		else
		{
			boolean lastValue = false;
			boolean lastConnectorNot = false;
			for(int i=0;i<cset.entries().length;i+=2)
			{
				boolean subResult =  maskCheckDateEntries(cset.entries()[i],C);
				if(lastConnectorNot)
					subResult = !subResult;
				lastValue = lastValue || subResult;
				if(i==cset.entries().length-1)
					return lastValue;
				final CompiledZMaskEntry entry = cset.entries()[i+1][0];
				if(entry.maskType()==MaskingLibrary.ZapperKey._OR)
					lastConnectorNot=true;
				else
				if(entry.maskType()==MaskingLibrary.ZapperKey.OR)
					lastConnectorNot=false;
				else
					Log.errOut("Badly compiled zappermask @ "+C.name());
			}
			return lastValue;
		}
	}

	protected boolean maskCheckDateEntries(final CompiledZMaskEntry[] set, final TimeClock C)
	{
		for(final CompiledZMaskEntry entry : set)
		{
			try
			{
				switch(entry.maskType())
				{
				case HOUR: // +HOUR
					{
						final int num = C.getHourOfDay();
						for(final Object o : entry.parms())
						{
							if(isDateMatch(o,num))
								return false;
						}
					}
					break;
				case _HOUR: // -HOUR
					{
						boolean found=false;
						final int num = C.getHourOfDay();
						for(final Object o : entry.parms())
						{
							if(isDateMatch(o,num))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case BIRTHYEAR: // +BIRTHYEAR
				case YEAR: // +YEAR
					{
						final int num = C.getYear();
						for(final Object o : entry.parms())
						{
							if(isDateMatch(o,num))
								return false;
						}
					}
					break;
				case _BIRTHYEAR: // -BIRTHYEAR
				case _YEAR: // -YEAR
					{
						boolean found=false;
						final int num = C.getYear();
						for(final Object o : entry.parms())
						{
							if(isDateMatch(o,num))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case BIRTHWEEK: // +BIRTHWEEK
				case WEEK: // +WEEK
					{
						final int num = C.getWeekOfMonth();
						for(final Object o : entry.parms())
						{
							if(isDateMatch(o,num))
								return false;
						}
					}
					break;
				case _BIRTHWEEK: // -BIRTHWEEK
				case _WEEK: // -WEEK
					{
						boolean found=false;
						final int num = C.getWeekOfMonth();
						for(final Object o : entry.parms())
						{
							if(isDateMatch(o,num))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case BIRTHSEASON: // +birthseason
				case SEASON: // +season
					{
						final int num = C.getSeasonCode().ordinal();
						for(final Object o : entry.parms())
						{
							if(isDateMatch(o,num))
								return false;
						}
					}
					break;
				case _BIRTHSEASON: // -birthseason
				case _SEASON: // -season
					{
						boolean found=false;
						final int num = C.getSeasonCode().ordinal();
						for(final Object o : entry.parms())
						{
							if(isDateMatch(o,num))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case BIRTHMONTH:
				case MONTH:
					{
						final int num = C.getMonth();
						for(final Object o : entry.parms())
						{
							if(isDateMatch(o,num))
								return false;
						}
					}
					break;
				case _BIRTHMONTH:
				case _MONTH:
					{
						boolean found=false;
						final int num = C.getMonth();
						for(final Object o : entry.parms())
						{
							if(isDateMatch(o,num))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case BIRTHWEEKOFYEAR:
				case WEEKOFYEAR:
					{
						final int num = C.getWeekOfYear();
						for(final Object o : entry.parms())
						{
							if(isDateMatch(o,num))
								return false;
						}
					}
					break;
				case _BIRTHWEEKOFYEAR:
				case _WEEKOFYEAR:
					{
						boolean found=false;
						final int num = C.getWeekOfYear();
						for(final Object o : entry.parms())
						{
							if(isDateMatch(o,num))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case BIRTHDAY:
				case DAY:
					{
						final int num = C.getDayOfMonth();
						for(final Object o : entry.parms())
						{
							if(isDateMatch(o,num))
								return false;
						}
					}
					break;
				case _BIRTHDAY:
				case _DAY:
					{
						boolean found=false;
						final int num = C.getDayOfMonth();
						for(final Object o : entry.parms())
						{
							if(isDateMatch(o,num))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case BIRTHDAYOFYEAR:
				case DAYOFYEAR:
					{
						final int num = C.getDayOfYear();
						for(final Object o : entry.parms())
						{
							if(isDateMatch(o,num))
								return false;
						}
					}
					break;
				case _BIRTHDAYOFYEAR:
				case _DAYOFYEAR:
					{
						boolean found=false;
						final int num = C.getDayOfYear();
						for(final Object o : entry.parms())
						{
							if(isDateMatch(o,num))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				default:
					break;
				}
			}
			catch(final Exception e)
			{}
		}
		return true;
	}

	protected boolean maskCheckSubEntries(final CompiledZMaskEntry[] set, final Environmental E, final boolean actual,
										  final MOB mob, final Item item, final Room room, final Physical P)
	{
		CharStats base=null;
		for(final CompiledZMaskEntry entry : set)
		{
			try
			{
				switch(entry.maskType())
				{
				case OR: //+or
				case _OR: //-or
					Log.errOut("Badly compiled zappermask @ "+E.Name()+"@"+CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(E)));
					break;
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
					if((!actual)
					&&(!baseClass.equals(mob.charStats().displayClassName())))
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
					if((!actual)
					&&(!baseClass.equals(mob.charStats().displayClassName())))
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
					if((!CMParms.contains(entry.parms(),CMLib.flags().getAlignmentName(mob)))
					&&(!CMParms.contains(entry.parms(),CMLib.flags().getInclinationName(mob))))
						return false;
					break;
				case _GENDER: // -gender
				{
					final String genderName;
					if(actual)
					{
						base=getBaseCharStats(base,mob);
						genderName = base.realGenderName().toUpperCase();
					}
					else
						genderName=mob.charStats().genderName().toUpperCase();
					if((!CMParms.contains(entry.parms(),""+genderName.charAt(0)))&&(!CMParms.contains(entry.parms(),genderName)))
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
							if(doZapperCompare(entry,level,v))
								found=true;
						}
						if(!found)
							return false;
					}
					break;
				case LEVEL: // +level
					if(P!=null)
					{
						final int level=actual?P.basePhyStats().level():P.phyStats().level();
						for(int v=0;v<entry.parms().length-1;v+=2)
						{
							if(doZapperCompare(entry,level,v))
								return false;
						}
					}
					break;
				case CLASSLEVEL: // +classlevel
					{
						final int cl=actual?mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass())
									 :mob.charStats().getClassLevel(mob.charStats().getCurrentClass());
						for(int v=0;v<entry.parms().length-1;v+=2)
						{
							if(doZapperCompare(entry,cl,v))
								return false;
						}
					}
					break;
				case _CLASSLEVEL: // -classlevel
					{
						boolean found=false;
						final int cl=actual?mob.baseCharStats().getClassLevel(mob.baseCharStats().getCurrentClass())
									 :mob.charStats().getClassLevel(mob.charStats().getCurrentClass());
						for(int v=0;v<entry.parms().length-1;v+=2)
						{
							if(doZapperCompare(entry,cl,v))
								found=true;
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
							if(doZapperCompare(entry,cl,i+1))
								found=true;
							allFound = allFound || found;
						}
						if(!allFound)
							return false;
					}
					break;
				case ANYCLASSLEVEL: // +anyclasslevel
					{
						for(int i=0;i<entry.parms().length;i+=3)
						{
							final CharClass C = (CharClass)entry.parms()[i+0];
							final int cl=actual?mob.baseCharStats().getClassLevel(C)
												:mob.charStats().getClassLevel(C);
							if(cl >= 0)
							{
								if(doZapperCompare(entry,cl,i+1))
									return false;
							}
						}
					}
					break;
				case _CLANLEVEL: // -clanlevel
					{
						boolean allFound = false;
						for(int i=0;i<entry.parms().length;i+=3)
						{
							boolean found=false;
							final String clanName = (String)entry.parms()[i];
							if(clanName.equals("1"))
							{
								final Iterator<Pair<Clan,Integer>> ci=mob.clans().iterator();
								if(!ci.hasNext())
									continue; // not found
								final int cl = ci.next().first.getClanLevel();
								if(doZapperCompare(entry,cl,i+1))
									found=true;
							}
							else
							if(clanName.equals("*"))
							{
								for(final Iterator<Pair<Clan,Integer>> ci=mob.clans().iterator();ci.hasNext();)
								{
									final int cl=ci.next().first.getClanLevel();
									if(doZapperCompare(entry,cl,i+1))
									{
										found=true;
										break;
									}
								}
							}
							else
							{
								final Clan C = CMLib.clans().getClanExact(clanName);
								if(C == null)
									continue;
								if(mob.getClanRole(C.clanID())==null)
									continue;
								final int cl = C.getClanLevel();
								if(doZapperCompare(entry,cl,i+1))
									found=true;
							}
							allFound = allFound || found;
						}
						if(!allFound)
							return false;
					}
					break;
				case CLANLEVEL: // +clanlevel
					{
						for(int i=0;i<entry.parms().length;i+=3)
						{
							final String clanName = (String)entry.parms()[i+0];
							if(clanName.equals("1"))
							{
								final Iterator<Pair<Clan,Integer>> ci=mob.clans().iterator();
								if(!ci.hasNext())
									continue; // not found
								final int cl = ci.next().first.getClanLevel();
								if(doZapperCompare(entry,cl,i+1))
									return false;
							}
							else
							if(clanName.equals("*"))
							{
								for(final Iterator<Pair<Clan,Integer>> ci=mob.clans().iterator();ci.hasNext();)
								{
									final int cl=ci.next().first.getClanLevel();
									if(doZapperCompare(entry,cl,i+1))
										return false;
								}
							}
							else
							{
								final Clan C = CMLib.clans().getClanExact(clanName);
								if(C == null)
									continue;
								if(mob.getClanRole(C.clanID())==null)
									continue;
								final int cl = C.getClanLevel();
								if(doZapperCompare(entry,cl,i+1))
									return false;
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
							if(doZapperCompare(entry,cl,v))
								found=true;
						}
						if(!found)
							return false;
					}
					break;
				case MAXCLASSLEVEL: // +maxclasslevel
					{
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
							if(doZapperCompare(entry,cl,v))
								return false;
						}
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
							if(CMParms.indexOf(entry.parms(), Integer.valueOf(((Weapon)W).weaponDamageType())) >= 0)
								return false;
						}
					}
					break;
				case _WEAPONTYPE: // -weapontype
					{
						final Environmental W=(E instanceof MOB) ? ((MOB)E).fetchWieldedItem() : E;
						if(W instanceof Weapon)
						{
							if(CMParms.indexOf(entry.parms(), Integer.valueOf(((Weapon)W).weaponDamageType())) < 0)
								return false;
						}
						else
							return false;
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
				case LOCATION: // +location
					{
						final Room R=CMLib.map().roomLocation(E);
						for(int i=0;i<entry.parms().length-1;i+=2)
						{
							if(checkLocation(E, mob, R, entry.parms()[i], entry.parms()[i+1]))
								return false;
						}
					}
					break;
				case _LOCATION: // -location
					{
						final Room R=CMLib.map().roomLocation(E);
						boolean found=false;
						for(int i=0;i<entry.parms().length-1;i+=2)
						{
							if(checkLocation(E, mob, R, entry.parms()[i], entry.parms()[i+1]))
							{
								found=true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case WEAPONCLASS: // +weaponclass
					{
						final Environmental W=(E instanceof MOB) ? ((MOB)E).fetchWieldedItem() : E;
						if(W instanceof Weapon)
						{
							if(CMParms.indexOf(entry.parms(), Integer.valueOf(((Weapon)W).weaponClassification())) >= 0)
								return false;
						}
					}
					break;
				case _WEAPONCLASS: // -weaponclass
					{
						final Environmental W=(E instanceof MOB) ? ((MOB)E).fetchWieldedItem() : E;
						if(W instanceof Weapon)
						{
							if(CMParms.indexOf(entry.parms(), Integer.valueOf(((Weapon)W).weaponClassification())) < 0)
								return false;
						}
					}
					break;
				case _OFFICER: // -officer
					{
						if(mob.isMonster())
						{
							final LegalBehavior B=CMLib.law().getLegalBehavior(mob.getStartRoom());
							if((B!=null)
							&&(B.isAnyOfficer(CMLib.law().getLegalObject(mob.getStartRoom()), mob)))
								return false;
						}
					}
					break;
				case OFFICER: //+officer
					{
						if(!mob.isMonster())
							return false;
						final LegalBehavior B=CMLib.law().getLegalBehavior(mob.getStartRoom());
						if((B!=null)
						&&(!B.isAnyOfficer(CMLib.law().getLegalObject(mob.getStartRoom()), mob)))
							return false;
					}
					break;
				case _JUDGE: // -judge
					{
						if(mob.isMonster())
						{
							final LegalBehavior B=CMLib.law().getLegalBehavior(mob.getStartRoom());
							if((B!=null)
							&&(B.isJudge(CMLib.law().getLegalObject(mob.getStartRoom()), mob)))
								return false;
						}
					}
					break;
				case JUDGE: // +judge
					{
						if(!mob.isMonster())
							return false;
						final LegalBehavior B=CMLib.law().getLegalBehavior(mob.getStartRoom());
						if((B!=null)
						&&(!B.isJudge(CMLib.law().getLegalObject(mob.getStartRoom()), mob)))
							return false;
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
						if(E instanceof SpellHolder)
						{
							final SpellHolder spellE = (SpellHolder)E;
							final String lowerSpellList = spellE.getSpellList().toLowerCase();
							for(int v=0;v<entry.parms().length-1;v+=2)
							{
								if(lowerSpellList.indexOf(((String)entry.parms()[v]).toLowerCase())>=0)
								{
									found = true;
									break;
								}
							}
						}
						else
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
						if(E instanceof SpellHolder)
						{
							final SpellHolder spellE = (SpellHolder)E;
							for(final Object o : entry.parms())
							{
								for(final Ability A : spellE.getSpells())
								{
									if(evaluateSkillFlagObject(o,A))
									{
										found = true;
										break;
									}
								}
								if(found)
									break;
							}
						}
						else
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
						if(E instanceof SpellHolder)
						{
							final SpellHolder spellE = (SpellHolder)E;
							final String lowerSpellList = spellE.getSpellList().toLowerCase();
							for(int v=0;v<entry.parms().length-1;v+=2)
							{
								if(lowerSpellList.indexOf(((String)entry.parms()[v]).toLowerCase())>=0)
									return false;
							}
						}
						else
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
						if(E instanceof SpellHolder)
						{
							final SpellHolder spellE = (SpellHolder)E;
							for(final Object o : entry.parms())
							{
								for(final Ability A : spellE.getSpells())
								{
									if(evaluateSkillFlagObject(o,A))
										return false;
								}
							}
						}
						else
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
				case PLAYER: // +player
					if(!mob.isMonster())
						return true;
					break;
				case NPC: // +npc
					if(mob.isMonster())
						return true;
					break;
				case _RACECAT: // -racecat
				{
					String raceCat=mob.baseCharStats().getMyRace().racialCategory();
					if((!actual)
					&&(!mob.baseCharStats().getMyRace().name().equals(mob.charStats().raceName())))
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
									||(((String)o).equals("*") && (clanID.length()>0)))
									{
										found=true;
										break;
									}
								}
								else
								if(o instanceof Pair)
								{
									@SuppressWarnings("unchecked")
									final Pair<String,String> oP=((Pair<String,String>)o);
									if(clanID.equalsIgnoreCase(oP.first)
									||((oP.first).equals("*") && (clanID.length()>0)))
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
								final String clanID = c.first.clanID();
								for(final Object o : entry.parms())
								{
									if(o instanceof String)
									{
										if(c.first.clanID().equalsIgnoreCase((String)o)
										||(((String)o).equals("*") && (clanID.length()>0)))
										{
											found=true;
											break;
										}
									}
									else
									if(o instanceof Pair)
									{
										@SuppressWarnings("unchecked")
										final Pair<String,String> oP=((Pair<String,String>)o);
										if(c.first.clanID().equalsIgnoreCase(oP.first)
										||((oP.first).equals("*") && (clanID.length()>0)))
										{
											if((oP.second).equals("*"))
											{
												found=true;
												break;
											}
											else
											{
												final ClanPosition cP=c.first.getGovernment().getPosition(oP.second);
												if(cP==null)
												{
													final Clan.Function cf = (Clan.Function)CMath.s_valueOf(Clan.Function.class, oP.second);
													if(cf != null)
													{
														final Authority a = c.first.getAuthority(c.second.intValue(), cf);
														if((a!=null)&&(a != Authority.CAN_NOT_DO))
														{
															found=true;
															break;
														}
														continue;
													}
												}
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
								||(((String)o).equals("*") && (clanID.length()>0)))
									return false;
							}
							else
							if(o instanceof Pair)
							{
								@SuppressWarnings("unchecked")
								final Pair<String,String> oP=((Pair<String,String>)o);
								if(clanID.equalsIgnoreCase(oP.first)
								||((oP.first).equals("*") && (clanID.length()>0)))
									return false;
							}
						}
					}
					else
					if(E instanceof MOB)
					{
						for(final Pair<Clan,Integer> c : ((MOB)E).clans())
						{
							final String clanID = c.first.clanID();
							for(final Object o : entry.parms())
							{
								if(o instanceof String)
								{
									if(clanID.equalsIgnoreCase((String)o)
									||(((String)o).equals("*") && (clanID.length()>0)))
										return false;
								}
								else
								if(o instanceof Pair)
								{
									@SuppressWarnings("unchecked")
									final Pair<String,String> oP=((Pair<String,String>)o);
									if(clanID.equalsIgnoreCase(oP.first)
									||((oP.first).equals("*") && (clanID.length()>0)))
									{
										if((oP.second).equals("*"))
											return false;
										else
										{
											final ClanPosition cP=c.first.getGovernment().getPosition(oP.second);
											if(cP==null)
											{
												final Clan.Function cf = (Clan.Function)CMath.s_valueOf(Clan.Function.class, oP.second);
												if(cf != null)
												{
													final Authority a = c.first.getAuthority(c.second.intValue(), cf);
													if((a!=null)&&(a != Authority.CAN_NOT_DO))
														return false;
												}
											}
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
				case PORT: // +PORT
					{
						final MudHost host=CMLib.host();
						for(final Object o : entry.parms())
						{
							if(host.getPort()==((Integer)o).intValue())
								return false;
						}
					}
					break;
				case _PORT: // -PORT
					{
						final MudHost host=CMLib.host();
						boolean found=false;
						for(final Object o : entry.parms())
						{
							if(host.getPort()==((Integer)o).intValue())
							{
								found=true;
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
							final int num = room.getArea().getTimeObj().getHourOfDay();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
							final int num = room.getArea().getTimeObj().getHourOfDay();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
				case YEAR: // +YEAR
					{
						if(room!=null)
						{
							final int num = room.getArea().getTimeObj().getYear();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
									return false;
							}
						}
					}
					break;
				case _YEAR: // -YEAR
					{
						boolean found=false;
						if(room!=null)
						{
							final int num = room.getArea().getTimeObj().getYear();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
				case BIRTHYEAR: // +BIRTHYEAR
					{
						if(mob.isPlayer())
						{
							final int num = mob.playerStats().getBirthday()[PlayerStats.BIRTHDEX_YEAR];
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
									return false;
							}
						}
					}
					break;
				case _BIRTHYEAR: // -BIRTHYEAR
					{
						boolean found=false;
						if(mob.isPlayer())
						{
							final int num = mob.playerStats().getBirthday()[PlayerStats.BIRTHDEX_YEAR];
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
				case WEEK: // +WEEK
					{
						if(room!=null)
						{
							final int num = room.getArea().getTimeObj().getWeekOfMonth();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
									return false;
							}
						}
					}
					break;
				case _WEEK: // -WEEK
					{
						boolean found=false;
						if(room!=null)
						{
							final int num = room.getArea().getTimeObj().getWeekOfMonth();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
				case BIRTHWEEK: // +BIRTHWEEK
					{
						if(mob.isPlayer()
						&&(mob.getStartRoom()!=null))
						{
							final TimeClock C = mob.playerStats().getBirthdayClock(mob.getStartRoom().getArea().getTimeObj());
							final int num = C.getWeekOfMonth();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
									return false;
							}
						}
					}
					break;
				case _BIRTHWEEK: // -BIRTHWEEK
					{
						boolean found=false;
						if(mob.isPlayer()
						&&(mob.getStartRoom()!=null))
						{
							final TimeClock C = mob.playerStats().getBirthdayClock(mob.getStartRoom().getArea().getTimeObj());
							final int num = C.getWeekOfMonth();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
				case BIRTHSEASON: // +birthseason
					{
						if(mob.isPlayer()
						&&(mob.getStartRoom()!=null))
						{
							final TimeClock C = mob.playerStats().getBirthdayClock(mob.getStartRoom().getArea().getTimeObj());
							final int num = C.getSeasonCode().ordinal();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
									return false;
							}
						}
					}
					break;
				case _BIRTHSEASON: // -birthseason
					{
						boolean found=false;
						if(mob.isPlayer()
						&&(mob.getStartRoom()!=null))
						{
							final TimeClock C = mob.playerStats().getBirthdayClock(mob.getStartRoom().getArea().getTimeObj());
							final int num = C.getSeasonCode().ordinal();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
							final int num = room.getArea().getTimeObj().getSeasonCode().ordinal();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
							final int num = room.getArea().getTimeObj().getSeasonCode().ordinal();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
				case DOMAIN: // +domain
					{
						if(room!=null)
						{
							for(final Object o : entry.parms())
							{
								if(room.domainType()==((Integer)o).intValue())
									return false;
							}
						}
					}
					break;
				case _DOMAIN: // -domain
					{
						boolean found=false;
						if(room!=null)
						{
							for(final Object o : entry.parms())
							{
								if(room.domainType()==((Integer)o).intValue())
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
							final int num = room.getArea().getTimeObj().getMonth();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
							final int num = room.getArea().getTimeObj().getMonth();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
				case BIRTHMONTH: // +birthmonth
					{
						if(mob.isPlayer())
						{
							final int num = mob.playerStats().getBirthday()[PlayerStats.BIRTHDEX_MONTH];
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
									return false;
							}
						}
					}
					break;
				case _BIRTHMONTH: // -birthmonth
					{
						boolean found=false;
						if(mob.isPlayer())
						{
							final int num = mob.playerStats().getBirthday()[PlayerStats.BIRTHDEX_MONTH];
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
				case DAYOFYEAR: // +dayofyear
					{
						if(room!=null)
						{
							final int num = room.getArea().getTimeObj().getDayOfYear();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
									return false;
							}
						}
					}
					break;
				case _DAYOFYEAR: // -dayofyear
					{
						boolean found=false;
						if(room!=null)
						{
							final int num = room.getArea().getTimeObj().getDayOfYear();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
				case BIRTHDAYOFYEAR: // +birthdayofyear
					{
						if(mob.isPlayer()
						&&(mob.getStartRoom()!=null))
						{
							final TimeClock C = mob.playerStats().getBirthdayClock(mob.getStartRoom().getArea().getTimeObj());
							final int num = C.getDayOfYear();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
									return false;
							}
						}
					}
					break;
				case _BIRTHDAYOFYEAR: // -birthdayofyear
					{
						boolean found=false;
						if(mob.isPlayer()
						&&(mob.getStartRoom()!=null))
						{
							final TimeClock C = mob.playerStats().getBirthdayClock(mob.getStartRoom().getArea().getTimeObj());
							final int num = C.getDayOfYear();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
				case WEEKOFYEAR: // +weekofyear
					{
						if(room!=null)
						{
							final int num = room.getArea().getTimeObj().getWeekOfYear();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
									return false;
							}
						}
					}
					break;
				case _WEEKOFYEAR: // -weekofyear
					{
						boolean found=false;
						if(room!=null)
						{
							final int num = room.getArea().getTimeObj().getWeekOfYear();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
				case BIRTHWEEKOFYEAR: // +birthweekofyear
					{
						if(mob.isPlayer()
						&&(mob.getStartRoom()!=null))
						{
							final TimeClock C = mob.playerStats().getBirthdayClock(mob.getStartRoom().getArea().getTimeObj());
							final int num = C.getWeekOfYear();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
									return false;
							}
						}
					}
					break;
				case _BIRTHWEEKOFYEAR: // -birthweekofyear
					{
						boolean found=false;
						if(mob.isPlayer()
						&&(mob.getStartRoom()!=null))
						{
							final TimeClock C = mob.playerStats().getBirthdayClock(mob.getStartRoom().getArea().getTimeObj());
							final int num = C.getWeekOfYear();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
							final int num = room.getArea().getTimeObj().getDayOfMonth();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
							final int num = room.getArea().getTimeObj().getDayOfMonth();
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
				case BIRTHDAY: // +birthday
					{
						if(mob.isPlayer())
						{
							final int num = mob.playerStats().getBirthday()[PlayerStats.BIRTHDEX_DAY];
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
									return false;
							}
						}
					}
					break;
				case _BIRTHDAY: // -birthday
					{
						boolean found=false;
						if(mob.isPlayer())
						{
							final int num = mob.playerStats().getBirthday()[PlayerStats.BIRTHDEX_DAY];
							for(final Object o : entry.parms())
							{
								if(isDateMatch(o,num))
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
							final String s = (String)o;
							if(s.startsWith("."))
							{
								Class<?> c = E.getClass();
								while(c != null)
								{
									if(c.getName().toUpperCase().endsWith(s))
									{
										found = true;
										break;
									}
									for(final Class<?> c1 : c.getInterfaces())
									{
										if(c1.getName().toUpperCase().endsWith(s))
										{
											found = true;
											break;
										}
									}
									if(found)
										break;
									c=c.getSuperclass();
								}
							}
							else
							if(E.ID().equalsIgnoreCase(s))
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
						final String s = (String)o;
						if(s.startsWith("."))
						{
							for(final Class<?> c : E.getClass().getInterfaces())
							{
								if(c.getName().toUpperCase().endsWith(s))
									return false;
							}
							Class<?> c = E.getClass();
							while(c != null)
							{
								if(c.getName().toUpperCase().endsWith(s))
									return false;
								c=c.getSuperclass();
							}
						}
						else
						if(E.ID().equalsIgnoreCase(s))
							return false;
					}
					break;
				case _RIDE: // -ride
					if((!(E instanceof Rider))||(((Rider)E).riding()==null)||(entry.parms().length==0))
						return false;
					else
					{
						final CompiledZMaskEntry[] sset = fixEntrySet(entry);
						return maskCheck(sset, ((Rider)E).riding(), actual);
					}
				case RIDE: // +ride
					if((!(E instanceof Rider))||(((Rider)E).riding()==null)||(entry.parms().length==0))
						return true;
					else
					{
						final CompiledZMaskEntry[] sset = fixEntrySet(entry);
						return !maskCheck(sset, ((Rider)E).riding(), actual);
					}
				case _FOLLOW: // -follow
					if((mob.amFollowing()==null)||(entry.parms().length==0))
						return false;
					else
					{
						final CompiledZMaskEntry[] sset = fixEntrySet(entry);
						return maskCheck(sset, mob.amFollowing(), actual);
					}
				case FOLLOW: //+follow
					if((mob.amFollowing()==null)||(entry.parms().length==0))
						return true;
					else
					{
						final CompiledZMaskEntry[] sset = fixEntrySet(entry);
						return !maskCheck(sset, mob.amFollowing(), actual);
					}
				case _DEITY: // -deity
					{
						final String worshipCharID=(actual?mob.charStats():mob.baseCharStats()).getWorshipCharID();
						if(worshipCharID.length()==0)
							return false;
						boolean found=false;
						for(final Object o : entry.parms())
						{
							if(worshipCharID.equalsIgnoreCase((String)o)||((String)o).equals("ANY"))
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
						final String worshipCharID=(actual?mob.charStats():mob.baseCharStats()).getWorshipCharID();
						if(worshipCharID.length()>0)
						{
							for(final Object o : entry.parms())
							{
								if(worshipCharID.equalsIgnoreCase((String)o))
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
							final String aName1 = room.getArea().Name();
							final String aName2 = room.getArea().name();
							for(final Object o : entry.parms())
							{
								if(((String)o).startsWith("*"))
								{
									if((aName1.toLowerCase().endsWith(((String)o).substring(1).toLowerCase()))
									||(aName2.toLowerCase().endsWith(((String)o).substring(1).toLowerCase())))
									{
										found = true;
										break;
									}
								}
								else
								if((aName1.equalsIgnoreCase((String)o))
								||(aName2.equalsIgnoreCase((String)o)))
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
							final String aName1 = room.getArea().Name();
							final String aName2 = room.getArea().name();
							for(final Object o : entry.parms())
							{
								if(((String)o).startsWith("*"))
								{
									if((aName1.toLowerCase().endsWith(((String)o).substring(1).toLowerCase()))
									||(aName2.toLowerCase().endsWith(((String)o).substring(1).toLowerCase())))
										return false;
								}
								else
								if((aName1.equalsIgnoreCase((String)o))||(aName2.equalsIgnoreCase((String)o)))
									return false;
							}
						}
						break;
					}
				case _PARENTAREA: // -parentarea
					{
						boolean found=false;
						if(room!=null)
						{
							final Area A=room.getArea();
							final String aName1 = A.Name();
							final String aName2 = A.name();
							for(final Object o : entry.parms())
							{
								if(((String)o).startsWith("*"))
								{
									if((aName1.toLowerCase().endsWith(((String)o).substring(1).toLowerCase()))
									||(aName2.toLowerCase().endsWith(((String)o).substring(1).toLowerCase())))
									{
										found = true;
										break;
									}
								}
								else
								if((aName1.equalsIgnoreCase((String)o))
								||(aName2.equalsIgnoreCase((String)o)))
								{
									found = true;
									break;
								}
								else
								if(A.isParentRecurse((String)o))
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
				case PARENTAREA: // +parentarea
					{
						if(room!=null)
						{
							final Area A=room.getArea();
							final String aName1 = A.Name();
							final String aName2 = A.name();
							for(final Object o : entry.parms())
							{
								if(((String)o).startsWith("*"))
								{
									if((aName1.toLowerCase().endsWith(((String)o).substring(1).toLowerCase()))
									||(aName2.toLowerCase().endsWith(((String)o).substring(1).toLowerCase())))
										return false;
								}
								else
								if((aName1.equalsIgnoreCase((String)o))
								||(aName2.equalsIgnoreCase((String)o)))
									return false;
								else
								if(A.isParentRecurse((String)o))
									return false;
							}
						}
						break;
					}
				case _AREAINSTANCE: // -areainstance
					if(room!=null)
					{
						final Area A=room.getArea();
						if(CMath.bset(A.flags(), Area.FLAG_INSTANCE_CHILD))
							return false;
					}
					break;
				case AREAINSTANCE: // +areainstance
					if(room!=null)
					{
						final Area A=room.getArea();
						if(!CMath.bset(A.flags(), Area.FLAG_INSTANCE_CHILD))
							return false;
					}
					break;
				case _AREABLURB: // -areablurb
					{
						boolean found=false;
						if(room!=null)
						{
							final Area A=room.getArea();
							for(final Enumeration<String> b = A.areaBlurbFlags(); b.hasMoreElements();)
							{
								final String areaBlurb = b.nextElement();
								for(final Object o : entry.parms())
								{
									@SuppressWarnings("unchecked")
									final Triad<Character,String,String> t =(Triad<Character,String,String>)o;
									switch(t.first.charValue())
									{
									case 's':
										if(areaBlurb.endsWith(t.second))
											found = true;
										break;
									case 'e':
										if(areaBlurb.startsWith(t.second))
											found = true;
										break;
									case ' ':
										if(areaBlurb.equals(t.second))
											found = true;
										break;
									}
									if(found)
									{
										if((t.third.length()>0)
										&&(!A.getBlurbFlag(areaBlurb).equalsIgnoreCase(t.third)))
											found=false;
										else
											break;
									}
								}
							}
						}
						if(!found)
							return false;
					}
					break;
				case AREABLURB: // +areablurb
					{
						if(room!=null)
						{
							final Area A=room.getArea();
							for(final Object o : entry.parms())
							{
								for(final Enumeration<String> b = A.areaBlurbFlags(); b.hasMoreElements();)
								{
									final String areaBlurb = b.nextElement();
									@SuppressWarnings("unchecked")
									final Triad<Character,String,String> t =(Triad<Character,String,String>)o;
									switch(t.first.charValue())
									{
									case 's':
										if(areaBlurb.endsWith(t.second))
										{
											if((t.third.length()==0)
											||(A.getBlurbFlag(areaBlurb).equalsIgnoreCase(t.third)))
												return false;
										}
										break;
									case 'e':
										if(areaBlurb.startsWith(t.second))
										{
											if((t.third.length()==0)
											||(A.getBlurbFlag(areaBlurb).equalsIgnoreCase(t.third)))
												return false;
										}
										break;
									case ' ':
										if(areaBlurb.equals(t.second))
										{
											if((t.third.length()==0)
											||(A.getBlurbFlag(areaBlurb).equalsIgnoreCase(t.third)))
												return false;
										}
										break;
									}
								}
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
				case _PLANE: // -plane
				{
					boolean found=false;
					if(E instanceof Physical)
					{
						String planeName = CMLib.flags().getPlaneOfExistence((Physical)E);
						if(planeName == null)
							planeName=L("Prime Material");
						for(final Object o : entry.parms())
						{
							if(planeName.equalsIgnoreCase((String)o))
							{
								found = true;
								break;
							}
						}
					}
					if(!found)
						return false;
					break;
				}
				case PLANE: // +plane
				{
					if(E instanceof Physical)
					{
						String planeName = CMLib.flags().getPlaneOfExistence((Physical)E);
						if(planeName == null)
							planeName=L("Prime Material");
						for(final Object o : entry.parms())
						{
							if(planeName.equalsIgnoreCase((String)o))
								return false;
						}
					}
					break;
				}
				case _HOME: // -home
				{
					boolean found=false;
					final Area A=CMLib.map().getStartArea(E);
					if(A!=null)
					{
						final String planeName=CMLib.flags().getPlaneOfExistence(A);
						for(final Object o : entry.parms())
						{
							if(((String)o).startsWith("*"))
							{
								if(A.Name().toLowerCase().endsWith(((String)o).substring(1).toLowerCase()))
								{
									found = true;
									break;
								}
							}
							else
							if((A.Name().equalsIgnoreCase((String)o))
							||(((String)o).equalsIgnoreCase(planeName)))
							{
								found = true;
								break;
							}
						}
					}
					if(!found)
						return false;
					break;
				}
				case HOME: // +home
					{
						final Area A=CMLib.map().getStartArea(E);
						if(A!=null)
						{
							final String planeName=CMLib.flags().getPlaneOfExistence(A);
							for(final Object o : entry.parms())
							{
								if(((String)o).startsWith("*"))
								{
									if(A.Name().toLowerCase().endsWith(((String)o).substring(1).toLowerCase()))
										return false;
								}
								else
								if((A.Name().equalsIgnoreCase((String)o))
								||(((String)o).equalsIgnoreCase(planeName)))
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
								final Item I = mob.findItem((String)o);
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
						if(!((Item)E).amWearingAt(Wearable.IN_INVENTORY))
							return false; // duh, -worn and +worn should behave differently!
					}
					break;
				case ALIGNMENT: // +alignment
					if((CMParms.contains(entry.parms(),CMLib.flags().getAlignmentName(mob)))
					||(CMParms.contains(entry.parms(),CMLib.flags().getInclinationName(mob))))
						return false;
					break;
				case GENDER: // +gender
				{
					final String genderName;
					if(actual)
					{
						base=getBaseCharStats(base,mob);
						genderName = base.realGenderName().toUpperCase();
					}
					else
						genderName=mob.charStats().genderName().toUpperCase();
					if((CMParms.contains(entry.parms(),""+genderName.charAt(0)))||(CMParms.contains(entry.parms(),genderName)))
						return false;
					break;
				}
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
					if(entry.parms().length>0)
					{
						if(E instanceof Area)
						{
							if(((Area)E).getIStat(Area.Stats.POPULATION)<(((Integer)entry.parms()[0]).intValue()))
								return false;
						}
						else
						if(mob.getGroupMembers(new HashSet<MOB>(1)).size()<(((Integer)entry.parms()[0]).intValue()))
							return false;
					}
					break;
				case _GROUPSIZE: // -groupsize
					if(entry.parms().length>0)
					{
						if(E instanceof Area)
						{
							if(((Area)E).getIStat(Area.Stats.POPULATION)>(((Integer)entry.parms()[0]).intValue()))
								return false;
						}
						else
						if(mob.getGroupMembers(new HashSet<MOB>(1)).size()>(((Integer)entry.parms()[0]).intValue()))
							return false;
					}
					break;
				case _IF: // -if
					{
						boolean oneIsOK = false;
						if(E instanceof PhysicalAgent)
						{
							for(int v=0;v<entry.parms().length-2;v+=3)
							{
								final ScriptingEngine SE = (ScriptingEngine)entry.parms()[v];
								final String[][] eval = (String[][])entry.parms()[v+1];
								final Object[] tmp = (Object[])entry.parms()[v+2];
								final MOB M = SE.getMakeMOB(E);
								final Item defaultItem=(E instanceof Item)?(Item)E:null;
								if(SE.eval(new MPContext((PhysicalAgent)E, M, M,null, defaultItem, null, "", tmp), eval, 0))
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
								final String[][] eval = (String[][])entry.parms()[v+1];
								final Object[] tmp = (Object[])entry.parms()[v+2];
								final MOB M = SE.getMakeMOB(E);
								final Item defaultItem=(E instanceof Item)?(Item)E:null;
								if(E instanceof PhysicalAgent)
								{
									if(SE.eval(new MPContext((PhysicalAgent)E, M, M,null, defaultItem, null, "", tmp), eval, 0))
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

	protected int getTimeValue(final TimeClock C, final ZapperKey key)
	{
		switch(key)
		{
		case WEEK/*ofmonth*/:
		case _WEEK/*ofmonth*/:
			return C.getWeekOfMonth();
		case DAYOFYEAR:
		case _DAYOFYEAR:
			return C.getDayOfYear();
		default:
			return C.get(toTimePeriod(key));
		}
	}

	protected int getTimeMax(final TimeClock C, final ZapperKey key)
	{
		switch(key)
		{
		case WEEK/*ofmonth*/:
		case _WEEK/*ofmonth*/:
			if(C.getDaysInWeek()<1)
				return 0;
			return (C.getDaysInMonth() / C.getDaysInWeek())-1;
		case DAYOFYEAR:
		case _DAYOFYEAR:
			return C.getDaysInYear();
		default:
			return C.getMax(toTimePeriod(key));
		}
	}

	protected TimePeriod toTimePeriod(final ZapperKey key)
	{
		switch(key)
		{
		case HOUR:
		case _HOUR:
			return TimePeriod.HOUR;
		case YEAR:
		case _YEAR:
			return TimePeriod.YEAR;
		case MONTH:
		case _MONTH:
			return TimePeriod.MONTH;
		case SEASON:
		case _SEASON:
			return TimePeriod.SEASON;
		case WEEK/*ofmonth*/:
		case _WEEK/*ofmonth*/:
		case WEEKOFYEAR:
		case _WEEKOFYEAR:
			return TimePeriod.WEEK;
		case DAY:
		case _DAY:
		case DAYOFYEAR:
		case _DAYOFYEAR:
			return TimePeriod.DAY;
		case BIRTHDAY:
		case _BIRTHDAY:
		case BIRTHDAYOFYEAR:
		case _BIRTHDAYOFYEAR:
			return TimePeriod.DAY;
		case BIRTHSEASON:
		case _BIRTHSEASON:
			return TimePeriod.SEASON;
		case BIRTHWEEK:
		case _BIRTHWEEK:
		case BIRTHWEEKOFYEAR:
		case _BIRTHWEEKOFYEAR:
			return TimePeriod.WEEK;
		case BIRTHYEAR:
		case _BIRTHYEAR:
			return TimePeriod.YEAR;
		case BIRTHMONTH:
		case _BIRTHMONTH:
			return TimePeriod.MONTH;
		default:
			return null;
		}
	}

	protected boolean useBirthTimePeriod(final ZapperKey key)
	{
		switch(key)
		{
		case BIRTHDAY:
		case _BIRTHDAY:
		case BIRTHDAYOFYEAR:
		case _BIRTHDAYOFYEAR:
		case BIRTHSEASON:
		case _BIRTHSEASON:
		case BIRTHWEEK:
		case _BIRTHWEEK:
		case BIRTHWEEKOFYEAR:
		case _BIRTHWEEKOFYEAR:
		case BIRTHYEAR:
		case _BIRTHYEAR:
		case BIRTHMONTH:
		case _BIRTHMONTH:
			return true;
		default:
			return false;
		}
	}

	protected boolean containsMaskRange(final CompiledZMaskEntry[] sset, final TimePeriod P)
	{
		for(final CompiledZMaskEntry entry : sset)
			if(toTimePeriod(entry.maskType()) == P)
				return true;
		return false;
	}

	protected TimeClock dateMaskSubEntryToNextTimeClock(final Physical pP, final CompiledZMaskEntry[] set, final boolean[] not)
	{
		final CompiledZMaskEntry[] sset = Arrays.copyOf(set, set.length);
		Arrays.sort(sset, new Comparator<CompiledZMaskEntry>()
		{
			@Override
			public int compare(final CompiledZMaskEntry o1, final CompiledZMaskEntry o2)
			{
				final TimePeriod p1 = toTimePeriod(o1.maskType());
				final TimePeriod p2 = toTimePeriod(o2.maskType());
				if(p1.getIncrement()<p2.getIncrement())
					return -1;
				if(p1.getIncrement()>p2.getIncrement())
					return 1;
				return 0;
			}
		});
		final TimeClock nowC = CMLib.time().homeClock(pP);
		final TimeClock C = (TimeClock)nowC.copyOf();
		if((pP instanceof MOB)&&(((MOB)pP).playerStats()!=null))
		{
			final List<CompiledZMaskEntry> bdEntries = new ArrayList<CompiledZMaskEntry>(3);
			for(final CompiledZMaskEntry entry : set)
			{
				if(useBirthTimePeriod(entry.maskType()))
					bdEntries.add(entry);
			}
			if((bdEntries.size()>0) && (!maskCheck(bdEntries.toArray(new CompiledZMaskEntry[bdEntries.size()]), pP, true)))
				return null;
		}
		final Map<TimePeriod, List<Integer>> okVals = new HashMap<TimePeriod, List<Integer>>();
		for(final CompiledZMaskEntry entry : sset)
		{
			try
			{
				final TimePeriod period = toTimePeriod(entry.maskType());
				if((period == null)||useBirthTimePeriod(entry.maskType()))
					continue;
				List<Integer> okV = okVals.get(period);
				if(okV == null)
				{
					okV = new ArrayList<Integer>();
					okVals.put(period, okV);
				}
				final int min = (period == TimePeriod.YEAR)?C.get(period):C.getMin(period);
				final int max = (period == TimePeriod.YEAR)?C.get(period)+100:getTimeMax(C,entry.maskType());
				for(final Object o : entry.parms())
					addDateValues(o, okV, min, max);
			}
			catch (final NullPointerException n)
			{
			}
		}
		for(final CompiledZMaskEntry entry : sset)
		{
			try
			{
				final TimePeriod period = toTimePeriod(entry.maskType());
				if((period == null)||useBirthTimePeriod(entry.maskType()))
					continue;
				final List<Integer> okV = okVals.get(period);
				if(okV == null) // if null, anything will do!
					continue;
				final int max = (period == TimePeriod.YEAR)?(C.get(period)+100):
								1+getTimeMax(C,entry.maskType());
				boolean useNot = !entry.maskType().name().startsWith("_");
				useNot = (not == null || (!not[0])) ? useNot : !useNot;
				Integer perI = Integer.valueOf(getTimeValue(C,entry.maskType()));
				if(useNot)
				{
					for(int i=0;i<=max;i++) // time for brute force
					{
						if(!okV.contains(perI))
							break;
						else
						{
							C.bump(period, 1);
							perI = Integer.valueOf(getTimeValue(C,entry.maskType()));
						}
					}
				}
				else
				if(!okV.contains(perI))
				{
					int bump = Integer.MAX_VALUE;
					for(final Integer I : okV)
					{
						final int bmp;
						if((I.intValue()>perI.intValue())||(period == TimePeriod.YEAR))
							bmp = I.intValue()-perI.intValue();
						else
						{
							if(period == TimePeriod.HOUR)
								bmp = 1+(max - perI.intValue()) + I.intValue();
							else
								bmp = (max - perI.intValue()) + I.intValue();
						}
						if(bmp < bump)
						{
							bump = bmp;
							if(bump < 2)
								break;
						}
					}
					if(bump != Integer.MAX_VALUE)
						C.bump(period, bump);
				}
				// if months matter, set days and lower to min
				{
					for(final TimePeriod P : TimePeriod.values())
					{
						if((P.getIncrement() < period.getIncrement())
						&&(!containsMaskRange(sset,P)))
						{
							switch(P)
							{
							case DAY: // period is month, week, year, etc
								if(period == TimePeriod.WEEK)
									C.set(TimePeriod.DAY, (C.getWeekOfMonth()*C.getDaysInWeek())+1);
								else
								if(!containsMaskRange(sset,TimePeriod.WEEK))
									C.set(P, C.getMin(P));
								break;
							case MONTH: // period must be year
							case HOUR: // period is year, month, day, etc
								C.set(P, C.getMin(P));
								break;
							default:
								break;
							}
						}
					}
				}
			}
			catch (final NullPointerException n)
			{
			}
		}
		return C;
	}

	@Override
	public TimeClock dateMaskToExpirationTimeClock(final Physical P, final CompiledZMask cset)
	{
		final Pair<CompiledZMaskEntry[], TimeClock> clock = dateMasksToNextTimeClock(P, cset);
		if((clock == null)||(clock.second==null))
			return null;
		TimePeriod lowestPeriodC = null;
		ZapperKey lowestKey = null;
		for(int i=0;i<clock.first.length;i++)
		{
			final CompiledZMaskEntry entry = clock.first[i];
			final TimePeriod period = toTimePeriod(entry.maskType());
			if((period == null)||(period==TimePeriod.ALLTIME))
				continue;
			if((lowestPeriodC == null)
			||(lowestPeriodC.getIncrement()>period.getIncrement()))
			{
				lowestPeriodC = period;
				lowestKey = entry.maskType();
			}
		}

		if(lowestPeriodC == null)
			return clock.second;
		final TimeClock C = (TimeClock)clock.second.copyOf();
		final int max = (lowestPeriodC == TimePeriod.YEAR)?C.get(lowestPeriodC)+100:getTimeMax(C,lowestKey);
		for(int i=0;i<max;i++)
		{
			C.bump(lowestPeriodC, 1);
			if(!maskCheckDateEntries(clock.first, C))
				return C;
		}
		return clock.second;
	}

	@Override
	public TimeClock dateMaskToNextTimeClock(final Physical P, final CompiledZMask cset)
	{
		final Pair<CompiledZMaskEntry[], TimeClock> clock = dateMasksToNextTimeClock(P, cset);
		if((clock == null)||(clock.second==null))
			return null;
		return clock.second;
	}

	protected Pair<CompiledZMaskEntry[], TimeClock> dateMasksToNextTimeClock(final Physical P, final CompiledZMask cset)
	{
		final boolean[] not = new boolean[] {false};
		if(cset.entries().length<3)
		{
			final CompiledZMaskEntry[] e = cset.entries()[0];
			return new Pair<CompiledZMaskEntry[], TimeClock>(e, dateMaskSubEntryToNextTimeClock(P, e, not));
		}
		else
		{
			TimeClock lastValue = null;
			CompiledZMaskEntry[] lastE = null;
			boolean lastConnectorNot = false;
			for(int i=0;i<cset.entries().length;i+=2)
			{
				final TimeClock C = dateMaskSubEntryToNextTimeClock(P, cset.entries()[i], new boolean[] { lastConnectorNot });
				if(C == null)
					continue;
				if((lastValue == null)||(lastValue.isBefore(C)))
				{
					lastValue = C;
					lastE = cset.entries()[i];
				}
				if(i==cset.entries().length-1)
					return new Pair<CompiledZMaskEntry[], TimeClock>(lastE, lastValue);
				final CompiledZMaskEntry entry = cset.entries()[i+1][0];
				if(entry.maskType()==MaskingLibrary.ZapperKey._OR)
					lastConnectorNot=true;
				else
				if(entry.maskType()==MaskingLibrary.ZapperKey.OR)
					lastConnectorNot=false;
			}
			return new Pair<CompiledZMaskEntry[], TimeClock>(lastE, lastValue);
		}
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
		if(cset.entries().length<3)
			return maskCheckSubEntries(cset.entries()[0],E);
		else
		{
			boolean lastValue = false;
			boolean lastConnectorNot = false;
			for(int i=0;i<cset.entries().length;i+=2)
			{
				boolean subResult =  maskCheckSubEntries(cset.entries()[i],E);
				if(lastConnectorNot)
					subResult = !subResult;
				lastValue = lastValue || subResult;
				if(i==cset.entries().length-1)
					return lastValue;
				final CompiledZMaskEntry entry = cset.entries()[i+1][0];
				if(entry.maskType()==MaskingLibrary.ZapperKey._OR)
					lastConnectorNot=true;
				else
				if(entry.maskType()==MaskingLibrary.ZapperKey.OR)
					lastConnectorNot=false;
				else
					Log.errOut("Badly compiled zappermask @ "+E.name());
			}
			return lastValue;
		}
	}

	protected boolean maskCheckSubEntries(final CompiledZMaskEntry set[], final PlayerLibrary.ThinPlayer E)
	{
		//boolean[] flags=(boolean[])cset.firstElement();
		for(final CompiledZMaskEntry entry : set)
		{
			try
			{
				switch(entry.maskType())
				{
				case OR: //+or
				case _OR: //-or
					Log.errOut("Badly compiled zappermask @ "+E.name());
					break;
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
				case _DEITY: // -Deity
					{
						if(E.worship().trim().length()==0)
							return false;
						boolean found=false;
						for(final Object o : entry.parms())
						{
							if(E.worship().equalsIgnoreCase((String)o)||((String)o).equals("ANY"))
							{
								found = true;
								break;
							}
						}
						if(!found)
							return false;
					}
					break;
				case DEITY: // +Deity
					{
						if(E.worship().trim().length()>0)
						{
							for(final Object o : entry.parms())
							{
								if(E.worship().equalsIgnoreCase((String)o))
									return false;
							}
						}
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
				case LEVEL: // +level
				{
					final int level=E.level();
					for(int v=0;v<entry.parms().length-1;v+=2)
					{
						switch((ZapperKey)entry.parms()[v])
						{
						case LVLGR: // -lvlgr
							if(level>((Integer)entry.parms()[v+1]).intValue())
								return false;
							break;
						case LVLGE: // -lvlge
							if(level>=((Integer)entry.parms()[v+1]).intValue())
								return false;
							break;
						case LVLLT: // -lvlt
							if(level<((Integer)entry.parms()[v+1]).intValue())
								return false;
							break;
						case LVLLE: // -lvlle
							if(level<=((Integer)entry.parms()[v+1]).intValue())
								return false;
							break;
						case LVLEQ: // -lvleq
							if(level==((Integer)entry.parms()[v+1]).intValue())
								return false;
							break;
						default:
							break;
						}
					}
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
				case _RIDE: //-ride
				case _FOLLOW: //-follow
					return false; // always the end of the line
				case RIDE: // +ride
				case FOLLOW: //+follow
					return true; // always the end of the line
				case ACCOUNT:// +accounts
				case _ACCOUNT:// -accounts
				case _ALIGNMENT: // -alignment
				case _GENDER: // -gender
				case _CLASSLEVEL: // -classlevel
				case CLASSLEVEL: // +classlevel
				case _MAXCLASSLEVEL: // -maxclasslevel
				case MAXCLASSLEVEL: // +maxclasslevel
				case ANYCLASSLEVEL: // +anyclasslevel
				case _ANYCLASSLEVEL: // -anyclasslevel
				case CLANLEVEL: // +clanlevel
				case _CLANLEVEL: // -clanlevel
				case _TATTOO: // -tattoo
				case TATTOO: // +tattoo
				case _MOOD: // -mood
				case MOOD: // +mood
				case _OFFICER: // -officer
				case OFFICER: // +officer
				case _JUDGE: // -judge
				case JUDGE: // +judge
				case NPC: // +npc
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
				case PLAYER: // +player
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
				case WEATHER: // +weather
				case _WEATHER: // -weather
				case DOMAIN: // +domain
				case _DOMAIN: // -domain
				case SEASON: // +season
				case _SEASON: // -season
				case MONTH: // +month
				case _MONTH: // -month
				case WEEK: // +week
				case _WEEK: // -week
				case WEEKOFYEAR: // +weekofyear
				case _WEEKOFYEAR: // -weekofyear
				case YEAR: // +year
				case _YEAR: // -year
				case DAY: // +day
				case _DAY: // -day
				case DAYOFYEAR: // +dayofyear
				case _DAYOFYEAR: // -dayofyear
				case BIRTHSEASON: // +birthseason
				case _BIRTHSEASON: // -birthseason
				case BIRTHMONTH: // +birthmonth
				case _BIRTHMONTH: // -mbirthonth
				case BIRTHWEEK: // +birthweek
				case _BIRTHWEEK: // -birthweek
				case BIRTHWEEKOFYEAR: // +birthweekofyear
				case _BIRTHWEEKOFYEAR: // -birthweekofyear
				case BIRTHYEAR: // +birthyear
				case _BIRTHYEAR: // -birthyear
				case BIRTHDAY: // +birthday
				case _BIRTHDAY: // -birthday
				case BIRTHDAYOFYEAR: // +birthdayofyear
				case _BIRTHDAYOFYEAR: // -birthdayofyear
				case QUALLVL: // +quallvl
				case _QUALLVL: // -quallvl
				case RESOURCE: // +resource
				case _RESOURCE: // -resource
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
				case PARENTAREA: // +parentarea
				case _PARENTAREA: // -parentarea
				case _AREAINSTANCE: // -areainstance
				case AREAINSTANCE: // +areainstance
				case _AREABLURB: // -areablurb
				case AREABLURB: // +areablurb
				case _HOME: // -home
				case HOME: // +home
				case _PLANE: // -plane
				case PLANE: // +plane
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
				case LOCATION: // +location
				case _LOCATION: // -location
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
				case PORT: // +PORT
					{
						final MudHost host=CMLib.host();
						for(final Object o : entry.parms())
						{
							if(host.getPort()==((Integer)o).intValue())
								return false;
						}
					}
					break;
				case _PORT: // -PORT
					{
						final MudHost host=CMLib.host();
						boolean found=false;
						for(final Object o : entry.parms())
						{
							if(host.getPort()==((Integer)o).intValue())
							{
								found=true;
								break;
							}
						}
						if(!found)
							return false;
					}
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
	public String separateZapperMask(final String newText)
	{
		if(newText == null)
			return "";
		return separateZapperMask(CMParms.parse(newText));
	}

	@Override
	public String separateZapperMask(final List<String> p)
	{
		if(p == null)
			return "";
		int start = 0;
		for(start=0;start<p.size();start++)
		{
			final String s = p.get(start);
			if(s.startsWith("+")||s.startsWith("-"))
				break;
		}
		if(start>=p.size())
			return "";
		int end = p.size()-1;
		for(end=p.size()-1;end>start;end--)
		{
			final String s = p.get(end);
			if(s.startsWith("+")||s.startsWith("-"))
				break;
		}
		if(end == start)
			return p.get(start);
		if(end == p.size()-1)
			return CMParms.combineQuoted(p, start, end+1);
		final Map<String,ZapperKey> zapCodes = this.getMaskCodes();
		final ZapperKey key = zapCodes.get(p.get(end).toUpperCase());
		if(key != null)
		{
			switch(key)
			{
			case IF:
			case _IF:
				end++;
				break;
			case _VALUE:
			case VALUE:
			case _ABILITY:
			case ABILITY:
			case _WEIGHT:
			case WEIGHT:
			case _ARMOR:
			case ARMOR:
			case _DAMAGE:
			case DAMAGE:
			case _ATTACK:
			case ATTACK:
			case _GROUPSIZE:
			case GROUPSIZE:
			case _STRENGTH:
			case STRENGTH:
			case _INTELLIGENCE:
			case INTELLIGENCE:
			case _WISDOM:
			case WISDOM:
			case _DEXTERITY:
			case DEXTERITY:
			case _CONSTITUTION:
			case CONSTITUTION:
			case _CHARISMA:
			case CHARISMA:
				while((end+1<p.size())
				&&(CMath.isInteger(p.get(end+1))))
					end++;
				break;
			default:
				break;
			}
		}
		return CMParms.combineQuoted(p, start, end+1);
	}

	@Override
	public String[] separateMaskStrs(final String newText)
	{
		final String[] strs=new String[2];
		if(newText == null)
		{
			strs[0]="";
			strs[1]="";
			return strs;
		}
		final int maskindex=newText.toUpperCase().lastIndexOf("MASK=");
		if(maskindex>0)
		{
			strs[0]=newText.substring(0,maskindex).trim();
			strs[1]=newText.substring(maskindex+5).trim();
		}
		else
		{
			strs[0]=newText;
			strs[1]="";
		}
		return strs;
	}

	@Override
	public CompiledZMask parseSpecialItemMask(final List<String> parsed)
	{
		SpecialItemType type = null;
		final StringBuilder finalMaskStr = new StringBuilder("");
		for(int i=0;i<parsed.size();i++)
		{
			String s = parsed.get(i).trim();
			if(s.length()==0)
				continue;
			if(Character.isDigit(s.charAt(0)))
			{
				if(s.endsWith("-")&&(i<parsed.size()-1)&&(CMath.isInteger(parsed.get(i+1))))
					s=s+parsed.remove(i+1);
				int x = s.indexOf('-');
				if((x<0)&&(i<parsed.size()-1)&&(parsed.get(i+1).startsWith("-")))
				{
					x=s.length();
					s+=parsed.remove(i+1);
				}
				if(x<0)
				{
					final int level = CMath.s_int(s);
					if(level <= 0)
					{
						parsed.add(0,L("@x1 is not a valid level or level range.",s));
						return null;
					}
					finalMaskStr.append(" -LEVEL +="+level);
					continue;
				}
				else
				if(x==s.length()-1)
				{
					final int level = CMath.s_int(s.substring(0,x));
					if(level <= 0)
					{
						parsed.add(0,L("@x1 is not a valid level or level range.",s));
						return null;
					}
					finalMaskStr.append(" -LEVEL +>="+level);
					continue;
				}
				else
				if(x==0)
				{
					final int level = CMath.s_int(s.substring(0,x));
					if(level <= 0)
					{
						parsed.add(0,L("@x1 is not a valid level or level range.",s));
						return null;
					}
					finalMaskStr.append(" -LEVEL +<="+level);
					continue;
				}
				else
				{
					final int minlevel = CMath.s_int(s.substring(0,x));
					final int maxlevel = CMath.s_int(s.substring(x+1));
					if((minlevel<=0)||(maxlevel <= 0))
					{
						parsed.add(0,L("@x1 is not a valid level or level range.",s));
						return null;
					}
					finalMaskStr.append(" +LEVEL -<"+minlevel+" ->"+maxlevel);
					continue;
				}
			}
			final String us = s.toUpperCase();
			if(CMath.s_valueOf(SpecialItemType.class, us) != null)
			{
				type = (SpecialItemType)CMath.s_valueOf(SpecialItemType.class, us);
				switch(type)
				{
				case ARMOR:
					finalMaskStr.append(" -JAVACLASS +GenArmor +StdArmor");
					break;
				case RESOURCE:
					finalMaskStr.append(" -JAVACLASS +GenResource +GenLiquidResource +GenFoodResource");
					break;
				case RING:
					finalMaskStr.append(" -WORNON +finger");
					break;
				case WAND:
					finalMaskStr.append(" -JAVACLASS +Wand +GenWand +StdWand");
					break;
				case WEAPON:
					finalMaskStr.append(" -JAVACLASS +Weapon +GenWeapon +StdWeapon");
					break;
				case FOOD:
					finalMaskStr.append(" -JAVACLASS +Food +StdFood +GenFood +GenFoodResource");
					break;
				case DRINK:
					finalMaskStr.append(" -JAVACLASS +Drink +StdDrink +GenDrink +GenLiquidResource");
					break;
				case POTION:
					finalMaskStr.append(" -JAVACLASS +Potion +StdPotion +GenPotion");
					break;
				}
				continue;
			}
			if((us.equalsIgnoreCase("NAME")||us.equalsIgnoreCase("NAMED"))
			&&(i<parsed.size()-1))
			{
				finalMaskStr.append(" -NAME +\""+CMParms.combine(parsed,i+1)+"\"");
				break;
			}
			final int cd = RawMaterial.CODES.FIND_IgnoreCase(us);
			if(cd >= 0)
			{
				finalMaskStr.append(" -RESOURCE +"+RawMaterial.CODES.NAME(cd));
				continue;
			}
			if(type != null)
			{
				switch(type)
				{
				case ARMOR:
				{
					final long wc = Wearable.CODES.FIND_ignoreCase(us);
					if(wc >=0)
					{
						finalMaskStr.append(" -WORNON +\""+Wearable.CODES.NAME(wc)+"\"");
						continue;
					}
					parsed.add(0,L("@x1 is not a valid term for armor.",s));
					return null;
				}
				case WEAPON:
				{
					boolean found=false;
					for(int cl=0;cl<Weapon.TYPE_DESCS.length;cl++)
					{
						if(s.equalsIgnoreCase(Weapon.TYPE_DESCS[cl]))
						{
							finalMaskStr.append(" -WEAPONTYPE +\""+Weapon.TYPE_DESCS[cl]+"\"");
							found=true;
							break;
						}
					}
					if(found)
						continue;
					for(int cl=0;cl<Weapon.CLASS_DESCS.length;cl++)
					{
						if(s.equalsIgnoreCase(Weapon.CLASS_DESCS[cl]))
						{
							finalMaskStr.append(" -WEAPONCLASS +\""+Weapon.CLASS_DESCS[cl]+"\"");
							found=true;
							break;
						}
					}
					if(found)
						continue;
					for(int cl=0;cl<Weapon.TYPE_DESCS.length;cl++)
					{
						if(us.startsWith(Weapon.TYPE_DESCS[cl]))
						{
							finalMaskStr.append(" -WEAPONTYPE +\""+Weapon.TYPE_DESCS[cl]+"\"");
							found=true;
							break;
						}
					}
					if(found)
						continue;
					for(int cl=0;cl<Weapon.CLASS_DESCS.length;cl++)
					{
						if(us.startsWith(Weapon.CLASS_DESCS[cl]))
						{
							finalMaskStr.append(" -WEAPONCLASS +\""+Weapon.CLASS_DESCS[cl]+"\"");
							found=true;
							break;
						}
					}
					if(found)
						continue;
					parsed.add(0,L("@x1 is not a valid term for armor.",s));
					return null;
				}
				case POTION:
				case WAND:
				{
					Ability A =CMClass.getAbilityByName(s,true);
					if(A == null)
						A = CMClass.getAbilityByName(s,false);
					if(A != null)
					{
						finalMaskStr.append(" -SKILL +"+A.ID());
						continue;
					}
					parsed.add(0,L("@x1 is not a valid term for potions and wands.",s));
					return null;
				}
				default:
					parsed.add(0,L("@x1 is not a valid term for @x2s.",s,type.name().toLowerCase()));
					return null;
				}
			}
			boolean found=false;
			for(int cl=0;cl<Weapon.CLASS_DESCS.length;cl++)
			{
				if(s.equalsIgnoreCase(Weapon.CLASS_DESCS[cl]))
				{
					finalMaskStr.append(" -JAVACLASS +Weapon +GenWeapon +StdWeapon");
					finalMaskStr.append(" -WEAPONCLASS +\""+Weapon.CLASS_DESCS[cl]+"\"");
					found=true;
					break;
				}
			}
			if(found)
				continue;
			for(int cl=0;cl<Weapon.CLASS_DESCS.length;cl++)
			{
				if(us.startsWith(Weapon.CLASS_DESCS[cl]))
				{
					finalMaskStr.append(" -JAVACLASS +Weapon +GenWeapon +StdWeapon");
					finalMaskStr.append(" -WEAPONCLASS +\""+Weapon.CLASS_DESCS[cl]+"\"");
					found=true;
					break;
				}
			}
			if(found)
				continue;
			parsed.add(0,L("@x1 is not a valid term for an unknown type.",s));
			return null;
		}
		return maskCompile(finalMaskStr.toString());
	}
}
