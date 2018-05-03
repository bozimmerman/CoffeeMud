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
   Copyright 2002-2018 Bo Zimmerman

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

public class Thief_SilentGold extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_SilentGold";
	}

	@Override
	public String displayText()
	{
		return L("(Silent AutoGold)");
	}

	private final static String localizedName = CMLib.lang().L("Silent AutoGold");

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
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[] triggerStrings =I(new String[] {"SILENTGOLD"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	private CMMsg lastMsg=null;

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(affected instanceof MOB)
		{
			if((msg.sourceMinor()==CMMsg.TYP_DEATH)
			&&(msg.source()!=affected)
			&&(CMLib.flags().canBeSeenBy(msg.source(),(MOB)affected))
			&&(msg!=lastMsg)
			&&(msg.source().location()==((MOB)affected).location()))
			{
				lastMsg=msg;
				final double money=CMLib.beanCounter().getTotalAbsoluteNativeValue(msg.source());
				final double exper=getXLEVELLevel((MOB)affected);
				final double gold=money/10.0*((2.0+exper)/2);
				if(gold>0.0)
				{
					final Coins C=CMLib.beanCounter().makeBestCurrency(msg.source(),gold);
					if((C!=null)&&(C.getNumberOfCoins()>0))
					{
						CMLib.beanCounter().subtractMoney(msg.source(),C.getTotalValue());
						final MOB mob=(MOB)affected;
						mob.location().addItem(C,ItemPossessor.Expire.Monster_EQ);
						mob.location().recoverRoomStats();
						final MOB victim=mob.getVictim();
						mob.setVictim(null);
						final CMMsg msg2=CMClass.getMsg(mob,C,this,CMMsg.MSG_THIEF_ACT,L("You silently loot <T-NAME> from the corpse of @x1",msg.source().name(mob)),CMMsg.MSG_THIEF_ACT,null,CMMsg.NO_EFFECT,null);
						if(mob.location().okMessage(mob,msg2))
						{
							mob.location().send(mob,msg2);
							CMLib.commands().postGet(mob,null,C,true);
						}
						if(victim!=null)
							mob.setVictim(victim);
					}
				}
			}
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.fetchEffect(ID())!=null))
		{
			mob.tell(L("You are no longer automatically looting gold from corpses silently."));
			mob.delEffect(mob.fetchEffect(ID()));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			mob.tell(L("You will now automatically loot gold from corpses silently."));
			beneficialAffect(mob,mob,asLevel,0);
			final Ability A=mob.fetchEffect(ID());
			if(A!=null)
				A.makeLongLasting();
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to start silently looting gold from corpses, but fail(s)."));
		return success;
	}

}
