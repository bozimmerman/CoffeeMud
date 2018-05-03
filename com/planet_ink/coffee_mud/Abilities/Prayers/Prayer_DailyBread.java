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

public class Prayer_DailyBread extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_DailyBread";
	}

	private final static String localizedName = CMLib.lang().L("Daily Bread");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_UNHOLY;
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
	protected int overrideMana()
	{
		return 100;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff<0)
			levelDiff=0;
		final boolean success=proficiencyCheck(mob,-(levelDiff*25),auto);
		Item Bread=null;
		Item BreadContainer=null;
		for(int i=0;i<target.numItems();i++)
		{
			final Item I=target.getItem(i);
			if((I!=null)&&(I instanceof Food))
			{
				if(I.container()!=null)
				{
					Bread=I;
					BreadContainer=I.container();
				}
				else
				{
					Bread=I;
					BreadContainer=null;
					break;
				}
			}
		}
		if((Bread!=null)&&(BreadContainer!=null))
			CMLib.commands().postGet(target,BreadContainer,Bread,false);
		if(Bread==null)
		{
			final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(target);
			if(SK!=null)
			{
				for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
				{
					final Environmental E2=i.next();
					if((E2!=null)&&(E2 instanceof Food))
					{
						Bread=(Item)E2.copyOf();
						target.addItem(Bread);
						break;
					}
				}
			}
		}
		if((success)&&(Bread!=null))
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L("^S<S-NAME> @x1 for <T-NAMESELF> to provide <S-HIS-HER> daily bread!^?",prayWord(mob)));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					msg=CMClass.getMsg(target,mob,Bread,CMMsg.MSG_GIVE,L("<S-NAME> gladly donate(s) <O-NAME> to <T-NAMESELF>."));
					if(mob.location().okMessage(mob,msg))
						mob.location().send(mob,msg);
				}
			}
		}
		else
			maliciousFizzle(mob,target,auto?"":L("<S-NAME> @x1 for <T-NAMESELF> to provide <S-HIS-HER> daily bread, but nothing happens.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
