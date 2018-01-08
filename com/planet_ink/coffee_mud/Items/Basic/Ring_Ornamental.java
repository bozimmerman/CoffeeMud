package com.planet_ink.coffee_mud.Items.Basic;
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
public class Ring_Ornamental extends Ring
{
	@Override
	public String ID()
	{
		return "Ring_Ornamental";
	}

	public final static int GOLD_RING 					= 0;
	public final static int SILVER_RING					= 1;
	public final static int COPPER_RING		  			= 2;
	public final static int PLATINUM_RING				= 3;
	public final static int GOLD_RING_DIAMOND			= 4;
	public final static int GOLD_RING_RUBY				= 5;
	public final static int GOLD_RING_OPAL				= 6;
	public final static int GOLD_RING_TOPAZ				= 7;
	public final static int GOLD_RING_SAPPHIRE			= 8;
	public final static int MITHRIL_RING	 			= 9;
	public final static int GOLD_RING_PEARL				= 10;
	public final static int GOLD_RING_EMERALD			= 11;
	public final static int STEEL_RING					= 12;
	public final static int BRONZE_RING					= 13;

	protected int lastLevel=-1;

	public Ring_Ornamental()
	{
		super();

		final int ringType = CMLib.dice().roll(1,14,-1);
		this.basePhyStats.setLevel(ringType);
		setItemDescription(this.basePhyStats.level());
		lastLevel=ringType;
		recoverPhyStats();
	}

	@Override
	public void recoverPhyStats()
	{
		if(lastLevel!=basePhyStats().level())
		{
			setItemDescription(basePhyStats().level());
			lastLevel=basePhyStats().level();
		}
		super.recoverPhyStats();
	}

	public void setItemDescription(int level)
	{
		switch(level)
		{
			case SILVER_RING:
				setName("a silver ring");
				setDisplayText("a silver ring is on the ground.");
				setDescription("It is a fancy silver ring inscribed with shields.");
				baseGoldValue=5;
				material=RawMaterial.RESOURCE_SILVER;
				break;
			case COPPER_RING:
				setName("a copper ring");
				setDisplayText("a copper ring is on the ground.");
				setDescription("It is a fancy copper ring inscribed with runes.");
				baseGoldValue=1;
				material=RawMaterial.RESOURCE_COPPER;
				break;
			case PLATINUM_RING:
				setName("a platinum ring");
				setDisplayText("a platinum ring is on the ground.");
				setDescription("It is a fancy platinum ring inscribed with ornate symbols.");
				baseGoldValue=500;
				material=RawMaterial.RESOURCE_PLATINUM;
				break;
			case GOLD_RING_DIAMOND:
				setName("a diamond ring");
				setDisplayText("a diamond ring is on the ground.");
				setDescription("It is a fancy gold ring with a diamond inset.");
				baseGoldValue=1000;
				material=RawMaterial.RESOURCE_DIAMOND;
				break;
			case GOLD_RING:
				setName("a gold ring");
				setDisplayText("a golden ring is on the ground.");
				setDescription("It is a simple gold ");
				baseGoldValue=50;
				material=RawMaterial.RESOURCE_GOLD;
				break;
			case GOLD_RING_RUBY:
				setName("a ruby ring");
				setDisplayText("a ruby ring is on the ground.");
				setDescription("It is a fancy gold ring with a ruby inset.");
				baseGoldValue=100;
				material=RawMaterial.RESOURCE_GEM;
				break;
			case GOLD_RING_OPAL:
				setName("a opal ring");
				setDisplayText("an opal ring is on the ground.");
				setDescription("It is a fancy gold ring with an opal inset.");
				baseGoldValue=75;
				material=RawMaterial.RESOURCE_GEM;
				break;
			case GOLD_RING_TOPAZ:
				setName("a diamond ring");
				setDisplayText("a diamond ring is on the ground.");
				setDescription("It is a fancy gold ring with a diamond inset.");
				baseGoldValue=65;
				material=RawMaterial.RESOURCE_GEM;
				break;
			case GOLD_RING_SAPPHIRE:
				setName("a sapphire ring");
				setDisplayText("a sapphire ring is on the ground.");
				setDescription("It is a fancy gold ring with a sapphire inset.");
				baseGoldValue=200;
				material=RawMaterial.RESOURCE_GEM;
				break;
			case MITHRIL_RING:
				setName("a mithril ring");
				setDisplayText("a mithril ring is on the ground.");
				setDescription("It is a fancy mithril ring.");
				baseGoldValue=20;
				material=RawMaterial.RESOURCE_MITHRIL;
				break;
			case GOLD_RING_PEARL:
				setName("a pearl ring");
				setDisplayText("a pearl ring is on the ground.");
				setDescription("It is a fancy gold ring with a pearl inset.");
				baseGoldValue=65;
				material=RawMaterial.RESOURCE_PEARL;
				break;
			case GOLD_RING_EMERALD:
				setName("a emerald ring");
				setDisplayText("a emerald ring is on the ground.");
				setDescription("It is a fancy gold ring with an emerald inset.");
				baseGoldValue=100;
				material=RawMaterial.RESOURCE_GEM;
				break;
			case BRONZE_RING:
				setName("a bronze ring");
				setDisplayText("a bronze ring is on the ground.");
				setDescription("It is a simple broze ring.");
				baseGoldValue=2;
				material=RawMaterial.RESOURCE_BRONZE;
				break;
			default:
				setName("a metal ring");
				setDisplayText("a simple steel ring is on the ground.");
				setDescription("It is a simple steel ring.");
				material=RawMaterial.RESOURCE_STEEL;
				break;
		}
	}
}
