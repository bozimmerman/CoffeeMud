package com.planet_ink.coffee_mud.Abilities.Archon;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
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
public class Archon_Shame extends ArchonSkill
{
	@Override
	public String ID()
	{
		return "Archon_Shame";
	}

	private final static String localizedName = CMLib.lang().L("Shame");

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

	private static final String[] triggerStrings = I(new String[] { "SHAME" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ARCHON;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(1);
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}


	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		long time=0;
		if(commands.size()>2)
		{
			final String last=(commands.get(commands.size()-1)).toUpperCase();
			final String num=commands.get(commands.size()-2);
			if((CMath.isInteger(num))&&(CMath.s_int(num)>0))
			{
				if("DAYS".startsWith(last))
					time=(TimeManager.MILI_DAY*CMath.s_int(num));
				else
				if("MONTHS".startsWith(last))
					time=(TimeManager.MILI_MONTH*CMath.s_int(num));
				else
				if("HOURS".startsWith(last))
					time=(TimeManager.MILI_HOUR*CMath.s_int(num));
				else
				if("MINUTES".startsWith(last))
					time=(TimeManager.MILI_MINUTE*CMath.s_int(num));
				else
				if("SECONDS".startsWith(last))
					time=(TimeManager.MILI_SECOND*CMath.s_int(num));
				else
				if("TICKS".startsWith(last))
					time=(CMProps.getTickMillis()*CMath.s_int(num));
				if(time>0)
				{
					commands.remove(commands.size()-1);
					commands.remove(commands.size()-1);
				}
			}
		}

		final MOB target=getTargetAnywhere(mob,commands,givenTarget,false,true,false);
		if(target==null)
			return false;

		final Ability A=target.fetchEffect("Shaming");
		if(A!=null)
		{
			A.unInvoke();
			mob.tell(L("@x1 is released from shaming.",target.Name()));
			return true;
		}

		if(!super.invoke(mob,commands,givenTarget,auto, asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?L("<T-NAME> is shamed!"):L("^F<S-NAME> shame(s) <T-NAMESELF>.^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg)
			&&(target.location()!=null))
			{
				mob.location().send(mob,msg);
				if(mob.location() != target.location())
					target.location().sendOthers(mob,msg);
				final Ability B=CMClass.getAbility("Shaming");
				if(B!=null)
				{
					B.setMiscText("GLOBAL");
					B.startTickDown(mob, target, (int)(time/CMProps.getTickMillis()));
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to shame <T-NAMESELF>, but fail(s)."));
		return success;
	}
}
