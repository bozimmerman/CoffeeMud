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
public class SaltWaterSurface extends WaterSurface
{
	public String ID(){return "SaltWaterSurface";}
	public SaltWaterSurface()
	{
		super();
	}
	protected String UnderWaterLocaleID(){return "UnderSaltWaterGrid";}


	public int liquidType(){return EnvResource.RESOURCE_SALTWATER;}
	public Vector resourceChoices(){return UnderSaltWater.roomResources;}

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
				   &&((thatSea instanceof UnderSaltWaterGrid)||(thatSea instanceof UnderSaltWaterThinGrid)))
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
		&&((room instanceof UnderSaltWaterGrid)||(room instanceof UnderSaltWaterThinGrid)))
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

}
