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
   Copyright 2004-2018 Bo Zimmerman

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

public class Prayer_Forgive extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Forgive";
	}

	private final static String localizedName = CMLib.lang().L("Forgive");

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
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
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
		LegalBehavior B=null;
		if(mob.location()!=null)
			B=CMLib.law().getLegalBehavior(mob.location());

		String name=CMParms.combine(commands,0);
		if(name.startsWith("$"))
			name=name.substring(1);
		if(name.endsWith("$"))
			name=name.substring(0,name.length()-1);
		if((name.trim().length()==0)&&(givenTarget!=null))
			name=givenTarget.Name();
		if(name.trim().length()==0)
		{
			mob.tell(L("Forgive whom?"));
			return false;
		}
		List<LegalWarrant> warrants=new Vector<LegalWarrant>();
		List<MOB> criminals=new Vector<MOB>();
		if(B!=null)
		{
			criminals=B.getCriminals(CMLib.law().getLegalObject(mob.location()),name);
			if(criminals.size()>0)
				warrants=B.getWarrantsOf(CMLib.law().getLegalObject(mob.location()),criminals.get(0));
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			if(warrants.size()==0)
				beneficialWordsFizzle(mob,null,L("<S-NAME> @x1 to forgive @x2 for no reason at all.",prayForWord(mob),name));
			else
			{
				final CMMsg msg=CMClass.getMsg(mob,mob.location(),this,verbalCastCode(mob,mob.location(),auto),auto?"":L("^S<S-NAME> @x1 to forgive @x2.^?",prayForWord(mob),name));
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					for(int i=0;i<warrants.size();i++)
					{
						final LegalWarrant W=warrants.get(i);
						W.setCrime("pardoned");
						W.setOffenses(0);
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> @x1 to forgive @x2, but nothing happens.",prayForWord(mob),name));

		// return whether it worked
		return success;
	}
}
