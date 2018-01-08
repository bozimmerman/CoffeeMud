package com.planet_ink.coffee_mud.Abilities.Ranger;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Thief.Thief_Hide;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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

public class Ranger_WoodlandCreep extends StdAbility
{
	@Override
	public String ID()
	{
		return "Ranger_WoodlandCreep";
	}

	private final static String localizedName = CMLib.lang().L("Woodland Creep");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Creeping through foliage)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
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
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[] triggerStrings =I(new String[] {"WCREEP"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_STEALTHY;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	protected int bonus=0;

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_HIDDEN);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SNEAKING);
		affectableStats.setSpeed(0.5);
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_DETECTION,proficiency()+25+bonus+affectableStats.getStat(CharStats.STAT_SAVE_DETECTION));
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return;

		final MOB mob=(MOB)affected;

		if((mob.location()!=null)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_SWAMP)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WOODS))
		{
			unInvoke();
			mob.recoverPhyStats();
		}
		if((msg.source()==affected)
		&&(msg.sourceMajor(CMMsg.MASK_MALICIOUS))
		&&(msg.source().isInCombat())
		&&(msg.source().rangeToTarget()<=0))
		{
 			unInvoke();
			mob.recoverPhyStats();
		}
		return;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(mob.fetchEffect(this.ID())!=null)
		{
			mob.tell(L("You are already creeping around."));
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell(L("Not while in combat!"));
			return false;
		}

		if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_SWAMP)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WOODS)
		&&(!auto))
		{
			mob.tell(L("You don't know how to creep around in a place like this."));
			return false;
		}

		final String str=L("You creep into some foliage.");
		boolean success=proficiencyCheck(mob,0,auto);

		if(!success)
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to creep into the foliage and fail(s)."));
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MSG_OK_ACTION:(CMMsg.MSG_DELICATE_HANDS_ACT|CMMsg.MASK_MOVE),str,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				beneficialAffect(mob, mob, asLevel, 0);
				final Ranger_WoodlandCreep newOne=(Ranger_WoodlandCreep)mob.fetchEffect(ID());
				newOne.bonus=getXLEVELLevel(mob)*2;
				newOne.makeLongLasting();
				mob.recoverPhyStats();
			}
			else
				success=false;
		}
		return success;
	}
}
