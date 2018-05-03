package com.planet_ink.coffee_mud.Behaviors;
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
public class CommonSpeaker extends StdBehavior
{
	@Override
	public String ID()
	{
		return "CommonSpeaker";
	}

	@Override
	public String accountForYourself()
	{
		return language+" speaking";
	}

	int tickTocker=1;
	int tickTock=0;
	String language="Common";
	
	@Override
	public void setParms(String parameters)
	{
		super.setParms(parameters);
		if(parameters.trim().length()>0)
			language=parameters;
		else
			language="Common";
		tickTocker=1;
		tickTock=0;
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Tickable.TICKID_MOB)
			return true;
		if(--tickTock>0)
			return true;
		if(!(ticking instanceof Environmental))
			return true;

		final Ability L=CMClass.getAbilityPrototype(language);
		if(L==null)
			Log.errOut("CommonSpeaker on "+ticking.name()+" in "+CMLib.map().getExtendedRoomID(CMLib.map().roomLocation((Environmental)ticking))
					+" has unknown language '"+language+"'");
		else
		{
			final Ability A=((MOB)ticking).fetchAbility(L.ID());
			if(A==null)
			{
				final Ability lA=CMClass.getAbility(L.ID());
				lA.setProficiency(100);
				lA.setSavable(false);
				((MOB)ticking).addAbility(lA);
				lA.autoInvocation((MOB)ticking, false);
				lA.invoke((MOB)ticking,null,false,0);
			}
			else
				A.invoke((MOB)ticking,null,false,0);
		}
		if((++tickTocker)==100)
			tickTocker=99;
		tickTock=tickTocker;
		return true;
	}
}
