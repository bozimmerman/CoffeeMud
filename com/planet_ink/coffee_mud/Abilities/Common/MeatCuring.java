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
   Copyright 2016-2018 Bo Zimmerman

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
public class MeatCuring extends CommonSkill
{
	@Override
	public String ID()
	{
		return "MeatCuring";
	}

	private final static String	localizedName	= CMLib.lang().L("MeatCuring");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "MEATCURING", "MEATCURE", "MCURING", "MCURE" });

	@Override
	public String supportedResourceString()
	{
		return "MISC";
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_EPICUREAN;
	}

	protected Item		found	= null;
	protected boolean	success	= false;

	@Override
	protected boolean canBeDoneSittingDown()
	{
		return true;
	}

	public MeatCuring()
	{
		super();
		displayText=L("You are curing...");
		verb=L("curing");
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(!aborted)
			&&(!helping)
			&&(found!=null))
			{
				final MOB mob=(MOB)affected;
				Ability oldA=found.fetchEffect("Prayer_Purify");
				if((oldA==null)
				||(oldA.canBeUninvoked() && success))
				{
					if(oldA!=null)
						oldA.unInvoke();
					final Ability A=CMClass.findAbility("Prayer_Purify");
					if(success)
						found.addNonUninvokableEffect(A);
					else
						A.startTickDown(mob, found, 20);
				}
				else
				if(mob!=null)
					commonEmote(mob,L("<S-NAME> mess(es) up curing @x1.",found.name()));
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
			commonTell(mob,L("You must specify what meat you want to cure."));
			return false;
		}
		String what=commands.get(0);
		Item target = super.getTarget(mob, null, givenTarget, commands, Wearable.FILTER_UNWORNONLY);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			commonTell(mob,L("You don't seem to have a '@x1'.",what));
			return false;
		}
		commands.remove(commands.get(0));

		if(((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_FLESH)
		||(!(target instanceof Food)))
		{
			commonTell(mob,L("You can't cure that."));
			return false;
		}
		
		if(target.fetchEffect("Poison_Rotten")!=null)
		{
			commonTell(mob,L("That's already rotten and can't be cured."));
			return false;
		}
		
		if(target.fetchEffect("Prayer_Purify")!=null)
		{
			commonTell(mob,L("That's already been cured."));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		verb=L("curing @x1",target.name());
		displayText=L("You are @x1",verb);
		found=target;
		success = true;
		if(!proficiencyCheck(mob,0,auto))
			success = false;
		final int duration=getDuration(1+(target.phyStats().weight()*2),mob,1,3);
		final CMMsg msg=CMClass.getMsg(mob,target,this,getActivityMessageType(),L("<S-NAME> start(s) curing <T-NAME>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
