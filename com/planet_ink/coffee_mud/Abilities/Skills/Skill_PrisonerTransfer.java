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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
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
public class Skill_PrisonerTransfer extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_PrisonerTransfer";
	}

	private final static String	localizedName	= CMLib.lang().L("Prisoner Transfer");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Prisoner Transfer)");


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

	private static final String[]	triggerStrings	= I(new String[] { "PRISONERTRANFER", "PTRANSFER" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected volatile Reference<MOB> enRoute = new WeakReference<MOB>(null);

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(enRoute.get()!=null)
		{
			if(affected instanceof MOB)
			{
				final MOB prisonerM=enRoute.get();
				final MOB copM=(MOB)affected;
				if((msg.sourceMinor()==CMMsg.TYP_ENTER)
				&&((msg.source()==copM)||(msg.source()==enRoute.get()))
				&&(invoker()!=null)
				&&(invoker().location()!=null)
				&&(copM!=null)
				&&(prisonerM!=null))
				{
					if(!CMLib.flags().isInTheGame(invoker(), true))
					{
						enRoute= new WeakReference<MOB>(null);
						unInvoke();
						return;
					}
					if((msg.target()!=invoker().location()))
					{
						return;
					}
					final Ability me=this;
					msg.addTrailerRunnable(new Runnable()
					{
						@Override
						public void run()
						{
							synchronized(me)
							{
								if(copM.location()!=invoker().location())
									return;
								if(copM.numFollowers()==0)
								{
									enRoute= new WeakReference<MOB>(null);
									unInvoke();
									return;
								}
								if(prisonerM.location()!=invoker().location())
									return;
								final Room R=copM.location();
								final LegalBehavior B=CMLib.law().getLegalBehavior(R);
								final Area legalA=CMLib.law().getLegalObject(R);
								if((B==null)||(legalA==null))
								{
									enRoute= new WeakReference<MOB>(null);
									unInvoke();
									return;
								}
								CMLib.commands().postSay(copM, invoker(), L("OK, @x1's is all yours.",prisonerM.charStats().heshe()));
								final List<LegalWarrant> warrants=B.getWarrantsOf(legalA, prisonerM);
								for(final LegalWarrant W : warrants)
								{
									if((W!=null)
									&&(W.arrestingOfficer()==copM))
									{
										W.setState(Law.STATE_SEEKING);
										W.setArrestingOfficer(legalA, invoker());
									}
								}
								final CMMsg msg1=CMClass.getMsg(prisonerM,copM,null,CMMsg.MSG_NOFOLLOW,L("<S-NAME> stop(s) following <T-NAMESELF>."));
								R.send(prisonerM,msg1);
								if(prisonerM.amFollowing()!=null)
									prisonerM.setFollowing(null);
								final CMMsg msg2=CMClass.getMsg(prisonerM,invoker(),null,CMMsg.MSG_FOLLOW,L("<S-NAME> start(s) following <T-NAMESELF>."));
								R.send(prisonerM,msg2);
								if(prisonerM.amFollowing()!=invoker())
									prisonerM.setFollowing(invoker());
								for(final Enumeration<Ability> e=prisonerM.effects();e.hasMoreElements();)
								{
									final Ability eA=e.nextElement();
									if((eA!=null)
									&&(eA.ID().equals("Skill_HandCuff")))
										eA.setInvoker(invoker());
								}
								CMLib.tracking().wanderAway(copM, false, true);
								unInvoke();
							}
						}
					});
				}
			}
		}
		else
		if((msg.sourceMinor()==CMMsg.TYP_LEGALSTATE)
		&&(msg.tool()==affected)
		&&(msg.value()==Law.STATE_MOVING) // moving to judge
		&&(invoker()!=null)
		&&(msg.othersMessage()!=null)
		&&(affected instanceof MOB)
		&&(((MOB)affected).mayIFight(msg.source())) // mobs, or pvp players, or otherwise at war
		&&(msg.source().location()==((MOB)affected).location())
		&&(invoker().location()!=null)
		&&(CMLib.flags().isInTheGame(invoker(), true)))
		{
			final MOB meM=(MOB)affected;
			final Room R=msg.source().location();
			final LegalBehavior B=CMLib.law().getLegalBehavior(R);
			final Area legalA=CMLib.law().getLegalObject(R);
			if((B!=null)
			&&(legalA!=null)
			&&(legalA.inMyMetroArea(invoker().location().getArea()))
			&&(B.isAnyOfficer(legalA, meM)))
			{
				final Room transR=invoker().location();
				if((transR != null)
				&&(!transR.amDestroyed())
				&&(transR.isInhabitant(invoker())))
				{
					final List<LegalWarrant> warrants=B.getWarrantsOf(legalA, msg.source());
					for(final LegalWarrant W : warrants)
					{
						if((W!=null)
						&&(W.arrestingOfficer()==meM))
						{
							W.setJail(transR);
							enRoute = new WeakReference<MOB>(W.criminal());
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
		double amt=CMLib.beanCounter().getTotalAbsoluteNativeValue(target) / 10.0;
		if(amt < target.phyStats().level()/2)
			amt=target.phyStats().level()/2;
		final String amtStr=CMLib.beanCounter().nameCurrencyLong(target, amt);
		if(CMLib.beanCounter().getTotalAbsoluteValue(mob, currency) < amt)
		{
			CMLib.commands().postSay(mob, L("That kind of paperwork is tricky, I couldn't do it for less than @x1.",amtStr));
			return false;
		}

		/*
		final Ability collectA=mob.fetchAbility("Skill_CollectBounty");
		if(collectA == null)
		{
			mob.tell(L("You need to know how to collect bounties to do that."));
			return false;
		}
		*/

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			if(R.show(mob, target, this, CMMsg.MSG_SPEAK, auto ? "" : L("<S-NAME> make(s) an arrangement with <T-NAME>.")))
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
						((Skill_PrisonerTransfer)A).enRoute= new WeakReference<MOB>(null);
						//CMLib.map().addGlobalHandler(A, CMMsg.TYP_LEGALSTATE);
					}
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> attempt(s) to purchase a prisoner trasnfer from <T-NAME>, but fail(s)."));

		// return whether it worked
		return success;
	}
}
