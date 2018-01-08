package com.planet_ink.coffee_mud.Abilities.Fighter;
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

import java.io.IOException;
import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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

public class Fighter_BloodBrother extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_BloodBrother";
	}

	private final static String	localizedName	= CMLib.lang().L("Blood Brother");
	
	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "BLOODBROTHER", "BROTHER" });

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

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
		return Ability.ACODE_SKILL | Ability.DOMAIN_INFLUENTIAL;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat()&&(!auto))
		{
			mob.tell(L("Not while you're fighting!"));
			return false;
		}
		
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if((target.isMonster())||(mob.isMonster()))
		{
			mob.tell(L("You can't become blood brother to @x1",target.name()));
			return false;
		}
		Tattoo tattChk=target.findTattoo("BLOODBROTHER:");
		if(tattChk!=null)
		{
			String name=tattChk.ID().substring("BLOODBROTHER:".length());
			if(CMLib.players().playerExists(name))
			{
				mob.tell(L("@x1 already has a blood brother.",target.name()));
				return false;
			}
			target.delTattoo(tattChk);
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto) && (target.session()!=null);
		if(success)
		{
			try
			{
				if(!target.session().confirm(L("@x1 wants to become your blood brother.  Is this OK (y/N)?",mob.Name()), "N", 5000))
					success=false;
			}
			catch (IOException e)
			{
				success=false;
			}
		}
		if(success)
		{
			invoker=mob;
			final Room R=mob.location();
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> and <T-NAME> become blood brothers!"));
			if((R!=null)&&(R.okMessage(mob,msg)))
			{
				R.send(mob,msg);
				mob.addTattoo("BROTHER:"+target.Name());
				target.addTattoo("BROTHER:"+mob.Name());
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> fail(s) to become blood brother to <T-NAMESELF>."));

		// return whether it worked
		return success;
	}
}
