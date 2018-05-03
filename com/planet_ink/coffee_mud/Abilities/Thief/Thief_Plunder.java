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
   Copyright 2016-2018 Bo Zimmerman

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

public class Thief_Plunder extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Plunder";
	}

	private final static String	localizedName	= CMLib.lang().L("Plunder");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
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
		return Ability.QUALITY_MALICIOUS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "PLUNDER" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_STEALING;
	}

	@Override
	public boolean disregardsArmorCheck(MOB mob)
	{
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell(L("Plunder whom?"));
			return false;
		}
		MOB target=null;
		if((givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		else
			target=mob.location().fetchInhabitant(CMParms.combine(commands,0));
		if((target==null)||(target.amDead())||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",CMParms.combine(commands,1)));
			return false;
		}
		if(!(target instanceof ShopKeeper))
		{
			mob.tell(L("You can't plunder @x1.",target.name(mob)));
			return false;
		}
		
		if(!target.mayIFight(mob))
		{
			mob.tell(L("You cannot plunder @x1.",target.charStats().himher()));
			return false;
		}

		double money=target.phyStats().level()+super.getXLEVELLevel(mob);
		double total=CMLib.beanCounter().getTotalAbsoluteNativeValue(target);
		if(total < money)
			money=total;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Room R=mob.location();
		final boolean success=proficiencyCheck(mob,0,auto);
		if((success)&&(R!=null))
		{
			final String str=L("<S-NAME> plunder(s) @x1 from the <T-NAME>.",CMLib.beanCounter().nameCurrencyShort(target,money));
			final CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MASK_MALICIOUS|CMMsg.MSG_THIEF_ACT,str);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				CMLib.beanCounter().subtractMoney(target, money);
				CMLib.beanCounter().addMoney(mob, CMLib.beanCounter().getCurrency(target), money);
				if(target.getVictim()!=mob)
					target.setVictim(mob);
			}
		}
		else
			maliciousFizzle(mob,target,L("<T-NAME> stop(s) <S-NAME> from trying to plunder <S-HIM-HER>!"));
		return success;
	}

}
