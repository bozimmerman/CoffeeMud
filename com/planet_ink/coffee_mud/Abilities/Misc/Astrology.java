package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/*
   Copyright 2022-2022 Bo Zimmerman

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
public class Astrology extends StdAbility
{
	@Override
	public String ID()
	{
		return "Astrology";
	}

	private final static String	localizedName	= CMLib.lang().L("Astrology");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public void setMiscText(final String newText)
	{
		if(newText.equals("RESET"))
		{
			savedHash = null;
			lastClock = null;
			affectHash	= null;
			affects.clear();
			myEntries.clear();
		}
		else
			super.setMiscText(newText);
	}

	protected volatile int[]		savedHash	= null;
	protected TimeClock				lastClock	= null;
	protected volatile int[]		affectHash	= null;
	protected final List<CMObject>	affects		= new Vector<CMObject>();

	protected final Map<TimePeriod, AstrologyEntry[]>	myEntries	= new Hashtable<TimePeriod, AstrologyEntry[]>();

	protected final static class AstrologyEntry
	{
		public final String					playerMask;
		public final String					dateMask;
		public final CompiledZMask			playerCMask;
		public final CompiledZMask			dateCMask;
		public final Pair<String, String>[]	props;
		public final TimePeriod				period;
		private final int					hashCode;

		public AstrologyEntry(final String pMask, final String dMask, final PairList<String,String> ps)
		{
			playerMask = pMask;
			playerCMask = CMLib.masking().maskCompile(playerMask);
			dateMask = dMask;
			dateCMask = CMLib.masking().maskCompile(dateMask);
			@SuppressWarnings("unchecked")
			final Pair<String,String>[] base = new Pair[ps.size()];
			props = ps.toArray(base);
			TimePeriod per = null;
			final String udmask = dMask.toUpperCase();
			for(final TimePeriod p : TimePeriod.values())
			{
				if(udmask.indexOf(p.name())>=0)
				{
					per=p;
					break;
				}
			}
			period = (per == null) ? TimePeriod.YEAR : per;
			int hc = 0;
			hc = playerCMask.hashCode() ^ dateCMask.hashCode() ^ period.hashCode();
			for(final Pair<String,String> p : props)
				hc = (hc << 8) ^ (p.first.hashCode() ^ p.second.hashCode());
			hashCode = hc;
		}

		@Override
		public int hashCode()
		{
			return hashCode;
		}
	}

	protected static int getAllAstrologyHash()
	{
		final Integer hash = (Integer)Resources.getResource("SYSTEM_ASTROLOGY_HASH");
		if(hash != null)
			return hash.intValue();
		int hashh = 0;
		for(final AstrologyEntry a : getAllAstrology())
			hashh = (hashh << 8) ^ a.hashCode();
		Resources.submitResource("SYSTEM_ASTROLOGY_HASH", Integer.valueOf(hashh));
		return hashh;
	}

	protected static boolean addProp(final PairList<String,String> fprops, final String propID, final String arg, final String s)
	{
		final Ability A=CMClass.getAbility(propID);
		if(A==null)
		{
			final Behavior B = CMClass.getBehavior(propID);
			if(B == null)
			{
				Log.errOut("Astrology","Unknown ability/behav id "+propID+" in "+s);
				return false;
			}
			else
				fprops.add(B.ID(),arg);
		}
		else
			fprops.add(A.ID(),arg);
		return true;
	}

	@SuppressWarnings("unchecked")
	protected static List<AstrologyEntry> getAllAstrology()
	{
		List<AstrologyEntry> astro = (List<AstrologyEntry>)Resources.getResource("SYSTEM_ASTROLOGY_INI");
		if(astro == null)
		{
			synchronized("SYSTEM_ASTROLOGY_INI".intern())
			{
				astro = (List<AstrologyEntry>)Resources.getResource("SYSTEM_ASTROLOGY_INI");
				if(astro != null)
					return astro;
				astro = new Vector<AstrologyEntry>();
				final List<String> lines = Resources.getFileLineVector(new CMFile(Resources.makeFileResourceName("astrology.txt"),null).text());
				for(String s : lines)
				{
					s=s.trim();
					if(s.startsWith("#"))
						continue;
					final int x1 = s.indexOf("::");
					if(x1<0)
						continue;
					final int x2 = s.indexOf("::",x1+2);
					if(x2<0)
						continue;
					final String pmask = s.substring(0,x1).trim();
					final String dmask = s.substring(x1+2, x2).trim();
					final String propStr = s.substring(x2+2).trim();
					int state=0;
					final PairList<String,String> fprops = new PairVector<String,String>();
					String propID="";
					final StringBuilder str=new StringBuilder("");
					for(int i=0;i<propStr.length();i++)
					{
						final char c=propStr.charAt(i);
						switch(state)
						{
						case 0: // between things
							if((c=='(')&&(propID.length()>0))
								state=2;
							else
							if(!Character.isWhitespace(c))
							{
								if(propID.length()>0)
								{
									if(!Astrology.addProp(fprops, propID, "", s))
									{
										i=propStr.length();
										propID="";
										break;
									}
									propID="";
									str.setLength(0);
								}
								str.append(c);
								state=1;
							}
							break;
						case 1: // in-proper-id
							if(Character.isWhitespace(c))
							{
								propID=str.toString();
								state=0;
							}
							else
							if(c=='(')
							{
								propID=str.toString();
								str.setLength(0);
								state=2;
							}
							else
								str.append(c);
							break;
						case 2: // in arg paren
							if((c=='\\')&&(i<propStr.length()-1))
							{
								i++;
								str.append(propStr.charAt(i));
							}
							else
							if(c==')')
							{
								final String args=str.toString();
								str.setLength(0);
								if(!Astrology.addProp(fprops, propID, args, s))
								{
									i=propStr.length();
									propID="";
									break;
								}
								propID="";
								state=0;
							}
							else
								str.append(c);
							break;
						}
					}
					if(propID.length()>0)
						Astrology.addProp(fprops, propID, str.toString(), s);
					else
					if(str.length()>0)
						Astrology.addProp(fprops, str.toString(),"", s);
					final AstrologyEntry entry = new AstrologyEntry(pmask,dmask,fprops);
					astro.add(entry);
				}
				Resources.removeResource("SYSTEM_ASTROLOGY_HASH");
				Resources.submitResource("SYSTEM_ASTROLOGY_INI", astro);
			}
		}
		return astro;
	}

	public void gatherEntries(final Physical P, final AstrologyEntry[] entries, final List<AstrologyEntry> apply, final int[] hash)
	{
		if((entries == null)||(entries.length==0))
			return;
		final MaskingLibrary mlib = CMLib.masking();
		for(final AstrologyEntry E : entries)
		{
			if(mlib.maskCheck(E.dateCMask, P, true))
			{
				apply.add(E);
				hash[0] ^= E.hashCode();
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		final Physical affected=this.affected;
		if(affected != null)
		{
			if(lastClock == null)
				lastClock = (TimeClock)CMClass.getCommon("DefaultTimeClock");
			final int astroHash = getAllAstrologyHash();
			if((savedHash==null)
			||(savedHash[0]!=astroHash))
			{
				if(savedHash == null)
					savedHash = new int[] { 0 };
				savedHash[0] = astroHash;
				assignMyEntries(affected);
			}
			if(myEntries.size()>0)
			{
				final TimeClock now = CMLib.time().homeClock(affected);
				if((now != null)
				&& (now.getHourOfDay() != lastClock.getHourOfDay()))
				{
					if((myEntries.containsKey(TimePeriod.HOUR))
					||((now.getDayOfYear() != lastClock.getDayOfYear())&&(myEntries.containsKey(TimePeriod.DAY)))
					||((now.getWeekOfYear() != lastClock.getWeekOfYear())&&(myEntries.containsKey(TimePeriod.WEEK)))
					||((now.getMonth() != lastClock.getMonth())&&(myEntries.containsKey(TimePeriod.MONTH)))
					||((now.getSeasonCode() != lastClock.getSeasonCode())&&(myEntries.containsKey(TimePeriod.SEASON)))
					||((now.getYear() != lastClock.getYear())&&(myEntries.containsKey(TimePeriod.YEAR))))
					{
						final List<AstrologyEntry> chk = new ArrayList<AstrologyEntry>();
						final int[] eHash = new int[] {0};
						final Room R=CMLib.map().roomLocation(affected);
						for(final TimePeriod key : myEntries.keySet())
							gatherEntries(R, myEntries.get(key), chk, eHash);
						if((affectHash==null)
						||(affectHash[0] != eHash[0]))
						{
							if(affectHash == null)
								affectHash = new int[1];
							affectHash[0] = eHash[0];
							affects.clear();
							for(final AstrologyEntry aE : chk)
							{
								for(final Pair<String,String> pE : aE.props)
								{
									final Ability A = CMClass.getAbility(pE.first);
									if(A != null)
									{
										A.setMiscText(pE.second);
										A.setAffectedOne(affected);
										A.setProficiency(100);;
										affects.add(A);
									}
									else
									{
										final Behavior B=CMClass.getBehavior(pE.first);
										if(B != null)
										{
											B.setParms(pE.second);
											if(affected instanceof PhysicalAgent)
												B.startBehavior((PhysicalAgent)affected);
											affects.add(B);
										}
									}
								}
							}
						}
						lastClock.setFromHoursSinceEpoc(now.toHoursSinceEpoc());
						affected.recoverPhyStats();
						if(affected instanceof MOB)
						{
							((MOB)affected).recoverCharStats();
							((MOB)affected).recoverMaxState();
						}
					}
				}
				for(final CMObject p : affects)
				{
					if(p instanceof Tickable)
						((Tickable)p).tick(ticking, tickID);
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		for(final CMObject p : affects)
		{
			if(p instanceof MsgListener)
			{
				((MsgListener)p).executeMsg(myHost, msg);
			}
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		for(final CMObject p : affects)
		{
			if(p instanceof MsgListener)
			{
				if(!((MsgListener)p).okMessage(myHost, msg))
					return false;
			}
		}
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null)
			return;
		for(final CMObject p : affects)
		{
			if(p instanceof StatsAffecting)
				((StatsAffecting)p).affectPhyStats(affected, affectableStats);
		}
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected, affectableStats);
		if(affected==null)
			return;
		for(final CMObject p : affects)
		{
			if(p instanceof StatsAffecting)
				((StatsAffecting)p).affectCharStats(affected, affectableStats);
		}
	}

	@Override
	public void affectCharState(final MOB affected, final CharState affectableStats)
	{
		super.affectCharState(affected, affectableStats);
		if(affected==null)
			return;
		for(final CMObject p : affects)
		{
			if(p instanceof StatsAffecting)
				((StatsAffecting)p).affectCharState(affected, affectableStats);
		}
	}
	protected void assignMyEntries(final Physical P)
	{
		final Map<TimePeriod,List<AstrologyEntry>> newMap = new HashMap<TimePeriod,List<AstrologyEntry>>();
		final List<AstrologyEntry> all = getAllAstrology();
		final HashMap<CompiledZMask,Boolean> tried = new HashMap<CompiledZMask,Boolean>();
		for(final AstrologyEntry a : all)
		{
			Boolean b;
			if((a.playerCMask == null)||(a.playerCMask.empty()))
				b=Boolean.TRUE;
			else
			{
				b = tried.get(a.playerCMask);
				if(b == null)
					b = Boolean.valueOf(CMLib.masking().maskCheck(a.playerCMask, P, true));
			}
			if(b.booleanValue())
			{
				List<AstrologyEntry> tuEntries = newMap.get(a.period);
				if(tuEntries == null)
				{
					tuEntries = new ArrayList<AstrologyEntry>();
					newMap.put(a.period, tuEntries);
				}
				tuEntries.add(a);
			}
		}
		myEntries.clear();
		for(final TimePeriod k : newMap.keySet())
			myEntries.put(k, newMap.get(k).toArray(new AstrologyEntry[0]));
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		if((P!=null)&&(P != affected))
			savedHash = null;
		super.setAffectedOne(P);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=getTarget(mob,commands,givenTarget);

		if(target==null)
			return false;
		if(target.fetchEffect(ID())!=null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,"");
			if(target.location()!=null)
			{
				if(target.location().okMessage(target,msg))
				{
					target.location().send(target,msg);
					final Ability A=(Ability)copyOf();
					target.addNonUninvokableEffect(A);
				}
			}
			else
			{
				final Ability A=(Ability)copyOf();
				target.addNonUninvokableEffect(A);
			}
		}
		return success;
	}
}
