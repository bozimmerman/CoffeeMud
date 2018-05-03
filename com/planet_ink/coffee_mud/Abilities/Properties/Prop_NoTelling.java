package com.planet_ink.coffee_mud.Abilities.Properties;
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
public class Prop_NoTelling extends Property
{
	@Override
	public String ID()
	{
		return "Prop_NoTelling";
	}

	@Override
	public String name()
	{
		return "Tel Neutralizing";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS | Ability.CAN_AREAS | Ability.CAN_MOBS;
	}

	@Override
	public String accountForYourself()
	{
		return "No Telling Field";
	}

	//protected boolean quiet=false;
	
	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		//final List<String> parms=CMParms.parse(newMiscText.toUpperCase().trim());
		//quiet = parms.contains("QUIET");
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.sourceMinor()==CMMsg.TYP_TELL)
		&&(msg.target() instanceof MOB)
		&&((!(affected instanceof MOB))||(msg.source()==affected)||(msg.target()==affected)))
		{
			if(affected instanceof MOB)
				msg.source().tell(L("Your message drifts into oblivion."));
			else
			if(affected instanceof Room)
			{
				if((CMSecurity.isAllowed(msg.source(),(Room)affected,CMSecurity.SecFlag.CMDROOMS))
				||(CMSecurity.isAllowed((MOB)msg.target(),(Room)affected,CMSecurity.SecFlag.CMDROOMS)))
					return true;
				else
				if(((Room)affected).isInhabitant(msg.source()))
					msg.source().tell(L("This is a no-tell zone."));
				else
					msg.source().tell(L("@x1 is in a no-tell zone.",msg.target().Name()));
			}
			else
			if(affected instanceof Area)
			{
				if(((Area)affected).amISubOp(msg.source().Name())
				||(((Area)affected).amISubOp(msg.target().Name()))
				||(CMSecurity.isAllowedAnywhere(msg.source(), CMSecurity.SecFlag.CMDROOMS)))
					return true;
				else
				if(CMLib.map().areaLocation(msg.source())==affected)
					msg.source().tell(L("This is a no-tell area."));
				else
					msg.source().tell(L("@x1 is in a no-tell area.",msg.target().Name()));
			}
			else
				msg.source().tell(L("No telling."));
			return false;
		}
		return true;
	}
}
