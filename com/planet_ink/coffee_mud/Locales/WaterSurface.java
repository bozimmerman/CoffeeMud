package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
	public String ID(){return "WaterSurface";}
	public WaterSurface()
	{
		super();
		name="the water";
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_WATERSURFACE;
		domainCondition=Room.CONDITION_WET;
	}

	protected String UnderWaterLocaleID(){return "UnderWaterGrid";}

	public void giveASky(int depth)
	{
		if(skyedYet) return;
		if(depth>1000) return;
		super.giveASky(depth+1);
		skyedYet=true;
		if((rawDoors()[Directions.DOWN]==null)
		&&((domainType()&Room.INDOORS)==0)
		&&(domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(domainType()!=Room.DOMAIN_OUTDOORS_AIR))
		{
			Exit o=CMClass.getExit("StdOpenDoorway");
			GridLocale sea=(GridLocale)CMClass.getLocale(UnderWaterLocaleID());
			sea.setArea(getArea());
			sea.setRoomID("");
			rawDoors()[Directions.DOWN]=sea;
			rawExits()[Directions.DOWN]=o;
			sea.rawDoors()[Directions.UP]=this;
			sea.rawExits()[Directions.UP]=o;
			for(int d=0;d<4;d++)
			{
				Room thatRoom=rawDoors()[d];
				Room thatSea=null;
				if((thatRoom!=null)&&(rawExits()[d]!=null))
				{
					thatRoom.giveASky(depth+1);
					thatSea=thatRoom.rawDoors()[Directions.DOWN];
				}
				if((thatSea!=null)
				&&(thatSea.roomID().length()==0)
				&&((thatSea instanceof UnderWaterGrid)||(thatSea instanceof UnderWaterThinGrid)))
				{
					sea.rawDoors()[d]=thatSea;
					sea.rawExits()[d]=rawExits()[d];
					thatSea.rawDoors()[Directions.getOpDirectionCode(d)]=sea;
					Exit xo=thatRoom.rawExits()[Directions.getOpDirectionCode(d)];
					if((xo==null)||(xo.hasADoor())) xo=o;
					thatSea.rawExits()[Directions.getOpDirectionCode(d)]=xo;
					((GridLocale)thatSea).clearGrid(null);
				}
			}
			sea.clearGrid(null);
			CMMap.addRoom(sea);
		}
	}

	public void clearSky()
	{
		if(!skyedYet) return;
		super.clearSky();
		Room room=rawDoors()[Directions.DOWN];
		if(room==null) return;
		if((room.roomID().length()==0)
		&&((room instanceof UnderWaterGrid)||(room instanceof UnderWaterThinGrid)))
		{
			((GridLocale)room).clearGrid(null);
			rawDoors()[Directions.UP]=null;
			rawExits()[Directions.UP]=null;
			room.rawDoors()[Directions.DOWN]=null;
			room.rawExits()[Directions.DOWN]=null;
			room.destroyRoom();
			CMMap.delRoom(room);
			skyedYet=false;
		}
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		switch(WaterSurface.isOkWaterSurfaceAffect(this,msg))
		{
		case -1: return false;
		case 1: return true;
		}
		return super.okMessage(myHost,msg);
	}
	public static int isOkWaterSurfaceAffect(Room room, CMMsg msg)
	{
		if(Sense.isSleeping(room))
			return 0;

		if(((msg.targetMinor()==CMMsg.TYP_LEAVE)
			||(msg.targetMinor()==CMMsg.TYP_ENTER)
			||(msg.targetMinor()==CMMsg.TYP_FLEE))
		&&(msg.amITarget(room))
		&&(msg.sourceMinor()!=CMMsg.TYP_RECALL)
		&&((msg.targetMinor()==CMMsg.TYP_ENTER)
		   ||(!(msg.tool() instanceof Ability))
		   ||(!Util.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING)))
		&&(!Sense.isFalling(msg.source()))
		&&(!Sense.isInFlight(msg.source()))
		&&(!Sense.isWaterWorthy(msg.source())))
		{
			MOB mob=msg.source();
			boolean hasBoat=false;
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I!=null)&&(I instanceof Rideable)&&(((Rideable)I).rideBasis()==Rideable.RIDEABLE_WATER))
				{	hasBoat=true; break;}
			}
			if((!Sense.isSwimming(mob))&&(!hasBoat)&&(!Sense.isInFlight(mob)))
			{
				mob.tell("You need to swim or ride a boat that way.");
				return -1;
			}
			else
			if(Sense.isSwimming(mob))
				if(mob.envStats().weight()>Math.round(Util.mul(mob.maxCarry(),0.50)))
				{
					mob.tell("You are too encumbered to swim.");
					return -1;
				}
		}
		else
		if(((msg.sourceMinor()==CMMsg.TYP_SIT)||(msg.sourceMinor()==CMMsg.TYP_SLEEP))
		&&((msg.source().riding()==null)||(!Sense.isSwimming(msg.source().riding()))))
		{
			msg.source().tell("You cannot rest here.");
			return -1;
		}
		else
		if(msg.amITarget(room)
		&&(msg.targetMinor()==CMMsg.TYP_DRINK)
		&&(room instanceof Drink))
		{
			if(((Drink)room).liquidType()==EnvResource.RESOURCE_SALTWATER)
			{
				msg.source().tell("You don't want to be drinking saltwater.");
				return -1;
			}
			return 1;
		}
		return 0;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		UnderWater.sinkAffects(this,msg);
	}
	public int thirstQuenched(){return 1000;}
	public int liquidHeld(){return Integer.MAX_VALUE-1000;}
	public int liquidRemaining(){return Integer.MAX_VALUE-1000;}
	public int liquidType(){return EnvResource.RESOURCE_FRESHWATER;}
	public void setLiquidType(int newLiquidType){}
	public void setThirstQuenched(int amount){}
	public void setLiquidHeld(int amount){}
	public void setLiquidRemaining(int amount){}
	public boolean containsDrink(){return true;}
	public Vector resourceChoices(){return UnderWater.roomResources;}
}
