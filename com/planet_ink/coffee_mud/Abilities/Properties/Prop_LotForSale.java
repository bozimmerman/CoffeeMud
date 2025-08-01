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
   Copyright 2015-2025 Bo Zimmerman

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
public class Prop_LotForSale extends Prop_LotsForSale
{
	@Override
	public String ID()
	{
		return "Prop_LotForSale";
	}

	@Override
	public String name()
	{
		return "Buy a room once, get all adjacent rooms free";
	}

	@Override
	public String getTitleID()
	{
		return super.getUniqueLotID();
	}

	@Override
	public LandTitle generateNextRoomTitle()
	{
		final LandTitle newTitle=(LandTitle)this.copyOf();
		newTitle.setBackTaxes(0);
		newTitle.setPrice(1); // because you don't have to buy it.
		return newTitle;
	}

	@Override
	protected void fillLotsCluster(final Room R, final List<Room> roomsV)
	{
		fillCluster(R, roomsV, getOwnerName(), true);
	}

	@Override
	public boolean canGenerateAdjacentRooms(final Room R)
	{
		final String displayText = (R==null)?"":R.displayText();
		return ((displayText.indexOf(INDOORSTR.trim())<0)
			  &&(displayText.indexOf(OUTDOORSTR.trim())<0)
			  &&(displayText.indexOf(SALESTR.trim())<0)
			  &&(displayText.indexOf(RENTSTR.trim())<0));
	}
}
