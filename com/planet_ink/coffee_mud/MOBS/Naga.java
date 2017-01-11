package com.planet_ink.coffee_mud.MOBS;
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
   Copyright 2002-2017 Bo Zimmerman

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
public class Naga extends StdMOB
{
	@Override
	public String ID()
	{
		return "Naga";
	}

	public Naga()
	{
		super();
		final Random randomizer = new Random(System.currentTimeMillis());

		username="a naga";
		setDescription("A serpent whose upper body is that of a man, and lower body that of a snake.");
		setDisplayText("A naga is here");
		CMLib.factions().setAlignment(this,Faction.Align.EVIL);
		setMoney(20);
		basePhyStats.setWeight(100 + Math.abs(randomizer.nextInt() % 101));
		setLevels(Arrays.asList(1,2,3,4,5,6,7,8,9));
		setLocations(Arrays.asList("Jungle","JungleGrid","LargeCaveRoom","SewerMaze","SewerRoom","ShallowWater","Shore","Swamp","WetCaveGrid","WetCaveRoom"));
		//CaveGrid,CaveMaze,CaveRoom,CaveSurface,CityStreet,Desert,DesertGrid,DesertMaze,FrozenMountains,FrozenPlains,GreatLake,Hills,HillsGrid,IcePlains,IceRoom,InTheAir,Jungle,JungleGrid,LargeCaveRoom,Mountains,MountainsGrid,MountainsMaze,MountainSurface,MountainSurfaceGrid,OceanGrid,Plains,PlainsGrid,Road,RoadGrid,SaltWaterSurface,SewerMaze,SewerRoom,ShallowWater,Shore,Swamp,TreeSurface,UnderSaltWater,UnderSaltWaterGrid,UnderSaltWaterMaze,UnderWater,UnderWaterGrid,UnderWaterMaze,WaterSurface,WetCaveGrid,WetCaveRoom,Whirlpool,Woods,WoodsGrid,WoodsMaze

		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,13 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.STAT_STRENGTH,12 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.STAT_DEXTERITY,15 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setMyRace(CMClass.getRace("Naga"));

		basePhyStats().setDamage(7);
		basePhyStats().setSpeed(2.0);
		basePhyStats().setAbility(0);
		basePhyStats().setLevel(4);
		basePhyStats().setArmor(80);

		baseState.setHitPoints(CMLib.dice().roll(basePhyStats().level(),20,basePhyStats().level()));

		addBehavior(CMClass.getBehavior("Mobile"));

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

}
