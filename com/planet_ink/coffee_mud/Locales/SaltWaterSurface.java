package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class SaltWaterSurface extends WaterSurface
{
	public String ID(){return "SaltWaterSurface";}
	public SaltWaterSurface()
	{
		super();
	}
	protected String UnderWaterLocaleID(){return "UnderSaltWaterGrid";}


	public int liquidType(){return RawMaterial.RESOURCE_SALTWATER;}
	public Vector resourceChoices(){return UnderSaltWater.roomResources;}

	public void giveASky(int depth)
	{
		if(skyedYet) return;
		if(depth>1000) return;
		super.giveASky(depth+1);
		skyedYet=true;
		
		if((roomID().length()==0)
		&&(getGridParent()!=null)
		&&(getGridParent().roomID().length()==0))
			return;
		
		if((rawDoors()[Directions.DOWN]==null)
		&&((domainType()&Room.INDOORS)==0)
		&&(domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(domainType()!=Room.DOMAIN_OUTDOORS_AIR)
		&&(CMProps.getIntVar(CMProps.SYSTEMI_SKYSIZE)!=0))
		{
			Exit dnE=null;
			Exit upE=CMClass.getExit("StdOpenDoorway");
			if(CMProps.getIntVar(CMProps.SYSTEMI_SKYSIZE)>0)
				dnE=upE;
			else
				dnE=CMClass.getExit("UnseenWalkway");
			GridLocale sea=(GridLocale)CMClass.getLocale(UnderWaterLocaleID());
			sea.setRoomID("");
			sea.setArea(getArea());
			rawDoors()[Directions.DOWN]=sea;
			setRawExit(Directions.DOWN,dnE);
			sea.rawDoors()[Directions.UP]=this;
			sea.setRawExit(Directions.UP,upE);
			for(int d=0;d<4;d++)
			{
				Room thatRoom=rawDoors()[d];
				Room thatSea=null;
				if((thatRoom!=null)&&(getRawExit(d)!=null))
				{
					thatRoom.giveASky(depth+1);
					thatSea=thatRoom.rawDoors()[Directions.DOWN];
				}
				if((thatSea!=null)
				   &&(thatSea.roomID().length()==0)
				   &&((thatSea instanceof UnderSaltWaterGrid)||(thatSea instanceof UnderSaltWaterThinGrid)))
				{
					sea.rawDoors()[d]=thatSea;
					sea.setRawExit(d,getRawExit(d));
					thatSea.rawDoors()[Directions.getOpDirectionCode(d)]=sea;
					if(thatRoom!=null)
					{
						Exit xo=thatRoom.getRawExit(Directions.getOpDirectionCode(d));
						if((xo==null)||(xo.hasADoor())) xo=upE;
						thatSea.setRawExit(Directions.getOpDirectionCode(d),xo);
					}
					((GridLocale)thatSea).clearGrid(null);
				}
			}
			sea.clearGrid(null);
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
			setRawExit(Directions.UP,null);
			room.destroy();
			skyedYet=false;
		}
	}

}
