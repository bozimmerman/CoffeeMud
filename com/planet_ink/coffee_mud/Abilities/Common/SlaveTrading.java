package com.planet_ink.coffee_mud.Abilities.Common;
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
   Copyright 2004-2023 Bo Zimmerman

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
public class SlaveTrading extends CommonSkill
{
	@Override
	public String ID()
	{
		return "SlaveTrading";
	}

	private final static String localizedName = CMLib.lang().L("Slave Trading");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"SLAVETRADING","SLAVETRADE","SLAVESELL","SSELL"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_CRIMINAL;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		commands.add(0,"SELL");
		Environmental shopM= null;
		if(commands.size()>2)
		{
			final String what=commands.get(commands.size()-1);
			shopM=mob.location().fetchInhabitant(what);
			if(shopM != null)
			{
				commands.remove(commands.size()-1);
				if(shopM == mob)
				{
					commonTelL(mob,"You can't trade with yourself.");
					return false;
				}
				if(!((MOB)shopM).isPlayer())
				{
					commonTelL(mob,"You can't trade with @x1.",shopM.Name());
					return false;
				}
			}
		}
		if(shopM==null)
			shopM=CMLib.english().parseShopkeeper(mob,commands,"to", "Sell whom to whom?");
		if(shopM==null)
			return false;
		if(commands.size()==0)
		{
			commonTelL(mob,"Sell whom?");
			return false;
		}

		final String str=CMParms.combine(commands,0);
		final MOB slaveM=getVisibleRoomTarget(mob,str);
		if(slaveM==null)
		{
			commonTelL(mob,"You don't see anyone called '@x1' here.",str);
			return false;
		}

		if(!CMLib.flags().canBeSeenBy(slaveM,mob))
		{
			commonTelL(mob,"You don't see anyone called '@x1' here.",str);
			return false;
		}
		if(!slaveM.isMonster())
		{
			commonTelL(mob,slaveM,null,"You can't sell <T-NAME> as a slave.");
			return false;
		}
		if(CMLib.flags().isAnimalIntelligence(slaveM))
		{
			commonTelL(mob,slaveM,null,"You can't sell <T-NAME> as a slave.  Animals are not slaves.");
			return false;
		}

		if(!CMLib.flags().isASlave(slaveM, mob))
		{
			commonTelL(mob,slaveM,null,"<T-NAME> do(es)n't seem to be your slave.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(proficiencyCheck(mob,0,auto))
		{
			final ShopKeeper sk = CMLib.coffeeShops().getShopKeeper(shopM);
			final CMMsg smsg=CMClass.getMsg(mob,slaveM,this,CMMsg.MSG_NOISYMOVEMENT,null);
			if(mob.location().okMessage(mob, smsg))
			{
				mob.location().send(mob,smsg);
				if((sk != null)
				||(!(shopM instanceof MOB)))
				{
					final CMMsg msg=CMClass.getMsg(mob,shopM,slaveM,CMMsg.MSG_SELL,L("<S-NAME> sell(s) <O-NAME> to <T-NAME>."));
					if(mob.location().okMessage(mob,msg))
						mob.location().send(mob,msg);
				}
				else
				{
					final CMMsg msg=CMClass.getMsg(mob,shopM,slaveM,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> hand(s) <O-NAME> over to <T-NAME>."));
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						final PrivateProperty P = CMLib.law().getPropertyRecord(slaveM);
						if((P != null)
						&&(shopM instanceof MOB))
						{
							P.setOwnerName(shopM.Name());
							if(slaveM.amFollowing()!=null)
								CMLib.commands().postFollow(slaveM, null, true);
							CMLib.commands().postFollow(slaveM, (MOB)shopM , false);
						}
						else
							mob.location().show(slaveM, null, CMMsg.MSG_OK_VISUAL, L("The trade fails."));
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,shopM,L("<S-NAME> <S-IS-ARE>n't able to strike a deal with <T-NAME>."));
		return true;
	}
}
