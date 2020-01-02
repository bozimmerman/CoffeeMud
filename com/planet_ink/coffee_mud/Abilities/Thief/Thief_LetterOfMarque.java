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
   Copyright 2019-2020 Bo Zimmerman

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
public class Thief_LetterOfMarque extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_LetterOfMarque";
	}

	@Override
	public String displayText()
	{
		return L("(Letter of Marque)");
	}

	private final static String localizedName = CMLib.lang().L("Letter of Marque");

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
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_LEGAL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[] triggerStrings =I(new String[] {"LETTEROFMARQUE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected Area legalA = null;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.source()==affected)
		{
			if((msg.sourceMinor()==CMMsg.TYP_FACTIONCHANGE)
			&&(msg.othersMessage()!=null)
			&&(msg.othersMessage().equalsIgnoreCase(CMLib.factions().getInclinationID()))
			&&(msg.value()<0))
			{
				if((legalA==null)
				||(!legalA.inMyMetroArea(CMLib.map().areaLocation(msg.source()))))
					msg.setValue(-msg.value());
			}
		}
		else
		if(msg.target()==affected)
		{
			if((msg.sourceMinor()==CMMsg.MSG_LEGALWARRANT)
			&&(super.proficiencyCheck(msg.source(), 0, false))
			&&(CMLib.dice().rollPercentage()<=((2*super.getXLEVELLevel(msg.source()))+25))
			&&((legalA==null)||(legalA.inMyMetroArea(CMLib.map().areaLocation(msg.source())))))
				return false;
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		if((affected instanceof MOB)&&(!((MOB)affected).amDead())&&(super.canBeUninvoked()))
			((MOB)affected).tell(L("Your letter of marque has expired."));
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=(givenTarget instanceof MOB)?(MOB)givenTarget:mob;
		if(target.fetchEffect(ID())!=null)
		{
			target.tell(L("You tear up your letter of marque."));
			target.delEffect(mob.fetchEffect(ID()));
			return false;
		}

		final LegalBehavior legalB = CMLib.law().getLegalBehavior(target.location());
		final Area legalA = CMLib.law().getLegalObject(target.location());
		MOB judge = null;
		if(auto)
			judge=target;
		else
		if((legalB!=null)&&(legalA!=null))
		{
			final Set<MOB> group = target.getGroupMembers(new HashSet<MOB>());
			for(final Enumeration<MOB> i=target.location().inhabitants();i.hasMoreElements();)
			{
				final MOB M=i.nextElement();
				if((M!=null)
				&&(M!=target)
				&&(!M.isPlayer())
				&&(!group.contains(M))
				&&(legalB.isJudge(legalA, M)))
					judge=M;
			}
		}
		if(judge==null)
		{
			target.tell(L("A letter of marque can only be issued by a legal judge."));
			return false;
		}
		if((!auto)
		&&(legalB!=null)
		&&(legalB.getWarrantsOf(legalA, target).size()>0))
		{
			target.tell(L("You can only be issued a letter of marque if you have no outstanding warrants in the area."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(target,judge,this,CMMsg.MSG_THIEF_ACT,
					auto?L("A letter of marque is issued to <S-NAME>."):
					L("<T-NAME> issue(s) a letter of marque to <S-NAMESELF>!"));
			if(success)
			{
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
				final TimeClock C=CMLib.time().localClock(mob);
				final int ticks = (int)(CMProps.getTicksPerMudHour() * C.getHoursInDay() * C.getDaysInMonth() * super.getXLEVELLevel(mob));
				final Thief_LetterOfMarque M = (Thief_LetterOfMarque)beneficialAffect(mob,target,asLevel,ticks);
				if(M!=null)
					M.legalA=legalA;
			}
		}
		else
			beneficialVisualFizzle(target,judge,L("<S-NAME> attempt(s) to convince <T-NAME> to issue <S-HIM-HER> a letter of marque, but fail(s)."));
		return success;
	}
}
