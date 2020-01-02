package com.planet_ink.coffee_mud.Locales;
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
   Copyright 2001-2020 Bo Zimmerman

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
public class Desert extends StdRoom
{
	@Override
	public String ID()
	{
		return "Desert";
	}

	public Desert()
	{
		super();
		name="the desert";
		basePhyStats.setWeight(2);
		recoverPhyStats();
		climask=Places.CLIMASK_HOT|CLIMASK_DRY;
	}

	@Override
	public int domainType()
	{
		return Room.DOMAIN_OUTDOORS_DESERT;
	}

	@Override
	protected int baseThirst()
	{
		return 4;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.amITarget(this)||(msg.targetMinor()==CMMsg.TYP_ADVANCE)||(msg.targetMinor()==CMMsg.TYP_RETREAT))
		&&(!msg.source().isMonster())
		&&(CMLib.dice().rollPercentage()==1)
		&&(isInhabitant(msg.source()))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
		{
			if((msg.source().curState().getMovement()<=(msg.source().maxState().getMovement()/2)))
			{
				final Ability A=CMClass.getAbility("Disease_Eczema");
				if((A!=null)&&(msg.source().fetchEffect(A.ID())==null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
					A.invoke(msg.source(),msg.source(),true,0);
			}
		}
		super.executeMsg(myHost,msg);
	}

	public static final Integer[] resourceList={
		Integer.valueOf(RawMaterial.RESOURCE_CACTUS),
		Integer.valueOf(RawMaterial.RESOURCE_SAND),
		Integer.valueOf(RawMaterial.RESOURCE_LAMPOIL),
		Integer.valueOf(RawMaterial.RESOURCE_PEPPERS),
		Integer.valueOf(RawMaterial.RESOURCE_SCALES),
		Integer.valueOf(RawMaterial.RESOURCE_DATES),
		Integer.valueOf(RawMaterial.RESOURCE_FRESHWATER)
	};
	public static final List<Integer> roomResources=new Vector<Integer>(Arrays.asList(resourceList));

	@Override
	public List<Integer> resourceChoices()
	{
		return Desert.roomResources;
	}
}
