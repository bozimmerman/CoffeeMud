package com.planet_ink.coffee_mud.Abilities.Prayers;
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

public class Prayer_Atonement extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Atonement";
	}

	private final static String localizedName = CMLib.lang().L("Atonement");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		CMMsg msg2=null;
		if((mob!=target)&&(!mob.getGroupMembers(new HashSet<MOB>()).contains(target)))
			msg2=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,L("<T-NAME> do(es) not seem to like <S-NAME> messing with <T-HIS-HER> head."));

		if(success&&(CMLib.factions().getFaction(CMLib.factions().AlignID())!=null))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L(auto?"<T-NAME> feel(s) more good.":"^S<S-NAME> "+prayWord(mob)+" to atone <T-NAMESELF>!^?"));
			if((mob.location().okMessage(mob,msg))
			&&((msg2==null)||(mob.location().okMessage(mob,msg2))))
			{
				mob.location().send(mob,msg);
				if((msg.value()<=0)&&((msg2==null)||(msg2.value()<=0)))
				{
					target.tell(L("Good, pure thoughts fill your head."));
					final int evilness=CMLib.dice().roll(10,adjustedLevel(mob,asLevel),0);
					CMLib.factions().postFactionChange(target,this, CMLib.factions().AlignID(), evilness);
				}
				if(msg2!=null)
					mob.location().send(mob,msg2);
			}
		}
		else
		{
			if((msg2!=null)&&(mob.location().okMessage(mob,msg2)))
				mob.location().send(mob,msg2);
			return beneficialWordsFizzle(mob,target,L("<S-NAME> point(s) at <T-NAMESELF> and @x1, but nothing happens.",prayWord(mob)));
		}

		// return whether it worked
		return success;
	}
}
