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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2020-2024 Bo Zimmerman

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
public class Skill_BefoulShrine extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_BefoulShrine";
	}

	private final static String	localizedName	= CMLib.lang().L("Befoul Shrine");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Befouled Shrine)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	private static final String[]	triggerStrings	= I(new String[] { "BEFOULSHRINE"});

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_CRIMINAL;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	@Override
	protected int overrideMana()
	{
		return 100;
	}

	protected int	code		= 0;

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(final int newCode)
	{
		code = newCode;
	}

	protected volatile int cleans=0;

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		cleans=0;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Physical affected=this.affected;
		if(affected instanceof Room)
		{
			if(CMLib.law().getClericInfused(affected)==null)
			{
				unInvoke();
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.sourceMinor()==CMMsg.TYP_HOLYEVENT)
		&&(msg.source() instanceof Deity)
		&&(msg.othersMessage()!=null)
		&&(msg.target()==affected)
		&&(msg.target() instanceof Room)
		&&(msg.othersMessage().equalsIgnoreCase(Deity.HolyEvent.SERVICE_BEGIN.toString())))
		{
			((Room)msg.target()).showHappens(CMMsg.MSG_OK_VISUAL, L("This place is far too befouled to be suitable for a religious service, and must be cleaned and purified first."));
			return false;
		}
		if((msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
		&&(msg.tool() instanceof Deity.DeityWorshipper)
		&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL))
		{
			((Room)msg.target()).showHappens(CMMsg.MSG_OK_VISUAL, L("This place is far too befouled to accept another infusion, and must be cleaned and purified first."));
			return false;
		}
		if((msg.sourceMinor()==CMMsg.TYP_HUH)
		&&(msg.targetMessage()!=null)
		&&(msg.source().location()==affected))
		{
			final MOB mob=msg.source();
			final List<String> cmds=CMParms.parse(msg.targetMessage());
			if(cmds.size()==0)
				return true;
			final Room R=mob.location();
			final String word=cmds.get(0).toUpperCase();
			if(("PURIFY".startsWith(word)||"CLEAN".startsWith(word))
			&&(R.show(msg.source(), null, CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> clean(s) and purif(ys)."))))
			{
				if(((++cleans)>=3)
				&&(CMLib.dice().rollPercentage()<(cleans*5)))
					unInvoke();
				return false;
			}
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		final Physical affected=this.affected;
		super.unInvoke();
		if((canBeUninvoked())&&(affected instanceof Room))
		{
			final Room R=(Room)affected;
			if(CMLib.law().getClericInfused(affected)!=null)
				R.showHappens(CMMsg.MSG_OK_VISUAL, L("This holy place is no longer befouled."));
			else
				R.showHappens(CMMsg.MSG_OK_VISUAL, L("This formerly holy place is no longer befouled."));
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob)))
		{
			mob.tell(L("You are on the floor!"));
			return false;
		}

		if(!CMLib.flags().isAliveAwakeMobileUnbound(mob,false))
			return false;

		final Room R=mob.location();
		if(R==null)
			return false;
		final Physical target=(givenTarget!=null)?givenTarget:R;

		final String deityName=CMLib.law().getClericInfused(target);
		if(deityName==null)
		{
			mob.tell(L("This place doesn't seem like much of a shrine."));
			return false;

		}
		if(deityName.equalsIgnoreCase(mob.baseCharStats().deityName()))
		{
			mob.tell(L("@x1 would not approve.",deityName));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_NOISYMOVEMENT,
					auto?L("This place is suddenly befouled!"):L("<S-NAME> befoul(s) this holy shrine of @x1!",deityName));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final int duration=(int)(CMLib.time().localClock(mob).getHoursInDay()*CMProps.getTicksPerMudHour())
								+ (adjustedLevel(mob,asLevel)/10)+super.getXLEVELLevel(mob);
				cleans=0;
				this.maliciousAffect(mob, target, asLevel, duration,-1);
				final Deity D=CMLib.map().getDeity(deityName);
				if(D!=null)
				{
					final String nameCode = D.Name().toUpperCase().trim().replace(' ', '_');
					final Faction F=CMLib.factions().getFaction("DEITY_"+nameCode);
					if(F!=null)
					{
						final int amt = -25 - (super.getXLEVELLevel(mob));
						if(CMLib.factions().postSkillFactionChange(mob,this, F.factionID(), amt))
							mob.tell(L("You lose @x1 faction with @x2.",""+(-amt),F.name()));
					}
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to befoul this shrine, but mess(es) it up."));
		return success;
	}
}
