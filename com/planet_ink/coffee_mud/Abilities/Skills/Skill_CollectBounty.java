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

public class Skill_CollectBounty extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_CollectBounty";
	}

	private final static String localizedName = CMLib.lang().L("Collect Bounty");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	private static final String[] triggerStrings =I(new String[] {"COLLECTBOUNTY","BOUNTY"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_LEGAL;
	}

	@Override
	public int usageType()
	{
		return USAGE_MANA;
	}

	public List<LegalWarrant> getWarrantsOf(MOB target, Room R)
	{
		return getWarrantsOf(target,CMLib.law().getLegalObject(R));
	}

	public List<LegalWarrant> getWarrantsOf(MOB target, Area legalA)
	{
		LegalBehavior B=null;
		if(legalA!=null)
			B=CMLib.law().getLegalBehavior(legalA);
		List<LegalWarrant> warrants=new Vector<LegalWarrant>();
		if(B!=null)
		{
			warrants=B.getWarrantsOf(legalA,target);
			for(int i=warrants.size()-1;i>=0;i--)
			{
				final LegalWarrant W=warrants.get(i);
				if(W.crime().equalsIgnoreCase("pardoned"))
					warrants.remove(i);
			}
		}
		return warrants;
	}

	public MOB findElligibleOfficer(Area myArea, Area legalA)
	{
		LegalBehavior B=null;
		if(legalA!=null)
			B=CMLib.law().getLegalBehavior(legalA);
		if((B!=null)&&(myArea!=null))
		{
			for(final Enumeration<Room> e=myArea.getMetroMap();e.hasMoreElements();)
			{
				final Room R=e.nextElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
					final MOB M=R.fetchInhabitant(i);
					if((M!=null)&&(B.isElligibleOfficer(legalA,M)))
						return M;
				}
			}
			if((legalA!=myArea)&&(legalA!=null))
			for(final Enumeration<Room> e=legalA.getMetroMap();e.hasMoreElements();)
			{
				final Room R=e.nextElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
					final MOB M=R.fetchInhabitant(i);
					if((M!=null)&&(B.isElligibleOfficer(legalA,M)))
						return M;
				}
			}
		}
		return null;
	}

	public MOB getJudgeIfHere(MOB mob, MOB target, Room R)
	{
		LegalBehavior B=null;
		if(R!=null)
			B=CMLib.law().getLegalBehavior(R);
		final Area legalA=CMLib.law().getLegalObject(R);
		if((B!=null)&&(R!=null))
		{
			for(int i=0;i<R.numInhabitants();i++)
			{
				final MOB M=R.fetchInhabitant(i);
				if((M!=null)&&(M!=mob)&&(M!=target)&&(B.isJudge(legalA,M)))
					return M;
			}
		}
		return null;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		final Room R=mob.location();
		if(mob.fetchEffect(ID())!=null)
		{
			mob.tell(L("You are already collecting a bounty.  Be patient."));
			return false;
		}

		final MOB judge=getJudgeIfHere(mob,target,R);

		if(judge==null)
		{
			mob.tell(L("You must present @x1 to the judge.",target.name(mob)));
			return false;
		}

		final List<LegalWarrant> warrants=getWarrantsOf(target,R);
		if(warrants.size()==0)
		{
			mob.tell(L("@x1 is not wanted for anything here.",target.name(mob)));
			return false;
		}
		if((target.amDead())||(!CMLib.flags().isInTheGame(target,true)))
		{
			mob.tell(L("@x1 is not _really_ here.",target.name(mob)));
			return false;
		}
		for(int w=0;w<warrants.size();w++)
		{
			final LegalWarrant W=warrants.get(w);
			if(W.crime().equalsIgnoreCase("pardoned"))
			{
				mob.tell(L("@x1 has been pardoned, and is no longer a criminal.",target.name(mob)));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		final Area legalA=CMLib.law().getLegalObject(R);
		if((success)&&(legalA!=null))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOUTH|CMMsg.MASK_SOUND|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),L("<S-NAME> turn(s) <T-NAMESELF> in to @x1 for the bounty.",judge.name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB officer=findElligibleOfficer(mob.location().getArea(),legalA);
				if((officer!=null)&&(!mob.location().isInhabitant(officer)))
					CMLib.tracking().wanderFromTo(officer,mob.location(),true);
				if((officer==null)||(!mob.location().isInhabitant(officer)))
				{
					CMLib.commands().postSay(judge,mob,L("I'm sorry, there are no free officers to take care of this one right now."),false,false);
					return false;
				}
				int gold=0;
				Ability A=mob.fetchEffect("Skill_HandCuff");
				if(A==null)
					A=mob.fetchEffect("Thief_Bind");
				if((A!=null)&&(target.amFollowing()==mob))
				{
					A.setInvoker(officer);
					target.setFollowing(officer);
				}
				LegalWarrant W=warrants.get(0);
				W.setArrestingOfficer(legalA,officer);
				W.setState(Law.STATE_REPORTING);
				for(int i=0;i<warrants.size();i++)
				{
					W=warrants.get(i);
					gold+=(W.punishment()*(5+getXLEVELLevel(mob)));
				}
				mob.location().show(judge,mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> pay(s) <T-NAMESELF> the bounty of @x1 on @x2.",CMLib.beanCounter().nameCurrencyShort(judge,gold),target.Name()));
				final String currency=CMLib.beanCounter().getCurrency(judge);
				CMLib.beanCounter().giveSomeoneMoney(judge,mob,currency,gold);
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to turn in <T-NAMESELF> to @x1 for the bounty, but can't get @x2 attention.",judge.name(),judge.charStats().hisher()));

		return success;
	}

}

