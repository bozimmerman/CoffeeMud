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
	
	protected void giveASky()
	{
		skyedYet=true;
		if((rawDoors()[Directions.DOWN]==null)
		&&((domainType()&Room.INDOORS)==0)
		&&(domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		&&(domainType()!=Room.DOMAIN_OUTDOORS_AIR))
		{
			Exit o=(Exit)CMClass.getExit("StdOpenDoorway");
			UnderSaltWaterGrid sky=new UnderSaltWaterGrid();
			sky.setArea(getArea());
			sky.setID("");
			rawDoors()[Directions.DOWN]=sky;
			rawExits()[Directions.DOWN]=o;
			sky.rawDoors()[Directions.UP]=this;
			sky.rawExits()[Directions.UP]=o;
			CMMap.addRoom(sky);
		}
	}
	
	public void clearSky()
	{
		if(!skyedYet) return;
		Room room=rawDoors()[Directions.DOWN];
		if(room==null) return;
		if((room.ID().length()==0)&&(room instanceof UnderSaltWaterGrid))
		{
			rawDoors()[Directions.UP]=null;
			rawExits()[Directions.UP]=null;
			room.rawDoors()[Directions.DOWN]=null;
			room.rawExits()[Directions.DOWN]=null;
			CMMap.delRoom(room);
			skyedYet=false;
		}
	}
	
}
