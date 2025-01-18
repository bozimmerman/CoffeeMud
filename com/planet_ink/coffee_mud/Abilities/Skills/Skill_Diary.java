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

	protected Set<DiaryEntry>	entries			= new SHashSet<DiaryEntry>();
	protected TimePeriod		period			= TimePeriod.DAY;
	protected Item				diaryI			= null;

	protected enum DiaryEntry
	{
		XP(0),
		KILL(2),
		DEATHS(3),
		AREA(4),
		FACTION(6),
		LOOT(7),
		DAMAGE(8),
		HEALING(9),
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

	protected void record(final String s)
	{
		if(this.diaryI == null)
			return;

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
			if(entries.contains(DiaryEntry.DEATHS))
			{
				if((msg.tool() instanceof MOB)
				&&(msg.tool() != msg.source()))
					record(L("@x1 killed @x2.",((MOB)msg.tool()).name(),msg.source().name()));
				else
					record(L("@x1 dies.",msg.source().name()));
				return;
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
		if(commands.size()==0)
		{
			mob.tell(L("Keep a diary on what, for what period, and what features?"));
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

		final Item target = super.getTarget(mob, R, givenTarget, commands, Wearable.FILTER_MOBINVONLY);
		if(target == null)
			return false;

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
				final Skill_Diary log = (Skill_Diary)super.beneficialAffect(mob,target,asLevel,Integer.MAX_VALUE/3);
				if(log != null)
				{
					//TODO: set period, entries, and diaryI
					log.makeLongLasting();
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to start keeping a diary in <T-NAMESELF>, but <S-HAS-HAVE> writers block."));
		return success;
	}
}
