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
   Copyright 2022-2022 Bo Zimmerman

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
public class Thief_DetectBombs extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_DetectBombs";
	}

	private final static String localizedName = CMLib.lang().L("Detect Bombs");

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

	@Override
	protected boolean ignoreCompounding()
	{
		return true;
	}

	private static final String[] triggerStrings =I(new String[] {"BCHECK"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected Environmental lastChecked=null;

	protected boolean isABomb(final Physical E)
	{
		final Trap T=CMLib.utensils().fetchMyTrap(E);
		if((T!=null)&&(T.isABomb()))
			return true;
		return false;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final String whatTounlock=CMParms.combine(commands,0);
		Physical checkThis=givenTarget;
		if((checkThis==null)&&(whatTounlock.equalsIgnoreCase("room")||whatTounlock.equalsIgnoreCase("here")))
			checkThis=mob.location();
		if(checkThis==null)
			checkThis=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(checkThis==null)
			return false;

		final int oldProficiency=proficiency();
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,+((((mob.phyStats().level()+(2*getXLEVELLevel(mob))))
											 -checkThis.phyStats().level())*3),auto);
		final Trap theTrap=CMLib.utensils().fetchMyTrap(checkThis);
		final CMMsg msg=CMClass.getMsg(mob,checkThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_DELICATE_HANDS_ACT,
				auto?null:L("<S-NAME> look(s) @x1 over very carefully.",((checkThis==null)?"":checkThis.name(mob))));
		if((checkThis!=null)
		&&(mob.location().okMessage(mob,msg)))
		{
			mob.location().send(mob,msg);
			if((checkThis==lastChecked)&&((theTrap==null)||(theTrap.disabled())))
				setProficiency(oldProficiency);
			if(checkThis instanceof Room)
			{
				final Room tR=(Room)checkThis;
				boolean found=false;
				boolean maybe=false;
				if(success)
				{
					for(final Enumeration<Item> i=tR.items();i.hasMoreElements();)
					{
						final Item I=i.nextElement();
						if(isABomb(I))
						{
							if((I.container()==null)
							&& CMLib.flags().canBeSeenBy(I, mob))
							{
								mob.tell(L("@x1 definitely a bomb.",I.name(mob)));
								found=true;
							}
							else
								maybe=true;
						}
					}
				}
				if(maybe)
					mob.tell(L("It doesn't seem like there are any bombs here, but you have a feeling."));
				else
				if(!success || !found)
					mob.tell(L("It doesn't seem like there are any bombs here."));
			}
			else
			if((!success)||(theTrap==null))
			{
				if(!auto)
					mob.tell(L("@x1 doesn't seem like a bomb.",checkThis.name(mob)));
				success=false;
			}
			else
			{
				if(theTrap.isABomb())
					mob.tell(L("@x1 definitely a bomb.",checkThis.name(mob)));
			}
			lastChecked=checkThis;
		}
		else
			success=false;

		return success;
	}
}
