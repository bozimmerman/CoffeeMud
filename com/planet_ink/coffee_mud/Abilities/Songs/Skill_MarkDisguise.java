package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.Thief.Thief_Mark;
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
   Copyright 2003-2018 Bo Zimmerman

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

public class Skill_MarkDisguise extends Skill_Disguise
{
	@Override
	public String ID()
	{
		return "Skill_MarkDisguise";
	}

	private final static String localizedName = CMLib.lang().L("Mark Disguise");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"MARKDISGUISE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}
	
	private MOB mark=null;
	
	public MOB getMark(MOB mob)
	{
		final Thief_Mark A=(Thief_Mark)mob.fetchEffect("Thief_Mark");
		if(A!=null)
			return A.mark;
		return null;
	}

	public int getMarkTicks(MOB mob)
	{
		final Thief_Mark A=(Thief_Mark)mob.fetchEffect("Thief_Mark");
		if((A!=null)&&(A.mark!=null))
			return A.ticks;
		return -1;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Skill_Disguise A=(Skill_Disguise)mob.fetchEffect("Skill_Disguise");
		if(A==null)
			A=(Skill_Disguise)mob.fetchEffect("Skill_MarkDisguise");
		if(A!=null)
		{
			A.unInvoke();
			mob.tell(L("You remove your disguise."));
			return true;
		}
		MOB target=getMark(mob);
		if(CMParms.combine(commands,0).equalsIgnoreCase("!"))
			target=mark;

		if(target==null)
		{
			mob.tell(L("You need to have marked someone before you can disguise yourself as him or her."));
			return false;
		}
		if(target.charStats().getClassLevel("Archon")>=0)
		{
			mob.tell(L("You may not disguise yourself as an Archon."));
			return false;
		}

		final int ticksWaited=getMarkTicks(mob);
		if(ticksWaited<15)
		{
			if(target==getMark(mob))
			{
				mob.tell(L("You'll need to observe your mark a little longer (@x1/15 ticks) before you can get the disguise right.",""+ticksWaited));
				return false;
			}
		}

		mark=target;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,mob,null,CMMsg.MSG_DELICATE_HANDS_ACT|(auto?CMMsg.MASK_ALWAYS:0),L("<S-NAME> turn(s) away for a second."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,0);
				A=(Skill_Disguise)mob.fetchEffect("Skill_MarkDisguise");
				A.values[0]=""+target.basePhyStats().weight();
				A.values[1]=""+target.basePhyStats().level();
				A.values[2]=target.charStats().genderName();
				A.values[3]=target.charStats().raceName();
				A.values[4]=""+target.phyStats().height();
				A.values[5]=target.name();
				A.values[6]=target.charStats().displayClassName();
				if(CMLib.flags().isGood(target))
					A.values[7]="good";
				else
				if(CMLib.flags().isEvil(target))
					A.values[7]="evil";
				A.makeLongLasting();

				mob.recoverCharStats();
				mob.recoverPhyStats();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> turn(s) away and then back, but look(s) the same."));
		return success;
	}

}
