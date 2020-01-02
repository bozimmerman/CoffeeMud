package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class WildTagTurf extends StdAbility
{
	@Override
	public String ID()
	{
		return "WildTagTurf";
	}

	private final static String localizedName = CMLib.lang().L("Wild Tag Turf");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Wild Tagged)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
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

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"WILDTAG","WTAGTURF","WTURFTAG","WILDTAGTURF","WILDTURFTAG"});
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
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_RACIALABILITY;
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;

		if((msg.tool() instanceof Ability)
		&&(!msg.source().Name().equals(text()))
		&&(msg.source().getClanRole(text())==null)
		&&(!msg.tool().ID().equals("WildTagTurf"))
		&&(((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_ANIMALAFFINITY)
			||((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_RACIALABILITY))
		&&(msg.sourceMinor()!=CMMsg.TYP_TEACH))
		{
			msg.source().tell(L("You definitely aren't allowed to do that on @x1's turf.",text()));
			return false;
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((msg.target()==affected)
		&&(affected instanceof Room)
		&&(msg.targetMinor()==CMMsg.TYP_SNIFF))
		{
			if((msg.source().Name().equals(text()))
			||((msg.source().getClanRole(text())!=null) && CMLib.clans().checkClanPrivilege(msg.source(), Clan.Function.CLAN_BENEFITS)))
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),null,
										CMMsg.MSG_OK_VISUAL,L("This is your turf."),
										CMMsg.NO_EFFECT,null,
										CMMsg.NO_EFFECT,null));
			else
				msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),null,
										CMMsg.MSG_OK_VISUAL,L("This turf has been claimed by @x1.",text()),
										CMMsg.NO_EFFECT,null,
										CMMsg.NO_EFFECT,null));
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Room target=mob.location();
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof Room))
			target=(Room)givenTarget;
		final Ability A=target.fetchEffect(ID());
		if(A!=null)
		{
			final Pair<Clan,Integer> clanRole=mob.getClanRole(A.text());
			if((A.text().equals(mob.Name())
				||((clanRole!=null)&&(clanRole.second.intValue()>=clanRole.first.getGovernment().getAcceptPos()))))
			{
				if(CMParms.combine(commands,0).equalsIgnoreCase("UNTAG"))
				{
					A.unInvoke();
					target.delEffect(A);
					mob.tell(L("This place has been wildly untagged."));
					return true;
				}
				else
				{
					mob.tell(L("This place has already been wildly tagged by @x1.",A.text()));
					return false;
				}
			}
			else
			if((A.invoker() != null) && (adjustedLevel(mob,asLevel) < A.adjustedLevel(A.invoker(),0)))
			{
				mob.tell(L("This place has already been wildly tagged by @x1.",A.text()));
				return false;
			}
			else
			{
				A.unInvoke();
				target.delEffect(A);
				mob.tell(L("This place has been wildly untagged."));
			}
		}
		if((!CMLib.law().doesOwnThisLand(mob,mob.location()))
		&&(CMLib.law().getLandTitle(mob.location())!=null)
		&&(CMLib.law().getLandTitle(mob.location()).getOwnerName().length()>0))
		{
			mob.tell(L("You can't wild tag anothers property!"));
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		||(mob.location().domainType()==Room.DOMAIN_INDOORS_WOOD)
		||(mob.location().domainType()==Room.DOMAIN_INDOORS_STONE))
		{
			mob.tell(L("You don't want to tag places like this."));
			return false;
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		final CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MASK_ALWAYS:CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,auto?"":L("<S-NAME> tag(s) this place as <S-HIS-HER> turf."));
		if(!success)
			return beneficialVisualFizzle(mob,null,auto?"":L("<S-NAME> attempt(s) to tag this place, but can't get it out."));
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			final Clan C=CMLib.clans().findRivalrousClan(mob);
			if(C!=null)
				setMiscText(C.clanID());
			else
				setMiscText(mob.Name());
			int duration = (int)((1+super.adjustedLevel(mob, asLevel)) * CMProps.getTicksPerHour());
			duration += (int)(CMProps.getTicksPerDay() * super.getXLEVELLevel(mob));
			beneficialAffect(mob,target,asLevel,duration);
		}
		return success;
	}
}
