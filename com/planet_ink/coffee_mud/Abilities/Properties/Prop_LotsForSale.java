package com.planet_ink.coffee_mud.Abilities.Properties;
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
public class Prop_LotsForSale extends Prop_RoomForSale
{
	public String ID() { return "Prop_LotsForSale"; }
	public String name(){ return "Putting many rooms up for sale";}


	private static boolean isCleanRoom(Room fromRoom, Room theRoom)
	{
		if(theRoom==null) return true;

		if((theRoom.roomID().length()>0)
		&&((CMLib.utensils().getLandTitle(theRoom)==null)
			||(CMLib.utensils().getLandTitle(theRoom).landOwner().length()>0)))
			return false;

		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room R=theRoom.rawDoors()[d];
			if((R!=null)
			   &&(R!=fromRoom)
			   &&(R.roomID().length()>0)
			   &&((CMLib.utensils().getLandTitle(R)==null)||(CMLib.utensils().getLandTitle(R).landOwner().length()>0)))
				return false;
		}
		return true;
	}

	public void updateLot()
	{
		if(!(affected instanceof Room))
			return;
		lastItemNums=updateLotWithThisData((Room)affected,this,true,scheduleReset,lastItemNums);

		Room R=(Room)affected;
		if(landOwner().length()==0)
		{
			boolean updateExits=false;
			boolean foundOne=false;
			for(int d=0;d<4;d++)
			{
				Room R2=R.rawDoors()[d];
				foundOne=foundOne||(R2!=null);
                Exit E=R.rawExits()[d];
				if((R2!=null)&&(isCleanRoom(R,R2)))
				{
					R.rawDoors()[d]=null;
					R.rawExits()[d]=null;
					updateExits=true;
					CMLib.utensils().obliterateRoom(R2);
				}
                else
                if((E!=null)&&(E.hasALock())&&(E.isGeneric()))
                {
                    E.setKeyName("");
                    E.setDoorsNLocks(E.hasADoor(),E.isOpen(),E.defaultsClosed(),false,false,false);
                    updateExits=true;
                    if(R2!=null)
                    {
                        E=R2.rawExits()[Directions.getOpDirectionCode(d)];
                        if((E!=null)&&(E.hasALock())&&(E.isGeneric()))
                        {
                            E.setKeyName("");
                            E.setDoorsNLocks(E.hasADoor(),E.isOpen(),E.defaultsClosed(),false,false,false);
                            CMLib.database().DBUpdateExits(R2);
                            R2.getArea().fillInAreaRoom(R2);
                        }
                    }
                }
			}
			if(!foundOne)
			{
				CMLib.utensils().obliterateRoom(R);
				return;
			}
			if(updateExits)
			{
				CMLib.database().DBUpdateExits(R);
				R.getArea().fillInAreaRoom(R);
			}
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
					R2.setRoomID(CMLib.map().getOpenRoomID(R.getArea().Name()));
					R2.setArea(R.getArea());
					LandTitle newTitle=CMLib.utensils().getLandTitle(R);
					if((newTitle!=null)&&(CMLib.utensils().getLandTitle(R2)==null))
					{
						newTitle=(LandTitle)((Ability)newTitle).copyOf();
						newTitle.setLandOwner("");
						newTitle.setBackTaxes(0);
						R2.addNonUninvokableEffect((Ability)newTitle);
					}
					R.rawDoors()[d]=R2;
					R.rawExits()[d]=CMClass.getExit("Open");
					R2.rawDoors()[Directions.getOpDirectionCode(d)]=R;
					R2.rawExits()[Directions.getOpDirectionCode(d)]=CMClass.getExit("Open");
					updateExits=true;

					CMLib.database().DBCreateRoom(R2,CMClass.className(R2));
					colorForSale(R2,newTitle.rentalProperty(),true);
					R2.getArea().fillInAreaRoom(R2);
					CMLib.database().DBUpdateExits(R2);
				}
			}
			if(updateExits)
			{
				CMLib.database().DBUpdateExits(R);
				R.getArea().fillInAreaRoom(R);
			}
		}
        scheduleReset=false;
	}
}
