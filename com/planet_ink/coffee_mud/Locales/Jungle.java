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
   Copyright 2002-2018 Bo Zimmerman

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
public class Jungle extends StdRoom
{
	@Override
	public String ID()
	{
		return "Jungle";
	}

	public Jungle()
	{
		super();
		name="the jungle";
		basePhyStats.setWeight(3);
		recoverPhyStats();
		climask=Places.CLIMASK_WET|CLIMASK_HOT;
	}

	@Override
	public int domainType()
	{
		return Room.DOMAIN_OUTDOORS_JUNGLE;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.amITarget(this)||(msg.targetMinor()==CMMsg.TYP_ADVANCE)||(msg.targetMinor()==CMMsg.TYP_RETREAT))
		&&(!msg.source().isMonster())
		&&(msg.source().curState().getHitPoints()<msg.source().maxState().getHitPoints())
		&&(CMLib.dice().rollPercentage()==1)
		&&(CMLib.dice().rollPercentage()==1)
		&&(isInhabitant(msg.source()))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
		{
			Ability A=null;
			if(CMLib.dice().rollPercentage()>50)
				A=CMClass.getAbility("Disease_Gonorrhea");
			else
				A=CMClass.getAbility("Disease_Malaria");
			if((A!=null)&&(msg.source().fetchEffect(A.ID())==null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
				A.invoke(msg.source(),msg.source(),true,0);
		}
		super.executeMsg(myHost,msg);
	}

	public static final Integer[] resourceList={
		Integer.valueOf(RawMaterial.RESOURCE_PLUMS),
		Integer.valueOf(RawMaterial.RESOURCE_PINEAPPLES),
		Integer.valueOf(RawMaterial.RESOURCE_COCONUTS),
		Integer.valueOf(RawMaterial.RESOURCE_BANANAS),
		Integer.valueOf(RawMaterial.RESOURCE_LIMES),
		Integer.valueOf(RawMaterial.RESOURCE_JADE),
		Integer.valueOf(RawMaterial.RESOURCE_SCALES),
		Integer.valueOf(RawMaterial.RESOURCE_HEMP),
		Integer.valueOf(RawMaterial.RESOURCE_BALSA),
		Integer.valueOf(RawMaterial.RESOURCE_IRONWOOD),
		Integer.valueOf(RawMaterial.RESOURCE_SILK),
		Integer.valueOf(RawMaterial.RESOURCE_FRUIT),
		Integer.valueOf(RawMaterial.RESOURCE_APPLES),
		Integer.valueOf(RawMaterial.RESOURCE_BERRIES),
		Integer.valueOf(RawMaterial.RESOURCE_ORANGES),
		Integer.valueOf(RawMaterial.RESOURCE_COFFEEBEANS),
		Integer.valueOf(RawMaterial.RESOURCE_HERBS),
		Integer.valueOf(RawMaterial.RESOURCE_DIRT),
		Integer.valueOf(RawMaterial.RESOURCE_VINE),
		Integer.valueOf(RawMaterial.RESOURCE_LEMONS),
		Integer.valueOf(RawMaterial.RESOURCE_FUR),
		Integer.valueOf(RawMaterial.RESOURCE_FEATHERS)
	};
	public static final List<Integer> roomResources=new Vector<Integer>(Arrays.asList(resourceList));

	@Override
	public List<Integer> resourceChoices()
	{
		return Jungle.roomResources;
	}
}
