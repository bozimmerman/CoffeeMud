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
   Copyright 2001-2018 Bo Zimmerman

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
public class CaveRoom extends StdRoom
{
	@Override
	public String ID()
	{
		return "CaveRoom";
	}

	public CaveRoom()
	{
		super();
		name="the cave";
		basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_DARK);
		basePhyStats.setWeight(2);
		recoverPhyStats();
		climask=Places.CLIMASK_NORMAL;
	}

	@Override
	public int domainType()
	{
		return Room.DOMAIN_INDOORS_CAVE;
	}

	@Override
	public int maxRange()
	{
		return 5;
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
			final Ability A=CMClass.getAbility("Disease_Syphilis");
			if((A!=null)&&(msg.source().fetchEffect(A.ID())==null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
				A.invoke(msg.source(),msg.source(),true,0);
		}
		super.executeMsg(myHost,msg);
	}

	public static final Integer[] resourceList={
		Integer.valueOf(RawMaterial.RESOURCE_GRANITE),
		Integer.valueOf(RawMaterial.RESOURCE_OBSIDIAN),
		Integer.valueOf(RawMaterial.RESOURCE_MARBLE),
		Integer.valueOf(RawMaterial.RESOURCE_STONE),
		Integer.valueOf(RawMaterial.RESOURCE_ALABASTER),
		Integer.valueOf(RawMaterial.RESOURCE_IRON),
		Integer.valueOf(RawMaterial.RESOURCE_LEAD),
		Integer.valueOf(RawMaterial.RESOURCE_GOLD),
		Integer.valueOf(RawMaterial.RESOURCE_WHITE_GOLD),
		Integer.valueOf(RawMaterial.RESOURCE_CHROMIUM),
		Integer.valueOf(RawMaterial.RESOURCE_SILVER),
		Integer.valueOf(RawMaterial.RESOURCE_ZINC),
		Integer.valueOf(RawMaterial.RESOURCE_COPPER),
		Integer.valueOf(RawMaterial.RESOURCE_TIN),
		Integer.valueOf(RawMaterial.RESOURCE_MITHRIL),
		Integer.valueOf(RawMaterial.RESOURCE_MUSHROOMS),
		Integer.valueOf(RawMaterial.RESOURCE_FUNGUS),
		Integer.valueOf(RawMaterial.RESOURCE_GEM),
		Integer.valueOf(RawMaterial.RESOURCE_PERIDOT),
		Integer.valueOf(RawMaterial.RESOURCE_DIAMOND),
		Integer.valueOf(RawMaterial.RESOURCE_LAPIS),
		Integer.valueOf(RawMaterial.RESOURCE_BLOODSTONE),
		Integer.valueOf(RawMaterial.RESOURCE_MOONSTONE),
		Integer.valueOf(RawMaterial.RESOURCE_ALEXANDRITE),
		Integer.valueOf(RawMaterial.RESOURCE_GEM),
		Integer.valueOf(RawMaterial.RESOURCE_SCALES),
		Integer.valueOf(RawMaterial.RESOURCE_CRYSTAL),
		Integer.valueOf(RawMaterial.RESOURCE_RUBY),
		Integer.valueOf(RawMaterial.RESOURCE_EMERALD),
		Integer.valueOf(RawMaterial.RESOURCE_SAPPHIRE),
		Integer.valueOf(RawMaterial.RESOURCE_AGATE),
		Integer.valueOf(RawMaterial.RESOURCE_DIRT),
		Integer.valueOf(RawMaterial.RESOURCE_CITRINE),
		Integer.valueOf(RawMaterial.RESOURCE_PLATINUM)};
	public static final List<Integer> roomResources=new Vector<Integer>(Arrays.asList(resourceList));

	@Override
	public List<Integer> resourceChoices()
	{
		return CaveRoom.roomResources;
	}
}
