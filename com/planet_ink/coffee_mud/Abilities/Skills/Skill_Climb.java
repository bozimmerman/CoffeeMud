package com.planet_ink.coffee_mud.Abilities.Skills;
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

public class Skill_Climb extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Climb";
	}

	private final static String localizedName = CMLib.lang().L("Climb");

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
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings = I(new String[] { "CLIMB" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_FITNESS;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_CLIMBING);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		int dirCode=-1;
		Physical target=givenTarget;
		if(target==null)
			dirCode = CMLib.directions().getGoodDirectionCode(CMParms.combine(commands,0));
		if(dirCode<0)
		{
			if(target == null)
				target=mob.location().fetchFromRoomFavorExits(CMParms.combine(commands,0));
			if(target instanceof Exit)
				dirCode = CMLib.map().getExitDir(mob.location(), (Exit)target);
			if((dirCode<0)&&(target != null))
			{
				if(target instanceof Rideable)
				{
					if(target instanceof Exit) // it's a portal .. so we just assume you can climb "in" it
					{
						
					}
					else
					if(((Rideable)target).rideBasis()!=Rideable.RIDEABLE_LADDER)
					{
						mob.tell(L("You can not climb '@x1'.",target.name(mob)));
						return false;
					}
					else // ordinary ladder item, just convert to an UP
					{
						target=null;
						dirCode=Directions.UP;
					}
				}
				else
				{
					mob.tell(L("You can not climb '@x1'.",target.name(mob)));
					return false;
				}
			}
			else
			{
				target = null; // it's an ordinary exit
			}
		}
		
		if((dirCode<0)&&(!(target instanceof Rideable)))
		{
			mob.tell(L("Climb where?"));
			return false;
		}
		else
		if((dirCode>=0)
		&&((mob.location().getRoomInDir(dirCode)==null)
		||(mob.location().getExitInDir(dirCode)==null)))
		{
			mob.tell(L("You can't climb that way."));
			return false;
		}
		
		if(CMLib.flags().isSitting(mob) // might be more subtlelty here...riding a horse is out, but what about a ladder?
		||CMLib.flags().isSleeping(mob))
		{
			mob.tell(L("You need to stand up first!"));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,null);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			success=proficiencyCheck(mob,0,auto);

			if(mob.fetchEffect(ID())==null)
			{
				final Ability A=(Ability)this.copyOf();
				A.setSavable(false);
				try
				{
					mob.addEffect(A);
					mob.recoverPhyStats();
					if(dirCode>=0)
						CMLib.tracking().walk(mob,dirCode,false,false);
					else
					if(target instanceof Rideable)
						CMLib.commands().forceStandardCommand(mob, "Enter", new XVector<String>("ENTER",mob.location().getContextName(target)));
				}
				finally
				{
					mob.delEffect(A);
				}
			}
			else
			{
				if(dirCode>=0)
					CMLib.tracking().walk(mob,dirCode,false,false);
				else
				if(target instanceof Rideable)
					CMLib.commands().forceStandardCommand(mob, "Enter", new XVector<String>("ENTER",mob.location().getContextName(target)));
			}
			mob.recoverPhyStats();
			if(!success)
				mob.location().executeMsg(mob,CMClass.getMsg(mob,mob.location(),CMMsg.MASK_MOVE|CMMsg.TYP_GENERAL,null));
		}
		return success;
	}

}
