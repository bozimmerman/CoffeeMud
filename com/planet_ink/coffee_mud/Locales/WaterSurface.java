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
public class WaterSurface extends StdRoom implements Drink
{
	@Override
	public String ID()
	{
		return "WaterSurface";
	}

	protected int liquidType = RawMaterial.RESOURCE_FRESHWATER;
	
	public WaterSurface()
	{
		super();
		name="the water";
		basePhyStats().setDisposition(basePhyStats().disposition()|PhyStats.IS_SWIMMING);
		basePhyStats.setWeight(2);
		recoverPhyStats();
		climask=Places.CLIMASK_WET;
	}

	@Override
	public int domainType()
	{
		return Room.DOMAIN_OUTDOORS_WATERSURFACE;
	}

	@Override
	public long decayTime()
	{
		return 0;
	}

	@Override
	public void setDecayTime(long time)
	{
	}

	protected String UnderWaterLocaleID()
	{
		return "UnderWaterGrid";
	}

	protected int UnderWaterDomainType()
	{
		return Room.DOMAIN_OUTDOORS_UNDERWATER;
	}

	protected boolean IsUnderWaterFatClass(Room thatSea)
	{
		return (thatSea instanceof UnderWaterGrid) 
			|| (thatSea instanceof UnderWaterThinGrid)
			|| (thatSea instanceof UnderWaterColumnGrid);
	}

	@Override
	public List<Room> getSky()
	{
		List<Room> skys = new Vector<Room>(1);
		if(!skyedYet) 
			return skys;
		skys.addAll(super.getSky());
		
		final Room room=rawDoors()[Directions.DOWN];
		if(room!=null)
		{
			if((room.roomID().length()==0)
			&&(IsUnderWaterFatClass(room)))
			{
				skys.add(room);
			}
		}
		return skys;
	}
	
	@Override
	public void giveASky(int depth)
	{
		if(skyedYet)
			return;
		if(depth>1000)
			return;
		super.giveASky(depth+1);
		skyedYet=true;

		if((roomID().length()==0)
		&&(getGridParent()!=null)
		&&(getGridParent().roomID().length()==0))
			return;

		if((rawDoors()[Directions.DOWN]==null)
		&&(domainType()!=UnderWaterDomainType())
		&&(domainType()!=Room.DOMAIN_OUTDOORS_AIR)
		&&(CMProps.getIntVar(CMProps.Int.SKYSIZE)!=0))
		{
			Exit dnE=null;
			final Exit upE=CMClass.getExit("StdOpenDoorway");
			if(CMProps.getIntVar(CMProps.Int.SKYSIZE)>0)
				dnE=upE;
			else
				dnE=CMClass.getExit("UnseenWalkway");
			final GridLocale sea=(GridLocale)CMClass.getLocale(UnderWaterLocaleID());
			sea.setRoomID("");
			sea.setArea(getArea());
			rawDoors()[Directions.DOWN]=sea;
			setRawExit(Directions.DOWN,dnE);
			sea.rawDoors()[Directions.UP]=this;
			sea.setRawExit(Directions.UP,upE);
			for(int dir : Directions.CODES())
			{
				Room thatRoom=rawDoors()[dir];
				Room thatSea=null;
				if((thatRoom!=null)&&(getRawExit(dir)!=null))
				{
					thatRoom=CMLib.map().getRoom(thatRoom);
					if(thatRoom != null)
					{
						thatRoom.giveASky(depth+1);
						thatSea=thatRoom.rawDoors()[Directions.DOWN];
					}
				}
				if((thatSea!=null)
				&&(thatSea.roomID().length()==0)
				&&(IsUnderWaterFatClass(thatSea)))
				{
					sea.rawDoors()[dir]=thatSea;
					sea.setRawExit(dir,getRawExit(dir));
					thatSea.rawDoors()[Directions.getOpDirectionCode(dir)]=sea;
					if(thatRoom!=null)
					{
						Exit xo=thatRoom.getRawExit(Directions.getOpDirectionCode(dir));
						if((xo==null)||(xo.hasADoor()))
							xo=upE;
						thatSea.setRawExit(Directions.getOpDirectionCode(dir),xo);
					}
					((GridLocale)thatSea).clearGrid(null);
				}
			}
			sea.clearGrid(null);
		}
	}

	@Override
	public void clearSky()
	{
		if(!skyedYet) 
			return;
		super.clearSky();
		final Room room=rawDoors()[Directions.DOWN];
		if(room!=null)
		{
			if((room.roomID().length()==0)
			&&(IsUnderWaterFatClass(room)))
			{
				((GridLocale)room).clearGrid(null);
				rawDoors()[Directions.DOWN]=null;
				setRawExit(Directions.DOWN,null);
				CMLib.map().emptyRoom(room,null,true);
				room.destroy();
				skyedYet=false;
			}
		}
		else
			skyedYet=false;
	}

	protected void fixUnderwater()
	{
		if(!this.skyedYet)
			giveASky(0);
		else
		if((rawDoors()[Directions.DOWN]==null)
		||(this.exits[Directions.DOWN]==null))
		{
			clearSky();
			giveASky(0);
		}
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		fixUnderwater();
		switch(CMLib.tracking().isOkWaterSurfaceAffect(this,msg))
		{
		case CANCEL:
			return false;
		case FORCEDOK:
			return true;
		default:
		case CONTINUE:
			return super.okMessage(myHost,msg);
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		fixUnderwater();
		super.executeMsg(myHost,msg);
		UnderWater.sinkAffects(this,msg);
	}

	@Override
	public int thirstQuenched()
	{
		return 1000;
	}

	@Override
	public int liquidHeld()
	{
		return Integer.MAX_VALUE - 1000;
	}

	@Override
	public int liquidRemaining()
	{
		return Integer.MAX_VALUE - 1000;
	}

	@Override
	public int liquidType()
	{
		return liquidType;
	}

	@Override
	public void setLiquidType(int newLiquidType)
	{
		liquidType = newLiquidType;
	}

	@Override
	public void setThirstQuenched(int amount)
	{
	}

	@Override
	public void setLiquidHeld(int amount)
	{
	}

	@Override
	public void setLiquidRemaining(int amount)
	{
	}

	@Override
	public boolean disappearsAfterDrinking()
	{
		return false;
	}

	@Override
	public boolean containsDrink()
	{
		return true;
	}

	@Override
	public int amountTakenToFillMe(Drink theSource)
	{
		return 0;
	}

	@Override
	public List<Integer> resourceChoices()
	{
		return UnderWater.roomResources;
	}
}
