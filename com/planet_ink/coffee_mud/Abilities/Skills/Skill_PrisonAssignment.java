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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrackingFlags;
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
public class Skill_PrisonAssignment extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_PrisonAssignment";
	}

	private final static String	localizedName	= CMLib.lang().L("Prison Assignment");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Prison Assignment)");


	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
		//CMLib.map().delGlobalHandler(this, CMMsg.TYP_LEGALSTATE);
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
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_LEGAL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "PRISONASSIGNMENT", "PASSIGN" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.sourceMinor()==CMMsg.TYP_LEGALSTATE)
		&&(msg.tool()==affected)
		&&(msg.value()==Law.STATE_MOVING2) // moving to prison
		&&(msg.othersMessage()!=null)
		&&(affected instanceof MOB)
		&&(((MOB)affected).mayIFight(msg.source())) // mobs, or pvp players, or otherwise at war
		&&(msg.source().location()==((MOB)affected).location())
		&&(text().length()>0))
		{
			final MOB meM=(MOB)affected;
			final Room R=msg.source().location();
			final LegalBehavior B=CMLib.law().getLegalBehavior(R);
			final Area legalA=CMLib.law().getLegalObject(R);
			if((B!=null)
			&&(legalA!=null)
			&&(B.isAnyOfficer(legalA, meM)))
			{
				final Room jail=CMLib.map().getRoom(text());
				if((jail != null)
				&&(!jail.amDestroyed()))
				{
					final List<LegalWarrant> warrants=B.getWarrantsOf(legalA, msg.source());
					for(final LegalWarrant W : warrants)
					{
						if((W!=null)
						&&(W.arrestingOfficer()==meM)
						&&(W.state()==Law.STATE_JAILING))
						{
							W.setJail(jail);
							unInvoke();
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((affecting()==null)||(!(affecting() instanceof MOB)))
			return false;
		return super.tick(ticking,tickID);
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		if((commands.size()>0)
		&&(commands.get(0).equalsIgnoreCase("SETPRISON")))
		{
			boolean foundOne=false;
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				final Room R2=R.getRoomInDir(d);
				final Exit E2=R.getExitInDir(d);
				if((R2!=null)
				&&(E2!=null)
				&&(E2.hasADoor())
				&&(E2.hasALock()))
					foundOne=true;
			}
			if((R.roomID().length()==0)
			||(!foundOne)
			||(CMath.bset(R.getArea().flags(), Area.FLAG_INSTANCE_CHILD))
			||(CMLib.flags().isDeadlyOrMaliciousEffect(R)))
			{
				mob.tell(L("This room is unsuitable as a prison cell."));
				return false;
			}
			mob.tell(L("You have set '@x1' as your new favorite prison cell.",R.displayText(mob)));
			setMiscText(R.roomID());
			return true;
		}
		if(text().length() == 0)
		{
			mob.tell(L("You have not yet established a prison cell.  Try PTRANSFER SETPRISON."));
		}

		final MOB target=this.getTarget(mob,commands,givenTarget, false, true);
		if(target==null)
			return false;

		if(mob.isInCombat())
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}
		else
		if(!CMLib.flags().isAliveAwakeMobileUnbound(target, false)
		||(!CMLib.flags().canBeHeardSpeakingBy(mob, target)))
		{
			mob.tell(L("@x1 doesn't seem to be paying any attention to you.",target.name(mob)));
			return false;
		}

		final LegalBehavior B=CMLib.law().getLegalBehavior(R);
		final Area legalA=CMLib.law().getLegalObject(R);
		if((B==null)
		||(legalA==null)
		||(!B.isAnyOfficer(legalA, target)))
		{
			mob.tell(L("@x1 is not an officer here.",target.name(mob)));
			return false;
		}

		if(!B.isElligibleOfficer(legalA, target))
		{
			mob.tell(L("@x1 is too busy to talk to you right now.",target.name(mob)));
			return false;
		}

		final String currency=CMLib.beanCounter().getCurrency(target);
		double amt=CMLib.beanCounter().getTotalAbsoluteNativeValue(target) / 2.0;
		if(amt < target.phyStats().level())
			amt=target.phyStats().level();
		amt = CMath.mul(amt, 1.25);
		final String amtStr=CMLib.beanCounter().nameCurrencyLong(target, amt);
		if(CMLib.beanCounter().getTotalAbsoluteValue(mob, currency) < amt)
		{
			CMLib.commands().postSay(target, L("That kind of paperwork is tricky, it can't be donefor less than @x1.",amtStr));
			return false;
		}

		final Room jailR=CMLib.map().getRoom(text());
		final TrackingLibrary.TrackingFlags flags=
			CMLib.tracking().newFlags()
			.plus(TrackingLibrary.TrackingFlag.NOAIR)
			.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
			.plus(TrackingLibrary.TrackingFlag.NOWATER);
		if((jailR == null)
		||(jailR.amDestroyed())
		||(!CMLib.tracking().getRadiantRooms(R, flags, 50).contains(jailR)))
		{
			mob.tell(L("@x1 doesn't know how to get to your jail.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			if(R.show(mob, target, this, CMMsg.MSG_SPEAK, auto ? "" : L("<S-NAME> make(s) a prison assignment arrangement with <T-NAME>.")))
			{
				final List<Coins> V=CMLib.beanCounter().makeAllCurrency(currency,amt);
				boolean allOK=true;
				for(int i=0;i<V.size();i++)
				{
					final Coins C=V.get(i);
					final CMMsg newMsg=CMClass.getMsg(mob,target,C,CMMsg.MSG_GIVE,L("<S-NAME> give(s) @x1 to <T-NAMESELF>.",C.Name()));
					if(R.okMessage(mob,newMsg))
						R.send(mob,newMsg);
					else
						allOK=false;
				}
				if(allOK)
				{
					final Ability oldA=target.fetchEffect(ID());
					if(oldA!=null)
					{
						oldA.unInvoke();
						target.delEffect(oldA);
					}
					final int duration = (int)(CMProps.getTicksPerMudHour() * CMLib.time().globalClock().getHoursInDay() * (1+super.getXLEVELLevel(mob)));
					final Ability A=beneficialAffect(mob, target, asLevel, duration);
					if(A!=null)
					{
						CMLib.beanCounter().subtractMoney(mob, amt);
						A.setMiscText(text());
						//CMLib.map().addGlobalHandler(A, CMMsg.TYP_LEGALSTATE);
					}
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to arrange a prison assignment with <T-NAME>, but fail(s)."));

		// return whether it worked
		return success;
	}
}
