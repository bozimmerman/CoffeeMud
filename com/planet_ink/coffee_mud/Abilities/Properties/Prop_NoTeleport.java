package com.planet_ink.coffee_mud.Abilities.Properties;
import java.util.ArrayList;
import java.util.List;

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
public class Prop_NoTeleport extends Property
{
	@Override
	public String ID()
	{
		return "Prop_NoTeleport";
	}

	protected List<String> exceptionRooms = new ArrayList<String>(1);
	
	@Override
	public String name()
	{
		return "Teleport-INTO Spell Neutralizing";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS | Ability.CAN_AREAS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_IMMUNER;
	}

	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		exceptionRooms=CMParms.parseCommas(CMParms.getParmStr(newMiscText.toLowerCase(), "EXCEPTIONS", ""), true);
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		final Room R=msg.source().location();
		if((msg.tool() instanceof Ability)
		&&(R!=null)
		&&(msg.sourceMinor()!=CMMsg.TYP_LEAVE))
		{
			final boolean summon=CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_SUMMONING);
			final boolean teleport=CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING);
			final boolean shere=(R==affected)
								||((affected instanceof Area)
									&&(((Area)affected).inMyMetroArea(R.getArea())));
			if(teleport 
			&& (msg.target() instanceof Room) 
			&& (affected instanceof Area)
			&& (exceptionRooms.contains(CMLib.map().getExtendedRoomID((Room)msg.target()).toLowerCase())
				||exceptionRooms.contains(((Room)msg.target()).getArea().Name().toLowerCase())))
				return true;
			if(((!shere)&&(!summon)&&(teleport))
			   ||((shere)&&(summon)))
			{
				final Ability A=(Ability)msg.tool();
				if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
				||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
				||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
				||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG))
					R.showHappens(CMMsg.MSG_OK_VISUAL,L("Magic energy fizzles and is absorbed into the air."));
				return false;
			}
		}
		return true;
	}
}
