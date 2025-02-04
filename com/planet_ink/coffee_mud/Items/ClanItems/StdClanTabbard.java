package com.planet_ink.coffee_mud.Items.ClanItems;
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
   Copyright 2019-2025 Bo Zimmerman

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
public class StdClanTabbard extends StdClanArmor
{
	@Override
	public String ID()
	{
		return "StdClanTabbard";
	}

	public StdClanTabbard()
	{
		super();

		setName("a clan tabbard");
		basePhyStats.setWeight(1);
		setDisplayText("a tabbard belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		setClanItemType(ClanItem.ClanItemType.TABBARD);
		material=RawMaterial.RESOURCE_COTTON;
		setRawProperLocationBitmap(Wearable.WORN_ABOUT_BODY);
		setRawLogicalAnd(false);
		recoverPhyStats();
	}

	@Override
	public String genericName()
	{
		if(CMLib.english().startsWithAnIndefiniteArticle(name())
		&&((this.clanID().length()==0)||(name().toLowerCase().indexOf(this.clanID().toLowerCase())<0)))
			return CMStrings.removeColors(name());
		return L("a tabbard");
	}
}
