package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2003-2018 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Prayer_SanctifyRoom extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_SanctifyRoom";
	}

	private final static String localizedName = CMLib.lang().L("Sanctify Room");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Sanctify Room)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_WARDING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ROOMS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}

	public static final SHashSet MSG_CODESH=new SHashSet(new Integer[]
	{
		Integer.valueOf(CMMsg.TYP_GET),
		Integer.valueOf(CMMsg.TYP_PULL),
		Integer.valueOf(CMMsg.TYP_PUSH),
		Integer.valueOf(CMMsg.TYP_CAST_SPELL)
	});

	protected boolean inRoom(MOB mob, Room R)
	{
		if(!CMLib.law().doesAnyoneHavePrivilegesHere(mob, text(), R))
		{
			mob.tell(L("You feel your muscles unwilling to cooperate."));
			return false;
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected==null)
			return super.okMessage(myHost,msg);

		final Room R=(Room)affected;
		final int targMinor=msg.targetMinor();
		if(((targMinor==CMMsg.TYP_GET)
			||(targMinor==CMMsg.TYP_PULL)
			||(targMinor==CMMsg.TYP_PUSH)
			||(targMinor==CMMsg.TYP_CAST_SPELL))
		&&(msg.target() instanceof Item)
		&&(!msg.targetMajor(CMMsg.MASK_INTERMSG))
		&&(!msg.source().isMine(msg.target()))
		&&((!(msg.tool() instanceof Item))
			||(!msg.source().isMine(msg.tool()))))
			return inRoom(msg.source(),R);
		return super.okMessage(myHost,msg);
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(target instanceof MOB)
			{
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=mob.location();
		if(target==null)
			return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(L("This place is already a sanctified place."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> @x1 to sanctify this place.^?",prayForWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				setMiscText(mob.Name());

				if((target instanceof Room)
				&&(CMLib.law().doesOwnThisProperty(mob,((Room)target))))
				{
					final String landOwnerName=CMLib.law().getPropertyOwnerName((Room)target);
					if((CMLib.clans().getClan(landOwnerName)!=null)
					&&(!CMLib.clans().getClan(landOwnerName).getMorgue().equals(CMLib.map().getExtendedRoomID((Room)target))))
					{
						setMiscText(landOwnerName);
						beneficialAffect(mob,target,asLevel,0);
					}
					else
					{
						target.addNonUninvokableEffect((Ability)this.copyOf());
						CMLib.database().DBUpdateRoom((Room)target);
					}
				}
				else
					beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 to sanctify this place, but <S-IS-ARE> not answered.",prayForWord(mob)));

		return success;
	}
}
