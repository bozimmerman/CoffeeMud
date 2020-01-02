package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2018-2020 Bo Zimmerman

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
public class Thief_Informant extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Informant";
	}

	private final static String	localizedName	= CMLib.lang().L("Informant");

	@Override
	public String name()
	{
		return localizedName;
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

	private static final String[]	triggerStrings	= I(new String[] { "INFORMANT" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean disregardsArmorCheck(final MOB mob)
	{
		return true;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_INFLUENTIAL;
	}

	protected Set<MOB> alreadyNames = new HashSet<MOB>();

	protected synchronized String matches(final MOB M)
	{
		if(alreadyNames.contains(M))
			return null;

		alreadyNames.add(M);
		if(text().equals("Warrant"))
		{
			final LegalBehavior B=CMLib.law().getLegalBehavior(M.location());
			final Area A=(B==null)?null:CMLib.law().getLegalObject(M.location());
			if((A==null)||(B==null))
				return null;
			if(B.hasWarrant(A, M))
				return "has a warrant out for their arrest here";
		}

		if(M.Name().equalsIgnoreCase(text()))
			return "is who you are looking for";

		if(M.charStats().raceName().equalsIgnoreCase(text()))
			return "is a "+text();

		if(M.charStats().displayClassName().equalsIgnoreCase(text()))
			return "is a "+text();

		if((CMath.isNumber(text()) && (CMLib.beanCounter().getTotalAbsoluteNativeValue(M)>=CMath.s_double(text()))))
			return "has "+CMLib.beanCounter().nameCurrencyShort(M, CMLib.beanCounter().getTotalAbsoluteNativeValue(M));

		return null;
	}

	protected void doMatch(final MOB M, final Physical affected, final Room R, final String match)
	{
		this.alreadyNames.add(M);
		final MOB invoM=invoker();
		final Room iR=CMLib.map().roomLocation(invoker());
		final Area A=CMLib.map().areaLocation(iR);
		final Area myA=CMLib.map().areaLocation(R);
		if((A==myA)&&(invoM!=null))
		{
			final List<String> say=new XVector<String>(new String[] {"WHISPER",invoM.Name(),M.Name(),match,"at",R.displayText(null)});
			if(R==iR)
				CMLib.commands().forceStandardCommand(M, "Whisper", say);
			else
			{
				iR.bringMobHere(M, false);
				invoM.tell(L("Your informant, @x1, arrives quietly.",affected.Name()));
				CMLib.commands().forceStandardCommand(M, "Whisper", say);
				invoM.tell(L("Your informant, @x1, leaves quietly.",affected.Name()));
				R.bringMobHere(M, false);
			}
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.targetMinor()==CMMsg.TYP_ENTER)
		{
			if(msg.source() != affected)
			{
				final String match = matches(msg.source());
				if(match!=null)
				{
					final Room R=CMLib.map().roomLocation(affected);
					if(R!=null)
						this.doMatch(msg.source(), affected, R, match);
				}
			}
		}
		super.executeMsg(myHost, msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Room R=CMLib.map().roomLocation(affected);
		if(R!=null)
		{
			for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if(M==null)
					continue;
				final String match = matches(M);
				if(match!=null)
					this.doMatch(M, affected, R, match);
			}
		}
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell(L("Make whom an informant, and for what information? Information can include: warrant, an amount of money, a name, a class, or a race."));
			return false;
		}
		final List<String> whoms=new XVector<String>(commands.remove(0));
		String reason=CMParms.combine(commands,0);
		final MOB target=this.getTarget(mob,whoms,givenTarget);
		if(target==null)
			return false;
		if(target == mob)
		{
			mob.tell(L("You can't make yourself an informant."));
			return false;
		}

		if((target.charStats().getStat(CharStats.STAT_INTELLIGENCE)<3)
		||(!target.isMonster()))
		{
			mob.tell(L("You can't make @x1 an informant.",target.name(mob)));
			return false;
		}
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("@x1 is already an informant.",target.name(mob)));
			return false;
		}

		if((reason.equalsIgnoreCase("law")||reason.equalsIgnoreCase("warrant")))
		{
			final LegalBehavior B=CMLib.law().getLegalBehavior(mob.location());
			final Area A=(B==null)?null:CMLib.law().getLegalObject(mob.location());
			if((A==null)||(B==null))
			{
				mob.tell(L("There's no point in making @x1 a law informant here.",target.name(mob)));
				return false;
			}

			final boolean isJudge=B.isJudge(A, target);
			if((!isJudge)&&(!B.isAnyOfficer(A, target)))
			{
				mob.tell(L("Paying off @x1 won't help you.",target.name(mob)));
				return false;
			}
			reason="Warrant";
		}
		else
		if(CMath.isInteger(commands.get(0)))
		{
			if(CMath.s_int(reason)<=0)
			{
				mob.tell(L("No one can inform you about @x1.",reason));
				return false;
			}
			if(commands.size()>1)
			{
				if(CMLib.english().matchAnyCurrencySet(CMParms.combine(commands,1))!=null)
				{
					final String currency = CMLib.english().matchAnyCurrencySet(CMParms.combine(commands,1));
					final double denom=CMLib.english().matchAnyDenomination(currency,CMParms.combine(commands,1));
					final long goldCoins=CMath.s_long(commands.get(0));
					reason = "" + (goldCoins * denom);
				}
				else
				{
					mob.tell(L("No one knows how to inform you about @x1.",reason));
					return false;
				}
			}
		}
		else
		if(CMLib.players().playerExistsAllHosts(reason))
		{
			reason=CMStrings.capitalizeAndLower(reason);
		}
		else
		if(CMClass.findRace(reason)!=null)
		{
			reason=CMClass.findRace(reason).ID();
		}
		else
		if(CMClass.findCharClass(reason)!=null)
		{
			reason=CMClass.findCharClass(reason).ID();
		}
		else
		{
			final Area A=CMLib.map().areaLocation(mob);
			final MOB M=(A!=null)?CMLib.map().findFirstInhabitant(A.getProperMap(), mob, reason, 10):null;
			if(M==null)
			{
				mob.tell(L("No one would even know how to inform you about @x1. Try warrant, an amount of money, a name, a class, or a race.",reason));
				return false;
			}
			reason = M.Name();
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final double amountRequired=100 - (5 * super.getXLOWCOSTLevel(mob)) - (5*super.getXLEVELLevel(mob));

		final String currency=CMLib.beanCounter().getCurrency(target);
		boolean success=proficiencyCheck(mob,0,auto);

		if((!success)||(CMLib.beanCounter().getTotalAbsoluteValue(mob,currency)<amountRequired))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,L("^T<S-NAME> attempt(s) to pay off <T-NAMESELF> to inform about '@x1', but no deal is reached.^?",reason));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			if(CMLib.beanCounter().getTotalAbsoluteValue(mob,currency)<amountRequired)
			{
				final String costWords=CMLib.beanCounter().nameCurrencyShort(currency,amountRequired);
				mob.tell(L("@x1 requires @x2 to do this.",target.charStats().HeShe(),costWords));
			}
			success=false;
		}
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_THIEF_ACT,L("^T<S-NAME> pay(s) off <T-NAMESELF> to be an informant.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				CMLib.beanCounter().subtractMoney(mob,currency,amountRequired);
				CMLib.beanCounter().addMoney(mob,currency,amountRequired);
				final TimeClock C=CMLib.time().localClock(mob);
				final int duration = (int)((C.getHoursInDay() + (super.getXLEVELLevel(mob))) * CMProps.getTicksPerMudHour());
				final Ability A=super.beneficialAffect(mob, target, asLevel, duration );
				if(A!=null)
					A.setMiscText(reason);
			}
			target.recoverPhyStats();
		}
		return success;
	}
}
