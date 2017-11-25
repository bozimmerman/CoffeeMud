package com.planet_ink.coffee_mud.Abilities.Common;
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
   Copyright 2017-2017 Bo Zimmerman

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
public class Publishing extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Publishing";
	}

	private final static String	localizedName	= CMLib.lang().L("Publishing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "PUBLISH", "PUBLISHING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_LEGAL;
	}

	protected Item		found	= null;
	protected boolean	success = false;

	@Override
	protected boolean canBeDoneSittingDown()
	{
		return true;
	}

	public Publishing()
	{
		super();
		displayText=L("You are publishing...");
		verb=L("publishing");
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted)&&(!helping))
			{
				final MOB mob=(MOB)affected;
				if(!success)
					commonTell(mob,L("You messed up your attempt to get published."));
				else
				{
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if(commands.size()<1)
		{
			commonTell(mob,L("Publish what?"));
			return false;
		}
		Item target = super.getTarget(mob, mob.location(), givenTarget, commands, Wearable.FILTER_UNWORNONLY);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			commonTell(mob,L("You don't seem to have a '@x1'.",(commands.get(0))));
			return false;
		}
		
		if((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PAPER)
		{
			commonTell(mob,L("You can't publish something like that."));
			return false;
		}
		if(!CMLib.flags().isReadable(target))
		{
			commonTell(mob,L("That's not even readable!"));
			return false;
		}
		
		String brand = getBrand(target);
		if((brand==null)||(brand.length()==0))
		{
			commonTell(mob,L("You aren't permitted to publish that."));
			return false;
		}
		if(!target.isGeneric())
		{
			commonTell(mob,L("You aren't able to publish that."));
			return false;
		}
		

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		verb=L("publishing @x1",target.name());
		displayText=L("You are @x1",verb);
		found=target;
		success=true;
		if(!proficiencyCheck(mob,0,auto))
			success=false;
		final int duration=getDuration(30,mob,1,3);
		final CMMsg msg=CMClass.getMsg(mob,target,this,getActivityMessageType(),L("<S-NAME> start(s) publishing <T-NAME>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
