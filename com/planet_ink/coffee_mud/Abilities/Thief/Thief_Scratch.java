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
   Copyright 2014-2018 Bo Zimmerman

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

public class Thief_Scratch extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Scratch";
	}

	private final static String localizedName = CMLib.lang().L("Scratch");

	@Override
	public String name()
	{
		return localizedName;
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

	private static final String[] triggerStrings =I(new String[] {"SCRATCH"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_DIRTYFIGHTING;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int overrideMana()
	{
		return 1;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(target.charStats().getBodyPart(Race.BODY_HAND)<0)
		{
			mob.tell(L("@x1 must stand up first!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		String str=null;
		if((success)&&(CMLib.combat().rollToHit(mob, target)))
		{
			str=auto?null:L("^F^<FIGHT^><S-NAME> descreetly swipe(s) at <T-NAMESELF>!^</FIGHT^>^?");
			final int attackCode =  CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0);
			final int hideOverrideCode = CMLib.flags().isHidden(mob)?CMMsg.TYP_LOOK:attackCode;
			final Set<MOB> combatants=CMLib.combat().getAllFightingAgainst(mob, new HashSet<MOB>(1));
			final boolean makePeace=(!mob.isInCombat()) && (combatants.size()==0);
			final CMMsg msg=CMClass.getMsg(mob,target,this,attackCode,str,attackCode,str,hideOverrideCode,str);
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final int damage=CMLib.dice().roll(1, 4, 0);
				CMLib.combat().postDamage(mob, target, mob.getNaturalWeapon(), damage, CMMsg.MASK_ALWAYS|CMMsg.TYP_WEAPONATTACK,
						mob.getNaturalWeapon().weaponDamageType(),L("<S-YOUPOSS> scratch <DAMAGES> <T-NAME>!"));
				if(CMLib.flags().isHidden(mob) && makePeace)
				{
					mob.makePeace(true);
					CMLib.combat().forcePeaceAllFightingAgainst(mob, combatants);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> attempt(s) to scratch <T-NAMESELF>, but miss(es)."));

		return success;
	}

}
