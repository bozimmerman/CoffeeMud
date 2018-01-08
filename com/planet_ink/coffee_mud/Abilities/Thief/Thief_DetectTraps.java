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

public class Thief_DetectTraps extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_DetectTraps";
	}

	private final static String localizedName = CMLib.lang().L("Detect Traps");

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
		return Ability.CAN_ITEMS|Ability.CAN_EXITS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_ALERT;
	}

	private static final String[] triggerStrings =I(new String[] {"CHECK"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected Environmental lastChecked=null;

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final String whatTounlock=CMParms.combine(commands,0);
		Physical unlockThis=givenTarget;
		Room nextRoom=null;
		int dirCode=-1;
		if(unlockThis==null)
		{
			dirCode=CMLib.directions().getGoodDirectionCode(whatTounlock);
			if(dirCode>=0)
			{
				unlockThis=mob.location().getExitInDir(dirCode);
				nextRoom=mob.location().getRoomInDir(dirCode);
			}
		}
		if((unlockThis==null)&&(whatTounlock.equalsIgnoreCase("room")||whatTounlock.equalsIgnoreCase("here")))
			unlockThis=mob.location();
		if(unlockThis==null)
			unlockThis=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(unlockThis==null)
			return false;

		final int oldProficiency=proficiency();
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,+((((mob.phyStats().level()+(2*getXLEVELLevel(mob))))
											 -unlockThis.phyStats().level())*3),auto);
		Trap theTrap=CMLib.utensils().fetchMyTrap(unlockThis);
		if(unlockThis instanceof Exit)
		{
			if(dirCode<0)
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if(mob.location().getExitInDir(d)==unlockThis)
				{
					dirCode=d;
					break;
				}
			}
			if(dirCode>=0)
			{
				final Exit exit=mob.location().getReverseExit(dirCode);
				Trap opTrap=null;
				Trap roomTrap=null;
				if(nextRoom!=null)
					roomTrap=CMLib.utensils().fetchMyTrap(nextRoom);
				if(exit!=null)
					opTrap=CMLib.utensils().fetchMyTrap(exit);
				if((theTrap!=null)&&(opTrap!=null))
				{
					if((theTrap.disabled())&&(!opTrap.disabled()))
						theTrap=opTrap;
				}
				else
				if((opTrap!=null)&&(theTrap==null))
					theTrap=opTrap;
				if((theTrap!=null)&&(theTrap.disabled())&&(roomTrap!=null))
				{
					opTrap=null;
					unlockThis=nextRoom;
					theTrap=roomTrap;
				}
			}
		}
		final String add=(dirCode>=0)?" "+CMLib.directions().getInDirectionName(dirCode):"";
		final CMMsg msg=CMClass.getMsg(mob,unlockThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_DELICATE_HANDS_ACT,auto?null:L("<S-NAME> look(s) @x1@x2 over very carefully.",((unlockThis==null)?"":unlockThis.name()),add));
		if((unlockThis!=null)&&(mob.location().okMessage(mob,msg)))
		{
			mob.location().send(mob,msg);
			if((unlockThis==lastChecked)&&((theTrap==null)||(theTrap.disabled())))
				setProficiency(oldProficiency);
			if((!success)||(theTrap==null))
			{
				if(!auto)
					mob.tell(L("You don't find any traps on @x1@x2.",unlockThis.name(),add));
				success=false;
			}
			else
			{
				if(theTrap.disabled())
					mob.tell(L("@x1@x2 is trapped, but the trap looks disabled for the moment.",unlockThis.name(),add));
				else
				if(theTrap.sprung())
					mob.tell(L("@x1@x2 is trapped, and the trap looks sprung.",unlockThis.name(),add));
				else
					mob.tell(L("@x1@x2 definitely looks trapped.",unlockThis.name(),add));
			}
			lastChecked=unlockThis;
		}
		else
			success=false;

		return success;
	}
}
