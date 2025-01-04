package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.CompiledFormula;
import com.planet_ink.coffee_mud.core.CMath.CompiledOperation;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpLevelLibrary.ModXP;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2025 Bo Zimmerman

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
public class Prop_ModExperience extends Property
{
	@Override
	public String ID()
	{
		return "Prop_ModExperience";
	}

	@Override
	public String name()
	{
		return "Modifying Experience Gained";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS | Ability.CAN_ITEMS | Ability.CAN_AREAS | Ability.CAN_ROOMS;
	}

	protected ModXP[] mods = new ModXP[0];

	@Override
	public String accountForYourself()
	{
		if(mods.length==0)
			return "Does nothing";
		final StringBuilder str=new StringBuilder("Modifies experience gained: ");
		for(final ModXP m : mods)
			str.append(m.operationFormula).append(", ");
		return str.substring(0,str.length()-2);
	}

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
		this.mods = CMLib.leveler().parseXPMods(newText);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(mods.length == 0)
			setMiscText(text());
		if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
		||(msg.sourceMinor()==CMMsg.TYP_RPXPCHANGE))
		{
			final boolean useTarget = ((affected instanceof Item)&&(msg.target() instanceof MOB));
			final MOB target=(msg.target() instanceof MOB)?((MOB)msg.target()):null;
			for(final ModXP m : mods)
			{
				if((((msg.target()==affected)||(m.selfXP && (msg.source()==affected)))&&(affected instanceof MOB))
				||((affected instanceof Rideable)
					&&(!m.rideOK)
					&&(msg.target()!=null)
					&&((msg.source().riding()==affected)
						|| ((affected instanceof Item)&&(msg.target().Name().equals(affected.Name()))) // what the actual f?
						||((msg.target() instanceof Rider)&&(((Rider)msg.target()).riding()==affected))))
				||((affected instanceof Item)
					&&(msg.source()==((Item)affected).owner())
					&&(((Item)affected).amBeingWornProperly()))
				||(affected instanceof Room)
				||(affected instanceof Area))
					msg.setValue(CMLib.leveler().handleXPMods(msg.source(), target, m, msg.sourceMessage(), useTarget, msg.value()));
			}
		}
		return super.okMessage(myHost,msg);
	}
}
