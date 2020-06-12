package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.PlanarVar;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.Align;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.Map.Entry;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class Fighter_PlanarVeteran extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_PlanarVeteran";
	}

	private final static String localizedName = CMLib.lang().L("Planar Veteran");

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
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_NATURELORE;
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

	private static final Integer[] landClasses = { Integer.valueOf(-1) };

	public Integer[] landClasses()
	{
		return landClasses;
	}

	protected volatile long		nextExpire	= Long.MAX_VALUE;
	protected volatile Area		lastArea	= null;
	protected Map<String, Long>	visits		= new STreeMap<String, Long>();
	protected final static CMParms.DelimiterChecker COMMA_DELIMITER=CMParms.createDelimiter(new char[]{','});

	protected final static int STAT_ARMOR=0;
	protected final static int STAT_DAMAGE=1;
	protected final static int STAT_MOVES=2;
	protected final static int STAT_HITPOINTS=3;
	protected final static int STAT_SPEED=4;
	protected final static int STAT_MINDSAVE=5;
	protected final static int STAT_ATTACK=6;
	protected final static int STAT_ALL=7;

	protected final int[] statBonus	= new int[STAT_ALL];

	protected synchronized void rebuildBonuses()
	{
		Arrays.fill(statBonus, 0);
		final PlanarAbility planeA=(PlanarAbility)CMClass.getAbility("StdPlanarAbility");
		for(final Iterator<String> pl = getUpperVisitations().keySet().iterator();pl.hasNext(); )
		{
			final String plane = pl.next();
			final Map<String,String> planeVars = planeA.getPlanarVars(plane);
			if((planeVars != null)
			&&(planeVars.size()>0))
			{
				final String catStr=planeVars.get(PlanarVar.CATEGORY.toString());
				if(catStr != null)
				{
					final List<String> categories=CMParms.parseCommas(catStr.toLowerCase(), true);
					if(categories.contains("inner"))
						statBonus[STAT_ARMOR] += 2;
					if(categories.contains("transitional"))
						statBonus[STAT_SPEED] += 10;
					final Faction aF=CMLib.factions().getFaction(CMLib.factions().getAlignmentID());
					if(categories.contains("outer"))
					{
						final List<com.planet_ink.coffee_mud.Common.interfaces.Faction.Align> aligns = new
								ArrayList<com.planet_ink.coffee_mud.Common.interfaces.Faction.Align>();
						if(aF!=null)
						{
							final int align = CMath.s_int(planeVars.get(PlanarVar.ALIGNMENT.name()));
							final Faction.FRange range = aF.fetchRange(align);
							if((range != null)&&(range.alignEquiv()!=Align.INDIFF))
								aligns.add(range.alignEquiv());
						}
						final String factions = planeVars.get(PlanarVar.FACTIONS.toString());
						if(factions!=null)
						{
							final PairList<String,String> factionList=new PairVector<String,String>(CMParms.parseSpaceParenList(factions));
							for(final Pair<String,String> p : factionList)
							{
								final String factionName = p.first;
								if(p.first.equals("*"))
									continue;
								Faction F=null;
								if(CMLib.factions().isFactionID(factionName))
									F=CMLib.factions().getFaction(factionName);
								if(F==null)
									F=CMLib.factions().getFactionByName(factionName);
								if(F!=null)
								{
									Faction.FRange FR;
									if(CMath.isInteger(p.second))
										FR=F.fetchRange(CMath.s_int(p.second));
									else
										FR = F.fetchRange(p.second);
									if((FR != null)&&(FR.alignEquiv()!=Align.INDIFF))
										aligns.add(FR.alignEquiv());
								}
							}
						}
						for(final Align A : aligns)
						{
							switch(A)
							{
							case GOOD:
								statBonus[STAT_MOVES] += 5;
								break;
							case NEUTRAL:
								statBonus[STAT_HITPOINTS] += 5;
								break;
							case EVIL:
								statBonus[STAT_DAMAGE] += 1;
								break;
							case LAWFUL:
								statBonus[STAT_MINDSAVE] += 3;
								break;
							case MODERATE:
								statBonus[STAT_ARMOR] += 2;
								break;
							case CHAOTIC:
								statBonus[STAT_ATTACK] += 1;
								break;
							default:
								break;
							}
						}
					}
				}
			}
		}
	}

	protected boolean hasVisited(final String plane)
	{
		return getUpperVisitations().containsKey(plane.toUpperCase().trim());
	}

	protected void addVisitation(final String plane)
	{
		final Map<String, Long> visits = getUpperVisitations();
		final String uplane = plane.toUpperCase().trim();
		if(!visits.containsKey(uplane))
		{
			final MOB M=(affected instanceof MOB)?(MOB)affected:invoker();
			final long expires = System.currentTimeMillis()
					+ (60000L * (M.phyStats().level() * (60 + (30 * super.getXTIMELevel(M)) )));
			visits.put(uplane, Long.valueOf(expires));
			if(expires < this.nextExpire)
				this.nextExpire = expires;
			setMiscText(buildVisitsParm(visits));
			rebuildBonuses();
		}
	}

	protected void parseVisits(final String text)
	{
		visits.clear();
		final Map<String,String> temp = CMParms.parseEQParms(text(), COMMA_DELIMITER, true);
		for(final String tKey : temp.keySet())
			visits.put(tKey, Long.valueOf(CMath.s_long(temp.get(tKey))));
	}

	protected String buildVisitsParm(final Map<String,Long> visits)
	{
		return CMParms.toEqListString(visits,',');
	}

	protected Map<String, Long> getUpperVisitations()
	{
		if((visits.size()==0)&&(text().length()>0))
		{
			parseVisits(text());
			rebuildBonuses();
			checkExpiredVisits();
		}
		return visits;
	}

	protected void checkExpiredVisits()
	{
		final Map<String,Long> visits = getUpperVisitations();
		boolean changed = false;
		long nextMs = Long.MAX_VALUE;
		final long now=System.currentTimeMillis();
		for(final Iterator<Entry<String,Long>> e=visits.entrySet().iterator();e.hasNext();)
		{
			final Entry<String,Long> E=e.next();
			final long expires = E.getValue().longValue();
			if(now > expires)
			{
				e.remove();
				changed=true;
			}
			else
			if(expires < nextMs)
				nextMs = expires;
		}
		if(changed)
		{
			setMiscText(buildVisitsParm(visits));
			rebuildBonuses();
		}
		this.nextExpire = nextMs;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		final Physical P=affected;
		if(P instanceof MOB)
		{
			final MOB mob=(MOB)P;
			final Room R=mob.location();
			final Area A=(R!=null)?R.getArea():null;
			if((A != null)&&(A != lastArea))
			{
				final String curPlane = CMLib.flags().getPlaneOfExistence(A);
				lastArea=A;
				if((curPlane != null)
				&&(curPlane.trim().length()>0)
				&&(!hasVisited(curPlane)))
					addVisitation(curPlane);
			}
			getUpperVisitations(); // to ensure bonuses exist always
			if(System.currentTimeMillis() > nextExpire)
				checkExpiredVisits();
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		return super.okMessage(myHost,msg);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(statBonus[STAT_ARMOR] != 0)
			affectableStats.setArmor(affectableStats.armor()-statBonus[STAT_ARMOR]);
		if(statBonus[STAT_DAMAGE] != 0)
			affectableStats.setDamage(affectableStats.damage()+statBonus[STAT_DAMAGE]);
		if(statBonus[STAT_SPEED] != 0)
			affectableStats.setSpeed(affectableStats.speed()+CMath.div(statBonus[STAT_SPEED],100.0));
		if(statBonus[STAT_ATTACK] != 0)
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+statBonus[STAT_ATTACK]);
	}

	@Override
	public void affectCharState(final MOB mob, final CharState affectableState)
	{
		super.affectCharState(mob,affectableState);
		if(statBonus[STAT_MOVES] != 0)
			affectableState.setMovement(affectableState.getMovement() + statBonus[STAT_MOVES]);
		if(statBonus[STAT_HITPOINTS] != 0)
			affectableState.setHitPoints(affectableState.getHitPoints() + statBonus[STAT_HITPOINTS]);
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		if(statBonus[STAT_MINDSAVE] != 0)
			affectableStats.setStat(CharStats.STAT_SAVE_MIND,
					statBonus[STAT_MINDSAVE]+affectableStats.getStat(CharStats.STAT_SAVE_MIND));
	}
}
