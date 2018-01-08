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

public class Thief_Pick extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Pick";
	}

	private final static String	localizedName	= CMLib.lang().L("Pick Locks");

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
		return Ability.CAN_ITEMS | Ability.CAN_EXITS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "PICK" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_CRIMINAL;
	}

	public int				code		= 0;
	public Vector<String>	lastDone	= new Vector<String>();

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(int newCode)
	{
		code = newCode;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final int[] dirCode=new int[]{-1};
		final Physical unlockThis=super.getOpenable(mob, mob.location(), givenTarget, commands, dirCode, true);
		if(unlockThis==null)
			return false;

		if(((unlockThis instanceof Exit)&&(!((Exit)unlockThis).hasALock()))
		||((unlockThis instanceof Container)&&(!((Container)unlockThis).hasALock()))
		||((unlockThis instanceof Item)&&(!(unlockThis instanceof Container))))
		{
			mob.tell(L("There is no lock on @x1!",unlockThis.name()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int adjustment=((mob.phyStats().level()+abilityCode()+(2*getXLEVELLevel(mob)))-unlockThis.phyStats().level())*5;
		if(adjustment>0)
			adjustment=0;
		final boolean success=proficiencyCheck(mob,adjustment,auto);

		if(!success)
			beneficialVisualFizzle(mob,unlockThis,L("<S-NAME> attempt(s) to pick the lock on <T-NAME> and fail(s)."));
		else
		{
			CMMsg msg=CMClass.getMsg(mob,unlockThis,this,auto?CMMsg.MSG_OK_VISUAL:(CMMsg.MSG_DELICATE_SMALL_HANDS_ACT),CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
			msg.setValue(0);
			if(mob.location().okMessage(mob,msg))
			{
				if(((unlockThis instanceof Exit)&&(!((Exit)unlockThis).isLocked()))
				||((unlockThis instanceof Container)&&(!((Container)unlockThis).isLocked())))
					msg=CMClass.getMsg(mob,unlockThis,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_LOCK,CMMsg.MSG_OK_VISUAL,auto?L("<T-NAME> vibrate(s) and click(s)."):L("<S-NAME> pick(s) and relock(s) <T-NAME>."));
				else
					msg=CMClass.getMsg(mob,unlockThis,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,auto?L("<T-NAME> vibrate(s) and click(s)."):L("<S-NAME> pick(s) the lock on <T-NAME>."));
				if(!lastDone.contains(""+unlockThis))
				{
					while(lastDone.size()>40)
						lastDone.removeElementAt(0);
					lastDone.addElement(""+unlockThis);
					msg.setValue(1); // this is to notify that the thief gets xp from doing this.
				}
				CMLib.utensils().roomAffectFully(msg,mob.location(),dirCode[0]);
			}
		}

		return success;
	}
}
