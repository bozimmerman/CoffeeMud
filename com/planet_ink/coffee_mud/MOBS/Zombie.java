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
   Copyright 2001-2017 Bo Zimmerman

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
public class Zombie extends Undead
{
	@Override
	public String ID()
	{
		return "Zombie";
	}

	public Zombie()
	{

		super();
		username="a zombie";
		setDescription("decayed and rotting, a dead body has been brought back to life...");
		setDisplayText("a zombie slowly moves about.");
		setMoney(10);
		basePhyStats.setWeight(30);
		baseCharStats().setMyRace(CMClass.getRace("Undead"));
		baseCharStats().getMyRace().startRacing(this,false);
		setLevels(Arrays.asList(1,2,3,4,5,6,7,8,9));
		setLocations(Arrays.asList("CaveGrid","CaveMaze","CaveRoom","CaveSurface","CityStreet","Desert","DesertGrid","DesertMaze","FrozenMountains","FrozenPlains","GreatLake","Hills","HillsGrid","IcePlains","IceRoom","Jungle","JungleGrid","LargeCaveRoom","Mountains","MountainsGrid","MountainsMaze","MountainSurface","MountainSurfaceGrid","Plains","PlainsGrid","Road","RoadGrid","SewerMaze","SewerRoom","ShallowWater","Shore","Swamp","TreeSurface","WaterSurface","WetCaveGrid","WetCaveRoom","Woods","WoodsGrid","WoodsMaze"));
		//CaveGrid,CaveMaze,CaveRoom,CaveSurface,CityStreet,Desert,DesertGrid,DesertMaze,FrozenMountains,FrozenPlains,GreatLake,Hills,HillsGrid,IcePlains,IceRoom,InTheAir,Jungle,JungleGrid,LargeCaveRoom,Mountains,MountainsGrid,MountainsMaze,MountainSurface,MountainSurfaceGrid,OceanGrid,Plains,PlainsGrid,Road,RoadGrid,SaltWaterSurface,SewerMaze,SewerRoom,ShallowWater,Shore,Swamp,TreeSurface,UnderSaltWater,UnderSaltWaterGrid,UnderSaltWaterMaze,UnderWater,UnderWaterGrid,UnderWaterMaze,WaterSurface,WetCaveGrid,WetCaveRoom,Whirlpool,Woods,WoodsGrid,WoodsMaze

		basePhyStats().setDamage(8);
		basePhyStats().setLevel(2);
		basePhyStats().setArmor(90);
		basePhyStats().setSpeed(1.0);

		baseState.setHitPoints(CMLib.dice().roll(basePhyStats().level(),20,basePhyStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

}
