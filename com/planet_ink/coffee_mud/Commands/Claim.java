package com.planet_ink.coffee_mud.Commands;
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
   Copyright 2015-2017 Bo Zimmerman

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

public class Claim extends StdCommand
{
	public Claim(){
	}

	public Item newItem=null;

	private LandTitle title=null;

	public Room r = null;
	private final String[] access=I(new String[]{"CLAIM"});
	@Override public String[] getAccessWords(){return access;}
	
	
	
	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		newItem = CMClass.getItem("GenTitle");
		Vector<String> origCmds=new XVector<String>(commands);
		final String ID=CMParms.combine(commands,1);
		if(commands.get(commands.size()-1).equalsIgnoreCase("LIST"))
		{
			mob.tell(L("Your claim max is currently "+mob.getMaxClaims()+" and you're using "+mob.getCurrentClaims()));
			return false;
		}
		
		
		r = mob.location();
		final LandTitle T=CMLib.law().getLandTitle(mob.location());
		mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> exclaim(s) that this plot of land belongs to them.^?"));
		int Levels = mob.phyStats().level();
		Levels = Levels/5;
		mob.setMaxClaims(Levels);

		
		
		if (mob.getCurrentClaims() == mob.getMaxClaims())
		{
			mob.tell(L("You do not have any claims left. Your max currently is "+mob.getMaxClaims()+" and you're using "+mob.getCurrentClaims()));
			return false;
		}
		else
		{
			if (T != null)
			{
				if(T.getOwnerName().length()>0)
					mob.tell(L("This property is already claimed."));
			}
			else if(mob.location().canBeClaimed == false)
			{
				mob.setCurrentClaims(mob.getCurrentClaims()+1);
				mob.tell(L("This property is becoming yours. You are using "+mob.getCurrentClaims()+" of "+mob.getMaxClaims()+"."));
				((LandTitle)newItem).setOwnerName(mob.Name());
				((LandTitle)newItem).updateTitle();
				((LandTitle)newItem).updateLot(new XVector(mob.name()));
				r.addEffect(CMClass.getAbility("Prop_RoomForSale(\""+mob.Name()+"/0\""));
				Environmental dest=mob.location();
				r = mob.location();
				((LandTitle)newItem).setLandPropertyID(CMLib.map().getExtendedRoomID(mob.location()));
				((Item)newItem).setReadableText(mob.location().roomID());
				if(!newItem.Name().startsWith("the title to"))
				{
					final List<Room> V=((LandTitle)newItem).getAllTitledRooms();
					if((V.size()<2)
							||(CMLib.map().getArea(((LandTitle)newItem).landPropertyID())!=null))
						newItem.setName("the title to "+((LandTitle)newItem).landPropertyID());
					else
						newItem.setName("the title to rooms around "+CMLib.map().getExtendedRoomID(V.get(0)));
				}
		
				if(dest instanceof Room)
				{
					final Ability A=CMClass.getAbility("Prop_RoomForSale");
					A.setMiscText(mob.Name()+"/");
					((LandTitle)newItem).updateTitle();
					((LandTitle)newItem).updateLot(null);
					((Room)dest).addItem(newItem);
					((Room)dest).addNonUninvokableEffect(A);	

					CMLib.database().DBUpdateRoom(mob.location());
					CMLib.database().DBUpdatePlayer(mob);
					
					
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("Suddenly, @x1 drops from the sky.",newItem.name()));
					return true;
				}
				else
					return false;
			}
			else
			{
				mob.tell(L("This is not a wilderness area and can not be claimed."));
			}
		}
		return false;
		
	}
	
	
	
	



}