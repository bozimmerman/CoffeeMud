package com.planet_ink.coffee_mud.Abilities.Skills;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/*
   Copyright 2017-2025 Bo Zimmerman

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
public class Skill_Diary extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Diary";
	}

	private final static String	localizedName	= CMLib.lang().L("Diary");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		if(diaryI!=null)
		{
			switch(this.period)
			{
			case DAY:
				return L("(Keeping daily diary)");
			case SEASON:
				return L("(Keeping seasonal diary)");
			default:
				return L("(Keeping "+this.period.name().toLowerCase()+"ly diary)");
			}
		}
		return "";
	}

	private final String[] AVAIL_PERIODS = new String[] {
		TimePeriod.HOUR.name().toUpperCase(),
		TimePeriod.DAY.name().toUpperCase(),
		TimePeriod.WEEK.name().toUpperCase(),
		TimePeriod.MONTH.name().toUpperCase(),
		TimePeriod.SEASON.name().toUpperCase(),
		TimePeriod.YEAR.name().toUpperCase()
	};
	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "DIARY" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_EDUCATIONLORE;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA | USAGE_MOVEMENT;
	}

	protected final Set<DiaryEntry>	entries			= new SHashSet<DiaryEntry>();
	protected TimePeriod			period			= TimePeriod.DAY;
	protected Book					diaryI			= null;
	protected volatile Area			lastA			= null;
	protected TimeClock				nextPeriodBegin	= null;
	protected final StringBuffer	pageBuffer		= new StringBuffer("");

	protected enum DiaryEntry
	{
		XP(0),
		KILLS(1),
		DEATHS(2),
		AREAS(3),
		FACTION(4),
		LOOT(5),
		DAMAGE(7),
		HEALING(8),
		ALL(10)
		;
		public int xLevel;
		private DiaryEntry(final int xLevel)
		{
			this.xLevel = xLevel;
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		return true;
	}

	protected String getPeriodDate(final TimePeriod period, final TimeClock clock)
	{
		final StringBuilder newDate = new StringBuilder("");
		switch(period)
		{
		case ALLTIME:
			newDate.append("All Time");
			nextPeriodBegin.bump(TimePeriod.YEAR, 9999999);
			break;
		case HOUR:
			newDate.append("hour "+clock.getHourOfDay()+" on ");
			//$FALL-THROUGH$
		case DAY:
			newDate.append("the "+clock.getDayOfMonth()+CMath.numAppendage(clock.getDayOfMonth()));
			newDate.append(" day of ");
			//$FALL-THROUGH$
		case MONTH:
			final int month = clock.getMonth();
			if((month>=0)&&(month<clock.getMonthsInYear()))
				newDate.append(clock.getMonthNames()[month]);
			else
				newDate.append("month "+month);
			if(clock.getYearNames().length>0)
				newDate.append(", ");
			//$FALL-THROUGH$
		case YEAR:
			break;
		case SEASON:
			newDate.append(clock.getSeasonCode().name()+", ");
			break;
		case WEEK:
			newDate.append("week "+clock.getWeekOfYear()+", ");
			break;
		}
		if(period != TimePeriod.ALLTIME)
		{
			if(clock.getYearNames().length>0)
				newDate.append("year "+CMStrings.replaceAll(clock.getYearNames()[clock.getYear()%clock.getYearNames().length],"#",""+clock.getYear()));
			else
				newDate.append("year "+clock.getYear());
		}
		return newDate.toString();
	}

	protected void record(String s)
	{
		if((this.diaryI == null)||(!(affected instanceof MOB)))
			return;
		final MOB mob=(MOB)affected;
		if(diaryI.owner() != affected)
		{
			unInvoke();
			return;
		}
		final Ability writeA = mob.fetchAbility("Skill_Write");
		if((writeA!=null)&&(!writeA.proficiencyCheck(mob, 0, false)))
			s="(unintelligible)";
		final TimeClock homeClock = CMLib.time().homeClock(mob);
		if((nextPeriodBegin == null)
		||(pageBuffer.length()==0))
		{
			pageBuffer.setLength(0);
			final TimeClock lastPeriodBegin = (TimeClock)homeClock.copyOf();
			lastPeriodBegin.bumpToNext(period, 0); // hopefully this resets it backward
			final String lastPeriodDate = getPeriodDate(period, lastPeriodBegin);
			if(diaryI.getUsedPages()>0)
			{
				final String chapter=diaryI.getContent(diaryI.getUsedPages());
				if(chapter.trim().startsWith(lastPeriodDate.trim()))
				{
					pageBuffer.setLength(0);
					pageBuffer.append(chapter);
				}
			}
			if(pageBuffer.length()==0)
			{
				pageBuffer.append(lastPeriodDate+"\n\r");
				final String cmd = pageBuffer.toString()+"\n\r";
				final CMMsg msg=CMClass.getMsg(mob,diaryI,writeA,CMMsg.TYP_WRITE,null,cmd,null);
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
				else
				{
					unInvoke();
					return;
				}
			}
			nextPeriodBegin = (TimeClock)homeClock.copyOf();
			nextPeriodBegin.bumpToNext(period, 1);
		}
		else
		if(homeClock.isAfter(nextPeriodBegin) || homeClock.isEqual(nextPeriodBegin))
		{
			pageBuffer.setLength(0);
			pageBuffer.append(getPeriodDate(period,nextPeriodBegin));
			nextPeriodBegin = (TimeClock)homeClock.copyOf();
			nextPeriodBegin.bumpToNext(period, 1);
			final String cmd = pageBuffer.toString()+"\n\r";
			final CMMsg msg=CMClass.getMsg(mob,diaryI,writeA,CMMsg.TYP_WRITE,null,cmd,null);
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			else
			{
				unInvoke();
				return;
			}
		}
		final String pageNum = ""+diaryI.getUsedPages();
		pageBuffer.append("\n\r"+s);
		final String cmd = "EDIT "+pageNum+" "+pageBuffer.toString();
		final CMMsg msg=CMClass.getMsg(mob,diaryI,writeA,CMMsg.TYP_REWRITE,null,cmd,null);
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		else
		{
			unInvoke();
			return;
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_DAMAGE:
			if((msg.target() instanceof MOB)
			&&(entries.contains(DiaryEntry.DAMAGE))
			&&(msg.value()>0))
			{
				if(msg.source() != msg.target())
					record(L("@x1 does @x2 points of damage to @x3.",msg.source().name(),msg.value()+"",msg.target().name()));
				else
					record(L("@x1 takes @x2 points of damage.",msg.source().name(),msg.value()+""));
			}
			break;
		case CMMsg.TYP_HEALING:
			if((msg.target() instanceof MOB)
			&&(entries.contains(DiaryEntry.HEALING))
			&&(msg.value()>0))
			{
				if(msg.source() != msg.target())
					record(L("@x1 does @x2 points of healing to @x3.",msg.source().name(),msg.value()+"",msg.target().name()));
				else
					record(L("@x1 takes @x2 points of healing.",msg.source().name(),msg.value()+""));
			}
			break;
		}
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_DEATH:
			if(entries.contains(DiaryEntry.DEATHS)
			||((msg.source()==affected)&&entries.contains(DiaryEntry.KILLS)))
			{
				if((msg.tool() instanceof MOB)
				&&(msg.tool() != msg.source()))
					record(L("@x1 killed @x2.",((MOB)msg.tool()).name(),msg.source().name()));
				else
					record(L("@x1 dies.",msg.source().name()));
				return;
			}
			break;
		case CMMsg.TYP_ENTER:
			if((msg.source()==affected)
			&&(msg.target() instanceof Room)
			&&(((Room)msg.target()).getArea() != lastA))
			{
				lastA = ((Room)msg.target()).getArea();
				record(L("Entering @x1.",lastA.name()));
			}
			break;
		case CMMsg.TYP_EXPCHANGE:
			if((entries.contains(DiaryEntry.XP))
			&&(msg.value()!=0))
			{
				if(msg.value()>0)
					record(L("@x1 gains @x2 points of experience.",msg.source().name(),msg.value()+""));
				else
					record(L("@x1 loses @x2 points of experience.",msg.source().name(),msg.value()+""));
			}
			break;
		case CMMsg.TYP_GET:
			if((msg.tool() instanceof Item)
			&&(msg.target() instanceof DeadBody)
			&&(entries.contains(DiaryEntry.LOOT)))
				record(L("@x1 loots @x2 from @x3.",msg.source().name(),msg.tool().name(),msg.target().name()));
			break;
		case CMMsg.TYP_FACTIONCHANGE:
			if((msg.othersMessage() != null)
			&&(entries.contains(DiaryEntry.FACTION))
			&& (msg.value() < Integer.MAX_VALUE)
			&& (msg.value() > Integer.MIN_VALUE)
			&& (msg.value()!=0))
			{
				final Faction F=CMLib.factions().getFaction(msg.othersMessage());
				if(F!=null)
				{
					if(msg.value()>0)
						record(L("@x1 gains @x2 points of @x3.",msg.source().name(),msg.value()+"",F.name()));
					else
						record(L("@x1 loses @x2 points of @x3.",msg.source().name(),msg.value()+"",F.name()));
				}
			}
			break;
		}
	}

	@Override
	public void unInvoke()
	{
		if(super.canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				((MOB)affected).tell(L("Diary keeping aborted."));
				diaryI = null;
				pageBuffer.setLength(0);
			}
		}
		super.unInvoke();
	}

	@Override
	protected boolean ignoreCompounding()
	{
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Ability oldA =mob.fetchEffect(ID());
		if(oldA!=null)
		{
			oldA.unInvoke();
			return false;
		}
		final List<String> availE = new ArrayList<String>();
		final int xl = super.getXLEVELLevel(mob);
		for(final DiaryEntry E : DiaryEntry.values())
		{
			if(xl >= E.xLevel)
				availE.add(E.name().toUpperCase().trim());
		}
		final List<String> availP = new XArrayList<String>(AVAIL_PERIODS);
		if(commands.size()<3)
		{
			mob.tell(L("Keep a diary in what book, for what period (@x1), and what features (@x2)?",
					CMParms.toListString(availP),CMParms.toListString(availE)));
			return false;
		}
		final Room R=mob.location();
		if(R==null)
			return false;
		final Skill_Write write=(Skill_Write)mob.fetchAbility("Skill_Write");
		if(write == null)
		{
			mob.tell(L("You don't know how to write!"));
			return false;
		}
		final Set<DiaryEntry> found = new HashSet<DiaryEntry>();
		TimePeriod per = null;
		for(int i=commands.size()-1;i>0;i--)
		{
			final String s = commands.get(i).toUpperCase().trim();
			if(availE.contains(s))
			{
				found.add((DiaryEntry)CMath.s_valueOf(DiaryEntry.class, s));
				commands.remove(i);
			}
			else
			if(availP.contains(s))
			{
				if(per != null)
				{
					mob.tell(L("You can not specify a second time period (@x1 and @x2).",s,per.name()));
					return false;
				}
				per = (TimePeriod)CMath.s_valueOf(TimePeriod.class, s);
				commands.remove(i);
			}
			else
			if(per == null)
			{
				mob.tell(L("Time period not specified (@x1).",CMParms.toListString(availP)));
				return false;
			}
			else
			if(found.size()==0)
			{
				mob.tell(L("No Diary Entries specified (@x1).",CMParms.toListString(availE)));
				return false;
			}
			else
				break;
		}

		if(found.contains(DiaryEntry.ALL))
			found.addAll(new XHashSet<DiaryEntry>(DiaryEntry.values()));

		final Item targetI = super.getTarget(mob, R, givenTarget, commands, Wearable.FILTER_MOBINVONLY);
		if(targetI == null)
			return false;
		if(!(targetI instanceof Book))
		{
			mob.tell(L("@x1 is not a book.",targetI.name(mob)));
			return false;
		}
		final Book target = (Book)targetI;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_EYES|CMMsg.TYP_OK_VISUAL|(auto?CMMsg.MASK_ALWAYS:0),
					L("<S-NAME> start(s) keeping a diary in <T-NAME>."));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				final Skill_Diary log = (Skill_Diary)super.beneficialAffect(mob,mob,asLevel,Integer.MAX_VALUE/3);
				if(log != null)
				{
					log.makeLongLasting();
					log.diaryI = target;
					log.entries.clear();
					log.entries.addAll(found);
					log.period = per;
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to start keeping a diary in <T-NAMESELF>, but <S-HAS-HAVE> writers block."));
		return success;
	}
}
