package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_LotsForSale extends Prop_RoomsForSale
{
	public String ID() { return "Prop_LotsForSale"; }
	public String name(){ return "Putting many rooms up for sale";}
	public Environmental newInstance(){	return new Prop_LotsForSale();}


	private static boolean isCleanRoom(Room fromRoom, Room theRoom)
	{
		if(theRoom==null) return true;

		if((theRoom.roomID().length()>0)
		&&((CoffeeUtensils.getLandTitle(theRoom)==null)
			||(CoffeeUtensils.getLandTitle(theRoom).landOwner().length()>0)))
			return false;

		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room R=theRoom.rawDoors()[d];
			if((R!=null)
			   &&(R!=fromRoom)
			   &&(R.roomID().length()>0)
			   &&((CoffeeUtensils.getLandTitle(R)==null)||(CoffeeUtensils.getLandTitle(R).landOwner().length()>0)))
				return false;
		}
		return true;
	}

	public void updateLot()
	{
		if(!(affected instanceof Room))
			return;
		lastItemNums=updateLotWithThisData((Room)affected,this,true,lastItemNums);
		
		Room R=(Room)affected;
		if(landOwner().length()==0)
		{
			boolean updateExits=false;
			boolean foundOne=false;
			for(int d=0;d<4;d++)
			{
				Room R2=R.rawDoors()[d];
				foundOne=foundOne||(R2!=null);
				if((R2!=null)&&(isCleanRoom(R,R2)))
				{
					R.rawDoors()[d]=null;
					R.rawExits()[d]=null;
					updateExits=true;
					CoffeeUtensils.obliterateRoom(R2);
					R2.getArea().fillInAreaRoom(R2);
				}
			}
			if(!foundOne)
			{
				CoffeeUtensils.obliterateRoom(R);
				return;
			}
			if(updateExits)
				CMClass.DBEngine().DBUpdateExits(R);
		}
		else
		{
			boolean updateExits=false;
			for(int d=0;d<4;d++)
			{
				Room R2=R.getRoomInDir(d);
				if(R2==null)
				{
					R2=CMClass.getLocale(CMClass.className(R));
					R2.setRoomID(CMMap.getOpenRoomID(R.getArea().Name()));
					R2.setArea(R.getArea());
					LandTitle newTitle=CoffeeUtensils.getLandTitle(R);
					if((newTitle!=null)&&(CoffeeUtensils.getLandTitle(R2)==null))
					{
						newTitle=(LandTitle)((Ability)newTitle).copyOf();
						newTitle.setLandOwner("");
						R2.addNonUninvokableEffect((Ability)newTitle);
					}
					R.rawDoors()[d]=R2;
					R.rawExits()[d]=CMClass.getExit("Open");
					R2.rawDoors()[Directions.getOpDirectionCode(d)]=R;
					R2.rawExits()[Directions.getOpDirectionCode(d)]=CMClass.getExit("Open");
					updateExits=true;

					CMClass.DBEngine().DBCreateRoom(R2,CMClass.className(R2));
					CMMap.addRoom(R2);
					colorForSale(R2,true);
					R2.getArea().fillInAreaRoom(R2);
					CMClass.DBEngine().DBUpdateExits(R2);
				}
			}
			if(updateExits)
				CMClass.DBEngine().DBUpdateExits(R);
			R.getArea().fillInAreaRoom(R);
		}
	}
}
