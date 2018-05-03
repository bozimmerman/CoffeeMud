package com.planet_ink.coffee_mud.Abilities.Spells;
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

public class Spell_ClanExperience extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_ClanExperience";
	}

	private final static String	localizedName	= CMLib.lang().L("Clan Experience");

	@Override
	public String name()
	{
		return localizedName;
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

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_ENCHANTMENT;
	}

	@Override
	public long flags()
	{
		return super.flags() | Ability.FLAG_CLANMAGIC;
	}

	@Override
	protected int overrideMana()
	{
		return 5;
	}

	@Override
	public boolean disregardsArmorCheck(MOB mob)
	{
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob==null)
			return false;
		if((mob.isMonster())||(mob.phyStats().level()<10))
		{
			mob.tell(L("You must be at least level 10 to donate experience to a clan"));
			return false;
		}

		if((commands==null)||(commands.size()==0))
		{
			mob.tell(L("You must specify how much experience to donate to your clan!"));
			return false;
		}

		if(!mob.clans().iterator().hasNext())
		{
			mob.tell(L("You aren't even a member of a clan."));
			return false;
		}
		int amt=0;
		Clan C = null;
		for(int i=0;i<commands.size();i++)
		{
			if(!CMath.isInteger(commands.get(i)))
			{
				String clanID=commands.get(i);
				C=CMLib.clans().getClan(clanID);
				if(C==null)
				{
					mob.tell(L("@x1 is not a valid clan name.  Try CLANLIST.",clanID));
					return false;
				}
				if(mob.getClanRole(C.clanID())==null)
				{
					mob.tell(L("You are not a member of @x1!",C.clanID()));
					return false;
				}
			}
			else
				amt=CMath.s_int(commands.get(i));
		}
		if(amt<=0)
		{
			mob.tell(L("Donate how much of your experience?"));
			return false;
		}
		
		if(C==null)
		{
			final Pair<Clan,Integer> clanPair=CMLib.clans().findPrivilegedClan(mob, Clan.Function.CLAN_BENEFITS);
			if(clanPair==null)
			{
				mob.tell(L("You need to specify a clan to donate to."));
				return false;
			}
			C=clanPair.first;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),L("^S<S-NAME> invoke(s) an experience donation spell between <S-HIM-HERSELF> and @x1.^?",C.name()));
			if(mob.location().okMessage(mob,msg))
			{
				CMLib.leveler().postExperience(mob, null, null, -amt, false);
				C.adjExp(amt);
				C.update();
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to invoke experience donation, but fizzle(s) the spell."));

		// return whether it worked
		return success;
	}
}
