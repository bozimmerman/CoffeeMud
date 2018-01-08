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

public class Thief_Sneak extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Sneak";
	}

	private final static String localizedName = CMLib.lang().L("Sneak");

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
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_STEALTHY;
	}

	private static final String[] triggerStrings = I(new String[] { "SNEAK" });

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
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		String dir=CMParms.combine(commands,0);
		if(commands.size()>0)
			dir=commands.get(commands.size()-1);
		Physical target=givenTarget;
		int dirCode=CMLib.directions().getGoodDirectionCode(dir);
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
						mob.tell(L("You can not sneak into '@x1'.",target.name(mob)));
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
					mob.tell(L("You can not sneak into '@x1'.",target.name(mob)));
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
			mob.tell(L("Sneak where?"));
			return false;
		}

		if((dirCode>=0)
		&&((mob.location().getRoomInDir(dirCode)==null)
			||(mob.location().getExitInDir(dirCode)==null)))
		{
			mob.tell(L("Sneak where?"));
			return false;
		}

		final MOB highestMOB=getHighestLevelMOB(mob,null);
		int levelDiff=(mob.phyStats().level()+(super.getXLEVELLevel(mob)*2))-getMOBLevel(highestMOB);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=false;
		String msgStr;
		if(dirCode<0)
			msgStr=L("You quietly sneak into <T-NAME>.");
		else
			msgStr=L("You quietly sneak @x1.",CMLib.directions().getDirectionName(dirCode));
		final CMMsg msg=CMClass.getMsg(mob,target,this,auto?CMMsg.MSG_OK_VISUAL:CMMsg.MSG_DELICATE_HANDS_ACT,msgStr,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if(levelDiff<0)
				levelDiff=levelDiff*8;
			else
				levelDiff=levelDiff*10;
			success=proficiencyCheck(mob,levelDiff,auto);
			if(success)
			{
				mob.basePhyStats().setDisposition(mob.basePhyStats().disposition()|PhyStats.IS_SNEAKING);
				mob.recoverPhyStats();
			}
			if(dirCode>=0)
				CMLib.tracking().walk(mob,dirCode,false,false);
			else
			if(target instanceof Rideable)
				CMLib.commands().forceStandardCommand(mob, "Enter", new XVector<String>("ENTER",mob.location().getContextName(target)));
			if(success)
			{

				final int disposition=mob.basePhyStats().disposition();
				if((disposition&PhyStats.IS_SNEAKING)>0)
				{
					mob.basePhyStats().setDisposition(disposition-PhyStats.IS_SNEAKING);
					mob.recoverPhyStats();
				}
				Ability toHide=mob.fetchAbility("Thief_Hide");
				if(toHide==null)
					toHide=mob.fetchAbility("Ranger_Hide");
				if(toHide!=null)
					toHide.invoke(mob,new Vector<String>(),null,false,asLevel);
			}
			if(CMLib.flags().isSneaking(mob))
				mob.phyStats().setDisposition(mob.phyStats().disposition()-PhyStats.IS_SNEAKING);
		}
		return success;
	}

}
