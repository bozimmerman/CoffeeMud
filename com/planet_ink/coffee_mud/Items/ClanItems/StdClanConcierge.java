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
   Copyright 2022-2025 Bo Zimmerman

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
public class StdClanConcierge extends StdClanArmor
{
	@Override
	public String ID()
	{
		return "StdClanConcierge";
	}

	public StdClanConcierge()
	{
		super();

		setName("a clan concierge ribbon");
		basePhyStats.setWeight(1);
		setDisplayText("a ribbon belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		setClanItemType(ClanItem.ClanItemType.SPECIALOTHER);
		material=RawMaterial.RESOURCE_COTTON;
		setRawProperLocationBitmap(Wearable.WORN_WAIST|Wearable.WORN_HEAD);
		setRawLogicalAnd(false);
		recoverPhyStats();
	}

	protected Behavior conciergeB = null;

	public Behavior getConcierge()
	{
		if((conciergeB != null)
		&&((clanID().length()==0)||(conciergeB.getParms().length()>0)))
			return conciergeB;
		conciergeB = CMClass.getBehavior("Concierge");
		if(clanID().length()==0)
			return conciergeB;
		if((this.rawProperLocationBitmap()==Wearable.WORN_HEAD)
		||(this.rawProperLocationBitmap()==(Wearable.WORN_HEAD|Wearable.WORN_HELD)))
			conciergeB.setParms(" CLAN=\""+clanID()+"\";AREAONLY=TRUE");
		else
			conciergeB.setParms(" CLAN=\""+clanID()+"\" ");
		if(!CMLib.threads().isTicking(this,Tickable.TICKID_ITEM_BEHAVIOR))
			CMLib.threads().startTickDown(this, TICKID_ITEM_BEHAVIOR, 1);
		return conciergeB;
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!getConcierge().okMessage(host, msg))
			return false;
		return super.okMessage(owner(),msg);
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		getConcierge().executeMsg(host, msg);
		super.executeMsg(owner(), msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		getConcierge().tick(owner(), TICKID_MOB);
		return true;
	}
}
