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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2013-2018 Bo Zimmerman

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
public class Prop_ImproveGather extends Property
{
	@Override
	public String ID()
	{
		return "Prop_ImproveGather";
	}

	@Override
	public String name()
	{
		return "Improve Gathering Skills";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS | Ability.CAN_ITEMS | Ability.CAN_AREAS | Ability.CAN_ROOMS;
	}

	protected CompiledZMask	mask		= null;
	protected String[]		improves	= new String[] { "ALL" };
	protected int			improvement	= 2;

	@Override
	public String accountForYourself()
	{
		return "Improves common skills " + CMParms.toListString(improves) + ". Gain: " + improvement;
	}

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		this.improvement=CMParms.getParmInt(newText, "AMT", improvement);
		final String maskStr=CMParms.getParmStr(newText, "MASK", "");
		if((maskStr==null)||(maskStr.length()==0))
			mask=null;
		else
			mask=CMLib.masking().maskCompile(maskStr);
		final String skillStr=CMParms.getParmStr(newText, "SKILLS", "ALL");
		final List<String> skills=CMParms.parseCommas(skillStr.toUpperCase().trim(), true);
		improves=skills.toArray(new String[skills.size()]);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.sourceMinor()==CMMsg.TYP_ITEMSGENERATED)
		&&(msg.tool() instanceof Ability)
		&&(CMath.bset(((Ability)msg.tool()).classificationCode(),Ability.DOMAIN_GATHERINGSKILL)
		&&(improvement > 1)
		&&(msg.source().location()!=null)
		&&(msg.source()==affected)||(msg.source().location()==affected)||(msg.source().location().getArea()==affected)
			||((affected instanceof Item)&&(((Item)affected).owner()==msg.source())&&(!((Item)affected).amWearingAt(Wearable.IN_INVENTORY))))
		&&(msg.source().fetchEffect(msg.tool().ID())==msg.tool())
		&&(CMParms.contains(improves, "ALL")||CMParms.contains(improves, msg.tool().ID().toUpperCase()))
		&&((mask==null)||(CMLib.masking().maskCheck(mask, msg.source(), true))))
		{
			msg.setValue(msg.value() + (msg.value()*(improvement - 1)));
		}
		return true;
	}
}
