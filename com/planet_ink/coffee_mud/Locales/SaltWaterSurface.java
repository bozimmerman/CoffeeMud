package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
public class SaltWaterSurface extends WaterSurface
{
	public SaltWaterSurface()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}

	public Environmental newInstance()
	{
		return new SaltWaterSurface();
	}
	public int liquidType(){return EnvResource.RESOURCE_SALTWATER;}
	public Vector resourceChoices(){return UnderWater.roomResources;}
	
	public void giveASky()
	{
		if(skyedYet) return;
		super.giveASky();
		skyedYet=true;
		if((rawDoors()[Directions.DOWN]==null)
		&&((domainType()&Room.INDOORS)==0)
		&&(domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(domainType()!=Room.DOMAIN_OUTDOORS_AIR))
		{
			Exit o=(Exit)CMClass.getExit("StdOpenDoorway");
			UnderSaltWaterGrid sea=new UnderSaltWaterGrid();
			sea.setArea(getArea());
			sea.setID("");
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
					thatRoom.giveASky();
					thatSea=thatRoom.rawDoors()[Directions.DOWN];
				}
				if((thatSea!=null)&&(thatSea.ID().length()==0)&&(thatSea instanceof UnderSaltWaterGrid))
				{
					sea.rawDoors()[d]=thatSea;
					sea.rawExits()[d]=rawExits()[d];
					thatSea.rawDoors()[Directions.getOpDirectionCode(d)]=sea;
					Exit xo=thatRoom.rawExits()[Directions.getOpDirectionCode(d)];
					if((xo==null)||(xo.hasADoor())) xo=o;
					thatSea.rawExits()[Directions.getOpDirectionCode(d)]=xo;
					((GridLocale)thatSea).clearGrid();
				}
			}
			sea.clearGrid();
			CMMap.addRoom(sea);
		}
	}
	
	public void clearSky()
	{
		if(!skyedYet) return;
		super.clearSky();
		Room room=rawDoors()[Directions.DOWN];
		if(room==null) return;
		if((room.ID().length()==0)&&(room instanceof UnderSaltWaterGrid))
		{
			((UnderSaltWaterGrid)room).clearGrid();
			rawDoors()[Directions.UP]=null;
			rawExits()[Directions.UP]=null;
			room.rawDoors()[Directions.DOWN]=null;
			room.rawExits()[Directions.DOWN]=null;
			CMMap.delRoom(room);
			skyedYet=false;
		}
	}
	
}
